package hcmute.nhom2.zaloapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class UpdateAccountActivity extends AppCompatActivity {

    EditText edtNameEdit, edtBirthEdit;
    RadioButton rbtnMaleEdit, rbtnFeMaleEdit;
    CircleImageView imgAvtEdit;
    ImageView imgCoverImageEdit, back;
    Button btnUpdate;
    PreferenceManager preferenceManager;
    FirebaseFirestore db ;
    StorageReference storageReference;
    ActivityResultLauncher<String> takePhotoAvt, takePhotoBg;
    Uri imageUriAvt, imageUriBg;
    ProgressDialog progressDialog;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account);

        AnhXa();

        //Init Firebase
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Users/UserImages/");
        storage = FirebaseStorage.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        takePhotoAvt = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgAvtEdit.setImageURI(result);
                        imageUriAvt = result;
                    }
                });

        imgAvtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhotoAvt.launch("image/*");
            }
        });

        takePhotoBg = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgCoverImageEdit.setImageURI(result);
                        imageUriBg = result;
                    }
                });

        imgCoverImageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhotoBg.launch("image/*");
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAccount();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        String PhoneNum = preferenceManager.getString("PhoneNum");
        db.collection("Users").document(PhoneNum)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task< DocumentSnapshot > task) {
                        if (task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();

                            assert doc != null;
                            String name = doc.getString("Name");
                            String gender = doc.getString("Gender");
                            String birth = doc.getString("Birth");
                            String image = doc.getString("Image");
                            String coverImage = doc.getString("CoverImage");

                            edtNameEdit.setText(name);
                            edtBirthEdit.setText(birth);
                            if(gender.equals("Male")) {
                                rbtnMaleEdit.setChecked(true);
                            }else {
                                rbtnFeMaleEdit.setChecked(true);
                            }
                            if(imageUriAvt != null ){
                                imgAvtEdit.setImageURI(imageUriAvt);
                            }else {
                                loadImage(image, imgAvtEdit);
                            }
                            if(imageUriBg != null){
                                imgCoverImageEdit.setImageURI(imageUriBg);
                            }else {
                                loadImage(coverImage, imgCoverImageEdit);
                            }

                        }else{
                            Toast.makeText(UpdateAccountActivity.this, "Không tồn tại thông tin", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
    private void AnhXa(){
        edtNameEdit = findViewById(R.id.edtNameEdit);
        edtBirthEdit = findViewById(R.id.edtBirthEdit);
        rbtnFeMaleEdit = findViewById(R.id.rbtnFemaleEdit);
        rbtnMaleEdit = findViewById(R.id.rbtnMaleEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        imgAvtEdit = findViewById(R.id.imgAvtEdit);
        imgCoverImageEdit = findViewById(R.id.imgCoverImageEdit);
        back = findViewById(R.id.back_setting2);
    }

    private void updateAccount(){
        String avt = "Image", bg = "CoverImage";
        String PhoneNum = preferenceManager.getString("PhoneNum");
        if(edtNameEdit.getText().toString().equals("")) {
            Toast.makeText(UpdateAccountActivity.this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();

        }else if(!rbtnFeMaleEdit.isChecked() && !rbtnMaleEdit.isChecked()){
            Toast.makeText(UpdateAccountActivity.this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();

        }else{
            db.collection("Users").document(PhoneNum)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("Name", edtNameEdit.getText().toString());
                    if (rbtnFeMaleEdit.isChecked()) {
                        data.put("Gender", "Female");
                    } else {
                        data.put("Gender", "Male");
                    }
                    data.put("Birth", edtBirthEdit.getText().toString());
                    if (imageUriAvt != null){
                        uploadToFirebase(imageUriAvt,avt);
                    }
                    if (imageUriBg != null){
                        uploadToFirebase(imageUriBg,bg);
                    }
                    db.collection("Users").document(PhoneNum)
                            .update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(UpdateAccountActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                Intent info = new Intent(UpdateAccountActivity.this, AccountSettingActivity.class);
                                startActivity(info);
                            }
                        })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Lỗi cập nhật", e.getMessage());
                                }
                            });
                }
            });
        }
    }

    private void uploadToFirebase(Uri uri, String imageType){
        String PhoneNum = preferenceManager.getString("PhoneNum");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading file.....");
        progressDialog.show();

        String name = UUID.randomUUID().toString();
        StorageReference fileRef = storageReference.child(name + "." + getFileExtension(uri));
        String filename = name + "." + getFileExtension(uri);
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, Object> data = new HashMap<>();
                        data.put(imageType, filename);
                        db.collection("Users").document(PhoneNum).update(data);

                        Toast.makeText(UpdateAccountActivity.this,"Đổi ảnh thành công", Toast.LENGTH_SHORT).show();
                        imgAvtEdit.setImageResource(R.drawable.anh1);
                    }
                });
            }

        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(UpdateAccountActivity.this,"Đổi ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    private void loadImage(String imageName, ImageView imageView){
        StorageReference storageReference = storage.getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(imageName);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String downloadUrl = uri.toString();
                Glide.with(getApplicationContext()).load(downloadUrl).into(imageView);
            }
        });
    }
    private void updateImageAnywhere(String PhoneNum, String fileName){
        String var = "Users." + PhoneNum + ".Image";
    }
}
