FROM public.ecr.aws/docker/library/node:20 as builder

WORKDIR /workdir
COPY package.json .
RUN npm install
COPY server/ ./server/
COPY public/ ./public/
COPY nuxt.config.ts tsconfig.json ./
COPY pages/ ./pages/
RUN rm -rf .output && npm run build

# -----
FROM public.ecr.aws/lambda/nodejs:20

COPY --from=builder /workdir/.output/ ${LAMBDA_TASK_ROOT}
CMD [ "server/index.handler" ]