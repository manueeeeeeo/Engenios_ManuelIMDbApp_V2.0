package com.clase.engenios_manuelimdbapp_v20;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private String nombre = ""; // Variable donde cargaré el nombre de usuario
    private String correo = null; // Variable donde cargaré le email de usuario
    private String imagenUrl = null; // Variable donde cargaré la url de la foto de perfil de usuario
    private String uIdUsuario = null; // Variable para cargar y manejar el uid del usuario
    private SharedPreferences sharedPreferences = null; // Variable para manejar las preferencias del usuario y guardar posibles datos
    private TextView infoCorreo = null; // Variable para manejar el textview del correo del usuario
    private TextView infoNombre = null; // Variables para manejar el textview del nombre del usuario
    private ImageView infoUrlFoto = null; // Variable para manejar la imageview de la foto de perfil del usuario
    private String email = ""; // Variable para manejar el email que obtenemos del anterior Intent
    private Toast mensajeToast = null; // Variable para manejar los Toast de está actividad
    private String message = ""; // Variable para almacenar el mensaje que recibo del otro Intent y así inicio sesión y cierro
    private String uid = ""; // Variable para almacenar y manejar el uid del usuario con sesion iniciada
    private List<Movie> movieList = new ArrayList<>(); // Lista compartida
    private boolean datosActualizados = false; // Indicador de cambio de datos
    private DatabaseUsers userdb = null; // Variable para manejar la base de datos local
    private UsersSync sincronizacionUser = null; // Variable para manejar la base de datos en la nube
    private String base64Imagen = ""; // Variable para manejar la imagen que tenemos en la bd local
    private String nombreUsuario = null; // Variable para manejar el nombre de usuario que tenemos en la local

    private boolean fotoDB = false; // Variable para manejar si tenemos o no tenemos un foto en la bd¡

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

        // Inicializo la base de datos local
        userdb = new DatabaseUsers(this);

        // Inicializo la base de datos en la nube
        sincronizacionUser = new UsersSync(this);

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

        // Llamar a obtenerDatosUsuario antes de intentar cargar la imagen
        obtenerDatosUsuario(uid);

        // Obtengo de la interfaz el botón de cerrar sesión
        botonCerrarSesion = (Button) headerView.findViewById(R.id.btnLogout);
        // Establezco la acción que voy a realizar al pulsar dicho botón
        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para cerrar sesion
                signOut();
            }
        });

        // Establezco al TextView del nombre de usuario el valor del nombre
        if(!nombre.isEmpty() || !nombre.equals("")){
            if(!nombreUsuario.isEmpty() || !nombreUsuario.equals("")){
                infoNombre.setText(nombreUsuario);
            }else{
                infoNombre.setText(nombre);
            }
        }

        // Compruebo el mensaje que recibo
        if(message.equals("Conectado por Facebook")){ // En caso de ser Conectado por Facebook
            // Muestro el mensaje en donde veriamos el correo
            infoCorreo.setText(message);
        }else{ // En caso de ser otro valor
            // Establezco al TextView del email de usuario el valor del email
            infoCorreo.setText(email);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Compruebo que el booleano de foto en la bd sea verdadero y que la variable tenga algo
            if(fotoDB && !base64Imagen.isEmpty()){
                // Compruebo que tipo de imagen es
                if (base64Imagen.startsWith("http")) { // Si es una URL
                    Glide.with(this)
                            .load(base64Imagen)
                            .override(300, 300)
                            .centerCrop()
                            .into(infoUrlFoto);
                } else { // Si es un Bitmap en Base64
                    Bitmap imagenPerfil = convertirBase64ABitmap(base64Imagen);
                    if (imagenPerfil != null) {
                        Glide.with(this)
                                .load(imagenPerfil)
                                .override(300, 300)
                                .centerCrop()
                                .into(infoUrlFoto);
                    }
                }
            }else{ // En caso de que falle alguna de las condiciones del principio
                // Cargo la imagen con Picasso
                Picasso.get().load(imagenUrl).into(infoUrlFoto);
            }
        }, 500); // Pequeño retraso para garantizar la carga de la imagen de la BD

        // Compruebo si existe algun usuario autentificado
        if (auth.getCurrentUser() != null) { // De serlo así
            // Obtengo el uid
            uid = auth.getCurrentUser().getUid();
            obtenerDatosUsuario(uid); // Cargo los datos desde la base de datos local
            actualizarInterfaz(); // Actualizo la interfaz
        } else {
            // Redirigo a la actividad de inicio de sesión
            Intent intent2 = new Intent(MainActivity.this, Inicio.class);
            startActivity(intent2);
            finish();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Recargo los datos en el onResumne
        obtenerDatosUsuario(uIdUsuario); // Método para obtener otra vez los datos del usuario
        actualizarInterfaz(); // Método para actualizar la interfaz con los nuevos datos
    }

    /**
     * Método al que llamo para una vez actualizado los datos
     * de la base local y de la nube, actualizar la interfaz con los nuevos datos obtenidos*/
    private void actualizarInterfaz() {
        // Actualizo el textview del nombre de usuario
        infoNombre.setText(nombreUsuario);

        // Compruebo el mensaje del principio para saber si muestro el correo o el Conectado por Facebook
        if(message.equals("Conectado por Facebook")){ // En caso de ser Conectado por Facebook
            // Muestro el mensaje en donde veriamos el correo
            infoCorreo.setText(message);
        }else{ // En caso de ser otro valor
            // Establezco al TextView del email de usuario el valor del email
            infoCorreo.setText(email);
        }

        // Compruebo que la imagen en donde guardo el valor sea diferentes de nulo
        if (!base64Imagen.isEmpty()) {
            if (base64Imagen.startsWith("http")) { // Si es una URL
                Glide.with(this)
                        .load(base64Imagen)
                        .override(300, 300)
                        .centerCrop()
                        .into(infoUrlFoto);
                fotoDB = true;
            } else { // Si es un Bitmap en Base64
                Bitmap imagenPerfil = convertirBase64ABitmap(base64Imagen);
                if (imagenPerfil != null) {
                    Glide.with(this)
                            .load(imagenPerfil)
                            .override(300, 300)
                            .centerCrop()
                            .into(infoUrlFoto);
                    fotoDB = true;
                }
            }
        } else if (imagenUrl != null && !imagenUrl.isEmpty()) { // En caso de que la imagen de la bd sea nula y tengamos una url de facebook o google
            // La cargamos con picasso
            Picasso.get().load(imagenUrl).into(infoUrlFoto);
        }
    }

    /**
     * @param uid
     * Método para obtener los datos del usuario desde la base de datos local,
     * primero que todo obtengo el cursor del usuario compruebo que no este nulo y
     * obtengo en un string el valor de la imagen pasado a texto, compruebo que no sea nulo
     * convierto el texto de base64 a bitmap y lo establezco en el elemento de la imagen, obtengo
     * obtengo todos los datos y sino pongo ""*/
    @SuppressLint("Range")
    public void obtenerDatosUsuario(String uid) {
        // Obtengo el cursor de la base de datos local
        Cursor cursor = userdb.obtenerUsuarioPorUid(uid);

        // Compruebo que el cursor no sea nulo
        if (cursor != null && cursor.moveToFirst()) { // En caso de que no sea nulo
            // Obtengo en una variable de tipo string la imagen en texto
            base64Imagen = cursor.isNull(cursor.getColumnIndex("imagen")) ? "" : cursor.getString(cursor.getColumnIndex("imagen"));
            // Compruebo si esa variable es nula o no
            if (!base64Imagen.isEmpty()) { // En caso de que la variable este rellena
                if (base64Imagen.startsWith("http")) { // Si es una URL
                    // Cargo la imagen con el glide normal
                    Glide.with(this)
                            .load(base64Imagen)
                            .override(300, 300)
                            .centerCrop()
                            .into(infoUrlFoto);
                    // Establezco la variable booleana en true
                    fotoDB = true;
                } else { // Si es un Bitmap en Base64
                    // Llamo al método para convertir el texto en base64 a bitmap
                    Bitmap imagenPerfil = convertirBase64ABitmap(base64Imagen);
                    // Cargo la imagen en el elemento con el glide
                    if (imagenPerfil != null) {
                        Glide.with(this)
                                .load(imagenPerfil)
                                .override(300, 300)
                                .centerCrop()
                                .into(infoUrlFoto);
                        // Establezco la variable booleana en true
                        fotoDB = true;
                    } else { // En caso de que la imagen pasada a bitmap sea nula
                        Log.e("Imagen", "No se pudo convertir la imagen");
                    }
                }
            } else { // En caso de que la variable sea nula
                Log.d("Imagen", "No hay imagen guardada en la BD");
            }
            // Obtengo el valor del nombre de usuario de la base de datos local
            nombreUsuario = cursor.isNull(cursor.getColumnIndex("displayName")) ? "" : cursor.getString(cursor.getColumnIndex("displayName"));
            // Establezco en el textview procedente el valor del nombre de usuario de la bd
            infoNombre.setText(nombreUsuario);
        } else {
            Log.d("Usuario", "No se encontró el usuario con UID: " + uid);
        }

        // Compruebo si el cursor sigue siendo diferente de nulo
        if (cursor != null) { // Si es así
            // Le cierro
            cursor.close();
        }
    }

    /**
     * @param base64String
     * Método en el que le paso como parametro un string
     * que es una imagen en base64 para convertirla en bitmap
     * y devolverla para poder cargarla en la imageView*/
    private Bitmap convertirBase64ABitmap(String base64String) {
        // Compruebo que el string que le paso no sea nulo
        if (base64String == null || base64String.isEmpty()) { // En caso de ser nulo
            // Devuelvo nulo
            return null;
        }
        // Utilizo un try catch para decodificar y convertir a bitmap
        try {
            // Intento decodificar los bytes del parametro pasado con el Base64 por defecto
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            // Retorno el bitmap decodificado
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
        } catch (IllegalArgumentException e) { // En caso de que surja alguna excepcion
            // Printo la excepción
            e.printStackTrace();
            // Devuelvo nulo
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

        return super.onOptionsItemSelected(item); // En caso de manejar otros casos
    }

    /**
     * Método para cerrar la sesión de manera definitiva en la app, usamos este método para que cuando cerremos
     * la sesión y volvamos a la actividad de Login aunque volvamos a pulsar el botón de iniciar con Google,
     * que nos vuelva a dejar elegir la cuenta de Google con la que queremos iniciar sesión*/
    private void signOut() {
        // Registro el logout
        registerUserLogout();

        // Compruebo que haya algun usuario autenticado en la actividad
        if (auth.getCurrentUser().getUid() != null){ // De ser así
            // Cierro sesion en google
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
            // Cierro la sesion en facebook
            LoginManager.getInstance().logOut();
            showToast("Sesión cerrada");
        }

        // Utilizamos el submetodo de auth firebase para cerrar la sesión
        auth.signOut();

        // Limpio todos los datos de las preferencias
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Verificamos si la sesión se cerró correctamente
        if (auth.getCurrentUser() == null) {
            // Si no hay un usuario actual, redirigimos al Inicio
            Intent intent = new Intent(MainActivity.this, Inicio.class);
            startActivity(intent);
            finish();
        } else {
            // Si la sesión no se cerró correctamente, mostramos un mensaje de error
            showToast("No se pudo cerrar sesión correctamente.");
        }
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