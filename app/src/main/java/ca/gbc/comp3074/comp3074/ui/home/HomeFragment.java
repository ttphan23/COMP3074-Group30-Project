package ca.gbc.comp3074.comp3074.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ca.gbc.comp3074.comp3074.R;
import ca.gbc.comp3074.comp3074.SessionManager;
import ca.gbc.comp3074.comp3074.databinding.FragmentHomeBinding;
import ca.gbc.comp3074.comp3074.LoginActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());
        sessionManager.initializeDefaultFollowing();

        // Get username from SessionManager
        String username = sessionManager.getUsername();
        TextView usernameText = root.findViewById(R.id.textView3);

        if (username != null && !username.isEmpty()) {
            usernameText.setText(username);
        } else {
            usernameText.setText("Guest");
        }

        // Logout button click
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Load trending games
        loadTrendingGames();

        return root;
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
                tvDetails.setText(description + " • ⭐ " + rating);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
