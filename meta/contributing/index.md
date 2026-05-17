# Contributing to Asgaya

> How to help improve Asgaya

---

## Welcome! 🎉

Thank you for your interest in contributing to Asgaya.

**What is Asgaya?**
A permissionless Bitcoin Cash remittance protocol with <1% fees, no KYC, and self-custody.

**Current Phase:** Documentation review (seeking external feedback before implementation)

---

## The Philosophy: Permissionless Contribution

**Asgaya's documentation is AI-accessible by design. This isn't accidental—it's our contribution model.**

### The Problem We Solved

Traditional open source has a knowledge barrier:
- Scattered documentation
- Tribal knowledge in maintainers' heads  
- New contributors spend weeks getting context
- Non-technical contributors can't participate meaningfully

**Asgaya inverts this:** The documentation IS the product. Complete, comprehensive, AI-accessible knowledge transfer.

### How It Works

**1. Complete Documentation**
- Every decision documented with rationale
- Every trade-off explained
- Every concept linked to implementation
- 180+ pages covering architecture to implementation details

**2. AI-Accessible Format**
- [llms.txt](https://docs.asgaya.org/llms.txt) - Complete sitemap for AI navigation
- [llms-full.txt](https://docs.asgaya.org/llms-full.txt) - All content concatenated
- Raw markdown URLs (`/path/to/page/index.md`) - Direct content access
- Zero 404s, clean internal linking

**3. The Result: Permissionless Contribution**

**You don't need to be a developer to contribute meaningfully.**

If you can:
- ✅ Read documentation
- ✅ Ask good questions to an AI (Claude, DeepSeek, etc.)
- ✅ Understand their explanations
- ✅ Identify problems or improvements

**Then you can contribute.** The AI handles the technical depth. You handle the critical thinking.

### Examples of Permissionless Contributions

**Non-technical contributors can:**
- Review economic incentive structures (does the fee model make sense?)
- Audit user flows for usability issues (will people understand this?)
- Identify missing edge cases (what happens if...?)
- Question architectural decisions (why this approach vs alternatives?)
- Validate regulatory compliance claims (is this legal advice accurate?)

**How:** Fetch llms.txt, ask your AI assistant to explain any section, provide feedback based on your understanding.

**The barrier isn't "can you code?" It's "can you think critically about the problem?"**

---

## Where to Start

**Choose what interests you:**

### Documentation Review
Browse the docs and identify areas to improve:
- [Core Architecture](../core-architecture/) - Design principles and economics
- [Android App](../android-app/) - Implementation specification
- [AI Review Guide](ai-review-guide.md) - Detailed review instructions

**With AI assistance:**
1. Ask your AI to fetch and explain any section
2. Question assumptions, identify gaps
3. Open a GitHub issue with your feedback

### Code Contributions (Future)

**Not ready yet!** We're documentation-first.

Implementation begins after documentation review is complete.

---

## How to Contribute

**Simple process:**

1. **Review** the documentation
2. **Open** a GitHub issue with specific feedback
3. **Discuss** with us in the issue
4. **We iterate** based on the discussion

That's it. No complex process, just good ideas and constructive discussion.

---

## What Makes Good Feedback?

**Specific and actionable:**
- ✅ "In `/android-app/notification-listener/security.md`, the bank shortcode whitelist assumes..."
- ❌ "Security could be better"

**Constructive:**
- ✅ "This has a race condition. Suggested fix: add optimistic locking"
- ❌ "This design is terrible"

**Cited when relevant:**
- ✅ "Consider Schnorr signatures instead. Reference: [link]"
- ❌ "You should use something else"

---

## Communication Channels

**Primary:**
- GitHub Issues: https://github.com/asgaya/docs/issues
- GitHub Discussions: https://github.com/asgaya/docs/discussions

**For critical security issues:**
- [security email TBD]

---

## Attribution

We acknowledge all substantial contributions:
- Documentation footer credits
- Project blog recognition
- Asgaya.org credits page

---

## License

**Documentation:** CC BY-SA 4.0
**Code (future):** MIT License

---

## More Information

- **For detailed review guidance:** See [AI Review Guide](meta/ai-review-guide.md)
- **For governance questions:** See [Accountability](meta/accountability.md)
- **To browse docs:** Visit https://docs.asgaya.org

---

*Ready to contribute? Open a GitHub issue and let's discuss!*
