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
          <div v-for="item in cart" :key="item.id" class="drawer-item mb-4">
            <div class="item-card-premium">
              <div class="item-img-wrap">
                <img :src="item.image || item.imageUrl" :alt="item.name">
              </div>
              <div class="item-details">
                <div class="d-flex justify-content-between align-items-start mb-1">
                  <h6 class="item-name">{{ item.name }}</h6>
                  <button class="btn-remove-compact" @click="removeItem(item.id)" title="Remover">
                    <i class="far fa-trash-alt"></i>
                  </button>
                </div>
                <div class="item-price-row">
                  <span class="item-price">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                  <div class="qty-stepper">
                    <button class="step-btn" @click="updateQuantity(item.id, item.quantity - 1)" :disabled="item.quantity <= 1">
                      <i class="fas fa-minus"></i>
                    </button>
                    <span class="step-val">{{ item.quantity }}</span>
                    <button class="step-btn" @click="updateQuantity(item.id, item.quantity + 1)">
                      <i class="fas fa-plus"></i>
                    </button>
                  </div>
                </div>
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

.item-img-wrap img { width: 80px; height: 80px; object-fit: contain; background: #fff; border-radius: 12px; border: 1px solid #f1f5f9; }

.item-card-premium { display: flex; gap: 1rem; background: #fff; padding: 10px; border-radius: 16px; transition: all 0.2s; }
.item-card-premium:hover { background: #f8fafc; }

.item-details { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.item-name { font-size: 0.85rem; font-weight: 600; color: #1e293b; margin: 0; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.item-price-row { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.item-price { font-size: 0.95rem; font-weight: 700; color: var(--cf-green); }

.qty-stepper { display: flex; align-items: center; background: #f1f5f9; border-radius: 100px; padding: 2px; }
.step-btn { width: 28px; height: 28px; border-radius: 50%; border: none; background: white; color: #64748b; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; transition: all 0.2s; }
.step-btn:hover:not(:disabled) { background: var(--cf-green); color: white; }
.step-val { min-width: 28px; text-align: center; font-size: 0.85rem; font-weight: 700; color: #1e293b; }

.btn-remove-compact { background: none; border: none; color: #94a3b8; font-size: 0.9rem; padding: 4px; transition: color 0.2s; }
.btn-remove-compact:hover { color: #ef4444; }

.drawer-footer .btn { border-radius: 100px; }

/* ---- RESPONSIVO (PREMIUM MOBILE REBUILD) ---- */
@media (max-width: 576px) {
  .cart-drawer { width: 100%; right: -100%; }
  .item-img-wrap img { width: 70px; height: 70px; }
}
</style>
