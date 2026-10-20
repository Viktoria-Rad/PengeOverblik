package app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static String getPropertyValue(String key, String fileName) {

        Properties properties = new Properties();

        try (InputStream input = Utils.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (input == null) {
                throw new RuntimeException("Kan ikke finde " + fileName);
            }

            properties.load(input);

            return properties.getProperty(key);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}