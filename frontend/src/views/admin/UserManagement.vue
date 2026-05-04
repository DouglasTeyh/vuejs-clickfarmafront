<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE GOVERNANÇA ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Gestão de Identidades</h3>
        <p class="editorial-subtitle">Administração de permissões e controle de acesso da rede</p>
      </div>
      <div class="header-tools">
        <div class="cf-input-group" style="width: auto;">
          <i class="fas fa-search"></i>
          <input v-model="busca" type="text" placeholder="Localizar usuário ou e-mail..." style="width: 200px;">
        </div>
        <div class="cf-input-group" style="width: auto; padding: 0.4rem 1rem;">
          <i class="fas fa-id-badge"></i>
          <select v-model="filtroRole" style="border: none; outline: none; background: transparent; font-size: 0.9rem; color: var(--cf-text-dark); cursor: pointer;">
            <option value="">Todos os Perfis</option>
            <option value="CUSTOMER">Consumidores</option>
            <option value="PHARMACY">Farmacêuticos</option>
            <option value="COURIER">Logística/Entregadores</option>
            <option value="ADMIN">Administradores</option>
          </select>
        </div>
        <button class="btn-refresh" @click="carregarUsuarios" :disabled="isLoading">
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
                <div class="entity-cell">
                  <div class="entity-avatar" :style="`background:${corRole(u.role)}15; color:${corRole(u.role)}`">
                    {{ u.nome?.charAt(0)?.toUpperCase() || '?' }}
                  </div>
                  <div class="entity-meta">
                    <span class="entity-name">{{ u.nome }}</span>
                    <span class="entity-sub">ID: #{{ u.id }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="doc-cell">
                  <span class="doc-val">{{ u.email }}</span>
                  <span class="loc-addr">{{ u.telefone || 'Telefone não vinculado' }}</span>
                </div>
              </td>
              <td>
                <div class="status-pill" :style="`background:${corRole(u.role)}10; border-color:${corRole(u.role)}30; color:${corRole(u.role)}`">
                  <span class="pill-dot" :style="`background:${corRole(u.role)}`"></span>
                  <span class="pill-text">{{ u.role || 'GUEST' }}</span>
                </div>
              </td>
              <td>
                <div class="loc-cell">
                  <span class="loc-city">{{ u.cidade || '—' }}</span>
                </div>
              </td>
              <td>
                <div class="date-cell">
                  <span class="date-val">{{ d(u.dataCadastro) }}</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-circle" @click="abrirEdicao(u)" title="Editar Perfil">
                    <i class="fas fa-user-edit"></i>
                  </button>
                  <button class="action-btn-circle danger" @click="confirmarExclusao(u)" title="Revogar Acesso">
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
      <div class="config-modal animate__animated animate__fadeInUp">
        <header class="config-header">
          <div class="header-brand">
            <div class="config-icon"><i class="fas fa-shield-halved"></i></div>
            <div class="config-meta">
              <h5>Perfil Administrativo</h5>
              <span class="config-id">EDITANDO IDENTIDADE #{{ form.id }}</span>
            </div>
          </div>
          <button class="close-modal" @click="fecharModal"><i class="fas fa-times"></i></button>
        </header>

        <div class="config-content cf-hide-scrollbar">
          <div class="config-grid">
            <div class="form-group col-span-2">
              <label class="editorial-label">Nome Completo do Portador</label>
              <input v-model="form.nome" class="editorial-input" type="text" placeholder="Nome social ou fantasia">
            </div>

            <div class="form-group">
              <label class="editorial-label">Perfil de Autorização</label>
              <select v-model="form.role" class="editorial-select">
                <option value="CUSTOMER">Consumidor (Padrão)</option>
                <option value="PHARMACY">Estabelecimento Farmacêutico</option>
                <option value="COURIER">Operador Logístico</option>
                <option value="ADMIN">Autoridade Administrativa</option>
              </select>
            </div>

            <div class="form-group">
              <label class="editorial-label">Telefone de Contato</label>
              <input v-model="form.telefone" @input="form.telefone = formatPhone($event.target.value)" class="editorial-input" type="text" placeholder="(00) 00000-0000">
            </div>

            <div class="form-group">
              <label class="editorial-label">CEP</label>
              <input v-model="form.cep" @input="form.cep = formatCEP($event.target.value)" class="editorial-input" type="text" placeholder="00000-000">
            </div>

            <div class="form-group col-span-2">
              <label class="editorial-label">Localização Base (Cidade / UF)</label>
              <input v-model="form.cidade" class="editorial-input" type="text" placeholder="Ex: Rio de Janeiro - RJ">
            </div>

            <div class="form-group col-span-2">
              <label class="editorial-label">E-mail de Acesso (Identificador Único)</label>
              <div style="display: flex; gap: 1rem; align-items: center; background: #f8fafc; padding: 0.85rem 1.25rem; border-radius: 14px; border: 1px solid var(--cf-border);">
                <i class="fas fa-lock text-muted"></i>
                <input v-model="form.email" type="email" disabled style="border: none; background: transparent; width: 100%; color: var(--cf-text-faint); font-weight: 600; outline: none;">
              </div>
              <span style="font-size: 0.7rem; color: var(--cf-text-muted); margin-top: 0.5rem; display: block;">Este dado é imutável para fins de auditoria de conta.</span>
            </div>

          </div>
        </div>

        <footer class="config-footer">
          <button class="btn-cancel" @click="fecharModal">Descartar</button>
          <button class="btn-save" @click="salvarAlteracoes" :disabled="isSaving">
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
    },
    formatCEP(v) {
      if (!v) return '';
      v = v.replace(/\D/g, '');
      v = v.replace(/^(\d{5})(\d)/, '$1-$2');
      return v.substring(0, 9);
    },
    formatPhone(v) {
      if (!v) return '';
      v = v.replace(/\D/g, '');
      v = v.replace(/^(\d{2})(\d)/, '($1) $2');
      v = v.replace(/(\d)(\d{4})$/, '$1-$2');
      return v.substring(0, 15);
    }
  }
};
</script>

