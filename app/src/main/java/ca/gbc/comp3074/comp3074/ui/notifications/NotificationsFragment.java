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
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
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

            // Save review
            sessionManager.saveReview(title, text, rating);
            
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

        return root;
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
