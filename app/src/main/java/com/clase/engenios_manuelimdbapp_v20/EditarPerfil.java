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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class EditarPerfil extends AppCompatActivity {
    private EditText editCorreo = null;
    private EditText editNombre = null;
    private EditText editUbi = null;
    private EditText editTele = null;
    private Button btnGuardar = null;
    private Button btnElegirImagen = null;
    private Button btnElegirUbicacion = null;
    private Button btnConfirmarUbicacion = null;
    private CountryCodePicker ccp = null;
    private SharedPreferences sharedPreferences = null;
    private ImageView imagenFotoPerfil = null;
    private Toast mensajeToast = null;
    private Bitmap selectedImage = null;
    private ImageView imagenMapa;

    private String prefijo = null;
    private String pais = null;
    private String paisCodigo = null;
    private String uid = null;

    private String nombre = null;
    private String email = null;
    private String ubicacionStr = null;
    private String numeroStr = null;
    private String urlPhoto = null;

    private DatabaseUsers usersDB = null;
    private FirebaseAuth auth = null;
    private FirebaseFirestore db = null;

    private static final int PERMISSION_REQUEST_CODE = 100;

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
                        btnGuardar.setVisibility(View.GONE);
                        ccp.setVisibility(View.GONE);
                        btnElegirImagen.setVisibility(View.GONE);
                        editTele.setVisibility(View.GONE);
                        imagenFotoPerfil.setVisibility(View.GONE);
                        btnElegirUbicacion.setVisibility(View.GONE);
                        editNombre.setVisibility(View.GONE);
                        editCorreo.setVisibility(View.GONE);
                        editUbi.setVisibility(View.GONE);
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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersDB = new DatabaseUsers(this);

        uid = auth.getCurrentUser().getUid();

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

        obtenerDatosUsuario(uid);

        if(!nombre.isEmpty()){
            editNombre.setText(nombre);
        }
        if(!ubicacionStr.isEmpty()){
            editUbi.setText(ubicacionStr);
        }
        if(!numeroStr.isEmpty()){
            editTele.setText(descifrarBase64(numeroStr));
        }
        editCorreo.setText(descifrarBase64(email));
        editCorreo.setEnabled(false);

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

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String telefono = editTele.getText().toString().trim();

                if (isValidNumberPhone(telefono)) {
                    if(!editNombre.getText().toString().isEmpty() || !editTele.getText().toString().isEmpty() ||
                    !editUbi.getText().toString().isEmpty()){
                        if(selectedImage!=null){
                            String imagenBase64 = convertirImagenABase64(selectedImage);
                            String numeroCompleto = prefijo+" "+editTele.getText().toString();
                            usersDB.guardarImagenEnBaseDeDatos(imagenBase64, uid);
                            if(editNombre.getText().toString().equals(auth.getCurrentUser().getDisplayName())){
                                usersDB.actualizarUsuario(uid, editUbi.getText().toString(), cifrarBase64(numeroCompleto), null, false);
                                showToast("Usuario actualizado correctamente");
                                finish();
                            }else{
                                usersDB.actualizarUsuario(uid, editUbi.getText().toString(), cifrarBase64(numeroCompleto), editNombre.getText().toString(), true);
                                showToast("Usuario actualizado correctamente");
                                finish();
                            }
                        }else{
                            showToast("Eliga una imagen para poner en su perfil");
                        }
                    }else{
                        showToast("Existen campos vacios");
                    }
                } else {
                    // Número no válido
                    showToast("Número de telefono no válido");
                }
            }
        });

        btnElegirUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlacePicker();
            }
        });

        ccp.setOnCountryChangeListener(() -> {
            pais = ccp.getSelectedCountryName();
            paisCodigo = ccp.getSelectedCountryNameCode();
            prefijo = ccp.getSelectedCountryCodeWithPlus();

            Toast.makeText(EditarPerfil.this,
                    "País: " + pais + "\nCódigo: " + "\nTel: " + prefijo,
                    Toast.LENGTH_LONG).show();
        });

        btnConfirmarUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagenMapa.setVisibility(View.GONE);
                btnConfirmarUbicacion.setVisibility(View.GONE);
                btnGuardar.setVisibility(View.VISIBLE);
                ccp.setVisibility(View.VISIBLE);
                btnElegirImagen.setVisibility(View.VISIBLE);
                editTele.setVisibility(View.VISIBLE);
                imagenFotoPerfil.setVisibility(View.VISIBLE);
                btnElegirUbicacion.setVisibility(View.VISIBLE);
                editNombre.setVisibility(View.VISIBLE);
                editCorreo.setVisibility(View.VISIBLE);
                editUbi.setVisibility(View.VISIBLE);
                showToast("Ubicación elegida");
            }
        });
    }

    /**
     * @param permissions
     * @param grantResults
     * @param requestCode
     * Método en el que */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método en el que */
    private void showImagePickerDialog() {
        CharSequence[] options = {"Tomar Foto", "Elegir de Galería"};

        new AlertDialog.Builder(EditarPerfil.this)
                .setTitle("Elegir Imagen")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraResult.launch(takePictureIntent);
                    } else if (which == 1) {
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryResult.launch(pickPhotoIntent);
                    }
                })
                .show();
    }

    /**
     * Método en el que*/
    private void openPlacePicker() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.apiKey_Maps));
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

        try {
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);

            placePickerLauncher.launch(intent);
            Log.d("PlacePicker", "Place Picker lanzado correctamente");
        } catch (Exception e) {
            Log.e("PlacePicker", "Error desconocido: " + e.getMessage());
        }
    }

    /**
     * @return
     * @param telefonoEntero
     * Método en el que */
    public boolean isValidNumberPhone(String telefonoEntero) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(telefonoEntero, ccp.getSelectedCountryNameCode());

            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream); // Usar JPEG con calidad ajustada
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap convertirBase64ABitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cifrarBase64(String dato) {
        return Base64.encodeToString(dato.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    private String descifrarBase64(String datoCifrado) {
        byte[] decodedBytes = Base64.decode(datoCifrado, Base64.DEFAULT);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    @SuppressLint("Range")
    public void obtenerDatosUsuario(String uid) {
        Cursor cursor = usersDB.obtenerUsuarioPorUid(uid);

        if (cursor != null && cursor.moveToFirst()) {
            String base64Imagen = cursor.isNull(cursor.getColumnIndex("imagen")) ? "" : cursor.getString(cursor.getColumnIndex("imagen"));
            if (!base64Imagen.isEmpty()) {
                Bitmap imagenPerfil = convertirBase64ABitmap(base64Imagen);
                if (imagenPerfil != null) {
                    imagenFotoPerfil.setImageBitmap(imagenPerfil);
                } else {
                    Log.e("Imagen", "No se pudo convertir la imagen");
                }
            } else {
                Log.d("Imagen", "No hay imagen guardada en la BD");
            }
            nombre = cursor.isNull(cursor.getColumnIndex("displayName")) ? "" : cursor.getString(cursor.getColumnIndex("displayName"));
            email = cursor.isNull(cursor.getColumnIndex("email")) ? "" : cursor.getString(cursor.getColumnIndex("email"));
            numeroStr = cursor.isNull(cursor.getColumnIndex("number")) ? "" : cursor.getString(cursor.getColumnIndex("number"));
            ubicacionStr = cursor.isNull(cursor.getColumnIndex("ubi")) ? "" : cursor.getString(cursor.getColumnIndex("ubi"));
        } else {
            Log.d("Usuario", "No se encontró el usuario con UID: " + uid);
        }

        if (cursor != null) {
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