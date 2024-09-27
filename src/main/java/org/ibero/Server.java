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
            ArrayList<String> serviceDataList = new ArrayList<>();
            serviceDataList.add("Santiago Cuervo");
            serviceDataList.add("Corte");
            serviceDataList.add("30");

            // 2. Pasar ese ArrayList como argumento al método receiveServiceRequest. Almacenar el resultado en un HashMap
            HashMap<String, Object> serviceRequestHash = receiveServiceRequest(serviceDataList);

            // 3. Pasar ese HashMap como argumento al método processServiceRequest
            processServiceRequest(conn, serviceRequestHash);

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
        PreparedStatement createTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS PEDIDOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE_CLIENTE TEXT, SERVICIO TEXT, PRECIO INT, FINALIZADO INT DEFAULT 0)");
        createTable.executeUpdate();
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

    public static void processRequestFinalization() {
        throw new Error("Not implemented yet");
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
