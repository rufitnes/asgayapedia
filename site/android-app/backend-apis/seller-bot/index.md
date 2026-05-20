# Becoming a BCH Seller

**Category:** Seller Operations (Decentralized)
**Priority:** 🟡 Optional (Phase 0 - only 1-2 sellers needed)
**Related:** [Covenant Creation](../covenant-creation.md), [NFT Scanner](../blockchain-scanner/nft-scanner.md)

---

## Overview

**This is NOT an Asgaya service.** This documentation is for **individuals who want to become BCH sellers** - providing liquidity by selling BCH to senders in exchange for EUR (Bizum/SEPA).

**What you'll run:**
1. **Seller bot** (smsbridge_loop.py) - Parses Bizum/SEPA notifications
2. **NFT manager** - Creates/updates your ASGAYA_SELLER_V1 NFT
3. **Covenant signer** - Releases BCH when payment verified

**What you'll earn:**
- 0.5% fee on every transaction (e.g., €0.50 on €100)
- Volume-based income (10 tx/day × €100 × 0.5% = €5/day = €150/month)

**What you'll need:**
- BCH float (0.5-2 BCH to start, ~€250-1000)
- EUR bank account (receive Bizum/SEPA payments)
- Server/device (pichan Raspberry Pi works great!)
- Technical skills (command line, basic scripting)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Seller Bot (You Run)                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. smsbridge_loop.py (Payment Monitoring)             │
│     - Monitors Bizum/SEPA notifications                │
│     - Parses payment amount + concept                  │
│     - Validates matches pending order                  │
│                                                         │
│  2. nft-manager.ts (Seller NFT)                        │
│     - Creates ASGAYA_SELLER_V1 NFT on-chain            │
│     - Updates availability (BCH balance, limits)       │
│     - Broadcasts to blockchain (visible in apps)       │
│                                                         │
│  3. covenant-signer.ts (BCH Release)                   │
│     - When payment verified → Signs covenant           │
│     - Releases BCH to buyer's sender covenant          │
│     - Updates NFT (new available balance)              │
│                                                         │
└─────────────────────────────────────────────────────────┘
                            ↓
                  BCH Blockchain (Public)
                            ↓
              Mobile Apps Query Your NFT
          (Discover you as available seller)
```

---

## Prerequisites

### 1. BCH Float

**How much BCH to start:**
- **Minimum:** 0.1 BCH (~€50 worth) - Handle 1-2 transactions
- **Recommended:** 0.5 BCH (~€250 worth) - Handle 5-10 transactions
- **Comfortable:** 2 BCH (~€1000 worth) - Handle 20+ transactions

**Where to get BCH:**
- Buy on exchange (Kraken, Binance, Coinbase)
- Transfer to your seller wallet
- Keep private keys secure (hardware wallet recommended)

---

### 2. EUR Bank Account

**Requirements:**
- **Spain:** Bizum-enabled bank account (most Spanish banks)
- **EU:** SEPA-enabled account (any EU bank)
- Ability to receive payments instantly

**Bizum limits:**
- Per transaction: €0.50 - €1,000
- Daily limit: Varies by bank (typically €1,000-2,000)
- Instant settlement (no chargebacks!)

---

### 3. Server/Device

**Options:**

**A. Raspberry Pi (Recommended for Phase 0)**
- Raspberry Pi 4/5 (4GB+ RAM)
- 64GB+ SD card
- Always-on, low power (~5W)
- Example: pichan setup (used in testing)

**B. VPS (Virtual Private Server)**
- DigitalOcean, Hetzner, OVH (~€5-10/month)
- Ubuntu 22.04 LTS
- 2GB RAM, 20GB storage

**C. Home Server**
- Any Linux machine (Ubuntu/Debian)
- Must be always-on (24/7)
- Stable internet connection

---

### 4. Technical Skills

**You should be comfortable with:**
- [ ] Command line (bash, ssh)
- [ ] Installing software (npm, python)
- [ ] Reading logs (troubleshooting)
- [ ] Git (cloning repositories)
- [ ] Basic security (SSH keys, firewalls)

**No need to know:**
- ❌ Advanced programming
- ❌ Blockchain development
- ❌ Cryptography

**We provide:** Scripts, documentation, and support

---

## Setup Guide

### Step 1: Install Dependencies

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Node.js 20+
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Install Python 3.11+
sudo apt install -y python3 python3-pip

# Install BCH Node (optional - can use public Electrum)
wget https://github.com/bitcoin-cash-node/bitcoin-cash-node/releases/download/v27.0.0/bitcoin-cash-node-27.0.0-aarch64-linux-gnu.tar.gz
tar -xzf bitcoin-cash-node-27.0.0-aarch64-linux-gnu.tar.gz
sudo cp -r bitcoin-cash-node-27.0.0/bin/* /usr/local/bin/

# Install CashScript SDK
npm install -g @cashscript/cashscript

# Verify installations
node --version  # Should be v20+
python3 --version  # Should be 3.11+
cashscript --version  # Should be latest
```

