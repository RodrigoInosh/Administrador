# README #

This README would normally document whatever steps are necessary to get your application up and running.

### Dependencia ###

* [Eclipse Java IDE version 4.5.*](https://www.eclipse.org/downloads/download.php?file=/oomph/epp/neon/R1/eclipse-inst-linux64.tar.gz)
* [Apache Tomcat v7.0.63](https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.63/bin/)
* [Java Oracle jdk 7](http://www.oracle.com/technetwork/es/java/javase/downloads/jdk7-downloads-1880260.html)
* Bases de Datos:
    * point_licitaciones
    * point_ordenes
    * point_adjudicadas

Las bases de datos se sacan del host:
```
http://sdk.instoreview.cl/point
user: mercadopublico
Pass: Gs5JDW64qPHuGeHX
```

## Carpetas ##
En la carpeta raíz se deben crear las carpetas:

* CargaMercados
* PointClienteLicitaciones
* configPoint
* logPoint
* Validaciones
 
### ¿Cómo ejecutarlo? ###

1. Instalar IDE Eclipse
2. Instalar Apache Tomcat 7.0
3. Cargar las base de datos en local
4. Dentro de la carpeta "configPoint" se debe crear el archivo config.properties con la siguiente estructura:

```
url =jdbc:mysql://127.0.0.1:3306/point_licitaciones?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull&useUnicode=yes&characterEncoding=UTF-8
user = root
pass = 1234
ord = point_ordenes
adj = point_adjudicadas
servidor = 127.0.0.1
classificator_host = 127.0.0.1
time_zone = -3
```

El archivo config.properties sirve para poder configurar las rutas de las bases de datos u otros parámetros sin tener que regenerar y volver a subir el **.war**

5. En el IDE hay que ir a File -> Import... -> Git -> Projects from Git -> Clone URI, y en el input "URI" se debe colocar la ruta del repositorio git@bitbucket.org:techk/point-administrador.git (Si la cuenta tiene usuario y contraseña se deben completar esos campos). Apretar next y elegir las branches que se bajarán. Apretar Finish.
6. En la sección **Servers** del IDE se debe crear un nuevo servidor, seleccionando como "Server runtime environment" Apache Tomcat v7.0 y añadir el proyecto "AdministradorBI", darle a finish.
7. Darle run al servidor y entrar a la url: http://127.0.0.1:8080/AdministradorBI/adminID.jsp?key=iq1Fh3cwuIK