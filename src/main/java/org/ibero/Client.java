
package org.ibero;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final String ADDRESS = "localhost"; // Dirección IP
    private static final int PORT = 12345; // Puerto cliente igual al de servidor

    public static void main(String[] args) {
        try (Socket socket = new Socket(ADDRESS, PORT);  // try con recursos para cerrar automáticamente los streams y conexión
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Lectura de datos que vienen desde el servidor. Se pasan de bytes a caracteres.
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado al servidor en " + ADDRESS + ":" + PORT);

            String userInput;
            while (true) {
                System.out.print("Ingrese comando (GET_SERVICES, ADD_REQUEST, EXIT): ");
                userInput = scanner.nextLine();

                out.println(userInput); // Enviar el comando al servidor

                // Leer y mostrar la respuesta del servidor
                String serverResponse = in.readLine();
                System.out.println("Servidor: " + serverResponse);

                if (userInput.equalsIgnoreCase("EXIT")) {
                    break; // Salir del bucle si el usuario ingresa "EXIT"
                }
            }

            System.out.println("Conexión cerrada.");
        } catch (UnknownHostException e) {
            System.err.println("No se encuentra el servidor: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de entrada/salida: " + e.getMessage());
        }
    }
}
