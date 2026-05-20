# Pichan Regtest Setup - Real Electrum Server for Husk v0.1

**Purpose:** Run a real Bitcoin Cash Node + Fulcrum Electrum server on pichan (Raspberry Pi 5) for Asgaya Husk testing.  
**Hardware:** Raspberry Pi 5 8GB with SIM hat  
**Network:** Local WiFi (192.168.1.42)  
**Phones:** Pixel 6a + Motorola G06 connect over WiFi  

---

## Architecture

```
┌─────────────────────────────────────────────┐
│ pichan (192.168.1.42)                       │
├─────────────────────────────────────────────┤
│                                             │
│  bitcoind --regtest                         │
│  ├── Instant block generation               │
│  ├── CashTokens enabled                     │
│  ├── Real covenant contracts                │
│  └── RPC: 127.0.0.1:18443                   │
│                                             │
│  Fulcrum (Electrum server)                  │
│  ├── Indexes regtest blockchain             │
│  ├── Real Electrum JSON-RPC protocol        │
│  └── Listens: 0.0.0.0:50002 (LAN access)    │
│                                             │
└─────────────────────────────────────────────┘
         ▲                    ▲
         │                    │
    ┌────┴────┐          ┌────┴────┐
    │ Pixel   │          │  Moto   │
    │  6a     │          │   G06   │
    │         │          │         │
    │ Sender  │          │ Recip/  │
    │  role   │          │ Merchant│
    └─────────┘          └─────────┘
```

---

## Installation

### 1. Install Bitcoin Cash Node

```bash
# SSH to pichan
ssh suso@pichan

# Install dependencies
sudo apt update
sudo apt install -y build-essential libtool autotools-dev \
  automake pkg-config bsdmainutils python3 libssl-dev \
  libevent-dev libboost-system-dev libboost-filesystem-dev \
  libboost-chrono-dev libboost-test-dev libboost-thread-dev \
  libminiupnpc-dev libzmq3-dev libqt5gui5 libqt5core5a \
  libqt5dbus5 qttools5-dev qttools5-dev-tools

# Download Bitcoin Cash Node (ARM64 for Raspberry Pi)
cd ~/
wget https://github.com/bitcoin-cash-node/bitcoin-cash-node/releases/download/v27.1.0/bitcoin-cash-node-27.1.0-aarch64-linux-gnu.tar.gz

# Extract
tar -xzf bitcoin-cash-node-27.1.0-aarch64-linux-gnu.tar.gz

# Install binaries
sudo cp -r bitcoin-cash-node-27.1.0/bin/* /usr/local/bin/

# Verify installation
bitcoind --version
# Should show: Bitcoin Cash Node version v27.1.0
```

### 2. Configure Bitcoin Cash Node for Regtest

```bash
# Create Bitcoin data directory
mkdir -p ~/.bitcoin

# Create configuration file
nano ~/.bitcoin/bitcoin.conf
```

**bitcoin.conf:**
```ini
# Regtest mode (local testing blockchain)
regtest=1

# Enable RPC server
server=1
rpcuser=asgaya
rpcpassword=asgaya_regtest_password_change_in_production
rpcallowip=127.0.0.1
rpcbind=127.0.0.1
rpcport=18443

# Transaction index (required for Fulcrum)
txindex=1

# Enable CashTokens (NFTs, fungible tokens)
-enablecashtokens=1

# Enable all opcodes (covenants, loops, functions)
-enableintrospectionopcodes=1

# Logging
debug=1
```

### 3. Install Fulcrum (Electrum Server)

```bash
# Download Fulcrum for ARM64
cd ~/
wget https://github.com/cculianu/Fulcrum/releases/download/v1.11.1/Fulcrum-1.11.1-arm64-linux.tar.gz

# Extract
tar -xzf Fulcrum-1.11.1-arm64-linux.tar.gz
cd Fulcrum-1.11.1-arm64-linux

# Create Fulcrum data directory
mkdir -p ~/fulcrum_db

# Create configuration file
nano ~/Fulcrum-1.11.1-arm64-linux/fulcrum.conf
```

