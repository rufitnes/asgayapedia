# AI-Optimized Documentation Access

Welcome! This directory contains Asgaya protocol documentation optimized for AI consumption.

## 🚀 Quick Start (2 minutes to documentation)

**New to Asgaya?** Start here:

**→ [quick-start.txt](quick-start.txt)** (500 lines, ~15 min read)

Condensed overview of the entire protocol: what it is, why it exists, how it works.

---

## 📋 Recommended Reading Order

**For comprehensive review:**

1. **[quick-start.txt](quick-start.txt)** → Overview & context (500 lines)
2. **[core-arch.txt](core-arch.txt)** → Why each feature exists (2,100 lines)
3. **[decisions.txt](decisions.txt)** → How we implement it (2,500 lines)
4. **[flows.txt](flows.txt)** → User experience (3,850 lines)
5. **[apis.txt](apis.txt)** → Technical specs (4,900 lines)

**Total:** ~14,000 lines, ~3-4 hours thorough review

---

## 🎯 Files by Context Window

### Small Context Models (<8K tokens)
**→ [quick-start.txt](quick-start.txt)** only

### Medium Context Models (8K-32K tokens)
**→ [quick-start.txt](quick-start.txt)** + **[core-arch.txt](core-arch.txt)** + **[decisions.txt](decisions.txt)**

### Large Context Models (32K+ tokens)
**→ [complete.txt](complete.txt)** (everything in one file, 15,250 lines)

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

| File | Lines | Size | Description |
|------|-------|------|-------------|
| **index.txt** | ~100 | 4.4KB | Navigation guide (plain text) |
| **quick-start.txt** | 497 | 16KB | Condensed overview |
| **overview.txt** | 323 | 11KB | Optimized summary (llm.txt format) |
| **core-arch.txt** | 2,115 | 74KB | Core architecture (WHY) |
| **decisions.txt** | 2,462 | 81KB | Design decisions (HOW) |
| **flows.txt** | 3,850 | 158KB | User flows (UX) |
| **apis.txt** | 4,909 | 138KB | Backend APIs (TECHNICAL) |
| **complete.txt** | 15,250 | 512KB | Everything combined |

**Total:** ~15,000 lines, ~1MB

---

## 🕐 Last Updated

**Version:** 1.0  
**Date:** 2026-05-05  
**Status:** All critical gaps resolved (DeepSeek review)

**Recent updates:**
- ✅ Fee split clarity (Kraken fee deducted first)
- ✅ Dynamic reward modulation (marked as POST-MVP)
- ✅ Unclaimed transaction expiry (24h, €0.10 fee)
- ✅ Dispute resolution framework (3-strike system)

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
