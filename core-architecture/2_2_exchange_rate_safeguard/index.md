# 2.2: Safeguard to Keep Exchange Rate Tied to Real-World Value

## Requirement Statement

> Safeguard to keep the exchange rate tied to real world value of the currencies in case of remittances

**From:** `engeneering_requirements.md` section 2.2

## Why This Requirement Exists

**The government extraction problem:** Some governments impose official exchange rates below market value to extract wealth from remittances.

**Example: Argentina**
- Official rate: 1 USD = 350 ARS
- Blue dollar rate: 1 USD = 1,000 ARS
- Government captures: ~65% of remittance value through rate manipulation

**Who gets hurt:** Migrants sending money home, families receiving remittances

**How it's unfair:** Hard-earned money stolen through financial repression

**Asgaya's answer:** Bypass government-controlled rates, use real market rates.

## Current Solution

**Use BCH/fiat market pairs to ensure fair value:**

```
Sender: €50 EUR
    ↓
Escrow: Buys BCH at Kraken market rate (e.g., €385/BCH)
    ↓
Amount: 0.1299 BCH
    ↓
Merchant: Receives 0.1299 BCH
    ↓
Merchant: Sells BCH at local market rate (or holds)
    ↓
Recipient: Gets fair market value in local currency
```

**Key protection:** BCH is global, permissionless, market-driven
- No government can dictate BCH/EUR rate
- No government can dictate BCH/local currency rate
- **Real market value always flows through**

**How this bypasses manipulation:**

**Traditional remittance (subject to control):**
```
EUR → Bank → Government-imposed rate → Local currency
Result: Government captures spread
```

**Asgaya (escapes control):**
```
EUR → Kraken (global market) → BCH → Local market → Local currency
Result: Market determines value, government can't intercept
```

## How This Meets The Requirement

**Guarantee of fair value:**

1. **EUR → BCH conversion:** Kraken global market rate (publicly visible, competitive)
2. **BCH → Local currency:** Local market rate (P2P, merchant sets, competitive)
3. **No extraction point:** Government can't impose artificial rate

**Transparency:**
- User sees: "€50 = 0.1299 BCH at rate €385/BCH"
- Merchant sees: "0.1299 BCH = X local currency at current rate"
- **All rates verifiable against public markets**

**Example: Honduras corridor**
```
Sender: €50
Kraken rate: €385/BCH → 0.1299 BCH
Honduras BCH/HNL rate: 1 BCH = 9,625 HNL (example)
Recipient gets: ~1,250 HNL
Western Union would give: ~1,100 HNL (inflated spread + fees)
Difference: 13.6% more value via Asgaya
```

## Trade-offs Made

**NO flexibility on exchange rates:**

**In previous requirements:** Freedom to users (cash out or hold, convenience choice)

**In THIS requirement:** NO compromise

**Users get:**
- Market rate
- Fixed compensation for escrow/merchant
- **That's it**

**Escrows CANNOT:**
- Inflate exchange rate
- Negotiate better personal rate
- Extract profit through spread manipulation

**Merchants CANNOT:**
- Quote below-market BCH rates to users
- Exploit information asymmetry
- Price-gouge on cash-outs

**Why zero flexibility?**

**Protection against:**
- Escrow becoming predatory (charging hidden spread)
- Merchant becoming exploitative (monopoly pricing)
- System replicating legacy problems (opacity, extraction)

**The line we draw:** Convenience = flexible. Exchange rate = non-negotiable.

## Implementation Status

- ✅ **Proven:** Kraken API provides transparent market rates
- ✅ **Proven:** BCH global markets exist (liquid, efficient)
- 🔄 **In development:** Display real-time rates in sender/merchant apps
- 🔄 **In development:** Rate comparison tools (Asgaya vs. WU/MoneyGram)
- ❌ **Not tested:** Local market rate discovery (how merchants price BCH)
- ❌ **Not tested:** User verification of fair rates

**Trial runs will determine:**
- Are our incentive amounts adequate?
- Do users trust the rates shown?
- Do merchants try to inflate spreads anyway?
- **Interview users, crunch numbers, adjust if needed**

## Future Improvements

**V1:** Establish rate transparency baseline
- Show Kraken rate in real-time in app
- Show local market rate comparison
- Educate users on how to verify

**V1.1:** Rate guarantees
- Escrow commits to "Kraken rate + max 0.5% spread"
- Merchant commits to "Local market rate +/- 1%"
- Transparent, published, enforceable

**V2:** Decentralized rate discovery
- On-chain rate oracles
- Community-verified local markets
- Impossible to manipulate without detection

**V3:** Smart contract enforcement
- Rates locked in smart contract
- Automatic arbitrage if deviation
- Trustless guarantee of fair value

## Related Documents

- **Research:** [RS009 Argentina blue dollar, RS010 Honduras rate analysis]
- **Concepts:** [To be cross-referenced by local LLM]
- **Architecture:** [Kraken integration, pull system rate locking]

---

*Last updated: April 8, 2026 by Coordination Claude*  
*Core principle: "Governments extract wealth through exchange rate manipulation. We bypass this with market rates."*  
*Trade-off: Freedom on convenience, ZERO flexibility on exchange rates. Market value is non-negotiable.*
