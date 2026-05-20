# Decision: Bizum Concept Field - Phone Numbers

**Decision Date:** April 22, 2026
**Status:** Implemented
**Related Requirement:** [Permissionless](../core-architecture/why-permissionless.md) (Error Prevention)

---

## The Goal (Architectural Ideal)

Use **semantic identifiers** in the Bizum concept field to enable automated matching and reduce errors.

**Ideal format:** `ASG_VEN_001` or `ASG-recipient-phone`

**Why we wanted this:**
- Clear signal that payment is Asgaya-related
- Easy pattern matching for notification listener
- Enables automated verification (escrow can validate format)
- Reduces risk of receiving unrelated Bizum payments
- Self-documenting (anyone can see what the transfer is for)

---

## The Constraint (Reality Check)

**Discovery:** Bank rejects Bizum transfers with underscores in the concept field.

**How we discovered this:**
- Real-world testing with €1 Bizum transfer (April 22, 2026)
- Used format: `ASG_VEN_001`
- Result: **Transfer rejected by bank**
- Error message: "Invalid characters in concept field"

**Research reference:** `../research/RS042_bizum_concept_field_constraints.md`

**Testing details:**
- Amount: €1.00
- From: Personal Bizum account (Spain)
- To: Test merchant account
- Concept: `ASG_VEN_001`
- Result: Rejected within seconds

**Additional constraint discovered:**
- Hyphens (`-`) also rejected
- Spaces allowed but discouraged (inconsistent SMS encoding)
- Special characters (`@`, `#`, `$`) rejected
- **Only alphanumeric and spaces reliably accepted**

---

## The Decision

**Use recipient phone numbers in Bizum concept field.**

**Format:** `+584121234567` (international format with `+`)

**Fallback:** `584121234567` (if `+` is rejected, drop it)

**Rationale:**
1. **Works within bank constraints** (verified by testing)
2. **Globally unique** (no collision risk)
3. **Self-documenting** (phone identifies recipient + corridor)
4. **No lookup table required** (reduces escrow complexity)
5. **Verifiable** (escrow can confirm recipient owns phone via SMS)

---

## Implementation Details

### Sender Flow
1. Sender enters recipient phone: `+58 412 123 4567`
2. App formats for Bizum concept: `584121234567` (no spaces no +)
3. Sender makes Bizum payment with this concept
4. Merchant receives notification with concept field

### Notification Listener (Merchant)
1. Parse incoming Bizum SMS/notification
2. Extract concept field: `584121234567`
3. Match against pending Asgaya transfers
4. If match found, proceed with BCH settlement

### Escrow Verification
1. Sender claims payment sent to `584121234567`
2. Escrow checks merchant received payment with that concept
3. If confirmed, triggers BCH purchase and settlement

---

## Trade-offs Accepted

### Lost: Semantic Identifiers
- Can't use formats like `ASG_VEN_001`
- Concept field is less self-documenting for humans
- Pattern matching is phone-number-specific (not generic)

### Gained: Reliability
- Works within real bank constraints
- No rejected transfers
- Simpler implementation (no code generation/lookup)
- Better verification (phone is cryptographically verifiable via SMS)

### Privacy Consideration
- Recipient phone visible in Bizum concept field
- **Mitigation:** Merchant already knows recipient phone (it's their customer)
- **Acceptable risk:** Bizum is P2P between merchant and sender; recipient phone is not exposed beyond this relationship

---

## Validation

**How we verify this decision:**
1. ✅ Successfully completed €1 test transfer (April 22, 2026)
2. ✅ Notification listener correctly parsed phone from concept
3. ✅ No transfer rejections in subsequent testing
4. ⏳ Pending: Full corridor testing (EUR→VES)

---

## Future Considerations

### If Bank Constraints Change
If Bizum starts accepting underscores/hyphens in future:
- **Do NOT migrate immediately**
- Phone numbers work reliably; changing adds risk
- Only consider migration if:
  - Phone numbers create security/privacy issues
  - New format provides significant advantage
  - Backward compatibility can be maintained

### Alternative Payment Systems
Other corridors may have different constraints:
- **PagoMóvil (Venezuela):** Test concept field constraints separately
- **Mercado Pago (Argentina):** May have different rules
- **SEPA transfers:** Longer concept fields, different character support

**Action:** Document constraints per-corridor, not per-protocol.

---

## Related Decisions

- [Payment Timeout Window](payment-timeout-window.md) — Notification delay constraints

---

## References

- **Research:** `../research/RS042_bizum_concept_field_constraints.md`
- **Implementation:** `/docs/android-app/notification-listener/bizum-android.md`
- **User Flow:** `/docs/android-app/flows/sender-flows.md` (Step 7: Bizum payment)

---

*Decision made: April 22, 2026*
*Validated: €1 test transfer successful*
*Status: Active, working as designed*
