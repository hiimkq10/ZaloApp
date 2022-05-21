package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.model.FriendRequest;
import hcmute.nhom2.zaloapp.model.Notification;
import hcmute.nhom2.zaloapp.utilities.Constants;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final LinkedList<Notification> notifications;
    private LayoutInflater inflater;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore db;
    private Context context;

    public static final int VIEW_TYPE_FRIEND_REQUEST = 1;

    public NotificationAdapter(Context context, LinkedList<Notification> notifications) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.notifications = notifications;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class FriendRequestViewHolder extends NotificationViewHolder {
        public final ImageView senderImage;
        public final TextView senderName;
        public final Button confirmBtn, deleteBtn;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            senderImage = itemView.findViewById(R.id.senderImage);
            senderName = itemView.findViewById(R.id.senderName);
            confirmBtn = itemView.findViewById(R.id.confirm);
            deleteBtn = itemView.findViewById(R.id.delete);
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FRIEND_REQUEST) {
            return new FriendRequestViewHolder(this.inflater.inflate(R.layout.friend_request, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        Notification mCurrent = notifications.get(position);
        if (getItemViewType(position) == VIEW_TYPE_FRIEND_REQUEST) {
            FriendRequest friendRequest = (FriendRequest) mCurrent;
            FriendRequestViewHolder friendRequestViewHolder = (FriendRequestViewHolder) holder;
            friendRequestViewHolder.senderName.setText(friendRequest.getSender().getName());
            StorageReference storageReference = firebaseStorage.getReference()
                    .child(Constants.KEY_COLLECTION_USERS)
                    .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                    .child(friendRequest.getSender().getImage());
            Glide.with(context).load(storageReference).into(friendRequestViewHolder.senderImage);

            friendRequestViewHolder.confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DocumentReference senderRef = db.collection(Constants.KEY_COLLECTION_USERS)
                            .document(friendRequest.getSender().getPhone());
                    DocumentReference receiverRef = db.collection(Constants.KEY_COLLECTION_USERS)
                            .document(friendRequest.getReceiver().getPhone());
                    db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            transaction.update(senderRef, Constants.KEY_ListFriends, FieldValue.arrayUnion(friendRequest.getReceiver().getPhone()));
                            transaction.update(receiverRef, Constants.KEY_ListFriends, FieldValue.arrayUnion(friendRequest.getSender().getPhone()));
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.collection(Constants.KEY_COLLECTION_Notifications).document(friendRequest.getId()).delete();
                            removeElement(mCurrent);
                        }
                    });
                }
            });

            friendRequestViewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.collection(Constants.KEY_COLLECTION_Notifications).document(friendRequest.getId()).delete();
                    removeElement(mCurrent);
                }
            });
        }
    }

    public void removeElement(Notification notification) {
        notifications.remove(notification);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (notifications.get(position).getType().equals(Constants.KEY_NOTIFICATION_TYPE_FriendRequest)) {
            return VIEW_TYPE_FRIEND_REQUEST;
        }
        else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
