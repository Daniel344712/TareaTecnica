package com.project.demo.rest.producto;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/producto")
public class ProductoRestController {

    @Autowired
    ProductoRepository productoRepository;

    @Autowired CategoriaRepository categoriaRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Producto> productoPage = productoRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productoPage.getTotalPages());
        meta.setTotalElements(productoPage.getTotalElements());
        meta.setPageNumber(productoPage.getNumber() + 1);
        meta.setPageSize(productoPage.getSize());

        return new GlobalResponseHandler().handleResponse("Producto retrieved successfully",
                productoPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> addProductoToCategoria(@PathVariable Long categoriaId, @RequestBody Producto producto, HttpServletRequest request) {
        Optional<Categoria> foundCategoria = categoriaRepository.findById(categoriaId);
        if (foundCategoria.isPresent()) {
            producto.setCategoria(foundCategoria.get());
            Producto saveProducto =  productoRepository.save(producto);
            return new GlobalResponseHandler().handleResponse("Product created successfully", saveProducto, HttpStatus.CREATED, request);
        }else{
            return new GlobalResponseHandler().handleResponse("Categoria Id" + categoriaId + "not found", HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateProducto(@PathVariable Long productoId, @RequestBody Producto producto, HttpServletRequest request) {
        Optional<Producto> foundProducto = productoRepository.findById(productoId);
        if (foundProducto.isPresent()) {
            producto.setId(foundProducto.get().getId());
            producto.setCategoria(foundProducto.get().getCategoria());
            productoRepository.save(producto);
            return new GlobalResponseHandler().handleResponse("Product updated successfully", producto, HttpStatus.OK, request);
        }else{
            return new GlobalResponseHandler().handleResponse("Product id not found" + productoId, HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> patchProducto(@PathVariable Long productoId, @RequestBody Producto producto, HttpServletRequest request) {
       Optional<Producto> foundProducto = productoRepository.findById(productoId);
       if (foundProducto.isPresent()) {
          if(producto.getStock() != null) foundProducto.get().setStock(producto.getStock());
          productoRepository.save(foundProducto.get());
          return new GlobalResponseHandler().handleResponse("Product updated successfully", producto, HttpStatus.OK, request);

       }else{
           return new GlobalResponseHandler().handleResponse("Product id not found" + productoId, HttpStatus.NOT_FOUND, request);
       }
    }

    @DeleteMapping("/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteProducto(@PathVariable Long productoId, HttpServletRequest request) {
        Optional<Producto> foundProducto = productoRepository.findById(productoId);
        if(foundProducto.isPresent()) {
           Optional<Categoria> categoria = categoriaRepository.findById(foundProducto.get().getCategoria().getId());
           categoria.get().getProducto().remove(foundProducto.get());
           productoRepository.deleteById(foundProducto.get().getId());

           return new GlobalResponseHandler().handleResponse("Product deleted successfully", HttpStatus.OK, request);
        } else{
            return new GlobalResponseHandler().handleResponse("Product id not found" + productoId, HttpStatus.NOT_FOUND, request);
        }
    }
}