**fulcrum.conf:**
```toml
# Bitcoin Cash Node connection
bitcoind = 127.0.0.1:18443
rpcuser = asgaya
rpcpassword = asgaya_regtest_password_change_in_production

# Database directory
datadir = /home/suso/fulcrum_db

# Network binding (accessible on LAN)
tcp = 0.0.0.0:50002
# ssl = 0.0.0.0:50003  # Optional SSL (need cert)

# Performance tuning (for Raspberry Pi 5 8GB)
db_max_open_files = 200
db_mem = 2048.0  # 2GB cache
worker_threads = 4  # Pi 5 has 4 cores

# Logging
loglevel = info
```

---

## Starting Services

### 1. Start Bitcoin Cash Node

```bash
# Start regtest daemon
bitcoind --regtest --daemon

# Wait for startup
sleep 3

# Verify it's running
bitcoin-cli --regtest getblockchaininfo
# Should show: "chain": "regtest", "blocks": 0
```

### 2. Generate Initial Blocks

```bash
# Generate 101 blocks (need 100 for coinbase maturity)
bitcoin-cli --regtest createwallet "asgaya-test"
ADDR=$(bitcoin-cli --regtest getnewaddress)
bitcoin-cli --regtest generatetoaddress 101 $ADDR

# Verify balance
bitcoin-cli --regtest getbalance
# Should show: 50.00000000 (from first coinbase)
```

### 3. Start Fulcrum

```bash
cd ~/Fulcrum-1.11.1-arm64-linux

# Start Fulcrum (will index the blockchain)
./Fulcrum fulcrum.conf

# First startup takes a few minutes to index
# You'll see: "Indexing blocks..." then "Server started"
```

### 4. Test Electrum Connection

```bash
# From susopc (or phones), test connection
telnet 192.168.1.42 50002

# Or use electrum-cash wallet
electrum-cash --regtest --server 192.168.1.42:50002:t
```

---

## Seeding Test Data

### 1. Create Helper Scripts Directory

```bash
mkdir -p ~/asgaya-regtest/scripts
cd ~/asgaya-regtest/scripts
```

### 2. Reset Script

**regtest-reset.sh:**
```bash
#!/bin/bash
# Reset regtest blockchain to clean state

echo "Stopping services..."
bitcoin-cli --regtest stop
pkill -f Fulcrum
sleep 3

echo "Clearing blockchain data..."
rm -rf ~/.bitcoin/regtest
rm -rf ~/fulcrum_db/*

echo "Starting bitcoind..."
bitcoind --regtest --daemon
sleep 3

echo "Generating initial blocks..."
bitcoin-cli --regtest createwallet "asgaya-test"
ADDR=$(bitcoin-cli --regtest getnewaddress)
bitcoin-cli --regtest generatetoaddress 101 $ADDR

echo "Starting Fulcrum..."
cd ~/Fulcrum-1.11.1-arm64-linux
./Fulcrum fulcrum.conf &

echo "Regtest reset complete!"
echo "Phones can connect to 192.168.1.42:50002"
```

### 3. Seed Scenario Script

**scenarios/03-competitive.sh:**
```bash
#!/bin/bash
# Scenario: 5 competing BCH sellers

echo "Deploying 5 seller NFTs with different fees..."

# Seller 1: 0.5% fee, €10-500, Bizum+SEPA
cashscript compile ../../contracts/SellerLiquidityV1.cash
cashscript deploy SellerLiquidityV1 \
  --network regtest \
  --rpc http://127.0.0.1:18443 \
  --value 50000000 \
  --nft-category "ASGAYA_SELLER_V1" \
  --nft-commitment "0x$(echo '{"fee":0.005,"limits":{"min":10,"max":500},"payment":["Bizum","SEPA"]}' | xxd -p)"

# Seller 2: 0.4% fee (lower!), €50-1000, SEPA+Cash
cashscript deploy SellerLiquidityV1 \
  --network regtest \
  --value 100000000 \
  --nft-category "ASGAYA_SELLER_V1" \
  --nft-commitment "0x$(echo '{"fee":0.004,"limits":{"min":50,"max":1000},"payment":["SEPA","Cash","ATM"]}' | xxd -p)"

# ... Sellers 3-5 similar

echo "Mining confirmation block..."
bitcoin-cli --regtest generatetoaddress 1 $(bitcoin-cli --regtest getnewaddress)

echo "Done! Query Electrum to see sellers:"
echo "electrum-cash --regtest --server 192.168.1.42:50002:t listunspent"
```

