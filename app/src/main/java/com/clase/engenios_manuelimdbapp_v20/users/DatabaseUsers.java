package com.clase.engenios_manuelimdbapp_v20.users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseUsers extends SQLiteOpenHelper {
    // Declaro todas las variables necesarias para está clase
    private static final String DATABASE_NAME = "users.db"; // Nombre de la base de datos
    private static final int DATABASE_VERSION = 1; // Versión de la base de datos

    private static final String TABLE_NAME = "usuarios"; // Establezco el nombre de la tabla donde voy a almacenar los usuarios
    private static final String COLUMN_ID = "id"; // Establezco el nombre de la columna del id del registro
    private static final String COLUMN_UID = "uid";// Establezco el nombre de la columna del uid del usuario
    private static final String COLUMN_NAME = "displayName"; // Establezco el nombre de la columna del nombre del usuario
    private static final String COLUMN_EMAIL = "email"; // Establezco el nombre de la columna del email del ususario
    private static final String COLUMN_UBICACION = "ubi"; // Establezco el nombre de la columna de la ubicación del usuario
    private static final String COLUMN_NUMERO = "number"; // Establezco el nombre de la columna del correo del usuario
    private static final String COLUMN_IMAGEN_PERFIL = "imagen"; // Establezco el nombre de la columna de la imagen de perfil del usuario
    private static final String COLUMN_LAST_LOGIN = "last_login"; // Establezco el nombre de la columna del último login
    private static final String COLUMN_LAST_LOGOUT = "last_logout"; // Establezco el nombre de la columna del último logout

    /**
     * @param context
     * Constructor para incializar la bd local con
     * la tabla de los usuarios*/
    public DatabaseUsers(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * @param db
     * Método onCreate en el que lo que hago es
     * crear la tabla de los usuarios con todos sus campos
     * y establezco las especificaciones de cada columna y si son
     * claves, etc*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creo la tabla de los usuarios
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // Genero la columna del id
                + COLUMN_UID + " TEXT NOT NULL UNIQUE, " // Genero la columna del uid
                + COLUMN_NAME + " TEXT NOT NULL, " // Genero la columna del nombre
                + COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " // Genero la columna del email
                + COLUMN_UBICACION + " TEXT, " // Genero la columna de la ubicacion
                + COLUMN_NUMERO + " TEXT, " // Genero la columna del número
                +COLUMN_IMAGEN_PERFIL + " TEXT, " // Genero la columna de la imagen de perfil
                + COLUMN_LAST_LOGIN + " TEXT, " // Genero la columna del último login
                + COLUMN_LAST_LOGOUT + " TEXT)"; // Genero la columna del último logout
        db.execSQL(CREATE_TABLE); // Ejecuto la sentencia y creo la tabla
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar la tabla anterior si existe
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        //onCreate(db);
    }

    /**
     * @return
     * @param name
     * @param email
     * Método insertar un usuario en la bd, le paso
     * el uid, nombre y email del usuario, incializo la base de datos
     * de sqlite y gracias al contentValues consigo insertar los valores
     * en un nuevo registro en la bd*/
    public long insertarUsuario(String uid, String name, String email) {
        // Creo el objeto de la bd local y lo incializo para así poder agregar cosas
        SQLiteDatabase db = this.getWritableDatabase();
        // Utilizo el contentValues para para poder crear un objeto e insertarlo en la bd local
        ContentValues values = new ContentValues();
        // Establezco el nombre de la columna y el valor a insertar
        values.put(COLUMN_UID, uid);
        // Establezco el nombre de la columna y el valor a insertar
        values.put(COLUMN_NAME, name);
        // Establezco el nombre de la columna y el valor a insertar
        values.put(COLUMN_EMAIL, email);
        // Genero una variable y llamo al submétodo de insertar de la bd local y paso el contentvalues ya que el logout y el login
        // inicialmente nulos
        long id = db.insert(TABLE_NAME, null, values);
        // Cierro la base de datos local
        db.close();
        // Devuelvo el valor del long que obtengo al insertar
        return id;
    }

    /**
     * @param uid
     * Método en el que inicializo la base de datos local para así
     * poder actualizar datos de la misma, utilizo el content values para
     * así poder crear una especie de objeto y así poder hacer insercciones y modificaciones
     * en la misma, por otro lado, utilizo un método para obtener el tiempo y actualizo
     * el registro que en uid tenga el valor que le paso*/
    public void actualizarLogin(String uid) {
        // Creo el objeto de la bd local y lo incializo para así poder agregar cosas
        SQLiteDatabase db = this.getWritableDatabase();
        // Utilizo el contentValues para para poder crear un objeto e insertarlo en la bd local
        ContentValues values = new ContentValues();
        // Establezco en la columna de último login el valor que obtengo del submétodo
        values.put(COLUMN_LAST_LOGIN, obtenerTiempoActual());
        // Utilizo el método update de sqlite para actualizar el registro que en el uid tenga el valor del parametro
        db.update(TABLE_NAME, values, COLUMN_UID + " = ?", new String[]{uid});
        // Cierro la base de datos
        db.close();
    }

    /**
     * @param uid
     * Método en el que inicializo la base de datos local para así
     * poder actualizar datos de la misma, utilizo el content values para
     * así poder crear una especie de objeto y así poder hacer insercciones y modificaciones
     * en la misma, por otro lado, utilizo un método para obtener el tiempo y actualizo
     * el registro que en uid tenga el valor que le paso*/
    public void actualizarLogout(String uid) {
        // Creo el objeto de la bd local y lo incializo para así poder agregar cosas
        SQLiteDatabase db = this.getWritableDatabase();
        // Utilizo el contentValues para para poder crear un objeto e insertarlo en la bd local
        ContentValues values = new ContentValues();
        // Establezco en la columna de último logout el valor que obtengo del submétodo
        values.put(COLUMN_LAST_LOGOUT, obtenerTiempoActual());
        // Utilizo el método update de sqlite para actualizar el registro que en el uid tenga el valor del parametro
        db.update(TABLE_NAME, values, COLUMN_UID + " = ?", new String[]{uid});
        // Cierro la base de datos
        db.close();
    }

    /**
     * @return
     * Método en el que obtengo el tiempo actual, de la fecha
     * dia, mes, año y el tiempo de horas, minutos y segundos,
     * además devuelvo el valor de dicho string del momento actual*/
    public String obtenerTiempoActual() {
        // Formateo una fecha obtenida del momento y le doy el formato de dia, mes y año, junto con la hora, minutos y segundos
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    /**
     * @return
     * @param uid
     * Método para obtener todos los datos de un usuario
     * basandonos en su uid*/
    public Cursor obtenerUsuarioPorUid(String uid) {
        // Creo el objeto de la bd local y lo incializo para así poder agregar y obtener cosas
        SQLiteDatabase db = this.getReadableDatabase();
        // Dvuelvo el cursor con toda la información del usuario buscado
        return db.query(TABLE_NAME, null, COLUMN_UID + " = ?", new String[]{uid}, null, null, null);
    }

    /**
     * @param uid
     * @param numero
     * @param ubicacion
     * Método en el que le paso como parametros el uid, la ubicación
     * y el número de telefono del usuario y el nombre para asegurarnos
     * de actualizar los datos del mismo*/
    public void actualizarUsuario(String uid, String ubicacion, String numero, String nombre, boolean cambiarNombre){
        // Creo el objeto de la bd local y lo incializo para así poder agregar cosas
        SQLiteDatabase db = this.getWritableDatabase();
        // Utilizo el contentValues para para poder crear un objeto e insertarlo en la bd local
        ContentValues values = new ContentValues();
        // Asigno los valores a la columna para la actualización
        values.put(DatabaseUsers.COLUMN_UBICACION, ubicacion);
        // Asigno los valores a la columna para la actualización
        values.put(DatabaseUsers.COLUMN_NUMERO, numero);
        // Compruebo si he de actualizar el nombre también
        if(cambiarNombre == true){ // De ser así
            // Guardo y asigno el nuevo valor del nombre en el contet values
            values.put(DatabaseUsers.COLUMN_NAME, nombre);
        }
        // Realiza la actualización de las dos columnas del usuario
        db.update(DatabaseUsers.TABLE_NAME, values, DatabaseUsers.COLUMN_UID + "=?", new String[]{uid});
        // Cierro la base de datos
        db.close();
    }

    /**
     * @param uid
     * @param imagenBase64
     * Método en el que le paso como parametros una imagen pasada
     * a base 64 para convertirla en texto y el uid del usuario y
     * lo que hago es insertar está imagen en texto en la columan de la
     * foto de perfil del usuario con ese uid registrado*/
    public void guardarImagenEnBaseDeDatos(String imagenBase64, String uid) {
        // Creo el objeto de la bd local y lo incializo para así poder agregar cosas
        SQLiteDatabase db = this.getWritableDatabase();
        // Utilizo el contentValues para para poder crear un objeto e insertarlo en la bd local
        ContentValues values = new ContentValues();
        // Asigno los valores a la columna para la actualización
        values.put(DatabaseUsers.COLUMN_IMAGEN_PERFIL, imagenBase64);
        // Realiza la actualización de la imagen del usuario
        db.update(DatabaseUsers.TABLE_NAME, values, DatabaseUsers.COLUMN_UID + "=?", new String[]{uid});
        // Cierro la base de datos
        db.close();
    }
}