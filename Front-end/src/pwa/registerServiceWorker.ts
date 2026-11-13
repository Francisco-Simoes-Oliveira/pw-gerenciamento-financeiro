export const PWA_UPDATE_EVENT = "pwa:update-available" as const

export type PwaUpdateDetail = {
  activate: () => void
}

declare global {
  interface WindowEventMap {
    [PWA_UPDATE_EVENT]: CustomEvent<PwaUpdateDetail>
  }
}

async function installServiceWorker() {
  try {
    const registration = await navigator.serviceWorker.register("/sw.js", {
      scope: "/",
      updateViaCache: "none",
    })

    let updateRequested = false

    navigator.serviceWorker.addEventListener("controllerchange", () => {
      if (updateRequested) window.location.reload()
    })

    const offerUpdate = (worker: ServiceWorker) => {
      window.dispatchEvent(
        new CustomEvent<PwaUpdateDetail>(PWA_UPDATE_EVENT, {
          detail: {
            activate: () => {
              updateRequested = true
              worker.postMessage({ type: "SKIP_WAITING" })
            },
          },
        })
      )
    }

    if (registration.waiting && navigator.serviceWorker.controller) {
      offerUpdate(registration.waiting)
    }

    registration.addEventListener("updatefound", () => {
      const installingWorker = registration.installing
      if (!installingWorker) return

      installingWorker.addEventListener("statechange", () => {
        if (installingWorker.state === "installed" && navigator.serviceWorker.controller) {
          offerUpdate(installingWorker)
        }
      })
    })

    const checkForUpdate = () => registration.update().catch(() => undefined)
    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "visible") void checkForUpdate()
    })
    window.setInterval(checkForUpdate, 60 * 60 * 1000)
  } catch (error) {
    console.error("Não foi possível registrar o modo offline do FinShare.", error)
  }
}

export function registerServiceWorker() {
  if (!import.meta.env.PROD || !("serviceWorker" in navigator)) return

  if (document.readyState === "complete") {
    void installServiceWorker()
  } else {
    window.addEventListener("load", () => void installServiceWorker(), { once: true })
  }
}

