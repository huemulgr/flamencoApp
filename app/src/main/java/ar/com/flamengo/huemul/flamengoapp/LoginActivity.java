package ar.com.flamengo.huemul.flamengoapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private SignInButton signInButton;
    private ProgressBar progressBar;
    private TextView title;
    private TextView subtitle;
    private TextView msjInvalid;

    public static boolean invalidQR;

    private boolean invalidMail = false;

    private Button scan_btn;

    public static final int SIGN_IN_CODE = 777;

    private String userEmail;

    private final static String codEmpresa = "1000";

    private FirebaseAuth fireBaseAuth;
    private FirebaseAuth.AuthStateListener fireBaseAuthListener;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference dataBaseReferenceValidUsers;

    private List<String> listValidUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        signInButton = (SignInButton) findViewById(R.id.btnSignIn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);

        title = (TextView) findViewById(R.id.textView_title_login);
        subtitle = (TextView) findViewById(R.id.textView_subtitle_login);

        msjInvalid = (TextView) findViewById(R.id.id_msj_invalid);
        msjInvalid.setVisibility(View.GONE);

        scan_btn = (Button) findViewById(R.id.btn_scan);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_CODE);
            }
        });

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ScanCodeActivity.class));
            }
        });


        fireBaseAuth = FirebaseAuth.getInstance();
        fireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    userEmail = user.getEmail();
                }
            }
        };

        listValidUser =  new ArrayList<String>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        fireBaseAuth.addAuthStateListener(fireBaseAuthListener);

        setListValidUsers();
    }

    private void setListValidUsers() {

        dataBaseReferenceValidUsers = ref.child("valid_users").getRef();

        dataBaseReferenceValidUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get map of users in datasnapshot
                collectDataValidUsers((Map<String,Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        });
    }

    private void collectDataValidUsers(Map<String,Object> validUsers) {

        Map entrada = (Map) validUsers.get(codEmpresa); //this.userEmail);

        if(entrada!=null && entrada.isEmpty()==false) {
            Iterator it = entrada.entrySet().iterator();

            listValidUser.clear();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                listValidUser.add(pair.getValue().toString());

                it.remove(); // avoids a ConcurrentModificationException
            }

            Log.d("VALEDE: ", "LISTA DE USUARIOS: " + listValidUser.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(fireBaseAuthListener!=null){
            fireBaseAuth.removeAuthStateListener(fireBaseAuthListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("CLICKEANDO", "se presiono el boton de login gmail");

        if(requestCode==SIGN_IN_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()) {
            //goMainScreen();
            fireBaseAuthWithGoogle(result.getSignInAccount());
        }else{
            Toast.makeText(this, R.string.not_log_in, Toast.LENGTH_SHORT).show();
        }
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount signInAccount) {

        Log.d("MIRANDO", "Entre de nuevo - " + signInAccount.getIdToken());

        progressBar.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);
        scan_btn.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        subtitle.setVisibility(View.GONE);
        msjInvalid.setVisibility(View.GONE);

        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

        fireBaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);
                signInButton.setVisibility(View.VISIBLE);
                scan_btn.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                subtitle.setVisibility(View.VISIBLE);

                boolean invalid = true;

                for (String usuarioValido : listValidUser) {
                    if(usuarioValido.equalsIgnoreCase(userEmail) == true){
                        goMainScreen();
                        invalid = false;
                    }
                }

                if(invalid==true) {
                    msjInvalid.setVisibility(View.VISIBLE);
                    msjInvalid.setText("El mail gmail ingresado no es un mail valido. Intente de nuevo con un mail valido o comuniquese con el administrador.");

                    revoke();
                }

                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), R.string.not_firebase_auth, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void revoke() {
        Log.d("DESLOGUEO", "Realizo e deslogueo. QUe tiene usermail " + this.userEmail);

        fireBaseAuth.signOut();

        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
            }
        });

        invalidMail = false;
    }

    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
