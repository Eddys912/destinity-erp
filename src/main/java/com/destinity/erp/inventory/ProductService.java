package com.destinity.erp.inventory;

import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.InputValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * Servicio encargado de manejar la lógica de negocio de los productos.
 * Procesa las operaciones antes de ser delegadas al repositorio y
 * prepara los datos para el controlador.
 */
@ApplicationScoped
public class ProductService {

    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final List<String> VALID_CATEGORIES = List.of("BLANCOS", "ALIMENTOS", "ELECTRÓNICOS");

    @Inject
    private ProductRepository productRepository;

    /**
     * Crea un nuevo producto
     *
     * @param product modelo de producto a crear
     * @return DTO del producto creado o null si falla
     */
    public ProductDTO createProduct(ProductModel product) {
        if (product.getId() == null) product.setId(new ObjectId());
        if (product.getStatus() == null || product.getStatus().isBlank()) product.setStatus("Disponible");
        if (product.getCreatedAt() == null) product.setCreatedAt(LocalDateTime.now());

        isValidProduct(product);

        String productId = productRepository.saveProduct(product);
        if (productId == null) {
            LOGGER.log(Level.WARNING, "No se pudo guardar el producto: {0}", product.getName());
            return null;
        }

        Optional<ProductModel> createdProduct = productRepository.findProductById(productId);
        createdProduct.ifPresent(p -> LOGGER.log(Level.INFO, "Producto creado: {0}", p.getName()));
        return createdProduct.map(this::convertToDTO).orElse(null);
    }

