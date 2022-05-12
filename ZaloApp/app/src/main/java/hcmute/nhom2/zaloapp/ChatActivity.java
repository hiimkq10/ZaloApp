package hcmute.nhom2.zaloapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import hcmute.nhom2.zaloapp.adapter.MessageAdapter;
import hcmute.nhom2.zaloapp.model.ChatMessage;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ChatActivity extends BaseActivity {

    private Contact receiver;
    private ShapeableImageView receiverImage;
    private TextView receiverName;
    private ImageButton btnBack, btnInfo, btnSend;
    private EditText inputMessage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinkedList<ChatMessage> messages;
    private MessageAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private View rootView;
    private Boolean scrooledToBottom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Binding();
        LoadReceiverDetails();
        SetListeners();
        Init();
        ListenMessages();
    }

    private void Init() {
        this.messages = new LinkedList<>();
        this.preferenceManager = new PreferenceManager(getApplicationContext());
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
        db = FirebaseFirestore.getInstance();
    }

    private void ListenMessages() {
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

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = this.messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderPhoneNum(documentChange.getDocument().getString(Constants.KEY_SenderPhoneNum));
                    chatMessage.setType(documentChange.getDocument().getString(Constants.KEY_Type));
                    chatMessage.setContent(documentChange.getDocument().getString(Constants.KEY_Content));
                    chatMessage.setTimestamp(documentChange.getDocument().getDate(Constants.KEY_Timestamp));
                    this.messages.addLast(chatMessage);
                }
            }
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

    private void SendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SenderPhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum));
        message.put(Constants.KEY_Type, "Text");
        message.put(Constants.KEY_Content, inputMessage.getText().toString());
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


    private void Binding() {
        receiverImage = findViewById(R.id.receiverImage);
        receiverName = findViewById(R.id.receiverName);
        btnBack = findViewById(R.id.btnBack);
        btnInfo = findViewById(R.id.btnInfo);
        btnSend = findViewById(R.id.btnSend);
        inputMessage = findViewById(R.id.inputMessage);
        recyclerView = findViewById(R.id.recyclerviewMessages);
        progressBar = findViewById(R.id.progressBar);
        rootView = findViewById(R.id.rootView);
    }

    private void LoadReceiverDetails() {
        receiver = (Contact) getIntent().getSerializableExtra(Constants.KEY_User);
        receiverName.setText(receiver.getName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(receiver.getImage());

        Glide.with(ChatActivity.this).load(storageReference).into(receiverImage);
    }

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
}