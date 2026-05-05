<template>
  <div class="cf-pharmacy-shell">
    <!-- Overlay mobile -->
    <div v-if="sidebarOpen && isMobile" class="cf-sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- ═══ SIDEBAR PREMIUM ═══ -->
    <aside class="cf-sidebar" :class="{ 'sidebar-open': sidebarOpen, 'collapsed': isCollapsed && !isMobile }">
      <!-- Logo Section -->
      <div class="sidebar-header">
        <div class="brand-wrap">
          <div class="brand-logo">
            <i class="fas fa-hospital-user"></i>
          </div>
          <div class="brand-meta" v-if="!isCollapsed || isMobile">
            <h1 class="brand-name">Click<span>Farma</span></h1>
            <span class="brand-tag">Painel da Unidade</span>
          </div>
        </div>
        <button class="collapse-btn d-none d-lg-flex" @click="isCollapsed = !isCollapsed">
          <i class="fas" :class="isCollapsed ? 'fa-chevron-right' : 'fa-chevron-left'"></i>
        </button>
      </div>

      <div class="sidebar-content cf-hide-scrollbar">
        <nav class="sidebar-nav">
          <!-- Visão Geral -->
          <router-link to="/pharmacy/dashboard" class="nav-link" :class="{ active: $route.path === '/pharmacy/dashboard' }" @click="handleNavClick">
            <div class="link-icon"><i class="fas fa-chart-pie"></i></div>
            <span class="link-text" v-if="!isCollapsed || isMobile">Painel de Controle</span>
            <div class="link-hover-pill"></div>
          </router-link>

          <h6 class="nav-label" v-if="!isCollapsed || isMobile">Operacional</h6>
          <router-link to="/pharmacy/orders" class="nav-link" :class="{ active: $route.path === '/pharmacy/orders' }" @click="handleNavClick">
            <div class="link-icon">
              <i class="fas fa-receipt"></i>
              <span class="badge-dot" v-if="pedidosPendentes > 0"></span>
            </div>
            <span class="link-text" v-if="!isCollapsed || isMobile">Gestão de Pedidos</span>
            <div class="link-hover-pill"></div>
          </router-link>

          <h6 class="nav-label" v-if="!isCollapsed || isMobile">Inventário</h6>
          <router-link to="/pharmacy/products" class="nav-link" :class="{ active: $route.path === '/pharmacy/products' }" @click="handleNavClick">
            <div class="link-icon"><i class="fas fa-boxes-stacked"></i></div>
            <span class="link-text" v-if="!isCollapsed || isMobile">Meus Produtos</span>
            <div class="link-hover-pill"></div>
          </router-link>

          <h6 class="nav-label" v-if="!isCollapsed || isMobile">Administrativo</h6>
          <router-link to="/pharmacy/financial" class="nav-link" :class="{ active: $route.path === '/pharmacy/financial' }" @click="handleNavClick">
            <div class="link-icon"><i class="fas fa-wallet"></i></div>
            <span class="link-text" v-if="!isCollapsed || isMobile">Financeiro</span>
            <div class="link-hover-pill"></div>
          </router-link>
          <router-link to="/pharmacy/settings" class="nav-link" :class="{ active: $route.path === '/pharmacy/settings' }" @click="handleNavClick">
            <div class="link-icon"><i class="fas fa-sliders"></i></div>
            <span class="link-text" v-if="!isCollapsed || isMobile">Configurações</span>
            <div class="link-hover-pill"></div>
          </router-link>
        </nav>
      </div>

      <!-- Pharmacy Profile Section -->
      <div class="sidebar-footer">
        <div class="user-profile-card">
          <div class="user-avatar-wrap">
            <img :src="farmaciaLogo || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" class="user-avatar">
            <div class="status-indicator online"></div>
          </div>
          <div class="user-details" v-if="!isCollapsed || isMobile">
            <p class="user-name">{{ farmaciaNome || 'Farmácia' }}</p>
            <p class="user-role">Parceiro Oficial</p>
          </div>
          <button @click="handleLogout" class="logout-btn" title="Sair do Painel">
            <i class="fas fa-power-off"></i>
          </button>
        </div>
      </div>
    </aside>

    <!-- ═══ MAIN CONTENT AREA ═══ -->
    <main class="cf-main">
      <!-- Top Navigation Bar -->
      <header class="cf-topbar shadow-sm">
        <div class="topbar-left">
          <button @click="sidebarOpen = !sidebarOpen" class="mobile-toggle d-lg-none">
            <i class="fas fa-bars-staggered"></i>
          </button>
          
          <div class="page-identity">
            <nav class="cf-breadcrumb d-none d-md-flex">
              <router-link to="/pharmacy/dashboard" class="bc-item">Unidade</router-link>
              <i class="fas fa-chevron-right bc-sep"></i>
              <span class="bc-item active">{{ pageTitle }}</span>
            </nav>
            <h2 class="topbar-page-title">{{ pageTitle }}</h2>
          </div>
        </div>

        <div class="topbar-right">
          <div class="global-tools">
            <button class="tool-icon-btn">
              <i class="far fa-bell"></i>
              <span class="notif-badge" v-if="pedidosPendentes > 0"></span>
            </button>
            <div class="divider"></div>
            <div class="pharmacy-badge">
              <i class="fas fa-check-circle text-success me-2"></i>
              <span class="status-label d-none d-md-block">Terminal Autorizado</span>
            </div>
          </div>
        </div>
      </header>

      <!-- View Wrapper -->
      <section class="cf-content">
        <router-view v-slot="{ Component }">
          <transition name="view-fade" mode="out-in">
            <div :key="$route.path">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>


