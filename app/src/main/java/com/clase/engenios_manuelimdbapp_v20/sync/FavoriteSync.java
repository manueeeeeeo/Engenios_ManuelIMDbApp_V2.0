package com.clase.engenios_manuelimdbapp_v20.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.clase.engenios_manuelimdbapp_v20.models.FavoriteMoviesDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteSync {
    // Variables necesarias para la clase
    private final FirebaseFirestore db; // Variable para gestionar la bd de Firestore
    private final String userId; // Variable para manejar el uid del usuario
    private final Context context; // Variable para manejar el contexto de la actividad
    private final FavoriteMoviesDatabase localDb; // Variable para manejar la bd de sqlite local
    private Toast mensajeToast; // Variable para manejar los toast que envie está actividad

    /**
     * @param context
     * Constructor en donde le paso el contexto, obtengo el uid del usuario
     * autenticado y la instancia de la bd de firestore*/
    public FavoriteSync(Context context) {
        this.db = FirebaseFirestore.getInstance(); // Inicializo la bd de firestore
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Inicializo el uid del usuario
        this.context = context; // Inicializo el contexto
        this.localDb = new FavoriteMoviesDatabase(context); // Inicializo el constructor de la bd local
        this.mensajeToast = null; // Inicializo el Toast que mostrará los mensajes
    }

    /**
     * Método para sincronizar la base de datos local con la base
     * de datos subida en la nube, para en caso de iniciar en otro
     * dispositivo con nuestra cuenta de google o de facebook que se
     * nos carguen nuestrar favoritas*/
    public void syncFavorites() {
        // Guardo en una variable la referencia en donde voy a tener que insertar la película
        CollectionReference moviesRef = db.collection("favorites") // obtengo la colección principal
                .document(userId) // Obtengo el documento que voy a querer leer, que es el uid del usuario
                .collection("movies"); // Obtengo la subcolección del usuario que son sus películas

        // Procedo a obtener todo lo que haya en la colección elegida
        moviesRef.get()
                // En caso de que todo vaya bien
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Compruebo que tenga algo en la colección
                    if (!queryDocumentSnapshots.isEmpty()) { // En caso de tener algo
                        // Genero una lista de tipo map para obtener las keys y los valores del json
                        List<Map<String, Object>> moviesList = new ArrayList<>();
                        // Utilizo un foreach para ir agregando a la lista uno por uno
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Agrego a la lista los datos del documento en esa posición
                            moviesList.add(document.getData());
                        }

                        // Llamo al método en donde guardo las películas una a una pasandole la lista
                        saveToLocal(moviesList);
                    }
                })
                // En caso de que algo falle en la llamada a la bd
                .addOnFailureListener(e -> Log.e("FavoriteSync", "Error al sincronizar favoritos", e));
    }

    /**
     * Método para ir guardando la lista de películas
     * disponibles en la bd de la nube a la bd local de
     * sqlite, vamos obteniendo todos los datos de todas las películas
     * y vamos agregandolo a la bd local*/
    private void saveToLocal(List<Map<String, Object>> moviesList) {
        // Utilizo un foreach para recorrer todos los objetos de la lista MAP
        for (Map<String, Object> movieData : moviesList) {
            String movieId = (String) movieData.get("id"); // Obtengo en una variable el id
            String title = (String) movieData.get("title"); // Obtengo en una variable el titulo
            String posterUrl = (String) movieData.get("posterUrl"); // Obtengo en una variable la url de la portada
            String releaseDate = (String) movieData.get("releaseDate"); // Obtengo en una variable la fecha de publicación
            String rating = (String) movieData.get("rating"); // Obtengo en una variable la valoración
            String overview = (String) movieData.get("overview"); // Obtengo en una variable la descripción

            // Inserto en la bd local la película con todos los elementos basandonos también el uid del usuario
            long result = localDb.insertarFavorita(movieId, posterUrl, title, releaseDate, overview, rating, userId);
            // Compruebo el código o el valor obtenido de la consulta
            if (result == -1) { // En caso de que el resultado sea -1 significa que hay un error
                Log.e("FavoriteSync", "Error al guardar la película en la base de datos local");
                showToast("Película "+title+" no se ha podido sincronizar...");
            } else { // En caso de que se agrege la película correctamente
                Log.d("FavoriteSync", "Película sincronizada localmente: " + title);
            }
        }
    }

    /**
     * Método para agregar una película junto con todos sus atributos
     * a la bd de firestore de fireabse*/
    public void agregarPeli(String movieId, String title, String posterUrl, String releaseDate, String rating, String overview) {
        // Guardo en una variable la referencia en donde voy a tener que insertar la película
        DocumentReference movieRef = db.collection("favorites") // obtengo la colección de la bd de firestore
                .document(userId) // obtengo el documento elegido
                .collection("movies") // obtengo la subcolección del usuario registrado
                .document(movieId); // obtengo el documento que hace referencia a la película de la lista de favoritas de ese usuario

        // Declaro un hasmap para poder guardar los valor de forma key-valor
        Map<String, Object> movieData = new HashMap<>();
        // Establezco el id de la película para luego agregarlo
        movieData.put("id", movieId);
        // Establezco el titulo de la película para luego agregarlo
        movieData.put("title", title);
        // Establezco la url de la portada de la película para luego agregarlo
        movieData.put("posterUrl", posterUrl);
        // Establezco la fecha de publicación de la película para luego agregarlo
        movieData.put("releaseDate", releaseDate);
        // Establezco la valoración de la película para luego agregarlo
        movieData.put("rating", rating);
        // Establezco la descripción de la película para luego agregarlo
        movieData.put("overview", overview);

        // Procedo a insertar los datos en la base de datos de firestore
        movieRef.set(movieData)
                // Establecemos lo que ocurrira en caso de que todo salga correctamente
                .addOnSuccessListener(aVoid -> Log.d("FavoriteSync", "Película añadida correctamente"))
                // Establecemos lo que ocurrira en caso de que algo salga mal
                .addOnFailureListener(e -> Log.e("FavoriteSync", "Error al añadir película", e));
    }

    /**
     * Método para eliminar una película de la lista de favoritas de un usuario
     * a trabes del id de la película*/
    public void borrarPeli(String movieId) {
        // Guardo en una variable la referencia en donde voy a tener que borrar la película
        DocumentReference movieRef = db.collection("favorites") // obtengo la colección de la bd de firestore
                .document(userId) // obtengo el documento elegido
                .collection("movies") // obtengo la subcolección del usuario registrado
                .document(movieId); // obtengo el documento que hace referencia a la película de la lista de favoritas de ese usuario

        // Procedo a borrar el documento seleccionado
        movieRef.delete()
                // Establecemos lo que ocurrira en caso de que todo salga correctamente
                .addOnSuccessListener(aVoid -> Log.d("FavoriteSync", "Película eliminada correctamente"))
                // Establecemos lo que ocurrira en caso de que algo salga mal
                .addOnFailureListener(e -> Log.e("FavoriteSync", "Error al eliminar película", e));
    }

    /**
     * Método para */
    public void listenToChanges(FavoriteChangeListener listener) {
        CollectionReference moviesRef = db.collection("favorites")
                .document(userId)
                .collection("movies");

        moviesRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e("FavoriteSync", "Error al escuchar cambios", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                List<Map<String, Object>> moviesList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    moviesList.add(document.getData());
                }

                listener.onFavoritesChanged(moviesList);
            }
        });
    }

    public interface FavoriteChangeListener {
        void onFavoritesChanged(List<Map<String, Object>> moviesList);
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo*/
    public void showToast(String mensaje){
        // Comprobamos si existe algun toast cargado en el toast de la variable global
        if (mensajeToast != null) { // En caso de que si que exista
            mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
        }

        // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
        mensajeToast = Toast.makeText(context, mensaje, Toast.LENGTH_SHORT);
        // Mostramos dicho Toast
        mensajeToast.show();
    }
}