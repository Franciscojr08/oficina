package br.com.prime.oficina.relatorio.application;

import br.com.prime.oficina.relatorio.application.dto.*;

import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.ordemservico.servicos.application.gateway.ServicoOrdemServicoGateway;
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
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private ServicoOrdemServicoGateway servicoOrdemServicoGateway;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveRetornarZeroQuandoNaoHouverTempoMedioDeOS() {
        when(ordemServicoGateway.calcularTempoMedioOSMinutos()).thenReturn(null);

        RelatorioResponse response = relatorioService.calcularTempoMedioOS();

        assertEquals(0.0, response.tempoMedioHoras());
        assertEquals("0 minutos", response.tempoFormatado());
    }

    @Test
    void deveFormatarTempoMedioDeOSComHorasEMinutos() {
        when(ordemServicoGateway.calcularTempoMedioOSMinutos()).thenReturn(150.0);

        RelatorioResponse response = relatorioService.calcularTempoMedioOS();

        assertEquals(2.5, response.tempoMedioHoras());
        assertEquals("2 horas e 30 minutos", response.tempoFormatado());
    }

    @Test
    void deveFormatarTempoMedioDeServicosComDiasHorasEMinutos() {
        when(servicoOrdemServicoGateway.calcularTempoMedioServicosMinutos()).thenReturn(3005.0);

        RelatorioResponse response = relatorioService.calcularTempoMedioServicos();

        assertEquals(50.08, response.tempoMedioHoras());
        assertEquals("2 dias, 2 horas e 5 minutos", response.tempoFormatado());
    }
}
