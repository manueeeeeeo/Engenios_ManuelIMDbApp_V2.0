package com.clase.engenios_manuelimdbapp_v20.sync;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UsersSync {
    private FirebaseFirestore db = null;
    private CollectionReference usersCollection = null;

    public UsersSync() {
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");
    }

    public void agregarOActualizarUsuario(String uid, String name, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", name);
        userData.put("email", email);
        userData.put("user_uid", uid);
        usersCollection.document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Log exitoso
                    System.out.println("Usuario agregado/actualizado exitosamente.");
                })
                .addOnFailureListener(e -> {
                    // Manejar el error
                    System.err.println("Error al agregar/actualizar usuario: " + e.getMessage());
                });
    }

    public void registrarEvento(String uid, String eventType) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", eventType);
        eventData.put("timestamp", obtenerTiempoActual());

        usersCollection.document(uid).collection("events").add(eventData)
                .addOnSuccessListener(documentReference -> System.out.println(eventType + " registrado correctamente."))
                .addOnFailureListener(e -> System.err.println("Error al registrar " + eventType + ": " + e.getMessage()));
    }

    public void registrarLogin(String uid) {
        registrarEvento(uid, "login");
    }

    public void registrarLogout(String uid) {
        registrarEvento(uid, "logout");
    }

    private String obtenerTiempoActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}