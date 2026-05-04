<template>
  <div class="cf-mgmt-premium">
    <!-- ═══ HEADER DE INVENTÁRIO ═══ -->
    <header class="mgmt-header">
      <div class="header-info">
        <h3 class="editorial-title">Gestão de Estoque</h3>
        <p class="editorial-subtitle">Administração do catálogo local e controle de disponibilidade farmacêutica</p>
      </div>
      <div class="header-tools">
        <button class="btn-premium-action" @click="abrirModalCadastro">
          <i class="fas fa-plus-circle"></i> Cadastrar Medicamento
        </button>
      </div>
    </header>

    <!-- ═══ FILTROS E PESQUISA ═══ -->
    <div class="inventory-filters-row mb-5">
      <div class="search-editorial-wrap shadow-sm">
        <i class="fas fa-magnifying-glass"></i>
        <input v-model="busca" type="text" placeholder="Localizar por nome, princípio ou laboratório...">
      </div>
      <div class="filter-group-premium shadow-sm">
        <i class="fas fa-filter"></i>
        <select v-model="filtroEstoque">
          <option value="todos">Todos os Níveis</option>
          <option value="baixo">Estoque Baixo</option>
          <option value="zerado">Sem Estoque</option>
        </select>
      </div>
    </div>

    <!-- ═══ TABELA DE INVENTÁRIO ═══ -->
    <div class="cf-table-card-premium">
      <div v-if="isLoading" class="loading-overlay-premium">
        <div class="cf-spinner"></div>
        <span>Sincronizando inventário farmacêutico...</span>
      </div>

      <div v-else class="table-scroll cf-hide-scrollbar">
        <table class="editorial-table">
          <thead>
            <tr>
              <th class="ps-5">Produto / Ativo</th>
              <th>Status Estoque</th>
              <th>Base / App</th>
              <th class="text-center pe-5">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in produtosFiltrados" :key="p.id" class="row-hover">
              <td class="ps-5">
                <div class="prod-cell">
                  <div class="prod-img-premium shadow-sm">
                    <img :src="getImageUrl(p.imageUrl)">
                  </div>
                  <div class="prod-meta">
                    <span class="prod-name">{{ p.nome }}</span>
                    <span class="prod-sub">{{ p.principioAtivo }} • {{ p.dosagem }}</span>
                    <div v-if="p.necessitaReceita" class="recipe-badge-mini">
                      <i class="fas fa-prescription"></i> Retenção de Receita
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <div class="inventory-pill" :class="getEstoqueClass(p.estoque)">
                  <span class="pill-dot"></span>
                  <span class="pill-text">{{ p.estoque }} UNIDADES</span>
                </div>
              </td>
              <td>
                <div class="price-stack">
                  <span class="price-base">R$ {{ (p.preco || 0).toFixed(2) }}</span>
                  <span class="price-final">R$ {{ calcularPrecoFinal(p.preco || 0).toFixed(2) }}</span>
                </div>
              </td>
              <td class="text-center pe-5">
                <div class="action-group">
                  <button class="action-btn-prod" @click="abrirModalEdicao(p)" title="Editar">
                    <i class="fas fa-sliders"></i>
                  </button>
                  <button class="action-btn-prod danger" @click="deletarProduto(p.id)" title="Remover">
                    <i class="fas fa-trash-can"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!produtosFiltrados.length">
              <td colspan="4" class="empty-state">
                <div class="empty-wrap">
                  <i class="fas fa-box-open"></i>
                  <p>Nenhum item localizado no catálogo local.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>


    <!-- ═══ MODAL: CONFIGURAÇÃO DE PRODUTO ═══ -->
    <div v-if="showModal" class="modal-premium-overlay" @click.self="showModal = false">
      <div class="asset-modal animate__animated animate__fadeInUp">
        <header class="modal-header-premium">
          <div class="header-brand">
            <div class="modal-icon"><i class="fas fa-capsules"></i></div>
            <div class="modal-meta">
              <h5>{{ editandoId ? 'Configuração Técnica' : 'Novo Ativo' }}</h5>
              <span class="modal-sub">{{ editandoId ? 'EDITANDO REGISTRO #' + editandoId : 'INCLUSÃO DE CATÁLOGO' }}</span>
            </div>
          </div>
          <button class="close-modal" @click="showModal = false"><i class="fas fa-times"></i></button>
        </header>

        <div class="modal-content-premium cf-hide-scrollbar">
          <form id="productForm" @submit.prevent="salvarProduto" class="product-config-grid">
            <!-- Coluna de Identidade -->
            <div class="form-col">
              <div class="form-group mb-4">
                <label class="editorial-label">Nomenclatura Comercial</label>
                <input v-model="form.nome" class="editorial-input lg" type="text" placeholder="Ex: Tylenol Sinus 500mg" required>
              </div>

              <div class="grid-2 gap-3 mb-4">
                <div class="form-group">
                  <label class="editorial-label">Substância Base</label>
                  <input v-model="form.principioAtivo" class="editorial-input" type="text" placeholder="Ex: Paracetamol">
                </div>
                <div class="form-group">
                  <label class="editorial-label">Dosagem/Forma</label>
                  <input v-model="form.dosagem" class="editorial-input" type="text" placeholder="Ex: 500mg - 20Cps">
                </div>
              </div>

              <div class="form-group mb-4">
                <label class="editorial-label">Laboratório Responsável</label>
                <input v-model="form.laboratorio" class="editorial-input" type="text" placeholder="Ex: Janssen-Cilag">
              </div>

              <div class="form-group mb-4">
                <label class="editorial-label">Vetor de Imagem</label>
                <div class="image-upload-premium" @click="$refs.fileInput.click()">
                  <div v-if="form.imageUrl" class="image-preview-main">
                    <img :src="getImageUrl(form.imageUrl)">
                    <div class="change-overlay"><i class="fas fa-camera"></i> Alterar</div>
                  </div>
                  <div v-else class="upload-placeholder">
                    <i class="fas fa-cloud-upload-alt"></i>
                    <span>Clique para enviar foto</span>
                  </div>
                  <input type="file" ref="fileInput" class="d-none" accept="image/*" @change="onFileChange">
                </div>
                <input v-model="form.imageUrl" class="editorial-input mt-2 small-text" type="text" placeholder="Ou insira uma URL externa...">
              </div>
            </div>

            <!-- Coluna de Mercado e Regulação -->
            <div class="form-col">
              <div class="form-group mb-4">
                <label class="editorial-label">Categoria de Mercado</label>
                <select v-model="form.categoriaId" class="editorial-select-premium" required>
                  <option v-for="cat in categorias" :key="cat.id" :value="cat.id">{{ cat.nome }}</option>
                </select>
              </div>

              <div class="grid-2 gap-3 mb-4">
                <div class="form-group">
                  <label class="editorial-label">Unidades em Estoque</label>
                  <input v-model.number="form.estoque" class="editorial-input" type="number" required min="0">
                </div>
                <div class="form-group">
                  <label class="editorial-label">Preço Base (R$)</label>
                  <input v-model.number="form.preco" class="editorial-input" type="number" step="0.01" required min="0.01">
                </div>
              </div>

              <div class="recipe-control-card mb-4" :class="{ 'required': form.necessitaReceita }">
                <div class="control-header">
                  <i class="fas fa-file-prescription"></i>
                  <span>Exigência de Receita</span>
                  <div class="form-check form-switch m-0 ms-auto">
                    <input class="form-check-input" type="checkbox" v-model="form.necessitaReceita">
                  </div>
                </div>
                <p class="control-help">Ative se este medicamento exigir retenção de receita para venda online.</p>
              </div>

              <div class="form-group">
                <label class="editorial-label">Descrição Técnica / Bula</label>
                <textarea v-model="form.descricao" class="editorial-input" rows="4" placeholder="Indicações, contraindicações e posologia..."></textarea>
              </div>
            </div>
          </form>
        </div>

        <footer class="modal-footer-premium">
          <div class="modal-price-calc">
            <span class="calc-label">Preço Final ao Consumidor (App)</span>
            <span class="calc-val">R$ {{ calcularPrecoFinal(form.preco || 0).toFixed(2) }}</span>
          </div>
          <div class="footer-actions">
            <button class="btn-cancel-modal" @click="showModal = false">Descartar</button>
            <button type="submit" form="productForm" class="btn-save-modal" :disabled="isSaving">
              <i v-if="isSaving" class="fas fa-circle-notch fa-spin me-2"></i>
              Confirmar Registro
            </button>
          </div>
        </footer>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import farmaciasService from '@/services/farmaciasService';

