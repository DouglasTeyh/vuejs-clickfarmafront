<template>
  <div class="courier-dashboard">
    <!-- Map Container -->
    <div id="courier-map" class="map-container"></div>

    <!-- Interface Inferior (Bottom Sheet Modal) -->
    <div class="bottom-sheet shadow-lg" :class="{'active': true}">
      <div class="sheet-handle"></div>

      <!-- ESTADO: WAITING (Aguardando Corridas) -->
      <div v-if="estado === 'WAITING'" class="text-center py-3">
        <div class="status-indicator online mb-2">
          <span class="pulse"></span> Online e buscando corridas...
        </div>
        <div class="text-muted small">Sua localização está sendo atualizada em tempo real.</div>
      </div>

      <!-- ESTADO: PROPOSAL (Nova Corrida Recebida) -->
      <div v-else-if="estado === 'PROPOSAL' && propostaAtual" class="proposal-card">
        <div class="bg-primary-subtle text-primary fw-bold text-center py-2 rounded mb-3">
          NOVA ENTREGA DISPONÍVEL!
        </div>
        <div class="d-flex justify-content-between align-items-center mb-3">
          <div>
            <h5 class="fw-bold mb-0">R$ {{ propostaAtual.valorFrete?.toFixed(2) || '0.00' }}</h5>
            <small class="text-muted">{{ propostaAtual.distanciaKm?.toFixed(1) || '0.0' }} km totais</small>
          </div>
          <div class="text-end">
            <span class="badge bg-warning text-dark">Dinâmico</span>
          </div>
        </div>
        
        <div class="route-info mb-4">
          <div class="d-flex mb-2">
            <i class="fas fa-store text-primary mt-1 me-2"></i>
            <div>
              <strong>Farmácia Origem</strong><br/>
              <span class="small text-muted">{{ propostaAtual.farmaciaNome || 'ClickFarma Center' }}</span>
            </div>
          </div>
          <div class="d-flex">
            <i class="fas fa-map-marker-alt text-danger mt-1 me-2"></i>
            <div>
              <strong>Entrega Cliente</strong><br/>
              <span class="small text-muted">{{ propostaAtual.enderecoEntrega }}</span>
            </div>
          </div>
        </div>

        <button class="btn btn-primary btn-lg w-100 fw-bold rounded-pill" @click="aceitarCorrida">
          ACEITAR CORRIDA
        </button>
      </div>

      <!-- ESTADO: TO_PHARMACY (A Caminho da Farmácia) -->
      <div v-else-if="estado === 'TO_PHARMACY'" class="action-card">
        <h5 class="fw-bold text-center mb-1">Vá para a Farmácia</h5>
        <p class="text-center text-muted small mb-3">Retire o pedido e informe o código fornecido pelo balcão.</p>
        
        <div class="d-flex mb-3 align-items-center justify-content-between">
            <span class="fw-bold"><i class="fas fa-store text-primary"></i> {{ corridaAtual?.farmaciaNome || 'ClickFarma Center' }}</span>
            <span class="badge bg-light text-dark border">Pedido #{{ corridaAtual?.codigoPedido || '0000' }}</span>
        </div>

        <div class="input-group mb-3">
          <input type="text" class="form-control text-center fw-bold form-control-lg" v-model="inputCodigoRetirada" placeholder="Cód de 4 dígitos" maxlength="4">
        </div>

        <button class="btn btn-primary btn-lg w-100 fw-bold rounded-pill" :disabled="inputCodigoRetirada.length < 4 || loadingAction" @click="confirmarRetirada">
          CONFIRMAR RETIRADA
        </button>
      </div>

      <!-- ESTADO: TO_CLIENT (A Caminho do Cliente) -->
      <div v-else-if="estado === 'TO_CLIENT'" class="action-card">
        <h5 class="fw-bold text-center mb-1">Entregue ao Cliente</h5>
        <p class="text-center text-muted small mb-3">Peça o código de 4 dígitos ao cliente para finalizar.</p>
        
        <div class="d-flex mb-3 align-items-center justify-content-between">
            <span class="fw-bold"><i class="fas fa-map-marker-alt text-danger"></i> Endereço</span>
        </div>
        <p class="small text-muted mb-3">{{ corridaAtual?.enderecoEntrega }}</p>

        <div class="input-group mb-3">
          <input type="text" class="form-control text-center fw-bold form-control-lg" v-model="inputCodigoEntrega" placeholder="Cód de 4 dígitos" maxlength="4">
        </div>

        <button class="btn btn-success btn-lg w-100 fw-bold rounded-pill" :disabled="inputCodigoEntrega.length < 4 || loadingAction" @click="confirmarEntrega">
          FINALIZAR ENTREGA
        </button>
      </div>
    </div>

    <!-- ═══ MODAL PIX OBRIGATÓRIO ═══ -->
    <div v-if="showPixModal" class="pix-modal-overlay">
      <div class="pix-modal-card">
        <div class="pix-modal-header text-center mb-4">
          <i class="fas fa-money-bill-transfer text-success fa-3x mb-3"></i>
          <h4 class="fw-bold">Cadastre sua Chave PIX</h4>
          <p class="text-muted small">Para receber o valor das suas corridas, precisamos que cadastre uma chave PIX. Você só verá esta tela até configurar.</p>
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold small">Tipo de Chave</label>
          <select v-model="pixForm.tipo" class="form-select">
            <option value="CPF">CPF</option>
            <option value="EMAIL">E-mail</option>
            <option value="TELEFONE">Telefone</option>
            <option value="ALEATORIA">Chave Aleatória</option>
          </select>
        </div>
        <div class="mb-4">
          <label class="form-label fw-bold small">Chave PIX</label>
          <input type="text" v-model="pixForm.chave" class="form-control" placeholder="Digite sua chave...">
        </div>
        <button class="btn btn-success w-100 fw-bold py-3" @click="salvarPix" :disabled="savingPix || !pixForm.chave">
          <span v-if="savingPix" class="spinner-border spinner-border-sm me-2"></span>
          Salvar Chave PIX
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import { useToast } from 'vue-toastification';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Corrigir ícones do Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

