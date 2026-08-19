import { useState, useEffect } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Plus, Tags, Loader2 } from "lucide-react"

import PageHeader from "@/components/shared/PageHeader"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import categoryService from "@/services/categoryService"
import walletService from "@/services/walletService"

const categorySchema = z.object({
  name: z.string().min(2, "O nome deve conter pelo menos 2 caracteres."),
  color: z.string().min(4, "Cor inválida").max(7),
  icon: z.string().optional(),
  type: z.enum(["INCOME", "EXPENSE"]),
  walletId: z.string().uuid("Selecione uma carteira"),
})

export default function Categories() {
  const [categories, setCategories] = useState([])
  const [wallets, setWallets] = useState([])
  const [selectedWalletId, setSelectedWalletId] = useState("")
  const [loading, setLoading] = useState(true)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  
  const user = JSON.parse(localStorage.getItem('usuario') || '{}')
  const ownerId = user?.id

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
    setValue
  } = useForm({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      name: "",
      color: "#10B981",
      icon: "tag",
      type: "EXPENSE",
      walletId: "",
    },
  })

  useEffect(() => {
    async function init() {
      if (!ownerId) {
        setLoading(false)
        return
      }
      try {
        setLoading(true)
        const res = await walletService.getWallets(ownerId)
        if (res.data?.success && res.data.data.length > 0) {
          setWallets(res.data.data)
          const firstWalletId = res.data.data[0].id
          setSelectedWalletId(firstWalletId)
          setValue("walletId", firstWalletId)
          loadCategories(firstWalletId)
        } else {
          setLoading(false)
        }
      } catch (error) {
        console.error(error)
        toast.error("Erro ao carregar carteiras.")
        setLoading(false)
      }
    }
    init()
  }, [])

  const loadCategories = async (walletId) => {
    try {
      setLoading(true)
      const res = await categoryService.listByWallet(walletId)
      if (res.data?.success) {
        setCategories(res.data.data)
      }
    } catch (error) {
      console.error(error)
      toast.error("Erro ao carregar categorias.")
    } finally {
      setLoading(false)
    }
  }

  const handleWalletChange = (e) => {
    const wId = e.target.value
    setSelectedWalletId(wId)
    setValue("walletId", wId)
    if (wId) {
      loadCategories(wId)
    } else {
      setCategories([])
    }
  }

  const onSubmit = async (data) => {
    try {
      await categoryService.createCategory(data)
      toast.success("Categoria criada com sucesso!")
      setIsDialogOpen(false)
      reset()
      setValue("walletId", selectedWalletId)
      loadCategories(selectedWalletId)
    } catch (error) {
      console.error(error)
      toast.error(error.response?.data?.message || "Erro ao criar categoria.")
    }
  }

  return (
    <div className="w-full space-y-6">
      <PageHeader
        title="Categorias"
        description="Configure os tipos de gastos e metas para cada grupo."
        breadcrumbs={[{ name: "Categorias" }]}
      />

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-3">
          <Label htmlFor="wallet-select" className="text-sm font-medium">Carteira:</Label>
          <select 
            id="wallet-select"
            className="flex h-10 w-full md:w-[200px] items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            value={selectedWalletId}
            onChange={handleWalletChange}
            disabled={loading || wallets.length === 0}
          >
            <option value="" disabled>Selecione uma carteira</option>
            {wallets.map(w => (
              <option key={w.id} value={w.id}>{w.name}</option>
            ))}
          </select>
        </div>
        
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button size="sm" className="gap-2" disabled={!selectedWalletId}>
              <Plus className="h-4 w-4" />
              Nova Categoria
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <form onSubmit={handleSubmit(onSubmit)}>
              <DialogHeader>
                <DialogTitle>Adicionar Categoria</DialogTitle>
                <DialogDescription>
                  Crie uma nova categoria para classificar suas transações.
                </DialogDescription>
              </DialogHeader>
              
              <div className="grid gap-4 py-4">
                <div className="space-y-1.5">
                  <Label htmlFor="name">Nome</Label>
                  <Input id="name" placeholder="Ex: Alimentação" {...register("name")} disabled={isSubmitting} />
                  {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="type">Tipo</Label>
                    <select 
                      id="type"
                      {...register("type")}
                      className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={isSubmitting}
                    >
                      <option value="EXPENSE">Despesa</option>
                      <option value="INCOME">Receita</option>
                    </select>
                    {errors.type && <p className="text-xs text-destructive">{errors.type.message}</p>}
                  </div>
                  
                  <div className="space-y-1.5">
                    <Label htmlFor="color">Cor</Label>
                    <div className="flex gap-2 items-center">
                      <Input type="color" className="w-12 p-1 h-9" id="color" {...register("color")} disabled={isSubmitting} />
                      <Input type="text" className="flex-1" placeholder="#10B981" {...register("color")} disabled={isSubmitting} />
                    </div>
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
          <Tags className="h-10 w-10 text-muted-foreground/50 mb-2" />
          <p>Você precisa criar uma carteira primeiro.</p>
        </div>
      ) : categories.length === 0 ? (
        <div className="flex flex-col gap-2 h-[200px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
          <Tags className="h-10 w-10 text-muted-foreground/50 mb-2" />
          <p>Nenhuma categoria encontrada para esta carteira.</p>
          <p className="text-xs">Crie sua primeira categoria clicando em "Nova Categoria".</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {categories.map((cat) => (
            <div key={cat.id} className="flex items-center p-4 rounded-xl border border-border bg-card shadow-sm hover:shadow-md transition-shadow">
              <div 
                className="w-10 h-10 rounded-lg flex items-center justify-center mr-4 shadow-sm"
                style={{ backgroundColor: `${cat.color}20`, color: cat.color }}
              >
                <Tags className="h-5 w-5" />
              </div>
              <div className="flex flex-col">
                <span className="font-semibold text-foreground text-sm">{cat.name}</span>
                <span className="text-xs font-medium text-muted-foreground">
                  {cat.type === 'INCOME' ? 'Receita' : 'Despesa'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
