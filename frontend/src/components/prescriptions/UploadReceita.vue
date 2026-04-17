<template>
  <div class="upload-receita-container">
    <div class="receita-card">
      <div class="receita-header">
        <h2>
          <i class="fas fa-camera-retro"></i>
          Leitura de Receita Médica
        </h2>
        <p class="descricao">
          Tire uma foto ou selecione uma imagem da sua receita médica.
          Nossa IA identifica os medicamentos automaticamente!
        </p>
      </div>

      <div class="upload-area" :class="{ 'has-image': imagemPreview }">
        <input
            type="file"
            ref="fileInput"
            @change="selecionarArquivo"
            accept="image/jpeg,image/png,image/jpg"
            class="file-input"
        />

        <div v-if="!imagemPreview" class="upload-placeholder">
          <i class="fas fa-cloud-upload-alt"></i>
          <p>Clique ou arraste uma imagem</p>
          <small>Suporta JPG, PNG (máx. 5MB)</small>
        </div>

        <div v-else class="preview-area">
          <img :src="imagemPreview" alt="Preview" class="preview-image" />
          <div class="preview-overlay">
            <button @click="limparImagem" class="btn-clear">
              <i class="fas fa-times"></i> Remover
            </button>
          </div>
        </div>
      </div>

      <div class="actions" v-if="imagemBase64 && !carregando && !resultado">
        <button @click="enviarReceita" class="btn-processar">
          <i class="fas fa-microchip"></i>
          Processar com IA
        </button>
      </div>

      <div v-if="carregando" class="loading-area">
        <div class="spinner"></div>
        <p>Analisando sua receita...</p>
        <small>Isso pode levar alguns segundos</small>
      </div>

      <div v-if="resultado" class="resultados">
        <div v-if="resultado.sucesso && resultado.medicamentos?.length > 0">
          <h3>
            <i class="fas fa-pills"></i>
            Medicamentos ({{ resultado.medicamentos.length }})
          </h3>

          <div class="medicamentos-lista">
            <div v-for="(med, index) in resultado.medicamentos" :key="index" class="medicamento-card">
              <div class="medicamento-info">
                <div class="medicamento-nome">
                  <strong>{{ med.nome }}</strong>
                  <span v-if="med.dosagem" class="dosagem">{{ med.dosagem }}</span>
                </div>
                <div class="quantidade">
                  <i class="fas fa-cubes"></i> Quantidade: {{ med.quantidade || 1 }}
                </div>
              </div>
              <button @click="adicionarAoCarrinho(med)" class="btn-adicionar">
                <i class="fas fa-cart-plus"></i> Adicionar
              </button>
            </div>
          </div>

          <div class="acoes-bulk">
            <button @click="adicionarTodosAoCarrinho" class="btn-adicionar-todos">
              <i class="fas fa-shopping-cart"></i>
              Adicionar Todos ({{ resultado.medicamentos.length }})
            </button>
          </div>
        </div>

        <div v-else-if="resultado.sucesso" class="alerta-sem-medicamentos">
          <i class="fas fa-eye-slash"></i>
          <h4>Nenhum medicamento identificado</h4>
          <p>Tente outra imagem com melhor qualidade</p>
          <button @click="limparImagem" class="btn-tentar-novamente">
            Tentar novamente
          </button>
        </div>

        <div v-else class="erro-area">
          <i class="fas fa-exclamation-triangle"></i>
          <h4>Erro ao processar</h4>
          <p>{{ resultado.erro }}</p>
          <button @click="limparImagem" class="btn-tentar-novamente">
            Tentar novamente
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import receitaService from '@/services/receitaService';

