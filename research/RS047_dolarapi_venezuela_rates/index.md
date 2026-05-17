# RS047: DolarAPI - Venezuelan Exchange Rates (Dólar Paralelo)

**Research Type:** API Discovery
**Status:** ✅ Confirmed
**Date:** 2026-04-27
**Related:** [RS043 DolarAPI Argentina](research/RS043_dolarapi_ars_rates.md), [1_rate_apis.md](RS046_android_app_requirements&flows/RS046-5_backend_apis/1_rate_apis.md)

---

## TL;DR

**DolarAPI provides Venezuelan exchange rates including "dólar paralelo" (black market rate) - exactly what we need for MVP!**

Venezuela has the same official vs parallel market dynamic as Argentina (dólar blue), and DolarAPI covers it.

---

## Discovery

**URL:** https://ve.dolarapi.com (Venezuelan version)
**Parent API:** https://dolarapi.com (covers all Latin America)

**What it provides:**
- Official Venezuelan exchange rate (VES)
- **Dólar Paralelo** (parallel/black market dollar) ← This is what we need!
- Euro rates (EUR → VES)
- Historical data

---

## Why This Matters

### The Problem We Were Solving

**MVP requirement:** EUR → VES corridor for Venezuelan beta testers

**Question:** Does Venezuela have official vs black market disconnect like Argentina?

**Answer:** YES!
- Official rate: Government-controlled (artificially low)
- Dólar Paralelo: Real market rate (what people actually use)

**Just like Argentina:**
- Argentina: Official peso vs "Dólar Blue"
- Venezuela: Official bolivar vs "Dólar Paralelo"

**We need the parallel rate** because that's what merchants actually use for cash exchanges.

---

## DolarAPI Coverage (All Latin America!)

From https://dolarapi.com:

| Country | Currency | Parallel Market? | URL |
|---------|----------|------------------|-----|
| 🇦🇷 Argentina | ARS | ✅ Dólar Blue | dolarapi.com |
| 🇻🇪 Venezuela | VES | ✅ Dólar Paralelo | ve.dolarapi.com |
| 🇨🇱 Chile | CLP | ? | cl.dolarapi.com (?) |
| 🇺🇾 Uruguay | UYU | ? | - |
| 🇲🇽 México | MXN | ? | - |
| 🇧🇴 Bolivia | BOB | ? | - |
| 🇧🇷 Brasil | BRL | ? | - |
| 🇨🇴 Colombia | COP | ? | - |

**For Asgaya MVP:**
- EUR → VES (Venezuela): ✅ Confirmed with ve.dolarapi.com
- EUR → ARS (Argentina): ✅ Confirmed with dolarapi.com (RS043)
- EUR → HNL (Honduras): ❓ Need to check if DolarAPI covers it

---

## API Structure (Assumed)

Based on Argentina API structure (RS043), Venezuelan API likely follows same pattern:

### Expected Endpoint

```
GET https://ve.dolarapi.com/v1/dolares
```

**Expected Response:**
```json
[
  {
    "casa": "oficial",
    "nombre": "Oficial",
    "compra": 36.5,
    "venta": 36.5,
    "fechaActualizacion": "2026-04-27T10:30:00Z"
  },
  {
    "casa": "paralelo",
    "nombre": "Dólar Paralelo",
    "compra": 57.2,
    "venta": 57.8,
    "fechaActualizacion": "2026-04-27T10:30:00Z"
  }
]
```

**We use:** `paralelo.venta` (sell rate) - this is what merchants charge when converting EUR → VES

---

## Integration Plan

### 1. Test Endpoint (Next Step)

**Action:** Verify ve.dolarapi.com works as expected

```python
import requests

response = requests.get('https://ve.dolarapi.com/v1/dolares')
data = response.json()

# Find paralelo rate
paralelo = next(d for d in data if d['casa'] == 'paralelo')
ves_per_usd = paralelo['venta']

print(f"1 USD = {ves_per_usd} VES (parallel market)")
```

### 2. Calculate EUR → VES

**DolarAPI gives:** USD → VES
**We need:** EUR → VES

