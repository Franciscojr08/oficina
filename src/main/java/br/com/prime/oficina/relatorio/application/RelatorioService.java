package br.com.prime.oficina.relatorio.application;

import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

	private final OrdemServicoRepository ordemServicoRepository;
	private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;

	public RelatorioResponse calcularTempoMedioOS() {
		Double mediaMinutos = ordemServicoRepository.calcularTempoMedioOSMinutos();
		return montarRelatorio(mediaMinutos);
	}

	public RelatorioResponse calcularTempoMedioServicos() {
		Double mediaMinutos = servicoOrdemServicoRepository.calcularTempoMedioServicosMinutos();
		return montarRelatorio(mediaMinutos);
	}

	private RelatorioResponse montarRelatorio(Double mediaMinutos) {
		if (mediaMinutos == null) {
			return new RelatorioResponse(0.0, "0 minutos");
		}

		BigDecimal mediaHoras = BigDecimal.valueOf(mediaMinutos)
				.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

		String tempoFormatado = formatarDuracao(Math.round(mediaMinutos));

		return new RelatorioResponse(mediaHoras.doubleValue(), tempoFormatado);
	}

	private String formatarDuracao(long minutosTotais) {
		long minutos = minutosTotais;

		long anos = minutos / (60 * 24 * 365);
		minutos %= (60 * 24 * 365);

		long meses = minutos / (60 * 24 * 30);
		minutos %= (60 * 24 * 30);

		long dias = minutos / (60 * 24);
		minutos %= (60 * 24);

		long horas = minutos / 60;
		minutos %= 60;

		List<String> partes = new ArrayList<>();

		if (anos > 0) {
			partes.add(anos + (anos == 1 ? " ano" : " anos"));
		}
		if (meses > 0) {
			partes.add(meses + (meses == 1 ? " mês" : " meses"));
		}
		if (dias > 0) {
			partes.add(dias + (dias == 1 ? " dia" : " dias"));
		}
		if (horas > 0) {
			partes.add(horas + " horas");
		}
		if (minutos > 0) {
			partes.add(minutos + (minutos == 1 ? " minuto" : " minutos"));
		}

		if (partes.isEmpty()) {
			return "0 minutos";
		}

		if (partes.size() == 1) {
			return partes.get(0);
		}

		if (partes.size() == 2) {
			return partes.get(0) + " e " + partes.get(1);
		}

		return String.join(", ", partes.subList(0, partes.size() - 1))
				+ " e " + partes.get(partes.size() - 1);
	}
}
