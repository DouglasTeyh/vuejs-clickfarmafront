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
      // Usando ReceitaWS via proxy (ou direto se for um ambiente que permita)
      // Nota: Em produção, o ideal é fazer essa chamada pelo BACKEND para evitar CORS e expor chaves.
      const response = await axios.get(`https://publica.cnpj.ws/cnpj/${cleanCnpj}`);
      
      const data = response.data;
      const status = data.estabelecimento.situacao_cadastral;
      
      if (status !== 'Ativa' && status !== 'ATIVA') {
        throw new Error(`Este CNPJ não está ativo (Status: ${status}).`);
      }

      return {
        nome: data.razao_social,
        fantasia: data.estabelecimento.nome_fantasia || data.razao_social,
        cnpj: data.cnpj,
        situacao: status,
        logradouro: data.estabelecimento.tipo_logradouro + ' ' + data.estabelecimento.logradouro,
        numero: data.estabelecimento.numero,
        bairro: data.estabelecimento.bairro,
        cep: data.estabelecimento.cep,
        cidade: data.estabelecimento.cidade.nome,
        estado: data.estabelecimento.estado.sigla
      };
    } catch (error) {
      console.error('Erro ao buscar CNPJ:', error);
      if (error.response && error.response.status === 429) {
        throw new Error('Muitas consultas. Aguarde um momento e tente novamente.');
      }
      throw new Error(error.message || 'Erro ao validar CNPJ.');
    }
  }
};

export default cnpjService;
