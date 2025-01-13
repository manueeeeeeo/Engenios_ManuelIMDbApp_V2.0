package com.clase.engenios_manuelimdbapp_v20.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Manuel
 * @version 1.0*/

public class Movie implements Parcelable {
    // Declaro todas las variables necesarias
    private String id; // Id de la película
    private String title; // Titulo de la película
    private String originalTitle; // Título original de la película
    private String posterPath; // Url de la portada de la película
    private String releaseDate; // Fecha de publicación de la película
    private String descripcion; // Descripción de la película
    private String valoracion; // Valoración de la película

    /**
     * Constructor vacio*/
    public Movie() {
    }

    /**
     * @param releaseDate
     * @param imageUrl
     * @param id
     * @param title
     * @param originalTitle
     * Constructor con todas las variables de la clase*/
    public Movie(String id, String title, String originalTitle, String imageUrl, String releaseDate) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.posterPath = imageUrl;
        this.releaseDate = releaseDate;
    }

    /**
     * @param in
     * Clase protegida que nos permite leer el objeto parceado*/
    protected Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        originalTitle = in.readString();
        posterPath = in.readString();
        releaseDate = in.readString();
        descripcion = in.readString();
        valoracion = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    /**
     * @return
     * Método para obtener el valor del id*/
    public String getId() {
        return id;
    }

    /**
     * @param id
     * Método para establecer el valor del id*/
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return
     * Método para obtener el título*/
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     * Método para establecer el valor del título*/
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return
     * Método para obtener el título original*/
    public String getOriginalTitle() {
        return originalTitle;
    }

    /**
     * @param originalTitle
     * Método para establecer el valor del título original*/
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    /**
     * @return
     * Método para obtener la url de la portada*/
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * @param posterPath
     * Método para establecer el valor de la url de la portada*/
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * @return
     * Método para obtener el valor de la fecha de publicación*/
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param releaseDate
     * Método para establecer el valor a la fecha de publicación*/
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * @return
     * Método para obtener la descripción*/
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @param descripcion
     * Método para establecer el valor de la descripción*/
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * @return
     * Método para obtener el valor de la valoración*/
    public String getValoracion() {
        return valoracion;
    }

    /**
     * @param valoracion
     * Método para establecer el valor de la valoración*/
    public void setValoracion(String valoracion) {
        this.valoracion = valoracion;
    }

    /**
     * @param node
     * Método para actualizar los valores de la clase de tipo Movie basandonos en la información
     * que obtenemos de la clase de tipo PopularMovieResponse, para posibles actualizaciones de datos
     * de la clase Movie*/
    public void updateFromNode(PopularMovieResponse.Node node) {
        this.title = node.getTitleText().getText();
        this.originalTitle = node.getTitleText().getText();
        this.posterPath = node.getPrimaryImage().getUrl();
        this.releaseDate = String.valueOf(node.getReleaseDate().getYear())+"/"+String.valueOf(node.getReleaseDate().getMonth())+"/"+
                node.getReleaseDate().getDay();
    }

    /**
     * @return
     * Método de los objetos parceables que nos permite describir que tipo de contenido
     * vamos a escribir y pasar en un parceable*/
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @param dest
     * @param flags
     * Método para poder escribir el objeto parceado*/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(releaseDate);
        dest.writeString(descripcion);
        dest.writeString(valoracion);
    }
}