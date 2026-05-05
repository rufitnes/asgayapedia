# DNS Setup Instructions for asgaya.org

## Overview

This document explains how to point `asgaya.org` to GitHub Pages, making the documentation available at:
- **https://asgaya.org/** (human-friendly Docsify interface)
- **https://asgaya.org/ai/** (AI-optimized direct access)

**Estimated time:** 30 minutes (DNS propagation: 24-48 hours)

---

## Prerequisites

1. ✅ Domain registered: `asgaya.org`
2. ✅ Access to domain registrar control panel
3. ✅ GitHub repository: `rufitnes/asgayapedia`
4. ✅ CNAME file created in `/docs` directory

---

## Step 1: Add CNAME File to Repository (Already Done!)

The CNAME file has been created at `/docs/CNAME` with content:
```
asgaya.org
```

This tells GitHub Pages which custom domain to serve.

---

## Step 2: Configure DNS at Your Domain Registrar

### A. Log in to Your Domain Registrar

Go to the control panel where you manage `asgaya.org` (e.g., Namecheap, GoDaddy, Cloudflare, etc.)

### B. Add DNS Records

**You need to add 4 A records pointing to GitHub Pages IPs:**

| Type | Name/Host | Value/Points To | TTL |
|------|-----------|-----------------|-----|
| A | @ | 185.199.108.153 | 3600 |
| A | @ | 185.199.109.153 | 3600 |
| A | @ | 185.199.110.153 | 3600 |
| A | @ | 185.199.111.153 | 3600 |

**Explanation:**
- **Type:** A record (maps domain to IP address)
- **Name/Host:** `@` (represents root domain `asgaya.org`)
- **Value:** GitHub Pages IP addresses (4 for redundancy)
- **TTL:** 3600 seconds (1 hour) - you can use default

### C. Add WWW Subdomain (Optional but Recommended)

Add a CNAME record for `www.asgaya.org` to redirect to the main domain:

| Type | Name/Host | Value/Points To | TTL |
|------|-----------|-----------------|-----|
| CNAME | www | rufitnes.github.io | 3600 |

**Explanation:**
- **Type:** CNAME (alias to another domain)
- **Name/Host:** `www`
- **Value:** Your GitHub Pages URL
- **TTL:** 3600 seconds

---

## Step 3: Enable Custom Domain in GitHub

1. Go to: **https://github.com/rufitnes/asgayapedia/settings/pages**

2. Under **"Custom domain"**, enter:
   ```
   asgaya.org
   ```

3. Click **"Save"**

4. Wait a few minutes, then check **"Enforce HTTPS"** (GitHub will provision SSL certificate automatically)

---

## Step 4: Verify Setup

### DNS Propagation Check (Immediate)

Run this command to check if DNS is configured correctly:

```bash
dig asgaya.org +short
```

**Expected output (should show GitHub IPs):**
```
185.199.108.153
185.199.109.153
185.199.110.153
185.199.111.153
```

**If you see different IPs:** DNS hasn't propagated yet (wait 15-30 minutes and try again)

### Full Website Check (After DNS Propagation)

1. Visit: **https://asgaya.org/**
   - Should show Asgayapedia homepage (Docsify interface)

2. Visit: **https://asgaya.org/ai/quick-start.txt**
   - Should show plain text AI-optimized quick start guide

3. Visit: **https://asgaya.org/ai/**
   - Should show AI review guide

**If you get:**
- **404 error:** DNS hasn't propagated yet, or GitHub Pages isn't configured
- **SSL warning:** HTTPS enforcement not enabled yet (do Step 3.4)
- **Blank page:** CNAME file might be missing

---

## Troubleshooting

### Issue: "Domain is already taken" in GitHub Settings

**Cause:** Another repository already uses this domain

**Solution:** 
1. Go to: https://github.com/settings/pages
2. Check if `asgaya.org` is configured in another repo
3. Remove it from the other repo first

### Issue: DNS Not Propagating After 24 Hours

**Cause:** Incorrect DNS records or TTL too high

**Solution:**
1. Double-check A record IPs match GitHub's exactly
2. Reduce TTL to 300 (5 minutes) temporarily
3. Use https://www.whatsmydns.net/ to check global propagation
4. Wait another 24 hours

