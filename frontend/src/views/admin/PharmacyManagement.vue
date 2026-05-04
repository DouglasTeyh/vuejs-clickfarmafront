<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DA REDE ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Rede de Unidades</h3>
        <p class="editorial-subtitle">Governança de parceiros e infraestrutura de repasses PIX</p>
      </div>
      <div class="header-tools">
        <div class="cf-input-group">
          <i class="fas fa-search"></i>
          <input v-model="busca" type="text" placeholder="Buscar unidade, CNPJ ou cidade...">
        </div>
        <button class="btn-refresh" @click="carregar" :disabled="isLoading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
        </button>
      </div>
    </header>

    <!-- ═══ GRID DE UNIDADES ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Mapeando rede de parceiros...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Unidade</th>
              <th>Documentação</th>
              <th>Logradouro</th>
              <th>Operacional</th>
              <th>Status Fiscal</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in filtradas" :key="f.id" class="row-hover">
              <td class="ps-5">
                <div class="entity-cell">
                  <div class="entity-avatar green">
                    <i class="fas fa-hospital"></i>
                  </div>
                  <div class="entity-meta">
                    <span class="entity-name">{{ f.nome }}</span>
                    <span class="entity-sub">ID: #{{ f.id }} • {{ f.email }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="doc-cell">
                  <span class="doc-val">{{ f.cnpj }}</span>
                  <span class="doc-label">CNPJ REGISTRADO</span>
                </div>
              </td>
              <td>
                <div class="loc-cell">
                  <span class="loc-city">{{ f.cidade || 'Localização não definida' }}</span>
                  <span class="loc-addr">{{ f.endereco?.split(',')[0] }}</span>
                </div>
              </td>
              <td>
                <div class="contact-cell">
                  <i class="fas fa-phone-alt me-2 text-muted"></i>
                  <span class="contact-val">{{ f.telefone || 'Sem contato' }}</span>
                </div>
              </td>
              <td>
                <div class="status-pill active">
                  <span class="pill-dot"></span>
                  <span class="pill-text">Operação Ativa</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-circle" @click="abrirEdicao(f)" title="Configurar Unidade">
                    <i class="fas fa-sliders"></i>
                  </button>
                  <button class="action-btn-circle danger" @click="confirmarDelete(f)" title="Suspender Operação">
                    <i class="fas fa-ban"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!filtradas.length">
              <td colspan="6" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-store-slash"></i>
                  <p>Nenhuma farmácia localizada nos registros ativos.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: CONFIGURAÇÃO DE UNIDADE ═══ -->
    <div v-if="modalEdicao" class="modal-premium-overlay" @click.self="fecharModal">
      <div class="config-modal animate__animated animate__fadeInUp">
        <header class="config-header">
          <div class="header-brand">
            <div class="config-icon"><i class="fas fa-cog"></i></div>
            <div class="config-meta">
              <h5>Configurar Unidade</h5>
              <span class="config-id">REGISTRO MESTRE #{{ editandoId }}</span>
            </div>
          </div>
          <button class="close-modal" @click="fecharModal"><i class="fas fa-times"></i></button>
        </header>

        <div class="config-content cf-hide-scrollbar">
          <div class="config-grid">
            <div class="form-group col-span-2">
              <label class="editorial-label">Razão Social / Nome Fantasia</label>
              <input v-model="form.nome" class="editorial-input" placeholder="Ex: Farmácia Clickfar Filial 01">
            </div>
            
            <div class="form-group">
              <label class="editorial-label">CNPJ Operacional</label>
              <input v-model="form.cnpj" class="editorial-input" placeholder="00.000.000/0001-00">
            </div>

            <div class="form-group">
              <label class="editorial-label">Cidade / UF</label>
              <input v-model="form.cidade" class="editorial-input" placeholder="Cidade - UF">
            </div>

            <div class="form-group col-span-2">
              <label class="editorial-label">Logradouro Completo</label>
              <input v-model="form.endereco" class="editorial-input" placeholder="Rua, Número, Bairro, Complemento">
            </div>

            <div class="section-divider col-span-2">
              <span>Inteligência Financeira (Repasses)</span>
            </div>

            <div class="form-group">
              <label class="editorial-label">Modalidade de Chave PIX</label>
              <select v-model="form.tipoChavePix" class="editorial-select">
                <option value="CNPJ">CNPJ</option>
                <option value="EMAIL">E-mail Corporativo</option>
                <option value="TELEFONE">Telefone</option>
                <option value="ALEATORIA">Chave Aleatória</option>
              </select>
            </div>

            <div class="form-group">
              <label class="editorial-label">Chave de Destino PIX</label>
              <input v-model="form.chavePix" class="editorial-input" placeholder="Insira a chave validada">
            </div>
          </div>
        </div>

        <footer class="config-footer">
          <button class="btn-cancel" @click="fecharModal">Descartar</button>
          <button class="btn-save" @click="salvar" :disabled="salvando">
            <i v-if="salvando" class="fas fa-circle-notch fa-spin me-2"></i>
            Sincronizar Unidade
          </button>
        </footer>
      </div>
    </div>

    <!-- ═══ MODAL: SUSPENSÃO DE OPERAÇÃO ═══ -->
    <div v-if="paraDeletar" class="modal-premium-overlay warning" @click.self="paraDeletar = null">
      <div class="alert-modal animate__animated animate__headShake">
        <div class="alert-icon"><i class="fas fa-exclamation-triangle"></i></div>
        <h4>Suspender Unidade?</h4>
        <p>A unidade <strong>{{ paraDeletar.nome }}</strong> será bloqueada de receber novos pedidos imediatamente.</p>
        <div class="alert-actions">
          <button class="btn-cancel flex-1" @click="paraDeletar = null">Manter Operação</button>
          <button class="btn-confirm-delete flex-1" @click="deletar">Sim, Bloquear</button>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';

