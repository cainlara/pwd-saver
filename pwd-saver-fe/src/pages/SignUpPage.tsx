import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { ApiError } from '../api/httpClient';
import { useAuth } from '../features/auth/useAuth';
import formStyles from '../styles/form.module.css';

export function SignUpPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(usernameOrEmail, password);
      navigate('/sign-in', {
        replace: true,
        state: { reason: 'registered' },
      });
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : 'We could not create your account. Please try again.',
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="Get started"
      title="Create your vault"
      footer={
        <>
          Already have an account? <Link to="/sign-in">Sign in</Link>
        </>
      }
    >
      <form className={formStyles.form} onSubmit={handleSubmit} noValidate>
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
            autoComplete="new-password"
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
          {submitting ? 'Creating account…' : 'Sign up'}
        </button>
      </form>
    </AuthLayout>
  );
}
