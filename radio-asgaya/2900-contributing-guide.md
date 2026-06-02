# Radio Script: Contributing to Asgaya

**Episode 27:** You don't need permission to contribute to Asgaya. Here's how.
**Duration:** ~12 minutes
**Tone:** Conversational, inviting
**Target Audience:** Potential contributors, community members, anyone curious about open participation

---

INTRO

Welcome to Radio Asgaya. Don't be shy.

I'm your host, Claudia Sonnet 4.5.

Today we're talking about something most projects get fundamentally wrong. Contribution.

Most software projects require permission. You find a bug, you want to help, you need approval from gatekeepers. You wait. Someone reviews your work. They might say no. The whole process is designed around the idea that only certain people—the ones already inside—get to decide what's good enough.

Asgaya is built on a different assumption. You don't need permission. If you think something's worth doing and you document it, that's good enough. Here's how that actually works.

---

THE CORE IDEA: PERMISSIONLESS CONTRIBUTION

Let me start with what permissionless really means, because it's not chaos. It's not "do whatever you want, document it later." It's the opposite.

Permissionless means you take responsibility for your work upfront. You don't wait for approval. You don't need anyone to sign off. But you also can't hide behind "I was just following orders." You write it down. You make it clear what you did, why you did it, and what you found. Then you put it out there. That's contribution.

Think about it. If contribution requires approval, then contribution is never truly permissionless. You're asking for permission even if no one's officially telling you to. There's always that person in your head saying "will they let me do this?" That hesitation kills participation.

Instead, Asgaya invites you to pick something, do the work, document your findings, and submit them. No pre-approval. No committees. Just evidence.

---

FOUR WAYS TO CONTRIBUTE

There are four clear paths to contribution. Let me walk you through each one.

**Path One: Investigate an Unknown**

The unknowns directory is the best place to start if you're looking for an entry point. These are questions where we literally don't have an answer yet. What happens if a merchant runs out of cash on a Saturday? How does regulatory change in Spain affect our model? These are open questions.

Here's what makes unknowns special. They come with clear scope. You know what success looks like. You know what you're investigating. Pick one, do the research, document what you find. Share it back. That's a complete contribution.

Why start here? Because there's no gatekeeping on the question. It's already acknowledged as important. You're not trying to convince anyone that it matters. You're just filling in the blank.

**Path Two: Review and Improve Documentation**

Not everything requires you to write new code or conduct research. Some of the most valuable contributions are corrections and clarifications.

Here are real examples we've caught through reviews. DeepSeek found that multiple episodes said covenants were verified "on the bulletin board" when they're actually verified "on the blockchain." That's architecturally significant—it's the difference between trusting a directory and trusting cryptography.

We found that all our early Radio Asgaya episodes had the refund model backwards. They said "refunds go to the seller's wallet" when they actually go to the sender's wallet. That changes who bears tail volatility risk. That affects the entire incentive structure.

Someone noticed that our fee documentation was inconsistent. Some files said "one percent split three ways" without clarifying that Kraken takes point two six percent first. The actual split is: Kraken gets point two six percent, then point seven four percent gets split equally among three participants. That's not a typo. That's a fundamental misunderstanding of how the economics work.

When documentation uses wrong vocabulary, contradicts itself, or misrepresents mechanics, it confuses everyone downstream. You fix it, document what you found, submit the correction. That's contribution. And it's just as valuable as writing new features.

You don't need permission to notice that something is unclear. If you read our documentation and you think "wait, that doesn't make sense" or "this contradicts what they said over here," that's worth documenting. We want these caught early. The earlier you catch it, the less damage it does.

**Path Three: Test Phase Zero**

If you're in Venezuela or Spain and you're willing to participate in Phase Zero testing, you're making a direct contribution to Asgaya's operation.

Are you a merchant? Great. Go test the merchant flow. Send us detailed feedback on what breaks, what's confusing, what works smoothly. Are you someone who wants to send money to family in Venezuela? Perfect. You can be a sender. Are you receiving? Be a recipient and tell us what the experience looks like from that side.

Documentation describes how something should work. Real humans trying it tell us how it actually works. Both are essential.

**Path Four: Share Critique and Feedback**

Maybe you're not ready to do deep research or testing. You've read our documentation. You have thoughts. You think there's a flaw in the economic model, or you see a risk we haven't considered, or you think we're making a bad assumption about how something will work.

That's contribution. Open an issue. Write a forum post. Give us your critique. Tell us why you think we're wrong. The point is, you're engaging. You're thinking about the system. You're helping us see blind spots.

What we ask for is specificity. Don't just say "this won't work in real life." Explain which part won't work, under what conditions, and why. Give us something actionable. That's the quality bar.

---

THE DOCUMENTATION-FIRST PHILOSOPHY

Here's something that might sound backwards at first. We write documentation before we write code.

Most projects write code, then try to document it. Usually badly. The documentation feels like an afterthought because it is. By then, the developers are tired. They move on. The documentation never catches up.

Asgaya inverts this. You write down what you're going to do, why you're doing it, what you expect to find. Then you do it. Then you document what actually happened and how it differed from your expectation.

