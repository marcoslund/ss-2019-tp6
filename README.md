# ss-2019-tp6

## Compilación

Es necesario tener instalado Maven y Java 8. Comando:
```bash
mvn clean package # compilacion
java -jar target/tpes-1.0-SNAPSHOT.jar # ejecucion
```
También puede importarse el proyecto desde una IDE.

## Visualización

Correr `python analysis/runner.py`. Esto va a crear 3 * 10 archivos en la raiz del proyecto con simulaciones de 5 segundos. Luego, eliminar los que terminan en `-input`. Mover a `analysis/results` los `.xyz` y a `analysis/exits` los que terminan en `.txt`.

Después se puede correr
* `python analysis/visualizer . 6` para calcular el caudal)
* `python analysis/visualizer analysis/results 7` para calcular la energia cinetica)

Comandos:
```bash
ovito
# Desde ovito, en la barra de menús:
# Scripting > Run Script file... > render.py
```
*Nota*: para poder dar formato a los archivos importados con Ovito usando el script de python, es necesario editar el script con la ruta correcta a los archivos (no es necesario si se ejecuta ovito desde el directorio del proyecto)
