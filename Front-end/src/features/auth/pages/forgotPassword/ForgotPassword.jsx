import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Mail, ArrowLeft, AlertCircle, Check, Loader2, TrendingUp } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import * as AppRoutes from "@/routes/AppRoutes"

const forgotSchema = z.object({
  email: z.string().email("Por favor, insira um e-mail válido."),
})

export default function ForgotPassword() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [successMessage, setSuccessMessage] = useState("")

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(forgotSchema),
    defaultValues: {
      email: "",
    },
  })

  const onSubmit = async (data) => {
    setLoading(true)
    setErrorMessage("")
    setSuccessMessage("")
    try {
      console.log("Forgot Password Request Email:", data.email)
      setTimeout(() => {
        setLoading(false)
        setSuccessMessage("Código de redefinição enviado com sucesso! Verifique sua caixa de entrada.")
        setTimeout(() => {
          navigate(AppRoutes.VerifyCode)
        }, 1500)
      }, 1200)
    } catch (err) {
      setLoading(false)
      setErrorMessage("Ocorreu um erro ao processar o seu pedido. Tente novamente.")
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
              Esqueceu sua senha?
            </h1>
            <p className="text-xs text-muted-foreground leading-relaxed px-4">
              Digite seu e-mail corporativo cadastrado para receber as instruções de recuperação.
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
            
            {/* Email Input */}
            <div className="space-y-1.5">
              <Label htmlFor="email">E-mail corporativo</Label>
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

            {/* Submit Button */}
            <Button type="submit" className="w-full h-10 mt-2 font-semibold cursor-pointer" disabled={loading}>
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Enviando...
                </span>
              ) : (
                "Enviar Código"
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
              Voltar para Fazer login
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
