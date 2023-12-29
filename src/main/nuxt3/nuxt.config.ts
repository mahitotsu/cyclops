export default defineNuxtConfig({
  devtools: { enabled: true },
  ssr: false,
  nitro: {
    preset: 'static',
    static: true,
    serveStatic: true,
  } 
})
