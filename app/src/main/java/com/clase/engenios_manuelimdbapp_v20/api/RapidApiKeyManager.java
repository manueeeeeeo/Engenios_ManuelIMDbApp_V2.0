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
        apiKeys.add("6d380b1d6cmsh63788105a9b29dfp1a9da1jsnc6a1d0b0947d");
        apiKeys.add("2f2412d974msh6d1124b73789023p1ca9dfjsn6e660867f17c");
        apiKeys.add("2d4355b846mshc466fa63c246723p12a355jsnc0a3ef230019");
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