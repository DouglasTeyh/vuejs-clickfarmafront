<template>
  <div class="auth-page-container py-5 fade-in-up">
    <div class="row justify-content-center w-100 m-0">
      <div class="col-md-6 col-lg-5">
        <div class="card shadow-sm border-primary" style="border-top-width: 4px; border-radius: var(--cf-r-xl)">
          <div class="card-header py-4 bg-white border-0 text-center">
            <div class="cf-logo-icon mb-3 mx-auto"><i class="fas fa-motorcycle"></i></div>
            <h3 class="mb-0 fw-bold" style="font-family: var(--cf-serif); color: var(--cf-text-dark)">Portal do Entregador</h3>
            <p class="text-muted small mt-1">Acesse suas entregas e acompanhe seus ganhos em tempo real.</p>
          </div>
          <div class="card-body p-4 p-md-5 pt-0">
            <form @submit.prevent="handleLogin">
              <div class="mb-4">
                <label class="form-label fw-bold text-muted small text-uppercase" style="letter-spacing: 0.05em;">E-mail</label>
                <input 
                  type="email" 
                  class="form-control form-control-lg bg-light" 
                  v-model="email"
                  required
                  placeholder="seu@email.com"
                >
              </div>
              
              <div class="mb-4">
                <label class="form-label fw-bold text-muted small text-uppercase" style="letter-spacing: 0.05em;">Senha</label>
                <input 
                  type="password" 
                  class="form-control form-control-lg bg-light" 
                  v-model="password"
                  required
                  placeholder="Sua senha"
                >
              </div>
              
              <button 
                type="submit" 
                class="btn btn-primary btn-lg w-100 py-3 fw-bold"
                style="border-radius: 12px; transition: all 0.2s;"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? 'Entrando...' : 'Acessar Meu Painel' }}
              </button>
            </form>
            
            <div class="text-center mt-4 pt-2">
              <p class="mb-0">
                <router-link to="/login" class="text-muted small text-decoration-none">
                  <i class="fas fa-arrow-left me-1"></i>
                  Voltar para o site ClickFarma
                </router-link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';

export default {
  name: 'CourierLogin',
  setup() {
    return { toast: useToast() };
  },
  data() {
    return {
      email: '',
      password: '',
      loading: false
    };
  },
  methods: {
    async handleLogin() {
      this.loading = true;
      try {
        const res = await api.post('/auth/login', { email: this.email, senha: this.password });
        if (res.data.role !== 'COURIER') {
          this.toast.error('Acesso negado. Esta conta não é de um entregador parceiro.');
          return;
        }
        localStorage.setItem('authToken', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data));
        this.toast.success(`Bem-vindo, ${res.data.nome}!`);
        this.$router.push('/courier/dashboard');
      } catch (err) {
        console.error('Erro no login entregador:', err);
        const msg = (err.response?.data?.mensagem || err.message || '').toLowerCase();
        if (msg.includes('401') || msg.includes('invalid') || msg.includes('credenciais')) {
          this.toast.error('E-mail ou senha incorretos.');
        } else {
          this.toast.error(err.response?.data?.mensagem || 'Falha na autenticação.');
        }
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.auth-page-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--cf-ivory);
}

.cf-logo-icon {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  background: var(--cf-green);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(34, 197, 94, 0.3);
}

.form-control:focus {
  background: #fff !important;
  border-color: var(--cf-green);
  box-shadow: 0 0 0 0.25rem rgba(34, 197, 94, 0.25);
}
</style>
