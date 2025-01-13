package com.clase.engenios_manuelimdbapp_v20.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Manuel
 * @version 1.0*/

public class MovieOverviewResponse implements Parcelable {

    @SerializedName("data")
    private Data data; // Variable que representa la key principal del JSON

    protected MovieOverviewResponse(Parcel in) {
        data = in.readParcelable(Data.class.getClassLoader());
    }

    public static final Creator<MovieOverviewResponse> CREATOR = new Creator<MovieOverviewResponse>() {
        @Override
        public MovieOverviewResponse createFromParcel(Parcel in) {
            return new MovieOverviewResponse(in);
        }

        @Override
        public MovieOverviewResponse[] newArray(int size) {
            return new MovieOverviewResponse[size];
        }
    };

    /**
     * @return
     * Método para obtener el objeto data*/
    public Data getData() {
        return data;
    }

    /**
     * @param data
     * Método para establecer el objeto data*/
    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
    }

    // Clase que he creado y utilizo para hacer referencia a Data
    public static class Data implements Parcelable {
        // Dentro de la key del JSON "data" tenemos otra que se llama title y ya dentro de está tenemos todos los datos
        @SerializedName("title")
        private Title title; // Variable para manejar el Title de la película

        public Data() {}

        protected Data(Parcel in) {
            title = in.readParcelable(Title.class.getClassLoader());
        }

        public static final Creator<Data> CREATOR = new Creator<Data>() {
            @Override
            public Data createFromParcel(Parcel in) {
                return new Data(in);
            }

            @Override
            public Data[] newArray(int size) {
                return new Data[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor del Title*/
        public Title getTitle() {
            return title;
        }

        /**
         * @param title
         * Método para establecer el valor del Title*/
        public void setTitle(Title title) {
            this.title = title;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(title, flags);
        }
    }

    public static class Title implements Parcelable {
        // Dentro de la key Title como ya he dicho tenemos toda la demás información, yo he decido crear las variables
        // solo para las que me interesa
        @SerializedName("id")
        private String id; // Variable que representa el id de la película o serie
        @SerializedName("titleText")
        private TitleText titleText; // Variable que representa la key para poder acceder al título
        @SerializedName("releaseDate")
        private ReleaseDate releaseDate; // Variable que representa la key para poder acceder a la fecha de publicación
        @SerializedName("ratingsSummary")
        private RatingsSummary ratingsSummary; // Variable que representa la key para poder acceder a la valoración de la película
        @SerializedName("plot")
        private Plot plot; // Variable que representa la key para poder acceder a la descripción de la película

        public Title() {}

        protected Title(Parcel in) {
            id = in.readString();
            titleText = in.readParcelable(TitleText.class.getClassLoader());
            releaseDate = in.readParcelable(ReleaseDate.class.getClassLoader());
            ratingsSummary = in.readParcelable(RatingsSummary.class.getClassLoader());
            plot = in.readParcelable(Plot.class.getClassLoader());
        }

        public static final Creator<Title> CREATOR = new Creator<Title>() {
            @Override
            public Title createFromParcel(Parcel in) {
                return new Title(in);
            }

            @Override
            public Title[] newArray(int size) {
                return new Title[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor del id de la película o serie*/
        public String getId() {
            return id;
        }

        /**
         * @param id
         * Método para establecer el valor del id de la película o series*/
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return
         * Método para obtener el valor del titleText*/
        public TitleText getTitleText() {
            return titleText;
        }

        /**
         * @param titleText
         * Método para establecer el valor del titleText*/
        public void setTitleText(TitleText titleText) {
            this.titleText = titleText;
        }

        /**
         * @return
         * Método para obtener el valor de la fecha de publiación*/
        public ReleaseDate getReleaseDate() {
            return releaseDate;
        }

        /**
         * @param releaseDate
         * Método para establecer el valor de la fecha de publicación*/
        public void setReleaseDate(ReleaseDate releaseDate) {
            this.releaseDate = releaseDate;
        }

        /**
         * @return
         * Método para obtener el valor del plot de la descripción*/
        public RatingsSummary getRatingsSummary() {
            return ratingsSummary;
        }

        /**
         * @param ratingsSummary
         * Método para establecer el valor del reatingsSummary*/
        public void setRatingsSummary(RatingsSummary ratingsSummary) {
            this.ratingsSummary = ratingsSummary;
        }

        /**
         * @return
         * Método para obtener el valor del plot de la descripción*/
        public Plot getPlot() {
            return plot;
        }

        /**
         * @param plot
         * Método para obtener el valor del plot de la descripción*/
        public void setPlot(Plot plot) {
            this.plot = plot;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeParcelable(titleText, flags);
            dest.writeParcelable(releaseDate, flags);
            dest.writeParcelable(ratingsSummary, flags);
            dest.writeParcelable(plot, flags);
        }
    }

    public static class TitleText implements Parcelable {
        @SerializedName("text")
        private String text;

        public TitleText() {}

        protected TitleText(Parcel in) {
            text = in.readString();
        }

        public static final Creator<TitleText> CREATOR = new Creator<TitleText>() {
            @Override
            public TitleText createFromParcel(Parcel in) {
                return new TitleText(in);
            }

            @Override
            public TitleText[] newArray(int size) {
                return new TitleText[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor del texto del título*/
        public String getText() {
            return text;
        }

        /**
         * @param text
         * Método para obtener el valor del texto del título*/
        public void setText(String text) {
            this.text = text;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
        }
    }

    public static class ReleaseDate implements Parcelable {
        @SerializedName("year")
        private int year;

        @SerializedName("month")
        private int month;

        @SerializedName("day")
        private int day;

        public ReleaseDate() {}

        protected ReleaseDate(Parcel in) {
            year = in.readInt();
            month = in.readInt();
            day = in.readInt();
        }

        public static final Creator<ReleaseDate> CREATOR = new Creator<ReleaseDate>() {
            @Override
            public ReleaseDate createFromParcel(Parcel in) {
                return new ReleaseDate(in);
            }

            @Override
            public ReleaseDate[] newArray(int size) {
                return new ReleaseDate[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor del año*/
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
         * Método para obtener el valor del mes*/
        public int getMonth() {
            return month;
        }

        /**
         * @param month
         * Método para establecer el valor del mes*/
        public void setMonth(int month) {
            this.month = month;
        }

        /**
         * @return
         * Método para obtener el valor del día*/
        public int getDay() {
            return day;
        }

        /**
         * @param day
         * Método para establecer el valor del día*/
        public void setDay(int day) {
            this.day = day;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(year);
            dest.writeInt(month);
            dest.writeInt(day);
        }
    }

    public static class RatingsSummary implements Parcelable {
        @SerializedName("aggregateRating")
        private double aggregateRating;

        public RatingsSummary() {}

        protected RatingsSummary(Parcel in) {
            aggregateRating = in.readDouble();
        }

        public static final Creator<RatingsSummary> CREATOR = new Creator<RatingsSummary>() {
            @Override
            public RatingsSummary createFromParcel(Parcel in) {
                return new RatingsSummary(in);
            }

            @Override
            public RatingsSummary[] newArray(int size) {
                return new RatingsSummary[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor de aggregateRating*/
        public double getAggregateRating() {
            return aggregateRating;
        }

        /**
         * @param aggregateRating
         * Método para establecer el valor de aggregateRating*/
        public void setAggregateRating(double aggregateRating) {
            this.aggregateRating = aggregateRating;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(aggregateRating);
        }
    }

    public static class Plot implements Parcelable {
        @SerializedName("plotText")
        private PlotText plotText;

        public Plot() {}

        protected Plot(Parcel in) {
            plotText = in.readParcelable(PlotText.class.getClassLoader());
        }

        public static final Creator<Plot> CREATOR = new Creator<Plot>() {
            @Override
            public Plot createFromParcel(Parcel in) {
                return new Plot(in);
            }

            @Override
            public Plot[] newArray(int size) {
                return new Plot[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor de plainText*/
        public PlotText getPlotText() {
            return plotText;
        }

        /**
         * @param plotText
         * Método para establecer el valor de plainText*/
        public void setPlotText(PlotText plotText) {
            this.plotText = plotText;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(plotText, flags);
        }
    }

    public static class PlotText implements Parcelable {
        @SerializedName("plainText")
        private String plainText;

        public PlotText() {}

        protected PlotText(Parcel in) {
            plainText = in.readString();
        }

        public static final Creator<PlotText> CREATOR = new Creator<PlotText>() {
            @Override
            public PlotText createFromParcel(Parcel in) {
                return new PlotText(in);
            }

            @Override
            public PlotText[] newArray(int size) {
                return new PlotText[size];
            }
        };

        /**
         * @return
         * Método para obtener el valor de plainText*/
        public String getPlainText() {
            return plainText;
        }

        /**
         * @param plainText
         * Método para establecer el valor de plainText*/
        public void setPlainText(String plainText) {
            this.plainText = plainText;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * @param flags
         * @param dest
         * Método para poder escribir el plainText siendo parcelable*/
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(plainText);
        }
    }
}