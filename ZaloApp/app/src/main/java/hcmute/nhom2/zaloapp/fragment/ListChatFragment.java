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

import hcmute.nhom2.zaloapp.Loading;
import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.adapter.ChatListAdapter;
import hcmute.nhom2.zaloapp.adapter.SimpleContactAdapter;
import hcmute.nhom2.zaloapp.model.Chat;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ListChatFragment extends Fragment {
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private View view;
    private Loading loading;

    private RecyclerView recyclerViewChats;
    private Boolean recyclerViewChatsLoaded = false;
    private RecyclerView recyclerViewContacts;
    private Boolean recyclerViewContactsLoaded = false;
    private ChatListAdapter adapter;
    private SimpleContactAdapter simpleContactAdapter;
    private LinkedList<Chat> chats;
    private LinkedList<Contact> simpleContacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_list_chat, container, false);
        loading = (Loading) getActivity();
        Binding();
        Init();
        ListenMessages();
        // Inflate the layout for this fragment
        return view;
    }

    private void Binding() {
        recyclerViewChats = view.findViewById(R.id.recyclerviewChats);
        recyclerViewContacts = view.findViewById(R.id.recyclerviewContacts);
    }

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

    public void ListenMessages() {
        db.collection(Constants.KEY_Rooms)
                .orderBy(Constants.KEY_COLLECTION_USERS + "." + preferenceManager.getString(Constants.KEY_PhoneNum))
                .addSnapshotListener(eventListener);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereArrayContains(Constants.KEY_ListFriends, preferenceManager.getString(Constants.KEY_PhoneNum))
                .addSnapshotListener(contactsEventListeners);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                HashMap<String, Object> user = (HashMap<String, Object>) documentChange.getDocument().getData().get(Constants.KEY_COLLECTION_USERS);

                Chat chat = new Chat();
                for (String key : user.keySet()) {
                    if (key.equals(preferenceManager.getString(Constants.KEY_PhoneNum))) {
                        HashMap<String, Object> sender = (HashMap<String, Object>) user.get(key);
                        chat.setRead((Boolean) sender.get(Constants.KEY_Read));
                    }
                    else {
                        chat.setPhone(key);
                        HashMap<String, Object> receiver = (HashMap<String, Object>) user.get(key);
                        chat.setName((String) receiver.get(Constants.KEY_Name));
                        chat.setImage((String) receiver.get(Constants.KEY_Image));
                        chat.setActive((Boolean) receiver.get(Constants.KEY_Active));
                    }
                }
                HashMap<String, Object> latestChat = (HashMap<String, Object>) documentChange.getDocument().getData().get(Constants.KEY_LatestChat);
                chat.setLatestChat(String.valueOf(latestChat.get(Constants.KEY_Content)));
                Timestamp timestamp = (Timestamp) latestChat.get(Constants.KEY_Timestamp);
                chat.setTimestamp(timestamp.toDate());

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    this.chats.add(chat);
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    int position = -1;
                    for (int i = 0; i < chats.size(); i++) {
                        if (user.containsKey(chats.get(i).getPhone())) {
                            position = i;
                            break;
                        }
                    }
                    this.chats.set(position, chat);
                }
            }
            Collections.sort(this.chats, (obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));
            this.adapter.notifyDataSetChanged();
            this.recyclerViewChatsLoaded = true;
        }
        loading.loading(this.recyclerViewChatsLoaded && this.recyclerViewContactsLoaded);
    });

    private final EventListener<QuerySnapshot> contactsEventListeners = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                Contact contact = new Contact();
                contact.setPhone(documentChange.getDocument().getId());
                contact.setImage(documentChange.getDocument().getString(Constants.KEY_Image));
                contact.setName(documentChange.getDocument().getString(Constants.KEY_Name));
                contact.setActive(documentChange.getDocument().getBoolean(Constants.KEY_Active));

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    if (contact.isActive()){
                        this.simpleContacts.add(contact);
                    }
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
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
                            this.simpleContacts.add(contact);
                        }
                    }
                    else {
                        this.simpleContacts.remove(position);
                    }
                }
            }
            this.simpleContactAdapter.notifyDataSetChanged();
            this.recyclerViewContactsLoaded = true;
        }
        loading.loading(this.recyclerViewChatsLoaded && this.recyclerViewContactsLoaded);
    });
}