package gov.goias.relatorios.consumer.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import gov.goias.relatorios.consumer.enuns.StatusRelatorio;
import gov.goias.relatorios.consumer.enuns.TipoRelatorio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_relatorio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolicitacaoRelatorio {

    @Id
    private String idSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRelatorio tipoRelatorio;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String sistema;

    @Column(name = "agendar_para")
    private LocalDateTime agendarPara;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRelatorio status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "data_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "progresso")
    private Integer progresso = 0;

    @Column(name = "mensagem_status")
    private String mensagemStatus;

    @Column(name = "caminho_arquivo")
    private String caminhoArquivo;

    @Column(name = "tamanho_arquivo")
    private Long tamanhoArquivo;

    public void gerarIdSolicitacao() {
        this.idSolicitacao = "rel-" + dataSolicitacao.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}