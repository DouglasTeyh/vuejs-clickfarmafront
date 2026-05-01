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
      const response = await axios.get(`https://viacep.com.br/ws/${cleanCep}/json/`);
      if (response.data.erro) {
        throw new Error('CEP não encontrado.');
      }
      return {
        logradouro: response.data.logradouro,
        bairro: response.data.bairro,
        cidade: response.data.localidade,
        estado: response.data.uf,
        cep: response.data.cep
      };
    } catch (error) {
      console.error('Erro ao buscar CEP:', error);
      throw error;
    }
  }
};

export default cepService;
