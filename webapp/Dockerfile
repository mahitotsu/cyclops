FROM public.ecr.aws/docker/library/node:18

WORKDIR /workdir
COPY package.json ./
RUN npm i
COPY public server app.vue nuxt.config.ts tsconfig.json ./
RUN npm run build