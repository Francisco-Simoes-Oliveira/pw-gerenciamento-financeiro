import { Award, CalendarDays, Sparkles, Trophy } from "lucide-react"
import LevelCard from "@/components/shared/LevelCard"
import PageHeader from "@/components/shared/PageHeader"
import { useGamification } from "@/contexts/GamificationContext"

const dateTime = new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" })

export default function Profile() {
  const { profile, loading } = useGamification()
  const user = JSON.parse(localStorage.getItem("usuario") || "{}")
  const unlocked = profile.achievements.filter((achievement) => achievement.unlocked).length

  return (
    <div className="mx-auto w-full max-w-6xl space-y-6">
      <PageHeader title="Perfil do Usuário" description="Seu progresso é calculado a partir de ações reais dentro do FinShare." breadcrumbs={[{ name: "Perfil" }]} />
      <section className="grid gap-6 lg:grid-cols-[1fr_1.4fr]">
        <div className="space-y-6">
          <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
            <div className="flex items-center gap-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-2xl font-bold text-primary">{(user.name || "U").charAt(0).toUpperCase()}</div>
              <div><h2 className="text-xl font-bold">{user.name || "Usuário"}</h2><p className="text-sm text-muted-foreground">{user.email || "Conta autenticada"}</p></div>
            </div>
            <div className="mt-6 grid grid-cols-2 gap-3">
              <div className="rounded-lg bg-secondary/50 p-3"><p className="text-xs text-muted-foreground">XP total</p><p className="mt-1 text-xl font-bold">{profile.totalXp}</p></div>
              <div className="rounded-lg bg-secondary/50 p-3"><p className="text-xs text-muted-foreground">Conquistas</p><p className="mt-1 text-xl font-bold">{unlocked}/{profile.achievements.length}</p></div>
            </div>
          </div>
          <LevelCard level={profile.level} rank={loading ? "Carregando nível..." : profile.rank} currentXp={profile.currentLevelXp} nextLevelXp={profile.xpPerLevel} totalXp={profile.totalXp} />
        </div>
        <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
          <div className="mb-5 flex items-center gap-2"><Sparkles className="h-5 w-5 text-primary" /><h2 className="text-lg font-bold">Histórico recente de XP</h2></div>
          {loading ? <p className="text-sm text-muted-foreground">Carregando progresso...</p> : profile.recentXpEvents.length === 0 ? (
            <div className="flex min-h-56 flex-col items-center justify-center text-center text-muted-foreground"><Trophy className="mb-3 h-9 w-9" /><p className="font-medium">Seu primeiro XP aparecerá aqui.</p><p className="text-sm">Crie uma carteira, registre uma transação ou defina uma meta.</p></div>
          ) : <div className="space-y-3">{profile.recentXpEvents.map((event, index) => (
            <div key={`${event.type}-${event.occurredAt}-${index}`} className="flex items-center gap-3 rounded-lg border border-border p-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-amber-500/10 text-amber-600"><Award className="h-4 w-4" /></div>
              <div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold">{event.label}</p><p className="flex items-center gap-1 text-xs text-muted-foreground"><CalendarDays className="h-3 w-3" />{dateTime.format(new Date(event.occurredAt))}</p></div>
              <span className="text-sm font-bold text-primary">+{event.xp} XP</span>
            </div>
          ))}</div>}
        </div>
      </section>
    </div>
  )
}
