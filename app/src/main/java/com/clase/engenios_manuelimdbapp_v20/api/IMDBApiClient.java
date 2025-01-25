package com.clase.engenios_manuelimdbapp_v20.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Manuel
 * @version 1.0*/

public class IMDBApiClient {
    private static final String BASE_URL = "imdb-com.p.rapidapi.com";
    private static IMDBApiService apiService;
    private static RapidApiKeyManager rapidApiKeyManager = new RapidApiKeyManager();

    public static String getApiKey(){
        return rapidApiKeyManager.getCurrentKey();
    }

    public static void switchApiKey(){
        rapidApiKeyManager.switchToNextKey();
        apiService = null;
    }
}