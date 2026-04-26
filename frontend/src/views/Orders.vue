<template>
  <div class="orders-page">
    <div class="container py-lg-5 py-4">
      
      <!-- Cabeçalho -->
      <div class="header-wrap d-flex justify-content-between align-items-end mb-5 fade-in-up">
        <div class="title-group">
          <span class="section-eyebrow">Minhas Compras</span>
          <h1 class="section-title mb-0">Meus <em>Pedidos</em></h1>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-outline-primary btn-refresh" @click="refreshOrders">
            <i class="fa-solid fa-arrows-rotate" :class="{ 'fa-spin': loading }"></i>
          </button>
        </div>
      </div>

      <!-- Estado Vazio -->
      <div v-if="!hasOrders && !loading" class="text-center py-5 fade-in-up">
        <div class="empty-state-box">
          <div class="empty-icon-wrap mb-4">
               <i class="fa-solid fa-box-open"></i>
          </div>
          <h3 class="section-title-sm mb-3">Nenhum pedido realizado</h3>
          <p class="text-muted mb-4 mx-auto" style="max-width: 400px;">
            Sua lista de compras está vazia. Que tal conferir nossas ofertas e garantir sua saúde hoje mesmo?
          </p>
          <router-link to="/products" class="btn btn-primary btn-lg">
            Ver Catálogo de Produtos
            <i class="fa-solid fa-arrow-right ms-2 mt-1"></i>
          </router-link>
        </div>
      </div>

      <div v-else class="row g-4">
        <div class="col-lg-8 fade-in-up">
            
          <div class="order-stats-bar mb-4 p-3 d-flex justify-content-between align-items-center">
             <span class="stats-text"><strong>{{ validOrders.length }}</strong> pedidos encontrados no seu histórico</span>
             <button v-if="hasOrders" class="btn-clear text-danger" @click="clearAllOrders">Limpar Tudo</button>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-green-mid" role="status"></div>
            <p class="mt-3 text-muted">Buscando seus dados...</p>
          </div>

          <!-- Lista de Pedidos -->
          <div v-for="order in validOrders" :key="order.id" class="cf-order-card mb-4">
            <div class="order-header d-flex justify-content-between p-4">
               <div class="id-group">
                  <span class="order-id">Pedido #{{ order.id }}</span>
                  <div class="order-meta d-flex gap-3">
                     <span><i class="fa-solid fa-calendar-day me-1"></i> {{ formatDate(order.date) }}</span>
                     <span><i class="fa-solid fa-cart-shopping me-1"></i> {{ order.items.length }} itens</span>
                  </div>
               </div>
               <div class="text-end">
                  <span :class="statusClass(order.status)" class="cf-badge mb-2 d-inline-block">
                    {{ statusText(order.status) }}
                  </span>
                  <h4 class="order-total-val mb-0">R$ {{ getOrderTotal(order) }}</h4>
               </div>
            </div>

            <div class="order-body p-4 pt-0">
               <!-- Itens (Compacto) -->
               <div class="order-items-preview mb-4">
                  <div v-for="item in order.items" :key="item.id" class="d-flex align-items-center py-2 border-bottom-cf">
                     <div class="item-visual-sm me-3">
                        📦
                     </div>
                     <div class="flex-grow-1">
                        <h6 class="item-name-sm mb-0">{{ item.name }}</h6>
                        <span class="item-meta-sm">Qtd: {{ item.quantity }} × R$ {{ item.price.toFixed(2).replace('.', ',') }}</span>
                     </div>
                     <span class="item-sub-total">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                  </div>
               </div>

               <!-- Ações -->
               <div class="d-flex gap-3 mt-4">
                  <button class="btn btn-outline-primary btn-sm flex-grow-1" @click="viewOrderDetails(order)">
                    <i class="fa-solid fa-circle-info me-2"></i>Ver Detalhes
                  </button>
                  <button class="btn btn-primary btn-sm flex-grow-1" @click="trackOrder(order)">
                    <i class="fa-solid fa-truck-ramp-box me-2"></i>Rastrear
                  </button>
               </div>
            </div>
          </div>
        </div>

        <!-- Sidebar de Resumo -->
        <div class="col-lg-4 fade-in-up" style="animation-delay: 0.1s">
          <div class="cf-summary-card">
            <h4 class="summary-title mb-4">Giro das Compras</h4>
            <div class="stats-grid">
               <div class="stat-box">
                  <span class="stat-label">Total de Gastos</span>
                  <p class="stat-val-lg text-green">R$ {{ totalSpent.replace('.', ',') }}</p>
               </div>
               <div class="stat-box mt-3">
                  <span class="stat-label">Ticket Médio</span>
                  <p class="stat-val">R$ {{ averageOrderValue.replace('.', ',') }}</p>
               </div>
            </div>
            
            <div class="cf-divider my-4"></div>
            
            <div class="status-summary">
               <h6 class="summary-subtitle mb-3">Status dos Pedidos</h6>
               <div v-for="status in orderStatusCount" :key="status.type" class="status-row mb-2">
                  <span class="status-label">{{ status.label }}</span>
                  <span :class="statusClass(status.type)" class="cf-mini-badge">{{ status.count }}</span>
               </div>
            </div>

            <router-link to="/products" class="btn btn-primary w-100 mt-5">
              Fazer Novo Pedido
              <i class="fa-solid fa-plus ms-1"></i>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Orders',
  data() {
    return {
      orders: [],
      loading: false
    }
  },
  computed: {
    hasOrders() { return this.validOrders.length > 0 },
    validOrders() {
      return this.orders
        .filter(order => this.isValidOrder(order))
        .sort((a, b) => new Date(b.date) - new Date(a.date))
    },
    averageOrderValue() {
      if (this.validOrders.length === 0) return '0,00'
      const total = this.validOrders.reduce((sum, order) => sum + this.calculateOrderTotal(order), 0)
      return (total / this.validOrders.length).toFixed(2)
    },
    totalSpent() {
      return this.validOrders.reduce((sum, order) => sum + this.calculateOrderTotal(order), 0).toFixed(2)
    },
    orderStatusCount() {
      const statusCount = {}
      this.validOrders.forEach(order => {
        statusCount[order.status] = (statusCount[order.status] || 0) + 1
      })
      return Object.keys(statusCount).map(status => ({
        type: status,
        label: this.statusText(status),
        count: statusCount[status]
      }))
    }
  },
  methods: {
    isValidOrder(order) { return order && order.id && order.items && order.date; },
    formatDate(dateString) { return dateString ? new Date(dateString).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }) : '-'; },
    statusClass(status) {
      const classes = { 'confirmed': 'badge-warning', 'processing': 'badge-warning', 'shipped': 'badge-success', 'delivered': 'badge-success', 'cancelled': 'badge-danger' };
      return classes[status] || 'badge-secondary';
    },
    statusText(status) {
      const texts = { 'confirmed': 'Confirmado', 'processing': 'Em Preparo', 'shipped': 'Enviado', 'delivered': 'Entregue', 'cancelled': 'Cancelado' };
      return texts[status] || status;
    },
    calculateOrderTotal(order) { return order.total || order.items.reduce((s, i) => s + (i.price * i.quantity), 0); },
    getOrderTotal(order) { return this.calculateOrderTotal(order).toFixed(2).replace('.', ','); },
    async refreshOrders() { this.loading = true; setTimeout(() => { this.loadOrders(); this.loading = false; }, 800); },
    viewOrderDetails(order) { this.$router.push(`/tracking/${order.id}`); },
    trackOrder(order) { this.$router.push(`/tracking/${order.id}`); },
    clearAllOrders() { if(confirm('Remover histórico?')) { this.orders = []; localStorage.removeItem('userOrders'); } },
    loadOrders() {
      const saved = localStorage.getItem('userOrders');
      if (saved) this.orders = JSON.parse(saved);
    }
  },
  mounted() { this.loadOrders(); }
}
</script>

