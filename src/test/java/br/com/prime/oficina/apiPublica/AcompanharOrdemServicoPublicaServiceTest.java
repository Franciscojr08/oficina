package br.com.prime.oficina.apiPublica;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcompanharOrdemServicoPublicaServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private AcompanharOrdemServicoPublicaService service;

    @Test
    void deveAcompanharOrdemServicoPorCodigo() {
        LocalDateTime dataCadastro = LocalDateTime.of(2026, 5, 1, 10, 0);
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setCodigo("OS-2026-0001");
        ordemServico.setDescricaoProblema("Barulho no motor");
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDataCadastro(dataCadastro);

        when(ordemServicoRepository.findByCodigo("OS-2026-0001")).thenReturn(Optional.of(ordemServico));

        AcompanharOrdemServicoPublicaResponse response = service.acompanharPorCodigo("OS-2026-0001");

        assertEquals("OS-2026-0001", response.codigo());
        assertEquals("Barulho no motor", response.descricaoProblema());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, response.status());
        assertEquals(dataCadastro, response.dataCadastro());
    }

    @Test
    void deveLancarExcecaoQuandoCodigoNaoExistir() {
        when(ordemServicoRepository.findByCodigo("OS-404")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.acompanharPorCodigo("OS-404")
        );

        assertEquals("Ordem de servico nao encontrada", exception.getMessage());
    }
}
