<template>
  <div class="cf-mgmt">
    <div class="dash-welcome mb-4">
      <div class="welcome-text">
        <h3 class="dash-page-title">Monitoramento de Estoque</h3>
        <p class="text-muted mb-0">Controle proativo de inventário e alertas de reposição emergencial.</p>
      </div>
      <div class="dash-actions">
        <div class="cf-pill-counter shadow-sm" v-if="lowStockProducts.length > 0">
          <span class="label text-danger">Alertas Críticos</span>
          <span class="count bg-danger">{{ lowStockProducts.length }}</span>
        </div>
      </div>
    </div>

    <!-- Banner de Alerta Crítico -->
    <div v-if="lowStockProducts.length > 0" class="cf-alert-banner animate__animated animate__headShake mb-4 shadow-sm">
      <div class="d-flex align-items-center gap-3">
        <div class="alert-icon-wrap bg-white text-danger shadow-sm">
          <i class="fas fa-triangle-exclamation"></i>
        </div>
        <div class="flex-grow-1">
          <h6 class="mb-1 fw-bold text-dark">Reposição Necessária</h6>
          <p class="mb-0 text-muted extra-small fw-bold text-uppercase letter-spacing-1">
            {{ lowStockProducts.length }} itens operando abaixo do limite de segurança ({{ lowStockThreshold }} un).
          </p>
        </div>
        <div class="d-none d-md-block">
          <button class="btn btn-sm btn-danger fw-bold rounded-pill px-3" @click="scrollToList">Verificar Itens</button>
        </div>
      </div>
    </div>

    <div class="row g-4 animate__animated animate__fadeInUp">
      <div v-for="product in products" :key="product.id" class="col-xl-3 col-lg-4 col-md-6">
        <div class="cf-inventory-card shadow-sm" :class="{ 'low-stock': product.stock < lowStockThreshold }">
          <div class="p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="cf-icon-avatar shadow-sm" :class="product.stock < lowStockThreshold ? 'bg-danger-subtle text-danger' : 'bg-success-subtle text-success'">
                <i class="fas fa-box-archive"></i>
              </div>
              <span class="cf-status-badge shadow-sm" :class="product.stock < lowStockThreshold ? 'rejeitada' : 'aprovada'">
                {{ product.stock < lowStockThreshold ? 'Repor' : 'Ok' }}
              </span>
            </div>

            <h6 class="cf-td-bold text-dark mb-1">{{ product.name }}</h6>
            <div class="text-muted extra-small fw-bold mb-4">SKU Interno: #{{ 1000 + product.id }}</div>

            <div class="cf-stock-display mb-4">
              <div class="label text-muted">Disponibilidade</div>
              <div class="d-flex align-items-end gap-2">
                <span class="value" :class="{ 'text-danger': product.stock < lowStockThreshold }">{{ product.stock }}</span>
                <span class="unit text-muted small fw-bold">UNIDADES</span>
              </div>
              <div class="progress mt-2" style="height: 6px;">
                <div class="progress-bar" :class="product.stock < lowStockThreshold ? 'bg-danger' : 'bg-success'" 
                     :style="`width: ${Math.min((product.stock / 50) * 100, 100)}%`"></div>
              </div>
            </div>

            <div class="cf-stock-input-group">
              <input v-model="stockUpdates[product.id]" type="number" class="cf-input-premium shadow-sm" placeholder="Nova Qtd..." />
              <button @click="updateStock(product.id)" class="cf-icon-btn shadow-sm" title="Salvar Estoque">
                <i class="fas fa-save"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InventoryManagement',
  data() {
    return {
      products: [],
      stockUpdates: {},
      lowStockThreshold: 10
    }
  },
  computed: {
    lowStockProducts() {
      return this.products.filter(p => p.stock < this.lowStockThreshold)
    }
  },
  mounted() {
    this.fetchProducts()
  },
  methods: {
    async fetchProducts() {
      // Mock data logic remains same for now
      this.products = [
        { id: 1, name: 'Paracetamol 500mg', stock: 5 },
        { id: 2, name: 'Amoxicilina 500mg', stock: 15 },
        { id: 3, name: 'Ibuprofeno 400mg', stock: 8 },
        { id: 4, name: 'Omeprazol 20mg', stock: 25 },
        { id: 5, name: 'Losartana 50mg', stock: 12 },
        { id: 6, name: 'Metformina 850mg', stock: 18 }
      ]
    },

    async updateStock(id) {
      const newStock = parseInt(this.stockUpdates[id])
      if (isNaN(newStock)) {
        alert('Insira uma quantidade válida')
        return
      }

      const product = this.products.find(p => p.id === id)
      if (product) {
        product.stock = newStock
        this.stockUpdates[id] = ''
        if (window.$toast) window.$toast.addToast(`Estoque de ${product.name} atualizado!`, 'success');
        else alert(`Estoque de ${product.name} atualizado para ${newStock} unidades!`)
      }
    },
    scrollToList() {
      // Smooth scroll placeholder
    }
  }
}
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

.cf-pill-counter { background: #fff; border-radius: 20px; padding: 0.5rem 1rem; border: 1px solid var(--cf-border); display: flex; align-items: center; gap: 0.75rem; }
.cf-pill-counter .label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; }
.cf-pill-counter .count { color: #fff; padding: 0.1rem 0.6rem; border-radius: 12px; font-size: 0.75rem; font-weight: 800; }

.cf-alert-banner { background: #FFF5F5; border: 1px solid #FED7D7; border-radius: 20px; padding: 1.25rem 1.5rem; border-left: 5px solid #E53E3E; }
.alert-icon-wrap { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; flex-shrink: 0; }

.cf-inventory-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); overflow: hidden; transition: all 0.3s; position: relative; }
.cf-inventory-card:hover { transform: translateY(-5px); box-shadow: var(--cf-shadow-md); }
.cf-inventory-card.low-stock { border-bottom: 5px solid #E53E3E; }

.cf-icon-avatar { width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; flex-shrink: 0; }

.cf-status-badge { font-size: 0.62rem; font-weight: 800; padding: 0.35rem 0.85rem; border-radius: 20px; text-transform: uppercase; display: inline-flex; align-items: center; }
.cf-status-badge.aprovada { background: #E8F5E9; color: #2E7D32; border: 1px solid #C8E6C9; }
.cf-status-badge.rejeitada { background: #FFF5F5; color: #E53E3E; border: 1px solid #FED7D7; }

.cf-stock-display .label { font-size: 0.62rem; font-weight: 700; text-transform: uppercase; color: var(--cf-text-muted); letter-spacing: 0.05em; margin-bottom: 2px; }
.cf-stock-display .value { font-size: 2rem; font-weight: 800; line-height: 1; }

.cf-stock-input-group { display: flex; gap: 0.75rem; }
.cf-stock-input-group .cf-input-premium { padding: 0.6rem 0.85rem; font-size: 0.82rem; border-radius: 12px; flex: 1; }

.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>