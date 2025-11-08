# Chat Feature Documentation

## Overview
This feature implements a chat screen where customers can communicate with the store in the OrderFood Android application.

## Implementation Details

### Architecture
The chat feature follows the existing project's MVVM architecture pattern with Room database for data persistence.

### Components Created

#### 1. Model Layer
- **ChatMessage.java** (`app/src/main/java/com/example/orderfood/model/ChatMessage.java`)
  - Room entity representing a chat message
  - Fields: id, message, timestamp, isFromCustomer, senderName
  - Stored in `chat_messages` table

#### 2. Data Access Layer
- **ChatDao.java** (`app/src/main/java/com/example/orderfood/dao/ChatDao.java`)
  - Data Access Object for chat operations
  - Methods: insert(), getAllMessages(), deleteAllMessages()

#### 3. Database
- **AppDatabase.java** (updated)
  - Added ChatMessage entity
  - Added chatDao() abstract method
  - Database version incremented to 4

#### 4. View Layer
- **ChatActivity.java** (`app/src/main/java/com/example/orderfood/view/ChatActivity.java`)
  - Main chat screen activity
  - Handles sending messages
  - Loads and displays message history
  - Simulates store responses

- **ChatAdapter.java** (`app/src/main/java/com/example/orderfood/view/ChatAdapter.java`)
  - RecyclerView adapter for chat messages
  - Differentiates between customer and store messages
  - Formats timestamps

#### 5. Layouts
- **activity_chat.xml** (`app/src/main/res/layout/activity_chat.xml`)
  - Chat screen layout
  - Header with back button and title
  - RecyclerView for messages
  - Message input field and send button

- **chat_message_item.xml** (`app/src/main/res/layout/chat_message_item.xml`)
  - Individual message item layout
  - Customer messages (right-aligned, green background)
  - Store messages (left-aligned, gray background)
  - Displays message text, sender name, and timestamp

- **activity_product_list.xml** (updated)
  - Added chat button next to cart button

#### 6. Resources
- **ic_chat.xml** (`app/src/main/res/drawable/ic_chat.xml`)
  - Chat icon for navigation button

#### 7. Navigation
- **ProductListActivity.java** (updated)
  - Added chat button with click listener
  - Opens ChatActivity when clicked

- **AndroidManifest.xml** (updated)
  - Registered ChatActivity

## Features

1. **Message Persistence**: All messages are stored in Room database and persist across app restarts
2. **Two-way Chat UI**: 
   - Customer messages appear on the right with green background
   - Store messages appear on the left with gray background
3. **Timestamps**: Each message shows the time it was sent
4. **Auto-scroll**: Chat automatically scrolls to show the latest message
5. **Simulated Responses**: Store automatically responds with random helpful messages (for demonstration)
6. **Easy Access**: Chat button accessible from the product list screen

## Usage

1. From the Product List screen, tap the chat icon (next to the cart icon)
2. Type a message in the input field at the bottom
3. Tap the send button to send your message
4. Messages are saved and will appear when you return to the chat screen
5. Tap the back button to return to the product list

## Database Schema

### chat_messages table
- `id` (INTEGER, PRIMARY KEY, AUTO INCREMENT)
- `message` (TEXT) - The message content
- `timestamp` (INTEGER) - Unix timestamp in milliseconds
- `is_from_customer` (INTEGER/BOOLEAN) - 1 if from customer, 0 if from store
- `sender_name` (TEXT) - Name of the sender

## Future Enhancements

Potential improvements for production use:
- Real-time messaging using Firebase or WebSocket
- Push notifications for new messages
- Image/file sharing
- Message read receipts
- Typing indicators
- Message editing/deletion
- Admin dashboard for store to manage chats
- Multiple chat rooms/conversations
