// Mudar esta linha:
// const API_URL = '/api/receita';

// Para esta:
const API_URL = 'http://localhost:8080/api/receita';

class ReceitaService {
    async processarReceita(imagemBase64, nomeArquivo) {
        const token = localStorage.getItem('token');

        const response = await fetch(`${API_URL}/processar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` })
            },
            body: JSON.stringify({ imagemBase64, nomeArquivo })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.erro || 'Erro ao processar receita');
        }

        return response.json();
    }

    async healthCheck() {
        const response = await fetch(`${API_URL}/health`);
        return response.json();
    }
}

export default new ReceitaService();