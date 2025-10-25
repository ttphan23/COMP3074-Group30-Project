package ca.gbc.comp3074.comp3074.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import ca.gbc.comp3074.comp3074.R;
import ca.gbc.comp3074.comp3074.SessionManager;
import ca.gbc.comp3074.comp3074.databinding.FragmentDashboardBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        // Load follow suggestions
        loadFollowSuggestions();

        // Load feed content
        loadFeedContent();

        return root;
    }

    private void loadFollowSuggestions() {
        LinearLayout llFollowSuggestions = binding.getRoot().findViewById(R.id.llFollowSuggestions);
        llFollowSuggestions.removeAllViews();

        JSONArray allUsers = sessionManager.getAllUsers();

        try {
            for (int i = 0; i < allUsers.length(); i++) {
                JSONObject user = allUsers.getJSONObject(i);
                String username = user.getString("username");

                // Create a horizontal layout for each user
                LinearLayout userLayout = new LinearLayout(requireContext());
                userLayout.setOrientation(LinearLayout.HORIZONTAL);
                userLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                userLayout.setLayoutParams(layoutParams);

                // Username text
                TextView tvUsername = new TextView(requireContext());
                tvUsername.setText("👤 " + username);
                tvUsername.setTextSize(16);
                tvUsername.setTextColor(getResources().getColor(R.color.text_primary, null));
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                tvUsername.setLayoutParams(textParams);
                tvUsername.setGravity(android.view.Gravity.CENTER_VERTICAL);

                // Follow/Unfollow button
                Button btnFollow = new Button(requireContext());
                boolean isFollowing = sessionManager.isFollowing(username);
                btnFollow.setText(isFollowing ? "Unfollow" : "Follow");
                btnFollow.setTextSize(12);
                btnFollow.setTextColor(getResources().getColor(R.color.white, null));
                btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(isFollowing ? R.color.button_danger : R.color.button_primary, null)
                ));
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(36)
                );
                btnFollow.setLayoutParams(btnParams);
                btnFollow.setPadding(dpToPx(16), 0, dpToPx(16), 0);

                btnFollow.setOnClickListener(v -> {
                    if (sessionManager.isFollowing(username)) {
                        sessionManager.unfollowUser(username);
                        btnFollow.setText("Follow");
                        btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.button_primary, null)
                        ));
                        Toast.makeText(requireContext(), "Unfollowed " + username, Toast.LENGTH_SHORT).show();
                    } else {
                        sessionManager.followUser(username);
                        btnFollow.setText("Unfollow");
                        btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.button_danger, null)
                        ));
                        Toast.makeText(requireContext(), "Following " + username, Toast.LENGTH_SHORT).show();
                    }
                    // Reload feed to show new content
                    loadFeedContent();
                });

                userLayout.addView(tvUsername);
                userLayout.addView(btnFollow);

                // Add divider except for last item
                if (i < allUsers.length() - 1) {
                    View divider = new View(requireContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );
                    dividerParams.setMargins(0, dpToPx(8), 0, 0);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                    userLayout.addView(divider);
                }

                llFollowSuggestions.addView(userLayout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadFeedContent() {
        LinearLayout llFeedContent = binding.getRoot().findViewById(R.id.llFeedContent);
        llFeedContent.removeAllViews();

        // Get reviews from followed users
        JSONArray followedReviews = sessionManager.getFollowedUsersReviews();
        
        // Also get user's own reviews
        String userReviewsJson = sessionManager.getAllReviewsJSON();
        try {
            JSONArray userReviews = new JSONArray(userReviewsJson);
            // Combine both arrays
            for (int i = 0; i < userReviews.length(); i++) {
                followedReviews.put(userReviews.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (followedReviews.length() == 0) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No activity yet.\nFollow friends to see their reviews!");
            emptyText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            emptyText.setTextSize(16);
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
            llFeedContent.addView(emptyText);
            return;
        }

        try {
            for (int i = followedReviews.length() - 1; i >= 0; i--) {
                JSONObject review = followedReviews.getJSONObject(i);
                String user = review.optString("user", "Guest");
                String title = review.optString("title", "Unknown Game");
                double rating = review.optDouble("rating", 0);
                String text = review.optString("text", "");

                // Create card for each review
                CardView card = new CardView(requireContext());
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, dpToPx(12));
                card.setLayoutParams(cardParams);
                card.setCardElevation(dpToPx(4));
                card.setRadius(dpToPx(8));
                card.setCardBackgroundColor(getResources().getColor(R.color.card_background, null));

                LinearLayout cardContent = new LinearLayout(requireContext());
                cardContent.setOrientation(LinearLayout.VERTICAL);
                cardContent.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

                // User and game title
                TextView tvHeader = new TextView(requireContext());
                tvHeader.setText("👤 " + user + " reviewed " + title);
                tvHeader.setTextSize(16);
                tvHeader.setTextColor(getResources().getColor(R.color.text_primary, null));
                tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);

                // Rating
                TextView tvRating = new TextView(requireContext());
                tvRating.setText("⭐ " + rating + " / 5.0");
                tvRating.setTextSize(14);
                tvRating.setTextColor(getResources().getColor(R.color.accent, null));
                tvRating.setTypeface(null, android.graphics.Typeface.BOLD);
                tvRating.setPadding(0, dpToPx(4), 0, dpToPx(4));

                // Review text
                TextView tvReview = new TextView(requireContext());
                tvReview.setText("\"" + text + "\"");
                tvReview.setTextSize(14);
                tvReview.setTextColor(getResources().getColor(R.color.text_secondary, null));
                tvReview.setPadding(0, dpToPx(8), 0, 0);

                cardContent.addView(tvHeader);
                cardContent.addView(tvRating);
                cardContent.addView(tvReview);
                card.addView(cardContent);

                llFeedContent.addView(card);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            TextView errorText = new TextView(requireContext());
            errorText.setText("Error loading feed");
            errorText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            llFeedContent.addView(errorText);
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

    @Override
    public void onResume() {
        super.onResume();
        // Refresh feed when returning to this fragment
        if (binding != null) {
            loadFeedContent();
        }
    }
}
