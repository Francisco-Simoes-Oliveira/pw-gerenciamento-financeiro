import api from '../config/axiosConfig';

class AuthService {
  async login(data) {
    const response = await api.post('/api/auth/login', data);
    return response;
  }

  async requestPasswordReset(email) {
    const response = await api.post('/api/auth/password-reset/request', { email });
    return response;
  }

  async confirmPasswordReset(token, newPassword) {
    const response = await api.post('/api/auth/password-reset/confirm', { token, newPassword });
    return response;
  }

  async register(data) {
    const response = await api.post('/api/users', data);
    return response;
  }
}

export default new AuthService();

