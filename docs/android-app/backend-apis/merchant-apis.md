# 4. Merchant APIs

**Category:** Core APIs (MVP Required)
**Priority:** 🟢 High (enables permissionless network growth)
**Related:** [RS046-3 Merchant Flows](android-app/flows/merchant-flows.md), [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md)

---

## Overview

Merchant APIs allow **any user** to list themselves as a merchant (provide cash to recipients). This is **permissionless** - anyone can create a merchant listing, just like posting an ad on Craigslist.

**Key insight:** There is no "merchant app" vs "user app". One app with two modes:
- 👤 **"Need Cash" mode** - I'm receiving a remittance
- 💵 **"Provide Cash" mode** - I'll hand cash to recipients for BCH

**MVP philosophy:**
- ✅ Self-service merchant listing (no approval needed)
- ✅ Any authenticated user can become a merchant
- ✅ Simple form: address, hours, transaction limit
- ✅ Network effect: More users = more potential merchants

---

## Permissionless Philosophy

**Why any user can be a merchant:**

**Removes bottlenecks:**
- ❌ No waiting for Suso to onboard merchants
- ❌ No approval queue
- ❌ No separate merchant verification
- ✅ Network grows organically

**Network effects:**
- More users → More potential merchants
- Users wear both hats (sometimes need cash, sometimes provide cash)
- Like Uber (drivers are also riders) or Airbnb (hosts are also guests)

**Trust through reputation:**
- Merchant ratings visible to recipients
- Transaction count shows experience
- Bad actors naturally filtered out (low ratings)

**For beta:**
- Start with 1-2 trusted users creating merchant listings
- Gradually invite more users
- Network density increases organically

---

## Instant Settlement Setting

> **⚠️ IMPORTANT:** `instant_settlement_enabled` is a **MERCHANT PROFILE SETTING**, not a per-transaction choice.
> - Merchant sets this **ONCE** in their profile
> - Applies to **ALL** future claims at that merchant
> - **Default: `false`** (keeps it simple - merchant receives BCH directly)
> - Can be toggled on/off anytime in merchant settings

---

**Merchants configure in their settings (not chosen at claim time):**

**`instant_settlement_enabled: false` (DEFAULT - RECOMMENDED FOR MVP):**
- Merchant receives BCH reward directly from escrow
- No LP involved
- Simple, straightforward flow
- Merchant earns full BCH reward
- Best for merchants who want to hold BCH
- **Default for new merchants to keep setup simple**

**`instant_settlement_enabled: true` (OPTIONAL - REQUIRES LP):**
- LP sends local fiat (VES, ARS, etc.) to merchant immediately when claim happens
- Merchant gets fiat in their bank account (~30 seconds)
- Merchant still gives cash to recipient
- LP receives BCH + reward from escrow
- Creates "bounty" for LPs (first-come-first-served competition)
- Best for merchants who need fiat liquidity, not BCH
- **Only enable when LP network is reliable in corridor**

**Key insights:**
- **One-time setting:** Configure once in merchant profile, applies to all claims
- **Not per-claim:** Merchant doesn't choose at claim time (too complex)
- **Default OFF:** Keeps onboarding simple for new merchants
- **Toggle anytime:** Merchant can enable/disable in settings based on needs
- **Related:** [Settlement APIs](android-app/backend-apis/settlement-apis.md) - How LP bounties work
- **Related:** [LP Flows](android-app/flows/lp-flows.md) - LP bounty system details

---

## API Endpoints Summary

**6 endpoints total:**

1. **GET /api/v1/merchants/nearby** - Find merchants near location (public)
2. **GET /api/v1/merchants/{id}** - Get merchant details (public)
3. **POST /api/v1/merchants/profile** - Create merchant listing (auth required)
4. **PUT /api/v1/merchants/profile** - Update merchant listing (auth required)
5. **DELETE /api/v1/merchants/profile** - Remove merchant listing (auth required)
6. **GET /api/v1/merchants/my-profile** - View own merchant profile (auth required)

---

### 1. GET /api/v1/merchants/nearby

**Purpose:** Find merchants near recipient's location (for map display)

