package hcmute.nhom2.zaloapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.LinkedList;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.adapter.ChatListAdapter;
import hcmute.nhom2.zaloapp.model.Chat;

public class ListChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private final LinkedList<Chat> chats = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_chat, container, false);
        recyclerView = view.findViewById(R.id.recyclerviewChats);

        for(int i = 0; i < 15; i++) {
            chats.add(new Chat("Nguyễn Xuân Phúc", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    new Date(), false, R.drawable.anh34));
        }

        adapter = new ChatListAdapter(getContext(), chats);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return view;
    }
}