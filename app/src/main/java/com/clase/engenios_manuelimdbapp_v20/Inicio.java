package com.clase.engenios_manuelimdbapp_v20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * @author Manuel
 * @version 1.0*/

public class Inicio extends AppCompatActivity {
    private FirebaseAuth auth=null; // Variable para controlar la Authentication de Firebase
    private SignInButton signInButton=null; // Variable para el botón de incio de sesión co Google
    private GoogleSignInClient googleSignInClient=null; // Variable para representar la instancia del flujo de datos en el inicio de sesión con Gogle
    private ActivityResultLauncher<Intent> signInLauncher=null; // Variable para controlar si el inicio de sesión fue correcto, fallo, etc
    private CallbackManager callbackManager = null;
    private LoginButton loginButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Obtengo la instancia de la autentificación de firebase
        auth = FirebaseAuth.getInstance();

        // Inicializa CallbackManager de Facebook
        callbackManager = CallbackManager.Factory.create();

        // Obtengo el componente de Login With Facebook de la interfaz
        loginButton = findViewById(R.id.facebook_login_button);
        // Establezco los permisos que necesitamos para acceder al Facebook
        loginButton.setPermissions("email", "public_profile");


        // Obtengo el elemento del botón de iniciar sesión con Google
        signInButton = findViewById(R.id.sign_in_button);
        // Llamo al método para poder cambiar el contenido de las letras del botón
        cambiarLetrasBoton();
        // Establezco un evento que al tocar el botón succeda
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método de iniciar sesión con Google
                signInWithGoogle();
            }
        });

        // Procedo a configurar las opciones de inicio de sesión con Google
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Solicito un token de usuario para poder ifentificar al usuario con firebase
                .requestIdToken(getString(R.string.client_id))
                // Solicitamos el correo del usuario
                .requestEmail()
                // Solicitamos el perfil del usuario, es decir, el nombre de la cuenta y la imagen
                .requestProfile()
                // Construimos las opciones de inicio de sesión
                .build();

        // Creo un cliente de inicio de sesión utilizando las configuraciones de inicio
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Utilizo el lanuncher para poder manejar mejor el resultado del inicio de sesión
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Compruebo si el usuario ha iniciado correctamente usando el codigo de resultado
                    if (result.getResultCode() == RESULT_OK) { // En caso de ser así
                        // Obtengo la información que me devuelve Google tras el inicio de sesión
                        Intent data = result.getData();
                        // Procedo a intar obtener la cuenta del usuario tras el inicio de sesión
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        // Utilizo un try catch para poder controlar más facilmente los errores y que no pete la aplicación
                        try {
                            // En caso de que todo salga bien, obtengo la cuenta del usuario que inicio
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            // Procedo a llamar al método para la autentificación de Firebase, pasandole la cuenta de Google
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) { // En caso de que surja algún error
                            // Mediante el log vemos todo el error por el Logcat
                            Log.w("Inicio", "Google sign-in failed", e);
                            // Lanzamos un Toast avisando al usuario del error ocurrido
                        }
                    }
                }
        );
    }

    /**
     * Método en el que lo que consigo es pedir a Google que inicie el flujo de inicio de sesión
     * con Google para poder manejar la salida*/
    private void signInWithGoogle() {
        // Creo un intent en el que llamo al inicio de sesión, como una especie de solicitud para poder iniciar sesión
        Intent signInIntent = googleSignInClient.getSignInIntent();
        // Utilizo el launcher para lanzar el intent
        signInLauncher.launch(signInIntent);
    }

    /**
     * @param account
     * Método en el que lo que hago es solicitar a Google el token id que se generá al iniciar sesión
     * correctamente cuando elegimos una cuenta de Google, posteriormente utilizo ese token id para poder
     * iniciar sesión con una credencial, en caso de que la tarea de finalice y todos los campos se carhuen bien
     * utilizamos un intent en donde establecemos datos que enviaremos para poder verlos en el MainActivity*/
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Procedo a obtener el token id que proporciona Google una vez elegida la cuenta que va a iniciar
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        // Utilizo dicho token id para poder iniciar sesión con Google en la aplicación haciendo referencia a Firebase Auth
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Compruebo una vez la tarea realizada si ha salido bien o no
                        if (task.isSuccessful()) { // En caso de que la tarea salga bien
                            FirebaseUser user = auth.getCurrentUser();
                            // Creo un intent para poder pasar al MainAcivity una vez iniciada la sesión
                            Intent intent = new Intent(Inicio.this, MainActivity.class);
                            // Establezco como parceable la key y el valor del nombre de usuario de la cuenta que inicio
                            intent.putExtra("name", user.getDisplayName());
                            // Establezco como parceable la key y el valor del email de usuario de la cuenta que inicio
                            intent.putExtra("email", user.getEmail());
                            // Establezco como parceable la key y el valor de la url de la foto de usuario de la cuenta que inicio
                            intent.putExtra("photoUrl", user.getPhotoUrl().toString());
                            // Iniciamos la actividad ya con el objeto parceable introducido y todo
                            startActivity(intent);
                            // Finalizamos la actividad actual
                            finish();
                        } else { // En caso de que la tarea no vaya del todo bien
                            // Por el Logcar lanzo un mensaje para ver la excepción que ha ocurrido y así poder solucionarla
                            Log.w("Inicio", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    /**
     * Con este método onStart lo que hago es comprobar si en este dispositivo ya tenemos
     * un usuario autenticado en firebase con Google y si su sesión sigue abierta, en caso de que sea así,
     * llamamos directamente al MainAcivity para no tener que volver a iniciar sesión pasandole los datos
     * de la cuenta como puede ser el nombre, email y url de la foto de perfil*/
    @Override
    protected void onStart() {
        super.onStart();

        // Obtengo el usuario autenticado del dispotivo
        FirebaseUser currentUser = auth.getCurrentUser();
        // Compruebo que se obtiene algo
        if (currentUser != null) { // Si se obtiene algo
            // Pasamos directamente con un intent a la pantalla del MainActivity para no tener que iniciar otra vez
            Intent intent = new Intent(Inicio.this, MainActivity.class);
            // Establezco como parceable la key y el valor del nombre de usuario de la cuenta que inicio
            intent.putExtra("name", currentUser.getDisplayName());
            // Establezco como parceable la key y el valor del email del usuario que inicio
            intent.putExtra("email", currentUser.getEmail());
            // Establezco como parceable la key y la url de la foto de perfil del usuario que inicio
            intent.putExtra("photoUrl", currentUser.getPhotoUrl().toString());
            // Lanzamos la actividad
            startActivity(intent);
            // Finalizamos la actual
            finish();
        }
    }

    /**
     * Método para poder cambiar lo escrito en el botón de inicio de sesión con Google,
     * tengo que cambiarlo así porque manualmente no he encontrado forma, lo que pasa esque
     * tengo que ir recorriendo todos los elementos y objetos hijos del botón hasta encontrar
     * algo que seá un TextView que es el que representa el mensaje, una vez le encontremos
     * lo que tengo que hacer es establecer el texto que quiero poner que en mi caso es
     * Sign in with Google*/
    public void cambiarLetrasBoton(){
        // Recorro todos los elementos y objetos hijos del botón de inicio de sesión
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            // Voy obteniendo las vistas
            android.view.View view = signInButton.getChildAt(i);
            // En caso de que encuentre el TextView que conforma el mensaje del botón
            if (view instanceof TextView) {
                // Le cambio el contenido al TetxView
                ((TextView) view).setText("Sign in with Google");
                // Y termino con el bucle
                break;
            }
        }
    }
}