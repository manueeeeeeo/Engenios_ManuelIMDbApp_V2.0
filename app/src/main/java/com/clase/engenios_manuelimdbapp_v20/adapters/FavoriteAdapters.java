package com.clase.engenios_manuelimdbapp_v20.adapters;

import android.annotation.SuppressLint;
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

public class FavoriteAdapters extends RecyclerView.Adapter<FavoriteAdapters.FavoriteViewHolder> {
    private Context context; // Contexto de la actividad o fragmento en donde cargemos el recycler
    private List<Movie> favoriteMoviesList; // Lista de películas favoritas
    private Toast mensajeToast = null; // Toast para mostrar mensajes
    private SharedPreferences sharedPreferences = null; // Variable para manejar las preferencias del usuario
    private String email = null; // Variable donde guardare y cargare el correo del usuario con la seión iniciada
    private String uid = null; // Varibel donde guardare y cargare el correo del usuario con la sesión iniciada

    /**
     * @param context
     * @param favoriteMoviesList
     * Constructor del adaptador en donde pasamos el contexto de la actividad o fragmento,
     * junto con la lista de películas o series favoritas*/
    public FavoriteAdapters(Context context, List<Movie> favoriteMoviesList) {
        this.context = context;
        this.favoriteMoviesList = favoriteMoviesList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Creo un Objeto de tipo Movie para guardar el de la posición actual para ir cargando todos los elementos y darles funcionalidad
        Movie movie = favoriteMoviesList.get(position);

        // Creo el obejto de tipo FavoriteSync y le inicializo pasandole el contexto
        FavoriteSync favoriteSync = new FavoriteSync(context);

        // Obtengo las preferencias del usuario
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Cargo en la variable de email el email guardado en las preferencias
        email = sharedPreferences.getString("emailUsuario", "nada");

        // Cargo en la variable el uid guardado en las preferencias
        uid = sharedPreferences.getString("uIdUsuario", "nada");

        // Procedo a cargar la imagen de la portada con Picasso
        Picasso.get()
                .load(movie.getPosterPath()) // Establezco la URL de la imagen de la portada
                .placeholder(R.drawable.baseline_autorenew_24) // Establezco la imagen del placeholder
                .error(R.drawable.icono_error) // Establezco la imagen en caso de que haya algun error
                .into(holder.movieImageView); // Establezco el elemento donde se va a cargar la imagen

        // Establecemos un evento para cuando hagamos un simple click en la película o serie
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Utilizo los Log para poder depurar con el LogCat
                Log.d("Prueba", movie.getId()); // Compruebo si hay id
                Log.d("Prueba", movie.getPosterPath()); // Compruebo si hay url de la portada

                // Creo un nuevo Intent para ir a ver todos los detalles de la película o serie favorita
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                // Introduzco los datos que voy a querer cargar después y además la key para poder recuperar el objeto
                intent.putExtra("movieDetails", movie);
                // Iniciamos la nueva actividad sin cerrar está y así no tener que volver a solicitar a la API cargar todas las respuestas
                context.startActivity(intent);
            }
        });

        // Establecemos un evento para cuando hagamos un on Long Click en el item
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Creamos una nueva instancia de la clase donde creamos y manejamos la bd de sqlite
                FavoriteMoviesDatabase database = new FavoriteMoviesDatabase(context);

                // En caso de que se pueda eliminar la pelicula basandonos en su Id de Pelicula y que este id seá mayor que 0
                if (database.borrarFavorita(movie.getId(), uid)>0) {
                    // Lanzamos un toast al usuario avisando que se ha eliminado la pelicula
                    showToast("Película eliminada de favoritos");
                    // Llamo al método para eliminar esa película de la bd de Firebase
                    favoriteSync.borrarPeli(movie.getId());
                    // Removemos de la lista de peliculas la posición a la cual hemos hecho el on long click
                    favoriteMoviesList.remove(position);
                    // Notificamos que se ha quitado algo del adaptador del recycler
                    notifyItemRemoved(position);
                    // Notificamos el cambio de datos al adaptador
                    notifyItemRangeChanged(position, favoriteMoviesList.size());
                } else { // En caso de que no se pueda eliminar la película
                    // Lanzamos un toast avisando al usuario de lo que ha pasado
                    showToast("Error al eliminar la película de favoritos");
                }

                return true; // Devolvemos true para manejar la respuesta del on long click
            }
        });
    }

    /**
     * @return
     * Método que nos permite para obtener y saber el tamaño de la lista de películas y series favoritas*/
    @Override
    public int getItemCount() {
        return favoriteMoviesList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        // ImagenView individual que nos sirve para generar cada elemento del RecyclerView
        ImageView movieImageView;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Obtenemos el elemento de la interfaz para asemejarlo
            movieImageView = itemView.findViewById(R.id.movieImageView);
        }
    }

    /**
     * @param newMovieList
     * Método para actualizar las peliculas, primero que todo limpio la lista
     * de películas, por otro lado, agrego todas las películas que le paso como parametro
     * a la lista y por último notifico al adaptador los cambios*/
    public void updateMovies(List<Movie> newMovieList) {
        // Limpio la lista de las películas favoritas
        this.favoriteMoviesList.clear();
        // Agrego todas las que le paso por parametro
        this.favoriteMoviesList.addAll(newMovieList);
        // Notifico al adaptador los cambios realizados
        notifyDataSetChanged();
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