**Authentication:** Optional (public endpoint, but better UX if authenticated)

**Request:**
```http
GET /api/v1/merchants/nearby?lat=10.4806&lng=-66.9036&radius=5000 HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <jwt_token>  # Optional
```

**Query parameters:**
- `lat`: Latitude (decimal degrees)
- `lng`: Longitude (decimal degrees)
- `radius`: Search radius in meters (default: 5000m = 5km, max: 50000m = 50km)
- `currency`: Optional filter (VES, ARS, HNL) - show only merchants accepting this currency

**Response:**
```json
{
  "merchants": [
    {
      "id": "merchant_9kLmP",
      "name": "Bodega El Roble",
      "location": {
        "lat": 10.4821,
        "lng": -66.9012,
        "address": "Av. Francisco de Miranda, Chacao, Caracas",
        "distance_meters": 450
      },
      "currencies_accepted": ["VES"],
      "rating": 4.8,
      "transaction_count": 127,
      "availability": {
        "status": "online",
        "last_seen": "2026-04-27T10:30:00Z"
      },
      "hours": {
        "monday": "08:00-20:00",
        "tuesday": "08:00-20:00",
        "wednesday": "08:00-20:00",
        "thursday": "08:00-20:00",
        "friday": "08:00-20:00",
        "saturday": "09:00-18:00",
        "sunday": "closed"
      },
      "instant_settlement_enabled": "false"
    },
    {
      "id": "merchant_2xNpQ",
      "name": "Panadería San José",
      "location": {
        "lat": 10.4798,
        "lng": -66.9045,
        "address": "Calle Paris, Los Palos Grandes, Caracas",
        "distance_meters": 680
      },
      "currencies_accepted": ["VES"],
      "rating": 4.9,
      "transaction_count": 203,
      "availability": {
        "status": "online",
        "last_seen": "2026-04-27T10:28:00Z"
      },
      "hours": {
        "monday": "06:00-19:00",
        "tuesday": "06:00-19:00",
        "wednesday": "06:00-19:00",
        "thursday": "06:00-19:00",
        "friday": "06:00-19:00",
        "saturday": "06:00-15:00",
        "sunday": "07:00-13:00"
      },
      "instant_settlement_enabled": true
    }
  ],
  "total_count": 2,
  "search_params": {
    "center": {
      "lat": 10.4806,
      "lng": -66.9036
    },
    "radius_meters": 5000
  }
}
```

**Response fields:**
- `merchants`: Array of nearby merchants
- `id`: Unique merchant ID
- `name`: Merchant business name
- `location.distance_meters`: Distance from search center
- `currencies_accepted`: Which currencies merchant can dispense
- `rating`: Average rating (1-5 stars)
- `transaction_count`: Total transactions completed
- `availability.status`: online/offline/busy
- `hours`: Operating hours (for display only)
- `instant_settlement_enabled`: true/false - Whether merchant wants instant fiat settlement via LP

**Use case:**
```
Recipient Elena opens app
  ↓
App gets device location: (10.4806, -66.9036)
  ↓
App calls: GET /api/v1/merchants/nearby?lat=10.4806&lng=-66.9036&radius=5000
  ↓
App shows map with 2 merchants nearby
  ↓
Elena sees: "Bodega El Roble - 450m away"
```

**Errors:**
- `INVALID_COORDINATES` (400): Lat/lng out of range
- `RADIUS_TOO_LARGE` (400): Radius > 50km
- `NO_MERCHANTS_FOUND` (404): No merchants in area

---

### 2. GET /api/v1/merchants/{merchant_id}

**Purpose:** Get detailed merchant information

**Authentication:** Optional

**Request:**
```http
GET /api/v1/merchants/merchant_9kLmP HTTP/1.1
Host: api.asgaya.com
```

