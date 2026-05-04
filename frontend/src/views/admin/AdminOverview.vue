<template>
  <div class="cf-overview-premium">
    <!-- ═══ HEADER EXECUTIVO ═══ -->
    <header class="overview-header">
      <div class="header-meta">
        <h3 class="editorial-title">Inteligência Operacional</h3>
        <p class="editorial-subtitle">Relatório consolidado de performance da rede ClickFarma</p>
      </div>
      <div class="header-actions">
        <div class="date-badge">
          <i class="far fa-calendar-alt"></i>
          <span>{{ currentMonth }}</span>
        </div>
        <button class="action-btn-gold" @click="loadData" :disabled="loading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>
          <span>Sincronizar Dados</span>
        </button>
      </div>
    </header>

    <!-- ═══ KPI DASHBOARD ═══ -->
    <div class="kpi-grid">
      <div v-for="kpi in kpis" :key="kpi.label" class="kpi-premium-card" :class="kpi.accent">
        <div class="card-glass"></div>
        <div class="kpi-icon-box">
          <i :class="kpi.icon"></i>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">{{ kpi.label }}</span>
          <div class="kpi-value-wrap">
            <h2 class="kpi-value">{{ kpi.value }}</h2>
            <div class="kpi-trend" v-if="kpi.trend">
              <i class="fas fa-arrow-trend-up"></i>
              <span>+12.5%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ BUSINESS ANALYTICS ═══ -->
    <div class="analytics-row">
      <!-- Volume Chart -->
      <div class="chart-box-main">
        <div class="box-header">
          <div class="title-group">
            <h5 class="box-title">Fluxo Transacional Semanal</h5>
            <span class="box-tag">Vendas vs Meta</span>
          </div>
          <div class="box-controls">
            <span class="indicator primary">Faturamento</span>
          </div>
        </div>
        <div class="chart-canvas-wrap">
          <canvas id="barChart"></canvas>
        </div>
      </div>

      <!-- Conversion Chart -->
      <div class="chart-box-side">
        <div class="box-header">
          <h5 class="box-title">Composição de Fluxo</h5>
        </div>
        <div class="chart-canvas-wrap doughnut-wrap">
          <canvas id="pieChart"></canvas>
          <div class="doughnut-center">
            <span class="center-val">{{ totalPedidos }}</span>
            <span class="center-label">Pedidos</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ RANKING BOARDS ═══ -->
    <div class="rankings-grid">
      <div v-for="(rank, idx) in rankings" :key="idx" class="rank-board">
        <div class="rank-header">
          <div class="rank-title-group">
            <h6 class="rank-title">{{ rank.title }}</h6>
            <span class="rank-subtitle">{{ rank.subtitle }}</span>
          </div>
          <div class="rank-badge-icon">
            <i :class="idx === 0 ? 'fas fa-store' : (idx === 1 ? 'fas fa-user-tie' : 'fas fa-truck-fast')"></i>
          </div>
        </div>
        
        <div class="rank-list">
          <div v-for="(item, i) in rank.data" :key="i" class="rank-item">
            <div class="rank-position">
              <div class="medal" :class="{ 'gold': i === 0, 'silver': i === 1, 'bronze': i === 2 }">
                {{ i + 1 }}
              </div>
            </div>
            <div class="rank-entity">
              <span class="entity-name">{{ item.name }}</span>
              <div class="entity-bar-wrap">
                <div class="entity-bar" :style="{ width: getBarWidth(item.value, rank.data) + '%' }"></div>
              </div>
            </div>
            <div class="rank-metric">
              <span class="metric-val">{{ rank.prefix }}{{ item.value }}</span>
            </div>
          </div>
          
          <div v-if="!rank.data.length" class="empty-rank">
            <div class="empty-glass">
              <i class="fas fa-microchip"></i>
              <p>Processando algoritmos...</p>
            </div>
          </div>
        </div>
        
        <div class="rank-footer" v-if="rank.data.length">
          <button class="rank-btn-more">Análise Detalhada <i class="fas fa-chevron-right ms-1"></i></button>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';

