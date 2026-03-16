package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.ItemPedidoRequestDTO;
import com.clickfarma.backend.dto.UpdateCartItemDTO;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrinho", description = "Gerenciamento do carrinho de compras do usuário autenticado")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(summary = "Obter Carrinho", description = "Retorna o carrinho de compras atual do usuário autenticado.")
    public ResponseEntity<Pedido> getCart() {
        Pedido cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    @Operation(summary = "Adicionar Produto", description = "Adiciona um produto ao carrinho ou atualiza a quantidade se já existir.")
    public ResponseEntity<Pedido> addItemToCart(@RequestBody ItemPedidoRequestDTO itemDTO) {
        Pedido cart = cartService.addItemToCart(itemDTO);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{produtoId}")
    @Operation(summary = "Atualizar Quantidade", description = "Atualiza a quantidade de um produto específico no carrinho.")
    public ResponseEntity<Pedido> updateItemInCart(
            @PathVariable Long produtoId,
            @RequestBody UpdateCartItemDTO itemDTO) {
        Pedido cart = cartService.updateItemInCart(produtoId, itemDTO);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{produtoId}")
    @Operation(summary = "Remover Produto", description = "Remove um produto específico do carrinho.")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long produtoId) {
        cartService.removeItemFromCart(produtoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Limpar Carrinho", description = "Remove todos os itens do carrinho atual.")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
