package br.com.prime.oficina.veiculo.infrastructure;

import br.com.prime.oficina.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByPlaca(String placa);

    Optional<Veiculo> findByPlaca(String placa);

    List<Veiculo> findByClienteId(Long clienteId);
}