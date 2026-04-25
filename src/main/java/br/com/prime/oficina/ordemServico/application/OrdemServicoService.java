package br.com.prime.oficina.ordemServico.application;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.cliente.infraestructure.ClienteRepository;
import br.com.prime.oficina.estoque.domain.Estoque;
import br.com.prime.oficina.estoque.infrastructure.EstoqueRepository;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.item.infrastructure.ItemRepository;
import br.com.prime.oficina.movimentoEstoque.domain.MovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.domain.TipoMovimentoEstoque;
import br.com.prime.oficina.movimentoEstoque.infrastructure.MovimentoEstoqueRepository;
import br.com.prime.oficina.ordemServico.domain.HistoricoOrdemServico;
import br.com.prime.oficina.ordemServico.domain.ItemOrdemServico;
import br.com.prime.oficina.ordemServico.domain.OrdemServico;
import br.com.prime.oficina.ordemServico.domain.ServicoOrdemServico;
import br.com.prime.oficina.ordemServico.infrastructure.HistoricoOrdemServicoRepository;
import br.com.prime.oficina.ordemServico.infrastructure.ItemOrdemServicoRepository;
import br.com.prime.oficina.ordemServico.infrastructure.OrdemServicoRepository;
import br.com.prime.oficina.ordemServico.infrastructure.ServicoOrdemServicoRepository;
import br.com.prime.oficina.servico.domain.Servico;
import br.com.prime.oficina.servico.infrasctucture.ServicoRepository;
import br.com.prime.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.prime.oficina.shared.exception.RegraNegocioException;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import br.com.prime.oficina.veiculo.infrastructure.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ItemRepository itemRepository;
    private final ServicoRepository servicoRepository;
    private final ItemOrdemServicoRepository itemOrdemServicoRepository;
    private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;
    private final EstoqueRepository estoqueRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final HistoricoOrdemServicoRepository historicoOrdemServicoRepository;
    
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private final static String SAIDA_DEFAULT_ITEM = "BAIXA DE ITEM NO ESTOQUE";

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCliente(Long clienteId) {
        return repository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorStatus(StatusOrdemServico status) {
        return repository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request) {
        Cliente cliente = buscarClientePorId(request.clienteId());
		if (!cliente.getAtivo()) {
			throw new RegraNegocioException("O cliente informado não está ativo");
		}

        Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());
		if (!veiculo.getAtivo()) {
			throw new RegraNegocioException("O Veículo informado não está ativo");
		}

        OrdemServico ordemServico = new OrdemServico();
		ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
		ordemServico.setValorTotalServicos(BigDecimal.ZERO);
		ordemServico.setValorTotalItens(BigDecimal.ZERO);
        preencherOrdemServico(ordemServico, request, veiculo, cliente);

		repository.saveAndFlush(ordemServico);
		entityManager.refresh(ordemServico);

		salvarHistorico(ordemServico,StatusOrdemServico.RECEBIDA);

        return toResponse(ordemServico);
    }

	private void salvarHistorico(OrdemServico ordemServico, StatusOrdemServico status) {
		HistoricoOrdemServico historicoOrdemServico = new HistoricoOrdemServico();
		historicoOrdemServico.setOrdemServico(ordemServico);
		historicoOrdemServico.setStatus(status);
		historicoOrdemServico.setObservacao("OS cadastrada");
		historicoOrdemServicoRepository.save(historicoOrdemServico);
	}

	@Transactional
    public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);
        Cliente cliente = buscarClientePorId(request.clienteId());
		if (!cliente.getAtivo()) {
			throw new RegraNegocioException("O cliente informado não está ativo");
		}

        Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());
		if (!veiculo.getAtivo()) {
			throw new RegraNegocioException("O Veículo informado não está ativo");
		}

        preencherOrdemServico(ordemServico, request, veiculo, cliente);
		repository.saveAndFlush(ordemServico);

		return toResponse(ordemServico);
    }

    @Transactional
    public OrdemServicoResponse adicionarItem(Long id, ItemOrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		if (ordemServico.getStatus().estaEmEdicao()) {
			String mensagem = String.format(
				"Não é possível adicionar o item, pois a ordem de serviço está %s",
				ordemServico.getStatus().getDescricao()
			);
			throw new RegraNegocioException(mensagem);
		}

		Item item = buscarItemPorId(request.itemId());
		if (!item.getAtivo()) {
			throw new RegraNegocioException("O Item informado não está ativo");
		}

        ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
        preencherItemOrdemServico(itemOrdemServico, ordemServico, item, request);

        itemOrdemServicoRepository.save(itemOrdemServico);

		BigDecimal novoTotal = getValorTotalItens(itemOrdemServico, ordemServico);
		ordemServico.setValorTotalItens(novoTotal);

        repository.saveAndFlush(ordemServico);
        return toResponse(ordemServico);
    }

	private BigDecimal getValorTotalItens(ItemOrdemServico itemOrdemServico, OrdemServico ordemServico) {
		BigDecimal valorItem = itemOrdemServico.getValorUnitario()
				.multiply(BigDecimal.valueOf(itemOrdemServico.getQuantidade()));
		return ordemServico.getValorTotalItens().add(valorItem);
	}

	@Transactional
    public OrdemServicoResponse adicionarServico(Long id, ServicoOrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		if (ordemServico.getStatus().estaEmEdicao()) {
			String mensagem = String.format(
					"Não é possível adicionar o serviço, pois a ordem de serviço está %s",
					ordemServico.getStatus().getDescricao()
			);
			throw new RegraNegocioException(mensagem);
		}

		Servico servico = buscarServicoPorId(request.servicoId());
		if (!servico.getAtivo()) {
			throw new RegraNegocioException("O Serviço informado não está ativo");
		}

        ServicoOrdemServico servicoOrdemServico = new ServicoOrdemServico();
        preencherServicoOrdemServico(servicoOrdemServico, ordemServico, servico);

        servicoOrdemServicoRepository.save(servicoOrdemServico);

		BigDecimal novoTotal = getValorTotalServicos(ordemServico, servicoOrdemServico);
		ordemServico.setValorTotalServicos(novoTotal);

        repository.saveAndFlush(ordemServico);
        return toResponse(ordemServico);
    }

	private BigDecimal getValorTotalServicos(OrdemServico ordemServico, ServicoOrdemServico servicoOrdemServico) {
		BigDecimal valorServico = servicoOrdemServico.getValorUnitario();
		return ordemServico.getValorTotalServicos().add(valorServico);
	}

	@Transactional
    public OrdemServicoResponse aprovarOrdemServico(Long id) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			String mensagem = String.format(
					"Não é possível aprovar a ordem de serviço, pois ela está %s",
				ordemServico.getStatus().getDescricao()
			);
			throw new RegraNegocioException(mensagem);
		}

		List<ItemOrdemServico> itemOrdemServicoList = itemOrdemServicoRepository.findByOrdemServicoId(id);

		for (ItemOrdemServico itemOrdemServico : itemOrdemServicoList) {
			Item item = itemOrdemServico.getItem();
			Estoque estoque = item.getEstoque();

			int quantidade = estoque.getQuantidade() - itemOrdemServico.getQuantidade();

			if (quantidade < 0) {
				throw new RegraNegocioException("Quantidade de estoque insuficiente");
			}

			estoque.setQuantidade(quantidade);
			item.setEstoque(estoque);

			itemRepository.saveAndFlush(item);
			estoqueRepository.saveAndFlush(estoque);

			MovimentoEstoque movimento = new MovimentoEstoque();
			movimento.setItem(item);
			movimento.setTipo(TipoMovimentoEstoque.SAIDA);
			movimento.setQuantidade(itemOrdemServico.getQuantidade());
			movimento.setOrdemServicoId(ordemServico.getId());
			movimento.setObservacao(SAIDA_DEFAULT_ITEM);
			movimentoEstoqueRepository.saveAndFlush(movimento);

			HistoricoOrdemServico historicoOrdemServico = historicoOrdemServicoRepository.findByOrdemServicoId(ordemServico.getId());

			historicoOrdemServicoRepository.saveAndFlush(historicoOrdemServico);
		}

		ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
		ordemServico.setDataAprovacao(LocalDateTime.now());
		ordemServico.setDataInicioExecucao(LocalDateTime.now());
		repository.saveAndFlush(ordemServico);

		salvarHistorico(ordemServico,StatusOrdemServico.EM_EXECUCAO);

        return toResponse(ordemServico);
    }

    @Transactional
    public OrdemServicoResponse reprovarOrdemServico(Long id) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			String mensagem = String.format(
					"Não é possível cancelar a ordem de serviço, pois ela está %s",
					ordemServico.getStatus().getDescricao()
			);
			throw new RegraNegocioException(mensagem);
		}

		List<ServicoOrdemServico> servicoOrdemServicoList = servicoOrdemServicoRepository.findByOrdemServicoId(id);

		for (ServicoOrdemServico servico : servicoOrdemServicoList) {
			servico.setStatus(StatusServico.CANCELADO);
			servicoOrdemServicoRepository.saveAndFlush(servico);
		}

		ordemServico.setStatus(StatusOrdemServico.CANCELADA);
		ordemServico.setDataCancelada(LocalDateTime.now());

		repository.saveAndFlush(ordemServico);

		salvarHistorico(ordemServico,StatusOrdemServico.CANCELADA);

		return toResponse(ordemServico);
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private Veiculo buscarVeiculoPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }

    private OrdemServico buscarOrdemServicoPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada"));
    }

    private Item buscarItemPorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));
    }

    private Servico buscarServicoPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico não encontrado"));
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

    private void preencherItemOrdemServico(
		ItemOrdemServico itemOrdemServico,
		OrdemServico ordemServico,
		Item item,
		ItemOrdemServicoRequest request
    ) {
        itemOrdemServico.setOrdemServico(ordemServico);
        itemOrdemServico.setItem(item);
        itemOrdemServico.setQuantidade(request.quantidade());
        itemOrdemServico.setValorUnitario(item.getValorUnitario());
    }

    private void preencherServicoOrdemServico(
		ServicoOrdemServico servicoOrdemServico,
		OrdemServico ordemServico,
		Servico servico
    ) {
        servicoOrdemServico.setOrdemServico(ordemServico);
        servicoOrdemServico.setServico(servico);
        servicoOrdemServico.setValorUnitario(servico.getValor());
        servicoOrdemServico.setStatus(StatusServico.PENDENTE);
    }
}
