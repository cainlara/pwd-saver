interface EyeOffIconProps {
  className?: string;
}

export function EyeOffIcon({ className }: EyeOffIconProps) {
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
      <path d="M2.7 3.3l14 14" />
      <path d="M8.3 5.2c.55-.14 1.12-.2 1.7-.2 5.5 0 8.5 6 8.5 6a15.2 15.2 0 0 1-2.8 3.6" />
      <path d="M5.6 5.7C3.1 7.2 1.5 10 1.5 10s3 6 8.5 6c1.1 0 2.1-.2 3-.6" />
      <path d="M8.1 10a1.9 1.9 0 0 0 2.7 2.7" />
    </svg>
  );
}
