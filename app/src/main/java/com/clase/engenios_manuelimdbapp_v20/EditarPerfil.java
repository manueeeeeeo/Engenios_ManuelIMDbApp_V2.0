package com.clase.engenios_manuelimdbapp_v20;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

import com.hbb20.CountryCodePicker;

public class EditarPerfil extends AppCompatActivity {
    private EditText editCorreo = null;
    private EditText editNombre = null;
    private EditText editUbi = null;
    private EditText editTele = null;
    private Button btnGuardar = null;
    private Button btnElegirImagen = null;
    private Button btnElegirUbicacion = null;
    private CountryCodePicker ccp = null;
    private SharedPreferences sharedPreferences = null;
    private ImageView imagenFotoPerfil = null;

    private String prefijo = null;
    private String pais = null;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private final ActivityResultLauncher<Intent> cameraResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        imagenFotoPerfil.setImageBitmap(photo);
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
                    }
                }
            });


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

        btnGuardar = (Button) findViewById(R.id.btnGuardarCambios);
        btnElegirUbicacion = (Button) findViewById(R.id.btnUbi);
        btnElegirImagen = (Button) findViewById(R.id.btnElegirIMG);
        imagenFotoPerfil = (ImageView) findViewById(R.id.imgPerfil);
        ccp = (CountryCodePicker) findViewById(R.id.prefijoNumero);
        editTele = (EditText) findViewById(R.id.editTelefono);

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

            }
        });

        btnElegirUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        ccp.setOnCountryChangeListener(() -> {
            pais = ccp.getSelectedCountryName();
            prefijo = ccp.getSelectedCountryCodeWithPlus();

            Toast.makeText(EditarPerfil.this,
                    "País: " + pais + "\nCódigo: " + "\nTel: " + prefijo,
                    Toast.LENGTH_LONG).show();
        });
    }

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
}