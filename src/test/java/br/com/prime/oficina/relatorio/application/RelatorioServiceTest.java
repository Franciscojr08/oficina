package br.com.prime.oficina.relatorio.application;

import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ServicoOrdemServicoRepository servicoOrdemServicoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveRetornarZeroQuandoNaoHouverTempoMedioDeOS() {
        when(ordemServicoRepository.calcularTempoMedioOSMinutos()).thenReturn(null);

        RelatorioResponse response = relatorioService.calcularTempoMedioOS();

        assertEquals(0.0, response.tempoMedioHoras());
        assertEquals("0 minutos", response.tempoFormatado());
    }

    @Test
    void deveFormatarTempoMedioDeOSComHorasEMinutos() {
        when(ordemServicoRepository.calcularTempoMedioOSMinutos()).thenReturn(150.0);

        RelatorioResponse response = relatorioService.calcularTempoMedioOS();

        assertEquals(2.5, response.tempoMedioHoras());
        assertEquals("2 horas e 30 minutos", response.tempoFormatado());
    }

    @Test
    void deveFormatarTempoMedioDeServicosComDiasHorasEMinutos() {
        when(servicoOrdemServicoRepository.calcularTempoMedioServicosMinutos()).thenReturn(3005.0);

        RelatorioResponse response = relatorioService.calcularTempoMedioServicos();

        assertEquals(50.08, response.tempoMedioHoras());
        assertEquals("2 dias, 2 horas e 5 minutos", response.tempoFormatado());
    }
}
