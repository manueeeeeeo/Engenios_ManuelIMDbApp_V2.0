package com.clase.engenios_manuelimdbapp_v20.api;

/**
 * @author Manuel
 * @version 1.0*/

public class IMDBApiClient {
    // Variables necesarias para la clase
    private static IMDBApiService apiService; // Referencia a la interfaz en donde tengo los endpoints de la api
    private static RapidApiKeyManager rapidApiKeyManager = new RapidApiKeyManager(); // Inicializo el manager de las keys de la api

    /**
     * @return
     * Método en el que devuelvo la key api
     * que obtengo del manage de la api*/
    public static String getApiKey(){
        // Devuelvo la key de la api actual
        return rapidApiKeyManager.getCurrentKey();
    }

    /**
     * Método en el que lo que hago es primero que todo
     * cambiar la key llamando al método manager de las keys
     * de la api y establezco como nulo la interfaz de las
     * llamadas a la api*/
    public static void switchApiKey(){
        // Llamao al método cambiar a la siguiente key e la api
        rapidApiKeyManager.switchToNextKey();
        // Establezco como nula la llamada a la interfaz de la api
        apiService = null;
    }
}