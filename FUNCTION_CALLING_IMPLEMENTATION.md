# Gemini Function Calling Implementation Guide

## Overview

This document explains the migration from rule-based keyword matching to AI-powered function calling using Google's Gemini API. This approach allows the AI to intelligently analyze user intent and execute appropriate actions without relying on hardcoded keyword patterns.

## Architecture Changes

### Before: Rule-Based Approach
```
User Message → Keyword Matching (if/else) → Direct Response/Action
```

Problems:
- Required exact keyword matches (e.g., "y pizza")
- No context awareness between messages
- Inflexible to natural language variations
- Hard to maintain and extend

### After: AI Function Calling Approach
```
User Message → Conversation History → Gemini AI Analysis → Function Call Decision → 
Function Execution → Result to AI → Natural Language Response
```

Benefits:
- AI understands natural language intent
- Maintains conversation context
- Handles missing parameters automatically
- Supports multi-turn conversations
- Extensible through function declarations

## Key Components

### 1. FunctionDeclaration.java
Defines functions that AI can call. Each function includes:
- **name**: Unique identifier
- **description**: Detailed Vietnamese description to guide AI
- **parameters**: Required and optional parameters with types and descriptions

Example:
```java
FunctionDeclaration addToCart = new FunctionDeclaration(
    "addToCart",
    "Thêm sản phẩm cụ thể và số lượng vào giỏ hàng. Chỉ sử dụng khi người dùng có ý định đặt mua hàng rõ ràng..."
);
addToCart.addParameter(new FunctionDeclaration.Parameter(
    "itemName",
    "string",
    "Tên chính xác của sản phẩm trong menu",
    true  // required
));
```

### 2. ConversationHistory.java
Manages conversation context across multiple messages. Tracks:
- User messages
- Model (AI) responses
- Function calls made by AI
- Function execution results

This enables AI to:
- Remember previous context (e.g., "I want it" referring to previously mentioned pizza)
- Make informed decisions based on conversation flow
- Handle multi-step interactions

### 3. GeminiChatService.java (Refactored)
Core service implementing function calling:

#### Key Methods:

**initializeFunctions()**
- Defines three functions: `addToCart`, `getMenu`, `getProductInfo`
- Each with detailed Vietnamese descriptions for better AI understanding

**generateResponse(String userMessage, ResponseCallback callback)**
- Adds user message to conversation history
- Calls Gemini API with function declarations
- Processes AI response (text or function call)

**callGeminiAPIWithFunctions(ResponseCallback callback)**
- Builds request with conversation history and function declarations
- Includes system instruction in Vietnamese
- Sends to Gemini 1.5 Flash (supports function calling)

**processGeminiResponse(String responseStr, ResponseCallback callback)**
- Checks if AI returned a function call or text
- If function call: executes function → adds result to history → calls AI again for natural response
- If text: returns directly to user

**executeFunction(String functionName, JSONObject args)**
- Routes to appropriate function implementation
- Returns structured JSON result

## Function Implementations

### 1. addToCart
**Purpose**: Add products to shopping cart

**Parameters**:
- `itemName` (string, required): Product name
- `quantity` (integer, required): Quantity to add

**Process**:
1. Search product in database by name (case-insensitive)
2. If found: Add/update cart item in database
3. Return success with product details or error if not found

**AI Understanding**: 
- Triggered by: "Tôi lấy", "Cho tôi", "Đặt", "Mua"
- Parses quantities from: "3 ly", "2 cái", "một phần"
- Normalizes product names automatically

### 2. getMenu
**Purpose**: Retrieve full menu with prices

**Parameters**: None

**Process**:
1. Query all products from database
2. Format as JSON array with name, category, price, description
3. Return menu list

**AI Understanding**:
- Triggered by: "menu", "thực đơn", "có món gì"

### 3. getProductInfo
**Purpose**: Get detailed information about specific product

**Parameters**:
- `itemName` (string, required): Product name to query

**Process**:
1. Search product in database
2. Return detailed info: name, category, price, description, rating
3. Return error if not found

**AI Understanding**:
- Triggered by: "giá bao nhiêu", "có gì", "làm từ gì"

## Conversation Flow Examples

### Example 1: Simple Order
```
User: "Cho tôi một pizza"
AI Analysis: Intent = order, item = pizza, quantity = 1
AI Action: Calls addToCart("Pizza", 1)
Function Result: {"success": true, "message": "Đã thêm 1 Pizza vào giỏ hàng", ...}
AI Response: "Tuyệt vời! Tôi đã thêm 1 Pizza vào giỏ hàng của bạn. Bạn có muốn gọi thêm gì không?"
```

### Example 2: Multi-turn Conversation
```
User: "Pizza giá bao nhiêu?"
AI Action: Calls getProductInfo("Pizza")
Function Result: {"name": "Pizza", "price": 10.99, ...}
AI Response: "Pizza có giá $10.99. Bạn có muốn đặt không?"

User: "Tôi lấy nó"
AI Analysis: "nó" refers to Pizza from context
AI Action: Calls addToCart("Pizza", 1)
AI Response: "Đã thêm Pizza vào giỏ hàng..."
```

### Example 3: Missing Parameter Handling
```
User: "Tôi muốn burger"
AI Analysis: Intent = order, item = burger, quantity = missing
AI Response: (No function call) "Bạn muốn bao nhiêu burger?"

User: "3 cái"
AI Analysis: quantity = 3, item from context = burger
AI Action: Calls addToCart("Burger", 3)
AI Response: "Đã thêm 3 Burger vào giỏ hàng..."
```

