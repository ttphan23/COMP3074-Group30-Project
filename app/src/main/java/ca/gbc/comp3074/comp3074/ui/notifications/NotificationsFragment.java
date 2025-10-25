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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ca.gbc.comp3074.comp3074.R;
import ca.gbc.comp3074.comp3074.SessionManager;
import ca.gbc.comp3074.comp3074.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new SessionManager(requireContext());

        Spinner spinnerGames = root.findViewById(R.id.spinnerGames);
        EditText etReview = root.findViewById(R.id.etReview);
        RatingBar ratingBar = root.findViewById(R.id.ratingbBar);
        Button btnSubmit = root.findViewById(R.id.btnSubmitReview);
        TextView txtSaved = root.findViewById(R.id.tvLastReview);
        Button btnClear = root.findViewById(R.id.btnClearReviews);

        // Populate Spinner with dummy game titles
        String[] games = {"Elden Ring", "The Legend of Zelda: BOTW", "Hollow Knight", "Stardew Valley", "God of War", "Baldur's Gate 3", "The Witcher 3", "Sekiro: Shadows Die Twice"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, games);
        spinnerGames.setAdapter(adapter);

        // Restore previous review
        String last = sessionManager.getAllReviews();
        if (last != null && !last.equals("No reviews yet.")) {
            txtSaved.setText(last);
        } else {
            txtSaved.setText("No previous reviews yet.");
        }

        // Submit review with animations and feedback
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
                // Shake animation for EditText
                shakeView(etReview);
                return;
            }

            if (rating == 0) {
                Snackbar.make(root, "⚠️ Please add a rating!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.accent, null))
                        .setTextColor(getResources().getColor(R.color.white, null))
                        .show();
                // Bounce animation for RatingBar
                bounceView(ratingBar);
                return;
            }

            boolean success = sessionManager.saveReview(title, text, rating);

            // Save review
            if (!success) {
                Snackbar.make(root, "⚠️ You already reviewed this game!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.accent, null))
                        .setTextColor(getResources().getColor(R.color.white, null))
                        .setAction("OK", view -> {})
                        .show();

                shakeView(etReview);
                return;
            }
            
            // Update display
            String allReviews = sessionManager.getAllReviews();
            txtSaved.setText(allReviews);
            
            // Clear inputs
            etReview.setText("");
            ratingBar.setRating(0f);

            // Success feedback with Snackbar
            Snackbar.make(root, "✅ Review submitted successfully!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.button_primary, null))
                    .setTextColor(getResources().getColor(R.color.white, null))
                    .setAction("VIEW", view -> {
                        // Scroll to reviews section
                        txtSaved.requestFocus();
                    })
                    .show();

            refreshReviewList(root);

            // Success animation
            scaleUpView(btnSubmit);

            // Show toast as well for extra feedback
            Toast.makeText(requireContext(), "🎮 Review saved!", Toast.LENGTH_SHORT).show();
        });

        // Clear reviews with confirmation
        btnClear.setOnClickListener(v -> {
            Snackbar.make(root, "Are you sure you want to delete all reviews?", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.button_danger, null))
                    .setTextColor(getResources().getColor(R.color.white, null))
                    .setAction("DELETE", view -> {
                        sessionManager.clearReviews();
                        txtSaved.setText("No previous reviews yet.");
                        Toast.makeText(requireContext(), "All reviews cleared", Toast.LENGTH_SHORT).show();
                        fadeInView(txtSaved);
                    })
                    .show();
        });

        // Add bounce animation on rating bar change
        ratingBar.setOnRatingBarChangeListener((bar, rating1, fromUser) -> {
            if (fromUser) {
                bounceView(bar);
            }
        });
        refreshReviewList(root);
        return root;
    }

    private void refreshReviewList(View root) {
        LinearLayout reviewContainer = root.findViewById(R.id.reviewContainer);
        reviewContainer.removeAllViews();

        try {
            JSONArray reviews = new JSONArray(sessionManager.getAllReviewsJSON());
            String currentUser = sessionManager.getUsername();

            if (reviews.length() == 0) {
                TextView empty = new TextView(requireContext());
                empty.setText("No previous reviews yet.");
                empty.setTextColor(getResources().getColor(R.color.text_secondary, null));
                reviewContainer.addView(empty);
                return;
            }

            for (int i = 0; i < reviews.length(); i++) {
                JSONObject review = reviews.getJSONObject(i);
                if (!review.getString("user").equals(currentUser)) continue;

                final String title = review.optString("title", "Untitled");
                final String text = review.optString("text", "(No content)");
                final float rating = (float) review.optDouble("rating", 0.0);

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
                btnEdit.setBackgroundTintList(getResources().getColorStateList(R.color.button_secondary, null));
                btnEdit.setTextColor(getResources().getColor(R.color.white, null));
                LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                btnEdit.setLayoutParams(editParams);
                btnRow.addView(btnEdit);

                Button btnDelete = new Button(requireContext());
                btnDelete.setText("DELETE");
                btnDelete.setAllCaps(true);
                btnDelete.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_danger));
                btnDelete.setTextColor(getResources().getColor(R.color.white, null));
                LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
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

                RatingBar editRating = new RatingBar(requireContext(), null, android.R.attr.ratingBarStyleIndicator);
                editRating.setNumStars(5);
                editRating.setStepSize(0.5f);
                editRating.setIsIndicator(false);
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
                btnSave.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary, null));
                btnSave.setTextColor(getResources().getColor(R.color.white, null));
                LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                btnSave.setLayoutParams(saveParams);
                saveCancelRow.addView(btnSave);

                Button btnCancel = new Button(requireContext());
                btnCancel.setText("CANCEL");
                btnCancel.setBackgroundTintList(getResources().getColorStateList(R.color.button_secondary, null));
                btnCancel.setTextColor(getResources().getColor(R.color.white, null));
                LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                btnCancel.setLayoutParams(cancelParams);
                saveCancelRow.addView(btnCancel);

                btnEdit.setOnClickListener(v -> {
                    editLayout.setVisibility(editLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    etEditText.requestFocus();
                });

                btnDelete.setOnClickListener(v -> {
                    try {
                        sessionManager.deleteReview(title);
                        Toast.makeText(requireContext(), "🗑️ Review deleted", Toast.LENGTH_SHORT).show();
                        refreshReviewList(root);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

                btnSave.setOnClickListener(v -> {
                    String newText = etEditText.getText().toString().trim();
                    float newRating = editRating.getRating();
                    if (newText.isEmpty()) {
                        Toast.makeText(requireContext(), "⚠️ Review text cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sessionManager.updateReview(title, newText, newRating);
                    Toast.makeText(requireContext(), "✅ Review updated!", Toast.LENGTH_SHORT).show();
                    refreshReviewList(root);
                    reviewContainer.invalidate();
                });

                btnCancel.setOnClickListener(v -> editLayout.setVisibility(View.GONE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Animation helpers
    private void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
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