export default {
  name: 'CourierDashboard',
  setup() {
    return { toast: useToast() };
  },
  data() {
    return {
      map: null,
      marker: null,
      estado: 'WAITING', // WAITING, PROPOSAL, TO_PHARMACY, TO_CLIENT
      propostaAtual: null,
      corridaAtual: null,
      inputCodigoRetirada: '',
      inputCodigoEntrega: '',
      loadingAction: false,
      myLat: -23.5505,
      myLng: -46.6333,
      watchId: null,
      pollInterval: null,
      showPixModal: false,
      savingPix: false,
      motoboy: null,
      pixForm: { tipo: 'CPF', chave: '' }
    };
  },
  async mounted() {
    this.initMap();
    this.startLocationTracking();
    this.startPollingPropostas();
  },
  beforeUnmount() {
    if (this.watchId) navigator.geolocation.clearWatch(this.watchId);
    if (this.pollInterval) clearInterval(this.pollInterval);
  },
  methods: {
    initMap() {
      if (!L) {
        console.error("Leaflet não carregado");
        return;
      }
      this.map = L.map('courier-map', { zoomControl: false }).setView([this.myLat, this.myLng], 15);
      L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        maxZoom: 19
      }).addTo(this.map);

      // Ícone customizado de moto
      const motoIcon = L.divIcon({
        className: 'custom-moto-icon',
        html: '<i class="fas fa-motorcycle"></i>',
        iconSize: [36, 36],
        iconAnchor: [18, 18]
      });

      this.marker = L.marker([this.myLat, this.myLng], { icon: motoIcon }).addTo(this.map);
    },
    startLocationTracking() {
      if ("geolocation" in navigator) {
        this.watchId = navigator.geolocation.watchPosition(async (position) => {
          this.myLat = position.coords.latitude;
          this.myLng = position.coords.longitude;
          
          if (this.marker) {
            this.marker.setLatLng([this.myLat, this.myLng]);
            this.map.panTo([this.myLat, this.myLng]);
          }

          // Enviar pro backend se tiver ID
          const user = JSON.parse(localStorage.getItem('user'));
          if (user) {
            try {
              const motoboys = await api.get('/motoboys');
              const mb = motoboys.data.find(m => m.usuario?.id === user.id);
              if (mb) {
                this.motoboy = mb;
                if (!mb.chavePix || mb.chavePix === '') {
                  this.showPixModal = true;
                }
                await api.patch(`/motoboys/${mb.id}/localizacao?lat=${this.myLat}&lng=${this.myLng}`);
              }
            } catch (e) {}
          }
        }, (error) => {
          console.warn("Geolocalização negada/falha:", error);
        }, { enableHighAccuracy: true });
      }
    },
    startPollingPropostas() {
      this.pollInterval = setInterval(async () => {
        if (this.estado !== 'WAITING') return;

        try {
          // Busca pedidos PRONTO_PARA_ENTREGA
          const res = await api.get('/pedidos'); // Num cenario real, existiria um /pedidos/disponiveis
          const disponiveis = res.data.filter(p => p.status === 'ENVIADO' && p.motoboy === null);
          
          if (disponiveis.length > 0) {
            this.propostaAtual = disponiveis[0]; // Pega a primeira
            this.estado = 'PROPOSAL';
            this.toast.info("Nova corrida disponível!");
          }
        } catch (e) {
          console.error(e);
        }
      }, 5000);
    },
    async aceitarCorrida() {
      try {
        this.loadingAction = true;
        const user = JSON.parse(localStorage.getItem('user'));
        const motoboys = await api.get('/motoboys');
        const mb = motoboys.data.find(m => m.usuario?.id === user.id);
        
        await api.post(`/pedidos/${this.propostaAtual.id}/aceitar?motoboyId=${mb.id}`);
        this.toast.success("Corrida Aceita! Dirija-se à Farmácia.");
        this.corridaAtual = this.propostaAtual;
        this.propostaAtual = null;
        this.estado = 'TO_PHARMACY';
      } catch (e) {
        this.toast.error(e.response?.data?.mensagem || "Alguém já pegou essa corrida.");
        this.estado = 'WAITING';
      } finally {
        this.loadingAction = false;
      }
    },
    async confirmarRetirada() {
      try {
        this.loadingAction = true;
        const user = JSON.parse(localStorage.getItem('user'));
        const motoboys = await api.get('/motoboys');
        const mb = motoboys.data.find(m => m.usuario?.id === user.id);

        await api.post(`/pedidos/${this.corridaAtual.id}/retirada?motoboyId=${mb.id}&codigoRetirada=${this.inputCodigoRetirada}`);
        this.toast.success("Retirada Confirmada! Vá para o cliente.");
        this.estado = 'TO_CLIENT';
        this.inputCodigoRetirada = '';
      } catch (e) {
        this.toast.error("Código Inválido.");
      } finally {
        this.loadingAction = false;
      }
    },
    async confirmarEntrega() {
      try {
        this.loadingAction = true;
        const user = JSON.parse(localStorage.getItem('user'));
        const motoboys = await api.get('/motoboys');
        const mb = motoboys.data.find(m => m.usuario?.id === user.id);

        await api.post(`/pedidos/${this.corridaAtual.id}/entrega?motoboyId=${mb.id}&codigoEntrega=${this.inputCodigoEntrega}`);
        this.toast.success("Entrega Concluída com Sucesso! Seu repasse PIX foi liberado.");
        this.estado = 'WAITING';
        this.corridaAtual = null;
        this.inputCodigoEntrega = '';
      } catch (e) {
        this.toast.error("Código Inválido.");
      } finally {
        this.loadingAction = false;
      }
    },
    async salvarPix() {
      this.savingPix = true;
      try {
        await api.patch(`/motoboys/${this.motoboy.id}/pix`, {
          chavePix: this.pixForm.chave,
          tipoChavePix: this.pixForm.tipo
        });
        this.showPixModal = false;
        this.toast.success('Chave PIX cadastrada com sucesso!');
      } catch (e) {
        this.toast.error('Erro ao salvar Chave PIX.');
      } finally {
        this.savingPix = false;
      }
    }
  }
};
</script>

