package com.clase.engenios_manuelimdbapp_v20.sync;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Manuel
 * @version 1.0*/

public class UsersSync {
    private FirebaseFirestore db = null; // Variable para manejar la base de datos de Firestore
    private CollectionReference usersCollection = null; // Variable para guardar la referencia de la colección que vamos a usar
    private DatabaseUsers userBD = null; // Variable para manejar la base de datos local

    /**
     * Contructor en donde inicializo la base de datos de Firestore,
     * además de darle el nombre de la colección a la misma*/
    public UsersSync(Context context) {
        db = FirebaseFirestore.getInstance(); // Inicializo la base de datos de firestore
        usersCollection = db.collection("users"); // Inicializo la colección de firestore
        userBD = new DatabaseUsers(context);
    }

    /**
     * @param email
     * @param name
     * @param uid
     * Método en el que lo que hago es pasarle como parametros el uid, el nombre
     * y el email del usuario, los guardo en un Map para poder darles una clave
     * y un valor, luego hago referencia a la colección de los usuarios, en concreto
     * a la que tenga el uid que le paso y subo los cambios en caso de haberlos*/
    public void agregarOActualizarUsuario(String uid, String name, String email) {
        // Guardo en un map que se llama eliminar imagen
        Map<String, Object> eliminarImagen = new HashMap<>();
        // El valor de email le establezco como nulo
        eliminarImagen.put("email", null);
        // Esto lo hago para evitar los valores duplicados en la bd en la nube

        // Establezco que se actualice la bd de la nube del usuarios con el uid que le paso
        usersCollection.document(uid).set(eliminarImagen, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Una vez que se haya producido bien la eliminación del email
                    // Declaro el map que voy a usar para darle formato en el NO SQL
                    Map<String, Object> userData = new HashMap<>();
                    // Introduzco la clave y el valor del nombre de la cuenta
                    userData.put("displayName", name);
                    // Introduzco la clave y el valor del email de la cuenta
                    userData.put("email", email);
                    // Introduzco la clave y el valor del uid de la cuenta
                    userData.put("user_uid", uid);

                    // Hago referencia a la coleeción elegido y además uso el merge() para no sobrescribir campos existentes, solo actualizar
                    // los deseados
                    usersCollection.document(uid).set(userData, SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> { // En caso de que todo salga bien
                                Log.d("login_register", "Registrado correctamente");
                            })
                            .addOnFailureListener(e -> { // En caso de que algo falle
                                Log.d("login_register", "No registrado correctamente ha ocurrido el siguiente error: "+ e.getMessage());
                            });
                })
                .addOnFailureListener(e -> Log.d("debug", "Error al eliminar la imagen: " + e.getMessage()));
    }

    /**
     * @param uid
     * Método en el que lo que hago es obtener la fecha y hora actual
     * procedo a crear una especie de objeto que represente el ultimo login
     * y logout del ususario en la aplicación, además compruebo que exista una lista
     * de este tipo en los datos del usuario en la bd de firestore, de ser así
     * me descargo esa lista, agrego el nuevo objeto a la lista y la subo, en caso
     * de no tener la lista aun creada, procedemos a crearla y subir el primer registro,
     * además de controlar los posibles fallos*/
    public void registrarLogin(String uid) {
        // Guardo en una variable de tipo String el tiempo actual
        String currentTime = obtenerTiempoActual();

        // Creo un map en donde crearé un nuevo objeto de tipo log
        Map<String, Object> loginLog = new HashMap<>();
        // En el login le doy el valor del tiempo actual
        loginLog.put("loginTime", currentTime);
        // Mientras que en el logout doy el valor nulo ya que todavia no se ha hecho el logout
        loginLog.put("logoutTime", null);

        // Descargo la lista actual de activity_log antes de modificarla, para evitar errores
        usersCollection.document(uid).get()
                .addOnSuccessListener(documentSnapshot -> { // En caso de que todo vaya bien
                    if (documentSnapshot.exists()) { // En caso de que el documento que busco si que exista
                        // Obtenengo la lista actual de activity_log y me la guardo
                        List<Map<String, Object>> activityLogs = (List<Map<String, Object>>) documentSnapshot.get("activity_log");

                        // Compruebo si la lista de maps tiene algo o no
                        if (activityLogs == null) { // En caso de no tener nada
                            // La inicializo
                            activityLogs = new ArrayList<>();
                        }

                        // Agrego el nuevo objeto de registro
                        activityLogs.add(loginLog);

                        // Procedo a subir la lista actualizada a la bd de Firestore
                        usersCollection.document(uid).update("activity_log", activityLogs)
                                .addOnSuccessListener(aVoid -> { // En caso de que todo vaya bien
                                    Log.d("login_register", "Registrado correctamente");
                                })
                                .addOnFailureListener(e -> { // En caso de que surja algun error
                                    Log.d("login_register", "Registrado correctamente");
                                });
                    } else { // En caso de que el docuemnto no exista
                        // En este caso crearemos la lista de cero y la inicializaremos
                        List<Map<String, Object>> newLogList = new ArrayList<>();
                        // Agregaremos el nuevo objeto de registro
                        newLogList.add(loginLog);

                        // Creo un nuevo map inicializado vacio
                        Map<String, Object> userData = new HashMap<>();
                        // Agrego a este map la nueva lista de registros de log
                        userData.put("activity_log", newLogList);

                        // Hago referencia a la colección de los usuarios y subo el nuevo map con los datos del ususario
                        usersCollection.document(uid).set(userData)
                                .addOnSuccessListener(aVoid -> { // En caso de que todo vaya bien
                                    Log.d("login_register", "Registrado correctamente");
                                })
                                .addOnFailureListener(e -> { // En caso de que algo falle
                                    Log.d("login_register", "Error: "+e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> { // En caso de que algo falle
                    Log.d("login_register", "Error: "+e.getMessage());
                });
    }

    /**
     * @param uid
     * Método en el que le paso como parametro el uid del ususario,
     * obtengo el tiempo actual para actualizar el logout, obtengo la lista
     * de logs de ese usuario en concreto, compruebo si la lista de maps está llena
     * ya que no tendría sentido que la lista este vacia, voy comprobando el último
     * registro de la lista de los logs y compruebo que de verdad el logout está vacio
     * para saber cual es el último login sin logout, una vez encontrado registro el logout
     * en la lista y procedo a subir la lista a la base de datos de firestore*/
    public void registrarLogout(String uid) {
        // Guardo en una variable de tipo String el tiempo actual
        String currentTime = obtenerTiempoActual();

        // Hago referencia al documento del uid y obtengo la información
        usersCollection.document(uid).get()
                .addOnSuccessListener(documentSnapshot -> { // En caso de que todo vaya bien
                    // Me descargo la lista actual de los logs del usuario
                    List<Map<String, Object>> activityLogs = (List<Map<String, Object>>) documentSnapshot.get("activity_log");

                    // Compruebo si la lista no está vacia ni es nula
                    if (activityLogs != null && !activityLogs.isEmpty()) { // En caso de que la lista este llena
                        // Busco el último login sin logout y procedo a actualizarlo
                        for (int i = activityLogs.size() - 1; i >= 0; i--) {
                            // Creo un obejto map y obtengo justo los datos del último que no tiene logout
                            Map<String, Object> log = activityLogs.get(i);
                            // Compruebo si de verdad el logout está vacio
                            if (log.get("logoutTime") == null) { // De ser así
                                // Actualizo solo el logout
                                log.put("logoutTime", currentTime);
                                // Utilizo un break para salir del bucle
                                break;
                            }
                        }

                        // Utilizo la llamada a la colección para actualizar la lista de los logs con la lista completa
                        usersCollection.document(uid).update("activity_log", activityLogs)
                                .addOnSuccessListener(aVoid -> { // En caso de que todo vaya bien
                                    Log.d("logout_register", "Registrado correctamente");
                                })
                                .addOnFailureListener(e -> { // En caso de que algo falle
                                    Log.d("logout_register", "Error: "+e.getMessage());
                                });
                    } else { // En caso de que la lista esté vacia
                        System.err.println("No se encontraron logs de actividad para actualizar.");
                    }
                })
                .addOnFailureListener(e -> { // En caso de que algo vaya mal
                    Log.d("Lista_Logs", "Error: "+e.getMessage());
                });
    }

    /**
     * @param uid
     * @param name
     * @param cambiarNomnbre
     * @param emailCodificado
     * @param fotoPerfil
     * @param ubicacion
     * @param numeroCodificado
     * En este método lo que estoy haciendo es agregar la nueva información
     * del usuario como puede ser su numero de telefono códicado, su
     * foto de perfil, su ubicación, etc. Primero que todo borro algunos
     * campos que se pueden sobreescribir si surjen errores y posteriormente
     * si su borrado ha salido bien, procedo a insertar todos los datos nuevos*/
    public void agregarDatosExtras(String uid, String name, String emailCodificado, String numeroCodificado, String fotoPerfil, String ubicacion, boolean cambiarNomnbre){
        // Declaro un map en donde meteré todos los valores de todas las variables que se pueden duplicar
        Map<String, Object> eliminarImagen = new HashMap<>();
        // Les dare a todas el valor nulo para borrar sus datos de la bd
        eliminarImagen.put("imagen_Perfil", null);
        eliminarImagen.put("number_phone", null);
        eliminarImagen.put("email", null);

        // Subire los campos a aquel documento que concuerde en la coleccion de users con el uid que le paso
        usersCollection.document(uid).set(eliminarImagen, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {// Una vez que todo haya salido bien
                    // Declaro el map que voy a usar para darle formato en el NO SQL
                    Map<String, Object> userData = new HashMap<>();
                    // Introduzco la clave y el valor del email codificado de la cuenta
                    userData.put("email", emailCodificado);
                    // Introduzco la clave y el valor del uid de la cuenta
                    userData.put("user_uid", uid);
                    // Introduzco la clave y el valor de la ubicacion de la cuenta
                    userData.put("ubication", ubicacion);
                    // Introduzco la clave y el valor del numero codificado de la cuenta
                    userData.put("number_phone", numeroCodificado);

                    // Compruebo que el string de la foto de perfil no sea nula
                    if (fotoPerfil != null && !fotoPerfil.isEmpty()) { // En caso de no ser nula
                        // Establezco al mapa otra key con su valor
                        userData.put("imagen_Perfil", fotoPerfil);
                    }

                    // Compruebo si la variable de cambiar nombre es true
                    if (cambiarNomnbre) { // De serlo
                        // Agrego al mapa otra key con su valor
                        userData.put("displayName", name);
                    }

                    usersCollection.document(uid).set(userData, SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("login_register", "Registrado correctamente");
                            })
                            .addOnFailureListener(e -> {
                                Log.d("login_register", "No registrado correctamente ha ocurrido el siguiente error: "+ e.getMessage());
                            });
                })
                .addOnFailureListener(e -> Log.d("debug", "Error al eliminar la imagen: " + e.getMessage()));
    }

    /**
     * @param uid
     * Método en el que obtengo de la base de
     * datos de firebase y cargo todos esos datos
     * en la base de datos en local*/
    public void guardarDatosEnLocal(String uid) {
        // Consulto en la bd en la nube si existe en la coleccion de users un documento que se llame como el uid que le paso
        usersCollection.document(uid).get()
                .addOnSuccessListener(snapshot -> { // En caso de que la consulta salga bien
                    // Compruebo si existe
                    if (snapshot.exists()) { // En caso de que si que exista
                        // Me guardo todos los valores en variables, en caso de que alguno falte, le pongo por defecto ""
                        String nombre = snapshot.getString("displayName") != null ? snapshot.getString("displayName") : "";
                        String email = snapshot.getString("email") != null ? snapshot.getString("email") : "";
                        String numero = snapshot.getString("number_phone") != null ? snapshot.getString("number_phone") : "";
                        String ubicacion = snapshot.getString("ubication") != null ? snapshot.getString("ubication") : "";
                        String imagenPerfil = snapshot.getString("imagen_Perfil") != null ? snapshot.getString("imagen_Perfil") : "";

                        // Procedo a crear un cursor en donde obtendré todos los datos del usuario por uid de la bd local
                        Cursor cursor = userBD.obtenerUsuarioPorUid(uid);
                        // Compruebo que el cursor no sea nulo
                        if (cursor != null && cursor.getCount() > 0) { // En caso de no ser nulo y tener algun campo
                            // Llamo al método para actualizar el usuario
                            userBD.actualizarUsuario(uid, ubicacion, numero, nombre, true);
                            // Llamo al método para guardar la imagen en la bd
                            userBD.guardarImagenEnBaseDeDatos(imagenPerfil, uid);
                        } else { // En caso de que no
                            // Procedo a llamar al método de la bd local para insertar TODOS los datos
                            userBD.insetarUsuarioConTodosLosDatos(uid, nombre, email, ubicacion, numero, imagenPerfil);
                        }

                        Log.d("FirestoreSync", "Datos guardados en la base de datos local.");
                    } else {
                        Log.w("FirestoreSync", "Documento Firestore no encontrado.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreSync", "Error al obtener datos: " + e.getMessage()));
    }



    /**
     * @return
     * Método en el que obtengo el tiempo actual en el formato
     * de año, mes,día, además de la hora, minuto y segundo y devuelvo
     * esa fecha en formato String para guardar el login o el logout*/
    private String obtenerTiempoActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}