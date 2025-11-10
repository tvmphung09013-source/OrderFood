package com.example.orderfood.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.model.CartItem;

import java.util.List;
import java.util.Locale;

public class CheckoutOrderAdapter extends RecyclerView.Adapter<CheckoutOrderAdapter.ViewHolder> {
    private List<CartItem> items;
    public CheckoutOrderAdapter(List<CartItem> items) {
        this.items = items;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkout_order_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.name.setText(item.getProduct().getName());
        holder.quantity.setText(String.format(Locale.getDefault(), "x%d", item.getQuantity()));
        holder.price.setText(String.format(Locale.getDefault(), "$%.2f", item.getProduct().getPrice()));
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, price;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.checkoutItemName);
            quantity = itemView.findViewById(R.id.checkoutItemQuantity);
            price = itemView.findViewById(R.id.checkoutItemPrice);
        }
    }
}
