<template>
  <div class="home">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>

    <div v-else>
      <!-- Hero Section -->
      <section class="hero-section text-white py-5">
        <div class="container">
          <div class="row align-items-center min-vh-50">
            <div class="col-lg-6">
              <h1 class="hero-title mb-4">Sua saúde em primeiro lugar</h1>
              <p class="hero-subtitle mb-4">Encontre os melhores medicamentos e produtos com entrega rápida e preços justos.</p>
              <div class="d-flex gap-3 flex-wrap">
                <router-link to="/products" class="btn btn-primary btn-lg px-4 py-3">🛍️ Ver Produtos</router-link>
                <router-link to="/promotions" class="btn btn-outline-light btn-lg px-4 py-3">💰 Promoções</router-link>
              </div>
            </div>
            <div class="col-lg-6 text-center mt-4 mt-lg-0">
              <div class="hero-visual">
                <div class="floating-pills">
                  <span class="pill pill-1">💊</span>
                  <span class="pill pill-2">🧴</span>
                  <span class="pill pill-3">🌡️</span>
                  <span class="pill pill-4">💊</span>
                </div>
                <div class="delivery-bike">
                  <span class="bike-icon">🚚</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Features Section -->
      <section class="features-section py-5">
        <div class="container">
          <div class="row text-center mb-5">
            <div class="col-12">
              <h2 class="section-title mb-3">Por que escolher a ClickFarma?</h2>
              <p class="section-subtitle">Tudo que você precisa para sua saúde e bem-estar</p>
            </div>
          </div>
          <div class="row g-4">
            <div class="col-md-4" v-for="f in features" :key="f.title">
              <div class="feature-card card border-0 h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon">{{ f.icon }}</span>
                </div>
                <h4 class="feature-title">{{ f.title }}</h4>
                <p class="feature-text">{{ f.text }}</p>
                <div class="mt-3">
                  <small class="feature-badge">{{ f.badge }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Categories Section -->
      <section class="categories-section py-5">
        <div class="container">
          <div class="row text-center mb-5">
            <div class="col-12">
              <h2 class="section-title mb-3">Categorias em Destaque</h2>
              <p class="section-subtitle">Encontre tudo que você precisa por categoria</p>
            </div>
          </div>
          <div class="row g-3">
            <div v-for="(cat, i) in categories" :key="cat" class="col-md-2 col-6">
              <router-link to="/products" class="text-decoration-none">
                <div class="category-card card border-0 h-100 text-center p-3">
                  <div class="category-icon mb-2">
                    <span class="category-emoji" :class="`category-${i+1}`">{{ getCategoryIcon(cat) }}</span>
                  </div>
                  <h6 class="category-name fw-semibold mb-0 small">{{ cat }}</h6>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Home',
  data() {
    return {
      loading: false,
      features: [
        { icon: '🚚', title: 'Entrega Rápida', text: 'Receba em até 2 horas na região metropolitana.', badge: '⏱️ Entrega Expressa' },
        { icon: '💳', title: 'Pagamento Seguro', text: 'Diversas formas de pagamento com total segurança.', badge: '🔒 100% Seguro' },
        { icon: '👨‍⚕️', title: 'Farmácia Confiável', text: 'Profissionais qualificados 24 horas.', badge: '📞 Atendimento Online' }
      ]
    }
  },
  computed: { ...mapGetters(['categories']) },
  methods: {
    getCategoryIcon(cat) {
      const icons = { 'Medicamentos': '💊', 'Cosméticos': '🧴', 'Higiene': '🚿', 'Vitaminas': '🌿', 'Maternidade': '👶', 'Bebês': '🍼' };
      return icons[cat] || '📦';
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&family=Inter:wght@300;400;500;600&display=swap');

.hero-section {
  background: linear-gradient(135deg, #0056A0 0%, #00B4D8 100%);
  overflow: hidden;
  position: relative;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><path fill="rgba(255,255,255,0.05)" d="M100 0L200 200H0L100 0z"/></svg>');
  background-size: 30px;
  opacity: 0.1;
}

.hero-title {
  font-family: 'Montserrat', sans-serif;
  font-size: 3.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
  background: linear-gradient(135deg, #ffffff, #e0f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.95);
}

.min-vh-50 {
  min-height: 60vh;
}

.section-title {
  font-family: 'Montserrat', sans-serif;
  font-size: 2.5rem;
  font-weight: 600;
  color: #0056A0;
  letter-spacing: -0.02em;
}

.section-subtitle {
  color: #475569;
  font-size: 1.1rem;
}

.feature-card {
  transition: all 250ms ease-in-out;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 86, 160, 0.12);
  border-color: #0056A0;
}

.feature-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(0, 86, 160, 0.1), rgba(0, 180, 216, 0.1));
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.feature-icon {
  font-size: 2.5rem;
}

.feature-title {
  font-family: 'Montserrat', sans-serif;
  font-size: 1.5rem;
  color: #0056A0;
}

.feature-text {
  color: #475569;
  line-height: 1.6;
}

.feature-badge {
  background: linear-gradient(135deg, #2E8B57, #22c55e);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.75rem;
  text-transform: uppercase;
}

.category-card {
  transition: all 250ms ease-in-out;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: white;
}

.category-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0, 86, 160, 0.12);
  border-color: #0056A0;
}

.category-emoji {
  font-size: 2.5rem;
  display: block;
}

.category-name {
  font-size: 0.95rem;
  color: #0056A0;
  font-weight: 600;
}

.hero-visual {
  position: relative;
  height: 300px;
}

.floating-pills {
  position: relative;
  height: 200px;
}

.pill {
  position: absolute;
  font-size: 2.5rem;
  animation: float 3s infinite;
}

.pill-1 { top: 20px; left: 50px; }
.pill-2 { top: 60px; right: 80px; }
.pill-3 { bottom: 40px; left: 80px; }
.pill-4 { bottom: 20px; right: 50px; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.delivery-bike {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.bike-icon {
  font-size: 4rem;
  animation: move 4s infinite;
}

@keyframes move {
  0%, 100% { transform: translateX(-10px); }
  50% { transform: translateX(10px); }
}

.btn-primary {
  background: linear-gradient(135deg, #0056A0, #00B4D8);
  color: white;
  border: none;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 86, 160, 0.4);
  color: white;
}

.btn-outline-light {
  border: 2px solid white;
  color: white;
  font-weight: 600;
}

.btn-outline-light:hover {
  background: white;
  color: #0056A0;
  transform: translateY(-2px);
}

.spinner-border.text-primary {
  color: #0056A0 !important;
}
</style>