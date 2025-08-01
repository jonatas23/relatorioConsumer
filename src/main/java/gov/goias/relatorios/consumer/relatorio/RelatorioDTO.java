package gov.goias.relatorios.consumer.relatorio;

import java.time.LocalDateTime;

public record RelatorioDTO(String codgUsuario, String nome, LocalDateTime agendarPara) {}