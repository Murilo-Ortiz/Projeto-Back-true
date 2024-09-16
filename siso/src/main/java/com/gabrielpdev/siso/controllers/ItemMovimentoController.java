package com.gabrielpdev.siso.controllers;

import com.gabrielpdev.siso.models.ItemMovimento;
import com.gabrielpdev.siso.services.ItemMovimentoService;
import com.gabrielpdev.siso.models.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itemMovimento")
@Validated
public class ItemMovimentoController {

    @Autowired
    private ItemMovimentoService itemMovimentoService;

    @GetMapping
    public ResponseEntity<List<ItemMovimento>> getAllItemMovimentos() {
        List<ItemMovimento> itemMovimentos = itemMovimentoService.findAll();
        return ResponseEntity.ok(itemMovimentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemMovimento> getItemMovimentoById(@PathVariable Long id) {
        try {
            ItemMovimento itemMovimento = itemMovimentoService.findById(id);
            return ResponseEntity.ok(itemMovimento);
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<ItemMovimento> createItemMovimento(@RequestBody ItemMovimento itemMovimento) {
        try {
            itemMovimentoService.createItemMovimento(itemMovimento);
            return ResponseEntity.status(HttpStatus.CREATED).body(itemMovimento);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemMovimento> updateItemMovimento(@PathVariable Long id, @RequestBody ItemMovimento itemMovimento) {
        try {
            itemMovimento.setId(id);
            itemMovimentoService.updateItemMovimento(itemMovimento);
            return ResponseEntity.ok(itemMovimento);
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemMovimento(@PathVariable Long id) {
        try {
            //itemMovimentoService.deleteItemMovimentoById(id);
            return ResponseEntity.noContent().build();
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/caixa/{idCaixa}")
    public ResponseEntity<List<ItemMovimento>> getMovimentosPorCaixa(@PathVariable Long idCaixa) {
        List<ItemMovimento> movimentos = itemMovimentoService.getMovimentosPorCaixa(idCaixa);
        return ResponseEntity.ok(movimentos);
    }
}
