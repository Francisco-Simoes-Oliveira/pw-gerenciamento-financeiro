import * as React from "react"
import { X } from "lucide-react"
import { cn } from "@/lib/utils"

interface SheetProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
  children: React.ReactNode
}

const SheetContext = React.createContext<{
  open?: boolean
  setOpen: (open: boolean) => void
} | null>(null)

export function Sheet({ open, onOpenChange, children }: SheetProps) {
  const [isOpen, setIsOpen] = React.useState(open || false)

  React.useEffect(() => {
    if (open !== undefined) {
      setIsOpen(open)
    }
  }, [open])

  const setOpen = React.useCallback(
    (value: boolean) => {
      setIsOpen(value)
      onOpenChange?.(value)
    },
    [onOpenChange]
  )

  return (
    <SheetContext.Provider value={{ open: isOpen, setOpen }}>
      {children}
    </SheetContext.Provider>
  )
}

export function SheetTrigger({ children, asChild, ...props }: { children: React.ReactElement<any> } & React.ButtonHTMLAttributes<HTMLButtonElement> & { asChild?: boolean }) {
  const context = React.useContext(SheetContext)
  if (!context) throw new Error("SheetTrigger must be used inside Sheet")

  return React.cloneElement(children, {
    onClick: (e: React.MouseEvent) => {
      if (children.props && typeof children.props.onClick === "function") {
        children.props.onClick(e)
      }
      context.setOpen(true)
    },
    ...props
  })
}

export function SheetOverlay({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  const context = React.useContext(SheetContext)
  if (!context) throw new Error("SheetOverlay must be used inside Sheet")

  if (!context.open) return null

  return (
    <div
      className={cn(
        "fixed inset-0 z-50 bg-black/60 backdrop-blur-sm transition-opacity duration-200 animate-in fade-in",
        className
      )}
      onClick={() => context.setOpen(false)}
      {...props}
    />
  )
}

interface SheetContentProps extends React.HTMLAttributes<HTMLDivElement> {
  side?: "top" | "bottom" | "left" | "right"
}

export function SheetContent({
  side = "right",
  className,
  children,
  ...props
}: SheetContentProps) {
  const context = React.useContext(SheetContext)
  if (!context) throw new Error("SheetContent must be used inside Sheet")

  if (!context.open) return null

  const sideClasses = {
    left: "inset-y-0 left-0 h-full w-3/4 max-w-sm border-r slide-in-from-left duration-200",
    right: "inset-y-0 right-0 h-full w-3/4 max-w-sm border-l slide-in-from-right duration-200",
    top: "inset-x-0 top-0 w-full h-1/3 border-b slide-in-from-top duration-200",
    bottom: "inset-x-0 bottom-0 w-full h-1/3 border-t slide-in-from-bottom duration-200",
  }

  return (
    <>
      <SheetOverlay />
      <div
        className={cn(
          "fixed z-50 bg-card p-6 shadow-xl transition ease-in-out text-card-foreground animate-in",
          sideClasses[side],
          className
        )}
        {...props}
      >
        {children}
        <button
          onClick={() => context.setOpen(false)}
          className="absolute right-4 top-4 rounded-md opacity-70 hover:opacity-100 transition-opacity focus:outline-none focus:ring-2 focus:ring-ring"
        >
          <X className="h-4 w-4" />
          <span className="sr-only">Fechar</span>
        </button>
      </div>
    </>
  )
}

export function SheetHeader({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("flex flex-col space-y-1.5 text-left", className)}
      {...props}
    />
  )
}

export function SheetFooter({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        "flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2 mt-6",
        className
      )}
      {...props}
    />
  )
}

export function SheetTitle({
  className,
  ...props
}: React.HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h2
      className={cn("text-lg font-semibold text-foreground", className)}
      {...props}
    />
  )
}

export function SheetDescription({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) {
  return (
    <p
      className={cn("text-sm text-muted-foreground", className)}
      {...props}
    />
  )
}
export function SheetClose({ children }: { children: React.ReactNode }) {
  const context = React.useContext(SheetContext)
  if (!context) throw new Error("SheetClose must be used inside Sheet")

  return (
    <div onClick={() => context.setOpen(false)} className="cursor-pointer">
      {children}
    </div>
  )
}
