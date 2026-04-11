# Video Session Bugs Analysis

## Bug 1: Session Status Not Updating (INITIATED → CONNECTED)
**Issue**: User's session list doesn't show real-time status updates when helper accepts.

**Fix Applied**:
1. Added WebSocket subscription to User Sessions page
2. Backend now sends notification to `/topic/user/{userId}/session-updates` when session is accepted
3. Frontend updates session status in real-time without page refresh

## Bug 2: Second User Session Fails
**Scenario**: 
- User 1 completes session with Helper
- User 1 logs out
- User 2 registers and logs in
- User 2 starts session with same Helper
- **Problem occurs here**

**Root Causes Identified**:

### 1. WebSocket Subscription Leaks
- Helper's browser maintains WebSocket subscriptions from previous session
- When new session starts, helper receives messages for BOTH old and new sessions
- This causes confusion in WebRTC signaling (offers/answers go to wrong session)

### 2. Peer Connection Not Fully Cleaned
- `peerConnectionRef.current` might not be properly closed between sessions
- Old ICE candidates might interfere with new connection

### 3. Session ID in Cleanup Closure
- `cleanupConnections()` uses `sessionId` from closure
- When component unmounts and remounts with new sessionId, cleanup might reference wrong session

### 4. Local Media Stream Reuse
- `localStreamRef.current` might not be properly stopped between sessions
- Camera/mic might be "busy" when trying to initialize for new session

## Fixes to Apply:

### Fix 1: Proper WebSocket Unsubscription
```javascript
// Store subscription reference
const subscriptionRef = useRef(null)

// When subscribing
subscriptionRef.current = client.subscribe(...)

// When cleaning up
if (subscriptionRef.current) {
  subscriptionRef.current.unsubscribe()
  subscriptionRef.current = null
}
```

### Fix 2: Force Disconnect WebSocket on Cleanup
```javascript
if (stompClientRef.current) {
  stompClientRef.current.deactivate()
  stompClientRef.current = null
}
```

### Fix 3: Ensure Media Tracks Are Stopped
```javascript
if (localStreamRef.current) {
  localStreamRef.current.getTracks().forEach(track => {
    track.stop()
    console.log('Stopped track:', track.kind, track.id)
  })
  localStreamRef.current = null
}
```

### Fix 4: Clear Video Elements
```javascript
if (localVideoRef.current) {
  localVideoRef.current.srcObject = null
  localVideoRef.current.load() // Force reload
}
if (remoteVideoRef.current) {
  remoteVideoRef.current.srcObject = null
  remoteVideoRef.current.load()
}
```

### Fix 5: Add Session ID to Cleanup
```javascript
const cleanupConnections = (currentSessionId) => {
  // Use passed sessionId instead of closure
  if (stompClientRef.current && stompClientRef.current.connected) {
    stompClientRef.current.publish({
      destination: `/app/session/${currentSessionId}/leave`,
      body: JSON.stringify({ userId: user.id })
    })
  }
  // ... rest of cleanup
}

// Call with current sessionId
return () => {
  cleanupConnections(sessionId)
}
```

## Testing Steps:

1. User 1 logs in, starts session with Helper
2. Both see video feeds
3. End session
4. User 1 logs out
5. User 2 registers and logs in
6. User 2 starts session with same Helper
7. **Expected**: Both see video feeds immediately
8. **Check**: No console errors about duplicate subscriptions or peer connection failures

## Additional Checks:

- Browser console should show "Cleaning up connections..." when leaving session
- Should see "WebSocket connected" when entering new session
- No "ICE connection failed" errors
- No "Peer connection already exists" warnings
