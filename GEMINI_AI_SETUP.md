# Gemini AI Integration Guide

## Overview
The chat feature uses Google Gemini AI with **Function Calling** to provide intelligent, context-aware responses. The AI can analyze natural language, maintain conversation history, and execute actions like adding items to cart, retrieving menu information, and answering questions about products.

## Key Features

### AI-Powered Function Calling
The system implements Gemini's function calling capability, allowing the AI to:
- **Understand Intent**: Analyzes user messages to determine actions (order, query, browse)
- **Extract Parameters**: Automatically parses item names, quantities, and other details
- **Maintain Context**: Remembers conversation history for multi-turn interactions
- **Handle Missing Data**: Asks clarifying questions when information is incomplete
- **Execute Actions**: Calls appropriate functions and generates natural language responses

### Available Functions

1. **addToCart**: Add products to shopping cart
   - Triggers: "Tôi lấy", "Cho tôi", "Đặt", "Mua"
   - Handles quantity expressions: "3 ly", "hai cái", "một phần"

2. **getMenu**: Retrieve full menu with prices
   - Triggers: "menu", "thực đơn", "có món gì"

3. **getProductInfo**: Get detailed product information
   - Triggers: "giá bao nhiêu", "có gì", "làm từ gì"

### Conversation History
The system maintains full conversation context:
- User messages
- AI responses
- Function calls and results
- Enables natural multi-turn conversations

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

The app uses the Gemini 1.5 Flash model which supports function calling.

## How It Works

### Request Flow

```
User Message
    ↓
Add to Conversation History
    ↓
Send to Gemini API (with function declarations)
    ↓
AI Analyzes Intent
    ↓
    ├─→ Returns Function Call
    │       ↓
    │   Execute Function (addToCart/getMenu/getProductInfo)
    │       ↓
    │   Add Result to History
    │       ↓
    │   Call Gemini Again for Natural Response
    │       ↓
    └─→ Returns Text Response
            ↓
Display to User & Add to History
```

### Function Calling Process

1. **Analysis Phase**: 
   - Gemini receives user message with function declarations
   - AI decides which function (if any) to call based on intent
   - Extracts required parameters from natural language

2. **Execution Phase**:
   - App receives function call from AI
   - Executes the function with provided parameters
   - Returns structured result to AI

3. **Response Generation Phase**:
   - AI receives function result
   - Generates natural language response
   - App displays response to user

## Example Conversations

### Simple Order
```
User: "Cho tôi một pizza"
AI: Calls addToCart("Pizza", 1)
AI: "Tuyệt vời! Tôi đã thêm 1 Pizza vào giỏ hàng của bạn. Bạn có muốn gọi thêm gì không?"
```

### Context-Aware Order
```
User: "Pizza giá bao nhiêu?"
AI: Calls getProductInfo("Pizza")
AI: "Pizza có giá $10.99. Bạn có muốn đặt không?"

User: "Tôi lấy nó"
AI: (Remembers "nó" = Pizza from context)
AI: Calls addToCart("Pizza", 1)
AI: "Đã thêm Pizza vào giỏ hàng của bạn!"
```

### Missing Parameter Handling
```
User: "Tôi muốn burger"
AI: "Bạn muốn bao nhiêu burger?"

User: "3 cái"
AI: Calls addToCart("Burger", 3)
AI: "Đã thêm 3 Burger vào giỏ hàng..."
```

### Menu Browsing
```
User: "Có món gì?"
AI: Calls getMenu()
AI: "Chúng tôi có các món sau:
     - Pizza ($10.99): A classic pizza...
     - Burger ($5.99): A juicy beef patty...
     - Salad ($7.99): Fresh greens...
     Bạn muốn đặt món gì?"
```

## Technical Details

### Model
- **Name**: gemini-1.5-flash
- **Endpoint**: `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`
- **Features**: Function calling, conversation history, system instructions

### Function Declarations
Functions are defined with detailed Vietnamese descriptions to guide AI behavior:

```java
FunctionDeclaration addToCart = new FunctionDeclaration(
    "addToCart",
    "Thêm sản phẩm cụ thể và số lượng vào giỏ hàng. Chỉ sử dụng khi người dùng có ý định đặt mua hàng rõ ràng (ví dụ: 'Tôi lấy', 'Cho tôi', 'Thêm vào giỏ', 'Đặt', 'Mua'). Mô hình cần phân tích các từ như 'cái', 'suất', 'phần', 'ly' thành số lượng và chuyển tên gọi thông thường thành tên sản phẩm chính xác."
);
```

### System Instruction
```
"Bạn là trợ lý nhà hàng thân thiện và hữu ích. Nhiệm vụ của bạn là giúp khách hàng đặt món ăn, trả lời câu hỏi về menu, và cung cấp thông tin chi tiết về các món ăn. Hãy luôn lịch sự, nhiệt tình và cung cấp thông tin chính xác."
```

