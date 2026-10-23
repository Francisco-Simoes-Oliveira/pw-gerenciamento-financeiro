import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react"
import { getGamificationProfile } from "@/services/gamificationService"

const emptyProfile = {
  totalXp: 0,
  level: 1,
  rank: "Explorador Financeiro",
  currentLevelXp: 0,
  xpPerLevel: 200,
  nextLevelTotalXp: 200,
  progressPercentage: 0,
  achievements: [],
  recentXpEvents: [],
}

const GamificationContext = createContext({ profile: emptyProfile, loading: true, refresh: async () => {} })

export function GamificationProvider({ children }) {
  const [profile, setProfile] = useState(emptyProfile)
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async ({ quiet = false } = {}) => {
    if (!quiet) setLoading(true)
    try {
      const response = await getGamificationProfile()
      if (response.data?.success) setProfile({ ...emptyProfile, ...response.data.data })
    } catch (error) {
      console.error("Gamification profile error:", error)
    } finally {
      if (!quiet) setLoading(false)
    }
  }, [])

  useEffect(() => {
    refresh()
    const handleRefresh = () => refresh({ quiet: true })
    window.addEventListener("gamification:refresh", handleRefresh)
    return () => window.removeEventListener("gamification:refresh", handleRefresh)
  }, [refresh])

  const value = useMemo(() => ({ profile, loading, refresh }), [profile, loading, refresh])
  return <GamificationContext.Provider value={value}>{children}</GamificationContext.Provider>
}

export function useGamification() {
  return useContext(GamificationContext)
}
