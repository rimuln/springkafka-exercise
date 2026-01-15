FROM node:24-alpine

WORKDIR /app

# Kopírujeme soubory se závislostmi
COPY package*.json ./

# Instalujeme závislosti
RUN npm install

# Kopírujeme zbytek kódu
COPY . .

# Exponujeme port 3000 (aby odpovídal CrossOrigin v Javě)
EXPOSE 3000

# Spustíme Vite a vynutíme port 3000 a hostování pro Docker
CMD ["npm", "run", "dev", "--", "--port", "3000", "--host"]