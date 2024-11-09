import socket
import time
import math

# Variables globales
SOCK_BUFFER = 1024
consultas_realizadas = []
datos = []

def cargar_datos(archivo):
    """Carga el archivo CSV con los datos de órdenes"""
    global datos
    with open(archivo, "r") as f:
        contenido = f.read()
    
    filas = contenido.split("\n")
    encabezados = filas[0].split(",")
    
    for fila in filas[1:]:
        if len(fila) == 0:
            continue
        valores = fila.split(",")
        registro = {}
        for i, encabezado in enumerate(encabezados):
            registro[encabezado] = valores[i]
        datos.append(registro)

def promedio_costos_producto(tipo_producto):
    """Calcula el promedio de costos de un tipo de producto"""
    suma = 0
    contador = 0
    for registro in datos:
        if registro['ProductType'] == tipo_producto:
            suma += float(registro['CostPrice'])
            contador += 1
    
    if contador == 0:
        return f"No hay datos para el producto {tipo_producto}"
    
    promedio = suma / contador
    return f"El promedio de costos de {tipo_producto} es {promedio:.2f}"

def producto_mas_vendido():
    """Encuentra el producto más vendido"""
    ventas_por_producto = {}
    for registro in datos:
        producto = registro['ProductType']
        if producto in ventas_por_producto:
            ventas_por_producto[producto] += 1
        else:
            ventas_por_producto[producto] = 1
    
    max_producto = max(ventas_por_producto.items(), key=lambda x: x[1])
    return f"El producto más vendido fue {max_producto[0]} con {max_producto[1]} ordenes"

def desviacion_estandar_costos(tipo_producto):
    """Calcula la desviación estándar de costos de un tipo de producto"""
    costos = []
    for registro in datos:
        if registro['ProductType'] == tipo_producto:
            costos.append(float(registro['CostPrice']))
    
    if not costos:
        return f"No hay datos para el producto {tipo_producto}"
    
    # Calcular media
    media = sum(costos) / len(costos)
    # Calcular suma de cuadrados de diferencias
    suma_cuadrados = sum((x - media) ** 2 for x in costos)
    # Calcular desviación estándar
    desviacion = math.sqrt(suma_cuadrados / len(costos))
    
    return f"La desviación estándar de {tipo_producto} es {desviacion:.2f}"

def mejor_canal_venta():
    """Encuentra el canal de venta con más ventas"""
    ventas_por_canal = {}
    total_por_canal = {}
    
    for registro in datos:
        canal = registro['ChannelSales']
        monto = float(registro['TotalSalesAmount'])
        
        if canal in ventas_por_canal:
            ventas_por_canal[canal] += 1
            total_por_canal[canal] += monto
        else:
            ventas_por_canal[canal] = 1
            total_por_canal[canal] = monto
    
    # Encontrar el canal con más ventas
    max_canal = max(ventas_por_canal.items(), key=lambda x: x[1])
    return f"El mejor canal de venta fue {max_canal[0]} con {max_canal[1]} ventas y con un total de {total_por_canal[max_canal[0]]:.2f} soles"

def distribucion_costos_producto(tipo_producto):
    """Calcula estadísticas de distribución de costos"""
    costos = []
    for registro in datos:
        if registro['ProductType'] == tipo_producto:
            costos.append(float(registro['CostPrice']))
    
    if not costos:
        return f"No hay datos para el producto {tipo_producto}"
    
    costos.sort()
    n = len(costos)
    media = sum(costos) / n
    mediana = costos[n//2] if n % 2 != 0 else (costos[n//2 - 1] + costos[n//2]) / 2
    minimo = min(costos)
    maximo = max(costos)
    
    return f"Distribución de ventas de {tipo_producto}: media {media:.2f}, mediana {mediana:.2f}, mínimo {minimo:.2f}, máximo {maximo:.2f}"

def procesar_consulta(consulta):
    """Procesa las consultas recibidas"""
    
    try:
        if consulta == "tipos productos":
            return obtener_tipos_productos()
        # No guardo la solicitud tipo productos porque es algo visual para mi menú del cliente. De querer guardar esa consulta, podemos subir
        # la linea inferior fuera del try
        consultas_realizadas.append(consulta)
        if consulta.startswith("promedio de costos de "):
            tipo_producto = consulta.replace("promedio de costos de ", "")
            return promedio_costos_producto(tipo_producto)
        elif consulta == "producto mas vendido":
            return producto_mas_vendido()
        elif consulta.startswith("desviación estándar de costos de "):
            tipo_producto = consulta.replace("desviación estándar de costos de ", "")
            return desviacion_estandar_costos(tipo_producto)
        elif consulta == "mejor canal de venta":
            return mejor_canal_venta()
        elif consulta.startswith("distribución de costos de "):
            tipo_producto = consulta.replace("distribución de costos de ", "")
            return distribucion_costos_producto(tipo_producto)
        else:
            return "Consulta no reconocida"
    except Exception as e:
        return f"Error al procesar la consulta: {str(e)}"

def obtener_tipos_productos():
    """Obtiene la lista única de tipos de productos"""
    tipos = set()
    for registro in datos:
        tipos.add(registro['ProductType'])
    return "Tipos de productos disponibles: " + ", ".join(sorted(tipos))

def iniciar_servidor(host="0.0.0.0", port=5000):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = (host, port)
    print(f"Iniciando servidor en {server_address[0]}:{server_address[1]}")
    
    sock.bind(server_address)
    sock.listen(5)
    
    try:
        while True:
            print("Esperando conexiones...")
            conn, client_address = sock.accept()
            print(f"Conexión de {client_address[0]}:{client_address[1]}")
            
            try:
                while True:
                    data = conn.recv(SOCK_BUFFER).decode('utf-8')
                    if not data:
                        break
                    
                    if data.lower() == "salir":
                        print("Cliente solicitó terminar")
                        guardar_reporte()
                        break
                    
                    print(f"Consulta recibida: {data}")
                    resultado = procesar_consulta(data)
                    conn.sendall(resultado.encode('utf-8'))
            except ConnectionResetError:
                print("El cliente ha cerrado la conexión de manera abrupta")
            finally:
                conn.close()
    except KeyboardInterrupt:
        print("\nApagando el servidor...")
    finally:
        guardar_reporte()
        sock.close()

def guardar_reporte():
    """Guarda el reporte de consultas realizadas"""
    with open('reporte.txt', 'w') as f:
        f.write("Reporte de consultas realizadas:\n")
        for i, consulta in enumerate(consultas_realizadas, 1):
            f.write(f"{i}. {consulta}\n")

if __name__ == '__main__':
    cargar_datos('orders_data_large.csv')
    iniciar_servidor()
