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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        //??nh x??? control t??? file xml
        AnhXa();

        //Kh???i t???i Firebase
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Users/UserImages/");
        storage = FirebaseStorage.getInstance();

        //T???o preferenceManager tham chi???u ?????n SharedPreferences
        preferenceManager = new PreferenceManager(getApplicationContext());

        //L???y uri ???nh ?????i di???n
        takePhotoAvt = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgAvtEdit.setImageURI(result);
                        imageUriAvt = result;
                    }
                });
        //M??? thu m???c image
        imgAvtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhotoAvt.launch("image/*");
            }
        });
        //L???y uri ???nh b??a
        takePhotoBg = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgCoverImageEdit.setImageURI(result);
                        imageUriBg = result;
                    }
                });
        //M??? thu m???c image
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
        //Tro ve trang truoc
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
        //L???y s??? ??i???n tho???i ??ang ????ng nh???p v??o h??? th???ng ???????c l??u tr??? trong v???i kh??a "PhoneNum"
        String PhoneNum = preferenceManager.getString("PhoneNum");
        //L???y th??gn tin ng?????i d??ng v?? hi???n th???
        db.collection("Users").document(PhoneNum)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task< DocumentSnapshot > task) {
                        if (task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();

                            assert doc != null;
                            String name = doc.getString("Name");    //Ten nguoi dung
                            String gender = doc.getString("Gender");//Gioi tinh
                            String birth = doc.getString("Birth");  //Ngay sinh
                            String image = doc.getString("Image");  //Ten anh dai dien
                            String coverImage = doc.getString("CoverImage");//Ten anh bia

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
                            Toast.makeText(UpdateAccountActivity.this, "Kh??ng t???n t???i th??ng tin", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
    //??nh x??? control t??? file xml
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
    //C???p nh???t th??ng tin ng?????i d??ng
    private void updateAccount(){
        String avt = "Image", bg = "CoverImage";    // Tao bien de phan biet anh dai dien - anh bia
        String PhoneNum = preferenceManager.getString("PhoneNum");  //Lay sdt dang dang nhap vao he thong
        if(edtNameEdit.getText().toString().equals("")) {
            Toast.makeText(UpdateAccountActivity.this, "Vui l??ng nh???p t??n", Toast.LENGTH_SHORT).show();

        }else if(!rbtnFeMaleEdit.isChecked() && !rbtnMaleEdit.isChecked()){
            Toast.makeText(UpdateAccountActivity.this, "Vui l??ng ch???n gi???i t??nh", Toast.LENGTH_SHORT).show();

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
                                updateNameAnywhere(PhoneNum,edtNameEdit.getText().toString());
                                Toast.makeText(UpdateAccountActivity.this, "C???p nh???t th??nh c??ng", Toast.LENGTH_SHORT).show();
                                Intent info = new Intent(UpdateAccountActivity.this, AccountSettingActivity.class);
                                startActivity(info);
                            }
                        })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("L???i c???p nh???t", e.getMessage());
                                }
                            });
                }
            });
        }
    }
    //upload ???nh l??n firebase
    private void uploadToFirebase(Uri uri, String imageType){
        String PhoneNum = preferenceManager.getString("PhoneNum"); //Lay sdt dang dang nhap vao he thong
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading file.....");
        progressDialog.show();

        String name = UUID.randomUUID().toString(); //T???o ng???u nhi??n t??n ???nh
        StorageReference fileRef = storageReference.child(name + "." + getFileExtension(uri));
        String filename = name + "." + getFileExtension(uri); //T??n ???nh ?????y ?????
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, Object> data = new HashMap<>();
                        data.put(imageType, filename);  // C???p nh???t t??n ???nh trong tr?????ng Image/CoverImage
                        db.collection("Users").document(PhoneNum).update(data);
                        updateImageAnywhere(PhoneNum,filename); // C???p nh???t ???nh trong Chats

                        Toast.makeText(UpdateAccountActivity.this,"?????i ???nh th??nh c??ng", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UpdateAccountActivity.this,"?????i ???nh th???t b???i", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //L???y ph???n m??? r???ng t???p
    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }
    //Load l???i imageView sau khi thay ?????i ???nh
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
    //C???p nh???t tr?????ng Image c???a ng?????i d??ng trong Collection Chats
    private void updateImageAnywhere(String PhoneNum, String fileName) {
        db.collection(Constants.KEY_Rooms)
                .orderBy(Constants.KEY_COLLECTION_USERS + "." + PhoneNum).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference documentReference = document.getReference();
                        documentReference.update(Constants.KEY_COLLECTION_USERS + "." + PhoneNum + ".Image", fileName);

                    }
                }
            }
        });
    }
    //C???p nh???t tr?????ng Name c???a ng?????i d??ng trong Collection Chats
    private void updateNameAnywhere(String PhoneNum, String newName) {
        db.collection(Constants.KEY_Rooms)
                .orderBy(Constants.KEY_COLLECTION_USERS + "." + PhoneNum).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference documentReference = document.getReference();
                            documentReference.update(Constants.KEY_COLLECTION_USERS + "." + PhoneNum + ".Name", newName);

                    }
                }
            }
        });
    }
}
