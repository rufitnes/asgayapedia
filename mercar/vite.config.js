import { defineConfig } from 'vite';

export default defineConfig({
  base: '/',
  publicDir: 'public',
  esbuild: { target: 'esnext' },
  optimizeDeps: { esbuildOptions: { target: 'esnext' } },
  build: { target: 'esnext' },
});
