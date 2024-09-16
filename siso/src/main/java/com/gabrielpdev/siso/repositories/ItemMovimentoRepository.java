package com.gabrielpdev.siso.repositories;

import com.gabrielpdev.siso.models.ItemMovimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ItemMovimentoRepository extends JpaRepository<ItemMovimento, Long> {
    List<ItemMovimento> findByCaixaId(Long idCaixa);
    List<ItemMovimento> findByCaixaIdInAndDataHoraMovimentoBetween(List<Long> caixaIds, Timestamp startDate, Timestamp endDate);

}
