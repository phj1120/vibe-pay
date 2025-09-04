// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  modules: ['vuetify-nuxt-module'],
  vite: {
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        }
      }
    }
  },
  vuetify: {
    vuetifyOptions: {
      theme: {
        defaultTheme: 'dark'
      }
    }
  },
  css: ['@mdi/font/css/materialdesignicons.css'],
})