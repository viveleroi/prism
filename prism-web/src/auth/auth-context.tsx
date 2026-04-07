import { createContext, useContext, useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { clearApiKey, getApiKey, onAuthError, setApiKey } from "../api/client";
import { fetchStatus } from "../api/queries";
import type { StatusResponse } from "../api/types";

type AuthPhase = "unauthenticated" | "validating" | "authenticated" | "error";

interface AuthContextValue {
  /** Which top-level view should render. */
  phase: AuthPhase;
  /** Latest server status; populated once the key is validated. */
  status: StatusResponse | undefined;
  /** A non-auth status error (auth failures move the phase to "error" instead). */
  statusError: Error | null;
  /** Store a key and begin validating it. */
  setKey: (key: string) => void;
  /** Clear the key and return to the unauthenticated (key prompt) state. */
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [hasKey, setHasKey] = useState(() => !!getApiKey());
  const [authError, setAuthError] = useState(false);

  // A 401 from any request (this status probe or a dashboard query) clears the
  // key and flips us to the error view.
  useEffect(() => onAuthError(() => setAuthError(true)), []);

  // Keep in sync when another tab signs in or out.
  useEffect(() => {
    const handler = () => setHasKey(!!getApiKey());
    window.addEventListener("storage", handler);
    return () => window.removeEventListener("storage", handler);
  }, []);

  // The status query doubles as the key validation: it only runs once a key is
  // present, and the dashboard waits for it to succeed before rendering.
  const { data, error, isSuccess } = useQuery({
    queryKey: ["status"],
    queryFn: fetchStatus,
    enabled: hasKey && !authError,
    refetchInterval: 30000,
  });

  // A 401 surfaces via authError (set above) rather than this error, so any
  // error remaining here is non-auth and should not block the dashboard.
  const nonAuthError = authError ? null : error;

  let phase: AuthPhase;
  if (authError) {
    phase = "error";
  } else if (!hasKey) {
    phase = "unauthenticated";
  } else if (isSuccess || nonAuthError) {
    phase = "authenticated";
  } else {
    phase = "validating";
  }

  const value: AuthContextValue = {
    phase,
    status: data,
    statusError: nonAuthError,
    setKey: (key: string) => {
      setApiKey(key);
      setAuthError(false);
      setHasKey(true);
    },
    signOut: () => {
      clearApiKey();
      setAuthError(false);
      setHasKey(false);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
