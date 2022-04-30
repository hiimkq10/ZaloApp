package hcmute.nhom2.zaloapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import hcmute.nhom2.zaloapp.fragment.ListChatFragment;
import hcmute.nhom2.zaloapp.fragment.ListContactFragment;

public class ListChatAndContactActivity extends AppCompatActivity {
    Toolbar myToolBar;
    TextView myToolBarTitle;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_chat_and_contact);

        myToolBar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        myToolBarTitle = findViewById(R.id.toolbarTitle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListChatFragment.class, null)
                .setReorderingAllowed(true)
                .commit();

        bottomNavigationView = findViewById(R.id.my_bottom_navigation);

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
}