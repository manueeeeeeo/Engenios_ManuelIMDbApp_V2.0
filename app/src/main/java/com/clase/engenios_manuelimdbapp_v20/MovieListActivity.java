package com.clase.engenios_manuelimdbapp_v20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.engenios_manuelimdbapp_v20.adapters.MovieAdapters;
import com.clase.engenios_manuelimdbapp_v20.api.TMDBApiService;
import com.clase.engenios_manuelimdbapp_v20.models.Movie;
import com.clase.engenios_manuelimdbapp_v20.models.TMDBMovie;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieListActivity extends AppCompatActivity {
    private RecyclerView recy = null; // Variable para manejr el recyclerView de la lista de la busqueda
    private TMDBApiService apiService = null; // Variable para poder hacer la llamada a la interfaz de la API de TMDB
    private MovieAdapters adaptador = null; // Variable que representa el adaptador del recyclerView
    private Toast mensajeToast = null; // Variable para manejar los Toast del fragmento
    private String year = null; // Variable para manejar el año de la busqueda
    private String generoId = null; // Variable para manejar el genero de la busqueda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtengo el Intent desde el que he accedido a esta actividad
        Intent intent = getIntent();
        // Guardo en la variable que he creado el id que paso por el Intent
        generoId = intent.getStringExtra("generoId");
        // Guardo en la variable del año que he creado el año que paso por Intent
        year = intent.getStringExtra("yearBusqueda");

        // Obtengo el recyclerView de la interfaz de usuario
        recy = (RecyclerView) findViewById(R.id.rec);
        // Establezco el layout al adaptador para que se vean dos columnas
        recy.setLayoutManager(new GridLayoutManager(this, 2));
        // Inicializo un nuevo adaptador pasandole el contexto del fragmento y una lista vacia en principio
        adaptador = new MovieAdapters(this, new ArrayList<>());
        // Establezco el adaptador al recyclerView
        recy.setAdapter(adaptador);

        // Configuro Retrofitpara conectarme a la API y obtener respuestas
        Retrofit retrofit = new Retrofit.Builder()
                // Establecemos cual es la dirección principal para las solicitud de conexión
                .baseUrl("https://api.themoviedb.org/3/")
                // Establecemos que la respuesta que obtengamos de la API lo covierta en un JSON
                .addConverterFactory(GsonConverterFactory.create())
                .build(); // Confirmamos y contruimos el objeto de retrofit personalizado

        // Inicializo la interfaz de la API para poder acceder a los métodos o endpoints de la misma
        apiService = retrofit.create(TMDBApiService.class);

        // Llamo al método para cargar las películas con la API de IMDB
        cargarPeliculas();
    }

    /**
     * Méotodo en donde hago una llamada a la API de TMDB para obtener
     * y filtrar películas destacadas por año y género, dentro de este método
     * compruebo tanto el método en caso de que todo funcione correctamente
     * por lo que generaría objetos de tipo Movie para poder mostrar, agregar a la lista
     * de favoritas, ect. Además, también miro en caso de que falle la llamada mostrar
     * el error y manejarlo para que no pete la aplicación*/
    public void cargarPeliculas(){
        // Llamo al método de la interfaz con la que manejo el servicio de la API
        apiService.searchMoviesByGenreAndYear(
                "b1c24c8d4a61565bdbe862465f3a20b5", // Establezco la key de la api
                "en-US", // Establezco el idioma en que me devuelve los datos
                generoId, // Establezco el id del género elegido
                year // Establezco el año para filtrar
        ).enqueue(new Callback<TMDBMovie>() {
            /**
             * @param call
             * @param response
             * Con este método onReponse es lo que ejecuto cuando la API
             * responde correctamente ante la llamada, lo que hago es crear una lista
             * para obtener los resultados de la misma, mientras que luego voy creando
             * objetos de tipo Movie rellenando todos sus datos y los vamos agregando a la lista
             * de películas y series filtradas por el año y género*/
            @Override
            public void onResponse(Call<TMDBMovie> call, Response<TMDBMovie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Creo una lista de TMDBMovie para obtener los resultados de la llamada a la API
                    List<TMDBMovie> tmdbMovies = response.body().getResults();

                    // Verifico que la lista de películas no esté vacía o sea nula
                    if (tmdbMovies != null && !tmdbMovies.isEmpty()) {
                        // Creo una lista de tipo Movie para poder ir generando las películas
                        List<Movie> movies = new ArrayList<>();

                        // Convierto cada objeto TMDBMovie a un objeto Movie
                        for (TMDBMovie tmdbMovie : tmdbMovies) {
                            String title = tmdbMovie.getTitle(); // Obtengo el titulo de la pelicula
                            String originalTitle = tmdbMovie.getOriginal_title(); // Obtengo el titulo original de la pelicula
                            String releaseDate = tmdbMovie.getRelease_date(); // Obtengo la fecha de estreno
                            String overview = tmdbMovie.getOverview(); // Obtengo la descripción de la misma
                            String rati = tmdbMovie.getVote_average(); // Obtengo la valoración de la película
                            String idMo = tmdbMovie.getId(); // Obtengo el id de la película

                            // Creo una variable para la imagen de portada, ya que la API solo nos da la ruta relativa
                            String imageUrl = "https://image.tmdb.org/t/p/w600_and_h900_bestv2" + tmdbMovie.getPoster_path();

                            // Creo el Objeto movie donde guardaré todos los datos
                            Movie movie = new Movie();
                            movie.setId(idMo); // Guardo el ID
                            movie.setTitle(title); // Guardo el título
                            movie.setOriginalTitle(originalTitle); // Guardo el titulo original
                            movie.setReleaseDate(releaseDate); // Guardo la fecha de publicación
                            movie.setPosterPath(imageUrl); // Guardo la imagen de portada
                            movie.setDescripcion(overview); // Guardo la descripción
                            movie.setValoracion(rati); // Guardo la valoración

                            // Agrego la Movie creada a la lista
                            movies.add(movie);
                        }

                        // Paso la lista completa de películas junto con el Contexto al adaptador del Recycler
                        adaptador = new MovieAdapters(MovieListActivity.this, movies);
                        // Establecemos el adaptador al recycler
                        recy.setAdapter(adaptador);
                    } else { // En caso de que la lista este vacia
                        // Lanzo un Toast al usuario diciendole que no se han encontrado películas
                        showToast("No se encontraron películas.");
                    }
                } else { // En caso de que la respuesta de la API no sea la correcta
                    // Lanzo un Toast al usuario diciendole que no se han encontrado películas
                    showToast("No se encontraron películas.");
                }
            }

            /**
             * @param call
             * @param t
             * Método que sucedera si la llamada a la API falla*/
            @Override
            public void onFailure(Call<TMDBMovie> call, Throwable t) {
                // Lanzo un Toast notificando al usuario del error ocurrido
                showToast("Error de conexión: " + t.getMessage());
            }
        });
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
        mensajeToast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
        // Mostramos dicho Toast
        mensajeToast.show();
    }
}