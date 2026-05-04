<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE CONFIGURAÇÃO ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Configurações da Unidade</h3>
        <p class="editorial-subtitle">Gestão da identidade corporativa e parâmetros de liquidação financeira</p>
      </div>
      <div class="header-tools">
        <button class="btn-save-premium" @click="salvarConfig" :disabled="isSaving">
          <i v-if="isSaving" class="fas fa-circle-notch fa-spin"></i>
          <i v-else class="fas fa-shield-check"></i>
          Salvar Alterações
        </button>
      </div>
    </header>

    <div v-if="isLoading" class="loading-overlay-premium">
      <div class="cf-spinner"></div>
      <span>Sincronizando perfil da unidade...</span>
    </div>

    <div v-else class="settings-editorial-grid">
      <!-- Coluna Principal: Dossier Corporativo -->
      <div class="settings-col flex-2">
        <div class="editorial-card-premium">
          <header class="card-header-premium">
            <div class="header-main">
              <i class="fas fa-id-card"></i>
              <h5>Identidade Jurídica</h5>
            </div>
          </header>
          <div class="card-body-premium">
            <div class="settings-form-grid">
              <div class="form-group col-span-2">
                <label class="editorial-label">Nome Fantasia (Exibição Pública)</label>
                <input v-model="form.nome" type="text" class="editorial-input lg" placeholder="Ex: Farmácia Click Matriz">
              </div>
              <div class="form-group">
                <label class="editorial-label">Inscrição CNPJ</label>
                <div class="locked-input-settings">
                  <i class="fas fa-lock"></i>
                  <input v-model="form.cnpj" readonly>
                </div>
              </div>
              <div class="form-group">
                <label class="editorial-label">Telefone de Contato</label>
                <input v-model="form.telefone" v-mask="'phone'" type="text" class="editorial-input" placeholder="(00) 00000-0000">
              </div>
              <div class="form-group">
                <label class="editorial-label">CEP</label>
                <input v-model="form.cep" v-mask="'cep'" type="text" class="editorial-input" placeholder="00000-000">
              </div>
              <div class="form-group col-span-2">
                <label class="editorial-label">E-mail de Notificações</label>
                <input v-model="form.email" type="email" class="editorial-input" placeholder="contato@farmacia.com.br">
              </div>
              <div class="form-group col-span-2">
                <label class="editorial-label">Localização da Unidade</label>
                <input v-model="form.endereco" type="text" class="editorial-input" placeholder="Logradouro, Bairro, Cidade - UF">
              </div>
            </div>
          </div>
        </div>

        <div class="editorial-card-premium mt-5">
          <header class="card-header-premium gold">
            <div class="header-main">
              <i class="fas fa-bank"></i>
              <h5>Custódia Financeira (PIX)</h5>
            </div>
          </header>
          <div class="card-body-premium">
            <div class="finance-alert shadow-sm mb-4">
              <i class="fas fa-shield-halved"></i>
              <div class="alert-text">
                <strong>Verificação de Repasse</strong>
                <p>Mantenha sua chave PIX atualizada para garantir a liquidação automática das suas vendas.</p>
              </div>
            </div>
            <div class="grid-2 gap-4">
              <div class="form-group">
                <label class="editorial-label">Tipo de Chave</label>
                <select v-model="form.tipoChavePix" class="editorial-select-premium">
                  <option value="CNPJ">CNPJ</option>
                  <option value="CPF">CPF</option>
                  <option value="EMAIL">E-mail</option>
                  <option value="TELEFONE">Telefone</option>
                  <option value="ALEATORIA">Chave Aleatória</option>
                </select>
              </div>
              <div class="form-group">
                <label class="editorial-label">Chave para Recebimento</label>
                <input v-model="form.chavePix" type="text" class="editorial-input" 
                       v-mask="form.tipoChavePix === 'CNPJ' ? 'cnpj' : (form.tipoChavePix === 'CPF' ? 'cpf' : (form.tipoChavePix === 'TELEFONE' ? 'phone' : ''))"
                       placeholder="Insira a chave exata">
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Coluna Lateral: Branding -->
      <div class="settings-col flex-1">
        <div class="editorial-card-premium branding-card">
          <header class="card-header-premium">
            <div class="header-main">
              <i class="fas fa-image"></i>
              <h5>Visual da Marca</h5>
            </div>
          </header>
          <div class="card-body-premium text-center">
            <div class="logo-display-wrap shadow-sm">
              <img :src="form.fotoUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'">
            </div>
            <div class="form-group mt-4 text-start">
              <label class="editorial-label">Vetor do Logotipo (URL)</label>
              <input v-model="form.fotoUrl" type="text" class="editorial-input" placeholder="https://link-da-imagem.png">
            </div>
            <div class="branding-guide mt-4">
              <i class="fas fa-circle-exclamation"></i>
              <span>Use imagens com fundo transparente para melhor integração no App.</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';

