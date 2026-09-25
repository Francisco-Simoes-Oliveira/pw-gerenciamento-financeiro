import { useEffect, useRef, useState } from 'react';
import realtimeService from '@/services/realtimeService';

export default function useRealtimeEvents(onEvent) {
  const callbackRef = useRef(onEvent);
  const [status, setStatus] = useState('disconnected');

  useEffect(() => {
    callbackRef.current = onEvent;
  }, [onEvent]);

  useEffect(() => {
    const disconnect = realtimeService.connect(
      (event) => callbackRef.current?.(event),
      setStatus,
    );

    return disconnect;
  }, []);

  return status;
}
