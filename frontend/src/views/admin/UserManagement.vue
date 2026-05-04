<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE GOVERNANÇA ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Gestão de Identidades</h3>
        <p class="editorial-subtitle">Administração de permissões e controle de acesso da rede</p>
      </div>
      <div class="header-tools">
        <div class="cf-filter-bar shadow-sm">
          <div class="filter-search">
            <i class="fas fa-search"></i>
            <input v-model="busca" type="text" placeholder="Localizar usuário ou e-mail...">
          </div>
          <div class="filter-divider"></div>
          <div class="filter-select">
            <i class="fas fa-id-badge"></i>
            <select v-model="filtroRole">
              <option value="">Todos os Perfis</option>
              <option value="CUSTOMER">Consumidores</option>
              <option value="PHARMACY">Farmacêuticos</option>
              <option value="COURIER">Logística/Entregadores</option>
              <option value="ADMIN">Administradores</option>
            </select>
          </div>
        </div>
        <button class="btn-refresh-circle" @click="carregarUsuarios" :disabled="isLoading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
        </button>
      </div>
    </header>

    <!-- ═══ GRID DE USUÁRIOS ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Sincronizando banco de identidades...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Identidade</th>
              <th>Credencial</th>
              <th>Perfil de Acesso</th>
              <th>Localização</th>
              <th>Data Cadastro</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in filtrados" :key="u.id" class="row-hover">
              <td class="ps-5">
                <div class="user-cell">
                  <div class="user-avatar-premium" :style="`background:${corRole(u.role)}15; color:${corRole(u.role)}`">
                    {{ u.nome?.charAt(0)?.toUpperCase() || '?' }}
                  </div>
                  <div class="user-meta">
                    <span class="user-name">{{ u.nome }}</span>
                    <span class="user-id">ID: #{{ u.id }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="cred-cell">
                  <span class="cred-email">{{ u.email }}</span>
                  <span class="cred-phone">{{ u.telefone || 'Telefone não vinculado' }}</span>
                </div>
              </td>
              <td>
                <div class="role-pill" :style="`background:${corRole(u.role)}10; border-color:${corRole(u.role)}30; color:${corRole(u.role)}`">
                  <span class="pill-dot" :style="`background:${corRole(u.role)}`"></span>
                  <span class="pill-text">{{ u.role || 'GUEST' }}</span>
                </div>
              </td>
              <td>
                <div class="loc-cell-user">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>{{ u.cidade || '—' }}</span>
                </div>
              </td>
              <td>
                <div class="date-cell-user">
                  <span class="date-val">{{ d(u.dataCadastro) }}</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-user" @click="abrirEdicao(u)" title="Editar Perfil">
                    <i class="fas fa-user-edit"></i>
                  </button>
                  <button class="action-btn-user danger" @click="confirmarExclusao(u)" title="Revogar Acesso">
                    <i class="fas fa-user-slash"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!filtrados.length">
              <td colspan="6" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-user-lock"></i>
                  <p>Nenhum perfil localizado sob estes critérios.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: PERFIL ADMINISTRATIVO ═══ -->
    <div v-if="showModal" class="modal-premium-overlay" @click.self="fecharModal">
      <div class="user-modal animate__animated animate__fadeInUp">
        <header class="modal-header-premium">
          <div class="header-brand">
            <div class="modal-icon"><i class="fas fa-shield-halved"></i></div>
            <div class="modal-meta">
              <h5>Perfil Administrativo</h5>
              <span class="modal-sub">EDITANDO IDENTIDADE #{{ form.id }}</span>
            </div>
          </div>
          <button class="close-modal" @click="fecharModal"><i class="fas fa-times"></i></button>
        </header>

        <div class="modal-content-premium cf-hide-scrollbar">
          <div class="user-config-grid">
            <div class="form-group col-span-2">
              <label class="editorial-label">Nome Completo do Portador</label>
              <input v-model="form.nome" class="editorial-input lg" type="text" placeholder="Nome social ou fantasia">
            </div>

            <div class="form-group">
              <label class="editorial-label">Perfil de Autorização</label>
              <select v-model="form.role" class="editorial-select-premium">
                <option value="CUSTOMER">Consumidor (Padrão)</option>
                <option value="PHARMACY">Estabelecimento Farmacêutico</option>
                <option value="COURIER">Operador Logístico</option>
                <option value="ADMIN">Autoridade Administrativa</option>
              </select>
            </div>

            <div class="form-group">
              <label class="editorial-label">Telefone de Contato</label>
              <input v-model="form.telefone" class="editorial-input" type="text" placeholder="(00) 00000-0000">
            </div>

            <div class="form-group col-span-2">
              <label class="editorial-label">E-mail de Acesso (Identificador Único)</label>
              <div class="locked-input">
                <i class="fas fa-lock"></i>
                <input v-model="form.email" type="email" disabled>
                <span>Este dado é imutável para fins de auditoria de conta.</span>
              </div>
            </div>

            <div class="form-group col-span-2">
              <label class="editorial-label">Localização Base (Cidade / UF)</label>
              <input v-model="form.cidade" class="editorial-input" type="text" placeholder="Ex: Rio de Janeiro - RJ">
            </div>
          </div>
        </div>

        <footer class="modal-footer-premium">
          <button class="btn-cancel-modal" @click="fecharModal">Descartar</button>
          <button class="btn-save-modal" @click="salvarAlteracoes" :disabled="isSaving">
            <i v-if="isSaving" class="fas fa-circle-notch fa-spin me-2"></i>
            Atualizar Identidade
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';
export default {
  name: 'UserManagement',
  data() { 
    return { 
      usuarios: [], 
      isLoading: true, 
      isSaving: false,
      busca: '', 
      filtroRole: '',
      showModal: false,
      form: { id: null, nome: '', email: '', role: '', telefone: '', cep: '', logradouro: '', numero: '', bairro: '', cidade: '' }
    }; 
  },
  computed: {
    filtrados() {
      return this.usuarios.filter(u => {
        const mr = !this.filtroRole || u.role === this.filtroRole;
        const mb = !this.busca || u.nome?.toLowerCase().includes(this.busca.toLowerCase()) || u.email?.toLowerCase().includes(this.busca.toLowerCase());
        return mr && mb;
      });
    }
  },
  async mounted() {
    await this.carregarUsuarios();
  },
  methods: {
    async carregarUsuarios() {
      this.isLoading = true;
      try { const { data } = await api.get('/usuarios'); this.usuarios = data; }
      catch (e) { console.error(e); }
      finally { this.isLoading = false; }
    },
    corRole(r) { return { ADMIN:'#2A5C45', PHARMACY:'#3D7A5E', COURIER:'#B89550', CUSTOMER:'#868680' }[r] || '#B0AFA9'; },
    d(dt) { if (!dt) return '—'; return new Date(dt).toLocaleDateString('pt-BR'); },
    
    abrirEdicao(u) {
      this.form = { ...u };
      this.showModal = true;
    },
    fecharModal() {
      this.showModal = false;
      this.form = { id: null, nome: '', email: '', role: '', telefone: '', cep: '', logradouro: '', numero: '', bairro: '', cidade: '' };
    },
    async salvarAlteracoes() {
      this.isSaving = true;
      try {
        await api.put(`/usuarios/${this.form.id}`, this.form);
        if (window.$toast) window.$toast.addToast('Usuário atualizado com sucesso!', 'success');
        await this.carregarUsuarios();
        this.fecharModal();
      } catch (err) {
        console.error(err);
        alert('Erro ao atualizar usuário.');
      } finally {
        this.isSaving = false;
      }
    },
    async confirmarExclusao(u) {
      if (confirm(`Deseja realmente excluir o usuário ${u.nome}? Esta ação é irreversível.`)) {
        try {
          await api.delete(`/usuarios/${u.id}`);
          if (window.$toast) window.$toast.addToast('Usuário removido!', 'success');
          await this.carregarUsuarios();
        } catch (err) {
          console.error(err);
          alert('Erro ao excluir usuário.');
        }
      }
    }
  }
};
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

.cf-table-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }

.cf-avatar-row { display: flex; align-items: center; gap: 0.85rem; }
.cf-user-avatar { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 0.95rem; font-weight: 800; flex-shrink: 0; }

.cf-role-badge { font-size: 0.62rem; font-weight: 800; letter-spacing: 0.1em; padding: 0.35rem 0.85rem; border-radius: 20px; text-transform: uppercase; display: inline-flex; align-items: center; }

.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
