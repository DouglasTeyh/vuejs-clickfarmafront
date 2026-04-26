<template>
  <div class="home fade-in-up">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border cf-spinner" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>

    <div v-else>
      <!-- Hero Section — Estilo Editorial Premium -->
      <section class="hero-section">
        <div class="container h-100">
          <div class="row align-items-center h-100">
            <div class="col-lg-7">
              <span class="section-eyebrow mb-2">Bem-vindo à ClickFarma</span>
              <h1 class="hero-title mb-4">Sua saúde, cuidada com <em>excelência</em></h1>
              <p class="hero-subtitle mb-5">Encontre medicamentos, dermocosméticos e cuidados essenciais com a curadoria que você merece.</p>
              
              <div class="hero-actions">
                <router-link to="/products" class="btn btn-primary">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
                  Explorar Catálogo
                </router-link>
                <router-link to="/promotions" class="btn btn-outline-primary">
                  Ver Ofertas
                </router-link>
              </div>

              <div class="hero-stats mt-5 pt-4">
                <div class="stat-item">
                  <span class="stat-num">2h</span>
                  <span class="stat-label">Entrega Expressa</span>
                </div>
                <div class="stat-sep"></div>
                <div class="stat-item">
                  <span class="stat-num">24h</span>
                  <span class="stat-label">Apoio Farmacêutico</span>
                </div>
              </div>
            </div>

            <div class="col-lg-5 d-none d-lg-block">
              <div class="hero-visual-wrapper">
                <div class="floating-icons">
                  <div class="float-icon icon-1">💊</div>
                  <div class="float-icon icon-2">🧴</div>
                  <div class="float-icon icon-3">🌿</div>
                </div>
                <div class="hero-circle-bg"></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Categorias Section -->
      <section class="categories-section py-5">
        <div class="container py-lg-4">
          <div class="row justify-content-center text-center mb-5">
            <div class="col-lg-7">
              <h2 class="section-title">Navegue por Categoria</h2>
              <p class="section-desc">Especialidades selecionadas para facilitar sua busca pelo cuidado ideal.</p>
            </div>
          </div>
          
          <div class="categories-grid">
            <router-link v-for="(cat, i) in categories" :key="cat" to="/products" class="category-item-link">
              <div class="cf-category-card">
                <div class="category-icon-bg">
                  <span class="category-emoji">{{ getCategoryIcon(cat) }}</span>
                </div>
                <span class="category-text">{{ cat }}</span>
              </div>
            </router-link>
          </div>
        </div>
      </section>

      <!-- Features Section -->
      <section class="features-section py-5">
        <div class="container py-lg-4">
          <div class="row g-4">
            <div class="col-md-4" v-for="f in features" :key="f.title">
              <div class="cf-feature-box">
                <div class="feature-icon-blob">{{ f.icon }}</div>
                <h3 class="feature-h">{{ f.title }}</h3>
                <p class="feature-p">{{ f.text }}</p>
                <span class="feature-tag">{{ f.badge }}</span>
              </div>
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
        { icon: '🚚', title: 'Entrega Veloz', text: 'Logística inteligente para você receber em casa rapidinho.', badge: 'Expressa' },
        { icon: '🔒', title: 'Segurança Total', text: 'Dados protegidos e medicamentos com garantia de procedência.', badge: 'Certificada' },
        { icon: '👨‍⚕️', title: 'Expertise', text: 'Dúvidas? Nossa equipe técnica está pronta para ajudar.', badge: '24 Horas' }
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
/* ---- HERO SECTION ---- */
.hero-section {
  position: relative;
  height: 80vh;
  min-height: 600px;
  background: var(--cf-white);
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 40%; height: 100%;
  background: var(--cf-green-xlight);
  z-index: 1;
}

.hero-section .container {
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: clamp(2.5rem, 5vw, 4.2rem);
  color: var(--cf-text-dark);
  line-height: 1.1;
  margin-bottom: 1.5rem;
}
.hero-title em {
  font-family: var(--cf-serif);
  font-style: italic;
  color: var(--cf-green);
}

