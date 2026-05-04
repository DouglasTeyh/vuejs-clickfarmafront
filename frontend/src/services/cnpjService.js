import axios from 'axios';

/**
 * Serviço para consulta de CNPJ.
 * Nota: APIs de CNPJ geralmente possuem restrições de CORS no navegador.
 * Este serviço tenta usar a ReceitaWS (via proxy ou direto se suportado).
 */
export const cnpjService = {
  /**
   * Consulta um CNPJ e verifica se está ativo.
   * @param {string} cnpj - O CNPJ a ser consultado (apenas números).
   * @returns {Promise<Object>} - Dados da empresa.
   */
  async buscarCnpj(cnpj) {
    const cleanCnpj = cnpj.replace(/\D/g, '');
    if (cleanCnpj.length !== 14) {
      throw new Error('CNPJ inválido. Deve conter 14 dígitos.');
    }

    try {
      // Usando BrasilAPI (mais estável e sem problemas graves de CORS para frontend)
      const response = await axios.get(`https://brasilapi.com.br/api/cnpj/v1/${cleanCnpj}`);
      
      const data = response.data;
      
      // No BrasilAPI, a situação cadastral é um número (2 = Ativa) ou string
      const status = data.descricao_situacao_cadastral || data.situacao_cadastral;
      
      return {
        nome: data.razao_social,
        fantasia: data.nome_fantasia || data.razao_social,
        cnpj: data.cnpj,
        situacao: status,
        logradouro: data.logradouro,
        numero: data.numero,
        bairro: data.bairro,
        cep: data.cep,
        cidade: data.municipio,
        estado: data.uf
      };
    } catch (error) {
      console.error('Erro ao buscar CNPJ:', error);
      if (error.response && error.response.status === 404) {
        throw new Error('CNPJ não encontrado.');
      }
      if (error.response && error.response.status === 400) {
        throw new Error('CNPJ inválido ou formatado incorretamente.');
      }
      if (error.response && error.response.status === 429) {
        throw new Error('Muitas consultas. Aguarde um momento.');
      }
      throw new Error('Erro ao validar CNPJ. Verifique se o número está correto.');
    }
  }
};

export default cnpjService;
