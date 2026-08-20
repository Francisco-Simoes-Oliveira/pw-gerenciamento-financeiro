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

  async addMember(walletId, targetUserId, permission) {
    const response = await api.post(`/api/wallets/${walletId}/members/${targetUserId}?permission=${permission}`);
    return response;
  }
}

export default new WalletService();