export default {
  name: 'AdminOverview',
  data() {
    return {
      kpis: [],
      totalPedidos: 0,
      rankings: [
        { title: 'Leaderboard Farmácias', subtitle: 'Líderes de Faturamento', header: 'Parceiro', metric: 'Receita', prefix: 'R$ ', data: [] },
        { title: 'Top Clientes Master', subtitle: 'Fidelidade & Ticket', header: 'Cliente', metric: 'Total', prefix: 'R$ ', data: [] },
        { title: 'Malha Logística', subtitle: 'Eficiência de Entrega', header: 'Operador', metric: 'Envios', prefix: '', data: [] }
      ],
      loading: true,
      barChart: null,
      pieChart: null
    };
  },
  computed: {
    currentMonth() {
      const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
      const d = new Date();
      return `${meses[d.getMonth()]} ${d.getFullYear()}`;
    }
  },

  async mounted() {
    await Promise.all([this.loadData(), this.loadCharts()]);
  },
  methods: {
    num(v) { return Number(v || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }); },

    async loadData() {
      this.loading = true;
      try {
        const { data: d } = await api.get('/dashboard/admin');
        this.totalPedidos = d.totalPedidos ?? 0;
        this.kpis = [
          { label: 'Receita Operacional', value: `R$ ${this.num(d.faturamentoTotal)}`, icon: 'fas fa-sack-dollar', accent: 'gold', trend: true },
          { label: 'Lucro de Plataforma', value: `R$ ${this.num(d.comissaoTotal)}`, icon: 'fas fa-chart-line', accent: 'green', trend: true },
          { label: 'Pedidos Consolidados', value: d.totalPedidos ?? 0, icon: 'fas fa-shopping-basket', accent: 'ivory' },
          { label: 'Estatura de Base', value: d.totalClientes ?? 0, icon: 'fas fa-users-viewfinder', accent: 'ivory' }
        ];

        const { data: r } = await api.get('/dashboard/admin/rankings');
        this.rankings[0].data = (r.topFarmacias || []).slice(0, 5).map(f => ({ name: f.nome, value: f.faturamento }));
        this.rankings[1].data = (r.topClientes || []).slice(0, 5).map(c => ({ name: c.nome || 'Consumidor', value: c.totalGasto }));
        this.rankings[2].data = (r.topMotoboys || []).slice(0, 5).map(m => ({ name: m.nome || 'Entregador', value: m.entregas }));
      } catch (e) {
        console.error(e);
      } finally {
        this.loading = false;
      }
    },
    getBarWidth(val, all) {
      const max = Math.max(...all.map(x => x.value), 1);
      return (val / max) * 100;
    },


    async loadCharts() {
      await this.$nextTick();
      const Chart = window.Chart;
      if (!Chart) return;
      
      try {
        const { data: s } = await api.get('/dashboard/admin/vendas-semana');
        const ctxBar = document.getElementById('barChart');
        if (ctxBar) {
          if (this.barChart) this.barChart.destroy();
          this.barChart = new Chart(ctxBar.getContext('2d'), {
            type: 'bar',
            data: {
              labels: s.labels || [],
              datasets: [{
                label: 'Faturamento',
                data: s.valores || [],
                backgroundColor: '#2A5C45',
                hoverBackgroundColor: '#1a3b2c',
                borderRadius: 12,
                barThickness: 32
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              plugins: { legend: { display: false } },
              scales: {
                x: { grid: { display: false }, ticks: { font: { weight: 'bold' } } },
                y: { 
                  grid: { color: 'rgba(0,0,0,0.05)', drawBorder: false }, 
                  ticks: { callback: v => `R$${v}`, font: { size: 10 } } 
                }
              }
            }
          });
        }

        const { data: st } = await api.get('/dashboard/admin/pedidos-por-status');
        const ctxPie = document.getElementById('pieChart');
        if (ctxPie) {
          if (this.pieChart) this.pieChart.destroy();
          this.pieChart = new Chart(ctxPie.getContext('2d'), {
            type: 'doughnut',
            data: {
              labels: Object.keys(st.porStatus || {}),
              datasets: [{
                data: Object.values(st.porStatus || {}),
                backgroundColor: ['#2A5C45', '#B89550', '#3D7A5E', '#B0AFA9', '#E8F2EC'],
                borderWidth: 8,
                borderColor: '#ffffff',
                hoverOffset: 10
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              cutout: '75%',
              plugins: { 
                legend: { 
                  position: 'bottom', 
                  labels: { 
                    usePointStyle: true, 
                    padding: 25,
                    font: { size: 11, weight: 'bold' }
                  } 
                } 
              }
            }
          });
        }
      } catch (e) { console.error(e); }
    }
  }
};
</script>

<style scoped>
.cf-overview-premium { animation: fadeIn 0.8s var(--cf-ease); }

/* Header */
.overview-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 3rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.header-actions { display: flex; align-items: center; gap: 1.5rem; }
.date-badge { background: #fff; padding: 0.6rem 1.25rem; border-radius: 50px; border: 1px solid var(--cf-border); display: flex; align-items: center; gap: 0.75rem; font-size: 0.85rem; font-weight: 700; color: var(--cf-text-muted); box-shadow: var(--cf-shadow-sm); }
.action-btn-gold { background: var(--cf-gold); color: #fff; border: none; padding: 0.75rem 1.5rem; border-radius: 12px; font-weight: 700; font-size: 0.85rem; display: flex; align-items: center; gap: 0.75rem; transition: all 0.3s var(--cf-ease); cursor: pointer; box-shadow: 0 4px 15px rgba(184,149,80,0.3); }
.action-btn-gold:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(184,149,80,0.4); }
.action-btn-gold:disabled { opacity: 0.7; cursor: not-allowed; }

/* KPI Grid */
.kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1.5rem; margin-bottom: 3rem; }
.kpi-premium-card { background: #fff; padding: 1.5rem; border-radius: 24px; border: 1px solid var(--cf-border); position: relative; overflow: hidden; display: flex; align-items: center; gap: 1.5rem; transition: all 0.4s var(--cf-ease); box-shadow: var(--cf-shadow-sm); }
.kpi-premium-card:hover { transform: translateY(-5px); box-shadow: var(--cf-shadow-md); border-color: var(--cf-gold-light); }
.card-glass { position: absolute; inset: 0; background: linear-gradient(135deg, rgba(255,255,255,0.8), rgba(255,255,255,0)); pointer-events: none; }

.kpi-icon-box { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; background: var(--cf-ivory); color: var(--cf-green); flex-shrink: 0; z-index: 1; }
.kpi-premium-card.gold .kpi-icon-box { background: var(--cf-gold-light); color: var(--cf-gold); }
.kpi-premium-card.green .kpi-icon-box { background: var(--cf-green-xlight); color: var(--cf-green); }

.kpi-info { flex: 1; z-index: 1; }
.kpi-label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.15em; }
.kpi-value-wrap { display: flex; align-items: baseline; gap: 0.75rem; margin-top: 0.25rem; }
.kpi-value { font-size: 1.8rem; font-weight: 800; color: var(--cf-text-dark); margin: 0; line-height: 1; letter-spacing: -0.02em; }
.kpi-trend { font-size: 0.7rem; font-weight: 700; color: #22c55e; display: flex; align-items: center; gap: 0.25rem; }

/* Charts */
.analytics-row { display: grid; grid-template-columns: 2fr 1fr; gap: 2rem; margin-bottom: 3rem; }
.chart-box-main, .chart-box-side { background: #fff; padding: 2rem; border-radius: 28px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); }
.box-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; }
.box-title { font-family: var(--cf-serif); font-size: 1.3rem; font-weight: 600; margin: 0; }
.box-tag { font-size: 0.7rem; font-weight: 700; color: var(--cf-text-faint); text-transform: uppercase; }
.indicator { display: flex; align-items: center; gap: 0.5rem; font-size: 0.75rem; font-weight: 700; color: var(--cf-text-muted); }
.indicator::before { content: ''; width: 10px; height: 10px; border-radius: 50%; background: var(--cf-green); }

.chart-canvas-wrap { height: 320px; position: relative; }
.doughnut-wrap { display: flex; align-items: center; justify-content: center; }
.doughnut-center { position: absolute; display: flex; flex-direction: column; align-items: center; pointer-events: none; }
.center-val { font-size: 2.2rem; font-weight: 800; color: var(--cf-text-dark); line-height: 1; }
.center-label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; margin-top: 0.2rem; }

/* Rankings */
.rankings-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 2rem; }
.rank-board { background: #fff; border-radius: 28px; border: 1px solid var(--cf-border); overflow: hidden; display: flex; flex-direction: column; box-shadow: var(--cf-shadow-sm); }
.rank-header { padding: 1.5rem 2rem; border-bottom: 1px solid var(--cf-border-mid); display: flex; justify-content: space-between; align-items: center; background: var(--cf-ivory-light); }
.rank-title { font-family: var(--cf-serif); font-size: 1.2rem; font-weight: 600; margin: 0; }
.rank-subtitle { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; }
.rank-badge-icon { width: 36px; height: 36px; background: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: var(--cf-gold); border: 1px solid var(--cf-border); }

.rank-list { padding: 1.5rem 2rem; flex: 1; }
.rank-item { display: flex; align-items: center; gap: 1.25rem; padding: 0.85rem 0; border-bottom: 1px solid rgba(0,0,0,0.03); }
.rank-item:last-child { border-bottom: none; }

.medal { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 800; background: var(--cf-ivory); color: var(--cf-text-muted); }
.medal.gold { background: var(--cf-gold); color: #fff; box-shadow: 0 4px 12px rgba(184,149,80,0.3); }
.medal.silver { background: #e2e8f0; color: #475569; }
.medal.bronze { background: #fed7aa; color: #9a3412; }

.rank-entity { flex: 1; min-width: 0; }
.entity-name { font-size: 0.9rem; font-weight: 700; color: var(--cf-text-dark); display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 0.35rem; }
.entity-bar-wrap { height: 4px; background: var(--cf-ivory); border-radius: 10px; overflow: hidden; }
.entity-bar { height: 100%; background: var(--cf-green); border-radius: 10px; opacity: 0.2; }
.rank-item:nth-child(1) .entity-bar { opacity: 0.8; }

.metric-val { font-size: 0.85rem; font-weight: 800; color: var(--cf-text-dark); }

.rank-footer { padding: 1.25rem 2rem; border-top: 1px solid var(--cf-border-mid); background: var(--cf-ivory-light); text-align: center; }
.rank-btn-more { background: none; border: none; font-size: 0.75rem; font-weight: 800; color: var(--cf-text-muted); text-transform: uppercase; cursor: pointer; transition: color 0.2s; }
.rank-btn-more:hover { color: var(--cf-gold); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 1200px) {
  .analytics-row { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .overview-header { flex-direction: column; align-items: flex-start; gap: 1.5rem; }
}
</style>
