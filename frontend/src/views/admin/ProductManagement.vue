<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DO CATÁLOGO ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Catálogo Global</h3>
        <p class="editorial-subtitle">Monitoramento de inventário e regulação de ativos farmacêuticos</p>
      </div>
      <div class="header-tools">
        <button class="btn-sync" @click="fetchProducts" :disabled="isLoading">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
          <span>Sincronizar Base</span>
        </button>
      </div>
    </header>

    <!-- ═══ FILTROS INTELIGENTES ═══ -->
    <div class="cf-filter-bar-premium shadow-sm">
      <div class="filter-group-search">
        <i class="fas fa-search"></i>
        <input v-model="busca" type="text" placeholder="Localizar por nome, laboratório ou princípio...">
      </div>
      <div class="filter-divider"></div>
      <div class="filter-group-select">
        <i class="fas fa-store"></i>
        <select v-model="filtroFarmacia">
          <option value="todas">Todas as Unidades</option>
          <option v-for="f in farmacias" :key="f.id" :value="f.id">{{ f.nome }}</option>
        </select>
      </div>
      <div class="filter-group-select">
        <i class="fas fa-file-medical"></i>
        <select v-model="filtroReceita">
          <option value="todos">Todas as Regulações</option>
          <option :value="true">Venda sob Receita</option>
          <option :value="false">Venda Livre (OTC)</option>
        </select>
      </div>
    </div>

    <!-- ═══ GRID DE PRODUTOS ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Mapeando inventário global...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Patrimônio / Produto</th>
              <th>Inventário</th>
              <th>Precificação</th>
              <th>Regulação</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="produto in produtosFiltrados" :key="produto.id" class="row-hover">
              <td class="ps-5">
                <div class="product-cell">
                  <div class="product-thumb">
                    <img :src="produto.imageUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" loading="lazy">
                  </div>
                  <div class="product-meta">
                    <span class="product-name">{{ produto.nome }}</span>
                    <span class="product-origin">
                      <i class="fas fa-store"></i> {{ produto.farmaciaNome || 'Rede Clickfar' }}
                    </span>
                  </div>
                </div>
              </td>
              <td>
                <div class="inventory-pill" :class="produto.estoque > 5 ? 'in-stock' : 'low-stock'">
                  <span class="pill-dot"></span>
                  <span class="pill-text">{{ produto.estoque }} unidades</span>
                </div>
              </td>
              <td>
                <div class="price-cell-premium">
                  <span class="currency">R$</span>
                  <span class="amount">{{ (produto.preco || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 }) }}</span>
                </div>
              </td>
              <td>
                <div class="regulation-cell">
                  <span v-if="produto.necessitaReceita" class="reg-badge red">
                    <i class="fas fa-file-prescription"></i> Retenção de Receita
                  </span>
                  <span v-else class="reg-badge gray">
                    <i class="fas fa-shopping-bag"></i> Venda Livre
                  </span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-pill" @click="abrirModalEdicao(produto)">
                    <i class="fas fa-sliders-h"></i>
                    <span>Ajustar</span>
                  </button>
                  <button class="action-btn-icon danger" @click="deletarProduto(produto.id)">
                    <i class="fas fa-trash-alt"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="produtosFiltrados.length === 0">
              <td colspan="5" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-boxes"></i>
                  <p>Nenhum ativo localizado no catálogo global.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══ MODAL: CONFIGURAÇÃO DE ATIVO ═══ -->
    <div v-if="showModal" class="modal-premium-overlay" @click.self="showModal = false">
      <div class="asset-modal animate__animated animate__fadeInUp">
        <header class="asset-header">
          <div class="header-brand">
            <div class="asset-icon"><i class="fas fa-box-open"></i></div>
            <div class="asset-meta">
              <h5>Configurar Ativo</h5>
              <span class="asset-id">ID GLOBAL: #{{ editandoId }}</span>
            </div>
          </div>
          <button class="close-modal" @click="showModal = false"><i class="fas fa-times"></i></button>
        </header>

        <div class="asset-content cf-hide-scrollbar">
          <form id="adminProductForm" @submit.prevent="atualizarProduto">
            <div class="asset-grid">
              <!-- Coluna: Visual e Principal -->
              <div class="asset-main-col">
                <div class="form-group mb-4">
                  <label class="editorial-label">Designação Comercial</label>
                  <input type="text" class="editorial-input lg" v-model="form.nome" required placeholder="Ex: Amoxicilina 500mg">
                </div>
                
                <div class="form-row mb-4">
                  <div class="form-group flex-1">
                    <label class="editorial-label">Princípio Ativo</label>
                    <input type="text" class="editorial-input" v-model="form.principioAtivo" placeholder="Composição">
                  </div>
                  <div class="form-group flex-1">
                    <label class="editorial-label">Dosagem</label>
                    <input type="text" class="editorial-input" v-model="form.dosagem" placeholder="Ex: 500mg">
                  </div>
                </div>

                <div class="form-group mb-4">
                  <label class="editorial-label">Laboratório Fabricante</label>
                  <input type="text" class="editorial-input" v-model="form.laboratorio" placeholder="Fabricante">
                </div>

                <div class="form-row">
                  <div class="form-group flex-1">
                    <label class="editorial-label">Estoque Atual</label>
                    <input type="number" class="editorial-input" v-model.number="form.estoque" required>
                  </div>
                  <div class="form-group flex-1">
                    <label class="editorial-label">Preço de Tabela (R$)</label>
                    <input type="number" step="0.01" class="editorial-input" v-model.number="form.preco" required>
                  </div>
                </div>
              </div>

              <!-- Coluna: Asset e Regulação -->
              <div class="asset-side-col">
                <div class="asset-preview-card ivory">
                  <label class="editorial-label text-center mb-3">Asset Digital</label>
                  <div class="preview-wrap">
                    <img :src="form.imageUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'">
                  </div>
                  <input type="text" class="editorial-input sm mt-3" v-model="form.imageUrl" placeholder="URL da imagem">
                </div>

                <div class="regulation-card mt-4">
                  <label class="editorial-label">Controle de Retenção</label>
                  <div class="custom-select-wrap">
                    <select class="editorial-select" v-model="form.necessitaReceita">
                      <option :value="true">Exige Receituário</option>
                      <option :value="false">Venda Livre (OTC)</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>

        <footer class="asset-footer">
          <button type="button" class="btn-cancel" @click="showModal = false">Descartar</button>
          <button type="submit" form="adminProductForm" class="btn-save" :disabled="isSaving">
            <i v-if="isSaving" class="fas fa-circle-notch fa-spin me-2"></i>
            Atualizar Patrimônio
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>


