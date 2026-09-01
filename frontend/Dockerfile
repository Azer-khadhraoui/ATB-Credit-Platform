# --- Build stage -------------------------------------------------------
FROM node:26-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# --- Runtime stage -------------------------------------------------------
FROM nginx:1.31-alpine

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

# nginx:alpine already runs its worker processes as the unprivileged "nginx" user.
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
