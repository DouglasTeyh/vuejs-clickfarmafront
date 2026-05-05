<template>
  <div class="cf-admin-shell">
    <!-- Overlay mobile -->
    <div v-if="sidebarOpen && isMobile" class="cf-sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- ═══ SIDEBAR PREMIUM ═══ -->
    <aside class="cf-sidebar" :class="{ 'sidebar-open': sidebarOpen, 'collapsed': isCollapsed && !isMobile }">
      <!-- Logo Section -->
      <div class="sidebar-header">
        <div class="brand-wrap">
          <div class="brand-logo">
            <i class="fas fa-leaf"></i>
          </div>
          <div class="brand-meta" v-if="!isCollapsed || isMobile">
            <h1 class="brand-name">Click<span>Farma</span></h1>
            <span class="brand-tag">Central Administrativa</span>
          </div>
        </div>
        <button class="collapse-btn d-none d-lg-flex" @click="isCollapsed = !isCollapsed">
          <i class="fas" :class="isCollapsed ? 'fa-chevron-right' : 'fa-chevron-left'"></i>
        </button>
      </div>

      <div class="sidebar-content cf-hide-scrollbar">
        <!-- Navigation Groups -->
        <nav class="sidebar-nav">
          <div v-for="group in navGroups" :key="group.label" class="nav-group">
            <h6 class="nav-label" v-if="!isCollapsed || isMobile">{{ group.label }}</h6>
            <div class="nav-items">
              <router-link v-for="item in group.items" :key="item.path" :to="item.path" class="nav-link"
                :class="{ active: $route.path === item.path }" @click="handleNavClick">
                <div class="link-icon">
                  <i :class="item.icon"></i>
                  <span class="badge-dot" v-if="item.badge"></span>
                </div>
                <span class="link-text" v-if="!isCollapsed || isMobile">{{ item.name }}</span>
                <div class="link-hover-pill"></div>
              </router-link>
            </div>
          </div>
        </nav>
      </div>

      <!-- User Profile Section -->
      <div class="sidebar-footer">
        <div class="user-profile-card">
          <div class="user-avatar-wrap">
            <div class="user-avatar">{{ currentUser?.nome?.charAt(0) || 'A' }}</div>
            <div class="status-indicator online"></div>
          </div>
          <div class="user-details" v-if="!isCollapsed || isMobile">
            <p class="user-name">{{ currentUser?.nome || 'Administrador' }}</p>
            <p class="user-role">Nível: Master</p>
          </div>
          <button @click="logout" class="logout-btn" title="Sair do Sistema">
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
              <router-link to="/admin/dashboard" class="bc-item">Painel</router-link>
              <i class="fas fa-chevron-right bc-sep"></i>
              <span class="bc-item active">{{ pageTitle }}</span>
            </nav>
            <h2 class="topbar-page-title">{{ pageTitle }}</h2>
          </div>
        </div>

        <div class="topbar-right">
          <div class="global-tools">
            <div class="tool-search d-none d-sm-flex">
              <i class="fas fa-search"></i>
              <input type="text" placeholder="Comando rápido (Alt+K)">
            </div>
            <button class="tool-icon-btn">
              <i class="far fa-bell"></i>
              <span class="notif-badge"></span>
            </button>
            <div class="divider"></div>
            <div class="system-health">
              <span class="pulse-dot"></span>
              <span class="status-label d-none d-md-block">API v1.4 Ativa</span>
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
export default {
  name: 'AdminDashboard',
  data() {
    return {
      sidebarOpen: false,
      isCollapsed: false,
      isMobile: false,
      currentUser: null,
      navGroups: [
        {
          label: 'Operacional',
          items: [
            { name: 'Visão Geral', path: '/admin/dashboard', icon: 'fas fa-chart-pie' },
            { name: 'Pedidos', path: '/admin/orders', icon: 'fas fa-shopping-cart', badge: true },
            { name: 'Receitas', path: '/admin/prescriptions', icon: 'fas fa-file-medical' }
          ]
        },
        {
          label: 'Ecossistema',
          items: [
            { name: 'Usuários', path: '/admin/users', icon: 'fas fa-user-friends' },
            { name: 'Farmácias', path: '/admin/pharmacies', icon: 'fas fa-hospital' },
            { name: 'Entregadores', path: '/admin/couriers', icon: 'fas fa-truck-fast' }
          ]
        },
        {
          label: 'Logística & Catálogo',
          items: [
            { name: 'Produtos', path: '/admin/products', icon: 'fas fa-boxes-stacked' },
            { name: 'Categorias', path: '/admin/categories', icon: 'fas fa-tags' },
            { name: 'Estoque', path: '/admin/inventory', icon: 'fas fa-clipboard-check' }
          ]
        },
        {
          label: 'Tesouraria',
          items: [
            { name: 'Conciliação', path: '/admin/payments', icon: 'fas fa-wallet' }
          ]
        }
      ]
    };
  },
  computed: {
    pageTitle() {
      const titles = {
        '/admin/dashboard': 'Relatório Executivo',
        '/admin/products': 'Gestão Global',
        '/admin/orders': 'Fluxo de Pedidos',
        '/admin/users': 'Base de Clientes',
        '/admin/pharmacies': 'Rede de Farmácias',
        '/admin/couriers': 'Gestão Logística',
        '/admin/payments': 'Conciliação Financeira',
        '/admin/categories': 'Arquitetura de Categorias',
        '/admin/prescriptions': 'Validação Técnica',
        '/admin/inventory': 'Controle de Estoque'
      };
      return titles[this.$route.path] || 'Painel Administrativo';
    }
  },
  mounted() {
    this.checkMobile();
    this.loadUser();
    window.addEventListener('resize', this.checkMobile);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 1024;
      if (!this.isMobile && this.sidebarOpen) this.sidebarOpen = false;
    },
    loadUser() {
      try {
        const raw = localStorage.getItem('user');
        if (raw) this.currentUser = JSON.parse(raw);
      } catch (e) { }
    },
    handleNavClick() {
      if (this.isMobile) this.sidebarOpen = false;
    },
    logout() {
      if (confirm('Deseja encerrar sua sessão administrativa com segurança?')) {
        localStorage.clear();
        this.$router.push('/admin/login');
      }
    }
  }
};
</script>


