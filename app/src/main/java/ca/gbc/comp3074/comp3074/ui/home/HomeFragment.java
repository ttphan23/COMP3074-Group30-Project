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
import android.widget.Toast;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialSharedAxis;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;

import android.widget.EditText;

import android.widget.Spinner;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;
    private List<Game> allGames;
    private String currentFilter = "all"; // "all", "played", "playing", "backlog"
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.Z, true);
        enter.setDuration(320);
        setEnterTransition(enter);
        MaterialSharedAxis returnAxis = new MaterialSharedAxis(MaterialSharedAxis.Z, false);
        returnAxis.setDuration(320);
        setReturnTransition(returnAxis);
    }

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

        Button btnEditLibrary = binding.getRoot().findViewById(R.id.btnEditLibrary);

        btnEditLibrary.setOnClickListener(v -> {
            showAddGameDialog();
        });

        TextView tvBestGame = root.findViewById(R.id.tvBestGame);
        String best = sessionManager.getBestGame();
        if (best != null && !best.isEmpty()) {
            tvBestGame.setText("⭐ Best Game: " + best);
        } else {
            tvBestGame.setText("⭐ Best Game: None");
        }
        
        // Load and display profile initial
        TextView tvProfileEmoji = root.findViewById(R.id.tvProfileEmoji);
        if (username != null && !username.isEmpty() && !username.equals("Guest")) {
            tvProfileEmoji.setText(username.substring(0, 1).toUpperCase());
        } else {
            tvProfileEmoji.setText("G");
        }

        // Initialize game library
        List<Game> savedGames = sessionManager.getGameLibrary();

        if (savedGames.isEmpty()) {
            initializeGames();              // 기본 목록 생성
            sessionManager.saveGameLibrary(allGames);
        } else {
            allGames = savedGames;
        }

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

        List<Game> saved = sessionManager.getGameLibrary();
        if (saved != null && !saved.isEmpty()) {
            allGames = saved;
            return;
        }

        allGames = new ArrayList<>();

        sessionManager.saveGameLibrary(allGames);
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


    private void showAddGameDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_game, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Game");
        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            EditText editTitle = dialogView.findViewById(R.id.editTitle);
            Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
            Spinner spinnerEmoji = dialogView.findViewById(R.id.spinnerEmoji);

            String title = editTitle.getText().toString();
            String status = spinnerStatus.getSelectedItem().toString().toLowerCase(); // Played → "played"
            String emoji = spinnerEmoji.getSelectedItem().toString();


            Game newGame = new Game(title, status, emoji);
            allGames.add(newGame);
            sessionManager.saveGameLibrary(allGames);
            loadGameLibrary();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void setupLongClick(TextView gameView, Game game) {

        gameView.setOnLongClickListener(v -> {

            String[] options = {"Edit", "Delete", "Set as Best Game"};

            new AlertDialog.Builder(requireContext())
                    .setTitle("Choose Action")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showEditDialog(game);
                        } else if (which == 1) {
                            confirmDeleteGame(game);
                        } else if (which == 2) {
                            setBestGame(game);
                        }
                    })
                    .show();

            return true;
        });
    }

    private void setBestGame(Game game) {
        sessionManager.setBestGame(game.getTitle());

        TextView tvBestGame = binding.getRoot().findViewById(R.id.tvBestGame);
        tvBestGame.setText("⭐ Best Game: " + game.getTitle());

        android.widget.Toast.makeText(requireContext(),
                game.getTitle() + " set as your Best Game!",
                android.widget.Toast.LENGTH_SHORT).show();
    }


    private void showEditDialog(Game selectedGame) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_game, null);

        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        Spinner statusSpinner = dialogView.findViewById(R.id.spinnerStatus);
        Spinner emojiSpinner = dialogView.findViewById(R.id.spinnerEmoji);

        titleInput.setText(selectedGame.getTitle());

        String[] statuses = getResources().getStringArray(R.array.status_options);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(selectedGame.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }

        String[] emojis = getResources().getStringArray(R.array.emoji_options);
        for (int i = 0; i < emojis.length; i++) {
            if (emojis[i].equals(selectedGame.getEmoji())) {
                emojiSpinner.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Game")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    selectedGame.setTitle(titleInput.getText().toString());
                    selectedGame.setStatus(statusSpinner.getSelectedItem().toString().toLowerCase());
                    selectedGame.setEmoji(emojiSpinner.getSelectedItem().toString());

                    sessionManager.saveGameLibrary(allGames);
                    loadGameLibrary();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void confirmDeleteGame(Game game) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Game")
                .setMessage("Are you sure you want to delete '" + game.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    allGames.remove(game);
                    sessionManager.saveGameLibrary(allGames);
                    loadGameLibrary();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        String filterText = currentFilter.equals("all") ? "Game Library" :
                           currentFilter.equals("played") ? "Completed" :
                           currentFilter.equals("playing") ? "Currently Playing" : "Backlog";
        libraryTitle.setText(filterText + " (" + filteredGames.size() + ")");

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

            gameView.setOnLongClickListener(v -> {
                Toast.makeText(requireContext(), game.getTitle() + " • " + game.getStatus(), Toast.LENGTH_SHORT).show();
                return true;
            });

            setupLongClick(gameView, game);

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

                // Create a row for each game
                LinearLayout gameRow = new LinearLayout(requireContext());
                gameRow.setOrientation(LinearLayout.HORIZONTAL);
                gameRow.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
                gameRow.setGravity(Gravity.CENTER_VERTICAL);

                // Game info column
                LinearLayout infoColumn = new LinearLayout(requireContext());
                infoColumn.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                infoColumn.setLayoutParams(infoParams);

                // Title
                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText(title);
                tvTitle.setTextSize(15);
                tvTitle.setTextColor(getResources().getColor(R.color.text_primary, null));
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                // Description
                TextView tvDetails = new TextView(requireContext());
                tvDetails.setText(description);
                tvDetails.setTextSize(13);
                tvDetails.setTextColor(getResources().getColor(R.color.text_secondary, null));

                infoColumn.addView(tvTitle);
                infoColumn.addView(tvDetails);

                // Rating badge
                TextView tvRating = new TextView(requireContext());
                tvRating.setText(String.format("%.1f", rating));
                tvRating.setTextSize(14);
                tvRating.setTextColor(getResources().getColor(R.color.rating_star, null));
                tvRating.setTypeface(null, android.graphics.Typeface.BOLD);

                gameRow.addView(infoColumn);
                gameRow.addView(tvRating);

                llTrendingGames.addView(gameRow);

                // Add divider except for last item
                if (i < Math.min(4, trendingGames.length()) - 1) {
                    View divider = new View(requireContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );
                    dividerParams.setMargins(dpToPx(12), 0, dpToPx(12), 0);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                    llTrendingGames.addView(divider);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            TextView errorText = new TextView(requireContext());
            errorText.setText("Unable to load trending games");
            errorText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            errorText.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
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
        // Refresh profile when returning from settings
        if (binding != null) {
            String username = sessionManager.getUsername();
            TextView tvProfileEmoji = binding.getRoot().findViewById(R.id.tvProfileEmoji);
            if (username != null && !username.isEmpty() && !username.equals("Guest")) {
                tvProfileEmoji.setText(username.substring(0, 1).toUpperCase());
            } else {
                tvProfileEmoji.setText("G");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
