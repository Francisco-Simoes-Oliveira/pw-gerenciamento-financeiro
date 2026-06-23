import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Trophy, ShieldAlert, Award } from "lucide-react"
import XpProgress from "./XpProgress"

export default function LevelCard({
  level = 5,
  rank = "Mestre Financeiro",
  currentXp = 750,
  nextLevelXp = 1000,
}) {
  return (
    <Card className="relative overflow-hidden border border-primary/20 bg-gradient-to-br from-card to-primary/5 shadow-md">
      {/* Background decoration shape */}
      <div className="absolute right-[-20px] top-[-20px] flex h-24 w-24 items-center justify-center rounded-full bg-primary/5 dark:bg-primary/10">
        <Award className="h-12 w-12 text-primary/10 dark:text-primary/20" />
      </div>

      <CardHeader className="pb-2">
        <div className="flex items-center gap-2">
          <Trophy className="h-4.5 w-4.5 text-yellow-500 animate-bounce" />
          <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground">
            Status Financeiro
          </CardTitle>
        </div>
      </CardHeader>
      
      <CardContent className="flex flex-col gap-4">
        {/* Level badge & Title */}
        <div className="flex items-center gap-4">
          <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-lg shadow-primary/20">
            <span className="text-xl font-extrabold select-none">L{level}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-lg font-bold text-foreground tracking-tight">
              {rank}
            </span>
            <span className="text-xs text-muted-foreground font-medium">
              Continue registrando suas despesas para subir de nível!
            </span>
          </div>
        </div>

        {/* Progress Bar Widget */}
        <XpProgress current={currentXp} total={nextLevelXp} />
      </CardContent>
    </Card>
  )
}
