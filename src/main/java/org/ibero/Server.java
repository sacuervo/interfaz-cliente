
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

        } catch (SQLException | IOException ex) {
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
    // ------ FIN MÉTODOS AUXILIARES ------


    // ------ DEFINICIÓN DE CLASES ------
    // Clase para manejar las solicitudes de los clientes
    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private Connection conn;

        public ClientHandler(Socket socket, Connection conn) {
            this.clientSocket = socket; // El socket requiere ser cerrado de forma manual
            this.conn = conn;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) { // Leer entradas línea por línea
                    // MANEJO DE SOLICITUDES DEL CLIENTE
                    // 1. Ver servicios disponibles
                    // 2. Añadir solicitud de servicio
                    // 3. Ver estado de los servicios
                    // 4. Cambiar estado del servicio a completed
                    // 5. Salir
                    if (inputLine.equalsIgnoreCase("GET_SERVICES")) {
                        out.println(getServices());
                    } else if (inputLine.startsWith("ADD_REQUEST")) {
                        out.println("Solicitud añadida.");
                    } else if (inputLine.equalsIgnoreCase("EXIT")) {
                        break;
                    } else {
                        out.println("Comando no reconocido.");
                    }
                }

                // l socket
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ------ FIN DEFINICIÓN DE CLASES ------
    // Clase de servicios disponibles
    class Service {
        private String name;
        private int price;

        public Service(String name, int price){
            this.name = name;
            this.price = price;
        }

        public String getNombre() {
            return name;
        }

        public void setNombre(String name) {
            this.name = name;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        @Override
        public String toString() {
            return "Servicio{" +
                    "Nombre: '" + name + '\'' +
                    ", Costo: " + price +
                    '}';
        }
    }

    // Clase de pedidos
    class ServiceRequest {
        private int id; // El id será autoincrementable
        private static int counter = 0;
        private String clientName;
        private String serviceName;
        private int price;
        private boolean completed;

        public ServiceRequest (String clientName, String serviceName, int price, boolean completed){
            counter++;
            this.id = counter;
            this.clientName = clientName;
            this.serviceName = serviceName;
            this.price = price;
            this.completed = completed;
        }

        public static int getId() {
            return id;
        }

        public String getNombreCliente() {
            return clientName;
        }

        public void setNombreCliente(String clientName) {
            this.clientName = clientName;
        }

        public String getNombreServicio() {
            return serviceName;
        }

        public void setNombreServicio(String serviceName) {
            this.serviceName = serviceName;
        }

        public int getPrecio() {
            return price;
        }

        public void setPrecio(int price) {
            this.price = price;
        }

        public boolean isFinalizado() {
            return completed;
        }

        public void setFinalizado(boolean completed) {
            this.completed = completed;
        }

        @Override
        public String toString() {
            return "Pedido {" +
                    "ID: " + id + '\'' +
                    "Nombre Cliente: '" + clientName + '\'' +
                    ", Servicio: '" + serviceName + '\'' +
                    ", Costo: " + price +
                    ", Finalizado: " + completed +
                    '}';
        }
    }
}
