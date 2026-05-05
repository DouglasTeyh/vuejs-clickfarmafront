<template>
  <div class="cf-product-card" :class="{ 'is-unavailable': !product.inStock }">

    <!-- Área visual do produto -->
    <div class="cf-card-image" @click="showQuickView" style="cursor: pointer;">

      <!-- Imagem ou ícone de categoria -->
      <div class="cf-product-visual">
        <img v-if="product.image || product.imageUrl" 
             :src="product.image || product.imageUrl" 
             :alt="product.name" 
             class="cf-product-img" />
        <div v-else class="cf-product-placeholder">
          <i class="fas" :class="getCategoryIcon(product.category)"></i>
        </div>
      </div>

      <!-- Badge de estoque e Promoção -->
      <div class="cf-badges">
        <span v-if="product.emPromocao" class="cf-promo-badge">
          -{{ product.descontoPercentual ? product.descontoPercentual.toFixed(0) : 0 }}% OFF
        </span>
        <span class="cf-stock-badge" :class="product.inStock ? 'badge-in' : 'badge-out'">
          {{ product.inStock ? 'Em estoque' : 'Indisponível' }}
        </span>
      </div>


    </div>

    <!-- Informações do produto -->
    <div class="cf-card-body">

      <span class="cf-category-label">{{ product.category }}</span>

      <div class="cf-card-link" @click="showQuickView" style="cursor: pointer;">
        <h3 class="cf-product-name">{{ product.name }}</h3>
        <div class="cf-product-meta mb-2">
          <span class="cf-dosage"><i class="fas fa-flask me-1"></i>{{ product.dosagem || 'N/A' }}</span>
          <span class="cf-pharmacy-name ms-2"><i class="fas fa-store me-1"></i>{{ product.farmaciaNome || 'ClickFarma' }}</span>
        </div>
        <p class="cf-product-desc">{{ product.description }}</p>
      </div>

      <!-- Preço + botão -->
      <div class="cf-card-foot">
        <div class="cf-price-wrap">
          <div v-if="product.emPromocao" class="cf-old-price">R$&nbsp;{{ formattedOldPrice }}</div>
          <span class="cf-price">R$&nbsp;{{ formattedPrice }}</span>
          <span class="cf-installment">12× de R$&nbsp;{{ installmentPrice }}</span>
        </div>

        <button
            class="cf-add-btn"
            :disabled="!product.inStock || addingToCart"
            :class="{ 'is-added': addedToCart }"
            @click.stop="handleAddToCart"
        >
          <span v-if="addingToCart" class="spinner-border spinner-border-sm"></span>
          <svg v-else-if="addedToCart" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
          </svg>
          <span>{{ shortButtonText }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'ProductCard',
  props: {
    product: { type: Object, required: true }
  },
  data() {
    return { addingToCart: false, addedToCart: false }
  },
  computed: {
    isAuthenticated() {
      return this.$store.getters.isAuthenticated;
    },
    formattedPrice() {
      const price = this.product.emPromocao ? this.product.precoComDesconto : this.product.price;
      return (price || 0).toFixed(2).replace('.', ',')
    },
    formattedOldPrice() {
      return (this.product.price || 0).toFixed(2).replace('.', ',')
    },
    installmentPrice() {
      const price = this.product.emPromocao ? this.product.precoComDesconto : this.product.price;
      return ((price || 0) / 12).toFixed(2).replace('.', ',')
    },
    shortButtonText() {
      if (this.addingToCart) return '...'
      if (this.addedToCart)  return 'OK!'
      if (!this.product.inStock) return 'Indisponível'
      return 'Adicionar'
    }
  },
  methods: {
    ...mapActions(['openQuickView']),
    getCategoryIcon(cat) {
      const icons = {
        'Medicamentos': 'fa-pills',
        'Cosméticos': 'fa-pump-soap',
        'Higiene': 'fa-shower',
        'Vitaminas': 'fa-leaf',
        'Maternidade': 'fa-baby'
      }
      return icons[cat] || 'fa-box'
    },
    showQuickView() {
      this.openQuickView(this.product)
    },
    async handleAddToCart(e) {
      e.stopPropagation()
      if (!this.isAuthenticated) {
        if (window.$toast) window.$toast.addToast('Você precisa entrar na sua conta para adicionar à sacola', 'error');
        this.$store.dispatch('openLoginModal');
        return;
      }
      if (!this.product.inStock || this.addingToCart) return
      this.addingToCart = true
      try {
        await this.$store.dispatch('addToCart', this.product)
        this.addedToCart = true
        if (window.$toast) window.$toast.addToast(`${this.product.name} adicionado ao carrinho!`, 'success');
        setTimeout(() => { this.addedToCart = false }, 2200)
      } catch (err) {
        console.error(err)
      } finally {
        this.addingToCart = false
      }
    }
  }
}
</script>

<style scoped>
/* ---- CARD ---- */
.cf-product-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: transform 250ms var(--cf-ease), box-shadow 250ms var(--cf-ease), border-color 250ms var(--cf-ease);
}
.cf-product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--cf-shadow-md);
  border-color: rgba(184,149,80,0.28);
}
.cf-product-card.is-unavailable { opacity: 0.68; }