---

### Step 2: Clone Asgaya Seller Bot

```bash
# Clone repository (when public)
git clone https://github.com/asgaya/seller-bot.git
cd seller-bot

# Install dependencies
npm install
pip3 install -r requirements.txt
```

---

### Step 3: Configure Seller Bot

```bash
# Copy example config
cp config.example.json config.json

# Edit configuration
nano config.json
```

**config.json:**
```json
{
  "seller": {
    "name": "Seller#YourName",
    "wallet": {
      "seedPhrase": "YOUR_12_WORD_SEED_PHRASE",
      "derivationPath": "m/44'/145'/0'/0/0"
    },
    "bchFloat": 0.5,
    "limits": {
      "min": 10,
      "max": 500
    },
    "fee": 0.005,
    "paymentMethods": ["bizum", "sepa"]
  },
  "bizum": {
    "phoneNumber": "+34612345678",
    "smsBridge": {
      "enabled": true,
      "device": "/dev/ttyUSB0",
      "baudRate": 115200
    }
  },
  "sepa": {
    "iban": "ES1234567890123456789012",
    "bankApi": {
      "enabled": false,
      "provider": "plaid",
      "apiKey": "YOUR_API_KEY"
    }
  },
  "electrum": {
    "server": "fulcrum.fountainhead.cash",
    "port": 50002,
    "protocol": "ssl"
  },
  "nft": {
    "category": "ASGAYA_SELLER_V1",
    "updateInterval": 300
  }
}
```

**Key settings:**
- `seedPhrase`: Your BCH wallet seed (KEEP SECURE!)
- `bchFloat`: How much BCH to make available (0.5 = €250 worth)
- `limits`: Min/max EUR per transaction
- `fee`: Your fee in decimal (0.005 = 0.5%)
- `phoneNumber`: Your Bizum phone (for SMS parsing)

---

### Step 4: Create Seller NFT

```bash
# Generate seller NFT on-chain
npm run nft:create

# Output:
# ✓ Created ASGAYA_SELLER_V1 NFT
# TXID: abc123...
# Your seller ID: 03def456...
# Visible in mobile apps in ~10 seconds (1 confirmation)
```

**What this does:**
1. Derives BCH address from your seed phrase
2. Creates NFT with commitment (payment methods, limits, fee, contact)
3. Locks 0.5 BCH in SellerLiquidity covenant
4. Broadcasts transaction to blockchain
5. Mobile apps can now discover you!

**Cost:** ~€0.01 (transaction fee)

---

### Step 5: Start Seller Bot

```bash
# Start all services (screen/tmux recommended)
npm run start

# Or start individually:
npm run smsbridge &    # Payment monitoring
npm run nft-manager &  # NFT updates
npm run covenant &     # Covenant signing
```

**Console output:**
```
[SMS Bridge] Listening for Bizum notifications on +34612345678
[NFT Manager] Broadcasting seller availability (0.5 BCH, €10-500, 0.5% fee)
[Covenant Signer] Ready to process orders
[INFO] Seller bot online! 🟢
```

---

### Step 6: Test with First Transaction