<script>
import api from '@/services/api';

export default {
  name: 'PharmacyLayout',
  data() {
    return {
      sidebarOpen: false,
      isCollapsed: false,
      isMobile: false,
      userName: 'Farmacêutico',
      farmaciaNome: '',
      farmaciaLogo: '',
      pedidosPendentes: 0,
      pollingInterval: null
    };
  },
  computed: {
    pageTitle() {
      const routes = {
        '/pharmacy/dashboard': 'Central de Inteligência',
        '/pharmacy/products':  'Gestão de Inventário',
        '/pharmacy/orders':    'Fluxo de Pedidos',
        '/pharmacy/financial': 'Tesouraria & Repasses',
        '/pharmacy/settings':  'Configurações da Unidade'
      };
      return routes[this.$route.path] || 'ClickFarma Terminal';
    }
  },
  async mounted() {
    this.checkMobile();
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.userName = user.nome || 'Farmacêutico';
    await this.fetchFarmaciaInfo(user.email);
    await this.carregarBadge();
    this.pollingInterval = setInterval(() => this.carregarBadge(), 30000);
    window.addEventListener('resize', this.checkMobile);
  },
  beforeUnmount() {
    clearInterval(this.pollingInterval);
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 1024;
      if (!this.isMobile && this.sidebarOpen) this.sidebarOpen = false;
    },
    async fetchFarmaciaInfo(email) {
      try {
        const { data } = await api.get('/farmacias');
        const f = data.find(x => x.email === email);
        if (f) {
          this.farmaciaNome = f.nome;
          this.farmaciaLogo = f.fotoUrl;
        }
      } catch (err) { console.error(err); }
    },
    async carregarBadge() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data: farmacias } = await api.get('/farmacias');
        const f = farmacias.find(x => x.email === user.email);
        if (f) {
          const res = await api.get(`/pedidos/farmacia/${f.id}`);
          this.pedidosPendentes = res.data.filter(p =>
            p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO'
          ).length;
        }
      } catch (err) { console.error(err); }
    },
    handleNavClick() {
      if (this.isMobile) this.sidebarOpen = false;
    },
    handleLogout() {
      if(confirm('Deseja encerrar sua sessão com segurança?')) {
        localStorage.clear();
        this.$router.push('/login');
      }
    }
  }
};
</script>


<style scoped>
/* ═══ Shell & Base ═══ */
.cf-pharmacy-shell {
  position: fixed;
  inset: 0;
  display: flex;
  overflow: hidden;
  background: var(--cf-ivory);
  font-family: var(--cf-sans);
  color: var(--cf-text-dark);
}

/* ═══ Sidebar Premium ═══ */
.cf-sidebar {
  width: 280px;
  background: var(--cf-green-dark);
  height: 100vh;
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  transition: all 0.4s var(--cf-ease);
  z-index: 1050;
  box-shadow: 10px 0 30px rgba(0,0,0,0.15);
}
.cf-sidebar.collapsed { width: 88px; }

.sidebar-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.brand-wrap { display: flex; align-items: center; gap: 1rem; overflow: hidden; }
.brand-logo {
  width: 42px; height: 42px;
  background: var(--cf-gold);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 1.1rem;
  box-shadow: 0 4px 15px rgba(184,149,80,0.4);
  flex-shrink: 0;
}
.brand-name {
  font-family: var(--cf-serif);
  font-size: 1.4rem;
  font-weight: 600;
  color: #fff;
  margin: 0;
  white-space: nowrap;
}
.brand-name span { color: var(--cf-gold); }
.brand-tag { font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.15em; color: rgba(255,255,255,0.4); display: block; }

