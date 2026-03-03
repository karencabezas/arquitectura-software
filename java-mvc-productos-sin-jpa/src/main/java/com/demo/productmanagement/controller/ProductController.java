package com.demo.productmanagement.controller;

import com.demo.productmanagement.model.Product;
import com.demo.productmanagement.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // List all products
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/list";
    }

    // Show form to create a new product
    @GetMapping("/new")
    public String showNewProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    // Save a new product
    @PostMapping
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "Producto guardado con éxito");
        return "redirect:/products";
    }

    // Show form to edit a product
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> optionalProduct = productService.getProductById(id);

        if (optionalProduct.isPresent()) {
            model.addAttribute("product", optionalProduct.get());
            return "products/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/products";
        }
    }

    // Update a product
    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        boolean updated = productService.updateProduct(product);

        if (updated) {
            redirectAttributes.addFlashAttribute("message", "Producto actualizado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el producto");
        }

        return "redirect:/products";
    }

    // View a product
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> optionalProduct = productService.getProductById(id);

        if (optionalProduct.isPresent()) {
            model.addAttribute("product", optionalProduct.get());
            return "products/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/products";
        }
    }

    // Delete a product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = productService.deleteProduct(id);

        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Producto eliminado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto");
        }

        return "redirect:/products";
    }
}