package com.clase.engenios_manuelimdbapp_v20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.engenios_manuelimdbapp_v20.models.Movie;
import com.squareup.picasso.Picasso;

/**
 * @author Manuel
 * @version  1.0*/

public class MovieDetailsActivity extends AppCompatActivity {
    private ImageView imagenPeli = null; // Variable de la película para cargar la portada
    private TextView titleView = null; // Variable para manejar el Textview del titulo
    private TextView valora = null; // Variable para manejar el Textview de la valoración
    private TextView fecha = null; // Variable para manejar el Textview de la fecha de publicación
    private TextView descrip = null; // Variable para manejar el Textview de la descripción
    private Button enviarSms = null; // Variable del botón para cuando queramos compartir una película por SMS
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 1; // Permiso de acceso a los contactos
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 2; // Permiso de acceso a enviar SMS
    private Toast mensajeToast = null; // Variable para controlar los toast de la aplicación
    private ActivityResultLauncher<Intent> contactPickerLauncher = null; // Variable para lanzar la actividad de elección de contacto y manejar el resultado
    private String selectedContactNumber = null; // Variable en donde guardo el número al que le voy a enviar la película
    private String movieMessage = "Mira!! Está película te puede gustar "; // Mensaje que completaré después y enviaré al contacto
    private Movie movie = null; // Objeto del tipo Movie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtengo todos los objetos de la interfaz de está actividad
        imagenPeli = (ImageView) findViewById(R.id.imagenPeli);
        titleView = (TextView) findViewById(R.id.txtTituloPeli);
        fecha = (TextView) findViewById(R.id.txtFe);
        valora = (TextView) findViewById(R.id.txtRating);
        descrip = (TextView) findViewById(R.id.txtDescr);
        enviarSms = (Button) findViewById(R.id.btnCompartirSms);

        // Obtengo el parceable que le pasé, para ahora cargar la información de la pelicula o serie
        movie = getIntent().getParcelableExtra("movieDetails");
        // Compruebo que recibo algo
        if (movie != null) { // En caso de que si que reciba algo
            // Establezco el titulo de la película o serie al TextView asociado
            titleView.setText(movie.getTitle());
            // Establezco la fecha de publicación de la película o serie al TextView asociado
            fecha.setText("Release Date: " +movie.getReleaseDate());
            // Establezco la foto de portada de la película o serie con la libreria Picasso
            Picasso.get()
                    .load(movie.getPosterPath()) // Establezco la url que voy a descargar ka imagen
                    .placeholder(R.drawable.baseline_autorenew_24) // Establezco el placeholder de la foto
                    .error(R.drawable.icono_error) // Foto si tenemos un error
                    .resize(300, 720) // Reajusto las dimensiones de la foto, porque algunas son muy grandes
                    .centerCrop() // Establezco que se recorte pero se mantengan las proporciones
                    .into(imagenPeli); // Establezco el item donde vamos a cargar la foto
            // Compruebo si el valor de la descripción es nula o no
            if(movie.getDescripcion()==null){ // En caso de que sea nula
                // Establezco la descripción de la película o serie en su Textview correspondiente
                descrip.setText("Descripción no disponible");
            }else{ // En caso de que no sea nula
                // Establezco la descripción de la película o serie en su Textview correspondiente
                descrip.setText(movie.getDescripcion());
            }
            // Compruebo si el valor de la valoración es nula
            if(movie.getValoracion()==null){ // En caso de que sea nula
                // Establezco la valoración de la película o serie en su Textview correspondiente
                valora.setText("Valoración no disponible");
            }else{ // En caso de que no sea nula
                // Establezco la valoración de la película o serie en su Textview correspondiente
                valora.setText(movie.getValoracion());
            }
        }

