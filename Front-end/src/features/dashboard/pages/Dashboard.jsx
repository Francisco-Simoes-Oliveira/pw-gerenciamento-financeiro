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

// Mock Data for Income vs Expenses chart
const chartData = [
  { name: "Jan", Receitas: 5000, Despesas: 3200 },
  { name: "Fev", Receitas: 6200, Despesas: 2400 },
  { name: "Mar", Receitas: 7400, Despesas: 4100 },
  { name: "Abr", Receitas: 6900, Despesas: 4300 },
  { name: "Mai", Receitas: 8200, Despesas: 3150 },
  { name: "Jun", Receitas: 7800, Despesas: 3900 },
]

// Mock Data for Categories doughnut chart
const categoryData = [
  { name: "Alimentação", value: 40, color: "#F59E0B" }, // orange
  { name: "Transporte", value: 25, color: "#3B82F6" }, // blue
  { name: "Moradia", value: 20, color: "#EF4444" }, // red
  { name: "Lazer", value: 15, color: "#10B981" }, // emerald
]

// Mock member lists for shared wallets
const familyAvatars = [
  "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&h=100&q=80",
  "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=100&h=100&q=80",
  "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&h=100&q=80",
]

const republicAvatars = [
  "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=100&h=100&q=80",
  "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=100&h=100&q=80",
  "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=100&h=100&q=80",
  "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=100&h=100&q=80",
  "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=100&h=100&q=80",
]

export default function Dashboard() {
  // Mock Date Greeting
  const today = new Date("2026-06-22T00:00:00")
  const options = { weekday: "long", year: "numeric", month: "long", day: "numeric" }
  const dateString = today.toLocaleDateString("pt-BR", options)

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      
      {/* Greeting top section */}
      <div className="flex flex-col space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-foreground sm:text-3xl flex items-center gap-2">
          Olá, Francisco <span className="animate-wiggle">👋</span>
        </h1>
        <p className="text-xs font-semibold text-muted-foreground capitalize">
          {dateString}
        </p>
      </div>

      {/* Top row: Metrics cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Saldo Atual"
          value="R$ 12.450,80"
          change="+2.4%"
          changeType="positive"
          icon={Wallet}
        />
        <StatsCard
          title="Total Receitas"
          value="R$ 8.200,00"
          change="Recebido"
          changeType="positive"
          icon={ArrowUpRight}
        />
        <StatsCard
          title="Total Despesas"
          value="R$ 3.150,00"
          change="Pago"
          changeType="negative"
          icon={ArrowDownRight}
        />
        <StatsCard
          title="Economia do Mês"
          value="R$ 5.050,00"
          change="61.5% do total"
          changeType="positive"
          icon={PiggyBank}
        />
      </div>

      {/* Middle row: Chart & Gamification Level + Shared Wallets */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Income vs Expenses double bar chart */}
        <FinancialCard
          title="Receitas vs Despesas"
          subtitle="Desempenho financeiro nos últimos 6 meses"
          className="lg:col-span-2"
        >
          <div className="h-[280px] w-full mt-4">
            <ResponsiveContainer width="100%" height="100%">
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
            </ResponsiveContainer>
          </div>
        </FinancialCard>

        {/* Gamification Level & Shared Wallets */}
        <div className="flex flex-col gap-6">
          {/* LevelCard gamification area */}
          <LevelCard level={5} rank="Mestre Financeiro" currentXp={750} nextLevelXp={1000} />

          {/* Shared Wallets Card */}
          <FinancialCard
            title="Shared Wallets"
            subtitle="Carteiras ativas com divisão de saldo"
            actions={
              <button className="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-primary border border-primary/20 hover:bg-primary/20 cursor-pointer transition-colors">
                <Plus className="h-4 w-4" />
              </button>
            }
          >
            <div className="flex flex-col gap-3 mt-3">
              <WalletCard
                name="Família Oliveira"
                type="Ativo"
                balance="R$ 4.200,00"
                members={familyAvatars}
              />
              <WalletCard
                name="República"
                type="Standard"
                balance="R$ 1.850,25"
                members={republicAvatars}
              />
            </div>
          </FinancialCard>
        </div>
      </div>

      {/* Bottom row: Recent Transactions & Category Breakdown */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Recent Transactions List Card */}
        <FinancialCard
          title="Recent Transactions"
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
                  <th className="px-4 py-3.5">Data</th>
                  <th className="px-6 py-3.5 text-right">Valor</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 text-xs">
                {/* Transaction 1 */}
                <tr className="hover:bg-slate-50/30 transition-colors">
                  <td className="px-6 py-4 font-semibold text-foreground flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-500/10 text-amber-600">
                      <Receipt className="h-4 w-4" />
                    </div>
                    Pão de Açúcar
                  </td>
                  <td className="px-4 py-4">
                    <CategoryBadge category="Alimentação" />
                  </td>
                  <td className="px-4 py-4 text-muted-foreground font-medium">
                    12 Jun 2024
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-destructive">
                    - R$ 450,20
                  </td>
                </tr>

                {/* Transaction 2 */}
                <tr className="hover:bg-slate-50/30 transition-colors">
                  <td className="px-6 py-4 font-semibold text-foreground flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-600">
                      <Coins className="h-4 w-4" />
                    </div>
                    Salário Mensal
                  </td>
                  <td className="px-4 py-4">
                    <CategoryBadge category="Renda" />
                  </td>
                  <td className="px-4 py-4 text-muted-foreground font-medium">
                    05 Jun 2024
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-emerald-600 dark:text-emerald-400">
                    + R$ 7.500,00
                  </td>
                </tr>

                {/* Transaction 3 */}
                <tr className="hover:bg-slate-50/30 transition-colors">
                  <td className="px-6 py-4 font-semibold text-foreground flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-500/10 text-indigo-600">
                      <Receipt className="h-4 w-4" />
                    </div>
                    Uber Trip
                  </td>
                  <td className="px-4 py-4">
                    <CategoryBadge category="Transporte" />
                  </td>
                  <td className="px-4 py-4 text-muted-foreground font-medium">
                    04 Jun 2024
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-destructive">
                    - R$ 32,50
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </FinancialCard>

        {/* Category Breakdown doughnut chart */}
        <FinancialCard
          title="Gastos por Categoria"
          subtitle="Distribuição percentual mensal de despesas"
        >
          <div className="h-[210px] w-full flex items-center justify-center mt-3 relative">
            <ResponsiveContainer width="100%" height="100%">
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
            </ResponsiveContainer>
            {/* Value in center of Doughnut */}
            <div className="absolute flex flex-col items-center justify-center">
              <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground">Total</span>
              <span className="text-xl font-extrabold text-foreground">100%</span>
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
