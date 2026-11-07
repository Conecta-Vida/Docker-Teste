import axios from 'axios';

// Cria a instância de comunicação com o Spring Boot
export const api = axios.create({
  baseURL: 'http://localhost:8080', // O endereço exato do seu Java
  headers: {
    'Content-Type': 'application/json',
  },
});