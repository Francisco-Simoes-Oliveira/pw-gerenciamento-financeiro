import PageHeader from "@/components/shared/PageHeader"

export default function Transactions() {
  return (
    <div className="w-full">
      <PageHeader
        title="Transações"
        description="Acompanhe suas despesas e receitas organizadas por categoria."
        breadcrumbs={[{ name: "Transações" }]}
      />
      <div className="flex h-[400px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
        Área de Transações (Pronto para a Fase 6)
      </div>
    </div>
  )
}
