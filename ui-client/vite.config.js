import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {  // Načte proměnné z .env souborů, pokud existují
  const env = loadEnv(mode, process.cwd(), '');
  const proxyTarget = process.env.VITE_API_URL || env.VITE_API_URL || 'http://localhost:8080';
  console.log('Proxy targeting to:', proxyTarget);

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  }
})
