import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Plus, Wallet as WalletIcon, Loader2 } from "lucide-react"

import PageHeader from "@/components/shared/PageHeader"
import WalletCard from "@/components/shared/WalletCard"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import walletService from "@/services/walletService"

const walletSchema = z.object({
  name: z.string().min(2, "O nome deve conter pelo menos 2 caracteres."),
  description: z.string().optional(),
  currency: z.string().min(3, "Ex: BRL, USD").max(3).toUpperCase(),
  color: z.string().min(4, "Cor inválida").max(7),
  icon: z.string().optional(),
})

export default function Wallets() {
  const navigate = useNavigate()
  const [wallets, setWallets] = useState([])
  const [loading, setLoading] = useState(true)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  
  const user = JSON.parse(localStorage.getItem('usuario') || '{}')
  const ownerId = user?.id

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(walletSchema),
    defaultValues: {
      name: "",
      description: "",
      currency: "BRL",
      color: "#3B82F6",
      icon: "wallet",
    },
  })

  const loadWallets = async () => {
    try {
      setLoading(true)
      const res = await walletService.getWallets()
      if (res.data?.success) {
        setWallets(res.data.data)
      }
    } catch (error) {
      console.error(error)
      toast.error("Erro ao carregar carteiras.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadWallets()
  }, [])

  const onSubmit = async (data) => {
    try {
      await walletService.createWallet(data)
      toast.success("Carteira criada com sucesso!")
      setIsDialogOpen(false)
      reset()
      loadWallets()
    } catch (error) {
      console.error(error)
      toast.error(error.response?.data?.message || "Erro ao criar carteira.")
    }
  }

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value || 0)
  }

  return (
    <div className="w-full space-y-6">
      <PageHeader
        title="Carteiras"
        description="Gerencie suas carteiras pessoais e compartilhadas com facilidade."
        breadcrumbs={[{ name: "Carteiras" }]}
      />

      <div className="flex justify-between items-center">
        <h2 className="text-lg font-medium text-foreground">Minhas Carteiras</h2>
        
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button size="sm" className="gap-2">
              <Plus className="h-4 w-4" />
              Nova Carteira
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <form onSubmit={handleSubmit(onSubmit)}>
              <DialogHeader>
                <DialogTitle>Adicionar Carteira</DialogTitle>
                <DialogDescription>
                  Crie uma nova carteira para gerenciar seus saldos separadamente.
                </DialogDescription>
              </DialogHeader>
              
              <div className="grid gap-4 py-4">
                <div className="space-y-1.5">
                  <Label htmlFor="name">Nome da Carteira</Label>
                  <Input id="name" placeholder="Ex: Conta Corrente" {...register("name")} disabled={isSubmitting} />
                  {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="description">Descrição</Label>
                  <Input id="description" placeholder="Opcional" {...register("description")} disabled={isSubmitting} />
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="currency">Moeda</Label>
                    <Input id="currency" placeholder="BRL" {...register("currency")} disabled={isSubmitting} />
                    {errors.currency && <p className="text-xs text-destructive">{errors.currency.message}</p>}
                  </div>
                  
                  <div className="space-y-1.5">
                    <Label htmlFor="color">Cor</Label>
                    <Input type="color" className="h-9 w-full p-1" id="color" {...register("color")} disabled={isSubmitting} />
                    {errors.color && <p className="text-xs text-destructive">{errors.color.message}</p>}
                  </div>
                </div>
              </div>
              
              <DialogFooter>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Salvar
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {loading ? (
        <div className="flex h-[200px] items-center justify-center rounded-xl border border-dashed border-border bg-card">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      ) : wallets.length === 0 ? (
        <div className="flex flex-col gap-2 h-[200px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
          <WalletIcon className="h-10 w-10 text-muted-foreground/50 mb-2" />
          <p>Nenhuma carteira encontrada.</p>
          <p className="text-xs">Crie sua primeira carteira clicando em "Nova Carteira".</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {wallets.map((w) => (
            <button
              key={w.id}
              type="button"
              className="text-left"
              onClick={() => navigate(`/wallets/${w.id}`)}
            >
              <WalletCard
                name={w.name}
                type={w.ownerId === ownerId ? "Pessoal" : "Compartilhada"}
                balance={formatCurrency(w.balance || 0)}
                members={[]}
              />
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
