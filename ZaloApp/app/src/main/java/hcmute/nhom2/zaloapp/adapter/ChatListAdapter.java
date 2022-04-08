package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.Chat;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>{
    private final LinkedList<Chat> chats;
    private LayoutInflater mInflater;

    public ChatListAdapter(Context context, LinkedList<Chat> chatList) {
        mInflater = LayoutInflater.from(context);
        this.chats = chatList;
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        public final TextView name, message, timestamp;
        public final ImageView image;
        final ChatListAdapter adapter;

        public ChatViewHolder(@NonNull View itemView, ChatListAdapter adapter) {
            super(itemView);
            this.name = itemView.findViewById(R.id.userName);
            this.message = itemView.findViewById(R.id.userMessage);
            this.timestamp = itemView.findViewById(R.id.userTimestamp);
            this.image = itemView.findViewById(R.id.userImage);
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
        holder.message.setText(mCurrent.getNewestChat());
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
        holder.timestamp.setText(formatter.format(mCurrent.getTimestamp()));
        holder.image.setImageResource(mCurrent.getImage());
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
