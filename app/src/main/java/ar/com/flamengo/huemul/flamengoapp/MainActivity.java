package ar.com.flamengo.huemul.flamengoapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ar.com.flamengo.huemul.flamengoapp.adapter.ListViewAdapter;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;

    private ImageView photoImageView;
    private TextView nameTextView;
    //private TextView emailTextView;
    //private TextView identificadorTextView;

    //private TextView mensajeTextView;
    //private EditText mensajeEditText;

    private TextView textViewNameCompany;

    private ListView listView;
    private ListViewAdapter adapter;
    private ArrayList<HashMap> listStatusMas;
    private ArrayList<HashMap> listValidUser;

    private FirebaseAuth fireBaseAuth;
    private FirebaseAuth.AuthStateListener fireBaseAuthListener;

    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    //private DatabaseReference mensajeRef;
    private DatabaseReference statusRegistersMAS;
    private DatabaseReference dataBaseReferenceValidUsers;

    private String userEmail;
    private final static String codEmpresa = "1000";

    private boolean validEmail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        declareReferenceOfAttributeFromActivity();

        fireBaseAuth = FirebaseAuth.getInstance();

        fireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if( user!=null ){
                    setUserData(user);
                    setMasData();
                }else{
                    goLogInScreen();
                }
            }
        };

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        Log.d("nuevo_token: ", newToken);
                    }
                });
    }

    private void declareReferenceOfAttributeFromActivity() {
        photoImageView = (ImageView) findViewById(R.id.photoImageView);

        nameTextView = (TextView) findViewById(R.id.textViewName);
        //emailTextView = (TextView) findViewById(R.id.textViewEmail);
        //identificadorTextView = (TextView) findViewById(R.id.textViewIdentificador);

        //mensajeTextView = (TextView) findViewById(R.id.mensajeTextView);
        //mensajeEditText = (EditText) findViewById(R.id.mensajeEditText);

        textViewNameCompany = (TextView) findViewById(R.id.textViewNameCompany);

        listView = (ListView) findViewById(R.id.listViewRegistros);
    }

    private void setUserData(FirebaseUser user) {

        nameTextView.setText(user.getDisplayName());
        //emailTextView.setText(user.getEmail());
        //identificadorTextView.setText(user.getUid());
        userEmail = user.getEmail();
        textViewNameCompany.setText("Runfo");

        Glide.with(this).load(user.getPhotoUrl()).into(photoImageView);
    }

    private void setMasData(){

        listStatusMas =  new ArrayList<HashMap>();
        listValidUser =  new ArrayList<HashMap>();

        adapter = new ListViewAdapter(this, listStatusMas);

        listView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        fireBaseAuth.addAuthStateListener(fireBaseAuthListener);

        fireBaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("message");
                            myRef.setValue("Hello, World!");
                        }
                    }
                });

        //setMensajeRef();

        setStatusRegisterMAS();

        setListValidUsers();

    }

    /*private void setMensajeRef() {
        mensajeRef = ref.child("mensaje");

        mensajeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);

                mensajeTextView.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void setStatusRegisterMAS() {
        statusRegistersMAS = ref.child("users").getRef();

        statusRegistersMAS.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Get map of users in datasnapshot
                    collectDataRegisters((Map<String,Object>) dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //handle databaseError
                }
        });
    }

    private void collectDataRegisters(Map<String,Object> users) {
        if(this.userEmail !=null
                && this.userEmail.isEmpty()==false && users != null ) {

            Map entrada = users.get(codEmpresa)!=null? (Map) users.get(codEmpresa) : null;

            if(entrada!=null && entrada.isEmpty()==false) {
                Iterator it = entrada.entrySet().iterator();

                listStatusMas.clear();

                while (it.hasNext()) {

                    Map.Entry pair = (Map.Entry) it.next();

                    listStatusMas.add(populateData(pair));
                    adapter.notifyDataSetChanged();

                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
        }

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
        ArrayList<String> temperaturas = new ArrayList<>();

        if(this.userEmail !=null && this.userEmail.isEmpty()==false) {
            Map entrada = (Map) validUsers.get(codEmpresa); //this.userEmail);

            Log.d("USUARIOS", entrada.toString());

            if(entrada!=null && entrada.isEmpty()==false) {
                Iterator it = entrada.entrySet().iterator();

                listValidUser.clear();

                while (it.hasNext()) {

                    Map.Entry pair = (Map.Entry) it.next();

                    Log.d("DATOS_USUARIOS", pair.toString());

                    if( this.validEmail == false && this.userEmail.equalsIgnoreCase(pair.getValue().toString())){
                        Log.d("VALIDO", pair.getValue().toString());
                        this.validEmail = true;
                    }

                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
        }
    }

    private HashMap populateData(Map.Entry pair) {

        Map dataRegister = (Map) pair.getValue();

        Log.d("Datos_Registros: ", pair.getKey() + " = " + dataRegister.toString());

        String orden = getDataOfRegister(dataRegister, "orden");

        String fechaHoraActualizado = getDataOfRegister(dataRegister, "fechaHoraActualizado");

        String estado = getDataOfRegister(dataRegister, "status");

        String nombre = getDataOfRegister(dataRegister, "nombre");

        String temperatura = getDataOfRegister(dataRegister, "valor");

        Log.d("Datos_Obtenidos: ", "id: "+ orden +
                " - fechaHoraActualizado: " + fechaHoraActualizado +
                " - nombre: " + nombre +
                " - temperatura: " + temperatura +
                " - estado: "+estado );

        HashMap temp = new HashMap();

        temp.put("COLUMNA_1", fechaHoraActualizado);
        temp.put("COLUMNA_2", nombre);
        temp.put("COLUMNA_3", temperatura);
        temp.put("COLUMNA_4", estado);

        return temp;
    }

    private String getDataOfRegister(Map dataRegister, String key) {
        String value = "";

        if(dataRegister.get(key)!=null) {
            value = String.valueOf(dataRegister.get(key));
        }
        return value;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(fireBaseAuth!=null){
            fireBaseAuth.removeAuthStateListener(fireBaseAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out: {
                logOut();
                Toast.makeText(this, R.string.signOut, Toast.LENGTH_LONG).show();
                break;
            }
            default: break;
        }
        return true;
    }

    public void logOut() {
        fireBaseAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    goLogInScreen();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.not_close_session, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void revoke() {
        fireBaseAuth.signOut();

        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    goLogInScreen();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.not_revoke, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goLogInScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*public void modificar(View view){
        String mensaje = mensajeEditText.getText().toString();
        mensajeRef.setValue(mensaje);

        mensajeEditText.setText("");
    }*/
}
