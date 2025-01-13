package com.clase.engenios_manuelimdbapp_v20.models;

import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class TMDBMovie {
    private int page; // Página actual de la respuesta
    private int totalResults; // Número total de resultados
    private int totalPages; // Número total de páginas
    private List<TMDBMovie> results; // Lista de películas

    // Nuevas variables que queremos agregar
    private String title; // Título de la película
    private String poster_path; // URL de la imagen de portada
    private String overview; // Descripción de la película
    private String release_date; // Fecha de lanzamiento
    private String vote_average; // Valoración de la película
    private String original_title; // Título original de la película
    private String id; // Id de la película

    /**
     * @return
     * Método para obtener el Títul*/
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     * Método para establecer el Título*/
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return
     * Método para obtener el id*/
    public String getId() {
        return id;
    }

    /**
     * @param id
     * Método para establecer el id*/
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return
     * Método para obtener el título original*/
    public String getOriginal_title() {
        return original_title;
    }

    /**
     * @param original_title
     * Método para establecer el título original*/
    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    /**
     * @return
     * Método para obtener la url de la portada*/
    public String getPoster_path() {
        return poster_path;
    }

    /**
     * @param poster_path
     * Método para establecer la url de la portada*/
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    /**
     * @return
     * Método para obtener la descripción*/
    public String getOverview() {
        return overview;
    }

    /**
     * @param overview
     * Método para establecer la descripción*/
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * @return
     * Método para obtener a fecha de publicación*/
    public String getRelease_date() {
        return release_date;
    }

    /**
     * @param release_date
     * Método para establecer la fecha de publiación*/
    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    /**
     * @return
     * Método para obtener la valoración*/
    public String getVote_average() {
        return vote_average;
    }

    /**
     * @param vote_average
     * Método para establecer la valoración*/
    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    /**
     * @return
     * Método para obtener la página*/
    public int getPage() {
        return page;
    }

    /**
     * @param page
     * Método para establecer la página*/
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return
     * Método para obtener el número total de resultados*/
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * @param totalResults
     * Método para establecer el número total de resultados*/
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * @return
     * Método para obtener el número total de páginas*/
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     * Método para establecer el número total de páginas*/
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * @return
     * Método para obtener la lista de resultados*/
    public List<TMDBMovie> getResults() {
        return results;
    }

    /**
     * @param results
     * Método para establecer la lista de resultados*/
    public void setResults(List<TMDBMovie> results) {
        this.results = results;
    }
}