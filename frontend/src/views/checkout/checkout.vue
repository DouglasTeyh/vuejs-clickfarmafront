<template>
  <div class="payment-methods">
    <h5 class="mb-3">💳 Método de Pagamento</h5>

    <div class="form-check mb-3">
      <input
        v-model="selectedMethod"
        value="credit_card"
        type="radio"
        class="form-check-input"
        id="creditCard"
      >
      <label class="form-check-label" for="creditCard">
        <i class="bi bi-credit-card"></i> Cartão de Crédito
      </label>
    </div>

    <div class="form-check mb-3">
      <input
        v-model="selectedMethod"
        value="debit_card"
        type="radio"
        class="form-check-input"
        id="debitCard"
      >
      <label class="form-check-label" for="debitCard">
        <i class="bi bi-card-checklist"></i> Cartão de Débito
      </label>
    </div>

    <div class="form-check mb-3">
      <input
        v-model="selectedMethod"
        value="pix"
        type="radio"
        class="form-check-input"
        id="pix"
      >
      <label class="form-check-label" for="pix">
        <i class="bi bi-qr-code"></i> PIX
      </label>
    </div>

    <div v-if="selectedMethod === 'pix'" class="alert alert-info mt-3">
      <p>Você será redirecionado para gerar um código PIX após confirmar o pedido.</p>
    </div>

    <div v-if="selectedMethod === 'credit_card' || selectedMethod === 'debit_card'" class="card-data mt-3">
      <div class="row">
        <div class="col-md-12 mb-3">
          <label class="form-label">Número do Cartão</label>
          <input v-model="cardData.number" type="text" class="form-control" placeholder="0000 0000 0000 0000">
        </div>
      </div>
      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="form-label">Nome no Cartão</label>
          <input v-model="cardData.name" type="text" class="form-control" placeholder="JOÃO DA SILVA">
        </div>
        <div class="col-md-3 mb-3">
          <label class="form-label">Validade</label>
          <input v-model="cardData.expiry" type="text" class="form-control" placeholder="MM/AA">
        </div>
        <div class="col-md-3 mb-3">
          <label class="form-label">CVV</label>
          <input v-model="cardData.cvv" type="text" class="form-control" placeholder="123">
        </div>
      </div>
      <div class="row" v-if="selectedMethod === 'credit_card'">
        <div class="col-md-6 mb-3">
          <label class="form-label">Parcelas</label>
          <select v-model="cardData.installments" class="form-select">
            <option v-for="i in 12" :key="i" :value="i">
              {{ i }}x de R$ {{ (finalTotal / i).toFixed(2) }}
            </option>
          </select>
        </div>
      </div>
    </div>
    <button class="btn btn-success mt-4" @click="confirmPayment">Confirmar Pagamento</button>
  </div>
</template>

<script>
import paymentService from '@/services/payment.js';

export default {
  name: 'Checkout',
  props: {
    finalTotal: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      selectedMethod: 'credit_card',
      cardData: {
        number: '',
        name: '',
        expiry: '',
        cvv: '',
        installments: 1
      }
    }
  },
  methods: {
    async confirmPayment() {
      try {
        if (this.selectedMethod === 'pix') {
          const response = await paymentService.generatePixCode({ total: this.finalTotal });
          alert('Código PIX gerado: ' + response.data.code);
        } else {
          const paymentData = {
            ...this.cardData,
            total: this.finalTotal,
            method: this.selectedMethod
          };
          const response = await paymentService.processCardPayment(paymentData);
          alert('Pagamento realizado com sucesso!');
        }
      } catch (error) {
        alert('Erro ao processar pagamento: ' + (error.response?.data?.message || error.message));
      }
    }
  }
}
</script>