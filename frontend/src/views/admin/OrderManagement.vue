<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER OPERACIONAL ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Monitoramento de Fluxo</h3>
        <p class="editorial-subtitle">Gestão centralizada de logística e transações da rede</p>
      </div>
      <div class="header-tools">
        <div class="cf-input-group">
          <i class="fas fa-search"></i>
          <input v-model="search" type="text" placeholder="Localizar pedido ou cliente...">
        </div>
        <button class="btn-refresh" @click="fetchOrders" :disabled="isLoading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
        </button>
      </div>
    </header>

    <!-- ═══ GRID DE PEDIDOS ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Sincronizando base de dados...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Identificador</th>
              <th>Comprador</th>
              <th>Operação</th>
              <th>Montante</th>
              <th>Status do Fluxo</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id" class="row-hover">
              <td class="ps-5">
                <div class="id-badge">#{{ order.codigoPedido || order.id }}</div>
              </td>
              <td>
                <div class="entity-cell">
                  <div class="entity-avatar">
                    {{ (order.nomeCliente || order.usuario?.nome || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div class="entity-meta">
                    <span class="entity-name">{{ order.nomeCliente || order.usuario?.nome || 'Consumidor' }}</span>
                    <span class="entity-sub">{{ order.usuario?.email || 'ID: ' + order.usuarioId }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="date-cell">
                  <span class="date-val">{{ formatDate(order.dataPedido).split(',')[0] }}</span>
                  <span class="time-val">{{ formatDate(order.dataPedido).split(',')[1] }}</span>
                </div>
              </td>
              <td>
                <div class="price-cell">
                  <span class="currency">R$</span>
                  <span class="amount">{{ Number(order.valorTotal || 0).toFixed(2) }}</span>
                </div>
              </td>
              <td>
                <div class="status-pill" :class="statusClass(order.status)">
                  <span class="pill-dot"></span>
                  <span class="pill-text">{{ order.status?.replace(/_/g, ' ') }}</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <button class="action-btn-view" @click="viewDetails(order)">
                  <i class="fas fa-file-invoice"></i>
                  <span>Dossiê</span>
                </button>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="6" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-box-open"></i>
                  <p>Nenhum registro encontrado no critério de busca.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: DOSSIÊ DO PEDIDO ═══ -->
    <div v-if="selectedOrder" class="modal-premium-overlay" @click.self="selectedOrder = null">
      <div class="dossier-modal animate__animated animate__fadeInUp">
        <header class="dossier-header">
          <div class="header-brand">
            <div class="dossier-icon"><i class="fas fa-clipboard-check"></i></div>
            <div class="dossier-meta">
              <h5>Dossiê Operacional</h5>
              <span class="dossier-id">CÓDIGO: #{{ selectedOrder.codigoPedido || selectedOrder.id }}</span>
            </div>
          </div>
          <button class="close-modal" @click="selectedOrder = null"><i class="fas fa-times"></i></button>
        </header>

        <div class="dossier-content cf-hide-scrollbar">
          <div class="dossier-grid">
            <!-- Coluna: Informações -->
            <div class="dossier-col">
              <h6 class="dossier-section-label">Origem do Pedido</h6>
              <div class="dossier-card-premium ivory">
                <div class="entity-card-lg">
                  <div class="lg-avatar">
                    {{ (selectedOrder.nomeCliente || selectedOrder.usuario?.nome || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div class="lg-meta">
                    <span class="lg-name">{{ selectedOrder.nomeCliente || selectedOrder.usuario?.nome }}</span>
                    <span class="lg-sub">{{ selectedOrder.usuario?.email || 'Cliente não autenticado' }}</span>
                  </div>
                </div>
                <div class="dossier-data-line">
                  <span class="data-label">Transação em</span>
                  <span class="data-val">{{ formatDate(selectedOrder.dataPedido) }}</span>
                </div>
              </div>

              <h6 class="dossier-section-label mt-4">Gestão de Fluxo</h6>
              <div class="dossier-card-premium gold">
                <div class="status-selector-wrap">
                  <div class="selector-current">
                    <span class="label">Status Atual:</span>
                    <div class="status-pill small" :class="statusClass(selectedOrder.status)">
                      {{ selectedOrder.status?.replace(/_/g, ' ') }}
                    </div>
                  </div>
                  <div class="selector-action">
                    <select v-model="newStatus" class="cf-select-modern">
                      <option v-for="s in statusOptions" :key="s" :value="s">{{ s.replace(/_/g, ' ') }}</option>
                    </select>
                    <button class="btn-update-status" @click="updateStatus" :disabled="isUpdating">
                      <i v-if="isUpdating" class="fas fa-circle-notch fa-spin"></i>
                      <span v-else>Atualizar</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Coluna: Itens -->
            <div class="dossier-col">
              <h6 class="dossier-section-label">Composição do Carrinho</h6>
              <div class="dossier-items-container">
                <div v-for="it in selectedOrder.itens" :key="it.id" class="dossier-item-row">
                  <div class="item-info">
                    <span class="item-name">{{ it.nomeProduto || it.produto?.nome }}</span>
                    <span class="item-sub">{{ it.produto?.dosagem || 'Concentração não informada' }}</span>
                  </div>
                  <div class="item-math">
                    <span class="item-qty">{{ it.quantidade }}x</span>
                    <span class="item-subtotal">R$ {{ Number((it.precoUnitario * it.quantidade) || 0).toFixed(2) }}</span>
                  </div>
                </div>
                
                <div class="dossier-total-box">
                  <div class="total-row">
                    <span class="label">Total Bruto</span>
                    <span class="val">R$ {{ Number(selectedOrder.valorTotal || 0).toFixed(2) }}</span>
                  </div>
                  <div class="total-row primary">
                    <span class="label">Investimento Total</span>
                    <span class="val-lg">R$ {{ Number(selectedOrder.valorTotal || 0).toFixed(2) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <footer class="dossier-footer">
          <button class="btn-print-dossier"><i class="fas fa-print me-2"></i>Imprimir Comprovante</button>
          <button class="btn-close-dossier" @click="selectedOrder = null">Concluir Revisão</button>
        </footer>
      </div>
    </div>
  </div>
</template>


<script>
import pedidoService from '@/services/pedidoService';

export default {
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      isLoading: true,
      search: '',
      selectedOrder: null,
      newStatus: '',
      isUpdating: false,
      statusOptions: [
        'AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_PREPARACAO', 
        'ENVIADO', 'EM_TRANSITO', 'ENTREGUE', 'CANCELADO'
      ]
    };
  },
  computed: {
    filteredOrders() {
      if (!this.search) return this.orders;
      const t = this.search.toLowerCase();
      return this.orders.filter(o => 
        o.id.toString().includes(t) || 
        o.codigoPedido?.toLowerCase().includes(t) ||
        (o.nomeCliente || o.usuario?.nome)?.toLowerCase().includes(t)
      );
    }
  },
  mounted() {
    this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      this.isLoading = true;
      try {
        const { data } = await pedidoService.listarTodos();
        this.orders = data;
      } catch (err) {
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    formatDate(d) {
      if (!d) return '—';
      return new Date(d).toLocaleString('pt-BR');
    },
    statusClass(s) {
      return { 
        AGUARDANDO_PAGAMENTO:'s-pending', PAGO:'s-paid', 
        EM_PREPARACAO:'s-prep', ENVIADO:'s-sent', 
        EM_TRANSITO:'s-transit', ENTREGUE:'s-done', 
        CANCELADO:'s-cancelled' 
      }[s] || '';
    },
    viewDetails(order) {
      this.selectedOrder = order;
      this.newStatus = order.status;
    },
    async updateStatus() {
      if (!this.selectedOrder) return;
      this.isUpdating = true;
      try {
        await pedidoService.atualizarStatus(this.selectedOrder.id, this.newStatus);
        this.selectedOrder.status = this.newStatus;
        await this.fetchOrders();
        alert('Status operacional atualizado com sucesso!');
      } catch (err) {
        alert('Erro ao sincronizar status.');
      } finally {
        this.isUpdating = false;
      }
    }
  }
};
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

/* Custom Styling */
.cf-table-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }

.cf-user-initials { width: 36px; height: 36px; border-radius: 12px; background: var(--cf-ivory); color: var(--cf-gold); display: flex; align-items: center; justify-content: center; font-size: 0.9rem; font-weight: 800; flex-shrink: 0; box-shadow: inset 0 0 0 1px rgba(184,149,80,0.1); }
.cf-user-initials.lg { width: 54px; height: 54px; font-size: 1.4rem; }

.cf-status-badge { font-size: 0.62rem; font-weight: 800; padding: 0.35rem 0.8rem; border-radius: 20px; text-transform: uppercase; letter-spacing: 0.1em; display: inline-flex; align-items: center; }
.s-pending   { background: #FFF8EC; color: #9A6700; }
.s-paid      { background: var(--cf-green-xlight); color: var(--cf-green); }
.s-prep      { background: #FFF0E6; color: #9A4500; }
.s-sent      { background: #EAF1FB; color: #2a6099; }
.s-transit   { background: #F0EAFB; color: #5a2a99; }
.s-done      { background: var(--cf-green-xlight); color: var(--cf-green-dark); box-shadow: inset 0 0 0 1px rgba(42,92,69,0.1); }
.s-cancelled { background: #fef2f2; color: #dc2626; }

.cf-detail-card { background: var(--cf-ivory); border: 1px solid var(--cf-border-mid); border-radius: 18px; }
.cf-status-update-box { display: flex; gap: 0.5rem; background: #fff; padding: 0.4rem; border-radius: 14px; border: 1px solid var(--cf-border); }

.cf-select-premium { border: none; outline: none; background: transparent; font-size: 0.85rem; font-weight: 700; color: var(--cf-text-dark); padding: 0 0.5rem; cursor: pointer; }

.total-badge { background: var(--cf-ivory); padding: 1.25rem 2rem; border-radius: 20px; border: 1px dashed var(--cf-green-mid); }

/* Layout Utils */
.cf-avatar-row { display: flex; align-items: center; gap: 0.85rem; }
.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>