package com.clase.engenios_manuelimdbapp_v20.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IMDBApiClient {
    private static final String BASE_URL = "imdb-com.p.rapidapi.com";
    private static IMDBApiService apiService;
    private static RapidApiKeyManager rapidApiKeyManager = new RapidApiKeyManager();

    public static IMDBApiService getApiService(){
        if(apiService == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(IMDBApiService.class);
        }
        return apiService;
    }

    public static String getApiKey(){
        return rapidApiKeyManager.getCurrentKey();
    }

    public static void switchApiKey(){
        rapidApiKeyManager.switchToNextKey();
    }
}