# Video Session Testing Checklist

## Quick Test: User Switching Issue

### Setup
- Backend running on port 8080 ✓
- Frontend running on port 3000 ✓
- Two user accounts created
- One helper account created

### Test Steps

#### 1. First User Session
- [ ] Login as User1
- [ ] Navigate to "Find Helpers"
- [ ] Select Helper1
- [ ] Start session
- [ ] **Verify**: Your video shows (camera light on)
- [ ] **Verify**: Remote video shows helper's video
- [ ] **Verify**: Both videos are working
- [ ] End session or logout

#### 2. Second User Session (THE FIX)
- [ ] Login as User2 (different account)
- [ ] Navigate to "Find Helpers"
- [ ] Select Helper1 (SAME helper)
- [ ] Start session
- [ ] **Verify**: Your video shows (no black screen)
- [ ] **Verify**: Remote video shows helper's video (no black screen)
- [ ] **Verify**: Both videos are working properly
- [ ] **Verify**: No "device in use" errors

#### 3. Check Browser Console
Open DevTools (F12) and verify these messages appear:

**On Logout:**
```
Cleaning up connections...
Stopped track: video
Stopped track: audio
Cleanup complete
Media resources cleaned up on logout
```

**On New Session:**
```
Local media initialized with 2 tracks
WebSocket connected for user: [userId]
```

**On Video Connection:**
```
=== Creating offer ===
Offer created and set as local description
Offer sent via WebSocket
Connection state: connected
```

#### 4. Verify Camera Release
- [ ] After logout, camera light should turn OFF
- [ ] No camera indicator in browser tab
- [ ] Camera available for other applications

## Advanced Tests

### Test A: Rapid User Switching
1. Login User1 → Start session → Logout
2. Login User2 → Start session → Logout
3. Login User3 → Start session
4. **Expected**: All sessions work without video issues

### Test B: Multiple Sessions Same User
1. Login as User1
2. Start session with Helper1 → End session
3. Start session with Helper1 again
4. **Expected**: Second session works properly

### Test C: Browser Refresh
1. Login as User
2. Start session with Helper
3. Refresh browser (F5)
4. Navigate back to session
5. **Expected**: Video reconnects or shows proper error

### Test D: Tab Close
1. Login as User
2. Start session with Helper
3. Close tab (X button)
4. Open new tab → Login
5. Start new session
6. **Expected**: No device conflicts

## Common Issues & Solutions

### Issue: Black screen on remote video
**Check:**
- Browser console for errors
- WebRTC connection state
- ICE connection state
- Network connectivity

**Solution:**
- Refresh page
- Check STUN server accessibility
- Verify WebSocket connection

### Issue: "Device in use" error
**Check:**
- Other tabs using camera
- Other applications using camera
- Previous session not cleaned up

**Solution:**
- Close all browser tabs
- Restart browser
- Check Task Manager for node processes

### Issue: No video after logout/login
**Check:**
- Console shows cleanup messages
- Camera light turns off on logout
- No errors in console

**Solution:**
- Clear browser cache
- Restart frontend server
- Check AuthContext cleanup function

## Browser DevTools Checks

### 1. Console Tab
Look for:
- ✅ Cleanup messages
- ✅ WebSocket connection logs
- ✅ WebRTC state changes
- ❌ No errors or warnings

### 2. Network Tab
Check:
- WebSocket connection to `/ws`
- Status: 101 Switching Protocols
- Messages being sent/received

### 3. Application Tab
Check localStorage:
- `token` - Should exist when logged in
- `user` - Should exist when logged in
- Both should be cleared on logout

### 4. Chrome WebRTC Internals
Open: `chrome://webrtc-internals/`
- Check active peer connections
- Verify old connections are closed
- Monitor ICE candidates
- Check connection stats

## Performance Checks

### Memory Usage
1. Open DevTools → Performance → Memory
2. Take heap snapshot before session
3. Start and end session
4. Take heap snapshot after
5. **Expected**: No significant memory increase

### Media Tracks
Run in console:
```javascript
navigator.mediaDevices.enumerateDevices().then(devices => {
  console.log(devices.filter(d => d.kind.includes('video')))
})
```
**Expected**: Devices not showing "in use" when not in session

## Success Criteria

✅ **All tests pass if:**
1. Video works for first user
2. Video works for second user with same helper
3. No black screens
4. Camera light turns off on logout
5. No "device in use" errors
6. Console shows proper cleanup messages
7. No memory leaks
8. WebRTC connections close properly

## Failure Scenarios

❌ **Test fails if:**
1. Black screen on remote video
2. Camera stays on after logout
3. "Device in use" error on second login
4. Console shows errors
5. WebRTC connection stuck in "connecting"
6. Memory usage keeps increasing

## Quick Debug Commands

### Check active media streams
```javascript
// Run in browser console
navigator.mediaDevices.getUserMedia({video: true, audio: true})
  .then(stream => {
    console.log('Active tracks:', stream.getTracks())
    stream.getTracks().forEach(t => t.stop())
  })
```

### Check video elements
```javascript
// Run in browser console
document.querySelectorAll('video').forEach(v => {
  console.log('Video element:', v.srcObject)
})
```

### Force cleanup
```javascript
// Run in browser console (emergency cleanup)
document.querySelectorAll('video').forEach(v => {
  if (v.srcObject) {
    v.srcObject.getTracks().forEach(t => t.stop())
    v.srcObject = null
  }
})
```

## Test Results Template

```
Date: ___________
Tester: ___________

Test 1 - First User Session: [ PASS / FAIL ]
Test 2 - Second User Session: [ PASS / FAIL ]
Test 3 - Console Messages: [ PASS / FAIL ]
Test 4 - Camera Release: [ PASS / FAIL ]

Advanced Test A: [ PASS / FAIL ]
Advanced Test B: [ PASS / FAIL ]
Advanced Test C: [ PASS / FAIL ]
Advanced Test D: [ PASS / FAIL ]

Notes:
_________________________________
_________________________________
_________________________________

Overall Result: [ PASS / FAIL ]
```

---

**Remember**: The main fix ensures that when you logout and login as a different user, all WebRTC resources (camera, microphone, peer connections, WebSocket) are properly cleaned up, allowing the new session to start fresh without conflicts.