**Response:**
```json
{
  "id": "merchant_9kLmP",
  "name": "Bodega El Roble",
  "description": "Family-owned corner store serving the community since 1998",
  "location": {
    "lat": 10.4821,
    "lng": -66.9012,
    "address": "Av. Francisco de Miranda, Chacao, Caracas",
    "neighborhood": "Chacao",
    "city": "Caracas",
    "country": "Venezuela"
  },
  "contact": {
    "phone": "+58412987654",
    "whatsapp": "+58412987654"
  },
  "currencies_accepted": ["VES"],
  "instant_settlement_enabled": "false",
  "rating": 4.8,
  "transaction_count": 127,
  "member_since": "2026-03-15T00:00:00Z",
  "availability": {
    "status": "online",
    "last_seen": "2026-04-27T10:30:00Z"
  },
  "hours": {
    "monday": "08:00-20:00",
    "tuesday": "08:00-20:00",
    "wednesday": "08:00-20:00",
    "thursday": "08:00-20:00",
    "friday": "08:00-20:00",
    "saturday": "09:00-18:00",
    "sunday": "closed"
  },
  "stats": {
    "total_transactions": 127,
    "total_volume_eur": 4850.00,
    "total_volume_ves": 301575.00,
    "average_transaction_eur": 38.19,
    "on_time_rate": 0.98
  },
  "reviews": [
    {
      "rating": 5,
      "comment": "Fast and friendly!",
      "timestamp": "2026-04-25T15:20:00Z"
    },
    {
      "rating": 5,
      "comment": "Always available, great service",
      "timestamp": "2026-04-20T11:10:00Z"
    }
  ]
}
```

**Response fields:**
- All fields from nearby search, plus:
- `description`: Merchant bio/description
- `stats`: Performance metrics
- `reviews`: Recent customer reviews (last 5)
- `on_time_rate`: % of transactions completed without issues

**Use case:**
```
Recipient taps merchant pin on map
  ↓
App calls: GET /api/v1/merchants/merchant_9kLmP
  ↓
App shows merchant detail screen:
- Name, address, distance
- Rating: 4.8 stars (127 transactions)
- Hours: Open now (closes 20:00)
- Reviews
- [Get Directions] [Call Merchant]
```

**Errors:**
- `NOT_FOUND` (404): Merchant ID doesn't exist

---

### 3. POST /api/v1/merchants/profile

**Purpose:** User creates their merchant listing (becomes a merchant)

**Authentication:** Required (JWT)

**Request:**
```http
POST /api/v1/merchants/profile HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "business_name": "Bodega El Roble",
  "description": "Family-owned corner store, happy to help with cash pickup",
  "location": {
    "lat": 10.4821,
    "lng": -66.9012,
    "address": "Av. Francisco de Miranda, Chacao, Caracas"
  },
  "contact": {
    "phone": "+58412987654",
    "whatsapp": "+58412987654"
  },
  "currencies_accepted": ["VES"],
  "instant_settlement_enabled": false,
  "bch_address": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
  "ves_pagomovil": "+58412987654",
  "hours": {
    "monday": "08:00-20:00",
    "tuesday": "08:00-20:00",
    "wednesday": "08:00-20:00",
    "thursday": "08:00-20:00",
    "friday": "08:00-20:00",
    "saturday": "09:00-18:00",
    "sunday": "closed"
  },
  "transaction_limit_eur": 50.00
}
```

**Request fields:**
- `business_name`: Name shown to recipients (can be just your name)
- `description`: Short bio (optional)
- `location`: Where recipients can find you
- `currencies_accepted`: ["VES"] for now (expandable: ["VES", "ARS", "HNL"])
- `instant_settlement_enabled`: true/false (see merchant settings)
- `bch_address`: Where to receive BCH payouts
- `ves_pagomovil`: Your PagoMóvil number for VES settlement
- `hours`: When you're available (optional, can be "flexible")
- `transaction_limit_eur`: Max amount per transaction (default: 50€)

**Response:**
```json
{
  "merchant_id": "merchant_9kLmP",
  "status": "active",
  "listed_at": "2026-04-27T11:15:00Z",
  "message": "You're now listed as a merchant! Recipients can find you on the map."
}
```

