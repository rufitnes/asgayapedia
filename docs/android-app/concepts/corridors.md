# Remittance Corridors

**What they are:** Supported sending country → receiving country pairs for remittances

## Overview

A remittance corridor represents a directional flow of money between two countries. Asgaya focuses on specific corridors where:
- High remittance volume exists
- Legacy fees are excessive (>3%)
- Regulatory environment permits permissionless services
- Local payment rails are accessible

## Phase 0 Corridor: Spain → Venezuela

### Why This Corridor?

**Sender Side (Spain):**
- Large Venezuelan diaspora (~300,000+ people)
- EU regulatory framework (MiCA-compliant possible)
- Instant payment rail available (Bizum)
- BCH-friendly banking environment

**Receiver Side (Venezuela):**
- High remittance dependency (>4% of GDP from Spain)
- Legacy services charge 6-10% fees
- Local instant payment available (PagoMóvil)
- Cash-based economy (merchant network viable)

**Market Conditions:**
- High fee differential: 6% legacy vs 1% Asgaya
- Strong demand signals from both diaspora and recipients
- Regulatory gap: no established crypto remittance services

## Corridor Economics

Each corridor has unique characteristics:

| Factor | Spain→Venezuela | Future: USA→Mexico |
|--------|-----------------|-------------------|
| **Volume** | ~€500M annually | ~$50B annually |
| **Legacy Fee** | 6-10% | 3-5% |
| **Sender Rail** | Bizum (instant) | Zelle/ACH |
| **Receiver Rail** | PagoMóvil (instant) | SPEI (instant) |
| **Merchant Density** | Moderate | High |
| **Regulatory** | MiCA | FinCEN/BSA |

## Corridor Expansion Criteria

Before adding a new corridor, validate:

1. **Demand Signal:** >$100M annual remittance flow
2. **Fee Gap:** Legacy services charge >3%
3. **Payment Rails:** Instant fiat transfers available both sides
4. **Merchant Network:** Cash-out points accessible
5. **Regulatory:** Permissionless operation possible
6. **BCH Liquidity:** Sellers available with capital

## Future Corridors (Phase 1+)

Potential expansion targets:
- **USA → Mexico:** Largest global corridor (~$50B)
- **Spain → Latin America:** Colombia, Ecuador, Peru
- **UAE → South Asia:** India, Pakistan, Bangladesh
- **Italy → North Africa:** Egypt, Tunisia

Each requires corridor-specific research and pilot testing.

## Technical Implementation

### Corridor Configuration
```json
{
  "corridorId": "ESP-VEN",
  "senderCountry": "ES",
  "receiverCountry": "VE",
  "senderCurrency": "EUR",
  "receiverCurrency": "VES",
  "senderPaymentMethods": ["bizum", "sepa"],
  "receiverPaymentMethods": ["pagomovil", "cash"],
  "exchangeRateSource": "DolarAPI",
  "active": true
}
```

### Corridor-Specific Logic
- Exchange rate calculations
- Payment method availability
- Merchant discovery (filtered by corridor)
- Fee structures (may vary by corridor economics)

## Related Documentation

- [Cold Start Strategy](../../decisions/cold-start-strategy.md) - How we launch in new corridors
- [Exchange Rates](../../decisions/how-exchange-rates-work.md) - Multi-currency conversion
- [Bulletin Board](../../concepts/bulletin-board.md) - Corridor filtering for merchants
- [Unknown: Corridor Demand Signals](../../unknowns/market/corridor-demand-signals.md)
