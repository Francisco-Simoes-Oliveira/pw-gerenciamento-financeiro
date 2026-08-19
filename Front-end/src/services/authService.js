import api from '../config/axiosConfig';
import { mockLogin } from '../mocks/authMock';

class AuthService {
  /**
   * Realiza o login do usuário. 
   * Temporariamente usando mock, pois o backend ainda não possui o endpoint `/api/auth/login`.
   * Quando o endpoint estiver disponível, basta trocar `mockLogin(data)` por `api.post('/api/auth/login', data)`.
   */
  async login(data) {
    // Para usar a API real futuramente:
    // const response = await api.post('/api/auth/login', data);
    // return response;
    
    return mockLogin(data);
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

  async createProfile(userId, profileData) {
    const response = await api.post(`/api/users/${userId}/profile`, profileData);
    return response;
  }
}

export default new AuthService();
