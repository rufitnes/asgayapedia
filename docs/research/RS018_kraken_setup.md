

# Research Session RS018: Kraken – Account Setup & API Key Generation for Escrow Automation

**Date:** April 2, 2026  
**Participants:** Suso (Founder), DeepSeek (Main Research Assistant)  
**Context:** Following the decision to use Kraken as a backup liquidity source for the escrow (see RS016), we need a practical guide to create a Kraken account, obtain API credentials, and configure the minimal permissions required for automated monitoring (`Query`). This session documents the exact steps, including the API secret format and security considerations.

---

## 1. Kraken Account Creation

1. **Go to Kraken.com** and click “Sign Up”.
2. Choose **Personal** account type (for testing; a Business account may be required for production).
3. Complete verification to reach at least **Intermediate** level (requires ID and proof of residence). This level unlocks API access and unlimited BCH withdrawals.
4. Enable **Two‑Factor Authentication (2FA)** – required for API key creation and withdrawals.

---

## 2. Generating an API Key with `Query` Permission

After logging in:

1. Navigate to **User Settings** → **Settings** → **Conections & API** (or directly `https://www.kraken.com/settings/api`).
2. Click **“Create API key”**.
3. Give the key a descriptive name, e.g., `escrow_query_monitor`.
4. Under **Permissions**, select only:
   - **Funds** → `Query`
   - *Do not select* `Deposit`, `Withdraw`, or `Earn`.
   - *Do not select* any permissions under **Orders & trades** or **Data** for a pure read‑only key.
5. (Optional) Restrict to a specific IP address if your escrow server has a static IP.
6. Click **“Create key”** and complete the 2FA challenge.

> **Important:** After creation, Kraken displays the **API Key** and **API Secret**.  
> The **API Secret** is a Base64‑encoded string, e.g.  
> `s3o80MmgPebFWAIrv5+ExcQc48UTpoFfQtCKVPgMp7VB6p+XObWmF0EMPaXuMBQ0iPs2C/fKmDYZeXRrrLxwrA==`  
> Copy it exactly. **Kraken will never show it again.**

---

## 3. Understanding the API Secret Format

In Python, the secret must be stored as a **bytes literal** (with a `b` prefix):

```python
api_secret = b"s3o80MmgPebFWAIrv5+ExcQc48UTpoFfQtCKVPgMp7VB6p+XObWmF0EMPaXuMBQ0iPs2C/fKmDYZeXRrrLxwrA=="

The script uses base64.b64decode(api_secret) to obtain the raw bytes required for HMAC‑SHA512 signing. If the secret is stored as a string, you must encode it: api_secret.encode().
4. Security Best Practices
Practice	Rationale
Never hardcode credentials in source files	Use environment variables or a config file excluded from version control.
Use read‑only permissions (Query) for monitoring	Even if the key leaks, funds cannot be moved.
Enable IP whitelisting	Only allow the escrow server’s IP to use the key.
Rotate keys periodically	Create a new key and delete the old one after a few months.
Store the API Secret in a password manager	It cannot be recovered from Kraken.
5. Verifying the Key with a Simple Request

After creating the key, test it with the Balance endpoint (see RS018). If successful, you will receive a JSON response containing your account balances.
6. Next Steps

    Test the Query permission (RS018).

    Later, create a separate API key with Withdraw and Create & modify orders for the actual trading/withdrawal logic (but only for a dedicated hot wallet with minimal funds).

7. Conclusion

A Kraken Intermediate account with a read‑only API key (Query) provides a safe way for the escrow to monitor balances without any risk of moving funds. This is the foundation for building automated backup liquidity.

Prepared by DeepSeek, Main Research Assistant, April 2, 2026
