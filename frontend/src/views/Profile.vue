<template>
  <div class="profile-page">
    <div class="container py-lg-5 py-4">
      
      <div class="header-wrap mb-5 fade-in-up">
        <span class="section-eyebrow">Minha Conta</span>
        <h1 class="section-title">Olá, <em>{{ profile.nome || 'Usuário' }}</em></h1>
      </div>

      <div class="row g-4">
        <!-- Sidebar -->
        <div class="col-lg-3 fade-in-up">
          <div class="cf-profile-sidebar h-100">
            <div class="nav flex-column gap-2">
              <button 
                @click="activeSection = 'personal'"
                class="nav-link-cf"
                :class="{ active: activeSection === 'personal' }"
              >
                <i class="fa-solid fa-user-gear"></i>
                Dados Pessoais
              </button>
              <button 
                @click="activeSection = 'security'"
                class="nav-link-cf"
                :class="{ active: activeSection === 'security' }"
              >
                <i class="fa-solid fa-shield-halved"></i>
                Segurança
              </button>
              <div class="cf-divider my-3"></div>
              <router-link to="/orders" class="nav-link-cf">
                <i class="fa-solid fa-box-archive"></i>
                Meus Pedidos
              </router-link>
              <router-link to="/prescriptions" class="nav-link-cf">
                <i class="fa-solid fa-file-prescription"></i>
                Minhas Receitas
              </router-link>
            </div>

            <div class="sidebar-stats mt-5 p-4">
               <span class="stats-label">Membro desde</span>
               <p class="stats-val mb-2">{{ memberSince }}</p>
               <span class="stats-label">Nível de Fidelidade</span>
               <div class="fidelity-badge">Cliente Premium</div>
            </div>
            
            <button @click="handleLogout" class="btn btn-outline-danger w-100 mt-4">
              <i class="fa-solid fa-right-from-bracket me-2"></i>
              Sair da Conta
            </button>
          </div>
        </div>

        <!-- Content Area -->
        <div class="col-lg-9 fade-in-up" style="animation-delay: 0.1s">
          <div class="cf-content-card">
            
            <!-- SEÇÃO PESSOAL -->
            <div v-show="activeSection === 'personal'" class="section-content">
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="section-title-sm">Dados Pessoais</h3>
                <button 
                  @click="toggleEditMode"
                  class="btn btn-sm"
                  :class="isEditing ? 'btn-outline-danger' : 'btn-outline-primary'"
                >
                  <i class="fa-solid" :class="isEditing ? 'fa-xmark' : 'fa-pen-to-square'"></i>
                  {{ isEditing ? 'Cancelar' : 'Editar Perfil' }}
                </button>
              </div>

              <form @submit.prevent="handleUpdateProfile" class="cf-form">
                <div class="row g-4">
                  <div class="col-md-6">
                    <label class="form-label">Nome Completo</label>
                    <input 
                      v-model="profile.nome" 
                      type="text" 
                      class="form-control" 
                      :disabled="!isEditing"
                      required
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">E-mail (Não editável)</label>
                    <input 
                      v-model="profile.email" 
                      type="email" 
                      class="form-control" 
                      disabled
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Telefone celular</label>
                    <input 
                      v-model="profile.telefone" 
                      type="tel" 
                      class="form-control"
                      :disabled="!isEditing"
                      placeholder="(00) 00000-0000"
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">CPF (Não editável)</label>
                    <input 
                      v-model="profile.cpf" 
                      type="text" 
                      class="form-control"
                      disabled
                    >
                  </div>

                  <div class="col-12 mt-4">
                    <h5 class="text-muted border-bottom pb-2 mb-3">Endereço</h5>
                  </div>

                  <div class="col-md-4">
                    <label class="form-label">CEP</label>
                    <div class="input-group">
                      <input 
                        v-model="profile.cep" 
                        @blur="lookupCep"
                        type="text" 
                        class="form-control"
                        :disabled="!isEditing"
                        maxlength="9"
                        placeholder="00000-000"
                      >
                      <span v-if="loadingCep" class="input-group-text bg-white">
                        <i class="fa-solid fa-spinner fa-spin text-primary"></i>
                      </span>
                    </div>
                  </div>

                  <div class="col-md-8">
                    <label class="form-label">Logradouro</label>
                    <input 
                      v-model="profile.logradouro" 
                      type="text" 
                      class="form-control"
                      :disabled="!isEditing"
                    >
                  </div>

                  <div class="col-md-3">
                    <label class="form-label">Número</label>
                    <input 
                      v-model="profile.numero" 
                      type="text" 
                      class="form-control"
                      :disabled="!isEditing"
                    >
                  </div>

                  <div class="col-md-5">
                    <label class="form-label">Bairro</label>
                    <input 
                      v-model="profile.bairro" 
                      type="text" 
                      class="form-control"
                      :disabled="!isEditing"
                    >
                  </div>

                  <div class="col-md-4">
                    <label class="form-label">Complemento</label>
                    <input 
                      v-model="profile.complemento" 
                      type="text" 
                      class="form-control"
                      :disabled="!isEditing"
                    >
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">Cidade (Preenchido via CEP)</label>
                    <input 
                      v-model="profile.cidade" 
                      type="text" 
                      class="form-control"
                      disabled
                    >
                  </div>

                  <div class="col-md-6">
                    <label class="form-label">Estado (Preenchido via CEP)</label>
                    <input 
                      v-model="profile.estado" 
                      type="text" 
                      class="form-control"
                      disabled
                    >
                  </div>
                  
                  <!-- Campos específicos para Farmácia -->
                  <template v-if="userRole === 'PHARMACY'">
                    <div class="col-12 mt-4">
                      <h5 class="text-muted border-bottom pb-2 mb-3">Dados Profissionais (Farmácia)</h5>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label">CNPJ (Não editável)</label>
                      <input 
                        v-model="profile.cnpj" 
                        type="text" 
                        class="form-control"
                        disabled
                      >
                    </div>
                    <div class="col-md-6">
                      <label class="form-label">Chave PIX (Para Recebimento)</label>
                      <input 
                        v-model="profile.chavePix" 
                        type="text" 
                        class="form-control"
                        :disabled="!isEditing"
                        placeholder="Email, CPF, Telefone ou Aleatória"
                      >
                    </div>
                  </template>
                </div>

                <div v-if="isEditing" class="mt-5 d-flex gap-3">
                  <button type="submit" class="btn btn-primary px-5" :disabled="isSaving">
                    <i v-if="isSaving" class="fa-solid fa-spinner fa-spin me-2"></i>
                    <i v-else class="fa-solid fa-floppy-disk me-2"></i>
                    Salvar Alterações
                  </button>
                  <button type="button" @click="cancelEdit" class="btn btn-light px-4">
                    Descartar
                  </button>
                </div>
              </form>
            </div>

            <!-- SEÇÃO SEGURANÇA -->
            <div v-show="activeSection === 'security'" class="section-content">
               <h3 class="section-title-sm mb-4">Segurança da Conta</h3>
               <div class="row">
                 <div class="col-md-12">
                   <div class="cf-form-box">
                      <h4 class="form-box-title mb-3">Alterar Senha</h4>
                      <form @submit.prevent="handleChangePassword">
                        <div class="mb-3">
                          <label class="form-label">Senha Atual</label>
                          <input v-model="passwordForm.currentPassword" type="password" class="form-control" required>
                        </div>
                        <div class="mb-3">
                          <label class="form-label">Nova Senha</label>
                          <input v-model="passwordForm.newPassword" type="password" class="form-control" required minlength="6">
                        </div>
                        <div class="mb-4">
                          <label class="form-label">Confirmar Nova Senha</label>
                          <input v-model="passwordForm.confirmPassword" type="password" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-primary px-4" :disabled="isChangingPassword">
                          <i v-if="isChangingPassword" class="fa-solid fa-spinner fa-spin me-2"></i>
                          Atualizar Senha
                        </button>
                      </form>
                   </div>
                 </div>
               </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { cepService } from '@/services/cepService';
