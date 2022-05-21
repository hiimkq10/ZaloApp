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
import hcmute.nhom2.zaloapp.adapter.NotificationAdapter;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.model.FriendRequest;
import hcmute.nhom2.zaloapp.model.Notification;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ListNotificationFragment extends Fragment {
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private View view;
    private Loading loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_list_notification, container, false);
        this.loading = (Loading) getActivity();
        preferenceManager = new PreferenceManager(getContext());
        db = FirebaseFirestore.getInstance();
        GetDataFromFireStore();
        // Inflate the layout for this fragment
        return view;
    }

    public void GetDataFromFireStore() {
        CollectionReference colRef = db.collection(Constants.KEY_COLLECTION_Notifications);

        colRef.whereEqualTo(Constants.KEY_To + "." + Constants.KEY_PhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinkedList<Notification> notifications = new LinkedList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().update(Constants.KEY_Read, true);
                                Contact sender = new Contact();
                                sender.setPhone(document.getString(Constants.KEY_From + "." + Constants.KEY_PhoneNum));
                                sender.setName(document.getString(Constants.KEY_From + "." + Constants.KEY_Name));
                                sender.setImage(document.getString(Constants.KEY_From + "." + Constants.KEY_Image));

                                Contact receiver = new Contact();
                                receiver.setPhone(document.getString(Constants.KEY_To + "." + Constants.KEY_PhoneNum));
                                receiver.setName(document.getString(Constants.KEY_To + "." + Constants.KEY_Name));
                                receiver.setImage(document.getString(Constants.KEY_To + "." + Constants.KEY_Image));

                                if (document.getString(Constants.KEY_Type).equals(Constants.KEY_NOTIFICATION_TYPE_FriendRequest)) {
                                    Notification notification = new FriendRequest();
                                    notification.setId(document.getId());
                                    notification.setType(document.getString(Constants.KEY_Type));
                                    ((FriendRequest) notification).setSender(sender);
                                    ((FriendRequest) notification).setReceiver(receiver);
                                    notifications.add(notification);
                                }
                            }
                            RecyclerView recyclerView = view.findViewById(R.id.recyclerviewNotifications);
                            NotificationAdapter adapter = new NotificationAdapter(getContext(), notifications);
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