<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DA REDE ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Arquitetura de Categorias</h3>
        <p class="editorial-subtitle">Estruture a navegação do catálogo através de taxonomias otimizadas para SEO.</p>
      </div>
      <div class="header-tools">
        <button class="btn-save" @click="abrirModalCriacao" style="background: var(--cf-gold); box-shadow: 0 4px 15px rgba(184,149,80,0.3);">
          <i class="fas fa-layer-group me-2"></i>Nova Categoria
        </button>
      </div>
    </header>

    <!-- ═══ GRID DE UNIDADES ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Indexando estrutura taxonômica...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Identificador</th>
              <th>Denominação</th>
              <th>Escopo / Descrição</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cat in categorias" :key="cat.id" class="row-hover">
              <td class="ps-5">
                <div class="id-badge">#{{ cat.id }}</div>
              </td>
              <td>
                <div class="entity-cell">
                  <div class="entity-avatar gold">
                    <i class="fas fa-tags"></i>
                  </div>
                  <div class="entity-meta">
                    <span class="entity-name">{{ cat.nome }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="doc-cell">
                  <span class="doc-val">{{ cat.descricao || 'Nenhuma descrição técnica informada.' }}</span>
                  <span class="doc-label mt-1 text-gold">SEGMENTO ATIVO</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-circle" @click="abrirModalEdicao(cat)" title="Editar Taxonomia">
                    <i class="fas fa-pen-nib"></i>
                  </button>
                  <button class="action-btn-circle danger" @click="deletarCategoria(cat.id)" title="Remover Segmento">
                    <i class="fas fa-trash-can"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="categorias.length === 0">
              <td colspan="4" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-sitemap"></i>
                  <p>Aguardando definição de estrutura de categorias.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: CONFIGURAÇÃO DE CATEGORIA ═══ -->
    <div v-if="showModal" class="modal-premium-overlay" @click.self="showModal = false">
      <div class="config-modal animate__animated animate__fadeInUp" style="max-width: 520px;">
        <header class="config-header">
          <div class="header-brand">
            <div class="config-icon" style="background: var(--cf-gold);"><i class="fas fa-tag"></i></div>
            <div class="config-meta">
              <h5>{{ editandoId ? 'Refinar Categoria' : 'Nova Taxonomia' }}</h5>
              <span class="config-id">Configuração de Catálogo</span>
            </div>
          </div>
          <button class="close-modal" @click="showModal = false"><i class="fas fa-times"></i></button>
        </header>

        <div class="config-content cf-hide-scrollbar">
          <form id="categoryForm" @submit.prevent="salvarCategoria">
            <div class="config-grid" style="grid-template-columns: 1fr;">
              <div class="form-group col-span-2">
                <label class="editorial-label">Nome de Exibição</label>
                <input type="text" class="editorial-input" v-model="form.nome" required placeholder="Ex: Higiene Pessoal, Dermocosméticos...">
              </div>
              <div class="form-group col-span-2">
                <label class="editorial-label">Descrição Técnica (Opcional)</label>
                <textarea class="editorial-textarea" v-model="form.descricao" rows="4" placeholder="Detalhe o escopo desta categoria para melhor indexação..."></textarea>
              </div>
            </div>
          </form>
        </div>

        <footer class="config-footer">
          <button type="button" class="btn-cancel" @click="showModal = false">Cancelar</button>
          <button type="submit" form="categoryForm" class="btn-save" :disabled="isSaving" style="background: var(--cf-gold); box-shadow: 0 4px 15px rgba(184,149,80,0.3);">
            <i v-if="isSaving" class="fas fa-circle-notch fa-spin me-2"></i>
            {{ editandoId ? 'Sincronizar Alterações' : 'Efetivar Categoria' }}
          </button>
        </footer>
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

