<template>
  <div class="products-page">
    <div class="container mt-4">
      <div class="row">
        <!-- Sidebar Filtros -->
        <div class="col-lg-3 col-xl-2 mb-4 d-none d-lg-block">
          <div class="filters-sidebar">
            <div class="results-count mb-4">
              <span class="text-muted small fw-bold">{{ filteredProducts.length }} resultados</span>
            </div>

            <div class="filter-section mb-5">
              <h6 class="filter-title">Categorias</h6>
              <ul class="filter-list">
                <li :class="{ active: !filters.category }">
                  <a href="#" @click.prevent="filters.category = ''">Todas as categorias</a>
                </li>
                <li v-for="cat in categoriesList" :key="cat" :class="{ active: filters.category === cat }">
                  <a href="#" @click.prevent="filters.category = cat">{{ cat }}</a>
                </li>
              </ul>
            </div>

            <div class="filter-section mb-5">
              <h6 class="filter-title">Ordenar por</h6>
              <ul class="filter-list">
                <li :class="{ active: filters.sortBy === 'relevance' }">
                  <a href="#" @click.prevent="filters.sortBy = 'relevance'">Mais relevantes</a>
                </li>
                <li :class="{ active: filters.sortBy === 'price' }">
                  <a href="#" @click.prevent="filters.sortBy = 'price'">Menor preço</a>
                </li>
                <li :class="{ active: filters.sortBy === 'price_desc' }">
                  <a href="#" @click.prevent="filters.sortBy = 'price_desc'">Maior preço</a>
                </li>
                <li :class="{ active: filters.sortBy === 'name' }">
                  <a href="#" @click.prevent="filters.sortBy = 'name'">A-Z</a>
                </li>
              </ul>
            </div>
            
            <div class="filter-section">
              <h6 class="filter-title">Disponibilidade</h6>
              <ul class="filter-list">
                <li :class="{ active: filters.stock === 'all' }">
                  <a href="#" @click.prevent="filters.stock = 'all'">Todos os produtos</a>
                </li>
                <li :class="{ active: filters.stock === 'in_stock' }">
                  <a href="#" @click.prevent="filters.stock = 'in_stock'">Em estoque</a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Conteúdo Principal -->
        <div class="col-lg-9 col-xl-10">
          <!-- Active Filters (Mobile) -->
          <div v-if="hasActiveFilters" class="active-filters d-lg-none mb-3">
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
              <button @click="clearAllFilters" class="btn btn-sm btn-link text-danger">Limpar</button>
            </div>
          </div>

          <!-- Filtros Mobile (Apenas em telas pequenas) -->
          <div class="d-lg-none mb-4 filters-mobile-row">
            <select v-model="filters.category" class="form-select form-select-sm d-inline-block w-auto me-2">
              <option value="">Categoria</option>
              <option v-for="cat in categoriesList" :key="cat" :value="cat">{{ cat }}</option>
            </select>
            <select v-model="filters.sortBy" class="form-select form-select-sm d-inline-block w-auto">
              <option value="relevance">Relevância</option>
              <option value="price">Menor Preço</option>
              <option value="name">A-Z</option>
            </select>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border cf-spinner" style="width: 2.5rem; height: 2.5rem;" role="status"></div>
            <p class="mt-3 text-muted fw-light">Organizando catálogo...</p>
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="error-state text-center py-5">
            <div class="error-icon mb-3"><i class="fas fa-exclamation-triangle fa-3x text-warning"></i></div>
            <h4 class="mb-3">Conexão interrompida</h4>
            <p class="text-muted mb-4">{{ error }}</p>
            <button @click="retryLoading" class="btn btn-primary">Tentar novamente</button>
          </div>

          <!-- Empty State -->
          <div v-else-if="filteredProducts.length === 0" class="empty-state text-center py-5">
            <div class="empty-icon mb-3"><i class="fas fa-search fa-3x text-muted"></i></div>
            <h4 class="mb-3">Nada encontrado</h4>
            <p class="text-muted mb-4">Tente outros termos ou remova os filtros ativos.</p>
            <button @click="clearAllFilters" class="btn btn-primary rounded-pill px-4">Ver Tudo</button>
          </div>

          <!-- Products Grid -->
          <div v-else class="products-grid">
            <div class="row g-2 g-md-4">
              <div
                  v-for="product in paginatedProducts"
                  :key="product.id"
                  class="col-6 col-md-6 col-lg-4 col-xl-3"
              >
                <ProductCard :product="product" />
              </div>
            </div>

            <!-- Pagination Control -->
            <div v-if="totalPages > 1" class="pagination-wrap mt-5 d-flex justify-content-center">
              <nav aria-label="Navegação de produtos">
                <ul class="pagination pagination-premium">
                  <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <button class="page-link" @click="changePage(currentPage - 1)">
                      <i class="fas fa-chevron-left"></i>
                    </button>
                  </li>
                  <li 
                    v-for="page in visiblePages" 
                    :key="page" 
                    class="page-item" 
                    :class="{ active: currentPage === page }"
                  >
                    <button class="page-link" @click="changePage(page)">{{ page }}</button>
                  </li>
                  <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <button class="page-link" @click="changePage(currentPage + 1)">
                      <i class="fas fa-chevron-right"></i>
                    </button>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import ProductCard from '@/components/products/ProductCard.vue'
