<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card shadow-sm border-0">
          <div class="card-header bg-white text-center py-4">
            <h4 class="mb-0">
              <i class="fas fa-lock me-2 text-primary"></i>
              {{ isRecovery ? 'Nova Senha' : 'Alterar Senha' }}
            </h4>
          </div>
          <div class="card-body p-4">
            <div v-if="success" class="alert alert-success text-center">
              <i class="fas fa-check-circle me-2"></i>
              Senha alterada com sucesso! Redirecionando...
            </div>

            <form v-else @submit.prevent="handleSubmit">
              <!-- Senha Atual (Só aparece se NÃO for recuperação via token) -->
              <div v-if="!isRecovery" class="mb-3">
                <label class="form-label">Senha Atual</label>
                <div class="input-group">
                  <span class="input-group-text bg-light border-end-0"><i class="fas fa-key text-muted"></i></span>
                  <input 
                    v-model="passwordForm.currentPassword" 
                    type="password" 
                    class="form-control border-start-0" 
                    placeholder="Sua senha atual"
                    required
                  >
                </div>
              </div>

              <!-- Nova Senha -->
              <div class="mb-3">
                <label class="form-label">Nova Senha</label>
                <div class="input-group">
                  <span class="input-group-text bg-light border-end-0"><i class="fas fa-lock text-muted"></i></span>
                  <input 
                    v-model="passwordForm.newPassword" 
                    type="password" 
                    class="form-control border-start-0" 
                    placeholder="Mínimo 6 caracteres"
                    minlength="6"
                    required
                  >
                </div>
              </div>

              <!-- Confirmar Nova Senha -->
              <div class="mb-4">
                <label class="form-label">Confirmar Nova Senha</label>
                <div class="input-group">
                  <span class="input-group-text bg-light border-end-0"><i class="fas fa-check text-muted"></i></span>
                  <input 
                    v-model="passwordForm.confirmPassword" 
                    type="password" 
                    class="form-control border-start-0" 
                    placeholder="Repita a nova senha"
                    required
                  >
                </div>
                <div v-if="passwordsMismatch" class="text-danger small mt-1">
                  As senhas não coincidem.
                </div>
              </div>

              <button 
                type="submit" 
                class="btn btn-primary w-100 py-2"
                :disabled="!isFormValid || loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                {{ isRecovery ? 'Redefinir Senha' : 'Salvar Nova Senha' }}
              </button>
            </form>

            <div class="text-center mt-4">
              <router-link :to="isRecovery ? '/login' : '/profile'" class="text-decoration-none text-muted small">
                <i class="fas fa-arrow-left me-1"></i> 
                Voltar para {{ isRecovery ? 'Login' : 'Perfil' }}
              </router-link>
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
  name: 'ResetPassword',
  data() {
    return {
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      loading: false,
      success: false,
      token: null
    }
  },
  computed: {
    isRecovery() {
      return !!this.token;
    },
    passwordsMismatch() {
      return this.passwordForm.newPassword && 
             this.passwordForm.confirmPassword && 
             this.passwordForm.newPassword !== this.passwordForm.confirmPassword;
    },
    isFormValid() {
      const basicValidation = this.passwordForm.newPassword && 
                             this.passwordForm.confirmPassword && 
                             !this.passwordsMismatch && 
                             this.passwordForm.newPassword.length >= 6;
      
      if (this.isRecovery) return basicValidation;
      return basicValidation && this.passwordForm.currentPassword;
    }
  },
  methods: {
    ...mapActions(['resetPassword']),
    
    async handleSubmit() {
      if (!this.isFormValid) return;
      
      this.loading = true;
      try {
        if (this.isRecovery) {
          // Caso: Recuperação via e-mail (esqueci minha senha)
          await this.resetPassword({
            token: this.token,
            password: this.passwordForm.newPassword
          });
        } else {
          // Caso: Usuário logado trocando senha
          // Aqui chamaríamos a action de trocar senha (ProfileController)
          await this.$store.dispatch('changePassword', {
            currentPassword: this.passwordForm.currentPassword,
            newPassword: this.passwordForm.newPassword
          });
        }
        
        this.success = true;
        setTimeout(() => {
          this.$router.push(this.isRecovery ? '/login' : '/profile');
        }, 3000);
        
      } catch (error) {
        alert(error.message || 'Erro ao processar solicitação');
      } finally {
        this.loading = false;
      }
    }
  },
  created() {
    this.token = this.$route.query.token;
  }
}
</script>

<style scoped>
.card {
  border-radius: 12px;
}
.input-group-text {
  border-right: none;
}
.form-control:focus {
  box-shadow: none;
  border-color: #dee2e6;
}
</style>
