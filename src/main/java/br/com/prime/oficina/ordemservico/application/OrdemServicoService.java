package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.movimentoestoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoestoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemservico.infrastructure.HistoricoOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.itens.infrastructure.ItemOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.prime.oficina.shared.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ItemOrdemServicoRepository itemOrdemServicoRepository;
    private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final HistoricoOrdemServicoRepository historicoOrdemServicoRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private static final String SAIDA_DEFAULT_ITEM = "BAIXA DE ITEM NO ESTOQUE";

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listar() {
        return ordemServicoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCliente(Long clienteId) {
        return ordemServicoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCodigo(String codigo) {
        return ordemServicoRepository.findByCodigo(codigo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorStatus(StatusOrdemServico status) {
        return ordemServicoRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request) {
        Cliente cliente = buscarClientePorId(request.clienteId());
		if (Boolean.FALSE.equals(cliente.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_CUSTOMER);
		}

        Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());
		if (Boolean.FALSE.equals(veiculo.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_VEHICLE);
		}

        OrdemServico ordemServico = new OrdemServico();
		ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
		ordemServico.setValorTotalServicos(BigDecimal.ZERO);
		ordemServico.setValorTotalItens(BigDecimal.ZERO);
        preencherOrdemServico(ordemServico, request, veiculo, cliente);

		ordemServicoRepository.saveAndFlush(ordemServico);
		entityManager.refresh(ordemServico);

		salvarHistorico(ordemServico,StatusOrdemServico.RECEBIDA);

        return toResponse(ordemServico);
    }

	private void salvarHistorico(OrdemServico ordemServico, StatusOrdemServico status) {
		HistoricoOrdemServico historicoOrdemServico = new HistoricoOrdemServico();
		historicoOrdemServico.setOrdemServico(ordemServico);
		historicoOrdemServico.setStatus(status);
		historicoOrdemServico.setObservacao(status.getDescricao());
		historicoOrdemServicoRepository.save(historicoOrdemServico);
	}

	@Transactional
    public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);
        Cliente cliente = buscarClientePorId(request.clienteId());
		if (Boolean.FALSE.equals(cliente.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_CUSTOMER);
		}

        Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());
		if (Boolean.FALSE.equals(veiculo.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_VEHICLE);
		}

        preencherOrdemServico(ordemServico, request, veiculo, cliente);
		ordemServicoRepository.saveAndFlush(ordemServico);

		return toResponse(ordemServico);
    }

	@Transactional
	public OrdemServicoResponse aprovarOrdemServico(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		validarStatus(ordemServico, StatusOrdemServico.AGUARDANDO_APROVACAO,
				"aprovar a ordem de serviço");

		atualizarStatus(ordemServico, StatusOrdemServico.APROVADA);

		return processarOrdemParaExecucao(id);
	}

	@Transactional
	public OrdemServicoResponse iniciarExecucao(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		validarStatus(ordemServico, StatusOrdemServico.AGUARDANDO_ITENS,
				"iniciar execução");

		return processarOrdemParaExecucao(id);
	}

	private OrdemServicoResponse processarOrdemParaExecucao(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		List<ItemOrdemServico> itens = itemOrdemServicoRepository.findByOrdemServicoId(id);

		boolean temEstoqueCompleto = estoqueRepository.temEstoqueCompletoParaOrdem(id);

		if (!temEstoqueCompleto) {
			atualizarStatus(ordemServico, StatusOrdemServico.AGUARDANDO_ITENS);
			return toResponse(ordemServico);
		}

		processarBaixasDeEstoque(itens, ordemServico);
		atualizarStatus(ordemServico, StatusOrdemServico.EM_EXECUCAO);

		return toResponse(ordemServico);
	}

	private void processarBaixasDeEstoque(List<ItemOrdemServico> itens, OrdemServico ordemServico) {
		List<MovimentoEstoque> movimentos = new ArrayList<>(itens.size());

		for (ItemOrdemServico itemOrdemServico : itens) {
			Item item = itemOrdemServico.getItem();
			Long estoqueId = item.getEstoque().getId();
			int quantidade = itemOrdemServico.getQuantidade();

			int atualizado = estoqueRepository.baixarEstoque(estoqueId, quantidade);

			if (atualizado == 0) {
				throw new RegraNegocioException(NOT_ENOUGH_STOCK_ITEM + item.getNome());
			}

			MovimentoEstoque movimento = criarMovimentoEstoque(item, quantidade, ordemServico.getId());
			movimentos.add(movimento);
		}

		movimentoEstoqueRepository.saveAll(movimentos);
	}

	private MovimentoEstoque criarMovimentoEstoque(Item item, int quantidade, Long ordemServicoId) {
		MovimentoEstoque movimento = new MovimentoEstoque();
		movimento.setItem(item);
		movimento.setTipo(TipoMovimentoEstoque.SAIDA);
		movimento.setQuantidade(quantidade);
		movimento.setOrdemServicoId(ordemServicoId);
		movimento.setObservacao(SAIDA_DEFAULT_ITEM);
		movimento.setDataMovimentacao(LocalDateTime.now());
		return movimento;
	}

	@Transactional
    public OrdemServicoResponse reprovarOrdemServico(Long id) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		validarStatus(ordemServico, StatusOrdemServico.AGUARDANDO_APROVACAO, "reprovar a ordem de serviço");

		List<ServicoOrdemServico> servicoOrdemServicoList = servicoOrdemServicoRepository.findByOrdemServicoId(id);

		for (ServicoOrdemServico servico : servicoOrdemServicoList) {
			servico.setStatus(StatusServico.CANCELADO);
			servicoOrdemServicoRepository.saveAndFlush(servico);
		}

		atualizarStatus(ordemServico, StatusOrdemServico.CANCELADA);

		return toResponse(ordemServico);
    }

	public void validarStatus(OrdemServico os, StatusOrdemServico statusEsperado, String acao) {
		if (os.getStatus() != statusEsperado) {
			throw new RegraNegocioException(
					"Não é possível %s, pois a ordem de serviço está %s"
							.formatted(acao, os.getStatus().getDescricao())
			);
		}
	}

    private OrdemServicoResponse toResponse(OrdemServico ordemServico) {
        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getCodigo(),
                ordemServico.getDescricaoProblema(),
                ordemServico.getObservacoesGerais(),
                ordemServico.getDescricaoServicosExecutados(),
                ordemServico.getStatus(),
                ordemServico.getValorTotalServicos(),
                ordemServico.getValorTotalItens(),
                ordemServico.getDataCadastro(),
                ordemServico.getDataEnvioAprovacao(),
                ordemServico.getDataAprovacao(),
                ordemServico.getDataInicioExecucao(),
                ordemServico.getDataFimExecucao(),
                ordemServico.getDataEntregue(),
                ordemServico.getDataCancelada()
        );
    }

    private Cliente buscarClientePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));
    }

    private Veiculo buscarVeiculoPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(VEHICLE_NOT_FOUND));
    }

    private OrdemServico buscarOrdemServicoPorId(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(SERVICE_ORDER_NOT_FOUND));
    }

    private void preencherOrdemServico(
		OrdemServico ordemServico,
		OrdemServicoRequest request,
		Veiculo veiculo,
		Cliente cliente
	) {
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setDescricaoProblema(request.descricaoProblema());
        ordemServico.setObservacoesGerais(request.observacoesGerais());
        ordemServico.setDescricaoServicosExecutados(request.descricaoServicosExecutados());
    }

	public OrdemServicoResponse iniciarDiagnostico(Long id) {
		return alterarStatus(
			id,
			StatusOrdemServico.RECEBIDA,
			StatusOrdemServico.EM_DIAGNOSTICO,
			"iniciar diagnóstico"
		);
	}

	public OrdemServicoResponse solicitarAprovacao(Long id) {
		List<ItemOrdemServico> itemOrdemServicoList = itemOrdemServicoRepository.findByOrdemServicoId(id);
		List<ServicoOrdemServico> servicoOrdemServicoList = servicoOrdemServicoRepository.findByOrdemServicoId(id);

		if (itemOrdemServicoList.isEmpty() || servicoOrdemServicoList.isEmpty()) {
			throw new RegraNegocioException("A OS deve possuir ao menos uma peça e um serviço para solicitar aprovação");
		}

		return alterarStatus(
			id,
			StatusOrdemServico.EM_DIAGNOSTICO,
			StatusOrdemServico.AGUARDANDO_APROVACAO,
			"solicitar aprovação"
		);
	}

	public OrdemServicoResponse entregarOrdemServico(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		validarStatus(ordemServico, StatusOrdemServico.FINALIZADA, "entregar");
		atualizarStatus(ordemServico, StatusOrdemServico.ENTREGUE);

		return toResponse(ordemServico);
	}

	private OrdemServicoResponse alterarStatus(
			Long id,
			StatusOrdemServico statusAtualPermitido,
			StatusOrdemServico novoStatus,
			String acao
	) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		validarStatus(ordemServico, statusAtualPermitido, acao);
		atualizarStatus(ordemServico, novoStatus);

		return toResponse(ordemServico);
	}

	public void atualizarStatus(OrdemServico os, StatusOrdemServico status) {
		os.setStatus(status);

		if (status.deveAtualizarDatas()) {
			aplicarRegrasDeStatus(os, status);
		}

		ordemServicoRepository.saveAndFlush(os);
		salvarHistorico(os, status);
	}

	private void aplicarRegrasDeStatus(OrdemServico os, StatusOrdemServico status) {
		switch (status) {
			case AGUARDANDO_APROVACAO -> os.setDataEnvioAprovacao(LocalDateTime.now());
			case APROVADA -> os.setDataAprovacao(LocalDateTime.now());
			case EM_EXECUCAO -> os.setDataInicioExecucao(LocalDateTime.now());
			case FINALIZADA -> os.setDataFimExecucao(LocalDateTime.now());
			case ENTREGUE -> os.setDataEntregue(LocalDateTime.now());
			case CANCELADA -> os.setDataCancelada(LocalDateTime.now());
			default -> throw new RegraNegocioException(INVALID_STATUS_FOR_MODIFICATION);
		}
	}
}
