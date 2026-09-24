import { Link } from "react-router-dom"
import { ChevronRight } from "lucide-react"
import { cn } from "@/lib/utils"

export default function PageHeader({
  title,
  description,
  breadcrumbs = [],
  actions,
  className,
}) {
  return (
    <div className={cn("flex flex-col gap-2 pb-6 border-b border-border/60 mb-6", className)}>
      {/* Breadcrumbs List */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <nav className="flex items-center gap-1.5 text-xs text-muted-foreground select-none font-medium">
          <Link to="/dashboard" className="hover:text-foreground transition-colors">
            Início
          </Link>
          {breadcrumbs.map((crumb, idx) => (
            <div key={idx} className="flex items-center gap-1.5">
              <ChevronRight className="h-3 w-3 shrink-0" />
              {crumb.path ? (
                <Link to={crumb.path} className="hover:text-foreground transition-colors">
                  {crumb.name}
                </Link>
              ) : (
                <span className="text-foreground/80">{crumb.name}</span>
              )}
            </div>
          ))}
        </nav>
      )}

      {/* Main title block */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-col space-y-1">
          <h1 className="text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
            {title}
          </h1>
          {description && (
            <p className="text-sm text-muted-foreground max-w-2xl">
              {description}
            </p>
          )}
        </div>
        {actions && (
          <div className="flex items-center gap-3 shrink-0 self-start sm:self-center">
            {actions}
          </div>
        )}
      </div>
    </div>
  )
}
