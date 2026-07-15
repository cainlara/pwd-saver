export function validateApiBaseUrl(value: string | undefined): string {
  if (!value) {
    throw new Error(
      'Missing required VITE_API_BASE_URL configuration value. Set it in .env, .env.local, or .env.[mode].local.',
    );
  }

  try {
    new URL(value);
  } catch {
    throw new Error(
      `Invalid VITE_API_BASE_URL "${value}": must be an absolute URL (e.g. http://localhost:9090).`,
    );
  }

  return value;
}
