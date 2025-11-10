package com.example.orderfood.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

	private final List<OrderHistoryDisplay> displays;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

	public OrderHistoryAdapter(List<OrderHistoryDisplay> displays) {
		this.displays = displays;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		OrderHistoryDisplay d = displays.get(position);
		holder.productImage.setImageResource(d.productImageId);
		holder.productName.setText(d.productName);
		String meta = "Order #" + d.orderId + "  •  $" + String.format(Locale.getDefault(), "%.2f", d.totalAmount) +
				"  •  " + d.itemsCount + " items";
		holder.metaText.setText(meta);
		holder.dateText.setText(dateFormat.format(new Date(d.createdAt)));
	}

	@Override
	public int getItemCount() {
		return displays == null ? 0 : displays.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView productImage;
		TextView productName;
		TextView metaText;
		TextView dateText;
		ViewHolder(@NonNull View itemView) {
			super(itemView);
			productImage = itemView.findViewById(R.id.historyItemImage);
			productName = itemView.findViewById(R.id.historyItemName);
			metaText = itemView.findViewById(R.id.historyItemMeta);
			dateText = itemView.findViewById(R.id.historyItemDate);
		}
	}

	public static class OrderHistoryDisplay {
		public int orderId;
		public double totalAmount;
		public long createdAt;
		public int itemsCount;
		public String productName;
		public int productImageId;
	}
}


