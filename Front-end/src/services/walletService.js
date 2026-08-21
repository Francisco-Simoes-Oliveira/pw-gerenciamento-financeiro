import api from '../config/axiosConfig';

class WalletService {
  async createWallet(data) {
    const response = await api.post(`/api/wallets`, data);
    return response;
  }

  async getWallets() {
    const response = await api.get(`/api/wallets`);
    return response;
  }

  async getWallet(id) {
    return api.get(`/api/wallets/${id}`);
  }

  async updateWallet(id, data) {
    return api.put(`/api/wallets/${id}`, data);
  }

  async deleteWallet(id) {
    return api.delete(`/api/wallets/${id}`);
  }

  async listMembers(walletId) {
    return api.get(`/api/wallets/${walletId}/members`);
  }

  async addMemberByEmail(walletId, data) {
    return api.post(`/api/wallets/${walletId}/members`, data);
  }

  async updateMember(walletId, userId, data) {
    return api.patch(`/api/wallets/${walletId}/members/${userId}`, data);
  }

  async removeMember(walletId, userId) {
    return api.delete(`/api/wallets/${walletId}/members/${userId}`);
  }

  async addMember(walletId, targetUserId, permission) {
    const response = await api.post(`/api/wallets/${walletId}/members/${targetUserId}?permission=${permission}`);
    return response;
  }
}

export default new WalletService();

