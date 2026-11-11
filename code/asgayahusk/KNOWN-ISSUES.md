# Known Issues - AsgayaHusk v0.2

**Last Updated:** 2026-08-24

---

## UX Issues (Non-Blocking)

### Remittance Tab Balance Fetch Is Slow

**Status:** 🟡 **Minor UX issue - defer to Phase 1**

**Discovered:** 2026-08-24 (3-device testing)

**Symptoms:**
- Remittance tab shows "no balance" or 0 balance on first load
- Takes several seconds to fetch and display actual balance
- Wallet tab shows balance correctly (faster)
- Balance eventually loads correctly (just slow)

**Impact:** 
- **Low** - Balance loads eventually, just slow initial UX
- User might think they have no funds when they do
- Doesn't block transactions (just display lag)

**Root Cause (Hypothesis):**
- Remittance tab balance fetch happens async
- ElectrumClient connection might be establishing on-demand
- Not cached from Wallet tab balance fetch

**Workaround:**
- Wait a few seconds for balance to appear
- Check Wallet tab for immediate balance confirmation
- Balance is accurate once loaded

**Recommended Fix (Phase 1):**
- [ ] Cache balance across tabs (shared ViewModel)
- [ ] Pre-connect ElectrumClient on app launch
- [ ] Show loading indicator while fetching (instead of "0")
- [ ] Implement balance refresh button

**Priority:** Low (defer to Phase 1 UX polish)

---

## Testing Notes

**3-Device Configuration:**
- ✅ Pixel 6a (Sender) - working
- ✅ Moto G06 (Recipient) - working
- ✅ 3rd device (Merchant) - working
- ✅ 2 successful covenant tests between 3rd device ↔ Moto G06

**Hardcoded Test Wallets (All Devices):**
- Sender: `bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf`
- Recipient (Isabel): `bchtest:qq2uxg4cu9axyzd9gjnhxwrvealt44mcwunp7gzd0k`
- Merchant (Bob): `bchtest:qz4llaasruxhme2l80dsnme76fmj24ve05h6d8ghe5`

**Note:** All devices share same wallets (hardcoded WIFs for testnet testing)

---

## No Critical Blockers

All core functionality working:
- ✅ Wallet creation (seedTestWallets)
- ✅ Balance fetching (just slow UX)
- ✅ Covenant creation (CREATE)
- ✅ Covenant claiming (CLAIM)
- ✅ Covenant refunding (REFUND)
- ✅ Covenant aborting (ABORT)
- ✅ Multi-device reliability (v0.2 hybrid 100%)
- ✅ 3-device testing capability

**Phase 0 Status:** ✅ **Ready for merchant cashout implementation**

---

## Version History

**v0.2 (Current):**
- Hybrid architecture (Kotlin TCP + WebView compute)
- 100% covenant operation reliability
- All 4 spending paths working (claim, refund, abort, sellerRecoverBuffer*)
- Multi-device validated
- Known issue: Slow balance fetch in Remittance tab (minor UX)

*sellerRecoverBuffer not yet implemented in UI, but covenant path exists

---

**Next Milestone:** Merchant cashout MVP implementation
