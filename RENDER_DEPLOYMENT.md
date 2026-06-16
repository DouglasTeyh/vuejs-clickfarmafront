# 🚀 Guia de Deploy - ClickFarma no Render (v2)

Este guia explica como colocar o ClickFarma no ar usando o **Render**, configurado para o plano gratuito com PostgreSQL e sistema Anti-Sono.

## 1. Estratégia de Deploy
Utilizaremos a funcionalidade **Blueprints** do Render, que configura tudo automaticamente baseado no arquivo `render.yaml`.

### Passo a Passo:
1. Faça o **Push** do seu código para o GitHub.
2. No painel do Render, vá em **Blueprints** > **New Blueprint Instance**.
3. Conecte seu repositório.
4. O Render detectará o arquivo `render.yaml` e mostrará os serviços:
   - `clickfarma-db` (PostgreSQL)
   - `clickfarma-backend` (Spring Boot)
   - `clickfarma-frontend` (Vue.js + Nginx)

## 2. Variáveis de Ambiente Obrigatórias
Durante a criação do Blueprint, você precisará preencher os seguintes valores no painel:

### Para o Backend (`clickfarma-backend`):
| Chave | Descrição |
| :--- | :--- |
| `GROQ_API_KEY` | Sua chave da API Groq. |
| `GEMINI_API_KEY` | Sua chave da API Gemini. |
| `OCR_SPACE_API_KEY` | Sua chave da API OCR Space. |
| `APP_FRONTEND_URL` | A URL que o Render der para o seu **frontend** (ex: `https://clickfarma.onrender.com`). |
| `APP_BACKEND_URL` | A URL que o Render der para este **backend** (ex: `https://backend.onrender.com`). **Isso ativa o Anti-Sono!** |
| `TELEGRAM_BOT_TOKEN` | Token do bot (opcional). |
| `MAIL_PASSWORD` | Senha do SMTP (Brevo/Gmail). |
| `ADMIN_EMAIL_1` | Seu e-mail de acesso admin. |
| `ADMIN_PASSWORD_1` | Sua senha de acesso admin. |

### Para o Frontend (`clickfarma-frontend`):
| Chave | Descrição |
| :--- | :--- |
| `BACKEND_URL` | A URL pública do backend (ex: `https://clickfarma-backend.onrender.com`). Não adicione `/api` ou barra `/` no final. |

## 3. Notas Técnicas (Importante!)

### 🐘 Banco de Dados (PostgreSQL)
O Blueprint já cria o banco e injeta as credenciais (`SPRING_DATASOURCE_URL`, etc) automaticamente no backend. Você não precisa configurar o banco manualmente.

### 💓 Sistema Anti-Sono (Keep-Alive)
No plano Free, o Render suspende o serviço após 15 min. Implementamos o `KeepAliveService` que faz um ping a cada 30 segundos. **Para funcionar, você DEVE preencher a variável `APP_BACKEND_URL` no painel do Render.**

### 🖼️ Persistência de Imagens
O disco do Render é efêmero. Isso significa que imagens de produtos enviadas por upload **serão apagadas** toda vez que o sistema reiniciar ou houver um novo deploy.
- **Recomendação**: Para uso real, use um serviço externo como Cloudinary para as imagens.

## 4. Dúvidas Frequentes
- **Quanto tempo demora o build?** Cerca de 3 a 5 minutos para o backend e 2 minutos para o frontend.
- **O banco de dados é persistente?** Sim, o PostgreSQL do Render mantém os dados mesmo que os serviços reiniciem.
