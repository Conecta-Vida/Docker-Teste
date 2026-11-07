import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

// PARA A EQUIPE: Esta é uma função utilitária padrão gerada pelo "Shadcn UI".
// Ela junta várias classes do TailwindCSS de forma inteligente, removendo conflitos.
// Ex: cn("bg-red-500", "bg-blue-500") vai prevalecer a última (blue-500).
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}