package br.com.prime.oficina.movimentoestoque.entrypoint.controller;

import br.com.prime.oficina.movimentoestoque.application.dto.MovimentoEstoqueResponse;
import br.com.prime.oficina.movimentoestoque.application.usecase.MovimentoEstoqueUseCase;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes-estoque")
@RequiredArgsConstructor
@Validated
public class MovimentoEstoqueController {

	private final MovimentoEstoqueUseCase movimentoEstoqueUseCase;

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<MovimentoEstoqueResponse>> listarPorItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(movimentoEstoqueUseCase.listarPorItem(itemId));
    }

    @GetMapping("/item/{itemId}/tipo/{tipo}")
    public ResponseEntity<List<MovimentoEstoqueResponse>> listarPorItemETipo(
            @PathVariable Long itemId,
            @PathVariable TipoMovimentoEstoque tipo
    ) {
        return ResponseEntity.ok(movimentoEstoqueUseCase.listarPorItemETipo(itemId, tipo));
    }

    @GetMapping
    public ResponseEntity<List<MovimentoEstoqueResponse>> listarTodos() {
        return ResponseEntity.ok(movimentoEstoqueUseCase.listarTodos());
    }

}