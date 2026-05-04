<template>
  <div class="address-selector p-3 border rounded bg-white shadow-sm mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h5 class="mb-0 fw-bold"><i class="fas fa-map-marker-alt me-2 text-primary"></i>Endereço de Entrega</h5>
      <button class="btn btn-sm btn-outline-primary" @click="showModal = true">
        <i class="fas fa-plus me-1"></i> Novo
      </button>
    </div>

    <div v-if="loading" class="text-center py-3">
      <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
    </div>

    <div v-else-if="enderecos.length === 0" class="text-muted small py-2">
      <i class="fas fa-info-circle me-1"></i> Nenhum endereço cadastrado.
    </div>

    <div v-else class="address-display">
      <!-- Caso mostre apenas o padrão ou um específico -->
      <div v-if="enderecoPadrao && !showAll" class="address-item p-3 mb-2 border rounded border-primary bg-light-primary d-flex justify-content-between align-items-center">
        <div class="small">
          <div class="fw-bold"><i class="fas fa-home me-2 text-primary"></i>{{ enderecoPadrao.logradouro }}, {{ enderecoPadrao.numero }}</div>
          <div class="text-muted">{{ enderecoPadrao.bairro }} - {{ enderecoPadrao.cidade }}/{{ enderecoPadrao.estado }}</div>
        </div>
        <button class="btn btn-link btn-sm text-primary text-decoration-none fw-bold" @click="showAll = true">Trocar</button>
      </div>

      <!-- Lista completa para troca -->
      <div v-if="showAll || !enderecoPadrao" class="address-list animate__animated animate__fadeIn">
        <div 
          v-for="addr in enderecos" 
          :key="addr.id"
          class="address-item p-2 mb-2 border rounded"
          :class="{ 'border-primary bg-light-primary': addr.padrao }"
          @click="selecionarEFechar(addr.id)"
          style="cursor: pointer;"
        >
          <div class="d-flex justify-content-between align-items-start">
            <div class="small">
              <div class="fw-bold">{{ addr.logradouro }}, {{ addr.numero }}</div>
              <div class="text-muted">{{ addr.bairro }} - {{ addr.cidade }}/{{ addr.estado }}</div>
            </div>
            <span v-if="addr.padrao" class="badge bg-primary">Padrão</span>
          </div>
        </div>
        <button v-if="enderecoPadrao" class="btn btn-light btn-sm w-100 mt-2" @click="showAll = false">Voltar</button>
      </div>
    </div>

    <!-- Modal Adicionar Endereço -->
    <div v-if="showModal" class="cf-modal-overlay" @click.self="showModal = false">
      <div class="cf-modal-box p-4 animate__animated animate__zoomIn" style="max-width: 500px;">
        <h5 class="fw-bold mb-4">Novo Endereço</h5>
        <form @submit.prevent="salvarEndereco">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-bold">CEP</label>
              <input v-model="form.cep" type="text" class="form-control" placeholder="00000-000" required>
            </div>
            <div class="col-12">
              <label class="form-label small fw-bold">Logradouro</label>
              <input v-model="form.logradouro" type="text" class="form-control" placeholder="Rua, Avenida..." required>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-bold">Número</label>
              <input v-model="form.numero" type="text" class="form-control" required>
            </div>
            <div class="col-md-8">
              <label class="form-label small fw-bold">Bairro</label>
              <input v-model="form.bairro" type="text" class="form-control" required>
            </div>
            <div class="col-md-8">
              <label class="form-label small fw-bold">Cidade</label>
              <input v-model="form.cidade" type="text" class="form-control" required>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-bold">Estado (UF)</label>
              <input v-model="form.estado" type="text" class="form-control" maxlength="2" required>
            </div>
            <div class="col-12">
              <label class="form-label small fw-bold">Complemento</label>
              <input v-model="form.complemento" type="text" class="form-control">
            </div>
          </div>
          <div class="mt-4 d-flex gap-2">
            <button type="button" class="btn btn-light w-100" @click="showModal = false">Cancelar</button>
            <button type="submit" class="btn btn-primary w-100" :disabled="saving">
              {{ saving ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'

export default {
  name: 'AddressSelector',
  data() {
    return {
      enderecos: [],
      loading: false,
      showModal: false,
      saving: false,
      form: {
        cep: '',
        logradouro: '',
        numero: '',
        bairro: '',
        cidade: '',
        estado: '',
        complemento: '',
        padrao: false
      },
      showAll: false
    }
  },
  computed: {
    enderecoPadrao() {
      return this.enderecos.find(a => a.padrao) || this.enderecos[0];
    }
  },
  async mounted() {
    await this.carregarEnderecos()
  },
  methods: {
    async carregarEnderecos() {
      this.loading = true
      try {
        const res = await api.get('/enderecos')
        this.enderecos = res.data
        this.$emit('loaded', this.enderecoPadrao)
      } catch (err) {
        console.error('Erro ao carregar endereços:', err)
      } finally {
        this.loading = false
      }
    },
    async salvarEndereco() {
      this.saving = true
      try {
        await api.post('/enderecos', this.form)
        this.showModal = false
        this.form = { cep: '', logradouro: '', numero: '', bairro: '', cidade: '', estado: '', complemento: '', padrao: false }
        await this.carregarEnderecos()
      } catch (err) {
        alert('Erro ao salvar endereço.')
      } finally {
        this.saving = false
      }
    },
    async selecionarPadrao(id) {
      try {
        await api.put(`/enderecos/${id}/padrao`)
        await this.carregarEnderecos()
      } catch (err) {
        console.error(err)
      }
    },
    async selecionarEFechar(id) {
      await this.selecionarPadrao(id)
      this.showAll = false
    }
  }
}
</script>

<style scoped>
.address-item { transition: all 0.2s; border: 1.5px solid #eee; }
.address-item:hover { border-color: var(--cf-green); }
.bg-light-primary { background-color: #f0f7f4; border-color: var(--cf-green) !important; }
.italic { font-style: italic; font-size: 0.75rem; }

.cf-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 2000;
  backdrop-filter: blur(4px);
}
.cf-modal-box { background: white; border-radius: 16px; width: 100%; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
</style>
