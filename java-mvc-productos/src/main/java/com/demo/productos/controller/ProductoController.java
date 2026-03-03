package com.demo.productos.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.demo.productos.model.Producto;
import com.demo.productos.service.ProductoService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Mostrar todos los productos
    @GetMapping({"", "/"})
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.listarTodosLosProductos();
        model.addAttribute("productos", productos);
        return "listar-productos";
    }

    // Formulario para crear un nuevo producto
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "crear-producto";
    }

    // Guardar un nuevo producto
    @PostMapping("/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "crear-producto";
        }

        productoService.guardarProducto(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Producto guardado con éxito.");
        return "redirect:/productos";
    }

    // Formulario para editar un producto existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerProductoPorId(id);
        model.addAttribute("producto", producto);
        return "editar-producto";
    }

    // Actualizar un producto existente
    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                     @Valid @ModelAttribute("producto") Producto producto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "editar-producto";
        }

        producto.setId(id);
        productoService.guardarProducto(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado con éxito.");
        return "redirect:/productos";
    }

    // Eliminar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.eliminarProducto(id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado con éxito.");
        return "redirect:/productos";
    }

    // Buscar productos por nombre
    @GetMapping("/buscar")
    public String buscarProductos(@RequestParam(required = false) String nombre, Model model) {
        List<Producto> productos;

        if (nombre != null && !nombre.isEmpty()) {
            productos = productoService.buscarProductosPorNombre(nombre);
            model.addAttribute("nombreBusqueda", nombre);
        } else {
            productos = productoService.listarTodosLosProductos();
        }

        model.addAttribute("productos", productos);
        return "listar-productos";
    }
}