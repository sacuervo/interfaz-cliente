package org.ibero;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Client {

    private static Socket socket = null;
    private static PrintWriter out = null;
    private static BufferedReader in = null;

    public static void main(String[] args) {
        try {
            // Conectar al servidor
            socket = new Socket("localhost", 9999);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Crear la interfaz gráfica
            JFrame frame = new JFrame("Menú IberoPet");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            // Crear radio buttons para las opciones del menú
            JRadioButton option1 = new JRadioButton("Ver servicios");
            JRadioButton option2 = new JRadioButton("Crear pedido");
            JRadioButton option3 = new JRadioButton("Ver estado de pedido");
            JRadioButton option4 = new JRadioButton("Finalizar pedido");
            JRadioButton option5 = new JRadioButton("Eliminar pedido");
            ButtonGroup group = new ButtonGroup();
            group.add(option1);
            group.add(option2);
            group.add(option3);
            group.add(option4);
            group.add(option5);

            // Crear botones
            JButton selectButton = new JButton("Seleccionar");
            JButton exitButton = new JButton("Salir");

            // Panel para agregar las opciones
            JPanel optionPanel = new JPanel();
            optionPanel.setLayout(new GridLayout(6, 1));
            optionPanel.add(option1);
            optionPanel.add(option2);
            optionPanel.add(option3);
            optionPanel.add(option4);
            optionPanel.add(option5);

            // Panel para agregar los botones de selección
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 2));
            buttonPanel.add(selectButton);
            buttonPanel.add(exitButton);

            // Añadir paneles al frame en posiciones adecuadas
            frame.add(optionPanel, BorderLayout.CENTER);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            frame.setVisible(true);

            // Vincular acciones al botón seleccionar
            selectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (option1.isSelected()) {
                        enviarComando("showservices");
                    } else if (option2.isSelected()) {
                        // Llamar al nuevo método para almacenar la respuesta
                        String servicesResponse = almacenarStringRespuesta("showservices");
                        String serviceId = null;

                        // Validar la entrada del servicio
                        boolean validService = false;

                        do {
                            serviceId = JOptionPane.showInputDialog("Seleccione el número del servicio que desea crear (1-3):\n" + servicesResponse);

                            // Si el usuario presiona Cancel, se sale al menú principal
                            if (serviceId == null) {
                                return; // Regresar al menú principal
                            }

                            if (!serviceId.trim().isEmpty()) {
                                try {
                                    int serviceNumber = Integer.parseInt(serviceId);
                                    if (serviceNumber < 1 || serviceNumber > 3) {
                                        JOptionPane.showMessageDialog(null, "Por favor, ingrese un número válido entre 1 y 3.");
                                    } else {
                                        // Pedir el nombre de la mascota
                                        String nombreMascota = JOptionPane.showInputDialog("Por favor ingrese el nombre de la mascota:");
                                        if (nombreMascota != null && !nombreMascota.trim().isEmpty()) {
                                            enviarComando("storeservice " + serviceId + " " + nombreMascota);
                                            validService = true; // Verdadero si todo es correcto
                                        } else {
                                            JOptionPane.showMessageDialog(null, "El nombre de la mascota no puede estar vacío.");
                                        }
                                    }
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(null, "Entrada inválida. Debe ingresar un número entero.");
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Debe ingresar un número.");
                            }
                        } while (!validService); // Repetir hasta que un servicio válido haya sido seleccionado
                        } else if (option3.isSelected()) {
                        int id = solicitarIdPedido();
                        enviarComando("getrequeststate " + id);
                    } else if (option4.isSelected()) {
                        int id = solicitarIdPedido();

                        enviarComando("endrequest " + id);
                    } else if (option5.isSelected()) {
                        int id = solicitarIdPedido();
                        enviarComando("deleterequest " + id);
                    } else {
                        JOptionPane.showMessageDialog(null, "Seleccione una opción.");
                    }
                }
            });

            // Acción para el botón de salir
            exitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor.");
            e.printStackTrace();
        }
    }

    // Método para enviar el comando al servidor y mostrar la respuesta
    private static String enviarComando(String comando) {
        try {
            out.println(comando);
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = in.readLine()) != null && !responseLine.equals("END")) {
                response.append(responseLine).append("\n"); // Cambiar \\n por \n
            }
            String result = response.toString();
            JOptionPane.showMessageDialog(null, result);
            return result; // Devolver la respuesta para uso posterior
        } catch (IOException ex) {
            ex.printStackTrace();
            return null; // Devolver null en caso de error
        }
    }

    // Nuevo método para almacenar la respuesta sin mostrar la ventana
    private static String almacenarStringRespuesta(String comando) {
        try {
            out.println(comando);
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = in.readLine()) != null && !responseLine.equals("END")) {
                response.append(responseLine).append("\n");
            }
            return response.toString(); // Solo devolver la respuesta
        } catch (IOException ex) {
            ex.printStackTrace();
            return null; // Devolver null en caso de error
        }
    }

    // Método para solicitar el ID del pedido al usuario
    private static int solicitarIdPedido() {
        int idProducto = -1;
        do {
            try {
                idProducto = Integer.parseInt(JOptionPane.showInputDialog("Introduzca el ID del pedido:"));
                if (idProducto < 0) {
                    JOptionPane.showMessageDialog(null, "ID inválido. Intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Entrada inválida. Debe ingresar un número entero.");
            }
        } while (idProducto < 0);
        return idProducto;
    }
}
