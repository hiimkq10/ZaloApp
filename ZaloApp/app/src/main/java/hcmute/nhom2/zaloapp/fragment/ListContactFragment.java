package hcmute.nhom2.zaloapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.adapter.ContactListAdapter;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ListContactFragment extends Fragment {
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private View view;
    private Loading loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_list_contact, container, false);
        this.loading = (Loading) getActivity();
        preferenceManager = new PreferenceManager(getContext());
        db = FirebaseFirestore.getInstance();
        GetDataFromFireStore();

        // Inflate the layout for this fragment
        return this.view;
    }

    public void GetDataFromFireStore() {
        CollectionReference colRef = db.collection(Constants.KEY_COLLECTION_USERS);

        colRef.whereArrayContains(Constants.KEY_ListFriends, preferenceManager.getString(Constants.KEY_PhoneNum))
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    LinkedList<Contact> contacts = new LinkedList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Contact contact = new Contact();
                        contact.setPhone(document.getId());
                        contact.setName(document.getString(Constants.KEY_Name));
                        contact.setImage(document.getString(Constants.KEY_Image));
                        contact.setActive(document.getBoolean(Constants.KEY_Active));
                        contacts.add(contact);
                    }
                    RecyclerView recyclerView = view.findViewById(R.id.recyclerviewContacts);
                    ContactListAdapter adapter = new ContactListAdapter(getContext(), contacts);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    loading.loading(false);
                }
                else {
                    Log.d("GetDataError", "Error getting documents: ", task.getException());
                }
            }
        });
    }

}