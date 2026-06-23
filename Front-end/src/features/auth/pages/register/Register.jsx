import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { User, Mail, Lock, Eye, EyeOff, Check, AlertCircle, Loader2, TrendingUp } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import UserService from "@/features/auth/service/UserService"
import * as AppRoutes from "@/routes/AppRoutes"

const userService = new UserService()

// Zod validation schema matching register
const registerSchema = z
  .object({
    name: z.string().min(2, "O nome deve conter pelo menos 2 caracteres."),
    email: z.string().email("Por favor, insira um e-mail válido."),
    password: z.string().min(8, "A senha deve conter pelo menos 8 caracteres."),
    confirmPassword: z.string(),
    agreeTerms: z.boolean().refine((val) => val === true, {
      message: "Você deve aceitar os termos de serviço e privacidade.",
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "As senhas não coincidem.",
    path: ["confirmPassword"],
  })

export default function Register() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [successMessage, setSuccessMessage] = useState("")
  const [passwordValue, setPasswordValue] = useState("")

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: "",
      email: "",
      password: "",
      confirmPassword: "",
      agreeTerms: false,
    },
  })

  // Real-time password strength validation rules
  const checks = {
    length: passwordValue.length >= 8,
    number: /\d/.test(passwordValue),
    symbol: /[^A-Za-z0-9]/.test(passwordValue),
  }

  const onSubmit = async (data) => {
    setLoading(true)
    setErrorMessage("")
    setSuccessMessage("")

    try {
      const payload = {
        name: data.name.trim(),
        email: data.email.trim(),
        password: data.password,
        active: true,
      }

      const response = await userService.register(payload)

      if (response.status === 200 || response.status === 201) {
        setSuccessMessage("Cadastro realizado com sucesso! Redirecionando...")
        setTimeout(() => {
          setLoading(false)
          navigate(AppRoutes.Login)
        }, 2000)
      } else {
        setLoading(false)
        setErrorMessage("Erro no cadastro. Tente novamente.")
      }
    } catch (err) {
      setLoading(false)
      const msg = err.response?.data?.message || err.response?.data || "Não foi possível conectar ao servidor."
      setErrorMessage(msg)
    }
  }

  return (
    <div className="flex min-h-screen w-screen items-center justify-center bg-slate-50/60 px-4 py-12 dark:bg-slate-950/40 antialiased font-sans">
      
      {/* Brand logo at top */}
      <div className="absolute top-8 left-8 flex items-center gap-2.5 select-none">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-md shadow-primary/20">
          <TrendingUp className="h-5 w-5" />
        </div>
        <span className="text-base font-bold tracking-tight text-foreground">FinShare</span>
      </div>

      <div className="w-full max-w-[440px] space-y-6">
        
        {/* Card container */}
        <div className="rounded-2xl border border-border bg-card p-8 shadow-sm transition-all hover:shadow-md">
          
          {/* Header titles */}
          <div className="flex flex-col space-y-2 text-center pb-4">
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              Crie sua conta
            </h1>
            <p className="text-xs text-muted-foreground leading-snug">
              Junte-se à plataforma de gestão institucional FinShare.
            </p>
          </div>

          {/* Success / Error Alerts */}
          {errorMessage && (
            <Alert variant="destructive" className="mb-4 animate-in fade-in slide-in-from-top-1 duration-200">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription className="text-xs">{errorMessage}</AlertDescription>
            </Alert>
          )}

          {successMessage && (
            <Alert variant="success" className="mb-4 animate-in fade-in slide-in-from-top-1 duration-200">
              <Check className="h-4 w-4" />
              <AlertDescription className="text-xs">{successMessage}</AlertDescription>
            </Alert>
          )}

          {/* Form */}
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            
            {/* Full Name Input */}
            <div className="space-y-1.5">
              <Label htmlFor="name">Nome completo</Label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="name"
                  type="text"
                  placeholder="Ex: João Silva"
                  className="pl-9 bg-card border-border/80 focus-visible:bg-card"
                  disabled={loading}
                  {...register("name")}
                />
              </div>
              {errors.name && (
                <p className="text-xs text-destructive font-medium mt-1">{errors.name.message}</p>
              )}
            </div>

            {/* Email Input */}
            <div className="space-y-1.5">
              <Label htmlFor="email">E-mail institucional</Label>
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
                  {...register("password", {
                    onChange: (e) => setPasswordValue(e.target.value),
                  })}
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

            {/* Password Strength Indicators (High visual fidelity) */}
            <div className="rounded-lg bg-slate-50 p-3.5 space-y-1.5 border border-border dark:bg-slate-950/20 text-xs">
              <span className="font-semibold text-foreground/80 block mb-1">Segurança da senha</span>
              <div className="flex items-center gap-2">
                <div className={`flex h-4 w-4 shrink-0 items-center justify-center rounded-full border ${checks.length ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-600 dark:text-emerald-400" : "border-border text-muted-foreground"}`}>
                  <Check className="h-2.5 w-2.5" />
                </div>
                <span className={checks.length ? "text-emerald-600 dark:text-emerald-400 font-medium" : "text-muted-foreground"}>
                  Mínimo de 8 caracteres
                </span>
              </div>
              <div className="flex items-center gap-2">
                <div className={`flex h-4 w-4 shrink-0 items-center justify-center rounded-full border ${checks.number ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-600 dark:text-emerald-400" : "border-border text-muted-foreground"}`}>
                  <Check className="h-2.5 w-2.5" />
                </div>
                <span className={checks.number ? "text-emerald-600 dark:text-emerald-400 font-medium" : "text-muted-foreground"}>
                  Pelo menos um número
                </span>
              </div>
              <div className="flex items-center gap-2">
                <div className={`flex h-4 w-4 shrink-0 items-center justify-center rounded-full border ${checks.symbol ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-600 dark:text-emerald-400" : "border-border text-muted-foreground"}`}>
                  <Check className="h-2.5 w-2.5" />
                </div>
                <span className={checks.symbol ? "text-emerald-600 dark:text-emerald-400 font-medium" : "text-muted-foreground"}>
                  Pelo menos um símbolo (!@#$)
                </span>
              </div>
            </div>

            {/* Confirm Password Input */}
            <div className="space-y-1.5">
              <Label htmlFor="confirmPassword">Confirmar senha</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  placeholder="••••••••"
                  className="pl-9 pr-10 bg-card border-border/80 focus-visible:bg-card"
                  disabled={loading}
                  {...register("confirmPassword")}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground outline-none cursor-pointer"
                  disabled={loading}
                >
                  {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="text-xs text-destructive font-medium mt-1">{errors.confirmPassword.message}</p>
              )}
            </div>

            {/* Terms and conditions agreement checkbox */}
            <div className="space-y-1.5">
              <label className="flex items-start gap-2.5 text-xs text-muted-foreground hover:text-foreground cursor-pointer select-none leading-relaxed">
                <input
                  type="checkbox"
                  className="rounded border-border text-primary focus:ring-primary h-4 w-4 cursor-pointer mt-0.5"
                  disabled={loading}
                  {...register("agreeTerms")}
                />
                <span>
                  Eu concordo com os{" "}
                  <span className="font-semibold text-primary hover:underline">Termos de Serviço</span>{" "}
                  e a{" "}
                  <span className="font-semibold text-primary hover:underline">Política de Privacidade</span>{" "}
                  da FinShare.
                </span>
              </label>
              {errors.agreeTerms && (
                <p className="text-xs text-destructive font-medium mt-1">{errors.agreeTerms.message}</p>
              )}
            </div>

            {/* Submit Button */}
            <Button type="submit" className="w-full h-10 mt-2 font-semibold cursor-pointer" disabled={loading}>
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Criando conta...
                </span>
              ) : (
                "Criar Conta"
              )}
            </Button>
          </form>

          {/* Go to login link */}
          <div className="text-center text-sm text-muted-foreground mt-6">
            Já possui uma conta?{" "}
            <span
              onClick={() => navigate(AppRoutes.Login)}
              className="font-semibold text-primary hover:underline cursor-pointer"
            >
              Fazer login
            </span>
          </div>
        </div>

        {/* Footer brand info */}
        <p className="text-center text-[10px] uppercase font-bold tracking-wider text-muted-foreground/50 select-none">
          © 2026 FINSHARE TECHNOLOGIES GROUP
        </p>
      </div>
    </div>
  )
}