<style scoped>
.orders-page { background: var(--cf-white); min-height: 90vh; }

.header-wrap { border-bottom: 1px solid var(--cf-border); padding-bottom: 1.5rem; }

/* EMPTY STATE */
.empty-state-box { padding: 4rem 2rem; background: var(--cf-ivory); border-radius: var(--cf-r-xl); border: 1px solid var(--cf-border); }
.empty-icon-wrap { width: 100px; height: 100px; background: white; border-radius: 50%; color: var(--cf-text-faint); display: flex; align-items: center; justify-content: center; font-size: 3rem; margin: 0 auto; box-shadow: var(--cf-shadow-sm); }
.section-title-sm { font-family: var(--cf-sans); font-size: 1.8rem; font-weight: 600; color: var(--cf-text-dark); }

/* ORDER CARD */
.order-stats-bar { background: var(--cf-green-xlight); border-radius: var(--cf-r-md); border: 1px solid var(--cf-border); }
.stats-text { font-size: 0.85rem; color: var(--cf-green-dark); }
.btn-clear { background: transparent; border: none; font-size: 0.7rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }

.cf-order-card { background: white; border: 1px solid var(--cf-border); border-radius: var(--cf-r-lg); overflow: hidden; transition: all 250ms; }
.cf-order-card:hover { border-color: var(--cf-gold); transform: translateY(-2px); box-shadow: var(--cf-shadow-md); }
.order-header { border-bottom: 1px solid var(--cf-ivory); }
.order-id { font-family: var(--cf-sans); font-size: 1.4rem; font-weight: 600; color: var(--cf-text-dark); display: block; }
.order-meta { font-size: 0.75rem; color: var(--cf-text-muted); }
.order-total-val { font-family: var(--cf-sans); font-size: 1.6rem; color: var(--cf-green); font-weight: 600; }

