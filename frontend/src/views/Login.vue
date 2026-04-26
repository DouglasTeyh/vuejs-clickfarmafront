<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-5">
        <div class="card fade-in-up">
          <div class="card-header">
            <h3 class="text-center mb-0">Entrar na ClickFarma</h3>
          </div>
          <div class="card-body">
            <form @submit.prevent="handleLogin" ref="loginForm">
              <div class="mb-3">
                <label class="form-label">Email:</label>
                <input 
                  ref="emailInput"
                  type="email" 
                  class="form-control" 
                  v-model="credentials.email"
                  required
                  placeholder="seu@email.com"
                  @input="clearFieldError('email')"
                  :class="{ 'is-invalid': fieldErrors.email }"
                >
                <div v-if="fieldErrors.email" class="invalid-feedback">
                  {{ fieldErrors.email }}
                </div>
              </div>
              
              <div class="mb-3">
                <label class="form-label">Senha:</label>
                <input 
                  ref="passwordInput"
                  type="password" 
                  class="form-control" 
                  v-model="credentials.password"
                  required
                  placeholder="Sua senha"
                  @input="clearFieldError('password')"
                  :class="{ 'is-invalid': fieldErrors.password }"
                >
                <div v-if="fieldErrors.password" class="invalid-feedback">
                  {{ fieldErrors.password }}
                </div>
              </div>
              
              <button 
                ref="submitButton"
                type="submit" 
                class="btn btn-primary w-100"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? 'Entrando...' : 'Entrar na Minha Conta' }}
              </button>
            </form>
            
            <div class="text-center mt-4">
              <p class="mb-2">Ainda não tem conta? 
                <router-link to="/register" class="fw-bold">Cadastre-se aqui</router-link>
              </p>
              <p class="mb-0">
                <router-link to="/forgot-password" class="text-primary">
                  <i class="fas fa-question-circle me-1"></i>
                  Não lembro minha senha
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
import { mapActions } from 'vuex'

export default {
  name: 'Login',
  data() {
    return {
      credentials: {
        email: '',
        password: ''
      },
      loading: false,
      fieldErrors: {}
    }
  },
  mounted() {
    this.focusEmailField();
  },
  methods: {
    ...mapActions(['login']),
    
    focusEmailField() {
      if (this.$refs.emailInput) {
        this.$refs.emailInput.focus();
      }
    },
    
    validateForm() {
      this.fieldErrors = {};
      
      if (!this.credentials.email) {
        this.fieldErrors.email = 'Email é obrigatório';
        this.focusOnError('emailInput');
        return false;
      }
      
      if (!this.credentials.email.includes('@')) {
        this.fieldErrors.email = 'Email inválido';
        this.focusOnError('emailInput');
        return false;
      }
      
      if (!this.credentials.password) {
        this.fieldErrors.password = 'Senha é obrigatória';
        this.focusOnError('passwordInput');
        return false;
      }
      
      return true;
    },
    
    focusOnError(fieldRef) {
      if (this.$refs[fieldRef]) {
        this.$refs[fieldRef].focus();
        this.$refs[fieldRef].scrollIntoView({ 
          behavior: 'smooth', 
          block: 'center' 
        });
      }
    },
    
    clearFieldError(fieldName) {
      if (this.fieldErrors[fieldName]) {
        this.fieldErrors[fieldName] = null;
      }
    },
    
    shakeForm() {
      if (this.$refs.loginForm) {
        this.$refs.loginForm.classList.add('shake');
        setTimeout(() => {
          this.$refs.loginForm.classList.remove('shake');
        }, 500);
      }
    },
    
    async handleLogin() {
      if (!this.validateForm()) {
        this.shakeForm();
        return;
      }
      
      this.loading = true;
      
      try {
        const user = await this.login(this.credentials);
        
        // Saudação personalizada
        const userName = user.name?.split(' ')[0] || '';
        alert(`Olá, ${userName}! Que bom te ver de volta! 🎉`);
        
        this.$router.push('/');
        
      } catch (error) {
        console.error('Erro no login:', error);
        
        // Tratamento específico de erros
        if (error.message?.includes('email') || error.message?.includes('não encontrado')) {
          this.fieldErrors.email = 'Email não encontrado. Verifique ou crie uma conta.';
          this.focusOnError('emailInput');
        } else if (error.message?.includes('senha') || error.message?.includes('password') || error.message?.includes('incorreta')) {
          this.fieldErrors.password = 'Senha incorreta. Tente novamente ou recupere sua senha.';
          this.focusOnError('passwordInput');
        } else {
          alert(error.message || 'Erro ao fazer login. Tente novamente.');
        }
        
        this.shakeForm();
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.shake {
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.invalid-feedback {
  display: block;
  font-size: 0.75rem;
  color: var(--cf-danger);
}

.card {
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  box-shadow: var(--cf-shadow-lg);
  overflow: hidden;
}

.card-header {
  background: var(--cf-white) !important;
  border-bottom: 1px solid var(--cf-border) !important;
  padding: 1.5rem;
}

.card-header h3 {
  font-family: var(--cf-sans);
  font-size: 1.6rem;
  font-weight: 600;
  color: var(--cf-text-dark);
}

.card-body {
  padding: 2rem;
  background: var(--cf-white);
}

.form-label {
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--cf-text-muted);
  font-weight: 500;
}

.form-control {
  border-radius: var(--cf-r-md);
  padding: 0.75rem 1rem;
  border: 1px solid var(--cf-border-mid);
  transition: all 0.2s var(--cf-ease);
}

.form-control:focus {
  border-color: var(--cf-green);
  box-shadow: 0 0 0 4px rgba(42,92,69,0.08);
}

.btn-primary {
  background: var(--cf-green);
  border: none;
  border-radius: var(--cf-r-md);
  padding: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.8rem;
  transition: all 0.2s var(--cf-ease);
}

.btn-primary:hover:not(:disabled) {
  background: var(--cf-green-dark);
  transform: translateY(-1px);
  box-shadow: var(--cf-shadow-md);
}

.text-primary {
  color: var(--cf-green) !important;
  text-decoration: none;
}
.text-primary:hover {
  text-decoration: underline;
}

.fw-bold {
  color: var(--cf-green);
}
</style>