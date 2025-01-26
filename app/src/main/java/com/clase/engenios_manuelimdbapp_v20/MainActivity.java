package com.clase.engenios_manuelimdbapp_v20;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.clase.engenios_manuelimdbapp_v20.models.Movie;
import com.clase.engenios_manuelimdbapp_v20.sync.UsersSync;
import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.clase.engenios_manuelimdbapp_v20.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavigationView navigationView;
    private FirebaseAuth auth=null; // Variable para manejar la autentificación con Firebase
    private Button botonCerrarSesion = null; // Variable para poder cerrar la sesión
    private String nombre = null; // Variable donde cargaré el nombre de usuario
    private String correo = null; // Variable donde cargaré le email de usuario
    private String imagenUrl = null; // Variable donde cargaré la url de la foto de perfil de usuario
    private String uIdUsuario = null; // Variable para cargar y manejar el uid del usuario
    private SharedPreferences sharedPreferences = null; // Variable para manejar las preferencias del usuario y guardar posibles datos
    private TextView infoCorreo = null; // Variable para manejar el textview del correo del usuario
    private TextView infoNombre = null; // Variables para manejar el textview del nombre del usuario
    private ImageView infoUrlFoto = null; // Variable para manejar la imageview de la foto de perfil del usuario
    private String email = null; // Variable para manejar el email que obtenemos del anterior Intent
    private Toast mensajeToast = null; // Variable para manejar los Toast de está actividad
    private String message = null; // Variable para almacenar el mensaje que recibo del otro Intent y así inicio sesión y cierro
    private String uid = null; // Variable para almacenar y manejar el uid del usuario con sesion iniciada
    private List<Movie> movieList = new ArrayList<>(); // Lista compartida
    private boolean datosActualizados = false; // Indicador de cambio de datos
    private DatabaseUsers userdb = null;
    private UsersSync sincronizacionUser = null;
    private String base64Imagen = null;
    private String nombreUsuario = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtengo la instancia de la autentificación de firebase
        auth = FirebaseAuth.getInstance();

        // Obtengo las preferencias del usuario
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Cargo en la variable que he creado el email del usuario y en caso de que no exista ningun registro ponemos el valor de nada
        correo = sharedPreferences.getString("emailUsuario", "nada");
        // Cargo en la variable que he creado el uid del usuario y en caso de que no exista ningun registro ponemos el valor de nada
        uIdUsuario = sharedPreferences.getString("uIdUsuario", "nada");

        userdb = new DatabaseUsers(this);

        sincronizacionUser = new UsersSync();

        // Obtengo el Intent a traves del cual he accedido a esta Actividad
        Intent intent = getIntent();
        // Obtengo el mensaje del Intent
        message = intent.getStringExtra("message");
        if (message != null) { // Compruebo que el mensaje no sea nulo
            if (message.equals("Conectado por Facebook")) { // En caso de que sea Facebook
                // Proceso los datos de Facebook
                nombre = intent.getStringExtra("name");
                imagenUrl = intent.getStringExtra("photoUrl");
                uid = intent.getStringExtra("uidUs");
            } else if (message.equals("Conectado por Google")) { // En caso de que sea Google
                // Proceso los datos de Google
                nombre = intent.getStringExtra("name");
                email = intent.getStringExtra("email");
                imagenUrl = intent.getStringExtra("photoUrl");
                uid = intent.getStringExtra("uidUs");
            } else { // En caso de que sea otro método de inicio de sesión
                // Lanzo un toast diciendo que ese método de inicio de sesión no está activado
                email = intent.getStringExtra("email");
                uid = intent.getStringExtra("uidUs");
                nombre = "";
                imagenUrl = "";
            }
        }

        // Compruebo la variable y lo que tengo guardo en el sharedPreferences
        if(correo.equals("nada")){ // En caso de que el correo sea igual a nada
            // Guardo y confirmo los cambios respecto al valor del email en preferencias
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("emailUsuario", email);
            editor.apply();
        }else{ // Si el correo es distinto de nada
            // Compruebo si es el mismo email o no
            if(!correo.equals(email)){ // En caso de que sea otro email diferente
                // Guardo el nuevo email en las preferencias y confirmo los cambios
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("emailUsuario", email);
                editor.apply();
            }
        }

        // Compruebo la variable y lo que tengo guardo en el sharedPreferences
        if(uIdUsuario.equals("nada")){ // En caso de que el uid sea igual a nada
            // Guardo y confirmo los cambios respecto al valor del email en preferencias
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uIdUsuario", uid);
            editor.apply();
        }else{ // Si el correo es distinto de nada
            // Compruebo si es el mismo email o no
            if(!uIdUsuario.equals(uid)){ // En caso de que sea otro uid diferente
                // Guardo el nuevo email en las preferencias y confirmo los cambios
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uIdUsuario", uid);
                editor.apply();
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // Obtengo la vista
        View headerView = navigationView.getHeaderView(0);

        // Y procedo a obtener todos los componenes necesarios para poder interactuar con la interfaz
        infoNombre = headerView.findViewById(R.id.txtUser);
        infoCorreo = headerView.findViewById(R.id.txtCorreo);
        infoUrlFoto = headerView.findViewById(R.id.imageUser);

        botonCerrarSesion = (Button) headerView.findViewById(R.id.btnLogout);
        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        // Establezco al TextView del nombre de usuario el valor del nombre
        if(!nombre.isEmpty() || !nombre.equals("")){
            infoNombre.setText(nombre);
        }

        // Compruebo el mensaje que recibo
        if(message.equals("Conectado por Facebook")){ // En caso de ser Conectado por Facebook
            // Muestro el mensaje en donde veriamos el correo
            infoCorreo.setText(message);
        }else{ // En caso de ser otro valor
            // Establezco al TextView del email de usuario el valor del email
            infoCorreo.setText(email);
        }

        obtenerDatosUsuario(uid);

        // Comprobamos que dentro de la variable que contiene la url de la foto de perfil haya algo
        if ((imagenUrl != null || !imagenUrl.isEmpty() || !imagenUrl.equals("")) && !message.equals("Conectado por otro método") && base64Imagen.isEmpty()) {
            // En caso afirmativo, procedo a cargar con la libreria Picasso la imagen
            Picasso.get().load(imagenUrl).into(infoUrlFoto);
        }else{
            obtenerDatosUsuario(uid);
        }

        // Configurar el NavigationController
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @SuppressLint("Range")
    public void obtenerDatosUsuario(String uid) {
        Cursor cursor = userdb.obtenerUsuarioPorUid(uid);

        if (cursor != null && cursor.moveToFirst()) {
            base64Imagen = cursor.isNull(cursor.getColumnIndex("imagen")) ? "" : cursor.getString(cursor.getColumnIndex("imagen"));
            if (!base64Imagen.isEmpty()) {
                Bitmap imagenPerfil = convertirBase64ABitmap(base64Imagen);
                if (imagenPerfil != null) {
                    //infoUrlFoto.setImageBitmap(imagenPerfil);
                    Glide.with(this)
                            .load(imagenPerfil)  // Carga directa del bitmap
                            .override(300, 300)  // Reducir tamaño de la imagen cargada
                            .centerCrop()  // Recortar la imagen para ajustarla mejor
                            .into(infoUrlFoto);
                } else {
                    Log.e("Imagen", "No se pudo convertir la imagen");
                }
            } else {
                Log.d("Imagen", "No hay imagen guardada en la BD");
            }
            nombreUsuario = cursor.isNull(cursor.getColumnIndex("displayName")) ? "" : cursor.getString(cursor.getColumnIndex("displayName"));
            infoNombre.setText(nombreUsuario);
        } else {
            Log.d("Usuario", "No se encontró el usuario con UID: " + uid);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private Bitmap convertirBase64ABitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Manejar eventos de clic en el menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // Obtenemos el ID del elemento seleccionado

        if (id == R.id.action_settings) {
            // Acción para "Settings"
            Toast.makeText(this, "Settings seleccionados", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_editUser) {
            // Acción para "Edit User"
            Intent intent = new Intent(MainActivity.this, EditarPerfil.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item); // Para manejar otros casos
    }

    // Método para obtener la lista de películas
    public List<Movie> getMovieList() {
        return movieList;
    }

    // Método para verificar si los datos han cambiado
    public boolean isDatosActualizados() {
        return datosActualizados;
    }

    /**
     * Método para cerrar la sesión de manera definitiva en la app, usamos este método para que cuando cerremos
     * la sesión y volvamos a la actividad de Login aunque volvamos a pulsar el botón de iniciar con Google,
     * que nos vuelva a dejar elegir la cuenta de Google con la que queremos iniciar sesión*/
    private void signOut() {
        registerUserLogout();
        // Compruebo si el mensaje es que hemos conectado por facebook o por google
        if (message.equals("Conectado por Facebook")) { // En caso de habernos conectado por Facebook
            // Cerramos la sesión de Facebook
            LoginManager.getInstance().logOut();
            // Lanzamos un Toast diciendo que hemos cerrado la sesión de la cuenta de Facebook
            showToast("Sesión cerrada con Facebook");
        } else if (message.equals("Conectado por Google")) { // En caso de habernos conectado por Google
            // Establecemos que el cliente del objeto de GoogleSingIn también cierre la sesión
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
            // Lanzamos un Toast diciendo que hemos cerrado la sesión de la cuenta de Google
            showToast("Sesión cerrada con Google");
        }

        // Utilizamos el submetodo de auth firebase para cerrar la sesión
        auth.signOut();

        //sincronizacionUser.registrarLogout(uIdUsuario);
        //userdb.actualizarLogout(uIdUsuario);
        //showToast("Logout Actualizado: "+userdb.obtenerTiempoActual());

        // Creamos un nuevo Intent para redirigir el usuario a la actividad de Inicio
        Intent intent = new Intent(MainActivity.this, Inicio.class);
        // Iniciamos el intent, iniciando así la nueva actividad
        startActivity(intent);
        // Finalizamos la actividad actual
        finish();
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Método en el que primero que todo obtengo el uid del usuario registrado
     * compruebo que el uid sea bien y no este vacio, una vez comprobado, procedo
     * a insertar el registro del logout tanto en la base de datos local y en la
     * nube, y además en las preferencias de ususario establezco que el usuario
     * no está logeado actualmente*/
    private void registerUserLogout() {
        // Compruebo si el uid es correcto y no está vacio
        if (auth.getCurrentUser().getUid() != null) { // En caso de que el uide esté bien
            // Registro el logout en la base de datos local
            userdb.actualizarLogout(uid);
            // Sincronizo el nuevo logout con la base de datos de Firestore
            sincronizacionUser.registrarLogout(uid);
            // Establezco en las preferencias el editor para cambiar cosas
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // En el editor establezco que el ususario no tiene la sesión iniciada
            editor.putBoolean("is_logged_in", false);
            // Establezco los cambios en las preferencias
            editor.apply();
            // En el LogCat imprimo que se ha registrado el logout en la bd
            Log.d("AppLifecycleManager", "Logout registrado en la base de datos.");
        }
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo*/
    public void showToast(String mensaje){
        if (this != null){
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
}