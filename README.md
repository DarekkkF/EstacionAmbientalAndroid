# Estación Ambiental - Android

Aplicación Android para el proyecto IoT de monitoreo ambiental.

## Funciones incluidas

- Login con Firebase Authentication (correo y contraseña).
- Creación de cuentas.
- Dashboard de estación ambiental.
- Lectura en tiempo real desde Firebase Realtime Database.
- Temperatura, humedad, luz, gas e índice de confort.
- Estado de la estación.
- Fecha de última actualización.
- Alertas compartidas entre dos celulares.
- Marcar alerta como atendida.
- Cierre de sesión.
- Interfaz simple, pensada para demostración académica.

## Estructura de Firebase esperada

```text
estaciones
└── estacion01
    └── actual
        ├── temperatura
        ├── humedad
        ├── luz
        ├── gas
        ├── indiceConfort
        ├── estado
        └── timestamp

control
└── estacion01
    ├── alerta
    ├── mensaje
    └── actualizadoPor
```

## Antes de abrir el proyecto

1. En Firebase agrega una aplicación Android con el package:

`com.example.estacionambiental`

2. Descarga el archivo `google-services.json`.
3. Copia ese archivo exactamente en:

`app/google-services.json`

4. En Firebase Authentication activa **Email/Password**.
5. Crea al menos dos usuarios para probar la interacción entre dos celulares.
6. En Realtime Database usa reglas que permitan el acceso a usuarios autenticados. Para comenzar:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

7. Crea manualmente los datos de prueba o deja que la parte AWS -> Lambda los escriba.

## Prueba de dos celulares

- Instala la misma APK en dos teléfonos.
- En el teléfono 1 inicia sesión con el usuario 1.
- En el teléfono 2 inicia sesión con el usuario 2.
- Ambos deben leer la misma ruta de la estación.
- Pulsa **ENVIAR ALERTA** en un teléfono.
- El otro teléfono mostrará la alerta automáticamente.
- Pulsa **MARCAR COMO ATENDIDA** para cerrar la alerta.

## Integración con el ESP32

El Android no necesita conectarse directamente al sensor. La arquitectura prevista es:

`ESP32 -> WiFi -> MQTT/TLS -> AWS IoT Core -> Lambda -> Firebase -> Android`

El ESP32 usa el tópico:

`estacion/ambiental/telemetria`

y Firebase utiliza la ruta:

`estaciones/estacion01/actual`

## GitHub

No subas credenciales administrativas, claves privadas ni secretos al repositorio.

## Nota sobre Firebase

Este proyecto se entrega con el código Android listo, pero el archivo `google-services.json` es específico de tu proyecto Firebase y debe ser el archivo de tu propia cuenta.