<style>
@import '@/assets/admin-shared.css';
</style>

<style scoped>
/* ═══ Shell & Base ═══ */
.cf-admin-shell {
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
  display: flex;
  flex-direction: column;
  transition: all 0.4s var(--cf-ease);
  z-index: 1050;
  box-shadow: 10px 0 30px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.cf-sidebar.collapsed {
  width: 88px;
}

/* Sidebar Header */
.sidebar-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
  transition: all 0.3s;
}

.cf-sidebar.collapsed .sidebar-header {
  padding: 0;
  justify-content: center;
}

.brand-wrap {
  display: flex;
  align-items: center;
  gap: 1rem;
  overflow: hidden;
  transition: all 0.3s;
}

.cf-sidebar.collapsed .brand-wrap {
  gap: 0;
}

.brand-logo {
  width: 42px;
  height: 42px;
  background: var(--cf-gold);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
  box-shadow: 0 4px 15px rgba(184, 149, 80, 0.4);
  flex-shrink: 0;
  transition: all 0.3s;
}

.brand-name {
  font-family: var(--cf-serif);
  font-size: 1.4rem;
  font-weight: 600;
  color: #fff;
  margin: 0;
  white-space: nowrap;
}

.brand-name span {
  color: var(--cf-gold);
}

.brand-tag {
  font-size: 0.6rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: rgba(255, 255, 255, 0.4);
  display: block;
}

