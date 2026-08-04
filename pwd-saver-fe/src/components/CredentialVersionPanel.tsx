import { useState } from 'react';
import { ApiError } from '../api/httpClient';
import type { CredentialEntry, CredentialVersion } from '../features/credentials/api';
import styles from './CredentialVersionPanel.module.css';

interface CredentialVersionPanelProps {
  credential: CredentialEntry;
  versions: CredentialVersion[] | undefined;
  isLoading: boolean;
  isError: boolean;
  error: unknown;
  onClose: () => void;
}

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
});

function formatCreatedAt(createdAt: string): string {
  return dateFormatter.format(new Date(createdAt));
}

export function CredentialVersionPanel({
  credential,
  versions,
  isLoading,
  isError,
  error,
  onClose,
}: CredentialVersionPanelProps) {
  const [revealedVersionIds, setRevealedVersionIds] = useState<Set<string>>(
    new Set(),
  );

  function toggleReveal(versionId: string) {
    setRevealedVersionIds((current) => {
      const next = new Set(current);
      if (next.has(versionId)) {
        next.delete(versionId);
      } else {
        next.add(versionId);
      }
      return next;
    });
  }

  const allRevealed =
    versions !== undefined &&
    versions.length > 0 &&
    revealedVersionIds.size === versions.length;

  function handleToggleAll() {
    if (!versions) {
      return;
    }
    setRevealedVersionIds(
      allRevealed ? new Set() : new Set(versions.map((v) => v.versionId)),
    );
  }

  return (
    <aside
      className={styles.panel}
      aria-label={`Version history for ${credential.username}`}
    >
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>Version history</h2>
          <p className={styles.subtitle}>{credential.username}</p>
        </div>
        <button
          type="button"
          className={styles.closeButton}
          onClick={onClose}
          aria-label="Close version history"
        >
          Close
        </button>
      </div>

      {isLoading ? (
        <p className={styles.status}>Loading version history…</p>
      ) : null}

      {isError ? (
        <p className={styles.error}>
          {error instanceof ApiError
            ? error.message
            : "We could not load this credential's version history. Please try again."}
        </p>
      ) : null}

      {!isLoading && !isError && versions && versions.length > 0 ? (
        <div className={styles.toolbar}>
          <button
            type="button"
            className={styles.toggleAllButton}
            onClick={handleToggleAll}
            aria-label={allRevealed ? 'Hide all passwords' : 'Reveal all passwords'}
          >
            {allRevealed ? 'Hide all' : 'Reveal all'}
          </button>
        </div>
      ) : null}

      {!isLoading && !isError && versions ? (
        <ul className={styles.list}>
          {versions.map((version, index) => {
            const revealed = revealedVersionIds.has(version.versionId);
            return (
              <li key={version.versionId} className={styles.item}>
                <div className={styles.itemHeader}>
                  <span className={styles.username}>{version.username}</span>
                  {index === 0 ? (
                    <span className={styles.currentBadge}>Current</span>
                  ) : null}
                </div>

                <p className={styles.timestamp}>
                  {formatCreatedAt(version.createdAt)}
                </p>

                <div className={styles.passwordRow}>
                  <span className={styles.password}>
                    {revealed ? version.password : '••••••••'}
                  </span>
                  <button
                    type="button"
                    className={styles.revealButton}
                    onClick={() => toggleReveal(version.versionId)}
                    aria-label={revealed ? 'Hide password' : 'Reveal password'}
                  >
                    {revealed ? 'Hide' : 'Reveal'}
                  </button>
                </div>

                {version.url ? (
                  <p className={styles.url}>{version.url}</p>
                ) : null}
                {version.description ? (
                  <p className={styles.description}>{version.description}</p>
                ) : null}
              </li>
            );
          })}
        </ul>
      ) : null}
    </aside>
  );
}
