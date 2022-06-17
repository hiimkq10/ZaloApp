package hcmute.nhom2.zaloapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference userDocumentReference;
    private CollectionReference roomCollectionReference;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Tạo preferenceManager tham chiếu đến SharedPreferences
        preferenceManager = new PreferenceManager(getApplicationContext());

        //Khởi tạo Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Tạo tham chiếu đến document thông tin cá nhân của người dùng hiện tại
        userDocumentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_PhoneNum));

        //Tạo tham chiếu đến collection Room
        roomCollectionReference = db.collection(Constants.KEY_Rooms);
    }

    // Cập nhật trạng thái hoạt đông là false khi activity không còn được focus
    @Override
    protected void onPause() {
        super.onPause();
        // Cập nhật active ở document user là false
        userDocumentReference.update(Constants.KEY_Active, false);

        // Cập nhật active ở collection room là false
        roomCollectionReference.orderBy(Constants.KEY_COLLECTION_USERS + "." + preferenceManager.getString(Constants.KEY_PhoneNum))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                documentSnapshot.getReference()
                                        .update(Constants.KEY_COLLECTION_USERS + "."
                                                + preferenceManager.getString(Constants.KEY_PhoneNum) + "."
                                                + Constants.KEY_Active, false);
                            }
                        }
                    }
                });
    }

    // Cập nhật trạng thái hoạt đông là true khi activity được focus trở lại
    @Override
    protected void onResume() {
        super.onResume();

        // Cập nhật active ở document user là true
        userDocumentReference.update(Constants.KEY_Active, true);

        // Cập nhật active ở collection room là true
        roomCollectionReference.orderBy(Constants.KEY_COLLECTION_USERS + "." + preferenceManager.getString(Constants.KEY_PhoneNum))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                documentSnapshot.getReference()
                                        .update(Constants.KEY_COLLECTION_USERS + "."
                                                + preferenceManager.getString(Constants.KEY_PhoneNum) + "."
                                                + Constants.KEY_Active, true);
                            }
                        }
                    }
                });
    }
}
