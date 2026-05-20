# Research Session RS016: EUR/BCH Exchanges – Backup Liquidity for the Escrow

**Date:** April 1, 2026  
**Participants:** Suso (Founder), DeepSeek (Main Research Assistant)  
**Context:** The escrow needs a reliable source of EUR/BCH liquidity to convert sender fiat to BCH instantly. While the ideal is to use decentralized liquidity (from a network of liquidity providers), a centralized exchange serves as a **backup plan** in case DEX participation is insufficient. This session researches exchanges that support the EUR/BCH pair, with a focus on transparency, automation, cost, and volume.

---

## 1. Objective

Identify the exchange(s) that best serve as a backup liquidity source for the escrow, based on the following criteria in order of importance:

1. **BCH address transparency / proof of reserves** – The exchange must demonstrate that it holds real BCH. This is critical because the escrow will buy BCH on the exchange and withdraw it to merchants. If the exchange operates on fractional reserves or paper trading, the escrow’s BCH purchases could be compromised.
2. **Full automation** – The exchange must offer an API that allows:
   - Balance queries
   - Market/limit orders (buy/sell BCH)
   - Withdrawal of BCH to external addresses
3. **Cost per trade** – Low trading fees and low BCH withdrawal fees.
4. **EUR/BCH volume** – Sufficient liquidity to execute orders without significant slippage.

Automated fiat withdrawal (moving EUR from the exchange to a bank account) is **not required** for the core escrow operation; it can be handled manually.

---

## 2. Exchange Comparison

### 2.1 Kraken

| Aspect | Details |
|--------|---------|
| **BCH transparency** | Kraken publishes **audited proof of reserves** for major assets, including BCH. Third‑party attestations confirm client assets are held 1:1. This meets the highest transparency standard. |
| **Full automation** | Yes, via REST and WebSocket APIs. Supports order placement, balance queries, and BCH withdrawals. API keys can be IP‑whitelisted and restricted to specific permissions. |
| **Cost per trade** | Trading fees: 0.16% maker / 0.26% taker (for low volume). BCH withdrawal fee: 0.0001 BCH (very low). |
| **EUR/BCH volume** | Consistently high, often €173–212 million in 24h. Deep liquidity. |
| **Verdict** | Excellent transparency, automation, and fees. Top candidate. |

### 2.2 Coinbase (Advanced Trade)

| Aspect | Details |
|--------|---------|
| **BCH transparency** | Coinbase does not provide audited proof of reserves for individual assets. As a regulated public company, they claim to hold client assets 1:1, but there is no cryptographic proof. |
| **Full automation** | Yes. Coinbase Advanced Trade API supports trading, balance, and withdrawals. |
| **Cost per trade** | Trading fees: 0.4% – 0.6% for lower tiers. BCH withdrawal fee: 0.0002 BCH. |
| **EUR/BCH volume** | ~€338–581 million 24h (very liquid). |
| **Verdict** | Good automation and volume, but higher fees and less transparency. A solid backup if Kraken is unavailable. |

### 2.3 Bitstamp

| Aspect | Details |
|--------|---------|
| **BCH transparency** | Bitstamp is a regulated European exchange but does not publish proof of reserves for BCH. Their terms state they hold client assets 1:1, but no public cryptographic proof. |
| **Full automation** | Yes. Classic REST API supports trading, balance, and withdrawals. |
| **Cost per trade** | Trading fees: 0.2% – 0.5% (volume‑based). BCH withdrawal fee: 0.0002 BCH. |
| **EUR/BCH volume** | ~€338 million 24h. Solid. |
| **Verdict** | Acceptable but lacks the transparency of Kraken. |

### 2.4 Binance

| Aspect | Details |
|--------|---------|
| **BCH transparency** | Binance has published proof of reserves for some assets, but historically the methodology has been less rigorous and the BCH component is not always clearly separated. Controversies around reserves reduce trust. |
| **Full automation** | Yes. Binance API is powerful and supports all needed operations. |
| **Cost per trade** | Very low: 0.1% spot trading. BCH withdrawal fee: 0.0005 BCH. |
| **EUR/BCH volume** | Direct BCH/EUR liquidity is lower; most volume is in BCH/USDT, requiring a stablecoin step. |
| **Verdict** | Low fees but transparency concerns and stablecoin dependency make it less suitable. |

---

## 3. Recommended Backup Exchange: Kraken

Based on the evaluation, **Kraken** is the optimal backup liquidity source for the escrow. Its **proof of reserves** ensures that the BCH bought is real and withdrawable, which is critical for the escrow’s safety. The API supports full automation, and fees are competitive.

**Alternative:** Coinbase Advanced Trade can serve as a secondary backup if Kraken’s account opening or operational constraints become an issue.

---

## 4. Requirements for the Escrow to Use the Exchange

For the escrow to use a centralized exchange as a backup, the following must be in place:

| Requirement | Details |
|-------------|---------|
| **Business account** | The escrow must operate as a legal entity and open a verified business account on the exchange. |
| **API key with proper permissions** | Key must allow trading, balance queries, and BCH withdrawals. IP whitelisting is recommended. |
| **Whitelisted withdrawal addresses** | Merchant BCH addresses must be pre‑approved on the exchange to prevent fraud. This can be done via the exchange’s “withdrawal address management” feature. |
| **Sufficient EUR balance** | The escrow’s exchange account must hold enough EUR to cover the maximum expected single transaction. This can be funded from the escrow’s bank account. |
| **Monitoring & fallback** | The escrow must monitor API failures and have a manual fallback procedure in case the exchange is down. |

---

## 5. Next Steps

1. **Open a Kraken business account** and complete verification.
2. **Set up API keys** with trading, balance, and withdrawal permissions; enable IP whitelisting.
3. **Add merchant BCH addresses** to the whitelist via the exchange’s withdrawal address management.
4. **Write a test script** (Python) to:
   - Query balance
   - Place a market buy order for a small amount of BCH
   - Withdraw that BCH to a test address
5. **Document the script** as a skill for future use.
6. **Decide on the buffer management strategy** – whether to keep a EUR buffer on the exchange or transfer funds on‑demand.

---

## 6. Conclusion

Kraken provides the most reliable backup liquidity for the escrow due to its transparency, automation capabilities, and competitive fees. While the preferred path is a decentralized liquidity network, having a centralized exchange backup ensures the protocol can continue operating even if DEX liquidity is insufficient. The next step is to implement the API integration and test the flow in a sandbox environment.

*Prepared by DeepSeek, Main Research Assistant, April 1, 2026*
