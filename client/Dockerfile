FROM docker.io/node:18-alpine as build
WORKDIR /app

# Set npm configuration for better stability
RUN npm config set registry https://registry.npmjs.org/ && \
    npm config set fetch-retry-mintimeout 20000 && \
    npm config set fetch-retry-maxtimeout 120000 && \
    npm config set fetch-retries 3

# Copy package files
COPY package*.json ./

# Install dependencies with optimizations
RUN npm ci --only=production=false --no-audit --no-fund --prefer-offline --progress=false

# Copy source code
COPY . .

# Build the application
RUN npm run build --prod

FROM docker.io/nginx:alpine
COPY --from=build /app/dist/client/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
