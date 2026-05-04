<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE BOAS-VINDAS ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Painel Executivo</h3>
        <p class="editorial-subtitle">Visão analítica de performance e inteligência logística</p>
      </div>
      <div class="header-tools">
        <div class="status-indicator">
          <span class="status-dot online"></span>
          <span class="status-text">Unidade Operacional</span>
        </div>
      </div>
    </header>

    <!-- ═══ KPI GRID PREMIUM ═══ -->
    <div class="cf-kpi-grid-premium mb-5">
      <div class="kpi-card-premium ivory">
        <div class="kpi-icon"><i class="fas fa-coins"></i></div>
        <div class="kpi-data">
          <span class="kpi-label">Faturamento Bruto</span>
          <div class="kpi-value-wrap">
            <span class="currency">R$</span>
            <span class="amount">{{ n(stats.faturamentoBruto) }}</span>
          </div>
        </div>
        <div class="kpi-visual">
          <div class="mini-chart-bar" style="height: 40%"></div>
          <div class="mini-chart-bar" style="height: 60%"></div>
          <div class="mini-chart-bar" style="height: 80%"></div>
        </div>
      </div>

      <div class="kpi-card-premium gold">
        <div class="kpi-icon"><i class="fas fa-vault"></i></div>
        <div class="kpi-data">
          <span class="kpi-label">Margem Líquida</span>
          <div class="kpi-value-wrap">
            <span class="currency">R$</span>
            <span class="amount">{{ n(stats.faturamentoLiquido) }}</span>
          </div>
          <span class="kpi-sub">Comissão: R$ {{ n(stats.comissaoPaga) }}</span>
        </div>
      </div>

      <div class="kpi-card-premium dark">
        <div class="kpi-icon"><i class="fas fa-box-archive"></i></div>
        <div class="kpi-data">
          <span class="kpi-label">Volume de Pedidos</span>
          <div class="kpi-value-wrap">
            <span class="amount">{{ stats.totalPedidos }}</span>
          </div>
          <span class="kpi-sub">Total acumulado</span>
        </div>
      </div>

      <div class="kpi-card-premium" :class="stats.pedidosPendentes > 0 ? 'alert' : 'neutral'">
        <div class="kpi-icon"><i class="fas fa-clock"></i></div>
        <div class="kpi-data">
          <span class="kpi-label">Ações Pendentes</span>
          <div class="kpi-value-wrap">
            <span class="amount">{{ stats.pedidosPendentes }}</span>
          </div>
          <span v-if="stats.pedidosPendentes > 0" class="kpi-sub">Requer atenção imediata</span>
          <span v-else class="kpi-sub">Operação em dia</span>
        </div>
        <div v-if="stats.pedidosPendentes > 0" class="pulse-ring"></div>
      </div>
    </div>

    <!-- ═══ ANALYTICS E ALERTAS ═══ -->
    <div class="dashboard-analytics-row">
      <!-- Performance Semanal -->
      <div class="analytics-card flex-2">
        <header class="card-header-premium">
          <div class="header-main">
            <i class="fas fa-chart-area"></i>
            <h5>Projeção de Receita</h5>
          </div>
          <span class="time-range">Últimos 7 dias</span>
        </header>
        <div class="chart-container-premium">
          <canvas id="lineChart"></canvas>
        </div>
      </div>

      <!-- Central de Despacho -->
      <div class="analytics-card flex-1">
        <header class="card-header-premium">
          <div class="header-main">
            <i class="fas fa-satellite-dish"></i>
            <h5>Fila de Despacho</h5>
          </div>
        </header>
        <div class="dispatch-center">
          <template v-if="stats.pedidosPendentes > 0">
            <div class="dispatch-status-alert">
              <div class="alert-icon-wrap">
                <i class="fas fa-truck-ramp-box"></i>
                <div class="alert-pulse"></div>
              </div>
              <h4>{{ stats.pedidosPendentes }} Pedidos aguardando</h4>
              <p>O tempo médio de preparo impacta seu ranqueamento na plataforma.</p>
              <router-link to="/pharmacy/orders" class="btn-dispatch-premium">
                Acessar Logística <i class="fas fa-chevron-right"></i>
              </router-link>
            </div>
          </template>
          <template v-else>
            <div class="dispatch-status-clear">
              <div class="clear-icon"><i class="fas fa-check-double"></i></div>
              <h4>Fluxo Normalizado</h4>
              <p>Nenhuma pendência logística detectada para sua unidade agora.</p>
              <router-link to="/pharmacy/orders" class="btn-history-premium">Ver Histórico</router-link>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- ═══ MODAL PIX OBRIGATÓRIO ═══ -->
    <div v-if="showPixModal" class="pix-modal-overlay">
      <div class="pix-modal-card">
        <div class="pix-modal-header text-center mb-4">
          <i class="fas fa-money-bill-transfer text-success fa-3x mb-3"></i>
          <h4 class="fw-bold">Cadastre sua Chave PIX</h4>
          <p class="text-muted small">Para receber os repasses das suas vendas, precisamos que você cadastre uma chave PIX válida. Você só verá esta mensagem até configurar.</p>
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold small">Tipo de Chave</label>
          <select v-model="pixForm.tipo" class="form-select">
            <option value="CNPJ">CNPJ</option>
            <option value="CPF">CPF</option>
            <option value="EMAIL">E-mail</option>
            <option value="TELEFONE">Telefone</option>
            <option value="ALEATORIA">Chave Aleatória</option>
          </select>
        </div>
        <div class="mb-4">
          <label class="form-label fw-bold small">Chave PIX</label>
          <input type="text" v-model="pixForm.chave" class="form-control" placeholder="Digite sua chave...">
        </div>
        <button class="btn btn-success w-100 fw-bold py-3" @click="salvarPix" :disabled="savingPix || !pixForm.chave">
          <span v-if="savingPix" class="spinner-border spinner-border-sm me-2"></span>
          Salvar Chave PIX
        </button>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';

