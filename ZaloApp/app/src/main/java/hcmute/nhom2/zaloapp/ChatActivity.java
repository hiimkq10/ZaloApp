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
    // ProgressBar s??? xu???t hi???n khi tin nh???n ??ang ???????c load.
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinkedList<ChatMessage> messages;
    private MessageAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private View rootView;
    // Bi???n scrooledToBottom gi??p nh???n bi???t recyclerview ???? t???ng ???????c scrool ?????n ????y hay ch??a.
    private Boolean scrooledToBottom = false;

    ActivityResultLauncher<String> takePhoto;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Binding();// ??nh x???
        LoadReceiverDetails();// Load th??ng tin ng?????i nh???n tin nh???n v?? hi???n th??? t??n, h??nh ???nh
        SetListeners();// Kh???i t???o s??? ki???n button, rootView
        Init();// Kh???i t???o c??c bi???n
        ListenMessages();// G???n event l???ng nghe tin nh???n m???i cho Room c???a ng?????i d??ng.

        SelectImage();// Ch???n h??nh ???nh g???i
    }

    // Kh???i t???o c??c bi???n
    private void Init() {
        // Kh???i t???o m???ng tin nh???n
        this.messages = new LinkedList<>();

        // Kh???i t???o preferenceManager
        this.preferenceManager = new PreferenceManager(getApplicationContext());

        // Kh???i t???o linearLayoutManager, adapter, g??n layout manager v?? adapter cho recyclerview
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

        // Kh???i t???o Firebase Firestore
        db = FirebaseFirestore.getInstance();
    }

    // G??n event l???ng nghe tin nh???n m???i cho Room c???a ng?????i d??ng.
    private void ListenMessages() {
        // Bi???n flag t????ng t??? id room c???a ng?????i d??ng, gi??p tham chi???u ?????n room ????
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

    // Event l???ng nghe xem n???u c?? tin nh???n m???i g???i ?????n ng?????i d??ng th?? hi???n th??? tin nh???n ????
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = this.messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    // Kh???i t???o tin nh???n v?? g??n n???i dung cho tin nh???n t??? document
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderPhoneNum(documentChange.getDocument().getString(Constants.KEY_SenderPhoneNum));
                    chatMessage.setType(documentChange.getDocument().getString(Constants.KEY_Type));
                    chatMessage.setContent(documentChange.getDocument().getString(Constants.KEY_Content));
                    chatMessage.setTimestamp(documentChange.getDocument().getDate(Constants.KEY_Timestamp));
                    this.messages.addLast(chatMessage);
                }
            }
            // S???p x???p m???ng tin nh???n theo th???i gian nh???n v?? hi???n th???
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

    // G???i tin nh???n
    private void SendMessage() {
        // L???y d??? li???u t??? inputMessage v?? g???n v??o Map message
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SenderPhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum));
        message.put(Constants.KEY_Type, "Text");
        message.put(Constants.KEY_Content, inputMessage.getText().toString());
        message.put(Constants.KEY_Timestamp, new Date());

        // Bi???n flag t????ng t??? id room c???a ng?????i d??ng, gi??p tham chi???u ?????n room ????
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
                            // N???u room t???n t???i th?? g???i tin nh???n l??n room, n???u kh??ng th?? t???o room r???i g???i tin nh???n
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

    // ??nh x???
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

    // Load th??ng tin ng?????i nh???n tin nh???n v?? hi???n th??? t??n, h??nh ???nh
    private void LoadReceiverDetails() {
        // L???y th??ng tin ng?????i nh???n t??? intent
        receiver = (Contact) getIntent().getSerializableExtra(Constants.KEY_User);

        // Hi???n th??? t??n
        receiverName.setText(receiver.getName());

        // T???o tham chi???u ?????n ???nh ng?????i nh???n tr??n Firebase storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(receiver.getImage());

        // Hi???n th??? ???nh
        Glide.with(ChatActivity.this).load(storageReference).into(receiverImage);
    }

    // Kh???i t???o s??? ki???n button, rootView
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
                    // G???i tin nh???n
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

        // Fix l???i RecyclerView kh??ng scrool ?????n button khi soft keyboard xu???t hi???n
        // rootView s??? li??n t???c ki???m tra ????? cao rootView hi???n t???i ????? x??c ?????nh soft keyboard c?? xu???t hi???n tr??n m??n h??nh kh??ng
        // khi soft keyboard hi???n l??n, n???u scrooledToBottom l?? false,
        // l??m recyclerView scrool ?????n bottom v?? g??n scrooledToBottom l?? true
        // Khi soft keyboard bi???n m???t, g??n scrooledToBottom l?? false
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
    //Ch???n h??nh ???nh
    public void SelectImage()
    {
        //L???y uri ???nh ???????c ch???n ????? g???i
        takePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imageUri = result;
                        SendImage(imageUri);//G???i ???nh
                    }
                });
        //L???y bitmap ???nh ch???p t??? m??y ???nh
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() !=null) {
                    Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                    imageUri = getImageUri(ChatActivity.this,image);//Chuy???n bitmap snag uri
                    SendImage(imageUri);// G???i ???nh
                }
            }
        });
        //T??y ch???n g???i ???nh t??? th?? m???c image ho???c t??? m??y ???nh
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = {"Th?? vi???n", "M??y ???nh", "????ng"};
                AlertDialog.Builder b = new AlertDialog.Builder(ChatActivity.this);
                b.setTitle("Ch???n ???nh t???:");
                b.setItems(items, new DialogInterface.OnClickListener() {
                    //X??? l?? s??? ki???n
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("M??y ???nh"))
                        {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            activityResultLauncher.launch(intent);
                        }
                        else if (items[which].equals("Th?? vi???n"))
                        {
                            takePhoto.launch("image/*");
                        }
                        else if (items[which].equals("????ng")) {
                            dialog.dismiss();
                        }
                    }
                });
                b.show();
            }
        });
    }
    //G???i tin nh???n d???ng h??nh ???nh
    private void SendImage(Uri uri) {
        String name = UUID.randomUUID().toString(); // T???o ng???u nhi??n t??n ???nh
        StorageReference fileRef = FirebaseStorage.getInstance().getReference("Users/UserImages/").child(name + "." + getFileExtension(uri));
        String filename = name + "." + getFileExtension(uri);// T??n ???nh ?????y ?????
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
                                    chat.put(Constants.KEY_Content, "[H??nh ???nh]"); // ?????t tin nh???n cu???i c??ng
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
    //L???y ph???n m??? r???ng t???p
    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }
    public static final int CAMERA_CODE = 123;
    //L???y uri c???a ???nh t??? Bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}