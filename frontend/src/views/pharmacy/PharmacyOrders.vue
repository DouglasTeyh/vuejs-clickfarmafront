<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE LOGÍSTICA ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Fluxo de Pedidos</h3>
        <p class="editorial-subtitle">Monitoramento de vendas e controle de despacho em tempo real</p>
      </div>
      <div class="header-tools">
        <button class="btn-sync-circle" @click="carregar" :disabled="isLoading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
        </button>
      </div>
    </header>

    <!-- ═══ FILTROS DE STATUS PREMIUM ═══ -->
    <nav class="editorial-status-tabs shadow-sm mb-5">
      <button v-for="aba in abas" :key="aba.status"
        class="status-tab-btn" :class="{ active: abaAtiva === aba.status }"
        @click="abaAtiva = aba.status">
        <div class="tab-icon-wrap"><i :class="aba.icon"></i></div>
        <div class="tab-content">
          <span class="tab-label">{{ aba.label }}</span>
          <span v-if="contarStatus(aba.status) > 0" class="tab-badge">{{ contarStatus(aba.status) }}</span>
        </div>
      </button>
    </nav>

    <!-- ═══ GRID DE DESPACHO ═══ -->
    <div v-if="isLoading" class="loading-overlay-premium">
      <div class="cf-spinner"></div>
      <span>Atualizando fluxo logístico...</span>
    </div>

    <div v-else-if="!pedidosFiltrados.length" class="empty-state-premium">
      <div class="empty-icon"><i class="fas fa-box-open"></i></div>
      <h4>Nenhuma remessa encontrada</h4>
      <p>Você não possui pedidos com o status "{{ abas.find(a => a.status === abaAtiva)?.label }}" no momento.</p>
    </div>

    <div v-else class="orders-grid-premium">
      <div v-for="p in pedidosFiltrados" :key="p.id" class="order-card-premium">
        <!-- Header do Card -->
        <header class="order-card-header">
          <div class="header-main">
            <span class="order-serial">#{{ p.codigoPedido || p.id }}</span>
            <div class="order-status-pill" :class="statusClass(p.status)">
              <span class="pill-dot"></span>
              <span class="pill-text">{{ p.status?.replace(/_/g,' ') }}</span>
            </div>
          </div>
          <span class="order-timestamp">{{ dt(p.dataPedido) }}</span>
        </header>

        <!-- Corpo do Card -->
        <div class="order-card-body">
          <div class="client-section mb-4">
            <label class="editorial-label">Destinatário</label>
            <div class="client-info">
              <div class="client-avatar">{{ (p.nomeCliente || p.usuario?.nome || '?').charAt(0) }}</div>
              <div class="client-meta">
                <span class="client-name">{{ p.nomeCliente || p.usuario?.nome || 'Consumidor Final' }}</span>
                <span v-if="p.enderecoEntrega" class="client-addr">
                  <i class="fas fa-location-dot"></i> {{ p.enderecoEntrega }}
                </span>
                <span v-if="(p.status === 'EM_PREPARACAO' || p.status === 'ENVIADO') && p.codigoRetirada" class="client-addr text-primary fw-bold mt-2">
                  <i class="fas fa-key"></i> Código Retirada (Motoboy): {{ p.codigoRetirada }}
                </span>
              </div>
            </div>
          </div>

          <div class="items-section">
            <label class="editorial-label">Composição do Pedido</label>
            <div class="items-dossier ivory">
              <div v-for="it in p.itens" :key="it.id" class="dossier-row">
                <span class="it-qty">{{ it.quantidade }}×</span>
                <span class="it-name">{{ it.nomeProduto || it.produto?.nome }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer do Card -->
        <footer class="order-card-footer">
          <div class="total-box">
            <span class="total-label">Montante Total</span>
            <div class="total-val">
              <span class="currency">R$</span>
              <span class="amount">{{ Number(p.valorTotal || 0).toFixed(2) }}</span>
            </div>
          </div>

          <div class="action-box">
            <template v-if="p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO'">
              <button class="btn-order-action primary" @click="mudarStatus(p.id, 'EM_PREPARACAO')">
                <i class="fas fa-vial-circle-check"></i> Iniciar Preparo
              </button>
            </template>
            <template v-else-if="p.status === 'EM_PREPARACAO'">
              <button class="btn-order-action gold" @click="mudarStatus(p.id, 'ENVIADO')">
                <i class="fas fa-box-check"></i> Pronto p/ Retirada
              </button>
            </template>
            <template v-else-if="p.status === 'ENVIADO'">
               <span class="status-summary text-warning fw-bold"><i class="fas fa-motorcycle"></i> Aguardando Motoboy</span>
            </template>
            <template v-else-if="p.status === 'ENTREGUE'">
              <div class="finalized-indicator">
                <i class="fas fa-circle-check"></i> Concluído
              </div>
            </template>
            <template v-else>
               <span class="status-summary">{{ p.status }}</span>
            </template>
          </div>
        </footer>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';
import pedidoService from '@/services/pedidoService';

export default {
  name: 'PharmacyOrders',
  data() {
    return {
      pedidos: [], isLoading: true, abaAtiva: 'AGUARDANDO_PAGAMENTO',
      farmaciaId: null, pollingInterval: null, lastCount: 0,
      abas: [
        { status: 'AGUARDANDO_PAGAMENTO', label: 'Pendentes', icon: 'fas fa-clock' },
        { status: 'PAGO',                 label: 'Pagos',      icon: 'fas fa-check-circle' },
        { status: 'EM_PREPARACAO',        label: 'Em Preparo', icon: 'fas fa-fire' },
        { status: 'ENVIADO',              label: 'Enviados',   icon: 'fas fa-motorcycle' },
        { status: 'ENTREGUE',             label: 'Entregues',  icon: 'fas fa-check-double' }
      ]
    };
  },
  computed: {
    pedidosFiltrados() { return this.pedidos.filter(p => p.status === this.abaAtiva); },
    pedidosPendentes() { return this.pedidos.filter(p => p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO').length; }
  },
  async mounted() {
    await this.carregarFarmacia();
    await this.carregar();
    this.pollingInterval = setInterval(() => this.carregarSilencioso(), 30000);
  },
  beforeUnmount() { clearInterval(this.pollingInterval); },
  methods: {
    async carregarFarmacia() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data } = await api.get('/farmacias');
        const f = data.find(x => x.email === user.email);
        if (f) this.farmaciaId = f.id;
      } catch {}
    },
    async carregar() {
      if (!this.farmaciaId) return;
      this.isLoading = true;
      try {
        const { data } = await pedidoService.listarPorFarmacia(this.farmaciaId);
        this.pedidos = data; this.lastCount = this.pedidosPendentes;
      } catch (e) { console.error(e); }
      finally { this.isLoading = false; }
    },
    async carregarSilencioso() {
      if (!this.farmaciaId) return;
      try {
        const { data } = await pedidoService.listarPorFarmacia(this.farmaciaId);
        const novo = data.filter(p => p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO').length;
        if (novo > this.lastCount) { this.tocarSom(); this.notificar(novo - this.lastCount); }
        this.pedidos = data; this.lastCount = novo;
      } catch {}
    },
    tocarSom() {
      try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        const o = ctx.createOscillator(); const g = ctx.createGain();
        o.connect(g); g.connect(ctx.destination);
        o.frequency.value = 880; g.gain.setValueAtTime(0.25, ctx.currentTime);
        g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);
        o.start(ctx.currentTime); o.stop(ctx.currentTime + 0.4);
      } catch {}
    },
    notificar(n) {
      if ('Notification' in window && Notification.permission === 'granted')
        new Notification('ClickFarma', { body: `${n} novo(s) pedido(s)!` });
    },
    async mudarStatus(id, status) {
      try { await pedidoService.atualizarStatus(id, status); await this.carregar(); }
      catch { alert('Erro ao atualizar status.'); }
    },
    contarStatus(s) { return this.pedidos.filter(p => p.status === s).length; },
    statusClass(s) {
      return { AGUARDANDO_PAGAMENTO:'s-pending', PAGO:'s-paid', EM_PREPARACAO:'s-prep', ENVIADO:'s-sent', EM_TRANSITO:'s-transit', ENTREGUE:'s-done', CANCELADO:'s-cancelled' }[s] || '';
    },
    dt(d) { if (!d) return '—'; return new Date(d).toLocaleString('pt-BR',{day:'2-digit',month:'2-digit',hour:'2-digit',minute:'2-digit'}); }
  }
};
</script>

