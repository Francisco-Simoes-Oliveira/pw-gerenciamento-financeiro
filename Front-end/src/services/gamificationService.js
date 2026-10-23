import api from "@/config/axiosConfig"

export async function getGamificationProfile() {
  return api.get("/api/gamification/profile")
}

export function notifyGamificationUpdate() {
  window.dispatchEvent(new CustomEvent("gamification:refresh"))
}
