# User Guide

**Mercury Messenger Portal - Field Messenger Operations Manual**

---

## Table of Contents

1. [Welcome](#welcome)
2. [Getting Started](#getting-started)
3. [Home Screen](#home-screen)
4. [Clocking In](#clocking-in)
5. [Today's Jobs](#todays-jobs)
6. [Completing a Job](#completing-a-job)
7. [Delaying a Job](#delaying-a-job)
8. [Departing to Next Job](#departing-to-next-job)
9. [Taking Photos](#taking-photos)
10. [Clocking Out](#clocking-out)
11. [Tips & Tricks](#tips--tricks)
12. [Troubleshooting](#troubleshooting)
13. [FAQ](#faq)

---

## Welcome

Mercury Messenger Portal is your mobile companion for managing delivery jobs, tracking locations, and completing deliveries efficiently. This guide walks you through every feature step-by-step.

### What You'll Do

- **Log in** with your employee credentials
- **Clock in** when you start your workday
- **View today's jobs** in sequence order
- **Travel to jobs** and update status
- **Take photos** of deliveries
- **Complete jobs** with GPS verification
- **Clock out** when you finish work

### Required Permissions

The app needs permission to:
- **GPS/Location**: Track your exact delivery location
- **Camera**: Take photos of packages/recipients
- **Storage**: Save photos temporarily

You'll be asked to grant these when needed.

---

## Getting Started

### First-Time Setup

#### Step 1: Install the App

1. Open Google Play Store
2. Search for "Mercury Messenger Portal"
3. Tap **Install**
4. Wait for installation (about 50MB)

#### Step 2: Launch the App

Tap **Open** to start Mercury Portal for the first time.

#### Step 3: See Login Screen

You'll see a login form with two fields:
- **Employee ID**: Your unique ID (example: `EMP001`)
- **Password**: Your password

### Login

```
Employee ID:  [EMP001      ]
Password:     [••••••••     ]
              [Login button]
```

**To log in:**
1. Enter your **Employee ID** (ask supervisor if unsure)
2. Enter your **Password**
3. Tap **Login**

**If login fails:**
- Check spelling of Employee ID
- Verify password (case-sensitive)
- Ensure internet connection is working
- Contact your supervisor

---

## Home Screen

After successful login, you'll see the **Home** screen.

### Home Screen Layout

```
┌─────────────────────────────┐
│  Mercury Messenger Portal   │
├─────────────────────────────┤
│                             │
│  👤 Luffy D. Monkey         │  ← Your name
│  📅 Monday, Apr 13, 2026    │  ← Today's date
│                             │
│  ┌───────────────────────┐  │
│  │ Clock In Status       │  │  ← Your work day status
│  │ Time: 07:30 AM       │  │
│  │ Location: Bangkok    │  │
│  └───────────────────────┘  │
│                             │
│  ┌─────────┐  ┌─────────┐   │
│  │View Jobs│  │Day Close│   │  ← Action buttons
│  └─────────┘  └─────────┘   │
│                             │
└─────────────────────────────┘
```

### What It Shows

- **Your Name** and today's date
- **Clock In Status**: Shows if you've clocked in
  - `Not Clocked In` = Clock In button available
  - `Clocked In at 07:30` = Clock Out button available
- **Two Main Buttons**:
  - **View Jobs** → See all jobs for today
  - **Day Close** → End your work day

### Clock In Button

When you start work:

1. Tap **Clock In** button (or green **Clock In** button)
2. App captures your GPS location
3. You see "Clocked In at [time]"
4. System records your start time

**What happens:**
- Your location is saved to company records
- Your work day officially starts
- Jobs become available to view

### Day Completed Status

After you've clocked out:

```
┌───────────────────────────┐
│ ✓ Day Completed          │
│ Clock In:  07:30 AM       │
│ Clock Out: 17:45 PM       │
└───────────────────────────┘
```

Shows your complete work day with start and end times.

---

## Clocking In

### Start Your Work Day

**Location:** Home screen

**Steps:**

1. Tap green **Clock In** button
2. App requests GPS permission (if first time)
   - Tap **Allow** to permit location access
3. App captures your current location
4. Status changes to "Clocked In at [time]"

### Why Clock In?

- Records official start time
- Enables you to view assigned jobs
- Proves you were available for work
- Used for payroll verification

### GPS Accuracy

The app uses your phone's GPS to pinpoint your location. For best accuracy:
- Use outdoors when possible
- Wait 5-10 seconds for GPS to lock
- Move away from tall buildings
- Ensure Location Services are enabled in phone Settings

### What Gets Recorded

When you clock in, the system saves:
- ✓ Date and time
- ✓ Your GPS coordinates
- ✓ Street address
- ✓ Your messenger ID

---

## Today's Jobs

### Viewing Your Jobs

**Location:** Tap **View Jobs** button from Home screen

You'll see **Today's Jobs** list with three sections:

```
┌─────────────────────────────────┐
│  TODAY'S JOBS                   │
├─────────────────────────────────┤
│                                 │
│ ACTIVE JOBS                     │  ← Jobs to complete
│ ┌──────────────────────────┐    │
│ │ 1. Delivery to John      │    │
│ │    📍 Silom, Bangkok     │    │
│ │    ⚡ DEPARTED           │    │
│ └──────────────────────────┘    │
│                                 │
│ COMPLETED JOBS                  │  ← Finished jobs
│ ┌──────────────────────────┐    │
│ │ 3. Delivery to Jane      │    │
│ │    ✓ COMPLETED           │    │
│ └──────────────────────────┘    │
│                                 │
│ DELAYED JOBS                    │  ← Jobs postponed
│ ┌──────────────────────────┐    │
│ │ 2. Pickup from Office    │    │
│ │    ⏸ DELAYED (Traffic)   │    │
│ └──────────────────────────┘    │
│                                 │
└─────────────────────────────────┘
```

### Job Status Meanings

| Status | Icon | Meaning |
|--------|------|---------|
| **ASSIGNED** | 📋 | Just assigned, ready to depart |
| **DEPARTED** | 🚗 | You're traveling to this job |
| **ARRIVED** | 📍 | You're at the location |
| **COMPLETED** | ✓ | Job finished successfully |
| **DELAYED** | ⏸ | Job postponed, reason recorded |

### Job Card Information

Each job shows:

```
┌─────────────────────────────────┐
│ 1  Delivery to Khun Somchai    │  ← Job sequence & title
│    📍 Silom, Bangkok 10110     │  ← Location
│    ⚡ DEPARTED                 │  ← Current status
│    Sender: Company A           │  ← From whom
│    Receiver: Khun Somchai      │  ← To whom
│    Note: Signature required    │  ← Special instructions
└─────────────────────────────────┘
```

### Reorder Jobs (Optional)

You can customize the order:

1. **Long-press** any active job (for 1 second)
2. **Drag upward/downward** to reorder
3. Release to save new order

**Note:** You can only reorder active jobs. Completed and delayed jobs stay in their original positions.

### Sorting

Jobs are automatically sorted by:
1. **Active jobs first** (in your chosen order)
2. **Completed jobs** (at bottom)
3. **Delayed jobs** (at very bottom)

---

## Completing a Job

### Job Detail Screen

When you arrive at a job location, tap the job card to open **Job Detail** screen.

```
┌──────────────────────────────┐
│ ← Job #1 PICKUP               │
├──────────────────────────────┤
│                              │
│ STATUS: DEPARTED             │  ← Current status
│                              │
│ FROM: Company A              │  ← Sender info
│ Phone: +668-1234-5678        │
│                              │
│ TO: Khun Somchai             │  ← Receiver info
│ Phone: +668-8765-4321        │
│                              │
│ 📍 Silom, Bangkok            │  ← Location & map link
│ 🗺 [Open in Maps]            │
│                              │
│ NOTE: Signature required     │  ← Special instructions
│                              │
│ [Complete] [Delay]           │  ← Action buttons
│                              │
└──────────────────────────────┘
```

### Complete Workflow (Without Photo)

Some jobs don't require photos. Here's the simple workflow:

**Step 1: Tap Complete Button**
- Button shows `Complete` when you're ready

**Step 2: Choose Arrival Location**
- A dialog appears with two options:
  ```
  Where did you complete this delivery?
  
  ⚪ Current GPS Location
     (Uses your phone's GPS right now)
  
  ⚪ Job Destination
     (Use the job's original location)
  ```
- Usually choose **Current GPS Location**
- Tap **Confirm**

**Step 3: Job Marked Complete**
- Status changes to ✓ **COMPLETED**
- Location and timestamp are saved
- A green **"Depart Next Job"** button appears

**Step 4: (Optional) Depart to Next Job**
- Tap **Depart Next Job** to go to next delivery
- You'll be taken to the next job's detail screen
- Its status automatically changes to DEPARTED

### Complete Workflow (With Photo)

Some jobs require proof via photo:

**Step 1: Job Requires Photo**
- When you tap **Complete**, instead of location picker, you go to camera

**Step 2: Camera Screen Opens**
```
┌────────────────────┐
│   📷 CAMERA       │
├────────────────────┤
│                    │
│  [Live camera view]│
│                    │
│  🎥 Capture Photo  │  ← Take picture
│  Skip ❌           │  ← Skip if unable
│                    │
└────────────────────┘
```

**Step 3: Take Photo**
- Aim camera at package/delivery scene
- Tap 🎥 **Capture Photo** button
- Photo is taken and shown
- Tap ✓ **Confirm** or ↻ **Retake**

**Step 4: Choose Location**
- After confirming photo, choose arrival location (same as non-photo flow)
- Select GPS or Job Destination
- Tap **Confirm**

**Step 5: Complete**
- Job marked ✓ **COMPLETED**
- Photo URL stored
- Location and timestamp saved

### Why Photos?

Photos provide proof of delivery:
- Confirms you were at correct location
- Shows condition of package
- Protects you and the company
- Resolves disputes about delivery

---

## Delaying a Job

If you can't complete a job right now, you can delay it.

### When to Delay

- ❌ Recipient not available
- ❌ Address not found
- ❌ Traffic/vehicle breakdown
- ❌ Safety concerns
- ❌ Incorrect package contents

### How to Delay

**Step 1: Open Job Detail**
- Tap the job you want to delay

**Step 2: Tap Red "Delay" Button**
- Located in the Contacts section (right side)
- Button shows 🔴 **Delay**

**Step 3: Delay Dialog Appears**
```
┌────────────────────────────────┐
│ DELAY THIS JOB                 │
│                                │
│ Reason (required):             │
│ [                          ]   │
│ [Type reason here...]      ]   │
│                                │
│       [Cancel]  [Submit]       │
│                                │
└────────────────────────────────┘
```

**Step 4: Enter Reason**
- Explain why you're delaying
- Examples:
  - "Recipient at meeting, return 14:00"
  - "Address not found, calling dispatcher"
  - "Vehicle breakdown, waiting for mechanic"
  - "Road closed due to accident"

**Step 5: Tap Submit**
- Reason is saved
- Job status changes to ⏸ **DELAYED**
- Reason shown in your remarks

### After Delaying

- Job moves to **DELAYED** section (bottom of list)
- You can tap "Depart Next Job" to go to next job
- Dispatcher is notified of delay
- You'll be reassigned to job later (or manually updated)

---

## Departing to Next Job

After completing or delaying a job, you can quickly move to the next one.

### When Button Appears

The green **"Depart Next Job"** button appears:
- ✓ Right after you **complete** a job
- ✓ Right after you **delay** a job
- ✗ NOT when viewing already-completed jobs (from list)

```
┌──────────────────────────────┐
│ ✓ COMPLETED (just now)       │
│                              │
│ ┌──────────────────────────┐ │
│ │ Depart Next Job          │ │  ← Green button
│ └──────────────────────────┘ │
│                              │
└──────────────────────────────┘
```

### How to Use

**Step 1: Tap "Depart Next Job"**
- Button appears after completing/delaying current job

**Step 2: App Finds Next Job**
- System looks for your next assigned job
- Captures your GPS location
- Updates next job status to DEPARTED

**Step 3: Navigates Automatically**
- You're taken to the next job's detail screen
- Status shows DEPARTED
- You can see location, contacts, notes

**Step 4: Continue**
- Travel to new location
- Repeat completion/delay process

### If No Next Job

If there's no job after current one:
```
Error: "No next job available"
```

Means:
- All jobs completed/delayed
- New jobs not yet assigned
- Go to **Day Closing** to clock out

---

## Taking Photos

### When Photos Are Required

Some jobs must have proof photos:
- Delivery to VIP clients
- High-value packages
- Special handling required
- Disputed deliveries

### Camera Screen

```
┌──────────────────────────┐
│     CAPTURE PHOTO        │
├──────────────────────────┤
│                          │
│   [Live camera stream]   │
│   [showing your phone's  │
│    camera view]          │
│                          │
│        🎥 Capture        │  ← Take photo
│        Skip              │  ← Can skip if needed
│                          │
│  Job: #1 Delivery        │
│  Location: Silom, BKK    │
│                          │
└──────────────────────────┘
```

### Take a Photo

**Step 1: Position Camera**
- Hold phone steady
- Frame the package/location in view
- Ensure good lighting

**Step 2: Tap 🎥 Capture**
- Photo is taken
- You see preview

**Step 3: Review Photo**
- Is it clear? Is it in focus?
- Tap ✓ **Confirm** to use this photo
- Tap ↻ **Retake** to try again

**Step 4: After Photo**
- Location picker appears
- Choose GPS or Job Destination
- Complete the job

### Photo Tips

✓ **Good photos:**
- Clear, well-lit images
- Package visible with identifying marks
- Recipient visible (optional but helpful)
- Address/location visible

✗ **Bad photos:**
- Blurry or out of focus
- Too dark to see details
- Person's face clearly visible (privacy)
- Unrelated images

### If You Can't Take Photo

If camera fails or you forget:
1. Tap **Skip** button
2. Job continues without photo
3. Reason logged in system
4. Dispatcher notified

---

## Clocking Out

### End Your Work Day

When all jobs are done (or close to done):

**Location:** Tap **Day Close** button from Home screen

### Day Closing Screen

```
┌──────────────────────────────┐
│    TODAY'S JOB SUMMARY       │
├──────────────────────────────┤
│                              │
│ Total Jobs:      5           │
│ Completed:       4           │
│ Pending:         0           │
│ Delayed:         1           │
│                              │
│ Add Reason For Pending Jobs  │
│ ┌──────────────────────────┐ │
│ │ Job #2: Traffic          │ │
│ │ Reason: Heavy traffic... │ │
│ └──────────────────────────┘ │
│                              │
│ [Confirm Day Close & Clock Out] │
│      (Yellow, then Green)      │
│                              │
└──────────────────────────────┘
```

### If Jobs Still Pending

If you have jobs not completed/delayed:

**Step 1: Add Reason for Each**
- Tap pending job
- Enter reason why not done
- Examples:
  - "Customer requested tomorrow"
  - "Waiting for parts arrival"
  - "Will follow up in morning"

**Step 2: Complete Each Job's Reason**
- Button becomes **yellow** when all reasons filled
- After last reason, dialog closes
- You return to Day Summary

### Clock Out Button Becomes Active

After all jobs handled:
```
Button changes:
Yellow  → "Confirm Day Close & Clock Out"
Green   → Tap to proceed
```

### Clock Out Process

**Step 1: Tap Green Clock Out Button**

**Step 2: Choose Location**
```
Where did you end your work day?

⚪ Current GPS Location
   (Use your current position)

⚪ HQ/Base
   (Use headquarters location)
```
- Usually choose **Current GPS Location**
- Tap **Confirm**

**Step 3: Processing**
- App records your exit time
- Sends data to company
- Saves your final location

**Step 4: Clock Out Complete**
```
Status changes to:
✓ Day Completed
  Clock In:  07:30 AM
  Clock Out: 17:45 PM
```

### After Clock Out

- All jobs locked (no further changes)
- Your work day officially ended
- You stay logged in (no auto-logout)
- Tomorrow you can log in and start fresh
- Next morning, all jobs reset to ASSIGNED

---

## Tips & Tricks

### Optimize Your Route

Before starting, look at all jobs:
1. Open **View Jobs**
2. See all locations in sequence
3. Plan your route mentally
4. Reorder jobs if needed (long-press and drag)
5. This saves time and fuel

### Save GPS Battery

GPS drains battery. To conserve:
- Clock in/out near the actual location (not ahead of time)
- Don't keep app open when not working
- Close other apps using GPS
- Reduce screen brightness
- Use battery saver mode at 20%

### Handle Network Issues

If internet drops:
- App works offline (uses cached data)
- Jobs still visible
- Can still update status locally
- Changes sync when connection returns
- Don't force-close the app

### Manage Your Time

Start early to avoid rush:
- Morning traffic is lighter
- Customers more available
- Fewer delays
- Better completion rate
- Less stress

### Check Job Notes

Always read the note:
- May have special instructions
- Signature requirements
- Contact information
- Time windows
- Access codes or floor numbers

### Keep Phone Charged

Your phone is your work tool:
- Charge overnight fully
- Bring charger in vehicle
- Use low-power mode if needed
- Don't let battery get < 10%
- Critical if system crashes mid-day

---

## Troubleshooting

### Login Failed

**Problem:** Can't log in
```
"Invalid credentials" error
```

**Solutions:**
1. Check Employee ID spelling
2. Verify Password (case-sensitive)
3. Ensure internet connection
   - Mobile data on or WiFi connected
   - Try WiFi if mobile data slow
4. Contact supervisor for ID/password reset

---

### GPS Not Working

**Problem:** Location not captured, map shows wrong location
```
Error: "Unable to get location"
or
Location is far from actual position
```

**Solutions:**
1. Check Location Services
   - Settings → Location → ON
2. Check app permissions
   - Settings → Apps → Mercury Portal
   - Location → Always / Allow all the time
3. Move to open area (away from buildings)
4. Wait 10-15 seconds for GPS lock
5. Restart phone
6. Contact support if persists

---

### Camera Not Working

**Problem:** Can't take photos
```
Error: "Camera permission denied"
or
Black screen in camera view
```

**Solutions:**
1. Grant camera permission
   - When prompted, tap Allow
   - Or Settings → Apps → Mercury Portal → Camera → Allow
2. Check if camera is working
   - Open native Camera app
   - If also fails, hardware issue
3. Restart app
4. Clear app cache
   - Settings → Apps → Mercury Portal → Storage → Clear Cache
5. Skip photo if temporary issue
6. Contact IT support

---

### App Keeps Crashing

**Problem:** App closes unexpectedly
```
"Mercury Portal has stopped"
or
App suddenly closes
```

**Solutions:**
1. **Force Stop & Restart**
   - Settings → Apps → Mercury Portal
   - Force Stop
   - Reopen app

2. **Clear Cache**
   - Settings → Apps → Mercury Portal
   - Storage → Clear Cache (NOT Clear Data)

3. **Restart Phone**
   - Hold power button → Restart
   - Wait for full restart

4. **Check Free Storage**
   - Settings → Storage
   - Need at least 500MB free
   - Delete unused apps/photos if needed

5. **Uninstall & Reinstall**
   - Last resort
   - Note down all pending work
   - Play Store → Mercury Portal → Uninstall
   - Reinstall
   - Log back in

---

### Data Lost or Wrong

**Problem:** Job completed but now shows as incomplete
```
Status changed back to ASSIGNED
or
Photo disappeared
```

**Solutions:**
1. **Poor Network:** Upload might have failed
   - Go back to job
   - Try completing again
   - Ensure full internet connection

2. **Time Sync:** Server time differs from phone
   - Settings → Date & Time → Automatic
   - Turn off and on again

3. **App Issue:** Rare database issue
   - Clear app cache (not data)
   - Restart app
   - Contact support with job ID

---

### Can't Find Job Location

**Problem:** Address in app doesn't match actual location
```
Google Maps shows different place
or
Directions are wrong
```

**Solutions:**
1. **Check Address Again**
   - Verify against delivery paperwork
   - Use customer phone to confirm

2. **Use Maps App**
   - Tap 🗺 "Open in Maps" button
   - Verify location matches
   - Get better directions

3. **Call Customer**
   - Use phone number in job details
   - Ask for landmarks
   - Ask for access code

4. **Verify Job Details**
   - Go back to job list
   - Double-check you have correct job
   - Sequence number matches your plan

---

### Connection Lost Mid-Job

**Problem:** Internet drops while completing job
```
No network connectivity
or
API timeout error
```

**Solutions:**
1. **Stay Calm** - App handles offline mode
   - Status update saved locally
   - Will sync when connected

2. **Regain Connection**
   - Move to area with better signal
   - Switch to WiFi if available
   - Wait a minute for retry

3. **If Status Saved Locally**
   - Job shows as updated
   - Sync happens automatically
   - Don't complete again

4. **Manual Retry**
   - If change doesn't sync after 30 min
   - Go to job and try updating again
   - Contact support if failed

---

## FAQ

### Q: Can I work offline?

**A:** Partially. The app works offline for:
- Viewing jobs (if loaded before)
- Updating job status locally
- Taking photos

But to actually submit changes, you need internet connection:
- Clock in/out requires network
- Photo upload requires network
- Status updates sync when connected

**Recommendation:** Ensure connection before key operations.

---

### Q: How long do photos store?

**A:** Photos are:
- Uploaded immediately to company servers
- Kept indefinitely for records
- Deleted from your phone after upload
- Accessible from company system

Your phone doesn't keep photos after upload (saves storage).

---

### Q: Can I undo a completed job?

**A:** No. Once completed:
- Status locked
- Can't be reverted
- Contact dispatcher/supervisor if mistake

**Prevention:** Always review before confirming!

---

### Q: What if I complete wrong job?

**A:** You can't undo, but:
1. Contact supervisor immediately
2. Provide job ID and issue
3. Mark correct job as completed manually
4. Supervisor adjusts in system

**Always verify job details before completing!**

---

### Q: Can I work for multiple days?

**A:** Not yet. Currently:
- Each day is separate session
- Clock in/out each day
- Jobs reset daily
- Multi-day jobs assigned fresh each day

**Future version** may support multi-day jobs.

---

### Q: How are jobs assigned?

**A:** Jobs assigned by:
- **Dispatcher** in morning
- Based on your location
- Your current jobs
- Geographic zones

You can't request specific jobs, but can:
- Delay if impossible
- Reorder if sequence allows
- Contact dispatcher if conflict

---

### Q: What if I'm delayed?

**A:** Always:
1. Tap job → Tap **Delay**
2. Enter reason (required)
3. Tap Submit

This notifies dispatcher:
- They know you're delayed
- Can reassign if needed
- Explains delay to customer

**Don't just skip jobs!**

---

### Q: Can I reassign a job?

**A:** For delivery reassignment:
1. Open job
2. Look for **Reassign** button (may need scroll)
3. Enter reason
4. Tap Submit

Sends request to dispatcher to reassign to someone else.

**Use for:** Can't complete, wrong address, customer request, etc.

---

### Q: How long to complete a job?

**A:** Depends on job type:
- Simple dropoff: 2-5 minutes
- With photo: 5-10 minutes
- Requires signature: 5-10 minutes
- Complex delivery: 10-20 minutes

**Tip:** Batch nearby jobs to reduce travel time.

---

### Q: What if phone dies?

**A:**
- **Before clock in:** Re-login when charged
- **After clock in:** You're still clocked in (until clock out)
- **During job:** Job timer pauses

**On restart:**
- If within same day: Status preserved
- If next day: Clock out auto-reverted
- Contact support if issues

**Prevention:** Keep phone charged!

---

### Q: Can I work from home?

**A:** You can:
- Clock in from anywhere (including home)
- App accepts any GPS location
- Not restricted to office/zone

But:
- Expected to be mobile (not same spot all day)
- Completion location must match job location
- Suspicious pattern may be flagged

---

### Q: How do I take time off?

**A:** Through your supervisor/HR:
- Inform supervisor in advance
- They remove jobs from your assignment
- App won't show any jobs that day
- Or clock in, then clock out immediately (no jobs)

**Don't just skip app.**

---

### Q: What about breaks?

**A:** Breaks handled outside app:
- App stays running if you want
- Or close and reopen when done
- No penalty for break time
- Status time locks when job completed

**Tip:** Close app during breaks to save battery.

---

### Q: Is my location private?

**A:** Your location is:
- ✓ Recorded by company
- ✓ Used for delivery verification
- ✓ Used for payroll tracking
- ✗ NOT shared publicly
- ✗ NOT sold to third parties

Your privacy is protected. Company only uses for work purposes.

---

### Q: Can I delete my account?

**A:** Account managed by company:
- Contact HR/Admin for deletion request
- Can disable app anytime
- If dismissed, account deactivated by company
- Data retained per company policy

---

## Support

### Need Help?

Contact your **Supervisor** or **Dispatcher**:
- In-app issues (crashes, permissions)
- Job-related questions (address, instructions)
- Account problems (login, password)

### Technical Issues Only

For app crashes and bugs:
- Note error message exactly
- Provide job ID if applicable
- Contact IT Support with details

---

**Document Version**: 1.0  
**Last Updated**: 2026-04-13  
**Status**: Ready for Field Messenger Training
