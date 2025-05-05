package com.destinity.erp.inventory;

import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.RestExceptionHandler;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST encargado de las peticiones con los productos.
 * Expone los endpoint de la API para registrar, consultar y
 * gestionar productos a tráves del sistema.
 */
@Path("/products")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductController {

    @Inject
    private ProductService productService;

    /**
     * Crea un nuevo producto
     *
     * @param product datos del producto a crear
     * @return respuesta con el producto creado o error
     */
    @POST
    public Response createProduct(ProductModel product) {
        try {
            productService.createProduct(product);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Producto creado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Obtiene productos paginados
     *
     * @param page número de página (desde 0)
     * @param size tamaño de página
     * @return respuesta con la lista paginada de productos
     */
    @GET
    @Path("/all")
    public Response getAllProducts(@QueryParam("page") int page, @QueryParam("size") int size) {
        try {
            List<ProductDTO> products = productService.getAllProducts(page, size);
            long totalCount = productService.getTotalProductCount();

            return Response.ok(products)
                    .header("X-Total-Count", totalCount)
                    .header("X-Page", page)
                    .header("X-Page-Size", size)
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Obtiene un producto por su ID
     *
     * @param id identificador del producto
     * @return respuesta con el producto o error si no existe
     */
    @GET
    public Response getProductById(@QueryParam("id") String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }
            ProductDTO product = productService.getProductById(id);
            return Response.ok(product).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca productos por categoría
     *
     * @param category categoría a buscar
     * @return respuesta con la lista de productos
     */
    @GET
    @Path("/category")
    public Response getProductsByCategory(@QueryParam("category") String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }
            List<ProductDTO> products = productService.getProductsByCategory(category);
            return Response.ok(products).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca productos por nombre
     *
     * @param name nombre o parte del nombre a buscar
     * @return respuesta con la lista de productos
     */
    @GET
    @Path("/search")
    public Response searchProductsByName(@QueryParam("name") String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }

            List<ProductDTO> products = productService.searchProductsByName(name);
            return Response.ok(products).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Actualiza un producto existente
     *
     * @param id identificador del producto
     * @param product datos actualizados
     * @return respuesta con el producto actualizado o error
     */
    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") String id, ProductModel product) {
        try {
            productService.updateProduct(id, product);
            return Response.status(Response.Status.OK)
                    .entity(Map.of("message", "Producto actualizado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Elimina un producto
     *
     * @param id identificador del producto
     * @return respuesta de éxito o error
     */
    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") String id) {
        try {
            productService.deleteProduct(id);
            return Response.status(Response.Status.OK)
                    .entity(Map.of("message", "Producto eliminado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }
}
