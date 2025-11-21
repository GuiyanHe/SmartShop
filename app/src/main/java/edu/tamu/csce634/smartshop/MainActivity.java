package edu.tamu.csce634.smartshop;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import edu.tamu.csce634.smartshop.databinding.ActivityMainBinding;
import edu.tamu.csce634.smartshop.managers.RecipeManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RecipeManager with context
        RecipeManager.getInstance(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_recipe)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupWithNavController(binding.navView, navController);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            navController.navigate(R.id.action_home_to_feedback);
        });

        // 监听导航目标的变化，根据不同的 Fragment 控制 UI 元素的显隐
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // 获取当前目的地的 ID
            int destinationId = destination.getId();

            // 判断是否是 MapFragment
            if (destinationId == R.id.navigation_map) {
                // 如果是地图页，隐藏悬浮按钮和底部导航栏
                fab.hide();
                binding.navView.setVisibility(android.view.View.GONE); // 隐藏底部导航栏

            } else {
                // 如果是其他页面，则显示底部导航栏
                binding.navView.setVisibility(android.view.View.VISIBLE); // 显示底部导航栏

                // 并且，根据是否是主页(Home)来决定是否显示悬浮按钮
                if (destinationId == R.id.navigation_home) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });

    }
}