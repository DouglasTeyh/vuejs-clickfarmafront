<template>
  <div class="checkout-page">
    <div class="container py-lg-5 py-4">
      
      <div class="mb-5 fade-in-up">
        <span class="section-eyebrow">Finalização</span>
        <h1 class="section-title">Finalizar <em>Pedido</em></h1>
      </div>

      <div class="row">
        <div class="col-md-8">
          <!-- Endereço -->
          <div class="mb-4">
            <AddressSelector @loaded="enderecoSelecionado = $event" />
          </div>
          <!-- Pagamento -->
          <div class="cf-checkout-card">
            <div class="card-header-cf">
               <i class="fa-solid fa-credit-card"></i>
               <span>Método de Pagamento</span>
            </div>
            <div class="p-4">
              <PaymentMethod @update-method="setMetodo" />
            </div>
          </div>
        </div>

        <!-- Direita: Resumo Fixo -->
        <div class="col-lg-4 fade-in-up" style="animation-delay: 0.1s">
          <div class="sticky-top" style="top: 100px;">
            <div class="cf-summary-card">
              <h4 class="summary-title mb-4">Resumo do Pedido</h4>
              
              <div class="summary-items mb-4">
                <div v-for="item in cart" :key="item.id" class="checkout-item-mini mb-3">
                  <div class="d-flex justify-content-between align-items-center">
                    <span class="item-name-mini text-truncate" style="max-width: 180px;">{{ item.name }}</span>
                    <span class="item-qty-mini small text-muted">x{{ item.quantity }}</span>
                    <span class="item-price-mini fw-bold">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                  </div>
                </div>
              </div>

              <div class="cf-divider mb-4"></div>

              <div class="total-box d-flex justify-content-between align-items-end mb-4">
                <div class="total-label-wrap">
                  <span class="total-eyebrow">Total a pagar</span>
                  <h4 class="total-label mb-0">Total Final</h4>
                </div>
                <h4 class="total-amount mb-0">R$ {{ cartTotal.toFixed(2).replace('.', ',') }}</h4>
              </div>

              <button @click="finalizar" :disabled="loading || !enderecoValido" class="btn btn-primary btn-lg w-100 py-3 mt-4">
                <i v-if="loading" class="fa-solid fa-spinner fa-spin me-2"></i>
                <i v-else class="fa-solid fa-lock me-2"></i>
                {{ loading ? 'Processando...' : 'Confirmar Pedido' }}
              </button>
              <small v-if="!enderecoValido" class="text-danger d-block mt-2 text-center">
                Preencha os dados de entrega para continuar.
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { OrderService } from '@/services/orderService.js';
import PaymentMethod from '@/components/checkout/PaymentMethod.vue';
import AddressSelector from '@/components/cart/AddressSelector.vue';
import api from '@/services/api';

export default {
  name: 'Checkout',
  components: { PaymentMethod, AddressSelector },
  data() {
    return {
      loading: false,
      enderecoSelecionado: null,
      metodo: 'MERCADO_PAGO'
    };
  },
  computed: {
    ...mapState(['user', 'cart', 'authToken']),
    isAuthenticated() { return !!this.authToken; },
    enderecoValido() {
      return Boolean(this.enderecoSelecionado);
    },
    cartTotal() {
      return this.cart.reduce((total, item) => total + (item.price * item.quantity), 0);
    }
  },
  async mounted() {
    if (this.isAuthenticated) {
      await api.get('/enderecos');
    }
  },
  created() {
  },
  methods: {
    setMetodo(m) { this.metodo = m; },
    async finalizar() {
      this.loading = true;
      try {
        const padrao = this.enderecoSelecionado;

        if (!padrao) {
          alert('Por favor, cadastre um endereço de entrega.');
          this.loading = false;
          return;
        }

        const enderecoString = `${padrao.logradouro}, ${padrao.numero} · ${padrao.bairro} · ${padrao.cidade}/${padrao.estado} · CEP ${padrao.cep}`;

        const pedidoRequest = {
          usuarioId: this.user ? this.user.id : 1,
          itens: this.cart.map(item => ({ produtoId: item.id, quantidade: item.quantity || 1 })),
          metodoPagamento: this.metodo,
          enderecoEntrega: enderecoString,
          observacoes: '',
          subtotal: this.cartTotal,
          valorFrete: 0.0,
          totalFinal: this.cartTotal
        };

        const res = await OrderService.createOrder(pedidoRequest);
        
        // Armazenar ID para a tela de sucesso
        if (res.id) localStorage.setItem('ultimoPedidoId', res.id);
        if (res.codigo) localStorage.setItem('ultimoCodigoPedido', res.codigo);

        if (res.linkPagamento) {
          window.location.href = res.linkPagamento;
        } else {
          this.$router.push('/sucesso-pagamento');
        }
      } catch (e) {
        console.error(e);
        alert("Erro ao finalizar pedido.");
      } finally {
        this.loading = false;
      }
    },
    formatCEP(v) {
      if (!v) return '';
      v = v.replace(/\D/g, '');
      v = v.replace(/^(\d{5})(\d)/, '$1-$2');
      return v.substring(0, 9);
    }
  }
}
</script>

<style scoped>
.checkout-page { background: var(--cf-white); min-height: 90vh; }

.cf-checkout-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  overflow: hidden;
}
.card-header-cf {
  background: var(--cf-green-xlight);
  padding: 1.25rem 2rem;
  border-bottom: 1px solid var(--cf-border);
  color: var(--cf-green);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.input-group-cf { position: relative; }
.input-group-cf i { position: absolute; left: 1.25rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); }

/* SUMMARY CARD */
.cf-summary-card {
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  padding: 2.5rem 2rem;
}
.summary-title { font-family: var(--cf-sans); font-size: 1.6rem; font-weight: 600; color: var(--cf-text-dark); }
.summary-item { display: flex; justify-content: space-between; gap: 1rem; }
.item-name { font-size: 0.85rem; color: var(--cf-text-mid); }
.item-price { font-size: 0.85rem; font-weight: 500; font-family: var(--cf-sans); color: var(--cf-text-dark); }

.total-eyebrow { font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-gold); font-weight: 600; display: block; }
.total-label { font-family: var(--cf-sans); font-size: 1.25rem; font-weight: 600; color: var(--cf-text-dark); }
.total-amount { font-family: var(--cf-sans); font-size: 2rem; font-weight: 600; color: var(--cf-green); }

.text-green { color: var(--cf-green); }

/* ---- MOBILE RESPONSIVENESS (PREMIUM REBUILD) ---- */
@media (max-width: 991px) {
  .checkout-page { padding-top: 0; }
  .container { padding-left: 1.25rem; padding-right: 1.25rem; }
  
  .section-title { font-size: 2rem; }
  
  .sticky-top {
    position: relative !important;
    top: 0 !important;
    margin-top: 2rem;
  }
  
  .cf-summary-card {
    padding: 1.5rem;
  }
  
  .total-amount {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .section-title { font-size: 1.6rem; }
  .card-header-cf { padding: 1rem 1.5rem; }
}
</style>
