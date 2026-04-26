<template>
  <div class="promotions-page">
    <div class="container py-lg-5 py-4">
      <div class="text-center mb-5 fade-in-up">
        <span class="section-eyebrow">Ofertas Exclusivas</span>
        <h1 class="section-title">Nossas <em>Promoções</em></h1>
        <p class="section-desc">Cuidado que cabe no seu bolso. Selecionamos as melhores ofertas para você.</p>
      </div>
      
      <div class="card cf-promo-alert mb-5 fade-in-up">
        <div class="card-body py-4 text-center">
          <i class="fas fa-magic text-gold mb-3 d-block" style="font-size: 1.5rem;"></i>
          <h5 class="fw-normal">Algoritmo de Economia em Ação</h5>
          <p class="mb-0 text-muted small">Estamos processando novas ofertas personalizadas para você. Volte em instantes!</p>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-md-4" v-for="p in promoCategories" :key="p.title">
          <div class="cf-feature-box text-center">
            <div class="promo-icon mb-3">{{ p.icon }}</div>
            <h5 class="feature-h">{{ p.title }}</h5>
            <p class="feature-p">Descontos que chegam a <strong>{{ p.discount }}</strong></p>
            <router-link to="/products" class="btn btn-sm btn-outline-primary mt-3">Ver Itens</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Promotions',
  data() {
    return {
      promoCategories: [
        { title: 'Medicamentos', icon: '💊', discount: '30% OFF' },
        { title: 'Linha Infantil', icon: '🍼', discount: '25% OFF' },
        { title: 'Dermocosméticos', icon: '🧴', discount: '40% OFF' }
      ]
    }
  }
}
</script>

<style scoped>
.promotions-page {
  min-height: 80vh;
  background: var(--cf-white);
}

.section-desc { color: var(--cf-text-muted); font-weight: 300; }

.cf-promo-alert {
  background: var(--cf-ivory);
  border: 1px dashed var(--cf-gold-mid);
  border-radius: var(--cf-r-xl);
}

.promo-icon {
  font-size: 3rem;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.05));
}

.text-gold { color: var(--cf-gold); }

.cf-feature-box {
  background: var(--cf-white);
  padding: 2.5rem 2rem;
  border-radius: var(--cf-r-xl);
  border: 1px solid var(--cf-border);
  transition: all 300ms var(--cf-ease);
}
.cf-feature-box:hover {
  transform: translateY(-5px);
  border-color: var(--cf-green-light);
  box-shadow: var(--cf-shadow-md);
}

.feature-h { font-family: var(--cf-sans); font-size: 1.4rem; font-weight: 600; color: var(--cf-text-dark); }
.feature-p { font-size: 0.95rem; color: var(--cf-text-muted); margin-bottom: 0; }
</style>
