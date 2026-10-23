import { BarChart3, CircleDollarSign, Eye, Flag, Lock, Trophy, Users } from "lucide-react"
import PageHeader from "@/components/shared/PageHeader"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { useGamification } from "@/contexts/GamificationContext"

const icons = { FIRST_STEP: Flag, IN_THE_BLACK: CircleDollarSign, TEAM_FINANCES: Users, GOAL_ACHIEVED: Trophy, FINANCIAL_ANALYST: BarChart3, WATCHING_SPENDING: Eye }

export default function Achievements() {
  const { profile, loading } = useGamification()
  const unlocked = profile.achievements.filter((achievement) => achievement.unlocked).length
  const total = profile.achievements.length || 6
  return (
    <div className="mx-auto w-full max-w-6xl space-y-6">
      <PageHeader title="Conquistas e Emblemas" description="Marcos desbloqueados por ações financeiras consistentes." breadcrumbs={[{ name: "Conquistas" }]} />
      <section className="rounded-xl border border-border bg-card p-5 shadow-sm">
        <div className="flex items-center justify-between gap-3"><div><p className="text-sm text-muted-foreground">Progresso do catálogo</p><p className="text-2xl font-bold">{unlocked} de {total}</p></div><Trophy className="h-9 w-9 text-amber-500" /></div>
        <Progress className="mt-4 h-2" value={Math.round(unlocked * 100 / total)} />
      </section>
      {loading ? <p className="text-sm text-muted-foreground">Carregando conquistas...</p> : <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {profile.achievements.map((achievement) => { const Icon = icons[achievement.code] || Trophy; return (
          <article key={achievement.code} className={`relative overflow-hidden rounded-xl border p-5 shadow-sm transition ${achievement.unlocked ? "border-amber-500/30 bg-gradient-to-br from-card to-amber-500/5" : "border-border bg-card opacity-70"}`}>
            <div className={`mb-4 flex h-12 w-12 items-center justify-center rounded-2xl ${achievement.unlocked ? "bg-amber-500/15 text-amber-600" : "bg-muted text-muted-foreground"}`}>{achievement.unlocked ? <Icon className="h-6 w-6" /> : <Lock className="h-5 w-5" />}</div>
            <div className="flex items-start justify-between gap-2"><h2 className="font-bold">{achievement.title}</h2><Badge variant={achievement.unlocked ? "default" : "secondary"}>{achievement.unlocked ? "Desbloqueada" : "Bloqueada"}</Badge></div>
            <p className="mt-2 text-sm text-muted-foreground">{achievement.description}</p>
            {achievement.unlockedAt && <p className="mt-4 text-xs font-medium text-amber-700 dark:text-amber-400">Conquistada em {new Date(achievement.unlockedAt).toLocaleDateString("pt-BR")}</p>}
          </article>
        )})}
      </section>}
    </div>
  )
}
