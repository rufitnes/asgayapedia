# Market-Making Partners — Distributed Coordination for Stability

**Concept Type:** Strategic Pattern
**Category:** Network Design
**Related:** [Bubble Prevention](bubble-prevention.md), [BCH Sellers as Capital Providers](bch-miners-as-escrows.md)

**⚠️ NOTE:** This document describes historical concepts for market stability coordination. In the current covenant-based architecture:
- **BCH sellers** post with volatility buffer capital (not "escrow operators")
- **BCH buyers** provide optional instant settlement (replacing "LP" role)
- Market coordination happens through smart contract mechanics rather than centralized coordination

See:
- **[Core Architecture: Incentives](../core-architecture/why-promote-adoption.md)** — Current participant roles
- **[Pull System](pull-system.md)** — Covenant-based mechanics

---

## What It Is

Pulperos (merchant liquidity providers) aren't just infrastructure - they're **active market participants** whose behavior affects price stability. By coordinating their actions during high-volume periods, they become distributed market makers that help stabilize BCH prices.

**Core insight:** "The other side of every transaction is a person whose behavior we can influence."

---

## The Problem It Solves

### Without Coordination

**Weekend remittance spike (€240k):**
```
1. BCH seller → Pre-positions reserves (removes buy pressure)
2. Users → Cash out at neighborhood stores
3. Pulperos → Accumulate BCH
4. Pulperos → HOLD BCH (no sell pressure)
5. Result → Price stays elevated, BCH seller replenishes at high price
```

**Outcome:** Arbitrage profits reduced, potential bubble formation.

### With Coordination

**Same weekend spike:**
```
1. BCH seller → Pre-positions reserves (removes buy pressure)
2. Users → Cash out at neighborhood stores
3. Pulperos → Accumulate BCH
4. System → Warns about spike risk
5. Pulperos → SELL immediately (adds sell pressure)
6. Result → Price balanced, stable market
```

**Outcome:** Better arbitrage, no bubble, network stability.

---

## How It Works

### Coordinated Market Balance

**The equation:**
```
Weekend stability =
  BCH seller removes buy pressure (pre-positioning) +
  Pulpero adds sell pressure (coordinated selling)
```

**Effect on capital requirements:**

| Strategy | BCH Seller Capital | Pulpero Compliance | Market Stability | BCH Seller Profit |
|----------|----------------|-------------------|------------------|---------------|
| **BCH seller only** | €100k | N/A | 60-70% | 0.35% |
| **+ Warning nudge** | €30k | 60% | 85% | 0.45% |
| **+ POS default** | €30k | 85% | 95% | 0.52% |

**Result:** 3x capital reduction, better stability, higher profits.

---

## Implementation Mechanisms

### 1. Warning Notifications (Active Nudge)

**Trigger:** Spike risk detected (>15% market impact)

**Notification to pulperos:**
```
⚠️ ALERTA DE VOLUMEN ALTO

Debido al alto número de remesas este fin de semana,
existe peligro de burbuja especulativa en BCH.

RECOMENDACIÓN: Convierta su BCH a HNL inmediatamente.

Volumen estimado: {forecast} BCH
Impacto de mercado: {impact}%

Puede volver a mantener BCH el lunes cuando el
volumen se normalice.

Esta recomendación protege su capital.

[Convertir a HNL Ahora]
```

**Why it works:**
- **Aligned incentives:** Pulpero avoids volatility risk
- **Counter-pressure:** Creates sell pressure to balance market
- **Educational:** Teaches market dynamics and pulpero's role
- **Honest:** Genuinely protects pulpero capital

**Expected compliance:** 60% (opt-in model)

---

### 2. POS Default Override (Opt-Out Model)

**More sophisticated approach with behavioral design:**

**Normal operation:**
- Pulpero controls when to convert BCH→HNL
- Full agency preserved

**High-volume spike detected:**
```
⚠️ CAMBIO TEMPORAL DE CONFIGURACIÓN

Debido al alto volumen de remesas este fin de semana
(estimado: €240k, 24% del mercado BCH),

SU POS CONVERTIRÁ AUTOMÁTICAMENTE BCH→HNL
en las próximas 48 horas.

RAZÓN: Protección contra burbuja especulativa.

Esta configuración volverá a normal el lunes.

Si prefiere mantener su configuración actual:

[RECHAZAR CAMBIO] [ACEPTAR PROTECCIÓN]

Usuarios que aceptan protección: 127/150 (85%)
```

**Default:** Auto-convert ENABLED (requires active rejection)

**Why opt-out beats opt-in:**
1. **Friction favors safety** - Most accept via inertia
2. **Preserves agency** - Can still reject
3. **Transparency** - Clear explanation of why
4. **Social proof** - Shows acceptance rate
5. **Temporary** - Returns to normal Monday (low commitment)

**Expected compliance:** 85% (opt-out + social proof)

---

## The Economics of Coordination

### Capital Efficiency Multiplier

**Without coordination:**
- Need €100k to cover €240k spike alone
- 60-70% effective
- High capital barrier

**With 85% pulpero coordination:**
- Need €30k BCH seller capital
- 95% effective
- **3x capital reduction**
- **Better results**

**Why coordination is powerful:**
- Distributed action beats centralized capital
- Many small actors > one large actor
- Network effects compound

---

### Profit Distribution

**BCH seller perspective:**
- Less capital tied up (€30k vs €100k)
- Better arbitrage (€378 replenish vs €385)
- Higher ROI (0.52% vs 0.35%)

**Pulpero perspective:**
- Sells at peak (€380 weekend)
- Avoids volatility risk
- Protected capital
- Educational benefit (learns market dynamics)

