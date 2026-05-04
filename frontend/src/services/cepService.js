import axios from 'axios';

/**
 * Serviço para consulta de CEP usando a API ViaCEP.
 */
export const cepService = {
  /**
   * Consulta um CEP e retorna os dados de endereço.
   * @param {string} cep - O CEP a ser consultado (apenas números).
   * @returns {Promise<Object>} - Dados do endereço.
   */
  async buscarCep(cep) {
    const cleanCep = cep.replace(/\D/g, '');
    if (cleanCep.length !== 8) {
      throw new Error('CEP inválido. Deve conter 8 dígitos.');
    }

    try {
      // BrasilAPI é mais rápida e agrega vários provedores (ViaCEP, Correios, etc)
      const response = await axios.get(`https://brasilapi.com.br/api/cep/v1/${cleanCep}`);
      return {
        logradouro: response.data.street,
        bairro: response.data.neighborhood,
        cidade: response.data.city,
        estado: response.data.state,
        cep: response.data.cep
      };
    } catch (error) {
      console.error('Erro ao buscar CEP:', error);
      if (error.response && error.response.status === 404) {
        throw new Error('CEP não encontrado.');
      }
      throw new Error('Erro ao buscar CEP. Verifique sua conexão.');
    }
  }
};

export default cepService;
