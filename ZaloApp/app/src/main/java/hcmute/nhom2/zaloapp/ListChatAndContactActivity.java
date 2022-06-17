package hcmute.nhom2.zaloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import hcmute.nhom2.zaloapp.fragment.ListChatFragment;
import hcmute.nhom2.zaloapp.fragment.ListContactFragment;
import hcmute.nhom2.zaloapp.fragment.ListNotificationFragment;
import hcmute.nhom2.zaloapp.utilities.Constants;
import hcmute.nhom2.zaloapp.utilities.PreferenceManager;

public class ListChatAndContactActivity extends BaseActivity implements Loading{
    private Toolbar myToolBar;
    private TextView myToolBarTitle;
    private BottomNavigationView bottomNavigationView;
    private PreferenceManager preferenceManager;
    private ShapeableImageView tbUserImage;
    private ProgressBar progressBar;
    private FragmentContainerView fragmentContainerView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_chat_and_contact);

        // Khởi tạo firestore db
        db = FirebaseFirestore.getInstance();

        // Khởi tạo preferenceManager
        preferenceManager = new PreferenceManager(ListChatAndContactActivity.this);
//        preferenceManager.putString(Constants.KEY_Image, "avatar.png");
//        preferenceManager.putString(Constants.KEY_PhoneNum, "0123456789");
//        preferenceManager.putString(Constants.KEY_Name, "Quang");
//        preferenceManager.putString(Constants.KEY_PhoneNum, "1123456789");
//        preferenceManager.putString(Constants.KEY_Name, "Thanh Hai");

        myToolBar = findViewById(R.id.my_toolbar);
        // Tạo action bar từ toolbar trong layout
        SetUpToolBar();

        myToolBarTitle = findViewById(R.id.toolbarTitle);
        progressBar = findViewById(R.id.progressBar);
        fragmentContainerView = findViewById(R.id.fragment_container);

        // Hiện thị ListChatFragment trong fragmentContainerView
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListChatFragment.class, null)
                .setReorderingAllowed(true)
                .commit();

        bottomNavigationView = findViewById(R.id.my_bottom_navigation);
        // Cài đặt Bottom Navigation
        SetUpBottomNavigation();

        // Tham chiếu đến ảnh người dùng trên firebase storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(preferenceManager.getString(Constants.KEY_Image));
        tbUserImage = findViewById(R.id.tb_user_image);

        // Hiển thị ảnh người dùng
        GlideApp.with(ListChatAndContactActivity.this)
                .load(storageReference)
                .into(tbUserImage);

        // Lắng nghe sự kiện
        SetListener();

    }

    // Tạo action bar từ toolbar trong layout
    public void SetUpToolBar() {
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    // Cài đặt Bottom Navigation
    public void SetUpBottomNavigation() {
        // Cài đặt sự kiện click cho các menu item của bottom navigation
        // Tùy vào id của item menu mà hiển thị fragment tương ứng
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
                switch (id) {
                    case R.id.menu_message:
                        if (!(fragment instanceof ListChatFragment))
                        {
                            fragmentContainerView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, ListChatFragment.class, null)
                                    .setReorderingAllowed(true)
                                    .commit();
                            myToolBarTitle.setText(R.string.menu_message);
                            return true;
                        }
                        break;
                    case R.id.menu_contact:
                        if (!(fragment instanceof ListContactFragment))
                        {
                            fragmentContainerView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, ListContactFragment.class, null)
                                    .setReorderingAllowed(true)
                                    .commit();
                            myToolBarTitle.setText(R.string.menu_contact);
                            return true;
                        }
                        break;
                    case R.id.menu_notification:
                        if (!(fragment instanceof ListNotificationFragment))
                        {
                            fragmentContainerView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, ListNotificationFragment.class, null)
                                    .setReorderingAllowed(true)
                                    .commit();
                            myToolBarTitle.setText(R.string.menu_notification);
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });
    }

    // Lắng nghe sự kiện
    private void SetListener() {
        tbUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent account = new Intent(ListChatAndContactActivity.this, AccountSettingActivity.class);
                startActivity(account);
            }
        });

        // Gán event lắng nghe khi người dùng có thông báo mới
        db.collection(Constants.KEY_COLLECTION_Notifications)
                .whereEqualTo(Constants.KEY_To + "." + Constants.KEY_PhoneNum, preferenceManager.getString(Constants.KEY_PhoneNum))
                .addSnapshotListener(eventListener);
    }

    // Lấy số lượng thông báo người dùng chưa đọc
    private int getBadgesNumber() {
        // Khởi tạo itemView tham chiếu đến item menu notification trong Bottom Navigation
        BottomNavigationItemView itemView = (BottomNavigationItemView) bottomNavigationView.getChildAt(3);
        // Tham chiếu đên badge của item menu notification
        BadgeDrawable badge =  bottomNavigationView.getOrCreateBadge(R.id.menu_notification);
        if (badge.isVisible()) {
            return badge.getNumber();
        }
        return 0;
    }

    // Gán số lượng thông báo người dùng chưa đọc
    private void setBadgesNumber(int num) {
        // Khởi tạo itemView tham chiếu đến item menu notification trong Bottom Navigation
        BottomNavigationItemView itemView = (BottomNavigationItemView) bottomNavigationView.getChildAt(3);
        // Tham chiếu đên badge của item menu notification
        BadgeDrawable badge =  bottomNavigationView.getOrCreateBadge(R.id.menu_notification);
        // Gán giá trị của badge bằng num
        if (num == 0) {
            badge.setVisible(false);
        }
        else {
            if (!badge.isVisible()) {
                badge.setVisible(true);
            }
        }
        badge.setNumber(num);
    }

    // Tạo event lắng nghe khi người dùng có thông báo mới và hiển thị số lượng thông báo chưa đọc
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int num = getBadgesNumber();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    num++;
                }
                else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                    if (num != 0) {
                        num--;
                    }
                }
            }
            setBadgesNumber(num);
        }
    });

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(ListChatAndContactActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    // Kiểm tra xem các fragment đã load hết nội dung hay chưa
    // Nếu rồi thì ẩn progressbar và hiện fragment container
    @Override
    public void loading(Boolean isLoading) {
        if (!isLoading) {
            this.progressBar.setVisibility(View.GONE);
            this.fragmentContainerView.setVisibility(View.VISIBLE);
        }
    }
}