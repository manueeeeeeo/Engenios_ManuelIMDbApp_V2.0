package com.clase.engenios_manuelimdbapp_v20.api;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class RapidApiKeyManager {
    private List<String> apiKeys = new ArrayList<>();
    private int currentKeyIndex = 0;

    // Constructor que inicializa las claves API
    public RapidApiKeyManager() {
        // Agrega aquí tus claves de RapidAPI
        apiKeys.add("45157d396bmshf14702227e85da3p1ff7a9jsna860971b474a");
        apiKeys.add("2f2412d974msh6d1124b73789023p1ca9dfjsn6e660867f17c");
        apiKeys.add("");
    }

    // Obtiene la clave API actual
    public String getCurrentKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No hay claves API configuradas.");
        }
        return apiKeys.get(currentKeyIndex);
    }

    // Cambia a la siguiente clave API
    public void switchToNextKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No hay claves API configuradas.");
        }
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size(); // Cicla entre las claves
    }
}