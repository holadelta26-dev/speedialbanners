# Marcado Rápido con Banners

App Android nativa (Kotlin) de marcado rápido con foto de fondo ("banner") por contacto.

## Cómo funciona
- Verás una cuadrícula de 8 casillas (correspondientes a las teclas 1–8).
- **Toca** una casilla vacía → primero eliges una foto de tu galería (el banner), luego eliges de dónde sale el número: **de tus contactos** o **escribiéndolo a mano** (útil si alguien cambió de número y aún no lo tienes guardado en la agenda).
- **Mantén presionada** una casilla ya asignada → llama directo a ese contacto.
- Toca una casilla ya asignada (toque corto) → puedes:
  - **Cambiar número** (de contactos o a mano, sin perder la foto)
  - **Cambiar foto** (sin perder el número)
  - **Quitar** la asignación completa
- Todo se guarda en el teléfono (SharedPreferences), así que persiste aunque cierres la app.

## Cómo compilarla (opción sin instalar nada — recomendada)

Puedes compilar el APK usando **GitHub Actions**, que compila en la nube. No necesitas Android Studio ni descargar nada pesado.

1. Crea una cuenta gratis en https://github.com si no tienes una
2. En la esquina superior derecha, toca el **+** → **New repository**
   - Nombre: `SpeedDialBanners` (o el que quieras)
   - Déjalo en **Public** o **Private**, como prefieras
   - Dale **Create repository**
3. En la página del repositorio recién creado, busca el link **"uploading an existing file"** (o el botón **Add file → Upload files**)
4. Arrastra **TODO el contenido** de esta carpeta `SpeedDialBanners` (todo lo de adentro, no la carpeta en sí) — incluyendo la carpeta oculta `.github` con el archivo `build.yml`. Si tu navegador no te deja arrastrar carpetas ocultas, sube el `.github/workflows/build.yml` por separado usando "Create new file" y escribiendo esa ruta exacta como nombre.
5. Dale **Commit changes** (guardar)
6. Ve a la pestaña **Actions** arriba del repositorio
7. Verás un flujo llamado **"Compilar APK"** — tócalo, luego el botón **Run workflow** → **Run workflow** de nuevo para confirmar
8. Espera 3-6 minutos (se actualiza solo). Cuando termine con una palomita ✅, entra a esa ejecución
9. Abajo, en la sección **Artifacts**, descarga **SpeedDialBanners-apk** — es un .zip con el `.apk` adentro
10. Pasa ese `.apk` a tu celular (Drive, WhatsApp a ti mismo, cable USB, etc.), tócalo desde el explorador de archivos del teléfono e instálalo (puede pedirte permitir "instalar apps de origen desconocido" la primera vez)

## Cómo compilarla con Android Studio (alternativa, si prefieres tenerlo instalado)
1. Instala **Android Studio** (gratis): https://developer.android.com/studio
2. Abre Android Studio → "Open" → selecciona esta carpeta `SpeedDialBanners`
3. Espera a que sincronice Gradle (la primera vez puede tardar varios minutos y necesita internet)
4. Conecta tu teléfono Android por USB con "Depuración USB" activada (Ajustes → Opciones de desarrollador), o usa un emulador
5. Presiona el botón ▶ (Run) en Android Studio

## Permisos que pedirá el teléfono
- **Llamar directamente**: se pide la primera vez que mantienes presionada una casilla (obligatorio para que el "mantener presionado = llamar" funcione sin abrir la app de Teléfono).
- Elegir foto y elegir contacto usan los selectores del propio sistema (Galería / Contactos), así que no piden permisos adicionales en tiempo de ejecución.

## Personalizar
- Para cambiar de 8 a más/menos casillas, edita `SLOT_COUNT` en `SlotStorage.kt` y ajusta `GridLayoutManager` en `MainActivity.kt` si quieres otra distribución (columnas).
- El diseño del banner (tamaño, texto, colores) está en `res/layout/item_speed_dial.xml`.
