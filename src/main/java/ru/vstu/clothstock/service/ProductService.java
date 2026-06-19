package ru.vstu.clothstock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vstu.clothstock.model.HistoryLog;
import ru.vstu.clothstock.model.Product;
import ru.vstu.clothstock.repository.HistoryLogRepository;
import ru.vstu.clothstock.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервисный класс, инкапсулирующий бизнес-логику управления товарным ассортиментом.
 * Отвечает за сохранение, удаление, поиск товаров и изменение их логических состояний.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final HistoryLogRepository historyLogRepository;
    private final EmailService emailService;

    /**
     * Получает полный список всех товаров из базы данных.
     *
     * @return список объектов {@link Product}
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Выполняет поиск товаров по совпадению в названии или артикуле (SKU).
     *
     * @param query строка поискового запроса
     * @return отфильтрованный список товаров или полный список, если запрос пуст
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query);
    }

    /**
     * Ищет товар по его уникальному идентификатору.
     *
     * @param id уникальный идентификатор товара (первичный ключ)
     * @return найденный объект {@link Product}
     * @throws RuntimeException если товар с указанным ID не найден
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Товар не найден"));
    }

    /**
     * Сохраняет новый товар или обновляет существующий.
     * При добавлении нового товара фиксируется текущее время для работы таймера.
     *
     * @param product объект сохраняемого товара
     */
    public void saveProduct(Product product) {
        boolean isNew = product.getId() == null;
        if (isNew) {
            product.setStatusUpdatedAt(LocalDateTime.now());
        } else if (product.getStatusUpdatedAt() == null) {
            product.setStatusUpdatedAt(LocalDateTime.now());
        }
        productRepository.save(product);
        logAction(isNew ? "Добавлен новый товар: " + product.getName() : "Обновлен товар: " + product.getName());
        checkStockAndAlert(product);
    }

    /**
     * Удаляет товар из базы данных по его идентификатору.
     *
     * @param id уникальный идентификатор удаляемого товара
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.deleteById(id);
        logAction("Удален товар: " + product.getName());
    }

    /**
     * Изменяет логический статус товара.
     * Если новый статус "Продан", метод автоматически уменьшает складской остаток на 1.
     *
     * @param id уникальный идентификатор товара
     * @param newStatus целевой статус ("На складе", "В торговом зале", "Продан")
     * @throws RuntimeException если происходит попытка продать товар с нулевым остатком
     */
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

        if ("На складе".equals(newStatus)) {
            product.setStatusUpdatedAt(LocalDateTime.now());
        }

        product.setStatus(newStatus);
        productRepository.save(product);
        logAction("Изменен статус товара " + product.getName() + " на '" + newStatus + "'");
        checkStockAndAlert(product);
    }

    /**
     * Проверяет текущий остаток товара и инициирует отправку email-уведомления,
     * если количество опускается до критического уровня.
     *
     * @param product проверяемый товар
     */
    private void checkStockAndAlert(Product product) {
        if (product.getStock() > 0 && product.getStock() <= 2) {
            emailService.sendLowStockAlert(product.getName(), product.getStock());
        }
    }

    /**
     * Записывает действие в системный журнал истории.
     *
     * @param description текстовое описание выполненного действия
     */
    private void logAction(String description) {
        HistoryLog log = new HistoryLog();
        log.setActionDescription(description);
        historyLogRepository.save(log);
    }
}