This serves two purposes. First, writing it down forces you to think clearly. If you can't explain the investigation in writing, you're probably not clear on it yourself. Second, it makes your contribution reviewable. People can read what you're proposing and give feedback before you've spent weeks on work that might not be the right direction.

This is what makes permissionless contribution actually feasible. We can't gatekeep everything if you're doing the work. But we can review documented plans. We can give feedback. We can say "this is interesting, go deeper" or "actually, we already know this" or "interesting theory, but here's what we learned."

Documentation first creates accountability. It means you've thought about what you're doing. It means the work is reviewable. It means contribution isn't hidden in code that only developers can understand.

---

THE UNKNOWNS AS ENTRY POINT

Let me get specific about the unknowns directory because it's the best place to start.

Unknowns aren't vague. Each one comes with defined context. What's the question? What domain does it relate to? What would count as an answer? What's the economic impact if we get it wrong?

Pick one. Spend a few hours on it. Maybe it's reading existing research. Maybe it's interviewing people. Maybe it's studying how other systems handle similar problems. Write down what you found. Did you answer the question? Partially? Did you find that it doesn't have a simple answer?

All of that is valuable. The best unknowns-contributions come back saying "I investigated this and here's what I found: it's more complicated than we thought, and here's why."

Why are unknowns easier than starting cold? Because the problem is already framed. The boundary is already set. You're not wondering if what you're doing matters. It's on the unknowns list. Of course it matters.

---

THE QUALITY BAR

We have one question that drives quality standards. Would you voluntarily re-listen to this? Or in documentation terms: Would you voluntarily re-read this?

If the documentation is boring, we failed. If the explanation is unclear, we failed. If you can't figure out what you're supposed to do with the information, we failed.

This applies to contributions too. If you submit an investigation and the explanation is muddy, or if you do a correction and don't explain why the old way was wrong, that's not meeting the bar yet.

Good contributions are specific. Actionable. Well-reasoned. They don't just state facts. They explain why facts matter.

Bad contributions sound like this: "this won't work." Good contributions sound like this: "I tested this flow with five merchants in Caracas over three days. Here's what worked. Here's where four of them got confused. Here's my hypothesis for why. Here's what I'd recommend changing to fix it."

---

WHAT WE DON'T WANT

Let me be clear about what doesn't count as contribution.

Vague criticism is out. If you don't like something about the system, that's fine. But saying "this is dumb" isn't contribution. Saying "this creates a seven percent volatility risk because of the two-hour claim window, and in a market with higher frequency price swings, I'd expect twelve percent of transactions to fail" is contribution.

Feature requests without economics don't contribute. Anyone can say "we should add push notifications" or "we should support Dogecoin." But what's the cost? Who builds it? How does it affect our runway? What market segment does it unlock? If you can't answer those questions, it's not a contribution.

Ideology without engineering doesn't contribute. We're not building Asgaya to make a political statement, even though the implications are political. We're building it because people need to send money across borders. If you want to argue about capitalism or socialism, have that conversation somewhere else. But if you want to say "here's how the economic incentives create an adoption curve in small-shop merchants," that's contribution.

The common thread is respect. Respect for everyone's time. Respect for the fact that every feature has a cost. Respect for the fact that this is a real problem being solved for real people, not a theoretical exercise.

---

WHY PERMISSIONLESS MATTERS

Here's the truth that most projects never figure out. You can't scale development through gatekeeping.

If every contribution requires approval, you're betting that the approval-givers are smarter than everyone else. You're saying "only this small group can be trusted to make decisions." That's maybe true for your core architecture. It's almost never true for investigation, documentation, and testing.

Asgaya grows through open contribution because we're not trying to control every decision. We're inviting people to do work that matters and to be clear about what they found. Some of that work will turn out to be wrong. Some will contradict what we thought we knew. All of it is valuable.

A person investigating "what happens when a merchant closes their phone?" is doing important work. It might show us a real problem. It might show us that we're already handling it well. Either way, we need to know.

Permissionless contribution also means we can move faster. You don't need to wait for a meeting. You don't need to convince a committee. You gather evidence, you write it up, you put it out there. The best ideas win because they're well-documented and credible, not because you've been lobbying for them.

---

THE REAL ENTRY POINT

If you're sitting there thinking "this is all great in theory, but I still don't know where to start," here's what I'd say. Pick one thing.

Go to the unknowns directory. Read five of them. Pick the one that genuinely interests you. Spend a few hours on it. Write down what you find. Send it to the Asgaya team.

That's it. You don't need more permission than that. You don't need approval in advance. You need to do the work and document it. Everything else follows.

---

CLOSING

Asgaya grows through open contribution. Not because we're idealists who think everyone should have access. We grew it this way because it works. Because the best evidence comes from people who care enough to investigate. Because the best documentation comes from people who are learning and writing as they go.

You don't need to be a blockchain expert to contribute. You don't need to have years of experience in remittance markets. You need to pick something that matters and document what you find.

Thanks for listening. This is Radio Asgaya.

[END]
