package ru.vstu.clothstock.controller;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vstu.clothstock.model.Product;
import ru.vstu.clothstock.service.ProductService;

import java.io.OutputStreamWriter;
import java.util.List;

@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ProductService productService;

    @GetMapping("/pdf")
    public void exportToPDF(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");

        List<Product> products = productService.getAllProducts();

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        document.add(new Paragraph("Profit/Loss Report - ClothStock\n\n"));

        PdfPTable table = new PdfPTable(5);
        table.addCell(new PdfPCell(new Phrase("SKU")));
        table.addCell(new PdfPCell(new Phrase("Name")));
        table.addCell(new PdfPCell(new Phrase("Purchase Price")));
        table.addCell(new PdfPCell(new Phrase("Sale Price")));
        table.addCell(new PdfPCell(new Phrase("Net Profit")));

        for (Product product : products) {
            if ("Продан".equals(product.getStatus())) {
                table.addCell(product.getSku());
                table.addCell(product.getName());
                table.addCell(String.valueOf(product.getPurchasePrice()));
                table.addCell(String.valueOf(product.getPrice()));
                table.addCell(String.valueOf(product.getPrice() - product.getPurchasePrice()));
            }
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/csv")
    public void exportToCSV(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"report.csv\"");

        List<Product> products = productService.getAllProducts();

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"))) {
            response.getOutputStream().write(0xEF);
            response.getOutputStream().write(0xBB);
            response.getOutputStream().write(0xBF);

            String[] header = {"SKU", "Name", "Purchase Price", "Sale Price", "Net Profit"};
            writer.writeNext(header);

            for (Product product : products) {
                if ("Продан".equals(product.getStatus())) {
                    String[] data = {
                            product.getSku(),
                            product.getName(),
                            String.valueOf(product.getPurchasePrice()),
                            String.valueOf(product.getPrice()),
                            String.valueOf(product.getPrice() - product.getPurchasePrice())
                    };
                    writer.writeNext(data);
                }
            }
        }
    }
}