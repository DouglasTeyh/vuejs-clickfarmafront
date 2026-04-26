<template>
  <div class="order-tracking-page">
    <div class="container py-lg-5 py-4">
      
      <!-- Cabeçalho Editorial -->
      <div class="tracking-header text-center mb-5 fade-in-up">
        <span class="section-eyebrow">Logística Inteligente</span>
        <h1 class="section-title mb-2">Rastrear <em>Pedido</em></h1>
        <p class="section-desc">Acompanhe cada etapa da sua entrega em tempo real.</p>
      </div>

      <!-- Busca de Pedido -->
      <div class="card cf-tracking-card mb-4 fade-in-up">
        <div class="card-body p-4">
          <div class="row g-3 align-items-end">
            <div class="col-md-9">
              <label class="form-label">Código do Pedido</label>
              <div class="input-group-cf">
                <i class="fa-solid fa-barcode"></i>
                <input 
                  v-model="searchOrderId" 
                  type="text" 
                  class="form-control" 
                  placeholder="Cole aqui seu código (Ex: ORD-ABC...)"
                  @keyup.enter="loadOrder"
                >
              </div>
            </div>
            <div class="col-md-3">
              <button 
                class="btn btn-primary w-100" 
                @click="loadOrder"
                :disabled="loading || !searchOrderId.trim()"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? 'Localizando...' : 'Localizar' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pedido Encontrado -->
      <div v-if="currentOrder" class="tracking-content fade-in-up">
        <div class="row g-4">
          <div class="col-lg-8">
            <!-- Informações Gerais do Pedido -->
            <div class="card cf-detail-card mb-4">
              <div class="card-header-cf">
                <div class="d-flex justify-content-between align-items-center">
                  <h5 class="mb-0">Pedido #{{ currentOrder.id }}</h5>
                  <span :class="statusClass(currentOrder.status)" class="cf-status-pill">
                    {{ getStatusText(currentOrder.status) }}
                  </span>
                </div>
              </div>
              <div class="card-body p-4">
                <div class="row g-4">
                  <div class="col-md-6 border-end-cf">
                    <div class="cf-info-row">
                      <span class="cf-info-label">Realizado em</span>
                      <span class="cf-info-val">{{ formatDate(currentOrder.date) }}</span>
                    </div>
                    <div class="cf-info-row mt-3">
                      <span class="cf-info-label">Valor Total</span>
                      <span class="cf-info-val text-green">R$ {{ getOrderTotal(currentOrder) }}</span>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="cf-info-row">
                      <span class="cf-info-label">Método de Pagamento</span>
                      <span class="cf-info-val">{{ getPaymentMethodLabel(currentOrder.paymentMethod) }}</span>
                    </div>
                    <div class="cf-info-row mt-3">
                      <span class="cf-info-label">Modalidade</span>
                      <span class="cf-info-val">{{ currentOrder.deliveryType === 'delivery' ? 'Entrega em Domicílio' : 'Retirada em Loja' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Timeline do Pedido -->
            <div class="card cf-detail-card mb-4">
              <div class="card-header-cf border-0 pb-0">
                <h5 class="mb-0">Histórico de Movimentação</h5>
              </div>
              <div class="card-body p-4">
                <OrderTimeline :order="currentOrder" :auto-refresh="true" />
              </div>
            </div>

            <!-- Mapa de Rastreamento -->
            <div class="card cf-detail-card mb-4">
              <div class="card-header-cf border-0 pb-0">
                <h5 class="mb-0">Mapa de Entrega</h5>
              </div>
              <div class="card-body p-4">
                <div class="map-container">
                  <LiveTrackingMap 
                    v-if="currentOrder && currentOrder.id"
                    :order-id="currentOrder.id"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Sidebar -->
          <div class="col-lg-4">
            <div class="card cf-sidebar-card mb-4">
              <div class="card-body p-4 text-center">
                <h6 class="cf-sidebar-title mb-4">Gerenciar</h6>
                <button class="btn btn-outline-primary w-100 mb-2" @click="refreshTracking" :disabled="loading">
                  <i class="fa-solid fa-sync-alt me-2" :class="{ 'fa-spin': loading }"></i> Atualizar Agora
                </button>
                <button class="btn btn-outline-secondary w-100 mb-2" @click="resetSearch">
                  Outro Pedido
                </button>
                <router-link to="/orders" class="btn btn-outline-secondary w-100">
                  Ver Histórico Completo
                </router-link>
              </div>
            </div>

            <div class="card cf-support-card">
              <div class="card-body p-4">
                <h6 class="cf-sidebar-title text-green">Apoio Farmacêutico</h6>
                <p class="small text-muted mb-4 fw-light">Sua entrega está com algum problema? Nossa equipe está de plantão.</p>
                <div class="support-links">
                  <a href="tel:81998189999" class="support-item">
                    <i class="fa-solid fa-phone-alt"></i> (81) 99818-9999
                  </a>
                  <a href="mailto:gustavson.adm@gmail.com" class="support-item mt-2">
                    <i class="fa-regular fa-envelope"></i> gustavson.adm@gmail.com
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Estados Vazios -->
      <div v-else-if="!loading" class="text-center py-5 fade-in">
        <div class="empty-visual mb-4">
          <i class="fa-solid fa-search"></i>
        </div>
        <h4 class="text-dark fw-normal">{{ searchPerformed ? 'Pedido não localizado' : 'Aguardando código' }}</h4>
        <p class="text-muted fw-light">{{ searchPerformed ? 'Verifique se o código está correto.' : 'Digite o código do seu pedido acima para começar.' }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import OrderTimeline from '@/components/orders/OrderTimeline.vue'
import LiveTrackingMap from '@/components/orders/LiveTrackingMap.vue'

export default {
  name: 'OrderTracking',
  components: {
    OrderTimeline,
    LiveTrackingMap
  },
  props: {
    orderId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      searchOrderId: '',
      currentOrder: null,
      loading: false,
      searchPerformed: false
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated']),
    
    userOrders() {
      try {
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
        
        if (this.isAuthenticated) {
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          return savedOrders
            .filter(order => order.userId === user.id)
            .slice(0, 5)
            .sort((a, b) => new Date(b.date) - new Date(a.date))
        }
        
        return savedOrders
          .slice(0, 3)
          .sort((a, b) => new Date(b.date) - new Date(a.date))
      } catch (error) {
        return []
      }
    }
  },
  methods: {
    ...mapActions(['fetchRealTimeTracking']),
    
    async loadOrder() {
      if (!this.searchOrderId.trim()) {
        alert('Por favor, informe o número do pedido')
        return
      }

      this.loading = true
      this.searchPerformed = true

      try {
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
        const order = savedOrders.find(o => 
          o.id.toLowerCase() === this.searchOrderId.trim().toLowerCase()
        )
        
        if (order) {
          this.currentOrder = order
          await this.loadTrackingInfo()
        } else {
          this.currentOrder = null
        }
      } catch (error) {
        console.error('Erro ao carregar pedido:', error)
        this.currentOrder = null
      } finally {
        this.loading = false
      }
    },
    
    selectOrder(order) {
      this.searchOrderId = order.id
      this.loadOrder()
    },
    
    async loadTrackingInfo() {
      if (!this.currentOrder?.id) return
      
      try {
        await this.fetchRealTimeTracking(this.currentOrder.id)
      } catch (error) {
        console.error('Erro ao carregar rastreamento:', error)
      }
    },
    
    async refreshTracking() {
      await this.loadTrackingInfo()
    },
    
    resetSearch() {
      this.searchOrderId = ''
      this.currentOrder = null
      this.searchPerformed = false
    },
    
    getStatusText(status) {
      const statusMap = {
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue',
        'cancelled': 'Cancelado'
      }
      return statusMap[status] || status
    },
    
    statusClass(status) {
      const classes = {
        'confirmed': 'bg-primary',
        'processing': 'bg-info',
        'shipped': 'bg-warning',
        'out_for_delivery': 'bg-warning text-dark',
        'delivered': 'bg-success',
        'cancelled': 'bg-danger'
      }
      return classes[status] || 'bg-secondary'
    },
    
    getPaymentMethodLabel(method) {
      const methods = {
        'credit_card': 'Cartão de Crédito',
        'debit_card': 'Cartão de Débito',
        'pix': 'PIX',
        'boleto': 'Boleto'
      }
      return methods[method] || method
    },
    
    formatDate(dateString) {
      try {
        if (!dateString) return 'Data não disponível'
        return new Date(dateString).toLocaleDateString('pt-BR', {
          day: '2-digit', month: '2-digit', year: 'numeric',
          hour: '2-digit', minute: '2-digit'
        })
      } catch (error) {
        return 'Data inválida'
      }
    },

    getOrderTotal(order) {
      if (!order) return '0.00'
      const total = order.total || (order.items || []).reduce((s, i) => s + (i.price * i.quantity), 0);
      return parseFloat(total).toFixed(2).replace('.', ',');
    }
  },
  
  async mounted() {
    if (this.orderId) {
      this.searchOrderId = this.orderId
      await this.loadOrder()
    } else if (this.userOrders.length > 0) {
      this.currentOrder = this.userOrders[0]
      await this.loadTrackingInfo()
    }
  }
}
</script>

<style scoped>
.order-tracking-page {
  min-height: 100vh;
  background: var(--cf-white);
  padding-bottom: 4rem;
}

.section-desc { color: var(--cf-text-muted); font-weight: 300; font-size: 1rem; }

/* SEARCH CARD */
.cf-tracking-card {
  border: 1px solid var(--cf-border);
  background: var(--cf-ivory);
  border-radius: var(--cf-r-lg);
}
.input-group-cf {
  position: relative;
  display: flex;
  align-items: center;
}
.input-group-cf i {
  position: absolute;
  left: 1.1rem;
  color: var(--cf-text-faint);
}
.input-group-cf .form-control {
  padding-left: 2.8rem;
  background: var(--cf-white);
}

/* CARDS GERAIS */
.cf-detail-card {
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  overflow: hidden;
  background: var(--cf-white);
}
.card-header-cf {
  background: var(--cf-white);
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--cf-border);
}
.card-header-cf h5 { font-family: var(--cf-sans); font-size: 1.25rem; font-weight: 600; color: var(--cf-text-dark); }

/* TABELA DE INFOS */
.cf-info-row { display: flex; flex-direction: column; }
.cf-info-label { font-size: 0.62rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); margin-bottom: 2px; }
.cf-info-val { font-size: 1rem; font-weight: 400; color: var(--cf-text-dark); }
.text-green { color: var(--cf-green) !important; font-family: var(--cf-sans); font-size: 1.4rem; font-weight: 500; }
.border-end-cf { border-right: 1px solid var(--cf-border); }

/* STATUS */
.cf-status-pill {
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: 500;
}
.bg-primary { background: var(--cf-green-light) !important; color: var(--cf-green) !important; }
.bg-info { background: var(--cf-sage-tint) !important; color: var(--cf-green-mid) !important; }
.bg-warning { background: var(--cf-gold-light) !important; color: var(--cf-gold) !important; }
.bg-success { background: var(--cf-green-light) !important; color: var(--cf-green) !important; }
.bg-danger { background: #FEF2F2 !important; color: var(--cf-danger) !important; }

/* SIDEBAR */
.cf-sidebar-card, .cf-support-card {
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
}
.cf-sidebar-title {
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--cf-text-faint);
  margin-bottom: 1rem;
}

.support-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
  color: var(--cf-text-dark);
  text-decoration: none;
  transition: color 200ms;
}
.support-item i { color: var(--cf-green); width: 15px; }
.support-item:hover { color: var(--cf-green); }

/* EMPTY VISUAL */
.empty-visual {
  width: 80px; height: 80px;
  background: var(--cf-ivory);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 2.2rem;
  color: var(--cf-text-faint);
}

.map-container { border-radius: var(--cf-r-md); overflow: hidden; border: 1px solid var(--cf-border); }

@media (max-width: 767.98px) {
  .border-end-cf { border-right: none; border-bottom: 1px solid var(--cf-border); padding-bottom: 1.5rem; }
}
</style>