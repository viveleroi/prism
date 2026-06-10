import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, NavLink } from "react-router";
import { ActivitiesPage } from "./pages/activities/activities";
import { QueuePage } from "./pages/queue/queue";
import { ApiKeyModal } from "./components/key-prompt-modal/key-prompt-modal";
import { ErrorPage } from "./components/error-page/error-page";
import { AuthProvider, useAuth } from "./auth/auth-context";
import { Banner } from "./components/banner/banner";
import { StatusDropdown } from "./components/status/status";
import { Button } from "@/components/ui/button";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
  },
});

// BASE_URL is slash-terminated ("/" or "/prism/"); React Router wants no trailing slash, except root.
const routerBasename = import.meta.env.BASE_URL.replace(/\/$/, "") || "/";

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter basename={routerBasename}>
          <Banner />
          <Content />
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

function Content() {
  const { phase, setKey, signOut } = useAuth();

  switch (phase) {
    case "error":
      return <ErrorPage onRetry={signOut} />;
    case "unauthenticated":
      return <ApiKeyModal onConnect={setKey} />;
    case "validating":
      return null;
    case "authenticated":
      return <Dashboard />;
  }
}

function Dashboard() {
  return (
    <>
      <header className="relative z-10 border-b bg-card/50 backdrop-blur-[18px]">
        <div className="py-4 px-8 flex items-center gap-4 max-w-[1240px] mx-auto">
          <img src={`${import.meta.env.BASE_URL}prism-retro-square-sm.png`} alt="" className="h-8 w-auto" />
          <h1 className="text-brand text-2xl mr-4">prism</h1>
          <nav className="flex gap-2">
            <NavLink to="/">
              {({ isActive }) => (
                <Button variant={isActive ? "secondary" : "outline"} className="rounded-full">
                  Activities
                </Button>
              )}
            </NavLink>
            <NavLink to="/queue">
              {({ isActive }) => (
                <Button variant={isActive ? "secondary" : "outline"} className="rounded-full">
                  Recording Queue
                </Button>
              )}
            </NavLink>
          </nav>
          <a
            href="https://docs.prism-mc.org"
            target="_blank"
            rel="noreferrer noopener"
            className="ml-auto text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Docs
          </a>
          <a
            href="https://ci.prism-mc.org"
            target="_blank"
            rel="noreferrer noopener"
            className="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Dev Builds
          </a>
          <StatusDropdown />
        </div>
      </header>
      <main className="py-6 px-8 max-w-[1240px] mx-auto">
        <Routes>
          <Route path="/" element={<ActivitiesPage />} />
          <Route path="/queue" element={<QueuePage />} />
        </Routes>
      </main>
    </>
  );
}
