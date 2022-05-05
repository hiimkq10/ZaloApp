package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import hcmute.nhom2.zaloapp.ChatActivity;
import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.Chat;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>{
    private final LinkedList<Chat> chats;
    private LayoutInflater mInflater;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private Context context;
    private PreferenceManager preferenceManager;

    public ChatListAdapter(Context context, LinkedList<Chat> chatList) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.chats = chatList;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        public final TextView name, message, timestamp;
        public final ImageView image, active;
        final ChatListAdapter adapter;

        public ChatViewHolder(@NonNull View itemView, ChatListAdapter adapter) {
            super(itemView);
            this.name = itemView.findViewById(R.id.userName);
            this.message = itemView.findViewById(R.id.userMessage);
            this.timestamp = itemView.findViewById(R.id.userTimestamp);
            this.image = itemView.findViewById(R.id.userImage);
            this.active = itemView.findViewById(R.id.userActive);
            this.adapter = adapter;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat mCurrent = this.chats.get(position);
        holder.name.setText(mCurrent.getName());
        holder.message.setText(mCurrent.getLatestChat());
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
        holder.timestamp.setText(formatter.format(mCurrent.getTimestamp()));

        if (mCurrent.getRead() != null && !mCurrent.getRead()) {
            holder.name.setTypeface(Typeface.DEFAULT_BOLD);
            holder.message.setTypeface(Typeface.DEFAULT_BOLD);
            holder.message.setTextColor(context.getResources().getColor(R.color.black));
            holder.timestamp.setTypeface(Typeface.DEFAULT_BOLD);
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.black));
        }
        else {
            holder.name.setTypeface(Typeface.DEFAULT);
            holder.message.setTypeface(Typeface.DEFAULT);
            holder.message.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
            holder.timestamp.setTypeface(Typeface.DEFAULT);
            holder.timestamp.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        }
        if (mCurrent.getImage() != null) {
            StorageReference storageReference = firebaseStorage.getReference()
                    .child(Constants.KEY_COLLECTION_USERS)
                    .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                    .child(mCurrent.getImage());
            Glide.with(holder.itemView.getContext()).load(storageReference).into(holder.image);
        }

        if (!mCurrent.isActive()) {
            holder.active.setVisibility(View.INVISIBLE);
        }
        else {
            holder.active.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Constants.KEY_User, mCurrent);
                final String flag;
                if (preferenceManager.getString(Constants.KEY_PhoneNum).compareTo(mCurrent.getPhone()) < 0) {
                    flag = preferenceManager.getString(Constants.KEY_PhoneNum) + "_" + mCurrent.getPhone();
                }
                else {
                    flag = mCurrent.getPhone() + "_" + preferenceManager.getString(Constants.KEY_PhoneNum);
                }

                db.collection(Constants.KEY_Rooms)
                        .whereEqualTo(flag, true)
                        .limit(1)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                        db.collection(Constants.KEY_Rooms)
                                                .document(documentSnapshot.getId())
                                                .update(Constants.KEY_COLLECTION_USERS + "."
                                                        + preferenceManager.getString(Constants.KEY_PhoneNum)
                                                        + "." + Constants.KEY_Read, true);
                                    }
                                }
                            }
                        });

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
