package hcmute.nhom2.zaloapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import hcmute.nhom2.zaloapp.adapter.MessageAdapter;
import hcmute.nhom2.zaloapp.model.ChatMessage;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ChatActivity extends BaseActivity {
    private Contact receiver;
    private ShapeableImageView receiverImage;
    private TextView receiverName;
    private ImageButton btnBack, btnInfo, btnSend, btnImage;
    private EditText inputMessage;
    // ProgressBar sẽ xuất hiện khi tin nhắn đang được load.
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinkedList<ChatMessage> messages;
    private MessageAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private View rootView;
    // Biến scrooledToBottom giúp nhận biết recyclerview đã từng được scrool đến đáy hay chưa.
    private Boolean scrooledToBottom = false;

    ActivityResultLauncher<String> takePhoto;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Binding();// Ánh xạ
        LoadReceiverDetails();// Load thông tin người nhận tin nhắn và hiển thị tên, hình ảnh
        SetListeners();// Khởi tạo sự kiện button, rootView
        Init();// Khởi tạo các biến
        ListenMessages();// Gắn event lắng nghe tin nhắn mới cho Room của người dùng.

        SelectImage();// Chọn hình ảnh gửi
    }

    // Khởi tạo các biến
    private void Init() {
        // Khởi tạo mảng tin nhắn
        this.messages = new LinkedList<>();

        // Khởi tạo preferenceManager
        this.preferenceManager = new PreferenceManager(getApplicationContext());

        // Khởi tạo linearLayoutManager, adapter, gán layout manager và adapter cho recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        this.adapter = new MessageAdapter(getApplicationContext(),
                messages,
                linearLayoutManager,
                preferenceManager.getString(Constants.KEY_PhoneNum),
                receiver.getImage());
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.setAdapter(this.adapter);

        // Khởi tạo Firebase Firestore
        db = FirebaseFirestore.getInstance();
    }

    // Gán event lắng nghe tin nhắn mới cho Room của người dùng.
    private void ListenMessages() {
        // Biến flag tương tự id room của người dùng, giúp tham chiếu đến room đó
        final String flag;
        if (preferenceManager.getString(Constants.KEY_PhoneNum).compareTo(this.receiver.getPhone()) < 0) {
            flag = preferenceManager.getString(Constants.KEY_PhoneNum) + "_" + this.receiver.getPhone();
        }
        else {
            flag = this.receiver.getPhone() + "_" + preferenceManager.getString(Constants.KEY_PhoneNum);
        }

        db.collection(Constants.KEY_Rooms)
                .whereEqualTo(flag, true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                recyclerView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                documentSnapshot.getReference().collection(Constants.KEY_SUB_COLLECTION_Chats).addSnapshotListener(eventListener);
                            }
                        }
                    }
                });
    }

    // Event lắng nghe xem nếu có tin nhắn mới gửi đến người dùng thì hiển thị tin nhắn đó
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = this.messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    // Khởi tạo tin nhắn và gán nội dung cho tin nhắn từ document
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderPhoneNum(documentChange.getDocument().getString(Constants.KEY_SenderPhoneNum));
                    chatMessage.setType(documentChange.getDocument().getString(Constants.KEY_Type));
                    chatMessage.setContent(documentChange.getDocument().getString(Constants.KEY_Content));
                    chatMessage.setTimestamp(documentChange.getDocument().getDate(Constants.KEY_Timestamp));
                    this.messages.addLast(chatMessage);
                }
            }
            // Sắp xếp mảng tin nhắn theo thời gian nhắn và hiển thị
            Collections.sort(messages, (obj1, obj2) -> obj1.getTimestamp().compareTo(obj2.getTimestamp()));
            if (count == 0) {
                this.adapter.notifyDataSetChanged();
                this.recyclerView.scrollToPosition(this.messages.size() - 1);
            } else {
                this.adapter.notifyItemRangeInserted(this.messages.size(), this.messages.size());
                this.recyclerView.scrollToPosition(this.messages.size() - 1);
            }
            this.recyclerView.setVisibility(View.VISIBLE);
        }
        this.progressBar.setVisibility(View.GONE);
    });

    // Gửi tin nhắn
    private void SendMessage() {
        // Lấy dữ liệu từ inputMessage và gắn vào Map message
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SenderPhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum));
        message.put(Constants.KEY_Type, "Text");
        message.put(Constants.KEY_Content, inputMessage.getText().toString());
        message.put(Constants.KEY_Timestamp, new Date());

        // Biến flag tương tự id room của người dùng, giúp tham chiếu đến room đó
        final String flag;
        if (preferenceManager.getString(Constants.KEY_PhoneNum).compareTo(this.receiver.getPhone()) < 0) {
            flag = preferenceManager.getString(Constants.KEY_PhoneNum) + "_" + this.receiver.getPhone();
        }
        else {
            flag = this.receiver.getPhone() + "_" + preferenceManager.getString(Constants.KEY_PhoneNum);
        }

        db.collection(Constants.KEY_Rooms)
                .whereEqualTo(flag, true)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Nếu room tồn tại thì gửi tin nhắn lên room, nếu không thì tạo room rồi gửi tin nhắn
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String id = documentSnapshot.getId();

                                    HashMap<String, Object> chat = new HashMap<>();
                                    chat.put(Constants.KEY_Content, message.get(Constants.KEY_Content));
                                    chat.put(Constants.KEY_Timestamp, message.get(Constants.KEY_Timestamp));

                                    HashMap<String, Object> updates = new HashMap<>();
                                    updates.put(Constants.KEY_COLLECTION_USERS + "." + receiver.getPhone() + "." + Constants.KEY_Read, false);
                                    updates.put(Constants.KEY_LatestChat, chat);

                                    db.collection(Constants.KEY_Rooms)
                                            .document(id)
                                            .update(updates)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    db.collection(Constants.KEY_Rooms)
                                                            .document(id)
                                                            .collection(Constants.KEY_SUB_COLLECTION_Chats)
                                                            .add(message);
                                                }
                                            });
                                }

                            }
                            else {
                                HashMap<String, Object> sender = new HashMap<>();
                                sender.put(Constants.KEY_Name, preferenceManager.getString(Constants.KEY_Name));
                                sender.put(Constants.KEY_Image, preferenceManager.getString(Constants.KEY_Image));
                                sender.put(Constants.KEY_Read, true);
                                sender.put(Constants.KEY_Active, true);

                                HashMap<String, Object> recieverUser = new HashMap<>();
                                recieverUser.put(Constants.KEY_Name, receiver.getName());
                                recieverUser.put(Constants.KEY_Image, receiver.getImage());
                                recieverUser.put(Constants.KEY_Read, false);
                                recieverUser.put(Constants.KEY_Active, receiver.isActive());

                                HashMap<String, Object> users = new HashMap<>();
                                users.put(preferenceManager.getString(Constants.KEY_PhoneNum), sender);
                                users.put(receiver.getPhone(), recieverUser);

                                HashMap<String, Object> chat = new HashMap<>();
                                chat.put(Constants.KEY_Content, message.get(Constants.KEY_Content));
                                chat.put(Constants.KEY_Timestamp, message.get(Constants.KEY_Timestamp));

                                HashMap<String, Object> room = new HashMap<>();
                                room.put(flag, true);
                                room.put(Constants.KEY_COLLECTION_USERS, users);
                                room.put(Constants.KEY_LatestChat, chat);
                                db.collection(Constants.KEY_Rooms)
                                        .add(room)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.collection(Constants.KEY_SUB_COLLECTION_Chats)
                                                        .add(message);
                                                documentReference.collection(Constants.KEY_SUB_COLLECTION_Chats)
                                                        .addSnapshotListener(eventListener);
                                            }
                                        });
                            }
                        }
                        else {
                            Log.d("Send Message Failed", "get failed with ", task.getException());
                        }
                    }
                });

    }

    // Ánh xạ
    private void Binding() {
        receiverImage = findViewById(R.id.receiverImage);
        receiverName = findViewById(R.id.receiverName);
        btnBack = findViewById(R.id.btnBack);
        btnInfo = findViewById(R.id.btnInfo);
        btnSend = findViewById(R.id.btnSend);
        btnImage = findViewById(R.id.btnImage);
        inputMessage = findViewById(R.id.inputMessage);
        recyclerView = findViewById(R.id.recyclerviewMessages);
        progressBar = findViewById(R.id.progressBar);
        rootView = findViewById(R.id.rootView);
    }

    // Load thông tin người nhận tin nhắn và hiển thị tên, hình ảnh
    private void LoadReceiverDetails() {
        // Lấy thông tin người nhận từ intent
        receiver = (Contact) getIntent().getSerializableExtra(Constants.KEY_User);

        // Hiển thị tên
        receiverName.setText(receiver.getName());

        // Tạo tham chiếu đến ảnh người nhận trên Firebase storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(receiver.getImage());

        // Hiển thị ảnh
        Glide.with(ChatActivity.this).load(storageReference).into(receiverImage);
    }

    // Khởi tạo sự kiện button, rootView
    private void SetListeners() {

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputMessage.getText().toString().trim().equals("")) {
                    // Gửi tin nhắn
                    SendMessage();
                    inputMessage.setText(null);
                }
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ReceiverInfoActivity.class);
                intent.putExtra("receiver", receiver);
                startActivity(intent);
            }
        });

        // Fix lỗi RecyclerView không scrool đến button khi soft keyboard xuất hiện
        // rootView sẽ liên tục kiểm tra độ cao rootView hiện tại để xác định soft keyboard có xuất hiện trên màn hình không
        // khi soft keyboard hiện lên, nếu scrooledToBottom là false,
        // làm recyclerView scrool đến bottom và gán scrooledToBottom là true
        // Khi soft keyboard biến mất, gán scrooledToBottom là false
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int rootViewHeight = rootView.getRootView().getHeight();
                int heightDiff = rootViewHeight - rootView.getHeight();
                if (heightDiff > rootViewHeight * 0.15) {
                    if (adapter.getItemCount() > 0 && !scrooledToBottom) {
                        recyclerView.scrollToPosition(messages.size() - 1);
                        scrooledToBottom = true;
                    }
                }
                else {
                    scrooledToBottom = false;
                }
            }
        });
    }

    //    private String getReadableDateTime(Date date) {
