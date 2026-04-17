package br.com.prime.oficina.movimentoEstoque.controller;

import br.com.prime.oficina.movimentoEstoque.application.MovimentoEstoqueResponse;
import br.com.prime.oficina.movimentoEstoque.application.MovimentoEstoqueService;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
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

    private final MovimentoEstoqueService movimentoEstoqueService;

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<MovimentoEstoqueResponse>> listarPorItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(movimentoEstoqueService.listarPorItem(itemId));
    }

    @GetMapping("/item/{itemId}/tipo/{tipo}")
    public ResponseEntity<List<MovimentoEstoqueResponse>> listarPorItemETipo(
            @PathVariable Long itemId,
            @PathVariable TipoMovimentoEstoque tipo
    ) {
        return ResponseEntity.ok(movimentoEstoqueService.listarPorItemETipo(itemId, tipo));
    }
}