<style scoped>
.cf-mgmt-premium { animation: fadeIn 0.6s var(--cf-ease); }

/* Header */
.mgmt-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.btn-sync-circle { width: 44px; height: 44px; border-radius: 50%; background: var(--cf-ivory); border: 1px solid var(--cf-border); color: var(--cf-gold); cursor: pointer; transition: all 0.3s; display: flex; align-items: center; justify-content: center; }
.btn-sync-circle:hover { background: var(--cf-gold-light); border-color: var(--cf-gold); }

/* Status Tabs */
.editorial-status-tabs { background: #fff; border: 1px solid var(--cf-border); border-radius: 20px; display: flex; padding: 0.5rem; gap: 0.5rem; overflow-x: auto; }
.status-tab-btn { background: transparent; border: none; padding: 0.75rem 1.5rem; border-radius: 14px; display: flex; align-items: center; gap: 1rem; cursor: pointer; transition: all 0.3s; min-width: 180px; }
.status-tab-btn:hover { background: var(--cf-ivory); }
.status-tab-btn.active { background: var(--cf-text-dark); color: #fff; }

.tab-icon-wrap { width: 32px; height: 32px; border-radius: 8px; background: var(--cf-ivory-light); display: flex; align-items: center; justify-content: center; font-size: 0.9rem; color: var(--cf-text-muted); }
.status-tab-btn.active .tab-icon-wrap { background: rgba(255,255,255,0.1); color: #fff; }

.tab-content { display: flex; flex-direction: column; text-align: left; }
.tab-label { font-size: 0.8rem; font-weight: 700; }
.tab-badge { font-size: 0.6rem; font-weight: 800; background: var(--cf-gold); color: #fff; padding: 1px 6px; border-radius: 6px; width: fit-content; margin-top: 2px; }

/* Grid and Cards */
.orders-grid-premium { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 2rem; }
.order-card-premium { background: #fff; border-radius: 28px; border: 1px solid var(--cf-border); display: flex; flex-direction: column; transition: all 0.3s; overflow: hidden; }
.order-card-premium:hover { transform: translateY(-5px); box-shadow: var(--cf-shadow-md); border-color: var(--cf-border-mid); }

.order-card-header { padding: 1.25rem 2rem; border-bottom: 1px solid rgba(0,0,0,0.03); display: flex; justify-content: space-between; align-items: center; background: var(--cf-ivory-light); }
.header-main { display: flex; align-items: center; gap: 1rem; }
.order-serial { font-family: var(--cf-sans); font-weight: 800; font-size: 1rem; color: var(--cf-text-dark); }
.order-timestamp { font-size: 0.65rem; font-weight: 700; color: var(--cf-text-faint); text-transform: uppercase; }

.order-status-pill { display: flex; align-items: center; gap: 0.5rem; padding: 0.25rem 0.75rem; border-radius: 50px; font-size: 0.65rem; font-weight: 800; text-transform: uppercase; }
.order-status-pill.s-pending { background: #fffbeb; color: #b45309; }
.order-status-pill.s-pending .pill-dot { background: #f59e0b; }
.order-status-pill.s-paid { background: var(--cf-green-xlight); color: var(--cf-green); }
.order-status-pill.s-paid .pill-dot { background: #22c55e; }
.order-status-pill.s-prep { background: #fef2f2; color: #b91c1c; }
.order-status-pill.s-prep .pill-dot { background: #ef4444; }
.pill-dot { width: 6px; height: 6px; border-radius: 50%; }

.order-card-body { padding: 2rem; flex: 1; }
.editorial-label { display: block; font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.75rem; }

.client-info { display: flex; align-items: center; gap: 1rem; }
.client-avatar { width: 44px; height: 44px; border-radius: 12px; background: var(--cf-ivory); color: var(--cf-text-dark); display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 1.2rem; border: 1px solid var(--cf-border); }
.client-meta { display: flex; flex-direction: column; }
.client-name { font-size: 1.1rem; font-weight: 700; color: var(--cf-text-dark); }
.client-addr { font-size: 0.75rem; color: var(--cf-text-muted); font-weight: 600; margin-top: 2px; }

.items-dossier { padding: 1.25rem; border-radius: 16px; border: 1px solid var(--cf-border); display: flex; flex-direction: column; gap: 0.5rem; }
.items-dossier.ivory { background: var(--cf-ivory-light); }
.dossier-row { display: flex; gap: 0.75rem; font-size: 0.85rem; font-weight: 600; color: var(--cf-text-dark); }
.it-qty { color: var(--cf-gold); font-weight: 800; }

.order-card-footer { padding: 1.5rem 2rem; border-top: 1px solid var(--cf-border); background: var(--cf-ivory-light); display: flex; justify-content: space-between; align-items: center; }
.total-box { display: flex; flex-direction: column; }
.total-label { font-size: 0.6rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; }
.total-val { display: flex; align-items: baseline; gap: 0.2rem; }
.total-val .currency { font-size: 0.75rem; font-weight: 800; color: var(--cf-green); }
.total-val .amount { font-size: 1.4rem; font-weight: 800; color: var(--cf-text-dark); }

.btn-order-action { border: none; padding: 0.75rem 1.5rem; border-radius: 50px; font-weight: 700; font-size: 0.8rem; display: flex; align-items: center; gap: 0.6rem; cursor: pointer; transition: all 0.3s; }
.btn-order-action.primary { background: var(--cf-green); color: #fff; }
.btn-order-action.primary:hover { background: var(--cf-green-dark); transform: translateY(-2px); box-shadow: 0 8px 20px rgba(42,92,69,0.2); }
.btn-order-action.gold { background: var(--cf-gold); color: #fff; }
.btn-order-action.gold:hover { background: var(--cf-gold-dark); transform: translateY(-2px); box-shadow: 0 8px 20px rgba(184,149,80,0.2); }

.finalized-indicator { color: var(--cf-green); font-weight: 800; font-size: 0.85rem; display: flex; align-items: center; gap: 0.5rem; }

.empty-state-premium { padding: 8rem 0; text-align: center; color: var(--cf-text-faint); }
.empty-icon { font-size: 4rem; margin-bottom: 1.5rem; opacity: 0.2; }
.empty-state-premium h4 { font-family: var(--cf-serif); font-size: 1.5rem; color: var(--cf-text-dark); margin-bottom: 0.5rem; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>
