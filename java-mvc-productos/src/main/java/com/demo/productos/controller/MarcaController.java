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

import com.demo.productos.model.Marca;
import com.demo.productos.service.MarcaService;

@Controller
@RequestMapping("/marcas")
public class MarcaController {

    @Autowired
    private MarcaService marcaService;

    // listar las marcas
    @GetMapping({"", "/"})
    public String listarMarcas(Model model) {
        List<Marca> marcas = marcaService.listarTodasLasMarcas();
        model.addAttribute("marcas", marcas);
        return "listar-marcas";
    }

    // Formulario para crear una marca
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaMarca(Model model) {
        model.addAttribute("marca", new Marca());
        return "crear-marca";
    }

    // Guardar una nueva marca
    @PostMapping("/guardar")
    public String guardarMarca(@Valid @ModelAttribute("marca") Marca marca,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "crear-marca";
        }

        marcaService.guardarMarca(marca);
        redirectAttributes.addFlashAttribute("mensaje", "Marca guardada con éxito.");
        return "redirect:/marcas";
    }

    // Formulario para editar una marca
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarMarca(@PathVariable Long id, Model model) {
        Marca marca = marcaService.obtenerMarcaPorId(id);
        model.addAttribute("marca", marca);
        return "editar-marca";
    }

    // Actualizar una marca
    @PostMapping("/actualizar/{id}")
    public String actualizarMarca(@PathVariable Long id,
                                  @Valid @ModelAttribute("marca") Marca marca,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "editar-marca";
        }

        marca.setId(id);
        marcaService.guardarMarca(marca);
        redirectAttributes.addFlashAttribute("mensaje", "Marca actualizada con éxito.");
        return "redirect:/marcas";
    }

    // Eliminar una marca
    @GetMapping("/eliminar/{id}")
    public String eliminarMarca(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        marcaService.eliminarMarca(id);
        redirectAttributes.addFlashAttribute("mensaje", "Marca eliminada con éxito.");
        return "redirect:/marcas";
    }

    // Buscar marcas por nombre
    @GetMapping("/buscar")
    public String buscarMarcas(@RequestParam(required = false) String nombre, Model model) {
        List<Marca> marcas;

        if (nombre != null && !nombre.isEmpty()) {
            marcas = marcaService.buscarMarcasPorNombre(nombre);
            model.addAttribute("nombreBusqueda", nombre);
        } else {
            marcas = marcaService.listarTodasLasMarcas();
        }

        model.addAttribute("marcas", marcas);
        return "listar-marcas";
    }

}
