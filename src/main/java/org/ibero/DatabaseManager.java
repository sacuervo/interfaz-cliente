package org.ibero;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseManager {

    public static void main(String[] args) throws SQLException {

        // Crear servicios de guardería
        ArrayList<HashMap<String, Object>> serviceList = createServiceList();

        try {
            // Inicializar conexión
            Connection conn = getConnection();

            // Revisar validez de conexión
            System.out.println(conn.isValid(0) ? "Conexión establecida de forma exitosa." : "No se pudo establecer la conexión.");

            // Inicializar tabla de servicios en base de datos
            initServiceMenu(conn, serviceList);

            // Inicializar tabla de clientes en base de datos
            initServiceRequests(conn);

            // Cerrar la conexión
            conn.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public static Connection getConnection() throws SQLException { // Crear conexión
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "\\src\\main\\java\\data\\database.db"); // Conexión junto con ruta a la base de datos
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return conn;
    }

    public static ArrayList<HashMap<String, Object>> createServiceList() {
        ArrayList<HashMap<String, Object>> services = new ArrayList<>();

        services.add(new HashMap<String, Object>() {{
            put("nombre", "Baño y cepillado");
            put("precio", 25);
        }});

        services.add(new HashMap<String, Object>() {{
            put("nombre", "Corte de uñas");
            put("precio", 10);
        }});

        services.add(new HashMap<String, Object>() {{
            put("nombre", "Día de cuidado");
            put("precio", 80);
        }});


        return services;
    }

    public static void initServiceMenu(Connection conn, ArrayList<HashMap<String, Object>> serviceList ) throws SQLException{
            PreparedStatement dropTable = conn.prepareStatement("DROP TABLE IF EXISTS SERVICIOS");
            dropTable.executeUpdate();

            PreparedStatement createTable = conn.prepareStatement("CREATE TABLE SERVICIOS (NOMBRE TEXT, PRECIO INTEGER)");
            createTable.executeUpdate();

            PreparedStatement populateTable = conn.prepareStatement("INSERT INTO SERVICIOS VALUES (?, ?)");
            serviceList.forEach(service -> {
                try {
                    populateTable.setString(1, (String) service.get("nombre"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    populateTable.setInt(2, (Integer) service.get("precio"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    populateTable.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public static void initServiceRequests (Connection conn) throws SQLException {
            PreparedStatement createTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS PEDIDOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE_CLIENTE TEXT, SERVICIO TEXT, PRECIO INT, FINALIZADO INT DEFAULT 0)");
            createTable.executeUpdate();
    }

}