package org.example.cnproject;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CnProjectApplication {

    public static void main(String[] args) {

        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            // Set system properties only if they aren't already set
            setIfAbsent("DATABASE_URL", dotenv.get("DATABASE_URL"));
            setIfAbsent("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
            setIfAbsent("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
            setIfAbsent("FTP_USERNAME", dotenv.get("FTP_USERNAME"));
            setIfAbsent("FTP_PASSWORD", dotenv.get("FTP_PASSWORD"));
            setIfAbsent("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
            setIfAbsent("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
            setIfAbsent("GITHUB_CLIENT_ID", dotenv.get("GITHUB_CLIENT_ID"));
            setIfAbsent("GITHUB_CLIENT_SECRET", dotenv.get("GITHUB_CLIENT_SECRET"));

        } catch (Exception ignored) {
            // Will ignore if .env is not found and rely on system environment variables
        }
        SpringApplication.run(CnProjectApplication.class, args);
    }
    private static void setIfAbsent(String key, String value) {
        if (System.getenv(key) == null && value != null) {
            System.setProperty(key, value);
        }
    }

}
