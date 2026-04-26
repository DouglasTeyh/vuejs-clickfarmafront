<template>
  <div class="profile-page">
    <div class="container py-lg-5 py-4">
      
      <div class="header-wrap mb-5 fade-in-up">
        <span class="section-eyebrow">Minha Conta</span>
        <h1 class="section-title">Olá, <em>{{ profile.name || 'Usuário' }}</em></h1>
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
                @click="activeSection = 'addresses'"
                class="nav-link-cf"
                :class="{ active: activeSection === 'addresses' }"
              >
                <i class="fa-solid fa-location-dot"></i>
                Meus Endereços
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
                  @click="toggleEditMode('personal')"
                  class="btn btn-sm"
                  :class="editMode.personal ? 'btn-outline-danger' : 'btn-outline-primary'"
                >
                  <i class="fa-solid" :class="editMode.personal ? 'fa-xmark' : 'fa-pen-to-square'"></i>
                  {{ editMode.personal ? 'Cancelar' : 'Editar' }}
                </button>
              </div>

              <form @submit.prevent="updateProfile" class="cf-form">
                <div class="row g-4">
                  <div class="col-md-6">
                    <label class="form-label">Nome Completo</label>
                    <input 
                      v-model="profile.name" 
                      type="text" 
                      class="form-control" 
                      :disabled="!editMode.personal"
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">E-mail</label>
                    <input 
                      v-model="profile.email" 
                      type="email" 
                      class="form-control" 
                      :disabled="!editMode.personal"
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Telefone celular</label>
                    <input 
                      v-model="profile.phone" 
                      type="tel" 
                      class="form-control"
                      :disabled="!editMode.personal"
                      placeholder="(00) 00000-0000"
                    >
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Data de Nascimento</label>
                    <input 
                      v-model="profile.birthDate" 
                      type="date" 
                      class="form-control"
                      :disabled="!editMode.personal"
                    >
                  </div>
                  <div class="col-md-12">
                     <label class="form-label">CPF (Apenas leitura)</label>
                     <input 
                       v-model="profile.cpf" 
                       type="text" 
                       class="form-control"
                       disabled
                     >
                  </div>
                </div>

                <div v-if="editMode.personal" class="mt-5 d-flex gap-3">
                  <button type="submit" class="btn btn-primary px-5">
                    <i class="fa-solid fa-floppy-disk me-2"></i>
                    Salvar Alterações
                  </button>
                </div>
              </form>
            </div>

            <!-- SEÇÃO ENDEREÇOS -->
            <div v-show="activeSection === 'addresses'" class="section-content">
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="section-title-sm">Meus Endereços</h3>
                <button 
                  v-if="!showAddAddressForm"
                  @click="showAddAddressForm = true"
                  class="btn btn-sm btn-primary"
                >
                  <i class="fa-solid fa-plus me-1"></i>
                  Novo Endereço
                </button>
              </div>

              <div class="addresses-grid" v-if="!showAddAddressForm">
                <div v-for="address in addresses" :key="address.id" class="cf-address-card">
                  <div class="d-flex justify-content-between">
                    <div class="address-icon-wrap">
                      <i class="fa-solid fa-house-user"></i>
                    </div>
                    <div class="address-actions">
                      <button class="icon-btn"><i class="fa-solid fa-pen"></i></button>
                      <button class="icon-btn text-danger"><i class="fa-solid fa-trash-can"></i></button>
                    </div>
                  </div>
                  <h5 class="address-nick mt-3">{{ address.nickname }}</h5>
                  <p class="address-text">{{ address.street }}, {{ address.number }}</p>
                  <p class="address-text">{{ address.neighborhood }} - {{ address.city }}/{{ address.state }}</p>
                  <p class="address-text mb-3">CEP: {{ address.zipCode }}</p>
                  <span v-if="address.isDefault" class="cf-badge badge-success">Padrão</span>
                </div>
              </div>

              <div v-else class="cf-form-box fade-in">
                 <h4 class="form-box-title mb-4">Adicionar Endereço</h4>
                 <form @submit.prevent="addAddress">
                    <div class="row g-3">
                       <div class="col-md-6">
                         <label class="form-label">Apelido (ex: Casa)</label>
                         <input v-model="newAddress.nickname" type="text" class="form-control" placeholder="Trabalho, Casa da Mãe...">
                       </div>
                       <div class="col-md-6">
                         <label class="form-label">CEP</label>
                         <input v-model="newAddress.zipCode" type="text" class="form-control">
                       </div>
                       <div class="col-md-10">
                         <label class="form-label">Logradouro</label>
                         <input v-model="newAddress.street" type="text" class="form-control">
                       </div>
                       <div class="col-md-2">
                         <label class="form-label">Núm.</label>
                         <input v-model="newAddress.number" type="text" class="form-control">
                       </div>
                    </div>
                    <div class="mt-4 d-flex gap-2">
                      <button type="submit" class="btn btn-primary">Salvar</button>
                      <button type="button" class="btn btn-outline-primary" @click="cancelAddAddress">Cancelar</button>
                    </div>
                 </form>
              </div>
            </div>

            <!-- SEÇÃO SEGURANÇA -->
            <div v-show="activeSection === 'security'" class="section-content">
               <h3 class="section-title-sm mb-4">Segurança da Conta</h3>
               <div class="row">
                 <div class="col-md-12">
                   <div class="cf-form-box">
                      <h4 class="form-box-title mb-3">Alterar Senha</h4>
                      <form @submit.prevent="changePassword">
                        <div class="mb-3">
                          <label class="form-label">Senha Atual</label>
                          <input v-model="passwordForm.currentPassword" type="password" class="form-control">
                        </div>
                        <div class="mb-3">
                          <label class="form-label">Nova Senha</label>
                          <input v-model="passwordForm.newPassword" type="password" class="form-control">
                        </div>
                        <div class="mb-4">
                          <label class="form-label">Confirmar Nova Senha</label>
                          <input v-model="passwordForm.confirmPassword" type="password" class="form-control">
                        </div>
                        <button type="submit" class="btn btn-primary px-4">Atualizar Senha</button>
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
import { mapState } from 'vuex'

