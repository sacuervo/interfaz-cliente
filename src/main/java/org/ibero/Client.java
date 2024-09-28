package org.ibero;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
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

            String userInput = ""; // Esta variable va a almacenar la cadena con el comando y los argumentos que se le envían en forma de cadena al servidor, y que este último debe ejecutar.
            String option; // Contiene la opción del menú que el usuario selecciona
            String responseLine = ""; // Funciona como un contenedor de la línea actual de la respuesta
            int idProducto = -1; // Usado como input del usuarido

            do {
                // Mostrar menú
                System.out.println("\nMenú IberoPet: ");
                System.out.println("1. Ver servicios");
                System.out.println("2. Crear pedido");
                System.out.println("3. Ver estado de pedido");
                System.out.println("4. Finalizar pedido");
                System.out.println("5. Eliminar pedido");
                System.out.println("6. Salir");
                System.out.print("Seleccione una opción: ");
                option = scanner.nextLine();
                System.out.println(option);

                switch (option) {
                    case "1": // Mostrar los servicios que ofrece la guardería
                        // Asignar userInput a comando
                        userInput = "showservices";

                        // Solicitar servicios al servidor
                        out.println(userInput);

                        // Mostrar servicios en consola
                        printServerResponse(in, responseLine);
                        break;
                    case "2": // Crear pedido nuevo
                        // Asignar userInput a comando
                        userInput = "showservices";

                        // Solicitar servicios al servidor
                        out.println(userInput);

                        // Mostrar servicios en consola
                        printServerResponse(in, responseLine);

                        // Pedir a usuario selección de un servicio
                        String serviceId;
                        int serviceInt = -1;

                        do {

                            System.out.println("Por favor seleccione el servicio que desea tomar (1 - 3): ");
                            serviceId = scanner.nextLine();

                            try {
                                serviceInt = Integer.parseInt(serviceId); // Intenta convertir la entrada a entero

                                if (serviceInt < 1 || serviceInt > 3) {
                                    System.out.println("Debe ingresar un valor entre 1 y 3.");
                                }

                            } catch (NumberFormatException ex) { // Captura excepción si la entrada no es un número
                                System.out.println("Debe ingresar un número válido.");
                            }


                        } while (serviceInt < 1 || serviceInt > 3);

                        // Pedir a usuario nombre del cliente
                        System.out.println("Por favor ingrese el nombre de la mascota: ");

                        String userName = scanner.nextLine();

                        // Mandar a servidor selección de servicio (seleccionservicio)
                        userInput = "storeservice " + serviceId + " " + userName;

                        // Mandar a servidor servicio seleccionado y nombre de usuario
                        out.println(userInput);

                        // Mostrar respuesta del servidor
                        printServerResponse(in, responseLine);
                        break;
                    case "3": // Ver estado de pedido
                        // Solicitar id del pedido
                        idProducto = -1;

                        do {
                            System.out.println("Por favor ingrese el ID del pedido:");

                            String input = scanner.nextLine();

                            try {
                                idProducto = Integer.parseInt(input);
                            } catch (NumberFormatException e) {
                                System.out.println("El valor ingresado no es un ID válido. Inténtelo de nuevo.");
                            }

                        } while (idProducto < 0); // Continúa hasta que se ingrese un entero no negativo

                        // Asignar userInput a comando
                        userInput = "getrequeststate " + idProducto;

                        // Pedir estado del pedido al servidor
                        out.println(userInput);

                        // Mostrar estado del pedido con id correspondiente o error
                        printServerResponse(in, responseLine);
                        break;
                    case "4": // Finalizar pedido
                        // Solicitar id del pedido
                        idProducto = -1;

                        do {
                            System.out.println("Por favor ingrese el ID del pedido:");

                            String input = scanner.nextLine();

                            try {
                                idProducto = Integer.parseInt(input);
                            } catch (NumberFormatException e) {
                                System.out.println("El valor ingresado no es un ID válido. Inténtelo de nuevo.");
                            }

                        } while (idProducto < 0); // Continúa hasta que se ingrese un entero no negativo

                        // Asignar userInput a comando
                        userInput = "endrequest " + idProducto;

                        // Mandar a servidor id de pedido
                        out.println(userInput);

                        // Mostrar actualización estado del pedido con id correspondiente o error
                        printServerResponse(in, responseLine);
                        break;

                    case "6" :
                        System.exit(0);
                    default:
                        System.out.println("Opción no válida, intente nuevamente.");
                        userInput = "";
                        break;
                }
            } while (!"6".equals(option));

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


    // Imprimir respuesta con saltos de línea. Mientras que el contenedor no sea nulo o almacene el código estandar "END", significa que hay contenido en la respuesta por leer
    // TODO: Arreglar método, atrapado en bucle infinito
    static void printServerResponse(BufferedReader in, String responseLine) {

        try {
            while ((responseLine = in.readLine()) != null && !responseLine.equals("END")) {
                System.out.println(responseLine);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
