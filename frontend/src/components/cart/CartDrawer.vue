<template>
  <div class="cart-drawer-overlay" :class="{ 'active': isOpen }" @click.self="$emit('close')">
    <div class="cart-drawer" :class="{ 'active': isOpen }">
      <!-- Cabeçalho do Drawer -->
      <div class="drawer-header p-4 d-flex justify-content-between align-items-center">
        <h4 class="mb-0 fw-bold"><i class="fas fa-shopping-bag me-2"></i>Minha Sacola</h4>
        <button class="btn-close-drawer" @click="$emit('close')">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <!-- Endereço de Entrega (Resumo) -->
      <div class="drawer-address px-4 py-3 border-bottom">
        <AddressSelector />
      </div>

      <!-- Conteúdo do Carrinho -->
      <div class="drawer-content p-4">
        <div v-if="cart.length === 0" class="empty-cart text-center py-5">
          <div class="empty-icon mb-3">
            <i class="fas fa-shopping-cart fa-3x text-muted"></i>
          </div>
          <h5>Sua sacola está vazia</h5>
          <p class="text-muted small">Adicione produtos para começar.</p>
          <button class="btn btn-primary btn-sm mt-3" @click="$emit('close')">Continuar Comprando</button>
        </div>

        <div v-else class="cart-items-list">
          <div v-for="item in cart" :key="item.id" class="drawer-item mb-4 pb-4 border-bottom">
            <div class="d-flex gap-3">
              <div class="item-img-wrap">
                <img :src="item.image || item.imageUrl" :alt="item.name" class="rounded">
              </div>
              <div class="item-info flex-grow-1">
                <h6 class="item-name mb-1">{{ item.name }}</h6>
                <div class="item-price-row d-flex justify-content-between align-items-center">
                  <span class="fw-bold text-green">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                  
                  <div class="qty-controls d-flex align-items-center">
                    <button class="btn-qty" @click="updateQuantity(item.id, item.quantity - 1)" :disabled="item.quantity <= 1">
                      <i class="fas fa-minus"></i>
                    </button>
                    <span class="qty-value mx-2">{{ item.quantity }}</span>
                    <button class="btn-qty" @click="updateQuantity(item.id, item.quantity + 1)">
                      <i class="fas fa-plus"></i>
                    </button>
                  </div>
                </div>
                <button class="btn-remove-item mt-2" @click="removeItem(item.id)">
                  <i class="far fa-trash-alt me-1"></i> Remover
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rodapé do Drawer -->
      <div v-if="cart.length > 0" class="drawer-footer p-4 border-top mt-auto">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <span class="text-muted">Subtotal</span>
          <h4 class="mb-0 fw-bold text-green">R$ {{ cartTotal.toFixed(2).replace('.', ',') }}</h4>
        </div>
        <router-link to="/checkout" class="btn btn-primary w-100 py-3 fw-bold" @click="$emit('close')">
          Finalizar Pedido <i class="fas fa-arrow-right ms-2"></i>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'
import AddressSelector from '@/components/cart/AddressSelector.vue'

export default {
  name: 'CartDrawer',
  components: { AddressSelector },
  props: {
    isOpen: { type: Boolean, default: false }
  },
  computed: {
    ...mapState(['cart']),
    ...mapGetters(['cartTotal'])
  },
  methods: {
    ...mapActions(['removeFromCart', 'updateCartQuantity']),
    updateQuantity(productId, quantity) {
      this.updateCartQuantity({ productId, quantity })
    },
    removeItem(productId) {
      this.removeFromCart(productId)
    }
  }
}
</script>

<style scoped>
.cart-drawer-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  z-index: 2100; visibility: hidden; opacity: 0; transition: all 0.3s;
  backdrop-filter: blur(4px);
}
.cart-drawer-overlay.active { visibility: visible; opacity: 1; }

.cart-drawer {
  position: absolute; top: 0; right: -400px; width: 400px; height: 100%;
  background: white; box-shadow: -5px 0 25px rgba(0,0,0,0.1);
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex; flex-direction: column;
}
.cart-drawer.active { right: 0; }

.btn-close-drawer {
  background: none; border: none; font-size: 1.2rem; color: var(--cf-text-faint);
  transition: color 0.2s;
}
.btn-close-drawer:hover { color: var(--cf-danger); }

.item-img-wrap img { width: 70px; height: 70px; object-fit: cover; background: #f8f9fa; }

.item-name { font-size: 0.95rem; font-weight: 600; color: var(--cf-text-dark); }
.text-green { color: var(--cf-green); }

.qty-controls { background: #f0f7f4; border-radius: 6px; padding: 2px; }
.btn-qty { 
  width: 24px; height: 24px; border: none; background: white; 
  border-radius: 4px; color: var(--cf-green); font-size: 0.7rem;
  display: flex; align-items: center; justify-content: center;
}
.btn-qty:disabled { opacity: 0.5; }

.btn-remove-item {
  background: none; border: none; color: var(--cf-text-faint); 
  font-size: 0.75rem; padding: 0;
}
.btn-remove-item:hover { color: var(--cf-danger); }

.drawer-content { overflow-y: auto; flex-grow: 1; }

/* ---- RESPONSIVO (PREMIUM MOBILE REBUILD) ---- */
@media (max-width: 576px) {
  .cart-drawer { 
    width: 100%; 
    right: -100%; 
    border-top-left-radius: 0;
  }
  
  .drawer-header {
    padding: 1.2rem 1.5rem !important;
  }

  .drawer-header h4 {
    font-size: 1.2rem;
  }

  .btn-close-drawer {
    font-size: 1.5rem; /* Maior para toque */
    padding: 0.5rem;
  }

  .drawer-content {
    padding: 1rem !important;
  }

  .item-img-wrap img {
    width: 60px;
    height: 60px;
  }

  .item-name {
    font-size: 0.9rem;
    margin-bottom: 0.5rem !important;
  }

  .item-price-row {
    flex-wrap: nowrap;
    gap: 0.5rem;
  }

  .text-green {
    font-size: 1.1rem;
  }

  .qty-controls {
    padding: 4px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
  }

  .btn-qty {
    width: 32px; /* Maior área de toque no mobile */
    height: 32px;
    font-size: 0.9rem;
    background: white;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  }

  .qty-value {
    min-width: 20px;
    text-align: center;
    font-weight: 600;
  }

  .btn-remove-item {
    font-size: 0.8rem;
    padding: 0.5rem 0;
  }

  .drawer-footer {
    padding: 1.5rem !important;
    padding-bottom: max(1.5rem, env(safe-area-inset-bottom)) !important; /* Suporte para iPhone safe area */
  }

  .drawer-footer .btn {
    padding: 1rem;
    font-size: 1.1rem;
    border-radius: 100px;
  }
}
</style>
