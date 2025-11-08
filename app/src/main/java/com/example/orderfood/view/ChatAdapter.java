package com.example.orderfood.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfood.R;
import com.example.orderfood.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout customerMessageLayout;
        private LinearLayout storeMessageLayout;
        private TextView customerMessageText;
        private TextView customerTimeText;
        private TextView storeMessageText;
        private TextView storeTimeText;
        private TextView storeSenderName;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            customerMessageLayout = itemView.findViewById(R.id.customerMessageLayout);
            storeMessageLayout = itemView.findViewById(R.id.storeMessageLayout);
            customerMessageText = itemView.findViewById(R.id.customerMessageText);
            customerTimeText = itemView.findViewById(R.id.customerTimeText);
            storeMessageText = itemView.findViewById(R.id.storeMessageText);
            storeTimeText = itemView.findViewById(R.id.storeTimeText);
            storeSenderName = itemView.findViewById(R.id.storeSenderName);
        }

        public void bind(ChatMessage message) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String time = timeFormat.format(new Date(message.getTimestamp()));

            if (message.isFromCustomer()) {
                customerMessageLayout.setVisibility(View.VISIBLE);
                storeMessageLayout.setVisibility(View.GONE);
                customerMessageText.setText(message.getMessage());
                customerTimeText.setText(time);
            } else {
                customerMessageLayout.setVisibility(View.GONE);
                storeMessageLayout.setVisibility(View.VISIBLE);
                storeMessageText.setText(message.getMessage());
                storeTimeText.setText(time);
                storeSenderName.setText(message.getSenderName());
            }
        }
    }
}
