package ar.com.flamengo.huemul.flamengoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference dataBaseReferenceValidUsers;

    private final static String codEmpresa = "1000";

    private String datoQrValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        datoQrValid = "";
    }

    @Override
    public void handleResult(Result result) {
        Log.d("Dato_QR", result.getText());

        String datoQRrecived = result.getText();

        if(datoQRrecived!=null && datoQRrecived.isEmpty()==false
                && datoQrValid!=null && datoQrValid.isEmpty()==false
                && datoQRrecived.equalsIgnoreCase(datoQrValid)==true){
            MainActivity.datoQR = result.getText();
            goMainScreen();
        }else{
            LoginActivity.invalidQR = true;
            goLogInScreen();
        }
    }

    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goLogInScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scannerView.setResultHandler(this);
        scannerView.startCamera();

        setListValidUsers();

    }

    private void setListValidUsers() {

        dataBaseReferenceValidUsers = ref.child("lista_codigo_qr").getRef();

        dataBaseReferenceValidUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get map of users in datasnapshot
                collectDataValidQRs((Map<String,Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        });
    }

    private void collectDataValidQRs(Map<String,Object> validUsers) {

        Map entrada = (Map) validUsers.get(codEmpresa); //this.userEmail);

        if(entrada!=null && entrada.isEmpty()==false) {
            Iterator it = entrada.entrySet().iterator();

            datoQrValid = "";

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                datoQrValid = pair.getValue().toString();

                Log.d("LISTA-QR", "Valor QR: " + datoQrValid);

                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }
}
