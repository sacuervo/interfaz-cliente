package org.ibero;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.net.*;


public class Server {

    public static void main(String[] args) throws SQLException {
        ServerSocket serverSocket = null;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/main/java/data/database.db")) { // Try con recursos para cerrar automáticamente la conexión

            // Crear lista de servicios de guardería
            ArrayList<HashMap<String, Object>> serviceList = createServiceList();

            // Pasar lista de servicios a base de datos
            initServiceMenu(conn, serviceList);

            // Inicializar tabla para crear pedidos de clientes
            initServiceRequests(conn);

            // Iniciar el servidor en el puerto 9999
            serverSocket = new ServerSocket(9999);

            System.out.println("Servidor iniciado y esperando conexiones...");

            while (true) {
                try {
                    // Esperar una conexión del cliente
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado.");

                    // Crear un nuevo hilo para manejar la conexión del cliente
                    new Thread(() -> {
                        try {
                            handleClient(clientSocket, conn);
                        } catch (IOException e) {
                            System.out.println("Error al manejar el cliente: " + e.getMessage());
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                System.out.println("Error cerrando el socket del cliente: " + e.getMessage());
                            }
                            System.out.println("Cliente desconectado. Esperando nuevas conexiones...");
                        }
                    }).start(); // Inicia el hilo
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        } finally { // Finally para cerrar el socket del servidor
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ------ MÉTODOS AUXILIARES  (MANEJO BASE DATOS) ------
    // Creación de servicios preestablecidos de forma local. Esto se pueden modificar de manera sencilla y se pasan a la base de datos cada vez que se inicie la aplicación.
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

    // Inicialización de tabla SERVICIOS en base de datos. Recibe los servicios preestablecidos y los pone en la base de datos. (CRUD -> C)
    public static void initServiceMenu(Connection conn, ArrayList<HashMap<String, Object>> serviceList) {
        try {
            PreparedStatement dropTable = conn.prepareStatement("DROP TABLE IF EXISTS SERVICIOS");
            dropTable.executeUpdate();

            PreparedStatement createTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS SERVICIOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE TEXT, PRECIO INTEGER)");
            createTable.executeUpdate();

            PreparedStatement populateTable = conn.prepareStatement("INSERT INTO SERVICIOS (NOMBRE, PRECIO) VALUES (?, ?)");
            serviceList.forEach(service -> {
                try {
                    populateTable.setString(1, (String) service.get("nombre"));
                    populateTable.setInt(2, (Integer) service.get("precio"));
                    populateTable.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Inicialización de tabla que incluye pedidos de servicios de clientes en base de datos
    public static void initServiceRequests(Connection conn) {

        try {
            PreparedStatement createTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS PEDIDOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE_CLIENTE TEXT, SERVICIO TEXT, PRECIO INT, FINALIZADO INT DEFAULT 0)");
            createTable.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    // Regresa string con todos los servicios de la veterinaria (CRUD -> R)
    public static String inquireAllServicesInformation(Connection conn) {
        String result = "";

        try {

            PreparedStatement retrieveAllEntries = conn.prepareStatement("SELECT * FROM SERVICIOS");

            ResultSet rs = retrieveAllEntries.executeQuery();

            int contador = 1;

            while (rs.next()) {

                String name = rs.getString("NOMBRE");
                String price = rs.getString("PRECIO");

                result += "\n--- Servicio # " + contador + " ---\nServicio: " + name + "\nPrecio: $" + price + "\n--------------------\n";

                contador++;

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;

    }

    // Regresa HashMap<String, Object> con la información de un servicio seleccionado
    public static HashMap<String, Object> grabServiceInformation(Connection conn, int id) {

        HashMap<String, Object> result = new HashMap<>();

        if (verifyServiceExistence(conn, id)) {
            try {
                PreparedStatement grabServiceStatement = conn.prepareStatement("SELECT * FROM SERVICIOS WHERE ID = ?");

                grabServiceStatement.setInt(1, id);

                ResultSet rs = grabServiceStatement.executeQuery();

                while (rs.next()) {
                    result.put("service", rs.getString("NOMBRE"));
                    result.put("price", rs.getInt("PRECIO"));
                }


            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } else {
            result.put("response", "No se ha encontrado el servicio. Por favor vuelva a intentar");
        }

        return result;
    }

    // Procesa el HashMap con la información del pedido del cliente y la pasa a la base de datos. Regresa el id del pedido para poder buscarlo después. (CRUD -> C)
    public static String processServiceRequest(Connection conn, HashMap<String, Object> serviceRequest) {
        int id = 0;
        try {
            PreparedStatement addServiceEntry = conn.prepareStatement("INSERT INTO PEDIDOS (NOMBRE_CLIENTE, SERVICIO, PRECIO, FINALIZADO) VALUES (?, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS);

            addServiceEntry.setString(1, (String) serviceRequest.get("name"));
            addServiceEntry.setString(2, (String) serviceRequest.get("service"));
            addServiceEntry.setInt(3, (Integer) serviceRequest.get("price"));

            addServiceEntry.executeUpdate();

            ResultSet generatedKeys = addServiceEntry.getGeneratedKeys();

            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "\nSe ha creado el pedido con el ID # " + id + ".";
    }

    // Procesa el servicio con el id propocionado en la base de datos y cambia el valor de FINALIZADO por 1 (proporcional a TRUE en SQLite) (CRUD -> U)
    public static String processRequestFinalization(Connection conn, int serviceId) {

        String result = "";

        if (verifyRequestExistence(conn, serviceId)) { // Verificar existencia del servicio antes
            try {

                PreparedStatement finalizeRequest = conn.prepareStatement("UPDATE PEDIDOS SET FINALIZADO = ? WHERE ID = ?");

                finalizeRequest.setInt(1, 1);
                finalizeRequest.setInt(2, serviceId);

                finalizeRequest.executeUpdate();

                result = "\nEstado de servicio actualizado exitosamente.";

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            result = "\nNo se ha encontrado el servicio. Por favor vuelva a intentar.";
        }

        return result;

    }

    // Regresa string toda la información de un pedido (CRUD -> R)
    public static String grabRequestInformation(Connection conn, int serviceId) {

        String result = "";

        if (verifyRequestExistence(conn, serviceId)) {
            try {
                PreparedStatement requestStateInquiry = conn.prepareStatement("SELECT * FROM PEDIDOS WHERE ID = ?");

                requestStateInquiry.setInt(1, serviceId);

                ResultSet rs = requestStateInquiry.executeQuery();

                while (rs.next()) {

                    int id = serviceId;
                    String name = rs.getString("NOMBRE_CLIENTE");
                    String service = rs.getString("SERVICIO");
                    String price = rs.getString("PRECIO");
                    String isComplete = (rs.getInt("FINALIZADO")) == 1 ? "Completo" : "En proceso";

                    result = "\n--- Servicio # " + id + " ---\nNombre: " + name + "\nServicio: " + service + "\nPrecio: $" + price + "\nEstado: " + isComplete + "\n--------------------";

                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("\nNo se ha encontrado el servicio. Por favor vuelva a intentar.");
        }

        return result;

    }

    // Regresa ArrayList<String> con la información de todos los pedidos (CRUD -> R)
    public static String inquireAllRequestsInformation(Connection conn) {

        String result = "";

        try {
            PreparedStatement retrieveAllEntries = conn.prepareStatement("SELECT * FROM PEDIDOS");

            ResultSet rs = retrieveAllEntries.executeQuery();

            while (rs.next()) {

                int id = rs.getInt("ID");
                String name = rs.getString("NOMBRE_CLIENTE");
                String service = rs.getString("SERVICIO");
                String price = rs.getString("PRECIO");
                String isComplete = (rs.getInt("FINALIZADO")) == 1 ? "Completo" : "En proceso";

                result += "\n--- Servicio # " + id + " ---\nNombre: " + name + "\nServicio: " + service + "\nPrecio: $" + price + "\nEstado: " + isComplete + "\n--------------------\n";


            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;

    }

    // Elimina el pedido que corresponda al id especificado (CRUD -> D)
    public static String deleteRequestEntry(Connection conn, int id) {

        String result = "";

        if (verifyRequestExistence(conn, id)) {
            try {

                PreparedStatement deletionQuery = conn.prepareStatement("DELETE FROM PEDIDOS WHERE ID = ?");

                deletionQuery.setInt(1, id);

                deletionQuery.executeUpdate();

                result = "\nEliminación de pedido '" + id + "' exitosa.";

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("\nNo se ha encontrado el servicio. Por favor vuelva a intentar.");
        }

        return result;
    }

    // Verificar existencia de pedido (CRUD -> R)
    public static boolean verifyRequestExistence(Connection conn, int id) {

        boolean result = false;

        try {

            PreparedStatement searchQuery = conn.prepareStatement("SELECT * FROM PEDIDOS WHERE ID = ?");

            searchQuery.setInt(1, id);

            ResultSet rs = searchQuery.executeQuery();

            if (rs.next()) {
                result = true;
            } else {
                result = false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    // Verificar existencia de servicio (CRUD -> R)
    public static boolean verifyServiceExistence(Connection conn, int id) {

        boolean result = false;

        try {

            PreparedStatement searchQuery = conn.prepareStatement("SELECT * FROM SERVICIOS WHERE ID = ?");

            searchQuery.setInt(1, id);

            ResultSet rs = searchQuery.executeQuery();

            if (rs.next()) {
                result = true;
            } else {
                result = false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    // ------ FIN MÉTODOS AUXILIARES (MANEJO BASE DATOS) ------

    // ------ MÉTODOS AUXILIARES (FUNCIONAMIENTO SERVIDOR) ------
    // Método para manejar la conexión del cliente
    private static void handleClient(Socket clientSocket, Connection conn) throws IOException {
        // Flujo de entrada para recibir mensajes del cliente
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Flujo de salida para enviar mensajes al cliente
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputLine;
        // Leer mensajes del cliente hasta que se envíe exit
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Mensaje del cliente: " + inputLine);

            // Procesar la entrada del cliente
            String response = processInput(inputLine, conn);

            // Enviar la respuesta al cliente
            out.println(response);

            if (inputLine.equalsIgnoreCase("exit")) {
                break;
            }
        }
    }

    // Función para procesar entrada de cliente y devolver salida
    private static String processInput(String input, Connection conn) {
        // Lógica para procesar el input
        String[] tokens = input.split(" "); // Se divide el contenido de la entrada para que la primera palabra del comando y el resto sean argumentos.
        String command = tokens[0].toLowerCase();

        switch (command) {
            case "showservices":
                return inquireAllServicesInformation(conn) + "\nEND";
            case "storeservice":
                // Verificar existencia del número de servicio que ingresa el usuario
                if (verifyServiceExistence(conn, Integer.parseInt(tokens[1]))) {
                    HashMap<String, Object> requestHash = grabServiceInformation(conn, Integer.parseInt(tokens[1]));
                    requestHash.put("name", tokens[2]);
                    return processServiceRequest(conn, requestHash) + "\nEND";
                } else {
                    return "El número de servicio ingresado no existe. Por favor vuelva a intentar.\nEND";
                }
            case "getrequeststate":
                // Verificar existencia del número de servicio que ingresa el usuario
                if (verifyRequestExistence(conn, Integer.parseInt(tokens[1]))) {
                    return grabRequestInformation(conn, Integer.parseInt(tokens[1])) + "\nEND";
                } else {
                    return "El número de pedido ingresado no existe. Por favor vuelva a intentar.\nEND";
                }
            case "endrequest":
                if (verifyRequestExistence(conn, Integer.parseInt(tokens[1]))) {
                    return processRequestFinalization(conn, Integer.parseInt(tokens[1])) + "\nEND";
                } else {
                    return "El número de pedido ingresado no existe. Por favor vuelva a intentar.\nEND";
                }
            case "deleterequest":
                if (verifyRequestExistence(conn, Integer.parseInt(tokens[1]))) {
                    return (deleteRequestEntry(conn, Integer.parseInt(tokens[1]))) + "\nEND";
                } else {
                    return "El número de pedido ingresado no existe. Por favor vuelva a intentar.\nEND";
                }
            case "exit":
                return "Desconectando...";
            default:
                return "Comando no reconocido.";
        }
    }

    // ------ FIN MÉTODOS AUXILIARES (FUNCIONAMIENTO SERVIDOR) ------
}