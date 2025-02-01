package com.clase.engenios_manuelimdbapp_v20;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.clase.engenios_manuelimdbapp_v20.sync.UsersSync;
import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author Manuel
 * @version 1.0*/

public class EditarPerfil extends AppCompatActivity {
    private EditText editCorreo = null; // Variable para controlar el editText del correo
    private EditText editNombre = null; // Variable para controlar el editText del nombre
    private EditText editUbi = null; // Variable para controlar el editText de la ubicación
    private EditText editTele = null; // Variable para controlar el editText del telefono
    private Button btnGuardar = null; // Variable para controlar el botón de guardar cambios
    private Button btnElegirImagen = null; // Variable para controlar el botón de elegir una imagen de perfil
    private Button btnElegirUbicacion = null; // Variable para controlar el botón de elegir la ubicación
    private Button btnConfirmarUbicacion = null; // Variable para controlar el botón de confirmar la ubicación
    private CountryCodePicker ccp = null; // Variable para controlar el spinner que muestra todos los prefijos
    private SharedPreferences sharedPreferences = null; // Variable para manejar las preferencias del usuario
    private ImageView imagenFotoPerfil = null; // Variable para manejar la imagen de la foto de perfil nueva
    private Toast mensajeToast = null; // Variable para manejar todos los toast de esta actividad
    private Bitmap selectedImage = null; // Variable de tipo Bitmap para manejar la imagen elegida o sacada
    private ImageView imagenMapa = null; // Varibale para ver si has elegido el sitio correcto del mapa
    private Button cancelarUbi = null; // Variable para manejar el botón de cancelar ubicacion
    private TextView textoTitulo = null; // Variable que representa y maneja el textview del titulo de la actividad

    private String prefijo = null; // Variable para manejar el prefijo elegido
    private String pais = null; // Variable para manejar el pais del prefijo elegido
    private String paisCodigo = null; // Variable para manejar el código del pais
    private String uid = null; // Variable para manejar el uid del usuario
    private String prefijoObtenido = null; // Variable donde guardo el prefijo obtenido de la base de datos
    private String numeroObtenido = null; // Variable donde guardo el número obtenido de la base de datos
    private String numeroDescifrado = null; // Variable donde guardo el número descifrado completo con el prefijo

    private String nombre = null; // Variable para manejar el nombre del usuario
    private String email = null; // Variable para manejar el email del usuario
    private String ubicacionStr = null; // Variable para manejar la ubicación del usuario
    private String numeroStr = null; // Variable para manejar el número del usuario
    private String urlPhoto = null; // Variable para manejar el string de la imagen

    private DatabaseUsers usersDB = null; // Variable que sirve para manejar la base de datos local de los usuarios
    private FirebaseAuth auth = null; // Variable que sirve para manejar la autentificación de los usuarios
    private FirebaseFirestore db = null; // Variable que sirve para manejar la base de datos en la nube de los usuarios
    private UsersSync sincronizarUsers = null;

    private static final int PERMISSION_REQUEST_CODE = 100; // Variable para manejar los permisos del usuario

