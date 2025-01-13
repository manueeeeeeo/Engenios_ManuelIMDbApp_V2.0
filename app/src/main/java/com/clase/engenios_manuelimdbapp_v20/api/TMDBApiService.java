package com.clase.engenios_manuelimdbapp_v20.api;

import com.clase.engenios_manuelimdbapp_v20.models.TMDBMovie;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Manuel
 * @version 1.0*/

public interface TMDBApiService {
    // Consulta a la API para obtener todos los géneros existentes en la API
    @GET("genre/movie/list")
    Call<JsonObject> getMovieGenres(@Query("api_key") String apiKey, @Query("language") String lenguaje); // Le paso de parametro la key y el lenguaje

    // Consulta a la API para obtener todas las películas aplicando en filtro de un género y un año
    @GET("discover/movie")
    Call<TMDBMovie> searchMoviesByGenreAndYear(
            @Query("api_key") String apiKey, // Paso como parametro la key
            @Query("language") String lenguaje, // Paso como parametro el lenguaje
            @Query("with_genres") String genreId, // Paso como parametro el id del género seleccionado
            @Query("primary_release_year") String year // Paso como parametro el año seleccionado
    );
}