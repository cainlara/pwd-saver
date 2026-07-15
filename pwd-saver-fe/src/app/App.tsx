import { BrowserRouter } from 'react-router-dom';
import { SessionProvider } from '../features/auth/SessionContext';
import { QueryProvider } from './QueryProvider';
import { AppRoutes } from './routes';

export function App() {
  return (
    <SessionProvider>
      <QueryProvider>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </QueryProvider>
    </SessionProvider>
  );
}
