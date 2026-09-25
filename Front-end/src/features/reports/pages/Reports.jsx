import { useEffect, useRef, useState } from 'react'
import { Download, FileText, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import walletService from '@/services/walletService'
import { downloadStatement, getStatementReport } from '@/services/reportService'
import useRealtimeEvents from '@/hooks/useRealtimeEvents'

const movement = { INCOME: 'Receita', EXPENSE: 'Despesa', TRANSFER_IN: 'Transferência recebida', TRANSFER_OUT: 'Transferência enviada' }
const status = { PAID: 'Pago', PENDING: 'Pendente', CANCELED: 'Cancelado' }
const inputClass = 'rounded-lg border border-border bg-background px-3 py-2 text-sm w-full'
const pageSize = 25
const dateLabel = (date) => date?.slice(0, 10).split('-').reverse().join('/')
function currentMonth() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const last = new Date(year, now.getMonth() + 1, 0).getDate()
  return { startDate: `${year}-${month}-01`, endDate: `${year}-${month}-${last}` }
}

export default function Reports() {
  const [wallets, setWallets] = useState([])
  const [walletsLoading, setWalletsLoading] = useState(true)
  const [filters, setFilters] = useState(() => ({ walletId: '', ...currentMonth() }))
  const [applied, setApplied] = useState(null)
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [exportError, setExportError] = useState('')
  const [exporting, setExporting] = useState('')
  const [revision, setRevision] = useState(0)
  const [page, setPage] = useState(0)
  const requestId = useRef(0)

  useEffect(() => {
    let active = true
    walletService.getWallets().then((res) => {
      if (!active) return
      const items = res.data.data || []
      setWallets(items)
      if (items.length) {
        const initial = { walletId: items[0].id, ...currentMonth() }
        setFilters(initial)
        setApplied(initial)
      }
    }).catch(() => { if (active) setError('Não foi possível carregar as carteiras. Reabra a página para tentar novamente.') })
      .finally(() => { if (active) setWalletsLoading(false) })
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!applied) return
    const controller = new AbortController()
    const id = ++requestId.current
    setLoading(true)
    setError('')
    setReport(null)
    getStatementReport(applied, controller.signal).then((data) => {
      if (id !== requestId.current || controller.signal.aborted) return
      setReport(data)
      setPage(0)
    }).catch((err) => {
      if (!controller.signal.aborted && id === requestId.current) {
        setError(err.response?.data?.message || 'Não foi possível carregar o extrato. Tente novamente.')
      }
    }).finally(() => {
      if (!controller.signal.aborted && id === requestId.current) setLoading(false)
    })
    return () => controller.abort()
  }, [applied, revision])

  useRealtimeEvents((event) => {
    if (event?.type?.startsWith('TRANSACTION_') && event.walletIds?.includes(applied?.walletId)) {
      setRevision((value) => value + 1)
    }
  })

  function apply(event) {
    event.preventDefault()
    const days = (Date.parse(filters.endDate) - Date.parse(filters.startDate)) / 86400000
    if (!filters.walletId || !Number.isFinite(days) || days < 0 || days >= 366) {
      setError('Selecione uma carteira e um período válido de até 366 dias.')
      return
    }
    setExportError('')
    setApplied({ ...filters })
  }

  async function exportFile(format) {
    setExporting(format)
    setExportError('')
    try { await downloadStatement(applied, format) }
    catch (err) {
      let message = 'Não foi possível exportar o extrato.'
      if (err.response?.data instanceof Blob) {
        try { message = JSON.parse(await err.response.data.text()).message || message } catch { /* Generic message for non-JSON errors. */ }
      }
      setExportError(message)
    } finally { setExporting('') }
  }

  const dirty = applied && Object.keys(filters).some((key) => filters[key] !== applied[key])
  const money = (value) => {
    try { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: report?.currency || 'BRL' }).format(value) }
    catch { return `${Number(value).toFixed(2)} ${report?.currency || ''}` }
  }
  const totalPages = Math.max(1, Math.ceil((report?.entries.length || 0) / pageSize))

  return (
    <div className="mx-auto max-w-7xl space-y-6 pb-10">
      <header>
        <h1 className="flex items-center gap-2 text-2xl font-bold"><FileText aria-hidden="true" /> Relatórios</h1>
        <p className="mt-2 text-muted-foreground">Extrato mensal e por período das suas carteiras, inclusive compartilhadas.</p>
      </header>
      <form onSubmit={apply} className="grid gap-4 rounded-xl border bg-card p-5 sm:grid-cols-2 lg:grid-cols-4">
        <label className="space-y-2 text-sm font-medium">Carteira
          <select required value={filters.walletId} onChange={(e) => setFilters({ ...filters, walletId: e.target.value })} className={inputClass} disabled={walletsLoading}>
            <option value="">Selecione uma carteira</option>
            {wallets.map((wallet) => <option key={wallet.id} value={wallet.id}>{wallet.name}</option>)}
          </select>
        </label>
        <label className="space-y-2 text-sm font-medium">Data inicial
          <input required type="date" min="1900-01-01" max="9998-12-31" value={filters.startDate} onChange={(e) => setFilters({ ...filters, startDate: e.target.value })} className={inputClass} />
        </label>
        <label className="space-y-2 text-sm font-medium">Data final
          <input required type="date" min={filters.startDate || '1900-01-01'} max="9998-12-31" value={filters.endDate} onChange={(e) => setFilters({ ...filters, endDate: e.target.value })} className={inputClass} />
        </label>
        <div className="flex items-end gap-2">
          <Button type="submit" disabled={walletsLoading || !wallets.length}><RefreshCw size={16} /> Consultar</Button>
          <Button type="button" variant="outline" onClick={() => setFilters({ ...filters, ...currentMonth() })}>Mês atual</Button>
        </div>
      </form>
      {dirty && <p role="status" className="text-sm text-muted-foreground">Clique em Consultar para aplicar os filtros alterados.</p>}
      {error && <p role="alert" className="rounded-lg border border-destructive p-4 text-destructive">{error}</p>}
      {(loading || walletsLoading) && <p role="status">Carregando extrato…</p>}
      {!walletsLoading && !wallets.length && !error && <p>Você ainda não tem carteiras disponíveis para consulta.</p>}
      {report && !loading && <>
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="text-lg font-semibold">{report.walletName}</h2>
            <p className="text-sm text-muted-foreground">{dateLabel(report.startDate)} a {dateLabel(report.endDate)} · {report.entries.length} lançamentos</p>
          </div>
          <div className="flex gap-2">
            {['pdf', 'csv'].map((format) => <Button key={format} variant="outline" disabled={!!exporting || !!dirty} onClick={() => exportFile(format)}>
              <Download size={16} /> {exporting === format ? 'Exportando…' : format.toUpperCase()}
            </Button>)}
          </div>
        </div>
        {exportError && <p role="alert" className="text-destructive">{exportError}</p>}
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          {[
            ['Receitas pagas', report.income], ['Despesas pagas', report.expense],
            ['Transferências recebidas', report.transferIn], ['Transferências enviadas', report.transferOut],
            ['Variação realizada', report.netChange],
          ].map(([label, value]) => <div key={label} className="rounded-xl border bg-card p-4">
            <p className="text-sm text-muted-foreground">{label}</p><p className="mt-2 break-words text-xl font-semibold">{money(value)}</p>
          </div>)}
        </div>
        <p className="text-sm text-muted-foreground">Totais de lançamentos pagos, pela data da transação. Pendentes: {report.pendingCount}; cancelados: {report.canceledCount}.
          {' '}A variação do período não inclui saldo inicial e pode diferir do saldo atual da carteira. Os arquivos contêm todos os lançamentos do filtro, não apenas esta página.</p>
        <div className="overflow-x-auto rounded-xl border bg-card">
          <table className="w-full text-left text-sm">
            <caption className="sr-only">Lançamentos do extrato de {report.walletName}</caption>
            <thead className="bg-muted"><tr>{['Data', 'Título / categoria', 'Movimento', 'Situação', 'Valor', 'Impacto realizado'].map((label) => <th scope="col" key={label} className="p-3">{label}</th>)}</tr></thead>
            <tbody>{report.entries.slice(page * pageSize, (page + 1) * pageSize).map((entry) => <tr key={entry.id} className="border-t">
              <td className="whitespace-nowrap p-3">{dateLabel(entry.date)}</td>
              <td className="max-w-xs break-words p-3">{entry.title || 'Sem título'}<span className="block text-xs text-muted-foreground">{entry.category || 'Sem categoria'}</span></td>
              <td className="p-3">{movement[entry.movement]}</td><td className="p-3">{status[entry.status]}</td>
              <td className="whitespace-nowrap p-3 tabular-nums">{money(entry.amount)}</td><td className="whitespace-nowrap p-3 tabular-nums">{money(entry.impact)}</td>
            </tr>)}</tbody>
          </table>
          {!report.entries.length && <p className="p-8 text-center text-muted-foreground">Nenhum lançamento no período selecionado.</p>}
        </div>
        <div className="flex items-center justify-end gap-3">
          <Button variant="outline" disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</Button>
          <span className="text-sm">Página {page + 1} de {totalPages}</span>
          <Button variant="outline" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Próxima</Button>
        </div>
      </>}
    </div>
  )
}
