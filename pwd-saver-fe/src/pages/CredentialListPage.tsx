import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { CredentialCard } from '../components/CredentialCard';
import { CredentialForm } from '../components/CredentialForm';
import { CredentialVersionPanel } from '../components/CredentialVersionPanel';
import { Modal } from '../components/Modal';
import { ApiError } from '../api/httpClient';
import { useAuth } from '../features/auth/useAuth';
import type { CredentialEntry, CredentialInput } from '../features/credentials/api';
import {
  useCreateCredential,
  useCredentials,
  useDeleteCredential,
  useUpdateCredential,
} from '../features/credentials/useCredentials';
import { useCredentialVersions } from '../features/credentials/useCredentialVersions';
import styles from './CredentialListPage.module.css';

export function CredentialListPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { data, isLoading, isError, error } = useCredentials();
  const createCredential = useCreateCredential();
  const updateCredential = useUpdateCredential();
  const deleteCredential = useDeleteCredential();
  const [isAdding, setIsAdding] = useState(false);
  const [editingCredential, setEditingCredential] =
    useState<CredentialEntry | null>(null);
  const [deletingCredential, setDeletingCredential] =
    useState<CredentialEntry | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [selectedCredentialId, setSelectedCredentialId] = useState<
    string | null
  >(null);
  const selectedCredential =
    data?.find((credential) => credential.id === selectedCredentialId) ??
    null;
  const credentialVersions = useCredentialVersions(selectedCredentialId);

  async function handleSignOut() {
    await logout();
    navigate('/', { replace: true });
  }

  async function handleCreate(input: CredentialInput) {
    await createCredential.mutateAsync(input);
    setIsAdding(false);
  }

  async function handleUpdate(input: CredentialInput) {
    if (!editingCredential) return;
    await updateCredential.mutateAsync({ id: editingCredential.id, input });
    setEditingCredential(null);
  }

  async function handleDelete() {
    if (!deletingCredential) return;
    setDeleteError(null);
    try {
      await deleteCredential.mutateAsync(deletingCredential.id);
      setDeletingCredential(null);
    } catch (err) {
      setDeleteError(
        err instanceof ApiError
          ? err.message
          : 'We could not delete this credential. Please try again.',
      );
    }
  }

  function handleCancelDelete() {
    setDeletingCredential(null);
    setDeleteError(null);
  }

  return (
    <div
      className={
        selectedCredential
          ? `${styles.page} ${styles.pageWithPanel}`
          : styles.page
      }
    >
      <header className={styles.header}>
        <span className={styles.logo}>Pwd Saver</span>
        <div className={styles.headerRight}>
          {user ? (
            <span className={styles.user}>{user.usernameOrEmail}</span>
          ) : null}
          <button
            type="button"
            className={styles.signOutButton}
            onClick={handleSignOut}
          >
            Sign out
          </button>
        </div>
      </header>

      <main className={styles.main}>
        <div className={styles.titleRow}>
          <h1 className={styles.title}>Your credentials</h1>
          <button
            type="button"
            className={styles.addButton}
            onClick={() => setIsAdding(true)}
          >
            Add credential
          </button>
        </div>

        {isLoading ? (
          <p className={styles.status}>Loading your vault…</p>
        ) : null}

        {isError ? (
          <p className={styles.error}>
            {error instanceof ApiError
              ? error.message
              : 'We could not load your credentials. Please try again.'}
          </p>
        ) : null}

        {!isLoading && !isError && data?.length === 0 ? (
          <div className={styles.empty}>
            <p>Your vault is empty.</p>
            <p className={styles.emptyHint}>
              Add your first credential to get started.
            </p>
          </div>
        ) : null}

        {data && data.length > 0 ? (
          <div className={styles.grid}>
            {data.map((credential) => (
              <CredentialCard
                key={credential.id}
                credential={credential}
                onEdit={setEditingCredential}
                onDelete={setDeletingCredential}
                onViewHistory={(selected) =>
                  setSelectedCredentialId(selected.id)
                }
              />
            ))}
          </div>
        ) : null}
      </main>

      {isAdding ? (
        <Modal onClose={() => setIsAdding(false)}>
          <CredentialForm
            title="Add credential"
            submitLabel="Add credential"
            onSubmit={handleCreate}
            onCancel={() => setIsAdding(false)}
          />
        </Modal>
      ) : null}

      {editingCredential ? (
        <Modal onClose={() => setEditingCredential(null)}>
          <CredentialForm
            title="Edit credential"
            initialValue={editingCredential}
            submitLabel="Save changes"
            onSubmit={handleUpdate}
            onCancel={() => setEditingCredential(null)}
          />
        </Modal>
      ) : null}

      {deletingCredential ? (
        <ConfirmDialog
          title="Delete credential"
          message={`Delete the credential for "${deletingCredential.username}"? This cannot be undone.`}
          confirmLabel="Delete"
          error={deleteError}
          onConfirm={handleDelete}
          onCancel={handleCancelDelete}
        />
      ) : null}

      {selectedCredential ? (
        <CredentialVersionPanel
          key={selectedCredential.id}
          credential={selectedCredential}
          versions={credentialVersions.data}
          isLoading={credentialVersions.isLoading}
          isError={credentialVersions.isError}
          error={credentialVersions.error}
          onClose={() => setSelectedCredentialId(null)}
        />
      ) : null}
    </div>
  );
}
