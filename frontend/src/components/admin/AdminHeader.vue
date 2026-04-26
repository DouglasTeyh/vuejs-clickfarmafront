<!-- src/components/admin/AdminHeader.vue -->
<template>
  <header class="admin-header">
    <div class="header-content">
      <div class="header-left">
        <h1>{{ currentPageTitle }}</h1>
      </div>
      <div class="header-right">
        <span class="user-info">👋 Olá, Admin</span>
        <button @click="logout" class="logout-btn">🚪 Sair</button>
      </div>
    </div>
  </header>
</template>

<script>
import { useRoute, useRouter } from 'vue-router'

export default {
  name: 'AdminHeader',
  setup() {
    const route = useRoute()
    const router = useRouter()

    const pageTitles = {
      '/admin/products': 'Gerenciar Produtos',
      '/admin/inventory': 'Gerenciar Estoque', 
      '/admin/orders': 'Visualizar Pedidos',
      '/admin/prescriptions': 'Validar Receitas',
      '/admin/users': 'Gerenciar Usuários'
    }

    const currentPageTitle = pageTitles[route.path] || 'Painel Administrativo'

    const logout = () => {
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      router.push('/login')
    }

    return {
      currentPageTitle,
      logout
    }
  }
}
</script>

<style scoped>
.admin-header {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 0 20px;
  height: 70px;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-left h1 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--cf-text-dark);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  color: var(--cf-text-muted);
}

.logout-btn {
  background: var(--cf-danger);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn:hover {
  background: #c0392b;
}
</style>