<script>
import api from '@/services/api';

export default {
  name: 'ProductManagement',
  data() {
    return {
      produtos: [],
      farmacias: [],
      isLoading: true,
      isSaving: false,
      showModal: false,
      busca: '',
      filtroFarmacia: 'todas',
      filtroReceita: 'todos',
      form: {},
      editandoId: null
    }
  },
  computed: {
    produtosFiltrados() {
      let lista = this.produtos;
      if (this.busca) {
        const t = this.busca.toLowerCase();
        lista = lista.filter(p => p.nome?.toLowerCase().includes(t) || p.principioAtivo?.toLowerCase().includes(t));
      }
      if (this.filtroFarmacia !== 'todas') {
        lista = lista.filter(p => p.farmaciaId === parseInt(this.filtroFarmacia));
      }
      if (this.filtroReceita !== 'todos') {
        lista = lista.filter(p => p.necessitaReceita === this.filtroReceita);
      }
      return lista;
    }
  },
  async mounted() {
    await this.fetchFarmacias();
    await this.fetchProducts();
  },
  methods: {
    async fetchFarmacias() {
      try {
        const res = await api.get('/farmacias');
        this.farmacias = res.data;
      } catch (err) { console.error(err); }
    },
    async fetchProducts() {
      this.isLoading = true;
      try {
        const res = await api.get('/produtos');
        this.produtos = res.data;
      } catch (err) { console.error(err); }
      finally { this.isLoading = false; }
    },
    abrirModalEdicao(produto) {
      this.editandoId = produto.id;
      const catId = produto.categoriaId || produto.categoria?.id;
      this.form = {
        nome: produto.nome || '',
        principioAtivo: produto.principioAtivo || '',
        dosagem: produto.dosagem || '',
        laboratorio: produto.laboratorio || '',
        imageUrl: produto.imageUrl || '',
        descricaoBreve: produto.descricaoBreve || '',
        descricao: produto.descricao || '',
        preco: produto.preco || 0,
        estoque: produto.estoque || 0,
        categoriaId: catId,
        farmaciaId: produto.farmaciaId,
        necessitaReceita: produto.necessitaReceita || false
      };
      this.showModal = true;
    },
    async atualizarProduto() {
      this.isSaving = true;
      try {
        await api.put(`/produtos/${this.editandoId}`, this.form);
        this.showModal = false;
        await this.fetchProducts();
        if (window.$toast) window.$toast.addToast('Produto atualizado com sucesso!', 'success');
      } catch (err) {
        console.error('Erro Admin Update:', err);
        alert('Erro ao atualizar produto.');
      } finally {
        this.isSaving = false;
      }
    },
    async deletarProduto(id) {
      if (confirm('Deseja excluir este produto globalmente?')) {
        try {
          await api.delete(`/produtos/${id}`);
          await this.fetchProducts();
        } catch (err) { alert('Erro ao deletar'); }
      }
    }
  }
}
</script>

