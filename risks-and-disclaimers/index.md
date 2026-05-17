# Risks and Disclaimers

**Last Updated:** May 17, 2026

---

## What Asgaya Is

Asgaya is an **experimental, unregulated protocol** for peer-to-peer remittances using Bitcoin Cash.

We are:
- ✅ An open protocol anyone can study, verify, and use
- ✅ Educational documentation for potential participants
- ✅ Tools that enable self-sovereign money movement
- ✅ Transparent about mechanisms, fees, and trade-offs

We are **not:**
- ❌ A regulated financial institution
- ❌ A custodian of your funds
- ❌ Providing financial, legal, or investment advice
- ❌ Responsible for regulatory compliance in your jurisdiction

---

## Your Responsibilities

By participating in Asgaya (as a user, merchant, or BCH seller), **you are solely responsible for:**

### Legal Compliance
- Understanding and complying with laws in your jurisdiction
- Determining if your participation is legal where you operate
- Obtaining any required licenses or registrations
- Reporting income or transactions as required by law

### Risk Assessment
- Evaluating whether the protocol suits your risk tolerance
- Understanding the technology and economic mechanisms
- Assessing counterparty risks in peer-to-peer transactions
- Accepting full responsibility for your decisions

### Technical Understanding
- Learning how Bitcoin Cash transactions work
- Understanding the two-step settlement mechanism
- Knowing how to secure your private keys
- Verifying the protocol's open-source code if desired

---

## Risks You Accept

### Regulatory Risk

**Asgaya operates in a legal gray area in most jurisdictions. You accept substantial regulatory risk by participating.**

- **No regulatory approval:** Asgaya has not sought approval from financial regulators in any jurisdiction. We have no licenses, no registrations, no government oversight.

- **Money transmission laws:** Many jurisdictions classify remittance services, cryptocurrency exchanges, or payment facilitation as regulated money transmission. Merchants and BCH sellers may be operating illegally depending on local laws.

- **MiCA "professional basis" threshold (EU):** Under the Markets in Crypto-Assets Regulation (MiCA), providing crypto services "on a professional basis" triggers CASP licensing requirements. While occasional BCH selling may fall below this threshold, high-volume BCH sellers could cross into regulated territory without clear guidance on where that line is. Volume thresholds are undefined and may be determined retroactively by regulators.

- **Know Your Customer (KYC) violations:** Asgaya deliberately avoids KYC to remain permissionless. This may violate anti-money laundering (AML) laws in jurisdictions that require customer identification.

- **Unlicensed financial services:** Providing remittance services without appropriate licenses can result in criminal prosecution, not just civil penalties. Prison sentences are possible in some jurisdictions.

- **Tax evasion risks:** Governments may view permissionless remittances as facilitating tax evasion or capital flight. Participants may face investigation even if personally compliant.

- **Changing regulations:** Cryptocurrency and remittance laws are evolving rapidly. What is legal today may be criminalized tomorrow. You accept the risk of retroactive enforcement.

- **Sanctions and embargoes:** Using cryptocurrency to circumvent financial sanctions (even unintentionally) can result in severe criminal penalties in many countries.

- **Enforcement uncertainty:** Regulatory authorities may take unpredictable actions. They may shut down services, seize funds, prosecute participants, or impose fines without warning.

- **No legal defense fund:** If you face legal action, you bear all costs. Asgaya provides no legal support, representation, or financial assistance.

- **Your complete liability:** You—and you alone—are responsible for all legal consequences of your participation. This includes criminal prosecution, civil liability, fines, asset seizure, and imprisonment.

**If you are not prepared to accept potential criminal liability, do not participate in Asgaya.**

### Financial Risk

**You can lose everything. There is no safety net.**

- **Total loss is possible:** Technical failures, smart contract bugs, counterparty default, lost keys, or user error can result in complete and permanent loss of all funds. This is not theoretical—it will happen to some users.

- **Bitcoin Cash volatility:** BCH price can fluctuate 10-20% or more in a single day. The EUR amount a recipient receives in local currency may be significantly different from what the sender intended, even with overcollateralization buffers.

- **No insurance:** Funds are not insured by any government deposit insurance scheme, private insurance company, or protocol guarantee fund. When funds are lost, they are gone forever.

