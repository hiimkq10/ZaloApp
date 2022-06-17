package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.ChatMessage;
import hcmute.nhom2.zaloapp.utilities.Constants;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LinkedList<ChatMessage> messages;
    private LayoutInflater inflater;
    private String senderPhoneNum;
    private String receiverImage;
    private FirebaseStorage firebaseStorage;
    private RecyclerView.LayoutManager layoutManager;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public MessageAdapter(Context context, LinkedList<ChatMessage> messages, RecyclerView.LayoutManager layoutManager, String senderPhoneNum, String receiverImage) {
        this.inflater = LayoutInflater.from(context);
        this.messages = messages;
        this.senderPhoneNum = senderPhoneNum;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.receiverImage = receiverImage;
        this.layoutManager = layoutManager;
    }

    // ReceivedMessageViewHolder lưu thông tin ánh xạ cho tin nhắn người dùng nhận được
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        public final TextView textMessage, textTimeStamp;
        public final ImageView imageMessage;
        public final ShapeableImageView receiverImage;
        final MessageAdapter adapter;

        public ReceivedMessageViewHolder(@NonNull View itemView, MessageAdapter adapter) {
            super(itemView);
            this.textMessage = itemView.findViewById(R.id.textMessage);
            this.textTimeStamp = itemView.findViewById(R.id.textTimeStamp);
            this.receiverImage = itemView.findViewById(R.id.receiverImage);
            this.imageMessage = itemView.findViewById(R.id.ImageMessage);
            this.adapter = adapter;
        }
    }

    // SentMessageViewHolder lưu thông tin ánh xạ cho tin nhắn người dùng gửi đi
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        public final TextView textMessage, textTimeStamp;
        public final ImageView imageMessage;
        final MessageAdapter adapter;

        public SentMessageViewHolder(@NonNull View itemView, MessageAdapter adapter) {
            super(itemView);
            this.textMessage = itemView.findViewById(R.id.textMessage);
            this.imageMessage = itemView.findViewById(R.id.ImageMessage);
            this.textTimeStamp = itemView.findViewById(R.id.textTimeStamp);
            this.adapter = adapter;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Kiểm tra loại tin nhắn là gửi đi hay nhận được
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(this.inflater
                    .inflate(R.layout.sent_message, parent, false), this);
        }
        else {
            return new ReceivedMessageViewHolder(this.inflater
                    .inflate(R.layout.recieved_message, parent, false), this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Hiển thị thông tin message theo loại message
        ChatMessage mCurrent = messages.get(position);
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).textMessage.requestLayout();
            ((SentMessageViewHolder) holder).textTimeStamp.setText(getReadableDateTime(mCurrent.getTimestamp()));
            if (mCurrent.getType().equals("Text")){
                ((SentMessageViewHolder) holder).textMessage.setVisibility(View.VISIBLE);
                ((SentMessageViewHolder) holder).imageMessage.setVisibility(View.GONE);

                ((SentMessageViewHolder) holder).textMessage.setText(mCurrent.getContent().trim());
            }
            else{
                ((SentMessageViewHolder) holder).textMessage.setVisibility(View.GONE);
                ((SentMessageViewHolder) holder).imageMessage.setVisibility(View.VISIBLE);

                StorageReference storageReference = firebaseStorage.getReference()
                        .child(Constants.KEY_COLLECTION_USERS)
                        .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                        .child(mCurrent.getContent());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String downloadUrl = uri.toString();
                        Glide.with(holder.itemView.getContext()).load(downloadUrl).into(((SentMessageViewHolder) holder).imageMessage);
                    }
                });
            }

            // Sự kiện click: khi click vào message thì hiển thị ngày nhắn
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((SentMessageViewHolder) holder).textTimeStamp.getVisibility() == View.GONE) {
                        ((SentMessageViewHolder) holder).textTimeStamp.setVisibility(View.VISIBLE);
                    }
                    else {
                        ((SentMessageViewHolder) holder).textTimeStamp.setVisibility(View.GONE);
                    }
                    avoidAutoScroll();
                }
            });
        }
        else {
            ((ReceivedMessageViewHolder) holder).textMessage.requestLayout();
            ((ReceivedMessageViewHolder) holder).textMessage.setText(mCurrent.getContent().trim());
            ((ReceivedMessageViewHolder) holder).textTimeStamp.setText(getReadableDateTime(mCurrent.getTimestamp()));
            StorageReference storageReference = firebaseStorage.getReference()
                    .child(Constants.KEY_COLLECTION_USERS)
                    .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                    .child(this.receiverImage);
            Glide.with(holder.itemView.getContext()).load(storageReference).into(((ReceivedMessageViewHolder) holder).receiverImage);
            if (mCurrent.getType().equals("Text")){
                ((ReceivedMessageViewHolder) holder).textMessage.setVisibility(View.VISIBLE);
                ((ReceivedMessageViewHolder) holder).imageMessage.setVisibility(View.GONE);

                ((ReceivedMessageViewHolder) holder).textMessage.setText(mCurrent.getContent().trim());
            }
            else{
                ((ReceivedMessageViewHolder) holder).textMessage.setVisibility(View.GONE);
                ((ReceivedMessageViewHolder) holder).imageMessage.setVisibility(View.VISIBLE);

                StorageReference storage = firebaseStorage.getReference()
                        .child(Constants.KEY_COLLECTION_USERS)
                        .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                        .child(mCurrent.getContent());
                storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String downloadUrl = uri.toString();
                        Glide.with(holder.itemView.getContext()).load(downloadUrl).into(((ReceivedMessageViewHolder) holder).imageMessage);
                    }
                });
            }

            // Sự kiện click: khi click vào message thì hiển thị ngày nhắn
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((ReceivedMessageViewHolder) holder).textTimeStamp.getVisibility() == View.GONE) {
                        ((ReceivedMessageViewHolder) holder).textTimeStamp.setVisibility(View.VISIBLE);
                    }
                    else {
                        ((ReceivedMessageViewHolder) holder).textTimeStamp.setVisibility(View.GONE);
                    }
                    avoidAutoScroll();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Phân loại view
    // Có 2 loại: SENT và RECEIVED
    // SENT: tin nhắn do người dùng gửi
    // RECEIVED: tin nhắn người dùng nhận được
    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderPhoneNum().equals(this.senderPhoneNum)) {
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // Hàm giúp sữa lỗi recycler view tự scrool
    private void avoidAutoScroll(){
        // bottomItem phần tử cuối recycler view đang hiển thị
        View bottomItem = layoutManager.getChildAt(layoutManager.getChildCount() - 1);
        int bottomItemPos = layoutManager.getPosition(bottomItem);
        int bottomOffset = layoutManager.getDecoratedTop(bottomItem);
        // scrool đến vị trí bottomItem
        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(bottomItemPos, bottomOffset);
    }

    // Format ngày theo định dạng mong muốn
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd yyyy -- hh:mm", Locale.getDefault()).format(date);
    }
}
