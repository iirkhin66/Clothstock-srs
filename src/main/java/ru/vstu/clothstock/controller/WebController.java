package ru.vstu.clothstock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vstu.clothstock.model.Product;
import ru.vstu.clothstock.model.User;
import ru.vstu.clothstock.repository.UserRepository;
import ru.vstu.clothstock.service.ProductService;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/products")
    public String productsPage(Model model, @RequestParam(required = false) String query) {
        if (query != null && !query.isBlank()) {
            model.addAttribute("products", productService.searchProducts(query));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        return "products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @PostMapping("/products/add")
    public String addProductSubmit(@ModelAttribute Product product) {
        product.setStatus("На складе");
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam String status) {
        productService.changeStatus(id, status);
        return "redirect:/products";
    }

    @GetMapping("/report")
    public String reportPage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "report";
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/users";
    }
}