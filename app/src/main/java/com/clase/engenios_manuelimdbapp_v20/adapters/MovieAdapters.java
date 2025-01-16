package com.clase.engenios_manuelimdbapp_v20.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.engenios_manuelimdbapp_v20.MovieDetailsActivity;
import com.clase.engenios_manuelimdbapp_v20.R;
import com.clase.engenios_manuelimdbapp_v20.models.FavoriteMoviesDatabase;
import com.clase.engenios_manuelimdbapp_v20.models.Movie;
import com.clase.engenios_manuelimdbapp_v20.sync.FavoriteSync;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class MovieAdapters extends RecyclerView.Adapter<MovieAdapters.MovieViewHolder> {
    private Context context; // Contexto de la actividad
    private List<Movie> movieList; // Lista de obtención de las peliculas
    private Toast mensajeToast = null; // Toast para el manejo de los mensajes
    private SharedPreferences sharedPreferences = null; // Variable para manejar las preferencias del usuario
    private String email = null; // Variable donde cargaré y manejaré el email del usuario con la sesión inciada
    private String uid = null; // Variable donde cargaré y manejaré el uid del usuario con la sesión iniciada

    /**
     * @param context
     * @param movieList
     * Constructor del adaptador para el recyclerview del top 10 de películas y series, en donde
     * le pasamos como parametros el contexto de la actividad o fragmento desde donde lo llamamos, además
     * de la lista de películas y series que hemos obtenido de la llamamda a la API de imdb.com*/
    public MovieAdapters(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        // Obtengo la pelicula para ponerla en el adaptador
        Movie movie = movieList.get(position);

        // Creo el obejto de tipo FavoriteSync y le inicializo pasandole el contexto
        FavoriteSync favoriteSync = new FavoriteSync(context);

        // Obtengo las preferencias del usuario
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Cargo en la variable email el correo del usuario con la sesión inciada
        email = sharedPreferences.getString("emailUsuario", "nada");

        // Cargo en la variable uid del usuario con la sesión iniciada
        uid = sharedPreferences.getString("uIdUsuario", "nada");

        // Procedo a cargar la imagen de la portada con Picasso
        Picasso.get()
                .load(movie.getPosterPath()) // La url de la imagen de la portada
                .placeholder(R.drawable.baseline_autorenew_24) // El placeholder de la foto
                .error(R.drawable.icono_error) // Foto si tenemos un error
                .into(holder.movieImageView); // El elemento donde vamos a cargar la foto de portada

        // Evento cuando clicko en la portada de la pelicula
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creo un nuevo objeto en donde le igualo a la pelicula que obtengo del adaptador
                Movie movieDetail = movie;
                // UTILIZO EL LOG PARA PODER IR DEPURANDO Y VER QUE TODO VA BIEN
                // Muestro el título de la película por el log
                Log.d("MovieAdapter", "Title: " + movie.getTitle());
                // Muestro la url de la imagen de portada de la película por el log
                Log.d("MovieAdapter", "Image URL: " + movie.getPosterPath());

                // Creo un nuevo intent al que voy a pasar los datos para poder cargar todo de forma más grande y detallada
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                // Introduzco los datos que voy a querer cargar después y además la key para poder recuperar el objeto
                intent.putExtra("movieDetails", movieDetail);
                // Iniciamos la nueva actividad sin cerrar está y así no tener que volver a solicitar a la API cargar todas las respuestas
                context.startActivity(intent);
            }
        });

        // Establezco un evento en la portada para cuando hagan un on click largo
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Creo un nuevo objeto de la clase que me va a ayudar con la bd de sqlite
                FavoriteMoviesDatabase database = new FavoriteMoviesDatabase(context);

                // Guardamos en una variable el id de la película
                String movieId = movie.getId();
                // Guardamos en una variable la url de la película
                String urlIm = movie.getPosterPath();
                // Guardamos en una variable el titulo de la película
                String moviTit = String.valueOf(movie.getTitle());
                // Guardamos en una variable la fecha de la película
                String fechaPe = movie.getReleaseDate();
                // Guardamos en una variable la descripción de la película
                String des = movie.getDescripcion();
                // Guardamos en una variable la valoración de la película
                String val = movie.getValoracion();

                // Tengo que comprobar si los dos valores del segundo endpoint son nulos o no
                if(des == null || val == null){ // En caso de que sean nulos
                    // Lanzo un Toast al usuario diciendole que lo vuelva a intentar para no dejar campos vacios en la bbdd
                    showToast("No podemos cargar todos los datos de la película, pruebe más tarde!!");
                }else{ // En caso de que no sean nulos
                    // En caso de que la película todavia no esté insertada en la bd
                    if (!database.existeEnLaBD(movieId, uid)) {
                        // Utilizo el método de insertar en la bd, pasandole los datos necesarios
                        long result = database.insertarFavorita(movieId, urlIm, moviTit, fechaPe, des, val, uid);
                        // Basandome en la respuesta del método anterior haremos una cosa u otra
                        if (result != -1) { // En caso de que el resultado sea diferente de -1
                            // Lanzamos un toast al usuario indicando que hemos agregado la película a la lista de favoritos
                            showToast("Película " + movie.getTitle() + " agregada a favoritos");
                            // Llamo al método de favoriteSync para agregar la película a la bd de firebase
                            favoriteSync.agregarPeli(movieId, moviTit,
                                    urlIm,
                                    fechaPe, val, des);
                        } else { // En caso de que sea -1
                            // Lanzamos un toast avisando al usuario que ha ocurrido un error al insertar
                            showToast("Error al agregar la película a favoritos");
                        }
                    } else { // En caso de que la película ya este en la bd
                        // Lanzamos un toast al usuario indicando que ya está esa película en su lista
                        showToast("La película ya está en favoritos");
                    }
                }

                return true; // Devolvemos true para manejar las respuestas del método on long click
            }
        });
    }

    /**
     * @return
     * Método para poder obtener y saber el tamaño de la lista de películas y series que obtenemos
     * de la consulta a la API*/
    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        // ImagenView individual que nos sirve para generar cada elemento del RecyclerView
        ImageView movieImageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            // Obtenemos el elemento de la interfaz para asemejarlo
            movieImageView = itemView.findViewById(R.id.movieImageView);
        }
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