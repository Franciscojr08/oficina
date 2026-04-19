package br.com.prime.oficina.ordemServico.domain;

import br.com.prime.oficina.cliente.domain.Cliente;
import br.com.prime.oficina.item.domain.Item;
import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.veiculo.domain.Veiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ordem_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true)
    private int codigo;

    @Column(name = "descricao_problema")
    private String descricaoProblema;

    @Column(name = "observacoes_gerais")
    private String observacoesGerais;

    @Column(name = "descricao_servicos_executados", nullable = false)
    private String descricaoServicosExecutados;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusOrdemServico status;

    @Column(name = "valor_total_servicos", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalServicos;

    @Column(name = "valor_total_itens", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalItens;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_envio_aprovacao")
    private LocalDateTime dataEnvioAprovacao;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "data_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "data_finalizada")
    private LocalDateTime dataFinalizada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @PrePersist
    public void prePersist() {
        this.dataCadastro = LocalDateTime.now();
        this.status = StatusOrdemServico.ABERTA;
    }
}
