package com.gabrielpdev.siso.controllers;

import com.gabrielpdev.siso.models.Caixa;
import com.gabrielpdev.siso.models.ItemMovimento;
import com.gabrielpdev.siso.services.CaixaService;
import com.gabrielpdev.siso.services.ItemMovimentoService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@RestController
@RequestMapping("/api/caixa")
public class CaixaController {

    @Autowired
    private CaixaService caixaService;

    @Autowired
    private ItemMovimentoService itemMovimentoService;

    @GetMapping("/{id_usuario}")
    public ResponseEntity<Caixa> getCaixa(@PathVariable Long id_usuario) {
        Optional<Caixa> caixa = this.caixaService.getCaixaAberto(id_usuario);
        return caixa.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id_usuario}")
    public ResponseEntity<Caixa> postCaixa(@PathVariable Long id_usuario) {
        Caixa novoCaixa = this.caixaService.abrirCaixa(id_usuario);
        if (novoCaixa == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Retorna 400 se houve erro ao abrir o caixa
        }
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoCaixa.getId())
                .toUri();

        return ResponseEntity.created(uri).body(novoCaixa);
    }

    @PutMapping("/{id_usuario}")
    public ResponseEntity<Void> putCaixa(@PathVariable Long id_usuario) {
        this.caixaService.fecharCaixa(id_usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id_caixa}/movimentos")
    public ResponseEntity<List<ItemMovimento>> getMovimentos(@PathVariable Long id_caixa) {
        List<ItemMovimento> movimentos = itemMovimentoService.getMovimentosPorCaixa(id_caixa);
        return ResponseEntity.ok(movimentos);
    }

    @PostMapping("/{id_caixa}/movimentos")
    public ResponseEntity<ItemMovimento> addMovimento(@PathVariable Long id_caixa, @RequestBody ItemMovimento itemMovimento) {
        try {
            // Verifica se o caixa existe
            Optional<Caixa> caixa = caixaService.getCaixaPorId(id_caixa);
            if (!caixa.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404 se o caixa não for encontrado
            }

            itemMovimento.setCaixa(caixa.get()); // Associa o caixa ao movimento
            ItemMovimento savedItemMovimento = itemMovimentoService.createItemMovimento(itemMovimento);

            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedItemMovimento.getId()).toUri();
            return ResponseEntity.created(uri).body(savedItemMovimento);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Retorna 400 se houver um erro
        }
    }
    @GetMapping("/caixas")
    public void gerarRelatorio(HttpServletResponse response) throws DocumentException, IOException {
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_caixas.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

        // Recupera todas as caixas
        List<Caixa> caixas = caixaService.getAllCaixas();
        for (Caixa caixa : caixas) {
            document.add(new Paragraph("Caixa nº: " + (caixa.getId() != null ? caixa.getId() : "-"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph("Usuário responsável: " + (caixa.getUsuario() != null ? caixa.getUsuario().getUsername() : "-"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph("Data de abertura: " + (caixa.getAbertura() != null ? dateFormat.format(caixa.getAbertura()) : "-"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph("Data de fechamento: " + (caixa.getFechamento() != null ? dateFormat.format(caixa.getFechamento()) : "-"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph("\n"));

            // Adiciona uma tabela com os movimentos do caixa
            PdfPTable table = new PdfPTable(6); // Ajustado para 7 colunas
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 2, 2});

            table.addCell("Operação");
            table.addCell("Tipo");
            table.addCell("Modalidade");
            table.addCell("Fornecedor - Dentista");
            table.addCell("Valor");
            table.addCell("Data/Hora");

            double total = 0.0;
            List<ItemMovimento> movimentos = itemMovimentoService.getMovimentosPorCaixa(caixa.getId());
            for (ItemMovimento movimento : movimentos) {
                table.addCell(movimento.getOperacao() != null ? movimento.getOperacao() : "-");

                String tipoReceitaOuDespesa = "-";
                if (movimento.getReceita() != null) {
                    tipoReceitaOuDespesa = movimento.getReceita().getDescricao(); // Supondo que a receita tem uma descrição
                } else if (movimento.getDespesa() != null) {
                    tipoReceitaOuDespesa = movimento.getDespesa().getDescricao(); // Supondo que a despesa tem uma descrição
                }
                table.addCell(tipoReceitaOuDespesa);

                table.addCell(movimento.getModalidade() != null ? movimento.getModalidade() : "-");

                String fornecedorOuDentista = "-";
                if (movimento.getFornecedor() != null) {
                    fornecedorOuDentista = movimento.getFornecedor().getNome(); // Supondo que o fornecedor tem um nome
                } else if (movimento.getDentista() != null) {
                    fornecedorOuDentista = movimento.getDentista().getNome(); // Supondo que o dentista tem um nome
                }
                table.addCell(fornecedorOuDentista);

                table.addCell(movimento.getValor() != null ? String.format(Locale.ENGLISH, "%.2f", movimento.getValor()) : "-");
                table.addCell(movimento.getDataHoraMovimento() != null ? dateFormat.format(movimento.getDataHoraMovimento()) : "-");

                total += (movimento.getValor() != null ? movimento.getValor() : 0.0);
            }

            document.add(table);
            document.add(new Paragraph("\nTotal do Caixa: " + String.format(Locale.ENGLISH, "%.2f", total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph("\n\n"));
        }

        document.close();
    }
}
