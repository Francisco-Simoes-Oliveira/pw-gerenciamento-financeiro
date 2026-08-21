import { useLocation, useNavigate } from "react-router-dom"
import { cn } from "@/lib/utils"
import {
  Home,
  Wallet,
  ArrowUpDown,
  Tag,
  Target,
  Trophy,
  Settings,
  User,
  ChevronLeft,
  ChevronRight,
  TrendingUp
} from "lucide-react"
import { Tooltip } from "@/components/ui/tooltip"

export default function AppSidebar({ collapsed, setCollapsed }) {
  const location = useLocation()
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem("usuario") || "{}")
  const userName = user.name || "Usuário"
  const userInitial = userName.charAt(0).toUpperCase()

  const menuItems = [
    { name: "Dashboard", path: "/dashboard", icon: Home },
    { name: "Wallets", path: "/wallets", icon: Wallet },
    { name: "Transactions", path: "/transactions", icon: ArrowUpDown },
    { name: "Categories", path: "/categories", icon: Tag },
    { name: "Goals", path: "/goals", icon: Target },
    { name: "Achievements", path: "/achievements", icon: Trophy },
    { name: "Settings", path: "/settings", icon: Settings },
    { name: "Profile", path: "/profile", icon: User },
  ]

  // Mocked level data for design integration
  const levelData = {
    level: 5,
    rank: "Mestre Financeiro",
    xp: 750,
    nextXp: 1000,
  }

  const handleNavigation = (path) => {
    navigate(path)
  }

  return (
    <aside
      className={cn(
        "relative flex h-full flex-col border-r border-border bg-card text-card-foreground transition-all duration-300 ease-in-out select-none z-40",
        collapsed ? "w-[72px]" : "w-[260px]"
      )}
    >
      {/* Brand logo section */}
      <div className="flex h-16 items-center justify-between px-4 border-b border-border">
        <div className="flex items-center gap-2.5 overflow-hidden">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-md shadow-primary/20">
            <TrendingUp className="h-5 w-5" />
          </div>
          {!collapsed && (
            <div className="flex flex-col animate-in fade-in duration-200">
              <span className="text-base font-bold tracking-tight text-foreground">
                FinShare
              </span>
              <span className="text-[10px] uppercase font-semibold tracking-wider text-muted-foreground">
                Institutional Grade
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Navigation menu list */}
      <nav className="flex-1 space-y-1.5 px-3 py-4 overflow-y-auto">
        {menuItems.map((item) => {
          const isActive = location.pathname === item.path
          const Icon = item.icon

          const buttonContent = (
            <button
              onClick={() => handleNavigation(item.path)}
              className={cn(
                "flex w-full items-center gap-3.5 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200 cursor-pointer group outline-none",
                isActive
                  ? "bg-primary text-primary-foreground shadow-md shadow-primary/10"
                  : "text-muted-foreground hover:bg-secondary/60 hover:text-foreground"
              )}
            >
              <Icon
                className={cn(
                  "h-4 w-4 shrink-0 transition-transform group-hover:scale-105",
                  isActive ? "text-primary-foreground" : "text-muted-foreground group-hover:text-foreground"
                )}
              />
              {!collapsed && (
                <span className="truncate text-[13px] animate-in fade-in duration-200">
                  {item.name}
                </span>
              )}
            </button>
          )

          if (collapsed) {
            return (
              <Tooltip key={item.name} content={item.name} delay={50}>
                <div className="w-full flex justify-center">{buttonContent}</div>
              </Tooltip>
            )
          }

          return <div key={item.name}>{buttonContent}</div>
        })}
      </nav>

      {/* User profile & Gamification sidebar bottom footer */}
      <div className="border-t border-border bg-slate-50/50 p-4 dark:bg-slate-950/20">
        {!collapsed ? (
          <div className="flex flex-col gap-3 animate-in fade-in duration-300">
            {/* Level status */}
            <div className="flex flex-col gap-1 rounded-lg border border-border bg-card p-2.5 shadow-sm">
              <div className="flex items-center justify-between text-[11px] font-semibold">
                <span className="text-primary uppercase tracking-wide">Nível {levelData.level}</span>
                <span className="text-muted-foreground">{levelData.xp} / {levelData.nextXp} XP</span>
              </div>
              <div className="relative h-1.5 w-full overflow-hidden rounded-full bg-secondary/80">
                <div
                  className="h-full bg-primary transition-all duration-500"
                  style={{ width: `${(levelData.xp / levelData.nextXp) * 100}%` }}
                />
              </div>
              <span className="text-[10px] text-muted-foreground text-center font-medium">
                {levelData.rank}
              </span>
            </div>

            {/* Profile trigger */}
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-border bg-primary/10 text-sm font-semibold text-primary shadow-sm" aria-label={userName}>
                {userInitial}
              </div>
              <div className="flex flex-col overflow-hidden">
                <span className="truncate text-xs font-semibold text-foreground">
                  {userName}
                </span>
                <span className="truncate text-[10px] text-muted-foreground">
                  Premium Member
                </span>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-4">
            <Tooltip content={`Nível ${levelData.level}: ${levelData.rank}`}>
              <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[10px] font-bold text-primary border border-primary/20">
                L{levelData.level}
              </div>
            </Tooltip>
            <Tooltip content={userName}>
              <div
                className="flex h-8 w-8 items-center justify-center rounded-full border border-border bg-primary/10 text-xs font-semibold text-primary shadow-sm cursor-pointer"
                onClick={() => handleNavigation("/profile")}
              >
                {userInitial}
              </div>
            </Tooltip>
          </div>
        )}
      </div>

      {/* Collapse collapse toggle toggle button */}
      <button
        onClick={() => setCollapsed(!collapsed)}
        className="absolute -right-3 top-20 flex h-6 w-6 items-center justify-center rounded-full border border-border bg-card shadow-sm hover:bg-accent text-muted-foreground hover:text-foreground cursor-pointer z-50 transition-colors"
      >
        {collapsed ? <ChevronRight className="h-3.5 w-3.5" /> : <ChevronLeft className="h-3.5 w-3.5" />}
      </button>
    </aside>
  )
}
