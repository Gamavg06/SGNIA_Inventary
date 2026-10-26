import socket
import threading
import time
import random
import sys

# Servidor Emulador de Báscula Systel Cuora Max
# Protocolo oficial Systel Cuora V7.0 / V6.2 (TCP/IP)
# Escucha en puerto 5000 (y 10001)

PORT_PRIMARY = 5000
PORT_SECONDARY = 10001

current_weight_grams = 2500  # 2.500 kg (PLU 22 CUERO S/G)
current_plu = 22
is_stable = True

def build_systel_binary_response(addr, func_code, payload):
    data = payload.encode('latin1')
    crc = addr ^ func_code
    for b in data:
        crc ^= b
    return bytes([addr, func_code]) + data + bytes([crc])

def handle_client(conn, addr, port):
    global current_weight_grams, current_plu, is_stable
    print(f"[CONEXION] Cliente conectado desde {addr} en puerto {port}")
    conn.settimeout(1.0)
    
    last_stream_time = time.time()
    
    while True:
        try:
            # 1. Leer peticiones entrantes de la app
            try:
                data = conn.recv(1024)
                if not data:
                    break
                
                # Petición Systel Binaria: [addr, func, ...]
                if len(data) >= 3:
                    scale_addr = data[0]
                    func = data[1]
                    
                    # Función 1: Solicitud de Peso, Estabilidad y Tara
                    if func == 1:
                        st_char = 'e' if is_stable else 'i'
                        payload = f"{current_weight_grams:06d}{st_char}000000"
                        resp = build_systel_binary_response(scale_addr, 1, payload)
                        conn.sendall(resp)
                    
                    # Función 23: PING / Estado
                    elif func == 23 or func == 0x17:
                        resp = build_systel_binary_response(scale_addr, 23, "ACK")
                        conn.sendall(resp)
                    
                    # Función 2: Firma digital (decimales de precio: 2)
                    elif func == 2:
                        payload = "F0001C000001S0001P00001A001D2"
                        resp = build_systel_binary_response(scale_addr, 2, payload)
                        conn.sendall(resp)
            except socket.timeout:
                pass
            
            # 2. Envío de flujo continuo (Stream ASCII) cada 400 ms
            now = time.time()
            if now - last_stream_time >= 0.4:
                last_stream_time = now
                st_char = 'e' if is_stable else 'i'
                # Formato Cuora Max ASCII estándar
                frame = f"{current_weight_grams:06d}{st_char}000000\r\n"
                conn.sendall(frame.encode('ascii'))
                
        except (ConnectionResetError, BrokenPipeError):
            break
        except Exception as e:
            break
            
    conn.close()
    print(f"[DESCONEXION] Cliente {addr} desconectado")

def start_server(port):
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        server.bind(('0.0.0.0', port))
        server.listen(5)
        print(f"[ACTIVO] Servidor Báscula Systel Cuora Max escuchando en 0.0.0.0:{port}")
        while True:
            conn, addr = server.accept()
            t = threading.Thread(target=handle_client, args=(conn, addr, port), daemon=True)
            t.start()
    except Exception as e:
        print(f"[ERROR] No se pudo iniciar en puerto {port}: {e}")

if __name__ == '__main__':
    t1 = threading.Thread(target=start_server, args=(PORT_PRIMARY,), daemon=True)
    t1.start()
    
    t2 = threading.Thread(target=start_server, args=(PORT_SECONDARY,), daemon=True)
    t2.start()
    
    print("Emulador Systel Cuora Max activo. Mantén el proceso corriendo...")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Servidor detenido")