export default {
  name: 'PharmacyProducts',
  data() {
    return {
      showModal: false,
      isLoading: true,
      isSaving: false,
      editandoId: null,
      farmaciaId: null,
      busca: '',
      filtroEstoque: 'todos',
      categorias: [],
      selectedFile: null,
      form: {
        nome: '', principioAtivo: '', dosagem: '', laboratorio: '', imageUrl: '',
        descricaoBreve: '', descricao: '', preco: 0, estoque: 0, 
        categoriaId: null, necessitaReceita: false
      },
      produtos: []
    }
  },
  computed: {
    produtosFiltrados() {
      let lista = this.produtos;
      if (this.busca) {
        const t = this.busca.toLowerCase();
        lista = lista.filter(p => p.nome?.toLowerCase().includes(t) || p.principioAtivo?.toLowerCase().includes(t));
      }
      if (this.filtroEstoque === 'baixo') lista = lista.filter(p => p.estoque > 0 && p.estoque < 10);
      else if (this.filtroEstoque === 'zerado') lista = lista.filter(p => p.estoque <= 0);
      return lista;
    }
  },
  async mounted() {
    await this.carregarCategorias();
    await this.carregarFarmaciaEProdutos();
  },
  methods: {
    async carregarCategorias() {
      try {
        const res = await api.get('/categorias');
        this.categorias = res.data;
      } catch (err) { console.error(err); }
    },
    async carregarFarmaciaEProdutos() {
      this.isLoading = true;
      try {
        const user = JSON.parse(localStorage.getItem('user'));
        if (user && user.farmaciaId) {
          this.farmaciaId = user.farmaciaId;
        } else {
          // Fallback se não tiver farmaciaId no user
          const { data: farmacias } = await farmaciasService.listarTodas();
          const f = farmacias.find(x => x.email === user.email);
          if (f) this.farmaciaId = f.id;
        }

        if (this.farmaciaId) {
          const { data: prods } = await api.get(`/produtos/buscar?farmaciaId=${this.farmaciaId}`);
          this.produtos = prods.filter(p => p.farmaciaId === this.farmaciaId);
        }
      } catch (err) { console.error(err); }
      finally { this.isLoading = false; }
    },
    calcularPrecoFinal(p) { return p * 1.05; },
    getImageUrl(url) {
      if (!url) return 'https://cdn-icons-png.flaticon.com/512/883/883360.png';
      if (url.startsWith('http')) return url;
      return url; // O proxy cuidará de /uploads
    },
    onFileChange(e) {
      const file = e.target.files[0];
      if (file) {
        this.selectedFile = file;
        // Preview temporário
        const reader = new FileReader();
        reader.onload = (event) => {
          this.form.imageUrl = event.target.result;
        };
        reader.readAsDataURL(file);
      }
    },
    getEstoqueClass(q) {
      if (q <= 0) return 'danger';
      if (q < 10) return 'warning';
      return 'success';
    },
    abrirModalCadastro() {
      this.editandoId = null;
      this.form = {
        nome: '', principioAtivo: '', dosagem: '', laboratorio: '', imageUrl: '',
        descricaoBreve: '', descricao: '', preco: 0, estoque: 0, 
        categoriaId: this.categorias[0]?.id || null, necessitaReceita: false
      };
      this.showModal = true;
    },
    abrirModalEdicao(p) {
      this.editandoId = p.id;
      this.form = { ...p, categoriaId: p.categoriaId || p.categoria?.id };
      this.showModal = true;
    },
    async salvarProduto() {
      if (!this.farmaciaId) return;
      this.isSaving = true;
      try {
        const payload = {
          nome: this.form.nome,
          principioAtivo: this.form.principioAtivo,
          dosagem: this.form.dosagem,
          laboratorio: this.form.laboratorio,
          imageUrl: this.form.imageUrl,
          descricao: this.form.descricao,
          preco: this.form.preco,
          estoque: this.form.estoque,
          categoriaId: this.form.categoriaId,
          necessitaReceita: this.form.necessitaReceita,
          farmaciaId: this.farmaciaId
        };
        
        let produtoId = this.editandoId;
        if (this.editandoId) {
          await api.put(`/produtos/${this.editandoId}`, payload);
        } else {
          const res = await api.post('/produtos', payload);
          // O backend retorna MensagemResponseDTO com o produto em 'dados'
          produtoId = res.data.dados?.id || res.data.id;
        }

        // Se houver arquivo selecionado, fazer upload
        if (this.selectedFile && produtoId) {
          const formData = new FormData();
          formData.append('file', this.selectedFile);
          await api.post(`/produtos/${produtoId}/upload-image`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
        }

        this.showModal = false;
        this.selectedFile = null;
        await this.carregarFarmaciaEProdutos();
      } catch (err) {
        console.error('❌ Erro ao salvar produto:', err);
        const msg = (err.response?.data?.mensagem || err.response?.data?.message || err.message || '').toLowerCase();
        
        let errorMsg = 'Não foi possível salvar o produto. Verifique os dados.';
        if (msg.includes('nome')) errorMsg = 'O nome do produto é inválido ou muito curto.';
        else if (msg.includes('preço') || msg.includes('price')) errorMsg = 'O preço deve ser maior que zero.';
        else if (msg.includes('estoque') || msg.includes('stock')) errorMsg = 'A quantidade em estoque é obrigatória.';
        else if (msg.includes('too long')) errorMsg = 'Algum campo contém texto demais.';
        
        if (window.$toast) {
          window.$toast.addToast(errorMsg, 'danger');
        } else {
          alert(errorMsg);
        }
      } finally { 
        this.isSaving = false; 
      }
    },
    async deletarProduto(id) {
      if (confirm('Deseja realmente excluir este produto?')) {
        try {
          await api.delete(`/produtos/${id}`);
          await this.carregarFarmaciaEProdutos();
        } catch (err) { alert('Erro ao deletar.'); }
      }
    }
  }
}
</script>

<style scoped>
.cf-mgmt-premium { animation: fadeIn 0.6s var(--cf-ease); }

/* Header */
.mgmt-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
.editorial-title { font-family: var(--cf-serif); font-size: 2.2rem; font-weight: 500; color: var(--cf-text-dark); margin: 0; }
.editorial-subtitle { font-size: 0.95rem; color: var(--cf-text-muted); margin: 0.25rem 0 0; }

.btn-premium-action { background: var(--cf-green); color: #fff; border: none; padding: 0.85rem 1.75rem; border-radius: 50px; font-weight: 700; display: flex; align-items: center; gap: 0.75rem; cursor: pointer; transition: all 0.3s; box-shadow: 0 10px 20px rgba(42,92,69,0.15); }
.btn-premium-action:hover { background: var(--cf-green-dark); transform: translateY(-2px); box-shadow: 0 15px 30px rgba(42,92,69,0.25); }

/* Filters */
.inventory-filters-row { display: flex; gap: 1.5rem; }
.search-editorial-wrap { flex: 1; background: #fff; border: 1px solid var(--cf-border); border-radius: 14px; padding: 0.5rem 1.25rem; display: flex; align-items: center; gap: 1rem; }
.search-editorial-wrap i { color: var(--cf-text-faint); font-size: 0.9rem; }
.search-editorial-wrap input { border: none; outline: none; width: 100%; font-size: 0.9rem; font-weight: 600; color: var(--cf-text-dark); }

.filter-group-premium { background: #fff; border: 1px solid var(--cf-border); border-radius: 14px; padding: 0.5rem 1.25rem; display: flex; align-items: center; gap: 1rem; min-width: 240px; }
.filter-group-premium i { color: var(--cf-gold); font-size: 0.85rem; }
.filter-group-premium select { border: none; outline: none; background: transparent; width: 100%; font-size: 0.8rem; font-weight: 800; color: var(--cf-text-dark); text-transform: uppercase; cursor: pointer; }

/* Table Premium */
.cf-table-card-premium { background: #fff; border-radius: 28px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.loading-overlay-premium { padding: 8rem 0; display: flex; flex-direction: column; align-items: center; gap: 1rem; color: var(--cf-text-muted); }

.editorial-table { width: 100%; border-collapse: collapse; }
.editorial-table th { padding: 1.25rem 1rem; background: var(--cf-ivory-light); font-size: 0.7rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); border-bottom: 1px solid var(--cf-border); }
.editorial-table td { padding: 1.25rem 1rem; vertical-align: middle; border-bottom: 1px solid rgba(0,0,0,0.03); }

.prod-cell { display: flex; align-items: center; gap: 1.25rem; }
.prod-img-premium { width: 52px; height: 52px; border-radius: 14px; background: #fff; border: 1px solid var(--cf-border); padding: 4px; flex-shrink: 0; }
.prod-img-premium img { width: 100%; height: 100%; object-fit: contain; }
.prod-meta { display: flex; flex-direction: column; }
.prod-name { font-size: 1rem; font-weight: 700; color: var(--cf-text-dark); }
.prod-sub { font-size: 0.75rem; color: var(--cf-text-muted); font-weight: 600; margin-top: 2px; }
.recipe-badge-mini { font-size: 0.55rem; font-weight: 800; color: #ef4444; background: #fff1f2; padding: 2px 8px; border-radius: 4px; text-transform: uppercase; margin-top: 6px; width: fit-content; display: flex; align-items: center; gap: 4px; }

.inventory-pill { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0.85rem; border-radius: 50px; }
.inventory-pill.success { background: var(--cf-green-xlight); color: var(--cf-green); }
.inventory-pill.warning { background: #fffbeb; color: #b45309; }
.inventory-pill.danger { background: #fef2f2; color: #ef4444; }
.inventory-pill .pill-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.inventory-pill .pill-text { font-size: 0.62rem; font-weight: 800; }

.price-stack { display: flex; flex-direction: column; }
.price-base { font-size: 0.7rem; color: var(--cf-text-faint); text-decoration: line-through; }
.price-final { font-size: 0.95rem; font-weight: 800; color: var(--cf-green); }

.action-group { display: flex; gap: 0.5rem; justify-content: center; }
.action-btn-prod { width: 36px; height: 36px; border-radius: 10px; border: 1px solid var(--cf-border); background: #fff; color: var(--cf-text-faint); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; }
.action-btn-prod:hover { color: var(--cf-gold); border-color: var(--cf-gold); background: var(--cf-ivory); }
.action-btn-prod.danger:hover { color: #ef4444; border-color: #fecaca; background: #fff1f2; }

/* Asset Modal */
.modal-premium-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(10px); z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 2rem; }
.asset-modal { background: #fff; width: 100%; max-width: 960px; border-radius: 40px; box-shadow: 0 40px 120px rgba(0,0,0,0.3); overflow: hidden; display: flex; flex-direction: column; max-height: 90vh; }

.modal-header-premium { padding: 1.5rem 2.5rem; background: var(--cf-ivory-light); border-bottom: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; }
.header-brand { display: flex; align-items: center; gap: 1.25rem; }
.modal-icon { width: 48px; height: 48px; border-radius: 14px; background: var(--cf-gold); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.modal-meta h5 { font-family: var(--cf-serif); font-size: 1.4rem; font-weight: 600; margin: 0; }
.modal-sub { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; }

.modal-content-premium { padding: 2.5rem; overflow-y: auto; }
.product-config-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 3rem; }

.grid-2 { display: grid; grid-template-columns: 1fr 1fr; }
.editorial-label { display: block; font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; margin-bottom: 0.6rem; }
.editorial-input, .editorial-select-premium { width: 100%; padding: 0.85rem 1.25rem; border-radius: 14px; border: 1px solid var(--cf-border); outline: none; font-size: 0.95rem; font-weight: 600; color: var(--cf-text-dark); background: var(--cf-ivory-light); transition: all 0.2s; }
.editorial-input:focus { border-color: var(--cf-gold); background: #fff; box-shadow: 0 0 0 4px rgba(184,149,80,0.1); }
.editorial-input.lg { font-size: 1.2rem; padding: 1rem 1.5rem; }

.image-upload-premium {
  width: 100%;
  height: 200px;
  background: var(--cf-ivory-light);
  border: 2px dashed var(--cf-border);
  border-radius: 24px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}
.image-upload-premium:hover { border-color: var(--cf-gold); background: #fff; }

.image-preview-main { width: 100%; height: 100%; position: relative; }
.image-preview-main img { width: 100%; height: 100%; object-fit: contain; padding: 1rem; }
.change-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; gap: 0.5rem;
  color: #fff; font-weight: 700; opacity: 0; transition: 0.3s;
}
.image-preview-main:hover .change-overlay { opacity: 1; }

.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 0.5rem; color: var(--cf-text-faint); }
.upload-placeholder i { font-size: 2.5rem; }
.upload-placeholder span { font-weight: 700; font-size: 0.75rem; text-transform: uppercase; }

.small-text { font-size: 0.7rem !important; }

.recipe-control-card { background: var(--cf-ivory-light); border: 1px solid var(--cf-border); border-radius: 20px; padding: 1.25rem; transition: all 0.3s; }
.recipe-control-card.required { border-color: #fecaca; background: #fff1f2; }
.control-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 0.5rem; }
.control-header i { color: #ef4444; }
.control-header span { font-weight: 800; font-size: 0.85rem; color: var(--cf-text-dark); }
.control-help { font-size: 0.7rem; color: var(--cf-text-muted); margin: 0; }

.modal-footer-premium { padding: 1.5rem 2.5rem; background: var(--cf-ivory-light); border-top: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; }
.modal-price-calc { display: flex; flex-direction: column; }
.calc-label { font-size: 0.6rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; }
.calc-val { font-size: 1.8rem; font-weight: 800; color: var(--cf-green); }

.footer-actions { display: flex; gap: 1rem; }
.btn-cancel-modal { background: #fff; border: 1px solid var(--cf-border); padding: 0.85rem 2rem; border-radius: 50px; font-weight: 700; color: var(--cf-text-muted); cursor: pointer; }
.btn-save-modal { background: var(--cf-text-dark); color: #fff; border: none; padding: 0.85rem 2.5rem; border-radius: 50px; font-weight: 700; cursor: pointer; transition: all 0.3s; }
.btn-save-modal:hover { background: #000; transform: translateY(-2px); box-shadow: 0 10px 25px rgba(0,0,0,0.2); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>
