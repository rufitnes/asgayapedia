# AI-Optimized Documentation Access

Welcome! This directory contains Asgaya protocol documentation optimized for AI consumption.

## 🚀 Quick Start (2 minutes to documentation)

**New to Asgaya?** Start here:

**→ [quick-start.txt](quick-start.txt)** (650 lines, ~15 min read)

Condensed overview of the entire protocol: what it is, why it exists, how it works.

---

## 📋 Recommended Reading Order

**For comprehensive review:**

1. **[quick-start.txt](quick-start.txt)** → Overview & context (650 lines) ✅ **v2.0**
2. **[complete.txt](complete.txt)** → Full documentation (3,898 lines) ✅ **v2.0**

**Alternative (older individual files):**
- **[core-arch.txt](core-arch.txt)** → Why each feature exists (2,100 lines) ⚠️ *Pre-covenant*
- **[decisions.txt](decisions.txt)** → How we implement it (2,500 lines) ⚠️ *Pre-covenant*
- **[flows.txt](flows.txt)** → User experience (3,850 lines) ⚠️ *Pre-covenant*
- **[apis.txt](apis.txt)** → Technical specs (4,900 lines) ⚠️ *Pre-covenant*

**Recommended:** Use quick-start.txt + complete.txt for current covenant architecture

---

## 🎯 Files by Context Window

### Small Context Models (<8K tokens)
**→ [quick-start.txt](quick-start.txt)** only

### Medium Context Models (8K-32K tokens)
**→ [quick-start.txt](quick-start.txt)** + **[core-arch.txt](core-arch.txt)** + **[decisions.txt](decisions.txt)**

### Large Context Models (32K+ tokens)
**→ [complete.txt](complete.txt)** (everything in one file, 3,898 lines) ✅ **v2.0 - Covenant Architecture**

---

## 📂 Files by Topic

### Strategic/Business Understanding
- **[quick-start.txt](quick-start.txt)** - High-level overview, problem/solution
- **[core-arch.txt](core-arch.txt)** - Feature rationale (the WHYs)

### Technical/Implementation
- **[decisions.txt](decisions.txt)** - Design decisions (the HOWs)
- **[apis.txt](apis.txt)** - Backend API specifications
- **[flows.txt](flows.txt)** - UX flows (sender, recipient, merchant, LP)

### Comprehensive
- **[complete.txt](complete.txt)** - All of the above + glossary + roadmap (one file)
- **[overview.txt](overview.txt)** - Optimized summary (llm.txt format)

### Navigation
- **[index.txt](index.txt)** - Plain text navigation guide (this README in .txt format)

---

## 🔍 What to Review

**Focus areas requested:**

1. **Consistency** - Do decisions match research?
2. **Missing pieces** - Critical gaps or edge cases?
3. **Clarity** - Can newcomers understand it?
4. **Technical soundness** - Security concerns?
5. **Research validation** - Do sources support conclusions?

---

## 💬 How to Provide Feedback

### GitHub Issues (Preferred)
**Repository:** https://github.com/rufitnes/asgayapedia/issues

**Label by severity:**
- `critical` - Blocks implementation, must fix
- `warning` - Should address before launch
- `improvement` - Nice to have, post-MVP

**Format:**
```
Title: Brief description
Body: 
- Location: [file, section, line number]
- Issue: Detailed explanation
- Impact: Why this matters
- Suggestion: Proposed fix (if applicable)
```

### Email
**Contact:** jesgf@yahoo.es

---

## 🌐 Direct Raw URLs (Copy-Paste Ready)

**Base URL:**
```
https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/
```

**Direct links:**
- [index.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/index.txt)
- [quick-start.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/quick-start.txt)
- [overview.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/overview.txt)
- [core-arch.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/core-arch.txt)
- [decisions.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/decisions.txt)
- [flows.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/flows.txt)
- [apis.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/apis.txt)
- [complete.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/complete.txt)

**Once asgaya.org goes live, these will also be available at:**
```
https://asgaya.org/ai/[filename]
```

---

## 📊 File Details

