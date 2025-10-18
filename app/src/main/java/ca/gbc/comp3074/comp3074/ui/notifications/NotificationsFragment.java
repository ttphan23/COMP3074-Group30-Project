package ca.gbc.comp3074.comp3074.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
        String[] games = {"Elden Ring", "The Legend of Zelda: BOTW", "Hollow Knight", "Stardew Valley", "God of War"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, games);
        spinnerGames.setAdapter(adapter);

        // Restore previous review
        String last = sessionManager.getAllReviews();
        if (last != null) txtSaved.setText("Last review:\n\n" + last);

        // Submit review
        btnSubmit.setOnClickListener(v -> {
            String title = spinnerGames.getSelectedItem().toString();
            String text = etReview.getText().toString();
            float rating = ratingBar.getRating();

            sessionManager.saveReview(title, text, rating);
            txtSaved.setText("Last review:\n\n" + sessionManager.getAllReviews());
            etReview.setText("");
            ratingBar.setRating(0f);
        });

        // Clear reviews
        btnClear.setOnClickListener(v -> {
            sessionManager.clearReviews();
            txtSaved.setText("No previous reviews yet.");
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
