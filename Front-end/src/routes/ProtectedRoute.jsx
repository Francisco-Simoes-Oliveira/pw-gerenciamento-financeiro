import { Navigate, Outlet } from "react-router-dom"
import * as AppRoutes from "@/routes/AppRoutes"

function isTokenValid(token) {
  if (!token) return false

  try {
    const parts = token.split(".")
    if (parts.length !== 3) return false

    // Decodifica o payload base64 (parte do meio)
    const payload = JSON.parse(atob(parts[1]))
    
    if (payload.exp) {
      const expirationTime = payload.exp * 1000
      if (Date.now() >= expirationTime) {
        return false // Token expirado
      }
    }
    return true
  } catch (e) {
    return false // Token malformado ou não é um JWT
  }
}

export default function ProtectedRoute() {
  const token = localStorage.getItem("app-token")

  if (!isTokenValid(token)) {
    // Se não tiver token, ou for inválido/expirado, limpa e manda pro Login
    localStorage.removeItem("app-token")
    localStorage.removeItem("usuario")
    
    return <Navigate to={AppRoutes.Login} replace />
  }

  return <Outlet />
}
