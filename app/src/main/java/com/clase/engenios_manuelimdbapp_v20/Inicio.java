package com.clase.engenios_manuelimdbapp_v20;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.clase.engenios_manuelimdbapp_v20.sync.UsersSync;
import com.clase.engenios_manuelimdbapp_v20.users.DatabaseUsers;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Toast mensajeToast = null;
    private Button btnIniciar = null;
    private Button btnRegistrarse = null;
    private ImageView imagenClave = null;
    private EditText editCorreo = null;
    private EditText editClave = null;
    private String email = null;
    private String clave = null;
    private DatabaseUsers userdb = null;
    private UsersSync sincronizarUsers = null;

    private boolean seVeClave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializo el sdk de Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Activo la app con Facebook
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_inicio);

        // Obtengo la instancia de la autentificación de firebase
        auth = FirebaseAuth.getInstance();

        // Inicializa CallbackManager de Facebook
        callbackManager = CallbackManager.Factory.create();

        userdb = new DatabaseUsers(this);

        sincronizarUsers = new UsersSync();

        editCorreo = (EditText) findViewById(R.id.editCorreo);
        editClave = (EditText) findViewById(R.id.editClave);
        imagenClave = (ImageView) findViewById(R.id.imagenVerClave);
        btnIniciar = (Button) findViewById(R.id.btnIniciarCorreo);
        btnRegistrarse = (Button) findViewById(R.id.btnRegistrarse);

        // Obtengo el componente de Login With Facebook de la interfaz
        loginButton = findViewById(R.id.facebook_login_button);
        // Establezco los permisos que necesitamos para acceder al Facebook
        loginButton.setPermissions("email", "public_profile");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                tokenAccesoFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                showToast("Inicio de sesión cancelado");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Inicio", "Error en el inicio de sesión con Facebook", error);
            }
        });

        imagenClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos la variable que declare antes para el manejo de si mostrar o no la clave
                if (seVeClave) { // En caso de que sea true
                    // Oculto la contraseña
                    editClave.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else { // En caso de que sea false
                    // Muestro la contraseña
                    editClave.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                // Mantengo el cursor al final del texto
                editClave.setSelection(editClave.getText().length());
                editClave.requestFocus(); // Aseguro que el EditText mantenga el foco

                // Alterno el estado de visibilidad
                seVeClave = !seVeClave;
            }
        });

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

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = (String) editCorreo.getText().toString();
                clave = (String) editClave.getText().toString();
                if(email.isEmpty()){
                    showToast("El campo del email está vacío!!");
                }else{
                    if(clave.isEmpty()){
                        showToast("El campo de la clave está vacío!!");
                    }else{
                        registrarseConCorreo(email, clave);
                    }
                }
            }
        });

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = (String) editCorreo.getText().toString();
                clave = (String) editClave.getText().toString();
                if(email.isEmpty()){
                    showToast("El campo del email está vacío!!");
                }else{
                    if(clave.isEmpty()){
                        showToast("El campo de la clave está vacío!!");
                    }else{
                        iniciarConCorreo(email, clave);
                    }
                }
            }
        });
    }

    public void registrarseConCorreo(String correo, String clave){
        auth.createUserWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showToast("Registro exitoso, Inicie Sesión!!");
                        } else {
                            showToast("Error en el registro: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void iniciarConCorreo(String correo, String clave){
        auth.signInWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            userdb.insertarUsuario(user.getUid(), user.getDisplayName(), user.getEmail());
                            userdb.actualizarLogin(user.getUid());
                            showToast("Login Actualizado: "+userdb.obtenerTiempoActual());
                            sincronizarUsers.agregarOActualizarUsuario(user.getUid(), user.getDisplayName(), user.getEmail());
                            sincronizarUsers.registrarLogin(user.getUid());
                            // Inicio de sesión exitoso
                            showToast("Inicio de sesión exitoso");
                            Intent intent = new Intent(Inicio.this, MainActivity.class);
                            intent.putExtra("uidUs", user.getUid());
                            intent.putExtra("photoUrl", "");
                            intent.putExtra("message", "Conectado por otro método"); // Establezco el mensaje de conectado con otro método
                            intent.putExtra("email", user.getEmail()); // Establezco el email
                            startActivity(intent);
                            finish();
                        } else {
                            // Error al iniciar sesión
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                            showToast("Error: " + error);
                        }
                    }
                });
    }

    public void proseguir(){
        FirebaseUser user = auth.getCurrentUser();
        Intent in = new Intent(Inicio.this, MainActivity.class);
        in.putExtra("uidUs", user.getUid());
        in.putExtra("photoUrl", "");
        in.putExtra("message", "Conectado por otro método"); // Establezco el mensaje de conectado con otro método
        in.putExtra("email", user.getEmail()); // Establezco el email
        startActivity(in);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void tokenAccesoFacebook(AccessToken token) {
        // Obtengo la credencial con la que voy a iniciar sesión en Firebase
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        // Intento autenticar al usuario con Firebase y Facebook
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si todo fue exitoso
                            FirebaseUser user = auth.getCurrentUser();

                            // Solicito los detalles del perfil de Facebook usando la API de Graph
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    if (response.getError() == null) {
                                        try {
                                            String facebookUserId = object.getString("id");
                                            String photoUrl = null;
                                            if (object.has("picture")) {
                                                JSONObject pictureData = object.getJSONObject("picture").getJSONObject("data");
                                                photoUrl = pictureData.getString("url");
                                            }

                                            userdb.insertarUsuario(user.getUid(), user.getDisplayName(), user.getEmail());
                                            userdb.actualizarLogin(user.getUid());
                                            showToast("Login Actualizado: "+userdb.obtenerTiempoActual());
                                            sincronizarUsers.agregarOActualizarUsuario(user.getUid(), user.getDisplayName(), user.getEmail());
                                            sincronizarUsers.registrarLogin(user.getUid());

                                            Intent intent = new Intent(Inicio.this, MainActivity.class);
                                            intent.putExtra("name", user.getDisplayName());
                                            intent.putExtra("photoUrl", photoUrl);
                                            // Establezco como parceable la key y el valor de la uid del usuario de la cuenta que inicio
                                            intent.putExtra("uidUs", user.getUid());
                                            intent.putExtra("message", "Conectado por Facebook");
                                            startActivity(intent);
                                            finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            showToast("Error al obtener la foto de perfil.");
                                        }
                                    } else {
                                        showToast("Error en la solicitud de perfil de Facebook.");
                                    }
                                }
                            });

                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,picture.type(large)");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {
                            Log.w("Inicio", "signInWithCredential:failure", task.getException());
                            showToast("Autenticación fallida "+task.getException());
                        }
                    }
                });
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
                            userdb.insertarUsuario(user.getUid(), user.getDisplayName(), user.getEmail());
                            userdb.actualizarLogin(user.getUid());
                            showToast("Login Actualizado: "+userdb.obtenerTiempoActual());
                            sincronizarUsers.agregarOActualizarUsuario(user.getUid(), user.getDisplayName(), user.getEmail());
                            sincronizarUsers.registrarLogin(user.getUid());
                            // Creo un intent para poder pasar al MainAcivity una vez iniciada la sesión
                            Intent intent = new Intent(Inicio.this, MainActivity.class);
                            // Establezco como parceable la key y el valor del nombre de usuario de la cuenta que inicio
                            intent.putExtra("name", user.getDisplayName());
                            // Establezco como parceable la key y el valor del email de usuario de la cuenta que inicio
                            intent.putExtra("email", user.getEmail());
                            // Establezco como parceable la key y el valor de la url de la foto de usuario de la cuenta que inicio
                            intent.putExtra("photoUrl", user.getPhotoUrl().toString());
                            // Establezco como parceable la key y el valor de la uid del usuario de la cuenta que inicio
                            intent.putExtra("uidUs", user.getUid());
                            // Establezco como parceable la key y el valor del mensaje para saber si está resgitrado con Google o Facebook
                            intent.putExtra("message", "Conectado por Google");
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
        // Creo una variable para más adelante cargar el id del proveedor
        String providerId = null;
        String photoUrl = null;

        // Compruebo que se obtiene algo
        if (currentUser != null) {
            // Obtengo los datos de los proveedores
            for (UserInfo userInfo : currentUser.getProviderData()) {
                // Guardo en la variable creada el id del proveedor
                providerId = userInfo.getProviderId();
            }

            userdb.actualizarLogin(currentUser.getUid());
            showToast("Login Actualizado: "+userdb.obtenerTiempoActual());
            sincronizarUsers.agregarOActualizarUsuario(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());
            sincronizarUsers.registrarLogin(currentUser.getUid());

            // Creo el Intent
            Intent intent = new Intent(Inicio.this, MainActivity.class);
            // Establezco como dato el nombre del usuario que ha iniciado sesión
            intent.putExtra("name", currentUser.getDisplayName());

            // Compruebo desde que proveedor he iniciado sesión
            if (providerId.equalsIgnoreCase("google.com")) { // En caso de ser desde google
                intent.putExtra("email", currentUser.getEmail()); // Establezco el email
                // Establezco como dato la url del usuario que ha iniciado sesión
                intent.putExtra("photoUrl", currentUser.getPhotoUrl().toString());
                intent.putExtra("message", "Conectado por Google"); // Establezco el mensaje de conectado con
                // Establezco como parceable la key y el valor de la uid del usuario de la cuenta que inicio
                intent.putExtra("uidUs", currentUser.getUid());

                // Inicio la actividad
                startActivity(intent);
                // Finalizo la actividad actual
                finish();
            } else if (providerId.equalsIgnoreCase("facebook.com")) { // En caso de ser desde facebook
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken != null && !accessToken.isExpired()) {
                    // Solicitar los detalles del perfil de Facebook usando la API de Graph
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            if (response.getError() == null) {
                                try {
                                    // Obtener la foto de perfil de Facebook
                                    String photoUrl = null;
                                    if (object.has("picture")) {
                                        JSONObject pictureData = object.getJSONObject("picture").getJSONObject("data");
                                        photoUrl = pictureData.getString("url");
                                    }

                                    // Establezco como parceable la key y el valor de la uid del usuario de la cuenta que inicio
                                    intent.putExtra("uidUs", currentUser.getUid());
                                    intent.putExtra("name", currentUser.getDisplayName());
                                    // Establezco la foto de perfil si se obtiene correctamente
                                    intent.putExtra("photoUrl", photoUrl);
                                    intent.putExtra("message", "Conectado por Facebook"); // Establezco el mensaje de conectado con

                                    // Inicio la actividad
                                    startActivity(intent);
                                    // Finalizo la actividad actual
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showToast("Error al obtener la foto de perfil.");
                                }
                            } else {
                                showToast("Error en la solicitud de perfil de Facebook.");
                            }
                        }
                    });

                    // Solicitar los datos del perfil (id, nombre y foto)
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,picture.type(large)");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
            } else { // En caso de ser desde otro
                intent.putExtra("uidUs", currentUser.getUid());
                intent.putExtra("photoUrl", "");
                intent.putExtra("message", "Conectado por otro método"); // Establezco el mensaje de conectado con otro método
                intent.putExtra("email", currentUser.getEmail()); // Establezco el email
                startActivity(intent);
                finish(); // Finalizo la actividad
            }
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