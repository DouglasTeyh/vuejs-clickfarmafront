<template>
  <div class="cf-mgmt-premium">
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Conciliação Financeira</h3>
        <p class="editorial-subtitle">Gestão de repasses, auditoria de PIX e fechamento de ciclos operacionais.</p>
      </div>
      <div class="header-tools">
        <button class="btn-save" @click="showGenerateModal = true" style="background: var(--cf-gold); box-shadow: 0 4px 15px rgba(184,149,80,0.3);">
          <i class="fas fa-plus-circle me-2"></i>Gerar Ciclo de Repasse
        </button>
      </div>
    </header>

    <!-- Filtros/Abas Premium -->
    <div class="cf-tabs-container">
      <div class="cf-tabs-wrapper">
        <button class="cf-tab-premium" :class="{ active: tab === 'TODOS' }" @click="tab = 'TODOS'">
          <i class="fas fa-list-ul me-2 opacity-50"></i>Todos os Registros
        </button>
        <button class="cf-tab-premium" :class="{ active: tab === 'PENDENTE' }" @click="tab = 'PENDENTE'">
          <i class="fas fa-clock me-2 opacity-50"></i>Repasses Pendentes
        </button>
        <button class="cf-tab-premium" :class="{ active: tab === 'PAGO' }" @click="tab = 'PAGO'">
          <i class="fas fa-check-double me-2 opacity-50"></i>Liquidações Concluídas
        </button>
      </div>
    </div>

    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Processando extratos financeiros...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Identificador</th>
              <th>Beneficiário / Destino</th>
              <th>Categoria</th>
              <th>Período</th>
              <th>Montante Líquido</th>
              <th>Canal de Pagamento</th>
              <th>Status</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in filteredPagamentos" :key="p.id" class="row-hover">
              <td class="ps-5">
                <div class="id-badge">#{{ p.id }}</div>
              </td>
              <td>
                <div class="entity-cell">
                  <div class="entity-avatar" :class="p.tipo === 'FARMACIA' ? 'green' : 'gold'">
                    <i :class="p.tipo === 'FARMACIA' ? 'fas fa-hospital' : 'fas fa-motorcycle'"></i>
                  </div>
                  <div class="entity-meta">
                    <span class="entity-name">{{ p.farmacia?.nome || p.motoboy?.nome }}</span>
                    <span class="entity-sub">{{ p.tipo === 'FARMACIA' ? 'Unidade Parceira' : 'Operador Logístico' }}</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="status-pill" :class="p.tipo === 'FARMACIA' ? 'active' : 'pending'">
                  <span class="pill-dot"></span>
                  <span class="pill-text">{{ p.tipo }}</span>
                </span>
              </td>
              <td>
                <div class="date-cell">
                  <span class="date-val"><i class="fas fa-calendar-day me-1 opacity-50"></i> {{ p.referenciaPeriodo }}</span>
                  <span class="time-val">Ciclo Mensal</span>
                </div>
              </td>
              <td>
                <div class="price-cell text-success">
                  <span class="currency">R$</span>
                  <span class="amount">{{ Number(p.valorLiquido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</span>
                </div>
                <div class="time-val">Taxas Deduzidas</div>
              </td>
              <td>
                <div class="cf-pix-pill" v-if="p.chavePix" @click="copyPix(p.chavePix)">
                  <span class="label">{{ p.tipoChavePix }}</span>
                  <span class="value">{{ p.chavePix }}</span>
                  <i class="fas fa-copy ms-1 text-gold"></i>
                </div>
                <span v-else class="doc-label text-muted">PIX não informado</span>
              </td>
              <td>
                <span class="status-pill shadow-sm" :class="p.status === 'PENDENTE' ? 'pending' : (p.status === 'PAGO' ? 'active' : 'rejected')">
                  <span class="pill-dot"></span>
                  <span class="pill-text">{{ p.status }}</span>
                </span>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button v-if="p.status === 'PENDENTE'" class="action-btn-circle" style="color: var(--cf-green); border-color: var(--cf-green);" @click="prepararPagamento(p)" title="Liquidar Repasse">
                    <i class="fas fa-money-bill-transfer"></i>
                  </button>
                  <button class="action-btn-circle" @click="viewDetails(p)" title="Detalhes do Lançamento">
                    <i class="fas fa-receipt"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPagamentos.length === 0">
              <td colspan="8" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-file-invoice-dollar"></i>
                  <p>Nenhum lançamento financeiro localizado.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: GERAR REPASSE ═══ -->
    <div v-if="showGenerateModal" class="modal-premium-overlay" @click.self="showGenerateModal = false">
      <div class="config-modal animate__animated animate__fadeInUp" style="max-width: 520px;">
        <header class="config-header">
          <div class="header-brand">
            <div class="config-icon" style="background: var(--cf-gold);"><i class="fas fa-hand-holding-dollar"></i></div>
            <div class="config-meta">
              <h5>Gerar Novo Repasse</h5>
              <span class="config-id">Configuração de Ciclo</span>
            </div>
          </div>
          <button class="close-modal" @click="showGenerateModal = false"><i class="fas fa-times"></i></button>
        </header>

        <div class="config-content cf-hide-scrollbar">
          <div class="config-grid" style="grid-template-columns: 1fr;">
            <div class="form-group">
              <label class="editorial-label">Categoria de Destinatário</label>
              <select v-model="gen.tipo" class="editorial-select">
                <option value="FARMACIA">Unidade Farmacêutica</option>
                <option value="MOTOBOY">Operador de Entrega</option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="editorial-label">Selecionar Parceiro</label>
              <select v-model="gen.id" class="editorial-select">
                <option :value="null" disabled>Selecione um parceiro ativo...</option>
                <template v-if="gen.tipo === 'FARMACIA'">
                  <option v-for="f in farmacias" :key="f.id" :value="f.id">{{ f.nome }}</option>
                </template>
                <template v-else>
                  <option v-for="m in motoboys" :key="m.id" :value="m.id">{{ m.nome }}</option>
                </template>
              </select>
            </div>

            <div class="form-group">
              <label class="editorial-label">Mês de Referência</label>
              <input v-model="gen.periodo" type="month" class="editorial-input">
              <span class="doc-label mt-2" style="display: block;"><i class="fas fa-info-circle me-1"></i>O sistema calculará automaticamente as vendas deste período.</span>
            </div>
          </div>
        </div>

        <footer class="config-footer">
          <button class="btn-cancel" @click="showGenerateModal = false">Cancelar</button>
          <button class="btn-save" @click="gerarPagamento" :disabled="isGenerating" style="background: var(--cf-gold); box-shadow: 0 4px 15px rgba(184,149,80,0.3);">
            <i v-if="isGenerating" class="fas fa-circle-notch fa-spin me-2"></i>
            Processar Lançamento
          </button>
        </footer>
      </div>
    </div>

    <!-- ═══ MODAL: LIQUIDAR REPASSE ═══ -->
    <div v-if="pagando" class="modal-premium-overlay" @click.self="pagando = null">
      <div class="config-modal animate__animated animate__fadeInUp" style="max-width: 480px;">
        <header class="config-header" style="background: var(--cf-green-xlight); border-bottom-color: rgba(34,197,94,0.1);">
          <div class="header-brand">
            <div class="config-icon"><i class="fas fa-check-to-slot"></i></div>
            <div class="config-meta">
              <h5>Liquidar Repasse</h5>
              <span class="config-id text-success">Confirmação de Baixa</span>
            </div>
          </div>
        </header>

        <div class="config-content text-center">
          <p class="text-muted mb-1">Confirmação de transferência enviada para:</p>
          <h5 class="entity-name mb-4" style="font-size: 1.3rem;">{{ pagando.farmacia?.nome || pagando.motoboy?.nome }}</h5>
          
          <div class="cf-amount-display mb-4 shadow-sm">
            <div class="label text-muted">Montante a Liquidar</div>
            <div class="value text-success">R$ {{ Number(pagando.valorLiquido).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</div>
          </div>
          
          <div class="cf-pix-display-full mb-4 shadow-sm">
            <div class="label text-muted small fw-bold mb-1">DADOS DE DESTINO ({{ pagando.tipoChavePix }})</div>
            <div class="pix-value font-monospace fw-bold" style="font-size: 1.1rem; color: var(--cf-text-dark);">{{ pagando.chavePix }}</div>
            <button class="action-btn-view mt-3" @click="copyPix(pagando.chavePix)">
              <i class="fas fa-copy me-1"></i> Copiar Chave
            </button>
          </div>

          <div class="text-start">
            <label class="editorial-label">Referência / ID Transação</label>
            <textarea v-model="obs" class="editorial-textarea" rows="2" placeholder="Ex: Protocolo de transferência ou data..."></textarea>
          </div>
        </div>

        <footer class="config-footer">
          <button class="btn-cancel flex-1" @click="pagando = null">Cancelar</button>
          <button class="btn-save flex-1" @click="confirmarPagamento" :disabled="isPaying">
            Confirmar e Baixar
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script>
import paymentService from '@/services/paymentService';
import api from '@/services/api';

export default {
  name: 'PaymentManagement',
  data() {
    return {
      pagamentos: [], farmacias: [], motoboys: [],
      isLoading: true, tab: 'TODOS',
      showGenerateModal: false, isGenerating: false,
      gen: { tipo: 'FARMACIA', id: null, periodo: new Date().toISOString().slice(0, 7) },
      pagando: null, isPaying: false, obs: ''
    };
  },
  computed: {
    filteredPagamentos() {
      if (this.tab === 'TODOS') return this.pagamentos;
      return this.pagamentos.filter(p => p.status === this.tab);
    }
  },
  async mounted() {
    await this.fetchInitialData();
    await this.fetchPagamentos();
  },
  methods: {
    async fetchInitialData() {
      try {
        const { data: f } = await api.get('/farmacias');
        this.farmacias = f;
        const { data: m } = await api.get('/motoboys');
        this.motoboys = m;
      } catch (err) { console.error(err); }
    },
    async fetchPagamentos() {
      this.isLoading = true;
      try {
        const { data } = await paymentService.listarTodos();
        this.pagamentos = data;
      } catch (err) { console.error(err); }
      finally { this.isLoading = false; }
    },
    statusClass(s) {
      return { PENDENTE: 's-pending', PAGO: 's-paid', CANCELADO: 's-cancelled' }[s] || '';
    },
    async copyPix(pix) {
      try {
        await navigator.clipboard.writeText(pix);
        if (window.$toast) window.$toast.addToast('Chave PIX copiada!', 'success');
        else alert('Chave PIX copiada!');
      } catch { alert('Erro ao copiar.'); }
    },
    async gerarPagamento() {
      if (!this.gen.id) return alert('Selecione o destinatário');
      this.isGenerating = true;
      try {
        if (this.gen.tipo === 'FARMACIA') {
          await paymentService.gerarPagamentoFarmacia(this.gen.id, this.gen.periodo);
        } else {
          await paymentService.gerarPagamentoMotoboy(this.gen.id, this.gen.periodo);
        }
        await this.fetchPagamentos();
        this.showGenerateModal = false;
        if (window.$toast) window.$toast.addToast('Repasse gerado com sucesso!', 'success');
      } catch (err) {
        alert(err.response?.data?.erro || 'Erro ao gerar repasse.');
      } finally { this.isGenerating = false; }
    },
    prepararPagamento(p) { this.pagando = p; this.obs = ''; },
    async confirmarPagamento() {
      this.isPaying = true;
      try {
        await paymentService.marcarComoPago(this.pagando.id, this.obs);
        await this.fetchPagamentos();
        this.pagando = null;
        if (window.$toast) window.$toast.addToast('Pagamento confirmado!', 'success');
      } catch (err) { alert('Erro ao confirmar pagamento.'); }
      finally { this.isPaying = false; }
    },
    viewDetails(p) {
       alert(`Detalhamento:\nObservações: ${p.observacoes || 'Nenhuma'}\nCriado em: ${new Date(p.dataCriacao).toLocaleString()}\nReferência: ${p.referenciaPeriodo}`);
    }
  }
};
</script>


