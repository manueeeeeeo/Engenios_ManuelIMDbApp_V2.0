package com.clase.engenios_manuelimdbapp_v20.ui.favoriteList;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.engenios_manuelimdbapp_v20.adapters.FavoriteAdapters;
import com.clase.engenios_manuelimdbapp_v20.databinding.FragmentFavoritelistBinding;
import com.clase.engenios_manuelimdbapp_v20.models.FavoriteMoviesDatabase;
import com.clase.engenios_manuelimdbapp_v20.models.Movie;
import com.clase.engenios_manuelimdbapp_v20.sync.FavoriteSync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class FavoriteMoviesList extends Fragment {

    private FragmentFavoritelistBinding binding;
    private RecyclerView recy = null; // Variable que hace referencia al RecyclerView
    private Button botonCompartir = null; // Variable para el control del botón de compartir lista de favoritos
    private Toast mensajeToast = null; // Variable para manejar los Toast del fragmento
    private BluetoothAdapter bluetoothAdapter; // Adaptador de Bluetooth
    private SharedPreferences sharedPreferences = null; // Variable para manejar las preferencias del usuario en la app
    private String email = null; // Variable donde guardo el correo con el que esta la sesión iniciada
    private String uid = null; // Variable donde guardo el uid con el que está la sesión iniciada
    private FavoriteSync favoriteSync = null; // Variable para manejar la sincronización de las películas en la nube y en local

    // Launcher para manejar la solicitud de permisos de Bluetooth
    private ActivityResultLauncher<String[]> solicitudMultiplesPermisos =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean todosAceptados = true; // Declaro la variable para controlar si todos estasn aceptados o no
                for (Boolean aceptado : result.values()) { // Hago un foreach para ir comprobando los permisos necesarios
                    if (!aceptado) { // En caso de que alguno sea falso
                        todosAceptados = false; // No están todos los permisos aceptados
                        break;
                    }
                }
                if (todosAceptados) { // En caso de tener todos los permisos aceptados
                    // Lanzmos un Toast avisando al usuario que tiene todos los permisos de Bluetooth concedidos
                    showToast("Todos los permisos de Bluetooth han sido concedidos");
                } else { // En caso contrario
                    // Le decimos que existe algun permiso no aceptado
                    showToast("Algunos permisos de Bluetooth han sido denegados");
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentFavoritelistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtengo el componente de tipo recyclerView de la interfaz
        recy = (RecyclerView) binding.recycle2;
        // Establezco el Layout para el recycler pasandole el contexto
        recy.setLayoutManager(new LinearLayoutManager(getContext()));

        // Compruebo si inicializar las preferencias o no
        if (sharedPreferences == null) { // En caso de que el valor de las preferencias sea nulo
            // Inicializo las preferencias correctamente
            sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        }

        // Cargo en la variable creada el valor del email del usuario con la sesión abierta
        email = sharedPreferences.getString("emailUsuario", "nada");

        // Cargo en la variable creada el valor del uid del usuario con la sesión abierta
        uid = sharedPreferences.getString("uIdUsuario", "nada");

        // Creo un objeto de tipo FavoriteMoviesDatabase que me sirve para tener acceso a la Base de Datos SQLite
        FavoriteMoviesDatabase database = new FavoriteMoviesDatabase(getContext());

        // Creo una lista de película y la cargo gracias al método del objeto antes creado que me obtiene todas las películas favortias
        List<Movie> favoriteMoviesList = database.obtenerTodasLasFavoritas(uid);
        // Inicializo un nuevo adaptador de tipo FavoriteAdapters pasandole el contexto y la lista de favoritas
        FavoriteAdapters adapter = new FavoriteAdapters(getContext(), favoriteMoviesList);
        // Establecemos ese adaptador al recycler
        recy.setAdapter(adapter);

        // Inicializo la clase pasandole el contexto
        favoriteSync = new FavoriteSync(getContext());
        // Llamo al método para sincronizar la bd local con la de la nube
        favoriteSync.syncFavorites(adapter);

        // Obtengo de la interfaz el botón de compartir la lista de favoritas
        botonCompartir = (Button) binding.btnCompartirFavs;
        // Establezco un evento para cuando pulsemos el botón de compartir
        botonCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarPermisosBluetooth();
                // Llamo al método para verificar si tenemos el Bluetooth activado pasandole la lista de favoritas
                verificarBluetooth(favoriteMoviesList);
            }
        });
        return root;
    }

    /**
     * @param favoriteMoviesList
     * Método para comprobar si el Bluetooth esta disponible en nuestro
     * dispositivo, si esta activado o no, y en caso de que se pueda
     * conectar y este activado llamamos al método para mostrar el dialogo
     * con el JSON de nuestras películas favoritas*/
    private void verificarBluetooth(List<Movie> favoriteMoviesList) {
        // Obtengo el adaptador default del bluetooth y así le inicializamos para saber si le tenemos o no activado
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) { // En caso de que el adaptador sea nulo
            // Lanzamos un Toast avisando al usuario que el Bluetooth no está disponible en su dispositivo
            showToast("Bluetooth no está disponible en este dispositivo");
        } else if (!bluetoothAdapter.isEnabled()) { // En caso de que el bluetooth este disponiblepero no lo tenga activado
            // Lanzamos un Toast avisando al usuario de lo mismo
            showToast("Bluetooth está desactivado. Por favor, actívalo para compartir.");
        } else { // En caso de que cumpla todos los requisitos anteriores
            // Llamamos al método para mostrar el dialogo
            mostrarDialogoJson(favoriteMoviesList);
        }
    }

    /**
     * Método en donde compruebo que el sdk del dispositivo para
     * saber si es android 12 o superior y debido a eso si es necesario
     * que solicite permisos de Bluetooth o no*/
    private void solicitarPermisosBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Para dispositivos Android 12.0 o superiores
            String[] permisos = {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
            solicitudMultiplesPermisos.launch(permisos);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Para dispositivos Android desde 6.0
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                solicitudMultiplesPermisos.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }
    }

    /**
     * @param favoriteMoviesList
     * Método para obtener el mensaje de tipo JSON y luego crear y configurar
     * el dialogo que me va a mostrar mi lista de favoritos tras compartir por
     * Bluetooth*/
    private void mostrarDialogoJson(List<Movie> favoriteMoviesList) {
        // Obtengo en un String el JSON entero de películas favoritas
        String jsonFavoritos = createFavoritesJson(favoriteMoviesList);
        // Establezco el builder a través del que voy crear el dialogo para compartir el JSON
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Estableco el titulo del dialogo
        builder.setTitle("Películas Favoritas en JSON");
        // Establezco el mensaje que voy a mostrar
        builder.setMessage(jsonFavoritos);
        // Establezco el botón de confirmar, le pongo el texto de Cerrar y establezco que al tocarle, el dialogo se cierre
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

        // Creo un dialogo gracias al builder
        AlertDialog dialog = builder.create();
        // Muestro el dialogo
        dialog.show();
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

    /**
     * @param favoriteMovies
     * @return
     * Método en el que creo el Array para el JSON y voy uno por uno leyendo los
     * datos de todas las movies de la lista de favoritas y los voy agregando en un objeto
     * de tipo JSONObject que representara cada película dentro del Array del JSON*/
    private String createFavoritesJson(List<Movie> favoriteMovies) {
        // Creo un objeto de tipo JSONArray ya que tengo diferentes objetos dentro del JSON y le inicializo en vacio
        JSONArray jsonArray = new JSONArray();

        // Utilizo el try catch para poder manejar los posibles errores a la hora de hacer el JSONArray
        try {
            // Utilizo un for para recorrer todas las películas de la lista de favoritas
            for (Movie movie : favoriteMovies) {
                // Creo un objeto de tipo JSONObject para irlos agregando al Array del JSON
                JSONObject jsonMovie = new JSONObject();
                // Establezco la key y valor del id
                jsonMovie.put("id", movie.getId());
                // Establezco la key y valor del titulo
                jsonMovie.put("title", movie.getTitle());
                // Establezco la key y valor del titulo original
                jsonMovie.put("originalTitle", movie.getOriginalTitle());
                // Establezco la key y valor de la url de la portada
                jsonMovie.put("imageUrl", movie.getPosterPath());
                // Establezco la key y valor de la fecha de publicación
                jsonMovie.put("releaseYear", movie.getReleaseDate());
                // Establezco la key y valor de la descripción
                jsonMovie.put("overview", movie.getDescripcion());
                // Establezco la key y valor de la valoración
                jsonMovie.put("rating", movie.getValoracion());

                // Agrego el objeto al Array del JSON
                jsonArray.put(jsonMovie);
            }
        } catch (Exception e) { // En caso de que surja alguna excepción
            // Escribo la excepción por el terminal
            e.printStackTrace();
        }

        // Devuelvo la representación del JSON de las películas favoritas
        return jsonArray.toString();
    }
}