import path from "path"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

export default defineConfig({
  plugins: [react()],
  // Em "npm run dev", /api é encaminhado para a admin-api local (porta 8080)
  server: {
    proxy: {
      "/api": process.env.VITE_PROXY_TARGET || "http://localhost:8080",
    },
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
})