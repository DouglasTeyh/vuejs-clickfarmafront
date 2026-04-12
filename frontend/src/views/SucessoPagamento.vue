<template>
  <div class="container mt-5">
    <div class="card text-center">
      <div class="card-body">
        <div class="mb-4">
          <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
        </div>
        <h1 class="card-title">Pagamento Confirmado!</h1>
        <p class="card-text">Seu pedido foi processado com sucesso.</p>
        <p class="card-text">Número do pedido: <strong>#{{ pedidoId }}</strong></p>
        <router-link to="/meus-pedidos" class="btn btn-primary">
          Ver meus pedidos
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pedidoId: localStorage.getItem('ultimoPedidoId') || ''
    }
  },
  mounted() {
    // Limpar carrinho
    localStorage.removeItem('cart');
    localStorage.removeItem('ultimoPedidoId');
    this.$store.dispatch('clearCart');
  }
}
</script>