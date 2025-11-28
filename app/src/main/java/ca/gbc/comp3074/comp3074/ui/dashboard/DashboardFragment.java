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
import com.google.android.material.transition.MaterialSharedAxis;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SessionManager sessionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        enter.setDuration(320);
        setEnterTransition(enter);
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        exit.setDuration(320);
        setReturnTransition(exit);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        binding.swipeRefresh.setColorSchemeResources(R.color.accent, R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadFollowSuggestions();
            loadFeedContent();
            binding.swipeRefresh.postDelayed(() -> binding.swipeRefresh.setRefreshing(false), 350);
        });

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
        String currentUsername = sessionManager.getUsername();

        try {
            int userCount = 0;
            for (int i = 0; i < allUsers.length(); i++) {
                JSONObject user = allUsers.getJSONObject(i);
                String username = user.getString("username");

                // Skip current user
                if (username.equals(currentUsername)) {
                    continue;
                }

                // Create a horizontal layout for each user
                LinearLayout userLayout = new LinearLayout(requireContext());
                userLayout.setOrientation(LinearLayout.HORIZONTAL);
                userLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                userLayout.setLayoutParams(layoutParams);
                userLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));

                // Avatar circle
                TextView tvAvatar = new TextView(requireContext());
                tvAvatar.setText("👤");
                tvAvatar.setTextSize(24);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                        dpToPx(40),
                        dpToPx(40)
                );
                avatarParams.setMargins(0, 0, dpToPx(12), 0);
                tvAvatar.setLayoutParams(avatarParams);
                tvAvatar.setGravity(android.view.Gravity.CENTER);

                // Username container
                LinearLayout usernameContainer = new LinearLayout(requireContext());
                usernameContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                usernameContainer.setLayoutParams(containerParams);
                usernameContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);

                // Username text
                TextView tvUsername = new TextView(requireContext());
                tvUsername.setText(username);
                tvUsername.setTextSize(16);
                tvUsername.setTextColor(getResources().getColor(R.color.text_primary, null));
                tvUsername.setTypeface(null, android.graphics.Typeface.BOLD);

                // Status text
                TextView tvStatus = new TextView(requireContext());
                boolean isFollowing = sessionManager.isFollowing(username);
                tvStatus.setText(isFollowing ? "Friend" : "Gamer");
                tvStatus.setTextSize(12);
                tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));

                usernameContainer.addView(tvUsername);
                usernameContainer.addView(tvStatus);

                // Follow/Unfollow button
                Button btnFollow = new Button(requireContext());
                btnFollow.setText(isFollowing ? "UNFOLLOW" : "FOLLOW");
                btnFollow.setTextSize(11);
                btnFollow.setTextColor(getResources().getColor(android.R.color.white, null));
                btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(isFollowing ? R.color.button_danger : R.color.button_primary, null)
                ));
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        dpToPx(90),
                        dpToPx(36)
                );
                btnFollow.setLayoutParams(btnParams);
                btnFollow.setAllCaps(true);

                btnFollow.setOnClickListener(v -> {
                    if (sessionManager.isFollowing(username)) {
                        sessionManager.unfollowUser(username);
                        btnFollow.setText("FOLLOW");
                        btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.button_primary, null)
                        ));
                        tvStatus.setText("Gamer");
                        Toast.makeText(requireContext(), "Unfollowed " + username, Toast.LENGTH_SHORT).show();
                        
                        // Bounce animation
                        btnFollow.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                btnFollow.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                            })
                            .start();
                    } else {
                        sessionManager.followUser(username);
                        btnFollow.setText("UNFOLLOW");
                        btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.button_danger, null)
                        ));
                        tvStatus.setText("Friend");
                        Toast.makeText(requireContext(), "Now following " + username + "! 🎮", Toast.LENGTH_SHORT).show();
                        
                        // Bounce animation
                        btnFollow.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                btnFollow.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                            })
                            .start();
                    }
                    // Reload feed to show new content
                    loadFeedContent();
                });

                userLayout.addView(tvAvatar);
                userLayout.addView(usernameContainer);
                userLayout.addView(btnFollow);

                // Fade-in animation
                userLayout.setAlpha(0f);
                userLayout.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(userCount * 50)
                    .start();

                llFollowSuggestions.addView(userLayout);

                // Add divider except for last item
                if (i < allUsers.length() - 1) {
                    View divider = new View(requireContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );
                    dividerParams.setMargins(dpToPx(12), 0, dpToPx(12), 0);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
                    llFollowSuggestions.addView(divider);
                }

                userCount++;
            }

            // If no users to display
            if (userCount == 0) {
                TextView emptyText = new TextView(requireContext());
                emptyText.setText("No users to follow");
                emptyText.setTextColor(getResources().getColor(R.color.text_secondary, null));
                emptyText.setTextSize(14);
                emptyText.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                emptyText.setGravity(android.view.Gravity.CENTER);
                llFollowSuggestions.addView(emptyText);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            TextView errorText = new TextView(requireContext());
            errorText.setText("Error loading users");
            errorText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            errorText.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            llFollowSuggestions.addView(errorText);
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
