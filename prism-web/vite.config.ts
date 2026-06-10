import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "path";

export default defineConfig(({ command }) => ({
  // Production builds bake a placeholder base into asset/API URLs that the plugin replaces at
  // serve time with the configured web.base-path (default "/"). Dev is always served from root.
  base: command === "build" ? "/__PRISM_BASE_PATH__/" : "/",
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    outDir: path.resolve(__dirname, "../prism-paper/src/main/resources/web"),
    emptyOutDir: true,
  },
  server: {
    port: 3000,
    proxy: {
      "/api": {
        target: "http://localhost:4040",
        changeOrigin: true,
      },
    },
  },
}));
