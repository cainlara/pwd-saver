import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { ApiError } from '../api/httpClient';
import { useAuth } from '../features/auth/useAuth';
import formStyles from '../styles/form.module.css';

export function SignInPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const routeState = location.state as { reason?: string } | null;
  const sessionExpired = routeState?.reason === 'session-expired';
  const signInRequired = routeState?.reason === 'sign-in-required';
  const justRegistered = routeState?.reason === 'registered';

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(usernameOrEmail, password);
      navigate('/credentials', { replace: true });
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : 'We could not sign you in. Please try again.',
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="Welcome back"
      title="Sign in to your vault"
      footer={
        <>
          Don&apos;t have an account? <Link to="/sign-up">Sign up</Link>
        </>
      }
    >
      <form className={formStyles.form} onSubmit={handleSubmit} noValidate>
        {sessionExpired ? (
          <p className={formStyles.formError}>
            Your session ended. Please sign in again to continue.
          </p>
        ) : null}
        {signInRequired ? (
          <p className={formStyles.formError}>Please sign in to continue.</p>
        ) : null}
        {justRegistered ? (
          <p className={formStyles.formSuccess}>
            Account created. Sign in to continue.
          </p>
        ) : null}
        {error ? <p className={formStyles.formError}>{error}</p> : null}
        <div className={formStyles.field}>
          <label className={formStyles.label} htmlFor="usernameOrEmail">
            Username or email
          </label>
          <input
            id="usernameOrEmail"
            className={formStyles.input}
            type="text"
            autoComplete="username"
            value={usernameOrEmail}
            onChange={(event) => setUsernameOrEmail(event.target.value)}
            required
          />
        </div>
        <div className={formStyles.field}>
          <label className={formStyles.label} htmlFor="password">
            Password
          </label>
          <input
            id="password"
            className={formStyles.input}
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </div>
        <button
          type="submit"
          className={formStyles.submitButton}
          disabled={submitting}
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </AuthLayout>
  );
}
