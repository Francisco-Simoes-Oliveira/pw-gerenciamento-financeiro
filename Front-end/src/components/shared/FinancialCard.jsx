import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card"
import { cn } from "@/lib/utils"

export default function FinancialCard({
  title,
  subtitle,
  actions,
  children,
  className,
  contentClassName,
}) {
  return (
    <Card className={cn("border border-border/80 bg-card hover:shadow-sm transition-all duration-300", className)}>
      {(title || subtitle || actions) && (
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
          <div className="flex flex-col space-y-1">
            {title && (
              <CardTitle className="text-base font-bold tracking-tight text-foreground sm:text-lg">
                {title}
              </CardTitle>
            )}
            {subtitle && (
              <CardDescription className="text-xs text-muted-foreground">
                {subtitle}
              </CardDescription>
            )}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </CardHeader>
      )}
      <CardContent className={cn("pt-0", contentClassName)}>
        {children}
      </CardContent>
    </Card>
  )
}
