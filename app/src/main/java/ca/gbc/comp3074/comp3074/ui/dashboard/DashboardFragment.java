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
import ca.gbc.comp3074.comp3074.data.AppDatabase;
import ca.gbc.comp3074.comp3074.data.Review;
import ca.gbc.comp3074.comp3074.data.ReviewDao;
import ca.gbc.comp3074.comp3074.databinding.FragmentDashboardBinding;
import com.google.android.material.transition.MaterialSharedAxis;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SessionManager sessionManager;
    private ReviewDao reviewDao;

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
        reviewDao = AppDatabase.getInstance(requireContext()).reviewDao();

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
                tvAvatar.setText(username.substring(0, 1).toUpperCase());
                tvAvatar.setTextSize(16);
                tvAvatar.setTextColor(getResources().getColor(R.color.white, null));
                tvAvatar.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                        dpToPx(44),
                        dpToPx(44)
                );
                avatarParams.setMargins(0, 0, dpToPx(12), 0);
                tvAvatar.setLayoutParams(avatarParams);
                tvAvatar.setGravity(android.view.Gravity.CENTER);
                tvAvatar.setBackgroundResource(R.drawable.profile_background);

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

        // Get reviews from Room DB (ordered by newest first)
        List<Review> roomReviews = reviewDao.getAllReviews();

        // Check if we have any Room reviews
        if (roomReviews.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No recent activity yet.");
            emptyText.setTextColor(getResources().getColor(R.color.text_secondary, null));
            emptyText.setTextSize(16);
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
            llFeedContent.addView(emptyText);
            return;
        }

        // Display Room DB reviews (newest first - already sorted by timestamp DESC)
        for (Review review : roomReviews) {
            addReviewCard(llFeedContent, review);
        }
    }

    private void addReviewCard(LinearLayout parent, Review review) {
        String user = review.getUsername();
        String title = review.getGameTitle();
        float rating = review.getRating();
        String text = review.getReviewText();

        CardView card = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setCardElevation(0);
        card.setRadius(dpToPx(16));
        card.setCardBackgroundColor(getResources().getColor(R.color.card_background, null));

        LinearLayout cardContent = new LinearLayout(requireContext());
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Header row with user info
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Avatar
        TextView tvAvatar = new TextView(requireContext());
        tvAvatar.setText(user != null && !user.isEmpty() ? user.substring(0, 1).toUpperCase() : "?");
        tvAvatar.setTextSize(14);
        tvAvatar.setTextColor(getResources().getColor(R.color.white, null));
        tvAvatar.setTypeface(null, android.graphics.Typeface.BOLD);
        tvAvatar.setGravity(android.view.Gravity.CENTER);
        tvAvatar.setBackgroundResource(R.drawable.profile_background);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
        avatarParams.setMargins(0, 0, dpToPx(12), 0);
        tvAvatar.setLayoutParams(avatarParams);

        // User info column
        LinearLayout userInfoColumn = new LinearLayout(requireContext());
        userInfoColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams userInfoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        userInfoColumn.setLayoutParams(userInfoParams);

        TextView tvUser = new TextView(requireContext());
        tvUser.setText(user != null ? user : "Unknown");
        tvUser.setTextSize(14);
        tvUser.setTextColor(getResources().getColor(R.color.text_primary, null));
        tvUser.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvGame = new TextView(requireContext());
        tvGame.setText(title);
        tvGame.setTextSize(12);
        tvGame.setTextColor(getResources().getColor(R.color.text_secondary, null));

        userInfoColumn.addView(tvUser);
        userInfoColumn.addView(tvGame);

        // Rating badge
        TextView tvRating = new TextView(requireContext());
        tvRating.setText(String.format("%.1f", rating));
        tvRating.setTextSize(14);
        tvRating.setTextColor(getResources().getColor(R.color.rating_star, null));
        tvRating.setTypeface(null, android.graphics.Typeface.BOLD);
        tvRating.setBackgroundResource(R.drawable.bg_button_secondary);
        tvRating.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));

        headerRow.addView(tvAvatar);
        headerRow.addView(userInfoColumn);
        headerRow.addView(tvRating);

        // Review text
        TextView tvReview = new TextView(requireContext());
        tvReview.setText(text);
        tvReview.setTextSize(14);
        tvReview.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tvReview.setPadding(0, dpToPx(12), 0, 0);
        tvReview.setLineSpacing(dpToPx(2), 1.0f);

        // Delete button
        Button btnDelete = new Button(requireContext());
        btnDelete.setText("Delete");
        btnDelete.setTextSize(12);
        btnDelete.setTextColor(getResources().getColor(R.color.error, null));
        btnDelete.setBackgroundResource(R.drawable.bg_button_outline);
        btnDelete.setAllCaps(false);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(36));
        deleteParams.setMargins(0, dpToPx(12), 0, 0);
        btnDelete.setLayoutParams(deleteParams);

        btnDelete.setOnClickListener(v -> {
            // Delete from Room DB
            reviewDao.delete(review);
            // Refresh the feed
            loadFeedContent();
            Toast.makeText(requireContext(), "Review deleted", Toast.LENGTH_SHORT).show();
        });

        cardContent.addView(headerRow);
        cardContent.addView(tvReview);
        cardContent.addView(btnDelete);
        card.addView(cardContent);

        parent.addView(card);
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
