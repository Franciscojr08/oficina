package br.com.prime.oficina.veiculo.controller;

import br.com.prime.oficina.veiculo.application.VeiculoRequest;
import br.com.prime.oficina.veiculo.application.VeiculoResponse;
import br.com.prime.oficina.veiculo.application.usecase.VeiculoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Validated
public class VeiculoController {

    private final VeiculoUseCase veiculoUseCase;

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@RequestBody @Valid VeiculoRequest request) {
        VeiculoResponse response = veiculoUseCase.criar(request);
        return ResponseEntity
                .created(URI.create("/veiculos/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<VeiculoResponse>> listar() {
        return ResponseEntity.ok(veiculoUseCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(veiculoUseCase.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VeiculoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(veiculoUseCase.listarPorCliente(clienteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid VeiculoRequest request) {
        return ResponseEntity.ok(veiculoUseCase.atualizar(id, request));
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<VeiculoResponse> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(veiculoUseCase.buscarPorPlaca(placa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        veiculoUseCase.inativar(id);
        return ResponseEntity.noContent().build();
    }
}