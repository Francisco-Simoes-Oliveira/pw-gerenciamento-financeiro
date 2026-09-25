import api from '../config/axiosConfig';

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const wsBaseUrl = (import.meta.env.VITE_WS_BASE_URL || apiBaseUrl.replace(/^http/i, 'ws')).replace(/\/+$/, '');

class RealtimeService {
  connect(onEvent, onStatusChange) {
    let socket = null;
    let reconnectTimer = null;
    let stopped = false;

    const notifyStatus = (status) => {
      onStatusChange?.(status);
    };

    const scheduleReconnect = () => {
      if (stopped || reconnectTimer) return;

      notifyStatus('reconnecting');
      reconnectTimer = window.setTimeout(() => {
        reconnectTimer = null;
        connectSocket();
      }, 3000);
    };

    const connectSocket = async () => {
      if (stopped) return;

      try {
        notifyStatus('connecting');
        const response = await api.post('/api/realtime/ticket');
        const ticket = response.data?.data?.ticket;

        if (!ticket || stopped) {
          scheduleReconnect();
          return;
        }

        socket = new WebSocket(`${wsBaseUrl}/ws/realtime?ticket=${encodeURIComponent(ticket)}`);

        socket.onopen = () => notifyStatus('connected');

        socket.onmessage = (message) => {
          try {
            onEvent?.(JSON.parse(message.data));
          } catch (error) {
            console.error('Evento de tempo real inválido:', error);
          }
        };

        socket.onerror = () => {
          // O evento close agenda uma nova tentativa com um novo ticket.
          socket?.close();
        };

        socket.onclose = () => {
          socket = null;
          if (!stopped) scheduleReconnect();
        };
      } catch (error) {
        console.error('Não foi possível conectar ao canal em tempo real:', error);
        scheduleReconnect();
      }
    };

    connectSocket();

    return () => {
      stopped = true;
      notifyStatus('disconnected');

      if (reconnectTimer) {
        window.clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }

      if (socket && socket.readyState < WebSocket.CLOSING) {
        socket.close(1000, 'Página encerrada');
      }
      socket = null;
    };
  }
}

export default new RealtimeService();
