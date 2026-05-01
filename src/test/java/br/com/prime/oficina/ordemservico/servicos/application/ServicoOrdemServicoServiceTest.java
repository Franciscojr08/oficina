package br.com.prime.oficina.ordemservico.servicos.application;

import br.com.prime.oficina.ordemservico.application.OrdemServicoService;
import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.servico.domain.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoOrdemServicoServiceTest {

    @Mock
    private ServicoOrdemServicoRepository repository;

    @Mock
    private OrdemServicoService ordemServicoService;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private ServicoOrdemServicoService service;

    private static final LocalDateTime DATA_ATUAL = LocalDateTime.now();


    @Test
    void testIniciarServico() {
        ServicoOrdemServico servicoOS = criarServicoOrdemServico();
        when(ordemServicoRepository.findById(anyLong())).thenReturn(Optional.of(criarOrdemServico()));
        when(repository.findByOrdemServicoIdAndServicoId(anyLong(), anyLong())).thenReturn(servicoOS);
        when(repository.save(any(ServicoOrdemServico.class))).thenReturn(servicoOS);

        var out = service.iniciarServico(1L, 2L);
        var outUpdated = new ServicoOrdemServicoResponse(
                out.codigoOS(),
                out.servicoId(),
                out.servicoNome(),
                out.valorServico(),
                out.status(),
                DATA_ATUAL,
                DATA_ATUAL,
                DATA_ATUAL
        );

        assertThat(outUpdated).usingRecursiveAssertion().isEqualTo(criarServicoOrdemServicoResponse());
    }

    @Test
    void testFinalizarServico() {
        ServicoOrdemServico servicoOS = criarServicoOrdemServico();
        servicoOS.setStatus(StatusServico.INICIADO);
        ServicoOrdemServicoResponse response = criarServicoOrdemServicoResponseFinalizado();
        when(ordemServicoRepository.findById(anyLong())).thenReturn(Optional.of(criarOrdemServico()));
        when(repository.findByOrdemServicoIdAndServicoId(anyLong(), anyLong())).thenReturn(servicoOS);
        when(repository.save(any(ServicoOrdemServico.class))).thenReturn(servicoOS);

        var out = service.finalizarServico(1L, 2L);
        var outUpdated = new ServicoOrdemServicoResponse(
                out.codigoOS(),
                out.servicoId(),
                out.servicoNome(),
                out.valorServico(),
                out.status(),
                DATA_ATUAL,
                DATA_ATUAL,
                DATA_ATUAL
        );

        assertThat(outUpdated).usingRecursiveAssertion().isEqualTo(response);
    }

    private OrdemServico criarOrdemServico() {
        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setCodigo("OS-" + (Long) 1L);
        os.setStatus(StatusOrdemServico.RECEBIDA);
        os.setValorTotalItens(BigDecimal.ZERO);
        os.setValorTotalServicos(BigDecimal.ZERO);
        os.setDataCadastro(DATA_ATUAL);
        return os;
    }

    private ServicoOrdemServico criarServicoOrdemServico() {
        ServicoOrdemServico servicoOS = new ServicoOrdemServico();
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Servico");
        servico.setValor(BigDecimal.TEN);
        servicoOS.setId(1L);
        servicoOS.setOrdemServico(criarOrdemServico());
        servicoOS.setStatus(StatusServico.PENDENTE);
        servicoOS.setServico(servico);
        servicoOS.setDataCadastro(DATA_ATUAL);
        servicoOS.setDataInicio(DATA_ATUAL);
        servicoOS.setDataFim(DATA_ATUAL);
        servicoOS.setValorUnitario(BigDecimal.TEN);
        return servicoOS;
    }

    private ServicoOrdemServicoResponse criarServicoOrdemServicoResponse() {
        return new ServicoOrdemServicoResponse(
                "OS-1",
                1L,
                "Servico",
                BigDecimal.TEN,
                StatusServico.INICIADO,
                DATA_ATUAL,
                DATA_ATUAL,
                DATA_ATUAL
        );
    }

    private ServicoOrdemServicoResponse criarServicoOrdemServicoResponseFinalizado() {
        return new ServicoOrdemServicoResponse(
                "OS-1",
                1L,
                "Servico",
                BigDecimal.TEN,
                StatusServico.FINALIZADO,
                DATA_ATUAL,
                DATA_ATUAL,
                DATA_ATUAL
        );
    }
}


