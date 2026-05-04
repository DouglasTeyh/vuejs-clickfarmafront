<template>
  <div class="products-page">
    <!-- Header Section — Limpo e Editorial -->


    <div class="filters-wrapper">
      <div class="container">
        <div class="filters-section">
        <div class="row g-3">
          <div class="col-lg-4 col-md-6">
            <div class="filter-group">
              <label class="form-label"><i class="fas fa-folder-open me-1"></i> Categoria</label>
              <select v-model="filters.category" class="form-select">
                <option value="">Todas as especialidades</option>
                <option v-for="cat in categoriesList" :key="cat" :value="cat">
                  {{ cat }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-lg-3 col-md-6">
            <div class="filter-group">
              <label class="form-label"><i class="fas fa-sort-amount-down me-1"></i> Ordenar por</label>
              <select v-model="filters.sortBy" class="form-select">
                <optgroup label="Valor">
                  <option value="price">Menor Preço</option>
                  <option value="price_desc">Maior Preço</option>
                </optgroup>
                <optgroup label="Ordem">
                  <option value="name">A - Z</option>
                  <option value="name_desc">Z - A</option>
                </optgroup>
                <optgroup label="Prioridade">
                  <option value="relevance">Relevância</option>
                </optgroup>
              </select>
            </div>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="container mt-4">
      <!-- Active Filters -->
      <div v-if="hasActiveFilters" class="active-filters">
        <span class="fw-medium small me-2">Filtrado por:</span>
        <div class="d-flex flex-wrap gap-2">
          <span v-if="searchTerm" class="filter-tag">
            "{{ searchTerm }}"
            <i class="fas fa-times" @click="searchTerm = ''" style="cursor:pointer"></i>
          </span>
          <span v-if="filters.category" class="filter-tag">
            {{ filters.category }}
            <i class="fas fa-times" @click="filters.category = ''" style="cursor:pointer"></i>
          </span>
          <button @click="clearAllFilters" class="clear-all">Limpar filtros</button>
        </div>
      </div>
    </div>

    <div class="container">

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border cf-spinner" style="width: 2.5rem; height: 2.5rem;" role="status"></div>
        <p class="mt-3 text-muted fw-light">Organizando catálogo...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon"><i class="fas fa-exclamation-triangle"></i></div>
        <h4 class="mb-3">Conexão interrompida</h4>
        <p class="text-muted mb-4">{{ error }}</p>
        <button @click="retryLoading" class="btn btn-primary">
          Tentar novamente
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredProducts.length === 0" class="empty-state">
        <div class="empty-icon"><i class="fas fa-search"></i></div>
        <h4 class="mb-3">Nada encontrado</h4>
        <p class="text-muted mb-4">Tente outros termos ou remova os filtros ativos.</p>
        <button @click="clearAllFilters" class="btn btn-primary">Ver Tudo</button>
      </div>

      <!-- Products Grid -->
      <div v-else class="products-grid">
        <div class="row g-3 g-md-4">
          <div
              v-for="product in filteredProducts"
              :key="product.id"
              class="col-6 col-md-6 col-lg-4 col-xl-3"
          >
            <ProductCard
                :product="product"
                @add-to-cart="handleAddToCart"
            />
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import ProductCard from '@/components/products/ProductCard.vue'

export default {
  name: 'Products',
  components: {
    ProductCard
  },
  data() {
    return {
      searchTerm: '',
      filters: {
        category: '',
        sortBy: 'name',
        stock: 'all'
      },
      loading: false,
      error: null
    }
  },
  watch: {
    '$route.query.q': {
      immediate: true,
      async handler(newVal) {
        this.searchTerm = newVal || '';
        if (newVal) {
          this.loading = true;
          await this.fetchProducts({ nome: newVal });
          this.loading = false;
        }
      }
    },
    '$route.query.category': {
      immediate: true,
      handler(newVal) {
        if (newVal) this.filters.category = newVal;
      }
    },
    '$route.query.ai': {
      immediate: true,
      handler(newVal) {
        if (newVal) this.handleAiSearch(newVal);
      }
    }
  },
  computed: {
    ...mapState(['products']),

    categoriesList() {
      if (!this.products || !Array.isArray(this.products)) {
        return []
      }
      const categories = [...new Set(this.products.map(p => p.category))].filter(Boolean)
      return categories.sort()
    },

    filteredProducts() {
      if (!this.products || !Array.isArray(this.products)) {
        return []
      }

      let filtered = this.products.filter(product => {
        const searchTermLower = this.searchTerm.toLowerCase()
        const matchesSearch = !this.searchTerm ||
            (product.name && product.name.toLowerCase().includes(searchTermLower)) ||
            (product.description && product.description.toLowerCase().includes(searchTermLower)) ||
            (product.principioAtivo && product.principioAtivo.toLowerCase().includes(searchTermLower)) ||
            (product.category && product.category.toLowerCase().includes(searchTermLower))

        const matchesCategory = !this.filters.category || product.category === this.filters.category

        // CORREÇÃO: Usa estoque ao invés de inStock
        const matchesStock = this.filters.stock === 'all' ||
            (this.filters.stock === 'in_stock' && product.estoque > 0) ||
            (this.filters.stock === 'out_of_stock' && product.estoque === 0)

        return matchesSearch && matchesCategory && matchesStock
      })

      // Ordenação
      switch (this.filters.sortBy) {
        case 'name_desc':
          filtered.sort((a, b) => (b.name || '').localeCompare(a.name || ''))
          break
        case 'price':
          filtered.sort((a, b) => (a.price || 0) - (b.price || 0))
          break
        case 'price_desc':
          filtered.sort((a, b) => (b.price || 0) - (a.price || 0))
          break
        case 'relevance':
          filtered.sort((a, b) => {
            const stockA = a.estoque || 0;
            const stockB = b.estoque || 0;
            if (stockA > 0 && stockB === 0) return -1;
            if (stockA === 0 && stockB > 0) return 1;
            return (a.name || '').localeCompare(b.name || '');
          })
          break
        default: // 'name'
          filtered.sort((a, b) => (a.name || '').localeCompare(b.name || ''))
          break
      }

      return filtered
    },

    hasActiveFilters() {
      return this.searchTerm || this.filters.category || this.filters.stock !== 'all'
    }
  },
  async mounted() {
    console.log('🚀 Componente Products montado - inicializando...')
    await this.initializeComponent()
  },
  methods: {
    ...mapActions(['fetchProducts', 'addToCart']),

    getCategoryIcon(category) {
      const icons = {
        'Medicamentos': 'fa-pills',
        'Cosméticos': 'fa-pump-soap',
        'Higiene': 'fa-shower',
        'Vitaminas': 'fa-leaf',
        'Maternidade': 'fa-baby'
      }
      return icons[category] || 'fa-box'
    },

    async initializeComponent() {
      this.loading = true
      this.error = null

      try {
        // Se houver busca por IA ou Query, o Watcher cuidará do carregamento inicial
        if (this.$route.query.ai || this.$route.query.q) {
          console.log('🔍 Busca ativa detectada, aguardando carregamento específico...');
          this.trackPageView();
          return;
        }

        console.log('📦 Buscando todos os produtos (sem filtros)...')
        await this.fetchProducts()
        console.log('✅ Produtos carregados com sucesso')
        console.log('📊 Total de produtos:', this.products?.length || 0)
        if (this.products && this.products.length > 0) {
          console.log('📋 Exemplo do primeiro produto:', this.products[0])
        }
        this.trackPageView()
      } catch (err) {
        console.error('❌ Erro ao carregar produtos:', err)
        this.error = 'Erro ao carregar produtos. Tente novamente.'
      } finally {
        this.loading = false
      }
    },

    trackPageView() {
      if (window.gtag) {
        window.gtag('event', 'page_view', {
          page_title: 'Página de Produtos',
          page_location: '/products'
        })
      }
    },

    async handleAiSearch(query) {
      this.loading = true;
      this.error = null;
      try {
        console.log('🤖 Disparando busca inteligente por IA:', query);
        const response = await this.$axios.get(`/api/produtos/busca-ia?query=${encodeURIComponent(query)}`);
        // Aqui estamos substituindo os produtos do store localmente apenas para exibição
        const mappedResults = response.data.map(p => ({
          id: p.id,
          name: p.nome,
          price: p.preco,
          category: p.categoriaNome || 'Geral',
          description: p.descricao || p.descricaoBreve || '',
          inStock: p.estoque !== null ? p.estoque > 0 : true,
          estoque: p.estoque !== null ? p.estoque : 0,
          imageUrl: p.imageUrl,
          images: [p.imageUrl || 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?q=80&w=800'],
          dosagem: p.dosagem,
          farmaciaNome: p.farmaciaNome,
          principioAtivo: p.principioAtivo,
          laboratorio: p.laboratorio,
          necessitaReceita: p.necessitaReceita,
          emPromocao: p.emPromocao,
          descontoPercentual: p.descontoPercentual,
          precoComDesconto: p.precoComDesconto
        }));
        this.$store.commit('SET_PRODUCTS', mappedResults);
        this.searchTerm = `Busca Inteligente: ${query}`;
      } catch (err) {
        console.error('❌ Erro na busca IA:', err);
        this.error = 'O assistente de IA não conseguiu processar sua busca. Tente termos simples.';
      } finally {
        this.loading = false;
      }
    },

    handleAddToCart(product) {
      this.addToCart(product);
      if (window.$toast) {
        window.$toast.addToast(`${product.name} adicionado ao carrinho!`, 'success');
      }
    },

    retryLoading() {
      this.initializeComponent()
    },

    async clearAllFilters() {
      this.searchTerm = ''
      this.filters = {
        category: '',
        sortBy: 'name',
        stock: 'all'
      }
      this.loading = true;
      await this.fetchProducts();
      this.loading = false;
    }
  }
}
</script>

<style scoped>
.products-page {
  background: var(--cf-ivory);
  min-height: 100vh;
  padding-bottom: 4rem;
}

.products-header {
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-border);
  padding: 3rem 0;
  margin-bottom: 3rem;
  position: relative;
}

.products-header h1 {
  font-family: var(--cf-serif);
  font-size: clamp(2.2rem, 4vw, 3rem);
  color: var(--cf-text-dark);
  font-weight: 400;
  margin-bottom: 0.5rem;
}

.products-header p {
  color: var(--cf-text-muted);
  font-weight: 300;
  font-size: 1.1rem;
}

.count-badge {
  background: var(--cf-green-xlight);
  color: var(--cf-green);
  padding: 0.5rem 1.2rem;
  border-radius: 100px;
  font-size: 0.75rem;
  letter-spacing: 0.05em;
  font-weight: 500;
  border: 1px solid var(--cf-green-light);
}

/* SEARCH & FILTERS */
.filters-wrapper {
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-border);
  z-index: 1000;
  position: sticky;
  top: 81px; /* Navbar (66px) + Top Strip (~15px) */
  transition: all 0.3s ease;
}