    // Permiso para poder acceder a la camara del usuario
    private final ActivityResultLauncher<Intent> cameraResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        imagenFotoPerfil.setImageBitmap(photo);
                        selectedImage = photo;
                    }
                }
            });

    // Permiso para poder acceder a la galeria del usuario
    private final ActivityResultLauncher<Intent> galleryResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri imageUri = data.getData();
                        imagenFotoPerfil.setImageURI(imageUri);

                        try {
                            Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            selectedImage = photo;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    // Resultado de la actividad para elegir la ubicación del usuario
    private final ActivityResultLauncher<Intent> placePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    if (editUbi != null) {
                        editUbi.setText(place.getAddress());
                    }

                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        String staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                                "center=" + latLng.latitude + "," + latLng.longitude +
                                "&zoom=15&size=600x300&maptype=roadmap" +
                                "&markers=color:red%7C" + latLng.latitude + "," + latLng.longitude +
                                "&key=" + getString(R.string.apiKey_Maps);

                        Glide.with(this)
                                .load(staticMapUrl)
                                .into(imagenMapa);

                        imagenMapa.setVisibility(View.VISIBLE);
                        btnConfirmarUbicacion.setVisibility(View.VISIBLE);
                        cancelarUbi.setVisibility(View.VISIBLE);
                        btnGuardar.setVisibility(View.GONE);
                        ccp.setVisibility(View.GONE);
                        btnElegirImagen.setVisibility(View.GONE);
                        editTele.setVisibility(View.GONE);
                        imagenFotoPerfil.setVisibility(View.GONE);
                        btnElegirUbicacion.setVisibility(View.GONE);
                        editNombre.setVisibility(View.GONE);
                        editCorreo.setVisibility(View.GONE);
                        editUbi.setVisibility(View.GONE);
                        textoTitulo.setText("Elegir Ubicación!!");
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializo el autentificador de firebase
        auth = FirebaseAuth.getInstance();
        // Inicializo la base de datos en la nube de firebase
        db = FirebaseFirestore.getInstance();
        // Inicializo la base de datos local
        usersDB = new DatabaseUsers(this);
        // Inicializo la base de datos en la nube
        sincronizarUsers = new UsersSync(this);

        // Obtengo el uid del usuario y lo guardo en una variable
        uid = auth.getCurrentUser().getUid();

        // Obtengo todos los elementos del usuario de la interfaz
        btnGuardar = (Button) findViewById(R.id.btnGuardarCambios);
        btnElegirUbicacion = (Button) findViewById(R.id.btnUbi);
        btnElegirImagen = (Button) findViewById(R.id.btnElegirIMG);
        btnConfirmarUbicacion = findViewById(R.id.btnConfirmarUbicacion);
        imagenFotoPerfil = (ImageView) findViewById(R.id.imgPerfil);
        ccp = (CountryCodePicker) findViewById(R.id.prefijoNumero);
        editTele = (EditText) findViewById(R.id.editTelefono);
        editUbi = (EditText) findViewById(R.id.editDireccion);
        editCorreo = (EditText) findViewById(R.id.editCorreo);
        editNombre = (EditText) findViewById(R.id.editNombre);
        imagenMapa = (ImageView) findViewById(R.id.mapImageView);
        textoTitulo = (TextView) findViewById(R.id.textView2);
        cancelarUbi = (Button) findViewById(R.id.btncancelarUbicacion);

        // Llamo al método para obtener los datos del usuario pasandole el uid
        obtenerDatosUsuario(uid);

        // Compruebo si la variable nombre está vacia
        if(!nombre.isEmpty()){ // Sino está vacia
            // Establezco el valor al editText
            editNombre.setText(nombre);
        }
        // Compruebo si la variable ubicación está vacia
        if(!ubicacionStr.isEmpty()){ // Sino está vacia
            // Establezco el valor al editText
            editUbi.setText(ubicacionStr);
        }
        // Compruebo si la variable numero está vacia
        if(!numeroStr.isEmpty()){ // Sino está vacia
            // Establezco en la variable del numero descifrado el valor del mismo
            numeroDescifrado = descifrarBase64(numeroStr);

            // Compruebo si el numero cifrado no está vacio
            if (!numeroDescifrado.isEmpty() && numeroDescifrado.contains(" ")) { // En caso de no estar vacio
                // Divido el string en dos partes a partide un espacio
                String[] partes = numeroDescifrado.split(" ", 2);
                // Me guardo la primera parte que será la relacionada con el prefijo
                prefijoObtenido = partes[0];
                prefijo = prefijoObtenido;
                // Me guardo la segunda parte que será la relacionada con el número
                numeroObtenido = partes[1];
                // Elimino del prefijo el +, para quedarme con el número
                prefijoObtenido = prefijoObtenido.replace("+", "");
                // Establezco el número obtenido al editText
                editTele.setText(numeroObtenido);
                // Establezco en el ccp el prefijo del pais
                ccp.setCountryForPhoneCode(Integer.parseInt(prefijoObtenido));
            }
        }

        editUbi.setEnabled(false);
        // Establezco en el ediText del correo el email descifrado en Base64
        editCorreo.setText(descifrarBase64(email));
        // Establezco el editText como que no se puede modificar
        editCorreo.setEnabled(false);

        // Establezco la acción de cuando pulso el botón de elegir imagen
        btnElegirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditarPerfil.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(EditarPerfil.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(EditarPerfil.this,
                            new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                } else {
                    showImagePickerDialog();
                }
            }
        });

        // Establezco la acción de cuando pulso el botón de guardar datos
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtengo en una variable el telefono del editText
                String telefono = editTele.getText().toString().trim();

                // Compruebo que sea un número valido
                if (isValidNumberPhone(telefono)) { // Si lo es
                    if(!editNombre.getText().toString().isEmpty() || !editTele.getText().toString().isEmpty() ||
                    !editUbi.getText().toString().isEmpty()){
                            String imagenGuardar = null;

                            if(urlPhoto != null && !urlPhoto.isEmpty()) { // Si es una URL
                                imagenGuardar = urlPhoto;
                            }else if (selectedImage != null){
                                imagenGuardar = convertirImagenABase64(selectedImage);
                            }else{
                                showToast("Elija una imagen para poner en su perfil");
                                return;
                            }
                            String numeroCompleto = prefijo+" "+editTele.getText().toString();
                            usersDB.guardarImagenEnBaseDeDatos(imagenGuardar, uid);
                            if(editNombre.getText().toString().equals(auth.getCurrentUser().getDisplayName())){
                                usersDB.actualizarUsuario(uid, editUbi.getText().toString(), cifrarBase64(numeroCompleto), null, false);
                                sincronizarUsers.agregarDatosExtras(uid, null, cifrarBase64(editCorreo.getText().toString()), cifrarBase64(numeroCompleto), imagenGuardar,editUbi.getText().toString(), false);
                                showToast("Usuario actualizado correctamente. Volviendo al Main!!");
                                FirebaseUser user = auth.getCurrentUser();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    finish();
                                }, 2000);
                            }else{
                                usersDB.actualizarUsuario(uid, editUbi.getText().toString(), cifrarBase64(numeroCompleto), editNombre.getText().toString(), true);
                                sincronizarUsers.agregarDatosExtras(uid, editNombre.getText().toString(), cifrarBase64(editCorreo.getText().toString()), cifrarBase64(numeroCompleto), imagenGuardar,editUbi.getText().toString(), true);
                                showToast("Usuario actualizado correctamente. Volviendo al Main!!");
                                FirebaseUser user = auth.getCurrentUser();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    finish();
                                }, 2000);
                            }
                    }else{ // En caso de que haya algun campo vacio
                        // Lanzo un Toast al usuario indicandoselo
                        showToast("Existen campos vacios");
                    }
                } else { // Si no e sun número valido
                    // Lanzo un Toast indicandoselo al usuario
                    showToast("Número de telefono no válido");
                }
            }
        });

        // Establezco la acción de cuando pulso el botón de elegir ubicación
        btnElegirUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para buscar una ubicación
                openPlacePicker();
            }
        });

        // Establezco la acciónq ue sucede cuando elijo un prefijp
        ccp.setOnCountryChangeListener(() -> {
            pais = ccp.getSelectedCountryName();
            paisCodigo = ccp.getSelectedCountryNameCode();
            prefijo = ccp.getSelectedCountryCodeWithPlus();

            Toast.makeText(EditarPerfil.this,
                    "País: " + pais + "\nCódigo: " + "\nTel: " + prefijo,
                    Toast.LENGTH_LONG).show();
        });

        // Establezco la acción de cuando pulso el botón de confirmar ubicación
        btnConfirmarUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Oculto los elementos que no me interesan ahora
                imagenMapa.setVisibility(View.GONE);
                btnConfirmarUbicacion.setVisibility(View.GONE);
                cancelarUbi.setVisibility(View.GONE);
                // Doy visibilidad a todos los demás elementos que si me interesan
                btnGuardar.setVisibility(View.VISIBLE);
                ccp.setVisibility(View.VISIBLE);
                btnElegirImagen.setVisibility(View.VISIBLE);
                editTele.setVisibility(View.VISIBLE);
                imagenFotoPerfil.setVisibility(View.VISIBLE);
                btnElegirUbicacion.setVisibility(View.VISIBLE);
                editNombre.setVisibility(View.VISIBLE);
                editCorreo.setVisibility(View.VISIBLE);
                editUbi.setVisibility(View.VISIBLE);
                textoTitulo.setText("Editar Perfil");
                // Lanzo un Toast notificando al usuario que se ha elegido bien la ubicación
                showToast("Ubicación elegida");
            }
        });

        // Establezco la acción de cuando pulso el botón de cancelar ubicación
        cancelarUbi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Oculto los elementos que no me interesan ahora
                imagenMapa.setVisibility(View.GONE);
                btnConfirmarUbicacion.setVisibility(View.GONE);
                cancelarUbi.setVisibility(View.GONE);
                // Doy visibilidad a todos los demás elementos que si me interesan
                btnGuardar.setVisibility(View.VISIBLE);
                ccp.setVisibility(View.VISIBLE);
                btnElegirImagen.setVisibility(View.VISIBLE);
                editTele.setVisibility(View.VISIBLE);
                imagenFotoPerfil.setVisibility(View.VISIBLE);
                btnElegirUbicacion.setVisibility(View.VISIBLE);
                editNombre.setVisibility(View.VISIBLE);
                editCorreo.setVisibility(View.VISIBLE);
                editUbi.setVisibility(View.VISIBLE);
                editUbi.setText("");
                textoTitulo.setText("Editar Perfil");
                // Lanzo un Toast notificando al usuario que se ha elegido bien la ubicación
                showToast("Ubicación Cancelada");
            }
        });
    }

    /**
     * @param permissions
     * @param grantResults
     * @param requestCode
     * Método en el que compruebo si tengo los permisos solicitados
     * aceptados o el usuario los ha rechazado*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                showToast("Permisos necesarios no otorgados");
            }
        }
    }

    /**
     * Método en el que una vez pulsado el botón de elegir foto, le doy las opciones
     * para que ponga una foto u otra, dependiendo la que elija, lanzo un intent, otro intent
     * o cargo la url puesta*/
    private void showImagePickerDialog() {
        // Declaro las opciones que existen a la hora de elegir la foto de perfil
        CharSequence[] options = {"Tomar Foto", "Elegir de Galería", "URL"};

        // Creo un nuevo dialogo que mostrar al usuario
        new AlertDialog.Builder(EditarPerfil.this)
                .setTitle("Elegir Imagen") // Establezco el titutlo
                .setItems(options, (dialog, which) -> {
                    // Controlo todas las posibilidades
                    if (which == 0) { // Si elige la de tomar foto
                        // Creo un intent para abrir la camara
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Lanzo el intent
                        cameraResult.launch(takePictureIntent);
                    } else if (which == 1) { // Si elige la galeria
                        // Creo el intent para abrir la galeria o fotos del dispositivo
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // Lanzo el intent
                        galleryResult.launch(pickPhotoIntent);
                    }else if(which == 2){ // Si elige introducir una url
                        showUrlInputDialog();
                    }
                })
                .show(); // Muestro el dialogo
    }

    /**
     * Método para que en caso de elegir la opción de introducir
     * una url de una imagen para establecermela, muestro un dialogo
     * con un editText para introducir la url y llamar al método para
     * validarla*/
    private void showUrlInputDialog() {
        // Creo un EditText para que el usuario ingrese la URL
        final EditText urlInput = new EditText(EditarPerfil.this);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI); // Me asegurode  que el teclado sea adecuado para URLs

        // Creo un nuevo dialogo
        new AlertDialog.Builder(EditarPerfil.this)
                .setTitle("Ingresa la URL de la Imagen") // Título del diálogo
                .setView(urlInput) // Vista del EditText
                .setPositiveButton("Cargar", (dialog, which) -> {
                    String url = urlInput.getText().toString().trim(); // Obtenengo la URL ingresada

                    // Llamo al método para agregar la foto de perfil con la URL con un try catch para manejar la excepción
                    try {
                        agregarFotoPerfilURL(url); // Llamo al método que valida y carga la imagen
                    } catch (IllegalArgumentException e) {
                        showToast("Error: " + e.getMessage()); // Muestro el toast si la url no es valida
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss()) // Botón de cancelar
                .show(); // Muestro el cuadro de diálogo
    }

    /**
     * Método en el que compruebo que todo este inicializado, en caso
     * de no estar inicializado, le incializados y luego mediante un try catch
     * manejo las excepciones y lanzo el intent que he declarado para cargar
     * la ubicación*/
    private void openPlacePicker() {
        // Compruebo que no este inicializado el objeto Places
        if (!Places.isInitialized()) { // En caso de que no este inicializo
            // Incializo gracias a la key de la api de Google Maps
            Places.initialize(getApplicationContext(), getString(R.string.apiKey_Maps));
        }
        // Creo una lista del objeto Place.Field para obtener la lista de id, nombre, lat y dirección de las ubicaciones
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        // Utilizo un try catch para poder controlar las excepciones
        try {
            // Creo un intent basandome en la lista de los Place.Field para luego lanzar el itent
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
            // Lanzo el Intent
            placePickerLauncher.launch(intent);
            // Lanzo un Logcat indicnado que todo ha salido correctamente
            Log.d("PlacePicker", "Place Picker lanzado correctamente");
        } catch (Exception e) { // En caso de que salte alguna excepción
            // Lanzo un Logcat indicando que ha ocurrido un error y se le paso
            Log.e("PlacePicker", "Error desconocido: " + e.getMessage());
        }
    }

    /**
     * @return
     * @param telefonoEntero
     * Método en el que compruebo si el número es valido o no, utilizo la libreria
     * de libphonenumber para validar si los número introducidos son optimos o no, es
     * decir si tu eliges el prefijo +34 el número intrpducido es correcto con ese prefijo*/
    public boolean isValidNumberPhone(String telefonoEntero) {
        // Inicializo la clase PhoneNumberUtil que me servirá para comprobar el número de telefono
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        // Utilizo un try catch para controlar si el número es valido no
        try {
            // Procedo a parsear el telefono entero junto con el codigo del nombre del prefijo
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(telefonoEntero, ccp.getSelectedCountryNameCode());
            // Devuelvo el valor que me devuelve la llamada al método de isValidNumber de la clase PhoneNumber
            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) { // En caso de que surja alguna excepción
            // Lanzo una excepción
            return false;
        }
    }

    /**
     * @param url
     * Método agregar una foto de perfil basandonos en la url
     * pasada como parametro, compruebo que no sea nulo, compruebo
     * que no contenga espacios, y pruebo si es una url legal, en caso
     * de que pase todo, cargo la url con picasso*/
    public void agregarFotoPerfilURL(String url){
        // Compruebo que la url del string no sea nulo ni este vacia
        if (url == null || url.trim().isEmpty()) { // De ser así
            // Lanzo la excepción
            throw new IllegalArgumentException("La URL no puede ser nula o vacía.");
        }

        // Comprubo que no contenga espacios en la url
        if (url.contains(" ")) { // D ser así
            // Lanzo la excepción
            throw new IllegalArgumentException("La URL no puede contener espacios o múltiples URLs: " + url);
        }

        // Valido que la url tenga el formato adecuado con un try catch para cazar la exceción
        try {
            new java.net.URL(url); // Si falla, lanzará MalformedURLException
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("La URL no tiene un formato válido: " + url, e);
        }

        urlPhoto = url;
        // Utilizo picasso para cargar la imagen en el elemento de imageview
        Picasso.get()
                .load(url) // URL de la imagen
                .resize(800, 800) // Redimensiona la imagen a un tamaño adecuado (ajustar según tus necesidades)
                .centerCrop() // Asegura que la imagen se recorte y se ajuste correctamente al ImageView
                .into(imagenFotoPerfil); // Coloca la imagen cargada en el ImageView
    }

    /**
     * @return
     * @param bitmap
     * Método en donde le paso un obejto de tipo bitmap y gracias
     * a la codificación de Base64, paso el objeto bitmap a una
     * cadena de texto, es decir, un string que será el que luego
     * guarde en la bd local*/
    private String convertirImagenABase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * @return
     * @param base64String
     * Método en el que compruebo que el parametro string
     * no es nulo, en caso de ser nulo devuelvo nada, en caso de no ser
     * nulo con un try catch decodifico el string y lo transformo en
     * un bitmap que luego cargue en la imagen*/
    private Bitmap convertirBase64ABitmap(String base64String) {
        // Compruebo que el string del parametro no sea nulo ni este vacio
        if (base64String == null || base64String.isEmpty()) { // En caso de que sea nulo
            // Retorno nulo
            return null;
        }
        // Utilizo un try catch para decodificar y convertir a bitmap
        try {
            // Intento decodificar los bytes del parametro pasado con el Base64 por defecto
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            // Retorno el bitmap decodificado
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) { // En caso de que surja alguna excepción
            // Printo la excepción
            e.printStackTrace();
            // Devuelvo nulo
            return null;
        }
    }

    /**
     * @return
     * @param dato
     * Método en el que cifro el string del parametro que paso en
     * formato base64*/
    private String cifrarBase64(String dato) {
        // Devuelvo el string codificado en base64
        return Base64.encodeToString(dato.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    /**
     * @return
     * @param datoCifrado
     * Método en el que le paso un string cifrado en base64
     * y procedo a descrifrar los datos del parametro pasado*/
    private String descifrarBase64(String datoCifrado) {
        // Intento decodificar los bytes del parametro pasado con el Base64 por defecto
        byte[] decodedBytes = Base64.decode(datoCifrado, Base64.DEFAULT);
        // Retornor el string descrifrado
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * @param uid
     * Método para obtener los datos del usuario desde la base de datos local,
     * primero que todo obtengo el cursor del usuario compruebo que no este nulo y
     * obtengo en un string el valor de la imagen pasado a texto, compruebo que no sea nulo
     * convierto el texto de base64 a bitmap y lo establezco en el elemento de la imagen, obtengo
     * obtengo todos los datos y sino pongo ""*/
    @SuppressLint("Range")
    public void obtenerDatosUsuario(String uid) {
        // Obtengo el cursor de la base de datos local
        Cursor cursor = usersDB.obtenerUsuarioPorUid(uid);

        // Compruebo que el cursor no sea nulo
        if (cursor != null && cursor.moveToFirst()) { // En caso de que no sea nulo
            // Obtengo en una variable de tipo string la imagen en texto
            String base64Imagen = cursor.isNull(cursor.getColumnIndex("imagen")) ? "" : cursor.getString(cursor.getColumnIndex("imagen"));
            // Compruebo que el string no sea nulo
            if (!base64Imagen.isEmpty()) { // En caso de no ser nulo
                if(base64Imagen.startsWith("http")){
                    urlPhoto = base64Imagen;
                    Picasso.get()
                            .load(urlPhoto) // URL de la imagen
                            .resize(800, 800) // Redimensiona la imagen a un tamaño adecuado (ajustar según tus necesidades)
                            .centerCrop() // Asegura que la imagen se recorte y se ajuste correctamente al ImageView
                            .into(imagenFotoPerfil); // Coloca la imagen cargada en el ImageView
                }else{
                    // Creo un bitmap y paso el string de base64 a bitmap
                    Bitmap imagenPerfil = convertirBase64ABitmap(base64Imagen);
                    // Compruebo que el bitmap no sea nulo
                    if (imagenPerfil != null) { // En caso de no ser nulo
                        // Establezco el bitmap de la imagen
                        imagenFotoPerfil.setImageBitmap(imagenPerfil);
                        selectedImage = imagenPerfil;
                    } else { // En caso de ser nulo
                        // Lanzo un Logcat indicando que no se pudo convertir la imagen
                        Log.e("Imagen", "No se pudo convertir la imagen");
                    }
                }
            } else { // En caso de que no haya nada guardado
                // Lanzo un Logcat indicnado que no hay imagenes guardadas en la bd
                Log.d("Imagen", "No hay imagen guardada en la BD");
            }
            // Obtengo el nombre
            nombre = cursor.isNull(cursor.getColumnIndex("displayName")) ? "" : cursor.getString(cursor.getColumnIndex("displayName"));
            // Obtengo el email
            email = cursor.isNull(cursor.getColumnIndex("email")) ? "" : cursor.getString(cursor.getColumnIndex("email"));
            // Obtengo el número
            numeroStr = cursor.isNull(cursor.getColumnIndex("number")) ? "" : cursor.getString(cursor.getColumnIndex("number"));
            // Obtengo la ubicacion
            ubicacionStr = cursor.isNull(cursor.getColumnIndex("ubi")) ? "" : cursor.getString(cursor.getColumnIndex("ubi"));
        } else { // En caso de que el cursor sea nulo
            // Lanzo un Logcat indicnado que no se ha podido encontrar el usuario con uid
            Log.d("Usuario", "No se encontró el usuario con UID: " + uid);
        }

        // Compruebo si el cursor sigue teniendo datos
        if (cursor != null) { // Si es asi
            // Cierro el cursor
            cursor.close();
        }
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo*/
    public void showToast(String mensaje){
        if (this != null){
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
}