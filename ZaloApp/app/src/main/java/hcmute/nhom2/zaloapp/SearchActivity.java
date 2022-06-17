package hcmute.nhom2.zaloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;

import hcmute.nhom2.zaloapp.adapter.SearchAdapter;
import hcmute.nhom2.zaloapp.model.Contact;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class SearchActivity extends AppCompatActivity {
    private EditText searchEdt;
    private ProgressBar progressBar;
    private RecyclerView searchRecyclerView;
    private ImageView backBtn;
    long delay = 500; // 0.5 giây sau khi người dùng dừng nhập
    long last_text_edit = 0; // Thời điểm lần cuối thay đổi giá trị searchEdt
    private LinkedList<Contact> contacts;
    private SearchAdapter searchAdapter;
    public final int PHONE_LENGTH = 10;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Binding();
        Init();
        SetListener();

        // Khởi tạo handler
        Handler handler = new Handler();

        // Gán sự kiến thay đổi giá trị của search edittext
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                }
            }
        });
    }

    //Khởi tạo input_finish_checker kiểm tra người dùng dừng nhâp tin nhắn
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            // Sau 0.5s người dùng dừng nhập tin nhắn thì gọi hàm GetData
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                String kw = searchEdt.getText().toString().trim();
                GetData(kw);
            }
        }
    };

    // Tìm kiếm dựa vào số điện thoại người dùng nhập và hiển thị lên màn hình
    // kw là số điện thoại người dùng nhập
    // Nếu không tìm thấy thì hiện rỗng
    public void GetData(String kw) {
        if (kw.length() == PHONE_LENGTH) {
            progressBar.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.GONE);
            contacts.clear();
            db.collection(Constants.KEY_COLLECTION_USERS).document(kw)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Tạo contact và gán thông tin người dùng cho contact
                                    Contact contact = new Contact();
                                    contact.setPhone(document.getId());
                                    contact.setName(document.getString(Constants.KEY_Name));
                                    contact.setImage(document.getString(Constants.KEY_Image));
                                    contact.setActive(document.getBoolean(Constants.KEY_Active));
                                    // Thêm contact vào contacts
                                    contacts.add(contact);
                                }
                                else {
                                    Toast.makeText(SearchActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                            searchAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            searchRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.GONE);
            contacts.clear();
            searchAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Lắng nghe sự kiện cho backBtn
    private void SetListener() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Khởi tạo giá trị cho biến
    private void Init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        contacts = new LinkedList<>();
        searchAdapter = new SearchAdapter(this, contacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        searchRecyclerView.setLayoutManager(linearLayoutManager);
        searchRecyclerView.setAdapter(searchAdapter);

    }

    // Ánh xạ
    private void Binding() {
        searchEdt = findViewById(R.id.search);
        progressBar = findViewById(R.id.progressBar);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        backBtn = findViewById(R.id.backBtn);
    }
}