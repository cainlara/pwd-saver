interface CopyIconProps {
  className?: string;
}

export function CopyIcon({ className }: CopyIconProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      <rect x="7" y="7" width="10.5" height="10.5" rx="1.5" />
      <path d="M12.5 7V4.5A1.5 1.5 0 0 0 11 3H4a1.5 1.5 0 0 0-1.5 1.5V12A1.5 1.5 0 0 0 4 13.5h2.5" />
    </svg>
  );
}