**Simulate a test purchase:**

```bash
# Send yourself a test Bizum payment
# Concept: "ASGAYA_TEST_abc123"

# Check logs:
tail -f logs/smsbridge.log
```

**Expected log:**
```
[2026-05-16 10:30:00] SMS received from Bizum
[2026-05-16 10:30:01] Parsed: €100.00 - Concept: ASGAYA_TEST_abc123
[2026-05-16 10:30:01] No matching order - TEST payment
[2026-05-16 10:30:01] ✓ SMS bridge working correctly
```

**When real order comes in:**
```
[2026-05-16 10:45:00] SMS received from Bizum
[2026-05-16 10:45:01] Parsed: €100.00 - Concept: ASGAYA_ORDER_xyz789
[2026-05-16 10:45:01] ✓ Matched pending order xyz789
[2026-05-16 10:45:02] Creating sender covenant (€99.00 for Elena#142)
[2026-05-16 10:45:03] Signing covenant...
[2026-05-16 10:45:04] ✓ BCH released to sender covenant (0.198 BCH)
[2026-05-16 10:45:05] Updating NFT (0.302 BCH now available)
[2026-05-16 10:45:06] ✓ Order xyz789 completed! Earned €0.50
```

---

## Operations

### Managing BCH Float

**Check current float:**
```bash
npm run status

# Output:
# Seller Status:
# - Available BCH: 0.302 BCH (~€151)
# - Pending orders: 1 (€50 locked)
# - Completed today: 5 orders (€500 volume, €2.50 earned)
# - NFT status: 🟢 Online
```

**Refill float when low:**
```bash
# Transfer more BCH to seller wallet
# BCH Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy

# Once received, NFT auto-updates
# [NFT Manager] Detected +0.5 BCH deposit
# [NFT Manager] Updated availability: 0.802 BCH
```

**Withdraw earnings:**
```bash
# Withdraw excess BCH to cold storage
npm run withdraw --amount 0.3 --to "bitcoincash:qr5h8w9t..."

# Keeps minimum 0.5 BCH float active
# Withdraws 0.3 BCH to your address
# NFT updates automatically
```

---

### Updating Seller Settings

**Change fee:**
```bash
# Edit config.json
nano config.json
# Change: "fee": 0.004  (0.4% instead of 0.5%)

# Restart NFT manager
npm run nft:update

# New fee visible in mobile apps within 30s
```

**Change limits:**
```bash
# Edit config.json
nano config.json
# Change: "limits": { "min": 20, "max": 300 }

# Update NFT
npm run nft:update
```

**Go offline temporarily:**
```bash
# Spend your seller NFT UTXO (withdraws all BCH)
npm run offline

# Mobile apps see: 🔴 Out of stock
# To go back online: npm run nft:create (recreate NFT)
```

---

### Monitoring Performance

**Daily stats:**
```bash
npm run stats --period today

# Output:
# Today's Stats (2026-05-16):
# - Orders completed: 8
# - Volume: €780.00
# - Fees earned: €3.90
# - Average order: €97.50
# - Busiest hour: 14:00-15:00 (3 orders)
```

**Weekly report:**
```bash
npm run stats --period week

# Output:
# This Week (May 12-18):
# - Orders: 47
# - Volume: €4,150
# - Fees: €20.75
# - Avg daily: 6.7 orders
# - Top sender: Iris (12 orders, €1,200 volume)
```

---

## Troubleshooting

### Common Issues

#### 1. SMS Bridge Not Receiving Notifications

**Problem:** smsbridge_loop.py not parsing Bizum SMS

**Solutions:**
```bash
# Check SMS device connection
ls /dev/ttyUSB*  # Should show /dev/ttyUSB0

# Test SMS reception manually
python3 smsbridge_loop.py --test

# Check Bizum SMS format
# Expected: "Has recibido 100,00€ de ASGAYA_ORDER_xyz789"
```

---

#### 2. NFT Not Visible in Mobile Apps

**Problem:** Mobile apps don't show your seller listing

