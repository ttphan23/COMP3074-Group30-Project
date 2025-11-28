package ca.gbc.comp3074.comp3074;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
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

        // 1. Set up the Toolbar as ActionBar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // 2. Bottom navigation view
        BottomNavigationView navView = binding.navView;

        // 3. Configure top-level destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_Feed,
                R.id.navigation_profile,
                R.id.navigation_review
        ).build();

        // 4. NavController
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // 5. Connect ActionBar to NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 6. Connect BottomNavigation to NavController
        NavigationUI.setupWithNavController(navView, navController);
    }

    // Allow NavController to handle back navigation through toolbar
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
