# Etapa 1: compilación de Angular
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# Clave de Google Maps JavaScript API (se toma de GOOGLE_MAPS_API_KEY en el .env vía docker-compose)
ARG GOOGLE_MAPS_API_KEY=""
RUN sed -i "s|googleMapsApiKey: ''|googleMapsApiKey: '${GOOGLE_MAPS_API_KEY}'|" src/environments/environment.ts \
    && npx ng build --configuration production

# Etapa 2: servidor web estático
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/savefrut-app/browser /usr/share/nginx/html
EXPOSE 80
