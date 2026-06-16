<template>
  <div class="promotions-page">
    <!-- Header Hero -->
    <div class="promo-hero">
      <div class="container text-center">
        <span class="badge rounded-pill bg-gold px-3 py-2 mb-3 fade-in">OFERTAS IMPERDÍVEIS</span>
        <h1 class="display-4 fw-bold mb-3 fade-in-up">Economize até <em>20%</em> OFF</h1>
        <p class="lead text-muted fade-in-up">Os melhores produtos com descontos exclusivos das farmácias parceiras.</p>
      </div>
    </div>

    <div class="container py-5">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-green" role="status"></div>
        <p class="mt-3 text-muted">Buscando as melhores ofertas...</p>
      </div>

      <div v-else-if="groupedProducts.length === 0" class="text-center py-5 no-promos">
        <img src="/images/empty-promo.svg" alt="Sem promoções" class="mb-4" style="max-width: 200px;">
        <h3>Nenhuma promoção ativa no momento</h3>
        <p class="text-muted">Fique de olho! Novas ofertas aparecem todos os dias.</p>
        <router-link to="/products" class="btn btn-green mt-3">Ver todos os produtos</router-link>
      </div>

      <div v-else>
        <!-- Loop por Categorias -->
        <div v-for="group in groupedProducts" :key="group.category" class="category-section mb-5">
          <div class="d-flex align-items-center mb-4 section-header">
            <h2 class="h4 fw-bold mb-0">{{ group.category }}</h2>
            <div class="flex-grow-1 ms-3 border-bottom opacity-25"></div>
            <span class="badge bg-green-light text-green ms-3">{{ group.items.length }} itens</span>
          </div>
          
          <div class="row g-4">
            <div v-for="product in group.items" :key="product.id" class="col-6 col-md-4 col-lg-3">
              <ProductCard :product="product" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ProductCard from '@/components/products/ProductCard.vue'
import api from '@/services/api'

export default {
  name: 'Promotions',
  components: { ProductCard },
  data() {
    return {
      products: [],
      loading: true
    }
  },
  computed: {
    groupedProducts() {
      const groups = {};
      this.products.forEach(p => {
        const cat = p.category || 'Outros';
        if (!groups[cat]) groups[cat] = [];
        groups[cat].push(p);
      });
      return Object.keys(groups).map(cat => ({
        category: cat,
        items: groups[cat]
      }));
    }
  },
  async created() {
    await this.fetchPromotions();
  },
  methods: {
    async fetchPromotions() {
      this.loading = true;
      try {
        const response = await api.get('/produtos/buscar?emPromocao=true');
        this.products = response.data;
      } catch (error) {
        console.error('Erro ao buscar promoções:', error);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.promotions-page {
  min-height: 100vh;
  background: #fcfcfc;
  padding-top: 80px; /* Offset for sticky header */
}

.promo-hero {
  background: white;
  padding: 4rem 0;
  border-bottom: 1px solid var(--cf-border);
  margin-bottom: 2rem;
}

.bg-gold { background-color: var(--cf-gold); color: white; }
.text-green { color: var(--cf-green); }
.bg-green-light { background-color: var(--cf-green-xlight); }
.btn-green { background: var(--cf-green); color: white; }
.btn-green:hover { background: var(--cf-green-dark); color: white; }

.category-section {
  animation: fadeIn 0.5s ease-out both;
}

.section-header h2 {
  color: var(--cf-text-dark);
  letter-spacing: -0.02em;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .display-4 { font-size: 2.2rem; }
}
</style>
