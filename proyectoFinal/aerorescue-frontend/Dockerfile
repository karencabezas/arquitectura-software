# ── Stage 1: Build ──────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /app

# Cache npm install layer
COPY package.json package-lock.json* ./
RUN npm install

# Build production bundle
COPY . .
RUN npm run build:prod

# ── Stage 2: Serve with Nginx ────────────────────────────
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static files
RUN rm -rf ./*

# Copy built Angular app
COPY --from=builder /app/dist/aerorescue-frontend/browser .

# Nginx config for Angular SPA (handle client-side routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
