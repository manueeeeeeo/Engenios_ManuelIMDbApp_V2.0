package com.clase.engenios_manuelimdbapp_v20.api;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class RapidApiKeyManager {
    // Variables necesarias para la clase
    private List<String> apiKeys = new ArrayList<>(); // Una lista de tipo string en donde almacenaré todas las keys
    private int currentKeyIndex = 0; // Variable de tipo int para saber en que posición de la lista estoy

    /**
     * Constructor de la clase en donde lo que hago
     * es agregar a la lista de las apiKeys todas aquellas
     * de las que dispongo*/
    public RapidApiKeyManager() {
        // Agrego aquí tus claves de RapidAPI
        apiKeys.add("45157d396bmshf14702227e85da3p1ff7a9jsna860971b474a");
        apiKeys.add("6d380b1d6cmsh63788105a9b29dfp1a9da1jsnc6a1d0b0947d");
        apiKeys.add("2f2412d974msh6d1124b73789023p1ca9dfjsn6e660867f17c");
        apiKeys.add("2d4355b846mshc466fa63c246723p12a355jsnc0a3ef230019");
    }

    /**
     * @return
     * Método en el que obtengo la key en la
     * posición actual*/
    public String getCurrentKey() {
        // Compruebo si está vacia la key
        if (apiKeys.isEmpty()) { // Si está vacia
            // Lanzo una excepción indicnado que no hay APIS configuradas
            throw new IllegalStateException("No hay claves API configuradas.");
        }
        // Devuelvo la key de la api en ese momento
        return apiKeys.get(currentKeyIndex);
    }

    /**
     * Método en el que voy cambiando las keys de las apis
     * hasta encontrar una que valga,a demás voy ciclando entre las
     * keys para en caso de que se acaben volver al principio*/
    public void switchToNextKey() {
        // Compruebo si la api está vacia
        if (apiKeys.isEmpty()) { // En caso de que este vacia
            // Lanzo una excepción indicnado que no hay APIS configuradas
            throw new IllegalStateException("No hay claves API configuradas.");
        }
        // Obtengo la nueva key de la api ciclando entre las claves
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
    }
}