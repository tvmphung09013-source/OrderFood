package com.example.orderfood.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

	private final List<Review> reviews;

	public ReviewAdapter(List<Review> reviews) {
		this.reviews = reviews;
	}

	@NonNull
	@Override
	public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
		return new ReviewViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
		Review review = reviews.get(position);
		String title = "★ " + String.format(java.util.Locale.getDefault(), "%.1f", review.getRating()) + "  •  User " + review.getUserId();
		String subtitle = review.getComment() == null ? "" : review.getComment();
		holder.titleTextView.setText(title);
		holder.subtitleTextView.setText(subtitle);
	}

	@Override
	public int getItemCount() {
		return reviews == null ? 0 : reviews.size();
	}

	static class ReviewViewHolder extends RecyclerView.ViewHolder {
		TextView titleTextView;
		TextView subtitleTextView;

		ReviewViewHolder(@NonNull View itemView) {
			super(itemView);
			titleTextView = itemView.findViewById(android.R.id.text1);
			subtitleTextView = itemView.findViewById(android.R.id.text2);
		}
	}
}


