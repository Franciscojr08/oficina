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
import org.jspecify.annotations.NonNull;
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

		salvarHistorico(ordemServico);

        return toResponse(ordemServico);
    }

	private void salvarHistorico(OrdemServico ordemServico) {
		HistoricoOrdemServico historicoOrdemServico = new HistoricoOrdemServico();
		historicoOrdemServico.setOrdemServico(ordemServico);
		historicoOrdemServico.setStatus(StatusOrdemServico.RECEBIDA);
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
        ItemOrdemServico itemOrdemServico = itemOrdemServicoRepository.findByOrdemServicoId(ordemServico.getId()).orElseThrow(() -> new RecursoNaoEncontradoException("Item ordem não encontrado"));
        Item item = itemOrdemServico.getItem();

        if(ordemServico.getStatus() == StatusOrdemServico.EM_EXECUCAO) throw new RegraNegocioException("Ordem de Serviço já em execução");
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDataAprovacao(LocalDateTime.now());

        Estoque estoque = item.getEstoque();
        estoque.setQuantidade(estoque.getQuantidade() - itemOrdemServico.getQuantidade());
        if(estoque.getQuantidade() < 0) throw new RegraNegocioException("Quantidade de estoque insuficiente");

        item.setEstoque(estoque);
        itemRepository.save(item);
        estoqueRepository.save(estoque);

        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.setItem(item);
        movimento.setTipo(TipoMovimentoEstoque.SAIDA);
        movimento.setQuantidade(itemOrdemServico.getQuantidade());
        movimento.setOrdemServicoId(ordemServico.getId());
        movimento.setObservacao(SAIDA_DEFAULT_ITEM);
        movimentoEstoqueRepository.save(movimento);

        HistoricoOrdemServico historicoOrdemServico = historicoOrdemServicoRepository.findByOrdemServicoId(ordemServico.getId())
                .orElseGet(() -> {
                    HistoricoOrdemServico historico = new HistoricoOrdemServico();
                    historico.setOrdemServico(ordemServico);
                    historico.setObservacao("Abertura de Histórico");
                    historico.setStatus(StatusOrdemServico.EM_EXECUCAO);
                    return historico;
                });

        historicoOrdemServicoRepository.save(historicoOrdemServico);
        OrdemServico atualizado = repository.save(ordemServico);
        return toResponse(atualizado);
    }

    @Transactional
    public OrdemServicoResponse reprovarOrdemServico(Long id) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);

        if(ordemServico.getStatus() == StatusOrdemServico.EM_EXECUCAO) throw new RegraNegocioException("Ordem de Serviço já em execução");

        ordemServico.setStatus(StatusOrdemServico.CANCELADA);

        OrdemServico atualizado = repository.save(ordemServico);
        return toResponse(atualizado);
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

    private void validarOrdemServicoDuplicada(String codigo) {
        if (repository.existsByCodigo(codigo)) {
            throw new RegraNegocioException("Já existe ordem de serviço cadastrada com esse código");
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

    private void preencherOrdemServico(OrdemServico ordemServico, OrdemServicoRequest request, Veiculo veiculo, Cliente cliente) {
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
