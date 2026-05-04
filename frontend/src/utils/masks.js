/**
 * Utilitários para máscaras de input
 */

export const maskCPF = (value) => {
  if (!value) return ''
  return value
    .replace(/\D/g, '')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})/, '$1-$2')
    .replace(/(-\d{2})\d+?$/, '$1')
}

export const maskCNPJ = (value) => {
  if (!value) return ''
  return value
    .replace(/\D/g, '')
    .replace(/(\d{2})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1/$2')
    .replace(/(\d{4})(\d)/, '$1-$2')
    .replace(/(-\d{2})\d+?$/, '$1')
}

export const maskCEP = (value) => {
  if (!value) return ''
  return value
    .replace(/\D/g, '')
    .replace(/(\d{5})(\d)/, '$1-$2')
    .replace(/(-\d{3})\d+?$/, '$1')
}

export const maskPhone = (value) => {
  if (!value) return ''
  return value
    .replace(/\D/g, '')
    .replace(/(\d{2})(\d)/, '($1) $2')
    .replace(/(\d{5})(\d)/, '$1-$2')
    .replace(/(-\d{4})\d+?$/, '$1')
}

/**
 * Diretiva Vue para máscaras
 * Uso: v-mask="'cpf'" ou v-mask="'cnpj'"
 */
export const vMask = {
  mounted(el, binding) {
    const input = el.tagName === 'INPUT' ? el : el.querySelector('input')
    if (!input) return

    const applyMask = () => {
      const startValue = input.value;
      let newValue = startValue;
      if (binding.value === 'cpf') newValue = maskCPF(startValue);
      else if (binding.value === 'cnpj') newValue = maskCNPJ(startValue);
      else if (binding.value === 'cep') newValue = maskCEP(startValue);
      else if (binding.value === 'phone') newValue = maskPhone(startValue);
      
      if (newValue !== startValue) {
        input.value = newValue;
        input.dispatchEvent(new Event('input'));
      }
    };

    input.addEventListener('input', applyMask)
  }
}
