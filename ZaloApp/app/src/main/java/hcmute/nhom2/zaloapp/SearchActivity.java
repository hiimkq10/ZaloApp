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
    long delay = 500; // 0.5 seconds after user stops typing
    long last_text_edit = 0;
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

        Handler handler = new Handler();
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

    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                String kw = searchEdt.getText().toString().trim();
                GetData(kw);
            }
        }
    };

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
                                    Contact contact = new Contact();
                                    contact.setPhone(document.getId());
                                    contact.setName(document.getString(Constants.KEY_Name));
                                    contact.setImage(document.getString(Constants.KEY_Image));
                                    contact.setActive(document.getBoolean(Constants.KEY_Active));
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

    private void SetListener() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void Init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        contacts = new LinkedList<>();
        searchAdapter = new SearchAdapter(this, contacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        searchRecyclerView.setLayoutManager(linearLayoutManager);
        searchRecyclerView.setAdapter(searchAdapter);

    }

    private void Binding() {
        searchEdt = findViewById(R.id.search);
        progressBar = findViewById(R.id.progressBar);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        backBtn = findViewById(R.id.backBtn);
    }
}