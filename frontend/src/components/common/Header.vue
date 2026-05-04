<template>
  <header>
    <!-- Faixa informativa top — verde muito suave -->
    <div class="top-strip">
      <div class="container d-flex justify-content-between align-items-center">
        <span>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            style="margin-right:5px">
            <rect x="1" y="3" width="15" height="13" />
            <path d="m16 8 6 2v8H16" />
            <circle cx="5.5" cy="18.5" r="2.5" />
            <circle cx="18.5" cy="18.5" r="2.5" />
          </svg>
          Frete grátis acima de R$&nbsp;150
        </span>
        <span class="d-none d-md-inline">Atendimento 24h · (81) 99818-9999</span>
        <span>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            style="margin-right:5px">
            <polyline points="20 6 9 17 4 12" />
          </svg>
          Produto original garantido
        </span>
      </div>
    </div>

    <!-- Navbar principal -->
    <nav class="navbar navbar-expand-lg navbar-main">
      <div class="container">

        <!-- Logo -->
        <router-link to="/" class="navbar-brand me-2 me-lg-4">
          <div class="brand-logo">
            <img src="/images/Logotipo.svg" alt="ClickFarma" class="brand-img">
          </div>
        </router-link>

        <!-- Ações Mobile (Sempre Visíveis) -->
        <div class="mobile-actions d-flex d-lg-none align-items-center gap-2 ms-auto me-2">
          <button class="cf-icon-btn" @click="toggleMobileSearch" aria-label="Buscar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" />
              <path d="m16.5 16.5 4 4" />
            </svg>
          </button>
          <router-link :to="{ name: 'Checkout', params: { cart: JSON.stringify(cart) } }"
            class="cf-icon-btn position-relative" aria-label="Carrinho">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <path d="M16 10a4 4 0 0 1-8 0" />
            </svg>
            <span v-if="cartItemsCount > 0" class="cf-cart-badge">{{ cartItemsCount }}</span>
          </router-link>
        </div>

        <!-- Toggle mobile -->
        <button class="navbar-toggler cf-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span></span><span></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarNav">
          <!-- Links de navegação (Lado Esquerdo) -->
          <ul class="navbar-nav me-auto gap-1">
            <li class="nav-item">
              <router-link to="/promotions" class="nav-link cf-nav-link">
                Promoções
                <span class="promo-pill">-20% OFF</span>
              </router-link>
            </li>
          </ul>

          <!-- Busca Estática (Centralizada) -->
          <div class="search-wrapper position-relative mx-auto d-none d-lg-block">
            <div class="search-container-static">
              <input 
                type="text" 
                class="form-control cf-search-input-static" 
                placeholder="Busque por nome ou sintoma..."
                v-model="searchQuery" 
                @keyup.enter="handleSearch"
                @input="handleInput"
                @focus="showSuggestions = true"
              >
              <button class="search-icon-btn" @click="handleSearch">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="7" />
                  <path d="m16.5 16.5 4 4" />
                </svg>
              </button>
            </div>

            <!-- Dropdown de Sugestões -->
            <div v-if="showSuggestions && (suggestions.length > 0 || loadingSuggestions)" class="search-suggestions shadow-sm">
              <div v-if="loadingSuggestions" class="p-3 text-center small text-muted">
                <span class="spinner-border spinner-border-sm me-2"></span> Buscando...
              </div>
              <ul v-else class="list-unstyled mb-0">
                <li 
                  v-for="(sug, index) in suggestions" 
                  :key="index" 
                  class="suggestion-item"
                  @click="selectSuggestion(sug)"
                >
                  <i class="fas fa-search me-2 text-muted small"></i> {{ sug }}
                </li>
                <li class="suggestion-item ai-search-item" @click="handleSearch">
                  <i class="fas fa-magic me-2 text-primary small"></i> 
                  Busca Inteligente: "<strong>{{ searchQuery }}</strong>"
                </li>
              </ul>
            </div>
          </div>

          <!-- Ações Desktop (Lado Direito) -->
          <div class="navbar-actions ms-auto d-none d-lg-flex align-items-center gap-3">
            <!-- Carrinho Desktop -->
            <router-link :to="{ name: 'Checkout', params: { cart: JSON.stringify(cart) } }"
              class="cf-icon-btn cf-cart position-relative" aria-label="Carrinho">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                <line x1="3" y1="6" x2="21" y2="6" />
                <path d="M16 10a4 4 0 0 1-8 0" />
              </svg>
              <span v-if="cartItemsCount > 0" class="cf-cart-badge">{{ cartItemsCount }}</span>
            </router-link>

            <!-- Divisor vertical -->
            <div class="nav-divider d-none d-lg-block"></div>

            <!-- Não autenticado -->
            <template v-if="!isAuthenticated">
              <button @click="openLoginModal" class="cf-btn-ghost">Entrar</button>
              <button @click="openRegisterModal" class="cf-btn-solid">Cadastrar</button>
            </template>

            <!-- Autenticado -->
            <div v-else class="dropdown">
              <button class="cf-user-btn dropdown-toggle" type="button" data-bs-toggle="dropdown">
                <div class="cf-avatar">{{ userInitials }}</div>
                <span class="d-none d-xl-inline cf-user-name">{{ user?.name?.split(' ')[0] || 'Olá' }}</span>
              </button>
              <ul class="dropdown-menu dropdown-menu-end cf-dropdown">
                <li class="cf-dropdown-user">
                  <span class="cf-dropdown-eyebrow">Minha conta</span>
                  <span class="cf-dropdown-fullname">{{ user?.name || 'Usuário' }}</span>
                </li>
                <li>
                  <hr class="cf-dd-divider">
                </li>
                <li><router-link to="/profile" class="dropdown-item cf-dd-item"><i class="fas fa-user-circle me-2"></i>Meu Perfil</router-link></li>
                <li><router-link to="/orders" class="dropdown-item cf-dd-item"><i class="fas fa-shopping-bag me-2"></i>Meus Pedidos</router-link></li>
                <li><router-link to="/track-order" class="dropdown-item cf-dd-item"><i class="fas fa-truck me-2"></i>Rastrear Pedido</router-link></li>
                <li>
                  <hr class="cf-dd-divider">
                </li>
                <li>
                  <button class="dropdown-item cf-dd-item cf-dd-danger" @click="handleLogout">
                    Sair
                  </button>
                </li>
              </ul>
            </div>
          </div>

          <!-- Ações Mobile dentro do Collapse (Login/Cadastro) -->
          <div class="d-lg-none mt-3 border-top pt-3">
            <template v-if="!isAuthenticated">
              <button @click="openLoginModal" class="cf-btn-ghost w-100 mb-2">Entrar</button>
              <button @click="openRegisterModal" class="cf-btn-solid w-100">Cadastrar</button>
            </template>
            <template v-else>
              <router-link to="/profile" class="nav-link"><i class="fas fa-user-circle me-2"></i>Meu Perfil</router-link>
              <router-link to="/orders" class="nav-link"><i class="fas fa-shopping-bag me-2"></i>Meus Pedidos</router-link>
              <router-link to="/track-order" class="nav-link"><i class="fas fa-truck me-2"></i>Rastrear Pedido</router-link>
              <button class="nav-link text-danger border-0 bg-transparent" @click="handleLogout"><i class="fas fa-sign-out-alt me-2"></i>Sair</button>
            </template>
          </div>
        </div>

        <!-- Barra de Busca Mobile (Aparece ao clicar na lupa) -->
        <div v-if="mobileSearchOpen" class="mobile-search-bar d-lg-none">
          <div class="container">
            <div class="search-container-static w-100">
              <input type="text" class="form-control cf-search-input-static" placeholder="Buscar produtos..."
                v-model="searchQuery" @keyup.enter="handleSearch" ref="mobileSearchInput">
              <button class="search-icon-btn" @click="handleSearch">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="7" />
                  <path d="m16.5 16.5 4 4" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  </header>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
