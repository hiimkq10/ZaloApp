package hcmute.nhom2.zaloapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

import hcmute.nhom2.zaloapp.Loading;
import hcmute.nhom2.zaloapp.PhoneContactActivity;
import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.adapter.ContactListAdapter;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ListContactFragment extends Fragment {
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private View view;
    // Biến loading kiểm tra tình trạng load dữ liệu đã hoàn thành chưa
    private Loading loading;
    // phoneContactLayout là khung danh bạ, click vào sẽ chuyển đến trang danh bạ
    private View phoneContactLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_list_contact, container, false);

        phoneContactLayout = view.findViewById(R.id.phone_contacts_layout);

        // Sự kiện click chuyển đến trang danh bạ
        phoneContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PhoneContactActivity.class);
                startActivity(intent);
            }
        });

        // Khởi tạo loading là ListChatAndContactActivity
        this.loading = (Loading) getActivity();
        preferenceManager = new PreferenceManager(getContext());
        db = FirebaseFirestore.getInstance();

        // Load contact từ Firestore
        GetDataFromFireStore();

        // Inflate the layout for this fragment
        return this.view;
    }

    // Load contact từ Firestore
    public void GetDataFromFireStore() {
        // colRef tham chiếm đến collection users trên Firestore
        CollectionReference colRef = db.collection(Constants.KEY_COLLECTION_USERS);

        // Lấy danh sách document có kết bạn với người dùng
        colRef.whereArrayContains(Constants.KEY_ListFriends, preferenceManager.getString(Constants.KEY_PhoneNum))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // contacts: danh sách bạn bè
                    LinkedList<Contact> contacts = new LinkedList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // contact lưu dữ liệu người dùng
                        Contact contact = new Contact();
                        contact.setPhone(document.getId());
                        contact.setName(document.getString(Constants.KEY_Name));
                        contact.setImage(document.getString(Constants.KEY_Image));
                        contact.setActive(document.getBoolean(Constants.KEY_Active));
                        // Thêm contact vào contacts
                        contacts.add(contact);
                    }
                    // Khởi tạo recyclerView, adapter, layout manager và hiển thị danh sách bạn bè
                    RecyclerView recyclerView = view.findViewById(R.id.recyclerviewContacts);
                    ContactListAdapter adapter = new ContactListAdapter(getContext(), contacts);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    // Cập nhật đã load xong
                    loading.loading(false);
                }
                else {
                    Log.d("GetDataError", "Error getting documents: ", task.getException());
                }
            }
        });
    }

}