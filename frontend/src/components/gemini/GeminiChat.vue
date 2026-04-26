<template>
  <div class="gemini-chat">
    <div class="chat-header">
      <i class="fa-solid fa-robot"></i>
      <h3>Assistente Virtual</h3>
      <button @click="$emit('close')" class="close-btn">✕</button>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
      >
        <div class="message-avatar">
          <i :class="msg.role === 'user' ? 'fa-solid fa-user' : 'fa-solid fa-robot'"></i>
        </div>
        <div class="message-content">
          <div class="message-text">{{ msg.content }}</div>
          <div class="message-time">{{ msg.time }}</div>
        </div>
      </div>

      <div v-if="loading" class="message bot">
        <div class="message-avatar">
          <i class="fa-solid fa-robot"></i>
        </div>
        <div class="message-content">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-wrapper">
      <div class="chat-input">
        <button class="attach-btn" title="Anexar Receita">
          <i class="fa-solid fa-paperclip"></i>
        </button>
        <textarea
            v-model="userMessage"
            class="message-textarea"
            @keyup.enter.exact="sendMessage"
            @keyup.enter.shift.exact="userMessage += '\n'"
            placeholder="Digite sua mensagem..."
            rows="1"
            :disabled="loading"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="loading || !userMessage.trim()">
          <i class="fa-solid fa-paper-plane"></i>
        </button>
      </div>
    </div>

    <div class="chat-suggestions">
      <button
          v-for="suggestion in suggestions"
          :key="suggestion"
          @click="sendSuggestion(suggestion)"
          class="suggestion-btn"
      >
        {{ suggestion }}
      </button>
      <button @click="getWellnessSuggestions" class="suggestion-btn wellness-btn">
        🌿 Recomendações de Bem-Estar
      </button>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'

export default {
  name: 'GeminiChat',
  data() {
    return {
      messages: [
        {
          role: 'bot',
          content: 'Olá! Sou o assistente virtual da ClickFarma. Como posso ajudar você hoje?',
          time: this.getCurrentTime()
        }
      ],
      userMessage: '',
      loading: false,
      suggestions: [
        '💊 Qual remédio para dor de cabeça?',
        '📦 Como rastrear meu pedido?',
        '🚚 Qual prazo de entrega?',
        '💳 Formas de pagamento'
      ]
    }
  },
  mounted() {
    this.scrollToBottom()
  },
  methods: {
    getCurrentTime() {
      return new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    async getWellnessSuggestions() {
      this.loading = true;
      try {
        const userId = localStorage.getItem('userId');
        const userName = localStorage.getItem('userName');

        const response = await api.post('/gemini/wellness', {
          userId: userId,
          userName: userName
        });

        this.messages.push({
          role: 'bot',
          content: response.data.response,
          time: this.getCurrentTime()
        });
      } catch (error) {
        console.error('Erro ao obter recomendações de bem-estar:', error);
        this.messages.push({
          role: 'bot',
          content: 'Desculpe, não consegui gerar recomendações de bem-estar no momento.',
          time: this.getCurrentTime()
        });
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },

    async sendMessage() {
      if (!this.userMessage.trim() || this.loading) return

      const message = this.userMessage.trim()
      this.messages.push({
        role: 'user',
        content: message,
        time: this.getCurrentTime()
      })
      this.userMessage = ''
      this.scrollToBottom()

      this.loading = true

      try {
        const response = await api.post('/gemini/chat', {
          message: message
        })

        this.messages.push({
          role: 'bot',
          content: response.data.response,
          time: this.getCurrentTime()
        })
      } catch (error) {
        console.error('Erro completo:', error)
        console.error('Detalhes da resposta:', error.response)

        let errorMessage = 'Desculpe, estou com problemas. Tente novamente mais tarde.'

        if (error.response) {
          console.error('Status:', error.response.status)
          console.error('Data:', error.response.data)

          if (error.response.status === 403) {
            errorMessage = 'Erro de permissão. Verificando configurações...'
          } else if (error.response.status === 401) {
            errorMessage = 'Você precisa estar autenticado para usar o chat.'
          } else if (error.response.status === 500) {
            errorMessage = 'Erro no servidor. Verifique se a chave da API Gemini está configurada.'
          } else if (error.response.data?.response) {
            errorMessage = error.response.data.response
          }
        } else if (error.message === 'Network Error') {
          errorMessage = 'Erro de conexão. Verifique se o backend está rodando em http://localhost:8080'
        }

        this.messages.push({
          role: 'bot',
          content: errorMessage,
          time: this.getCurrentTime()
        })
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },

    sendSuggestion(suggestion) {
      this.userMessage = suggestion
      this.sendMessage()
    }
  }
}
</script>

<style scoped>
.gemini-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--cf-white);
  border-radius: var(--cf-r-xl);
  overflow: hidden;
  box-shadow: var(--cf-shadow-lg);
  border: 1px solid var(--cf-border);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 1.1rem 1.4rem;
  background: var(--cf-green);
  color: white;
}

.chat-header i { font-size: 1.2rem; opacity: 0.9; }
.chat-header h3 {
  flex: 1;
  margin: 0;
  font-family: var(--cf-sans);
  font-size: 1.25rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.close-btn {
  background: rgba(255,255,255,0.1);
  border: none;
  color: white;
  width: 28px; height: 28px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 200ms var(--cf-ease);
}
.close-btn:hover { background: rgba(255,255,255,0.2); transform: rotate(90deg); }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  background: var(--cf-ivory);
  display: flex;
  flex-direction: column;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 1.2rem;
  max-width: 85%;
  animation: messageUp 0.4s var(--cf-ease) both;
}

@keyframes messageUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user { align-self: flex-end; flex-direction: row-reverse; }
.message.bot { align-self: flex-start; }

.message-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  font-size: 0.9rem;
}
.message.bot .message-avatar { background: var(--cf-green); color: white; }
.message.user .message-avatar { background: var(--cf-gold); color: white; }

