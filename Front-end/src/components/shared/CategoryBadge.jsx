import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

export default function CategoryBadge({ category, className }) {
  // Normalize categories for mapping
  const normalized = category?.trim().toLowerCase() || ""

  const colorMap = {
    alimentacao: "bg-amber-500/10 text-amber-700 border-amber-500/20 dark:text-amber-400 dark:border-amber-500/30",
    renda: "bg-emerald-500/10 text-emerald-700 border-emerald-500/20 dark:text-emerald-400 dark:border-emerald-500/30",
    transporte: "bg-indigo-500/10 text-indigo-700 border-indigo-500/20 dark:text-indigo-400 dark:border-indigo-500/30",
    moradia: "bg-rose-500/10 text-rose-700 border-rose-500/20 dark:text-rose-400 dark:border-rose-500/30",
    lazer: "bg-pink-500/10 text-pink-700 border-pink-500/20 dark:text-pink-400 dark:border-pink-500/30",
    saude: "bg-teal-500/10 text-teal-700 border-teal-500/20 dark:text-teal-400 dark:border-teal-500/30",
    educacao: "bg-blue-500/10 text-blue-700 border-blue-500/20 dark:text-blue-400 dark:border-blue-500/30",
    outros: "bg-slate-500/10 text-slate-700 border-slate-500/20 dark:text-slate-400 dark:border-slate-500/30",
  }

  // Fallback styling for unknown categories
  const matchedClass = colorMap[normalized] || colorMap.outros

  return (
    <Badge
      variant="outline"
      className={cn("text-[10px] font-semibold px-2 py-0.5 rounded-full capitalize border", matchedClass, className)}
    >
      {category}
    </Badge>
  )
}
