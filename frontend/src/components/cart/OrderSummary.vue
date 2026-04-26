<template>
  <div class="order-summary-card">
    <h4 class="summary-title mb-4">Resumo do Pedido</h4>

    <!-- BARRA DE FRETE GRÁTIS -->
    <div class="shipping-nudge mb-4" :class="{ 'is-free': ganhouFrete }">
      <div class="d-flex justify-content-between mb-2">
        <span class="nudge-text">
          <i class="fa-solid" :class="ganhouFrete ? 'fa-truck-fast' : 'fa-truck-arrow-right'"></i>
          {{ ganhouFrete ? 'Frete Grátis Garantido!' : `Faltam R$ ${faltaQuanto.toFixed(2).replace('.', ',')} para Frete Grátis` }}
        </span>
        <span class="nudge-percent">{{ porcentagem }}%</span>
      </div>
      <div class="cf-progress">
        <div
            class="cf-progress-inner"
            :style="{ width: porcentagem + '%' }"
        ></div>
      </div>
    </div>

    <!-- VALORES DETALHADOS -->
    <div class="summary-list mb-4">
      <div class="summary-row d-flex justify-content-between mb-3">
        <span class="summary-label">Subtotal ({{ itemsCount }} itens)</span>
        <span class="summary-val">R$ {{ total.toFixed(2).replace('.', ',') }}</span>
      </div>

      <div class="summary-row d-flex justify-content-between mb-3">
        <span class="summary-label">Frete Estimado</span>
        <span class="summary-val" :class="{ 'text-green': ganhouFrete }">
          {{ ganhouFrete ? 'GRÁTIS' : 'R$ ' + valorFretePadrao.toFixed(2).replace('.', ',') }}
        </span>
      </div>

      <div class="cf-divider my-4"></div>

      <div class="d-flex justify-content-between align-items-end mt-3">
        <div class="total-label-wrap">
          <span class="total-eyebrow">Valor total</span>
          <h4 class="total-label mb-0">Total Final</h4>
        </div>
        <h4 class="total-amount mb-0">R$ {{ totalFinal.toFixed(2).replace('.', ',') }}</h4>
      </div>
    </div>

    <!-- BOTÃO DE AÇÃO -->
    <router-link
        to="/checkout"
        class="btn btn-primary btn-checkout w-100 py-3"
        :class="{ 'disabled': itemsCount === 0 }"
    >
      Finalizar Compra
      <i class="fa-solid fa-arrow-right ms-2 mt-1"></i>
    </router-link>
    
    <div class="text-center mt-3">
      <span class="secure-checkout text-muted">
        <i class="fa-solid fa-shield-halved me-1"></i>
        Checkout 100% Seguro
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderSummary',
  props: {
    itemsCount: { type: Number, required: true },
    total: { type: Number, required: true }
  },
  data() {
    return {
      threshold: 150.00,
      valorFretePadrao: 15.00
    }
  },
  computed: {
    faltaQuanto() {
      const valor = this.threshold - this.total;
      return valor > 0 ? valor : 0;
    },
    porcentagem() {
      const p = (this.total / this.threshold) * 100;
      return Math.min(p, 100).toFixed(0);
    },
    ganhouFrete() {
      return this.total >= this.threshold;
    },
    totalFinal() {
      return this.ganhouFrete ? this.total : this.total + this.valorFretePadrao;
    }
  }
}
</script>

<style scoped>
.order-summary-card {
  padding: 2rem;
  background: var(--cf-ivory);
  border-radius: var(--cf-r-xl);
  border: 1px solid var(--cf-border);
}

.summary-title { font-family: var(--cf-sans); font-size: 1.5rem; font-weight: 600; color: var(--cf-text-dark); }

.shipping-nudge {
  padding: 1rem;
  background: var(--cf-white);
  border-radius: var(--cf-r-md);
  border: 1px solid var(--cf-border);
}
.nudge-text { font-size: 0.72rem; font-weight: 500; color: var(--cf-text-mid); }
.nudge-percent { font-size: 0.75rem; font-weight: 600; color: var(--cf-green); }

.cf-progress {
  height: 6px;
  background: var(--cf-green-light);
  border-radius: 100px;
  overflow: hidden;
}
.cf-progress-inner {
  height: 100%;
  background: var(--cf-green);
  transition: width 0.6s var(--cf-ease);
}
.is-free .cf-progress-inner { background: var(--cf-gold); }
.text-green { color: var(--cf-green); font-weight: 600; }

.summary-label { font-size: 0.88rem; color: var(--cf-text-muted); }
.summary-val { font-size: 0.95rem; font-weight: 500; color: var(--cf-text-dark); font-family: var(--cf-sans); }

.total-eyebrow { font-size: 0.62rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-gold); display: block; font-weight: 600; }
.total-label { font-family: var(--cf-sans); font-size: 1.2rem; font-weight: 600; }
.total-amount { font-family: var(--cf-sans); font-size: 1.8rem; font-weight: 600; color: var(--cf-green); }

.btn-checkout { font-size: 0.85rem; letter-spacing: 0.08em; display: flex; align-items: center; justify-content: center; }
.secure-checkout { font-size: 0.68rem; letter-spacing: 0.05em; opacity: 0.7; }

.disabled { pointer-events: none; opacity: 0.4; filter: grayscale(1); }
</style>
