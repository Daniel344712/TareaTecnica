package com.project.demo.rest.categoria;

import com.project.demo.logic.entity.Categoria.Categoria;
import com.project.demo.logic.entity.Categoria.CategoriaRepository;
import com.project.demo.logic.entity.Producto.Producto;
import com.project.demo.logic.entity.Producto.ProductoRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.order.Order;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/categoria")
public class CategoriaRestController {
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository ProductoRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Categoria> categoriaPage = categoriaRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriaPage.getTotalPages());
        meta.setTotalElements(categoriaPage.getTotalElements());
        meta.setPageNumber(categoriaPage.getNumber() + 1);
        meta.setPageSize(categoriaPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categoria retrieved successfully",
                categoriaPage.getContent(), HttpStatus.OK, meta);

    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createCategoria(@RequestBody Categoria categoria, HttpServletRequest request) {
        if (categoria.getDescripcion() != null && categoria.getNombre() != null) {
            categoriaRepository.save(categoria);
            return new GlobalResponseHandler().handleResponse("Categoria created successfully", categoria, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoria creation failed", categoria, HttpStatus.BAD_REQUEST, request);
        }

    }

    @PutMapping("{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateCategoria(@PathVariable Long categoriaId, @RequestBody Categoria categoria, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = categoriaRepository.findById(categoriaId);
        if (foundCategoria.isPresent()) {
            categoria.setId(categoriaId);
            categoria.setProducto(foundCategoria.get().getProducto());
            categoriaRepository.save(categoria);
            return new GlobalResponseHandler().handleResponse("Categoria updated successfully", categoria, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoria update failed: " + categoriaId + " not found", HttpStatus.NOT_FOUND, request);
        }
    }


    @PatchMapping("{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> patchProducto(@PathVariable Long categoriaId, @RequestBody Categoria categoria, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = categoriaRepository.findById(categoriaId);
        if (foundCategoria.isPresent()) {
            if(categoria.getNombre() != null) foundCategoria.get().setNombre(categoria.getNombre());
            categoriaRepository.save(foundCategoria.get());
            return new GlobalResponseHandler().handleResponse("Categoria updated successfully", HttpStatus.OK, request);

        }else{
            return new GlobalResponseHandler().handleResponse("Categoria id not found" + categoriaId, HttpStatus.NOT_FOUND, request);
        }
    }
    @DeleteMapping("{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteCategoria(@PathVariable Long categoriaId, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = categoriaRepository.findById(categoriaId);

        if (foundCategoria.isPresent()) {
            categoriaRepository.deleteById(foundCategoria.get().getId());
            return new GlobalResponseHandler().handleResponse("Categoria deleted successfully", HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Categoria not found" + categoriaId, HttpStatus.NOT_FOUND, request );
        }
    }

}