export default {
  name: 'PharmacyDashboard',
  data() {
    return {
      stats: { faturamentoBruto:0, faturamentoLiquido:0, comissaoPaga:0, totalPedidos:0, pedidosPendentes:0, totalProdutos:0 },
      lineChart: null,
      showPixModal: false,
      savingPix: false,
      farmacia: null,
      pixForm: { tipo: 'CNPJ', chave: '' }
    };
  },
  async mounted() {
    await this.carregar();
  },
  methods: {
    n(v) { return Number(v || 0).toFixed(2); },
    async carregar() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data: farmacias } = await api.get('/farmacias');
        const farmacia = farmacias.find(f => f.email === user.email);
        if (!farmacia) return;
        this.farmacia = farmacia;

        if (!farmacia.chavePix || farmacia.chavePix === '') {
          this.showPixModal = true;
        }

        const { data: s } = await api.get(`/dashboard/farmacia/${farmacia.id}`);
        this.stats = s;
        await this.carregarGrafico(farmacia.id);
      } catch (e) { console.error(e); }
    },
    async carregarGrafico(id) {
      await this.$nextTick();
      const Chart = window.Chart;
      if (!Chart) return;
      try {
        const { data } = await api.get(`/dashboard/farmacia/${id}/vendas-semana`);
        const ctx = document.getElementById('lineChart');
        if (!ctx) return;
        if (this.lineChart) this.lineChart.destroy();
        this.lineChart = new Chart(ctx.getContext('2d'), {
          type: 'line',
          data: {
            labels: data.labels || [],
            datasets: [{
              label: 'Vendas (R$)',
              data: data.valores || [],
              borderColor: '#2A5C45',
              backgroundColor: 'rgba(42,92,69,0.07)',
              tension: 0.4, fill: true,
              pointBackgroundColor: '#2A5C45',
              pointRadius: 4, pointHoverRadius: 6,
              borderWidth: 3
            }]
          },
          options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              x: { grid: { display: false }, ticks: { font: { family: "'DM Sans', sans-serif", size: 11 } } },
              y: { beginAtZero: true, grid: { color: 'rgba(28,28,26,0.05)' },
                   ticks: { callback: v => `R$${v}`, font: { family: "'DM Sans', sans-serif", size: 10 } } }
            }
          }
        });
      } catch (e) { console.error(e); }
    },
    async salvarPix() {
      this.savingPix = true;
      try {
        await api.patch(`/farmacias/${this.farmacia.id}/pix`, {
          chavePix: this.pixForm.chave,
          tipoChavePix: this.pixForm.tipo
        });
        this.showPixModal = false;
        alert('Chave PIX cadastrada com sucesso!');
      } catch (e) {
        alert('Erro ao salvar Chave PIX.');
      } finally {
        this.savingPix = false;
      }
    }
  }
};
</script>

<style scoped>
.cf-mgmt-premium { animation: fadeIn 0.6s var(--cf-ease); }

