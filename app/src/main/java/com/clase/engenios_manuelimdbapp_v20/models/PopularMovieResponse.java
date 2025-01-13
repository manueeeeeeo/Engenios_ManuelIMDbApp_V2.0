package com.clase.engenios_manuelimdbapp_v20.models;

import java.util.List;

/**
 * @author Manuel
 * @version  1.0
 * Está clase la he creado basandome en la respuesta que nos da el JSON de la API, ya que para poder obtener
 * ciertos valores debemos entrar dentro de diferentes keys por así decirlo*/

public class PopularMovieResponse {
    // Variable principal de tipo Data para poder obtener la respuesta de la API
    private Data data;

    /**
     * @return
     * Método para obtener el Data*/
    public Data getData() {
        return data;
    }

    /**
     * @param data
     * Método para darle valor al Data*/
    public void setData(Data data) {
        this.data = data;
    }

    // Clase que creo y utilizo para poder hacer referencia a Data
    public static class Data {
        // Variable que me permite obtener el top de títulos de películas y series actuales
        private TopMeterTitles topMeterTitles;

        /**
         * @return
         * Método para obtener el TopMeterTitles*/
        public TopMeterTitles getTopMeterTitles() {
            return topMeterTitles;
        }

        /**
         * @param topMeterTitles
         * Método para establecerle un valor a TopMeterTitles*/
        public void setTopMeterTitles(TopMeterTitles topMeterTitles) {
            this.topMeterTitles = topMeterTitles;
        }
    }

    // Clase que creo y utilizo para poder hacer referencia a TopMeterTitles
    public static class TopMeterTitles {
        // Lista de Edges para poder obtener el top 10
        private List<Edge> edges;

        /**
         * @return
         * Método para obtener la lista de Eges*/
        public List<Edge> getEdges() {
            return edges;
        }

        /**
         * @param edges
         * Método para darle valor a la variable de la lista de Edges*/
        public void setEdges(List<Edge> edges) {
            this.edges = edges;
        }
    }

    // Clase que creo y utilizo para poder hacer referencia a Edge
    public static class Edge {
        // Variable de tipo node para poder acceder a todos los nodos del JSON
        private Node node;

        /**
         * @return
         * Método para obtener el valor del nodo*/
        public Node getNode() {
            return node;
        }

        /**
         * @param node
         * Método para poder establecer el valor del nodo*/
        public void setNode(Node node) {
            this.node = node;
        }
    }

    // Clase que creo y utilizo para poder hacer referencia a Node, node es la clase que contiene todas las variables que obtener del JSON
    public static class Node {
        // Declaro todas las variables necesarias para la clase
        private String id; // Id de la película o serie
        private TitleText titleText; // Título de la película
        private PrimaryImage primaryImage; // Imagen de la portada
        private MeterRanking meterRanking; // Valoración en el top 10
        private ReleaseDate releaseDate; // Fecha de publicación de la película o serie

        /**
         * @return
         * Método para obtener el valor de la variable TitleText*/
        public TitleText getTitleText() {
            return titleText;
        }

        /**
         * @param titleText
         * Método para establecer el valor a la variable TitleText*/
        public void setTitleText(TitleText titleText) {
            this.titleText = titleText;
        }

        /**
         * @return
         * Método para obtener el valor de la variable PrimaryImage*/
        public PrimaryImage getPrimaryImage() {
            return primaryImage;
        }

        /**
         * @param primaryImage
         * Método para establecer el valor de la variable PrimaryImage*/
        public void setPrimaryImage(PrimaryImage primaryImage) {
            this.primaryImage = primaryImage;
        }

        /**
         * @return
         * Método para obtener el valor de la variable MeterRanking*/
        public MeterRanking getMeterRanking() {
            return meterRanking;
        }

        /**
         * @param meterRanking
         * Método para establecer el valor de la variable MeterRanking*/
        public void setMeterRanking(MeterRanking meterRanking) {
            this.meterRanking = meterRanking;
        }

        /**
         * @return
         * Método para obtener el valor de la variable ReleaseDate*/
        public ReleaseDate getReleaseDate() {
            return releaseDate;
        }

        /**
         * @param releaseDate
         * Método para establecer el valor de la variable RelesaeDate*/
        public void setReleaseDate(ReleaseDate releaseDate) {
            this.releaseDate = releaseDate;
        }

        /**
         * @return
         * Método para obtener el valor de Id*/
        public String getId() {
            return id;
        }

        /**
         * @param id
         * Método para establecer el valor de Id*/
        public void setId(String id) {
            this.id = id;
        }
    }

    // Clase que he creado y utilizo para poder acceder y leer el título
    public static class TitleText {
        // Variable que contiene el título de la película o serie
        private String text;

        /**
         * @return
         * Método para obtener el valor del título*/
        public String getText() {
            return text;
        }

        /**
         * @param text
         * Método para establecer el valor del título*/
        public void setText(String text) {
            this.text = text;
        }
    }

    // Clase que he creado y utilizo para poder acceder y leer la url de la imagen de portada
    public static class PrimaryImage {
        // Variable que contiene la url de la portada
        private String url;

        /**
         * @return
         * Método para poder obtener el valor de la url*/
        public String getUrl() {
            return url;
        }

        /**
         * @param url
         * Método para establecer el valor de la url*/
        public void setUrl(String url) {
            this.url = url;
        }
    }

    // Clase que he creado y utilizo para poder acceder y leer la califciación en el top 10 de la película o serie
    public static class MeterRanking {
        // Variabla para la calificación
        private int currentRank;

        /**
         * @return
         * Método para poder obtener el valor del currentRank*/
        public int getCurrentRank() {
            return currentRank;
        }

        /**
         * @param currentRank
         * Método para poder establecer el valor del currentRank*/
        public void setCurrentRank(int currentRank) {
            this.currentRank = currentRank;
        }
    }

    // Clase que he creado y utilizo para poder acceder y leer la fecha de publicación
    public static class ReleaseDate {
        private int year; // Variable del año
        private int month; // Variable del mes
        private int day; // Variable del día

        /**
         * @return
         * Método para obtener el año*/
        public int getYear() {
            return year;
        }

        /**
         * @param year
         * Método para establecer el valor del año*/
        public void setYear(int year) {
            this.year = year;
        }

        /**
         * @return
         * Método para obtener el mes*/
        public int getMonth() {
            return month;
        }

        /**
         * @param month
         * Método apra establecer el valor del mes*/
        public void setMonth(int month) {
            this.month = month;
        }

        /**
         * @return
         * Método para obtener el dia*/
        public int getDay() {
            return day;
        }

        /**
         * @param day
         * Método para establecer el valor del día*/
        public void setDay(int day) {
            this.day = day;
        }

        /**
         * Método toString para darle formato a como se veria al imprimir la clase*/
        @Override
        public String toString() {
            return year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day); // Formato YYYY-MM-DD
        }
    }
}