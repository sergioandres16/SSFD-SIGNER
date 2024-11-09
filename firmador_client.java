import socket

SOCK_BUFFER = 1024

def enviar_consulta(consulta):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = ('localhost', 5000)
    
    try:
        sock.connect(server_address)
        sock.sendall(consulta.encode('utf-8'))
        respuesta = sock.recv(SOCK_BUFFER).decode('utf-8')
        return respuesta
    except ConnectionRefusedError:
        print("No se pudo conectar al servidor. Asegúrese de que esté en ejecución.")
        return None
    finally:
        sock.close()

def mostrar_menu():
    # Primero obtenemos y mostramos los tipos de productos
    print("\n=== TIPOS DE PRODUCTOS DISPONIBLES ===")
    tipos = enviar_consulta("tipos productos")
    if tipos:
        print(tipos)
    
    print("\n=== MENÚ DE CONSULTAS ===")
    print("1. Promedio de costos de [TipoProducto]")
    print("2. Producto más vendido")
    print("3. Desviación estándar de costos de [TipoProducto]")
    print("4. Mejor canal de venta")
    print("5. Distribución de costos de [TipoProducto]")
    print("6. Salir")

if __name__ == '__main__':
    # Verificamos la conexión inicial
    print("Conectando con el servidor...")
    respuesta_inicial = enviar_consulta("tipos productos")
    if not respuesta_inicial:
        print("No se pudo establecer conexión con el servidor. Saliendo...")
        exit()
    
    while True:
        mostrar_menu()
        opcion = input("\nSeleccione una opción (1-6): ")
        
        if opcion == '6':
            print("Enviando señal de salida al servidor...")
            enviar_consulta("salir")
            print("¡Hasta luego!")
            break
            
        elif opcion == '1':
            tipo = input("Ingrese el tipo de producto (de la lista mostrada arriba): ")
            respuesta = enviar_consulta(f"promedio de costos de {tipo}")
            if respuesta:
                print(f"\nRespuesta del servidor: {respuesta}")
            
        elif opcion == '2':
            respuesta = enviar_consulta("producto mas vendido")
            if respuesta:
                print(f"\nRespuesta del servidor: {respuesta}")
            
        elif opcion == '3':
            tipo = input("Ingrese el tipo de producto (de la lista mostrada arriba): ")
            respuesta = enviar_consulta(f"desviación estándar de costos de {tipo}")
            if respuesta:
                print(f"\nRespuesta del servidor: {respuesta}")
            
        elif opcion == '4':
            respuesta = enviar_consulta("mejor canal de venta")
            if respuesta:
                print(f"\nRespuesta del servidor: {respuesta}")
            
        elif opcion == '5':
            tipo = input("Ingrese el tipo de producto (de la lista mostrada arriba): ")
            respuesta = enviar_consulta(f"distribución de costos de {tipo}")
            if respuesta:
                print(f"\nRespuesta del servidor: {respuesta}")
            
        else:
            print("Opción no válida. Por favor, intente nuevamente.")
        
        input("\nPresione Enter para continuar...")