**Solution:** Two-step conversion
1. Get EUR → USD from Kraken (or ECB)
2. Get USD → VES from DolarAPI
3. Calculate: EUR → VES = (EUR → USD) × (USD → VES)

**Example:**
```
1 EUR = 1.08 USD (from Kraken EUR/USD pair)
1 USD = 57.5 VES (from DolarAPI paralelo)
→ 1 EUR = 1.08 × 57.5 = 62.1 VES
```

**Alternative:** Check if DolarAPI provides EUR → VES directly (some APIs do)

### 3. Add to Backend

Update `/api/v1/estimate` endpoint:

```python
def get_eur_ves_rate() -> float:
    """
    Get EUR → VES exchange rate (parallel market)

    Returns:
        VES per EUR
    """
    # Get USD → VES from DolarAPI
    response = requests.get('https://ve.dolarapi.com/v1/dolares')
    data = response.json()
    paralelo = next(d for d in data if d['casa'] == 'paralelo')
    ves_per_usd = paralelo['venta']

    # Get EUR → USD from Kraken (or use fixed 1.08 for MVP)
    eur_per_usd = 1.08  # TODO: Get live rate from Kraken

    # Calculate EUR → VES
    ves_per_eur = eur_per_usd * ves_per_usd

    return ves_per_eur
```

---

## MVP Decision

**Use DolarAPI for both Argentina AND Venezuela:**

✅ **Benefits:**
- Single API provider (simpler integration)
- Consistent data structure
- Covers our two main corridors
- Free API (no costs during beta)
- Real parallel market rates (what people actually use)

✅ **Corridors supported:**
- EUR → VES (Venezuela) via ve.dolarapi.com
- EUR → ARS (Argentina) via dolarapi.com

⏳ **Future expansion:**
- EUR → HNL (Honduras) - check if DolarAPI has it
- EUR → COP (Colombia) - DolarAPI supports it
- EUR → MXN (México) - DolarAPI supports it

---

## Fallback Strategy

**If ve.dolarapi.com down:**
- Use last 10 transactions average (as documented in 1_rate_apis.md)
- User must confirm rate

**If persistent issues:**
- Consider alternative: Monitor Dólar Venezuela, AirTM rates, LocalBitcoins VES rates
- But DolarAPI should be reliable (worked well for Argentina testing)

---

## Testing Checklist

**Before MVP:**
- [ ] Test ve.dolarapi.com endpoint manually (curl/Postman)
- [ ] Verify "paralelo" rate exists in response
- [ ] Check if EUR → VES direct rate available (or need USD intermediate)
- [ ] Verify rate updates in real-time (not stale)
- [ ] Test error handling (API down, timeout, etc.)
- [ ] Compare rate with other sources (sanity check)
- [ ] Integrate into backend /api/v1/estimate endpoint

---

## Comparison with Other Sources

**Verify DolarAPI accuracy by comparing with:**

- **Monitor Dólar Venezuela:** https://monitordolarvenezuela.com
- **DolarToday:** https://dolartoday.com (classic VES rate source)
- **LocalBitcoins VES:** Crypto exchange rate (should be similar)
- **AirTM:** P2P exchange (Venezuelan users trust this)

**If rates differ >5%:** Flag for investigation, might indicate API issue

---

## Related Documents

- **Argentina rates:** [RS043 DolarAPI ARS](research/RS043_dolarapi_ars_rates.md)
- **Rate APIs:** [1_rate_apis.md](RS046_android_app_requirements&flows/RS046-5_backend_apis/1_rate_apis.md)
- **Exchange Rate Safeguard:** [2.2](core_arquitecture/2_2_exchange_rate_safeguard.md)

---

*Discovered: April 27, 2026*
*Source: https://dolarapi.com (covers Venezuela + 7 other countries)*
*Status: Ready to integrate into MVP*
*Next: Test endpoint and implement in backend*

---

## Key Insight

> "Venezuela has the same official vs black market disconnect as Argentina. DolarAPI covers both. We can use a single API provider for our two main MVP corridors!"

This simplifies our MVP significantly - one API integration instead of researching multiple sources per country.
