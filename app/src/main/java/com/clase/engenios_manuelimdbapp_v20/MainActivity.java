package com.clase.engenios_manuelimdbapp_v20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.clase.engenios_manuelimdbapp_v20.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtengo la instancia de la autentificación de firebase
        auth = FirebaseAuth.getInstance();

        // Obtengo las preferencias del usuario
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Cargo en la variable que he creado el email del usuario y en caso de que no exista ningun registro ponemos el valor de nada
        correo = sharedPreferences.getString("emailUsuario", "nada");

        // Creo un intent que lo que hace es obtener lo que le enviamos desde el login (siempre ha de recibir algo)
        Intent intent = getIntent();
        // Creamos una variable de tipo nombre donde cargamos el nombre del usuario
        nombre = intent.getStringExtra("name");
        // Creamos una variable de tipo email donde cargamos el email del usuario
        email = intent.getStringExtra("email");
        // Creamos una variable de tipo url de la foto de perfil donde cargamos la url del usuario
        imagenUrl = intent.getStringExtra("photoUrl");

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
        infoNombre.setText(nombre);
        // Establezco al TextView del email de usuario el valor del email
        infoCorreo.setText(email);

        // Comprobamos que dentro de la variable que contiene la url de la foto de perfil haya algo
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            // En caso afirmativo, procedo a cargar con la libreria Picasso la imagen
            Picasso.get().load(imagenUrl).into(infoUrlFoto);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Método para cerrar la sesión de manera definitiva en la app, usamos este método para que cuando cerremos
     * la sesión y volvamos a la actividad de Login aunque volvamos a pulsar el botón de iniciar con Google,
     * que nos vuelva a dejar elegir la cuenta de Google con la que queremos iniciar sesión*/
    private void signOut() {
        // Utilizamos el submetodo de auth firebase para cerrar la sesión
        auth.signOut();

        // Establecemos que el cliente del objeto de GoogleSingIn también cierre la sesión
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

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
}