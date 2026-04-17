package br.com.prime.oficina.estoque.controller;

import br.com.prime.oficina.estoque.application.EstoqueRequest;
import br.com.prime.oficina.estoque.application.EstoqueResponse;
import br.com.prime.oficina.estoque.application.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoques")
@RequiredArgsConstructor
@Validated
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    public ResponseEntity<List<EstoqueResponse>> listarTodos() {
        return ResponseEntity.ok(estoqueService.listarTodos());
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<EstoqueResponse> buscarPorItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(estoqueService.buscarPorItem(itemId));
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<EstoqueResponse> atualizarPorItem(
            @PathVariable Long itemId,
            @RequestBody @Valid EstoqueRequest request
    ) {
        return ResponseEntity.ok(estoqueService.atualizarPorItem(itemId, request));
    }
}