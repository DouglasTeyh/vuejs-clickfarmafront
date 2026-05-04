<template>
  <div class="cf-mgmt">
    <div class="dash-welcome mb-4">
      <div class="welcome-text">
        <h3 class="dash-page-title">Gestão de Logística</h3>
        <p class="text-muted mb-0">Administre a frota de entregadores parceiros e repasses financeiros.</p>
      </div>
      <div class="dash-actions">
        <div class="cf-search-wrap">
          <i class="fas fa-search cf-search-icon"></i>
          <input v-model="busca" type="text" class="cf-search" placeholder="Buscar por nome ou CPF...">
        </div>
      </div>
    </div>

    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div><span>Sincronizando frota...</span>
      </div>
      <div v-else class="table-responsive cf-hide-scrollbar">
        <table class="cf-table">
          <thead>
            <tr>
              <th class="ps-4">ID</th>
              <th>Entregador / Operador</th>
              <th>Documentação</th>
              <th>Dados Financeiros</th>
              <th>Cadastro</th>
              <th>Eficiência</th>
              <th class="text-center pe-4">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in motoboysFiltrados" :key="m.id">
              <td class="ps-4">
                <div class="cf-td-bold text-muted small">#{{ m.id }}</div>
              </td>
              <td>
                <div class="cf-avatar-row">
                  <div class="cf-icon-avatar bg-warning-subtle text-warning shadow-sm"><i class="fas fa-motorcycle"></i></div>
                  <div>
                    <div class="cf-td-bold text-dark">{{ m.nome }}</div>
                    <div class="text-muted extra-small fw-bold">{{ m.telefone || 'Sem contato' }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="cf-mono fw-bold">CPF: {{ m.cpf }}</div>
                <div class="text-muted extra-small">Perfil Verificado</div>
              </td>
              <td>
                <div v-if="m.chavePix" class="small">
                  <span class="cf-role-badge shadow-sm" style="background:var(--cf-ivory);color:var(--cf-gold);border:1px solid rgba(184,149,80,0.1)">
                    {{ m.tipoChavePix }}
                  </span>
                  <div class="text-muted extra-small mt-1 fw-bold">{{ m.chavePix }}</div>
                </div>
                <span v-else class="text-muted extra-small fw-bold opacity-50 italic">PIX Pendente</span>
              </td>
              <td class="text-muted small fw-bold">{{ formatarData(m.dataCadastro) }}</td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <span class="status-dot online"></span>
                  <span class="extra-small fw-bold text-success text-uppercase letter-spacing-1">Disponível</span>
                </div>
              </td>
              <td class="text-center pe-4">
                <div class="d-flex justify-content-center gap-2">
                  <button class="cf-icon-btn shadow-sm" @click="abrirEdicao(m)" title="Configurar Perfil"><i class="fas fa-user-pen"></i></button>
                  <button class="cf-icon-btn danger shadow-sm" @click="confirmarDelete(m)" title="Remover"><i class="fas fa-trash-can"></i></button>
                </div>
              </td>
            </tr>
            <tr v-if="motoboysFiltrados.length === 0">
              <td colspan="7" class="cf-empty py-5">
                <i class="fas fa-motorcycle fa-3x mb-3 opacity-10"></i>
                <p class="fw-bold text-muted">Nenhum operador logístico localizado.</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de Edição -->
    <div v-if="modalEdicao" class="cf-modal-overlay" @click.self="fecharModal">
      <div class="cf-modal-box animate__animated animate__fadeInUp dash-card" style="width: 700px;">
        <div class="cf-modal-header border-bottom bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-warning-subtle text-warning shadow-sm">
              <i class="fas fa-motorcycle"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Perfil do Entregador</h5>
              <p class="mb-0 text-muted extra-small fw-bold text-uppercase letter-spacing-1">ID Operador: #{{ editandoId }}</p>
            </div>
          </div>
          <button class="btn-close-custom shadow-sm" @click="fecharModal"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body p-4 bg-white">
          <div class="row g-4">
            <div class="col-md-8">
              <label class="cf-label-premium">Nome Completo</label>
              <input v-model="form.nome" class="cf-input-premium" placeholder="Nome do entregador">
            </div>
            <div class="col-md-4">
              <label class="cf-label-premium">CPF Fiscal</label>
              <input v-model="form.cpf" class="cf-input-premium" placeholder="000.000.000-00">
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">Telefone de Contato</label>
              <input v-model="form.telefone" class="cf-input-premium" placeholder="(00) 00000-0000">
            </div>
            <div class="col-md-3">
              <label class="cf-label-premium">Tipo PIX</label>
              <select v-model="form.tipoChavePix" class="cf-input-premium">
                <option value="CPF">CPF</option>
                <option value="EMAIL">E-mail</option>
                <option value="TELEFONE">Telefone</option>
                <option value="ALEATORIA">Chave Aleatória</option>
              </select>
            </div>
            <div class="col-md-9">
              <label class="cf-label-premium">Chave PIX para Repasses</label>
              <input v-model="form.chavePix" class="cf-input-premium" placeholder="Insira a chave para pagamentos">
            </div>
          </div>
        </div>
        
        <div class="cf-modal-footer bg-light-subtle p-3 border-top">
          <button class="btn btn-outline-secondary fw-bold px-4 rounded-pill" @click="fecharModal">Cancelar</button>
          <button class="cf-btn-primary px-4 shadow-sm" @click="salvar" :disabled="salvando">
            <i v-if="salvando" class="spinner-border spinner-border-sm me-2"></i>
            Atualizar Cadastro
          </button>
        </div>
      </div>
    </div>

    <!-- Confirmação Delete -->
    <div v-if="paraDeletar" class="cf-modal-overlay" @click.self="paraDeletar = null">
      <div class="cf-modal-box animate__animated animate__headShake shadow-lg" style="max-width:420px">
        <div class="cf-modal-header bg-danger-subtle py-3 border-0">
          <div class="d-flex align-items-center gap-3 text-danger">
            <i class="fas fa-exclamation-triangle fa-lg"></i>
            <h5 class="mb-0 fw-bold">Remover Operador</h5>
          </div>
        </div>
        <div class="cf-modal-body text-center p-4 bg-white">
          <p class="text-muted mb-2">Deseja realmente desvincular o entregador:</p>
          <h5 class="fw-bold text-dark mb-4">{{ paraDeletar.nome }}</h5>
          <div class="alert alert-danger small border-0 py-2">
            <i class="fas fa-info-circle me-1"></i> Esta ação é irreversível e removerá todos os acessos.
          </div>
        </div>
        <div class="cf-modal-footer bg-light-subtle border-0 p-3">
          <button class="btn btn-outline-secondary fw-bold flex-grow-1 rounded-pill" @click="paraDeletar = null">Manter Cadastro</button>
          <button class="btn btn-danger fw-bold flex-grow-1 rounded-pill shadow-sm" @click="deletar">Sim, Remover</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CourierManagement',
  data() {
    return { 
      motoboys: [], isLoading: true, busca: '', paraDeletar: null,
      modalEdicao: false, form: {}, editandoId: null, salvando: false
    };
  },
  computed: {
    motoboysFiltrados() {
      if (!this.busca) return this.motoboys;
      const t = this.busca.toLowerCase();
      return this.motoboys.filter(m =>
        m.nome?.toLowerCase().includes(t) || m.cpf?.includes(t)
      );
    }
  },
  async mounted() {
    await this.carregar();
  },
  methods: {
    async carregar() {
      this.isLoading = true;
      try {
        const res = await api.get('/motoboys');
        this.motoboys = res.data;
      } catch (err) {
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    abrirEdicao(m) { this.editandoId = m.id; this.form = { ...m }; this.modalEdicao = true; },
    fecharModal() { this.modalEdicao = false; this.form = {}; this.editandoId = null; },
    async salvar() {
      this.salvando = true;
      try { 
        await api.put(`/motoboys/${this.editandoId}`, this.form); 
        await this.carregar(); 
        this.fecharModal(); 
      }
      catch { alert('Erro ao salvar.'); }
      finally { this.salvando = false; }
    },
    confirmarDelete(m) { this.paraDeletar = m; },
    async deletar() {
      try {
        await api.delete(`/motoboys/${this.paraDeletar.id}`);
        await this.carregar();
        this.paraDeletar = null;
      } catch (err) {
        alert('Erro ao remover.');
      }
    },
    formatarData(d) {
      if (!d) return '—';
      return new Date(d).toLocaleDateString('pt-BR');
    }
  }
};
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

.cf-table-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }

.cf-avatar-row { display: flex; align-items: center; gap: 0.85rem; }
.cf-icon-avatar { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; flex-shrink: 0; }

.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

.cf-mono { font-family: 'DM Mono', monospace; font-size: 0.78rem; color: var(--cf-text-dark); }

.cf-role-badge { font-size: 0.62rem; font-weight: 800; letter-spacing: 0.1em; padding: 0.25rem 0.65rem; border-radius: 20px; text-transform: uppercase; display: inline-flex; align-items: center; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
