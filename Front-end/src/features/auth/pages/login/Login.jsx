import { useState } from "react"
import authService from "@/services/authService"
import { useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Mail, Lock, Eye, EyeOff, LogIn, TrendingUp, AlertCircle } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import * as AppRoutes from "@/routes/AppRoutes"

// Zod Validation Schema for Login
const loginSchema = z.object({
  email: z.string().email("Por favor, insira um e-mail válido."),
  password: z.string().min(6, "A senha deve ter pelo menos 6 caracteres."),
  rememberMe: z.boolean().optional(),
})

export default function Login() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
      rememberMe: false,
    },
  })

  const onSubmit = async (data) => {
    setLoading(true)
    setErrorMessage("")
    try {
      const response = await authService.login(data)
      const { token, user } = response.data.data
      
      localStorage.setItem("app-token", token)
      localStorage.setItem("usuario", JSON.stringify(user))

      setLoading(false)
      navigate(AppRoutes.Dashboard)
    } catch (err) {
      setLoading(false)
      setErrorMessage("E-mail corporativo ou senha incorretos.")
    }
  }

  return (
    <div className="flex min-h-screen w-screen bg-background text-foreground antialiased overflow-hidden font-sans">
      
      {/* Left split pane: Isometric graphics (Visible on lg and above) */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between bg-gradient-to-br from-blue-900 via-blue-950 to-slate-950 p-12 text-white relative">
        {/* Glow ambient decoration */}
        <div className="absolute top-1/4 left-1/4 h-96 w-96 rounded-full bg-blue-500/10 blur-[100px]" />
        
        {/* Logo Branding */}
        <div className="flex items-center gap-2.5 z-10">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 shadow-lg shadow-blue-500/20">
            <TrendingUp className="h-5 w-5" />
          </div>
          <div className="flex flex-col">
            <span className="text-base font-bold tracking-tight text-white">FinShare</span>
            <span className="text-[10px] uppercase font-semibold tracking-wider text-blue-400">Institutional Grade</span>
          </div>
        </div>

        {/* Isometric SVG Microchip Layout */}
        <div className="flex flex-1 items-center justify-center z-10 py-12">
          <svg
            className="w-full max-w-[340px] drop-shadow-2xl animate-pulse duration-[3000ms]"
            viewBox="0 0 400 400"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            {/* Grid base */}
            <path d="M200 40 L360 140 L200 240 L40 140 Z" fill="#1E293B" opacity="0.4" />
            <path d="M200 60 L330 140 L200 220 L70 140 Z" stroke="#3b82f6" strokeWidth="1" strokeDasharray="4 4" opacity="0.3" />
            
            {/* Connection tracks */}
            <path d="M120 140 L200 190" stroke="#3b82f6" strokeWidth="2" opacity="0.6" />
            <path d="M280 140 L200 190" stroke="#3b82f6" strokeWidth="2" opacity="0.6" />
            <path d="M200 90 L200 190" stroke="#60a5fa" strokeWidth="2.5" opacity="0.8" />
            
            {/* Metallic Isometric Plate */}
            <g transform="translate(100, 100)">
              {/* Top face */}
              <path d="M100 20 L180 70 L100 120 L20 70 Z" fill="url(#chipGradient)" />
              {/* Side faces */}
              <path d="M20 70 L100 120 L100 130 L20 80 Z" fill="#0F172A" />
              <path d="M100 120 L180 70 L180 80 L100 130 Z" fill="#1E293B" />
              
              {/* Circuit designs on chip */}
              <path d="M100 40 L140 65 L100 90 L60 65 Z" fill="none" stroke="#60a5fa" strokeWidth="1.5" opacity="0.5" />
              <circle cx="100" cy="65" r="8" fill="#3b82f6" opacity="0.9" />
              <circle cx="100" cy="65" r="4" fill="#ffffff" />
            </g>

            {/* Float particles */}
            <circle cx="180" cy="90" r="3" fill="#60a5fa" opacity="0.8" />
            <circle cx="230" cy="280" r="4" fill="#3b82f6" opacity="0.6" />
            <circle cx="90" cy="220" r="2.5" fill="#93c5fd" opacity="0.7" />

            <defs>
              <linearGradient id="chipGradient" x1="20" y1="20" x2="180" y2="120" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#1E3A8A" />
                <stop offset="50%" stopColor="#3B82F6" />
                <stop offset="100%" stopColor="#1E293B" />
              </linearGradient>
            </defs>
          </svg>
        </div>

        {/* Content footer copy */}
        <div className="space-y-3 z-10 max-w-md">
          <h2 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">
            Shared financial control, redefined.
          </h2>
          <p className="text-sm text-blue-200/85 leading-relaxed">
            Unified intelligence for teams and partners. Manage wallets, track flows, and gain insights with institutional precision.
          </p>
        </div>
      </div>

      {/* Right split pane: Form card */}
      <div className="flex flex-1 flex-col justify-between bg-slate-50/20 px-6 py-12 dark:bg-slate-950/20 sm:px-12 lg:w-1/2">
        <div className="mx-auto flex w-full max-w-[400px] flex-col justify-center space-y-6 self-center my-auto">
          
          {/* Header section */}
          <div className="flex flex-col space-y-2 text-center sm:text-left">
            {/* Logo on mobile view */}
            <div className="flex items-center gap-2 lg:hidden mb-4 justify-center sm:justify-start">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-md">
                <TrendingUp className="h-4.5 w-4.5" />
              </div>
              <span className="text-base font-bold tracking-tight">FinShare</span>
            </div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
              Bem-vindo de volta
            </h1>
            <p className="text-sm text-muted-foreground leading-snug">
              Acesse sua conta institucional para gerenciar seus ativos.
            </p>
          </div>

          {/* Error alerts */}
          {errorMessage && (
            <Alert variant="destructive" className="animate-in fade-in slide-in-from-top-1 duration-200">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription className="text-xs">{errorMessage}</AlertDescription>
            </Alert>
          )}

          {/* Form */}
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            
            {/* Email Input */}
            <div className="space-y-1.5">
              <Label htmlFor="email">E-mail Corporativo</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="nome@empresa.com"
                  className="pl-9 bg-card border-border/80 focus-visible:bg-card"
                  disabled={loading}
                  {...register("email")}
                />
              </div>
              {errors.email && (
                <p className="text-xs text-destructive font-medium mt-1">{errors.email.message}</p>
              )}
            </div>

            {/* Password Input */}
            <div className="space-y-1.5">
              <Label htmlFor="password">Senha</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  className="pl-9 pr-10 bg-card border-border/80 focus-visible:bg-card"
                  disabled={loading}
                  {...register("password")}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground outline-none cursor-pointer"
                  disabled={loading}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {errors.password && (
                <p className="text-xs text-destructive font-medium mt-1">{errors.password.message}</p>
              )}
            </div>

            {/* Checkbox and Forgot password link */}
            <div className="flex items-center justify-between text-sm select-none">
              <label className="flex items-center gap-2 font-medium text-muted-foreground hover:text-foreground cursor-pointer">
                <input
                  type="checkbox"
                  className="rounded border-border text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                  disabled={loading}
                  {...register("rememberMe")}
                />
                Lembrar-me
              </label>
              <span
                onClick={() => navigate(AppRoutes.ForgotPassword)}
                className="font-medium text-primary hover:underline cursor-pointer"
              >
                Esqueci minha senha
              </span>
            </div>

            {/* Submit Button */}
            <Button type="submit" className="w-full h-10 mt-2 cursor-pointer font-semibold" disabled={loading}>
              <span className="flex items-center justify-center gap-2">
                <LogIn className="h-4 w-4" />
                {loading ? "Entrando..." : "Entrar"}
              </span>
            </Button>
          </form>

          {/* Go to register link */}
          <div className="text-center text-sm text-muted-foreground">
            Não tem uma conta?{" "}
            <span
              onClick={() => navigate(AppRoutes.Register)}
              className="font-semibold text-primary hover:underline cursor-pointer"
            >
              Criar conta
            </span>
          </div>
        </div>

        {/* Footer info layout */}
        <div className="flex items-center justify-center gap-4 text-xs font-semibold text-muted-foreground/60 select-none mt-12 sm:mt-0">
          <span className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            Sistemas Online
          </span>
          <span>|</span>
          <span className="flex items-center gap-1">
            AES-256 Encrypted
          </span>
        </div>
      </div>
    </div>
  )
}