export default {
  name: 'Profile',
  data() {
    return {
      activeSection: 'personal',
      editMode: { personal: false },
      showAddAddressForm: false,
      profile: { name: '', email: '', phone: '', birthDate: '', cpf: '123.456.789-00' },
      addresses: [
        { id: 1, nickname: 'CasaPrincipal', street: 'Rua das Flores', number: '123', neighborhood: 'Centro', city: 'São Paulo', state: 'SP', zipCode: '01234-567', isDefault: true }
      ],
      newAddress: { nickname: '', street: '', number: '', neighborhood: '', city: '', state: '', zipCode: '' },
      passwordForm: { currentPassword: '', newPassword: '', confirmPassword: '' },
      memberSince: 'Janeiro de 2024'
    }
  },
  computed: { ...mapState(['user']) },
  mounted() {
    if (this.user) {
      this.profile.name = this.user.name || '';
      this.profile.email = this.user.email || '';
    }
  },
  methods: {
    toggleEditMode(section) { this.editMode[section] = !this.editMode[section]; },
    updateProfile() { alert('Perfil atualizado!'); this.editMode.personal = false; },
    addAddress() {
      this.addresses.push({ id: Date.now(), ...this.newAddress, isDefault: false });
      this.cancelAddAddress();
    },
    cancelAddAddress() { this.showAddAddressForm = false; this.newAddress = { nickname: '', street: '', number: '', neighborhood: '', city: '', state: '', zipCode: '' }; },
    changePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) return alert('Senhas não coincidem');
      alert('Senha alterada!');
      this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
    }
  }
}
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

/* ADDRESS GRID */
.addresses-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.5rem; }
.cf-address-card {
  padding: 1.75rem;
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  position: relative;
  transition: all 220ms;
}
.cf-address-card:hover { border-color: var(--cf-gold); transform: translateY(-3px); box-shadow: var(--cf-shadow-sm); }
.address-nick { font-family: var(--cf-sans); font-size: 1.3rem; font-weight: 600; margin-bottom: 0.5rem; }
.address-text { font-size: 0.9rem; margin-bottom: 0.15rem; color: var(--cf-text-muted); }
.address-icon-wrap { width: 40px; height: 40px; background: var(--cf-sage-tint); color: var(--cf-green); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; }
.icon-btn { background: transparent; border: none; color: var(--cf-text-faint); padding: 5px; transition: color 200ms; }
.icon-btn:hover { color: var(--cf-green); }

.cf-form-box { background: var(--cf-ivory); padding: 2rem; border-radius: var(--cf-r-lg); border: 1px solid var(--cf-border); }
.form-box-title { font-family: var(--cf-sans); font-size: 1.4rem; font-weight: 600; color: var(--cf-text-dark); }

.cf-badge { font-size: 0.6rem; padding: 3px 10px; border-radius: 100px; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }
.badge-success { background: var(--cf-green-light); color: var(--cf-green); }

@media (max-width: 768px) {
  .cf-content-card { padding: 1.5rem; }
}
</style>