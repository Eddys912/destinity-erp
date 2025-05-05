package com.destinity.erp.sales;

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
 * Servicio encargado de manejar la lógica de negocio de las ventas.
 * Procesa las operaciones antes de ser delegadas al repositorio y
 * prepara los datos para el controlador.
 */
@ApplicationScoped
public class SaleService {

    private static final Logger LOGGER = Logger.getLogger(SaleService.class.getName());
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Inject
    private SaleRepository saleRepository;

    /**
     * Crea una nueva venta
     *
     * @param sale modelo de venta a crear
     * @return DTO de la venta creado o null si falla
     */
    public SaleDTO createSale(SaleModel sale) {
        if (sale.getId() == null) sale.setId(new ObjectId());
        if (sale.getStatus() == null || sale.getStatus().isBlank()) sale.setStatus("Completada");
        if (sale.getCreatedAt() == null) sale.setCreatedAt(LocalDateTime.now());

        String saleId = saleRepository.saveSale(sale);
        if (saleId == null) {
            LOGGER.log(Level.WARNING, "No se pudo guardar la venta: {0}", sale.getId());
            return null;
        }

        Optional<SaleModel> createdSale = saleRepository.findSaleById(saleId);
        createdSale.ifPresent(p -> LOGGER.log(Level.INFO, "Venta creada: {0}", p.getId()));
        return createdSale.map(this::convertToDTO).orElse(null);
    }

    /**
     * Obtiene todos las ventas paginadas
     *
     * @param page número de página (desde 0)
     * @param pageSize tamaño de página
     * @return lista de DTOs de ventas
     */
    public List<SaleDTO> getAllSales(int page, int pageSize) {
        if (page < 0) page = DEFAULT_PAGE;
        if (pageSize <= 0) pageSize = DEFAULT_PAGE_SIZE;

        List<SaleModel> sales = saleRepository.findAllSales(page, pageSize);
        if (sales == null || sales.isEmpty()) {
            LOGGER.warning("No hay ventas registradas");
            throw CustomException.notFound("No hay ventas registradas");
        }
        LOGGER.log(Level.INFO, "Ventas obtenidas: {0}", sales.size());
        return sales.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Obtiene una venta por su ID
     *
     * @param id identificador de la venta
     * @return DTO de la venta o null si no existe
     */
    public SaleDTO getSaleById(String id) {
        Optional<SaleModel> sale = saleRepository.findSaleById(id);
        if (sale.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró la venta con ID: {0}", id);
            throw CustomException.notFound("No existe la venta con el identificador proporcionado");
        }
        LOGGER.log(Level.INFO, "Venta encontrada: {0}", sale.get().getId());
        return convertToDTO(sale.get());
    }

    /**
     * Obtiene ventas por estatus
     *
     * @param status estatus a buscar
     * @return lista de DTOs de ventas
     */
    public List<SaleDTO> getSalesByStatus(String status) {
        List<SaleModel> sales = saleRepository.findSalesByStatus(status);
        if (sales == null || sales.isEmpty()) {
            LOGGER.log(Level.WARNING, "No hay ventas con el estatus: {0}", status);
            throw CustomException.notFound("No hay ventas con el estatus" + status);
        }
        LOGGER.log(Level.INFO, "Ventas encontradas con el estatus: {0}, cantidad: {1}",
                new Object[]{status, sales.size()});
        return sales.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Busqueda general por texto (coincidencia parcial)
     *
     * @param textSearch texto a buscar
     * @return lista de DTOs de ventas
     */
    public List<SaleDTO> searchSalesByClient(String textSearch) {
        List<SaleModel> sales = saleRepository.findSalesByText(textSearch);
        if (sales == null || sales.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontraron ventas con el texto: {0}", textSearch);
            throw CustomException.notFound("No se encontraron ventas con el texto proporcionado");
        }
        LOGGER.log(Level.INFO, "Ventas encontrados con el texto: {0}, cantidad: {1}",
                new Object[]{textSearch, sales.size()});
        return sales.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Actualiza un venta existente
     *
     * @param id identificador de la venta
     * @param sale datos actualizados
     * @throws CustomException si la venta no es válida
     * @return DTO del venta actualizado o null si falla
     */
    public SaleDTO updateSale(String id, SaleModel sale) {
        Optional<SaleModel> optionalExistingSale = saleRepository.findSaleById(id);
        if (optionalExistingSale.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró la venta para actualizar con ID: {0}", id);
            throw CustomException.notFound("No existe la venta con el identificador proporcionado");
        }

        SaleModel existingSale = optionalExistingSale.get();

        if (sale.getStatus().equals(existingSale.getStatus())) {
            LOGGER.log(Level.WARNING, "No se detectaron cambios al actualizar la venta con ID: {0}", id);
            throw CustomException.business("No se detectaron cambios. La venta no fue modificada");
        }

        InputValidator.isNotEmpty(sale.getStatus(), "Estatus");
        existingSale.setStatus(sale.getStatus());
        existingSale.setUpdatedAt(LocalDateTime.now());

        String updatedId = saleRepository.updateSale(existingSale);
        if (updatedId == null) {
            LOGGER.log(Level.WARNING, "No se pudo actualizar la venta con ID: {0}", id);
            return null;
        }

        Optional<SaleModel> updatedSale = saleRepository.findSaleById(updatedId);
        updatedSale.ifPresent(p -> LOGGER.log(Level.INFO, "Venta actualizada: {0}", p.getId()));
        return updatedSale.map(this::convertToDTO).orElse(null);
    }

    /**
     * Elimina una venta
     *
     * @param id identificador de la venta
     * @throws CustomException si la venta no es válida
     * @return true si se eliminó correctamente
     */
    public boolean deleteSale(String id) {
        Optional<SaleModel> existingSale = saleRepository.findSaleById(id);
        if (existingSale.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró la venta para eliminar con ID: {0}", id);
            throw CustomException.notFound("No existe la venta con el identificador proporcionado");
        }

        boolean deleted = saleRepository.deleteSale(id);
        if (!deleted)
            LOGGER.log(Level.WARNING, "Error al intentar eliminar la venta con ID: {0}", id);
        LOGGER.log(Level.INFO, "Venta eliminada con ID: {0}", id);
        return deleted;
    }

    /**
     * Obtiene el conteo total de ventas
     *
     * @return número total de ventas
     */
    public long getTotalSaleCount() {
        long count = saleRepository.countSales();
        LOGGER.log(Level.INFO, "Total de ventas registradas: {0}", count);
        return count;
    }

    /**
     * Convierte un modelo a DTO
     *
     * @param sale modelo de venta
     * @return DTO de venta
     */
    private SaleDTO convertToDTO(SaleModel sale) {
        if (sale == null) return null;
        return new SaleDTO(sale);
    }
}
