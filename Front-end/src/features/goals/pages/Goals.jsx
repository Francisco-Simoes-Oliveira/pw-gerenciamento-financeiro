import { useEffect, useMemo, useState } from "react"
import { CalendarDays, CheckCircle2, Loader2, Plus, Target, Trash2 } from "lucide-react"
import PageHeader from "@/components/shared/PageHeader"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { toast } from "sonner"
import { goalService } from "@/services/goalService"
import walletService from "@/services/walletService"
import { notifyGamificationUpdate } from "@/services/gamificationService"

const statusLabels = { ACTIVE: "Em andamento", COMPLETED: "Concluída", CANCELED: "Cancelada" }

export default function Goals() {
  const [goals, setGoals] = useState([])
  const [wallets, setWallets] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [open, setOpen] = useState(false)
  const [progressValues, setProgressValues] = useState({})
  const [form, setForm] = useState({ walletId: "", name: "", targetAmount: "", currentAmount: "0", deadline: "" })

  async function loadData() {
    try {
      setLoading(true)
      const [goalsResponse, walletsResponse] = await Promise.all([goalService.list(), walletService.getWallets()])
      const loadedGoals = goalsResponse.data?.data || []
      const loadedWallets = walletsResponse.data?.data || []
      setGoals(loadedGoals)
      setWallets(loadedWallets)
      setProgressValues(Object.fromEntries(loadedGoals.map((goal) => [goal.id, String(goal.currentAmount)])))
      setForm((current) => ({ ...current, walletId: current.walletId || loadedWallets[0]?.id || "" }))
    } catch (error) {
      console.error(error)
      toast.error("Não foi possível carregar suas metas.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [])

  const summary = useMemo(() => ({
    active: goals.filter((goal) => goal.status === "ACTIVE").length,
    completed: goals.filter((goal) => goal.status === "COMPLETED").length,
  }), [goals])

  async function createGoal(event) {
    event.preventDefault()
    if (!form.walletId || !form.name.trim() || Number(form.targetAmount) <= 0 || Number(form.currentAmount) < 0) {
      toast.error("Preencha carteira, nome e valores válidos.")
      return
    }
    try {
      setSaving(true)
      await goalService.create({
        walletId: form.walletId,
        name: form.name.trim(),
        targetAmount: Number(form.targetAmount),
        currentAmount: Number(form.currentAmount || 0),
        deadline: form.deadline || null,
      })
      toast.success("Meta criada com sucesso.")
      setOpen(false)
      setForm({ walletId: form.walletId, name: "", targetAmount: "", currentAmount: "0", deadline: "" })
      await loadData()
      notifyGamificationUpdate()
    } catch (error) {
      toast.error(error.response?.data?.message || "Não foi possível criar a meta.")
    } finally {
      setSaving(false)
    }
  }

  async function saveProgress(goal) {
    const value = Number(progressValues[goal.id])
    if (!Number.isFinite(value) || value < 0) {
      toast.error("Informe um valor acumulado válido.")
      return
    }
    try {
      setSaving(true)
      const response = await goalService.updateProgress(goal.id, value)
      const completed = response.data?.data?.status === "COMPLETED"
      toast.success(completed ? "Meta concluída! +200 XP e nova conquista." : "Progresso atualizado.")
      await loadData()
      notifyGamificationUpdate()
    } catch (error) {
      toast.error(error.response?.data?.message || "Não foi possível atualizar a meta.")
    } finally {
      setSaving(false)
    }
  }

  async function cancelGoal(goal) {
    try {
      setSaving(true)
      await goalService.cancel(goal.id)
      toast.success("Meta cancelada.")
      await loadData()
    } catch (error) {
      toast.error(error.response?.data?.message || "Não foi possível cancelar a meta.")
    } finally {
      setSaving(false)
    }
  }

  const currency = (value, code = "BRL") => new Intl.NumberFormat("pt-BR", { style: "currency", currency: code }).format(value || 0)

  return (
    <div className="mx-auto w-full max-w-6xl space-y-6">
      <PageHeader title="Metas Financeiras" description="Defina objetivos, acompanhe o valor acumulado e ganhe XP ao concluí-los." breadcrumbs={[{ name: "Metas" }]} />
      <section className="grid gap-4 sm:grid-cols-3">
        <div className="rounded-xl border border-border bg-card p-5"><p className="text-sm text-muted-foreground">Total de metas</p><p className="mt-2 text-3xl font-bold">{goals.length}</p></div>
        <div className="rounded-xl border border-border bg-card p-5"><p className="text-sm text-muted-foreground">Em andamento</p><p className="mt-2 text-3xl font-bold text-primary">{summary.active}</p></div>
        <div className="rounded-xl border border-border bg-card p-5"><p className="text-sm text-muted-foreground">Concluídas</p><p className="mt-2 text-3xl font-bold text-emerald-600">{summary.completed}</p></div>
      </section>

      <div className="flex items-center justify-between gap-4"><div><h2 className="text-lg font-bold">Seus objetivos</h2><p className="text-sm text-muted-foreground">O progresso é informado por você e fica associado à carteira selecionada.</p></div>
        <Dialog open={open} onOpenChange={setOpen}><DialogTrigger asChild><Button disabled={wallets.length === 0}><Plus className="mr-2 h-4 w-4" />Nova meta</Button></DialogTrigger>
          <DialogContent><form onSubmit={createGoal}><DialogHeader><DialogTitle>Criar meta financeira</DialogTitle><DialogDescription>Uma meta concluída concede 200 XP. A primeira criação concede 30 XP.</DialogDescription></DialogHeader>
            <div className="grid gap-4 py-4">
              <div><Label htmlFor="goal-wallet">Carteira</Label><select id="goal-wallet" className="mt-1 flex h-10 w-full rounded-md border border-input bg-background px-3 text-sm" value={form.walletId} onChange={(event) => setForm({ ...form, walletId: event.target.value })}>{wallets.map((wallet) => <option key={wallet.id} value={wallet.id}>{wallet.name}</option>)}</select></div>
              <div><Label htmlFor="goal-name">Nome</Label><Input id="goal-name" className="mt-1" maxLength={100} placeholder="Ex: Reserva de emergência" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></div>
              <div className="grid grid-cols-2 gap-3"><div><Label htmlFor="goal-target">Valor-alvo</Label><Input id="goal-target" className="mt-1" type="number" min="0.01" step="0.01" value={form.targetAmount} onChange={(event) => setForm({ ...form, targetAmount: event.target.value })} /></div><div><Label htmlFor="goal-current">Já acumulado</Label><Input id="goal-current" className="mt-1" type="number" min="0" step="0.01" value={form.currentAmount} onChange={(event) => setForm({ ...form, currentAmount: event.target.value })} /></div></div>
              <div><Label htmlFor="goal-deadline">Prazo opcional</Label><Input id="goal-deadline" className="mt-1" type="date" min={new Date().toISOString().split("T")[0]} value={form.deadline} onChange={(event) => setForm({ ...form, deadline: event.target.value })} /></div>
            </div><DialogFooter><Button type="submit" disabled={saving}>{saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}Criar meta</Button></DialogFooter>
          </form></DialogContent></Dialog>
      </div>

      {loading ? <div className="py-12 text-center"><Loader2 className="mx-auto h-6 w-6 animate-spin text-primary" /></div> : goals.length === 0 ? (
        <div className="flex min-h-64 flex-col items-center justify-center rounded-xl border border-dashed border-border bg-card p-8 text-center"><Target className="mb-3 h-10 w-10 text-primary" /><h2 className="font-bold">Crie sua primeira meta</h2><p className="mt-1 text-sm text-muted-foreground">Escolha uma carteira, defina o valor-alvo e acompanhe sua evolução.</p></div>
      ) : <section className="grid gap-4 lg:grid-cols-2">{goals.map((goal) => (
        <article key={goal.id} className={`rounded-xl border bg-card p-5 shadow-sm ${goal.status === "COMPLETED" ? "border-emerald-500/30" : "border-border"}`}>
          <div className="flex items-start justify-between gap-3"><div className="flex min-w-0 items-center gap-3"><div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${goal.status === "COMPLETED" ? "bg-emerald-500/10 text-emerald-600" : "bg-primary/10 text-primary"}`}>{goal.status === "COMPLETED" ? <CheckCircle2 className="h-5 w-5" /> : <Target className="h-5 w-5" />}</div><div className="min-w-0"><h3 className="truncate font-bold">{goal.name}</h3><p className="truncate text-xs text-muted-foreground">{goal.walletName} · {statusLabels[goal.status]}</p></div></div>{goal.status === "ACTIVE" && <Button variant="ghost" size="icon" title="Cancelar meta" onClick={() => cancelGoal(goal)} disabled={saving}><Trash2 className="h-4 w-4 text-destructive" /></Button>}</div>
          <div className="mt-5 flex items-end justify-between gap-3"><div><p className="text-xs text-muted-foreground">Valor acumulado</p><p className="text-lg font-bold">{currency(goal.currentAmount, goal.currency)} <span className="text-sm font-medium text-muted-foreground">/ {currency(goal.targetAmount, goal.currency)}</span></p></div><span className="text-sm font-bold text-primary">{goal.progressPercentage}%</span></div>
          <Progress className="mt-2 h-2" value={goal.progressPercentage} />
          {goal.deadline && <p className="mt-3 flex items-center gap-1 text-xs text-muted-foreground"><CalendarDays className="h-3.5 w-3.5" />Prazo: {new Date(`${goal.deadline}T12:00:00`).toLocaleDateString("pt-BR")}</p>}
          {goal.status === "ACTIVE" && <div className="mt-4 flex gap-2 border-t border-border pt-4"><Input type="number" min="0" step="0.01" aria-label={`Valor acumulado em ${goal.name}`} value={progressValues[goal.id] ?? ""} onChange={(event) => setProgressValues({ ...progressValues, [goal.id]: event.target.value })} /><Button variant="outline" onClick={() => saveProgress(goal)} disabled={saving}>Salvar progresso</Button></div>}
          {goal.status === "COMPLETED" && <p className="mt-4 rounded-lg bg-emerald-500/10 p-3 text-sm font-semibold text-emerald-700 dark:text-emerald-400">Objetivo concluído · +200 XP</p>}
        </article>
      ))}</section>}
    </div>
  )
}
