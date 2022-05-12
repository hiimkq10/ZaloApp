package hcmute.nhom2.zaloapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import hcmute.nhom2.zaloapp.utilities.Constants;

public class ForgotPassActivity extends AppCompatActivity {
    EditText edtPhone, edtPass, edtRePass;
    Button btnUpdatePass;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        //Anh xa
        edtPhone = findViewById(R.id.edtPhoneForgotPass);
        edtPass = findViewById(R.id.edtPassForgotPass);
        edtRePass = findViewById(R.id.edtRePassForgotPass);
        btnUpdatePass = findViewById(R.id.btnUpdatePass);

        //Init Firebase
        db = FirebaseFirestore.getInstance();

        btnUpdatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtPhone.getText().toString().equals("")) {
                    Toast.makeText(ForgotPassActivity.this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();

                }else if(edtPass.getText().toString().equals("")){
                    Toast.makeText(ForgotPassActivity.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();

                }else if(!edtRePass.getText().toString().equals(edtPass.getText().toString())){
                    Toast.makeText(ForgotPassActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();

                }else{
                    Map<String, Object> data = new HashMap<>();
                    data.put(Constants.KEY_Password, edtPass.getText().toString());

                    db.collection(Constants.KEY_COLLECTION_USERS).document(edtPhone.getText().toString())
                            .collection(Constants.KEY_SUB_COLLECTION_PrivateData).document(edtPhone.getText().toString())
                            .update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ForgotPassActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            Intent login = new Intent(ForgotPassActivity.this, LoginActivity.class);
                            startActivity(login);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Lỗi cập nhật", e.getMessage());
                        }
                    });
                }
            }
        });
    }
}