import api from '@/services/api'

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
      error: null,
      // Pagination
      currentPage: 1,
      itemsPerPage: 40
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
    },
    
    // Pagination Logic
    totalPages() {
      return Math.ceil(this.filteredProducts.length / this.itemsPerPage);
    },
    paginatedProducts() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredProducts.slice(start, end);
    },
    visiblePages() {
      const pages = [];
      const start = Math.max(1, this.currentPage - 2);
      const end = Math.min(this.totalPages, this.currentPage + 2);
      for (let i = start; i <= end; i++) pages.push(i);
      return pages;
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
        const response = await api.get(`/produtos/busca-ia?query=${encodeURIComponent(query)}`);
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
      this.currentPage = 1;
    },

    changePage(page) {
      if (page < 1 || page > this.totalPages) return;
      this.currentPage = page;
      window.scrollTo({ top: 0, behavior: 'smooth' });
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

.filters-sidebar {
  background: transparent;
  padding: 0.5rem;
  position: sticky;
  top: 100px;
}

.filter-section {
  padding-right: 1rem;
}

.filter-title {
  font-family: var(--cf-sans);
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.filter-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.filter-list li {
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
}

.filter-list a {
  text-decoration: none;
  color: #666;
  transition: all 0.2s;
  display: block;
  padding: 2px 0;
}

.filter-list a:hover {
  color: var(--cf-green);
  padding-left: 4px;
}

.filter-list li.active a {
  color: var(--cf-green);
  font-weight: 700;
  padding-left: 0;
}

.results-count {
  border-bottom: 1px solid var(--cf-border);
  padding-bottom: 1rem;
}

.filters-mobile-row {
  overflow-x: auto;
  white-space: nowrap;
  padding-bottom: 5px;
  -webkit-overflow-scrolling: touch;
}

.filter-tag {
  background: var(--cf-green-light);
  color: var(--cf-green-dark);
  font-size: 0.75rem;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.empty-state, .error-state {
  background: white;
  border-radius: 24px;
  border: 1px dashed var(--cf-border-mid);
}

@media (max-width: 768px) {
  .products-grid .row { margin-left: -5px; margin-right: -5px; }
  .products-grid .col-6 { padding-left: 5px; padding-right: 5px; }
}

/* Pagination Styling */
.pagination-premium .page-link {
  border: none;
  background: var(--cf-white);
  color: var(--cf-text-mid);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px !important;
  margin: 0 4px;
  font-weight: 700;
  box-shadow: var(--cf-shadow-sm);
  transition: all 0.3s var(--cf-ease);
}
.pagination-premium .page-item.active .page-link {
  background: var(--cf-green);
  color: white;
  box-shadow: 0 4px 15px rgba(42,92,69,0.3);
}
.pagination-premium .page-link:hover:not(.disabled) {
  background: var(--cf-green-xlight);
  color: var(--cf-green);
  transform: translateY(-2px);
}
</style>