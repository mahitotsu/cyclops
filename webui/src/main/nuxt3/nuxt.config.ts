export default defineNuxtConfig({
  devtools: { enabled: true },
  nitro: {
    preset: "static",
    output: {
      dir: `${__dirname}/../../../target/output`,
      publicDir: `${__dirname}/../webapp`,
    }
  },
})
