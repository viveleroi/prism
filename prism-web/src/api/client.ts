const API_KEY_STORAGE_KEY = "prism-api-key";

type AuthErrorListener = () => void;
const authErrorListeners = new Set<AuthErrorListener>();

/**
 * Subscribe to be notified whenever a request is rejected with a 401. Returns an
 * unsubscribe function.
 */
export function onAuthError(listener: AuthErrorListener): () => void {
  authErrorListeners.add(listener);
  return () => {
    authErrorListeners.delete(listener);
  };
}

export function getApiKey(): string {
  return localStorage.getItem(API_KEY_STORAGE_KEY) ?? "";
}

export function setApiKey(key: string): void {
  localStorage.setItem(API_KEY_STORAGE_KEY, key);
}

export function clearApiKey(): void {
  localStorage.removeItem(API_KEY_STORAGE_KEY);
}

export async function apiFetch<T>(path: string): Promise<T> {
  // Resolve the request against the app's base URL so it works when hosted under a sub-path.
  // BASE_URL is slash-terminated ("/" or "/prism/"); strip the path's leading slash to join cleanly.
  const url = `${import.meta.env.BASE_URL}${path.replace(/^\//, "")}`;
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${getApiKey()}` },
  });

  if (res.status === 401) {
    clearApiKey();
    authErrorListeners.forEach((listener) => listener());

    throw new AuthError();
  }

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
  }

  return res.json();
}

export class AuthError extends Error {
  constructor() {
    super("Unauthorized");
    this.name = "AuthError";
  }
}
