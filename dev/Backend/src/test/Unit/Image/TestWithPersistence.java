package Unit.Image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

public class TestWithPersistence {
    @Autowired
    protected EntityManager entityManager;
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public void commit() {
        entityManager.flush();
        entityManager.clear();
    }

    public void printAllTables() {
        List<Map<String, Object>> tableNamesResult = getAllTableNames();

        for (Map<String, Object> tableNameRow : tableNamesResult) {
            String tableName = (String) tableNameRow.get("TABLE_NAME"); // Assuming "TABLE_NAME" is the key
            if (tableName != null) {
                System.out.println("\n--- Table: " + tableName + " ---");
                printTable(tableName);
            }
        }
    }

    public List<Map<String, Object>> getAllTableNames() {
        String sql = "SHOW TABLES;";
        return jdbcTemplate.queryForList(sql);
    }

    public void printTable(String tableName) {
        String sql = "SELECT * FROM "+tableName; // Use DESCRIBE or equivalent in your DBMS (works in MySQL, MariaDB, etc.)
        System.out.println("TABLE: "+tableName);
        jdbcTemplate.queryForList(sql).forEach(row -> {
            System.out.println("ROW:"+row);
        });
    }
} 