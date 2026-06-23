import { Outlet } from "react-router-dom"
import AppSidebar from "../shared/AppSidebar"
import AppHeader from "../shared/AppHeader"
import { useState, useEffect } from "react"
import { Sheet, SheetContent } from "@/components/ui/sheet"

export default function DashboardLayout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
  const [isMobile, setIsMobile] = useState(false)

  useEffect(() => {
    const handleResize = () => {
      const width = window.innerWidth
      const mobile = width < 768
      const tablet = width >= 768 && width < 1024
      
      setIsMobile(mobile)
      
      if (mobile) {
        setSidebarCollapsed(true)
      } else if (tablet) {
        setSidebarCollapsed(true)
      } else {
        setSidebarCollapsed(false)
      }
    }

    handleResize()
    window.addEventListener("resize", handleResize)
    return () => window.removeEventListener("resize", handleResize)
  }, [])

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-background text-foreground antialiased font-sans">
      {/* Sidebar for Desktop & Tablet viewports */}
      {!isMobile && (
        <AppSidebar collapsed={sidebarCollapsed} setCollapsed={setSidebarCollapsed} />
      )}

      {/* Sheet Overlay sidebar for Mobile viewports */}
      {isMobile && (
        <Sheet open={isMobileMenuOpen} onOpenChange={setIsMobileMenuOpen}>
          <SheetContent side="left" className="p-0 w-[260px] border-r border-border bg-card">
            <div className="h-full relative [&>button]:hidden">
              <AppSidebar collapsed={false} setCollapsed={() => setIsMobileMenuOpen(false)} />
            </div>
          </SheetContent>
        </Sheet>
      )}

      <div className="flex flex-1 flex-col overflow-hidden min-w-0">
        <AppHeader
          isMobile={isMobile}
          onMenuToggle={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        />
        <main className="flex-1 overflow-y-auto bg-slate-50/50 p-4 md:p-6 dark:bg-slate-950/20">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
