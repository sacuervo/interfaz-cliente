package org.ibero;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        Scanner scanner = new Scanner(System.in);

        try {
            // Conectar al servidor en la dirección localhost y el puerto 9999
            socket = new Socket("localhost", 9999);
            System.out.println("Conectado al servidor.");

            // Flujo de salida para enviar mensajes al servidor
            out = new PrintWriter(socket.getOutputStream(), true);
            // Flujo de entrada para recibir mensajes del servidor
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String userInput = "";
            String option;
            String responseLine; // Funciona como un contenedor de la línea actual de la respuesta

            do {
                // Mostrar menú
                System.out.println("\nMenú IberoPet: ");
                System.out.println("1. Ver servicios");
                System.out.println("2. Crear pedido");
                System.out.println("3. Ver estado de pedido");
                System.out.println("4. Finalizar pedido");
                System.out.println("5. Eliminar pedido");
                System.out.print("Seleccione una opción: ");
                option = scanner.nextLine();
                System.out.println(option);

                switch (option) {
                    case "1":
                        userInput = option;
                        break;
                    case "2":
                        out.println("ver servicios");

                        System.out.println(in.readLine());

                        out.println("cosa2");

                        while ((responseLine = in.readLine()) != null && !responseLine.equals("END")) {
                            System.out.println(responseLine);
                        }; // Imprimir respuesta con saltos de línea. Mientras que el contenedor no sea nulo o almacene el código estandar "END", significa que hay contenido en la respuesta por leer

                        break;
                    case "3":
                        System.out.print("Ingrese la cadena a convertir a minúsculas: ");
                        userInput = "lowercase " + scanner.nextLine();
                        break;
                    case "4":
                        userInput = "exit";
                        break;
                    default:
                        System.out.println("Opción no válida, intente nuevamente.");
                        userInput = "";
                        break;
                }

                if (!userInput.isEmpty()) {
                    // Enviar el mensaje al servidor
                    out.println(userInput);
                    // Leer la respuesta del servidor
                    System.out.println("Respuesta del servidor: " + in.readLine());
                }
            } while (!"4".equals(option));

            System.out.println("Desconectado del servidor.");
        } catch (UnknownHostException e) {
            System.err.println("No se pudo conectar al host especificado.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error de E/S al conectar con el servidor.");
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
