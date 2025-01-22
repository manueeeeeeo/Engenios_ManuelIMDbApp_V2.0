package com.clase.engenios_manuelimdbapp_v20.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AppLifecycleManager extends Application implements Application.ActivityLifecycleCallbacks {

    private boolean isInBackground = false;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private Handler logoutHandler;
    private Runnable logoutRunnable;
    private static final long LOGOUT_DELAY = 30000; // Ejemplo: 30 segundos
    private SharedPreferences preferences;
    private DatabaseUsers databaseUsers;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        databaseUsers = new DatabaseUsers(this); // Inicializar base de datos
        logoutHandler = new Handler();
        logoutRunnable = () -> {
            // Acción de logout automático
            registerUserLogout();
        };
    }

    private void registerUserLogout() {
        String uid = obtenerUidActual();
        if (uid != null) {
            databaseUsers.actualizarLogout(uid); // Registrar logout en la base de datos
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_logged_in", false);
            editor.apply();
            Log.d("AppLifecycleManager", "Logout registrado en la base de datos.");
        }
    }

    private void registerUserLogin() {
        String uid = obtenerUidActual();
        if (uid != null) {
            databaseUsers.actualizarLogin(uid); // Registrar login en la base de datos
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.apply();
            Log.d("AppLifecycleManager", "Login registrado en la base de datos.");
        }
    }

    private String obtenerUidActual() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        isInBackground = false;
        logoutHandler.removeCallbacks(logoutRunnable);

        // Registrar el login al reanudar la actividad
        registerUserLogin();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isInBackground = true;
        logoutHandler.postDelayed(logoutRunnable, LOGOUT_DELAY);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences++;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences--;
            if (activityReferences == 0) {
                // Si no hay actividades activas, la app está cerrada
                logoutHandler.postDelayed(logoutRunnable, LOGOUT_DELAY);
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            // Registrar logout si la UI se minimiza
            registerUserLogout();
        }
        super.onTrimMemory(level);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Implementación vacía
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Implementación vacía
    }
}