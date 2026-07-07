package br.com.prime.oficina.item.entrypoint.controller;

import br.com.prime.oficina.item.application.dto.ItemAtualizacaoRequest;
import br.com.prime.oficina.item.application.dto.ItemRequest;
import br.com.prime.oficina.item.application.dto.ItemResponse;
import br.com.prime.oficina.item.application.usecase.ItemUseCase;
import br.com.prime.oficina.item.domain.TipoItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/itens")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemUseCase itemUseCase;

    @PostMapping
    public ResponseEntity<ItemResponse> criar(@RequestBody @Valid ItemRequest request) {
        ItemResponse response = itemUseCase.criar(request);
        return ResponseEntity.created(URI.create("/itens/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> listar() {
        return ResponseEntity.ok(itemUseCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemUseCase.buscarPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ItemResponse>> listarPorTipo(@PathVariable TipoItem tipo) {
        return ResponseEntity.ok(itemUseCase.listarPorTipo(tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ItemAtualizacaoRequest request
    ) {
        return ResponseEntity.ok(itemUseCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        itemUseCase.inativar(id);
        return ResponseEntity.noContent().build();
    }
}