import authService from '@/services/auth';

export default {
  name: 'Profile',
  data() {
    return {
      activeSection: 'personal',
      isEditing: false,
      isSaving: false,
      isChangingPassword: false,
      loadingCep: false,
      profile: {
        nome: '',
        email: '',
        telefone: '',
        cpf: '',
        cep: '',
        logradouro: '',
        numero: '',
        bairro: '',
        complemento: '',
        cidade: '',
        estado: '',
        cnpj: '',
        chavePix: ''
      },
      originalProfile: null,
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      memberSince: ''
    };
  },
  computed: {
    ...mapState(['user']),
    userRole() {
      return this.user?.role?.toUpperCase() || 'USER';
    }
  },
  async mounted() {
    await this.loadProfileData();
  },
  methods: {
    ...mapActions(['logout']),
    
    async loadProfileData() {
      try {
        const response = await authService.getProfile();
        const data = response.data;
        
        this.profile = {
          nome: data.nome || '',
          email: data.email || '',
          telefone: data.telefone || '',
          cpf: data.cpf || '',
          cep: data.cep || '',
          logradouro: data.logradouro || '',
          numero: data.numero || '',
          bairro: data.bairro || '',
          complemento: data.complemento || '',
          cidade: data.cidade || '',
          estado: data.estado || '',
          cnpj: data.cnpj || '',
          chavePix: data.chavePix || ''
        };
        
        this.originalProfile = { ...this.profile };
        
        if (data.dataCadastro) {
          const date = new Date(data.dataCadastro);
          this.memberSince = date.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
        } else {
          this.memberSince = 'Recentemente';
        }
      } catch (error) {
        console.error('Erro ao carregar perfil:', error);
      }
    },

    toggleEditMode() {
      if (this.isEditing) {
        this.cancelEdit();
      } else {
        this.isEditing = true;
      }
    },

    cancelEdit() {
      this.profile = { ...this.originalProfile };
      this.isEditing = false;
    },

    async lookupCep() {
      const cep = this.profile.cep?.replace(/\D/g, '');
      if (cep && cep.length === 8) {
        this.loadingCep = true;
        try {
          const data = await cepService.buscarCep(cep);
          if (!data.erro) {
            this.profile.logradouro = data.logradouro;
            this.profile.bairro = data.bairro;
            this.profile.cidade = data.localidade;
            this.profile.estado = data.uf;
          }
        } catch (error) {
          console.error('Erro ao buscar CEP:', error);
        } finally {
          this.loadingCep = false;
        }
      }
    },

    async handleUpdateProfile() {
      this.isSaving = true;
      try {
        await authService.updateProfile(this.profile);
        this.originalProfile = { ...this.profile };
        this.isEditing = false;
        alert('Perfil atualizado com sucesso!');
        
        // Atualiza o usuário no Vuex
        this.$store.commit('SET_USER', {
          ...this.user,
          name: this.profile.nome
        });
        localStorage.setItem('user', JSON.stringify({
          ...this.user,
          name: this.profile.nome
        }));
      } catch (error) {
        console.error('Erro ao atualizar perfil:', error);
        alert('Erro ao atualizar perfil: ' + (error.response?.data?.message || error.message));
      } finally {
        this.isSaving = false;
      }
    },

    async handleChangePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        alert('A nova senha e a confirmação não coincidem.');
        return;
      }
      
      this.isChangingPassword = true;
      try {
        await authService.changePassword({
          currentPassword: this.passwordForm.currentPassword,
          newPassword: this.passwordForm.newPassword
        });
        alert('Senha alterada com sucesso!');
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
      } catch (error) {
        console.error('Erro ao alterar senha:', error);
        alert('Erro ao alterar senha: ' + (error.response?.data?.message || error.message));
      } finally {
        this.isChangingPassword = false;
      }
    },

    async handleLogout() {
      if (confirm('Tem certeza que deseja sair?')) {
        this.logout();
        this.$router.push('/');
      }
    }
  }
};
</script>

