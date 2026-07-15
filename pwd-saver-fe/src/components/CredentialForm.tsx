import { useState, type FormEvent } from 'react';
import type {
  CredentialEntry,
  CredentialInput,
} from '../features/credentials/api';
import formStyles from '../styles/form.module.css';
import styles from './CredentialForm.module.css';

interface CredentialFormProps {
  title: string;
  initialValue?: CredentialEntry;
  submitLabel: string;
  onSubmit: (input: CredentialInput) => Promise<void>;
  onCancel: () => void;
}

export function CredentialForm({
  title,
  initialValue,
  submitLabel,
  onSubmit,
  onCancel,
}: CredentialFormProps) {
  const [username, setUsername] = useState(initialValue?.username ?? '');
  const [password, setPassword] = useState(initialValue?.password ?? '');
  const [url, setUrl] = useState(initialValue?.url ?? '');
  const [description, setDescription] = useState(
    initialValue?.description ?? '',
  );
  const [validationError, setValidationError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError(null);

    if (!username.trim() || !password.trim()) {
      setValidationError('Username and password are required.');
      return;
    }
    setValidationError(null);
    setSubmitting(true);

    try {
      await onSubmit({
        username: username.trim(),
        password,
        url: url.trim() || undefined,
        description: description.trim() || undefined,
      });
    } catch (err) {
      setSubmitError(
        err instanceof Error
          ? err.message
          : 'We could not save this credential. Please try again.',
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className={formStyles.form} onSubmit={handleSubmit} noValidate>
      <h2 className={styles.title}>{title}</h2>

      {validationError ? (
        <p className={formStyles.formError}>{validationError}</p>
      ) : null}
      {submitError ? <p className={formStyles.formError}>{submitError}</p> : null}

      <div className={formStyles.field}>
        <label className={formStyles.label} htmlFor="cred-username">
          Username
        </label>
        <input
          id="cred-username"
          className={formStyles.input}
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          required
        />
      </div>

      <div className={formStyles.field}>
        <label className={formStyles.label} htmlFor="cred-password">
          Password
        </label>
        <input
          id="cred-password"
          type="password"
          className={formStyles.input}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />
      </div>

      <div className={formStyles.field}>
        <label className={formStyles.label} htmlFor="cred-url">
          Site / URL (optional)
        </label>
        <input
          id="cred-url"
          className={formStyles.input}
          value={url}
          onChange={(event) => setUrl(event.target.value)}
        />
      </div>

      <div className={formStyles.field}>
        <label className={formStyles.label} htmlFor="cred-description">
          Description (optional)
        </label>
        <input
          id="cred-description"
          className={formStyles.input}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
      </div>

      <div className={styles.actions}>
        <button
          type="button"
          className={styles.cancelButton}
          onClick={onCancel}
        >
          Cancel
        </button>
        <button
          type="submit"
          className={formStyles.submitButton}
          disabled={submitting}
        >
          {submitting ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
