# 5. Testing Strategy

**Category:** Quality Assurance & Validation
**Priority:** 🔴 Critical
**Related:** [Index](android-app/notification-listener/README.md), [security.md](android-app/notification-listener/security.md)

---

## Overview

NotificationListener is mission-critical: if parsing fails, users get stuck in manual confirmation flow (bad UX).

**Testing philosophy:**
1. **Start simple:** Payment flow first (no merchants, no VES)
2. **Build complexity:** Add remittance flow after payment validated
3. **Real-world testing:** Use actual bank notifications, not mocks
4. **Fuzzy monitoring:** Detect format changes proactively
5. **Manual fallback:** Always works, even if parsing fails

---

## Testing Phases

### Phase 1: Payment Flow (Week 1-2)

**Goal:** Validate core concept with simplest flow

**Flow:**
```
Sender → Bizum to Escrow → OP_RETURN notification → Recipient
(No merchant, no VES, no PagoMóvil)
```

**Why start here:**
- Simpler implementation (fewer moving parts)
- Tests two critical components:
  - Bizum parser (fiat → escrow detection)
  - OP_RETURN monitor (BCH → user notification)
- Lower risk for initial beta testers
- Can iterate faster without merchant coordination

**Deliverables:**
- [ ] Bizum parser works on escrow device (Suso's phone)
- [ ] OP_RETURN notifications reach recipient within 30s
- [ ] End-to-end payment flow: Iris sends €100 → Carlos receives notification
- [ ] Parse success rate >95% for Sabadell Bizum

---

### Phase 2: Remittance Flow (Week 3-4)

**Goal:** Add merchant cash-out and VES settlement

**Flow:**
```
Sender → Bizum → Escrow → OP_RETURN → Recipient →
Merchant → PagoMóvil → Settlement → BCH to LP
```

**Additional complexity:**
- Merchant device needs PagoMóvil parser
- Venezuelan bank SMS formats (Banesco, Mercantil, etc.)
- Settlement state machine
- Two-sided confirmation (merchant + recipient)

**Deliverables:**
- [ ] PagoMóvil parser works on merchant device (Venezuela)
- [ ] Merchant cash handoff flow validated
- [ ] Settlement completion triggers BCH payout
- [ ] Parse success rate >85% for Venezuelan banks

---

### Phase 3: Multi-Bank Support (Week 5-6)

**Goal:** Expand beyond Sabadell (Spain) and Banesco (Venezuela)

**Spain:**
- CaixaBank Bizum notifications
- BBVA Bizum notifications
- Santander Bizum notifications

**Venezuela:**
- Mercantil PagoMóvil SMS
- Provincial PagoMóvil SMS
- Bancaribe PagoMóvil SMS

**Deliverables:**
- [ ] Parsers for 3+ Spanish banks
- [ ] Parsers for 3+ Venezuelan banks
- [ ] Bank-specific regex patterns documented
- [ ] Fuzzy detector catches format variations

---

## Testing Levels

### 1. Unit Tests (Parser Logic)

**Test each parser in isolation with known notification formats.**

#### Bizum Parser Tests

```kotlin
class BizumParserTest {

    @Test
    fun `parse Sabadell Bizum notification - standard format`() {
        val notification = mockSabadellNotification(
            title = "Bizum",
            text = "Has recibido 100,00€ de +34612345678. Concepto: +34666123456"
        )

        val result = BizumParser.parse(notification)

        assertNotNull(result)
        assertEquals(100.0, result?.amountEur)
        assertEquals("+34666123456", result?.concept)
    }

    @Test
    fun `parse Sabadell Bizum notification - multiline format`() {
        val notification = mockSabadellNotification(
            title = "Bizum recibido",
            text = """
                Bizum recibido
                Importe: 100,00 €
                De: +34612345678
                Concepto: +34666123456
            """.trimIndent()
        )

        val result = BizumParser.parse(notification)

        assertNotNull(result)
        assertEquals(100.0, result?.amountEur)
        assertEquals("+34666123456", result?.concept)
    }

    @Test
    fun `parse amount with comma decimal separator`() {
        val text = "Has recibido 150,50€ de +34612345678. Concepto: +34666123456"
        val notification = mockSabadellNotification(text = text)

        val result = BizumParser.parse(notification)

        assertEquals(150.5, result?.amountEur)
    }

    @Test
    fun `parse large amount with thousands separator`() {
        val text = "Has recibido 1.234,56€ de +34612345678. Concepto: +34666123456"
        val notification = mockSabadellNotification(text = text)

        val result = BizumParser.parse(notification)

        assertEquals(1234.56, result?.amountEur)
    }

    @Test
    fun `ignore Bizum sent notifications`() {
        val notification = mockSabadellNotification(
            text = "Has enviado 100,00€ a +34612345678"  // "enviado" = sent
        )

        val result = BizumParser.parse(notification)

        assertNull(result)  // Should ignore sent notifications
    }

    @Test
    fun `ignore non-Bizum notifications`() {
        val notification = mockSabadellNotification(
            title = "Transferencia",
            text = "Transferencia recibida: 100€"
        )

        val result = BizumParser.parse(notification)

        assertNull(result)
    }

    @Test
    fun `extract concept with special characters`() {
        val text = "Has recibido 50,00€ de +34612345678. Concepto: +34-666-123-456"
        val notification = mockSabadellNotification(text = text)

        val result = BizumParser.parse(notification)

        assertTrue(result?.concept?.contains("666123456") == true)
    }
}
```

---

#### PagoMóvil Parser Tests

```kotlin
class PagoMovilParserTest {

    @Test
    fun `parse Banesco PagoMóvil SMS`() {
        val sms = """
            Recibiste Bs. 6.210,00 de 0414-123-4567 por PagoMovil.
            Ref: ASGAYA_settle_9kLmP
            Fecha: 27/04/26 10:45
        """.trimIndent()

        val result = PagoMovilParser.parse("BANESCO", sms)

        assertNotNull(result)
        assertEquals("settle_9kLmP", result?.settlementId)
        assertEquals(6210.0, result?.amountVes)
        assertEquals("04141234567", result?.senderPhone)
        assertEquals("Banesco", result?.bank)
    }

    @Test
    fun `parse Mercantil PagoMóvil SMS`() {
        val sms = """
            PagoMovil recibido: BsS 6210,00
            Telefono: 04141234567
            Referencia: ASGAYA_settle_9kLmP
        """.trimIndent()

        val result = PagoMovilParser.parse("MERCANTIL", sms)

        assertEquals("settle_9kLmP", result?.settlementId)
        assertEquals(6210.0, result?.amountVes)
    }

    @Test
    fun `parse amount with thousands separator`() {
        val sms = "Recibiste Bs. 15.210,50 de 0414123456. Ref: ASGAYA_settle_X"

        val result = PagoMovilParser.parse("BANESCO", sms)

        assertEquals(15210.5, result?.amountVes)
    }

    @Test
    fun `parse amount without cents (Provincial)`() {
        val sms = "Has recibido 6.210 Bs por PagoMovil. Concepto: ASGAYA_settle_X"

        val result = PagoMovilParser.parse("PROVINCIAL", sms)

        assertEquals(6210.0, result?.amountVes)
    }

    @Test
    fun `ignore non-Asgaya PagoMóvil`() {
        val sms = "Recibiste Bs. 100 de 0414123456. Ref: AlmuerzoAmigos"

        val result = PagoMovilParser.parse("BANESCO", sms)

        assertNull(result)  // No ASGAYA_ prefix in reference
    }

    @Test
    fun `ignore regular SMS`() {
        val sms = "Hola! Como estas?"

        val result = PagoMovilParser.parse("FRIEND", sms)

        assertNull(result)
    }

    @Test
    fun `handle unknown bank with generic parser`() {
        val sms = "Pago recibido Bs. 5000. Referencia: ASGAYA_settle_ABC"

        val result = PagoMovilParser.parse("UNKNOWN_BANK", sms)

        assertNotNull(result)  // Generic parser should still extract settlement ID
        assertEquals("settle_ABC", result?.settlementId)
        assertEquals(5000.0, result?.amountVes)
        assertEquals("Unknown", result?.bank)
    }
}
```

---

#### OP_RETURN Parser Tests

```kotlin
class OpReturnParserTest {

    @Test
    fun `parse ASGAYA_TXN_READY message`() {
        val message = "ASGAYA_TXN_READY_7382"

        val result = OpReturnParser.parse(message)

        assertEquals(NotificationType.TXN_READY, result?.type)
        assertEquals("7382", result?.data)
    }

    @Test
    fun `parse ASGAYA_TXN_COMPLETED message`() {
        val message = "ASGAYA_TXN_COMPLETED_txn_7Hk9mNpQ2wX"

        val result = OpReturnParser.parse(message)

        assertEquals(NotificationType.TXN_COMPLETED, result?.type)
        assertEquals("txn_7Hk9mNpQ2wX", result?.data)
    }

    @Test
    fun `parse ASGAYA_SETTLEMENT message`() {
        val message = "ASGAYA_SETTLEMENT_settle_9kLmP"

        val result = OpReturnParser.parse(message)

        assertEquals(NotificationType.SETTLEMENT, result?.type)
        assertEquals("settle_9kLmP", result?.data)
    }

    @Test
    fun `ignore non-Asgaya OP_RETURN`() {
        val message = "MEMO:Hello World"

        val result = OpReturnParser.parse(message)

        assertNull(result)
    }

    @Test
    fun `handle malformed ASGAYA message`() {
        val message = "ASGAYA_UNKNOWN_FORMAT"

        val result = OpReturnParser.parse(message)

        assertNull(result)  // Unknown format should return null
    }
}
```

---

### 2. Integration Tests (End-to-End Flows)

**Test complete flows with real backend integration.**

#### Payment Flow Integration Test

```kotlin
@Test
fun `end-to-end payment flow - Bizum to OP_RETURN`() = runTest {
    // Setup: Create transaction in backend
    val transaction = apiClient.createTransaction(
        senderAddress = "bitcoincash:sender...",
        recipientAddress = "bitcoincash:recipient...",
        amountEur = 100.0,
        recipientPhone = "+34666123456"
    )

    assertEquals("pending_payment", transaction.status)

    // Simulate: Sender sends Bizum
    val bizumNotification = mockBizumNotification(
        amount = 100.0,
        concept = "+34666123456"
    )

    // Action: Escrow NotificationListener processes
    handleBizumNotification(bizumNotification)

    // Wait for backend processing
    delay(2000)

    // Verify: Transaction status updated
    val updated = apiClient.getTransaction(transaction.id)
    assertEquals("payment_received", updated.status)

    // Verify: OP_RETURN notification sent to recipient
    // (Check BCH testnet for OP_RETURN transaction)
    val opReturnTx = waitForOpReturnNotification(
        recipientAddress = "bitcoincash:recipient...",
        timeout = 30_000  // 30 seconds
    )

    assertNotNull(opReturnTx)
    assertTrue(opReturnTx.opReturnData.contains("ASGAYA_TXN_READY"))
}
```

---

#### Remittance Flow Integration Test

```kotlin
@Test
fun `end-to-end remittance flow - EUR to VES`() = runTest {
    // 1. Create transaction
    val transaction = apiClient.createTransaction(...)

    // 2. Sender sends Bizum
    handleBizumNotification(mockBizumNotification(...))
    assertEquals("payment_received", apiClient.getTransaction(transaction.id).status)

    // 3. Recipient goes to merchant
    val merchantCode = apiClient.getMerchantCode(transaction.id)
    apiClient.merchantConfirmHandoff(merchantCode)

    // 4. Recipient confirms received cash
    apiClient.recipientConfirmReceived(transaction.id)
    assertEquals("cash_received", apiClient.getTransaction(transaction.id).status)

    // 5. Backend creates settlement for LP
    val settlement = waitForSettlement(
        merchantId = "merchant_123",
        timeout = 10_000
    )

    assertNotNull(settlement)

    // 6. LP sends PagoMóvil to merchant
    val pagoMovilSms = mockPagoMovilSms(
        amount = settlement.amount_ves,
        settlementId = settlement.id
    )

    // 7. Merchant device parses PagoMóvil
    handlePagoMovilNotification(pagoMovilSms)
    delay(2000)

    // 8. Verify settlement confirmed
    val updatedSettlement = apiClient.getSettlement(settlement.id)
    assertEquals("ves_confirmed", updatedSettlement.status)

    // 9. Verify BCH sent to LP
    assertEquals("bch_sent", waitForSettlementStatus(settlement.id, "bch_sent", 30_000))
}
```

---

### 3. Real-World Testing

**Most important: Test with actual bank notifications on real devices.**

#### Test Checklist: Bizum (Spain)

**Escrow device: Suso's phone (Sabadell account)**

- [ ] **Setup:**
  - [ ] Install Asgaya escrow app on Suso's phone
  - [ ] Grant NotificationListener permission
  - [ ] Verify app monitoring Sabadell notifications
  - [ ] Check backend connectivity

- [ ] **Test 1: Small amount (€10)**
  - [ ] Iris sends €10 Bizum to escrow
  - [ ] Concept: "+34666123456" (Carlos's phone)
  - [ ] Verify: Notification arrives within 5 seconds
  - [ ] Verify: Parser extracts amount=10, concept="+34666123456"
  - [ ] Verify: Backend confirms transaction
  - [ ] Verify: Carlos receives OP_RETURN notification

- [ ] **Test 2: Typical amount (€100)**
  - [ ] Iris sends €100 Bizum
  - [ ] Verify: Same flow as Test 1

- [ ] **Test 3: Large amount (€500)**
  - [ ] Iris sends €500 Bizum
  - [ ] Verify: Amount with comma separator parsed correctly
  - [ ] Verify: No anomaly flags (expected for beta testing)

- [ ] **Test 4: Decimal cents (€123.45)**
  - [ ] Iris sends €123.45 Bizum
  - [ ] Verify: Cents parsed correctly (123,45 → 123.45)

- [ ] **Test 5: Special characters in concept**
  - [ ] Concept: "+34-666-123-456" (with hyphens)
  - [ ] Verify: Still matches recipient phone

- [ ] **Test 6: Non-Asgaya Bizum (control)**
  - [ ] Iris sends €10 Bizum with different concept
  - [ ] Verify: Parser ignores (not flagged as Asgaya transaction)

- [ ] **Test 7: Sent Bizum (control)**
  - [ ] Suso sends Bizum to someone
  - [ ] Verify: Parser ignores "enviado" (sent) notifications

- [ ] **Parse Success Rate:**
  - [ ] Collect 20+ Bizum notifications over 1 week
  - [ ] Calculate: successful_parses / total_bizum_notifications
  - [ ] Target: >95%

---

#### Test Checklist: PagoMóvil (Venezuela)

**Merchant device: Partner's phone (Venezuelan bank account)**

- [ ] **Setup:**
  - [ ] Install Asgaya merchant app on partner's phone
  - [ ] Grant SMS permissions
  - [ ] Verify app monitoring SMS
  - [ ] Check backend connectivity

- [ ] **Test 1: Banesco PagoMóvil**
  - [ ] Send Bs. 6,210 PagoMóvil to merchant
  - [ ] Reference: "ASGAYA_settle_TEST1"
  - [ ] Verify: SMS arrives
  - [ ] Verify: Parser extracts amount=6210, settlementId="settle_TEST1"
  - [ ] Verify: Backend confirms settlement

- [ ] **Test 2: Mercantil PagoMóvil**
  - [ ] Same as Test 1, different bank
  - [ ] Verify: Mercantil-specific format parsed

- [ ] **Test 3: Provincial PagoMóvil**
  - [ ] Same as Test 1
  - [ ] Verify: Works even if amount has no cents

- [ ] **Test 4: Large amount (Bs. 50,000)**
  - [ ] Verify: Thousands separator handled correctly

- [ ] **Test 5: Non-Asgaya PagoMóvil (control)**
  - [ ] Receive PagoMóvil with different reference
  - [ ] Verify: Parser ignores

- [ ] **Parse Success Rate:**
  - [ ] Collect 20+ PagoMóvil SMS over 1 week
  - [ ] Calculate success rate by bank
  - [ ] Target: >85% overall

---

#### Test Checklist: OP_RETURN (BCH Testnet)

**All devices: Sender, recipient, merchant, LP**

- [ ] **Setup:**
  - [ ] All apps connected to BCH testnet
  - [ ] SPV wallets synced
  - [ ] Users have testnet addresses

- [ ] **Test 1: Transaction ready notification**
  - [ ] Backend sends: "ASGAYA_TXN_READY_7382"
  - [ ] To: Recipient's testnet address
  - [ ] Verify: Notification appears within 30 seconds
  - [ ] Verify: Message parsed correctly
  - [ ] Verify: User sees "Code: 7382" in app

- [ ] **Test 2: Transaction completed notification**
  - [ ] Backend sends: "ASGAYA_TXN_COMPLETED_txn_X"
  - [ ] To: Sender's address
  - [ ] Verify: Sender sees "Transaction complete"

- [ ] **Test 3: Settlement notification**
  - [ ] Backend sends: "ASGAYA_SETTLEMENT_settle_Y"
  - [ ] To: LP's address
  - [ ] Verify: LP sees "New settlement available"

- [ ] **Test 4: Dust received**
  - [ ] Verify: Users receive 546 sats with each notification
  - [ ] Verify: Wallet balance updates

- [ ] **Test 5: Multiple notifications**
  - [ ] Send 5 OP_RETURN notifications in quick succession
  - [ ] Verify: All displayed (no drops)

- [ ] **Test 6: Background sync**
  - [ ] Send notification while app closed
  - [ ] Open app
  - [ ] Verify: Notification appears after sync

- [ ] **Delivery Time:**
  - [ ] Measure: Time from broadcast to notification display
  - [ ] Target: <30 seconds average

---

### 4. Fuzzy Detector Testing

**Validate format change detection.**

#### Test Scenarios

```kotlin
@Test
fun `detect Sabadell format change`() {
    val detector = FuzzyFormatDetector()

    // Baseline: Known format
    val notification1 = mockSabadellNotification(
        text = "Has recibido 100,00€ de +34612345678. Concepto: +34666123456"
    )
    detector.checkNotification(notification1)  // No alert (first time)

    // Same format: No alert
    val notification2 = mockSabadellNotification(
        text = "Has recibido 50,00€ de +34611111111. Concepto: +34666999888"
    )
    detector.checkNotification(notification2)  // No alert (similar structure)

    // Changed format: Alert!
    val notification3 = mockSabadellNotification(
        text = "Bizum recibido: 100€. De: +34612345678. Ref: +34666123456"
    )

    val alert = detector.checkNotification(notification3)

    assertNotNull(alert)
    assertTrue(alert.similarity < 0.8)
    assertEquals("SABADELL", alert.bankId)
}
```

---

#### Manual Fuzzy Testing

- [ ] **Baseline collection:**
  - [ ] Collect 20+ Sabadell Bizum notifications
  - [ ] Train fuzzy detector on known patterns

- [ ] **Format variation testing:**
  - [ ] Wait for real bank format change (or simulate)
  - [ ] Verify: Fuzzy detector flags change
  - [ ] Verify: Alert sent to escrow operator
  - [ ] Verify: Manual fallback works during transition

- [ ] **False positive rate:**
  - [ ] Target: <5% false alerts on normal variations

---

## Performance Testing

### Latency Measurements

```kotlin
class PerformanceTest {

    @Test
    fun `measure Bizum parse latency`() {
        val notification = mockSabadellNotification(...)

        val start = System.nanoTime()
        val result = BizumParser.parse(notification)
        val end = System.nanoTime()

        val latencyMs = (end - start) / 1_000_000.0

        assertNotNull(result)
        assertTrue(latencyMs < 10)  // <10ms target
    }

    @Test
    fun `measure end-to-end payment flow latency`() = runTest {
        val start = System.currentTimeMillis()

        // 1. Bizum notification arrives
        handleBizumNotification(...)

        // 2. Wait for OP_RETURN notification
        waitForOpReturnNotification(...)

        val end = System.currentTimeMillis()
        val totalLatency = end - start

        assertTrue(totalLatency < 30_000)  // <30s target
    }
}
```

**Targets:**
- **Parser latency:** <10ms per notification
- **Backend API call:** <500ms
- **OP_RETURN delivery:** <30s (SPV sync dependent)
- **Total flow (Bizum → OP_RETURN):** <45s

---

## Failure Mode Testing

### Test Error Scenarios

- [ ] **Network failure during API call**
  - [ ] Disconnect network
  - [ ] Trigger Bizum notification
  - [ ] Verify: Retry with exponential backoff
  - [ ] Verify: Queued for background sync

- [ ] **Malformed notification**
  - [ ] Send notification with missing amount field
  - [ ] Verify: Parser returns null
  - [ ] Verify: Unparsed notification sent to backend
  - [ ] Verify: Manual fallback shown to user

- [ ] **Backend rejection (409 Conflict)**
  - [ ] Send duplicate Bizum notification
  - [ ] Verify: Backend returns 409
  - [ ] Verify: App handles gracefully (no error shown to user)

- [ ] **SPV wallet offline**
  - [ ] Disable internet
  - [ ] Send OP_RETURN notification
  - [ ] Re-enable internet
  - [ ] Verify: Notification appears after sync

---

## Monitoring During Testing

### Metrics to Track

```
Testing Dashboard (Live)
────────────────────────────────────
Bizum Parser
  ✅ Success: 47/50 (94%)
  ❌ Failures: 3
     • Missing concept field (1)
     • Amount format mismatch (2)
  ⏱️  Avg latency: 4ms

PagoMóvil Parser
  ✅ Success: 18/22 (82%)
  ❌ Failures: 4
     • Unknown bank format (3)
     • Missing reference (1)
  ⏱️  Avg latency: 6ms

OP_RETURN Monitor
  ✅ Delivered: 142/142 (100%)
  ⏱️  Avg delivery: 22s

Fuzzy Detector
  ⚠️  Format changes detected: 1
     • SABADELL (2026-04-15) - Review needed
```

---

## Related Documents

- **Index:** [NotificationListener Architecture](android-app/notification-listener/README.md) - Overview
- **Security:** [security.md](android-app/notification-listener/security.md) - Security testing
- **Monitoring:** [README.md](android-app/notification-listener/README.md) - Production metrics

---

*Created: April 28, 2026*
*Status: Draft (Testing Plan)*
*Philosophy: Start simple (payment flow), build complexity (remittance), real-world validation*
