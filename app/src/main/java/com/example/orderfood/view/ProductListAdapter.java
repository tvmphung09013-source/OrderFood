package com.example.orderfood.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.model.Product;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_list_item, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.name.setText(product.getName());
        holder.category.setText(product.getCategory());
        holder.price.setText(String.format("$%.2f", product.getPrice()));
        holder.rating.setText(String.valueOf(product.getRating()));

        int imageId = product.getImageId();
        try {
            if (imageId > 0) {
                holder.imageView.setImageResource(imageId);
            } else {
                holder.imageView.setImageResource(R.drawable.default_product_image);
            }
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.default_product_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filterList(List<Product> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, price, rating;
        ImageView imageView;

        ProductViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.productNameTextView);
            category = view.findViewById(R.id.productCategoryTextView);
            price = view.findViewById(R.id.productPriceTextView);
            rating = view.findViewById(R.id.productRatingTextView);
            imageView = view.findViewById(R.id.productImageView);
        }
    }
}