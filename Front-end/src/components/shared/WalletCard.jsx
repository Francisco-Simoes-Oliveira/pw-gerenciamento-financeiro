import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

export default function WalletCard({
  name,
  type = "Pessoal",
  balance,
  members = [], // Array of avatar image URLs
  className,
}) {
  return (
    <Card className={cn("border border-border bg-card/50 hover:bg-card hover:border-primary/20 transition-all duration-300", className)}>
      <CardContent className="p-4 flex flex-col gap-4">
        {/* Header containing name and type badge */}
        <div className="flex items-center justify-between">
          <span className="text-sm font-semibold text-foreground tracking-tight">
            {name}
          </span>
          <Badge
            variant={type.toLowerCase() === "ativo" ? "success" : "secondary"}
            className="text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5"
          >
            {type}
          </Badge>
        </div>

        {/* Member profile overlap group */}
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <div className="flex -space-x-2 overflow-hidden mr-2">
              {members.slice(0, 3).map((url, idx) => (
                <img
                  key={idx}
                  className="inline-block h-6.5 w-6.5 rounded-full ring-2 ring-card object-cover"
                  src={url}
                  alt={`Membro ${idx + 1}`}
                />
              ))}
              {members.length > 3 && (
                <div className="inline-flex h-6.5 w-6.5 items-center justify-center rounded-full bg-muted text-[9px] font-bold text-muted-foreground ring-2 ring-card">
                  +{members.length - 3}
                </div>
              )}
            </div>
            <span className="text-xs text-muted-foreground font-medium">
              {members.length} {members.length === 1 ? "membro" : "membros"}
            </span>
          </div>

          {/* Balance display */}
          <div className="flex flex-col items-end">
            <span className="text-[10px] text-muted-foreground font-semibold uppercase tracking-wider">
              Saldo Compartilhado
            </span>
            <span className="text-sm font-bold text-primary dark:text-foreground">
              {balance}
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