export default {
  name: 'PharmacySettings',
  data() {
    return {
      isLoading: true,
      isSaving: false,
      farmaciaId: null,
      form: {
        nome: '', cnpj: '', telefone: '', email: '',
        endereco: '', fotoUrl: '', chavePix: '', tipoChavePix: 'CNPJ'
      }
    };
  },
  async mounted() {
    await this.fetchFarmacia();
  },
  methods: {
    async fetchFarmacia() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data } = await api.get('/farmacias');
        const far = data.find(f => f.email === user.email);
        if (far) {
          this.farmaciaId = far.id;
          this.form = { ...far };
        }
      } catch (err) {
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async salvarConfig() {
      this.isSaving = true;
      try {
        const payload = { ...this.form };
        delete payload.usuario;
        delete payload.dataCadastro;
        
        await api.put(`/farmacias/${this.farmaciaId}`, payload);
        alert('Configurações salvas com sucesso!');
      } catch (err) {
        alert('Erro ao salvar configurações.');
      } finally {
        this.isSaving = false;
      }
    }
  }
};
</script>

<style scoped>
.cf-mgmt-premium { animation: fadeIn 0.6s var(--cf-ease); }

/* Header */
.mgmt-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.btn-save-premium { background: var(--cf-green); color: #fff; border: none; padding: 0.85rem 2rem; border-radius: 50px; font-weight: 700; display: flex; align-items: center; gap: 0.75rem; cursor: pointer; transition: all 0.3s; }
.btn-save-premium:hover { background: var(--cf-green-dark); transform: translateY(-2px); box-shadow: 0 10px 25px rgba(42,92,69,0.2); }

/* Grid Layout */
.settings-editorial-grid { display: flex; gap: 2.5rem; }
.settings-col { display: flex; flex-direction: column; }
.flex-2 { flex: 2; }
.flex-1 { flex: 1; }

.editorial-card-premium { background: #fff; border-radius: 32px; border: 1px solid var(--cf-border); overflow: hidden; box-shadow: var(--cf-shadow-sm); }
.card-header-premium { padding: 1.5rem 2rem; border-bottom: 1px solid var(--cf-border); background: var(--cf-ivory-light); display: flex; align-items: center; justify-content: space-between; }
.card-header-premium.gold { background: var(--cf-gold-light); }
.header-main { display: flex; align-items: center; gap: 1rem; }
.header-main i { color: var(--cf-gold); font-size: 1.1rem; }
.header-main h5 { font-family: var(--cf-serif); font-size: 1.25rem; font-weight: 600; margin: 0; }

.card-body-premium { padding: 2.5rem; }
.settings-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
.col-span-2 { grid-column: span 2; }

.editorial-label { display: block; font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; margin-bottom: 0.6rem; }
.editorial-input, .editorial-select-premium { width: 100%; padding: 0.85rem 1.25rem; border-radius: 14px; border: 1px solid var(--cf-border); outline: none; font-size: 0.95rem; font-weight: 600; color: var(--cf-text-dark); background: var(--cf-ivory-light); transition: all 0.2s; }
.editorial-input:focus { border-color: var(--cf-gold); background: #fff; box-shadow: 0 0 0 4px rgba(184,149,80,0.1); }
.editorial-input.lg { font-size: 1.2rem; font-weight: 700; }

.locked-input-settings { position: relative; background: #f8fafc; border: 1px solid var(--cf-border); border-radius: 14px; padding: 0.85rem 1.25rem; }
.locked-input-settings i { position: absolute; right: 1.25rem; top: 1.1rem; color: var(--cf-text-faint); }
.locked-input-settings input { border: none; background: transparent; outline: none; width: 100%; font-weight: 700; color: var(--cf-text-muted); cursor: not-allowed; }

/* Finance Alert */
.finance-alert { background: #fffbeb; border: 1px solid #fde68a; border-radius: 16px; padding: 1.25rem; display: flex; gap: 1rem; align-items: flex-start; }
.finance-alert i { color: #d97706; font-size: 1.2rem; margin-top: 2px; }
.alert-text strong { display: block; font-size: 0.85rem; font-weight: 800; color: #92400e; margin-bottom: 2px; }
.alert-text p { font-size: 0.75rem; color: #b45309; margin: 0; }

/* Branding Card */
.logo-display-wrap { width: 220px; height: 220px; border-radius: 32px; background: #fff; border: 1px solid var(--cf-border); padding: 1.5rem; margin: 0 auto; display: flex; align-items: center; justify-content: center; }
.logo-display-wrap img { width: 100%; height: 100%; object-fit: contain; }
.branding-guide { display: flex; align-items: center; gap: 0.6rem; color: var(--cf-text-faint); font-size: 0.65rem; font-weight: 700; text-transform: uppercase; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>
