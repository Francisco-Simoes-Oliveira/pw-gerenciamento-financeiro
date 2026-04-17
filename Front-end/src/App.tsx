import Login from "./features/auth/pages/login/Login"
import Register from "./features/auth/pages/register/Register"
import ForgotPassword from "./features/auth/pages/forgotPassword/ForgotPassword"
import VerifyCode from "./features/auth/pages/verifyCode/VerifyCode"

import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"

export function App() {
  return (
    <div className="flex min-h-svh min-w-svh p-6">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/verify-code" element={<VerifyCode />} />
        </Routes>
      </BrowserRouter>
    </div>
  )
}

export default App
