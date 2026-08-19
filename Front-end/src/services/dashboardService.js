import api from '../config/axiosConfig';

class DashboardService {
  /**
   * Obtém o resumo geral do dashboard.
   * Pode receber walletId opcional para filtrar.
   */
  async getDashboardSummary(walletId = null) {
    const params = walletId ? { walletId } : {};
    const response = await api.get('/api/reports/dashboard', { params });
    return response;
  }

  /**
   * Obtém o extrato de transações.
   */
  async getStatement(params) {
    // params can include: page, size, walletId, categoryId, startDate, endDate, type, status
    const response = await api.get('/api/reports/statement', { params });
    return response;
  }

  /**
   * Obtém indicadores financeiros (Ticket Médio, Maior Despesa).
   */
  async getIndicators() {
    const response = await api.get('/api/reports/indicators');
    return response;
  }
}

export default new DashboardService();
