import PageHeader from "@/components/shared/PageHeader"

export default function ChangePassword() {
  return (
    <div className="w-full">
      <PageHeader
        title="Configurações"
        description="Gerencie suas credenciais de acesso e segurança da conta."
        breadcrumbs={[{ name: "Configurações" }]}
      />
      <div className="flex h-[400px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
        Área de Configurações / Alterar Senha (Pronto para a Fase 4)
      </div>
    </div>
  )
}