.hero-subtitle {
  font-size: 1.15rem;
  color: var(--cf-text-muted);
  max-width: 500px;
  font-weight: 300;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

/* Stats */
.hero-stats {
  display: flex;
  align-items: center;
  gap: 2rem;
}
.stat-item { display: flex; flex-direction: column; }
.stat-num { font-family: var(--cf-sans); font-size: 1.6rem; font-weight: 500; color: var(--cf-green); line-height: 1; }
.stat-label { font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.12em; color: var(--cf-text-faint); margin-top: 4px; }
.stat-sep { width: 1px; height: 30px; background: var(--cf-border-mid); }

/* Visuals */
.hero-visual-wrapper {
  position: relative;
  width: 100%; height: 400px;
  display: flex; align-items: center; justify-content: center;
}
.hero-circle-bg {
  width: 320px; height: 320px;
  background: var(--cf-green-light);
  border-radius: 50%;
  border: 1px dashed var(--cf-green-mid);
}
.floating-icons { position: absolute; inset: 0; }
.float-icon {
  position: absolute; font-size: 3rem;
  filter: drop-shadow(0 8px 15px rgba(0,0,0,0.1));
  animation: float 4s infinite ease-in-out;
}
.icon-1 { top: 20%; left: 30%; animation-delay: 0s; }
.icon-2 { top: 50%; right: 20%; animation-delay: 1s; }
.icon-3 { bottom: 20%; left: 40%; animation-delay: 2s; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

/* ---- CATEGORIES ---- */
.section-desc { color: var(--cf-text-muted); font-weight: 300; font-size: 1.05rem; }

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1.5rem;
}

.cf-category-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  padding: 1.8rem 1rem;
  text-align: center;
  transition: all 300ms var(--cf-ease);
}
.category-item-link { text-decoration: none; }

.category-icon-bg {
  width: 70px; height: 70px;
  background: var(--cf-ivory);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 1.2rem;
  transition: all 300ms var(--cf-ease);
}
.category-emoji { font-size: 2rem; }

.category-text {
  font-family: var(--cf-sans);
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--cf-text-mid);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.category-item-link:hover .cf-category-card {
  transform: translateY(-5px);
  border-color: var(--cf-green-light);
  box-shadow: var(--cf-shadow-md);
}
.category-item-link:hover .category-icon-bg {
  background: var(--cf-green-xlight);
  transform: scale(1.1);
}
.category-item-link:hover .category-text { color: var(--cf-green); }

/* ---- FEATURES ---- */
.cf-feature-box {
  background: var(--cf-white);
  padding: 2.5rem 2rem;
  border-radius: var(--cf-r-xl);
  height: 100%;
  border: 1px solid var(--cf-border);
  transition: all 300ms var(--cf-ease);
}
.feature-icon-blob {
  font-size: 2.4rem;
  margin-bottom: 1.25rem;
}
.feature-h {
  font-family: var(--cf-sans);
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--cf-text-dark);
  margin-bottom: 0.8rem;
}
.feature-p {
  font-size: 0.9rem;
  color: var(--cf-text-muted);
  line-height: 1.6;
}
.feature-tag {
  display: inline-block;
  font-size: 0.6rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--cf-green);
  background: var(--cf-green-light);
  padding: 3px 10px;
  border-radius: 100px;
  font-weight: 500;
  margin-top: 1rem;
}

.cf-feature-box:hover {
  background: var(--cf-ivory);
  border-color: var(--cf-green-light);
  box-shadow: var(--cf-shadow-sm);
}

.cf-spinner { color: var(--cf-green); }

/* ---- RESPONSIVO ---- */
@media (max-width: 991.98px) {
  .hero-section { height: auto; padding: 4rem 0; }
  .hero-section::before { display: none; }
  .hero-title { font-size: 2.8rem; }
}
</style>