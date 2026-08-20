import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import { Toaster } from 'sonner'
import Login from "./features/auth/pages/login/Login"
import Register from "./features/auth/pages/register/Register"
import ForgotPassword from "./features/auth/pages/forgotPassword/ForgotPassword"
import VerifyCode from "./features/auth/pages/verifyCode/VerifyCode"

import DashboardLayout from "./components/layouts/DashboardLayout"
import Dashboard from "./features/dashboard/pages/Dashboard"
import Wallets from "./features/wallets/pages/Wallets"
import Transactions from "./features/transactions/pages/Transactions"
import Categories from "./features/categories/pages/Categories"
import Goals from "./features/goals/pages/Goals"
import Achievements from "./features/achievements/pages/Achievements"
import ChangePassword from "./features/settings/pages/changePassword"
import Profile from "./features/profile/pages/Profile"
import ProtectedRoute from "./routes/ProtectedRoute"
import { useNavigate } from "react-router-dom"
import { useEffect } from "react"

function AuthListener() {
  const navigate = useNavigate()

  useEffect(() => {
    const handleUnauthorized = () => {
      navigate('/login', { replace: true })
    }

    window.addEventListener('auth:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized)
  }, [navigate])

  return null
}

export function App() {
  return (
    <div className="flex h-screen w-screen bg-background">
      <BrowserRouter>
        <AuthListener />
        <Routes>
          {/* Public Auth Routes */}
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/verify-code" element={<VerifyCode />} />

          {/* Protected Dashboard Layout Routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<DashboardLayout />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/wallets" element={<Wallets />} />
              <Route path="/transactions" element={<Transactions />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/goals" element={<Goals />} />
              <Route path="/achievements" element={<Achievements />} />
              <Route path="/settings" element={<ChangePassword />} />
              <Route path="/profile" element={<Profile />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </BrowserRouter>
      <Toaster position="top-right" richColors />
    </div>
  )
}

export default App
