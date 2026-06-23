import * as React from "react"
import { cn } from "@/lib/utils"

interface DropdownMenuProps {
  children: React.ReactNode
}

const DropdownMenuContext = React.createContext<{
  open: boolean
  setOpen: (open: boolean) => void
  triggerRef: React.RefObject<HTMLElement | null>
} | null>(null)

export function DropdownMenu({ children }: DropdownMenuProps) {
  const [open, setOpen] = React.useState(false)
  const triggerRef = React.useRef<HTMLElement | null>(null)

  return (
    <DropdownMenuContext.Provider value={{ open, setOpen, triggerRef }}>
      <div className="relative inline-block text-left">{children}</div>
    </DropdownMenuContext.Provider>
  )
}

export function DropdownMenuTrigger({ children, asChild }: { children: React.ReactElement<any>; asChild?: boolean }) {
  const context = React.useContext(DropdownMenuContext)
  if (!context) throw new Error("DropdownMenuTrigger must be used inside DropdownMenu")

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation()
    context.setOpen(!context.open)
  }

  const triggerElement = React.cloneElement(children, {
    onClick: handleToggle,
    ref: context.triggerRef as any,
  })

  return asChild ? triggerElement : <span className="inline-block cursor-pointer">{triggerElement}</span>
}

export function DropdownMenuContent({
  className,
  align = "right",
  children,
  ...props
}: React.HTMLAttributes<HTMLDivElement> & { align?: "left" | "right" }) {
  const context = React.useContext(DropdownMenuContext)
  if (!context) throw new Error("DropdownMenuContent must be used inside DropdownMenu")

  React.useEffect(() => {
    if (!context.open) return

    const handleOutsideClick = (e: MouseEvent) => {
      if (
        context.triggerRef.current &&
        !context.triggerRef.current.contains(e.target as Node)
      ) {
        context.setOpen(false)
      }
    }

    document.addEventListener("click", handleOutsideClick)
    return () => {
      document.removeEventListener("click", handleOutsideClick)
    }
  }, [context.open])

  if (!context.open) return null

  const alignmentClass = align === "right" ? "right-0" : "left-0"

  return (
    <div
      className={cn(
        "absolute z-50 mt-2 w-56 rounded-md border border-border bg-card p-1 shadow-lg ring-1 ring-black/5 focus:outline-none animate-in fade-in slide-in-from-top-1 duration-100",
        alignmentClass,
        className
      )}
      onClick={() => context.setOpen(false)}
      {...props}
    >
      {children}
    </div>
  )
}

export function DropdownMenuItem({
  className,
  ...props
}: React.HTMLAttributes<HTMLButtonElement> & { disabled?: boolean }) {
  return (
    <button
      className={cn(
        "flex w-full items-center rounded-sm px-2 py-1.5 text-sm text-foreground hover:bg-accent hover:text-accent-foreground outline-none transition-colors cursor-pointer disabled:pointer-events-none disabled:opacity-50 text-left",
        className
      )}
      {...props}
    />
  )
}

export function DropdownMenuLabel({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("px-2 py-1.5 text-xs font-semibold text-muted-foreground", className)}
      {...props}
    />
  )
}

export function DropdownMenuSeparator({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("-mx-1 my-1 h-px bg-border", className)}
      {...props}
    />
  )
}
