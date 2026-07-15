import { Link } from 'react-router-dom';
import styles from './LandingPage.module.css';

export function LandingPage() {
  return (
    <div className={styles.page}>
      <div className={styles.glow} aria-hidden="true" />
      <header className={styles.header}>
        <span className={styles.logo}>Pwd Saver</span>
      </header>
      <main className={styles.hero}>
        <p className={styles.eyebrow}>Your secrets, kept safe</p>
        <h1 className={styles.title}>
          One vault for every password you own.
        </h1>
        <p className={styles.subtitle}>
          Store, organize, and reveal your credentials on demand — nothing
          leaves your account.
        </p>
        <div className={styles.actions}>
          <Link to="/sign-in" className={styles.primaryLink}>
            Sign In
          </Link>
          <Link to="/sign-up" className={styles.secondaryLink}>
            Sign Up
          </Link>
        </div>
      </main>
    </div>
  );
}
