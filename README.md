# Asgayapedia

**Official documentation for the Asgaya Protocol**

Bitcoin Cash remittances with <1% fees, no KYC, self-custody.

---

## 📖 Read the Documentation

**For humans:** [docs.asgaya.org](https://docs.asgaya.org/)  
**For AI:** [raw.githubusercontent.com/.../docs/index.md](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/index.md)

---

## 🎯 What is Asgaya?

A Bitcoin Cash wallet with a **peer-to-peer marketplace** to trade any fiat currency for BCH or BCH-native tokens (H€, HAu).

- **<1% total fees** - Trade fiat↔BCH cheaper than exchanges or remittance services
- **No KYC** - Permissionless participation
- **No custody** - Payment-first covenant model
- **Volatility protection** - H€ and HAu stability tokens via AnyHedge

**Use cases:** Remittances, merchant cash-out, cross-border payments, BCH liquidity provision.

**Initial testing:** EUR→VES corridor (Spain→Venezuela) chosen to validate the model in a challenging environment. Architecture works for any fiat currency pair.

---

## 📚 Documentation Structure

Visit [docs.asgaya.org](https://docs.asgaya.org/) to explore:

- **[implementation/](https://docs.asgaya.org/implementation/)** - Android app reference implementation (WebView hybrid architecture, connection patterns, transaction state management)
- **[the-mechanism/](https://docs.asgaya.org/the-mechanism/)** - The 5 gears and how they interact
- **[user-journeys/](https://docs.asgaya.org/user-journeys/)** - Sender, recipient, merchant, trader perspectives
- **[why-this-design/](https://docs.asgaya.org/why-this-design/)** - Design constraints and rationale
- **[unknowns/](https://docs.asgaya.org/unknowns/)** - 32 investigation briefs (research entry point)
- **[research/](https://docs.asgaya.org/research/)** - Research sessions and summaries

---

## 🔍 Current Status

**Phase:** Phase 0 — Active Implementation & Testnet3 Validation  
**Last Update:** August 21, 2026  
**Architecture:** Payment-first covenants + H€/HAu stability layer

**Implementation progress:**
- ✅ **Covenant v2.6** — All 5 spending paths (claim, merchantCashout, refund, abort, sellerRecoverBuffer) validated on testnet3
- ✅ **First inter-device claim** — Sender (Moto G06) → Recipient (Pixel 6a) guaranteed-value transfer proven (Aug 10, 2026)
- ✅ **v0.2 hybrid architecture** — Kotlin owns network, WebView does compute (build/sign). Eliminated the WebView connection-hang bug class; multi-device reliable
- ✅ **Oracle integration** — Dynamic pubkey fetching, zero hardcoded keys (Aug 16)
- ✅ **7 implementation components** (TightDS reviewed)
- ✅ **H€/HAu stability tokens** via 7-day AnyHedge contracts
- ✅ **Payment-first covenant architecture** (no seller capital risk)
- ✅ **32 structured unknowns** for Phase 0 validation
- ✅ **Documentation publicly accessible** at docs.asgaya.org

**Notable:** An Android reference implementation is under active development and being tested on testnet3 with real devices.

---

## 💬 Feedback Welcome

- **GitHub Issues:** [github.com/rufitnes/asgayapedia/issues](https://github.com/rufitnes/asgayapedia/issues)
- **Email:** rufitnes@proton.me

---

## 📜 License

- **Documentation:** [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)
- **Future Code:** MIT License (when implemented)

---

## 🙏 Acknowledgments

**Created by:** Rufitnes (Suso) - rufitnes@proton.me

**With contributions from:**
- Claude Sonnet 4.5 (Coordination) - Documentation structure, architecture refinement
- DeepSeek (TightDS) - Technical review, covenant architecture validation 

**Inspired by:** The Bitcoin Cash community's vision of peer-to-peer electronic cash for the world.

---

*Asgaya: Building permissionless financial access, one remittance at a time.* 🚀
