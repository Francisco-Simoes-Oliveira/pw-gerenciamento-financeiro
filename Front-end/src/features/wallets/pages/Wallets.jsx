import PageHeader from "@/components/shared/PageHeader"

export default function Wallets() {
  return (
    <div className="w-full">
      <PageHeader
        title="Carteiras"
        description="Gerencie suas carteiras pessoais e compartilhadas com facilidade."
        breadcrumbs={[{ name: "Carteiras" }]}
      />
      <div className="flex h-[400px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
        Área de Carteiras (Pronto para a Fase 5)
      </div>
    </div>
  )
}
