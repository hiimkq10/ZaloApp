package hcmute.nhom2.zaloapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    Button btnLogin, btnRegis;
    PreferenceManager preferenceManager ;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Ánh xạ control từ file xml
        btnLogin = findViewById(R.id.btnLoginMain);
        btnRegis = findViewById(R.id.btnRegisMain);
        //Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        //Tạo preferenceManager tham chiếu đến SharedPreferences
        preferenceManager = new PreferenceManager(getApplicationContext());
        //Kiểm tra có tài khoản đang đăng nhập không
        checkExist();
        //Chuyển đến trang Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
            }
        });
        //Chuyển đến trang Đăng ký
        btnRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regis = new Intent(MainActivity.this, RegisActivity.class);
                startActivity(regis);
            }
        });
    }
    //Kiểm tra có tài khoản đang đăng nhập không
    private void checkExist(){
        //Lấy số điện thoại đang đăng nhập vào hệ thống được lưu trữ trong với khóa "PhoneNum"
        String phone = preferenceManager.getString("PhoneNum");
        if( phone != null){
            SaveUser(phone); //Lưu người dùng đang đăng nhập
            Intent home = new Intent(MainActivity.this, ListChatAndContactActivity.class);
            startActivity(home);
        }
    }

    //Lưu người dùng đang đăng nhập
    private void SaveUser(String PhoneNum){

        db.collection("Users").document(PhoneNum)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task< DocumentSnapshot > task) {
                String Phone = PhoneNum.toString().trim();
                if (task.getResult().exists()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    String name = doc.getString("Name"); //Tên người dùng
                    String image = doc.getString("Image");// Tên file ảnh đại diện
                    List<String> listFriends = (List<String>) doc.get("ListFriends"); //Danh sách bạn bè

                    Set<String> friends = new HashSet<String>();
                    if( listFriends != null)
                    {
                        friends.addAll(listFriends);
                    }else{
                        friends.addAll(Collections.EMPTY_SET);
                    }

                    preferenceManager.putString("Name", name);
                    preferenceManager.putString("Image", image);
                    preferenceManager.putString("PhoneNum", Phone);
                    preferenceManager.putStringSet("ListFriends", friends);
                }
            }
        });
    }
}