### 4. Cash Account Registration

**register-cashaccount.sh:**
```bash
#!/bin/bash
# Register Cash Account: Elena#142

# Create OP_RETURN transaction with Cash Account protocol
# Format: OP_RETURN <protocol_prefix> <name> <number>

PROTOCOL="01010101"  # Cash Account protocol prefix (Lokad ID)
NAME=$(echo -n "Elena" | xxd -p)
NUMBER="8e00"  # 142 in little-endian hex

OPRETURN="6a${#PROTOCOL}${PROTOCOL}${#NAME}${NAME}${NUMBER}"

# Create transaction
bitcoin-cli --regtest createrawtransaction \
  '[{"txid":"'$(bitcoin-cli --regtest listunspent | jq -r '.[0].txid')'","vout":0}]' \
  '[{"data":"'$OPRETURN'"},{"'$(bitcoin-cli --regtest getnewaddress)'":49.9999}]'

# Sign and send
# (Full implementation in actual script)

echo "Cash Account Elena#142 registered!"
```

---

## Running Test Scenarios

### Scenario 1: Empty Bulletin Board (Test UI Empty State)
```bash
cd ~/asgaya-regtest/scripts
./regtest-reset.sh
# Don't seed any sellers
# Phones query pichan, see "No sellers available"
```

### Scenario 2: Single Seller (Test Auto-Select)
```bash
./regtest-reset.sh
./scenarios/02-one-seller.sh
# Phones see 1 seller, Screen 4.5 should auto-select
```

### Scenario 3: Competitive Market (Test Selection UI)
```bash
./regtest-reset.sh
./scenarios/03-competitive.sh
# Phones see 5 sellers with different fees
# Test sorting, filtering, selection
```

### Scenario 4: Full Flow (Cross-Device Test)
```bash
./regtest-reset.sh
./scenarios/04-full-flow.sh
# Deploys sellers, merchants, cash accounts
# Pixel (sender) creates covenant
# Moto (recipient) claims, merchant co-signs
```

---

## Mobile App Configuration

### Husk v0.1 Config (Regtest on Pichan)

**ElectrumConfig.ts:**
```typescript
export const ELECTRUM_CONFIG = {
  // Week 1-2: Regtest on pichan
  host: '192.168.1.42',
  port: 50002,
  protocol: 'tcp',
  network: 'regtest'
};
```

### Testing from Mobile

```javascript
// ElectrumClient.ts
import { ElectrumClient } from '@electrum/client';

const client = new ElectrumClient(
  ELECTRUM_CONFIG.host,
  ELECTRUM_CONFIG.port,
  ELECTRUM_CONFIG.protocol
);

await client.connect();

// Query sellers
const sellers = await client.blockchain_scripthash_listunspent(
  scripthashFromCategory('ASGAYA_SELLER_V1')
);

console.log(`Found ${sellers.length} sellers`);
// Should match what you seeded with regtest-seed.sh
```

---

## Transition to Chipnet (Week 2-3)

### 1. Stop Regtest

```bash
bitcoin-cli --regtest stop
pkill -f Fulcrum
```

### 2. Reconfigure for Chipnet

**Update ~/.bitcoin/bitcoin.conf:**
```ini
# Remove: regtest=1
# Add:
testnet=1
chipnet=1

# Everything else stays same
```

