import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: process.env.ELECTRON ? './' : '/',
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/task': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/rag': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/doc': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: process.env.ELECTRON ? 'dist' : '../src/main/resources/static',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        manualChunks: undefined
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})




