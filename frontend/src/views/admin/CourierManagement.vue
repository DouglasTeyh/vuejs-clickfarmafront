<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE LOGÍSTICA ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Gestão de Logística</h3>
        <p class="editorial-subtitle">Administre a frota de entregadores parceiros e repasses financeiros.</p>
      </div>
      <div class="header-tools">
        <div class="cf-input-group" style="width: auto;">
          <i class="fas fa-search"></i>
          <input v-model="busca" type="text" placeholder="Buscar por nome ou CPF..." style="width: 200px;">
        </div>
        <button class="btn-refresh" @click="carregar" :disabled="isLoading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
        </button>
      </div>
    </header>

    <!-- ═══ GRID DE ENTREGADORES ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Sincronizando frota...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Identificador</th>
              <th>Operador</th>
              <th>Documentação</th>
              <th>Dados Financeiros</th>
              <th>Cadastro</th>
              <th>Disponibilidade</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in motoboysFiltrados" :key="m.id" class="row-hover">
              <td class="ps-5">
                <div class="id-badge">#{{ m.id }}</div>
              </td>
              <td>
                <div class="entity-cell">
                  <div class="entity-avatar gold">
                    <i class="fas fa-motorcycle"></i>
                  </div>
                  <div class="entity-meta">
                    <span class="entity-name">{{ m.nome }}</span>
                    <span class="entity-sub">{{ m.telefone || 'Sem contato' }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="doc-cell">
                  <span class="doc-val">{{ m.cpf }}</span>
                  <span class="doc-label mt-1">PERFIL VERIFICADO</span>
                </div>
              </td>
              <td>
                <div v-if="m.chavePix" class="doc-cell">
                  <span class="doc-label text-gold">{{ m.tipoChavePix }}</span>
                  <span class="doc-val mt-1">{{ m.chavePix }}</span>
                </div>
                <span v-else class="doc-label text-muted">PIX PENDENTE</span>
              </td>
              <td>
                <div class="date-cell">
                  <span class="date-val">{{ formatarData(m.dataCadastro) }}</span>
                </div>
              </td>
              <td>
                <div class="status-pill active">
                  <span class="pill-dot"></span>
                  <span class="pill-text">Disponível</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-circle" @click="abrirEdicao(m)" title="Configurar Perfil">
                    <i class="fas fa-user-pen"></i>
                  </button>
                  <button class="action-btn-circle danger" @click="confirmarDelete(m)" title="Remover Operador">
                    <i class="fas fa-trash-can"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="motoboysFiltrados.length === 0">
              <td colspan="7" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-motorcycle"></i>
                  <p>Nenhum operador logístico localizado.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: CONFIGURAÇÃO DE OPERADOR ═══ -->
    <div v-if="modalEdicao" class="modal-premium-overlay" @click.self="fecharModal">
      <div class="config-modal animate__animated animate__fadeInUp">
        <header class="config-header">
          <div class="header-brand">
            <div class="config-icon" style="background: var(--cf-gold);"><i class="fas fa-motorcycle"></i></div>
            <div class="config-meta">
              <h5>Perfil do Entregador</h5>
              <span class="config-id">ID OPERADOR #{{ editandoId }}</span>
            </div>
          </div>
          <button class="close-modal" @click="fecharModal"><i class="fas fa-times"></i></button>
        </header>

        <div class="config-content cf-hide-scrollbar">
          <div class="config-grid">
            <div class="form-group col-span-2">
              <label class="editorial-label">Nome Completo</label>
              <input v-model="form.nome" class="editorial-input" placeholder="Nome do entregador">
            </div>

            <div class="form-group">
              <label class="editorial-label">CPF Fiscal</label>
              <input v-model="form.cpf" v-mask="'cpf'" class="editorial-input" placeholder="000.000.000-00">
            </div>

            <div class="form-group">
              <label class="editorial-label">Telefone de Contato</label>
              <input v-model="form.telefone" v-mask="'phone'" class="editorial-input" placeholder="(00) 00000-0000">
            </div>

            <div class="section-divider col-span-2">Inteligência Financeira (Repasses)</div>

            <div class="form-group">
              <label class="editorial-label">Tipo PIX</label>
              <select v-model="form.tipoChavePix" class="editorial-select">
                <option value="CPF">CPF</option>
                <option value="EMAIL">E-mail</option>
                <option value="TELEFONE">Telefone</option>
                <option value="ALEATORIA">Chave Aleatória</option>
              </select>
            </div>

            <div class="form-group">
              <label class="editorial-label">Chave PIX para Repasses</label>
              <input v-model="form.chavePix" v-mask="form.tipoChavePix === 'CPF' ? 'cpf' : (form.tipoChavePix === 'TELEFONE' ? 'phone' : '')" class="editorial-input" placeholder="Insira a chave para pagamentos">
            </div>
          </div>
        </div>

        <footer class="config-footer">
          <button class="btn-cancel" @click="fecharModal">Cancelar</button>
          <button class="btn-save" @click="salvar" :disabled="salvando" style="background: var(--cf-gold); box-shadow: 0 4px 15px rgba(184,149,80,0.3);">
            <i v-if="salvando" class="fas fa-circle-notch fa-spin me-2"></i>
            Atualizar Cadastro
          </button>
        </footer>
      </div>
    </div>

    <!-- ═══ MODAL: EXCLUIR ═══ -->
    <div v-if="paraDeletar" class="modal-premium-overlay" @click.self="paraDeletar = null">
      <div class="alert-modal animate__animated animate__headShake">
        <div class="alert-icon"><i class="fas fa-exclamation-triangle"></i></div>
        <h4>Remover Operador</h4>
        <p>Deseja realmente desvincular o entregador <strong>{{ paraDeletar.nome }}</strong>? Esta ação é irreversível.</p>
        <div class="alert-actions">
          <button class="btn-cancel flex-1" @click="paraDeletar = null">Manter</button>
          <button class="btn-confirm-delete flex-1" @click="deletar">Sim, Remover</button>
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

