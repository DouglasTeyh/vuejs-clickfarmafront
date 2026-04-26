<template>
  <div class="cart-page">
    <div class="container py-lg-5 py-4">
      <div class="text-center mb-5 fade-in-up">
        <span class="section-eyebrow">Seu Pedido</span>
        <h1 class="section-title">Meu <em>Carrinho</em></h1>
      </div>

      <!-- Carrinho vazio -->
      <div v-if="cart.length === 0" class="fade-in-up">
        <EmptyCart />
      </div>

      <!-- Carrinho com itens -->
      <div v-else class="row g-4">
        <div class="col-lg-8 fade-in-up">
          <!-- Consultor de IA -->
          <div class="mb-4">
            <CartAIAdvisor
                :cartItems="cart"
                :cartTotal="cartTotal"
            />
          </div>

          <div class="cart-items-list">
            <CartItem
                v-for="item in cart"
                :key="item.id"
                :item="item"
                @update-quantity="updateQuantity"
                @remove-item="removeFromCart"
            />
          </div>
        </div>

        <!-- Resumo do pedido -->
        <div class="col-lg-4 fade-in-up" style="animation-delay: 0.15s">
          <div class="sticky-top" style="top: 100px;">
            <OrderSummary
                :items-count="cartItemsCount"
                :total="cartTotal"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'
import EmptyCart from '@/components/cart/EmptyCart.vue'
import CartItem from '@/components/cart/CartItem.vue'
import OrderSummary from '@/components/cart/OrderSummary.vue'
import CartAIAdvisor from '@/components/cart/CartAIAdvisor.vue'

export default {
  name: 'CartPage',
  components: {
    EmptyCart,
    CartItem,
    OrderSummary,
    CartAIAdvisor
  },
  computed: {
    ...mapState(['cart']),
    ...mapGetters(['cartItemsCount', 'cartTotal'])
  },
  methods: {
    ...mapActions(['removeFromCart', 'updateCartQuantity']),

    updateQuantity(productId, quantity) {
      const validQuantity = Math.max(1, quantity)
      this.updateCartQuantity({ productId, quantity: validQuantity })
    }
  }
}
</script>

<style scoped>
.cart-page {
  background: var(--cf-white);
  min-height: 80vh;
}

.cart-items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>