## Customization

### Add New Functions

1. Create function declaration in `initializeFunctions()`:
```java
FunctionDeclaration newFunction = new FunctionDeclaration(
    "functionName",
    "Detailed description in Vietnamese"
);
newFunction.addParameter(new FunctionDeclaration.Parameter(
    "paramName",
    "string",
    "Parameter description",
    true  // required
));
functionDeclarations.add(newFunction);
```

2. Implement execution logic in `executeFunction()`:
```java
case "functionName":
    result = executeNewFunction(args);
    break;
```

3. Create implementation method:
```java
private JSONObject executeNewFunction(JSONObject args) throws Exception {
    // Implementation
    JSONObject result = new JSONObject();
    // ... process and return result
    return result;
}
```

### Modify AI Behavior

Edit system instruction in `callGeminiAPIWithFunctions()`:
```java
systemPart.put("text", "Your custom system instruction...");
```

### Adjust Function Descriptions

Function descriptions directly impact AI behavior. More detailed descriptions lead to better decision-making:
- Include example trigger phrases
- Explain parameter variations
- Specify when to use the function
- Use user's language (Vietnamese for Vietnamese users)

## Advantages Over Rule-Based Approach

| Feature | Old (Rule-Based) | New (Function Calling) |
|---------|------------------|------------------------|
| User Input | Exact keywords required | Natural language |
| Context | No memory | Full conversation history |
| Flexibility | Rigid patterns | Flexible understanding |
| Maintenance | Complex if/else chains | Clean function declarations |
| Quantity Parsing | Manual extraction | AI handles automatically |
| Multi-language | Duplicate logic | Single description |

## Troubleshooting

### API Not Responding
- Check internet connection
- Verify API key is correct
- Check Logcat for error messages: `adb logcat | grep GeminiChatService`
- Ensure using gemini-1.5-flash or gemini-1.5-pro (function calling support)

### Rate Limits
- Gemini API has free tier limits (15 requests/minute, 1M tokens/minute)
- Implement request throttling if needed
- Consider upgrading to paid tier for production

### Network Errors
- App handles errors gracefully
- User sees error toast
- Check logs for detailed error information

### Function Not Called
- Check function description clarity
- Verify parameter definitions
- Review conversation history in logs
- AI might ask for missing parameters first

## Migration from Old System

The old system used keyword matching:
```java
// OLD CODE (Removed)
if (lowerUserMessage.startsWith("y ")) {
    String itemName = lowerUserMessage.substring(2).trim();
    // ... add to cart
}
```

New system uses AI function calling:
```java
// NEW CODE
// AI automatically decides to call addToCart() based on user intent
// No keyword matching needed!
```

**Benefits of Migration**:
- ~120 lines of if/else removed
- Better user experience
- Easier to maintain and extend
- Supports natural language variations

## Security Best Practices

1. **API Key Management**
   ```gradle
   // In local.properties
   GEMINI_API_KEY=your_key_here
   
   // In build.gradle
   buildConfigField "String", "GEMINI_API_KEY", 
       "\"${project.findProperty('GEMINI_API_KEY') ?: ''}\""
   ```

2. **Backend Proxy** (Recommended for production)
   - Move API calls to your server
   - Hide API keys from client app
   - Add authentication and rate limiting
   - Monitor usage and costs

3. **Input Validation**
   - Validate all function parameters
   - Sanitize user inputs
   - Implement proper error handling

4. **Content Filtering**
   - Monitor conversation content
   - Filter inappropriate requests
   - Log suspicious activity

## Future Enhancements

Potential improvements:
- [ ] Add `removeFromCart` function
- [ ] Add `viewCart` function
- [ ] Add `checkout` function with order confirmation
- [ ] Add `getRecommendations` based on preferences
- [ ] Implement preference memory (dietary restrictions, favorites)
- [ ] Add voice input/output
- [ ] Support image understanding for food photos
- [ ] Multi-language support (auto-detect user language)
- [ ] Personalized recommendations based on history

## Documentation

- **Implementation Guide**: See [FUNCTION_CALLING_IMPLEMENTATION.md](FUNCTION_CALLING_IMPLEMENTATION.md) for detailed technical documentation
- **Chat Feature**: See [CHAT_FEATURE.md](CHAT_FEATURE.md) for chat UI documentation
- **Architecture**: See [ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md) for system architecture

## Support

For issues or questions:
- Check Logcat: `adb logcat | grep GeminiChatService`
- Review [Gemini API Documentation](https://ai.google.dev/docs)
- Check function calling examples: [Gemini Function Calling Guide](https://ai.google.dev/docs/function_calling)
- Enable debug logging in GeminiChatService

---

**Note**: This implementation provides a production-ready foundation for AI-powered ordering. The function calling approach is significantly more powerful and maintainable than keyword-based systems, while providing a natural conversational experience for users.
