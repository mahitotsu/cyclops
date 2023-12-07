FROM public.ecr.aws/docker/library/node:20

WORKDIR /workdir
COPY package.json .
RUN npm install

COPY ./public/ ./public/
COPY nuxt.config.ts tsconfig.json ./
COPY ./server/ ./server/
COPY ./pages/ ./pages/
RUN npm run build
COPY index.mjs /workdir

RUN mkdir -p /asset/.output\
 && cp -r /workdir/.output /asset/\
 && cp /workdir/index.mjs /asset/
