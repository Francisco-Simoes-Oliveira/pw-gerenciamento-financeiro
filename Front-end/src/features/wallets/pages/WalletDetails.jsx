import { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { ArrowLeft, Loader2, Mail, Trash2, UserPlus } from "lucide-react"
import { useNavigate, useParams } from "react-router-dom"
import { toast } from "sonner"

import PageHeader from "@/components/shared/PageHeader"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import walletService from "@/services/walletService"

const memberSchema = z.object({
  email: z.string().email("Informe um e-mail válido."),
  role: z.enum(["EDITOR", "VIEWER"]),
})

const roleLabel = { OWNER: "OWNER", EDITOR: "EDITOR", VIEWER: "VIEWER" }

export default function WalletDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [wallet, setWallet] = useState(null)
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [membersLoading, setMembersLoading] = useState(true)
  const [error, setError] = useState("")
  const currentUser = JSON.parse(localStorage.getItem("usuario") || "{}")
  const isOwner = wallet?.ownerId === currentUser?.id

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(memberSchema),
    defaultValues: { email: "", role: "EDITOR" },
  })

  const loadMembers = async () => {
    try {
      setMembersLoading(true)
      const response = await walletService.listMembers(id)
      setMembers(response.data?.data || [])
    } catch (requestError) {
      console.error(requestError)
      toast.error("Não foi possível carregar os membros.")
    } finally {
      setMembersLoading(false)
    }
  }

  useEffect(() => {
    async function loadWallet() {
      try {
        setLoading(true)
        const response = await walletService.getWallet(id)
        setWallet(response.data?.data)
        await loadMembers()
      } catch (requestError) {
        console.error(requestError)
        setError("Não foi possível carregar esta carteira.")
      } finally {
        setLoading(false)
      }
    }
    loadWallet()
  }, [id])

  const onSubmit = async (data) => {
    try {
      await walletService.addMemberByEmail(id, data)
      toast.success("Membro adicionado com sucesso.")
      reset()
      await loadMembers()
    } catch (requestError) {
      console.error(requestError)
      const status = requestError.response?.status
      if (status === 404) toast.error("Usuário não encontrado.")
      else if (status === 409) toast.error("Este usuário já é membro desta carteira.")
      else if (status === 403) toast.error("Você não possui permissão para gerenciar os membros desta carteira.")
      else toast.error(requestError.response?.data?.message || "Não foi possível adicionar o membro.")
    }
  }

  const handleRoleChange = async (userId, role) => {
    try {
      await walletService.updateMember(id, userId, { role })
      toast.success("Permissão atualizada.")
      await loadMembers()
    } catch (requestError) {
      console.error(requestError)
      toast.error(requestError.response?.data?.message || "Não foi possível atualizar a permissão.")
    }
  }

  const handleRemove = async (userId) => {
    try {
      await walletService.removeMember(id, userId)
      toast.success("Membro removido.")
      await loadMembers()
    } catch (requestError) {
      console.error(requestError)
      toast.error(requestError.response?.data?.message || "Não foi possível remover o membro.")
    }
  }

  if (loading) {
    return <div className="flex h-64 items-center justify-center"><Loader2 className="h-6 w-6 animate-spin" /></div>
  }

  if (error || !wallet) {
    return <div className="space-y-4"><PageHeader title="Carteira" breadcrumbs={[{ name: "Carteiras" }, { name: "Detalhes" }]} /><p className="text-destructive">{error || "Carteira não encontrada."}</p></div>
  }

  return (
    <div className="w-full space-y-6">
      <PageHeader title={wallet.name} description={wallet.description || "Detalhes e membros da carteira."} breadcrumbs={[{ name: "Carteiras" }, { name: wallet.name }]} />
      <Button variant="outline" size="sm" onClick={() => navigate("/wallets")}><ArrowLeft className="mr-2 h-4 w-4" />Voltar para carteiras</Button>

      <section className="grid gap-4 md:grid-cols-3">
        <div className="rounded-xl border border-border bg-card p-5"><p className="text-xs text-muted-foreground">Saldo</p><p className="mt-2 text-2xl font-bold">{new Intl.NumberFormat("pt-BR", { style: "currency", currency: wallet.currency || "BRL" }).format(wallet.balance || 0)}</p></div>
        <div className="rounded-xl border border-border bg-card p-5"><p className="text-xs text-muted-foreground">Moeda</p><p className="mt-2 text-lg font-semibold">{wallet.currency || "BRL"}</p></div>
        <div className="rounded-xl border border-border bg-card p-5"><p className="text-xs text-muted-foreground">Tipo</p><p className="mt-2 text-lg font-semibold">{isOwner ? "Pessoal" : "Compartilhada"}</p></div>
      </section>

      <section className="rounded-xl border border-border bg-card p-5">
        <div className="mb-5 flex items-center justify-between gap-3"><div><h2 className="text-lg font-semibold">Membros da carteira</h2><p className="text-sm text-muted-foreground">Papéis e permissões reais da API.</p></div><UserPlus className="h-5 w-5 text-primary" /></div>
        {isOwner && <form onSubmit={handleSubmit(onSubmit)} className="mb-6 grid gap-3 rounded-lg border border-dashed border-border p-4 md:grid-cols-[1fr_160px_auto] md:items-end"><div><Label htmlFor="member-email">E-mail</Label><div className="relative mt-1"><Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><Input id="member-email" className="pl-9" placeholder="usuario@email.com" {...register("email")} disabled={isSubmitting} /></div>{errors.email && <p className="mt-1 text-xs text-destructive">{errors.email.message}</p>}</div><div><Label htmlFor="member-role">Papel</Label><select id="member-role" className="mt-1 flex h-10 w-full rounded-md border border-input bg-background px-3 text-sm" {...register("role")} disabled={isSubmitting}><option value="EDITOR">EDITOR</option><option value="VIEWER">VIEWER</option></select></div><Button type="submit" disabled={isSubmitting}>{isSubmitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <UserPlus className="mr-2 h-4 w-4" />}Adicionar</Button></form>}
        {membersLoading ? <div className="py-8 text-center"><Loader2 className="mx-auto h-5 w-5 animate-spin" /></div> : members.length === 0 ? <p className="py-8 text-center text-sm text-muted-foreground">Nenhum membro encontrado.</p> : <div className="space-y-3">{members.map((member) => <div key={member.userId} className="flex flex-col gap-3 rounded-lg border border-border p-4 sm:flex-row sm:items-center sm:justify-between"><div className="flex items-center gap-3"><div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 font-semibold text-primary">{member.name?.charAt(0)?.toUpperCase()}</div><div><p className="font-medium">{member.name}</p><p className="text-xs text-muted-foreground">{member.email}</p></div></div><div className="flex items-center gap-2">{member.role === "OWNER" ? <span className="rounded-md bg-primary/10 px-2 py-1 text-xs font-semibold text-primary">{roleLabel[member.role]}</span> : isOwner ? <><select className="h-9 rounded-md border border-input bg-background px-2 text-xs" value={member.role} onChange={(event) => handleRoleChange(member.userId, event.target.value)}><option value="EDITOR">EDITOR</option><option value="VIEWER">VIEWER</option></select><Button variant="ghost" size="icon" title="Remover membro" onClick={() => handleRemove(member.userId)}><Trash2 className="h-4 w-4 text-destructive" /></Button></> : <span className="rounded-md bg-muted px-2 py-1 text-xs font-semibold">{roleLabel[member.role]}</span>}</div></div>)}</div>}
      </section>
    </div>
  )
}