export default {
  name: 'PharmacyManagement',
  data() {
    return { farmacias: [], isLoading: true, busca: '', modalEdicao: false, form: {}, editandoId: null, paraDeletar: null, salvando: false };
  },
  computed: {
    filtradas() {
      if (!this.busca) return this.farmacias;
      const t = this.busca.toLowerCase();
      return this.farmacias.filter(f => f.nome?.toLowerCase().includes(t) || f.cidade?.toLowerCase().includes(t) || f.cnpj?.includes(t) || f.email?.toLowerCase().includes(t));
    }
  },
  async mounted() { await this.carregar(); },
  methods: {
    async carregar() {
      this.isLoading = true;
      try { const { data } = await api.get('/farmacias'); this.farmacias = data; }
      catch (e) { console.error(e); }
      finally { this.isLoading = false; }
    },
    abrirEdicao(f) { this.editandoId = f.id; this.form = { ...f }; this.modalEdicao = true; },
    fecharModal() { this.modalEdicao = false; this.form = {}; this.editandoId = null; },
    async salvar() {
      this.salvando = true;
      try { await api.put(`/farmacias/${this.editandoId}`, this.form); await this.carregar(); this.fecharModal(); }
      catch { alert('Erro ao salvar.'); }
      finally { this.salvando = false; }
    },
    confirmarDelete(f) { this.paraDeletar = f; },
    async deletar() {
      try { await api.delete(`/farmacias/${this.paraDeletar.id}`); await this.carregar(); this.paraDeletar = null; }
      catch { alert('Erro ao remover.'); }
    },
    d(dt) { if (!dt) return '—'; return new Date(dt).toLocaleDateString('pt-BR'); }
  }
};
</script>

<style scoped>
.cf-mgmt-premium { animation: fadeIn 0.6s var(--cf-ease); }

