<template>
  <div class="cf-auth-shell">
    <div class="cf-auth-container fade-in-up">
      <div class="cf-auth-card">
        <div class="card-branding-side">
          <div class="brand-logo-large">
            <i class="fas fa-shield-halved"></i>
          </div>
          <div class="brand-text">
            <h2>Click<span>Farma</span></h2>
            <p>Comando Central Admin</p>
          </div>
          <div class="branding-footer">
            <span>Acesso restrito a auditores e gestores da rede ClickFarma. Suas ações são monitoradas por logs de segurança.</span>
          </div>
        </div>

        <div class="card-form-side">
          <header class="form-header">
            <h3>Terminal de Gestão</h3>
            <p>Autenticação necessária para acesso aos módulos operacionais.</p>
          </header>

          <form @submit.prevent="login" class="cf-premium-form">
            <div class="form-group-premium mb-4">
              <label class="premium-label">Credencial de Acesso</label>
              <div class="premium-input-wrap">
                <i class="fas fa-user-shield"></i>
                <input v-model="email" type="email" placeholder="admin@clickfarma.com" required>
              </div>
            </div>

            <div class="form-group-premium mb-5">
              <label class="premium-label">Código Secreto</label>
              <div class="premium-input-wrap">
                <i class="fas fa-key"></i>
                <input v-model="password" type="password" placeholder="••••••••" required>
              </div>
            </div>

            <div v-if="error" class="error-msg-premium animate__animated animate__shakeX mb-4">
              <i class="fas fa-lock"></i> {{ error }}
            </div>

            <button type="submit" class="cf-btn-primary-large w-100" :disabled="isLoading">
              <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
              {{ isLoading ? 'Validando...' : 'Iniciar Sessão Segura' }}
            </button>

            <div class="mt-4 p-3 bg-light rounded" style="border: 1px dashed var(--cf-border);">
              <small class="text-muted d-block mb-1 fw-bold">Ambiente de Homologação:</small>
              <code style="font-size: 0.75rem; color: var(--cf-text-muted);">jdts1@discente.ifpe.edu.br / admin123click</code>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import api from '@/services/api';

const email = ref('');
const password = ref('');
const error = ref('');
const isLoading = ref(false);
const router = useRouter();
const store = useStore();

const login = async () => {
  isLoading.value = true;
  error.value = '';

  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      senha: password.value
    });

    const { token, role, nome, id } = response.data;

    if (role !== 'ADMIN') {
      error.value = 'Nível de privilégio insuficiente para acessar este terminal.';
      return;
    }

    const userData = { id, nome, email: email.value, role };
    
    localStorage.setItem('authToken', token);
    localStorage.setItem('user', JSON.stringify(userData));
    
    store.commit('SET_USER', userData);
    store.commit('SET_AUTH_TOKEN', token);

    router.push('/admin/dashboard');
  } catch (err) {
    console.error('Erro no login admin:', err);
    error.value = err.response?.data?.mensagem || 'Falha na autenticação. Verifique suas credenciais.';
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.cf-auth-shell { min-height: 100vh; background: var(--cf-ivory); display: flex; align-items: center; justify-content: center; padding: 2rem; }
.cf-auth-container { width: 100%; max-width: 900px; }
.cf-auth-card { background: #fff; border-radius: 32px; box-shadow: 0 40px 100px rgba(0,0,0,0.1); display: flex; overflow: hidden; min-height: 600px; border: 1px solid var(--cf-border); }

/* Branding Side */
.card-branding-side { flex: 1; background: #1a1a1a; padding: 4rem; display: flex; flex-direction: column; justify-content: center; color: #fff; position: relative; }
.brand-logo-large { width: 70px; height: 70px; background: var(--cf-gold); border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 2rem; margin-bottom: 2rem; box-shadow: 0 15px 35px rgba(184,149,80,0.3); }
.brand-text h2 { font-family: var(--cf-serif); font-size: 2.5rem; margin: 0; }
.brand-text h2 span { color: var(--cf-gold); }
.brand-text p { font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.2em; color: rgba(255,255,255,0.4); margin-top: 0.5rem; }
.branding-footer { margin-top: auto; font-size: 0.9rem; color: rgba(255,255,255,0.4); line-height: 1.6; }

/* Form Side */
.card-form-side { flex: 1.2; padding: 4rem; display: flex; flex-direction: column; justify-content: center; background: #fff; }
.form-header h3 { font-family: var(--cf-serif); font-size: 2rem; font-weight: 600; color: var(--cf-text-dark); margin-bottom: 0.5rem; }
.form-header p { color: var(--cf-text-muted); font-size: 0.95rem; margin-bottom: 2.5rem; }

.premium-label { font-size: 0.65rem; font-weight: 800; color: var(--cf-text-faint); text-transform: uppercase; letter-spacing: 0.1em; margin-bottom: 0.6rem; display: block; }
.premium-input-wrap { position: relative; }
.premium-input-wrap i { position: absolute; left: 1.25rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); font-size: 1rem; }
.premium-input-wrap input { width: 100%; padding: 1.1rem 1.25rem 1.1rem 3rem; border-radius: 14px; border: 1px solid var(--cf-border); background: var(--cf-ivory-light); outline: none; transition: all 0.2s; font-weight: 600; color: var(--cf-text-dark); }
.premium-input-wrap input:focus { border-color: var(--cf-gold); background: #fff; box-shadow: 0 0 0 4px rgba(184,149,80,0.1); }

.cf-btn-primary-large { background: var(--cf-text-dark); color: #fff; border: none; padding: 1.1rem; border-radius: 16px; font-weight: 700; font-size: 1rem; cursor: pointer; transition: all 0.3s; }
.cf-btn-primary-large:hover { background: #000; transform: translateY(-2px); box-shadow: 0 15px 35px rgba(0,0,0,0.2); }

.error-msg-premium { background: #fff1f2; color: #e11d48; padding: 1rem; border-radius: 14px; font-size: 0.85rem; font-weight: 600; display: flex; align-items: center; gap: 0.75rem; }

@media (max-width: 768px) {
  .cf-auth-card { flex-direction: column; }
  .card-branding-side { padding: 3rem; }
  .card-form-side { padding: 3rem; }
}
</style>