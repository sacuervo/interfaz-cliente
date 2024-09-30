# Documentación para `Server.java` y `Client.java`

## Descripción General

Este proyecto es una versión actualizada de la aplicación cliente-servidor para gestionar servicios en una guardería de mascotas. La aplicación permite crear, actualizar, consultar y eliminar pedidos de servicios, esta vez con una interfaz gráfica en el cliente.

## `Server.java`

### Descripción

El archivo `Server.java` implementa la lógica del servidor, que incluye la conexión a la base de datos y el procesamiento de las solicitudes de los clientes. Este servidor opera como el controlador principal para gestionar los pedidos de servicios, ofreciendo operaciones CRUD (Crear, Leer, Actualizar y Eliminar) para los pedidos.

### Dependencias

- **Java I/O:** Para el manejo de entradas y salidas de datos (`BufferedReader`, `PrintWriter`).
- **Java SQL:** Para interactuar con la base de datos (`Connection`, `PreparedStatement`, `ResultSet`).
- **Java Networking:** Para la comunicación entre el servidor y los clientes (`Socket`, `ServerSocket`).
- **Java Util:** Para utilizar estructuras de datos como `HashMap`.

### Funcionalidad Principal

1. **Métodos CRUD para la base de datos:**
   - `grabServiceInformation`: Recupera información de un servicio específico.
   - `processServiceRequest`: Añade un nuevo pedido a la base de datos.
   - `processRequestFinalization`: Marca un pedido como finalizado.
   - `grabRequestInformation`: Obtiene información sobre un pedido específico.
   - `inquireAllRequestsInformation`: Recupera la información de todos los pedidos.
   - `deleteRequestEntry`: Elimina un pedido de la base de datos.
   - `verifyRequestExistence`: Verifica si un pedido existe en la base de datos.
   - `verifyServiceExistence`: Verifica si un servicio existe en la base de datos.

2. **Métodos de funcionamiento del servidor:**
   - `handleClient`: Gestiona la conexión con los clientes y procesa las entradas que envían.
   - `processInput`: Procesa los comandos enviados por el cliente para interactuar con la base de datos.

3. **Comandos admitidos:**
   - `"showservices"`: Lista todos los servicios disponibles.
   - `"storeservice [service_id] [client_name]"`: Crea un nuevo pedido.
   - `"getrequeststate [request_id]"`: Muestra el estado de un pedido específico.
   - `"endrequest [request_id]"`: Marca un pedido como finalizado.
   - `"deleterequest [request_id]"`: Elimina un pedido.
   - `"exit"`: Finaliza la conexión con el cliente.

### Ejecución

Para iniciar el servidor, compila y ejecuta el método `main` en `Server.java`. El servidor espera conexiones de los clientes en el puerto 9999 y procesa las solicitudes según los comandos recibidos.

## `Client.java`

### Descripción

El archivo `Client.java` implementa la lógica del cliente con una interfaz gráfica usando `Swing`. El cliente se conecta al servidor y le envía comandos para realizar operaciones relacionadas con los servicios y pedidos.

### Dependencias

- **Java I/O:** Para el manejo de entradas y salidas de datos (`BufferedReader`, `PrintWriter`).
- **Java Networking:** Para la comunicación con el servidor (`Socket`).
- **Java Swing:** Para crear la interfaz gráfica del usuario.
- **Java AWT:** Para manejar eventos y componentes gráficos.

### Funcionalidad Principal

1. **Interfaz Gráfica:**
   - Muestra un menú con las opciones:
     1. Ver servicios.
     2. Crear pedido.
     3. Ver estado de pedido.
     4. Finalizar pedido.
     5. Eliminar pedido.
   - Botones de selección para ejecutar las opciones del menú y un botón para salir de la aplicación.

2. **Interacción con el Servidor:**
   - El cliente se conecta al servidor en `localhost` y en el puerto `9999`.
   - Envía comandos al servidor según las opciones seleccionadas por el usuario en la interfaz gráfica.
   - Muestra las respuestas del servidor en cuadros de diálogo.

3. **Métodos Clave:**
   - `enviarComando`: Envía un comando al servidor y muestra la respuesta en un cuadro de diálogo.
   - `almacenarStringRespuesta`: Envía un comando al servidor y almacena la respuesta para uso posterior.
   - `solicitarIdPedido`: Solicita al usuario que ingrese un ID de pedido.
   - `main`: Configura la interfaz gráfica y establece las conexiones necesarias con el servidor.

### Ejecución

1. Asegúrate de estar en el directorio raiz del proyecto.
2. Compilar el cliente y el servidor -> `javac ./src/Client.java ./src/Server.java`
3. Ejecutar el servidor añadiendo el .jar al CLASSPATH -> `java -cp ".;sqlite-jdbc-3.46.1.3.jar;src" ./src/Server.java`
4. Ejecutar el cliente -> `java ./src/Client.java`


### Interfaz Gráfica

- La interfaz gráfica está construida con `Swing`, proporcionando una experiencia más amigable al usuario.
- El menú incluye opciones para ver los servicios, crear pedidos, ver el estado de un pedido, finalizar un pedido y eliminarlo, con cuadros de diálogo para la entrada de datos.

### Consideraciones

- El servidor debe estar ejecutándose antes de iniciar el cliente para que este pueda conectarse correctamente.
- La entrada del usuario es validada para asegurar que los datos sean correctos, como la selección de servicios y el ingreso de IDs de pedidos.

### Manejo de Errores

- El cliente incluye validaciones y cuadros de diálogo que notifican al usuario sobre entradas inválidas o errores de conexión.
- El servidor maneja excepciones SQL y verifica la existencia de registros antes de operar sobre ellos.

## Resumen

Esta versión actualizada de la aplicación cliente-servidor incluye una interfaz gráfica en el cliente que mejora la experiencia del usuario. El servidor gestiona los datos y realiza las operaciones CRUD en la base de datos, mientras que el cliente permite al usuario interactuar con los servicios y pedidos mediante una interfaz amigable.
