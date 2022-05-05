package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.content.Intent;
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

import java.util.LinkedList;

import hcmute.nhom2.zaloapp.ChatActivity;
import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {
    private final LinkedList<Contact> contacts;
    private LayoutInflater mInflater;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private PreferenceManager preferenceManager;

    public ContactListAdapter(Context context, LinkedList<Contact> contacts) {
        this.mInflater = LayoutInflater.from(context);
        this.contacts = contacts;
        this.context = context;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image, active;
        public final TextView name;
        final ContactListAdapter adapter;

        public ContactViewHolder(@NonNull View itemView, ContactListAdapter adapter) {
            super(itemView);
            this.image = itemView.findViewById(R.id.userContactImage);
            this.active = itemView.findViewById(R.id.userContactActive);
            this.name = itemView.findViewById(R.id.userContactName);
            this.adapter = adapter;
        }
    }

    @NonNull
    @Override
    public ContactListAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactListAdapter.ContactViewHolder holder, int position) {
        Contact mCurrent = this.contacts.get(position);
        holder.name.setText(mCurrent.getName());
        StorageReference storageReference = firebaseStorage.getReference()
                        .child(Constants.KEY_COLLECTION_USERS)
                        .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                        .child(mCurrent.getImage());
        Glide.with(holder.itemView.getContext()).load(storageReference).into(holder.image);
        if (!mCurrent.isActive()) {
            holder.active.setVisibility(View.INVISIBLE);
//            holder.active.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
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
        return contacts.size();
    }
}