**Debug:**
```bash
# Check NFT exists on-chain
npm run nft:verify

# Query via Electrum
electrum-cli --server fulcrum.fountainhead.cash:50002:s \
  blockchain.tokeninfo.utxos_by_category ASGAYA_SELLER_V1

# Should show your NFT with correct commitment
```

**Common causes:**
- NFT UTXO spent (went offline accidentally)
- NFT commitment malformed (recreate with `npm run nft:create`)
- Mobile app cache (wait 30s for cache refresh)

---

#### 3. Covenant Signing Fails

**Problem:** Can't release BCH after payment verified

**Debug:**
```bash
# Check wallet balance
cashscript wallet balance --network mainnet

# Check pending covenants
npm run covenant:list

# Manually sign specific covenant
npm run covenant:sign --order xyz789
```

**Common causes:**
- Insufficient BCH in wallet (refill float)
- Covenant already signed (check blockchain)
- Network congestion (retry after 10min)

---

### Logs & Debugging

**Check logs:**
```bash
# Real-time logs
tail -f logs/seller-bot.log

# SMS bridge specific
tail -f logs/smsbridge.log

# NFT manager specific
tail -f logs/nft-manager.log

# Covenant signer specific
tail -f logs/covenant-signer.log
```

**Enable debug mode:**
```bash
# Edit config.json
"debug": true

# Restart services
npm run restart
```

---

## Security Best Practices

### 1. Protect Your Seed Phrase

**DO:**
- ✅ Store seed phrase offline (paper, metal backup)
- ✅ Encrypt config.json (`gpg -c config.json`)
- ✅ Use hardware wallet (Ledger/Trezor) for large floats
- ✅ Limit float size (only keep what you need online)

**DON'T:**
- ❌ Share seed phrase with anyone
- ❌ Store seed in cloud (Dropbox, Google Drive)
- ❌ Reuse seed from other wallets
- ❌ Keep large amounts on hot wallet

---

### 2. Server Security

**Firewall setup:**
```bash
# Allow only necessary ports
sudo ufw allow ssh
sudo ufw allow 50002/tcp  # Electrum (if running local node)
sudo ufw enable
```

**SSH hardening:**
```bash
# Disable password auth (keys only)
sudo nano /etc/ssh/sshd_config
# Set: PasswordAuthentication no
sudo systemctl restart sshd
```

**Auto-updates:**
```bash
# Enable automatic security updates
sudo apt install unattended-upgrades
sudo dpkg-reconfigure -plow unattended-upgrades
```

---

### 3. Bizum/SEPA Security

**Monitor for fraud:**
- Check all payments match expected orders
- Flag suspicious patterns (same sender, rapid orders)
- Keep logs for 90 days (dispute resolution)

**Chargeback protection:**
- Bizum: No chargebacks (instant settlement)
- SEPA: 8-week chargeback window (higher risk)
- Recommend Bizum only for Phase 0

---

## Economics & Profitability

### Revenue Model

**Per transaction:**
```
Sender pays: €100.50 (€100 + 0.5% fee)
You receive: €100.50 (Bizum instant)
You send: €100.00 worth of BCH (0.198 BCH @ €505/BCH)
You keep: €0.50 (0.5% fee)
```

**Daily estimates:**
```
5 orders/day × €100 avg × 0.5% fee = €2.50/day = €75/month
10 orders/day × €100 avg × 0.5% fee = €5/day = €150/month
20 orders/day × €100 avg × 0.5% fee = €10/day = €300/month
```

---

### Costs

**Fixed costs:**
- Server: €0 (Raspberry Pi) or €5-10/month (VPS)
- BCH float: €0 (no opportunity cost if selling anyway)
- Electricity: ~€2/month (Raspberry Pi)

**Variable costs:**
- BCH network fees: ~€0.01 per transaction (negligible)
- NFT updates: ~€0.01 every 5 minutes (when updating availability)

**Total monthly cost:** ~€2-12 depending on setup

---

### Break-Even Analysis

**Minimum to break even:**
```
Fixed costs: €10/month
Break-even: €10 / €0.50 per tx = 20 transactions/month
= ~0.7 transactions/day
```

