package com.destinity.erp.sales;

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
 * Controlador REST encargado de las peticiones con las ventas.
 * Expone los endpoint de la API para registrar, consultar y
 * gestionar ventas a tráves del sistema.
 */
@Path("/sales")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SaleController {

    @Inject
    private SaleService saleService;

    /**
     * Crea una nueva venta
     *
     * @param sale datos de la venta a crear
     * @return respuesta con la venta creada o error
     */
    @POST
    public Response createSale(SaleModel sale) {
        try {
            saleService.createSale(sale);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Venta creada satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Obtiene ventas paginadas
     *
     * @param page número de página (desde 0)
     * @param size tamaño de página
     * @return respuesta con la lista paginada de ventas
     */
    @GET
    @Path("/all")
    public Response getAllSales(@QueryParam("page") int page, @QueryParam("size") int size) {
        try {
            List<SaleDTO> sales = saleService.getAllSales(page, size);
            long totalCount = saleService.getTotalSaleCount();

            return Response.ok(sales)
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
     * Obtiene un venta por su ID
     *
     * @param id identificador del venta
     * @return respuesta con la venta o error si no existe
     */
    @GET
    public Response getSaleById(@QueryParam("id") String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }
            SaleDTO sale = saleService.getSaleById(id);
            return Response.ok(sale).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca ventas por estatus
     *
     * @param status estatus a buscar
     * @return respuesta con la lista de ventas
     */
    @GET
    @Path("/status")
    public Response getSalesByStatus(@QueryParam("status") String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }
            List<SaleDTO> sales = saleService.getSalesByStatus(status);
            return Response.ok(sales).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca ventas por cliente
     *
     * @param client nombre o parte del nombre a buscar
     * @return respuesta con la lista de ventas
     */
    @GET
    @Path("/search")
    public Response searchSalesByName(@QueryParam("name") String client) {
        try {
            if (client == null || client.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }

            List<SaleDTO> sales = saleService.searchSalesByClient(client);
            return Response.ok(sales).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Actualiza una venta existente
     *
     * @param id identificador del venta
     * @param sale datos actualizados
     * @return respuesta con el venta actualizado o error
     */
    @PUT
    @Path("/{id}")
    public Response updateSale(@PathParam("id") String id, SaleModel sale) {
        try {
            saleService.updateSale(id, sale);
            return Response.status(Response.Status.OK)
                    .entity(Map.of("message", "Venta actualizada satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Elimina una venta
     *
     * @param id identificador del venta
     * @return respuesta de éxito o error
     */
    @DELETE
    @Path("/{id}")
    public Response deleteSale(@PathParam("id") String id) {
        try {
            saleService.deleteSale(id);
            return Response.status(Response.Status.OK)
                    .entity(Map.of("message", "Venta eliminada satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }
}
