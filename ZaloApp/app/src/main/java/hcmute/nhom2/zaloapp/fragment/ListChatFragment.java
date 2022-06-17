package hcmute.nhom2.zaloapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import hcmute.nhom2.zaloapp.ConcreteBuilder.ChatConcreteBuilder;
import hcmute.nhom2.zaloapp.Loading;
import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.adapter.ChatListAdapter;
import hcmute.nhom2.zaloapp.adapter.SimpleContactAdapter;
import hcmute.nhom2.zaloapp.builder.ChatBuilder;
import hcmute.nhom2.zaloapp.model.Chat;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ListChatFragment extends Fragment {
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private View view;
    // Biến loading kiểm tra tình trạng load dữ liệu đã hoàn thành chưa
    private Loading loading;

    // recyclerViewChats hiển thị danh sách chat (ảnh, tên người nhận, đoạn chat cuối cùng)
    private RecyclerView recyclerViewChats;
    // recyclerViewChatsLoaded lưu tình trạng load dữ liệu chat
    private Boolean recyclerViewChatsLoaded = false;
    // recyclerViewContacts hiển thị thông tin người dùng gồm ảnh, tên, tình trạng hoạt động
    private RecyclerView recyclerViewContacts;
    // recyclerViewContactsLoaded lưu tình trạng load dữ liệu contact
    private Boolean recyclerViewContactsLoaded = false;
    // ChatListAdapter cho recyclerViewChats
    private ChatListAdapter adapter;
    // SimpleContactAdapter cho recyclerViewContacts
    private SimpleContactAdapter simpleContactAdapter;
    // chats: danh sách chat
    private LinkedList<Chat> chats;
    // simpleContacts: danh sách contact
    private LinkedList<Contact> simpleContacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_list_chat, container, false);

        // Khởi tạo loading là ListChatAndContactActivity
        loading = (Loading) getActivity();

        // Ánh xạ
        Binding();

        // Khởi tạo biến, set adapter, layout manager
        Init();

        // Lắng nghe thay đổi từ firestore
        ListenMessages();
        // Inflate the layout for this fragment
        return view;
    }

    // Ánh xạ
    private void Binding() {
        recyclerViewChats = view.findViewById(R.id.recyclerviewChats);
        recyclerViewContacts = view.findViewById(R.id.recyclerviewContacts);
    }

    // Khởi tạo biến, set adapter, layout manager
    private void Init() {
        this.chats = new LinkedList<>();
        this.simpleContacts = new LinkedList<>();
        preferenceManager = new PreferenceManager(getContext());
        adapter = new ChatListAdapter(getContext(), chats);
        recyclerViewChats.setAdapter(adapter);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        simpleContactAdapter = new SimpleContactAdapter(getContext(), simpleContacts);
        recyclerViewContacts.setAdapter(simpleContactAdapter);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        db = FirebaseFirestore.getInstance();
    }

    // Lắng nghe thay đổi từ firestore
    public void ListenMessages() {
        // Gán Event lắng nghe xem có tin nhắn mói gửi đến không
        db.collection(Constants.KEY_Rooms)
                .orderBy(Constants.KEY_COLLECTION_USERS + "." + preferenceManager.getString(Constants.KEY_PhoneNum))
                .addSnapshotListener(eventListener);

        // Gán Event lắng nghe xem có người dùng nào trong danh sách bạn online không
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereArrayContains(Constants.KEY_ListFriends, preferenceManager.getString(Constants.KEY_PhoneNum))
                .addSnapshotListener(contactsEventListeners);
    }

    // Event lắng nghe xem có tin nhắn mói gửi đến không
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                // user lưu thông tin người gửi, người nhận
                HashMap<String, Object> user = (HashMap<String, Object>) documentChange.getDocument().getData().get(Constants.KEY_COLLECTION_USERS);
                // chatBuilder giúp khởi tạo chat
                ChatBuilder chatBuilder = new ChatConcreteBuilder();
                for (String key : user.keySet()) {
                    // Kiểm tra là người gửi hay người nhận do chat chỉ lưu người gửi
                    if (key.equals(preferenceManager.getString(Constants.KEY_PhoneNum))) {
                        HashMap<String, Object> sender = (HashMap<String, Object>) user.get(key);
                        chatBuilder = chatBuilder.setRead((Boolean) sender.get(Constants.KEY_Read));
                    }
                    else {
                        chatBuilder = chatBuilder.setPhone(key);
                        HashMap<String, Object> receiver = (HashMap<String, Object>) user.get(key);
                        chatBuilder = chatBuilder.setName((String) receiver.get(Constants.KEY_Name));
                        chatBuilder = chatBuilder.setImage((String) receiver.get(Constants.KEY_Image));
                        chatBuilder = chatBuilder.setActive((Boolean) receiver.get(Constants.KEY_Active));
                    }
                }
                // latestChat lưu thông tin tin nhắn cuối cùng gồm nội dung, thời gian nhắn
                HashMap<String, Object> latestChat = (HashMap<String, Object>) documentChange.getDocument().getData().get(Constants.KEY_LatestChat);
                chatBuilder = chatBuilder.setLatestChat(String.valueOf(latestChat.get(Constants.KEY_Content)));
                Timestamp timestamp = (Timestamp) latestChat.get(Constants.KEY_Timestamp);
                chatBuilder = chatBuilder.setTimestamp(timestamp.toDate());
                Chat chat = chatBuilder.build();

                // Nếu loại DocumentChange là Add thì thêm chat vừa tạo vào danh sách chats
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    this.chats.add(chat);
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    // Nếu loại DocumentChange là Modified thì kiểm tra xem thay đổi đó là gì
                    // Nếu là thay đổi ở latest chat hoặc tình trạng đọc tin nhắn thì cập nhật lại danh sách chats
                    for (int i = 0; i < chats.size(); i++) {
                        if (user.containsKey(chats.get(i).getPhone())) {
                            if (!chats.get(i).getLatestChat().equals(chat.getLatestChat()) || chats.get(i).getRead() != chat.getRead()) {
                                this.chats.set(i, chat);
                                break;
                            }
                        }
                    }
                }
            }
            // Sắp xếp mảng chat theo timestamp
            Collections.sort(this.chats, (obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));
            this.adapter.notifyDataSetChanged();
            // Cập nhật đã load xong
            this.recyclerViewChatsLoaded = true;
        }
        // Gọi hàm loading là hàm đã định nghĩa trong ListChatAndContactActivity
        loading.loading(this.recyclerViewChatsLoaded && this.recyclerViewContactsLoaded);
    });

    // Event lắng nghe xem có người dùng nào trong danh sách bạn đang online không
    private final EventListener<QuerySnapshot> contactsEventListeners = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                // contact lưu thông tin người dùng gồm ảnh, tên, tình trạng online
                Contact contact = new Contact();
                contact.setPhone(documentChange.getDocument().getId());
                contact.setImage(documentChange.getDocument().getString(Constants.KEY_Image));
                contact.setName(documentChange.getDocument().getString(Constants.KEY_Name));
                contact.setActive(documentChange.getDocument().getBoolean(Constants.KEY_Active));

                // Nếu DocumentChange là Add thì thêm contact mới vào simpleContacts
                // Nếu DocumentChange là Modified thì cập nhật lại simpleContacts
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    if (contact.isActive()){
                        this.simpleContacts.add(contact);
                    }
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    // position vị trí contact trong simpleContacts
                    int position = -1;
                    for (int i = 0; i < simpleContacts.size(); i++) {
                        if (contact.getPhone().equals(simpleContacts.get(i).getPhone())) {
                            position = i;
                            break;
                        }
                    }
                    if (contact.isActive()) {
                        if (position != -1)
                        {
                            this.simpleContacts.set(position, contact);
                        }
                        else {
                            // Contact không có trong simpleContacts
                            this.simpleContacts.add(contact);
                        }
                    }
                    else {
                        // Nếu Contact có tình trạng off thì xóa contact khỏi simpleContacts
                        this.simpleContacts.remove(position);
                    }
                }
            }
            this.simpleContactAdapter.notifyDataSetChanged();
            // Cập nhật đã load xong
            this.recyclerViewContactsLoaded = true;
        }
        // Gọi hàm loading là hàm đã định nghĩa trong ListChatAndContactActivity
        loading.loading(this.recyclerViewChatsLoaded && this.recyclerViewContactsLoaded);
    });
}