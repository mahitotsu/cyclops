// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  nitro: {
    preset: 'static',
    output: {
      publicDir: '../../../target/classes/static',
    }
  },
})
