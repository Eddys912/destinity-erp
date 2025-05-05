package com.destinity.erp.inventory;

import com.destinity.erp.database.DataBaseConnection;
import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.ToDate;
import com.mongodb.ErrorCategory;
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
 * Administra la conversión entre el modelo de producto y los
 * documentos almacenados en la base de datos.
 */
@ApplicationScoped
public class ProductRepository {

    private static final Logger LOGGER = Logger.getLogger(ProductRepository.class.getName());
    private static final String COLLECTION_NAME = "inventory";

    @Inject
    private DataBaseConnection dbConnection;

    /**
     * Obtiene la colección de productos
     *
     * @return MongoCollection de productos
     */
    private MongoCollection<Document> getProductCollection() {
        return dbConnection.getDatabase().getCollection(COLLECTION_NAME);
    }

    /**
     * Guarda un nuevo producto en la base de datos
     *
     * @param product modelo del producto a guardar
     * @return ID del producto guardado o null si falla
     */
    public String saveProduct(ProductModel product) {
        try {
            Document productDoc = productToDocument(product);
            InsertOneResult result = getProductCollection().insertOne(productDoc);
            return result.getInsertedId().asObjectId().getValue().toString();
        } catch (MongoWriteException e) {
            WriteError writeError = e.getError();
            if (writeError != null && writeError.getCode() == 121) {
                LOGGER.log(Level.WARNING, "El documento no cumple con el esquema definido: {0}", e.getMessage());
                throw CustomException.dbValidationFailed("El documento no cumple con el esquema definido.");
            } else if (writeError != null && writeError.getCategory() == ErrorCategory.DUPLICATE_KEY) {
                LOGGER.log(Level.WARNING, "Ya existe un producto con ese nombre: {0}", e.getMessage());
                throw CustomException.dbDuplicatedKey("Ya existe un producto con ese nombre.");
            } else {
                LOGGER.log(Level.SEVERE, "Error al insertar el producto en la base de datos: {0}", e.getMessage());
                throw CustomException.dbError("Error al insertar el producto en la base de datos.");
            }
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Busca todos los productos (con paginación)
     *
     * @param page número de página (empezando desde 0)
     * @param pageSize tamaño de página
     * @return Lista de productos
     */
    public List<ProductModel> findAllProducts(int page, int pageSize) {
        try {
            List<ProductModel> products = new ArrayList<>();
            FindIterable<Document> documents = getProductCollection()
                    .find()
                    .skip(page * pageSize)
                    .limit(pageSize);

            for (Document doc : documents) products.add(documentToProduct(doc));
            return products;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener productos - Page" + page + ", Size" + pageSize + " {0}", e);
            throw CustomException.dbError("Error al obtener productos.");
        }
    }

    /**
     * Busca un producto por su ID
     *
     * @param id identificador del producto
     * @return Optional con el producto encontrado o vacío si no existe
     */
    public Optional<ProductModel> findProductById(String id) {
        try {
            if (id == null || !ObjectId.isValid(id)) {
                LOGGER.log(Level.WARNING, "ID inválido recibido: {0}", id);
                return Optional.empty();
            }
            ObjectId objectId = new ObjectId(id);
            Document doc = getProductCollection().find(Filters.eq("_id", objectId)).first();
            return Optional.ofNullable(documentToProduct(doc));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener producto ID: " + id + " {0}", e);
            throw CustomException.dbError("Error al obtener producto.");
        }
    }

    /**
     * Busca productos por categoría
     *
     * @param category categoría de productos
     * @return Lista de productos de la categoría especificada
     */
    public List<ProductModel> findProductsByCategory(String category) {
        try {
            List<ProductModel> products = new ArrayList<>();
            FindIterable<Document> documents = getProductCollection().find(Filters.eq("category", category));

            for (Document doc : documents) products.add(documentToProduct(doc));
            return products;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener productos de la categoria: " + category + " {0}", e);
            throw CustomException.dbError("Error al obtener productos de la categoria: " + category);
        }
    }

    /**
     * Busca productos por texto (búsqueda parcial)
     *
     * @param searchText parte del texto a buscar
     * @return Lista de ventas que contienen el texto
     */
    public List<ProductModel> findProductsByText(String searchText) {
        try {
            List<ProductModel> products = new ArrayList<>();
            String searchFilter = ".*" + Pattern.quote(searchText) + ".*";
            Bson filter = Filters.or(
                    Filters.regex("name", searchFilter, "i"),
                    Filters.regex("category", searchFilter, "i"),
                    Filters.regex("description", searchFilter, "i"),
                    Filters.regex("provider", searchFilter, "i"));
            FindIterable<Document> documents = getProductCollection().find(filter);

            for (Document doc : documents) products.add(documentToProduct(doc));
            return products;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener productos con el texto: " + searchText + " {0}", e);
            throw CustomException.dbError("Error al obtener productos con el texto: " + searchText);
        }
    }

    /**
     * Actualiza un producto existente
     *
     * @param product modelo con los datos actualizados
     * @return ID del producto actualizado o null si falla
     */
    public String updateProduct(ProductModel product) {
        try {
            Document updateDoc = productToDocument(product);
            UpdateResult result = getProductCollection().updateOne(
                    Filters.eq("_id", product.getId()),
                    new Document("$set", updateDoc)
            );
            return result.getModifiedCount() > 0 ? product.getId().toString() : null;
        } catch (MongoWriteException e) {
            WriteError writeError = e.getError();
            if (writeError != null && writeError.getCode() == 121) {
                LOGGER.log(Level.WARNING, "El documento no cumple con el esquema definido: {0}", e.getMessage());
                throw CustomException.dbValidationFailed("El documento no cumple con el esquema definido.");
            } else if (writeError != null && writeError.getCategory() == ErrorCategory.DUPLICATE_KEY) {
                LOGGER.log(Level.WARNING, "Ya existe un producto con ese nombre: {0}", e.getMessage());
                throw CustomException.dbDuplicatedKey("Ya existe un producto con ese nombre.");
            } else {
                LOGGER.log(Level.SEVERE, "Error al actualizar el producto: {0}", e.getMessage());
                throw CustomException.dbError("Error al actualizar el producto.");
            }
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Elimina un producto por su ID
     *
     * @param id identificador del producto a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteProduct(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            DeleteResult result = getProductCollection().deleteOne(Filters.eq("_id", objectId));
            return result.getDeletedCount() > 0;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error al eliminar producto: " + id + " {0}", e);
            throw CustomException.dbError("Error al eliminar producto.");
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Convierte un ProductModel a un Document de MongoDB
     *
     * @param product modelo del producto
     * @return Document para MongoDB
     */
    private Document productToDocument(ProductModel product) {
        Document productDoc = new Document();

        return productDoc
                .append("_id", product.getId())
                .append("name", product.getName())
                .append("price", product.getPrice())
                .append("stock", product.getStock())
                .append("category", product.getCategory())
                .append("description", product.getDescription())
                .append("image", product.getImage())
                .append("provider", product.getProvider())
                .append("status", product.getStatus())
                .append("createdAt", ToDate.toDate(product.getCreatedAt()))
                .append("updatedAt", ToDate.toDate(product.getUpdatedAt()));
    }

    /**
     * Convierte un Document de MongoDB a un ProductModel
     *
     * @param doc Document de MongoDB
     * @return ProductModel
     */
    private ProductModel documentToProduct(Document doc) {
        if (doc == null) return null;

        ProductModel product = new ProductModel();

        product.setId(doc.getObjectId("_id"));
        product.setName(doc.getString("name"));
        product.setPrice(doc.getDouble("price"));
        product.setStock(doc.getInteger("stock"));
        product.setCategory(doc.getString("category"));
        product.setDescription(doc.getString("description"));
        product.setImage(doc.getString("image"));
        product.setProvider(doc.getString("provider"));
        product.setStatus(doc.getString("status"));
        product.setCreatedAt(ToDate.toLocalDateTime(doc.getDate("createdAt")));
        product.setUpdatedAt(ToDate.toLocalDateTime(doc.getDate("updatedAt")));

        return product;
    }

    /**
     * Cuenta el total de productos
     *
     * @return número total de productos
     */
    public long countProducts() {
        try {
            return getProductCollection().countDocuments();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", e.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }
}
