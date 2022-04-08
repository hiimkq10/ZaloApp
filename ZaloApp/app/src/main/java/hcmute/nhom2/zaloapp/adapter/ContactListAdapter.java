package hcmute.nhom2.zaloapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import hcmute.nhom2.zaloapp.R;
import hcmute.nhom2.zaloapp.model.Contact;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {
    private final LinkedList<Contact> contacts;
    private LayoutInflater mInflater;

    public ContactListAdapter(Context context, LinkedList<Contact> contacts) {
        this.mInflater = LayoutInflater.from(context);
        this.contacts = contacts;
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
        holder.image.setImageResource(mCurrent.getImage());
        if (!mCurrent.isActive()) {
            holder.active.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
