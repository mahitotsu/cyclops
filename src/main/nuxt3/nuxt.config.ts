export default defineNuxtConfig({
  /*
  app:{
    baseURL: '/webapp-0.0.1-SNAPSHOT/'
  },
  */
  devtools: { enabled: true },
  ssr: false,
  nitro: {
    preset: 'static',
    static: true,
    serveStatic: true
  }
})
