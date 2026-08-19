import axios from 'axios';
import { toast } from 'sonner';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('app-token');
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    const tokenUsuario = usuario?.token;

    if (token || tokenUsuario) {
      config.headers.Authorization = `Bearer ${token || tokenUsuario}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      const message = data?.message || 'Ocorreu um erro na requisição.';

      switch (status) {
        case 400:
          toast.error(`Requisição inválida: ${message}`);
          break;
        case 401:
          toast.error('Sessão expirada. Por favor, faça login novamente.');
          localStorage.removeItem('app-token');
          localStorage.removeItem('usuario');
          window.location.href = '/login';
          break;
        case 403:
          toast.error('Acesso negado.');
          break;
        case 404:
          toast.error(`Não encontrado: ${message}`);
          break;
        case 409:
          toast.error(`Conflito: ${message}`);
          break;
        case 422:
          toast.error(`Erro de validação: ${message}`);
          break;
        case 500:
          toast.error('Erro interno do servidor. Tente novamente mais tarde.');
          break;
        default:
          toast.error(message);
      }
    } else if (error.request) {
      toast.error('Erro de conexão. Verifique sua rede e se o backend está executando.');
    } else {
      toast.error('Ocorreu um erro inesperado.');
    }

    return Promise.reject(error);
  }
);

export default api;