- **No refunds or chargebacks:** Blockchain transactions are irreversible. There is no customer service to call, no dispute resolution process, no way to reverse a mistaken transaction.

- **Counterparty default:** Merchants may take cash and refuse to co-sign. BCH sellers may disappear after accepting bounties. Recipients may lose funds if they fail to claim within 24 hours. Smart contracts enforce rules, but cannot force humans to behave honestly.

- **Smart contract risk:** Covenants are experimental technology. Bugs in CashScript code, unexpected edge cases in timeout cascades, or failures in the EUR-denomination logic could lock funds permanently or distribute them incorrectly.

- **No one to sue:** As a permissionless protocol with no corporate entity, there is no one to hold liable for losses. Contributors have explicitly disclaimed all liability. Courts may have no jurisdiction even if you tried.

- **Liquidity risk:** Even if you successfully receive BCH, you may be unable to convert it to local currency at acceptable rates, or at all, depending on local market conditions.

**Only risk funds you can afford to lose completely. Treat participation as an experiment, not a financial service.**

### Technical Risk
- **Software bugs:** The protocol is experimental and may contain errors
- **Covenant vulnerabilities:** Smart contract code may have bugs despite review and testing
- **CashScript limitations:** Covenant language is powerful but relatively new technology
- **Timeout cascade failures:** 24-hour refund mechanism may behave unexpectedly
- **Network failures:** Bitcoin Cash network or fiat payment systems may experience downtime
- **User error:** Mistakes in addresses, amounts, or confirmations may be irreversible
- **Notification system failures:** SMS parsing or OP_RETURN data may fail to sync properly
- **Security vulnerabilities:** Despite best efforts, security flaws may exist

### Counterparty Risk
- **Merchant default:** Merchants may fail to provide cash after co-signing covenant
- **BCH seller default:** Sellers may fail to honor accepted bounties (mitigated by overcollateralization)
- **Timeout cascade:** 24-hour claim window creates time pressure; unclaimed funds trigger refund split
- **Covenant bugs:** Smart contract code may contain errors despite audit efforts
- **Reputation systems:** Trust mechanisms are experimental and may fail

---

## Our Commitments

While we cannot eliminate risks, we commit to:

### Transparency
- **Open documentation:** All mechanisms explained in detail
- **Open source:** Code available for independent verification
- **No hidden fees:** All costs clearly disclosed
- **Honest about limitations:** We document what we don't know

### Education
- **Clear explanations:** Technical concepts explained for non-experts
- **Risk disclosure:** Honest about potential problems
- **Best practices:** Guidance on safe participation
- **Community support:** Open channels for questions

### No Custody
- **Self-sovereign design:** Users control their own keys
- **No fund access:** Asgaya team cannot access user funds
- **Permissionless:** No central authority can freeze or seize
- **Transparent flows:** All transactions verifiable on-chain

---

## No Warranties

Asgaya is provided **"as is"** with **no warranties of any kind**, express or implied, including but not limited to:

- No warranty of merchantability
- No warranty of fitness for a particular purpose
- No warranty of non-infringement
- No warranty of error-free operation
- No warranty of availability or uptime

---

## Limitation of Liability

To the maximum extent permitted by law:

- Asgaya contributors are **not liable** for any losses, damages, or legal consequences
- Users participate **entirely at their own risk**
- No contributor owes any duty of care to users
- No contributor makes any representations about legal compliance

---

## Acknowledgment

**By using Asgaya documentation, tools, or protocol:**

- ✅ You acknowledge reading and understanding these risks
- ✅ You accept full responsibility for your participation
- ✅ You agree that Asgaya contributors have no liability to you
- ✅ You confirm you will comply with applicable laws
- ✅ You understand this is an experimental protocol with no guarantees

---

## Questions or Concerns?

- **Technical questions:** See [Contributing Guide](meta/contributing.md)
- **Legal questions:** Consult your own legal counsel (we cannot provide legal advice)
- **Regulatory questions:** Contact your local financial authority
- **General discussion:** [GitHub Discussions](https://github.com/asgaya/docs/discussions)

---

## Updates to This Document

We may update this document as:
- Regulations evolve
- Risks become better understood
- Community feedback identifies gaps

**Check this document periodically.** Continued participation after updates means you accept the revised terms.

---

*This document does not constitute legal advice. Consult qualified legal counsel in your jurisdiction before participating in Asgaya.*
