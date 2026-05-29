# Cash Accounts

**What they are:** Human-readable payment identifiers on Bitcoin Cash (e.g., `username#123`)

## Overview

CashAccounts provide a user-friendly alternative to cryptocurrency addresses. Instead of sending BCH to a long hexadecimal address, users can send to readable identifiers like `carlos#42567`.

## How Asgaya Uses CashAccounts

### Recipient Identification
When a sender creates a remittance covenant, they specify the recipient's CashAccount. This identifies:
- Who can claim the BCH
- Where to send claim notifications
- The recipient's public key for covenant validation

### Example Flow
```
1. Sender enters: "carlos#42567"
2. App resolves to BCH address: bitcoincash:qr...
3. Covenant created addressed to that pubkey
4. Recipient "carlos" receives notification
5. Carlos claims using his CashAccount credentials
```

## Technical Details

- **Format:** `username#number`
- **Blockchain:** BCH CashAccounts are registered on-chain
- **Resolution:** Apps query the blockchain to resolve CashAccount → address
- **Collision Prevention:** The number suffix ensures uniqueness

## Benefits for Asgaya

1. **User-Friendly:** Recipients remembered by name, not hex strings
2. **Notification Target:** CashAccount username can link to phone/app
3. **Verification:** Sender can confirm they're sending to the right person
4. **Privacy:** One CashAccount can represent multiple addresses

## Security Considerations

- CashAccounts are publicly registered on BCH blockchain
- Anyone can look up a CashAccount and see its associated address
- For privacy, users can register multiple CashAccounts
- Asgaya validates CashAccount signatures to prevent impersonation

## Related Documentation

- [Bitcoin Cash CashAccounts Specification](https://gitlab.com/cash-accounts/specification) - Protocol specification
- [Covenant Setup Flow](../flows/sender-flows/covenant-setup/2-recipient-selection.md)
- [Bulletin Board](../../concepts/bulletin-board.md) - How merchants advertise CashAccounts
