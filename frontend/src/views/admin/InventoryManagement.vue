<template>
  <div class="cf-mgmt-premium">
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Monitoramento de Estoque</h3>
        <p class="editorial-subtitle">Controle proativo de inventário e alertas de reposição emergencial.</p>
      </div>
      <div class="header-tools">
        <div class="cf-pill-counter shadow-sm" v-if="lowStockProducts.length > 0">
          <span class="label text-danger">Alertas Críticos</span>
          <span class="count bg-danger">{{ lowStockProducts.length }}</span>
        </div>
      </div>
    </header>

    <!-- Banner de Alerta Crítico -->
    <div v-if="lowStockProducts.length > 0" class="cf-alert-banner animate__animated animate__headShake mb-4 shadow-sm">
      <div class="alert-icon-wrap shadow-sm">
        <i class="fas fa-triangle-exclamation"></i>
      </div>
      <div class="flex-grow-1">
        <h6>Reposição Necessária</h6>
        <p>{{ lowStockProducts.length }} itens operando abaixo do limite de segurança ({{ lowStockThreshold }} un).</p>
      </div>
      <div class="d-none d-md-block">
        <button class="btn btn-sm btn-danger fw-bold rounded-pill px-3" @click="scrollToList">Verificar Itens</button>
      </div>
    </div>

    <div class="row g-4 animate__animated animate__fadeInUp">
      <div v-for="product in products" :key="product.id" class="col-xl-3 col-lg-4 col-md-6">
        <div class="cf-inventory-card shadow-sm" :class="{ 'low-stock': product.stock < lowStockThreshold }">
          <div class="p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="entity-avatar shadow-sm" :class="product.stock < lowStockThreshold ? 'bg-danger-subtle text-danger' : 'green'">
                <i class="fas fa-box-archive"></i>
              </div>
              <span class="status-pill shadow-sm" :class="product.stock < lowStockThreshold ? 'rejected' : 'active'">
                <span class="pill-dot"></span>
                <span class="pill-text">{{ product.stock < lowStockThreshold ? 'Repor' : 'Ok' }}</span>
              </span>
            </div>

            <h6 class="entity-name mb-1" style="font-size: 1.1rem;">{{ product.name }}</h6>
            <div class="entity-sub fw-bold mb-4">SKU Interno: #{{ 1000 + product.id }}</div>

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
              <button @click="updateStock(product.id)" class="action-btn-circle shadow-sm" title="Salvar Estoque">
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
