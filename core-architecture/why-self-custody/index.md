# Why: Self-Custody

**Sub-requirement of:** [Why: Permissionless](core-architecture/why-permissionless.md)

**Core Requirement:** Users must hold their own BCH keys. Asgaya has zero access to user funds.

---

## The Problem

### Custodial Systems Are Permissioned by Definition

**What "custodial" means:**
- Platform controls user funds
- Platform can freeze accounts
- Platform can be compelled by authorities
- Platform can be hacked
- **Platform decides who can access their money**

**Examples:**
- PayPal freezes accounts arbitrarily
- Banks freeze accounts based on "suspicious activity"
- Exchanges freeze accounts based on nationality
- **All claim it's "for your protection"**

**The truth:** Custodial = you're asking permission to use your own money.

---

## Why Self-Custody Is Non-Negotiable

### 1. Custodial Makes Asgaya a Target

**If Asgaya controlled user funds:**
- Single point of failure (hack Asgaya = steal all funds)
- Honeypot for attackers (millions in one place)
- Liability for losses (users sue if hacked)
- Insurance requirements (expensive, maybe impossible)

**With self-custody:**
- No central honeypot (each user controls own funds)
- Can't hack what we don't have
- No liability for user losses (they control keys)
- **Asgaya can't lose what it never had**

### 2. Custodial Requires Regulatory Licenses

**If Asgaya held user funds, we'd be classified as:**
- Payment Institution (requires license, €125k+ capital requirement in EU)
- Money Transmitter (state-by-state licenses in US, €millions in compliance)
- Cryptocurrency Exchange (MiCA regulations in EU, complex compliance)

**What this means:**
- Years to get licenses
- Millions in capital requirements
- Ongoing compliance costs
- KYC requirements (defeats no-KYC principle)
- **Can't serve unbanked if we need banking licenses**

**With self-custody:**
- Not a custodian (no custody license needed)
- Not holding funds (not a payment processor)
- Peer-to-peer coordinator (regulatory gray zone, not clearly prohibited)
- **Can launch and serve users immediately**

### 3. Custodial Concentrates Control

If Asgaya controlled funds, it would have the power to restrict access—something the protocol is designed to avoid. Examples of custodial limitations:
- Account restrictions by jurisdiction
- Transaction filtering by address
- Mandatory reporting requirements
- Risk-based service denial
- **With self-custody, only the user controls their assets**

**With self-custody:**
- Can't freeze what we don't control
- Can't block what we can't access
- Can't report what we don't see
- **Users own their money, literally**

### 4. Self-Custody Is Self-Sovereignty

**The philosophical core:**

**Custodial model:**
- "We'll hold your money safely"
- "Trust us to protect you"
- "We know what's best"
- **You're dependent on our goodwill**

**Self-custody model:**
- "You hold your money"
- "You're responsible for protecting it"
- "You decide what's best"
- **You're sovereign over your wealth**

**"Not your keys, not your coins" isn't a slogan—it's reality.**

---

## The Trade-Off: Convenience vs. Sovereignty

### What Users Give Up with Self-Custody

**No password reset:**
- Lose your keys → Funds gone forever
- No customer service can help
- No "forgot password" button
- **Responsibility is yours**

**No reversals:**
- Send to wrong address → Funds gone
- Get scammed → No chargeback
- Make mistake → No undo
- **Transactions are final**

**No safety net:**
- Phone stolen → Need recovery phrase or funds gone
- Recovery phrase lost → Funds gone forever
- No bank to call, no insurance to claim
- **You are the bank**

### What Users Gain with Self-Custody

**True ownership:**
- Your keys, your coins, your control
- No one can freeze your account
- No one can confiscate your wealth
- **Financial sovereignty**

**Permissionless access:**
- Transactions proceed without central approval
- Access doesn't depend on nationality, politics, or beliefs
- The protocol treats all participants equally
- **Open by design**

**Privacy:**
- No central database of your transactions
- No company selling your data
- No government surveillance by default
- **Your finances are your business**

---

## Why This Trade-Off Is Acceptable

### The Alternative (Custodial) Is Worse

**Custodial creates systemic risk for everyone:**
- One hack → Everyone loses
- One court order → Everyone's accounts frozen
- One regulatory change → Service shuts down
- **Centralized failure affects all users**

**Self-custody creates individual responsibility:**
- User loses keys → Only that user affected
- User makes mistake → Only that user affected
- No systemic risk
- **Each user's security is independent**

