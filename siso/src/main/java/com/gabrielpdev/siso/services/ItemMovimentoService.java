package com.gabrielpdev.siso.services;

import com.gabrielpdev.siso.models.ItemMovimento;
import com.gabrielpdev.siso.models.exceptions.DataBindingViolationException;
import com.gabrielpdev.siso.models.exceptions.ObjectNotFoundException;
import com.gabrielpdev.siso.repositories.ItemMovimentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class ItemMovimentoService {

    @Autowired
    private ItemMovimentoRepository itemMovimentoRepository;

    // Retorna todos os movimentos
    public List<ItemMovimento> findAll() {
        return itemMovimentoRepository.findAll();
    }

    // Retorna um movimento por ID
    public ItemMovimento findById(Long id) {
        return itemMovimentoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("O itemMovimento {id: " + id + "} não foi encontrado"));
    }

    // Adiciona um novo movimento
    @Transactional
    public ItemMovimento createItemMovimento(ItemMovimento itemMovimento) {
        itemMovimento.setId(null);
        return itemMovimentoRepository.save(itemMovimento); // Retorna o item salvo, que incluirá o ID gerado
    }

    // Atualiza um movimento existente
    @Transactional
    public ItemMovimento updateItemMovimento(ItemMovimento itemMovimento) {
        if (!itemMovimentoRepository.existsById(itemMovimento.getId())) {
            throw new ObjectNotFoundException("O itemMovimento {id: " + itemMovimento.getId() + "} não foi encontrado");
        }

        return itemMovimentoRepository.save(itemMovimento); // Atualiza e retorna o item atualizado
    }

    // Deleta um movimento pelo ID
    public void deleteItemMovimento(Long id) {
        if (!itemMovimentoRepository.existsById(id)) {
            throw new ObjectNotFoundException("O itemMovimento {id: " + id + "} não foi encontrado");
        }

        try {
            itemMovimentoRepository.deleteById(id);
        } catch (Exception e) {
            throw new DataBindingViolationException("O itemMovimento não pode ser deletado.");
        }
    }

    // Obtém todos os movimentos para um caixa específico
    public List<ItemMovimento> getMovimentosPorCaixa(Long idCaixa) {
        return itemMovimentoRepository.findByCaixaId(idCaixa);
    }

    // Retorna um movimento por ID
    public ItemMovimento getItemMovimentoPorId(Long id) {
        return itemMovimentoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("O itemMovimento {id: " + id + "} não foi encontrado"));
    }

    public List<ItemMovimento> getMovimentosPorCaixasNoPeriodo(List<Long> caixaIds, Timestamp startDate, Timestamp endDate) {
        return itemMovimentoRepository.findByCaixaIdInAndDataHoraMovimentoBetween(caixaIds, startDate, endDate);
    }
}
