<template>
  <div class="cf-auth-shell">
    <div class="cf-auth-container fade-in-up">
      <div class="cf-auth-card">
        <div class="card-branding-side">
          <div class="brand-logo-large">
            <i class="fas fa-hospital-user"></i>
          </div>
          <div class="brand-text">
            <h2>Click<span>Farma</span></h2>
            <p>Portal do Estabelecimento</p>
          </div>
          <div class="branding-footer">
            <span>Seja um parceiro ClickFarma e amplie seu alcance logístico com inteligência.</span>
          </div>
        </div>

        <div class="card-form-side">
          <header class="form-header">
            <h3>Credenciamento de Unidade</h3>
            <p>Inicie sua jornada na maior rede de conveniência farmacêutica.</p>
          </header>

          <form @submit.prevent="register" class="cf-premium-form">
            <div class="form-scroll cf-hide-scrollbar">
              <div class="form-group-premium mb-4">
                <label class="premium-label">Razão Social / Nome Fantasia</label>
                <div class="premium-input-wrap">
                  <i class="fas fa-store"></i>
                  <input v-model="form.nome" type="text" placeholder="Nome da sua farmácia" :class="{ 'is-invalid': fieldErrors.nome }" @input="clearFieldError('nome')">
                </div>
                <span v-if="fieldErrors.nome" class="premium-error">{{ fieldErrors.nome }}</span>
              </div>

              <div class="row g-3 mb-4">
                <div class="col-md-7">
                  <div class="form-group-premium">
                    <label class="premium-label">CNPJ</label>
                    <div class="premium-input-wrap">
                      <i class="fas fa-id-card"></i>
                      <input v-model="form.cnpj" v-mask="'cnpj'" @blur="validateCnpj" type="text" placeholder="00.000.000/0000-00" :class="{ 'is-invalid': fieldErrors.cnpj }" @input="clearFieldError('cnpj')">
                    </div>
                    <span v-if="fieldErrors.cnpj" class="premium-error">{{ fieldErrors.cnpj }}</span>
                  </div>
                </div>
                <div class="col-md-5">
                  <div class="form-group-premium">
                    <label class="premium-label">Telefone</label>
                    <div class="premium-input-wrap">
                      <i class="fas fa-phone"></i>
                      <input v-model="form.telefone" v-mask="'phone'" type="text" placeholder="(00) 00000-0000" :class="{ 'is-invalid': fieldErrors.telefone }" @input="clearFieldError('telefone')">
                    </div>
                    <span v-if="fieldErrors.telefone" class="premium-error">{{ fieldErrors.telefone }}</span>
                  </div>
                </div>
              </div>

              <div class="form-group-premium mb-4">
                <label class="premium-label">E-mail Corporativo</label>
                <div class="premium-input-wrap">
                  <i class="fas fa-envelope"></i>
                  <input v-model="form.email" type="email" placeholder="exemplo@farmacia.com" :class="{ 'is-invalid': fieldErrors.email }" @input="clearFieldError('email')">
                </div>
                <span v-if="fieldErrors.email" class="premium-error">{{ fieldErrors.email }}</span>
              </div>

              <div class="form-group-premium mb-4">
                <label class="premium-label">Senha de Acesso</label>
                <div class="premium-input-wrap">
                  <i class="fas fa-lock"></i>
                  <input v-model="form.senha" type="password" placeholder="Mínimo 6 caracteres" :class="{ 'is-invalid': fieldErrors.senha }" @input="clearFieldError('senha')">
                </div>
                <span v-if="fieldErrors.senha" class="premium-error">{{ fieldErrors.senha }}</span>
              </div>

              <div class="form-group-premium mb-4">
                <label class="premium-label">Localização (Endereço Completo)</label>
                <div class="premium-input-wrap">
                  <i class="fas fa-map-marker-alt"></i>
                  <input v-model="form.endereco" type="text" placeholder="Rua, Número, Bairro" :class="{ 'is-invalid': fieldErrors.endereco }" @input="clearFieldError('endereco')">
                </div>
                <span v-if="fieldErrors.endereco" class="premium-error">{{ fieldErrors.endereco }}</span>
              </div>

              <div class="row g-3 mb-4">
                <div class="col-md-8">
                  <div class="form-group-premium">
                    <label class="premium-label">Cidade</label>
                    <div class="premium-input-wrap">
                      <i class="fas fa-city"></i>
                      <input v-model="form.cidade" type="text" placeholder="Sua cidade" required>
                    </div>
                  </div>
                </div>
                <div class="col-md-4">
                  <div class="form-group-premium">
                    <label class="premium-label">CEP</label>
                    <div class="premium-input-wrap">
                      <i class="fas fa-map-pin"></i>
                      <input v-model="form.cep" v-mask="'cep'" @blur="handleCepBlur" type="text" placeholder="00000-000" :class="{ 'is-invalid': fieldErrors.cep }" @input="clearFieldError('cep')">
                    </div>
                    <span v-if="fieldErrors.cep" class="premium-error">{{ fieldErrors.cep }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="error" class="error-msg-premium animate__animated animate__headShake">
              <i class="fas fa-exclamation-circle"></i> {{ error }}
            </div>

            <div v-if="success" class="success-msg-premium animate__animated animate__fadeIn">
              <i class="fas fa-check-circle"></i> Cadastro realizado! Redirecionando...
            </div>

            <button type="submit" class="cf-btn-primary-large w-100 mt-2" :disabled="isLoading">
              <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
              {{ isLoading ? 'Processando...' : 'Finalizar Credenciamento' }}
            </button>

            <div class="form-footer-text">
              Já possui uma unidade? <router-link to="/pharmacy/login">Acesse o Terminal</router-link>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import farmaciasService from '@/services/farmaciasService';
import { cnpjService } from '@/services/cnpjService';
import { cepService } from '@/services/cepService';

const router = useRouter();
const isLoading = ref(false);
const loadingCep = ref(false);
const validatingCnpj = ref(false);
const error = ref('');
const success = ref(false);
const fieldErrors = reactive({});

const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

const clearFieldError = (field) => {
  if (fieldErrors[field]) delete fieldErrors[field];
};

const form = reactive({
  nome: '',
  cnpj: '',
  endereco: '',
  cidade: '',
  cep: '',
  email: '',
  senha: '',
  telefone: '',
  fotoUrl: ''
});

const validateCnpj = async () => {
  if (!form.cnpj || form.cnpj.length < 14) return;
  clearFieldError('cnpj');
  
  validatingCnpj.value = true;
  try {
    const data = await cnpjService.buscarCnpj(form.cnpj);
    if (data) {
      if (!form.nome) form.nome = data.fantasia || data.nome;
      if (!form.cep) {
        form.cep = data.cep;
        form.endereco = `${data.logradouro}, ${data.numero} - ${data.bairro}`;
        form.cidade = data.cidade;
      }
    }
  } catch (err) {
    fieldErrors.cnpj = 'CNPJ inválido ou não encontrado.';
  } finally {
    validatingCnpj.value = false;
  }
};

const handleCepBlur = async () => {
  const cleanCep = form.cep.replace(/\D/g, '');
  if (cleanCep.length !== 8) return;
  clearFieldError('cep');

  loadingCep.value = true;
  try {
    const data = await cepService.buscarCep(cleanCep);
    form.endereco = data.logradouro;
    form.cidade = data.cidade;
  } catch (err) {
    fieldErrors.cep = 'CEP não encontrado.';
  } finally {
    loadingCep.value = false;
  }
};

const register = async () => {
  Object.keys(fieldErrors).forEach(k => delete fieldErrors[k]);
  error.value = '';
  success.value = false;

  if (!form.nome) fieldErrors.nome = 'Razão Social é obrigatória.';
  if (!form.cnpj) fieldErrors.cnpj = 'CNPJ é obrigatório.';
  if (!form.telefone) fieldErrors.telefone = 'Telefone é obrigatório.';
  if (!form.email) {
    fieldErrors.email = 'E-mail é obrigatório.';
  } else if (!validateEmail(form.email)) {
    fieldErrors.email = 'E-mail inválido.';
  }
  if (!form.senha) {
    fieldErrors.senha = 'Senha é obrigatória.';
  } else if (form.senha.length < 6) {
    fieldErrors.senha = 'Mínimo 6 caracteres.';
  Object.keys(fieldErrors).forEach(k => fieldErrors[k] = '');

  // Validação Local
  let hasError = false;
  if (!form.nome) { fieldErrors.nome = 'Nome é obrigatório.'; hasError = true; }
  if (!form.email) { fieldErrors.email = 'E-mail é obrigatório.'; hasError = true; }
  if (!form.senha) { fieldErrors.senha = 'Senha é obrigatória.'; hasError = true; }
  if (!form.cnpj) { fieldErrors.cnpj = 'CNPJ é obrigatório.'; hasError = true; }
  if (!form.cep) { fieldErrors.cep = 'CEP é obrigatório.'; hasError = true; }
  if (!form.endereco) { fieldErrors.endereco = 'Endereço é obrigatório.'; hasError = true; }

  if (hasError) {
    error.value = 'Por favor, preencha todos os campos obrigatórios.';
    return;
  }

  isLoading.value = true;
  try {
    await farmaciasService.registrar(form);
    success.value = true;
    setTimeout(() => {
      router.push('/pharmacy/login');
    }, 2000);
  } catch (err) {
    console.error('Erro no cadastro de farmácia:', err);
    const msg = (err.response?.data?.mensagem || err.message || '').toLowerCase();
    
    // Mapeamento PRECISO baseado na chave do PostgreSQL
    if (msg.includes('key (email)=') || (msg.includes('email') && msg.includes('already exists'))) {
      fieldErrors.email = 'Este e-mail já está em uso.';
    } else if (msg.includes('key (cnpj)=') || (msg.includes('cnpj') && msg.includes('already exists'))) {
      fieldErrors.cnpj = 'Este CNPJ já está cadastrado.';
    } else {
      error.value = msg || 'Erro ao realizar cadastro. Verifique os dados.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.cf-auth-shell { min-height: 100vh; background: var(--cf-ivory); display: flex; align-items: center; justify-content: center; padding: 2rem; }
.cf-auth-container { width: 100%; max-width: 1000px; }
.cf-auth-card { background: #fff; border-radius: 32px; box-shadow: 0 40px 100px rgba(0,0,0,0.1); display: flex; overflow: hidden; min-height: 700px; border: 1px solid var(--cf-border); }

/* Branding Side */
.card-branding-side { flex: 1; background: var(--cf-green-dark); padding: 4rem; display: flex; flex-direction: column; justify-content: center; color: #fff; position: relative; }
.brand-logo-large { width: 70px; height: 70px; background: var(--cf-gold); border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 2rem; margin-bottom: 2rem; box-shadow: 0 15px 35px rgba(184,149,80,0.3); }
.brand-text h2 { font-family: var(--cf-serif); font-size: 2.5rem; margin: 0; }
.brand-text h2 span { color: var(--cf-gold); }
.brand-text p { font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.2em; color: rgba(255,255,255,0.4); margin-top: 0.5rem; }
.branding-footer { margin-top: auto; font-size: 0.9rem; color: rgba(255,255,255,0.6); line-height: 1.6; }

/* Form Side */
.card-form-side { flex: 1.3; padding: 4rem; display: flex; flex-direction: column; background: #fff; }
.form-header h3 { font-family: var(--cf-serif); font-size: 2rem; font-weight: 600; color: var(--cf-text-dark); margin-bottom: 0.5rem; }
.form-header p { color: var(--cf-text-muted); font-size: 0.95rem; margin-bottom: 2.5rem; }

.form-scroll { max-height: 450px; overflow-y: auto; padding-right: 1rem; margin-bottom: 1rem; }

.premium-label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; margin-bottom: 0.6rem; display: block; }
.premium-input-wrap { position: relative; }
.premium-input-wrap i { position: absolute; left: 1.25rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); font-size: 1rem; }
.premium-input-wrap input { width: 100%; padding: 1rem 1.25rem 1rem 3rem; border-radius: 14px; border: 1px solid var(--cf-border); background: var(--cf-ivory-light); outline: none; transition: all 0.2s; font-weight: 600; color: var(--cf-text-dark); }
.premium-input-wrap input:focus { border-color: var(--cf-gold); background: #fff; box-shadow: 0 0 0 4px rgba(184,149,80,0.1); }

.cf-btn-primary-large { background: var(--cf-text-dark); color: #fff; border: none; padding: 1.1rem; border-radius: 16px; font-weight: 700; font-size: 1rem; cursor: pointer; transition: all 0.3s; }
.cf-btn-primary-large:hover { background: #000; transform: translateY(-2px); box-shadow: 0 15px 35px rgba(0,0,0,0.2); }

.error-msg-premium { background: #fff1f2; color: #e11d48; padding: 1rem; border-radius: 14px; margin-bottom: 1.5rem; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.75rem; }
.success-msg-premium { background: #ecfdf5; color: #059669; padding: 1rem; border-radius: 14px; margin-bottom: 1.5rem; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.75rem; }

.premium-error { color: #e11d48; font-size: 0.7rem; font-weight: 600; margin-top: 0.3rem; display: block; }
.is-invalid { border-color: #e11d48 !important; background-color: #fff1f2 !important; }

.form-footer-text { text-align: center; margin-top: 2rem; font-size: 0.9rem; color: var(--cf-text-muted); }
.form-footer-text a { color: var(--cf-gold); font-weight: 700; text-decoration: none; }

@media (max-width: 992px) {
  .cf-auth-card { flex-direction: column; }
  .card-branding-side { padding: 3rem; }
  .card-form-side { padding: 3rem; }
}
</style>