<style scoped>
.cf-mgmt-premium { animation: fadeIn 0.6s var(--cf-ease); }

/* Header */
.mgmt-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.btn-sync { background: var(--cf-ivory); border: 1px solid var(--cf-border); padding: 0.75rem 1.5rem; border-radius: 50px; font-weight: 700; font-size: 0.8rem; color: var(--cf-green); display: flex; align-items: center; gap: 0.75rem; cursor: pointer; transition: all 0.3s; }
.btn-sync:hover { background: var(--cf-green-xlight); border-color: var(--cf-green); }

/* Filter Bar */
.cf-filter-bar-premium { background: #fff; border: 1px solid var(--cf-border); border-radius: 20px; padding: 0.5rem 1.5rem; display: flex; align-items: center; gap: 1.5rem; margin-bottom: 2.5rem; }
.filter-group-search { flex: 1.5; display: flex; align-items: center; gap: 0.85rem; }
.filter-group-search i { color: var(--cf-text-faint); }
.filter-group-search input { border: none; outline: none; width: 100%; font-size: 0.9rem; color: var(--cf-text-dark); font-weight: 600; padding: 0.75rem 0; }

.filter-divider { width: 1px; height: 30px; background: var(--cf-border); }

.filter-group-select { flex: 1; display: flex; align-items: center; gap: 0.75rem; color: var(--cf-text-muted); }
.filter-group-select select { border: none; outline: none; background: transparent; font-size: 0.85rem; font-weight: 700; color: var(--cf-text-dark); cursor: pointer; width: 100%; }

/* Table Premium */
.cf-table-card-premium { background: #fff; border-radius: 28px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.loading-overlay-premium { padding: 8rem 0; display: flex; flex-direction: column; align-items: center; gap: 1rem; color: var(--cf-text-muted); }

.editorial-table { width: 100%; border-collapse: collapse; }
.editorial-table th { padding: 1.25rem 1rem; background: var(--cf-ivory-light); font-size: 0.7rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); border-bottom: 1px solid var(--cf-border); }
.editorial-table td { padding: 1.25rem 1rem; vertical-align: middle; border-bottom: 1px solid rgba(0,0,0,0.03); }

.product-cell { display: flex; align-items: center; gap: 1.25rem; }
.product-thumb { width: 48px; height: 48px; border-radius: 12px; background: #fff; border: 1px solid var(--cf-border); overflow: hidden; display: flex; align-items: center; justify-content: center; }
.product-thumb img { width: 100%; height: 100%; object-fit: contain; padding: 4px; }
.product-meta { display: flex; flex-direction: column; }
.product-name { font-size: 0.95rem; font-weight: 700; color: var(--cf-text-dark); }
.product-origin { font-size: 0.7rem; color: var(--cf-text-faint); font-weight: 600; display: flex; align-items: center; gap: 0.35rem; }

.inventory-pill { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0.85rem; border-radius: 50px; font-size: 0.7rem; font-weight: 800; }
.inventory-pill.in-stock { background: var(--cf-green-xlight); color: var(--cf-green); border: 1px solid rgba(34,197,94,0.1); }
.inventory-pill.in-stock .pill-dot { background: #22c55e; }
.inventory-pill.low-stock { background: #fff1f2; color: #ef4444; border: 1px solid rgba(239,68,68,0.1); }
.inventory-pill.low-stock .pill-dot { background: #ef4444; }
.pill-dot { width: 6px; height: 6px; border-radius: 50%; }

.price-cell-premium { display: flex; align-items: baseline; gap: 0.2rem; }
.price-cell-premium .currency { font-size: 0.75rem; font-weight: 800; color: var(--cf-green); }
.price-cell-premium .amount { font-size: 1.1rem; font-weight: 800; color: var(--cf-text-dark); }

.reg-badge { font-size: 0.6rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.05em; padding: 0.35rem 0.75rem; border-radius: 8px; display: inline-flex; align-items: center; gap: 0.4rem; }
.reg-badge.red { background: #fff1f2; color: #ef4444; border: 1px solid rgba(239,68,68,0.1); }
.reg-badge.gray { background: var(--cf-ivory); color: var(--cf-text-muted); border: 1px solid var(--cf-border); }

.action-group { display: flex; gap: 0.5rem; justify-content: center; }
.action-btn-pill { background: #fff; border: 1px solid var(--cf-border); padding: 0.4rem 1rem; border-radius: 50px; font-size: 0.7rem; font-weight: 700; color: var(--cf-text-muted); display: flex; align-items: center; gap: 0.5rem; cursor: pointer; transition: all 0.2s; }
.action-btn-pill:hover { border-color: var(--cf-gold); color: var(--cf-gold); background: var(--cf-ivory); }
.action-btn-icon { width: 34px; height: 34px; border-radius: 50%; border: 1px solid var(--cf-border); background: #fff; color: var(--cf-text-faint); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; }
.action-btn-icon.danger:hover { background: #fff1f2; color: #ef4444; border-color: #fecaca; }

/* Asset Modal */
.modal-premium-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(10px); z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 2rem; }
.asset-modal { background: #fff; width: 100%; max-width: 950px; border-radius: 36px; box-shadow: 0 40px 120px rgba(0,0,0,0.3); overflow: hidden; display: flex; flex-direction: column; max-height: 90vh; }

.asset-header { padding: 1.5rem 2.5rem; background: var(--cf-ivory-light); border-bottom: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; }
.header-brand { display: flex; align-items: center; gap: 1.25rem; }
.asset-icon { width: 50px; height: 50px; border-radius: 16px; background: var(--cf-text-dark); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.asset-meta h5 { font-family: var(--cf-serif); font-size: 1.5rem; font-weight: 600; margin: 0; }
.asset-id { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; }
.close-modal { background: none; border: none; color: var(--cf-text-faint); cursor: pointer; font-size: 1.2rem; }

.asset-content { padding: 2.5rem; overflow-y: auto; flex: 1; }
.asset-grid { display: grid; grid-template-columns: 1.3fr 0.7fr; gap: 3rem; }

.editorial-label { display: block; font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.12em; margin-bottom: 0.6rem; }
.editorial-input { width: 100%; padding: 0.85rem 1.25rem; border-radius: 14px; border: 1px solid var(--cf-border); outline: none; font-size: 0.95rem; font-weight: 600; color: var(--cf-text-dark); background: var(--cf-ivory-light); transition: all 0.2s; }
.editorial-input:focus { border-color: var(--cf-gold); background: #fff; box-shadow: 0 0 0 4px rgba(184,149,80,0.1); }
.editorial-input.lg { font-size: 1.2rem; padding: 1.1rem 1.5rem; border-color: var(--cf-border-mid); }

.form-row { display: flex; gap: 1.5rem; }
.flex-1 { flex: 1; }

.asset-preview-card { padding: 2rem; border-radius: 24px; border: 2px dashed var(--cf-border); display: flex; flex-direction: column; align-items: center; }
.asset-preview-card.ivory { background: var(--cf-ivory); }
.preview-wrap { width: 100%; aspect-ratio: 1/1; background: #fff; border-radius: 20px; display: flex; align-items: center; justify-content: center; padding: 1.5rem; box-shadow: var(--cf-shadow-sm); }
.preview-wrap img { max-width: 100%; max-height: 100%; object-fit: contain; }

.regulation-card { background: #fff; border: 1px solid var(--cf-border); padding: 1.5rem; border-radius: 20px; }
.editorial-select { width: 100%; padding: 0.85rem 1.25rem; border-radius: 12px; border: 1px solid var(--cf-border); font-weight: 700; color: var(--cf-text-dark); cursor: pointer; }

.asset-footer { padding: 1.5rem 2.5rem; background: var(--cf-ivory-light); border-top: 1px solid var(--cf-border); display: flex; justify-content: flex-end; gap: 1rem; }
.btn-cancel { background: #fff; border: 1px solid var(--cf-border); padding: 0.85rem 2rem; border-radius: 50px; font-weight: 700; color: var(--cf-text-muted); cursor: pointer; }
.btn-save { background: var(--cf-text-dark); color: #fff; border: none; padding: 0.85rem 2.5rem; border-radius: 50px; font-weight: 700; cursor: pointer; transition: all 0.3s; }
.btn-save:hover { background: #000; transform: translateY(-2px); box-shadow: 0 10px 30px rgba(0,0,0,0.2); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>