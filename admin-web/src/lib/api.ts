import axios from 'axios';

// Cria a instância de comunicação com o Spring Boot
// As rotas já começam com /api. Base vazia = mesmo endereço do site:
//  - Docker/Swarm: o Nginx do admin-web encaminha /api para o serviço admin-api
//  - npm run dev: o Vite encaminha /api para http://localhost:8080
// Para usar outro servidor, defina VITE_API_URL no build (ex.: https://minha-api.com).
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  headers: {
    'Content-Type': 'application/json',
  },
});