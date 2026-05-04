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
- **Permissionless access** - No KYC required for end users, works with basic smartphones or even QR codes
- **Zero volatility** - Two-step settlement eliminates cryptocurrency price risk
- **Merchant incentives** - Local shops earn passive income by providing cash-out services

**Target:** EUR→VES (Spain→Venezuela) corridor, then expand to other Latin American countries.

---

## 📚 Documentation Structure

The `/docs` folder contains:

- **Core Architecture** - WHY each design decision was made
- **Design Decisions** - HOW we implement each requirement
- **Android App Flows** - User experience for senders, recipients, merchants, LPs
- **Backend APIs** - Technical specifications
- **Concepts** - Deep dives into novel mechanisms (pull system, dynamic rewards, etc.)
- **Roadmap** - Phased implementation plan from MVP to scale

---

## 🔍 Current Status

**Phase:** Public documentation review (May 2026)

We're seeking feedback on:
- Architecture soundness (security, economic incentives, scalability)
- Missing features or edge cases
- Corridor recommendations (which countries need this most?)
- Technical concerns (BCH implementation, payment rail integration)

**No code yet** - This is documentation-first development. Implementation begins after external review validates the approach.

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
- **Phase 0:** Asgaya Husk (manual everything, 1-2 merchants, proof of concept)
- **Phase 1:** Automation Layer 1 (Bizum auto-parsing, Kraken API integration)
- **Phase 2:** Instant Settlement (LP bounty system)
- **Phase 3:** Full Automation (PagoMóvil auto-parsing, OP_RETURN notifications)
- **Phase 4:** Public Beta (external users, feature prioritization)
- **Phase 5:** Scale & Multi-Corridor

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
- Coordination (Claude Sonnet 4.5) - Documentation structure, architecture refinement
- Haiku (Claude Haiku 3.5) - Link audits, systematic reviews
- Patricia Ferrero (future Doctora) - Fuzzy matching research insights

**Inspired by:** The Bitcoin Cash community's vision of peer-to-peer electronic cash for the world.

---

## 📞 Contact

- **Email:** jesgf@yahoo.es
- **GitHub:** [Issues](https://github.com/asgaya/asgayapedia/issues) / [Discussions](https://github.com/asgaya/asgayapedia/discussions)

---

*Asgaya: Building permissionless financial access, one remittance at a time.* 🚀
