import { useEffect, useRef, useState } from "react"
import { Input } from "@/components/ui/input"

const currencyFormatter = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
})

const formatCents = (digits) => currencyFormatter.format(Number(digits || "0") / 100)

const digitsFromValue = (value) => String(value ?? "").replace(/\D/g, "")

export default function CurrencyInput({ value, onChange, ...props }) {
  const [digits, setDigits] = useState(() => {
    const numericValue = Number(value || 0)
    return numericValue > 0 ? String(Math.round(numericValue * 100)) : ""
  })
  const inputRef = useRef(null)
  const lastChangeValue = useRef(null)

  useEffect(() => {
    const numericValue = Number(value || 0)
    if (lastChangeValue.current === numericValue) {
      lastChangeValue.current = null
      return
    }
    const nextDigits = numericValue > 0 ? String(Math.round(numericValue * 100)) : ""
    setDigits((currentDigits) => currentDigits === nextDigits ? currentDigits : nextDigits)
  }, [value])

  const handleChange = (event) => {
    const nextDigits = digitsFromValue(event.target.value)
    const numericValue = nextDigits ? Number(nextDigits) / 100 : 0
    lastChangeValue.current = numericValue
    setDigits(nextDigits)
    onChange(numericValue)
  }

  const handleFocus = (event) => {
    event.target.select()
    props.onFocus?.(event)
  }

  return (
    <Input
      {...props}
      ref={inputRef}
      inputMode="decimal"
      value={digits ? formatCents(digits) : ""}
      onChange={handleChange}
      onFocus={handleFocus}
    />
  )
}
