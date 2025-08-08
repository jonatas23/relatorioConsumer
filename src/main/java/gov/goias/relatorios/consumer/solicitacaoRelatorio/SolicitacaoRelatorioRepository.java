package gov.goias.relatorios.consumer.solicitacaoRelatorio;

import gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.SolicitacaoRelatorio;
import gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.enuns.StatusRelatorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SolicitacaoRelatorioRepository extends JpaRepository<SolicitacaoRelatorio, String> {

    List<SolicitacaoRelatorio> findByUsuarioOrderByDataSolicitacaoDesc(String usuario);
    Page<SolicitacaoRelatorio> findByUsuarioOrderByDataSolicitacaoDesc(String usuario, Pageable pageable);
    List<SolicitacaoRelatorio> findByStatusOrderByDataSolicitacaoDesc(StatusRelatorio status);

    @Query("SELECT s FROM SolicitacaoRelatorio s WHERE s.agendarPara <= :agora AND s.status = :status")
    List<SolicitacaoRelatorio> findRelatoriosParaExecucao(
        @Param("agora") LocalDateTime agora,
        @Param("status") StatusRelatorio status
    );

    @Query("SELECT s FROM SolicitacaoRelatorio s WHERE s.usuario = :usuario AND s.dataSolicitacao BETWEEN :inicio AND :fim")
    List<SolicitacaoRelatorio> findByUsuarioAndPeriodo(
        @Param("usuario") String usuario,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}