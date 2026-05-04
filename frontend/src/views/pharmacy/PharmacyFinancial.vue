<template>
  <div class="cf-mgmt">
    <div class="dash-welcome mb-4">
      <div class="welcome-text">
        <h3 class="dash-page-title">Gestão Financeira</h3>
        <p class="text-muted mb-0">Monitore seus repasses e configure seus dados de recebimento.</p>
      </div>
    </div>

    <div class="row g-4">
      <!-- Configuração de PIX -->
      <div class="col-lg-5">
        <div class="dash-card h-100">
          <div class="p-4 border-bottom">
            <h5 class="mb-0 fw-bold"><i class="fas fa-qrcode me-2 text-warning"></i>Dados para Repasse</h5>
            <p class="text-muted small mt-1 mb-0">Defina sua chave PIX para recebimento das vendas.</p>
          </div>
          <div class="p-4">
            <div v-if="loadingPix" class="text-center py-4">
              <div class="cf-spinner mx-auto"></div>
            </div>
            <form v-else @submit.prevent="salvarPix">
              <div class="mb-3">
                <label class="cf-label-premium">Tipo de Chave</label>
                <select v-model="pix.tipo" class="cf-select w-100" required>
                  <option value="CNPJ">CNPJ</option>
                  <option value="CPF">CPF</option>
                  <option value="EMAIL">E-mail</option>
                  <option value="TELEFONE">Telefone</option>
                  <option value="ALEATORIA">Chave Aleatória</option>
                </select>
              </div>
              <div class="mb-4">
                <label class="cf-label-premium">Chave PIX</label>
                <input v-model="pix.chave" type="text" class="cf-input-premium" 
                       v-mask="pix.tipo === 'CNPJ' ? 'cnpj' : (pix.tipo === 'CPF' ? 'cpf' : (pix.tipo === 'TELEFONE' ? 'phone' : ''))"
                       placeholder="Insira sua chave aqui..." required>
                <div class="extra-small text-muted mt-2">
                  <i class="fas fa-info-circle me-1"></i>Certifique-se de que a chave está correta.
                </div>
              </div>
              <button type="submit" class="cf-btn-premium w-100" :disabled="salvandoPix">
                <i v-if="salvandoPix" class="spinner-border spinner-border-sm me-2"></i>
                <i v-else class="fas fa-check-circle me-2"></i>
                {{ salvandoPix ? 'Sincronizando...' : 'Atualizar Dados PIX' }}
              </button>
            </form>

            <div class="cf-info-alert mt-4">
              <i class="fas fa-shield-halved me-2"></i>
              <small>Os repasses são processados semanalmente após a validação das entregas.</small>
            </div>
          </div>
        </div>
      </div>

      <!-- Histórico de Repasses -->
      <div class="col-lg-7">
        <div class="dash-card h-100">
          <div class="p-4 border-bottom">
            <h5 class="mb-0 fw-bold"><i class="fas fa-history me-2 text-primary"></i>Histórico de Recebimentos</h5>
            <p class="text-muted small mt-1 mb-0">Acompanhe todos os pagamentos realizados pela plataforma.</p>
          </div>
          <div class="p-0">
            <div v-if="loadingHistorico" class="text-center py-5">
              <div class="cf-spinner mx-auto"></div>
            </div>
            <div v-else class="table-responsive cf-hide-scrollbar">
              <table class="dash-table">
                <thead>
                  <tr>
                    <th>Período</th>
                    <th>Valor Líquido</th>
                    <th>Status</th>
                    <th>Data</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="pg in pagamentos" :key="pg.id">
                    <td><span class="fw-bold text-dark">{{ formatarPeriodo(pg.referenciaPeriodo) }}</span></td>
                    <td class="text-success fw-bold">R$ {{ pg.valorLiquido.toFixed(2) }}</td>
                    <td>
                      <span class="cf-status-badge" :class="pg.status === 'PAGO' ? 's-done' : 's-pending'">
                        {{ pg.status }}
                      </span>
                    </td>
                    <td class="small text-muted">{{ formatarData(pg.dataPagamento || pg.dataCriacao) }}</td>
                  </tr>
                  <tr v-if="pagamentos.length === 0">
                    <td colspan="4" class="cf-empty py-5">
                      <i class="fas fa-file-invoice-dollar fa-2x mb-3 opacity-25"></i>
                      <p>Nenhum repasse registrado até o momento.</p>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import farmaciasService from '@/services/farmaciasService';

