import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    host: "0.0.0.0",
    port: 18080,
    proxy: {
      "/api": {
        target: "http://localhost:18081",
        changeOrigin: true,
      },
      "/health": {
        target: "http://localhost:18081",
        changeOrigin: true,
      },
      "/api-docs": {
        target: "http://localhost:18081",
        changeOrigin: true,
      },
    },
  },
});
