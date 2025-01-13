package com.clase.engenios_manuelimdbapp_v20.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.engenios_manuelimdbapp_v20.adapters.MovieAdapters;
import com.clase.engenios_manuelimdbapp_v20.api.IMDBApiClient;
import com.clase.engenios_manuelimdbapp_v20.api.IMDBApiService;
import com.clase.engenios_manuelimdbapp_v20.databinding.FragmentHomeBinding;
import com.clase.engenios_manuelimdbapp_v20.models.Movie;
import com.clase.engenios_manuelimdbapp_v20.models.MovieOverviewResponse;
import com.clase.engenios_manuelimdbapp_v20.models.PopularMovieResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Manuel
 * @version 1.0*/

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView; // Variable que nos permite manejar el RecyclerView de la interfaz
    private MovieAdapters adapter; // Variable que hace referencia al adaptador para el recycler de las películas
    private List<Movie> movieList = new ArrayList<>(); // Variable inicializada de la lista de películas
    private IMDBApiService imdbApiService; // Variable para poder usar la interfaz y los métodos de la API
    private int respuestasCorrectas = 0; // Variable para contar si todo se ha cargado correctamente
    private Toast mensajeToast = null; // Variable para controlar los Toast de este fragmento
    private final List<Call<?>> llamadasActivas = new ArrayList<>(); // Variable con la lista de llamadas para cancelarlas en caso de ser necesario
    private String apiKey = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Limpio la lista de películas al iniciar el HomeFragment para evitar posibles errores y duplicaciones
        movieList.clear();

        respuestasCorrectas = 0;

        // Obtengo el reyclerView
        recyclerView = binding.recyclerView;
        // Establezco el nuevo Layout ya que tienen que ir de 2 en 2
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Inicializo el constructor del adaptador pasandole el contexto y la lista
        adapter = new MovieAdapters(getContext(), movieList);
        // Establezco al recyclerView el adaptador
        recyclerView.setAdapter(adapter);

        // Creo un cliente de Http personalizado
        OkHttpClient client = new OkHttpClient.Builder()
                // Agrego un interceptor que nos permite obtener la solicitud de conexión antes de enviarla
                .addInterceptor(chain -> {
                    // Creamos una nueva solicitud basandonos en la original
                    Request newRequest = chain.request().newBuilder()
                            // Establecemos un encabezado con el token para acceder a la conexión segura
                            .addHeader("X-RapidAPI-Key", "2d4355b846mshc466fa63c246723p12a355jsnc0a3ef230019")
                            // Establecemos un encabezado con el host al que vamos a solicitar conectarnos
                            .addHeader("X-RapidAPI-Host", "imdb-com.p.rapidapi.com")
                            .build(); // Confirmamos las configuraciones de la conexión
                    // Devolvemos y enviamos la solicitud
                    return chain.proceed(newRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS) // Establezco el tiempo maximo al intentar conectarnos
                .readTimeout(30, TimeUnit.SECONDS) // Establezco el tiempo maximo de lectura de la conexión
                .build(); // Confirmamos y construimos nuestro cliente

        // Utilizo el objeto retrofit que me permite convetir objetos de dificiles a faciles de obtener y usar
        Retrofit retrofit = new Retrofit.Builder()
                // Establecemos cual es la dirección principal para las solicitud de conexión
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                // Establecemos el cliente que se va a conectar que es el que hemos creado antes
                .client(client)
                // Establecemos que la respuesta que nos de el Host que lo pase a un JSON
                .addConverterFactory(GsonConverterFactory.create())
                // Confirmamos y construimos nuestras configuraciones
                .build();

        // Doy valor al objeto de imdbApiService con el retrofie que lo que nos permite es llamar a los métodos de la API como si fueran métodos Java
        imdbApiService = retrofit.create(IMDBApiService.class);
        // Muestro un Toast indicando que se está cargando correctamente
        showToast("Cargando el Top 10...");
        // Llamo al método en donde cargo el top 10 de películas y series
        loadTopRatedMoviesAndTvShows();

        return root;
    }

    /**
     * Método en el que hago una llamada a la API utilizando la interfaz que tengo creada
     * y ejecuto el endpoint de tilte/get-top-meter para obtener el top 10 de películas y
     * series del momento, obtengo los datos que me interesan del JSON como el id,
     * la url de la imagen de la portada, el título y la fecha de publicación de la película
     * o serie. Una vez obtenemos el top 10 actualizamos el recyclerView y el adaptador para
     * que salgan las carteleras, lanzamos un Toast indicando que se están cargando otros
     * datos y llamo al método para obtener la información que me falta de cada item, además
     * de controlar el hecho de poder recibir y manejar los errores y excepciones que
     * puedan surgir*/
    private void loadTopRatedMoviesAndTvShows() {
        // Obtengo la key con la que voy a ejecutar el comando
        apiKey = IMDBApiClient.getApiKey();
        // Creo un objeto de tipo llamada a la API indicando el método con el que vamos a enlazar el endpoint y le indicamos que lo
        // obtenemos en inglés con el parametro US
        Call<PopularMovieResponse> call = imdbApiService.getTopMeterTitles("US");
        // Agrego la llamada a la API a la lista
        llamadasActivas.add(call);
        // Procedemos a ejecutar la llamada anterior
        call.enqueue(new Callback<PopularMovieResponse>() {
            /**
             * @param response
             * @param call
             * Método onResponse que se ejecuta cuando */
            @Override
            public void onResponse(Call<PopularMovieResponse> call, Response<PopularMovieResponse> response) {
                // Como ya se ha obtenido algo de la llamada la elimino de la lista
                llamadasActivas.remove(call);
                // Compruebo si el fragmento actual sigue en el top 10 o no para saber si sigo cargando cosas o no
                if (!isAdded()) { // En caso de que el HomeFragment ya no este adjunto no hacemos nada
                    return;
                }
                // Compruebo que la respuesta sea satisfactoria y el cuerpo de la misma no sea nulo
                if (response.isSuccessful() && response.body() != null) {
                    // Creo una lista de objetos de tipo PopularMovie junto con Edge y obtengo de la llamada a la api
                    // todas las keys e informaciones de edges
                    List<PopularMovieResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();

                    // Compruebo que la lista no sea nula o este vacia
                    if (edges != null && !edges.isEmpty()) { // En caso de que la lista esté rellena
                        // Limpio la lista de películas para no obtenerlas repetidas
                        movieList.clear();

                        // Utilizo un for para obtener el top 10 e ir creando objetos de tipo Movie para agregarlos al recyclerView
                        for (int i = 0; i < Math.min(edges.size(), 10); i++) {
                            // Creo un objeto de tipo Edge y obtego de la lista de edges la posición en la que estamos en el for
                            PopularMovieResponse.Edge edge = edges.get(i);
                            // Ahora creo un objeto de tipo node que consigo obtenerle gracias al objeto edge antes creado
                            PopularMovieResponse.Node node = edge.getNode();

                            // Creo un objeto de tipo Movie para agregarla los datos
                            Movie movie = new Movie();
                            // Le establezco el valor de la id
                            movie.setId(node.getId());
                            // Le establezco el valor de la url de la portada
                            movie.setPosterPath(node.getPrimaryImage().getUrl());
                            // Le establezco el título de la película o serie
                            movie.setTitle(node.getTitleText().getText());
                            // Le establezco el valor de la fecha de publicación
                            movie.setReleaseDate(String.valueOf(node.getReleaseDate()));
                            // Establezco como nulo la descripción
                            movie.setDescripcion(null);
                            // Establezco como nulo la valoración
                            movie.setValoracion(null);

                            // Agrego la película a la lista
                            movieList.add(movie);
                        }
                        // Notifico al adaptador que se ha cargado una parte de la información de las películas y series
                        adapter.notifyDataSetChanged();

                        // Lanzo un Toast avisando al usuario que se siguen cargando los datos
                        showToast("Caratulas cargadas, cargando más datos...");

                        // Llamo al método para obtener la información restante de las películas y series iniciando por la posición 0
                        loadMovieDetailsSequentially(movieList, 0, 0);
                    } else { // En caso de que edges sea nulo
                        // Lanzamos por el LogCat el error o mensaje de advertencia de que no hay título disponibles
                        Log.e("HomeFragment", "No hay títulos disponibles");
                    }
                }else if (response.code() == 429) {
                    // Límite alcanzado: cambiar clave API y reintentar
                    Log.e("HomeFragment", "Límite de solicitudes alcanzado. Cambiando API Key.");
                    IMDBApiClient.switchApiKey();
                    loadTopRatedMoviesAndTvShows();
                } else {
                    Log.e("HomeFragment", "Error en la respuesta: " + response.message());
                }
            }

            /**
             * @param call
             * @param t
             * Método onFailure en el que en caso de que falle algo
             * imprimo por el LogCat el error de la api y además muestro
             * el mensaje del propio error*/
            @Override
            public void onFailure(Call<PopularMovieResponse> call, Throwable t) {
                // Como ya hemos obtenido algo de la llamada a la API elimino la llamada de la lista
                llamadasActivas.remove(call);
                // Compruebo si el fragmento actual sigue en el top 10 o no para saber si sigo cargando cosas o no
                if (!isAdded()) { // En caso de que el HomeFragment ya no este adjunto no hacemos nada
                    return;
                }
                // Llamo de manera recursiva al mismo método con la siguiente película
                Log.e("HomeFragment", "Error en la llamada API: "+t.getMessage());
            }
        });
    }

    /**
     * @param movies
     * @param index
     * Método en el que le paso una lista de película en donde ya he cargado
     * todas las películas y series del top 10 con algunos de sus datos y un indice que en
     * un principio es un 0, compruebo que el indice que le paso no sea mayor o igual a la
     * longitud de la lista de películas, obtengo la película que le toca basandome en el
     * index y procedo a hacer una llamada a la api de imdb.com para obtener los datos
     * que me faltan que son la descripción y la valoración una vez obtenidas, las establezco
     * en la película que toca y llamo al mismo método de forma recursiva sumando un 1 al
     * index*/
    private void loadMovieDetailsSequentially(List<Movie> movies, int index, int reintento) {
        // Compruebo si el index que le pasamos es superior a la longitud de la lista de películas
        if (index >= movies.size()) {
            // Cuando todas las películas han sido creadas y obtenidos todos sus datos
            comprobarSiEstanDetalles(movies.size());
            // Devuelvo y finalizo el método
            return;
        }

        // Creo un objeto de tipo Movie en donde cargo los datos de la película con el indice en la lista que le pasamos
        Movie movie = movies.get(index);
        // Configuro la llamada a la API utilziando la interfaz de imdbApiService y con el método en donde obtengo los datos de una
        // película o serie pasandole como parametro el id de la película
        Call<MovieOverviewResponse> call = imdbApiService.getMovieOverview(movie.getId());
        // Agrego la llamada a la API a la lista
        llamadasActivas.add(call);
        call.enqueue(new Callback<MovieOverviewResponse>() {
            /**
             * @param call
             * @param response
             * Método onResponse en el que obtengo la descripción y la valoración
             * de la película que le toca basandome en el index y además guardo
             * esos datos en el objeto de tipo Movie, actualizo la lista solo la película
             * que ha obtenido nuevos datos, sumo uno para asegurarme de que se ha cargado
             * correctamente*/
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                // Como ya hemos obtenido algo de la llamada, elimino la llamada de la lista
                llamadasActivas.remove(call);
                // Compruebo si el fragmento actual sigue en el top 10 o no para saber si sigo cargando cosas o no
                if (!isAdded()) { // En caso de que el HomeFragment ya no este adjunto no hacemos nada
                    return;
                }
                // Compruebo que la respuesta sea satisfactoria y el cuerpo no sea nulo
                if (response.isSuccessful() && response.body() != null) { // Si todo ha salido bien
                    // Creo una variable de tipo string en donde guardaré el valor de la key especial para la descripción,
                    // como tal lo que estoy haciendo es ir accediendo a todas las keys del JSON y compruebo si son nulas
                    // debido a que por ejemplo no existan o algun otro error, y en caso de que todas las keys tengan algo
                    // en su interior lo que hago es acceder al cuerpo, data, title, plot, plotText y plainText, que es por así
                    // decirlo la ruta para poder acceder al valor de la descripción de la película o serie
                    String plotText = response.body().getData().getTitle().getPlot() != null &&
                            response.body().getData().getTitle().getPlot().getPlotText() != null &&
                            response.body().getData().getTitle().getPlot().getPlotText().getPlainText() != null
                            ? response.body().getData().getTitle().getPlot().getPlotText().getPlainText()
                            : "Descripción no disponible";
                    // Estblezco en la película la descripción con la variable anterior
                    movie.setDescripcion(plotText);

                    // Obtengo la valoración de la película comprobando las key y que no sean nulas, guardando
                    // todo en una variable de tipo double, aquí pasa lo mismo que con la descripción
                    double rating = response.body().getData().getTitle().getRatingsSummary() != null
                            ? response.body().getData().getTitle().getRatingsSummary().getAggregateRating()
                            : 0.0;
                    // Establezco la valoración pasando la variable anterior a string
                    movie.setValoracion(String.valueOf(rating));

                    // Obtengo en una variable el indice de la película actual
                    int movieIndex = movieList.indexOf(movie);
                    // Compruebo que el indice sea correcto
                    if (movieIndex != -1) { // En caso de que sea correcto
                        // Actualizo el adaptador solo la posición de la película que acabo de cargar todos los datos
                        adapter.notifyItemChanged(movieIndex);
                        // Sumo uno a la variable para contabilizar las respuestas realizadas y obtenidas
                        respuestasCorrectas++;
                    }
                } else { // En caso de que la respuesta no sea satisfactoria o el cuerpo sea nulo
                    // Muestro por el LogCat el error al cargar el id de las películas
                    Log.e("API_FAILURE", "Error al cargar los detalles de la película: " + movie.getId());
                }

                // Llamo de manera recursiva al mismo método con la siguiente película
                loadMovieDetailsSequentially(movies, index + 1, 0);
            }

            /**
             * @param call
             * @param t
             * Método onFailure en el que en caso de que falle algo
             * imprimo por el LogCat el error de la api y además muestro
             * el mensaje del propio error*/
            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                // Como ya hemos obtenido algo de la llamada elimino la llamada de la lista
                llamadasActivas.remove(call);
                // Compruebo si el fragmento actual sigue en el top 10 o no para saber si sigo cargando cosas o no
                if (!isAdded()) { // En caso de que el HomeFragment ya no este adjunto no hacemos nada
                    return;
                }
                Log.e("API_FAILURE", "Error en la llamada API: " + t.getMessage());
                Log.e("API_FAILURE", "Entrando al método de volver a intentar");
                // Llamo de manera recursiva al mismo método con la siguiente película
                volveraIntentar(index, reintento, movies);
            }
        });
    }

    /**
     * @param movies
     * @param index
     * @param reintento
     * Método para que en caso de que falle la llamada a la obtención de datos de una película
     * o serie se repita hasta 3 veces, en caso de que se repita 3 veces y siga sin cargar
     * la establecemos como que no se pueden cargar los detalles*/
    private void volveraIntentar(int index, int reintento, List<Movie> movies) {
        int intentosMaximos = 3; // Límite de reintentos
        // Comprubo que el reintento sea menor que el número de intentos
        if (reintento < intentosMaximos) { // De ser así
            // Lanzo un Toast avisando al usuario que nose que película se va a reintentar cargar los datos
            showToast("Reintentando cargar detalles de la película " + movies.get(index).getTitle());
            // Procedo a llamar al método para obtener los datos sumando uno a la variable de intento
            loadMovieDetailsSequentially(movies, index, reintento + 1);
        } else { // De ser igual o mayor al número maximo de intentos
            // Lanzo un Toast diciendo al usuario que no se han podido cargar los detalles
            showToast("No se pudo cargar los detalles de " + movies.get(index).getTitle() + ". Continuando...");
            // Prosigo con la siguiente película y sus datos
            loadMovieDetailsSequentially(movies, index + 1, 0);
        }
    }

    /**
     * @param totalMovies
     * Método en el que le paso como parametro un entero que representa
     * el número total de películas o series que se tienen que haber cargado*/
    private void comprobarSiEstanDetalles(int totalMovies) {
        // Verifico si ya se han procesado todas las películas y he obetenido todos sus datos
        if (respuestasCorrectas == totalMovies) {
            // Lanzo un Toast al usuario avisandole de que ya se ha cargado todo correctamente
            showToast("Top 10 cargado correctamente!!");
        }
    }

    /**
     * En el método on DestroyView lo que esto haciendo es
     * aparte de limpiar todos los componentes, recorro la lista
     * de llamadas y en caso de que haya alguna activa, la cancelo
     * para que no de problemas al cambiar de fragmento*/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // Mediante un foreach en caso de que alguna de las llamadas de la lista este activa la elimmino
        for (Call<?> call : llamadasActivas) {
            // Compruebo si no está cancelada
            if (!call.isCanceled()) { // Si no está cancelada
                // La cancelo
                call.cancel();
            }
        }
        // Limpio la lista de llamadas
        llamadasActivas.clear();

        // Limpio el RecyclerView
        recyclerView = null;
        // Limpio el adaptador
        adapter = null;

        // Compruebo si existen mensajes a mostrar
        if (mensajeToast != null) { // En caso de que si que haya
            // Los cancelo
            mensajeToast.cancel();
            // Y pongo nulo a la variable para mostrarlos
            mensajeToast = null;
        }
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo*/
    public void showToast(String mensaje){
        if (getContext() != null){
            // Comprobamos si existe algun toast cargado en el toast de la variable global
            if (mensajeToast != null) { // En caso de que si que exista
                mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
            }

            // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
            mensajeToast = Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}