// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  ssr: false,
  nitro: {
    preset: "node",
    serveStatic: true,
    output: {
      dir: `${__dirname}/../../../target/.output`
    }
  }
});
