import "./Login.css"
import { useState } from "react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useNavigate } from "react-router-dom"
import * as AppRoutes from "@/routes/AppRoutes"

const Login = () => {
  const navigate = useNavigate()

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")

  const handleSubmit = (e) => {
    e.preventDefault()
    // Lógica de autenticação aqui
    if (email != "" && password != "") {
      console.log("Email:", email)
      console.log("Password:", password)
      navigate(AppRoutes.Dashboard)
    } else {
      console.log("Preencha todos os campos")
    }
  }

  return (
    <div className="login-page flex h-screen w-full items-center justify-center">
      <div className="login-conteiner flex min-h-75 min-w-75 flex-col items-center justify-center gap-4 rounded-lg p-6 outline">
        <h2 className="text-xl font-bold">Login</h2>
        <form
          className="login-form flex min-w-75 flex-col items-center justify-center gap-2"
          action=""
          onSubmit={handleSubmit}
        >
          <div className="form-field flex flex-col">
            <label htmlFor="emailFiled">Email</label>
            <Input
              id="emailFiled"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              type="email"
              placeholder="nome@exemplo.com"
              required
            />
          </div>
          <div className="form-field flex flex-col">
            <label htmlFor="passwordField">Senha</label>
            <Input
              id="passwordField"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              placeholder="••••••••"
              required
            />
            <a
              href={AppRoutes.ForgotPassword}
              className="self-end text-sm text-blue-500 hover:underline"
            >
              esqueci a senha
            </a>
            <div className="form-check flex items-center gap-2">
              <input
                className="form-check-input"
                type="checkbox"
                value=""
                id="flexCheckDefault"
              />
              <label
                className="form-check-label text-sm text-gray-600"
                htmlFor="flexCheckDefault"
              >
                Lembrar-me
              </label>
            </div>
          </div>
          <Button variant="outline" className="btn" type="submit">
            Iniciar
          </Button>
        </form>
      </div>
    </div>
  )
}

export default Login
