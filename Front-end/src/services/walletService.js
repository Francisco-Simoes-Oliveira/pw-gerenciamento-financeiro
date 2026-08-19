import api from '../config/axiosConfig';

class WalletService {
  async createWallet(data, ownerId) {
    const response = await api.post(`/api/wallets?ownerId=${ownerId}`, data);
    return response;
  }

  async getWallets(ownerId) {
    const response = await api.get(`/api/wallets?ownerId=${ownerId}`);
    return response;
  }

  async addMember(walletId, targetUserId, currentUserId, permission) {
    const response = await api.post(`/api/wallets/${walletId}/members/${targetUserId}?currentUserId=${currentUserId}&permission=${permission}`);
    return response;
  }
}

export default new WalletService();
