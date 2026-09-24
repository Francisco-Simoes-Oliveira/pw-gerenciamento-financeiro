import api from '../config/axiosConfig';

class TransactionService {
  async listTransactions(params) {
    const response = await api.get('/api/transactions', { params });
    return response;
  }

  async getTransaction(id) {
    const response = await api.get(`/api/transactions/${id}`);
    return response;
  }

  async createTransaction(data) {
    const response = await api.post('/api/transactions', data);
    return response;
  }

  async updateTransaction(id, data) {
    const response = await api.put(`/api/transactions/${id}`, data);
    return response;
  }

  async deleteTransaction(id) {
    const response = await api.delete(`/api/transactions/${id}`);
    return response;
  }
}

export default new TransactionService();
