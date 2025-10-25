package ca.gbc.comp3074.comp3074.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ca.gbc.comp3074.comp3074.R;
import ca.gbc.comp3074.comp3074.SessionManager;
import ca.gbc.comp3074.comp3074.SettingsActivity;
import ca.gbc.comp3074.comp3074.databinding.FragmentHomeBinding;
import ca.gbc.comp3074.comp3074.LoginActivity;
import ca.gbc.comp3074.comp3074.model.Game;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;
    private List<Game> allGames;
    private String currentFilter = "all"; // "all", "played", "playing", "backlog"
    private SharedPreferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());
        sessionManager.initializeDefaultFollowing();
        
        preferences = requireContext().getSharedPreferences("UserProfile", requireContext().MODE_PRIVATE);

        // Get username from SessionManager
        String username = sessionManager.getUsername();
        TextView usernameText = root.findViewById(R.id.textView3);

        if (username != null && !username.isEmpty()) {
            usernameText.setText(username);
        } else {
            usernameText.setText("Guest");
        }
        
        // Load and display profile emoji
        TextView tvProfileEmoji = root.findViewById(R.id.tvProfileEmoji);
        String profileEmoji = preferences.getString("profileEmoji", "👤");
        tvProfileEmoji.setText(profileEmoji);

        // Initialize game library
        initializeGames();

        // Setup filter buttons
        setupFilterButtons();
        
        // Settings button click
        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        // Logout button click
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Load trending games
        loadTrendingGames();
        
        // Load game library with default "all" filter
        loadGameLibrary();

        return root;
    }

    private void initializeGames() {
        allGames = new ArrayList<>();
        // Played games
        allGames.add(new Game("The Legend of Zelda: BOTW", "played", "🎮"));
        allGames.add(new Game("The Witcher 3", "played", "⚔️"));
        allGames.add(new Game("God of War", "played", "🪓"));
        
        // Currently playing
        allGames.add(new Game("Elden Ring", "playing", "🎮"));
        allGames.add(new Game("Cyberpunk 2077", "playing", "🤖"));
        allGames.add(new Game("Red Dead Redemption 2", "playing", "🤠"));
        
        // Backlog
        allGames.add(new Game("Hollow Knight", "backlog", "🦋"));
        allGames.add(new Game("Sekiro", "backlog", "⚔️"));
        allGames.add(new Game("Final Fantasy XVI", "backlog", "🐉"));
        allGames.add(new Game("Baldur's Gate 3", "backlog", "🎲"));
    }

    private void setupFilterButtons() {
        Button btnPlayed = binding.getRoot().findViewById(R.id.button);
        Button btnPlaying = binding.getRoot().findViewById(R.id.button2);
        Button btnBacklog = binding.getRoot().findViewById(R.id.button3);

        // Set initial state - all buttons active
        updateButtonStyles(btnPlayed, btnPlaying, btnBacklog, null);

        btnPlayed.setOnClickListener(v -> {
            currentFilter = currentFilter.equals("played") ? "all" : "played";
            updateButtonStyles(btnPlayed, btnPlaying, btnBacklog, 
                currentFilter.equals("played") ? btnPlayed : null);
            loadGameLibrary();
        });

        btnPlaying.setOnClickListener(v -> {
            currentFilter = currentFilter.equals("playing") ? "all" : "playing";
            updateButtonStyles(btnPlayed, btnPlaying, btnBacklog, 
                currentFilter.equals("playing") ? btnPlaying : null);
            loadGameLibrary();
        });

        btnBacklog.setOnClickListener(v -> {
            currentFilter = currentFilter.equals("backlog") ? "all" : "backlog";
            updateButtonStyles(btnPlayed, btnPlaying, btnBacklog, 
                currentFilter.equals("backlog") ? btnBacklog : null);
            loadGameLibrary();
        });
    }

    private void updateButtonStyles(Button btnPlayed, Button btnPlaying, Button btnBacklog, Button activeButton) {
        int primaryColor = getResources().getColor(R.color.primary, null);
        int accentColor = getResources().getColor(R.color.accent, null);
        int whiteColor = getResources().getColor(android.R.color.white, null);

        // Reset all buttons to default state
        btnPlayed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
        btnPlaying.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
        btnBacklog.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));

        btnPlayed.setTextColor(whiteColor);
        btnPlaying.setTextColor(whiteColor);
        btnBacklog.setTextColor(whiteColor);

        // Highlight active button
        if (activeButton != null) {
            activeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accentColor));
            
            // Add scale animation
            activeButton.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .withEndAction(() -> {
                    activeButton.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start();
                })
                .start();
        }
    }

    private void loadGameLibrary() {
        LinearLayout libraryContainer = binding.getRoot().findViewById(R.id.linearLayout2);
        libraryContainer.removeAllViews();

        List<Game> filteredGames = new ArrayList<>();
        
        // Filter games based on current filter
        for (Game game : allGames) {
            if (currentFilter.equals("all") || game.getStatus().equals(currentFilter)) {
                filteredGames.add(game);
            }
        }

        // Update library title with count
        TextView libraryTitle = binding.getRoot().findViewById(R.id.tvLibraryTitle);
        String filterText = currentFilter.equals("all") ? "My Game Library" : 
                           currentFilter.equals("played") ? "Completed Games" :
                           currentFilter.equals("playing") ? "Currently Playing" : "My Backlog";
        libraryTitle.setText("📚 " + filterText + " (" + filteredGames.size() + ")");

        if (filteredGames.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No games found in this category");
            emptyText.setTextSize(14);
            emptyText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            emptyText.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            emptyText.setGravity(Gravity.CENTER);
            libraryContainer.addView(emptyText);
            return;
        }

        // Add games to library with animations
        for (int i = 0; i < filteredGames.size(); i++) {
            Game game = filteredGames.get(i);
            
            TextView gameView = new TextView(requireContext());
            gameView.setText(game.getEmoji() + " " + game.getTitle());
            gameView.setTextSize(16);
            gameView.setTextColor(getResources().getColor(R.color.text_primary, null));
            gameView.setTypeface(null, android.graphics.Typeface.BOLD);
            gameView.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            gameView.setBackground(getResources().getDrawable(android.R.drawable.list_selector_background, null));
            
            // Add click effect
            gameView.setOnClickListener(v -> {
                // Could navigate to game details in the future
                android.widget.Toast.makeText(requireContext(), 
                    "Selected: " + game.getTitle(), 
                    android.widget.Toast.LENGTH_SHORT).show();
            });

            // Fade-in animation
            gameView.setAlpha(0f);
            gameView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(i * 50)
                .start();

            libraryContainer.addView(gameView);

            // Add divider except for last item
            if (i < filteredGames.size() - 1) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1)
                );
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                libraryContainer.addView(divider);
            }
        }
    }

    private void loadTrendingGames() {
        LinearLayout llTrendingGames = binding.getRoot().findViewById(R.id.llTrendingGames);
        llTrendingGames.removeAllViews();

        JSONArray trendingGames = sessionManager.getTrendingGames();

        try {
            for (int i = 0; i < Math.min(4, trendingGames.length()); i++) {
                JSONObject game = trendingGames.getJSONObject(i);
                String title = game.getString("title");
                String description = game.getString("description");
                double rating = game.getDouble("rating");

                // Create a card for each game
                LinearLayout gameCard = new LinearLayout(requireContext());
                gameCard.setOrientation(LinearLayout.VERTICAL);
                gameCard.setPadding(0, dpToPx(8), 0, dpToPx(8));

                // Title
                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText("🎮 " + title);
                tvTitle.setTextSize(18);
                tvTitle.setTextColor(getResources().getColor(R.color.text_primary, null));
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                // Description and rating
                TextView tvDetails = new TextView(requireContext());
                String formattedRating = String.format("%.1f",rating);
                tvDetails.setText(description + " • ⭐ " + formattedRating);
                tvDetails.setTextSize(14);
                tvDetails.setTextColor(getResources().getColor(R.color.text_secondary, null));
                tvDetails.setPadding(0, dpToPx(4), 0, 0);

                gameCard.addView(tvTitle);
                gameCard.addView(tvDetails);

                // Add divider except for last item
                if (i < Math.min(4, trendingGames.length()) - 1) {
                    View divider = new View(requireContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );
                    dividerParams.setMargins(0, dpToPx(8), 0, 0);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                    gameCard.addView(divider);
                }

                llTrendingGames.addView(gameCard);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            TextView errorText = new TextView(requireContext());
            errorText.setText("Unable to load trending games");
            errorText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            llTrendingGames.addView(errorText);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile emoji when returning from settings
        if (binding != null && preferences != null) {
            TextView tvProfileEmoji = binding.getRoot().findViewById(R.id.tvProfileEmoji);
            String profileEmoji = preferences.getString("profileEmoji", "👤");
            tvProfileEmoji.setText(profileEmoji);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
