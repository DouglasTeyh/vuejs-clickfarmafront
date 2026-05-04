<template>
  <div class="card mb-3 cart-item">
    <div class="card-body">
      <div class="row align-items-center">
        <!-- Imagem do produto -->
        <div class="col-md-2 text-center">
          <img 
            :src="itemImage" 
            :alt="item.name"
            class="img-fluid rounded item-image"
            @error="handleImageError"
            loading="lazy"
          >
        </div>
        
        <!-- Informações do produto -->
        <div class="col-md-4">
          <h5 class="item-name">{{ item.name }}</h5>
          <p class="text-muted item-description">{{ truncatedDescription }}</p>
          <span class="badge cf-badge-gold">{{ item.category }}</span>
        </div>
        
        <!-- Quantidade -->
        <div class="col-md-2">
          <div class="quantity-control-group d-flex align-items-center">
            <button class="btn-qty" @click="handleQuantityChange(item.quantity - 1)" :disabled="item.quantity <= 1">
              <i class="fas fa-minus"></i>
            </button>
            <span class="qty-value mx-2">{{ item.quantity }}</span>
            <button class="btn-qty" @click="handleQuantityChange(item.quantity + 1)">
              <i class="fas fa-plus"></i>
            </button>
          </div>
        </div>
        
        <!-- Preço -->
        <div class="col-md-2">
          <p class="fw-bold item-total mb-0">R$ {{ itemTotal }}</p>
          <p class="text-muted item-unit-price small">R$ {{ itemPrice }} cada</p>
        </div>
        
        <!-- Remover -->
        <div class="col-md-2 text-end">
          <button 
            @click="$emit('remove-item', item.id)" 
            class="btn-remove"
            title="Remover do carrinho"
          >
            <i class="far fa-trash-alt"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { placeholderConfig } from '@/utils/placeholder'

export default {
  name: 'CartItem',
  props: {
    item: {
      type: Object,
      required: true,
      validator: (item) => {
        return (
          item.id &&
          item.name &&
          item.description &&
          typeof item.price === 'number' &&
          typeof item.quantity === 'number'
        )
      }
    }
  },
  data() {
    return {
      imageError: false
    }
  },
  computed: {
    truncatedDescription() {
      const maxLength = 60
      return this.item.description.length > maxLength 
        ? this.item.description.substring(0, maxLength) + '...' 
        : this.item.description
    },
    itemTotal() {
      return (this.item.price * this.item.quantity).toFixed(2)
    },
    itemPrice() {
      return this.item.price.toFixed(2)
    },
    itemImage() {
      return placeholderConfig.getProductImage(this.item.image)
    }
  },
  methods: {
    handleQuantityChange(newQuantity) {
      const quantity = Math.max(1, newQuantity)
      this.$emit('update-quantity', this.item.id, quantity)
    },
    handleImageError(event) {
      placeholderConfig.handleImageError(event)
    }
  }
}
</script>

<style scoped>
.cart-item {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.item-image {
  max-height: 80px;
  object-fit: cover;
  background-color: var(--cf-green-xlight);
  padding: 4px;
  border: 1px solid var(--cf-border);
}

.item-name {
  color: var(--cf-text-dark);
  margin-bottom: 0.5rem;
}

.item-description {
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.quantity-control-group {
  background: var(--cf-green-xlight);
  border-radius: 8px;
  padding: 4px;
  width: fit-content;
}

.btn-qty {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: white;
  color: var(--cf-green);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.btn-qty:hover:not(:disabled) {
  background: var(--cf-green);
  color: white;
}

.btn-qty:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-value {
  font-weight: 600;
  min-width: 20px;
  text-align: center;
  color: var(--cf-text-dark);
}

.btn-remove {
  background: transparent;
  border: none;
  color: #ccc;
  font-size: 1.1rem;
  transition: color 0.2s;
  padding: 8px;
}

.btn-remove:hover {
  color: var(--cf-danger);
}

.item-total {
  font-size: 1.1rem;
  margin-bottom: 0.2rem;
  color: var(--cf-green);
}

.item-unit-price {
  font-size: 0.8rem;
  margin-bottom: 0;
}

.badge {
  font-size: 0.7rem;
}

.cf-badge-gold { background: var(--cf-gold-light); color: var(--cf-gold); }
</style>