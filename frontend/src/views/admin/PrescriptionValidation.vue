<template>
  <div class="cf-mgmt">
    <div class="dash-welcome mb-4">
      <div class="welcome-text">
        <h3 class="dash-page-title">Validação de Prescrições</h3>
        <p class="text-muted mb-0">Auditoria técnica de receitas digitais para conformidade farmacêutica.</p>
      </div>
      <div class="dash-actions">
        <div class="cf-pill-counter shadow-sm">
          <span class="label">Pendências</span>
          <span class="count">{{ prescriptions.filter(p => p.status === 'Pendente').length }}</span>
        </div>
      </div>
    </div>

    <div class="row g-4 animate__animated animate__fadeInUp">
      <div v-for="prescription in prescriptions" :key="prescription.id" class="col-xl-4 col-md-6">
        <div class="cf-prescription-card shadow-sm" :class="{ 'pending-border': prescription.status === 'Pendente' }">
          <!-- Card Header -->
          <div class="p-3 border-bottom d-flex justify-content-between align-items-center bg-light-subtle">
            <div class="extra-small fw-bold text-muted letter-spacing-1">PEDIDO #{{ prescription.orderId }}</div>
            <span class="cf-status-badge shadow-sm" :class="prescription.status.toLowerCase()">
              <i class="fas fa-circle me-1 small opacity-50"></i>
              {{ prescription.status }}
            </span>
          </div>

          <!-- Card Body -->
          <div class="p-4 bg-white">
            <div class="d-flex align-items-start gap-3 mb-4">
              <div class="cf-icon-avatar primary shadow-sm" :style="`background:${getStatusColor(prescription.status)}15; color:${getStatusColor(prescription.status)}; border: 1px solid ${getStatusColor(prescription.status)}30`">
                <i class="fas fa-file-prescription"></i>
              </div>
              <div class="flex-grow-1">
                <div class="cf-td-bold text-dark mb-1">{{ prescription.medication }}</div>
                <div class="extra-small text-muted fw-bold">{{ prescription.customerName }}</div>
              </div>
            </div>

            <div class="cf-info-grid mb-4">
              <div class="info-item">
                <label>Emissão</label>
                <div class="value"><i class="fas fa-calendar-day me-1 opacity-50"></i> {{ d(prescription.date) }}</div>
              </div>
              <div class="info-item text-end" v-if="prescription.validatedAt">
                <label>Auditoria</label>
                <div class="value text-success fw-bold">{{ prescription.validatedAt.split(' ')[0] }}</div>
              </div>
              <div class="info-item text-end" v-else>
                <label>Auditoria</label>
                <div class="value text-muted italic">Aguardando</div>
              </div>
            </div>

            <div class="cf-file-box shadow-sm mb-2" v-if="prescription.fileUrl">
              <div class="d-flex align-items-center">
                <div class="file-icon bg-danger-subtle text-danger shadow-sm"><i class="fas fa-file-pdf"></i></div>
                <div class="ms-3 flex-grow-1">
                  <div class="extra-small fw-bold text-dark">Documento Digital</div>
                  <a :href="prescription.fileUrl" target="_blank" class="small text-primary fw-bold text-decoration-none">Visualizar Guia <i class="fas fa-external-link-alt ms-1 small"></i></a>
                </div>
              </div>
            </div>
          </div>

          <!-- Card Actions -->
          <div class="p-3 bg-light-subtle border-top mt-auto" v-if="prescription.status === 'Pendente'">
            <div class="row g-2">
              <div class="col-6">
                <button @click="validatePrescription(prescription.id, 'Aprovada')" class="cf-btn-action success w-100 shadow-sm">
                  <i class="fas fa-check-circle me-1"></i> Aprovar
                </button>
              </div>
              <div class="col-6">
                <button @click="validatePrescription(prescription.id, 'Rejeitada')" class="cf-btn-action danger w-100 shadow-sm">
                  <i class="fas fa-times-circle me-1"></i> Rejeitar
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!prescriptions.length" class="col-12 text-center py-5">
        <div class="cf-empty py-5">
          <i class="fas fa-clipboard-check fa-4x mb-3 opacity-10"></i>
          <p class="fw-bold text-muted">Nenhuma receita pendente de auditoria.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PrescriptionValidation',
  data() {
    return {
      prescriptions: [],
      uploadingPrescriptionId: null,
      selectedFile: null
    }
  },
  mounted() {
    this.fetchPrescriptions()
  },
  methods: {
    async fetchPrescriptions() {
      // Mock data preserved for visual demonstration
      this.prescriptions = [
        { id: 1, orderId: 1001, customerName: 'João Silva', medication: 'Antibiótico Amoxicilina', date: '2023-10-01', status: 'Pendente', fileUrl: '/mock/receita1.pdf', validatedAt: null },
        { id: 2, orderId: 1002, customerName: 'Maria Oliveira', medication: 'Paracetamol Controlado', date: '2023-10-02', status: 'Aprovada', fileUrl: '/mock/receita2.pdf', validatedAt: '02/10/2023 14:30' },
        { id: 3, orderId: 1003, customerName: 'Carlos Santos', medication: 'Dipirona Sódica', date: '2023-10-03', status: 'Pendente', fileUrl: '/mock/receita3.pdf', validatedAt: null }
      ];
    },
    getStatusColor(s) { return { 'Pendente': '#B89550', 'Aprovada': '#2A5C45', 'Rejeitada': '#ef4444' }[s] || '#64748b'; },
    d(dt) { return new Date(dt).toLocaleDateString('pt-BR'); },

    async validatePrescription(id, status) {
      if (confirm(`Confirmar ${status.toLowerCase()} da receita?`)) {
        const p = this.prescriptions.find(p => p.id === id);
        if (p) {
          p.status = status;
          p.validatedAt = new Date().toLocaleString('pt-BR');
        }
        if (window.$toast) window.$toast.addToast(`Receita ${status.toLowerCase()}!`, 'success');
      }
    }
  }
}
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

