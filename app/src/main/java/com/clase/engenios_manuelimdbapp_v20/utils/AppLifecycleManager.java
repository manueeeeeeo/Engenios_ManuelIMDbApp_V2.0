package com.clase.engenios_manuelimdbapp_v20.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.clase.engenios_manuelimdbapp_v20.sync.UsersSync;
import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AppLifecycleManager extends Application implements Application.ActivityLifecycleCallbacks {

    private boolean isInBackground = false;  // Variable para manejar si la app está de fondo
    private int activityReferences = 0; // Variable para manejar los inicios de la apps
    private boolean isActivityChangingConfigurations = false;
    // Handler y Runnable para gestionar el cierre de sesión automático tras un tiempo de inactividad
    private Handler logoutHandler = null;
    private Runnable logoutRunnable = null;
    private static final long LOGOUT_DELAY = 1000; // Variable para el delay del logout
    private SharedPreferences preferences = null; // Variable para manejar las preferencias del usuario
    private DatabaseUsers databaseUsers = null; // Variable para la manejar la bd de los ususarios locales
    private Toast mensajeToast = null; // Variable para manejar los Toast de está actividad
    private UsersSync sincronizacionUsuarios = null; // Variable para manejar la sincronización de los usuario

    private boolean isExplicitLogout = false; // Variable para manejar los logout implicitos

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        // Inicializo las preferencias
        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        // Incilizo la base de datos locales de ususarios
        databaseUsers = new DatabaseUsers(this);
        // Inicializo la base de datos en la nube
        sincronizacionUsuarios = new UsersSync(this);
        logoutHandler = new Handler(); // Inicializo el Handler de la app
        logoutRunnable = () -> registerUserLogout(); // Llamo al método para registrar el logout del usuario

        // Verifico si el usuario está logueado al inicio
        if (preferences.getBoolean("is_logged_in", false)) { // En caso de ser así
            // Llamo al método para registrar el login del usuario
            registerUserLogin();
        }
    }

    /**
     * Método en el que primero que todo obtengo el uid del usuario registrado
     * compruebo que el uid sea bien y no este vacio, una vez comprobado, procedo
     * a insertar el registro del logout tanto en la base de datos local y en la
     * nube, y además en las preferencias de ususario establezco que el usuario
     * no está logeado actualmente*/
    private void registerUserLogout() {
        // Obtengo en una variable de tipo String el uid del usuario iniciado
        String uid = obtenerUidActual();
        // Compruebo si el uid es correcto y no está vacio
        if (uid != null) { // En caso de que el uide esté bien
            // Registro el logout en la base de datos local
            databaseUsers.actualizarLogout(uid);
            // Lanzo un Toast diciendo que el Logout se ha actualizado
            showToast("Logout Actualizado: "+databaseUsers.obtenerTiempoActual());
            // Sincronizo el nuevo logout con la base de datos de Firestore
            sincronizacionUsuarios.registrarLogout(uid);
            // Establezco en las preferencias el editor para cambiar cosas
            SharedPreferences.Editor editor = preferences.edit();
            // En el editor establezco que el ususario no tiene la sesión iniciada
            editor.putBoolean("is_logged_in", false);
            // Establezco los cambios en las preferencias
            editor.apply();
            // En el LogCat imprimo que se ha registrado el logout en la bd
            Log.d("AppLifecycleManager", "Logout registrado en la base de datos.");
        }
    }

    /**
     * Método en el que primero que todo obtengo el uid del usuario registrado
     * compruebo que el uid sea bien y no este vacio, una vez comprobado, procedo
     * a insertar el registro del login tanto en la base de datos local y en la
     * nube, y además en las preferencias de ususario establezco que el usuario
     * si está logeado actualmente*/
    private void registerUserLogin() {
        // Establezco el logout implicito como falso
        isExplicitLogout = false;
        // Obtengo en una variable de tipo String el uid del usuario iniciado
        String uid = obtenerUidActual();
        // Compruebo si el uid es correcto y no está vacio
        if (uid != null) { // En caso de que el uide esté bien
            // Registro el login en la base de datos local
            databaseUsers.actualizarLogin(uid);
            // Registo el login en la base de datos en la nube
            sincronizacionUsuarios.registrarLogin(uid);
            // Muestro el Toast diciendo que el login se ha actualizado
            showToast("Login Actualizado: "+databaseUsers.obtenerTiempoActual());
            // Establezco en las preferencias el editor para cambiar cosas
            SharedPreferences.Editor editor = preferences.edit();
            // En el editor establezco que el ususario si tiene la sesión iniciada
            editor.putBoolean("is_logged_in", true);
            // Establezco los cambios en las preferencias
            editor.apply();
            // En el LogCat imprimo que se ha registrado el logout en la bd
            Log.d("AppLifecycleManager", "Login registrado en la base de datos.");
        }
    }

    /**
     * @return
     * Método en el que lo que hago es crear una variable de tipo
     * FirebaseUser e inicializo la instancia de la autentificación
     * para obtener el usuario registrado, posteriormente, compruebo
     * si el ususario está vacio, devuelvo nulo y por otro lado
     * en caso de que si que este relleno obtengo el uid del usuario autentificado*/
    private String obtenerUidActual() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // Establezco la variable en falso de si la app está de fondo
        isInBackground = false;
        // Remuevo todas las callbacks que esten en ejecucción
        logoutHandler.removeCallbacks(logoutRunnable);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // Establezco la variable en verdadero de si la app está de fondo
        isInBackground = true;
        // Programo el cierre de sesión automático después de un retraso especificado
        logoutHandler.postDelayed(logoutRunnable, LOGOUT_DELAY);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // En caso de que no cambien las configuraciones de la actividad
        if (!isActivityChangingConfigurations) {
            // Inrcemento en uno el contador de referencias de actividades
            activityReferences++;

            // Si es el primer inicio de actividad
            if (activityReferences == 1 && !isExplicitLogout) {
                // En el Log indico que la app está en primer plano
                Log.d("AppLifecycleManager", "App entrando al primer plano, registrando login.");
                // Llamo al método para registrar el login del usuario
                registerUserLogin();
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // Resto uno a la referencia de las activdidades
        activityReferences--;
        // Establezco los cambios de las configuraciones de la actividad
        isActivityChangingConfigurations = activity.isChangingConfigurations();

        // En caso de que la app haya perdido el plano principal
        if (activityReferences == 0 && !isActivityChangingConfigurations) {
            // En caso de que no sea un logout explicito
            if (!isExplicitLogout) {
                // Imprimo por el Log que la app ha pasado a segundo plano
                Log.d("AppLifecycleManager", "App en segundo plano, registrando logout implícito.");
                // Llamo al método para registrar el logout
                registerUserLogout();
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Establezco los cambios de las configuraciones de la actividad
        isActivityChangingConfigurations = activity.isChangingConfigurations();
    }

    @Override
    public void onTrimMemory(int level) {
        // Compruebo que el nivel de recorte de la memoria indica que la UI se ha ocutlado o pasado a segundo plano
        if (level == TRIM_MEMORY_UI_HIDDEN || level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            // Registro logout si la UI se minimiza
            registerUserLogout();
        }
        super.onTrimMemory(level);
    }

    /**
     * Método para comprobar si el Logout que
     * ha sucedido es un Logout Explicito*/
    public void onExplicitLogout() {
        // Señalo la variable como que si que se que es explicito
        isExplicitLogout = true;
        // Llamo al método para registrarle también
        registerUserLogout();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // No utilizo esto
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // No utilizo esto
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