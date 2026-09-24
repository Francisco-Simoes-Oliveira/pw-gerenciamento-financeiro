import { Progress } from "@/components/ui/progress"

export default function XpProgress({ current = 0, total = 100 }) {
  const percentage = Math.round((current / total) * 100)

  return (
    <div className="flex flex-col gap-1.5 w-full">
      <div className="flex items-center justify-between text-xs font-semibold">
        <span className="text-muted-foreground">Progresso de Nível</span>
        <span className="text-foreground">{current} / {total} XP ({percentage}%)</span>
      </div>
      <Progress
        value={percentage}
        className="h-2 w-full bg-secondary dark:bg-muted"
        indicatorClassName="bg-primary shadow-[0_0_8px_rgba(var(--color-primary),0.5)]"
      />
    </div>
  )
}
