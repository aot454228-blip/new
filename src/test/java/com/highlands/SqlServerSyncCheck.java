package com.highlands;

import org.hibernate.cfg.Configuration;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Creates a NEW isolated SQL Server database; never changes HighlandsDB. Keeps it for inspection. */
public class SqlServerSyncCheck {
    public static void main(String[] args) throws Exception {
        if (!"yes".equals(System.getenv("HIGHLANDS_TEST_DB")))
            throw new IllegalArgumentException("Set HIGHLANDS_TEST_DB=yes to create an isolated test database");
        Properties config = new Properties();
        Path local = Path.of("application-local.properties");
        if (Files.exists(local)) try (var reader = Files.newBufferedReader(local, StandardCharsets.UTF_8)) {
            config.load(reader);
        }
        String url = value(config, "spring.datasource.url", "SPRING_DATASOURCE_URL",
                "jdbc:sqlserver://localhost:1433;databaseName=HighlandsDB;encrypt=true;trustServerCertificate=true;loginTimeout=5");
        String user = value(config, "spring.datasource.username", "SPRING_DATASOURCE_USERNAME", "sa");
        String pass = value(config, "spring.datasource.password", "SPRING_DATASOURCE_PASSWORD", "");
        if (args.length == 1 && "--validate-existing".equals(args[0])) {
            System.setProperty("spring.config.import", "");
            System.setProperty("spring.datasource.url", url);
            System.setProperty("spring.datasource.username", user);
            System.setProperty("spring.datasource.password", pass);
            try (var ctx = new org.springframework.boot.builder.SpringApplicationBuilder(HighlandsApplication.class)
                    .web(org.springframework.boot.WebApplicationType.NONE)
                    .run("--spring.jpa.hibernate.ddl-auto=validate", "--debug=false", "--logging.level.root=WARN",
                            "--logging.level.org.springframework=WARN", "--logging.level.org.hibernate.SQL=WARN")) {
                System.out.println("PASS: existing HighlandsDB schema validated without writing business data");
            }
            return;
        }
        String db = "HighlandsSyncCheck_" + System.currentTimeMillis();
        String master = database(url, "master");
        try (var connection = DriverManager.getConnection(master, user, pass);
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE [" + db + "]");
        }
        String isolated = database(url, db);
        System.out.println("Isolated SQL Server database: " + db);
        Configuration schema = new Configuration();
        schema.setProperty("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        schema.setProperty("hibernate.connection.url", isolated);
        schema.setProperty("hibernate.connection.username", user);
        schema.setProperty("hibernate.connection.password", pass);
        schema.setProperty("hibernate.hbm2ddl.auto", "create-only");
        try (var files = Files.list(Path.of("src/main/java/com/highlands/entity"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList())
                schema.addAnnotatedClass(Class.forName("com.highlands.entity."
                        + file.getFileName().toString().replace(".java", "")));
        }
        try (var factory = schema.buildSessionFactory()) { }
        String migration = Files.readString(Path.of("sql/migration_dong_bo_nghiep_vu.sql"), StandardCharsets.UTF_8)
                .replace("USE HighlandsDB;", "USE [" + db + "];");
        try (var connection = DriverManager.getConnection(isolated, user, pass);
             var statement = connection.createStatement()) {
            for (int i = 0; i < 2; i++)
                for (String batch : migration.split("(?im)^\s*GO\s*$"))
                    if (!batch.isBlank()) statement.execute(batch);
        }
        System.setProperty("spring.config.import", "");
        System.setProperty("spring.datasource.url", isolated);
        System.setProperty("spring.datasource.username", user);
        System.setProperty("spring.datasource.password", pass);
        DongBoNghiepVuIntegrationCheck.main(new String[0]);
        System.out.println("PASS: SQL Server isolated schema, repeatable migration and complete service workflow");
    }
    private static String database(String url, String name) {
        if (url.matches("(?i).*databaseName=[^;]*.*"))
            return url.replaceAll("(?i)databaseName=[^;]*", "databaseName=" + name);
        return url + ";databaseName=" + name;
    }
    private static String value(Properties p, String key, String env, String fallback) {
        String value = System.getenv(env);
        return value == null ? p.getProperty(key, fallback) : value;
    }
}
