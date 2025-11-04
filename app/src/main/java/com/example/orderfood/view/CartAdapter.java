package com.example.orderfood.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private CartListener cartListener;

    public interface CartListener {
        void onQuantityChange(int productId, int newQuantity);
        void onDelete(int productId);
    }

    public CartAdapter(List<CartItem> cartItemList, CartListener cartListener) {
        this.cartItemList = cartItemList;
        this.cartListener = cartListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_list_item, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        holder.productName.setText(cartItem.getProduct().getName());
        holder.productPrice.setText(String.format("$%.2f", cartItem.getProduct().getPrice()));
        holder.quantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.productImage.setImageResource(cartItem.getProduct().getImageId());

        holder.increaseButton.setOnClickListener(v -> {
            cartListener.onQuantityChange(cartItem.getProductId(), cartItem.getQuantity() + 1);
        });

        holder.decreaseButton.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                cartListener.onQuantityChange(cartItem.getProductId(), cartItem.getQuantity() - 1);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            cartListener.onDelete(cartItem.getProductId());
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItemList = cartItems;
        notifyDataSetChanged();
    }

    public List<CartItem> getCartItems() {
        return cartItemList;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantity;
        ImageButton increaseButton, decreaseButton, deleteButton;

        CartViewHolder(View view) {
            super(view);
            productImage = view.findViewById(R.id.cartProductImageView);
            productName = view.findViewById(R.id.cartProductNameTextView);
            productPrice = view.findViewById(R.id.cartProductPriceTextView);
            quantity = view.findViewById(R.id.quantityTextView);
            increaseButton = view.findViewById(R.id.increaseQuantityButton);
            decreaseButton = view.findViewById(R.id.decreaseQuantityButton);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
