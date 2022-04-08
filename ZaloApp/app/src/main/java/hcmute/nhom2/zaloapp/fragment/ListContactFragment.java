package hcmute.nhom2.zaloapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Random;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.adapter.ContactListAdapter;
import hcmute.nhom2.zaloapp.model.Contact;

public class ListContactFragment extends Fragment {
    private RecyclerView recyclerView;
    private final LinkedList<Contact> contacts = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_contact, container, false);
        recyclerView = view.findViewById(R.id.recyclerviewContacts);

        Random random = new Random();
        int j;
        for(int i = 0; i < 20; i++) {
            j = random.nextInt(2);
            if (j == 1) {
                contacts.add(new Contact("Nguyễn Xuân Phúc", true, R.drawable.anh34));
            }
            else
            {
                contacts.add(new Contact("Nguyễn Xuân Phúc", false, R.drawable.anh34));
            }
        }

        ContactListAdapter adapter = new ContactListAdapter(getContext(), contacts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return view;
    }
}