import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    // 跨域代理：将 /api 前缀的请求转发到后端，避免浏览器 CORS 限制
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        // 后端 context-path 为 / ，无需 rewrite
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
  },
})