export default {
  name: 'Header',
  data() {
    return {
      searchQuery: '',
      mobileSearchOpen: false,
      suggestions: [],
      showSuggestions: false,
      loadingSuggestions: false
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'cartItemsCount', 'user', 'cart']),
    userInitials() {
      const name = this.user?.name || ''
      return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || 'U'
    }
  },
  methods: {
    ...mapActions(['logout', 'openLoginModal', 'openRegisterModal']),
    async handleLogout() { await this.logout(); this.$router.push('/') },
    
    handleInput() {
      if (this.searchQuery.length >= 3) {
        this.debouncedFetchSuggestions();
      } else {
        this.suggestions = [];
      }
    },

    async fetchSuggestions() {
      if (this.searchQuery.length < 3) return;
      this.loadingSuggestions = true;
      try {
        const response = await this.$axios.get(`/api/produtos/sugestoes?query=${this.searchQuery}`);
        this.suggestions = response.data;
      } catch (error) {
        console.error('Erro ao buscar sugestões:', error);
      } finally {
        this.loadingSuggestions = false;
      }
    },

    debouncedFetchSuggestions() {
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.fetchSuggestions();
      }, 300);
    },

    selectSuggestion(sug) {
      this.searchQuery = sug;
      this.showSuggestions = false;
      this.handleSearch();
    },

    handleSearch() {
      if (!this.searchQuery.trim()) return;
      
      const query = this.searchQuery.trim();
      this.showSuggestions = false;
      
      // Lógica de IA se for semântica
      const isSemantic = query.split(' ').length > 2 || /dor|febre|gripe|remedio|para/i.test(query);

      if (isSemantic) {
        this.$router.push({ path: '/products', query: { ai: query } });
      } else {
        this.$router.push({ path: '/products', query: { q: query } });
      }
      
      this.searchQuery = '';
      this.mobileSearchOpen = false;
    },

    toggleMobileSearch() {
      this.mobileSearchOpen = !this.mobileSearchOpen;
      if (this.mobileSearchOpen) {
        this.$nextTick(() => {
          if (this.$refs.mobileSearchInput) this.$refs.mobileSearchInput.focus();
        });
      }
    },

    handleClickOutside(event) {
      if (!this.$el.contains(event.target)) {
        this.showSuggestions = false;
      }
    }
  },
  created() {
    this.searchTimeout = null;
    if (typeof window !== 'undefined') {
      window.addEventListener('click', this.handleClickOutside);
    }
  },
  beforeUnmount() {
    if (typeof window !== 'undefined') {
      window.removeEventListener('click', this.handleClickOutside);
    }
  }
}
</script>

