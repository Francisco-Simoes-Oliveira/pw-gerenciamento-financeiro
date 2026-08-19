import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Key, Lock, Eye, EyeOff, Check, AlertCircle, Loader2, TrendingUp, ArrowLeft } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import authService from "@/services/authService"
import * as AppRoutes from "@/routes/AppRoutes"

const verifySchema = z
  .object({
    code: z.string().min(6, "O código de verificação deve conter 6 caracteres."),
    password: z.string().min(8, "A senha deve conter pelo menos 8 caracteres."),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "As senhas não coincidem.",
    path: ["confirmPassword"],
  })

export default function VerifyCode() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [successMessage, setSuccessMessage] = useState("")

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(verifySchema),
    defaultValues: {
      code: "",
      password: "",
      confirmPassword: "",
    },
  })

  const onSubmit = async (data) => {
    setLoading(true)
    setErrorMessage("")
    setSuccessMessage("")
    try {
      await authService.confirmPasswordReset(data.code, data.password)
      
      setLoading(false)
      setSuccessMessage("Senha redefinida com sucesso! Você está sendo redirecionado...")
      setTimeout(() => {
        navigate(AppRoutes.Login)
      }, 1500)
    } catch (err) {
      setLoading(false)
      const msg = err.response?.data?.message || err.response?.data || "Código inválido ou expirado. Verifique os dados."
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
              Definir Nova Senha
            </h1>
            <p className="text-xs text-muted-foreground leading-relaxed px-4">
              Insira o código enviado ao seu e-mail e defina a sua nova senha institucional.
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
            
            {/* Reset Code Input */}
            <div className="space-y-1.5">
              <Label htmlFor="code">Código de verificação</Label>
              <div className="relative">
                <Key className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="code"
                  type="text"
                  placeholder="Ex: 123456"
                  maxLength={6}
                  className="pl-9 bg-card border-border/80 tracking-widest font-semibold focus-visible:bg-card"
                  disabled={loading}
                  {...register("code")}
                />
              </div>
              {errors.code && (
                <p className="text-xs text-destructive font-medium mt-1">{errors.code.message}</p>
              )}
            </div>

            {/* New Password Input */}
            <div className="space-y-1.5">
              <Label htmlFor="password">Nova Senha</Label>
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

            {/* Confirm New Password Input */}
            <div className="space-y-1.5">
              <Label htmlFor="confirmPassword">Confirmar Nova Senha</Label>
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

            {/* Submit Button */}
            <Button type="submit" className="w-full h-10 mt-2 font-semibold cursor-pointer" disabled={loading}>
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Salvando...
                </span>
              ) : (
                "Salvar e Entrar"
              )}
            </Button>
          </form>

          {/* Back to login trigger */}
          <div className="text-center text-sm text-muted-foreground mt-6">
            <span
              onClick={() => navigate(AppRoutes.Login)}
              className="inline-flex items-center gap-1.5 font-semibold text-primary hover:underline cursor-pointer"
            >
              <ArrowLeft className="h-4 w-4" />
              Cancelar e Voltar
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
