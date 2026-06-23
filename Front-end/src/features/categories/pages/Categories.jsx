import PageHeader from "@/components/shared/PageHeader"

export default function Categories() {
  return (
    <div className="w-full">
      <PageHeader
        title="Categorias"
        description="Configure os tipos de gastos e metas para cada grupo."
        breadcrumbs={[{ name: "Categorias" }]}
      />
      <div className="flex h-[400px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
        Área de Categorias (Pronto para a Fase 7)
      </div>
    </div>
  )
}