**Better:** Small percentage of users lose funds due to lost keys
**Worse:** Platform hacked and all users lose funds

**The math favors self-custody.**

### User Choice Is Preserved

**Don't want responsibility? → Cash out immediately**
- Receive BCH from remittance
- Go to merchant immediately
- Exchange for local currency
- Hold zero BCH
- **Zero risk of lost keys**

**Want sovereignty? → Hold BCH and accept responsibility**
- Save recovery phrase
- Protect your keys
- Accept risk
- Gain control
- **You chose this**

**The system supports both:**
- Low-commitment users cash out (safe, simple)
- High-commitment users hold BCH (sovereign, responsible)
- **No one is forced into a model they don't want**

---

## Self-Custody Enables True Peer-to-Peer

### Asgaya Doesn't Touch Funds—Ever

**The flow:**
1. Sender sends EUR to escrow's bank account (escrow controls)
2. Escrow buys BCH on Kraken (escrow controls)
3. Escrow sends BCH to recipient's wallet (recipient controls)
4. Recipient cashes out at merchant or holds (recipient controls)

**At no point does "Asgaya the platform" control any funds.**

**Each actor controls their own funds:**
- Escrow: Controls their bank account + BCH wallet
- Merchant: Controls their BCH wallet
- Recipient: Controls their BCH wallet
- Sender: Controls their bank account

**Asgaya coordinates. We don't custody.**

**This is what "peer-to-peer" actually means.** Not "platform intermediating," but participants transacting directly while Asgaya provides the communication layer.

---

## Why "Lost Keys = Lost Funds" Is a Feature, Not a Bug

### Harsh Reality Ensures Responsibility

**If we offered recovery:**
- "Password reset via email" → Email gets hacked, funds stolen
- "Customer service recovery" → Social engineering attacks
- "Backup keys on servers" → Server gets hacked, all funds stolen
- **Every safety net is a security hole**

**By making loss permanent:**
- Users take it seriously
- Users save recovery phrases
- Users understand the stakes
- **Accountability produces careful behavior**

**Example:**
- User A: "Lost keys = lost funds? I'll write down my recovery phrase in three places"
- User B: "I can always reset password, no worries" → Gets phished, loses funds

**Harsh reality produces better security practices than false safety.**

### The Unrecoverable Is Unstealable

**Key insight:** If Asgaya can recover your keys, so can hackers.

**Scenarios:**

**Recoverable system:**
- User forgets password
- Clicks "reset password"
- Receives email with reset link
- **Attacker hacks email, steals funds**

**Unrecoverable system:**
- User forgets password
- No reset option
- Funds locked forever
- **Attacker can't access them either**

**Trade-off:** Some users lose funds due to carelessness vs all users at risk of theft.

**Self-custody chooses: Better 5% lose due to carelessness than 100% vulnerable to hacking.**

---

## Education Before Commitment

### Users Must Understand Before They Hold

**The onboarding requirement:**

**Before wallet creation:**
1. Explain self-custody clearly
2. Show recovery phrase importance
3. Test understanding ("What happens if you lose your recovery phrase?")
4. Offer choice: "Cash out immediately" vs "Hold BCH"
5. **Only proceed if user demonstrates understanding**

**Why this matters:**
- User who doesn't understand → Loses funds → Blames Asgaya
- User who was educated → Loses funds → Accepts responsibility

**We can't prevent all losses, but we can ensure users knew the stakes.**

---

## Related Requirements

- [Why: Permissionless](core-architecture/why-permissionless.md) — Self-custody is essential for true permissionless access
- [Why: No KYC](core-architecture/why-no-kyc.md) — Custody would require licenses, which require KYC

---

## Trade-offs and Decisions

See the **Decisions** section for how we achieve self-custody safely:

- Decision: Key backup education flow (coming soon)
- Decision: Tiered security recommendations (coming soon)

---

## The Bottom Line

**Custodial = Asking permission to use your own money.**

**Self-custody = Owning your money, literally.**

The trade-off is real:
- Lose keys → Lose funds (no recovery)
- But no one can take your funds from you (no censorship)

**For a truly permissionless system, this trade-off is non-negotiable.**

If we held user funds, we'd need licenses. If we need licenses, we need KYC. If we need KYC, we exclude the unbanked.

**Self-custody isn't just about philosophy—it's about staying permissionless.**

---

*Last updated: May 1, 2026*
*Core principle: "Not your keys, not your coins. Self-custody enables permissionless, but requires responsibility."*
