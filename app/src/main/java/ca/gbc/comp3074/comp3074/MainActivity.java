package ca.gbc.comp3074.comp3074;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import ca.gbc.comp3074.comp3074.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Bottom navigation view
        BottomNavigationView navView = binding.navView;

        // NavController
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Connect BottomNavigation to NavController
        NavigationUI.setupWithNavController(navView, navController);
    }
}
