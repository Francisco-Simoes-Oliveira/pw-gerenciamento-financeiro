import api from '../config/axiosConfig'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export async function startFinancialInsight(params) {
  const response = await api.post('/api/ai/insights', params)
  return response.data.data
}

export function streamFinancialInsight(start, handlers = {}) {
  const url = `${apiBaseUrl}${start.streamPath}?ticket=${encodeURIComponent(start.ticket)}`
  const source = new EventSource(url)
  let settled = false

  source.addEventListener('progress', (event) => {
    try { handlers.onProgress?.(JSON.parse(event.data)) }
    catch {
      settled = true
      source.close()
      handlers.onError?.('O servidor enviou um progresso inválido.')
    }
  })

  source.addEventListener('complete', (event) => {
    settled = true
    source.close()
    try { handlers.onComplete?.(JSON.parse(event.data)) }
    catch { handlers.onError?.('O servidor enviou um resultado inválido.') }
  })

  source.addEventListener('analysis-error', (event) => {
    settled = true
    source.close()
    try { handlers.onError?.(JSON.parse(event.data).message) }
    catch { handlers.onError?.('Não foi possível concluir a análise.') }
  })

  source.onerror = () => {
    if (settled) return
    settled = true
    source.close()
    handlers.onError?.('A conexão com a análise foi interrompida. Tente novamente.')
  }

  return () => {
    settled = true
    source.close()
  }
}
