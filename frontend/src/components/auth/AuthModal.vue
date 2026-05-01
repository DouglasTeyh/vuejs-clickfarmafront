<template>
  <div v-if="isOpen" class="auth-modal-overlay" @click.self="close">
    <div class="auth-modal-content fade-in-up">
      <button class="close-btn" @click="close" aria-label="Fechar">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>

      <div class="auth-modal-body">
        <div class="auth-header mb-4">
          <img src="/images/Logotipo.svg" alt="ClickFarma" class="modal-logo mb-3">
          <h2 class="fw-bold">{{ isLoginMode ? 'Bem-vindo de volta' : 'Criar sua conta' }}</h2>
          <p class="text-muted">{{ isLoginMode ? 'Entre para gerenciar seus pedidos' : 'Junte-se à ClickFarma hoje mesmo' }}</p>
        </div>

        <!-- Login Form -->
        <form v-if="isLoginMode" @submit.prevent="handleLogin" class="auth-form">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input 
              type="email" 
              class="form-control" 
              v-model="loginData.email" 
              placeholder="seu@email.com"
              required
            >
          </div>
          <div class="mb-4">
            <label class="form-label d-flex justify-content-between">
              Senha
              <a href="#" class="forgot-link" @click.prevent="goToForgot">Esqueceu?</a>
            </label>
            <input 
              type="password" 
              class="form-control" 
              v-model="loginData.senha" 
              placeholder="Sua senha"
              required
            >
          </div>
          <button type="submit" class="cf-btn-solid w-100 py-3 mb-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <!-- Register Form -->
        <form v-else @submit.prevent="handleRegister" class="auth-form">
          <div class="mb-3">
            <label class="form-label">Nome Completo</label>
            <input 
              type="text" 
              class="form-control" 
              v-model="registerData.nome" 
              placeholder="Ex: João Silva"
              required
            >
          </div>
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input 
              type="email" 
              class="form-control" 
              v-model="registerData.email" 
              placeholder="seu@email.com"
              required
            >
          </div>
          <div class="mb-4">
            <label class="form-label">Senha</label>
            <input 
              type="password" 
              class="form-control" 
              v-model="registerData.senha" 
              placeholder="Mínimo 6 caracteres"
              required
            >
          </div>
          <button type="submit" class="cf-btn-solid w-100 py-3 mb-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Criando conta...' : 'Cadastrar' }}
          </button>
        </form>

        <div class="auth-footer text-center">
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
import { mapActions } from 'vuex'

export default {
  name: 'AuthModal',
  props: {
    isOpen: Boolean,
    initialMode: {
      type: String,
      default: 'login' // 'login' or 'register'
    }
  },
  data() {
    return {
      isLoginMode: this.initialMode === 'login',
      loading: false,
      loginData: {
        email: '',
        senha: ''
      },
      registerData: {
        nome: '',
        email: '',
        senha: ''
      }
    }
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
    },
    async handleLogin() {
      this.loading = true;
      try {
        await this.login(this.loginData);
        this.close();
        this.$router.go(0); // Recarrega para garantir estado limpo
      } catch (error) {
        alert(error.message || 'Erro ao entrar. Verifique suas credenciais.');
      } finally {
        this.loading = false;
      }
    },
    async handleRegister() {
      this.loading = true;
      try {
        await this.register(this.registerData);
        alert('Conta criada com sucesso! Você já pode entrar.');
        this.isLoginMode = true;
      } catch (error) {
        alert(error.message || 'Erro ao criar conta.');
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
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.auth-modal-content {
  background: white;
  width: 100%;
  max-width: 440px;
  border-radius: var(--cf-r-xl);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
  border: 1px solid var(--cf-border);
}

.auth-modal-body {
  padding: 2.5rem;
}

.close-btn {
  position: absolute;
  top: 1.2rem;
  right: 1.2rem;
  background: var(--cf-ivory);
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cf-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
}

.close-btn:hover {
  background: #fee2e2;
  color: var(--cf-danger);
  transform: rotate(90deg);
}

.modal-logo {
  height: 48px;
  width: auto;
}

.auth-header h2 {
  font-size: 1.75rem;
  color: var(--cf-text-dark);
  margin-bottom: 0.5rem;
}

.auth-header p {
  font-size: 0.95rem;
}

.form-label {
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  margin-bottom: 0.5rem;
}

.form-control {
  padding: 0.8rem 1rem;
  border-radius: var(--cf-r-md);
  border: 1px solid var(--cf-border);
  background: var(--cf-ivory);
  font-size: 0.95rem;
  transition: all 0.2s;
}

.form-control:focus {
  border-color: var(--cf-green);
  background: white;
  box-shadow: 0 0 0 4px rgba(42, 92, 69, 0.1);
}

.forgot-link {
  font-size: 0.8rem;
  color: var(--cf-green);
  text-decoration: none;
}

.toggle-link {
  color: var(--cf-green);
  text-decoration: none;
}

.toggle-link:hover {
  text-decoration: underline;
}

.auth-footer p {
  font-size: 0.9rem;
}

@media (max-width: 576px) {
  .auth-modal-body {
    padding: 2rem 1.5rem;
  }
}
</style>
