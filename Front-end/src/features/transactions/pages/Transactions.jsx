import { useState, useEffect } from "react"
import { Controller, useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { Plus, Receipt, Loader2, Coins } from "lucide-react"

import PageHeader from "@/components/shared/PageHeader"
import CategoryBadge from "@/components/shared/CategoryBadge"
import CurrencyInput from "@/components/shared/CurrencyInput"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import transactionService from "@/services/transactionService"
import walletService from "@/services/walletService"
import categoryService from "@/services/categoryService"

const transactionSchema = z.object({
  walletId: z.string().uuid("Selecione uma carteira"),
  categoryId: z.string().uuid("Selecione uma categoria"),
  amount: z.coerce.number().min(0.01, "O valor deve ser maior que zero"),
  type: z.enum(["INCOME", "EXPENSE", "TRANSFER"]),
  destinationWalletId: z.string().optional(),
  date: z.string().min(10, "Data inválida"),
  description: z.string().min(2, "A descrição deve conter pelo menos 2 caracteres."),
  status: z.enum(["PENDING", "PAID", "CANCELED"]),
}).superRefine((data, context) => {
  if (data.type === "TRANSFER" && (!data.destinationWalletId || !z.string().uuid().safeParse(data.destinationWalletId).success)) {
    context.addIssue({ code: z.ZodIssueCode.custom, path: ["destinationWalletId"], message: "Selecione a carteira de destino" })
  }
})

export default function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [wallets, setWallets] = useState([])
  const [categories, setCategories] = useState([])
  const [selectedWalletId, setSelectedWalletId] = useState("")
  const [loading, setLoading] = useState(true)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  
  const user = JSON.parse(localStorage.getItem('usuario') || '{}')
  const ownerId = user?.id

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isSubmitting },
    setValue,
    watch
  } = useForm({
    resolver: zodResolver(transactionSchema),
    defaultValues: {
      walletId: "",
      categoryId: "",
      amount: 0,
      type: "EXPENSE",
      destinationWalletId: "",
      date: new Date().toISOString().split('T')[0],
      description: "",
      status: "PENDING",
    },
  })

  const currentType = watch("type")

  useEffect(() => {
    async function init() {
      if (!ownerId) {
        setLoading(false)
        return
      }
      try {
        setLoading(true)
        const res = await walletService.getWallets()
        if (res.data?.success && Array.isArray(res.data.data) && res.data.data.length > 0) {
          setWallets(res.data.data)
          const firstWalletId = res.data.data[0].id
          setSelectedWalletId(firstWalletId)
          setValue("walletId", firstWalletId)
          loadCategories(firstWalletId)
          loadTransactions(firstWalletId)
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
      const res = await categoryService.listByWallet(walletId)
      if (res.data?.success) {
        setCategories(res.data.data)
      }
    } catch (error) {
      console.error(error)
    }
  }

  const loadTransactions = async (walletId) => {
    try {
      setLoading(true)
      const res = await transactionService.listTransactions({ walletId })
      if (res.data?.success) {
        setTransactions(Array.isArray(res.data.data) ? res.data.data : [])
      }
    } catch (error) {
      console.error(error)
      toast.error("Erro ao carregar transações.")
    } finally {
      setLoading(false)
    }
  }

  const handleWalletChange = (e) => {
    const wId = e.target.value
    setSelectedWalletId(wId)
    setValue("walletId", wId)
    setValue("categoryId", "")
    if (wId) {
      loadCategories(wId)
      loadTransactions(wId)
    } else {
      setTransactions([])
      setCategories([])
    }
  }

  const onSubmit = async (data) => {
    try {
      const payload = {
        walletId: data.walletId,
        categoryId: data.categoryId,
        amount: data.amount,
        type: data.type,
        status: data.status,
        ...(data.type === "TRANSFER" ? { destinationWalletId: data.destinationWalletId } : {}),
        title: data.description, // Mapeando description do form para title
        description: data.description,
        transactionDate: new Date(`${data.date}T12:00:00`).toISOString(),
      }

      await transactionService.createTransaction(payload)
      toast.success("Transação criada com sucesso!")
      setIsDialogOpen(false)
      reset()
      setValue("walletId", selectedWalletId)
      setValue("type", "EXPENSE")
      setValue("status", "PENDING")
      setValue("date", new Date().toISOString().split('T')[0])
      loadTransactions(selectedWalletId)
    } catch (error) {
      console.error(error)
      toast.error(error.response?.data?.message || "Erro ao criar transação.")
    }
  }

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value || 0)
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('pt-BR')
  }

  const filteredCategories = categories.filter(c => c.type === currentType)

  return (
    <div className="w-full space-y-6">
      <PageHeader
        title="Transações"
        description="Acompanhe suas despesas e receitas organizadas por categoria."
        breadcrumbs={[{ name: "Transações" }]}
      />

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-3">
          <Label htmlFor="wallet-select" className="text-sm font-medium">Carteira:</Label>
          <select 
            id="wallet-select"
            className="flex h-10 w-full md:w-50 items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
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
              Nova Transação
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-106.25">
            <form onSubmit={handleSubmit(onSubmit)}>
              <DialogHeader>
                <DialogTitle>Adicionar Transação</DialogTitle>
                <DialogDescription>
                  Registre uma nova receita ou despesa.
                </DialogDescription>
              </DialogHeader>
              
              <div className="grid gap-4 py-4">
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
                      <option value="TRANSFER">Transferência</option>
                    </select>
                    {errors.type && <p className="text-xs text-destructive">{errors.type.message}</p>}
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="amount">Valor</Label>
                    <Controller
                      name="amount"
                      control={control}
                      render={({ field }) => (
                        <CurrencyInput
                          id="amount"
                          placeholder="R$ 0,00"
                          value={field.value}
                          onChange={field.onChange}
                          disabled={isSubmitting}
                        />
                      )}
                    />
                    {errors.amount && <p className="text-xs text-destructive">{errors.amount.message}</p>}
                  </div>
                </div>

                {currentType === "TRANSFER" && <div className="space-y-1.5">
                  <Label htmlFor="destinationWalletId">Carteira de destino</Label>
                  <select id="destinationWalletId" {...register("destinationWalletId")} className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm" disabled={isSubmitting}>
                    <option value="" disabled>Selecione a carteira</option>
                    {wallets.filter((wallet) => wallet.id !== selectedWalletId).map((wallet) => <option key={wallet.id} value={wallet.id}>{wallet.name}</option>)}
                  </select>
                  {errors.destinationWalletId && <p className="text-xs text-destructive">{errors.destinationWalletId.message}</p>}
                </div>}

                <div className="space-y-1.5">
                  <Label htmlFor="description">Descrição</Label>
                  <Input id="description" placeholder="Ex: Supermercado" {...register("description")} disabled={isSubmitting} />
                  {errors.description && <p className="text-xs text-destructive">{errors.description.message}</p>}
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="status">Status</Label>
                  <select
                    id="status"
                    {...register("status")}
                    className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                    disabled={isSubmitting}
                  >
                    <option value="PENDING">Pendente</option>
                    <option value="PAID">Pago</option>
                    <option value="CANCELED">Cancelado</option>
                  </select>
                  {errors.status && <p className="text-xs text-destructive">{errors.status.message}</p>}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="categoryId">Categoria</Label>
                    <select 
                      id="categoryId"
                      {...register("categoryId")}
                      className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={isSubmitting}
                    >
                      <option value="" disabled>Selecione</option>
                      {filteredCategories.map(c => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                    {errors.categoryId && <p className="text-xs text-destructive">{errors.categoryId.message}</p>}
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="date">Data</Label>
                    <Input id="date" type="date" {...register("date")} disabled={isSubmitting} />
                    {errors.date && <p className="text-xs text-destructive">{errors.date.message}</p>}
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
        <div className="flex h-50 items-center justify-center rounded-xl border border-dashed border-border bg-card">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      ) : wallets.length === 0 ? (
        <div className="flex flex-col gap-2 h-50 items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
          <Receipt className="h-10 w-10 text-muted-foreground/50 mb-2" />
          <p>Você precisa criar uma carteira primeiro.</p>
        </div>
      ) : transactions.length === 0 ? (
        <div className="flex flex-col gap-2 h-50 items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
          <Receipt className="h-10 w-10 text-muted-foreground/50 mb-2" />
          <p>Nenhuma transação encontrada para esta carteira.</p>
          <p className="text-xs">Crie sua primeira transação clicando em "Nova Transação".</p>
        </div>
      ) : (
        <div className="rounded-xl border border-border bg-card overflow-hidden">
          <div className="overflow-x-auto min-w-full">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-border bg-slate-50/50 dark:bg-slate-950/20 text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                  <th className="px-6 py-3.5">Descrição</th>
                  <th className="px-4 py-3.5">Categoria</th>
                  <th className="px-4 py-3.5">Data</th>
                  <th className="px-6 py-3.5 text-right">Valor</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 text-xs">
                {transactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/30 transition-colors">
                    <td className="px-6 py-4 font-semibold text-foreground flex items-center gap-3">
                      <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${tx.type === 'INCOME' ? 'bg-emerald-500/10 text-emerald-600' : 'bg-amber-500/10 text-amber-600'}`}>
                        {tx.type === 'INCOME' ? <Coins className="h-4 w-4" /> : <Receipt className="h-4 w-4" />}
                      </div>
                      {tx.title || tx.description}
                    </td>
                    <td className="px-4 py-4">
                      <CategoryBadge category={tx.categoryName || 'Geral'} color={tx.categoryColor} />
                    </td>
                    <td className="px-4 py-4 text-muted-foreground font-medium">
                      {formatDate(tx.transactionDate)}
                    </td>
                    <td className={`px-6 py-4 text-right font-bold ${tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-destructive'}`}>
                      {tx.type === 'INCOME' ? '+' : '-'} {formatCurrency(tx.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