.filters-section {
  padding: 1rem 0;
}

/* RESPONSIVO (PREMIUM MOBILE REBUILD) */
@media (max-width: 991px) {
  .filters-wrapper {
    top: 60px;
    padding: 0.5rem 0;
    box-shadow: 0 4px 15px rgba(0,0,0,0.03);
  }

  .filters-section {
    padding: 0.5rem 0;
  }

  .filters-section .row {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 5px;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  .filters-section .row::-webkit-scrollbar {
    display: none;
  }

  .filters-section .col-lg-4, 
  .filters-section .col-lg-3,
  .filters-section .col-md-6 {
    flex: 0 0 auto;
    width: 200px;
    padding-right: 10px;
  }

  .form-select {
    font-size: 0.85rem;
    padding: 0.5rem 1rem;
    border-radius: 12px;
  }
}

@media (max-width: 768px) {
  .products-header { padding: 1.5rem 0; text-align: left; }
  .products-header h1 { font-size: 1.8rem; font-weight: 700; }
  
  .active-filters {
    flex-wrap: wrap;
    padding: 0.6rem;
    border-radius: 10px;
  }
  
  .filter-tag {
    font-size: 0.7rem;
    padding: 4px 8px;
    border-radius: 8px;
  }

  .products-grid .row {
    margin-left: -5px;
    margin-right: -5px;
  }

  .products-grid .col-6 {
    padding-left: 5px;
    padding-right: 5px;
  }
}
</style>