.message-text {
  padding: 0.8rem 1rem;
  border-radius: var(--cf-r-md);
  font-size: 0.9rem;
  line-height: 1.5;
  box-shadow: var(--cf-shadow-xs);
}
.message.bot .message-text { background: var(--cf-white); color: var(--cf-text-dark); border: 1px solid var(--cf-border); }
.message.user .message-text { background: var(--cf-green); color: white; }

.message-time { font-size: 0.65rem; color: var(--cf-text-faint); margin-top: 4px; padding: 0 4px; }

/* INDICATOR */
.typing-indicator {
  display: flex; gap: 4px; padding: 10px;
}
.typing-indicator span {
  width: 6px; height: 6px; background: var(--cf-green-mid);
  border-radius: 50%; animation: typing 1.4s infinite;
}
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* INPUT */
.chat-input-wrapper {
  background: var(--cf-white);
  border-top: 1px solid var(--cf-border);
}

.chat-input {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 1rem 1.4rem;
}

.message-textarea {
  flex: 1;
  border: 1px solid var(--cf-border-mid);
  border-radius: var(--cf-r-md);
  padding: 0.6rem 1rem;
  font-size: 0.88rem;
  outline: none;
  transition: border 200ms;
  resize: none;
}
.message-textarea:focus { border-color: var(--cf-green); }

.attach-btn {
  background: none;
  border: none;
  color: var(--cf-text-faint);
  font-size: 1.1rem;
  cursor: pointer;
  transition: color 200ms;
  padding: 5px;
}
.attach-btn:hover { color: var(--cf-green); }

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--cf-green);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms;
  flex-shrink: 0;
}
.send-btn:hover:not(:disabled) { background: var(--cf-green-dark); transform: scale(1.05); }
.send-btn:disabled { background: var(--cf-cream); color: var(--cf-text-faint); cursor: not-allowed; }

/* SUGGESTIONS */
.chat-suggestions {
  display: flex; flex-wrap: wrap; gap: 6px;
  padding: 0.8rem 1.4rem 1.2rem; background: var(--cf-white);
}
.suggestion-btn {
  font-size: 0.72rem; padding: 5px 12px;
  border-radius: 100px; border: 1px solid var(--cf-border-mid);
  background: var(--cf-ivory); color: var(--cf-text-muted);
  cursor: pointer; transition: all 200ms;
}
.suggestion-btn:hover { border-color: var(--cf-green); color: var(--cf-green); background: var(--cf-green-xlight); }

.wellness-btn {
  background: var(--cf-green-xlight); color: var(--cf-green);
  border-color: var(--cf-green-light); font-weight: 500;
}
</style>