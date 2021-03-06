package hcmute.nhom2.zaloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedList;

import hcmute.nhom2.zaloapp.adapter.ContactListAdapter;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class PhoneContactActivity extends AppCompatActivity {
    public static final int REQUEST_READ_CONTACTS = 79;
    private Toolbar toolbar;
    private Cursor cursor;
    // Danh sách số điện thoại trong danh bạ
    private ArrayList<String> phoneList;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_contact);

        // Tạo ActionBar từ toolbar trong layout
        toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle(getResources().getString(R.string.phone_contacts));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = FirebaseFirestore.getInstance();

        phoneList = new ArrayList<>();

        // Kiếm tra quyền đọc danh bạ
        // Nếu đã được cấp quyền thì gọi hàm GetContacts
        // Nếu chưa thì xin quyền
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            GetContacts();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }

        // Nếu load thành công danh bạ thì gọi hàm GetDataFromFireStore
        if (phoneList.size() > 0) {
            GetDataFromFireStore();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Hàm nhận kiểm tra kết quả việc xin quyền
    // Nếu người dùng cấp quyền thì gọi hàm GetContacts
    // Nếu không thì thông báo không load được
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GetContacts();
                } else {
                    Toast.makeText(this, "Bạn không cho phép mở danh bạ", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Hàm lấy các số điện thoại từ danh bạ
    public void GetContacts() {
        // my_phone là số điện thoại của người dùng hiện tại
        // Dùng my_phone để tránh trường hợp số điện thoại của người dùng bị thêm vào phoneList
        String my_phone = preferenceManager.getString(Constants.KEY_PhoneNum);
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                if (cur.getInt(cur.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        // Lấy số điện thoại và thêm vào phoneList
                        String phoneNo = pCur.getString(pCur.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (!phoneNo.equals(my_phone)) {
                            phoneList.add(phoneNo);
                        }
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
    }

    // Hàm lấy thông tin người dùng từ firestore theo phoneList
    public void GetDataFromFireStore() {
        // Tham chiếu đến collection Users
        CollectionReference colRef = db.collection(Constants.KEY_COLLECTION_USERS);

        // Lấy các document có documentId nằm trong phoneList
        colRef.whereIn(FieldPath.documentId(), phoneList)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Tạo List contacts là danh sách người dùng
                            LinkedList<Contact> contacts = new LinkedList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Tạo contact và gán thông tin người dùng cho contact
                                Contact contact = new Contact();
                                contact.setPhone(document.getId());
                                contact.setName(document.getString(Constants.KEY_Name));
                                contact.setImage(document.getString(Constants.KEY_Image));
                                contact.setActive(document.getBoolean(Constants.KEY_Active));
                                // Thêm contact vào contacts
                                contacts.add(contact);
                            }
                            // Tạo recyclerView, adapter, layout manager để hiện thị contacts
                            RecyclerView recyclerView = findViewById(R.id.recyclerviewContacts);
                            ContactListAdapter adapter = new ContactListAdapter(getApplicationContext(), contacts);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        }
                        else {
                            Log.d("GetDataError", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}