<style scoped>
.profile-page { background: var(--cf-white); min-height: 90vh; }

/* SIDEBAR */
.cf-profile-sidebar {
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  padding: 1.5rem;
}
.nav-link-cf {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0.85rem 1.25rem;
  font-family: var(--cf-sans);
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--cf-text-muted);
  background: transparent;
  border: none;
  border-radius: var(--cf-r-md);
  text-align: left;
  transition: all 200ms;
  text-decoration: none;
  width: 100%;
}
.nav-link-cf i { width: 20px; font-size: 1rem; color: var(--cf-text-faint); }
.nav-link-cf:hover { background: var(--cf-white); color: var(--cf-green); }
.nav-link-cf.active { background: var(--cf-green); color: white; }
.nav-link-cf.active i { color: white; }

.sidebar-stats { background: var(--cf-white); border-radius: var(--cf-r-lg); border: 1px solid var(--cf-border); }
.stats-label { font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.12em; color: var(--cf-text-faint); font-weight: 600; display: block; }
.stats-val { font-size: 0.95rem; color: var(--cf-text-dark); font-weight: 500; }
.fidelity-badge { font-size: 0.65rem; padding: 4px 10px; background: var(--cf-gold-light); color: var(--cf-gold); border-radius: 100px; display: inline-block; font-weight: 600; text-transform: uppercase; }

/* CONTENT CARD */
.cf-content-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  padding: 3rem;
  min-height: 100%;
}
.section-title-sm { font-family: var(--cf-sans); font-size: 1.8rem; font-weight: 600; color: var(--cf-green); }

.cf-form-box { background: var(--cf-ivory); padding: 2rem; border-radius: var(--cf-r-lg); border: 1px solid var(--cf-border); }
.form-box-title { font-family: var(--cf-sans); font-size: 1.4rem; font-weight: 600; color: var(--cf-text-dark); }

@media (max-width: 768px) {
  .cf-content-card { padding: 1.5rem; }
}

.input-group-text {
  border-left: none;
}
</style>