<style scoped>
/* ---- STICKY HEADER ---- */
header {
  position: fixed;
  top: 0;
  z-index: 1001;
  width: 100%;
  background: var(--cf-white);
}

/* ---- TOP STRIP ---- */
.top-strip {
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-border);
  color: var(--cf-green);
  font-size: 0.68rem;
  font-weight: 400;
  letter-spacing: 0.09em;
  padding: 0.45rem 0;
}

/* ---- NAVBAR ---- */
.navbar-main {
  background: var(--cf-white);
  border-bottom: none;
  padding: 0;
  min-height: 66px;
}

/* ---- LOGO ---- */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.brand-img {
  max-height: 56px;
  width: auto;
  display: block;
}

/* ---- NAV LINKS ---- */
.cf-nav-link {
  font-family: var(--cf-sans);
  font-size: 0.78rem;
  font-weight: 400;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--cf-text-muted) !important;
  padding: 0.45rem 0.85rem !important;
  border-radius: var(--cf-r-md);
  transition: all 180ms var(--cf-ease);
  display: flex;
  align-items: center;
  gap: 5px;
}

.cf-nav-link:hover {
  color: var(--cf-green) !important;
  background: var(--cf-green-xlight);
}

.cf-nav-link.router-link-active {
  color: var(--cf-green) !important;
  background: var(--cf-green-light);
}

.promo-pill {
  font-size: 0.58rem;
  font-weight: 500;
  letter-spacing: 0.08em;
  background: var(--cf-gold);
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  line-height: 1.2;
}

/* ---- SEARCH STATIC ---- */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-md);
  margin-top: 8px;
  z-index: 1050;
  max-height: 350px;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.suggestion-item {
  padding: 0.75rem 1.2rem;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s;
  color: var(--cf-text-dark);
}

.suggestion-item:hover {
  background: var(--cf-ivory);
}

.ai-search-item {
  border-top: 1px solid var(--cf-border);
  background: var(--cf-green-xlight);
  color: var(--cf-green);
}

.ai-search-item:hover {
  background: var(--cf-green-light);
}

.search-wrapper {
  width: 440px;
  max-width: 570px;
  margin: 0 1.5rem;
}

.search-container-static {
  position: relative;
  display: flex;
  align-items: center;
}

.cf-search-input-static {
  width: 100%;
  padding: 0.6rem 2.8rem 0.6rem 1.2rem;
  background: var(--cf-ivory);
  border: 1.5px solid var(--cf-border);
  border-radius: 100px;
  /* Estilo mais moderno e arredondado */
  font-size: 0.9rem;
  transition: all 250ms ease;
  color: var(--cf-text-dark);
}

.cf-search-input-static:focus {
  background: white;
  border-color: var(--cf-green);
  box-shadow: 0 4px 12px rgba(42, 92, 69, 0.08);
  outline: none;
}

.search-icon-btn {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  color: var(--cf-green);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  cursor: pointer;
  transition: transform 0.2s;
}

.search-icon-btn:hover {
  transform: scale(1.1);
}

.mobile-search-bar {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: white;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--cf-border);
  box-shadow: var(--cf-shadow-sm);
  z-index: 999;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-10px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.cf-icon-btn {

  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: var(--cf-r-md);
  color: var(--cf-text-muted);
  cursor: pointer;
  transition: all 160ms var(--cf-ease);
  text-decoration: none;
}

.cf-icon-btn:hover {
  background: var(--cf-cream);
  color: var(--cf-green);
}

.cf-cart {
  position: relative;
}

