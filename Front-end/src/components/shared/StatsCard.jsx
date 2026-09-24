import { Card, CardContent } from "@/components/ui/card"
import { cn } from "@/lib/utils"
import { ArrowUpRight, ArrowDownRight } from "lucide-react"

export default function StatsCard({
  title,
  value,
  change,
  changeType = "neutral",
  icon: Icon,
  className,
}) {
  return (
    <Card className={cn("overflow-hidden border border-border/80 bg-card hover:shadow-md transition-all duration-300", className)}>
      <CardContent className="p-6">
        <div className="flex items-center justify-between space-y-0">
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
            {title}
          </p>
          {Icon && (
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-secondary/80 text-muted-foreground">
              <Icon className="h-4 w-4" />
            </div>
          )}
        </div>
        <div className="flex items-baseline justify-between mt-3">
          <div className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
            {value}
          </div>
          {change && (
            <div
              className={cn(
                "inline-flex items-center gap-0.5 rounded-full px-1.5 py-0.5 text-[10px] font-semibold",
                changeType === "positive" && "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400",
                changeType === "negative" && "bg-destructive/10 text-destructive",
                changeType === "neutral" && "bg-secondary text-muted-foreground"
              )}
            >
              {changeType === "positive" && <ArrowUpRight className="h-3 w-3" />}
              {changeType === "negative" && <ArrowDownRight className="h-3 w-3" />}
              <span>{change}</span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
