package ca.gbc.comp3074.comp3074.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        TextView feedText = root.findViewById(R.id.tvFeedContent);

        String json = sessionManager.getAllReviewsJSON();
        try {
            JSONArray arr = new JSONArray(json);
            if (arr.length() == 0) {
                feedText.setText("No activity in feed yet.");
            } else {
                StringBuilder feedBuilder = new StringBuilder();
                for (int i = arr.length() - 1; i >= 0; i--) {
                    JSONObject review = arr.getJSONObject(i);
                    String user = review.optString("user", "Guest");
                    String title = review.optString("title", "Unknown Game");
                    double rating = review.optDouble("rating", 0);
                    String text = review.optString("text", "");

                    feedBuilder.append("👤 ").append(user).append(" reviewed ")
                            .append(title).append(" — ").append(rating).append("★")
                            .append("\n")
                            .append(text)
                            .append("\n-----------------------------\n");
                }
                feedText.setText(feedBuilder.toString());
            }
        } catch (JSONException e) {
            feedText.setText("Error loading feed.");
            e.printStackTrace();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
