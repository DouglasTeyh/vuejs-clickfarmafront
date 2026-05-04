import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Products from '../views/Products.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Cart from '../views/Cart.vue'
import Checkout from '../views/Checkout.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Profile from '../views/Profile.vue'
import Addresses from '../views/Addresses.vue'
import ResetPassword from '../views/ResetPassword.vue'
import Prescriptions from '../views/Prescriptions.vue'
import OrderConfirmation from '../views/OrderConfirmation.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
// import BackendOrderTracking from '../views/BackendOrderTracking.vue' // Removido pois o arquivo não existe


import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import ProductManagement from '../views/admin/ProductManagement.vue'
import InventoryManagement from '../views/admin/InventoryManagement.vue'
import OrderManagement from '../views/admin/OrderManagement.vue'
import PrescriptionValidation from '../views/admin/PrescriptionValidation.vue'
import UserManagement from '../views/admin/UserManagement.vue'
import PharmacyManagement from '../views/admin/PharmacyManagement.vue'
import CourierManagement from '../views/admin/CourierManagement.vue'
import CategoryManagement from '../views/admin/CategoryManagement.vue'

// Importe o componente
import UploadReceita from '@/components/prescriptions/UploadReceita.vue';
import AdminOverview from '../views/admin/AdminOverview.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/products',
    name: 'Products',
    component: Products
  },
  {
    path: '/promotions',
    name: 'Promotions',
    component: () => import('../views/Promotions.vue')
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: ProductDetail
  },
  {
    path: '/login',
    name: 'Login',
    component: Home,
    beforeEnter: (to, from, next) => {
      store.dispatch('openLoginModal')
      next('/')
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Home,
    beforeEnter: (to, from, next) => {
      store.dispatch('openRegisterModal')
      next('/')
    }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart
  },
  {
    path: '/checkout/:cart?',
    name: 'Checkout',
    component: Checkout,
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/addresses',
    name: 'Addresses',
    component: Addresses,
    meta: { requiresAuth: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPassword,
    meta: { requiresAuth: false }
  },
  {
    path: '/prescriptions',
    name: 'Prescriptions',
    component: Prescriptions,
    meta: { requiresAuth: true }
  },

  // Nova rota de upload de receita
  {
    path: '/prescriptions/upload',
    name: 'UploadReceita',
    component: UploadReceita,
    meta: {
      title: 'Leitura de Receita Médica',
      requiresAuth: false // ou true se precisar de login
    }
  },

  {
    path: '/sucesso-pagamento',
    name: 'SucessoPagamento',
    component: () => import('@/views/SucessoPagamento.vue')
  },
  {
    path: '/order-confirmation',
    name: 'OrderConfirmation',
    component: OrderConfirmation,
    meta: { requiresAuth: true }
  },
  {
    path: '/payment-method',
    name: 'PaymentMethod',
    component: () => import('../components/checkout/PaymentMethod.vue'),
    meta: { requiresAuth: true }
  },

  // 🔥 NOVAS ROTAS ADICIONADAS AQUI
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dashboard',
    name: 'UserDashboard',
    component: () => import('../views/UserDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tracking/:orderId',
    name: 'OrderTracking',
    component: () => import('../views/OrderTracking.vue'),
    props: true,
    meta: { requiresAuth: true }
  },
  // {
  //   path: '/tracking-backend/:codigoPedido?',
  //   name: 'BackendOrderTracking',
  //   component: BackendOrderTracking,
  //   props: true,
  //   meta: { requiresAuth: true }
  // },

  // Rotas existentes de rastreamento (mantidas para compatibilidade)
  {
    path: '/track-order',
    name: 'OrderTrackingOld',
    component: () => import('../views/OrderTracking.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/track-order/:orderId',
    name: 'OrderTrackingWithId',
    component: () => import('../views/OrderTracking.vue'),
    props: true,
    meta: { requiresAuth: true }
  },

  // Outras rotas existentes
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue')
  },
  {
    path: '/contact',
    name: 'Contact',
    component: () => import('../views/Contact.vue')
  },
  
  // 🔥 ROTAS FARMÁCIA
  {
    path: '/pharmacy/login',
    name: 'PharmacyLogin',
    component: Home,
    beforeEnter: (to, from, next) => {
      store.dispatch('openLoginModal')
      next('/')
    }
  },
  {
    path: '/pharmacy/register',
    name: 'PharmacyRegister',
    component: Home,
    beforeEnter: (to, from, next) => {
      store.dispatch('openRegisterModal')
      next('/')
    }
  },
  {
    path: '/pharmacy',
    component: () => import('../views/pharmacy/PharmacyLayout.vue'),
    meta: { requiresAuth: true, requiresPharmacy: true },
    children: [
      {
        path: '',
        redirect: '/pharmacy/dashboard'
      },
      {
        path: 'dashboard',
        name: 'PharmacyDashboard',
        component: () => import('../views/pharmacy/PharmacyDashboard.vue')
      },
      {
        path: 'products',
        name: 'PharmacyProducts',
        component: () => import('../views/pharmacy/PharmacyProducts.vue')
      },
      {
        path: 'orders',
        name: 'PharmacyOrders',
        component: () => import('../views/pharmacy/PharmacyOrders.vue')
      },
      {
        path: 'financial',
        name: 'PharmacyFinancial',
        component: () => import('../views/pharmacy/PharmacyFinancial.vue')
      },
      {
        path: 'settings',
        name: 'PharmacySettings',
        component: () => import('../views/pharmacy/PharmacySettings.vue')
      }
    ]
  },

  // 🔥 ROTAS ENTREGADOR
  {
    path: '/courier/login',
    name: 'CourierLogin',
    component: Home,
    beforeEnter: (to, from, next) => {
      store.dispatch('openLoginModal')
      next('/')
    }
  },
  {
    path: '/courier',
    component: () => import('../views/courier/CourierLayout.vue'),
    meta: { requiresAuth: true, requiresCourier: true },
    children: [
      {
        path: '',
        redirect: '/courier/dashboard'
      },
      {
        path: 'dashboard',
        name: 'CourierDashboard',
        component: () => import('../views/courier/CourierDashboard.vue')
      },
      {
        path: 'deliveries',
        name: 'CourierDeliveries',
        component: () => import('../views/courier/CourierDashboard.vue') // Reusando por enquanto
      },
      {
        path: 'financial',
        name: 'CourierFinancial',
        component: () => import('../views/courier/CourierFinancial.vue')
      }
    ]
  },

  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: Home,
    beforeEnter: (to, from, next) => {
      store.dispatch('openLoginModal')
      next('/')
    }
  },
  {
    path: '/admin',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboardOverview',
        component: AdminOverview
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: ProductManagement
      },
      {
        path: 'inventory',
        name: 'AdminInventory',
        component: InventoryManagement
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: OrderManagement
      },
      {
        path: 'prescriptions',
        name: 'AdminPrescriptions',
        component: PrescriptionValidation
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: UserManagement
      },
      {
        path: 'pharmacies',
        name: 'AdminPharmacies',
        component: PharmacyManagement
      },
      {
        path: 'couriers',
        name: 'AdminCouriers',
        component: CourierManagement
      },
      {
        path: 'payments',
        name: 'AdminPayments',
        component: () => import('../views/admin/PaymentManagement.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: CategoryManagement
      }
    ]
  }
]

import store from '../store'

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('authToken')
  let user = {}
  try {
    const rawUser = localStorage.getItem('user')
    if (rawUser && rawUser !== 'undefined') {
      user = JSON.parse(rawUser)
    }
  } catch (e) {
    console.error('Erro ao processar dados do usuário:', e)
    localStorage.removeItem('user')
  }
  const isAdmin = user.role?.toUpperCase() === 'ADMIN'
  const isPharmacy = user.role?.toUpperCase() === 'PHARMACY'
  const isCourier = user.role?.toUpperCase() === 'COURIER'

  if (to.meta.requiresAuth && !isAuthenticated) {
    // Em vez de redirecionar, abrimos o modal global
    store.dispatch('openLoginModal', to.fullPath)
    
    // Se viemos de algum lugar, permanecemos lá. Se é um acesso direto, vamos para home.
    if (from.path === '/' && to.path !== '/') {
        next('/')
    } else {
        next(false)
    }
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next('/')
  } else if (to.meta.requiresPharmacy && !isPharmacy) {
    next('/')
  } else if (to.meta.requiresCourier && !isCourier) {
    next('/')
  } else {
    next()
  }
})

export default router