**Use case:**
```
User Jorge opens app → Taps "Provide Cash" tab
  ↓
Fills form:
- Business: "Bodega El Roble"
- Address: (auto-filled from GPS, editable)
- Hours: Mon-Sat 8am-8pm
- Max transaction: €50
- Settlement: Hold BCH
  ↓
Taps "List Me as Merchant"
  ↓
POST /api/v1/merchants/profile
  ↓
✅ "You're now listed! Recipients nearby can find you."
```

**Validations:**
- User must be phone-verified (can't list without verified account)
- `bch_address` must be valid BCH address
- `ves_pagomovil` must be valid Venezuelan phone
- `transaction_limit_eur` must be ≤ 100€ (spam prevention)
- User can only have ONE merchant profile (can't create multiple listings)

**Errors:**
- `PHONE_NOT_VERIFIED` (403): Must verify phone first
- `MERCHANT_ALREADY_EXISTS` (409): User already has a merchant profile
- `INVALID_BCH_ADDRESS` (400): BCH address format invalid
- `INVALID_PHONE` (400): PagoMóvil number format invalid
- `TRANSACTION_LIMIT_TOO_HIGH` (400): Limit > 100€

---

### 4. PUT /api/v1/merchants/profile

**Purpose:** User updates their merchant listing

**Authentication:** Required (JWT)

**Request:**
```http
PUT /api/v1/merchants/profile HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "hours": {
    "monday": "09:00-18:00",
    "tuesday": "09:00-18:00",
    "wednesday": "09:00-18:00",
    "thursday": "09:00-18:00",
    "friday": "09:00-18:00",
    "saturday": "10:00-14:00",
    "sunday": "closed"
  },
  "transaction_limit_eur": 75.00,
  "instant_settlement_enabled": "true"
}
```

**Partial updates allowed** - only send fields you want to change

**Response:**
```json
{
  "merchant_id": "merchant_9kLmP",
  "updated_at": "2026-04-27T12:30:00Z",
  "message": "Merchant profile updated successfully"
}
```

**Use case:**
```
Merchant Jorge changes hours for holiday weekend
  ↓
Opens "Provide Cash" tab → Taps "Edit Profile"
  ↓
Updates hours: Saturday 10am-2pm only
  ↓
Taps "Save"
  ↓
PUT /api/v1/merchants/profile
  ↓
✅ "Profile updated"
```

**Errors:**
- `MERCHANT_NOT_FOUND` (404): User has no merchant profile
- `INVALID_UPDATE` (400): Invalid field values

---

### 5. DELETE /api/v1/merchants/profile

**Purpose:** User removes their merchant listing (stop being a merchant)

**Authentication:** Required (JWT)

**Request:**
```http
DELETE /api/v1/merchants/profile HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "message": "Merchant profile removed. You can list yourself again anytime.",
  "removed_at": "2026-04-27T13:00:00Z"
}
```

**Use case:**
```
Merchant decides to stop providing cash service
  ↓
Opens "Provide Cash" tab → Taps "Remove My Listing"
  ↓
Confirms: "Are you sure?"
  ↓
DELETE /api/v1/merchants/profile
  ↓
✅ "Listing removed. You can re-list anytime."
```

**Important:**
- Merchant's historical transactions stay intact (for ratings/stats)
- Merchant disappears from map immediately
- Can re-create listing anytime (no penalties)

**Errors:**
- `MERCHANT_NOT_FOUND` (404): User has no merchant profile
- `ACTIVE_TRANSACTIONS` (409): Can't delete while active transactions pending

---

### 6. GET /api/v1/merchants/my-profile

**Purpose:** User views their own merchant profile

**Authentication:** Required (JWT)

**Request:**
```http
GET /api/v1/merchants/my-profile HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "merchant_id": "merchant_9kLmP",
  "business_name": "Bodega El Roble",
  "location": {
    "lat": 10.4821,
    "lng": -66.9012,
    "address": "Av. Francisco de Miranda, Chacao, Caracas"
  },
  "currencies_accepted": ["VES"],
  "instant_settlement_enabled": false,
  "bch_address": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
  "ves_pagomovil": "+58412987654",
  "hours": {
    "monday": "08:00-20:00",
    "tuesday": "08:00-20:00",
    "wednesday": "08:00-20:00",
    "thursday": "08:00-20:00",
    "friday": "08:00-20:00",
    "saturday": "09:00-18:00",
    "sunday": "closed"
  },
  "transaction_limit_eur": 50.00,
  "status": "active",
  "stats": {
    "total_transactions": 127,
    "rating": 4.8,
    "total_volume_eur": 4850.00,
    "member_since": "2026-03-15T00:00:00Z"
  }
}
```

**Use case:**
```
Merchant opens "Provide Cash" tab
  ↓
App calls: GET /api/v1/merchants/my-profile
  ↓
Shows:
- Your listing: "Bodega El Roble"
- Rating: 4.8 stars (127 transactions)
- Total earned: €4850 worth of BCH
- [Edit Profile] [Remove Listing]
```

**Errors:**
- `MERCHANT_NOT_FOUND` (404): User has no merchant profile (show "Create Listing" button)

---

## MVP Implementation Notes

### Backend Database Schema

**Merchant table (linked to users table):**
```python
class Merchant:
    id: str  # merchant_9kLmP
    user_id: str  # FK to users table (one-to-one relationship)
    business_name: str
    description: str  # Optional

    # Location
    lat: float
    lng: float
    address: str

    # Contact (from user's profile)
    phone: str  # From users.phone
    whatsapp: str  # Optional

    # Settings
    currencies_accepted: list[str]  # ["VES"]
    instant_settlement_enabled: bool  # true = want fiat via LP, false = want BCH directly
    bch_address: str  # For BCH payouts
    ves_pagomovil: str  # For VES settlement
    transaction_limit_eur: float  # Default 50, max 100

    # Hours (JSON)
    hours: dict  # {"monday": "08:00-20:00", ...}

    # Status
    status: str  # active, inactive, suspended
    availability: str  # online, offline, busy
    last_seen: datetime

    # Stats
    rating: float  # Calculated from transaction reviews
    transaction_count: int
    total_volume_eur: float

    created_at: datetime
    updated_at: datetime
```

**Key constraint:**
```sql
UNIQUE(user_id)  -- One merchant profile per user
```

### Self-Service Merchant Onboarding (MVP)

**Flow for first beta merchant:**

**Step 1: User creates account**
- Downloads app
- Verifies phone: +58412987654
- Now has authenticated user account

**Step 2: User creates merchant listing (via app)**
- Taps "Provide Cash" tab
- Fills form (business name, address, hours, etc.)
- Taps "List Me as Merchant"
- `POST /api/v1/merchants/profile` called
- ✅ Merchant listing created instantly

**Step 3: Test the flow**
- User switches to "Need Cash" mode
- Searches nearby merchants
- Sees their own listing on map
- ✅ End-to-end flow working

**Done!** Merchant is live. No manual database inserts needed.

---

## Permissionless but Safe

**How we prevent abuse without approval workflow:**

**Requirement: Phone verification**
- Must verify phone before creating merchant listing
- One phone = one account = one merchant listing
- Prevents spam/duplicate listings

**Transaction limits:**
- Max €50-100 per transaction (adjustable by merchant)
- Limits exposure for new/untrusted merchants
- Can increase limit after building reputation

**Reputation system:**
- Ratings from recipients visible to all users
- Low-rated merchants naturally avoided
- Can suspend merchants with <3.0 rating (post-MVP)

**For beta:**
- Start with 1-2 trusted users (Suso's contacts)
- They create merchant listings themselves (no manual insert)
- Gradually invite more users
- Trust emerges organically through ratings

---

## Merchant Availability Logic

**How "availability" is determined:**

**Backend updates merchant.last_seen when:**
- Merchant opens app (app sends heartbeat)
- Merchant confirms transaction
- Merchant taps "I'm available" button

**Status rules:**
```python
def get_merchant_availability(merchant):
    now = datetime.now()
    last_seen = merchant.last_seen

    if now - last_seen < timedelta(minutes=5):
        return "online"
    elif now - last_seen < timedelta(minutes=30):
        return "recently_active"
    else:
        return "offline"
```

**App displays:**
- 🟢 Online (last seen <5 min)
- 🟡 Recently active (last seen 5-30 min)
- 🔴 Offline (last seen >30 min)

**For MVP with 1-2 merchants:**
- Can text merchants "be ready" before sending user
- Don't rely on real-time status
- Post-MVP: Add push notification "Elena is coming with code 7382"

---

## Testing Checklist

**Before MVP:**
- [ ] GET /merchants/nearby returns test merchants
- [ ] Distance calculation accurate (use Haversine formula)
- [ ] GET /merchants/{id} returns full details
- [ ] Availability status updates correctly
- [ ] Map displays merchants at correct locations
- [ ] Error handling for invalid coordinates

**Edge cases:**
- [ ] No merchants in radius → Show message "No nearby merchants"
- [ ] User location denied → Ask to enable location or enter address
- [ ] Merchant goes offline during transaction → Handle gracefully
- [ ] Multiple merchants same location → Display both on map

---

## Security Considerations

### Public Endpoint Safety

**GET /merchants/nearby is public because:**
- User might browse before creating account
- No sensitive data exposed
- Helps with user acquisition

**What's NOT exposed:**
- Merchant's BCH address (not in public response)
- Merchant's PagoMóvil number (not in public response)
- Merchant's earnings/revenue
- Internal merchant IDs (use opaque IDs like merchant_9kLmP)

### Rate Limiting

**Prevent abuse:**
- Max 60 requests/minute per IP for nearby search
- Max 120 requests/minute for merchant details
- No scraping entire merchant database

---

## App UX: One App, Two Modes

**How the app switches between modes:**

**Bottom navigation:**
```
[Need Cash] [Provide Cash] [Profile]
```

**"Need Cash" mode (recipient):**
- Shows map of nearby merchants
- Enters code from sender
- Confirms receiving cash

**"Provide Cash" mode (merchant):**
- Shows "Create Listing" button (if no profile)
- OR shows "My Merchant Profile" (if listed)
- Enters code from recipient
- Confirms handing cash
- Toggle availability (online/offline)

**Profile tab (shared):**
- User settings
- Transaction history (both sent & received)
- Merchant stats (if merchant)

**Key insight:**
- Users can use BOTH modes in the same day
- Example: Receive remittance in morning, provide cash service in afternoon
- No artificial separation between "merchant app" and "user app"

---

## Future Enhancements (Post-MVP)

### V1.1: Merchant Verification (Optional)
- Add optional "Verified Merchant" badge
- Requires document upload (business license, ID)
- Manual review by Asgaya team
- Verified merchants get higher transaction limits (€200+)
- **Important:** Unverified merchants still allowed (permissionless)

### V1.2: Merchant Analytics Dashboard
- Add `GET /api/v1/merchants/my-analytics` endpoint
- Merchant dashboard showing:
  - Earnings over time (in EUR, VES, BCH)
  - Transaction volume charts
  - Customer ratings breakdown
  - Settlement history
  - Peak hours analysis

### V2: Merchant Discovery Improvements
- Filter by rating (>4.5 stars only)
- Sort by distance, rating, transaction count
- Search by name/neighborhood
- "Favorite merchants" for repeat users
- "Notify me when merchants nearby" push notification

### V2: Dynamic Transaction Limits
- Increase limits based on reputation
- New merchant: €50 limit
- 10 transactions + 4.5★: €100 limit
- 50 transactions + 4.8★: €200 limit
- 200 transactions + 4.9★: €500 limit

---

## Related Documents

- **User Flows:**
  - [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) - Recipient sees merchant map
  - [RS046-3 Merchant Flows](android-app/flows/merchant-flows.md) - Merchant settings
- **Other APIs:**
  - [transaction-apis.md](android-app/backend-apis/transaction-apis.md) - Transaction confirmation
  - [settlement-apis.md](android-app/backend-apis/settlement-apis.md) - Settlement preferences
- **Backend Index:** [RS046-5 Backend APIs Index](android-app/backend-apis/README.md)

---

*Created: April 27, 2026*
*Status: Complete (MVP Minimal)*
*Philosophy: Manual onboarding for beta, automated registration post-MVP*
*MVP: 1-2 merchants, manual database insert, public nearby search only*
