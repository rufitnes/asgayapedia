# Asgayapedia

**Official documentation for the Asgaya Protocol**

Asgaya is an open-source, permissionless remittance protocol that enables cross-border payments at <1% total cost using Bitcoin Cash as settlement layer.

---

## 📖 Read the Documentation

**Live site:** [https://rufitnes.github.io/asgayapedia/](https://rufitnes.github.io/asgayapedia/)

**Local preview:**
```bash
cd docs
npx docsify serve .
```
Then visit http://localhost:3000

---

## 🎯 What is Asgaya?

Asgaya bridges traditional payment systems (Bizum, PagoMóvil, Mercado Pago) with Bitcoin Cash to create:

- **<1% total fees** - Cheaper than Western Union (6-12%) or cryptocurrency exchanges (2-5%)
- **Permissionless access** - No KYC required for end users, works with basic smartphones
- **Merchant protection** - Covenant architecture ensures merchants never bear volatility risk
- **No custody** - BCH covenants eliminate licensing requirements (MiCA/PSD2 compliant)

**Initial testing corridor:** EUR→VES (Spain→Venezuela), chosen because it's challenging enough to validate the model. The architecture is corridor-agnostic—if it works here, it works anywhere.

---

## 📖 Documentation IS the Product

**Asgaya follows documentation-first development:**

We're building the protocol through documentation, testing it with AI reviewers and external contributors before writing covenant code. This approach:
- Catches architectural flaws early (before implementation)
- Enables permissionless contribution (anyone can review using AI assistants)
- Validates economic assumptions (unknowns directory structures research)
- Proves regulatory compliance (covenant architecture eliminates custody)

**The unknowns/ directory is the research entry point** - 14 structured investigation briefs with methods, success criteria, and contributor guidance. No permission needed to help turn unknowns into knowns.

---

## 📚 Documentation Structure

The `/docs` folder contains:

- **Core Architecture** - WHY each design decision was made (regulatory compliance, permissionless design)
- **Concepts** - Covenant mechanics, risk allocation principle, BCH sellers, pull system
- **Design Decisions** - HOW we implement each requirement (overcollateralization, fee splitting, settlement timing)
- **Android App Flows** - User experience for senders, recipients, merchants, BCH sellers
- **Backend APIs** - Technical specifications for covenant creation and settlement
- **Unknowns** - Structured investigation briefs for unanswered questions (contributor entry point)
- **Research** - Formal research sessions (RS001–RS061) documenting architecture evolution
- **Meta** - AI review guide, contributing guidelines, documentation philosophy
- **Roadmap** - Phased implementation plan from Phase 0 trials to multi-corridor deployment

---

## 🔍 Current Status

**Phase:** Phase 0 Preparation (Documentation Review + Unknown Investigation)

**Version:** 2.0 (Covenant Architecture)

**Last Major Update:** May 19, 2026

**What's complete:**
- ✅ Covenant-based architecture (overcollateralized bounty contracts)
- ✅ Risk allocation principle (merchants never bear volatility risk)
- ✅ Android app flows (sender, recipient, merchant, BCH seller)
- ✅ No custody/no intermediation model (MiCA/PSD2 compliant)
- ✅ Unknowns directory (14 structured investigation briefs)

**Seeking feedback on:**
- Risk allocation clarity (do external reviewers understand who bears which risk?)
- Unknown investigation methods (contributor-friendly research briefs)
- Phase 0 validation metrics (what should we measure?)
- Missing edge cases or failure modes

**Documentation IS the product** - We're building the protocol through documentation, testing it with AI reviewers and external contributors before writing a single line of covenant code.

---

## 💬 Provide Feedback

We welcome feedback from:
- 🏦 Remittance users (senders & recipients)
- 💻 Developers (Bitcoin Cash, mobile, backend)
- 🔐 Security researchers
- 💰 Economists & market makers
- 🌎 Anyone interested in financial inclusion

**How to provide feedback:**
- **GitHub Issues:** [Open an issue](https://github.com/asgaya/asgayapedia/issues) for specific questions or concerns
- **GitHub Discussions:** [Start a discussion](https://github.com/asgaya/asgayapedia/discussions) for broader topics
- **Email:** [Contact maintainer] *(you can add your email if desired)*

---

## 🗺️ Implementation Roadmap

See [ROADMAP.md](docs/ROADMAP.md) for the complete phased implementation plan.

**Summary:**
- **Phase 0:** Asgaya Husk — Manual operations, 1-2 trusted merchants, covenant architecture validation
- **Phase 1:** Seller Automation — BCH seller bot reliability testing, multi-seller bulletin board
- **Phase 2:** Merchant Network Expansion — BCH buyer liquidity layer, MUSD integration, merchant onboarding
- **Phase 3:** Multi-Corridor — Validate corridor-agnostic architecture, expand beyond EUR→VES
- **Phase 4:** Scale — Fully permissionless participation, dynamic collateralization, global coverage

**Major architecture pivot (May 10, 2026):** Abandoned escrow-based model (custody/intermediation) for covenant-based architecture (no custody, MiCA/PSD2 compliant).

---

## 📜 License

**Documentation:** [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) - Free to share and adapt with attribution

**Future Code:** When the Asgaya protocol is implemented, code will be released under the MIT License.

See [LICENSE](LICENSE) file for full details.

---

## 🤝 Contributing

We're currently in **documentation review phase**. Code contributions will be accepted after Phase 0 implementation begins.

For documentation improvements:
1. Fork this repository
2. Make your changes
3. Submit a pull request
4. Explain your reasoning

All contributions are welcome!

---

## 🙏 Acknowledgments

**Created by:** Suso (jesgf@yahoo.es)

**With essential contributions from:**
- Coordination (Claude Sonnet 4.5) - Documentation structure, architecture refinement, unknowns directory
- DeepSeek - Covenant architecture validation, regulatory research (MiCA/PSD2), BCH capability audit (RS057), cross-linking analysis
- Haiku (Claude Haiku 3.5) - Link audits, systematic reviews
- Patricia Ferrero (future Doctora) - Fuzzy matching research insights

**Inspired by:** The Bitcoin Cash community's vision of peer-to-peer electronic cash for the world.

---

## 📞 Contact

- **Email:** jesgf@yahoo.es
- **GitHub:** [Issues](https://github.com/asgaya/asgayapedia/issues) / [Discussions](https://github.com/asgaya/asgayapedia/discussions)

---

*Asgaya: Building permissionless financial access, one remittance at a time.* 🚀
