import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import styles from './AuthLayout.module.css';

interface AuthLayoutProps {
  eyebrow: string;
  title: string;
  children: ReactNode;
  footer: ReactNode;
}

export function AuthLayout({
  eyebrow,
  title,
  children,
  footer,
}: AuthLayoutProps) {
  return (
    <div className={styles.page}>
      <div className={styles.glow} aria-hidden="true" />
      <div className={styles.card}>
        <Link to="/" className={styles.logo}>
          Pwd Saver
        </Link>
        <p className={styles.eyebrow}>{eyebrow}</p>
        <h1 className={styles.title}>{title}</h1>
        {children}
        <div className={styles.footer}>{footer}</div>
      </div>
    </div>
  );
}
