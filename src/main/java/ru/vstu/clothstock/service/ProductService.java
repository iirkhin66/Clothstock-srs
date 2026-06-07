package ru.vstu.clothstock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vstu.clothstock.model.HistoryLog;
import ru.vstu.clothstock.model.Product;
import ru.vstu.clothstock.repository.HistoryLogRepository;
import ru.vstu.clothstock.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final HistoryLogRepository historyLogRepository;
    private final EmailService emailService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Товар не найден"));
    }

    public void saveProduct(Product product) {
        boolean isNew = product.getId() == null;
        productRepository.save(product);
        logAction(isNew ? "Добавлен новый товар: " + product.getName() : "Обновлен товар: " + product.getName());
        checkStockAndAlert(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.deleteById(id);
        logAction("Удален товар: " + product.getName());
    }

    public void changeStatus(Long id, String newStatus) {
        Product product = getProductById(id);

        if (product.getStatus().equals(newStatus)) {
            return;
        }

        if ("Продан".equals(newStatus)) {
            if (product.getStock() <= 0) {
                throw new RuntimeException("Невозможно продать товар, остаток равен 0");
            }
            product.setStock(product.getStock() - 1);
        }

        product.setStatus(newStatus);
        productRepository.save(product);
        logAction("Изменен статус товара " + product.getName() + " на '" + newStatus + "'");
        checkStockAndAlert(product);
    }

    private void checkStockAndAlert(Product product) {
        if (product.getStock() > 0 && product.getStock() <= 2) {
            emailService.sendLowStockAlert(product.getName(), product.getStock());
        }
    }

    private void logAction(String description) {
        HistoryLog log = new HistoryLog();
        log.setActionDescription(description);
        historyLogRepository.save(log);
    }
}