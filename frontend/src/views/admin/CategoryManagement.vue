<template>
  <div class="cf-mgmt">
    <div class="dash-welcome mb-4">
      <div class="welcome-text">
        <h3 class="dash-page-title">Arquitetura de Categorias</h3>
        <p class="text-muted mb-0">Estruture a navegação do catálogo através de taxonomias otimizadas para SEO.</p>
      </div>
      <div class="dash-actions">
        <button class="cf-btn-primary shadow-sm" @click="abrirModalCriacao">
          <i class="fas fa-layer-group me-2"></i>Nova Categoria
        </button>
      </div>
    </div>

    <!-- Tabela de Categorias Premium -->
    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div><span>Indexando estrutura taxonômica...</span>
      </div>
      <div v-else class="table-responsive cf-hide-scrollbar">
        <table class="cf-table">
          <thead>
            <tr>
              <th class="ps-4" style="width: 120px;">ID</th>
              <th>Denominação</th>
              <th>Escopo / Descrição</th>
              <th class="text-center pe-4" style="width: 180px;">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cat in categorias" :key="cat.id">
              <td class="ps-4">
                <div class="cf-td-bold text-muted small">#{{ cat.id }}</div>
              </td>
              <td>
                <div class="cf-avatar-row">
                  <div class="cf-cat-badge shadow-sm"><i class="fas fa-tags"></i></div>
                  <div class="cf-td-bold text-dark">{{ cat.nome }}</div>
                </div>
              </td>
              <td>
                <div class="text-muted small fw-bold">{{ cat.descricao || 'Nenhuma descrição técnica informada.' }}</div>
                <div class="extra-small text-gold opacity-75 mt-1 fw-bold text-uppercase letter-spacing-1">Segmento Ativo</div>
              </td>
              <td class="text-center pe-4">
                <div class="d-flex justify-content-center gap-2">
                  <button class="cf-icon-btn shadow-sm" @click="abrirModalEdicao(cat)" title="Editar Taxonomia">
                    <i class="fas fa-pen-nib"></i>
                  </button>
                  <button class="cf-icon-btn danger shadow-sm" @click="deletarCategoria(cat.id)" title="Remover Segmento">
                    <i class="fas fa-trash-can"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="categorias.length === 0">
              <td colspan="4" class="cf-empty py-5">
                <i class="fas fa-sitemap fa-3x mb-3 opacity-10"></i>
                <p class="fw-bold text-muted">Aguardando definição de estrutura de categorias.</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Criar/Editar Premium -->
    <div v-if="showModal" class="cf-modal-overlay" @click.self="showModal = false">
      <div class="cf-modal-box animate__animated animate__fadeInUp dash-card" style="width: 520px;">
        <div class="cf-modal-header border-bottom bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-primary-subtle text-primary shadow-sm">
              <i class="fas fa-tag"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">{{ editandoId ? 'Refinar Categoria' : 'Nova Taxonomia' }}</h5>
              <p class="mb-0 text-muted extra-small fw-bold text-uppercase letter-spacing-1">Configuração de Catálogo</p>
            </div>
          </div>
          <button class="btn-close-custom shadow-sm" @click="showModal = false"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body p-4 bg-white">
           <form id="categoryForm" @submit.prevent="salvarCategoria">
            <div class="mb-4">
              <label class="cf-label-premium">Nome de Exibição</label>
              <input type="text" class="cf-input-premium" v-model="form.nome" required placeholder="Ex: Higiene Pessoal, Dermocosméticos...">
            </div>
            <div class="mb-0">
              <label class="cf-label-premium">Descrição Técnica (Opcional)</label>
              <textarea class="cf-input-premium" v-model="form.descricao" rows="4" placeholder="Detalhe o escopo desta categoria para melhor indexação..."></textarea>
            </div>
           </form>
        </div>

        <div class="cf-modal-footer bg-light-subtle p-3 border-top">
          <button type="button" class="btn btn-outline-secondary fw-bold px-4 rounded-pill" @click="showModal = false">Cancelar</button>
          <button type="submit" form="categoryForm" class="cf-btn-primary px-4 shadow-sm" :disabled="isSaving">
            <i v-if="isSaving" class="spinner-border spinner-border-sm me-2"></i>
            {{ editandoId ? 'Sincronizar Alterações' : 'Efetivar Categoria' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CategoryManagement',
  data() {
    return {
      categorias: [],
      isLoading: true,
      isSaving: false,
      showModal: false,
      editandoId: null,
      form: {
        nome: '',
        descricao: ''
      }
    }
  },
  async mounted() {
    await this.fetchCategorias();
  },
  methods: {
    async fetchCategorias() {
      this.isLoading = true;
      try {
        const res = await api.get('/categorias');
        this.categorias = res.data;
      } catch (err) {
        console.error('Erro ao buscar categorias:', err);
      } finally {
        this.isLoading = false;
      }
    },
    abrirModalCriacao() {
      this.editandoId = null;
      this.form = { nome: '', descricao: '' };
      this.showModal = true;
    },
    abrirModalEdicao(cat) {
      this.editandoId = cat.id;
      this.form = { nome: cat.nome, descricao: cat.descricao };
      this.showModal = true;
    },
    async salvarCategoria() {
      this.isSaving = true;
      try {
        if (this.editandoId) {
          await api.put(`/categorias/${this.editandoId}`, this.form);
        } else {
          await api.post('/categorias', this.form);
        }
        this.showModal = false;
        await this.fetchCategorias();
        if (this.$store) this.$store.dispatch('fetchCategories');
        if (window.$toast) window.$toast.addToast('Estrutura atualizada!', 'success');
      } catch (err) {
        console.error('Erro ao salvar categoria:', err);
        alert('Erro ao salvar categoria.');
      } finally {
        this.isSaving = false;
      }
    },
    async deletarCategoria(id) {
      if (confirm('Deseja realmente excluir esta categoria? Isso pode afetar produtos vinculados.')) {
        try {
          await api.delete(`/categorias/${id}`);
          await this.fetchCategorias();
          if (this.$store) this.$store.dispatch('fetchCategories');
        } catch (err) {
          alert('Erro ao deletar categoria. Verifique se existem produtos vinculados a ela.');
        }
      }
    }
  }
}
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; animation: fadeIn 0.5s ease-out; }

.cf-table-card { background: #fff; border-radius: 24px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }

.cf-cat-badge { width: 40px; height: 40px; border-radius: 12px; background: var(--cf-ivory); color: var(--cf-gold); display: flex; align-items: center; justify-content: center; font-size: 1.1rem; border: 1px solid rgba(184,149,80,0.1); }

.cf-avatar-row { display: flex; align-items: center; gap: 1rem; }

.extra-small { font-size: 0.62rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.letter-spacing-1 { letter-spacing: 0.08em; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
