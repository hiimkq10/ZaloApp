package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder>{
    private final LinkedList<Contact> contacts;
    private LayoutInflater mInflater;
    // context: activity gọi adapter
    private Context context;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private PreferenceManager preferenceManager;

    public SearchAdapter(Context context, LinkedList<Contact> contacts) {
        this.mInflater = LayoutInflater.from(context);
        this.contacts = contacts;
        this.context = context;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
    }

    // SearchViewHolder lưu thông tin ánh xạ
    class SearchViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image, active;
        public final TextView name;
        public final Button sendRequestBtn;
        final SearchAdapter adapter;

        public SearchViewHolder(@NonNull View itemView, SearchAdapter adapter) {
            super(itemView);
            this.image = itemView.findViewById(R.id.userContactImage);
            this.active = itemView.findViewById(R.id.userContactActive);
            this.name = itemView.findViewById(R.id.userContactName);
            this.sendRequestBtn = itemView.findViewById(R.id.sendRequestBtn);
            this.adapter = adapter;
        }
    }


    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.search_item, parent, false);
        return new SearchViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        // Hiển thị thông tin người dùng được tìm kiếm
        Contact mCurrent = this.contacts.get(position);
        holder.name.setText(mCurrent.getName());
        StorageReference storageReference = firebaseStorage.getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(mCurrent.getImage());
        Glide.with(holder.itemView.getContext()).load(storageReference).into(holder.image);
        holder.active.setVisibility(View.INVISIBLE);

        // phoneNum số điện thoại người dùng
        String phoneNum = preferenceManager.getString(Constants.KEY_PhoneNum);

        // friends danh sách bạn của người dùng
        Set<String> friends = preferenceManager.getStringSet(Constants.KEY_ListFriends);

        // Nếu người dùng tự tìm số điện thoại của mình thì chỉ hiện tên, ảnh
        if (mCurrent.getPhone().equals(phoneNum)) {
            holder.sendRequestBtn.setVisibility(View.GONE);
        }
        else{
            // Nếu người dùng được tìm kiếm có trong danh sách bạn bè thì hiển thị tên ảnh, nút Friend
            if (friends.contains(mCurrent.getPhone())) {
                holder.sendRequestBtn.setVisibility(View.VISIBLE);
                holder.sendRequestBtn.setText(context.getString(R.string.friend));
            }
            else {
                // Trường hợp người dùng chưa kết bạn
                // Nếu người dùng được tìm kiếm chưa gửi lời mời kết bạn thì hiển thị tên, ảnh, nút Add Friend
                // Nếu người dùng được tìm kiếm đã gửi lời mời kết bạn thì hiển thị tên, ảnh, nút Accept
                // Nếu người dùng đã gửi lời mời kết bạn cho người dùng được tìm kiếm thì hiển thị tên, ảnh, nút Request Sent
                db.collection(Constants.KEY_COLLECTION_Notifications)
                        .whereEqualTo(Constants.KEY_From + "." + Constants.KEY_PhoneNum, phoneNum)
                        .whereEqualTo(Constants.KEY_To + "." + Constants.KEY_PhoneNum, mCurrent.getPhone())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) {
                                        db.collection(Constants.KEY_COLLECTION_Notifications)
                                                .whereEqualTo(Constants.KEY_To + "." + Constants.KEY_PhoneNum, phoneNum)
                                                .whereEqualTo(Constants.KEY_From + "." + Constants.KEY_PhoneNum, mCurrent.getPhone())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        holder.sendRequestBtn.setVisibility(View.VISIBLE);
                                                        if (queryDocumentSnapshots.isEmpty()) {
                                                            holder.sendRequestBtn.setText(context.getString(R.string.add_friend));
                                                        }
                                                        else {
                                                            holder.sendRequestBtn.setText(context.getString(R.string.accept_request));
                                                        }
                                                    }
                                                });
                                    }
                                    else {
                                        holder.sendRequestBtn.setVisibility(View.VISIBLE);
                                        holder.sendRequestBtn.setText(context.getString(R.string.request_sent));
                                    }
                                }
                            }
                        });
            }
        }

        // Sự kiện click vào sendRequestBtn
        // Nếu sendRequestBtn đang hiển thị add friend thì gửi lời mời kết bạn cho người dùng được tìm kiếm, sendRequestBtn chuyển sang hiển thị Request sent
        // Nếu sendRequestBtn đang hiển thị Request Sent thì xóa lời mời kết bạn, sendRequestBtn chuyển sang hiển thị add friend
        // Nếu sendRequestBtn đang hiển thị Accept thì thêm người dùng vào danh sách bạn, sendRequestBtn chuyển sang hiển thị Friend
        holder.sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.sendRequestBtn.getText().toString().equals(context.getString(R.string.add_friend)))
                {
                    HashMap<String, Object> sender = new HashMap<>();
                    sender.put(Constants.KEY_PhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum));
                    sender.put(Constants.KEY_Name, preferenceManager.getString(Constants.KEY_Name));
                    sender.put(Constants.KEY_Image, preferenceManager.getString(Constants.KEY_Image));

                    HashMap<String, Object> receiver = new HashMap<>();
                    receiver.put(Constants.KEY_PhoneNum, mCurrent.getPhone());
                    receiver.put(Constants.KEY_Name, mCurrent.getName());
                    receiver.put(Constants.KEY_Image, mCurrent.getImage());

                    HashMap<String, Object> notification = new HashMap<>();
                    notification.put(Constants.KEY_Type, Constants.KEY_NOTIFICATION_TYPE_FriendRequest);
                    notification.put(Constants.KEY_From, sender);
                    notification.put(Constants.KEY_To, receiver);
                    notification.put(Constants.KEY_Read, false);

                    db.collection(Constants.KEY_COLLECTION_Notifications).add(notification)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        holder.sendRequestBtn.setText(context.getString(R.string.request_sent));
                                    }
                                }
                            });
                }
                else if (holder.sendRequestBtn.getText().toString().equals(context.getString(R.string.request_sent))) {
                    db.collection(Constants.KEY_COLLECTION_Notifications)
                            .whereEqualTo(Constants.KEY_From + "." + Constants.KEY_PhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum))
                            .whereEqualTo(Constants.KEY_To + "." + Constants.KEY_PhoneNum, mCurrent.getPhone())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                            documentSnapshot.getReference().delete();
                                        }
                                        holder.sendRequestBtn.setText(context.getString(R.string.add_friend));
                                    }
                                }
                            });
                }
                else if (holder.sendRequestBtn.getText().toString().equals(context.getString(R.string.accept_request))){
                    // tham chiếu đến document thông báo của người gửi
                    DocumentReference senderRef = db.collection(Constants.KEY_COLLECTION_USERS)
                            .document(mCurrent.getPhone());
                    // tham chiếu đến document thông báo của người nhận
                    DocumentReference receiverRef = db.collection(Constants.KEY_COLLECTION_USERS)
                            .document(phoneNum);

                    // Cập nhật danh sách bạn
                    db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            // Thêm người nhận vào danh sách bạn của người gửi
                            transaction.update(senderRef, Constants.KEY_ListFriends, FieldValue.arrayUnion(phoneNum));
                            // Thêm người gửi vào danh sách bạn của người nhận
                            transaction.update(receiverRef, Constants.KEY_ListFriends, FieldValue.arrayUnion(mCurrent.getPhone()));
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.collection(Constants.KEY_COLLECTION_Notifications)
                                    .whereEqualTo(Constants.KEY_From + "." + Constants.KEY_PhoneNum, mCurrent.getPhone())
                                    .whereEqualTo(Constants.KEY_To + "." + Constants.KEY_PhoneNum, phoneNum)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                // Xóa lời mời kết bạn
                                                QuerySnapshot querySnapshot = task.getResult();
                                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                                documentSnapshot.getReference().delete();
                                                holder.sendRequestBtn.setText(context.getString(R.string.friend));
                                            }
                                        }
                                    });
                        }
                    });
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
