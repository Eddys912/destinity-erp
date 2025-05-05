package com.destinity.erp.database;

import com.destinity.erp.utils.EnvReader;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class DataBaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DataBaseConnection.class.getName());

    @Inject
    private EnvReader envReader;

    private MongoClient mongoClient;
    private MongoDatabase database;

    @PostConstruct
    public void init() {
        try {
            String mongoUri = envReader.getProperty("MONGO_URI");
            String dbName = envReader.getProperty("MONGO_DATABASE");

            if (mongoUri == null || dbName == null) {
                LOGGER.log(Level.SEVERE, "Variables de conexión de Mongo no configuradas");
                return;
            }
            LOGGER.log(Level.INFO, "Conectando a MongoDB Atlas...");
            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase(dbName);
            LOGGER.log(Level.INFO, "Conexión exitosa a la base de datos: {0}", dbName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al conectar con MongoDB", e);
        }
    }

    @PreDestroy
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            LOGGER.log(Level.INFO, "Conexión a MongoDB cerrada");
        }
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