.collapse-btn {
  background: rgba(255,255,255,0.05); border: none;
  width: 28px; height: 28px; border-radius: 8px;
  color: rgba(255,255,255,0.3); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.collapse-btn:hover { background: var(--cf-gold); color: #fff; }

.sidebar-content { flex: 1; overflow-y: auto; padding: 1.5rem 1rem; }
.nav-group { margin-bottom: 2rem; }
.nav-label { font-size: 0.65rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.15em; color: rgba(255,255,255,0.2); margin: 1.5rem 0 0.75rem 1rem; }

.nav-link {
  display: flex; align-items: center; gap: 1rem; padding: 0.8rem 1rem;
  color: rgba(255,255,255,0.5); text-decoration: none;
  border-radius: 14px; transition: all 0.3s var(--cf-ease);
  position: relative; margin-bottom: 0.25rem;
}
.link-icon { width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; position: relative; }
.nav-link:hover { background: rgba(255,255,255,0.05); color: #fff; }
.nav-link.active { background: rgba(184,149,80,0.15); color: var(--cf-gold); }
.nav-link.active .link-hover-pill { position: absolute; left: -10px; width: 4px; height: 20px; background: var(--cf-gold); border-radius: 0 4px 4px 0; box-shadow: 2px 0 10px var(--cf-gold); }
.link-text { font-size: 0.9rem; font-weight: 500; white-space: nowrap; }

.badge-dot { position: absolute; top: -2px; right: -2px; width: 8px; height: 8px; background: #ef4444; border: 2px solid var(--cf-green-dark); border-radius: 50%; box-shadow: 0 0 10px #ef4444; }

.sidebar-footer { padding: 1.5rem 1rem; border-top: 1px solid rgba(255,255,255,0.05); background: rgba(0,0,0,0.2); }
.user-profile-card { background: rgba(255,255,255,0.03); border-radius: 18px; padding: 0.75rem; display: flex; align-items: center; gap: 0.85rem; }
.user-avatar-wrap { position: relative; }
.user-avatar { width: 42px; height: 42px; background: #fff; border-radius: 12px; object-fit: cover; border: 2px solid var(--cf-gold); }
.status-indicator { position: absolute; bottom: -2px; right: -2px; width: 12px; height: 12px; border-radius: 50%; border: 2px solid var(--cf-green-dark); }
.status-indicator.online { background: #22c55e; }
.user-name { font-size: 0.85rem; font-weight: 700; color: #fff; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 120px; }
.user-role { font-size: 0.65rem; color: rgba(255,255,255,0.4); margin: 0; }
.logout-btn { background: none; border: none; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; color: rgba(255,255,255,0.2); cursor: pointer; transition: all 0.2s; }
.logout-btn:hover { color: #ff6b6b; }

/* ═══ Main Content ═══ */
.cf-main { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  height: 100vh;
  min-width: 0; 
}
.cf-topbar { height: 80px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 2rem; z-index: 1000; position: sticky; top: 0; }
.topbar-left { display: flex; align-items: center; gap: 2rem; }
.mobile-toggle { background: var(--cf-ivory); border: 1px solid var(--cf-border); width: 44px; height: 44px; border-radius: 12px; color: var(--cf-text-dark); cursor: pointer; }
.cf-breadcrumb { gap: 0.6rem; align-items: center; margin-bottom: 0.2rem; }
.bc-item { font-size: 0.72rem; color: var(--cf-text-faint); text-decoration: none; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.bc-sep { font-size: 0.55rem; color: var(--cf-text-faint); opacity: 0.5; }
.bc-item.active { color: var(--cf-gold); }
.topbar-page-title { font-family: var(--cf-serif); font-size: 1.6rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }

.topbar-right { display: flex; align-items: center; gap: 2rem; }
.global-tools { display: flex; align-items: center; gap: 1.25rem; }
.tool-icon-btn { width: 40px; height: 40px; border-radius: 12px; background: #fff; border: 1px solid var(--cf-border); color: var(--cf-text-muted); cursor: pointer; position: relative; transition: all 0.2s; }
.tool-icon-btn:hover { border-color: var(--cf-green); color: var(--cf-green); background: var(--cf-ivory-light); }
.notif-badge { position: absolute; top: 8px; right: 8px; width: 8px; height: 8px; background: #ef4444; border: 2px solid #fff; border-radius: 50%; }
.divider { width: 1px; height: 30px; background: var(--cf-border); }
.pharmacy-badge { display: flex; align-items: center; gap: 0.6rem; padding: 0.5rem 0.8rem; background: var(--cf-green-xlight); border-radius: 50px; }
.status-label { font-size: 0.65rem; font-weight: 800; color: var(--cf-green); text-transform: uppercase; letter-spacing: 0.05em; }

.cf-content { padding: 2.5rem; flex: 1; overflow-y: auto; }

/* ═══ Transitions ═══ */
.view-fade-enter-active, .view-fade-leave-active { transition: opacity 0.3s var(--cf-ease), transform 0.3s var(--cf-ease); }
.view-fade-enter-from { opacity: 0; transform: translateY(10px); }
.view-fade-leave-to { opacity: 0; transform: translateY(-10px); }

.cf-sidebar-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); z-index: 1040; }

@media (max-width: 1024px) {
  .cf-sidebar { position: fixed; left: -280px; }
  .cf-sidebar.sidebar-open { left: 0; }
  .cf-sidebar.collapsed { width: 280px; }
  .sidebar-header .collapse-btn { display: none; }
  .cf-content { padding: 1.5rem; }
}
</style>
