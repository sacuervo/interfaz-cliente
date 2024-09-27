package org.ibero;


public class Servidor {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            // Iniciar el servidor en el puerto 9999
            serverSocket = new ServerSocket(9999);
            System.out.println("Servidor iniciado y esperando conexiones...");

            while (true) {
                // Esperar una conexión del cliente
                clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                // Crear un manejador de cliente para procesar la conexión
                handleClient(clientSocket);

                // Cerrar la conexión con el cliente después de manejar la solicitud
                clientSocket.close();
                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para manejar la conexión del cliente
    private static void handleClient(Socket clientSocket) throws IOException {
        // Flujo de entrada para recibir mensajes del cliente
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Flujo de salida para enviar mensajes al cliente
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputLine;
        // Leer mensajes del cliente hasta que se envíe exit
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Mensaje del cliente: " + inputLine);

            // Procesar la entrada del cliente
            String response = processInput(inputLine);

            // Enviar la respuesta al cliente
            out.println(response);

            if (inputLine.equalsIgnoreCase("exit")) {
                break;
            }
        }
    }

    // Función para procesar entrada de cliente y devolver salida
    private static String processInput(String input) {
        // Lógica para procesar el input
        System.out.println(input);
        String[] tokens = input.split(" "); // Se divide el contenido de la entrada para que la primera palabra del comando y el resto sean argumentos.
        String command = tokens[0].toLowerCase();
        for (int i = 0; i < tokens.length; i++){
            System.out.println(tokens[i]);
        }

        switch (command) {
            case "ver":
                return "Baño, corte y guarderia";
            case "cosa2":
                return "a\nb" + "\nc\nd";
            case "exit":
                return "Desconectando...";
            default:
                return "Comando no reconocido.";
        }
    }

}
