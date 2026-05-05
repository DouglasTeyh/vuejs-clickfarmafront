<template>
  <div id="app">
    <!-- Header e chatbot APENAS fora dos painéis -->
    <template v-if="!isDashboardRoute">
      <Header />
      <main class="main-content" :class="{ 'blur-bg': isAuthModalOpen }">
        <router-view />
      </main>
      
      <!-- Overlay para o Chatbot (Blur + Darken) -->
      <transition name="fade">
        <div v-if="isChatOpen" class="chat-overlay" @click="toggleChat"></div>
      </transition>

      <div v-show="!isChatOpen" class="gemini-floating-btn" @click="toggleChat">
        <i class="fa-solid fa-robot"></i>
      </div>
      
      <transition name="drawer">
        <div v-if="isChatOpen" class="gemini-modal">
          <GeminiChat @close="toggleChat" />
        </div>
      </transition>
      <ProductQuickView
        :isOpen="isQuickViewOpen"
        :product="quickViewProduct"
        @close="closeQuickView"
      />
      <AuthModal 
        :isOpen="isAuthModalOpen"
        :initialMode="authModalMode"
        @close="closeAuthModal"
      />
      <CartDrawer 
        :isOpen="isCartDrawerOpen" 
        @close="closeCartDrawer" 
      />
      <NotificationToast ref="toast" />
      <Footer />
    </template>

    <!-- Dashboards: sem header, sem footer, sem chatbot -->
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script>
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import GeminiChat from '@/components/gemini/GeminiChat.vue'
import ProductQuickView from '@/components/products/ProductQuickView.vue'
import AuthModal from '@/components/auth/AuthModal.vue'
import CartDrawer from '@/components/cart/CartDrawer.vue'
import NotificationToast from '@/components/common/NotificationToast.vue'
import { mapState, mapActions } from 'vuex'

export default {
  name: 'App',
  components: { Header, Footer, GeminiChat, ProductQuickView, AuthModal, NotificationToast, CartDrawer },
  data() {
    return { isChatOpen: false };
  },
  mounted() {
    // Expoe o toast globalmente
    window.$toast = this.$refs.toast;
    // Verifica status de autenticação ao carregar
    this.checkAuthStatus();
  },
  computed: {
    ...mapState(['isQuickViewOpen', 'quickViewProduct', 'isAuthModalOpen', 'authModalMode', 'isCartDrawerOpen']),
    isDashboardRoute() {
      const path = this.$route.path;
      return path.startsWith('/admin') || 
             path.startsWith('/pharmacy') || 
             path.startsWith('/courier') || 
             path.startsWith('/dashboard') || 
             path.startsWith('/orders') ||
             path.startsWith('/tracking') ||
             path === '/addresses' ||
             path === '/profile';
    }
  },
  methods: {
    ...mapActions(['closeQuickView', 'closeAuthModal', 'closeCartDrawer', 'checkAuthStatus']),
    toggleChat() { this.isChatOpen = !this.isChatOpen; }
  }
}
</script>

<style>
body { background-color: var(--cf-ivory) !important; }
.main-content { 
  min-height: calc(100vh - 160px); 
  background: var(--cf-ivory);
  padding-top: 91px; /* Offset for fixed header (66px + 25px) */
  transition: filter 0.3s ease;
}

.main-content.blur-bg {
  filter: blur(8px);
  pointer-events: none;
  user-select: none;
}

.gemini-floating-btn {
  position: fixed; bottom: 25px; right: 25px;
  width: 68px; height: 68px;
  background: var(--cf-green); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: var(--cf-shadow-md);
  transition: all 300ms var(--cf-ease); 
  z-index: 2000; /* Abaixo do CartDrawer (2100) */
  color: white; font-size: 1.8rem; border: 4px solid var(--cf-white);
}
.gemini-floating-btn:hover {
  transform: scale(1.08) translateY(-3px);
  background: var(--cf-green-dark); box-shadow: var(--cf-shadow-lg);
}

.gemini-modal {
  position: fixed; top: 0; right: 0; bottom: 0;
  width: 420px; height: 100vh; z-index: 2050;
  box-shadow: -10px 0 30px rgba(0,0,0,0.1);
  overflow: hidden;
  background: var(--cf-white);
}

/* Transição do Drawer */
.drawer-enter-active, .drawer-leave-active {
  transition: all 400ms var(--cf-ease);
}
.drawer-enter-from, .drawer-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* Overlay do Chat */
.chat-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.1);
  backdrop-filter: blur(8px) brightness(0.6);
  z-index: 2040;
  cursor: pointer;
}

/* Transição de Fade para o Overlay */
.fade-enter-active, .fade-leave-active {
  transition: opacity 400ms var(--cf-ease);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .gemini-modal { width: 100%; height: 100%; top: 0; right: 0; }
}
</style>