<style scoped>
.courier-dashboard { position: relative; height: calc(100vh - 60px); width: 100%; overflow: hidden; }
.map-container { position: absolute; inset: 0; z-index: 1; }

.bottom-sheet {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: white; z-index: 10;
  border-top-left-radius: 24px; border-top-right-radius: 24px;
  padding: 1.5rem; transition: transform 0.3s;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.1);
}

.sheet-handle { width: 40px; height: 5px; background: #e2e8f0; border-radius: 10px; margin: 0 auto 1rem; }

.status-indicator { display: inline-flex; align-items: center; gap: 8px; font-weight: 600; }
.pulse { display: block; width: 10px; height: 10px; border-radius: 50%; background: #22c55e; box-shadow: 0 0 0 rgba(34, 197, 94, 0.4); animation: pulse 2s infinite; }

@keyframes pulse {
  0% { -moz-box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.4); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.4); }
  70% { -moz-box-shadow: 0 0 0 10px rgba(34, 197, 94, 0); box-shadow: 0 0 0 10px rgba(34, 197, 94, 0); }
  100% { -moz-box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

.custom-moto-icon {
  background: #1e293b; color: white; border-radius: 50%; border: 2px solid white;
  display: flex !important; align-items: center; justify-content: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.3);
}

.input-group input { background: #f8fafc; border: 1px solid #e2e8f0; letter-spacing: 0.2em; font-size: 1.25rem; }

.pix-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(5px); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.pix-modal-card { background: #fff; width: 90%; max-width: 400px; padding: 2rem; border-radius: 24px; box-shadow: 0 25px 50px rgba(0,0,0,0.25); animation: fadeIn 0.4s ease; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>
