package com.destinity.erp.sales;

import com.destinity.erp.database.DataBaseConnection;
import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.ToDate;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Repositorio encargado de la interacción directa con MongoDb.
 * Administra la conversión entre el modelo de ventas y los
 * documentos almacenados en la base de datos.
 */
@ApplicationScoped
public class SaleRepository {

    private static final Logger LOGGER = Logger.getLogger(SaleRepository.class.getName());
    private static final String COLLECTION_NAME = "sales";

    @Inject
    private DataBaseConnection dbConnection;

    /**
     * Obtiene la colección de ventas
     *
     * @return MongoCollection de ventas
     */
    private MongoCollection<Document> getSaleCollection() {
        return dbConnection.getDatabase().getCollection(COLLECTION_NAME);
    }

    /**
     * Guarda una nueva venta en la base de datos
     *
     * @param sale modelo de venta a guardar
     * @return ID de la venta guardada o null si falla
     */
    public String saveSale(SaleModel sale) {
        try {
            Document saleDoc = saleToDocument(sale);
            InsertOneResult result = getSaleCollection().insertOne(saleDoc);
            return result.getInsertedId().asObjectId().getValue().toString();
        } catch (MongoWriteException e) {
            WriteError writeError = e.getError();
            if (writeError != null && writeError.getCode() == 121) {
                LOGGER.log(Level.WARNING, "El documento no cumple con el esquema definido: {0}", e.getMessage());
                throw CustomException.dbValidationFailed("El documento no cumple con el esquema definido.");
            } else {
                LOGGER.log(Level.SEVERE, "Error al insertar la venta en la base de datos: {0}", e.getMessage());
                throw CustomException.dbError("Error al insertar la venta en la base de datos.");
            }
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Busca todas las ventas (con paginación)
     *
     * @param page número de página (empezando desde 0)
     * @param pageSize tamaño de página
     * @return Lista de ventas
     */
    public List<SaleModel> findAllSales(int page, int pageSize) {
        try {
            List<SaleModel> sales = new ArrayList<>();
            FindIterable<Document> documents = getSaleCollection()
                    .find()
                    .skip(page * pageSize)
                    .limit(pageSize);

            for (Document doc : documents) sales.add(documentToSale(doc));
            return sales;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener ventas - Page" + page + ", Size" + pageSize + " {0}", e);
            throw CustomException.dbError("Error al obtener ventas.");
        }
    }

    /**
     * Busca una venta por su ID
     *
     * @param id identificador de la venta
     * @return Optional con la venta encontrada o vacío si no existe
     */
    public Optional<SaleModel> findSaleById(String id) {
        try {
            if (id == null || !ObjectId.isValid(id)) {
                LOGGER.log(Level.WARNING, "ID inválido recibido: {0}", id);
                return Optional.empty();
            }
            ObjectId objectId = new ObjectId(id);
            Document doc = getSaleCollection().find(Filters.eq("_id", objectId)).first();
            return Optional.ofNullable(documentToSale(doc));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener venta ID: " + id + " {0}", e);
            throw CustomException.dbError("Error al obtener venta.");
        }
    }

    /**
     * Busca ventas por categoría
     *
     * @param status estatus de ventas
     * @return Lista de ventas de la categoría especificada
     */
    public List<SaleModel> findSalesByStatus(String status) {
        try {
            List<SaleModel> sales = new ArrayList<>();
            FindIterable<Document> documents = getSaleCollection().find(Filters.eq("status", status));

            for (Document doc : documents) sales.add(documentToSale(doc));
            return sales;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener ventas del estatus: " + status + " {0}", e);
            throw CustomException.dbError("Error al obtener ventas del estatus: " + status);
        }
    }

    /**
     * Busca ventas por texto (búsqueda parcial)
     *
     * @param searchText parte del texto a buscar
     * @return Lista de ventas que contienen el texto
     */
    public List<SaleModel> findSalesByText(String searchText) {
        try {
            List<SaleModel> sales = new ArrayList<>();
            String searchFilter = ".*" + Pattern.quote(searchText) + ".*";
            Bson filter = Filters.or(
                    Filters.regex("paymentMethod", searchFilter, "i"),
                    Filters.regex("customerInfo.name", searchFilter, "i"),
                    Filters.regex("status", searchFilter, "i"));
            FindIterable<Document> documents = getSaleCollection().find(filter);

            for (Document doc : documents) sales.add(documentToSale(doc));
            return sales;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener ventas con el texto: " + searchText + " {0}", e);
            throw CustomException.dbError("Error al obtener ventas con el texto: " + searchText);
        }
    }

    /**
     * Actualiza una venta existente
     *
     * @param sale modelo con los datos actualizados
     * @return ID del venta actualizado o null si falla
     */
    public String updateSale(SaleModel sale) {
        try {
            Document updateDoc = saleToDocument(sale);
            UpdateResult result = getSaleCollection().updateOne(
                    Filters.eq("_id", sale.getId()),
                    new Document("$set", updateDoc)
            );
            return result.getModifiedCount() > 0 ? sale.getId().toString() : null;
        } catch (MongoWriteException e) {
            WriteError writeError = e.getError();
            if (writeError != null && writeError.getCode() == 121) {
                LOGGER.log(Level.WARNING, "El documento no cumple con el esquema definido: {0}", e.getMessage());
                throw CustomException.dbValidationFailed("El documento no cumple con el esquema definido.");
            } else {
                LOGGER.log(Level.SEVERE, "Error al actualizar el venta: {0}", e.getMessage());
                throw CustomException.dbError("Error al actualizar el venta.");
            }
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Elimina una venta por su ID
     *
     * @param id identificador de la venta a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteSale(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            DeleteResult result = getSaleCollection().deleteOne(Filters.eq("_id", objectId));
            return result.getDeletedCount() > 0;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error al eliminar venta: " + id + " {0}", e);
            throw CustomException.dbError("Error al eliminar venta.");
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Convierte un SaleModel a un Document de MongoDB
     *
     * @param sale modelo de la venta
     * @return Document para MongoDB
     */
    private Document saleToDocument(SaleModel sale) {
        Document saleDoc = new Document();
        Document customerDoc = new Document();
        Document productDoc = new Document();

        saleDoc
                .append("_id", sale.getId())
                .append("paymentMethod", sale.getPaymentMethod())
                .append("totalAmount", sale.getTotalAmount())
                .append("status", sale.getStatus())
                .append("saleDate", ToDate.toDate(sale.getSaleDate()))
                .append("createdAt", ToDate.toDate(sale.getCreatedAt()))
                .append("updatedAt", ToDate.toDate(sale.getUpdatedAt()));

        customerDoc
                .append("id", sale.getCustomerInfo().getId())
                .append("name", sale.getCustomerInfo().getName())
                .append("email", sale.getCustomerInfo().getEmail());
        saleDoc.append("customerInfo", customerDoc);

        productDoc
                .append("id", sale.getProductSold().getId())
                .append("name", sale.getProductSold().getName())
                .append("price", sale.getProductSold().getPrice())
                .append("quantity", sale.getProductSold().getQuantity())
                .append("subTotal", sale.getProductSold().getSubTotal());
        saleDoc.append("productSold", productDoc);

        return saleDoc;
    }

    /**
     * Convierte un Document de MongoDB a un SaleModel
     *
     * @param doc Document de MongoDB
     * @return SaleModel
     */
    private SaleModel documentToSale(Document doc) {
        if (doc == null) return null;

        SaleModel sale = new SaleModel();

        sale.setId(doc.getObjectId("_id"));
        sale.setPaymentMethod(doc.getString("paymentMethod"));
        sale.setTotalAmount(doc.getDouble("totalAmount"));
        sale.setStatus(doc.getString("status"));
        sale.setSaleDate(ToDate.toLocalDateTime(doc.getDate("saleDate")));
        sale.setCreatedAt(ToDate.toLocalDateTime(doc.getDate("createdAt")));
        sale.setUpdatedAt(ToDate.toLocalDateTime(doc.getDate("updatedAt")));

        if (doc.containsKey("customerInfo")) {
            Document cust = doc.get("customerInfo", Document.class);
            sale.setCustomerInfo(new SaleModel.CustomerInfo(
                    cust.getString("id"),
                    cust.getString("name"),
                    cust.getString("email")
            ));
        }
        if (doc.containsKey("productSold")) {
            Document prod = doc.get("productSold", Document.class);
            sale.setProductSold(new SaleModel.ProductSold(
                    prod.getString("id"),
                    prod.getString("name"),
                    prod.getDouble("price"),
                    prod.getInteger("quantity"),
                    prod.getDouble("subTotal")
            ));
        }
        return sale;
    }

    /**
     * Cuenta el total de ventas
     *
     * @return número total de ventas
     */
    public long countSales() {
        try {
            return getSaleCollection().countDocuments();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", e.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }
}
