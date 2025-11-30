package ca.gbc.comp3074.comp3074.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialSharedAxis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ca.gbc.comp3074.comp3074.LoginActivity;
import ca.gbc.comp3074.comp3074.R;
import ca.gbc.comp3074.comp3074.SessionManager;
import ca.gbc.comp3074.comp3074.SettingsActivity;
import ca.gbc.comp3074.comp3074.data.remote.ApiClient;
import ca.gbc.comp3074.comp3074.data.remote.models.GameApiModel;
import ca.gbc.comp3074.comp3074.databinding.FragmentHomeBinding;
import ca.gbc.comp3074.comp3074.model.Game;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;
    private List<Game> allGames;
    private String currentFilter = "all"; // "all", "played", "playing", "backlog"
    private SharedPreferences preferences;

    // API games for picker
    private final List<GameApiModel> apiGames = new ArrayList<>();

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

        // Username
        String username = sessionManager.getUsername();
        TextView usernameText = root.findViewById(R.id.textView3);
        if (username != null && !username.isEmpty()) {
            usernameText.setText(username);
        } else {
            usernameText.setText("Guest");
        }

        // Best game label
        TextView tvBestGame = root.findViewById(R.id.tvBestGame);
        String best = sessionManager.getBestGame();
        if (best != null && !best.isEmpty()) {
            tvBestGame.setText("⭐ Best Game: " + best);
        } else {
            tvBestGame.setText("⭐ Best Game: None");
        }

        // Profile avatar
        TextView tvProfileEmoji = root.findViewById(R.id.tvProfileEmoji);
        if (username != null && !username.isEmpty() && !username.equals("Guest")) {
            tvProfileEmoji.setText(username.substring(0, 1).toUpperCase());
        } else {
            tvProfileEmoji.setText("G");
        }

        // Edit Library → pick from API
        Button btnEditLibrary = binding.getRoot().findViewById(R.id.btnEditLibrary);
        btnEditLibrary.setOnClickListener(v -> showApiGamePickerDialog());

        // Initialize game library (from prefs or empty)
        List<Game> savedGames = sessionManager.getGameLibrary();
        if (savedGames.isEmpty()) {
            initializeGames();
            sessionManager.saveGameLibrary(allGames);
        } else {
            allGames = savedGames;
        }

        // Filter buttons
        setupFilterButtons();

        // Settings button
        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        // Logout button
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Local dummy trending for now
        loadTrendingGames();

        // Fetch API games (used for picker)
        fetchApiGamesForLibrary();

        // Initial library render
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
        // Start empty; user will add from API picker
        sessionManager.saveGameLibrary(allGames);
    }

    private void setupFilterButtons() {
        Button btnPlayed = binding.getRoot().findViewById(R.id.button);
        Button btnPlaying = binding.getRoot().findViewById(R.id.button2);
        Button btnBacklog = binding.getRoot().findViewById(R.id.button3);

        // initial: all
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

        btnPlayed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
        btnPlaying.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
        btnBacklog.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));

        btnPlayed.setTextColor(whiteColor);
        btnPlaying.setTextColor(whiteColor);
        btnBacklog.setTextColor(whiteColor);

        if (activeButton != null) {
            activeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accentColor));
            activeButton.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .withEndAction(() -> activeButton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start())
                    .start();
        }
    }

    // =======================
    // API GAME PICKER (NEW)
    // =======================

    private void fetchApiGamesForLibrary() {
        ApiClient.getGameApiService()
                .getTrendingGames()
                .enqueue(new Callback<List<GameApiModel>>() {
                    @Override
                    public void onResponse(Call<List<GameApiModel>> call, Response<List<GameApiModel>> response) {
                        if (!isAdded()) return;
                        List<GameApiModel> body = response.body();
                        apiGames.clear();
                        if (response.isSuccessful() && body != null) {
                            apiGames.addAll(body);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GameApiModel>> call, Throwable t) {
                        // ignore for now; picker will show toast if empty
                    }
                });
    }

    private void showApiGamePickerDialog() {
        if (apiGames.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Game list is still loading or API unavailable. Try again in a moment.",
                    Toast.LENGTH_SHORT).show();
            // Try refetch just in case
            fetchApiGamesForLibrary();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose a Game from API");

        // Container layout
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(16);
        container.setPadding(padding, padding, padding, padding);

        // Search box
        EditText searchInput = new EditText(requireContext());
        searchInput.setHint("Search games…");
        container.addView(searchInput);

        // ListView with fixed height
        ListView listView = new ListView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(300)
        );
        listView.setLayoutParams(params);
        container.addView(listView);

        // Build list of titles from API games
        List<String> allTitles = new ArrayList<>();
        for (GameApiModel g : apiGames) {
            allTitles.add(g.getTitle());
        }

        // Adapter with filterable list
        List<String> filtered = new ArrayList<>(allTitles);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                filtered
        );
        listView.setAdapter(adapter);

        // Filter as user types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                filtered.clear();
                if (query.isEmpty()) {
                    filtered.addAll(allTitles);
                } else {
                    for (String title : allTitles) {
                        if (title.toLowerCase().contains(query)) {
                            filtered.add(title);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder
                .setView(container)
                .setNegativeButton("Cancel", null)
                .create();

        // On selection → open status/emoji dialog with locked title
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = filtered.get(position);
            dialog.dismiss();
            showAddGameDialogWithTitle(selectedTitle);
        });

        dialog.show();
    }

    private void showAddGameDialogWithTitle(String preselectedTitle) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_game, null);

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Spinner spinnerEmoji = dialogView.findViewById(R.id.spinnerEmoji);

        // Lock title to API game name
        editTitle.setText(preselectedTitle);
        editTitle.setEnabled(false);
        editTitle.setFocusable(false);
        editTitle.setClickable(false);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add to Library")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String status = spinnerStatus.getSelectedItem().toString().toLowerCase();
                    String emoji = spinnerEmoji.getSelectedItem().toString();

                    Game newGame = new Game(preselectedTitle, status, emoji);
                    allGames.add(newGame);
                    sessionManager.saveGameLibrary(allGames);
                    loadGameLibrary();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =======================
    // EDIT / DELETE / BEST
    // =======================

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

        Toast.makeText(requireContext(),
                game.getTitle() + " set as your Best Game!",
                Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog(Game selectedGame) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_game, null);

        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        Spinner statusSpinner = dialogView.findViewById(R.id.spinnerStatus);
        Spinner emojiSpinner = dialogView.findViewById(R.id.spinnerEmoji);

        // Keep API title locked
        titleInput.setText(selectedGame.getTitle());
        titleInput.setEnabled(false);
        titleInput.setFocusable(false);
        titleInput.setClickable(false);

        // Set status spinner
        String[] statuses = getResources().getStringArray(R.array.status_options);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(selectedGame.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }

        // Set emoji spinner
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

    // =======================
    // LIBRARY RENDER
    // =======================

    private void loadGameLibrary() {
        LinearLayout libraryContainer = binding.getRoot().findViewById(R.id.linearLayout2);
        libraryContainer.removeAllViews();

        List<Game> filteredGames = new ArrayList<>();

        for (Game game : allGames) {
            if (currentFilter.equals("all") || game.getStatus().equals(currentFilter)) {
                filteredGames.add(game);
            }
        }

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

        for (int i = 0; i < filteredGames.size(); i++) {
            Game game = filteredGames.get(i);

            TextView gameView = new TextView(requireContext());
            gameView.setText(game.getEmoji() + " " + game.getTitle());
            gameView.setTextSize(16);
            gameView.setTextColor(getResources().getColor(R.color.text_primary, null));
            gameView.setTypeface(null, android.graphics.Typeface.BOLD);
            gameView.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            gameView.setBackground(
                    getResources().getDrawable(android.R.drawable.list_selector_background, null)
            );

            gameView.setOnClickListener(v -> Toast.makeText(
                    requireContext(),
                    "Selected: " + game.getTitle(),
                    Toast.LENGTH_SHORT
            ).show());

            // Long-press → Edit / Delete / Set Best
            setupLongClick(gameView, game);

            gameView.setAlpha(0f);
            gameView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(i * 50L)
                    .start();

            libraryContainer.addView(gameView);

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

    // =======================
    // TRENDING (LOCAL DUMMY)
    // =======================

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

                LinearLayout gameRow = new LinearLayout(requireContext());
                gameRow.setOrientation(LinearLayout.HORIZONTAL);
                gameRow.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
                gameRow.setGravity(Gravity.CENTER_VERTICAL);

                LinearLayout infoColumn = new LinearLayout(requireContext());
                infoColumn.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                infoColumn.setLayoutParams(infoParams);

                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText(title);
                tvTitle.setTextSize(15);
                tvTitle.setTextColor(getResources().getColor(R.color.text_primary, null));
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvDetails = new TextView(requireContext());
                tvDetails.setText(description);
                tvDetails.setTextSize(13);
                tvDetails.setTextColor(getResources().getColor(R.color.text_secondary, null));

                infoColumn.addView(tvTitle);
                infoColumn.addView(tvDetails);

                TextView tvRating = new TextView(requireContext());
                tvRating.setText(String.format("%.1f", rating));
                tvRating.setTextSize(14);
                tvRating.setTextColor(getResources().getColor(R.color.rating_star, null));
                tvRating.setTypeface(null, android.graphics.Typeface.BOLD);

                gameRow.addView(infoColumn);
                gameRow.addView(tvRating);

                llTrendingGames.addView(gameRow);

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

    // =======================

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onResume() {
        super.onResume();
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
