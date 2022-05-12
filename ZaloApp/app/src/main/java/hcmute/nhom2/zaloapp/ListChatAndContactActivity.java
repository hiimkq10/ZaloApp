package hcmute.nhom2.zaloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import hcmute.nhom2.zaloapp.fragment.ListChatFragment;
import hcmute.nhom2.zaloapp.fragment.ListContactFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_chat_and_contact);

        preferenceManager = new PreferenceManager(ListChatAndContactActivity.this);
//        preferenceManager.putString(Constants.KEY_Image, "avatar.png");
//        preferenceManager.putString(Constants.KEY_PhoneNum, "0123456789");
//        preferenceManager.putString(Constants.KEY_Name, "Quang");
//        preferenceManager.putString(Constants.KEY_PhoneNum, "1123456789");
//        preferenceManager.putString(Constants.KEY_Name, "Thanh Hai");

        myToolBar = findViewById(R.id.my_toolbar);
        SetUpToolBar();

        myToolBarTitle = findViewById(R.id.toolbarTitle);
        progressBar = findViewById(R.id.progressBar);
        fragmentContainerView = findViewById(R.id.fragment_container);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListChatFragment.class, null)
                .setReorderingAllowed(true)
                .commit();

        bottomNavigationView = findViewById(R.id.my_bottom_navigation);
        SetUpBottomNavigation();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.KEY_COLLECTION_USERS)
                .child(Constants.KEY_STORAGE_FOLDER_UserImages)
                .child(preferenceManager.getString(Constants.KEY_Image));
        tbUserImage = findViewById(R.id.tb_user_image);

        GlideApp.with(ListChatAndContactActivity.this)
                .load(storageReference)
                .into(tbUserImage);

    }

    public void SetUpToolBar() {
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void SetUpBottomNavigation() {
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
                    default:
                        return false;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public void loading(Boolean isLoading) {
        if (!isLoading) {
            this.progressBar.setVisibility(View.GONE);
            this.fragmentContainerView.setVisibility(View.VISIBLE);
        }
    }
}