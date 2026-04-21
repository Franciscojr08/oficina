package br.com.prime.oficina.cliente.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import br.com.prime.oficina.cliente.application.ClienteRequest;
import br.com.prime.oficina.cliente.application.ClienteResponse;
import br.com.prime.oficina.cliente.application.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Validated
public class ClienteController {

	private final ClienteService clienteService;

	@PostMapping
	public ResponseEntity<ClienteResponse> criar(@RequestBody @Valid ClienteRequest request) {
		ClienteResponse response = clienteService.criar(request);
		return ResponseEntity.created(URI.create("/clientes/" + response.id())).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ClienteResponse>> listar() {
		return ResponseEntity.ok(clienteService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(clienteService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequest request) {
		return ResponseEntity.ok(clienteService.atualizar(id, request));
	}

	@GetMapping("/documento/{documento}")
	public ResponseEntity<ClienteResponse> buscarPorDocumento(@PathVariable String documento) {
		return ResponseEntity.ok(clienteService.findByCpfCnpj(documento));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> inativar(@PathVariable Long id) {
		clienteService.inativar(id);
		return ResponseEntity.noContent().build();
	}
}
