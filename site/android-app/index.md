← [Back to Home](README.md)

# Android App Documentation

Complete specification for the Asgaya Android application.

## Sections

### [User Flows](flows/) — 5 documents
How users interact with the app:
- [Remittance & Merchant Cash-Out](flows/archive/remittance-merchant-cash-out.md) - Initiating remittances
- [BCH Payment Flows](flows/archive/bch-payment-flows.md) - Direct BCH payments
- [Recipient Flows](flows/recipient-flows.md) - Receiving cash
- [Merchant Flows](flows/merchant-flows.md) - Providing liquidity

### [Backend APIs](backend-apis/) — 9 documents
REST endpoints and communication patterns:
- Rate, Transaction, Settlement, Merchant, User APIs
- BCH-native architecture (OP_RETURN notifications, BCH signature auth)

### [NotificationListener](notification-listener/) — 6 documents
The "heart of the app" - bridges fiat and BCH networks:
- Bizum (Spain EUR)
- PagoMóvil (Venezuela VES)
- OP_RETURN (BCH blockchain)

## Status

**Phase:** Documentation Complete (Seeking Review)

**Implementation:** Not started (docs-first approach)

**Next:** Phase 1 implementation (Payment Flow)
