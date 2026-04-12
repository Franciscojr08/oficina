package br.com.prime.oficina.servico.controller;

import br.com.prime.oficina.servico.application.ServicoRequest;
import br.com.prime.oficina.servico.application.ServicoResponse;
import br.com.prime.oficina.servico.application.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
@Validated
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@RequestBody @Valid ServicoRequest request) {
        ServicoResponse response = servicoService.criar(request);
        return ResponseEntity.created(URI.create("/servicos/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar() {
        return ResponseEntity.ok(servicoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ServicoRequest request) {
        return ResponseEntity.ok(servicoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        servicoService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}