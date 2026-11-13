import { useEffect, useRef, useState } from "react"
import { RefreshCw, Wifi, WifiOff } from "lucide-react"

import { Button } from "@/components/ui/button"
import { PWA_UPDATE_EVENT, type PwaUpdateDetail } from "@/pwa/registerServiceWorker"

export function AppStatus() {
  const [online, setOnline] = useState(() => navigator.onLine)
  const [connectionRestored, setConnectionRestored] = useState(false)
  const [updateAction, setUpdateAction] = useState<(() => void) | null>(null)
  const wasOffline = useRef(!navigator.onLine)

  useEffect(() => {
    let restoredTimer: number | undefined

    const handleOffline = () => {
      window.clearTimeout(restoredTimer)
      wasOffline.current = true
      setConnectionRestored(false)
      setOnline(false)
    }
    const handleOnline = () => {
      setOnline(true)
      if (!wasOffline.current) return

      wasOffline.current = false
      setConnectionRestored(true)
      restoredTimer = window.setTimeout(() => setConnectionRestored(false), 4000)
    }
    const handleUpdate = (event: CustomEvent<PwaUpdateDetail>) => {
      setUpdateAction(() => event.detail.activate)
    }

    window.addEventListener("offline", handleOffline)
    window.addEventListener("online", handleOnline)
    window.addEventListener(PWA_UPDATE_EVENT, handleUpdate)

    return () => {
      window.clearTimeout(restoredTimer)
      window.removeEventListener("offline", handleOffline)
      window.removeEventListener("online", handleOnline)
      window.removeEventListener(PWA_UPDATE_EVENT, handleUpdate)
    }
  }, [])

  return (
    <div
      className="pointer-events-none fixed inset-x-4 bottom-4 z-[100] flex flex-col items-center gap-2"
      aria-live="polite"
      aria-atomic="true"
    >
      {!online && (
        <div className="pointer-events-auto flex max-w-xl items-start gap-3 rounded-xl border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-950 shadow-lg dark:border-amber-800 dark:bg-amber-950 dark:text-amber-100">
          <WifiOff className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
          <span>
            Você está offline. A estrutura do FinShare continua disponível, mas os dados financeiros
            precisam de conexão e podem não ser carregados.
          </span>
        </div>
      )}

      {connectionRestored && (
        <div className="flex items-center gap-2 rounded-xl border border-emerald-300 bg-emerald-50 px-4 py-2 text-sm text-emerald-950 shadow-lg dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-100">
          <Wifi className="size-4" aria-hidden="true" />
          Conexão restabelecida. Os dados podem ser atualizados novamente.
        </div>
      )}

      {updateAction && (
        <div className="pointer-events-auto flex max-w-xl items-center gap-3 rounded-xl border bg-background px-4 py-3 text-sm text-foreground shadow-lg">
          <RefreshCw className="size-4 shrink-0 text-primary" aria-hidden="true" />
          <span>Uma nova versão do FinShare está pronta.</span>
          <Button size="sm" onClick={updateAction}>
            Atualizar agora
          </Button>
        </div>
      )}
    </div>
  )
}