.item-visual-sm { width: 40px; height: 40px; background: var(--cf-ivory); border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.item-name-sm { font-size: 0.9rem; color: var(--cf-text-dark); font-weight: 500; }
.item-meta-sm { font-size: 0.75rem; color: var(--cf-text-faint); }
.item-sub-total { font-family: var(--cf-sans); font-size: 0.95rem; font-weight: 600; color: var(--cf-text-dark); }
.border-bottom-cf { border-bottom: 1px solid var(--cf-border); }

/* SUMMARY CARD */
.cf-summary-card { background: var(--cf-ivory); border: 1px solid var(--cf-border); border-radius: var(--cf-r-xl); padding: 2rem; position: sticky; top: 100px; }
.summary-title { font-family: var(--cf-sans); font-size: 1.5rem; font-weight: 600; color: var(--cf-text-dark); }
.stat-label { font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); font-weight: 600; display: block; }
.stat-val-lg { font-size: 2rem; font-weight: 600; font-family: var(--cf-sans); }
.stat-val { font-size: 1.2rem; font-weight: 500; color: var(--cf-text-dark); font-family: var(--cf-sans); }

.summary-subtitle { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); font-weight: 600; margin-top: 1rem; }
.status-row { display: flex; justify-content: space-between; align-items: center; }
.status-label { font-size: 0.85rem; color: var(--cf-text-mid); }
.cf-mini-badge { padding: 2px 8px; border-radius: 100px; font-size: 0.65rem; font-weight: 600; }

.cf-badge { font-size: 0.65rem; padding: 4px 12px; border-radius: 100px; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }
.badge-warning { background: var(--cf-gold-light); color: var(--cf-gold); }
.badge-success { background: var(--cf-green-light); color: var(--cf-green); }
.badge-danger { background: #F9EDED; color: var(--cf-danger); }
.badge-secondary { background: var(--cf-cream); color: var(--cf-text-muted); }

.text-green { color: var(--cf-green) !important; }
</style>