.cf-cart-badge {
  position: absolute;
  top: 3px;
  right: 3px;
  background: var(--cf-green);
  color: white;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  font-size: 0.6rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid var(--cf-white);
}

.nav-divider {
  width: 1px;
  height: 22px;
  background: var(--cf-border-mid);
  margin: 0 4px;
}

/* ---- AUTH BUTTONS ---- */
.cf-btn-ghost {
  font-family: var(--cf-sans);
  font-size: 0.74rem;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: var(--cf-text-dark);
  background: transparent;
  border: 1px solid var(--cf-border-mid);
  border-radius: var(--cf-r-md);
  padding: 0.48rem 1.1rem;
  text-decoration: none;
  transition: all 180ms var(--cf-ease);
  white-space: nowrap;
}

.cf-btn-ghost:hover {
  border-color: var(--cf-green);
  color: var(--cf-green);
  background: var(--cf-green-xlight);
}

.cf-btn-solid {
  font-family: var(--cf-sans);
  font-size: 0.74rem;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: white;
  background: var(--cf-green);
  border: none;
  border-radius: var(--cf-r-md);
  padding: 0.48rem 1.1rem;
  text-decoration: none;
  transition: all 180ms var(--cf-ease);
  white-space: nowrap;
}

.cf-btn-solid:hover {
  background: var(--cf-green-dark);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(42, 92, 69, 0.20);
}

/* ---- USER MENU ---- */
.cf-user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--cf-green-xlight);
  border: 1px solid var(--cf-green-light);
  border-radius: 100px;
  padding: 0.3rem 0.75rem 0.3rem 0.35rem;
  cursor: pointer;
  transition: all 180ms var(--cf-ease);
  color: var(--cf-green);
}

.cf-user-btn:hover {
  background: var(--cf-green-light);
  border-color: rgba(42, 92, 69, 0.25);
}

.cf-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--cf-green);
  color: white;
  font-size: 0.65rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.04em;
  flex-shrink: 0;
}

.cf-user-name {
  font-size: 0.8rem;
  color: var(--cf-green);
  font-weight: 400;
}

/* ---- DROPDOWN ---- */
.cf-dropdown {
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  box-shadow: var(--cf-shadow-md);
  padding: 0.4rem;
  min-width: 185px;
  background: var(--cf-white);
  margin-top: 6px !important;
}

.cf-dropdown-user {
  padding: 0.55rem 0.7rem 0.4rem;
}

.cf-dropdown-eyebrow {
  display: block;
  font-size: 0.62rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--cf-text-faint);
}

.cf-dropdown-fullname {
  display: block;
  font-family: var(--cf-sans);
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  margin-top: 2px;
}

.cf-dd-item {
  font-size: 0.84rem;
  color: var(--cf-text-muted);
  padding: 0.55rem 0.7rem;
  border-radius: var(--cf-r-md);
  transition: all 150ms var(--cf-ease);
  font-family: var(--cf-sans);
}

.cf-dd-item:hover {
  background: var(--cf-ivory);
  color: var(--cf-text-dark);
}

.cf-dd-danger {
  color: var(--cf-danger) !important;
}

.cf-dd-danger:hover {
  background: #FEF2F2 !important;
}

.cf-dd-divider {
  border: none;
  border-top: 1px solid var(--cf-border);
  margin: 0.25rem 0;
}

/* ---- TOGGLER MOBILE ---- */
.cf-toggler {
  border: none;
  background: none;
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  border-radius: var(--cf-r-sm);
}

.cf-toggler span {
  display: block;
  width: 21px;
  height: 1.5px;
  background: var(--cf-text-dark);
  border-radius: 1px;
  transition: all 250ms var(--cf-ease);
}

.cf-toggler:focus {
  box-shadow: none;
}

/* ---- RESPONSIVO ---- */
@media (max-width: 991.98px) {
  .navbar-collapse {
    background: var(--cf-white);
    border-top: 1px solid var(--cf-border);
    padding: 1rem 0;
    margin-top: 0.5rem;
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    z-index: 1000;
    box-shadow: var(--cf-shadow-md);
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }

  .navbar-actions {
    padding-top: 1rem;
    border-top: 1px solid var(--cf-border);
    margin-top: 1rem;
    flex-direction: column;
    align-items: stretch !important;
    gap: 12px !important;
  }

  .search-wrapper {
    width: 100%;
    margin: 1rem 0;
    max-width: none;
  }

  .cf-search-input.expanded {
    width: 100%;
  }

  .search-container {
    width: 100%;
  }

  .cf-btn-ghost,
  .cf-btn-solid {
    width: 100%;
    text-align: center;
  }

  .cf-user-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 576px) {
  .top-strip {
    font-size: 0.6rem;
  }

  .brand-img {
    max-height: 44px;
  }
}
</style>