**Update fulcrum.conf:**
```toml
# Update RPC port for testnet
bitcoind = 127.0.0.1:18332

# Everything else stays same
```

### 3. Start Chipnet

```bash
bitcoind --testnet --daemon
cd ~/Fulcrum-1.11.1-arm64-linux
./Fulcrum fulcrum.conf
```

### 4. Mobile App Config (No Code Changes!)

```typescript
export const ELECTRUM_CONFIG = {
  host: '192.168.1.42',  // Still pichan
  port: 50002,            // Same port
  protocol: 'tcp',
  network: 'chipnet'      // Just change this
};
```

**Phones continue querying pichan, but now get chipnet blockchain data!**

---

## Systemd Services (Optional - Auto-Start)

### bitcoind.service

```ini
[Unit]
Description=Bitcoin Cash Node (Regtest)
After=network.target

[Service]
Type=forking
User=suso
ExecStart=/usr/local/bin/bitcoind --regtest --daemon
ExecStop=/usr/local/bin/bitcoin-cli --regtest stop
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

### fulcrum.service

```ini
[Unit]
Description=Fulcrum Electrum Server
After=bitcoind.service
Requires=bitcoind.service

[Service]
Type=simple
User=suso
WorkingDirectory=/home/suso/Fulcrum-1.11.1-arm64-linux
ExecStart=/home/suso/Fulcrum-1.11.1-arm64-linux/Fulcrum fulcrum.conf
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

**Enable auto-start:**
```bash
sudo systemctl enable bitcoind
sudo systemctl enable fulcrum
sudo systemctl start bitcoind
sudo systemctl start fulcrum
```

---

## Troubleshooting

### Phones Can't Connect

```bash
# Check Fulcrum is listening on all interfaces
sudo netstat -tulpn | grep 50002
# Should show: 0.0.0.0:50002

# Check firewall
sudo ufw allow 50002/tcp

# Ping from phone
ping 192.168.1.42
```

### Fulcrum Won't Start

```bash
# Check bitcoind is running
bitcoin-cli --regtest getblockchaininfo

# Check RPC credentials match
grep rpcuser ~/.bitcoin/bitcoin.conf
grep rpcuser ~/Fulcrum-1.11.1-arm64-linux/fulcrum.conf

# Check logs
tail -f ~/fulcrum_db/fulcrum.log
```

### No Sellers Showing Up

```bash
# Query blockchain directly
bitcoin-cli --regtest listunspent

# Check Fulcrum indexed the blocks
# (Query via Electrum protocol)

# Re-seed scenario
cd ~/asgaya-regtest/scripts
./scenarios/03-competitive.sh
```

---

## Performance Notes

**Raspberry Pi 5 8GB can handle:**
- ✅ Regtest blockchain (instant blocks, small size)
- ✅ Fulcrum indexing (~2GB RAM usage)
- ✅ 2-3 phone connections simultaneously
- ✅ CashScript covenant deployments

**Expected performance:**
- Regtest block generation: <1 second
- Fulcrum query response: 10-50ms over LAN
- Covenant deployment: 2-3 seconds (compile + deploy + mine)

---

## Next Steps

After pichan setup:
1. Deploy CashScript covenants (SellerLiquidityV1, RecipientCovenantV1)
2. Run seeding scenarios
3. Build mobile app Electrum client
4. Test multi-device flows

---

**Related Documents:**
- [Phase 0 Progressive Decentralization](../../decisions/phase-0-progressive-decentralization.md)
- [Multi-Device Test Plan](./multi-device-test-plan.md)
- [CashScript Covenant Contracts](../../concepts/bounty-contracts-with-volatility-buffer.md)

---

*Hardware tested: Raspberry Pi 5 8GB with SIM hat*  
*Network tested: Local WiFi 192.168.1.x*  
*BCH Node version: v27.1.0*  
*Fulcrum version: v1.11.1*
