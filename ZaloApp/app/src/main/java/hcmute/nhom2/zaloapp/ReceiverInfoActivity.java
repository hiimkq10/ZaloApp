package hcmute.nhom2.zaloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;

public class ReceiverInfoActivity extends AppCompatActivity {
    private ImageView receiverImage;
    private TextView receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_info);
        Binding();
        LoadReceiverInfo();
    }

    // Load thông tin người nhận
    private void LoadReceiverInfo() {
        Intent intent = getIntent();
        Contact receiver = (Contact) intent.getSerializableExtra("receiver");

        // Hiển thị tên người nhận
        receiverName.setText(receiver.getName());

        // Tham chiếu đến ảnh người nhận trên firebase storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(receiver.getImage());

        // Hiển thị hình ảnh người nhận
        Glide.with(ReceiverInfoActivity.this).load(storageReference).into(receiverImage);
    }

    // Ánh xạ
    private void Binding() {
        receiverImage = findViewById(R.id.receiverImage);
        receiverName = findViewById(R.id.receiverName);
    }
}