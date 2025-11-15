package ca.gbc.comp3074.comp3074.ui.notifications;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import ca.gbc.comp3074.comp3074.R;
import ca.gbc.comp3074.comp3074.SessionManager;
import ca.gbc.comp3074.comp3074.data.ReviewRepository;
import ca.gbc.comp3074.comp3074.data.local.entities.ReviewEntity;
import ca.gbc.comp3074.comp3074.databinding.FragmentNotificationsBinding;

import ca.gbc.comp3074.comp3074.data.remote.ApiClient;
import ca.gbc.comp3074.comp3074.data.remote.models.GameApiModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private SessionManager sessionManager;
    private ReviewRepository reviewRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());
        reviewRepository = new ReviewRepository(requireContext());

        Spinner spinnerGames = binding.spinnerGames;
        EditText etReview = binding.etReview;
        RatingBar ratingBar = binding.ratingbBar;
        Button btnSubmit = binding.btnSubmitReview;
        TextView txtSaved = binding.tvLastReview;
        Button btnClear = binding.btnClearReviews;

        // Dynamic list for game titles (from API or fallback)
        List<String> gameTitles = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                gameTitles
        );
        spinnerGames.setAdapter(adapter);

        // Load game options from backend (with local fallback)
        loadGameOptions(gameTitles, adapter);

        // Load existing reviews for current user
        String currentUser = sessionManager.getUsername();
        List<ReviewEntity> existingReviews = reviewRepository.getReviewsForUser(currentUser);
        txtSaved.setText(buildReviewsSummary(existingReviews));

        // Submit review
        btnSubmit.setOnClickListener(v -> {
            String title = spinnerGames.getSelectedItem().toString();
            String text = etReview.getText().toString();
            float rating = ratingBar.getRating();

            // Validation
            if (text.trim().isEmpty()) {
                Snackbar.make(root, "⚠️ Please write a review!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.accent, null))
                        .setTextColor(getResources().getColor(R.color.white, null))
                        .show();
                shakeView(etReview);
                return;
            }

            if (rating == 0) {
                Snackbar.make(root, "⚠️ Please add a rating!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.accent, null))
                        .setTextColor(getResources().getColor(R.color.white, null))
                        .show();
                bounceView(ratingBar);
                return;
            }

            boolean success = reviewRepository.addReview(currentUser, title, text, rating);

            if (!success) {
                Snackbar.make(root, "⚠️ You already reviewed this game!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.accent, null))
                        .setTextColor(getResources().getColor(R.color.white, null))
                        .setAction("OK", view -> {})
                        .show();
                shakeView(etReview);
                return;
            }

            // Update summary text
            List<ReviewEntity> updated = reviewRepository.getReviewsForUser(currentUser);
            binding.tvLastReview.setText(buildReviewsSummary(updated));

            // Clear inputs
            etReview.setText("");
            ratingBar.setRating(0f);

            // Success feedback
            Snackbar.make(root, "✅ Review submitted successfully!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.button_primary, null))
                    .setTextColor(getResources().getColor(R.color.white, null))
                    .setAction("VIEW", view -> binding.tvLastReview.requestFocus())
                    .show();

            refreshReviewList();
            scaleUpView(btnSubmit);
            Toast.makeText(requireContext(), "🎮 Review saved!", Toast.LENGTH_SHORT).show();
        });

        // Clear reviews (all reviews in local DB)
        btnClear.setOnClickListener(v -> {
            Snackbar.make(root, "Are you sure you want to delete all reviews?", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.button_danger, null))
                    .setTextColor(getResources().getColor(R.color.white, null))
                    .setAction("DELETE", view -> {
                        reviewRepository.deleteAllReviews();
                        binding.tvLastReview.setText("No previous reviews yet.");
                        Toast.makeText(requireContext(), "All reviews cleared", Toast.LENGTH_SHORT).show();
                        refreshReviewList();
                        fadeInView(binding.tvLastReview);
                    })
                    .show();
        });

        // Bounce animation on rating change
        ratingBar.setOnRatingBarChangeListener((bar, rating1, fromUser) -> {
            if (fromUser) {
                bounceView(bar);
            }
        });

        // Build the detailed list of reviews below
        refreshReviewList();

        return root;
    }

    private void loadGameOptions(List<String> gameTitles, ArrayAdapter<String> adapter) {
        // Call the same backend as HomeFragment
        ApiClient.getGameApiService()
                .getTrendingGames()
                .enqueue(new Callback<List<GameApiModel>>() {
                    @Override
                    public void onResponse(Call<List<GameApiModel>> call,
                                           Response<List<GameApiModel>> response) {
                        if (!isAdded()) return;

                        List<GameApiModel> games = response.body();
                        gameTitles.clear();

                        if (response.isSuccessful() && games != null && !games.isEmpty()) {
                            // Use titles from API
                            for (GameApiModel g : games) {
                                gameTitles.add(g.getTitle());
                            }
                        } else {
                            // Fallback to local hard-coded list
                            addFallbackGames(gameTitles);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<List<GameApiModel>> call, Throwable t) {
                        if (!isAdded()) return;
                        // Network error → fallback to local list
                        gameTitles.clear();
                        addFallbackGames(gameTitles);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void addFallbackGames(List<String> gameTitles) {
        String[] fallback = {
                "Elden Ring",
                "The Legend of Zelda: BOTW",
                "Hollow Knight",
                "Stardew Valley",
                "God of War",
                "Baldur's Gate 3",
                "The Witcher 3",
                "Sekiro: Shadows Die Twice"
        };
        for (String g : fallback) {
            gameTitles.add(g);
        }
    }

    private void refreshReviewList() {
        if (binding == null) return;

        LinearLayout reviewContainer = binding.reviewContainer;
        reviewContainer.removeAllViews();

        String currentUser = sessionManager.getUsername();
        List<ReviewEntity> reviews = reviewRepository.getReviewsForUser(currentUser);

        if (reviews == null || reviews.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No previous reviews yet.");
            empty.setTextColor(getResources().getColor(R.color.text_secondary, null));
            reviewContainer.addView(empty);
            return;
        }

        for (ReviewEntity review : reviews) {
            final String title = review.gameTitle;
            final String text = review.reviewText;
            final float rating = review.rating;

            LinearLayout reviewBlock = new LinearLayout(requireContext());
            reviewBlock.setOrientation(LinearLayout.VERTICAL);
            reviewBlock.setPadding(0, 0, 0, 40);
            reviewContainer.addView(reviewBlock);

            TextView tv = new TextView(requireContext());
            tv.setText("🎮 " + title + " — " + rating + "★\n" + text);
            tv.setTextColor(getResources().getColor(R.color.text_primary, null));
            reviewBlock.addView(tv);

            LinearLayout btnRow = new LinearLayout(requireContext());
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            reviewBlock.addView(btnRow);

            Button btnEdit = new Button(requireContext());
            btnEdit.setText("EDIT");
            btnEdit.setAllCaps(true);
            btnEdit.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_secondary, null)
            );
            btnEdit.setTextColor(getResources().getColor(R.color.white, null));
            LinearLayout.LayoutParams editParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnEdit.setLayoutParams(editParams);
            btnRow.addView(btnEdit);

            Button btnDelete = new Button(requireContext());
            btnDelete.setText("DELETE");
            btnDelete.setAllCaps(true);
            btnDelete.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.button_danger)
            );
            btnDelete.setTextColor(getResources().getColor(R.color.white, null));
            LinearLayout.LayoutParams delParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnDelete.setLayoutParams(delParams);
            btnRow.addView(btnDelete);

            LinearLayout editLayout = new LinearLayout(requireContext());
            editLayout.setOrientation(LinearLayout.VERTICAL);
            editLayout.setVisibility(View.GONE);
            editLayout.setPadding(0, 15, 0, 0);
            reviewBlock.addView(editLayout);

            EditText etEditText = new EditText(requireContext());
            etEditText.setText(text);
            etEditText.setHint("Edit your review...");
            etEditText.setBackgroundResource(R.drawable.edit_text_background);
            etEditText.setTextColor(getResources().getColor(R.color.text_primary, null));
            editLayout.addView(etEditText);

            RatingBar editRating = new RatingBar(requireContext(), null, android.R.attr.ratingBarStyleSmall);
            editRating.setNumStars(5);
            editRating.setStepSize(0.5f);
            editRating.setIsIndicator(false);
            editRating.setRating(rating);
            editRating.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            editRating.setScaleX(1.2f);
            editRating.setScaleY(1.2f);
            editRating.setPadding(20, 20, 0, 20);

            editLayout.addView(editRating);

            LinearLayout saveCancelRow = new LinearLayout(requireContext());
            saveCancelRow.setOrientation(LinearLayout.HORIZONTAL);
            editLayout.addView(saveCancelRow);

            Button btnSave = new Button(requireContext());
            btnSave.setText("SAVE");
            btnSave.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_primary, null)
            );
            btnSave.setTextColor(getResources().getColor(R.color.white, null));
            LinearLayout.LayoutParams saveParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnSave.setLayoutParams(saveParams);
            saveCancelRow.addView(btnSave);

            Button btnCancel = new Button(requireContext());
            btnCancel.setText("CANCEL");
            btnCancel.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_secondary, null)
            );
            btnCancel.setTextColor(getResources().getColor(R.color.white, null));
            LinearLayout.LayoutParams cancelParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            btnCancel.setLayoutParams(cancelParams);
            saveCancelRow.addView(btnCancel);

            btnEdit.setOnClickListener(v -> {
                editLayout.setVisibility(
                        editLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                );
                etEditText.requestFocus();
            });

            btnDelete.setOnClickListener(v -> {
                reviewRepository.deleteReview(currentUser, title);
                Toast.makeText(requireContext(), "🗑️ Review deleted", Toast.LENGTH_SHORT).show();
                // Update summary + list
                List<ReviewEntity> updated = reviewRepository.getReviewsForUser(currentUser);
                binding.tvLastReview.setText(buildReviewsSummary(updated));
                refreshReviewList();
            });

            btnSave.setOnClickListener(v -> {
                String newText = etEditText.getText().toString().trim();
                float newRating = editRating.getRating();
                if (newText.isEmpty()) {
                    Toast.makeText(requireContext(), "⚠️ Review text cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                reviewRepository.updateReview(currentUser, title, newText, newRating);
                Toast.makeText(requireContext(), "✅ Review updated!", Toast.LENGTH_SHORT).show();

                // Update summary text at the top
                List<ReviewEntity> updated = reviewRepository.getReviewsForUser(currentUser);
                binding.tvLastReview.setText(buildReviewsSummary(updated));

                refreshReviewList();
            });

            btnCancel.setOnClickListener(v -> editLayout.setVisibility(View.GONE));
        }
    }

    // Build a summary string like old SessionManager.getAllReviews()
    private String buildReviewsSummary(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return "No previous reviews yet.";
        }

        StringBuilder builder = new StringBuilder();
        for (ReviewEntity r : reviews) {
            builder.append("🎮 ")
                    .append(r.gameTitle)
                    .append(" — ")
                    .append(r.rating)
                    .append("★\n")
                    .append(r.reviewText)
                    .append("\n\n");
        }
        return builder.toString().trim();
    }

    // Animation helpers
    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view,
                "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0
        );
        animator.setDuration(500);
        animator.start();
    }

    private void bounceView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        animator.setDuration(300);
        animator2.setDuration(300);
        animator.setInterpolator(new BounceInterpolator());
        animator2.setInterpolator(new BounceInterpolator());
        animator.start();
        animator2.start();
    }

    private void scaleUpView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
        animator.setDuration(200);
        animator2.setDuration(200);
        animator.start();
        animator2.start();
    }

    private void fadeInView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(500);
        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}