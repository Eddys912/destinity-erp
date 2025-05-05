package com.destinity.erp.utils;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria encargada de cargar y acceder a las propiedades definidas en el archivo env.properties.
 * Permite centralizar la lectura de configuraciones del sistema a través de claves definidas externamente.
 * Ideal para gestionar variables como credenciales, URIs de conexión, parámetros de entorno, etc.
 */
@ApplicationScoped
public class EnvReader {

    private static final Logger LOGGER = Logger.getLogger(EnvReader.class.getName());
    private final Properties properties = new Properties();

    public EnvReader() {
        loadProperties();
    }

    /**
     * Carga las propiedades desde el archivo env.properties ubicado en el classpath.
     * Si no se encuentra o ocurre un error, se registra en el log del sistema.
     */
    public void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("/env.properties")) {
            if (input != null) {
                properties.load(input);
            }
            LOGGER.log(Level.SEVERE, "No se pudo encontrar el archivo env.properties");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar el archivo env.properties", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