/* ---- IMAGEM ---- */
.cf-card-image {
  display: block;
  position: relative;
  background-color: #ffffff !important;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  text-decoration: none;
}

.cf-product-visual {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 350ms var(--cf-ease);
}
.cf-product-card:hover .cf-product-visual {
  transform: scale(1.07);
}

.cf-product-icon {
  font-size: 3.8rem;
  filter: drop-shadow(0 4px 10px rgba(42,92,69,0.12));
  user-select: none;
}

.cf-product-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 1rem;
  transition: transform 350ms var(--cf-ease);
}
.cf-product-card:hover .cf-product-img {
  transform: scale(1.08);
}

/* ---- BADGES ---- */
.cf-badges {
  position: absolute;
  top: 10px;
  left: 10px;
}
.cf-stock-badge {
  font-size: 0.62rem;
  font-weight: 500;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  padding: 3px 9px;
  border-radius: 3px;
  margin-left: 5px;
}
.cf-promo-badge {
  background: var(--cf-gold);
  color: white;
  font-size: 0.62rem;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 3px;
}
.badge-in  { background: var(--cf-green-light); color: var(--cf-green); }
.badge-out { background: #F9EDED; color: var(--cf-danger); }



/* ---- CORPO ---- */
.cf-card-body {
  padding: 0.9rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 0.25rem;
}

.cf-category-label {
  font-size: 0.62rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--cf-gold);
  font-weight: 500;
}

.cf-card-link { text-decoration: none; color: inherit; }

.cf-product-name {
  font-family: var(--cf-sans);
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--cf-text-dark);
  line-height: 1.25;
  margin: 0.2rem 0 0.3rem;
  letter-spacing: -0.01em;
  transition: color 160ms var(--cf-ease);
}
.cf-card-link:hover .cf-product-name { color: var(--cf-green); }

.cf-product-placeholder {
  font-size: 3rem;
  color: var(--cf-green-light);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cf-product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 0.72rem;
  color: var(--cf-text-muted);
}

.cf-dosage, .cf-pharmacy-name {
  background: var(--cf-cream);
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.cf-product-desc {
  font-size: 0.82rem;
  line-height: 1.55;
  color: var(--cf-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5em;
  margin: 0;
}

/* ---- RODAPÉ DO CARD ---- */
.cf-card-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 0.6rem;
  margin-top: auto;
  padding-top: 0.8rem;
  border-top: 1px solid var(--cf-border);
}

.cf-price-wrap {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.cf-old-price {
  font-size: 0.75rem;
  color: var(--cf-text-faint);
  text-decoration: line-through;
  margin-bottom: -2px;
}

.cf-price {
  font-family: var(--cf-sans);
  font-size: 1.45rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  line-height: 1;
}

.cf-installment {
  font-size: 0.7rem;
  color: var(--cf-text-faint);
  font-weight: 300;
}

/* ---- BOTÃO ---- */
.cf-add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--cf-green);
  color: white;
  border: none;
  border-radius: var(--cf-r-md);
  padding: 0.55rem 0.9rem;
  font-size: 0.72rem;
  font-weight: 400;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 220ms var(--cf-ease);
  font-family: var(--cf-sans);
  white-space: nowrap;
  flex-shrink: 0;
}
.cf-add-btn:hover:not(:disabled) {
  background: var(--cf-green-dark);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(42,92,69,0.24);
}
.cf-add-btn:disabled {
  background: var(--cf-cream);
  color: var(--cf-text-faint);
  cursor: not-allowed;
  transform: none;
}
.cf-add-btn.is-added {
  background: var(--cf-gold);
}
.cf-add-btn.is-added:hover:not(:disabled) {
  background: var(--cf-gold-hover);
  box-shadow: 0 4px 12px rgba(184,149,80,0.24);
}

/* ---- RESPONSIVO (PREMIUM MOBILE REBUILD) ---- */
@media (max-width: 768px) {
  .cf-card-image {
    aspect-ratio: auto;
    height: 140px; /* Altura fixa para manter proporção em grids de 2 colunas */
  }

  .cf-product-visual {
    height: 100%;
  }

  .cf-product-img {
    padding: 0.5rem;
  }

  .cf-card-body {
    padding: 0.8rem;
    gap: 0.2rem;
  }

  .cf-product-name {
    font-size: 0.9rem;
    line-height: 1.2;
    min-height: 2.4em; /* Mantém alinhamento mesmo se o nome for curto */
    margin-bottom: 0.5rem;
  }

  .cf-product-meta {
    display: none; /* Esconde meta infos para economizar espaço no card mobile */
  }

  .cf-product-desc {
    display: none; /* Esconde descrição no mobile */
  }

  .cf-price {
    font-size: 1.15rem;
    font-weight: 600;
  }

  .cf-old-price {
    font-size: 0.65rem;
  }

  .cf-installment {
    font-size: 0.6rem;
  }

  .cf-card-foot {
    padding-top: 0.6rem;
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .cf-add-btn {
    width: 100%;
    justify-content: center;
    padding: 0.6rem;
    font-size: 0.75rem;
    font-weight: 600;
  }
}

@media (max-width: 380px) {
  .cf-card-image { height: 120px; }
  .cf-price { font-size: 1rem; }
  .cf-add-btn span { font-size: 0.65rem; }
}
</style>