/* Header */
.mgmt-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.status-indicator { display: flex; align-items: center; gap: 0.6rem; background: #fff; padding: 0.5rem 1rem; border-radius: 50px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.online { background: #22c55e; box-shadow: 0 0 10px rgba(34,197,94,0.4); }
.status-text { font-size: 0.75rem; font-weight: 800; color: var(--cf-text-dark); text-transform: uppercase; }

/* KPI Grid */
.cf-kpi-grid-premium { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; }
.kpi-card-premium { background: #fff; border-radius: 24px; padding: 1.5rem; border: 1px solid var(--cf-border); display: flex; flex-direction: column; gap: 1rem; position: relative; overflow: hidden; transition: all 0.3s; }
.kpi-card-premium:hover { transform: translateY(-5px); box-shadow: var(--cf-shadow-md); border-color: var(--cf-border-mid); }

.kpi-icon { width: 44px; height: 44px; border-radius: 12px; background: var(--cf-ivory-light); color: var(--cf-text-muted); display: flex; align-items: center; justify-content: center; font-size: 1.1rem; }
.kpi-label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.08em; }
.kpi-value-wrap { display: flex; align-items: baseline; gap: 0.25rem; }
.kpi-value-wrap .currency { font-size: 0.8rem; font-weight: 800; color: var(--cf-green); }
.kpi-value-wrap .amount { font-size: 1.6rem; font-weight: 800; color: var(--cf-text-dark); letter-spacing: -0.02em; }
.kpi-sub { font-size: 0.65rem; font-weight: 700; color: var(--cf-text-faint); }

.kpi-card-premium.ivory { background: var(--cf-ivory); }
.kpi-card-premium.gold .kpi-icon { background: var(--cf-gold-light); color: var(--cf-gold); }
.kpi-card-premium.dark { background: var(--cf-text-dark); color: #fff; border: none; }
.kpi-card-premium.dark .kpi-label { color: rgba(255,255,255,0.6); }
.kpi-card-premium.dark .amount { color: #fff; }
.kpi-card-premium.dark .kpi-icon { background: rgba(255,255,255,0.1); color: #fff; }

.kpi-card-premium.alert { border-color: #fecaca; background: #fff1f2; }
.kpi-card-premium.alert .kpi-icon { background: #fee2e2; color: #ef4444; }
.kpi-card-premium.alert .amount { color: #b91c1c; }

.kpi-visual { display: flex; align-items: flex-end; gap: 4px; height: 30px; position: absolute; right: 1.5rem; bottom: 1.5rem; }
.mini-chart-bar { width: 4px; border-radius: 2px; background: var(--cf-green); opacity: 0.2; }

/* Pulse Ring */
.pulse-ring { position: absolute; top: 1rem; right: 1rem; width: 8px; height: 8px; border-radius: 50%; background: #ef4444; box-shadow: 0 0 0 rgba(239,68,68,0.4); animation: pulse-red 2s infinite; }
@keyframes pulse-red { 0% { box-shadow: 0 0 0 0 rgba(239,68,68,0.7); } 70% { box-shadow: 0 0 0 10px rgba(239,68,68,0); } 100% { box-shadow: 0 0 0 0 rgba(239,68,68,0); } }

/* Analytics Grid */
.dashboard-analytics-row { display: flex; gap: 2rem; }
.analytics-card { background: #fff; border-radius: 32px; border: 1px solid var(--cf-border); display: flex; flex-direction: column; overflow: hidden; }
.flex-2 { flex: 2; }
.flex-1 { flex: 1; }

.card-header-premium { padding: 1.5rem 2rem; border-bottom: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; }
.header-main { display: flex; align-items: center; gap: 1rem; }
.header-main i { font-size: 1rem; color: var(--cf-gold); }
.header-main h5 { font-family: var(--cf-serif); font-size: 1.25rem; font-weight: 600; margin: 0; }
.time-range { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.05em; }

.chart-container-premium { padding: 2rem; height: 340px; }

/* Dispatch Center */
.dispatch-center { flex: 1; display: flex; flex-direction: column; justify-content: center; padding: 3rem 2rem; text-align: center; }
.dispatch-status-alert h4 { font-family: var(--cf-serif); font-size: 1.5rem; font-weight: 600; margin: 1.5rem 0 0.5rem; }
.dispatch-status-alert p { font-size: 0.9rem; color: var(--cf-text-muted); margin-bottom: 2rem; }

.alert-icon-wrap { width: 90px; height: 90px; margin: 0 auto; border-radius: 50%; background: #fff1f2; display: flex; align-items: center; justify-content: center; font-size: 2rem; color: #ef4444; position: relative; }
.alert-pulse { position: absolute; inset: -4px; border: 4px solid #fecaca; border-radius: 50%; animation: alert-ring 1.5s infinite; }
@keyframes alert-ring { from {transform:scale(0.8); opacity:1} to {transform:scale(1.4); opacity:0} }

.btn-dispatch-premium { background: var(--cf-text-dark); color: #fff; padding: 1rem 2rem; border-radius: 50px; font-weight: 700; text-decoration: none; display: inline-flex; align-items: center; gap: 0.75rem; transition: all 0.3s; }
.btn-dispatch-premium:hover { background: #000; transform: translateY(-2px); box-shadow: 0 10px 25px rgba(0,0,0,0.2); }

.dispatch-status-clear .clear-icon { font-size: 3.5rem; color: var(--cf-green); margin-bottom: 1.5rem; }
.dispatch-status-clear h4 { font-family: var(--cf-serif); font-size: 1.5rem; font-weight: 600; margin: 0 0 0.5rem; }
.btn-history-premium { background: var(--cf-ivory); border: 1px solid var(--cf-border); color: var(--cf-text-dark); padding: 0.75rem 2rem; border-radius: 50px; font-weight: 700; text-decoration: none; display: inline-block; transition: all 0.2s; }

.pix-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(5px); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.pix-modal-card { background: #fff; width: 100%; max-width: 450px; padding: 2.5rem; border-radius: 24px; box-shadow: 0 25px 50px rgba(0,0,0,0.25); animation: fadeIn 0.4s ease; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>
