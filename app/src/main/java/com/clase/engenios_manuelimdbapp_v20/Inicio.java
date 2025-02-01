package com.clase.engenios_manuelimdbapp_v20;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Manuel
 * @version 1.0*/

public class Inicio extends AppCompatActivity {
    private FirebaseAuth auth=null; // Variable para controlar la Authentication de Firebase
    private SignInButton signInButton=null; // Variable para el botón de incio de sesión co Google
    private GoogleSignInClient googleSignInClient=null; // Variable para representar la instancia del flujo de datos en el inicio de sesión con Gogle
    private ActivityResultLauncher<Intent> signInLauncher=null; // Variable para controlar si el inicio de sesión fue correcto, fallo, etc
    private CallbackManager callbackManager = null; // Variable para manejar el manager de callback de facebook
    private LoginButton loginButton = null; // Variable para manejar el botón de login de facebook
    private Toast mensajeToast = null; // Variable manejar los toast del usuario
    private Button btnIniciar = null; // Variable para manejar el boton de iniciar
    private Button btnRegistrarse = null; // Variable para manejar el boton de registrarse
    private ImageView imagenClave = null; // Variable para manejar el imageview de ver o no la clave
    private EditText editCorreo = null; // Variable para manejar el editText del correo del usuario
    private EditText editClave = null; // Variable para manejar el editText de la clave del usuario
    private String email = null; // Variable para manejar el string del email
    private String clave = null; // Variable para manejar el string de la clave
    private DatabaseUsers userdb = null; // Variable para manejar la base de datos local
    private UsersSync sincronizarUsers = null; // Variable para manejar la base de datos en la nube
    private SharedPreferences preferences = null; // Variable para manejar las preferencias del usuario
    private ActivityResultLauncher<Intent> googleSignInLauncher = null; // Variable para manejar
    private AuthCredential nuevaCredencial = null; // Variable para manejar la nueva credencial
    private String loginMethod = null; // Variable para manejar el ultimo método de acceso

    private boolean seVeClave = false; // Variable para manejar si la clave se ve o no

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

        // Inicializo la base de datos local
        userdb = new DatabaseUsers(this);

        // Inicializo la base de datos en la nube
        sincronizarUsers = new UsersSync(this);

        // Obtengo las preferencias de los usuarios de la app
        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // Llamo al método para configurar el launcher de inicio de sesión con google
        configurarGoogleSignInLauncher();

        // Obtengo el ultimo método de login hecho
        loginMethod = preferences.getString("login_method", "unknown");

        // Obtengo todos los elementos de la interfaz
        editCorreo = (EditText) findViewById(R.id.editCorreo);
        editClave = (EditText) findViewById(R.id.editClave);
        imagenClave = (ImageView) findViewById(R.id.imagenVerClave);
        btnIniciar = (Button) findViewById(R.id.btnIniciarCorreo);
        btnRegistrarse = (Button) findViewById(R.id.btnRegistrarse);

        // Obtengo el componente de Login With Facebook de la interfaz
        loginButton = findViewById(R.id.facebook_login_button);
        // Establezco los permisos que necesitamos para acceder al Facebook
        loginButton.setPermissions("email", "public_profile");