export default {
  name: 'UploadReceita',
  data() {
    return {
      imagemBase64: null,
      imagemPreview: null,
      nomeArquivo: null,
      carregando: false,
      resultado: null
    };
  },
  methods: {
    selecionarArquivo(event) {
      const arquivo = event.target.files[0];
      if (!arquivo) return;

      if (arquivo.size > 5 * 1024 * 1024) {
        alert('A imagem deve ter no máximo 5MB');
        return;
      }

      this.nomeArquivo = arquivo.name;
      this.resultado = null;

      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagemPreview = e.target.result;
        this.imagemBase64 = e.target.result.split(',')[1];
      };
      reader.readAsDataURL(arquivo);
    },

    limparImagem() {
      this.imagemBase64 = null;
      this.imagemPreview = null;
      this.nomeArquivo = null;
      this.resultado = null;
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = '';
      }
    },

    async enviarReceita() {
      if (!this.imagemBase64) {
        alert('Selecione uma imagem primeiro');
        return;
      }

      this.carregando = true;

      try {
        const response = await receitaService.processarReceita(
            this.imagemBase64,
            this.nomeArquivo
        );
        this.resultado = response;
      } catch (error) {
        this.resultado = {
          sucesso: false,
          erro: error.response?.data?.erro || error.message
        };
      } finally {
        this.carregando = false;
      }
    },

    adicionarAoCarrinho(medicamento) {
      const item = {
        id: Date.now(),
        nome: medicamento.nome,
        quantidade: medicamento.quantidade || 1,
        dosagem: medicamento.dosagem || '',
        origem: 'receita',
        dataAdicao: new Date().toISOString()
      };

      if (this.$store && this.$store.commit) {
        this.$store.commit('carrinho/adicionarItem', item);
        alert(`${medicamento.nome} adicionado ao carrinho!`);
      } else {
        const carrinho = JSON.parse(localStorage.getItem('carrinho') || '[]');
        carrinho.push(item);
        localStorage.setItem('carrinho', JSON.stringify(carrinho));
        alert(`${medicamento.nome} adicionado ao carrinho (local)!`);
      }
    },

    adicionarTodosAoCarrinho() {
      if (!this.resultado?.medicamentos) return;

      this.resultado.medicamentos.forEach(med => {
        this.adicionarAoCarrinho(med);
      });

      setTimeout(() => {
        if (this.$router) {
          this.$router.push('/cart');
        }
      }, 1000);
    }
  }
};
</script>

<style scoped>
.upload-receita-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.receita-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.receita-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}

.receita-header h2 {
  margin: 0 0 10px;
}

.descricao {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.upload-area {
  margin: 30px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  min-height: 250px;
}

.upload-area:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.upload-placeholder {
  text-align: center;
  padding: 60px 20px;
}

.upload-placeholder i {
  font-size: 64px;
  color: #999;
  margin-bottom: 20px;
}

.preview-area {
  position: relative;
  text-align: center;
  padding: 20px;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}

.preview-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
}

.btn-clear {
  background: rgba(220, 53, 69, 0.9);
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  cursor: pointer;
}

.actions {
  text-align: center;
  margin: 20px 30px 30px;
}

.btn-processar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 14px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  width: 100%;
}

.btn-processar:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.loading-area {
  text-align: center;
  padding: 40px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.resultados {
  margin: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.medicamentos-lista {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.medicamento-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #28a745;
}

.medicamento-card:hover {
  transform: translateX(5px);
}

.medicamento-nome strong {
  font-size: 16px;
  display: block;
  margin-bottom: 5px;
}

.dosagem {
  display: inline-block;
  background: #e9ecef;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  margin-left: 8px;
}

.quantidade {
  font-size: 13px;
  color: #666;
  margin-top: 5px;
}

.btn-adicionar {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-adicionar:hover {
  background: #218838;
}

.acoes-bulk {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #dee2e6;
}

.btn-adicionar-todos {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-adicionar-todos:hover {
  background: #0056b3;
}

.alerta-sem-medicamentos,
.erro-area {
  text-align: center;
  padding: 40px;
}

.alerta-sem-medicamentos i {
  font-size: 48px;
  color: #ffc107;
}

.erro-area i {
  font-size: 48px;
  color: #dc3545;
}

.btn-tentar-novamente {
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 15px;
}

@media (max-width: 768px) {
  .medicamento-card {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }

  .btn-adicionar {
    width: 100%;
  }
}
</style>