import * as React from "react"
import { cn } from "@/lib/utils"

interface TooltipProps {
  content: React.ReactNode
  children: React.ReactNode
  delay?: number
}

export function Tooltip({ content, children, delay = 200 }: TooltipProps) {
  const [show, setShow] = React.useState(false)
  const timeoutRef = React.useRef<any>(null)

  const handleMouseEnter = () => {
    timeoutRef.current = setTimeout(() => {
      setShow(true)
    }, delay)
  }

  const handleMouseLeave = () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
    }
    setShow(false)
  }

  React.useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [])

  return (
    <div
      className="relative inline-block"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      {children}
      {show && (
        <div
          className={cn(
            "absolute z-50 px-2 py-1 text-xs font-medium text-popover-foreground bg-popover rounded border border-border shadow-md -translate-x-1/2 left-1/2 bottom-full mb-2 whitespace-nowrap animate-in fade-in zoom-in-95 duration-100",
          )}
        >
          {content}
        </div>
      )}
    </div>
  )
}

export function TooltipProvider({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

export function TooltipTrigger({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

export function TooltipContent({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}
