package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infrastructure.ClienteRepository;
import br.com.prime.oficina.ordemservico.itens.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoService;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoService;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemservico.itens.infrastructure.ItemOrdemServicoRepository;
import br.com.prime.oficina.ordemservico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemservico.servicos.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
	private final ItemOrdemServicoService itemOrdemServicoService;
	private final ServicoOrdemServicoService servicoOrdemServicoService;
	private final HistoricoOrdemServicoService historicoOrdemServicoService;
	private final OrdemServicoStatusService ordemServicoStatusService;
	private final OrdemServicoEstoqueService ordemServicoEstoqueService;
	private final OrdemServicoMapper ordemServicoMapper;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listar() {
        return ordemServicoRepository.listagemOrdensServico()
                .stream()
                .map(ordemServicoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCliente(Long clienteId) {
        return ordemServicoRepository.findByClienteId(clienteId)
                .stream()
                .map(ordemServicoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCodigo(String codigo) {
        return ordemServicoRepository.findByCodigo(codigo)
                .stream()
                .map(ordemServicoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorStatus(StatusOrdemServico status) {
        return ordemServicoRepository.findByStatus(status)
                .stream()
                .map(ordemServicoMapper::toResponse)
                .toList();
    }

	@Transactional
	public OrdemServicoResponse criar(OrdemServicoRequest request) {
		Cliente cliente = buscarClientePorId(request.clienteId());
		Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());

		validarVeiculoPertenceAoCliente(veiculo, cliente);

        OrdemServico ordemServico = new OrdemServico();
		ordemServicoStatusService.definirStatusInicial(ordemServico);
		ordemServico.setValorTotalServicos(BigDecimal.ZERO);
		ordemServico.setValorTotalItens(BigDecimal.ZERO);

		preencherOrdemServico(
			ordemServico,
			request,
			veiculo,
			cliente
		);

		ordemServicoRepository.saveAndFlush(ordemServico);
		entityManager.refresh(ordemServico);

		historicoOrdemServicoService.registrar(ordemServico, StatusOrdemServico.RECEBIDA);
		adicionarServicosDoCadastro(ordemServico, request);
		adicionarItensDoCadastro(ordemServico, request);

        return ordemServicoMapper.toResponse(ordemServico);
    }

	private void adicionarServicosDoCadastro(OrdemServico ordemServico, OrdemServicoRequest request) {
		if (request.servicos() == null) {
			return;
		}

		request.servicos().forEach(servicoId ->
				servicoOrdemServicoService.adicionarServicoDuranteCadastro(ordemServico, servicoId)
		);
	}

	private void adicionarItensDoCadastro(OrdemServico ordemServico, OrdemServicoRequest request) {
		if (request.itens() == null) {
			return;
		}

		request.itens().forEach(item ->
				itemOrdemServicoService.adicionarItemDuranteCadastro(ordemServico, item)
		);
	}

	@Transactional
    public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);
		Cliente cliente = buscarClientePorId(request.clienteId());
		Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());

		validarVeiculoPertenceAoCliente(veiculo, cliente);

		preencherOrdemServico(
			ordemServico,
			request,
			veiculo,
			cliente
		);
		ordemServicoRepository.saveAndFlush(ordemServico);

		return ordemServicoMapper.toResponse(ordemServico);
	}

	private void validarVeiculoPertenceAoCliente(Veiculo veiculo, Cliente cliente) {
		if (!veiculo.getCliente().getId().equals(cliente.getId())) {
			throw new RegraNegocioException(VEHICLE_DOES_NOT_BELONG_TO_CUSTOMER);
		}
	}

	@Transactional
	public OrdemServicoResponse aprovarOrdemServico(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.aprovar(ordemServico);

		return processarOrdemParaExecucao(id);
	}

	@Transactional
	public OrdemServicoResponse iniciarExecucao(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.validarPodeIniciarExecucao(ordemServico);

		return processarOrdemParaExecucao(id);
	}

	private OrdemServicoResponse processarOrdemParaExecucao(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		boolean temEstoqueCompleto = ordemServicoEstoqueService.temEstoqueCompletoParaOrdem(id);

		if (!temEstoqueCompleto) {
			ordemServicoStatusService.aguardarItens(ordemServico);

			return ordemServicoMapper.toResponse(ordemServico);
		}

		ordemServicoEstoqueService.baixarEstoqueDaOrdem(ordemServico);
		ordemServicoStatusService.iniciarExecucao(ordemServico);

		return ordemServicoMapper.toResponse(ordemServico);
	}

	@Transactional
    public OrdemServicoResponse reprovarOrdemServico(Long id) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.validarPodeReprovar(ordemServico);

		servicoOrdemServicoService.cancelarServicosDaOrdem(id);
		ordemServicoStatusService.cancelarPorReprovacao(ordemServico);

		return ordemServicoMapper.toResponse(ordemServico);
    }

    private Cliente buscarClientePorId(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(CUSTOMER_NOT_FOUND));

		if (Boolean.FALSE.equals(cliente.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_CUSTOMER);
		}

		return cliente;
    }

    private Veiculo buscarVeiculoPorId(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(VEHICLE_NOT_FOUND));

		if (Boolean.FALSE.equals(veiculo.getAtivo())) {
			throw new RegraNegocioException(NOT_ACTIVE_VEHICLE);
		}

		return veiculo;
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
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.iniciarDiagnostico(ordemServico);

		return ordemServicoMapper.toResponse(ordemServico);
	}

	public OrdemServicoResponse solicitarAprovacao(Long id) {
		List<ItemOrdemServico> itemOrdemServicoList = itemOrdemServicoRepository.findByOrdemServicoId(id);
		List<ServicoOrdemServico> servicoOrdemServicoList = servicoOrdemServicoRepository.findByOrdemServicoId(id);

		if (itemOrdemServicoList.isEmpty() || servicoOrdemServicoList.isEmpty()) {
			throw new RegraNegocioException(SERVICE_ORDER_MUST_HAVE_ITEM_AND_SERVICE_TO_REQUEST_APPROVAL);
		}

		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.solicitarAprovacao(ordemServico);

		return ordemServicoMapper.toResponse(ordemServico);
	}

	public OrdemServicoResponse entregarOrdemServico(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		ordemServicoStatusService.entregar(ordemServico);

		return ordemServicoMapper.toResponse(ordemServico);
	}

	public StatusOrdemServicoResponse consultarStatus(Long id) {
		OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		return new StatusOrdemServicoResponse(ordemServico.getCodigo(),ordemServico.getStatus());
	}
}
