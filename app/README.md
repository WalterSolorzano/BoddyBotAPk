# UniBuddy - Tu Asistente Universitario Inteligente

Bienvenido a UniBuddy, una aplicación de Android desarrollada con Jetpack Compose y Kotlin. UniBuddy está diseñada para ayudar a los estudiantes universitarios a organizar su vida académica, gestionar su asistencia y tiempos de viaje, y mantener la productividad.

## Estructura del Proyecto

### 1. Tablero de Control (Dashboard)
El Tablero de Control es la pantalla principal que ofrece una vista integral de tu día:
- **Resumen Diario:** Una vista contextual que te indica si hoy es un buen día para faltar (según tu margen de faltas, exámenes programados, y clima). Muestra variaciones de texto divertidas y motivacionales.
- **Reporte de Ruta:** Toma tu ubicación GPS real para determinar a qué distancia estás del campus. Compara tu tiempo de viaje actual con tu tiempo promedio y calcula tu hora de llegada respecto a tu próxima clase.
  - *Avatar de Mapa:* Si el GPS detecta que ya estás en el campus (a menos de 0.5km), aparecerá un pequeño avatar de tu ubicación dentro de la ilustración del mapa.
- **Widgets de Progreso:** Un carrusel de tarjetas animadas con información útil como el clima (si está lloviendo), tu progreso semestral y otros datos clave de tu rendimiento.
- **Análisis de Viabilidad de Falta:** Una sección dedicada a mostrar tus clases de hoy, con indicadores claros de si puedes faltar o no (basado en el límite de faltas de cada materia).

### 2. Gestión de Asistencias y Faltas
Este módulo permite registrar tu historial de asistencias a las materias.
- Lleva la cuenta exacta de faltas de cada materia basado en un porcentaje requerido por la universidad (por ejemplo 70% o 80%).
- Si estás muy cerca del campus (detectado por GPS) y tienes una clase en los próximos minutos, el sistema automáticamente levanta un *Geofence Prompt* ofreciéndote registrar tu asistencia en un clic.

### 3. Modo Focus (Pomodoro Integrado)
Una pantalla dedicada a la productividad, ideal para concentrarse en la biblioteca o estudiando para un parcial:
- **Temporizador Personalizable:** Permite configurar ciclos de trabajo (ej. 25m) y descanso (ej. 5m).
- **Animaciones del Personaje:** Nuestra mascota "Buddy" cambia de estado dependiendo de lo que estés haciendo: en "Modo Estudio" se pone los lentes y trabaja concentrado, y en reposo vuelve a la normalidad. La pantalla late rítmicamente al color del estado actual (Naranja para trabajo, Verde para descanso).

### 4. Configuración (ConfigTab)
Donde puedes personalizar tu experiencia y los parámetros del algoritmo.
- **Datos Personales:** Nombre de usuario y ubicación (origen/destino) para cálculos de ruta.
- **Preferencias de Viaje:** Incluye un *Switch* para "Llegar Temprano (+10m)", el cual le añade minutos a tus cálculos de margen para asegurarte de nunca llegar tarde.
- **Semana Semestral:** Configura en qué semana del semestre te encuentras para mantener los widgets sincronizados.

### 5. Historial de Calificaciones (Notas) y Progreso (Pensum)
- Registra tus materias aprobadas.
- Guarda tus evaluaciones o parciales y sus respectivas calificaciones.
- Incluye recordatorios visuales que se cruzan con el Tablero de Control si ese día debes rendir una evaluación.

## Tecnologías y Prácticas Utilizadas
- **UI Moderna:** Jetpack Compose utilizando los estándares visuales de Material Design 3 y una paleta de colores corporativa e intuitiva.
- **Arquitectura Local First:** Todos los registros (materias, faltas, configuraciones) se persisten localmente utilizando SQLite / Room o DataStore / SharedPreferences, asegurando privacidad y uso sin conexión.
- **Algoritmo Contextual:** Los cálculos de asistencia e indicaciones de llegada no son datos estáticos; analizan múltiples variables combinadas (distancia GPS en tiempo real + día de la semana + eventos de evaluación + límite de faltas calculados matemáticamente + lluvia).
- **Feedback Visual:** Uso de `GraphicsLayer`, `Canvas` y animaciones infinitas para mantener una interfaz pulida y viva.

¡Explora la aplicación y mejora tu organización universitaria con UniBuddy!