.collapse-btn {
  background: rgba(255, 255, 255, 0.05);
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.cf-sidebar.collapsed .collapse-btn {
  position: absolute;
  right: -14px;
  top: 26px;
  background: var(--cf-gold);
  color: #fff;
  border: 2px solid var(--cf-green-dark);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.collapse-btn:hover {
  background: var(--cf-gold);
  color: #fff;
  transform: scale(1.1);
}

/* Sidebar Nav */
.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 1rem;
}

.nav-group {
  margin-bottom: 2rem;
}

.nav-label {
  font-size: 0.65rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: rgba(255, 255, 255, 0.25);
  margin: 0 0 1rem 1rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.8rem 1rem;
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  border-radius: 14px;
  transition: all 0.3s var(--cf-ease);
  position: relative;
  margin-bottom: 0.25rem;
}

.link-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  position: relative;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.nav-link.active {
  background: rgba(184, 149, 80, 0.15);
  color: var(--cf-gold);
}

.nav-link.active .link-hover-pill {
  position: absolute;
  left: -10px;
  width: 4px;
  height: 20px;
  background: var(--cf-gold);
  border-radius: 0 4px 4px 0;
  box-shadow: 2px 0 10px var(--cf-gold);
}

.link-text {
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.badge-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: var(--cf-gold);
  border: 2px solid var(--cf-green-dark);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--cf-gold);
}

/* Sidebar Footer */
.sidebar-footer {
  padding: 1.5rem 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(0, 0, 0, 0.2);
}

.user-profile-card {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 18px;
  padding: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.user-avatar-wrap {
  position: relative;
}

.user-avatar {
  width: 42px;
  height: 42px;
  background: var(--cf-gold);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #fff;
  font-size: 1.1rem;
}

.status-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid var(--cf-green-dark);
}

.status-indicator.online {
  background: #22c55e;
}

.user-name {
  font-size: 0.85rem;
  font-weight: 700;
  color: #fff;
  margin: 0;
  white-space: nowrap;
}

.user-role {
  font-size: 0.65rem;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

.logout-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  color: #ff6b6b;
  transform: scale(1.1);
}

/* ═══ Main Content ═══ */
.cf-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-width: 0;
}

/* Topbar Premium */
.cf-topbar {
  height: 80px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  z-index: 1000;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.mobile-toggle {
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border);
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: var(--cf-text-dark);
  cursor: pointer;
}

.cf-breadcrumb {
  gap: 0.6rem;
  align-items: center;
  margin-bottom: 0.2rem;
}

.bc-item {
  font-size: 0.72rem;
  color: var(--cf-text-faint);
  text-decoration: none;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.bc-sep {
  font-size: 0.55rem;
  color: var(--cf-text-faint);
  opacity: 0.5;
}

.bc-item.active {
  color: var(--cf-gold);
}

.topbar-page-title {
  font-family: var(--cf-serif);
  font-size: 1.6rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  margin: 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.global-tools {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.tool-search {
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border);
  border-radius: 12px;
  padding: 0.6rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 280px;
}

.tool-search i {
  color: var(--cf-text-faint);
  font-size: 0.85rem;
}

.tool-search input {
  border: none;
  background: transparent;
  font-size: 0.85rem;
  outline: none;
  width: 100%;
  color: var(--cf-text-dark);
}

.tool-icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--cf-border);
  color: var(--cf-text-muted);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.tool-icon-btn:hover {
  border-color: var(--cf-green);
  color: var(--cf-green);
  background: var(--cf-ivory-light);
}

.notif-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border: 2px solid #fff;
  border-radius: 50%;
}

.system-health {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.8rem;
  background: var(--cf-green-xlight);
  border-radius: 50px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: var(--cf-green);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-label {
  font-size: 0.65rem;
  font-weight: 800;
  color: var(--cf-green);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(42, 92, 69, 0.7);
  }

  70% {
    transform: scale(1);
    box-shadow: 0 0 0 6px rgba(42, 92, 69, 0);
  }

  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(42, 92, 69, 0);
  }
}

/* Content Area */
.cf-content {
  padding: 2.5rem;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* ═══ Transitions ═══ */
.view-fade-enter-active,
.view-fade-leave-active {
  transition: opacity 0.3s var(--cf-ease), transform 0.3s var(--cf-ease);
}

.view-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.view-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.cf-sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 1040;
}

@media (max-width: 1024px) {
  .cf-sidebar {
    position: fixed;
    left: -280px;
  }

  .cf-sidebar.sidebar-open {
    left: 0;
  }

  .cf-sidebar.collapsed {
    width: 280px;
  }

  .sidebar-header .collapse-btn {
    display: none;
  }

  .cf-content {
    padding: 1.5rem;
  }
}
</style>
