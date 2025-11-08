# Gemini AI Integration Guide

## Overview
The chat feature now uses Google Gemini AI to provide intelligent responses about menu items, ingredients, preparation methods, and food stories.

## Features

### AI-Powered Responses
The chat assistant can now:
- Answer questions about menu items (Pizza, Burger, Salad)
- Provide information about ingredients
- Explain preparation methods
- Share stories and background about dishes
- Give recommendations based on customer preferences
- Answer pricing questions

### Fallback System
If the Gemini API is not configured or fails, the system automatically falls back to intelligent keyword-based responses that cover:
- Specific menu items (pizza, burger, salad)
- Ingredients (nguyên liệu)
- Preparation methods (chế biến, cooking)
- Pricing information
- Recommendations

## Setup Instructions

### 1. Get Gemini API Key

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy your API key

### 2. Configure the App

Open `GeminiChatService.java` and replace the API key:

```java
private static final String API_KEY = "YOUR_ACTUAL_API_KEY_HERE";
```

**Important Security Note:** For production apps:
- Store API keys in `local.properties` or environment variables
- Use BuildConfig to access keys
- Never commit API keys to version control
- Consider using a backend proxy to hide API keys

### 3. Build and Run

The app will automatically use:
- **Gemini API** if configured (provides AI-powered responses)
- **Fallback responses** if API key is not set (provides keyword-based responses)

## How It Works

### Request Flow

```
User Message
    ↓
ChatActivity.sendMessage()
    ↓
Save to Database
    ↓
GeminiChatService.generateResponse()
    ↓
Build Context (Menu + User Question)
    ↓
Call Gemini API
    ↓
Parse Response
    ↓
Save AI Response to Database
    ↓
Display in Chat UI
```

### Context Building

The service automatically includes menu context in every request:
```
Menu items available:
- Pizza (Fast Food): A classic pizza... - Price: $10.99
- Burger (Fast Food): A juicy beef patty... - Price: $5.99
- Salad (Healthy): A mix of fresh greens... - Price: $7.99

Customer question: [User's message]
```

This ensures the AI has full knowledge of your menu when responding.

## Example Conversations

### About Ingredients
**Customer:** "What ingredients are in the pizza?"
**AI:** "Our pizza is made with rich tomato sauce, mozzarella cheese, and pepperoni topping, all baked to perfection. The ingredients are fresh and high-quality!"

### About Preparation
**Customer:** "How do you prepare the burger?"
**AI:** "Our burger is made with a juicy beef patty grilled to perfection, served with fresh lettuce, tomatoes, onions, and our special sauce in a toasted sesame seed bun."

### Recommendations
**Customer:** "What do you recommend for a healthy meal?"
**AI:** "I recommend our Salad! It's made with fresh greens, cherry tomatoes, cucumbers, and bell peppers with a light vinaigrette dressing - perfect for a healthy meal at $7.99."

## Customization

### Modify AI Behavior

Edit the prompt in `GeminiChatService.buildPrompt()`:

```java
private String buildPrompt(String userMessage, String menuContext) {
    return "You are a [CUSTOMIZE ROLE]. " +
           menuContext + "\n\n" +
           "Customer question: " + userMessage + "\n\n" +
           "[CUSTOMIZE INSTRUCTIONS]";
}
```

### Add More Fallback Responses

Edit `GeminiChatService.generateFallbackResponse()` to add more keywords and responses:

```java
if (lowerPrompt.contains("your-keyword")) {
    return "Your custom response here";
}
```

### Adjust Response Delay

In `ChatActivity.sendMessage()`, change the delay:

```java
handler.postDelayed(() -> generateAIResponse(messageText), 1000); // milliseconds
```

## Technical Details

### Dependencies Added

```gradle
// Gemini API
implementation 'com.google.ai.client.generativeai:generativeai:0.1.2'
implementation 'com.google.guava:guava:31.1-android'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

### Permissions Added

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### New Files

1. **GeminiChatService.java** - Service for AI integration
   - API communication
   - Context building
   - Fallback logic

2. **Updated ChatActivity.java** - Enhanced with AI responses
   - GeminiChatService integration
   - Error handling
   - Fallback mechanism

## Troubleshooting

### API Not Responding
- Check internet connection
- Verify API key is correct
- Check Logcat for error messages
- System will use fallback responses

### Rate Limits
- Gemini API has free tier limits
- Consider implementing request throttling
- Use backend proxy for production

### Network Errors
- App handles errors gracefully
- Falls back to keyword-based responses
- User sees error toast

## Future Enhancements

Potential improvements:
- [ ] Streaming responses for real-time chat feel
- [ ] Multi-language support
- [ ] Image understanding for food photos
- [ ] Order placement through chat
- [ ] Conversation history analysis
- [ ] Personalized recommendations
- [ ] Voice input/output

## Security Best Practices

For production deployment:

1. **API Key Management**
   ```gradle
   // In local.properties
   GEMINI_API_KEY=your_key_here
   
   // In build.gradle
   buildConfigField "String", "GEMINI_API_KEY", 
       "\"${project.findProperty('GEMINI_API_KEY') ?: ''}\""
   ```

2. **Backend Proxy**
   - Move API calls to your server
   - Hide API keys from client app
   - Add authentication and rate limiting

3. **Content Filtering**
   - Validate user input
   - Filter inappropriate content
   - Monitor API usage

## Support

For issues or questions:
- Check Logcat for detailed error messages
- Review [Gemini API Documentation](https://ai.google.dev/docs)
- Test with fallback responses first
- Ensure internet permission is granted

---

**Note:** The app works perfectly with or without Gemini API configured. The intelligent fallback system ensures users always get helpful responses about your menu!
