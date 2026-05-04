<template>
  <div class="cf-mgmt">
    <div class="dash-welcome mb-4">
      <div class="welcome-text">
        <h3 class="dash-page-title">Conciliação Financeira</h3>
        <p class="text-muted mb-0">Gestão de repasses, auditoria de PIX e fechamento de ciclos operacionais.</p>
      </div>
      <div class="dash-actions">
        <button class="cf-btn-primary shadow-sm" @click="showGenerateModal = true">
          <i class="fas fa-plus-circle me-2"></i>Gerar Ciclo de Repasse
        </button>
      </div>
    </div>

    <!-- Filtros/Abas Premium -->
    <div class="cf-tabs-container mb-4">
      <div class="cf-tabs-wrapper shadow-sm">
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

    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div><span>Processando extratos financeiros...</span>
      </div>
      <div v-else class="table-responsive cf-hide-scrollbar">
        <table class="cf-table">
          <thead>
            <tr>
              <th class="ps-4">ID</th>
              <th>Beneficiário / Destino</th>
              <th>Categoria</th>
              <th>Período</th>
              <th>Montante Líquido</th>
              <th>Canal de Pagamento</th>
              <th>Status</th>
              <th class="text-center pe-4">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in filteredPagamentos" :key="p.id">
              <td class="ps-4">
                <div class="cf-td-bold text-muted small">#{{ p.id }}</div>
              </td>
              <td>
                <div class="cf-avatar-row">
                  <div class="cf-icon-avatar shadow-sm" :class="p.tipo === 'FARMACIA' ? 'bg-primary-subtle text-primary' : 'bg-warning-subtle text-warning'">
                    <i :class="p.tipo === 'FARMACIA' ? 'fas fa-hospital' : 'fas fa-motorcycle'"></i>
                  </div>
                  <div>
                    <div class="cf-td-bold text-dark">{{ p.farmacia?.nome || p.motoboy?.nome }}</div>
                    <div class="text-muted extra-small fw-bold">{{ p.tipo === 'FARMACIA' ? 'Unidade Parceira' : 'Operador Logístico' }}</div>
                  </div>
                </div>
              </td>
              <td>
                <span class="cf-role-badge shadow-sm" :style="p.tipo === 'FARMACIA' ? 'background:#E8F5E9;color:#2E7D32;border:1px solid #C8E6C9' : 'background:#FFF8E1;color:#F57F17;border:1px solid #FFECB3'">
                  {{ p.tipo }}
                </span>
              </td>
              <td>
                <div class="text-dark small fw-bold"><i class="fas fa-calendar-day me-1 opacity-50"></i> {{ p.referenciaPeriodo }}</div>
                <div class="text-muted extra-small">Ciclo Mensal</div>
              </td>
              <td>
                <div class="text-success fw-bold">R$ {{ Number(p.valorLiquido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</div>
                <div class="text-muted extra-small">Taxas Deduzidas</div>
              </td>
              <td>
                <div class="cf-pix-pill shadow-sm" v-if="p.chavePix" @click="copyPix(p.chavePix)">
                  <span class="label">{{ p.tipoChavePix }}</span>
                  <span class="value">{{ p.chavePix }}</span>
                  <i class="fas fa-copy ms-1"></i>
                </div>
                <span v-else class="text-muted extra-small fw-bold italic opacity-50">PIX não informado</span>
              </td>
              <td>
                <span class="cf-status-badge shadow-sm" :class="statusClass(p.status)">
                  <i class="fas fa-circle me-1 small opacity-50"></i>
                  {{ p.status }}
                </span>
              </td>
              <td class="text-center pe-4">
                <div class="d-flex justify-content-center gap-2">
                  <button v-if="p.status === 'PENDENTE'" class="cf-btn-action success shadow-sm" @click="prepararPagamento(p)" title="Liquidar Repasse">
                    <i class="fas fa-money-bill-transfer me-1"></i> Liquidar
                  </button>
                  <button class="cf-icon-btn shadow-sm" @click="viewDetails(p)" title="Detalhes do Lançamento">
                    <i class="fas fa-receipt"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredPagamentos.length === 0">
              <td colspan="8" class="cf-empty py-5">
                <i class="fas fa-file-invoice-dollar fa-3x mb-3 opacity-10"></i>
                <p class="fw-bold text-muted">Nenhum lançamento financeiro localizado.</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Gerar Repasse -->
    <div v-if="showGenerateModal" class="cf-modal-overlay" @click.self="showGenerateModal = false">
      <div class="cf-modal-box animate__animated animate__fadeInUp dash-card" style="width: 520px">
        <div class="cf-modal-header border-bottom bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-primary-subtle text-primary shadow-sm">
              <i class="fas fa-hand-holding-dollar"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Gerar Novo Repasse</h5>
              <p class="mb-0 text-muted extra-small fw-bold text-uppercase letter-spacing-1">Configuração de Ciclo</p>
            </div>
          </div>
          <button class="btn-close-custom shadow-sm" @click="showGenerateModal = false"><i class="fas fa-times"></i></button>
        </div>
        <div class="cf-modal-body p-4 bg-white">
          <div class="row g-4">
            <div class="col-12">
              <label class="cf-label-premium">Categoria de Destinatário</label>
              <select v-model="gen.tipo" class="cf-input-premium">
                <option value="FARMACIA">Unidade Farmacêutica</option>
                <option value="MOTOBOY">Operador de Entrega</option>
              </select>
            </div>
            <div class="col-12">
              <label class="cf-label-premium">Selecionar Parceiro</label>
              <select v-model="gen.id" class="cf-input-premium">
                <option :value="null" disabled>Selecione um parceiro ativo...</option>
                <template v-if="gen.tipo === 'FARMACIA'">
                  <option v-for="f in farmacias" :key="f.id" :value="f.id">{{ f.nome }}</option>
                </template>
                <template v-else>
                  <option v-for="m in motoboys" :key="m.id" :value="m.id">{{ m.nome }}</option>
                </template>
              </select>
            </div>
            <div class="col-12">
              <label class="cf-label-premium">Mês de Referência</label>
              <input v-model="gen.periodo" type="month" class="cf-input-premium">
              <small class="text-muted extra-small mt-1 d-block"><i class="fas fa-info-circle me-1"></i>O sistema calculará automaticamente as vendas deste período.</small>
            </div>
          </div>
        </div>
        <div class="cf-modal-footer bg-light-subtle p-3 border-top">
          <button class="btn btn-outline-secondary fw-bold px-4 rounded-pill" @click="showGenerateModal = false">Cancelar</button>
          <button class="cf-btn-primary px-4 shadow-sm" @click="gerarPagamento" :disabled="isGenerating">
            <i v-if="isGenerating" class="spinner-border spinner-border-sm me-2"></i>
            Processar Lançamento
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Confirmar Pagamento -->
    <div v-if="pagando" class="cf-modal-overlay" @click.self="pagando = null">
      <div class="cf-modal-box animate__animated animate__fadeInUp dash-card" style="width: 480px">
        <div class="cf-modal-header border-bottom bg-success-subtle">
          <div class="d-flex align-items-center gap-3 text-success">
            <div class="modal-icon-wrap bg-white shadow-sm">
              <i class="fas fa-check-to-slot"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Liquidar Repasse</h5>
              <p class="mb-0 extra-small fw-bold text-uppercase letter-spacing-1">Confirmação de Baixa</p>
            </div>
          </div>
        </div>
        <div class="cf-modal-body p-4 text-center bg-white">
          <p class="text-muted mb-1">Confirmação de transferência enviada para:</p>
          <h5 class="fw-bold text-dark mb-3">{{ pagando.farmacia?.nome || pagando.motoboy?.nome }}</h5>
          
          <div class="cf-amount-display mb-4 shadow-sm">
            <div class="label text-muted">Montante a Liquidar</div>
            <div class="value text-success">R$ {{ Number(pagando.valorLiquido).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</div>
          </div>
          
          <div class="cf-pix-display-full mb-4 shadow-sm">
            <div class="label text-muted small fw-bold mb-1">DADOS DE DESTINO ({{ pagando.tipoChavePix }})</div>
            <div class="pix-value font-monospace fw-bold">{{ pagando.chavePix }}</div>
            <button class="btn btn-sm btn-link text-primary fw-bold text-decoration-none mt-1" @click="copyPix(pagando.chavePix)">
              <i class="fas fa-copy me-1"></i> Copiar Chave
            </button>
          </div>

          <div class="text-start">
            <label class="cf-label-premium">Referência / ID Transação</label>
            <textarea v-model="obs" class="cf-input-premium" rows="2" placeholder="Ex: Protocolo de transferência ou data..."></textarea>
          </div>
        </div>
        <div class="cf-modal-footer bg-light-subtle p-3 border-top">
          <button class="btn btn-outline-secondary fw-bold px-4 rounded-pill flex-grow-1" @click="pagando = null">Cancelar</button>
          <button class="btn btn-success fw-bold px-4 rounded-pill flex-grow-1 shadow-sm" @click="confirmarPagamento" :disabled="isPaying">
            Confirmar e Baixar
          </button>
        </div>
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

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

/* Tabs Premium */
.cf-tabs-wrapper { display: flex; background: #fff; padding: 6px; border-radius: 16px; border: 1px solid var(--cf-border); gap: 6px; width: fit-content; }
.cf-tab-premium { padding: 0.6rem 1.25rem; border-radius: 12px; border: none; background: transparent; color: var(--cf-text-muted); font-size: 0.82rem; font-weight: 700; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.cf-tab-premium:hover { background: var(--cf-ivory); color: var(--cf-text-dark); }
.cf-tab-premium.active { background: var(--cf-green); color: #fff; box-shadow: var(--cf-shadow-sm); }

.cf-table-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }

.cf-avatar-row { display: flex; align-items: center; gap: 0.85rem; }
.cf-icon-avatar { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; flex-shrink: 0; }

.cf-pix-pill { display: inline-flex; align-items: center; background: var(--cf-ivory); border-radius: 20px; border: 1px solid var(--cf-border); padding: 0.25rem 0.75rem; cursor: pointer; transition: all 0.2s; }
.cf-pix-pill:hover { border-color: var(--cf-gold); background: #fff; }
.cf-pix-pill .label { font-size: 0.62rem; font-weight: 800; color: var(--cf-gold); text-transform: uppercase; margin-right: 0.5rem; }
.cf-pix-pill .value { font-size: 0.75rem; font-weight: 600; color: var(--cf-text-dark); }

.cf-btn-action { border: none; border-radius: 10px; padding: 0.4rem 0.85rem; font-size: 0.75rem; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.cf-btn-action.success { background: var(--cf-green-xlight); color: var(--cf-green); border: 1px solid rgba(42,92,69,0.2); }
.cf-btn-action.success:hover { background: var(--cf-green); color: #fff; }

.cf-amount-display { background: var(--cf-ivory); border-radius: 16px; padding: 1.25rem; text-align: center; border: 1px dashed var(--cf-border-mid); }
.cf-amount-display .label { font-size: 0.68rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.25rem; }
.cf-amount-display .value { font-size: 1.8rem; font-weight: 800; }

.cf-pix-display-full { background: #f8fafc; border-radius: 16px; padding: 1.25rem; border: 1px solid #e2e8f0; }

.cf-status-badge { font-size: 0.62rem; font-weight: 800; padding: 0.35rem 0.85rem; border-radius: 20px; text-transform: uppercase; display: inline-flex; align-items: center; }
.s-pending { background: #FFF8EC; color: #9A6700; border: 1px solid #FFECB3; }
.s-paid { background: #E8F5E9; color: #2E7D32; border: 1px solid #C8E6C9; }
.s-cancelled { background: #F9EDED; color: var(--cf-danger); border: 1px solid #F8D7DA; }

.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