**Network perspective:**
- Stable prices (no bubble formation)
- Trust maintained
- Adoption continues
- Everyone wins

---

## Ethical Considerations

### Is This Manipulation?

**NO. Here's why:**

**Manipulation characteristics:**
- Hidden information
- Serves manipulator only
- Harms participants
- Dishonest intent

**This coordination:**
- ✅ Transparent information (clear explanation)
- ✅ Serves all participants (aligned incentives)
- ✅ Protects pulperos (genuine capital protection)
- ✅ Honest intent (prevent real bubble risk)

**The difference:**
- **Manipulation:** "Buy now!" (pump & dump)
- **Coordination:** "Sell now to protect your capital from incoming spike"

---

### Legitimate Precedents

**Similar coordination patterns:**

1. **Central bank communications:**
   - Forward guidance about interest rates
   - Coordinates market expectations
   - Prevents disorderly moves

2. **Traffic apps (Waze):**
   - Coordinates route selection
   - Prevents congestion
   - Benefits all drivers

3. **Airline load management:**
   - Offers incentives to change flights
   - Prevents overbooking issues
   - Passengers choose to participate

4. **Farmer cooperatives:**
   - Coordinate harvest timing
   - Prevents price crashes
   - Members benefit collectively

**Pattern:** Information + choice + aligned incentives = legitimate coordination

---

## Trust Requirements

### For This to Work Ethically

**1. Genuine risk only:**
```python
if market_impact > 0.15:  # Real spike risk
    warn_pulperos()
else:
    # Don't cry wolf
    pass
```

**2. Transparent explanation:**
- Why warning triggered
- What will happen
- When returns to normal

**3. Always preserve agency:**
- Opt-out available
- No penalties for rejection
- Respect choice

**4. Temporary only:**
- 48-hour window max
- Automatic return to normal
- Not permanent control

**5. Track record:**
- Show past accuracy
- Admit when wrong
- Build trust over time

**Violation of any = breaks trust = system fails**

---

## Success Metrics

### Primary Metrics

**Coordination effectiveness:**
- **Warning compliance:** 60% target (opt-in)
- **POS default compliance:** 85% target (opt-out)
- **Market stability:** <5% volatility during spike

**Capital efficiency:**
- **Coverage ratio:** BCH seller capital / spike volume
- **Effectiveness multiplier:** Stability / capital deployed
- **ROI improvement:** With coordination vs without

### Secondary Metrics

**Trust indicators:**
- Pulpero satisfaction with warnings
- False positive rate (warnings without real spike)
- Opt-out rate trends (should stay stable)

**Educational impact:**
- Pulpero understanding of market dynamics
- Proactive selling before warnings
- Independent stability contributions

---

## Evolution Path

### Phase 1: Manual Warnings (Now)

- Operator detects spike risk manually
- Sends notifications through POS
- Tracks compliance manually
- Learns optimal messaging

### Phase 2: Automated Detection (Month 6)

- Bot detects spike risk automatically
- Sends warnings based on forecast
- Tracks compliance automatically
- A/B tests messaging

### Phase 3: Predictive Coordination (Month 12)

- Pulperos anticipate patterns independently
- Proactive selling before warnings
- System coordinates less, education more
- Distributed intelligence emerges

### Phase 4: Self-Organizing Network (Month 18+)

- Pulperos coordinate peer-to-peer
- Warnings become confirmations not instructions
- Network stability self-maintains
- Asgaya provides info, network acts

---

## Risks and Mitigations

### Risk 1: Over-coordination

**Problem:** Everyone sells, causes crash

**Mitigation:**
- Graduated warnings (not all-or-nothing)
- Stagger selling recommendations
- Monitor market depth continuously

### Risk 2: Trust erosion

**Problem:** False warnings, pulperos lose trust

**Mitigation:**
- High threshold for warnings (>15% impact)
- Transparent track record
- Admit errors quickly

### Risk 3: Regulatory concern

**Problem:** Coordinated selling = market manipulation?

**Mitigation:**
- Transparent operation
- Legitimate purpose (capital protection)
- Preserves individual choice
- Document legal review

### Risk 4: Adverse selection

**Problem:** Sophisticated pulperos reject, naive accept

**Mitigation:**
- Education for all participants
- Show track record
- Make rejecting legitimate choice
- No penalties

---

## Why This Matters

### Most Crypto Projects Fail Because:

❌ Centralized control (manipulation)
❌ Misaligned incentives (Ponzi)
❌ No real utility (speculation only)
❌ Can't handle scale (technical limits)

### Asgaya Succeeds Because:

✅ Distributed coordination (not control)
✅ Aligned incentives (everyone benefits)
✅ Real utility (remittances needed)
✅ Coordination scales better than capital

**The insight:** You don't need massive capital to stabilize markets if you have coordinated participants with aligned incentives.

---

## Related Concepts

- **Bubble Prevention:** [bubble-prevention.md](bubble-prevention.md)
- **BCH Seller Incentives:** [bch-miners-as-escrows.md](bch-miners-as-escrows.md)
- **Pull System:** [pull-system.md](pull-system.md)

---

## Key Quotes

> "Pulperos aren't just cash-out points - they're the other side of the market. Their selling behavior is a control variable we can influence." - Suso

> "Coordination beats capital. Many small actors working together > one large actor working alone." - Market-Making Partners Concept

---

*Concept discovered: April 20, 2026*
*Emerged from: Capital constraint analysis + Sunday operations question*
*Insight: The question "are neighborhood stores open Sunday?" led to entire coordination strategy*
*Philosophy: Coordination > Capital, Education > Control, Network > Individual*
*Status: Strategic framework validated, ready for implementation*
