<template>
  <div v-if="isOpen" class="auth-modal-overlay" @click.self="close">
    <div class="auth-modal-content fade-in-up" :class="{ 'wide-modal': !isLoginMode }">
      <button class="close-btn" @click="close" aria-label="Fechar">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>

      <div class="auth-modal-body">
        <div class="auth-header mb-4 text-center">
          <img src="/images/Logotipo.svg" alt="ClickFarma" class="modal-logo mb-3">
          <h2 class="fw-bold">{{ isLoginMode ? 'Bem-vindo de volta' : 'Criar sua conta' }}</h2>
          <p class="text-muted">{{ isLoginMode ? 'Entre para gerenciar seus pedidos' : 'Junte-se à ClickFarma hoje mesmo' }}</p>
        </div>


        <!-- General Error Alert -->
        <div v-if="error" class="alert-danger-soft mb-4 shake-error">
          <i class="fa-solid fa-circle-exclamation"></i>
          <span>{{ error }}</span>
        </div>

        <!-- Role Selector (Only in Register) -->
        <div v-if="!isLoginMode" class="role-selector mb-4">
          <button 
            type="button" 
            class="role-btn" 
            :class="{ active: registerData.role === 'CUSTOMER' }"
            @click="registerData.role = 'CUSTOMER'"
          >
            <i class="fa-solid fa-user me-2"></i> Cliente
          </button>
          <button 
            type="button" 
            class="role-btn" 
            :class="{ active: registerData.role === 'PHARMACY' }"
            @click="registerData.role = 'PHARMACY'"
          >
            <i class="fa-solid fa-prescription-bottle-medical me-2"></i> Farmácia
          </button>
          <button 
            type="button" 
            class="role-btn" 
            :class="{ active: registerData.role === 'COURIER' }"
            @click="registerData.role = 'COURIER'"
          >
            <i class="fa-solid fa-motorcycle me-2"></i> Entregador
          </button>
        </div>

        <!-- Login Form -->
        <form v-if="isLoginMode" @submit.prevent="handleLogin" class="auth-form" :class="{ 'shake-error': shake }">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input 
              type="email" 
              class="form-control" 
              :class="{ 'is-invalid': fieldErrors.email || (error && !loginData.email) }"
              v-model="loginData.email" 
              placeholder="seu@email.com"
              @input="clearFieldError('email')"
            >
            <span v-if="fieldErrors.email" class="error-message">{{ fieldErrors.email }}</span>
          </div>
          <div class="mb-4">
            <label class="form-label d-flex justify-content-between">
              Senha
              <a href="#" class="forgot-link" @click.prevent="goToForgot">Esqueceu?</a>
            </label>
            <input 
              type="password" 
              class="form-control" 
              :class="{ 'is-invalid': fieldErrors.senha || (error && !loginData.senha) }"
              v-model="loginData.senha" 
              placeholder="Sua senha"
              @input="clearFieldError('senha')"
            >
            <span v-if="fieldErrors.senha" class="error-message">{{ fieldErrors.senha }}</span>
          </div>
          <button type="submit" class="cf-btn-solid w-100 py-3 mb-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <!-- Register Form -->
        <form v-else @submit.prevent="handleRegister" class="auth-form scrollable-form" :class="{ 'shake-error': shake }">
          <div class="row g-3">
            <!-- Basic Info -->
            <div class="col-md-6">
              <label class="form-label">Nome {{ registerData.role === 'PHARMACY' ? 'do Estabelecimento' : 'Completo' }} *</label>
              <input 
                type="text" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.nome }"
                v-model="registerData.nome"
                @input="clearFieldError('nome')"
              >
              <span v-if="fieldErrors.nome" class="error-message">{{ fieldErrors.nome }}</span>
            </div>
            <div class="col-md-6">
              <label class="form-label">Email *</label>
              <input 
                type="email" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.email }"
                v-model="registerData.email"
                @input="clearFieldError('email')"
              >
              <span v-if="fieldErrors.email" class="error-message">{{ fieldErrors.email }}</span>
            </div>

            <!-- Documents -->
            <div class="col-md-6" v-if="registerData.role === 'PHARMACY'">
              <label class="form-label">CNPJ *</label>
              <div class="input-group has-validation">
                <input 
                  type="text" 
                  class="form-control" 
                  :class="{ 'is-invalid': fieldErrors.cnpj }"
                  v-model="registerData.cnpj" 
                  v-mask="'cnpj'"
                  @blur="validateCnpj"
                  @input="clearFieldError('cnpj')"
                  placeholder="00.000.000/0000-00"
                >
                <span v-if="validatingCnpj" class="input-group-text"><i class="fa-solid fa-spinner fa-spin"></i></span>
              </div>
              <span v-if="fieldErrors.cnpj" class="error-message">{{ fieldErrors.cnpj }}</span>
            </div>
            <div class="col-md-6" v-else>
              <label class="form-label">CPF *</label>
              <input 
                type="text" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.cpf }"
                v-model="registerData.cpf" 
                v-mask="'cpf'"
                @input="clearFieldError('cpf')"
                placeholder="000.000.000-00"
              >
              <span v-if="fieldErrors.cpf" class="error-message">{{ fieldErrors.cpf }}</span>
            </div>

            <!-- PIX for Pharmacy -->
            <div class="col-md-6" v-if="registerData.role === 'PHARMACY'">
              <label class="form-label">Chave PIX para Recebimento *</label>
              <input 
                type="text" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.chavePix }"
                v-model="registerData.chavePix" 
                @input="clearFieldError('chavePix')"
                placeholder="Chave para repasses semanais"
              >
              <span v-if="fieldErrors.chavePix" class="error-message">{{ fieldErrors.chavePix }}</span>
            </div>

            <!-- Address Section -->
            <div class="col-12 mt-4">
              <h6 class="fw-bold border-bottom pb-2">Endereço</h6>
            </div>

            <div class="col-md-4">
              <label class="form-label">CEP *</label>
              <div class="input-group has-validation">
                <input 
                  type="text" 
                  class="form-control" 
                  :class="{ 'is-invalid': fieldErrors.cep }"
                  v-model="registerData.cep" 
                  v-mask="'cep'"
                  @blur="handleCepBlur"
                  @input="clearFieldError('cep')"
                  placeholder="00000-000"
                >
                <span v-if="loadingCep" class="input-group-text"><i class="fa-solid fa-spinner fa-spin"></i></span>
              </div>
              <span v-if="fieldErrors.cep" class="error-message">{{ fieldErrors.cep }}</span>
            </div>
            <div class="col-md-8">
              <label class="form-label">Logradouro *</label>
              <input 
                type="text" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.logradouro }"
                v-model="registerData.logradouro"
                @input="clearFieldError('logradouro')"
              >
              <span v-if="fieldErrors.logradouro" class="error-message">{{ fieldErrors.logradouro }}</span>
            </div>
            <div class="col-md-3">
              <label class="form-label">Número *</label>
              <input 
                type="text" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.numero }"
                v-model="registerData.numero"
                @input="clearFieldError('numero')"
              >
              <span v-if="fieldErrors.numero" class="error-message">{{ fieldErrors.numero }}</span>
            </div>
            <div class="col-md-5">
              <label class="form-label">Bairro *</label>
              <input 
                type="text" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.bairro }"
                v-model="registerData.bairro"
                @input="clearFieldError('bairro')"
              >
              <span v-if="fieldErrors.bairro" class="error-message">{{ fieldErrors.bairro }}</span>
            </div>
            <div class="col-md-4">
              <label class="form-label">Complemento</label>
              <input type="text" class="form-control" v-model="registerData.complemento">
            </div>

            <div class="col-md-8">
              <label class="form-label">Cidade</label>
              <input type="text" class="form-control bg-light" v-model="registerData.cidade" readonly>
            </div>
            <div class="col-md-4">
              <label class="form-label">Estado</label>
              <input type="text" class="form-control bg-light" v-model="registerData.estado" readonly>
            </div>

            <div class="col-12 mt-3">
              <label class="form-label">Senha *</label>
              <input 
                type="password" 
                class="form-control" 
                :class="{ 'is-invalid': fieldErrors.senha }"
                v-model="registerData.senha" 
                @input="clearFieldError('senha')"
                placeholder="Mínimo 6 caracteres"
              >
              <span v-if="fieldErrors.senha" class="error-message">{{ fieldErrors.senha }}</span>
            </div>
          </div>

          <button type="submit" class="cf-btn-solid w-100 py-3 mt-4 mb-3" :disabled="loading || validatingCnpj">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Criando conta...' : 'Cadastrar' }}
          </button>
        </form>

        <div class="auth-footer text-center mt-3">
          <p class="mb-0">
            {{ isLoginMode ? 'Ainda não tem conta?' : 'Já tem uma conta?' }}
            <a href="#" class="toggle-link fw-bold" @click.prevent="toggleMode">
              {{ isLoginMode ? 'Cadastre-se' : 'Faça login' }}
            </a>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import { cepService } from '@/services/cepService'