//        return new SimpleDateFormat("MMMM dd, yyyy -- hh:mm a", Locale.getDefault()).format(date);
//    }
    //Chọn hình ảnh
    public void SelectImage()
    {
        //Lấy uri ảnh được chọn để gửi
        takePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imageUri = result;
                        SendImage(imageUri);//Gửi ảnh
                    }
                });
        //Lấy bitmap ảnh chụp từ máy ảnh
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() !=null) {
                    Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                    imageUri = getImageUri(ChatActivity.this,image);//Chuyển bitmap snag uri
                    SendImage(imageUri);// Gửi ảnh
                }
            }
        });
        //Tùy chọn gửi ảnh từ thư mục image hoặc từ máy ảnh
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = {"Thư viện", "Máy ảnh", "Đóng"};
                AlertDialog.Builder b = new AlertDialog.Builder(ChatActivity.this);
                b.setTitle("Chọn ảnh từ:");
                b.setItems(items, new DialogInterface.OnClickListener() {
                    //Xử lý sự kiện
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("Máy ảnh"))
                        {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            activityResultLauncher.launch(intent);
                        }
                        else if (items[which].equals("Thư viện"))
                        {
                            takePhoto.launch("image/*");
                        }
                        else if (items[which].equals("Đóng")) {
                            dialog.dismiss();
                        }
                    }
                });
                b.show();
            }
        });
    }
    //Gửi tin nhắn dạng hình ảnh
    private void SendImage(Uri uri) {
        String name = UUID.randomUUID().toString(); // Tạo ngẫu nhiên tên ảnh
        StorageReference fileRef = FirebaseStorage.getInstance().getReference("Users/UserImages/").child(name + "." + getFileExtension(uri));
        String filename = name + "." + getFileExtension(uri);// Tên ảnh đầy đủ
        fileRef.putFile(uri);

        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SenderPhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum));
        message.put(Constants.KEY_Type, "Image");
        message.put(Constants.KEY_Content, filename);
        message.put(Constants.KEY_Timestamp, new Date());

        final String flag;
        if (preferenceManager.getString(Constants.KEY_PhoneNum).compareTo(this.receiver.getPhone()) < 0) {
            flag = preferenceManager.getString(Constants.KEY_PhoneNum) + "_" + this.receiver.getPhone();
        }
        else {
            flag = this.receiver.getPhone() + "_" + preferenceManager.getString(Constants.KEY_PhoneNum);
        }

        db.collection(Constants.KEY_Rooms)
                .whereEqualTo(flag, true)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    String id = documentSnapshot.getId();

                                    HashMap<String, Object> chat = new HashMap<>();
                                    chat.put(Constants.KEY_Content, "[Hình ảnh]"); // Đặt tin nhắn cuối cùng
                                    chat.put(Constants.KEY_Timestamp, message.get(Constants.KEY_Timestamp));

                                    HashMap<String, Object> updates = new HashMap<>();
                                    updates.put(Constants.KEY_COLLECTION_USERS + "." + receiver.getPhone() + "." + Constants.KEY_Read, false);
                                    updates.put(Constants.KEY_LatestChat, chat);

                                    db.collection(Constants.KEY_Rooms)
                                            .document(id)
                                            .update(updates)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    db.collection(Constants.KEY_Rooms)
                                                            .document(id)
                                                            .collection(Constants.KEY_SUB_COLLECTION_Chats)
                                                            .add(message);
                                                }
                                            });
                                }

                            }
                            else {
                                HashMap<String, Object> sender = new HashMap<>();
                                sender.put(Constants.KEY_Name, preferenceManager.getString(Constants.KEY_Name));
                                sender.put(Constants.KEY_Image, preferenceManager.getString(Constants.KEY_Image));
                                sender.put(Constants.KEY_Read, true);
                                sender.put(Constants.KEY_Active, true);

                                HashMap<String, Object> recieverUser = new HashMap<>();
                                recieverUser.put(Constants.KEY_Name, receiver.getName());
                                recieverUser.put(Constants.KEY_Image, receiver.getImage());
                                recieverUser.put(Constants.KEY_Read, false);
                                recieverUser.put(Constants.KEY_Active, receiver.isActive());

                                HashMap<String, Object> users = new HashMap<>();
                                users.put(preferenceManager.getString(Constants.KEY_PhoneNum), sender);
                                users.put(receiver.getPhone(), recieverUser);

                                HashMap<String, Object> chat = new HashMap<>();
                                chat.put(Constants.KEY_Content, message.get(Constants.KEY_Content));
                                chat.put(Constants.KEY_Timestamp, message.get(Constants.KEY_Timestamp));

                                HashMap<String, Object> room = new HashMap<>();
                                room.put(flag, true);
                                room.put(Constants.KEY_COLLECTION_USERS, users);
                                room.put(Constants.KEY_LatestChat, chat);
                                db.collection(Constants.KEY_Rooms)
                                        .add(room)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.collection(Constants.KEY_SUB_COLLECTION_Chats)
                                                        .add(message);
                                                documentReference.collection(Constants.KEY_SUB_COLLECTION_Chats)
                                                        .addSnapshotListener(eventListener);
                                            }
                                        });
                            }
                        }
                        else {
                            Log.d("Send Message Failed", "get failed with ", task.getException());
                        }
                    }
                });

    }
    //Lấy phần mở rộng tệp
    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }
    public static final int CAMERA_CODE = 123;
    //Lấy uri của ảnh từ Bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}