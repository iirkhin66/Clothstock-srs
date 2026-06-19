package ru.vstu.clothstock.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vstu.clothstock.model.Product;
import ru.vstu.clothstock.repository.HistoryLogRepository;
import ru.vstu.clothstock.repository.ProductRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HistoryLogRepository historyLogRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ProductService productService;

    @Test
    void changeStatus_ToSold_DecreasesStock() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Куртка");
        product.setStatus("В торговом зале");
        product.setStock(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.changeStatus(1L, "Продан");

        assertEquals("Продан", product.getStatus());
        assertEquals(4, product.getStock()); // Остаток должен уменьшиться на 1
        verify(productRepository, times(1)).save(product);
        verify(historyLogRepository, times(1)).save(any());
    }

    @Test
    void changeStatus_ToSold_ZeroStock_ThrowsException() {
        Product product = new Product();
        product.setId(2L);
        product.setName("Футболка");
        product.setStatus("На складе");
        product.setStock(0);

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.changeStatus(2L, "Продан");
        });

        assertEquals("Невозможно продать товар, остаток равен 0", exception.getMessage());
        verify(productRepository, never()).save(any());
    }
}