import { useState } from 'react';
import type { CredentialEntry } from '../features/credentials/api';
import styles from './CredentialCard.module.css';

interface CredentialCardProps {
  credential: CredentialEntry;
  onEdit?: (credential: CredentialEntry) => void;
  onDelete?: (credential: CredentialEntry) => void;
  onViewHistory?: (credential: CredentialEntry) => void;
}

const URL_SCHEME_PATTERN = /^[a-z][a-z\d+.-]*:/i;

function toAbsoluteHref(url: string): string {
  return URL_SCHEME_PATTERN.test(url) ? url : `https://${url}`;
}

export function CredentialCard({
  credential,
  onEdit,
  onDelete,
  onViewHistory,
}: CredentialCardProps) {
  const [revealed, setRevealed] = useState(false);
  const [urlTooltipVisible, setUrlTooltipVisible] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard
      .writeText(credential.password)
      .then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1500);
      })
      .catch(() => {});
  };

  return (
    <article className={styles.card}>
      <div className={styles.header}>
        <h3 className={styles.username}>{credential.username}</h3>
        {credential.url ? (
          <span
            className={styles.urlWrapper}
            onMouseOver={() => setUrlTooltipVisible(true)}
            onMouseOut={() => setUrlTooltipVisible(false)}
          >
            <a
              className={styles.url}
              href={toAbsoluteHref(credential.url)}
              target="_blank"
              rel="noreferrer"
              aria-label={credential.url}
            >
              🌐
            </a>
            {urlTooltipVisible ? (
              <span className={styles.urlTooltip} role="tooltip">
                {credential.url}
              </span>
            ) : null}
          </span>
        ) : null}
      </div>

      <div className={styles.passwordRow}>
        <span className={styles.password}>
          {revealed ? credential.password : '••••••••'}
        </span>
        <button
          type="button"
          className={styles.revealButton}
          onClick={() => setRevealed((current) => !current)}
          aria-label={revealed ? 'Hide password' : 'Reveal password'}
        >
          {revealed ? 'Hide' : 'Reveal'}
        </button>
        <button
          type="button"
          className={styles.revealButton}
          onClick={handleCopy}
          aria-label="Copy password"
        >
          {copied ? 'Copied' : 'Copy'}
        </button>
      </div>

      {credential.description ? (
        <p className={styles.description}>{credential.description}</p>
      ) : null}

      {onEdit || onDelete || onViewHistory ? (
        <div className={styles.actions}>
          {onEdit ? (
            <button
              type="button"
              className={styles.actionButton}
              onClick={() => onEdit(credential)}
            >
              Edit
            </button>
          ) : null}
          {onViewHistory ? (
            <button
              type="button"
              className={styles.actionButton}
              onClick={() => onViewHistory(credential)}
            >
              History
            </button>
          ) : null}
          {onDelete ? (
            <button
              type="button"
              className={styles.dangerButton}
              onClick={() => onDelete(credential)}
            >
              Delete
            </button>
          ) : null}
        </div>
      ) : null}
    </article>
  );
}
