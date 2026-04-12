package br.com.prime.oficina.veiculo.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        validarPlacaDuplicada(request.placa());

        Cliente cliente = buscarClientePorId(request.clienteId());

        Veiculo veiculo = new Veiculo();
        preencherVeiculo(veiculo, request, cliente);

        Veiculo salvo = veiculoRepository.save(veiculo);
        return toResponse(salvo);
    }

    public List<VeiculoResponse> listar() {
        return veiculoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VeiculoResponse buscarPorId(Long id) {
        Veiculo veiculo = buscarVeiculoPorId(id);
        return toResponse(veiculo);
    }

    public List<VeiculoResponse> listarPorCliente(Long clienteId) {
        return veiculoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
        Veiculo veiculo = buscarVeiculoPorId(id);

        Cliente cliente = buscarClientePorId(request.clienteId());

        if (!veiculo.getPlaca().equals(request.placa())
                && veiculoRepository.existsByPlaca(request.placa())) {
            throw new RuntimeException("Já existe veículo cadastrado com esta placa");
        }

        preencherVeiculo(veiculo, request, cliente);
        Veiculo atualizado = veiculoRepository.save(veiculo);

        return toResponse(atualizado);
    }

    @Transactional
    public void inativar(Long id) {
        Veiculo veiculo = buscarVeiculoPorId(id);

        veiculo.setAtivo(false);
        veiculoRepository.save(veiculo);
    }

    private void validarPlacaDuplicada(String placa) {
        if (veiculoRepository.existsByPlaca(placa)) {
            throw new RegraNegocioException("Já existe veículo cadastrado com esta placa");
        }
    }

    private Cliente buscarClientePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private Veiculo buscarVeiculoPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }

    private VeiculoResponse toResponse(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getCliente().getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getObservacao(),
                veiculo.getAtivo(),
                veiculo.getDataCriacao(),
                veiculo.getDataAtualizacao()
        );
    }

    private void preencherVeiculo(Veiculo veiculo, VeiculoRequest request, Cliente cliente) {
        veiculo.setCliente(cliente);
        veiculo.setPlaca(request.placa());
        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAno(request.ano());
        veiculo.setCor(request.cor());
        veiculo.setObservacao(request.observacao());
    }


}