import { cnpjService } from '@/services/cnpjService'

export default {
  name: 'AuthModal',
  props: {
    isOpen: Boolean,
    initialMode: {
      type: String,
      default: 'login'
    }
  },
  data() {
    return {
      isLoginMode: this.initialMode === 'login',
      loading: false,
      loadingCep: false,
      validatingCnpj: false,
      error: null,
      shake: false,
      fieldErrors: {},
      loginData: {
        email: '',
        senha: ''
      },
      registerData: {
        nome: '',
        email: '',
        senha: '',
        role: 'CUSTOMER',
        cpf: '',
        cnpj: '',
        chavePix: '',
        cep: '',
        logradouro: '',
        numero: '',
        bairro: '',
        cidade: '',
        estado: '',
        complemento: ''
      }
    }
  },
  computed: {
    ...mapState(['authRedirectPath'])
  },
  watch: {
    initialMode(newMode) {
      this.isLoginMode = newMode === 'login';
    }
  },
  methods: {
    ...mapActions(['login', 'register']),
    close() {
      this.$emit('close');
    },
    toggleMode() {
      this.isLoginMode = !this.isLoginMode;
      this.error = null;
      this.fieldErrors = {};
    },
    validateEmail(email) {
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return re.test(email);
    },
    clearFieldError(field) {
      if (this.fieldErrors[field]) {
        delete this.fieldErrors[field];
      }
      if (Object.keys(this.fieldErrors).length === 0) {
        this.error = null;
      }
    },
    async handleLogin() {
      this.fieldErrors = {};
      this.error = null;
      this.shake = false;

      if (!this.loginData.email) {
        this.fieldErrors.email = 'O email é obrigatório.';
      } else if (!this.validateEmail(this.loginData.email)) {
        this.fieldErrors.email = 'Insira um email válido.';
      }

      if (!this.loginData.senha) {
        this.fieldErrors.senha = 'A senha é obrigatória.';
      }

      if (Object.keys(this.fieldErrors).length > 0) {
        this.triggerShake();
        return;
      }

      this.loading = true;
      try {
        const user = await this.login(this.loginData);
        if (window.$toast) {
            const nameToSplit = (user && (user.nome || user.name)) || 'Usuário';
            const firstName = String(nameToSplit).split(' ')[0];
            window.$toast.addToast(`Bem-vindo de volta, ${firstName}!`, 'success');
        }
        this.close();
        const role = user.role?.toUpperCase();
        if (role === 'PHARMACY') { this.$router.push('/pharmacy/dashboard'); }
        else if (role === 'COURIER') { this.$router.push('/courier/dashboard'); }
        else if (role === 'ADMIN') { this.$router.push('/admin/dashboard'); }
        else {
          if (this.authRedirectPath) { this.$router.push(this.authRedirectPath); }
          else { this.$router.go(0); }
        }
      } catch (err) {
        console.error('❌ Erro no login:', err);
        const msg = (err.message || '').toLowerCase();
        if (msg.includes('credenciais') || msg.includes('invalid') || msg.includes('401')) {
          this.error = 'E-mail ou senha incorretos.';
          this.fieldErrors.email = ' ';
          this.fieldErrors.senha = ' ';
        } else {
          this.error = err.message || 'Falha ao entrar.';
        }
        this.triggerShake();
      } finally {
        this.loading = false;
      }
    },
    triggerShake() {
      this.shake = true;
      setTimeout(() => { this.shake = false; }, 500);
    },
    async validateCnpj() {
      if (!this.registerData.cnpj || this.registerData.cnpj.length < 14) return;
      
      this.validatingCnpj = true;
      try {
        const data = await cnpjService.buscarCnpj(this.registerData.cnpj);
        if (data) {
          if (!this.registerData.nome) this.registerData.nome = data.fantasia || data.nome;
          if (!this.registerData.cep) {
            this.registerData.cep = data.cep;
            this.registerData.logradouro = data.logradouro;
            this.registerData.numero = data.numero;
            this.registerData.bairro = data.bairro;
            this.registerData.cidade = data.cidade;
            this.registerData.estado = data.estado;
          }
        }
      } catch (err) {
        this.fieldErrors.cnpj = err.message || 'CNPJ inválido ou não encontrado.';
      } finally {
        this.validatingCnpj = false;
      }
    },
    async handleCepBlur() {
      const cep = this.registerData.cep.replace(/\D/g, '');
      if (cep.length !== 8) return;

      this.loadingCep = true;
      try {
        const data = await cepService.buscarCep(cep);
        this.registerData.logradouro = data.logradouro;
        this.registerData.bairro = data.bairro;
        this.registerData.cidade = data.cidade;
        this.registerData.estado = data.estado;
      } catch (error) {
        if (window.$toast) window.$toast.addToast('CEP não encontrado.', 'warning');
      } finally {
        this.loadingCep = false;
      }
    },
    async handleRegister() {
      this.fieldErrors = {};
      this.error = null;
      this.shake = false;

      let hasError = false;
      if (!this.registerData.nome) { this.fieldErrors.nome = 'Nome é obrigatório.'; hasError = true; }
      if (!this.registerData.email) { 
        this.fieldErrors.email = 'Email é obrigatório.'; hasError = true; 
      } else if (!this.validateEmail(this.registerData.email)) {
        this.fieldErrors.email = 'Email inválido.'; hasError = true;
      }
      
      if (!this.registerData.senha) {
        this.fieldErrors.senha = 'Senha é obrigatória.'; hasError = true;
      } else if (this.registerData.senha.length < 6) {
        this.fieldErrors.senha = 'A senha deve ter pelo menos 6 caracteres.'; hasError = true;
      }

      if (this.registerData.role === 'PHARMACY') {
        if (!this.registerData.cnpj) { this.fieldErrors.cnpj = 'CNPJ é obrigatório.'; hasError = true; }
        if (!this.registerData.cep) { this.fieldErrors.cep = 'CEP é obrigatório.'; hasError = true; }
        if (!this.registerData.logradouro) { this.fieldErrors.logradouro = 'Endereço é obrigatório.'; hasError = true; }
        if (!this.registerData.numero) { this.fieldErrors.numero = 'Nº é obrigatório.'; hasError = true; }
      } else {
        if (!this.registerData.cpf) { this.fieldErrors.cpf = 'CPF é obrigatório.'; hasError = true; }
      }

      if (hasError) {
        this.error = 'Por favor, preencha todos os campos obrigatórios corretamente.';
        this.triggerShake();
        return;
      }

      this.loading = true;
      try {
        const user = await this.register(this.registerData);
        if (user) {
          if (window.$toast) window.$toast.addToast('Conta criada com sucesso!', 'success');
          this.close();
          const role = user.role?.toUpperCase();
          if (role === 'PHARMACY') { this.$router.push('/pharmacy/dashboard'); } 
          else if (role === 'COURIER') { this.$router.push('/courier/dashboard'); }
          else { this.$router.go(0); }
        }
      } catch (err) {
        console.error('❌ Erro no registro:', err);
        const msg = (err.message || '').toLowerCase();
        
        if (msg.includes('key (email)=') || (msg.includes('email') && msg.includes('already exists'))) {
          this.fieldErrors.email = 'Este e-mail já está em uso.';
        } else if (msg.includes('key (cnpj)=') || (msg.includes('cnpj') && msg.includes('already exists'))) {
          this.fieldErrors.cnpj = 'Este CNPJ já está cadastrado.';
        } else if (msg.includes('key (cpf)=') || (msg.includes('cpf') && msg.includes('already exists'))) {
          this.fieldErrors.cpf = 'Este CPF já está cadastrado.';
        } else {
          this.error = err.message || 'Erro ao criar conta.';
        }
        this.triggerShake();
      } finally {
        this.loading = false;
      }
    },
    goToForgot() {
      this.close();
      this.$router.push('/forgot-password');
    }
  }
}
</script>