        // Procedo a configurar el Launcher para poder abrir otra actividad que en este caso será la de elección de un contacto
        contactPickerLauncher = registerForActivityResult(
                // Defino la nueva actividad que voy a llamar para poder así obtener posibles resultados
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Compruebo si el código de proceso es el correcto y los datos que he cargado son distintos de nulo
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // De ser así obtengo la uri del contacto elegido
                        Uri contactUri = result.getData().getData();
                        // Compruebo si la Uri del contacto elegida es nula o no
                        if (contactUri != null) {
                            // En caso de que no sea nula, llamo al método para conseguir su número de telefono
                            extractPhoneNumber(contactUri);
                        }
                    } else { // En caso de que no pase el filtro
                        // Lanzo un Toast al usuario avisandole de que no ha elegido ningun contacto
                        showToast("No se seleccionó ningún contacto");
                    }
                }
        );

        // Verifico si el usuario tiene los permisos de envio de sms ya activados o no
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) { // En caso de que no tenga los permisos acceptados
            // Procedo a mostrarle el dialogo para aceptar los permisos de envio de sms
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSION_REQUEST_CODE
            );
        }

        // Establezco un evento para cuando hagamos un click en el botón de compartir por sms
        enviarSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verifico si ya tiene el permiso de acceso a los contactos puesto
                if (ContextCompat.checkSelfPermission(
                        MovieDetailsActivity.this,
                        android.Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED) { // En caso de que no lo tenga
                    // Procedo a mostrarle el dialogo para que acepte los permisos de acceso a los contactos
                    ActivityCompat.requestPermissions(
                            MovieDetailsActivity.this,
                            new String[]{android.Manifest.permission.READ_CONTACTS},
                            CONTACTS_PERMISSION_REQUEST_CODE
                    );

                } else { // En caso de ya tener los permisos de acceo a la lectura de contactos concedidos
                    // Lanzamos un Toast avisando al usuario
                    showToast("Permisos de lectura de contactos ya concedidos");
                    // Llamo al método para elegir al contacto al que le voy a enviar la película
                    openContactPicker();
                }
            }
        });
    }

    /**
     * Método para establecer el intent mediante el cual vamos a poder ver todos nuestros
     * contactos y elegir uno de ellos*/
    private void openContactPicker() {
        // Creamos un intent estableciendo y especificando que vamos a mostrar una lista de contactos del usuario y que tiene que eelgir uno
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        // Utilizamos el lanzaodr de intents para poder ejecutar el intent de antes y poder ver la lista de contactos
        contactPickerLauncher.launch(intent);
    }

    /**
     * @param contactUri
     * Método en el que gracias al Uri del contacto puedo obtener el id del mismo y seguir filtrando
     * todos los contactos hasta que encuentre uno con ese identificador, una vez encontrado procedo
     * a poder obtener su número de telefono con otro cursor y otro filtro, una vez que tengo el número
     * si ha salido bien y el número es valido, llamo al método desde el que abro la aplicación
     * de sms con un Intent*/
    private void extractPhoneNumber(Uri contactUri) {
        // Variable en donde guardo el id del contacto
        String contactId = null;

        // Utilizo un try catch para poder controlar las excepciones
        // Aquí lo que hago es utilizar un cursor para poder consultar el id del contacto
        try (Cursor cursor = getContentResolver().query(contactUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) { // En caso de que el cursor no sea nulo y se pueda seguir moviendo
                // Obtengo la posición de la columna que contiene el id del contacto
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                // Introduzco en la variable antes creada el valor del id del contacto
                contactId = cursor.getString(idIndex);
            }
        }

        // Compruebo si el id del contacto es correcto
        if (contactId != null) { // En caso de que no sea nulo
            // Establezco el dato que quiero obtener, que en este caso es el numero
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
            // Filtro para solo obtener el contacto que tenga ese id
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
            // Consigo ese contaccto deseado
            String[] selectionArgs = {contactId};

            // Uso otro try catch para asi poder controlar las excepciones
            // Utilizo un cursor para poder obtener la posición de la colunma y registro del número aplicando el filtro del contacto
            try (Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null)) {

                // Compruebo si el cursor en donde controlo y consulto el número no es nulo y puede moverse al siguiente
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    // Obtengo la posición de la columna en donde está el número
                    int numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    // Guardo el número de telefono de esa columna y posición en la variable que he creado
                    selectedContactNumber = phoneCursor.getString(numberIndex);

                    // Compruebo si el número del contacto elegido no es nulo ni está vacio
                    if (selectedContactNumber != null && !selectedContactNumber.trim().isEmpty()) {
                        // Lanzo un Toast avisando al usuario indicandole el número del contacto que ha elegido
                        showToast("Número seleccionado: " + selectedContactNumber);

                        // Compruebo que el usuario tenga los permisos de envio de sms activados
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                                != PackageManager.PERMISSION_GRANTED) { // En caso de que no los tenga activados
                            // Mostramos el dialogo al usuario para que acepte los permisos
                            ActivityCompat.requestPermissions(
                                    this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    SEND_SMS_PERMISSION_REQUEST_CODE
                            );
                        } else { // En caso de que pase todos los filtros anteriores
                            // Llamo al método ya para abrir la aplicación de sms y poder enviar el sms
                            openSmsApp();
                        }
                    } else { // En caso de que el número de contacto no sea valido para poder enviarle un sms
                        // Lanzo un Toast al usuario avisandole del error
                        showToast("El contacto no tiene un número de teléfono válido.");
                    }
                } else { // En caso de que no se pueda obtener el número del contacto
                    // Lanzo un Toast al usuario avisandole del error
                    showToast("No se pudo obtener el número de teléfono.");
                }
            } catch (Exception e) { // En caso de que ocurra alguna excepción
                e.printStackTrace();
                // Lanzo un Toast al usuario avisandole del error
                showToast("Error al obtener el número del contacto.");
            }
        } else { // En caso de que no podamos obtener el id del contacto
            // Lanzo un Toast al usuario avisandole del error
            showToast("No se pudo obtener el ID del contacto.");
        }
    }

    /**
     * Método en donde una vez comprobado si el número del contacto elegido es correcto o no
     * procedo a crear un Intent en donde meteré datos como el tipo de acción que tiene que hacer
     * que en este caso es enviar un sms, estableceré el número al que se tiene que enviar, el mensaje
     * y abriré el intent en donde se nos abrirá una conversación con ese contacto y con el mensaje listo
     * para enviar*/
    private void openSmsApp() {
        // Procedo a verificar si el número seleccionado es nulo o está vacia
        if (selectedContactNumber != null && !selectedContactNumber.trim().isEmpty()) {
            // En caso de estar relleno y no ser nulo uso un try catch para poder manejar las excepciones
            try {
                // Creo un nuevo intent en donde especifico que quiero enviar un sms
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                // Establezco el destinatario del sms gracias al smsto y al número que he obtenido al elegir un contacto
                smsIntent.setData(Uri.parse("smsto:" + Uri.encode(selectedContactNumber)));
                // Establezco el cuerpo del texto con la estiqueta sms_body con el mensaje que yo quiera
                smsIntent.putExtra("sms_body", movieMessage+movie.getTitle()+" con valoración: "+valora.getText().toString());
                // Lanzo la actividad que me llevará a una conversación por sms con el contacto elegido y con el mensaje para enviar
                startActivity(smsIntent);
            } catch (Exception e) { // En caso de que surja alguna exceipción
                // Por el logCat muestro todo el error para así depurar
                Log.e("SMS_INTENT_ERROR", "Error al abrir la app de SMS", e);
                // Lanzo un Toast al usuario avisandole de que algo ha ido mal
                showToast("No se pudo abrir la aplicación de SMS.");
            }
        } else { // En caso de que si sea nulo o este vacio
            // Lanzo un Toast al usuario avisano de que no se pudo obtener el número valido para el envio
            showToast("No se pudo obtener un número válido para el SMS.");
        }
    }

    /**
     * @param permissions
     * @param grantResults
     * @param requestCode
     * Método en el que comprobamos si tenemos los permisos activados comprobando
     * los parametros que pasamos para ver si vamos a comprobar el permiso de acceso a contactos
     * o el de poder enviar sms*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permiso de contactos concedido");
                openContactPicker();
            } else {
                showToast("Permiso de contactos denegado");
            }
        } else if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permiso de envío de SMS concedido");
            } else {
                showToast("Permiso de envío de SMS denegado");
            }
        }
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo*/
    public void showToast(String mensaje){
        // Comprobamos si existe algun toast cargado en el toast de la variable global
        if (mensajeToast != null) { // En caso de que si que exista
            mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
        }

        // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
        mensajeToast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
        // Mostramos dicho Toast
        mensajeToast.show();
    }
}