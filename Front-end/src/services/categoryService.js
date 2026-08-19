import api from '../config/axiosConfig';

class CategoryService {
  async listByWallet(walletId) {
    const response = await api.get(`/api/categories?walletId=${walletId}`);
    return response;
  }

  async getCategory(id) {
    const response = await api.get(`/api/categories/${id}`);
    return response;
  }

  async createCategory(data) {
    const response = await api.post('/api/categories', data);
    return response;
  }

  async updateCategory(id, data) {
    const response = await api.put(`/api/categories/${id}`, data);
    return response;
  }

  async deleteCategory(id) {
    const response = await api.delete(`/api/categories/${id}`);
    return response;
  }
}

export default new CategoryService();
