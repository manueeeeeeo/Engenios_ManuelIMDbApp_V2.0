package com.clase.engenios_manuelimdbapp_v20.api;

import com.clase.engenios_manuelimdbapp_v20.models.MovieOverviewResponse;
import com.clase.engenios_manuelimdbapp_v20.models.PopularMovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Manuel
 * @version 1.0*/

public interface IMDBApiService {
    // Consulta a la API para obtener el top 10 de peliculas y series
    @GET("title/get-top-meter")
    Call<PopularMovieResponse> getTopMeterTitles(@Query("Country") String idioma); // Le paso de parametro el idioma en el que lo quiero

    // Consulta a la API para obtener toda la información de una pelicula o serie
    @GET("title/get-overview")
    Call<MovieOverviewResponse> getMovieOverview(@Query("tconst") String movieId); // Le paso como parametro el id de la película
}