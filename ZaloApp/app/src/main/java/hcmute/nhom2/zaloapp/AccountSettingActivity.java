package hcmute.nhom2.zaloapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class AccountSettingActivity extends AppCompatActivity {
    public static final int MY_REQUEST_CODE = 10;

    private TextView infoAccount, editAccount, logout;
    PreferenceManager preferenceManager;
    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        infoAccount = findViewById(R.id.infoAccount);
        editAccount = findViewById(R.id.editAccount);
        logout = findViewById(R.id.logout);

        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        infoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountSettingActivity.this, InfoAccountActivity.class);
                startActivity(intent);
            }
        });
        editAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountSettingActivity.this, UpdateAccountActivity.class);
                startActivity(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }
    private void logout(){
        String PhoneNum = preferenceManager.getString("PhoneNum");
        new AlertDialog.Builder(AccountSettingActivity.this)
                .setMessage("Bạn muốn đăng xuất?")
                .setCancelable(false)
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateActive(PhoneNum);
                        preferenceManager.clear();
                        Intent intent = new Intent(AccountSettingActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }
    private void updateActive(String PhoneNum){
        db.collection(Constants.KEY_COLLECTION_USERS).document(PhoneNum).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> data = new HashMap<>();
                data.put(Constants.KEY_Active, false);
                db.collection(Constants.KEY_COLLECTION_USERS).document(PhoneNum).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });
    }
}