    /**
     * Obtiene todos los productos paginados
     *
     * @param page número de página (desde 0)
     * @param pageSize tamaño de página
     * @return lista de DTOs de productos
     */
    public List<ProductDTO> getAllProducts(int page, int pageSize) {
        if (page < 0) page = DEFAULT_PAGE;
        if (pageSize <= 0) pageSize = DEFAULT_PAGE_SIZE;

        List<ProductModel> products = productRepository.findAllProducts(page, pageSize);
        if (products == null || products.isEmpty()) {
            LOGGER.warning("No hay productos en el inventario");
            throw CustomException.notFound("No hay productos en el inventario");
        }
        LOGGER.log(Level.INFO, "Productos obtenidos: {0}", products.size());
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Obtiene un producto por su ID
     *
     * @param id identificador del producto
     * @return DTO del producto o null si no existe
     */
    public ProductDTO getProductById(String id) {
        Optional<ProductModel> product = productRepository.findProductById(id);
        if (product.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el producto con ID: {0}", id);
            throw CustomException.notFound("No existe el producto con el identificador proporcionado");
        }
        LOGGER.log(Level.INFO, "Producto encontrado: {0}", product.get().getName());
        return convertToDTO(product.get());
    }

    /**
     * Obtiene productos por categoría
     *
     * @param category categoría a buscar
     * @return lista de DTOs de productos
     */
    public List<ProductDTO> getProductsByCategory(String category) {
        List<ProductModel> products = productRepository.findProductsByCategory(category);
        if (products == null || products.isEmpty()) {
            LOGGER.log(Level.WARNING, "No hay productos en la categoría: {0}", category);
            throw CustomException.notFound("No hay productos en la categoría proporcionada");
        }
        LOGGER.log(Level.INFO, "Productos encontrados en la categoría: {0}, cantidad: {1}",
                new Object[]{category, products.size()});
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Busqueda general por texto (coincidencia parcial)
     *
     * @param textSearch texto a buscar
     * @return lista de DTOs de ventas
     */
    public List<ProductDTO> searchProductsByName(String textSearch) {
        List<ProductModel> products = productRepository.findProductsByText(textSearch);
        if (products == null || products.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontraron productos con el texto: {0}", textSearch);
            throw CustomException.notFound("No se encontraron productos con el texto proporcionado");
        }
        LOGGER.log(Level.INFO, "Productos encontrados con el texto: {0}, cantidad: {1}",
                new Object[]{textSearch, products.size()});
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Actualiza un producto existente
     *
     * @param id identificador del producto
     * @param product datos actualizados
     * @throws CustomException si el producto no es válido
     * @return DTO del producto actualizado o null si falla
     */
    public ProductDTO updateProduct(String id, ProductModel product) {
        Optional<ProductModel> optionalExistingProduct = productRepository.findProductById(id);
        if (optionalExistingProduct.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el producto para actualizar con ID: {0}", id);
            throw CustomException.notFound("No existe el producto con el identificador proporcionado");
        }

        ProductModel existingProduct = optionalExistingProduct.get();

        if (isSameProduct(product, existingProduct)) {
            LOGGER.log(Level.WARNING, "No se detectaron cambios al actualizar el producto con ID: {0}", id);
            throw CustomException.business("No se detectaron cambios. El producto no fue modificado");
        }

        isValidProduct(product);
        applyChanges(existingProduct, product);
        existingProduct.setUpdatedAt(LocalDateTime.now());

        String updatedId = productRepository.updateProduct(existingProduct);
        if (updatedId == null) {
            LOGGER.log(Level.WARNING, "No se pudo actualizar el producto con ID: {0}", id);
            return null;
        }

        Optional<ProductModel> updatedProduct = productRepository.findProductById(updatedId);
        updatedProduct.ifPresent(p -> LOGGER.log(Level.INFO, "Producto actualizado: {0}", p.getName()));
        return updatedProduct.map(this::convertToDTO).orElse(null);
    }

    /**
     * Elimina un producto
     *
     * @param id identificador del producto
     * @throws CustomException si el producto no es válido
     * @return true si se eliminó correctamente
     */
    public boolean deleteProduct(String id) {
        Optional<ProductModel> existingProduct = productRepository.findProductById(id);
        if (existingProduct.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el producto para eliminar con ID: {0}", id);
            throw CustomException.notFound("No existe el producto con el identificador proporcionado");
        }

        boolean deleted = productRepository.deleteProduct(id);
        if (!deleted)
            LOGGER.log(Level.WARNING, "Error al intentar eliminar el producto con ID: {0}", id);
        LOGGER.log(Level.INFO, "Producto eliminado con ID: {0}", id);
        return deleted;
    }

    /**
     * Obtiene el conteo total de productos
     *
     * @return número total de productos
     */
    public long getTotalProductCount() {
        long count = productRepository.countProducts();
        LOGGER.log(Level.INFO, "Total de productos en inventario: {0}", count);
        return count;
    }

    /**
     * Valida un producto
     *
     * @param category producto a validar
     * @return true o false
     */
    private boolean isValidCategory(String category) {
        return VALID_CATEGORIES.contains(category.toUpperCase());
    }

    /**
     * Muestra todas las categorias validas
     *
     * @return categorias validas
     */
    private String alloewdCategories() {
        return String.join(", ", VALID_CATEGORIES);
    }

    /**
     * Valida un producto
     *
     * @param product producto a validar
     * @throws CustomException si el producto no es válido
     * @throws InputValidator si algún campo no es válido
     */
    private void isValidProduct(ProductModel product) {
        if (product == null)
            throw CustomException.business("El producto no puede estar vacio");

        InputValidator.isNotEmpty(product.getName(), "Nombre");
        InputValidator.isNotEmpty(product.getCategory(), "Categoria");
        InputValidator.isNotEmpty(product.getDescription(), "Descrpción");
        InputValidator.isNotEmpty(product.getProvider(), "Proveedor");
        InputValidator.isNotEmpty(product.getImage(), "Imágen");

        if (product.getPrice() == null || product.getPrice() <= 0.0)
            throw CustomException.business("El precio debe ser mayor a 0");
        if (product.getStock() == null || product.getStock() <= 10)
            throw CustomException.business("El stock debe ser mayor a 10");
        if (product.getStock() % 1 != 0)
            throw CustomException.business("El stock debe contener un número entero");
        if (!isValidCategory(product.getCategory()))
            throw CustomException.business("La categoría no es valida. Usa: " + alloewdCategories());
    }

    /**
     * Comprueba si dos productos son iguales
     *
     * @param p1 primer producto
     * @param p2 segundo producto
     * @return true si son iguales
     */
    private boolean isSameProduct(ProductModel p1, ProductModel p2) {
        return p1.getName().equals(p2.getName())
                && p1.getCategory().equals(p2.getCategory())
                && p1.getDescription().equals(p2.getDescription())
                && p1.getImage().equals(p2.getImage())
                && p1.getProvider().equals(p2.getProvider())
                && p1.getStatus().equals(p2.getStatus())
                && Double.compare(p1.getPrice(), p2.getPrice()) == 0
                && Integer.compare(p1.getStock(), p2.getStock()) == 0;
    }

    /**
     * Aplica cambios de un producto a otro
     *
     * @param target producto destino
     * @param source producto origen
     */
    private void applyChanges(ProductModel target, ProductModel source) {
        target.setName(source.getName());
        target.setPrice(source.getPrice());
        target.setStock(source.getStock());
        target.setCategory(source.getCategory());
        target.setImage(source.getImage());
        target.setDescription(source.getDescription());
        target.setProvider(source.getProvider());
        target.setStatus(source.getStatus());
    }

    /**
     * Convierte un modelo a DTO
     *
     * @param product modelo de producto
     * @return DTO de producto
     */
    private ProductDTO convertToDTO(ProductModel product) {
        if (product == null) return null;
        return new ProductDTO(product);
    }
}
