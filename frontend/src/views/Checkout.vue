<template>
  <div class="container mt-5 mb-5">
    <div class="row">
      <div class="col-md-8">
        <!-- Endereço -->
        <div class="card shadow-sm border-0 p-4 mb-4">
          <h4 class="mb-4 font-weight-bold">📍 Entrega</h4>
          <input type="text" v-model="endereco" class="form-control bg-light p-3" placeholder="Seu endereço completo">
        </div>
        <!-- Pagamento (Componente Filho) -->
        <div class="card shadow-sm border-0 p-4">
          <PaymentMethod @update-method="setMetodo" />
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm border-0 p-4 sticky-top" style="top: 20px;">
          <h4 class="mb-4 font-weight-bold">Resumo</h4>
          <div class="d-flex justify-content-between mb-4 h5 font-weight-bold">
            <span>Total</span>
            <span class="text-primary">R$ {{ cartTotal.toFixed(2) }}</span>
          </div>
          <button @click="finalizar" :disabled="loading" class="btn btn-primary btn-lg w-100 py-3 font-weight-bold shadow-sm">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Processando...' : 'FINALIZAR E PAGAR' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import { OrderService } from '@/services/orderService.js';
import PaymentMethod from '@/components/checkout/PaymentMethod.vue';

export default {
  props: ['cart'],
  components: { PaymentMethod },
  data() {
    return {
      loading: false,
      endereco: 'Rua das Flores, 123',
      metodo: 'MERCADO_PAGO',
      localCart: []
    };
  },
  computed: {
    ...mapState(['user']),
    cartTotal() {
      return this.localCart.reduce((total, item) => total + (item.price * item.quantity), 0);
    }
  },
  created() {
    if (this.cart) {
      this.localCart = JSON.parse(this.cart);
    }
  },
  methods: {
    setMetodo(m) {
      this.metodo = m;
    },

    async finalizar() {
      this.loading = true;
      try {
        const pedidoRequest = {
          usuarioId: this.user ? this.user.id : 1,
          itens: this.localCart.map(item => ({
            produtoId: item.id,
            quantidade: item.quantity || 1
          })),
          metodoPagamento: this.metodo,
          enderecoEntrega: this.endereco,
          observacoes: '',
          subtotal: this.cartTotal,
          valorFrete: 0.0,
          totalFinal: this.cartTotal
        };

        const res = await OrderService.createOrder(pedidoRequest);

        if (res.linkPagamento) {
          window.location.href = res.linkPagamento;
        } else {
          console.error("Resposta do servidor não contém o link de pagamento:", res);
          alert("Erro: Link de pagamento não gerado.");
        }
      } catch (e) {
        console.error("Erro ao finalizar o pedido:", e);
        const errorMessage = e.response?.data?.message || "Erro ao conectar com o servidor Java.";
        alert(errorMessage);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>