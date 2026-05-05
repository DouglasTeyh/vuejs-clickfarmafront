<template>
  <div class="success-page">
    <div class="container h-100 d-flex align-items-center justify-content-center">
      <div class="success-card text-center fade-in-up">
        <div class="success-animation mb-4">
          <div class="checkmark-circle">
            <div class="background"></div>
            <div class="checkmark draw"></div>
          </div>
        </div>
        
        <h1 class="premium-title">Pagamento <em>Confirmado!</em></h1>
        <p class="premium-subtitle mb-5">
          Sua transação foi processada com sucesso. <br>
          Estamos preparando tudo com muito carinho.
        </p>

        <div class="order-info-glass mb-5">
          <span class="info-label">Número do Pedido</span>
          <h4 class="info-val">#{{ codigoPedido || pedidoId || 'N/A' }}</h4>
        </div>

        <div class="redirect-info">
          <p class="text-muted small">Redirecionando para seus pedidos em <strong>{{ countdown }}s</strong>...</p>
          <div class="progress-bar-wrap">
            <div class="progress-bar-fill" :style="{ width: progress + '%' }"></div>
          </div>
        </div>

        <div class="d-flex gap-3 justify-content-center mt-5">
          <router-link to="/orders" class="btn btn-primary btn-lg rounded-pill px-5">
            Ir Agora <i class="fas fa-arrow-right ms-2"></i>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SucessoPagamento',
  data() {
    return {
      pedidoId: localStorage.getItem('ultimoPedidoId') || '',
      codigoPedido: localStorage.getItem('ultimoCodigoPedido') || '',
      countdown: 4,
      progress: 0,
      timer: null
    }
  },
  mounted() {
    // Limpar carrinho e flags de checkout
    localStorage.removeItem('cart');
    this.$store.dispatch('clearCart');
    
    // Iniciar contagem regressiva para redirecionamento
    this.startCountdown();
  },
  methods: {
    startCountdown() {
      const totalTime = 4000; // 4s
      const interval = 100;
      let elapsed = 0;

      this.timer = setInterval(() => {
        elapsed += interval;
        this.progress = (elapsed / totalTime) * 100;
        
        if (elapsed % 1000 === 0) {
          this.countdown--;
        }

        if (elapsed >= totalTime) {
          clearInterval(this.timer);
          this.$router.push('/orders');
        }
      }, interval);
    }
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer);
  }
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  background: var(--cf-ivory);
  display: flex;
  align-items: center;
}

.success-card {
  background: white;
  padding: 4rem;
  border-radius: 40px;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.08);
  max-width: 600px;
  width: 100%;
}

.premium-title {
  font-family: var(--cf-serif);
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--cf-text-dark);
  margin-bottom: 1rem;
}
.premium-title em { font-style: italic; color: var(--cf-green); }

.premium-subtitle {
  font-size: 1.1rem;
  color: var(--cf-text-muted);
  line-height: 1.6;
}

.order-info-glass {
  background: var(--cf-green-xlight);
  padding: 1.5rem;
  border-radius: 20px;
  border: 1px solid var(--cf-green-light);
}
.info-label {
  display: block;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--cf-green);
  font-weight: 700;
  margin-bottom: 0.5rem;
}
.info-val {
  margin: 0;
  font-family: var(--cf-sans);
  font-weight: 800;
  color: var(--cf-green-dark);
}

.progress-bar-wrap {
  width: 100%;
  height: 4px;
  background: #f1f5f9;
  border-radius: 10px;
  overflow: hidden;
  margin-top: 10px;
}
.progress-bar-fill {
  height: 100%;
  background: var(--cf-green);
  transition: width 0.1s linear;
}

/* Checkmark Animation */
.checkmark-circle {
  width: 100px; height: 100px;
  position: relative; margin: 0 auto;
}
.background {
  width: 100px; height: 100px;
  border-radius: 50%; background: var(--cf-green);
  animation: pulse 2s infinite;
}
.checkmark {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%) rotate(-45deg);
}
.checkmark.draw:after {
  animation-duration: 800ms;
  animation-timing-function: ease;
  animation-name: checkmark;
  transform: scaleX(-1) rotate(135deg);
}
.checkmark:after {
  opacity: 1; height: 40px; width: 20px;
  transform-origin: left top;
  border-right: 5px solid white;
  border-top: 5px solid white;
  content: ''; display: block;
}

@keyframes checkmark {
  0% { height: 0; width: 0; opacity: 1; }
  20% { height: 0; width: 20px; opacity: 1; }
  40% { height: 40px; width: 20px; opacity: 1; }
  100% { height: 40px; width: 20px; opacity: 1; }
}
@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(42,92,69, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 20px rgba(42,92,69, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(42,92,69, 0); }
}

@media (max-width: 576px) {
  .success-card { padding: 2rem; border-radius: 0; box-shadow: none; }
  .premium-title { font-size: 2rem; }
}
</style>