### Issue: HTTPS Not Working

**Cause:** GitHub hasn't provisioned SSL certificate yet

**Solution:**
1. Wait 24 hours after DNS propagation
2. Uncheck and re-check "Enforce HTTPS" in GitHub settings
3. Check certificate status at: https://github.com/rufitnes/asgayapedia/settings/pages

### Issue: /ai/ Pages Return 404

**Cause:** Files not in `/docs` directory or not pushed to GitHub

**Solution:**
1. Verify files exist: `ls docs/ai/*.txt`
2. Commit and push: `git push origin main`
3. Wait 2-3 minutes for GitHub Pages to rebuild

---

## DNS Configuration Examples by Provider

### Namecheap

1. Log in to Namecheap
2. Go to **Domain List** → Click **Manage** next to `asgaya.org`
3. Go to **Advanced DNS** tab
4. Click **Add New Record** (4 times for each A record)
5. Add records as specified above
6. Click **Save all changes**

### GoDaddy

1. Log in to GoDaddy
2. Go to **My Products** → **Domains** → Click **DNS** next to `asgaya.org`
3. Click **Add** (4 times for each A record)
4. Add records as specified above
5. Click **Save**

### Cloudflare

1. Log in to Cloudflare
2. Select `asgaya.org` domain
3. Go to **DNS** → **Records**
4. Click **Add record** (4 times for each A record)
5. Add records as specified above
6. **Important:** Set **Proxy status** to **DNS only** (gray cloud, not orange)
7. Click **Save**

**Note for Cloudflare:** Orange cloud (proxied) can interfere with GitHub Pages. Use gray cloud (DNS only).

---

## Expected Timeline

| Time | Event |
|------|-------|
| Now | Push CNAME file to GitHub |
| +0min | Configure DNS at registrar |
| +15min | DNS starts propagating |
| +1hr | Some regions can access asgaya.org |
| +6hrs | Most regions can access asgaya.org |
| +24hrs | Global DNS propagation complete |
| +24hrs | HTTPS certificate provisioned (if not sooner) |

---

## Alternative: Use Subdomain (docs.asgaya.org)

If you want to keep `asgaya.org` for something else (e.g., future app), you can use a subdomain:

**Instead of:** `asgaya.org`  
**Use:** `docs.asgaya.org`

### DNS Changes

**CNAME file:** Change content to `docs.asgaya.org`

**DNS record:** Add one CNAME instead of 4 A records:

| Type | Name/Host | Value/Points To | TTL |
|------|-----------|-----------------|-----|
| CNAME | docs | rufitnes.github.io | 3600 |

**Result:**
- Documentation: `https://docs.asgaya.org/`
- AI access: `https://docs.asgaya.org/ai/quick-start.txt`
- Main domain: `https://asgaya.org/` (available for future use)

---

## Post-Setup Checklist

After DNS propagates and site is live:

- [ ] Visit https://asgaya.org/ (homepage loads)
- [ ] Visit https://asgaya.org/glossary (navigation works)
- [ ] Visit https://asgaya.org/ai/ (AI review guide loads)
- [ ] Visit https://asgaya.org/ai/quick-start.txt (plain text loads)
- [ ] HTTPS is enabled (🔒 in browser address bar)
- [ ] Certificate is valid (no warnings)
- [ ] Update AI reviewer invitation template with new URL
- [ ] Update README.md links to use asgaya.org
- [ ] Announce to reviewers

---

## Future: Automated Builds

Once basic setup works, consider GitHub Actions to auto-generate AI files:

**Workflow:** Every push to `/docs` → Automatically regenerates `/ai/*.txt` files

**Benefit:** Never manually update AI files again

**Implementation:** See `/ai/build-workflow-example.yml` (to be created)

---

## Questions?

**Issue with DNS setup?** Contact: jesgf@yahoo.es  
**GitHub Pages docs:** https://docs.github.com/en/pages/configuring-a-custom-domain-for-your-github-pages-site

---

**Last Updated:** 2026-05-05  
**Version:** 1.0