.cf-pill-counter { background: #fff; border-radius: 20px; padding: 0.5rem 1rem; border: 1px solid var(--cf-border); display: flex; align-items: center; gap: 0.75rem; }
.cf-pill-counter .label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; }
.cf-pill-counter .count { background: var(--cf-green); color: #fff; padding: 0.1rem 0.6rem; border-radius: 12px; font-size: 0.75rem; font-weight: 800; }

.cf-prescription-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); overflow: hidden; display: flex; flex-direction: column; transition: all 0.3s; height: 100%; }
.cf-prescription-card:hover { transform: translateY(-5px); box-shadow: var(--cf-shadow-md); }
.cf-prescription-card.pending-border { border-left: 4px solid var(--cf-gold); }

.cf-icon-avatar { width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; flex-shrink: 0; }

.cf-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.cf-info-grid label { display: block; font-size: 0.62rem; text-transform: uppercase; color: var(--cf-text-muted); font-weight: 700; margin-bottom: 4px; letter-spacing: 0.05em; }
.cf-info-grid .value { font-size: 0.85rem; color: var(--cf-text-dark); font-weight: 600; }

.cf-status-badge { font-size: 0.62rem; font-weight: 800; padding: 0.35rem 0.85rem; border-radius: 20px; text-transform: uppercase; display: inline-flex; align-items: center; }
.cf-status-badge.pendente { background: #FFF8EC; color: #9A6700; border: 1px solid #FFECB3; }
.cf-status-badge.aprovada { background: #E8F5E9; color: #2E7D32; border: 1px solid #C8E6C9; }
.cf-status-badge.rejeitada { background: #F9EDED; color: var(--cf-danger); border: 1px solid #F8D7DA; }

.cf-file-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 16px; padding: 1rem; }
.file-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1rem; flex-shrink: 0; }

.cf-btn-action { border: none; border-radius: 12px; padding: 0.6rem 1rem; font-size: 0.78rem; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; }
.cf-btn-action.success { background: var(--cf-green-xlight); color: var(--cf-green); border: 1px solid rgba(42,92,69,0.2); }
.cf-btn-action.success:hover { background: var(--cf-green); color: #fff; }
.cf-btn-action.danger { background: #FFF5F5; color: var(--cf-danger); border: 1px solid #F8D7DA; }
.cf-btn-action.danger:hover { background: var(--cf-danger); color: #fff; }

.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>