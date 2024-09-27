/* TODO:
    1. Base de datos:
        - Agregar menú de servicios -> createServiceList() local,  initServiceMenu() db
        - Crear tabla para almacenar pedidos de clientes
        - Implementar adición de pedido en función independiente
        - Implementar finalización de pedido en función independiente
    2. Cliente:
        - Crear menú principal
        - Mandar mensajes al servidor según las opciones que se seleccionen
        - Imprimir mensaje según respuesta que se reciba del servidor
    3. Servidor:
        - Atar cada mensaje del cliente a una operación con el servidor
        - Dar formato a respuestas del servidor por medio de funciones independientes para enviar respuestas al cliente
 */

package org.ibero;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static org.ibero.Server_backup.initServiceMenu;
import static org.ibero.Server_backup.initServiceRequests;

public class Server {

    private static final int PORT = 12345; // Puerto del servidor

    public static void main(String[] args) throws SQLException {

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/main/java/data/database.db")) { // Try con recursos para cerrar automáticamente la conexión

            // Crear lista de servicios de guardería
            ArrayList<HashMap<String, Object>> serviceList = createServiceList();

            // Pasar lista de servicios a base de datos
            initServiceMenu(conn, serviceList);

            // Inicializar tabla para crear pedidos de clientes
            initServiceRequests(conn);

            // PRUEBAS
            // Simulación de creación de pedido en base de datos
            // 1. Crear ArrayList<String> con nombre, servicio y precio
//            ArrayList<String> serviceDataList = new ArrayList<>();
//            serviceDataList.add("Santiago Cuervo");
//            serviceDataList.add("Corte");
//            serviceDataList.add("30");

            // 2. Pasar ese ArrayList como argumento al método receiveServiceRequest. Almacenar el resultado en un HashMap
//            HashMap<String, Object> serviceRequestHash = receiveServiceRequest(serviceDataList);

            // 3. Pasar ese HashMap como argumento al método processServiceRequest
//            processServiceRequest(conn, serviceRequestHash);
//            processServiceRequest(conn, serviceRequestHash);
//            processServiceRequest(conn, serviceRequestHash);

            // 4. Finalizar un proceso processRequestFinalization()
//            processRequestFinalization(conn, 5);

            // 5. Revisar el estado de un servicio inquireRequestInformation()
//            System.out.println(inquireRequestInformation(conn, 4));
//            System.out.println(inquireRequestInformation(conn, 5));
//            System.out.println(inquireRequestInformation(conn, 2));

            // 6. Ver el estado de todos los pedidos inquireAllRequestsInformation()
//            inquireAllRequestsInformation(conn).forEach(System.out::println);

            // 7. Ver cuáles servicios ofrece la guardería
//            inquireAllServicesInformation(conn).forEach(System.out::println);

            // 8. Eliminar servicio al proveer un id
//            deleteRequestEntry(conn, 6);

            // 9. Verificar existencia del pedido verifyRequestExistence()
            System.out.println(verifyRequestExistence(conn, 4));
            System.out.println(verifyRequestExistence(conn, 10));

//            // Iniciar servidor para escuchar conexiones de clientes
//            ServerSocket serverSocket = new ServerSocket(PORT);
//            System.out.println("Servidor iniciado en el puerto " + PORT);
//
//            // Esperar conexiones de clientes
//            while (true) {
//                // Esperar conexión
//                Socket clientSocket = serverSocket.accept();
//                // Notificar conexión
//                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
//
//                // Crear un nuevo hilo para manejar la conexión con cliente
//                new ClientHandler(clientSocket, conn).start();
//            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    // ------ MÉTODOS AUXILIARES ------
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

    // Inicialización de tabla SERVICIOS en base de datos. Recibe los servicios preestablecidos y los pone en la base de datos.
    public static void initServiceMenu(Connection conn, ArrayList<HashMap<String, Object>> serviceList) throws SQLException {
        PreparedStatement dropTable = conn.prepareStatement("DROP TABLE IF EXISTS SERVICIOS");
        dropTable.executeUpdate();

        PreparedStatement createTable = conn.prepareStatement("CREATE TABLE SERVICIOS (NOMBRE TEXT, PRECIO INTEGER)");
        createTable.executeUpdate();

        PreparedStatement populateTable = conn.prepareStatement("INSERT INTO SERVICIOS VALUES (?, ?)");
        serviceList.forEach(service -> {
            try {
                populateTable.setString(1, (String) service.get("nombre"));
                populateTable.setInt(2, (Integer) service.get("precio"));
                populateTable.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Inicialización de tabla que incluye pedidos de servicios de clientes en base de datos
    public static void initServiceRequests(Connection conn) throws SQLException {

        try {
            PreparedStatement createTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS PEDIDOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE_CLIENTE TEXT, SERVICIO TEXT, PRECIO INT, FINALIZADO INT DEFAULT 0)");
            createTable.executeUpdate();
        } catch (SQLException ex){
            ex.printStackTrace();
        }

    }

    // Regresa string con todos los servicios de la veterinaria
    public static ArrayList<String> inquireAllServicesInformation(Connection conn) {

        ArrayList<String> resultArray = new ArrayList<>();

        try {

            PreparedStatement retrieveAllEntries = conn.prepareStatement("SELECT * FROM SERVICIOS");

            ResultSet rs = retrieveAllEntries.executeQuery();

            int contador = 1;

            while (rs.next()) {

                String result = "";

                String name = rs.getString("NOMBRE");
                String cost = rs.getString("PRECIO");

                result = "\n--- Servicio # " + contador + " ---\nServicio: " + name + "\nPrecio: $" + cost + "\n--------------------";

                resultArray.add(result);

                contador++;

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return resultArray;

    }

    // Procesa la información que recibe por parte del cliente para crear un HashMap con la información del servicio
    public static HashMap<String, Object> receiveServiceRequest(ArrayList<String> serviceInfo) {

        HashMap<String, Object> serviceRequestHash = new HashMap<>();

        serviceRequestHash.put("nombre", serviceInfo.get(0));
        serviceRequestHash.put("servicio", serviceInfo.get(1));
        serviceRequestHash.put("precio", serviceInfo.get(2));

        return serviceRequestHash;
    }

    // Procesa el HashMap con la información del pedido del cliente y la pasa a la base de datos. Regresa el id del pedido para poder buscarlo después.
    public static int processServiceRequest(Connection conn, HashMap<String, Object> serviceRequest) {
        int id = 0;
        try {
            PreparedStatement addServiceEntry = conn.prepareStatement("INSERT INTO PEDIDOS (NOMBRE_CLIENTE, SERVICIO, PRECIO, FINALIZADO) VALUES (?, ?, ?, 0)");

            addServiceEntry.setString(1, (String) serviceRequest.get("nombre"));
            addServiceEntry.setString(2, (String) serviceRequest.get("servicio"));
            addServiceEntry.setInt(3, Integer.parseInt((String) serviceRequest.get("precio")));

            addServiceEntry.executeUpdate();

            PreparedStatement retrieveServiceId = conn.prepareStatement("SELECT ID FROM PEDIDOS WHERE NOMBRE_CLIENTE = ?");

            retrieveServiceId.setString(1, (String) serviceRequest.get("nombre"));

            ResultSet resultId = retrieveServiceId.executeQuery();

            if (resultId.next()) {
                id = resultId.getInt("ID");
            }

            resultId.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return id;
    }

    // Procesa el servicio con el id propocionado en la base de datos y cambia el valor de FINALIZADO por 1 (proporcional a TRUE en SQLite)
    public static void processRequestFinalization(Connection conn, int serviceId) {

        try {

            PreparedStatement finalizeRequest = conn.prepareStatement("UPDATE PEDIDOS SET FINALIZADO = ? WHERE ID = ?");

            finalizeRequest.setInt(1, 1);
            finalizeRequest.setInt(2, serviceId);

            finalizeRequest.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    // Regresa string toda la información de un pedido
    public static String inquireRequestInformation(Connection conn, int serviceId) {

        String result = "";

        try {
            PreparedStatement requestStateInquiry = conn.prepareStatement("SELECT * FROM PEDIDOS WHERE ID = ?");

            requestStateInquiry.setInt(1, serviceId);

            ResultSet rs = requestStateInquiry.executeQuery();

            while (rs.next()) {

                int id = serviceId;
                String name = rs.getString("NOMBRE_CLIENTE");
                String service = rs.getString("SERVICIO");
                String cost = rs.getString("PRECIO");
                String isComplete = (rs.getInt("FINALIZADO")) == 1 ? "Completo" : "En proceso";

                result = "\n--- Servicio # " + id + " ---\nNombre: " + name + "\nServicio: " + service + "\nPrecio: $" + cost + "\nEstado: " + isComplete + "\n--------------------\n";

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;

    }

    // Regresa ArrayList<String> con la información de todos los pedidos
    public static ArrayList<String> inquireAllRequestsInformation(Connection conn) {

        ArrayList<String> resultArray = new ArrayList<>();

        try {
            PreparedStatement retrieveAllEntries = conn.prepareStatement("SELECT * FROM PEDIDOS");

            ResultSet rs = retrieveAllEntries.executeQuery();

            while (rs.next()) {

                String result = "";

                int id = rs.getInt("ID");
                String name = rs.getString("NOMBRE_CLIENTE");
                String service = rs.getString("SERVICIO");
                String cost = rs.getString("PRECIO");
                String isComplete = (rs.getInt("FINALIZADO")) == 1 ? "Completo" : "En proceso";

                result = "\n--- Servicio # " + id + " ---\nNombre: " + name + "\nServicio: " + service + "\nPrecio: $" + cost + "\nEstado: " + isComplete + "\n--------------------";

                resultArray.add(result);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return resultArray;

    }

    // Elimina el pedido que corresponda al id especificado
    public static void deleteRequestEntry(Connection conn, int id) {

        try {

            PreparedStatement deletionQuery = conn.prepareStatement("DELETE FROM PEDIDOS WHERE ID = ?");

            deletionQuery.setInt(1, id);

            deletionQuery.executeUpdate();

            System.out.println("\nEliminación de pedido '" + id + "' exitosa.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    ;

    // Verificar existencia de pedido
    public static boolean verifyRequestExistence(Connection conn, int id) {

        boolean result = false;

        try {

            PreparedStatement searchQuery = conn.prepareStatement("SELECT * FROM PEDIDOS WHERE ID = ?");

            searchQuery.setInt(1, id);

            ResultSet rs = searchQuery.executeQuery();

            if (rs.next()){
                result = true;
            } else {
                result = false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    // ------ FIN MÉTODOS AUXILIARES ------


    // ------ DEFINICIÓN DE CLASES ------
    // Clase para manejar las solicitudes de los clientes
//    static class ClientHandler extends Thread {
//        private Socket clientSocket;
//        private Connection conn;
//
//        public ClientHandler(Socket socket, Connection conn) {
//            this.clientSocket = socket; // El socket requiere ser cerrado de forma manual
//            this.conn = conn;
//        }
//
//        @Override
//        public void run() {
//            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
//
//                String inputLine;
//                while ((inputLine = in.readLine()) != null) { // Leer entradas línea por línea
//                    // MANEJO DE SOLICITUDES DEL CLIENTE
//                    // 1. Ver servicios disponibles
//                    // 2. Añadir solicitud de servicio
//                    // 3. Ver estado de los servicios
//                    // 4. Cambiar estado del servicio a completed
//                    // 5. Salir
//                    if (inputLine.equalsIgnoreCase("GET_SERVICES")) {
//                        out.println(getServices());
//                    } else if (inputLine.startsWith("ADD_REQUEST")) {
//                        out.println("Solicitud añadida.");
//                    } else if (inputLine.equalsIgnoreCase("EXIT")) {
//                        break;
//                    } else {
//                        out.println("Comando no reconocido.");
//                    }
//                }
//
//                // l socket
//                clientSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
