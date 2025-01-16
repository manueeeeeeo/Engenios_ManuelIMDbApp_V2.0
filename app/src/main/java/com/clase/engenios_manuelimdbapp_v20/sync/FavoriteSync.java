package com.clase.engenios_manuelimdbapp_v20.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

    /**
     * @param context
     * Constructor en donde le paso el contexto, obtengo el uid del usuario
     * autenticado y la instancia de la bd de firestore*/
    public FavoriteSync(Context context) {
        this.db = FirebaseFirestore.getInstance(); // Inicializo la bd de firestore
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Inicializo el uid del usuario
        this.context = context; // Inicializo el contexto
    }

    /**
     * Método para sincronizar la base de datos local con la base
     * de datos subida en la nube, para en caso de iniciar en otro
     * dispositivo con nuestra cuenta de google o de facebook que se
     * nos carguen nuestrar favoritas*/
    public void syncFavorites() {
        CollectionReference moviesRef = db.collection("favorites")
                .document(userId)
                .collection("movies");

        moviesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Map<String, Object>> moviesList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            moviesList.add(document.getData());
                        }

                        saveToLocal(moviesList);
                    }
                })
                .addOnFailureListener(e -> Log.e("FavoriteSync", "Error al sincronizar favoritos", e));
    }

    /**
     * Método para */
    private void saveToLocal(List<Map<String, Object>> moviesList) {
        SharedPreferences prefs = context.getSharedPreferences("Favoritos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String moviesJson = gson.toJson(moviesList);
        editor.putString("peliculas", moviesJson);
        editor.apply();

        Log.d("FavoriteSync", "Datos sincronizados localmente");
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

    /**
     * Interfaz para */
    public interface FavoriteChangeListener {
        void onFavoritesChanged(List<Map<String, Object>> moviesList);
    }
}