export default {
  name: 'PharmacyFinancial',
  data() {
    return {
      pix: { tipo: 'CNPJ', chave: '' },
      pagamentos: [],
      farmaciaId: null,
      loadingPix: true,
      salvandoPix: false,
      loadingHistorico: true
    }
  },
  async mounted() {
    await this.carregarDados();
  },
  methods: {
    async carregarDados() {
      try {
        const user = JSON.parse(localStorage.getItem('user'));
        const farmacias = await farmaciasService.listarTodas();
        const farmacia = farmacias.data.find(f => f.email === user.email);
        
        if (farmacia) {
          this.farmaciaId = farmacia.id;
          this.pix.chave = farmacia.chavePix || '';
          this.pix.tipo = farmacia.tipoChavePix || 'CNPJ';
          this.loadingPix = false;

          const resP = await api.get(`/pagamentos/farmacia/${this.farmaciaId}`);
          this.pagamentos = resP.data;
        }
      } catch (err) {
        console.error(err);
      } finally {
        this.loadingPix = false;
        this.loadingHistorico = false;
      }
    },
    async salvarPix() {
      this.salvandoPix = true;
      try {
        await api.patch(`/farmacias/${this.farmaciaId}/pix`, {
          chavePix: this.pix.chave,
          tipoChavePix: this.pix.tipo
        });
        alert('Dados PIX atualizados com sucesso!');
      } catch (err) {
        alert('Erro ao atualizar PIX');
      } finally {
        this.salvandoPix = false;
      }
    },
    formatarPeriodo(p) {
      if (!p) return '—';
      const [ano, mes] = p.split('-');
      const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
      return `${meses[parseInt(mes)-1]} / ${ano}`;
    },
    formatarData(d) {
      if (!d) return '—';
      return new Date(d).toLocaleDateString('pt-BR');
    }
  }
}
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; }

/* Financial Specifics */
.cf-btn-premium { background: var(--cf-green); color: #fff; border: none; padding: 0.9rem; border-radius: 12px; font-weight: 700; transition: all 0.2s; box-shadow: 0 4px 12px rgba(42,92,69,0.15); }
.cf-btn-premium:hover { background: var(--cf-green-dark); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(42,92,69,0.25); }
.cf-btn-premium:disabled { opacity: 0.7; transform: none; }

.cf-info-alert { background: var(--cf-ivory); color: var(--cf-text-muted); padding: 1rem; border-radius: 12px; border: 1px solid var(--cf-border); display: flex; align-items: flex-start; }

.cf-status-badge { font-size: 0.65rem; font-weight: 700; padding: 0.25rem 0.8rem; border-radius: 20px; text-transform: uppercase; letter-spacing: 0.05em; }
.s-pending { background: #FFF8EC; color: #9A6700; }
.s-done { background: var(--cf-green-xlight); color: var(--cf-green-dark); }

/* Dashboard UI Shared Utils */
.cf-select { padding: 0.75rem 1rem; border: 1px solid var(--cf-border); border-radius: 12px; font-family: var(--cf-sans); font-size: 0.9rem; color: var(--cf-text-dark); background: var(--cf-white); outline: none; transition: all 0.2s; }
.cf-select:focus { border-color: var(--cf-green); }

.cf-label-premium { font-size: 0.72rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.45rem; display: block; }
.cf-input-premium {
  width: 100%; padding: 0.8rem 1rem; border-radius: 12px;
  border: 1px solid var(--cf-border); background: #fff;
  font-size: 0.92rem; transition: all 0.2s; outline: none;
}
.cf-input-premium:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

.extra-small { font-size: 0.65rem; }

.cf-loading-row { display: flex; align-items: center; justify-content: center; gap: 1rem; padding: 5rem 2rem; color: var(--cf-text-muted); font-size: 0.9rem; }
.cf-spinner { width: 28px; height: 28px; border: 3px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.cf-empty { text-align: center; color: var(--cf-text-faint); padding: 4rem 2rem; font-size: 0.9rem; }
</style>