<style scoped>
.auth-modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  z-index: 9999; display: flex; align-items: center; justify-content: center; padding: 1.5rem;
}

.auth-modal-content {
  background: white; width: 100%; max-width: 440px;
  border-radius: var(--cf-r-xl); box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  position: relative; overflow: hidden; border: 1px solid var(--cf-border);
  transition: max-width 0.3s ease;
}

.auth-modal-content.wide-modal { max-width: 700px; }

.auth-modal-body { padding: 2.5rem; }

.scrollable-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.scrollable-form::-webkit-scrollbar { width: 6px; }
.scrollable-form::-webkit-scrollbar-thumb { background: var(--cf-border); border-radius: 10px; }

.role-selector {
  display: flex; gap: 10px; justify-content: center;
}

.role-btn {
  flex: 1; padding: 12px 8px; border: 1px solid var(--cf-border);
  background: var(--cf-ivory); border-radius: var(--cf-r-md);
  font-size: 0.8rem; font-weight: 600; color: var(--cf-text-mid);
  transition: all 0.2s;
}

.role-btn.active {
  background: var(--cf-green-xlight); border-color: var(--cf-green);
  color: var(--cf-green);
}

.close-btn {
  position: absolute; top: 1.2rem; right: 1.2rem;
  background: var(--cf-ivory); border: none; width: 36px; height: 36px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  color: var(--cf-text-muted); cursor: pointer; transition: all 0.2s; z-index: 10;
}

.close-btn:hover { background: #fee2e2; color: var(--cf-danger); transform: rotate(90deg); }

.modal-logo { height: 48px; width: auto; }

.form-label { font-size: 0.85rem; font-weight: 500; color: var(--cf-text-dark); margin-bottom: 0.4rem; }

.form-control {
  padding: 0.75rem 1rem; border-radius: var(--cf-r-md);
  border: 1px solid var(--cf-border); background: var(--cf-ivory);
  font-size: 0.9rem; transition: all 0.2s;
}

.form-control:focus {
  border-color: var(--cf-green); background: white;
  box-shadow: 0 0 0 4px rgba(42, 92, 69, 0.1);
}

.toggle-link { color: var(--cf-green); text-decoration: none; }
.toggle-link:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .auth-modal-content.wide-modal { max-width: 100%; }
  .role-selector { flex-direction: column; }
}

/* Error Styles */
.error-message {
  color: var(--cf-danger);
  font-size: 0.75rem;
  margin-top: 0.25rem;
  display: block;
}

.alert-danger-soft {
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 0.75rem;
  border-radius: var(--cf-r-md);
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.is-invalid {
  border-color: var(--cf-danger) !important;
}

.shake-error {
  animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
