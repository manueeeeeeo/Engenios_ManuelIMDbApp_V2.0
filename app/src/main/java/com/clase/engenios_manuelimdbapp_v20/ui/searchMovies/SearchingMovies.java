package com.clase.engenios_manuelimdbapp_v20.ui.searchMovies;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.clase.engenios_manuelimdbapp_v20.MovieListActivity;
import com.clase.engenios_manuelimdbapp_v20.R;
import com.clase.engenios_manuelimdbapp_v20.adapters.SpinnerGeneroAdapter;
import com.clase.engenios_manuelimdbapp_v20.api.TMDBApiService;
import com.clase.engenios_manuelimdbapp_v20.databinding.FragmentSearchmoviesBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Manuel
 * @version 1.0*/

public class SearchingMovies extends Fragment {
    // Declaro las variables necesarias para la clase
    private FragmentSearchmoviesBinding binding=null;
    private EditText busqueda = null; // Variable que representa el editText donde metemos el año para filtrar las películas
    private Button botonBuscar = null; // Variable que representa el botón de buscar película
    private Spinner spinnerGenero = null; // Variable que representa el spinner de categorias de películas
    private Toast mensajeToast = null; // Variable para manejar los Toast del fragmento
    private TMDBApiService apiService = null; // Variable para poder hacer la llamada a la interfaz de la API de TMDB
    private Map<String, String> genreMap = new HashMap<>(); // Mapa para almacenar ID de géneros y nombres de la busqueda en la API

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSearchmoviesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        // Obtengo de la interfaz todos los componentes visuales necesarios
        busqueda = (EditText) binding.editFechaPeli;
        busqueda.setText("");
        botonBuscar = (Button) binding.btnBuscarPeli;
        spinnerGenero = (Spinner) binding.spinnerCategorias;

        // Configuro Retrofitpara conectarme a la API y obtener respuestas
        Retrofit retrofit = new Retrofit.Builder()
                // Establecemos cual es la dirección principal para las solicitud de conexión
                .baseUrl("https://api.themoviedb.org/3/")
                // Establecemos que la respuesta que obtengamos de la API lo covierta en un JSON
                .addConverterFactory(GsonConverterFactory.create())
                .build(); // Confirmamos y contruimos el objeto de retrofit personalizado

        // Inicializo la interfaz de la API para poder acceder a los métodos o endpoints de la misma
        apiService = retrofit.create(TMDBApiService.class);

        // Procedo a llamar al endpoint para obtener todos los géneros
        // Como parametro paso la Key de la API, y el lenguaje en el que vamos a obtener los generos
        apiService.getMovieGenres("b1c24c8d4a61565bdbe862465f3a20b5", "en-US").enqueue(new Callback<JsonObject>() {
            /**
             * @param call
             * @param response
             * En este método onResponse lo que hago es en caso de que la llamada para
             * obtener los géneros de la API de TMDB y poder establecer el adaptador
             * al spinner*/
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) { // En caso de que la respuesta sea correcta y el cuerpo no sea nulo
                    // Creo un objeto de tipo JsonObject en donde cargo todos los datos obtenidos de la respuesta
                    JsonObject responseObject = response.body();
                    // Creo un objeto de tipo JsonArray en donde obtengo del JsonObject el array de géneros
                    JsonArray genresArray = responseObject.getAsJsonArray("genres");

                    // Extraigo los nombres de los géneros, junto con sus ids y relleno el genreMap
                    genreMap.clear();  // Limpio el mapa en cada nueva carga
                    List<String> genreNames = new ArrayList<>();
                    // Utilizo un foreach para recorres el array de géneros
                    for (JsonElement element : genresArray) {
                        // Creo un objeto de tipo JsonObject para ir obteniendo los datos de los objetos del array
                        JsonObject genre = element.getAsJsonObject();
                        // Obtengo el nombre del género
                        String genreName = genre.get("name").getAsString();
                        // Obtengo el ID de género
                        String genreId = genre.get("id").getAsString();
                        // Agrego a la lista de nombre de géneros el nombre
                        genreNames.add(genreName);
                        // Mapeo el nombre del género y de su ID
                        genreMap.put(genreName, genreId);
                    }

                    // Configuro el adaptador del Spinner
                    SpinnerGeneroAdapter adapter = new SpinnerGeneroAdapter(
                            getActivity(), // Le paso la actividad
                            android.R.layout.simple_spinner_item, // Le paso el item de spinner de la interfaz
                            genreNames.toArray(new String[0]) // Le paso la lista en formato array con la elección principal
                    );
                    // Establezco el adaptador del spinner del genero con el adaptador que he creado antes
                    spinnerGenero.setAdapter(adapter);
                } else { // En caso de que la respuesta no sea satisfactoria o sea nula
                    // Lano un Toast avisando al usuario de lo ocurrido
                    showToast("Error al cargar los géneros");
                }
            }

            /**
             * @param call
             * @param t
             * En este método onFailure lo que hacemos es que en caso de que
             * falle la llamada a la API mostrmos un Toast al usuario
             * avisandole del error ocurrido*/
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Lanzo un Toast al usuario para que sepá que error a ocurrido
                showToast("Error de conexión: " + t.getMessage());
            }
        });

        // Establezco un evento al botón de buscar para que suceda cuando le pulsemos
        botonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creo una variable en donde obtengo el año para filtrar
                String year = busqueda.getText().toString();
                // Obtengo el genero que he elegido con el spinner
                String selectedGenre = (String) spinnerGenero.getSelectedItem();

                // Comprubo si las variables anteriores son nulas o no estan rellenas
                if (year.isEmpty() || selectedGenre == null) {
                    // Si así es, lanzo un Toast avisando al usuario que rellene todos los datos
                    showToast("Por favor, ingrese un año y seleccione un género.");
                    // Y finalizamos el método
                    return;
                }

                // Obtengo el id del género que he elegido en el spinner
                String genreId = genreMap.get(selectedGenre);

                // Limpio el EditText del año
                busqueda.setText("");

                // Creo el Intent para pasar a la siguiente actividad
                Intent in = new Intent(getContext(), MovieListActivity.class);
                // Le paso como parametros dos objetos
                // El id del género que he seleccionado
                in.putExtra("generoId", genreId);
                // El año de la busqueda de las películas
                in.putExtra("yearBusqueda", year);
                startActivity(in);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        mensajeToast = Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT);
        // Mostramos dicho Toast
        mensajeToast.show();
    }
}