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
import br.com.prime.oficina.shared.exception.RecursoDuplicadoException;
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
    public List<OrdemServicoResponse> listarPorCodigo(int codigo) {
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
        validarOrdemServicoDuplicada(request.codigo());

        Cliente cliente = buscarClientePorId(request.clienteId());
        if(!cliente.getAtivo()) throw new RegraNegocioException("Cliente inativo");

        Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());
        if(!veiculo.getAtivo()) throw new RegraNegocioException("Veiculo Inativo");

        OrdemServico ordemServico = new OrdemServico();
        preencherOrdemServico(ordemServico, request, veiculo, cliente);

        OrdemServico salvo = repository.save(ordemServico);
        return toResponse(salvo);
    }

    @Transactional
    public OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);
        Cliente cliente = buscarClientePorId(request.clienteId());
        Veiculo veiculo = buscarVeiculoPorId(request.veiculoId());

        if(!(ordemServico.getCodigo() == request.codigo())
            && repository.existsByCodigo(request.codigo())) {
            throw new RecursoDuplicadoException("Já existe ordem de serviço cadastrada com esse código");
        }

        preencherOrdemServico(ordemServico, request, veiculo, cliente);
        OrdemServico atualizado = repository.save(ordemServico);

        return toResponse(atualizado);
    }

    @Transactional
    public OrdemServicoResponse adicionarItem(Long id, ItemOrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);
        Item item = buscarItemPorId(request.itemId());

        ItemOrdemServico itemOrdemServico = new ItemOrdemServico();
        preencherItemOrdemServico(itemOrdemServico, ordemServico, item, request);

        itemOrdemServicoRepository.save(itemOrdemServico);
        ordemServico.setValorTotalItens(ordemServico.getValorTotalItens().add(itemOrdemServico.getValorUnitario().multiply(BigDecimal.valueOf(itemOrdemServico.getQuantidade()))));

        OrdemServico atualizado = repository.save(ordemServico);
        return toResponse(atualizado);
    }

    @Transactional
    public OrdemServicoResponse adicionarServico(Long id, ServicoOrdemServicoRequest request) {
        OrdemServico ordemServico = buscarOrdemServicoPorId(id);
        Servico servico = buscarServicoPorId(request.servicoId());

        ServicoOrdemServico servicoOrdemServico = new ServicoOrdemServico();
        preencherServicoOrdemServico(servicoOrdemServico, ordemServico, servico, request);

        servicoOrdemServicoRepository.save(servicoOrdemServico);
        ordemServico.setValorTotalServicos(ordemServico.getValorTotalServicos().add(servicoOrdemServico.getValorUnitario().multiply(servicoOrdemServico.getServico().getPrecoBase())));

        OrdemServico atualizado = repository.save(ordemServico);
        return toResponse(atualizado);
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
                    historico.setObservacao("Abertura de Historico");
                    historico.setStatus(StatusHistorico.ATIVO);
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
                ordemServico.getDataFinalizada()
        );
    }

    private void validarOrdemServicoDuplicada(int codigo) {
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
        ordemServico.setCodigo(request.codigo());
        ordemServico.setDescricaoProblema(request.descricaoProblema());
        ordemServico.setObservacoesGerais(request.observacoesGerais());
        ordemServico.setDescricaoServicosExecutados(request.descricaoServicosExecutados());
        ordemServico.setStatus(request.status());
        ordemServico.setValorTotalServicos(request.valorTotalServicos());
        ordemServico.setValorTotalItens(request.valorTotalItens());
    }

    private void preencherItemOrdemServico(ItemOrdemServico itemOrdemServico, OrdemServico ordemServico, Item item, ItemOrdemServicoRequest request) {
        itemOrdemServico.setOrdemServico(ordemServico);
        itemOrdemServico.setItem(item);
        itemOrdemServico.setQuantidade(request.quantidade());
        itemOrdemServico.setValorUnitario(request.valorUnitario());
    }

    private void preencherServicoOrdemServico(ServicoOrdemServico servicoOrdemServico, OrdemServico ordemServico, Servico servico, ServicoOrdemServicoRequest request) {
        servicoOrdemServico.setOrdemServico(ordemServico);
        servicoOrdemServico.setServico(servico);
        servicoOrdemServico.setValorUnitario(request.valorUnitario());
        servicoOrdemServico.setStatus(request.status());
    }
}