## Technical Details

### API Endpoint
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
```

**Note**: Use `gemini-1.5-flash` or `gemini-1.5-pro` for function calling support. Earlier models like `gemini-pro` don't support this feature.

### Request Format
```json
{
  "contents": [
    {
      "role": "user",
      "parts": [{"text": "Cho tôi pizza"}]
    }
  ],
  "tools": [
    {
      "functionDeclarations": [
        {
          "name": "addToCart",
          "description": "Thêm sản phẩm...",
          "parameters": {
            "type": "object",
            "properties": {
              "itemName": {"type": "string", "description": "..."},
              "quantity": {"type": "integer", "description": "..."}
            },
            "required": ["itemName", "quantity"]
          }
        }
      ]
    }
  ],
  "systemInstruction": {
    "parts": [{"text": "Bạn là trợ lý nhà hàng..."}]
  }
}
```

### Response Format (Function Call)
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "functionCall": {
          "name": "addToCart",
          "args": {
            "itemName": "Pizza",
            "quantity": 1
          }
        }
      }]
    }
  }]
}
```

### Response Format (Text)
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "Tuyệt vời! Tôi đã thêm Pizza vào giỏ hàng..."
      }]
    }
  }]
}
```

## Best Practices

### 1. Function Descriptions
- Write detailed descriptions in Vietnamese (matches user language)
- Include example trigger phrases
- Explain parameter normalization (e.g., "ly" → quantity)
- Be specific about when to use the function

### 2. Conversation History
- Always maintain conversation state
- Add all interactions to history (user, model, functions)
- Don't reset history during a session
- Consider implementing history pruning for very long conversations

### 3. Parameter Handling
- Use required vs optional parameters appropriately
- Provide default values when sensible (e.g., quantity defaults to 1)
- Let AI ask for missing required parameters

### 4. Error Handling
- Return structured errors from function executions
- AI will convert errors to natural language
- Log all errors for debugging

### 5. System Instructions
- Keep system instruction clear and concise
- Define AI's role and behavior
- Use Vietnamese for Vietnamese-speaking users

## Advantages Over Rule-Based Approach

| Aspect | Rule-Based | Function Calling |
|--------|-----------|------------------|
| Flexibility | Exact keyword match required | Natural language understanding |
| Context | No context awareness | Full conversation context |
| Extensibility | Add if/else for each case | Add function declaration |
| Maintainability | Complex nested conditions | Clean function definitions |
| User Experience | Rigid command syntax | Natural conversation |
| Quantity Parsing | Manual parsing needed | AI handles automatically |
| Missing Data | Hardcoded prompts | AI asks naturally |
| Multi-language | Duplicate logic for each | Single description guides AI |

## Migration Impact

### Removed Code
- All keyword-based `if (lowerUserMessage.contains(...))` checks
- Manual pattern matching for "y [item_name]"
- Hardcoded response strings for each scenario
- Manual quantity extraction logic

### Added Code
- FunctionDeclaration model class
- ConversationHistory model class
- Function execution methods
- API request/response processing for function calling

### Code Reduction
- ~120 lines of if/else conditions → ~90 lines of function declarations
- More maintainable and extensible
- Better separation of concerns

## Testing Recommendations

### Test Scenarios
1. **Basic Orders**: "Cho tôi pizza", "Tôi muốn burger"
2. **Quantities**: "3 pizza", "hai cái burger", "một ly coca"
3. **Context**: Ask price → Order ("Tôi lấy nó")
4. **Menu**: "Có món gì?", "Menu"
5. **Product Info**: "Pizza có gì?", "Burger giá bao nhiêu?"
6. **Missing Parameters**: "Tôi muốn burger" → AI asks quantity
7. **Invalid Items**: "Cho tôi sushi" → AI explains not available
8. **Multiple Items**: Sequential orders in same conversation

### Expected Behaviors
- AI remembers context across messages
- AI asks for missing information naturally
- AI handles variations in phrasing
- AI converts function results to natural Vietnamese

## Future Enhancements

### Potential Additions
1. **removeFromCart** function
2. **updateQuantity** function
3. **getCart** function to view current cart
4. **checkout** function to place order
5. **getRecommendations** based on preferences
6. **searchByIngredient** function
7. **filterByPrice** function
8. **getPromotions** function

### Advanced Features
1. **Multi-turn Order Building**: Collect items across multiple messages
2. **Preference Memory**: Remember user preferences (vegetarian, etc.)
3. **Smart Suggestions**: Based on cart contents
4. **Order History**: Reference previous orders
5. **Conversational Checkout**: Confirm order details naturally

## Security Considerations

1. **API Key Management**
   - Never hardcode in production
   - Use environment variables or BuildConfig
   - Rotate keys regularly

2. **Input Validation**
   - Validate function parameters
   - Sanitize product names
   - Check quantity ranges

3. **Rate Limiting**
   - Implement request throttling
   - Handle API quotas gracefully

4. **Error Messages**
   - Don't expose internal errors to users
   - Log detailed errors server-side

## Conclusion

The migration to Gemini Function Calling represents a significant improvement in the chat system's capabilities. By letting AI handle intent recognition and parameter extraction, we've created a more natural, flexible, and maintainable solution that provides a better user experience while reducing code complexity.

The conversation history management ensures context awareness, and the detailed function descriptions guide the AI to make appropriate decisions without rigid rule matching.
