package br.com.prime.oficina.estoque.entrypoint.controller;

import br.com.prime.oficina.estoque.application.dto.EstoqueRequest;
import br.com.prime.oficina.estoque.application.dto.EstoqueResponse;
import br.com.prime.oficina.estoque.application.usecase.EstoqueUseCase;
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

	private final EstoqueUseCase estoqueUseCase;

    @GetMapping
    public ResponseEntity<List<EstoqueResponse>> listarTodos() {
	    return ResponseEntity.ok(estoqueUseCase.listarTodos());
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<EstoqueResponse> buscarPorItem(@PathVariable Long itemId) {
	    return ResponseEntity.ok(estoqueUseCase.buscarPorItem(itemId));
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<EstoqueResponse> atualizarPorItem(
            @PathVariable Long itemId,
            @RequestBody @Valid EstoqueRequest request
    ) {
	    return ResponseEntity.ok(estoqueUseCase.atualizarPorItem(itemId, request));
    }
}