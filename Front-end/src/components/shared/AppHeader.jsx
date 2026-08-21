import { Bell, Search, HelpCircle, Settings, LogOut, User, Menu } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { Input } from "@/components/ui/input"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Badge } from "@/components/ui/badge"

export default function AppHeader({ isMobile, onMenuToggle }) {
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem("usuario") || "{}")
  const userName = user.name || "Usuário"
  const userEmail = user.email || ""
  const userInitial = userName.charAt(0).toUpperCase()

  return (
    <header className="flex h-16 w-full items-center justify-between border-b border-border bg-card/85 backdrop-blur-sm px-4 md:px-6 z-30">
      
      {/* Left section: Hamburger on mobile, else empty */}
      <div className="flex items-center gap-1.5 flex-1 max-w-md">
        {isMobile && (
          <button
            onClick={onMenuToggle}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer transition-colors outline-none shrink-0"
          >
            <Menu className="h-5 w-5" />
          </button>
        )}
        
        {/* Search Input Box */}
        <div className="relative w-full">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            type="text"
            placeholder="Search institutional data..."
            className="pl-9 h-9 w-full bg-slate-50/50 dark:bg-slate-950/20 border-border focus-visible:bg-background transition-all"
          />
        </div>
      </div>

      {/* Action Utilities Buttons */}
      <div className="flex items-center gap-2 md:gap-4 ml-4">
        {/* Help Circle Button (hidden on mobile) */}
        {!isMobile && (
          <button className="flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer transition-colors outline-none">
            <HelpCircle className="h-4.5 w-4.5" />
          </button>
        )}

        {/* Notifications Icon Button */}
        <button className="relative flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer transition-colors outline-none">
          <Bell className="h-4.5 w-4.5" />
          <Badge className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center p-0 text-[9px] font-bold bg-primary border-2 border-card">
            3
          </Badge>
        </button>

        {/* Quick Settings Icon Button (hidden on mobile) */}
        {!isMobile && (
          <button
            onClick={() => navigate("/settings")}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-muted hover:text-foreground cursor-pointer transition-colors outline-none"
          >
            <Settings className="h-4.5 w-4.5" />
          </button>
        )}

        {/* Separator Divider (hidden on mobile) */}
        {!isMobile && <div className="h-6 w-px bg-border" />}

        {/* User Account Avatar Dropdown */}
        <DropdownMenu>
          <DropdownMenuTrigger>
            <div className="flex items-center gap-2 cursor-pointer outline-none">
              <div className="flex h-8 w-8 items-center justify-center rounded-full border border-border bg-primary/10 text-xs font-semibold text-primary shadow-sm transition-transform hover:scale-105" aria-label={userName}>
                {userInitial}
              </div>
            </div>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="right" className="w-52">
            <DropdownMenuLabel>
              <div className="flex flex-col">
                <span className="font-semibold text-foreground text-xs">{userName}</span>
                {userEmail && <span className="text-[10px] text-muted-foreground font-medium">{userEmail}</span>}
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => navigate("/profile")}>
              <User className="mr-2 h-4 w-4 text-muted-foreground" />
              <span>Ver Perfil</span>
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => navigate("/settings")}>
              <Settings className="mr-2 h-4 w-4 text-muted-foreground" />
              <span>Configurações</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem 
              onClick={() => {
                localStorage.removeItem("app-token")
                localStorage.removeItem("usuario")
                navigate("/login")
              }} 
              className="text-destructive hover:bg-destructive/10"
            >
              <LogOut className="mr-2 h-4 w-4" />
              <span>Sair da Conta</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