        // Establezco la acción cuando registramos el callback de Facebook
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) { // En caso de que todo vaya bien
                // Llamo al método para acceder con facebook
                tokenAccesoFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() { // En caso de que algo falle
                // Lanzo un Toast indicando que se ha cancelado el inicio de sesion
                showToast("Inicio de sesión cancelado");
            }

            @Override
            public void onError(FacebookException error) { // En caso de que ocurra un error
                // Lanzo el error por el Logcat
                Log.e("Inicio", "Error en el inicio de sesión con Facebook", error);
            }
        });

        // Establezco la acción para cuando toque la imagen de ver clave
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

        // Establezco la acción al pulsar el botón de registrarse
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Guardo en las variables emai y clave los valores que obtengo del editText de correo y calve
                email = (String) editCorreo.getText().toString();
                clave = (String) editClave.getText().toString();
                // Compruebo que no esten vacios
                if(email.isEmpty()){ // Si el email esta vacio
                    // Lanzo un toast
                    showToast("El campo del email está vacío!!");
                }else{ // Si el email no esta vacio
                    if(clave.isEmpty()){ // Si la clave esta vacia
                        // Lanzo un Toast
                        showToast("El campo de la clave está vacío!!");
                    }else{ // Si la clave no esta vacia
                        // Llamo al método para registrar el email con la clave
                        registrarseConCorreo(email, clave);
                    }
                }
            }
        });

        // Establezco la acción que sucederá al pulsar el botón de inicio de sesion
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Guardo en las variables emai y clave los valores que obtengo del editText de correo y calve
                email = (String) editCorreo.getText().toString();
                clave = (String) editClave.getText().toString();
                // Compruebo que no esten vacios
                if(email.isEmpty()){// Si el email esta vacio
                    // Lanzo un Toast
                    showToast("El campo del email está vacío!!");
                }else{ // Si el email esta lleno
                    if(clave.isEmpty()){ // Si la clave esta vacia
                        // Lanzo un Toast
                        showToast("El campo de la clave está vacío!!");
                    }else{ // Si la clave esta llena
                        // Llamo al método de iniciar sesion con email y clave
                        iniciarConCorreo(email, clave);
                    }
                }
            }
        });
    }

    /**
     * @param nuevaCredencial
     * @param email2
     * @param prov
     * Método en el que le paso el correo, la credencial y el proeveedor,
     * en este como tal lo que hago es llamarle cuando existe una colisión de
     * cuentas por la existinción de otra cuenta con ese email, asique llamo a
     * un método para mostrar un dialogo para saber si quiere vincular las cuentas*/
    private void manejarColisionPorEmail(String email2, AuthCredential nuevaCredencial, String prov) {
        // Compruebo que el email que le paso no sea nulo
        if (email2 != null && !email2.isEmpty()) { // En caso de que no sea nulo
            // Llamo al método para mostrar al usuario el dialogo de si quiere vincular las cuentas
            mostrarDialogoVinculacion(email2, nuevaCredencial, "google.com", "facebook.com");
        } else { // En caso de que el email sea nulo
            // Lanzo un Toast diciendo que el correo no es valido
            showToast("El correo proporcionado es inválido.");
        }
    }

    /**
     * @param email2
     * @param nuevaCredencial
     * @param metodoExistente
     * @param provNuevo
     * Método en el que le muestro un dialogo en el usuario
     * diciendole que ya existe una cuenta con ese correo en firebase
     * y que si quiere vincular las cuentas, si da que no, pues nada
     * pero si da que si solicitamos las credenciales ya existentes
     * en firebase*/
    private void mostrarDialogoVinculacion(String email2, AuthCredential nuevaCredencial, String metodoExistente, String provNuevo) {
        // Creo un nuevo dialogo
        new AlertDialog.Builder(this)
                .setTitle("Conflicto de inicio de sesión") // Establezco el titulo
                .setMessage("El correo ya está registrado con otro método: " + metodoExistente + ". ¿Deseas vincular tu cuenta de " + provNuevo + " con esta?") // Establezco el mensaje
                .setPositiveButton("Sí, vincular", (dialog, which) -> { // En caso de que de al botón de aceptar
                    // Llamo al método para solicitar la credencial existente
                    solicitarCredencialExistente(email2, nuevaCredencial, metodoExistente);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> showToast("Inicio de sesión cancelado.")) // Si elige el botón de rechazar
                .setCancelable(false) // Pongo que no sea cancelable
                .show(); // Muestro el dialogo
    }

    /**
     * @param nuevaCredencial
     * @param email2
     * @param metodoExistente
     * Método en el que dependiendo el método existente en
     * firebase llamo a un método o no sabiendo que iniciara
     * un flujo u otro para iniciar sesión con facebook
     * o google*/
    private void solicitarCredencialExistente(String email2, AuthCredential nuevaCredencial, String metodoExistente) {
        // Utilizo un switch para comparar el metodo existente ya en Firebase
        switch (metodoExistente) {
            // En caso de que sea el proveedor de Google
            case GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD:
                // Llamo al método para inicia el flujo de inicio de sesion de Google
                iniciarFlujoGoogle(email2, nuevaCredencial);
                break;
            // En caso de que el proveedor de Facebook
            case FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD:
                // Llamo al método para inicia el flujo de inicio de sesion de Facebook
                iniciarFlujoFacebook(email2, nuevaCredencial);
                break;
            // En caso de que sea otro proveedor
            default:
                // Lanzo un Toast diciendo que ese proveedor no le soporto
                showToast("Método no soportado para vinculación: " + metodoExistente);
                break;
        }
    }

    /**
     * Método en el que configuro el launcher
     * de inicio de sesion con google*/
    private void configurarGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                AuthCredential credencialExistente = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                autenticarConCredencialExistente(credencialExistente, this.nuevaCredencial, "Google", null);
                            }
                        } catch (ApiException e) {
                            showToast("Error en inicio de sesión con Google: " + e.getMessage());
                        }
                    }
                }
        );
    }

    /**
     * @param nuevaCredencial
     * @param email
     * Método en el que le paso un email y la nuve credencial
     * de autentificación y procedo a iniciar el flujo para iniciar
     * sesión con google y confirmar la viculacion*/
    private void iniciarFlujoGoogle(String email, AuthCredential nuevaCredencial) {
        // Inicializo y le doy valor a la nuveCredencial a nivel de clase
        this.nuevaCredencial = nuevaCredencial;
        // Configuro los datos para iniciar sesion con google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        // Llamo al método para guardar el método de inicio de sesion
        guardarMetodoInicioSesion("facebook.com");
        // Creo el cliente de google
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        // Creo el intent haciendo referencia al cliente de google
        Intent signInIntent = googleSignInClient.getSignInIntent();
        // Lanzo la actividad
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * @param nuevaCredencial
     * @param email
     * Método en el que le paso un email
     * y la nuvea credencial con la que ovy a iniciar sesion y
     * inicio el flujo de facebook para confirmar la vinculacion*/
    private void iniciarFlujoFacebook(String email, AuthCredential nuevaCredencial) {
        // Inicializo y le doy valor a la nuveCredencial a nivel de clase
        this.nuevaCredencial = nuevaCredencial;
        // Establezco los permisos del botón de login para obtener el email
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
        // Establezco al callback de facebook tras registrarme
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) { // En caso de que todo haya ido bien
                // Obtengo el token
                AccessToken token = loginResult.getAccessToken();
                if (token != null) { // Compruebo que no sea nulo
                    // Declaro la credenial existente gracias al token de facebook
                    AuthCredential credencialExistente = FacebookAuthProvider.getCredential(token.getToken());
                    // Llamo al método para iniciar con el token existente
                    autenticarConCredencialExistente(credencialExistente, nuevaCredencial, "Facebook", token);
                }
            }

            @Override
            public void onCancel() { // En caso de que se cancele
                showToast("Inicio de sesión con Facebook cancelado.");
            }

            @Override
            public void onError(FacebookException error) { // En caso de que surja algun error
                showToast("Error en inicio de sesión con Facebook: " + error.getMessage());
            }
        });
    }

    /**
     * @param token
     * @param credencialExistente
     * @param nuevaCredencial
     * @param prov
     * Método en el que le paso la credencial que existe, la nueva credencial, el
     * proveedor con el que iniciamos y el token de facebook y lo que hacemos es
     * que en caso de llamar a este método significa que ha surgido una colision
     * de cuentas entonces intentaremos vincular las dos cuentas que son la
     * misma pero de diferente proveedor*/
    private void autenticarConCredencialExistente(AuthCredential credencialExistente, AuthCredential nuevaCredencial, String prov, AccessToken token) {
        // Intento iniciar sesion con la credencual existente
        auth.signInWithCredential(credencialExistente).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) { // En caso de que todo vaya bien
                // Guardaremos en una variable el usuario autentificado
                FirebaseUser user = auth.getCurrentUser();
                // Comprobaremos que no sea nulo
                if (user != null) {
                    // Intentaremos vincular la credencial antigua con la nueva
                    user.linkWithCredential(nuevaCredencial).addOnCompleteListener(linkTask -> {
                        if (linkTask.isSuccessful()) { // En caso de que en enlace haya ido bien
                            // Lanzaremos un toast indicandolo
                            showToast("Cuentas vinculadas exitosamente.");
                            // Y comprobaremos el proveedor que tenemos para iniciar sesion con unos datos u otros
                            if(prov.equals("Facebook")){
                                cargarDatosUsuario("Facebook", user, token);
                            }else if(prov.equals("Google")){
                                cargarDatosUsuario("Google", user, null);
                            }
                        } else { // En caso de que surja algún error a la hora de vincularlas
                            // Lanzaremos un toast avisando al usuario del error
                            showToast("Error al vincular cuentas: " +
                                    (linkTask.getException() != null ? linkTask.getException().getMessage() : "Desconocido."));
                        }
                    });
                } else { // En caso de que el usuario autentificado sea nulo
                    // Lanzaremos un toast indicandoselo al usuario
                    showToast("Error: No se pudo autenticar al usuario existente.");
                }
            } else { // En caso de que surja algún error al autenticar la credencial existente
                // Lanzaremos un toast indicandoselo al usuario
                showToast("Error al autenticar con la credencial existente: " +
                        (authTask.getException() != null ? authTask.getException().getMessage() : "Desconocido."));
            }
        });
    }

    /**
     * @param user
     * @param token
     * @param tipoProvee
     * Método en el que paso como parametros el tipo de proveedor, el usuario de firebase
     * la url de la foto que obtengo de facebook y dependiendo del valor del parametro
     * tipoProvee voy guardando y pasando al Intent unos datos del usuario u otros, para
     * así simplificar la tarea de paso de datos y manejar mejor la posible vinculación
     * de las cuentas*/
    public void cargarDatosUsuario(String tipoProvee, FirebaseUser user, AccessToken token){
        // Primero que todo compruebo el proveedor
        if(tipoProvee.equals("Google")){ // Si es google
            // Guardo el método de inicio de sesion
            guardarMetodoInicioSesion("google.com");

            // Verificar si el usuario ya existe en la base de datos local
            Cursor cursor = userdb.obtenerUsuarioPorUid(user.getUid());
            boolean usuarioExiste = (cursor != null && cursor.getCount() > 0);
            if (cursor != null) {
                cursor.close(); // Cierro el cursor después de usarlo
            }

            if (!usuarioExiste) {
                // Consulto a Firestore para obtener los datos del usuario
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(user.getUid());

                userRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String nombre = "Usuario";  // Nombre por defecto
                        String email = "Sin Email"; // Email por defecto

                        if (document != null && document.exists()) {
                            // Si el documento en Firestore existe, obtener los datos
                            nombre = document.getString("displayName") != null ? document.getString("displayName") : nombre;
                            email = document.getString("email") != null ? document.getString("email") : email;
                            userdb.insertarOActualizarUsuario(user.getUid(), nombre, email);
                            sincronizarUsers.guardarDatosEnLocal(user.getUid());
                        } else {
                            // Guardar en la base de datos local (SQLite)
                            nombre = user.getDisplayName();
                            email = cifrarBase64(user.getEmail());
                            // Guardar en la base de datos local
                            userdb.insertarOActualizarUsuario(user.getUid(), nombre, email);
                            // Y guardar en la base de datos de la nube
                            sincronizarUsers.agregarOActualizarUsuario(user.getUid(), nombre, email);
                        }

                        registerUserLogin();
                        // Redirigir al MainActivity solo después de obtener la imagen
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
                    } else {
                        Log.e("Firestore", "Error al obtener usuario", task.getException());
                        showToast("Error al obtener datos del usuario.");
                    };
                });

            } else {
                // Si el usuario ya existe en local, solo sincronizar y abrir la app
                registerUserLogin();
                sincronizarUsers.guardarDatosEnLocal(user.getUid());
                // Redirigir al MainActivity solo después de obtener la imagen
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
            }
        }else if(tipoProvee.equals("Facebook")){ // En caso de que el proveedor sea facebook
            // Guardo el método de inicio de sesion
            guardarMetodoInicioSesion("facebook.com");
            // Llamo al método para obtener la foto de perfil de facebook pasandole el usuario y el token
            obtenerImagenPerfilFacebook(token,user);
        }else if(tipoProvee.equals("Correo")){ // En caso de que el proveedor sea por correo
            // Verifico si el usuario ya existe en la base de datos local
            Cursor cursor = userdb.obtenerUsuarioPorUid(user.getUid());
            boolean usuarioExiste = (cursor != null && cursor.getCount() > 0);
            if (cursor != null) {
                cursor.close(); // Cierro el cursor después de usarlo
            }

            if (!usuarioExiste) {
                // Consulto a Firestore para obtener los datos del usuario
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(user.getUid());

                userRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String nombre = "Usuario";  // Nombre por defecto
                        String email = "Sin Email"; // Email por defecto

                        if (document != null && document.exists()) {
                            // Si el documento en Firestore existe, obtener los datos
                            nombre = document.getString("displayName") != null ? document.getString("displayName") : nombre;
                            email = document.getString("email") != null ? document.getString("email") : email;
                            userdb.insertarOActualizarUsuario(user.getUid(), nombre, email);
                            sincronizarUsers.guardarDatosEnLocal(user.getUid());
                        } else {
                            // Guardar en la base de datos local (SQLite)
                            nombre = user.getDisplayName();
                            email = cifrarBase64(user.getEmail());
                            // Guardar en la base de datos local
                            userdb.insertarOActualizarUsuario(user.getUid(), nombre, email);
                            // Y guardar en la base de datos de la nube
                            sincronizarUsers.agregarOActualizarUsuario(user.getUid(), nombre, email);
                        }

                        registerUserLogin();
                        // Redirigir al MainActivity solo después de obtener la imagen
                        Intent intent = new Intent(Inicio.this, MainActivity.class);
                        intent.putExtra("uidUs", user.getUid());
                        intent.putExtra("name", user.getDisplayName());
                        intent.putExtra("photoUrl", "");
                        intent.putExtra("message", "Conectado por otro método");
                        intent.putExtra("email", user.getEmail());
                        guardarMetodoInicioSesion("otro");
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("Firestore", "Error al obtener usuario", task.getException());
                        showToast("Error al obtener datos del usuario.");
                    }

                    // Registrar el inicio de sesión y continuar
                    //registerUserLogin();
                    //sincronizarUsers.guardarDatosEnLocal(user.getUid());
                });

            } else { // En caso de que sea otro
                // Si el usuario ya existe en local, solo sincronizar y abrir la app
                registerUserLogin();
                sincronizarUsers.guardarDatosEnLocal(user.getUid());
                // Redirigir al MainActivity solo después de obtener la imagen
                Intent intent = new Intent(Inicio.this, MainActivity.class);
                intent.putExtra("uidUs", user.getUid());
                intent.putExtra("name", "Desconocido");
                intent.putExtra("photoUrl", user.getDisplayName());
                intent.putExtra("message", "Conectado por otro método");
                intent.putExtra("email", user.getEmail());
                guardarMetodoInicioSesion("otro");
                startActivity(intent);
                finish();
            }
        }
    }

    /**
     * @param token
     * @param user
     * Método en el que le paso los datos como el token
     * y el usuario de firebase y solicito la imagen de perfil
     * del usuario autentificado en facebook*/
    public void obtenerImagenPerfilFacebook(AccessToken token, FirebaseUser user) {
        // Solicito los detalles del perfil de Facebook usando la API de Graph
        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                // En caso de que se complete compruebo si hemos tenido algun error
                if (response.getError() == null) { // En caso de no tener ningun error
                    // Utilizo un try catch para manejar las excepciones
                    try {
                        // Inicializo una variable que es temporal para la url de la foto
                        String tempPhotoUrl  = null;
                        // Procedo a obtener la url de la foto de faceboook
                        if (object.has("picture")) {
                            JSONObject pictureData = object.getJSONObject("picture").getJSONObject("data");
                            tempPhotoUrl  = pictureData.getString("url");
                        }

                        // Creo una variable final para guardar la url
                        final String photoUrl = tempPhotoUrl;

                        // Verifico si el usuario ya existe en la base de datos local
                        Cursor cursor = userdb.obtenerUsuarioPorUid(user.getUid());
                        boolean usuarioExiste = (cursor != null && cursor.getCount() > 0);
                        // En caso de que el cursor tenga algo
                        if (cursor != null) {
                            cursor.close(); // Cierro el cursor después de usarlo
                        }

                        // Si el usuario no existe en la bd local
                        if (!usuarioExiste) {
                            // Consulto a Firestore para obtener los datos del usuario
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference userRef = db.collection("users").document(user.getUid());

                            userRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();

                                    String nombre = "Usuario";  // Nombre por defecto
                                    String email = "Sin Email"; // Email por defecto

                                    if (document != null && document.exists()) {
                                        // Si el documento en Firestore existe, obtenengo los datos
                                        nombre = document.getString("displayName") != null ? document.getString("displayName") : nombre;
                                        email = document.getString("email") != null ? document.getString("email") : email;
                                        // Inserto en la bd local
                                        userdb.insertarOActualizarUsuario(user.getUid(), nombre, email);
                                        // Cargi todos los datos a local
                                        sincronizarUsers.guardarDatosEnLocal(user.getUid());
                                    } else { // En caso de que no exista
                                        // Guardar en la base de datos local (SQLite)
                                        if(!user.getDisplayName().isEmpty()){
                                            nombre = user.getDisplayName();
                                        }
                                        email = cifrarBase64(user.getEmail());
                                        // Guardar en la base de datos local
                                        userdb.insertarOActualizarUsuario(user.getUid(), nombre, email);
                                        // Y guardar en la base de datos en la nube
                                        sincronizarUsers.agregarOActualizarUsuario(user.getUid(), nombre, email);
                                    }

                                } else { // En caso de que surja algun error a la hora de obtener los datos del usuario
                                    Log.e("Firestore", "Error al obtener usuario", task.getException());
                                    showToast("Error al obtener datos del usuario.");
                                }

                                // Registrar el inicio de sesión y continuar
                                registerUserLogin();
                                abrirMainActivityFacebook(user, photoUrl);
                            });

                        } else {
                            // Si el usuario ya existe en local, solo sincronizar y abrir la app
                            registerUserLogin();
                            sincronizarUsers.guardarDatosEnLocal(user.getUid());
                            // Redirigir al MainActivity solo después de obtener la imagen
                            abrirMainActivityFacebook(user, photoUrl);
                        }

                    } catch (JSONException e) { // En caso de que surja alguna excepción
                        // Printo el error
                        e.printStackTrace();
                        // Lanzo un toast indicando lo ocurrido
                        showToast("Error al obtener la foto de perfil.");
                    }
                } else { // En caso de que algo vaya mal en la solicitud a la api de facebook
                    // Lanzo un toast indicandoselo al usuario
                    showToast("Error en la solicitud de perfil de Facebook.");
                }
            }
        });

        // Establecemos los parámetros para obtener los detalles del perfil
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * @param user
     * @param photoUrl
     * Método en donde le paso el usuario de Firebase y
     * la url de la foto para proceder a la declaración del
     * Intent para pasar a la siguiente actividad y pasarle los datos*/
    private void abrirMainActivityFacebook(FirebaseUser user, String photoUrl) {
        Intent intent = new Intent(Inicio.this, MainActivity.class);
        intent.putExtra("name", user.getDisplayName() != null ? user.getDisplayName() : "Usuario");
        intent.putExtra("photoUrl", photoUrl != null ? photoUrl : "");
        intent.putExtra("uidUs", user.getUid());
        intent.putExtra("message", "Conectado por Facebook");
        startActivity(intent);
        finish();
    }

    /**
     * @param correo
     * @param clave
     * Método para registrar la cuenta en
     * la autentificación en el proveedor de
     * correo y clave*/
    private void registrarseConCorreo(String correo, String clave) {
        // Llamo al submétodo del auth para iniciar con el email y contraseña
        auth.createUserWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // En caso de que se complete la acción, compruebo de que todo vaya bien
                        if (task.isSuccessful()) { // En caso de que todo vaya bien
                            // Obtengo el usuario recién creado
                            FirebaseUser user = auth.getCurrentUser();

                            // Aquí establezco el displayName predefinido
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName("Desconocido")
                                        .build();

                                // Actualizo el perfil del usuario con el displayName
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    showToast("Registro exitoso, Inicie Sesión!!");
                                                } else {
                                                    showToast("Error al actualizar el nombre de usuario: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else { // En caso de que falle algo
                            // Lanzo un toast diciendo el error que ha ocurddio
                            showToast("Error en el registro: " + task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * @param correo
     * @param clave
     * Método al que le paso como parametros
     * un correo y una clave y procedo a iniciar sesion
     * en firebase con esos valores*/
    public void iniciarConCorreo(String correo, String clave){
        // Llamo al método para iniciar sesion con email y clave
        auth.signInWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // En caso de que todo vaya bien
                            // Obtengo el usuario autentificado en el movil
                            FirebaseUser user = auth.getCurrentUser();
                            // Inicio de sesión exitoso
                            showToast("Inicio de sesión exitoso");
                            //Llamo al método para cargar los datos de ese usuario autenticado
                            cargarDatosUsuario("Correo", user, null);
                        } else { // En caso de que suceda algun error
                            // Error al iniciar sesión
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                            showToast("Error: " + error);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @param token
     * Método en donde le paso el token de acceso que me proporciona Facebook
     * y procedo a obtener la credenacial con ese token y a iniciar en firebase
     * sesion*/
    private void tokenAccesoFacebook(AccessToken token) {
        // Obtengo la credencial para iniciar
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        // Inicio sesion en firebase con la credencial
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            // Compruebo si todo ha salido bien
            if (task.isSuccessful()) { // De ser así
                // Obtengo en una variable el usuario autentificado
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) { // Compruebo que el usuario no sea nulo
                    // Llamo al método de cargar los datos con todos los datos oportunos
                    cargarDatosUsuario("Facebook", user, token);
                }
            } else { // De no ser así
                // Manejo la excepción y compruebo si la que ha saltado es por colision de cuentas
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    // Obtengo el usuario de la colisión
                    FirebaseAuthUserCollisionException collisionException = (FirebaseAuthUserCollisionException) task.getException();
                    // Me guardo en una variable email
                    String email2 = collisionException.getEmail();
                    // Llamo al método para manejar la misma
                    manejarColisionPorEmail(email2, credential, "Facebook");
                } else { // Si es por otra
                    // Guardo en una variable el error
                    String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                    // Lanzo un toast para proporiconar información
                    showToast("Error de autenticación: " + error);
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
     * Método para ir registrando los logins del usuario,
     * lo que hacemos es comprobar si el usuario identificado y su uid
     * no son nulos, si es así llamamos a los métodos para actualizar
     * el login tanto en local como en la nube y en las preferencias
     * lo que hago es marcar un valor que tengo para saber si esta
     * iniciado o no como true.*/
    private void registerUserLogin() {
        // Compruebo si el uid es correcto y no está vacio
        if (auth.getCurrentUser().getUid() != null) { // En caso de que el uide esté bien
            // Registro el login en la base de datos local
            userdb.actualizarLogin(auth.getCurrentUser().getUid());
            // Registo el login en la base de datos en la nube
            sincronizarUsers.registrarLogin(auth.getCurrentUser().getUid());
            // Establezco en las preferencias el editor para cambiar cosas
            SharedPreferences.Editor editor = preferences.edit();
            // En el editor establezco que el ususario si tiene la sesión iniciada
            editor.putBoolean("is_logged_in", true);
            // Establezco los cambios en las preferencias
            editor.apply();
        }
    }

    /**
     * @param account
     * Método en el que lo que hago es solicitar a Google el token id que se generá al iniciar sesión
     * correctamente cuando elegimos una cuenta de Google, posteriormente utilizo ese token id para poder
     * iniciar sesión con una credencial, en caso de que la tarea de finalice y todos los campos se carhuen bien
     * utilizamos un intent en donde establecemos datos que enviaremos para poder verlos en el MainActivity*/
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Obtengo la credencial con la que voy a iniciar en firebase desde google
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        // Llamo al método de iniciar sesion con credencial
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            // Compruebo si todo ha ido bien
            if (task.isSuccessful()) { // En caso de que si
                // Inicializo un usuario de firebase que es el usuario actual
                FirebaseUser user = auth.getCurrentUser();
                // Llamo al método de cargar los datos indicando el método de inicio de sesion, el usuario de firebase y que no tenemos token
                cargarDatosUsuario("Google", user, null);
            } else { // En caso de que algo falle
                // Comprobamos que la excepción sea porque ha surgido una colisión de cuentas
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    // Obtenemos en una variable un email de la cuenta
                    String email2 = account.getEmail();
                    // Y llamo al método para manejar la colisión pasandole todos los datos oportunos
                    manejarColisionPorEmail(email2, credential, "Google");
                } else {
                    showToast("Autenticación fallida: " + (task.getException() != null ? task.getException().getMessage() : "Error desconocido."));
                }
            }
        });
    }

    /**
     * @return
     * @param dato
     * Método en el que le paso por parametro un
     * string que es el dato a cifrar, y cifro el
     * string en base64*/
    private String cifrarBase64(String dato) {
        // Devuelvo el string cifrado
        return Base64.encodeToString(dato.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
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
        if (currentUser != null) {
            // Obtengo en una variable el método de acceso de las preferencias si no hay nada ponemos unknown
            loginMethod = preferences.getString("login_method", "unknown");

            // Obtengo en un cursor todos los datos del usuario pasandole el uid
            Cursor cursor = userdb.obtenerUsuarioPorUid(currentUser.getUid());
            // Compruebo si el usuario existe o no en la bd local
            boolean usuarioExiste = (cursor != null && cursor.getCount() > 0);

            // En caso de que no exista
            if (!usuarioExiste) {
                // Llamo al método para insertar en la bd local
                userdb.insertarOActualizarUsuario(currentUser.getUid(), currentUser.getDisplayName(), cifrarBase64(currentUser.getEmail()));
                // Llamo al método para insertar en la bd de la nube
                sincronizarUsers.agregarOActualizarUsuario(currentUser.getUid(), currentUser.getDisplayName(), cifrarBase64(currentUser.getEmail()));
            }
            // Llamo al método para cargar los datos de la nube al local
            sincronizarUsers.guardarDatosEnLocal(currentUser.getUid());

            // Creo el Intent
            Intent intent = new Intent(Inicio.this, MainActivity.class);
            // Establezco como dato el nombre del usuario que ha iniciado sesión
            intent.putExtra("name", currentUser.getDisplayName());

            // Compruebo desde que proveedor he iniciado sesión
            if (loginMethod.equalsIgnoreCase("google")) { // En caso de ser desde google
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
            } else if (loginMethod.equalsIgnoreCase("facebook")) { // En caso de ser desde facebook
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken != null && !accessToken.isExpired()) {
                    // Solicitar los detalles del perfil de Facebook usando la API de Graph
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            if (response.getError() == null) {
                                // Utilizo un try catch por si ocurre algun error a la hora de cargar los datos
                                try {
                                    // Obtenengo la foto de perfil de Facebook
                                    String photoUrl = null;
                                    if (object.has("picture")) {
                                        JSONObject pictureData = object.getJSONObject("picture").getJSONObject("data");
                                        photoUrl = pictureData.getString("url");
                                    }

                                    // Establezco los parametros que voy a pasar con el intent
                                    intent.putExtra("uidUs", currentUser.getUid());
                                    intent.putExtra("name", currentUser.getDisplayName());
                                    // Establezco la foto de perfil si se obtiene correctamente
                                    intent.putExtra("photoUrl", photoUrl);
                                    intent.putExtra("message", "Conectado por Facebook"); // Establezco el mensaje de conectado con Facebook

                                    // Inicio la actividad
                                    startActivity(intent);
                                    // Finalizo la actividad actual
                                    finish();
                                } catch (JSONException e) { // En caso de que surja alguna excepción
                                    // Printo el error
                                    e.printStackTrace();
                                    // Lanzo un Toast avisando al usuario
                                    showToast("Error al obtener la foto de perfil.");
                                }
                            } else { // En caso de que la respuesta de facebook sea nula
                                // Lanzo un Toast avisando al usuario
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
                intent.putExtra("name", currentUser.getDisplayName());
                intent.putExtra("photoUrl", "");
                intent.putExtra("message", "Conectado por otro método"); // Establezco el mensaje de conectado con otro método
                intent.putExtra("email", currentUser.getEmail());
                startActivity(intent);
                finish(); // Finalizo la actividad
            }
        }
    }

    /**
     * @param providerId
     * Método en el que le paso como parametro el
     * id del proveedor, y procedo a guardarlo en las
     * preferencias del usuario, por eso tengo que
     * manejar dependiendo del id, guardo un método u tro*/
    private void guardarMetodoInicioSesion(String providerId) {
        // Inicializo las preferencias
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        // Inicializo el editor de las preferencias
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Compruebo que id de proveedor he pasado
        if (providerId.equalsIgnoreCase("google.com")) { // En caso de ser el de Google
            // Guardo en preferencias el método de inicio de sesión
            editor.putString("login_method", "google");
        } else if (providerId.equalsIgnoreCase("facebook.com")) { // En caso de ser el de Facebook
            // Guardo en preferencias el método de inicio de sesión
            editor.putString("login_method", "facebook");
        } else { // En caso de ser otro
            // Guardo en preferencias el método de inicio de sesión
            editor.putString("login_method", "other");
        }
        editor.apply(); // Guardar los cambios de forma asíncrona
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