| File | Lines | Status | Description |
|------|-------|--------|-------------|
| **quick-start.txt** | 650 | ✅ v2.0 | Condensed overview (covenant architecture) |
| **complete.txt** | 3,898 | ✅ v2.0 | Everything combined (covenant architecture) |
| **index.txt** | ~100 | ⚠️ Pre-covenant | Navigation guide (plain text) |
| **overview.txt** | 323 | ⚠️ Pre-covenant | Optimized summary (llm.txt format) |
| **core-arch.txt** | 2,115 | ⚠️ Pre-covenant | Core architecture (WHY) |
| **decisions.txt** | 2,462 | ⚠️ Pre-covenant | Design decisions (HOW) |
| **flows.txt** | 3,850 | ⚠️ Pre-covenant | User flows (UX) |
| **apis.txt** | 4,909 | ⚠️ Pre-covenant | Backend APIs (TECHNICAL) |

**Current (May 11):** quick-start.txt + complete.txt reflect covenant architecture  
**Legacy (Pre-May 10):** Other files describe old escrow model

---

## 🕐 Last Updated

**Version:** 2.0 (Covenant Architecture)  
**Date:** 2026-05-11  
**Status:** ✅ **CURRENT** (quick-start.txt + complete.txt)

---

## ⚠️ IMPORTANT NOTICE

**Status of AI-optimized files (May 11, 2026):**

✅ **CURRENT (Covenant Architecture v2.0):**
- **quick-start.txt** (650 lines) - Regenerated May 11
- **complete.txt** (3,898 lines) - Regenerated May 11

⚠️ **STALE (Escrow Architecture v1.0):**
- All other .txt files still describe the OLD escrow model (pre-May 10, 2026)

**Key changes in v2.0:**
- ❌ Escrow model **abandoned** (MiCA compliance issue)
- ✅ Covenant-based architecture **implemented** (BCH smart contracts)
- ❌ Mediator role **removed** (Phase 0 = autonomous covenant)
- ✅ Fee split **updated** (2-way: Seller 0.5%, Merchant 0.5%)
- ✅ BCH seller hedge mechanism **documented**

**For AI reviewers:** Use **quick-start.txt** and **complete.txt** for current covenant architecture.

**Main documentation:** https://github.com/rufitnes/asgayapedia/tree/main/docs (all updated)

---

## 📋 Update Log

**May 11, 2026 (v2.0 Regeneration):**
- ✅ quick-start.txt regenerated (650 lines, covenant architecture)
- ✅ complete.txt regenerated (3,898 lines, covenant architecture)
- ✅ BCH seller hedge mechanism documented
- ✅ Fee split unified (2-way: 0.5% seller, 0.5% merchant)
- ✅ Dispute resolution simplified (Phase 0 autonomous)
- ✅ Phase 0 validation checklist integrated

**May 5, 2026 (v1.0 - Now Obsolete):**
- ⚠️ Fee split clarity (Kraken fee deducted first) [OBSOLETE]
- ⚠️ Dynamic reward modulation (marked as POST-MVP) [OBSOLETE]
- ⚠️ Unclaimed transaction expiry (24h, €0.10 fee) [KEPT]
- ⚠️ Dispute resolution framework (3-strike system) [SIMPLIFIED]

---

## 📚 Human-Friendly Access

**For humans (with visual UI):**
- **Docsify site:** https://rufitnes.github.io/asgayapedia/
- **GitHub repo:** https://github.com/rufitnes/asgayapedia

**For developers:**
- **Clone repo:** `git clone https://github.com/rufitnes/asgayapedia.git`

---

## ❓ Questions?

**Project maintainer:** Suso (jesgf@yahoo.es)  
**Repository:** https://github.com/rufitnes/asgayapedia  
**Documentation site:** https://rufitnes.github.io/asgayapedia/

---

## 📜 License

**Documentation:** CC BY 4.0 (Creative Commons Attribution 4.0 International)  
**Future code:** MIT License (when implementation starts)

---

**Built with ❤️ for AI reviewers who want direct, efficient access to documentation.**

*This directory structure was created after DeepSeek's review highlighted the friction AI instances face when trying to access GitHub-hosted documentation. The goal: 90% reduction in pre-review effort.*
