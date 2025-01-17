package com.clase.engenios_manuelimdbapp_v20.models;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class FavoriteMoviesDatabase extends SQLiteOpenHelper {
    // Declaro todas las variables necesarias para la clase
    private static final String DATABASE_NAME = "favorites.db"; // Nombre de la base de datos
    private static final int DATABASE_VERSION = 5; // Versión de la base de datos
    private static final String TABLE_NAME = "favorites"; // Nombre de la tabla de la base de datos

    private static final String COLUMN_ID = "id"; // Identificador de cada registro
    private static final String COLUMN_MOVIE_ID = "movie_id"; // Id de la película o serie agregada
    private static final String COLUMN_IMAGE_URL = "image_url"; // Url de la portada de la película o serie agredada
    private static final String COLUMN_TITLE_MOVIE = "movie_title"; // Título de la película o serie
    private static final String COLUMN_DATE_RELEASE = "date_release"; // Fecha de publicación de la película o serie
    private static final String COLUMN_DESCRIP_MOVIE = "movie_descrip"; // Título de la película o serie
    private static final String COLUMN_VALORA_MOVIE = "movi_rating"; // Fecha de publicación de la película o serie
    private static final String COLUMN_EMAIL_USER = "user_email"; // Correo con el que relaciona la lista de favoritas

    /**
     * @param context
     * Constructor al que le pasamos el contexto de la actividad o fragmento desde
     * el que llamamos a la clase*/
    public FavoriteMoviesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * @param db
     * En este método onCreate lo que estamos haciendo es que la primera vez que se inicialice
     * la bd de sqlite, generamos un string que viene siendo una consulta en donde creamos una nueva
     * tabla dandole el nombre de la tabla, todas las columnas y el tipo de dato que contiene cada una,
     * posteriormente, utilizamos el parametro de db que le pasamos para ejecutar una sentencia de SQL
     * que viene siendo la consulta que tenmos desarrollada en el String*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Guardamos en una variable la consulta que es la creación de una nueva tabla con todos sus datos
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MOVIE_ID + " TEXT NOT NULL, "
                + COLUMN_IMAGE_URL + " TEXT, "
                +COLUMN_TITLE_MOVIE +" TEXT, "
                +COLUMN_DATE_RELEASE +" TEXT, "
                +COLUMN_DESCRIP_MOVIE +" TEXT, "
                +COLUMN_VALORA_MOVIE +" TEXT, "
                +COLUMN_EMAIL_USER + " TEXT NOT NULL "
                + ")";
        // Procedemos a ejecutar la sentencia SQL
        db.execSQL(CREATE_TABLE);
    }

    /**
     * @param db
     * @param newVersion
     * @param oldVersion
     * En este método onUpgrade lo que hago es ya que he ido creando la bd poco a poco y me faltaban datos
     * y columnas por agregar lo que hago esque si existe algun dispisitivo con la versión de la bd anterior a la 2
     * es decir, la vesión 1, procedemo a ejecutar una sentencia SQL en donde agrego una nueva columna que es la relacionada
     * con con el título de la película, posteriormente, si la versión es más antigua que la 3, procedo a insertar
     * una nueva columna que esta vez esta relacionada con la fecha de la película o serie*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Si la base de datos está en la versión 1, agrego la columna COLUMN_TITLE_MOVIE
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_TITLE_MOVIE + " TEXT");
        }
        if(oldVersion<3){
            // Si la base de datos está en la versión 2, agrega la columna COLUMN_DATE_RELEASE
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_DATE_RELEASE + " TEXT");
        }
        if(oldVersion<4){
            // Si la base de datos está en la versión 3, agrega la columna COLUMN_DESCRIP_MOVIE y COLUMN_VALORA_MOVIE
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_DESCRIP_MOVIE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_VALORA_MOVIE + " TEXT");
        }
        if (oldVersion < 5) {
            // Si la base de datos está en la versión4, agrega la columna COLUMN_EMAIL_USER
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_EMAIL_USER + " TEXT NOT NULL");
        }
    }

    /**
     * @param movieId
     * @param imageUrl
     * Método en el que utilizamos otra vez el objeto SQLiteDatabase para poder escribir en la BBDD
     * posteriormente en un objeto de tipo ContentValues establecemos todas las columnas en las que vamos a insertar,
     * seguido de los valores que vamos a insertar y por ultimo utilizamos el submetodo de SQLiteDatabase para
     * poder insertar en la BBDD*/
    public long insertarFavorita(String movieId, String imageUrl, String movieTi, String date, String descrip, String valor, String userEmail) {
        // Utilizo el objeto SQLiteDatabase para poder acceder a escribir en la BBDD
        SQLiteDatabase db = this.getWritableDatabase();
        // Utilizo el ContentValues para establecer los valores y columnas que voy a ejecutar en la insercción
        ContentValues values = new ContentValues();
        // Establezco la columna en la que va a ir el ID de la película y el valor que va a tener
        values.put(COLUMN_MOVIE_ID, movieId);
        // Establezco la columna en la que va a ir la url de la portada de la película y el valor que va a tener
        values.put(COLUMN_IMAGE_URL, imageUrl);
        // Establezco la columna en la que va a ir el titulo de la película y el valor que va a tener
        values.put(COLUMN_TITLE_MOVIE, movieTi);
        // Establezco la columna en la que va a ir la fecha de publicación de la película y el valor que va a tener
        values.put(COLUMN_DATE_RELEASE, date);
        // Establezco la columna en la que va a ir la descripción de la película y el valor que va a tener
        values.put(COLUMN_DESCRIP_MOVIE, descrip);
        // Establezco la columna en la que va a ir la valoración de la película y el valor que va a tener
        values.put(COLUMN_VALORA_MOVIE, valor);
        // Establezco la columna en la que va a ir el correo del usuario y el valor que va a tener
        values.put(COLUMN_EMAIL_USER, userEmail);

        // Procdeo a insertar en la tabla que le indico todos los valores que deseo
        return db.insert(TABLE_NAME, null, values);
    }

    /**
     * @return
     * Método en el que lo que hago es crear una lista de películas que serán las favoritas del usuario
     * y posteriormente uso el objeto SQliteDatabase para poder acceder a la misma, y usando un cursor
     * recorremos todos los registros de la tabla que le indicamos creando para cada registro un nuevo objeto de tipo
     * Movie en donde guardamos los parametros que hay en cada registro haciendo referencia a cada columna, por último
     * agregamos estos objetos a la lista y una vez cerrado el cursor y la conexión a la BBDD devolvemos la lista que creamos
     * antes*/
    public List<Movie> obtenerTodasLasFavoritas(String userEmail) {
        // Creo una lista de objetos de tipo Movie que representará todas las películas favoritas del usuario
        List<Movie> favoriteMovies = new ArrayList<>();
        // Utilizo el siguiente objeto para poder tener acceso a la lectura de la BBDD
        SQLiteDatabase db = this.getReadableDatabase();

        // Filtro por correo del usuario
        String selection = COLUMN_EMAIL_USER + " = ?";
        String[] selectionArgs = {userEmail};

        // Utilizo  un cursor para poder hacer referencia a la tabla y otros posibles campos para obtener todos los registros
        // dentro de la BBDD que sean del usuario registrado con el correo que pasamos por parametro
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // Comprobamos si el curosr está vacio y si puede moverse al primer registro
        if (cursor != null && cursor.moveToFirst()) { // En caso de ser así
            do {
                // Utilizo el cursor para obtener un objeto de tipo string dando indicación de la columna del que lo tiene que coger
                @SuppressLint("Range") String movieId = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_ID));
                // Utilizo el cursor para obtener un objeto de tipo string dando indicación de la columna del que lo tiene que coger
                @SuppressLint("Range") String imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
                // Utilizo el cursor para obtener un objeto de tipo string dando indicación de la columna del que lo tiene que coger
                @SuppressLint("Range") String movieTi = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE_MOVIE));
                // Utilizo el cursor para obtener un objeto de tipo string dando indicación de la columna del que lo tiene que coger
                @SuppressLint("Range") String movieDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE_RELEASE));
                // Utilizo el cursor para obtener un objeto de tipo string dando indicación de la columna del que lo tiene que coger
                @SuppressLint("Range") String movieDe = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIP_MOVIE));
                // Utilizo el cursor para obtener un objeto de tipo string dando indicación de la columna del que lo tiene que coger
                @SuppressLint("Range") String movieVal = cursor.getString(cursor.getColumnIndex(COLUMN_VALORA_MOVIE));

                // Creo un objeto de tipo Movie
                Movie movie = new Movie();
                // Le establezco el id
                movie.setId(movieId);
                // Le establezco la url de la portada
                movie.setPosterPath(imageUrl);
                // Le establezco el título
                movie.setTitle(movieTi);
                // Le establezco la fecha de publicación
                movie.setReleaseDate(movieDate);
                // Le establezco la descripción
                movie.setDescripcion(movieDe);
                // Le establezco la valoración
                movie.setValoracion(movieVal);

                // Agrego el objeto a la lista de favoritos
                favoriteMovies.add(movie);
            } while (cursor.moveToNext()); // Realizamos lo anterior mientras el cursor pueda moverse al siguiente registro

            // Cerramos el cursor
            cursor.close();
        }

        // Cerramos la conexión con la BBDD
        db.close();
        // Retornamos la lista de películas y series favoritas
        return favoriteMovies;
    }


    /**
     * @param movieId
     * @return
     * Método para borrar una película de la base de datos, pasamos como argumento
     * el id de la película ya que es único, accemos un obejto de tipo SQLiteDtabase para
     * poder conectarnos a la bd de la lista de favoritos y procedemos a borrar la película
     * indicando que se elimine la que en la columna COLUMN_MOVIE_ID tenga el valor del
     * parametro que hemos pasado, esa operación devuelve un entero que sera con el que en otra
     * clase controlaremos la ejecucción*/
    public int borrarFavorita(String movieId, String userEmail) {
        // Creo un objeto que me va a permitir la escritura de la BBDD
        SQLiteDatabase db = this.getWritableDatabase();
        // Defino la condición WHERE para eliminar la película o serie basandome en el id de la película y el email
        // del usuario que tenga la sesión iniciada
        String whereClause = COLUMN_MOVIE_ID + " = ? AND " + COLUMN_EMAIL_USER + " = ?";
        String[] whereArgs = {movieId, userEmail};
        // Utilizo el submetodo de la clase db para ejecutar la eliminación de la película de la lista de
        // favoritas utilizando las condiciones de filtrado where
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    /**
     * @param movieId
     * @return
     * Método en el que nos conectamos a la bd de sqlite donde tenemos la lista de favoritas,
     * una vez establecida la conexión vamos a usar un Cursor para poder recorrer todos los registros
     * de la misma, haciendo una consulta de si la columna COLUMN_MOVIE_ID es igual al parametro que
     * pasamos, en caso de que el cursor detecte que hay alguna guardamos el true o false en una variable de tipo
     * boolean, posteriormente cerramos el cursor y retornamos el valor true o false para saber si ya existe
     * o no la película en la lista de favoritas*/
    public boolean existeEnLaBD(String movieId, String userEmail) {
        // Creo un obejto que me va a permitir la escritura de la BBDD
        SQLiteDatabase db = this.getReadableDatabase();
        // Creo un string en donde voy a escribir la consulta con respecto a que tiene que ser una película con un id y un correo en comun
        String selection = COLUMN_MOVIE_ID + " = ? AND " + COLUMN_EMAIL_USER + " = ?";
        // Establezco en un array de strings los valores que va a tomar cada ? de la consulta anterior
        String[] selectionArgs = {movieId, userEmail};
        //Utilizo un cursor para ejecutar la consulta y obtener todas las películas que tengan ese id y ese correo
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        // En una variable de tipo booleana, guardo si el cursor ha obtenido más de 0 respuestas, entonces significará que ya existe
        boolean existe = (cursor.getCount() > 0);
        // Cerramos el cursor
        cursor.close();
        // Cerramos el objeto que nos ayuda a escribir y leer en la bbdd
        db.close();
        // Devolvemos la variable booleana
        return existe;
    }

    /**
     * @return
     * @param userId
     * Método para obtener solo los ids de las películas favoritas
     * del usuario que le pasamos el id*/
    public List<String> obtenerIdsFavoritas(String userId) {
        // Creamos una lista de tipo string y la inicializamos
        List<String> ids = new ArrayList<>();
        // Llamamos a la clase e inicializamos dandonos cuenta de que es la que nos va a permitir leer la base de datos local
        SQLiteDatabase db = this.getReadableDatabase();
        // Utilizamos un cursor para ejecutar una query para filtrar y obtener todos los id de películas de un usuario en particular
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_MOVIE_ID + " FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL_USER + " = ?", new String[]{userId});
        // Comprobamos que el cursor no sea nulo
        if (cursor != null) { // En caso de no ser nulo
            // Utilizamos un bucle while para irnos moviendo por el curosr
            while (cursor.moveToNext()) {
                // Y vamos agregando el id de la película a la lista
                ids.add(cursor.getString(0));
            }
            // Una vez hayamos finalizado con el cursor, le cerramos
            cursor.close();
        }
        // Retornamos la lista de ids
        return ids;
    }
}