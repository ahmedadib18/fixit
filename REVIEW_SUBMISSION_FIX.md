# Review Submission Fix

## Problem
When submitting a review after a session, the application returned a 500 Internal Server Error with the message:
```
No static resource api/reviews for request '/api/reviews'
```

## Root Cause
The frontend and backend API endpoints were mismatched:

**Frontend was calling:**
- `POST /api/reviews` with body: `{ sessionId, userId, helperId, rating, reviewText }`

**Backend expected:**
- `POST /api/reviews/sessions/{sessionId}` with body: `{ userId, helperId, rating, reviewText }`

## Solution Applied

### 1. Fixed Frontend Review Service (`frontend/src/services/reviewService.js`)

**Changed:**
```javascript
submitReview: async (reviewData) => {
  const response = await api.post('/reviews', reviewData)
  return response.data
}
```

**To:**
```javascript
submitReview: async (reviewData) => {
  const { sessionId, ...requestBody } = reviewData
  const response = await api.post(`/reviews/sessions/${sessionId}`, requestBody)
  return response.data
}
```

This extracts `sessionId` from the request data and puts it in the URL path, while sending the rest as the request body.

### 2. Fixed Other Review Endpoints

**Changed:**
```javascript
getHelperReviews: async (helperId) => {
  const response = await api.get(`/reviews/helper/${helperId}`)
  return response.data
}
```

**To:**
```javascript
getHelperReviews: async (helperId) => {
  const response = await api.get(`/reviews/helpers/${helperId}`)
  return response.data
}
```

### 3. Added Missing Backend Endpoint

Added `GET /api/reviews/sessions/{sessionId}` endpoint to `ReviewController.java`:

```java
@GetMapping("/sessions/{sessionId}")
public ResponseEntity<?> getReviewBySession(@PathVariable String sessionId) {
    try {
        Review review = reviewService.getReviewBySession(sessionId);
        return ResponseEntity.ok(review);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }
}
```

### 4. Added Helper Average Rating Endpoint

Added method to frontend service:
```javascript
getHelperAverageRating: async (helperId) => {
  const response = await api.get(`/reviews/helpers/${helperId}/rating`)
  return response.data
}
```

## Backend Endpoint Summary

### ReviewController Endpoints:
1. `POST /api/reviews/sessions/{sessionId}` - Submit a review
   - Body: `{ userId, helperId, rating, reviewText }`
   
2. `GET /api/reviews/helpers/{helperId}` - Get all reviews for a helper
   
3. `GET /api/reviews/helpers/{helperId}/rating` - Get average rating for a helper
   
4. `GET /api/reviews/sessions/{sessionId}` - Get review for a specific session

## Data Flow

1. User completes a session (status: ENDED)
2. User navigates to Submit Review page
3. Frontend loads session data via `GET /api/sessions/{sessionId}/log`
4. SessionResponse DTO includes:
   - `helperId` - Helper's ID
   - `helperName` - Helper's full name
   - `categoryId` - Category ID
   - `categoryName` - Category name
   - `status` - Session status
   - Other session details
5. User selects rating and writes review
6. Frontend calls `POST /api/reviews/sessions/{sessionId}` with:
   ```json
   {
     "userId": 123,
     "helperId": 456,
     "rating": 5,
     "reviewText": "Great helper!"
   }
   ```
7. Backend validates:
   - Session exists and is ENDED
   - No duplicate review for this session
   - Rating is between 1-5
   - User and Helper exist
8. Backend creates and saves Review entity
9. Frontend redirects to sessions list

## Testing Steps

1. Start backend: `./mvnw spring-boot:run` (ensure port 8080 is free)
2. Start frontend: `npm run dev` (in frontend directory)
3. Complete a session and end it
4. Navigate to the session and click "Review"
5. Select a rating (1-5 stars)
6. Optionally add review text
7. Click "Submit Review"
8. Verify success message and redirect to sessions list

## Files Modified

1. `frontend/src/services/reviewService.js` - Fixed API endpoints
2. `src/main/java/com/fixit/fixit/controller/ReviewController.java` - Added missing endpoint

## Related Components

- **Entity**: `Review.java` - Maps to reviews table
- **DTO**: `SubmitReviewRequest.java` - Request validation
- **DTO**: `SessionResponse.java` - Includes helperId in response
- **Service**: `ReviewService.java` - Business logic for reviews
- **Repository**: `ReviewRepository.java` - Data access for reviews
- **Frontend**: `SubmitReview.jsx` - Review submission UI

## Notes

- The backend was already correctly implemented
- The issue was purely a frontend API endpoint mismatch
- No database changes required
- No entity changes required
- The fix maintains RESTful API conventions (resource ID in path, data in body)
