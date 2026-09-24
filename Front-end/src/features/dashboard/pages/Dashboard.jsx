import { useState, useEffect } from "react"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts"
import {
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  Coins,
  TrendingUp,
  Receipt,
  PiggyBank,
  Plus
} from "lucide-react"

import StatsCard from "@/components/shared/StatsCard"
import FinancialCard from "@/components/shared/FinancialCard"
import WalletCard from "@/components/shared/WalletCard"
import CategoryBadge from "@/components/shared/CategoryBadge"
import LevelCard from "@/components/shared/LevelCard"

import dashboardService from "@/services/dashboardService"
import walletService from "@/services/walletService"
import { toast } from "sonner"

export default function Dashboard() {
  const [summary, setSummary] = useState({
    currentBalance: 0,
    monthlyIncome: 0,
    monthlyExpense: 0,
    monthlyTransfer: 0
  })
  const [transactions, setTransactions] = useState([])
  const [chartData, setChartData] = useState([])
  const [categoryData, setCategoryData] = useState([])
  const [sharedWallets, setSharedWallets] = useState([])
  
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true)
        
        // Fetch real API data
        const summaryRes = await dashboardService.getDashboardSummary()
        if (summaryRes.data?.success) {
          setSummary(summaryRes.data.data)
        }

        const monthlyRes = await dashboardService.getMonthly()
        if (monthlyRes.data?.success) {
          setChartData((monthlyRes.data.data || []).map((item) => ({
            name: item.month,
            Receitas: Number(item.income || 0),
            Despesas: Number(item.expense || 0),
          })))
        }

        const categoryRes = await dashboardService.getExpensesByCategory()
        if (categoryRes.data?.success) {
          setCategoryData((categoryRes.data.data || []).map((item, index) => ({
            name: item.category,
            value: item.percentage || 0,
            color: ["#1E3A8A", "#B91C1C", "#059669", "#D97706"][index % 4],
          })))
        }

        const statementRes = await dashboardService.getStatement({ page: 0, size: 5 })
        if (statementRes.data?.success) {
          setTransactions(statementRes.data.data.content || [])
        }

        const walletsRes = await walletService.getWallets()
        if (walletsRes.data?.success) {
          setSharedWallets(walletsRes.data.data || [])
        }

      } catch (error) {
        console.error("Dashboard error:", error)
        toast.error("Erro ao carregar dados do dashboard.")
      } finally {
        setLoading(false)
      }
    }
    loadData()
  }, [])

  // Mock Date Greeting
  const today = new Date("2026-06-22T00:00:00")
  const options = { weekday: "long", year: "numeric", month: "long", day: "numeric" }
  const dateString = today.toLocaleDateString("pt-BR", options)

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
  }

  // Get user name from local storage
  const user = JSON.parse(localStorage.getItem('usuario') || '{"name": "Usuário"}')
  const firstName = user.name.split(' ')[0]

  // Calculate savings percentage
  const totalIncome = summary.monthlyIncome || 0
  const totalExpense = summary.monthlyExpense || 0
  const savings = totalIncome - totalExpense
  const savingsPercent = totalIncome > 0 ? ((savings / totalIncome) * 100).toFixed(1) : 0

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      
      {/* Greeting top section */}
      <div className="flex flex-col space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-foreground sm:text-3xl flex items-center gap-2">
          Olá, {firstName} <span className="animate-wiggle">👋</span>
        </h1>
        <p className="text-xs font-semibold text-muted-foreground capitalize">
          {dateString}
        </p>
      </div>

      {/* Top row: Metrics cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Saldo Atual"
          value={loading ? "Carregando..." : formatCurrency(summary.currentBalance)}
          change="Ativo"
          changeType="positive"
          icon={Wallet}
        />
        <StatsCard
          title="Total Receitas"
          value={loading ? "Carregando..." : formatCurrency(summary.monthlyIncome)}
          change="No mês"
          changeType="positive"
          icon={ArrowUpRight}
        />
        <StatsCard
          title="Total Despesas"
          value={loading ? "Carregando..." : formatCurrency(summary.monthlyExpense)}
          change="No mês"
          changeType="negative"
          icon={ArrowDownRight}
        />
        <StatsCard
          title="Economia do Mês"
          value={loading ? "Carregando..." : formatCurrency(savings)}
          change={`${savingsPercent}% do total`}
          changeType={savings > 0 ? "positive" : "negative"}
          icon={PiggyBank}
        />
      </div>

      {/* Middle row: Chart & Gamification Level + Shared Wallets */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Income vs Expenses double bar chart */}
        <FinancialCard
          title="Receitas vs Despesas"
          subtitle="Dados reais disponíveis no relatório"
          className="lg:col-span-2"
        >
          <div className="h-70 w-full mt-4">
            {chartData.length === 0 ? <div className="flex h-full items-center justify-center text-sm text-muted-foreground">Ainda não existem dados mensais suficientes.</div> : <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F1F5F9" />
                <XAxis dataKey="name" stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
                <ChartTooltip
                  cursor={{ fill: "#F8FAFC" }}
                  content={({ active, payload }) => {
                    if (active && payload && payload.length) {
                      return (
                        <div className="rounded-lg border border-border bg-card p-2.5 shadow-sm text-xs">
                          <p className="font-bold text-foreground mb-1">{payload[0].payload.name}</p>
                          <p className="text-primary font-semibold">Receitas: R$ {payload[0].value}</p>
                          <p className="text-destructive font-semibold">Despesas: R$ {payload[1].value}</p>
                        </div>
                      )
                    }
                    return null
                  }}
                />
                <Bar dataKey="Receitas" fill="#1E3A8A" radius={[4, 4, 0, 0]} maxBarSize={30} />
                <Bar dataKey="Despesas" fill="#B91C1C" radius={[4, 4, 0, 0]} maxBarSize={30} />
              </BarChart>
            </ResponsiveContainer>}
          </div>
        </FinancialCard>

        {/* Gamification Level & Shared Wallets */}
        <div className="flex flex-col gap-6">
          {/* LevelCard gamification area */}
          <LevelCard level={5} rank="Mestre Financeiro" currentXp={750} nextLevelXp={1000} />

          {/* Shared Wallets Card */}
          <FinancialCard
            title="Carteiras Compartilhadas"
            subtitle="Carteiras retornadas pela API"
            actions={
              <button className="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-primary border border-primary/20 hover:bg-primary/20 cursor-pointer transition-colors">
                <Plus className="h-4 w-4" />
              </button>
            }
          >
            <div className="flex flex-col gap-3 mt-3">
              {sharedWallets.length === 0 ? <p className="text-sm text-muted-foreground">Você ainda não possui carteiras.</p> : sharedWallets.map((wallet) => (
                <WalletCard
                  key={wallet.id}
                  name={wallet.name}
                  type={wallet.ownerId === user?.id ? "Pessoal" : "Compartilhada"}
                  balance={formatCurrency(wallet.balance || 0)}
                  members={[]}
                />
              ))}
            </div>
          </FinancialCard>
        </div>
      </div>

      {/* Bottom row: Recent Transactions & Category Breakdown */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Recent Transactions List Card */}
        <FinancialCard
          title="Transações Recentes"
          subtitle="Últimos registros adicionados ao sistema"
          className="lg:col-span-2"
          contentClassName="p-0"
        >
          <div className="overflow-x-auto min-w-full">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border bg-slate-50/50 dark:bg-slate-950/20 text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                  <th className="px-6 py-3.5">Descrição</th>
                  <th className="px-4 py-3.5">Categoria</th>
                  <th className="px-6 py-3.5 text-right">Valor</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 text-xs">
                {loading ? (
                  <tr>
                    <td colSpan="3" className="px-6 py-8 text-center text-muted-foreground">
                      Carregando transações...
                    </td>
                  </tr>
                ) : transactions.length === 0 ? (
                  <tr>
                    <td colSpan="3" className="px-6 py-8 text-center text-muted-foreground">
                      Nenhuma transação encontrada.
                    </td>
                  </tr>
                ) : (
                  transactions.map((tx) => (
                    <tr key={tx.transactionId} className="hover:bg-slate-50/30 transition-colors">
                      <td className="px-6 py-4 font-semibold text-foreground flex items-center gap-3">
                        <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${tx.type === 'INCOME' ? 'bg-emerald-500/10 text-emerald-600' : tx.type === 'EXPENSE' ? 'bg-amber-500/10 text-amber-600' : 'bg-indigo-500/10 text-indigo-600'}`}>
                          {tx.type === 'INCOME' ? <Coins className="h-4 w-4" /> : <Receipt className="h-4 w-4" />}
                        </div>
                        {tx.walletName || 'Transação'}
                      </td>
                      <td className="px-4 py-4">
                        <CategoryBadge category={tx.categoryName || 'Geral'} color={tx.categoryColor} />
                      </td>
                      <td className={`px-6 py-4 text-right font-bold ${tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-destructive'}`}>
                        {tx.type === 'INCOME' ? '+' : '-'} {formatCurrency(tx.amount)}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </FinancialCard>

        {/* Category Breakdown doughnut chart */}
        <FinancialCard
          title="Gastos por Categoria"
          subtitle="Distribuição calculada a partir do extrato real"
        >
          <div className="h-52.5 w-full flex items-center justify-center mt-3 relative">
            {categoryData.length === 0 ? <div className="flex h-full items-center justify-center text-sm text-muted-foreground">Ainda não existem movimentações.</div> : <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {categoryData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <ChartTooltip
                  content={({ active, payload }) => {
                    if (active && payload && payload.length) {
                      return (
                        <div className="rounded-lg border border-border bg-card px-2.5 py-1.5 shadow-sm text-xs font-semibold">
                          {payload[0].name}: {payload[0].value}%
                        </div>
                      )
                    }
                    return null
                  }}
                />
              </PieChart>
            </ResponsiveContainer>}
            {/* Value in center of Doughnut */}
            <div className="absolute flex flex-col items-center justify-center">
              <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground">Total</span>
              <span className="text-xl font-extrabold text-foreground">{categoryData.length ? "100%" : "-"}</span>
            </div>
          </div>

          {/* Doughnut Labels list */}
          <div className="grid grid-cols-2 gap-2.5 mt-3 border-t border-border pt-4">
            {categoryData.map((item) => (
              <div key={item.name} className="flex items-center gap-2">
                <span className="h-2 w-2 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                <span className="text-[11px] font-medium text-foreground truncate">{item.name}</span>
                <span className="text-[11px] font-bold text-muted-foreground ml-auto">{item.value}%</span>
              </div>
            ))}
          </div>
        </FinancialCard>
      </div>

    </div>
  )
}