**Realistic profitability:**
```
Phase 0 (1-2 senders): 5 tx/day = €75/month - €10 costs = €65 profit
Phase 1 (10+ senders): 20 tx/day = €300/month - €10 costs = €290 profit
Phase 2 (100+ senders): 50 tx/day = €750/month - €10 costs = €740 profit
```

---

## Advanced Features (Phase 1+)

### Dynamic Pricing

**Adjust fee based on demand:**
```javascript
// config.json
"dynamicPricing": {
  "enabled": true,
  "lowDemand": 0.004,   // 0.4% when few orders
  "highDemand": 0.006,  // 0.6% when busy
  "threshold": 10       // Switch at 10 orders/hour
}
```

---

### Multi-Currency Support

**Accept multiple payment methods:**
```javascript
// config.json
"paymentMethods": [
  {
    "type": "bizum",
    "currency": "EUR",
    "limits": { "min": 10, "max": 500 }
  },
  {
    "type": "sepa",
    "currency": "EUR",
    "limits": { "min": 50, "max": 2000 }
  },
  {
    "type": "revolut",
    "currency": "GBP",
    "limits": { "min": 10, "max": 400 }
  }
]
```

---

### Automated Float Management

**Auto-refill from exchange:**
```javascript
// config.json
"autoRefill": {
  "enabled": true,
  "minBalance": 0.3,     // Refill when below 0.3 BCH
  "targetBalance": 1.0,  // Buy up to 1.0 BCH
  "exchange": "kraken",
  "apiKey": "YOUR_API_KEY"
}
```

---

## FAQ

**Q: Do I need to register with Asgaya?**  
A: No! Anyone can become a seller. Just create an NFT and start accepting orders.

**Q: What if I run out of BCH?**  
A: Your NFT shows "Out of stock" (value = 0). Mobile apps won't show you. Refill BCH to go back online.

**Q: Can I charge more than 0.5% fee?**  
A: Yes, you set your own fee. But senders will choose cheaper sellers. Market finds equilibrium.

**Q: What if payment arrives but I don't have BCH?**  
A: Don't accept orders larger than your float! Bot rejects orders exceeding available BCH.

**Q: Is this legal?**  
A: You're selling your own BCH for EUR (peer-to-peer). Check your local regulations. Most jurisdictions: legal if occasional/non-commercial.

**Q: What about taxes?**  
A: Trading BCH for fiat may be taxable (capital gains). Keep records. Consult tax advisor.

**Q: Can I stop anytime?**  
A: Yes! Just run `npm run offline` and withdraw your BCH. No commitments.

**Q: Do I need to be online 24/7?**  
A: Recommended for best visibility, but not required. Your NFT shows "Offline" when bot is down.

---

## Support & Community

**Need help?**
- GitHub Issues: https://github.com/asgaya/seller-bot/issues
- Telegram: t.me/asgaya_sellers
- Email: sellers@asgaya.org

**Share your setup:**
- Post your stats on Twitter: #AsgayaSeller
- Help other sellers in Telegram
- Contribute improvements to GitHub

---

## Related Documentation

- **Covenant Creation:** [covenant-creation.md](../covenant-creation.md) - How seller covenants work
- **NFT Scanner:** [blockchain-scanner/nft-scanner.md](../blockchain-scanner/nft-scanner.md) - How apps discover you
- **Phase 0 Setup:** [pichan-regtest-setup.md](../pichan-regtest-setup.md) - Testing on regtest
- **Architecture:** [bch-native-architecture.md](../bch-native-architecture.md) - Pull system design

---

*Created: May 16, 2026*  
*Audience: Individuals wanting to become BCH sellers (NOT an Asgaya service)*  
*Philosophy: Permissionless, decentralized, profit from BCH liquidity provision*  
*Phase 0: Manual setup, 1-2 sellers sufficient to prove model*  
*Phase 1+: Automated tooling, dynamic pricing, multi-currency*

---

**Ready to start? Follow the setup guide above! 🚀**
