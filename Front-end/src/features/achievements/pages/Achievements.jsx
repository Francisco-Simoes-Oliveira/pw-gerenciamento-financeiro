import PageHeader from "@/components/shared/PageHeader"

export default function Achievements() {
  return (
    <div className="w-full">
      <PageHeader
        title="Conquistas e Emblemas"
        description="Visualize seus prêmios e troféus desbloqueados ao economizar e bater metas."
        breadcrumbs={[{ name: "Conquistas" }]}
      />
      <div className="flex h-[400px] items-center justify-center rounded-xl border border-dashed border-border bg-card p-6 text-center text-muted-foreground">
        Área de Conquistas (Pronto para o pós-Fase 7)
      </div>
    </div>
  )
}