/* Header */
.mgmt-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.header-tools { display: flex; align-items: center; gap: 1rem; }
.cf-input-group { background: #fff; border: 1px solid var(--cf-border); border-radius: 14px; padding: 0.65rem 1.25rem; display: flex; align-items: center; gap: 0.75rem; width: 320px; box-shadow: var(--cf-shadow-sm); }
.cf-input-group i { color: var(--cf-text-faint); font-size: 0.9rem; }
.cf-input-group input { border: none; outline: none; font-size: 0.9rem; width: 100%; color: var(--cf-text-dark); }
.btn-refresh { width: 46px; height: 46px; border-radius: 14px; background: var(--cf-ivory); border: 1px solid var(--cf-border); color: var(--cf-green); cursor: pointer; transition: all 0.3s; display: flex; align-items: center; justify-content: center; }
.btn-refresh:hover { background: var(--cf-green-xlight); transform: rotate(180deg); }

/* Table Premium */
.cf-table-card-premium { background: #fff; border-radius: 28px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.loading-overlay-premium { padding: 8rem 0; display: flex; flex-direction: column; align-items: center; gap: 1rem; color: var(--cf-text-muted); }

.editorial-table { width: 100%; border-collapse: collapse; }
.editorial-table th { padding: 1.25rem 1rem; background: var(--cf-ivory-light); font-size: 0.7rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); border-bottom: 1px solid var(--cf-border); }
.editorial-table td { padding: 1.5rem 1rem; vertical-align: middle; border-bottom: 1px solid rgba(0,0,0,0.03); }

.entity-cell { display: flex; align-items: center; gap: 1rem; }
.entity-avatar { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; }
.entity-avatar.green { background: var(--cf-green-xlight); color: var(--cf-green); }
.entity-meta { display: flex; flex-direction: column; }
.entity-name { font-size: 1rem; font-weight: 700; color: var(--cf-text-dark); }
.entity-sub { font-size: 0.72rem; color: var(--cf-text-faint); }

.doc-cell { display: flex; flex-direction: column; }
.doc-val { font-family: 'DM Mono', monospace; font-size: 0.85rem; font-weight: 700; color: var(--cf-text-dark); }
.doc-label { font-size: 0.6rem; font-weight: 800; color: var(--cf-text-faint); }

.loc-cell { display: flex; flex-direction: column; }
.loc-city { font-size: 0.85rem; font-weight: 700; color: var(--cf-text-dark); }
.loc-addr { font-size: 0.72rem; color: var(--cf-text-muted); }

.contact-cell { font-size: 0.85rem; font-weight: 700; color: var(--cf-text-muted); }

.status-pill { display: inline-flex; align-items: center; gap: 0.6rem; padding: 0.4rem 1rem; border-radius: 50px; background: var(--cf-ivory); border: 1px solid var(--cf-border); }
.pill-dot { width: 8px; height: 8px; border-radius: 50%; background: #94a3b8; }
.pill-text { font-size: 0.65rem; font-weight: 800; text-transform: uppercase; color: var(--cf-text-muted); }
.status-pill.active { background: var(--cf-green-xlight); border-color: rgba(34,197,94,0.1); }
.status-pill.active .pill-dot { background: #22c55e; }
.status-pill.active .pill-text { color: var(--cf-green); }

.action-group { display: flex; gap: 0.5rem; justify-content: center; }
.action-btn-circle { width: 36px; height: 36px; border-radius: 50%; border: 1px solid var(--cf-border); background: #fff; color: var(--cf-text-muted); cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; }
.action-btn-circle:hover { border-color: var(--cf-gold); color: var(--cf-gold); background: var(--cf-ivory); }
.action-btn-circle.danger:hover { border-color: #ef4444; color: #ef4444; background: #fff1f2; }

/* Config Modal */
.modal-premium-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(8px); z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 2rem; }
.config-modal { background: #fff; width: 100%; max-width: 800px; border-radius: 32px; box-shadow: 0 30px 100px rgba(0,0,0,0.2); overflow: hidden; display: flex; flex-direction: column; }

.config-header { padding: 1.5rem 2.5rem; background: var(--cf-ivory-light); border-bottom: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; }
.header-brand { display: flex; align-items: center; gap: 1.25rem; }
.config-icon { width: 48px; height: 48px; border-radius: 14px; background: var(--cf-green); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.config-meta h5 { font-family: var(--cf-serif); font-size: 1.4rem; font-weight: 600; margin: 0; }
.config-id { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; }
.close-modal { background: none; border: none; color: var(--cf-text-faint); cursor: pointer; font-size: 1.2rem; }

.config-content { padding: 2.5rem; }
.config-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
.col-span-2 { grid-column: span 2; }

.editorial-label { display: block; font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; margin-bottom: 0.5rem; }
.editorial-input, .editorial-select { width: 100%; padding: 0.85rem 1.25rem; border-radius: 14px; border: 1px solid var(--cf-border); outline: none; font-size: 0.9rem; font-weight: 600; color: var(--cf-text-dark); transition: all 0.2s; background: var(--cf-ivory-light); }
.editorial-input:focus { border-color: var(--cf-gold); background: #fff; box-shadow: 0 0 0 4px rgba(184,149,80,0.1); }

.section-divider { grid-column: span 2; padding-top: 1rem; border-top: 1px solid var(--cf-border); margin: 1rem 0; font-size: 0.7rem; font-weight: 800; color: var(--cf-gold); text-transform: uppercase; letter-spacing: 0.15em; }

.config-footer { padding: 1.5rem 2.5rem; background: var(--cf-ivory-light); border-top: 1px solid var(--cf-border); display: flex; justify-content: flex-end; gap: 1rem; }
.btn-cancel { background: #fff; border: 1px solid var(--cf-border); padding: 0.85rem 2rem; border-radius: 50px; font-weight: 700; font-size: 0.85rem; color: var(--cf-text-muted); cursor: pointer; }
.btn-save { background: var(--cf-green); color: #fff; border: none; padding: 0.85rem 2.5rem; border-radius: 50px; font-weight: 700; font-size: 0.85rem; cursor: pointer; transition: all 0.3s; box-shadow: 0 4px 15px rgba(42,92,69,0.2); }
.btn-save:hover { background: var(--cf-green-dark); transform: translateY(-2px); }

/* Alert Modal */
.alert-modal { background: #fff; padding: 3rem; border-radius: 32px; width: 100%; max-width: 440px; text-align: center; box-shadow: 0 25px 80px rgba(0,0,0,0.15); }
.alert-icon { width: 70px; height: 70px; background: #fff1f2; color: #ef4444; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2rem; margin: 0 auto 1.5rem; }
.alert-modal h4 { font-family: var(--cf-serif); font-size: 1.6rem; font-weight: 600; margin-bottom: 1rem; }
.alert-modal p { color: var(--cf-text-muted); font-size: 0.95rem; margin-bottom: 2rem; }
.alert-actions { display: flex; gap: 1rem; }
.btn-confirm-delete { background: #ef4444; color: #fff; border: none; padding: 0.85rem 1rem; border-radius: 50px; font-weight: 700; cursor: pointer; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>
