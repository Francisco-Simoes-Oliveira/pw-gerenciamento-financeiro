import PageHeader from "@/components/shared/PageHeader"

export default function Profile() {
  return (
    <div className="w-full">
      <PageHeader
        title="Perfil do Usuário"
        description="Visualização dos seus dados, XP acumulado e nível atual."
        breadcrumbs={[{ name: "Perfil" }]}
      />
      <div className="flex h-[400px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
        Área do Perfil (Pronto para a Fase 7)
      </div>
    </div>
  )
}
