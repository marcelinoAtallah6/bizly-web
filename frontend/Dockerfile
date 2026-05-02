# Stage 1: Build the Angular app
FROM node:20.5.1 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --force
COPY . .
RUN npm run build --prod

# Stage 2: Serve the app using NGINX
FROM nginx:alpine
COPY --from=builder /app/dist/tsp /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
