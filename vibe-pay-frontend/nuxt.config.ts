// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  modules: ['vuetify-nuxt-module'],
  
  // 백엔드 API 프록시 설정
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
  
  // Vuetify 테마 설정 (토스 스타일)
  vuetify: {
    vuetifyOptions: {
      theme: {
        defaultTheme: 'light',
        themes: {
          light: {
            colors: {
              primary: '#0064FF', // 토스 블루
              secondary: '#00C896', // 토스 그린  
              accent: '#FF6B6B',
              error: '#FF4757',
              warning: '#FFA726',
              info: '#26C6DA',
              success: '#00C896',
              background: '#FAFAFA',
              surface: '#FFFFFF'
            }
          }
        }
      }
    }
  },
  
  // 글로벌 CSS
  css: ['@mdi/font/css/materialdesignicons.css'],
})