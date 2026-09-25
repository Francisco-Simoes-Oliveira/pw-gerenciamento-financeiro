import api from '../config/axiosConfig'

export async function getStatementReport(params, signal) {
  const response = await api.get('/api/reports/statement/report', { params, signal })
  return response.data.data
}

export async function downloadStatement(params, format) {
  const response = await api.get('/api/reports/statement/export', {
    params: { ...params, format }, responseType: 'blob',
  })
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = `extrato-${params.startDate}-${params.endDate}.${format}`
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
