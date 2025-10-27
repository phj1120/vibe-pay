# VibePay Requirements Documentation

## Overview
This directory contains comprehensive requirements extracted from the existing VibePay codebase through reverse engineering. These documents serve as the blueprint for rebuilding the project with improved code quality and adherence to conventions.

## Document Index

### Phase 1: Data Layer
**File:** [phase1-data-layer.md](./phase1-data-layer.md) (917 lines)

**Contents:**
- Database schema for all 8 tables
- Entity classes (Member, Product, RewardPoints, PointHistory, Order, OrderItem, Payment, PaymentLog)
- Enum definitions (PaymentMethod, PaymentStatus, PayType, PgCompany, OrderStatus, TransactionType)
- DTO specifications
- MyBatis Mapper interfaces and XML templates
- Complete data layer foundation

**Purpose:** Establish consistent data structures across entire application in one phase

---

### Phase 2-1: Member Domain
**File:** [phase2-member-domain.md](./phase2-member-domain.md) (573 lines)

**Contents:**
- MemberService business logic
- MemberController REST API (5 endpoints)
- Frontend: pages/members/index.vue (list page)
- Frontend: pages/members/[id].vue (detail page)
- CRUD operations with validation

**Complexity:** ⭐ Simple (good starting point)

---

### Phase 2-2: Product Domain
**File:** [phase2-product-domain.md](./phase2-product-domain.md) (601 lines)

**Contents:**
- ProductService business logic
- ProductController REST API (5 endpoints)
- Frontend: pages/products/index.vue (grid view)
- Frontend: pages/products/[id].vue (detail page)
- Product catalog management

**Complexity:** ⭐ Simple

---

### Phase 2-3: RewardPoints Domain
**File:** [phase2-rewardpoints-domain.md](./phase2-rewardpoints-domain.md) (721 lines)

**Contents:**
- RewardPointsService (earn, use, refund operations)
- PointHistoryService (transaction logging)
- RewardPointsController and PointHistoryController APIs
- Enhancement to pages/members/[id].vue (points section)
- Point statistics and history display

**Complexity:** ⭐⭐ Moderate

---

### Phase 2-4: Payment Domain
**File:** [phase2-payment-domain.md](./phase2-payment-domain.md) (600 lines)

**Contents:**
- Factory Pattern: PaymentProcessorFactory, PaymentGatewayFactory
- Adapter Pattern: InicisAdapter, NicePayAdapter, TossAdapter
- Strategy Pattern: CreditCardPaymentProcessor, PointPaymentProcessor
- PaymentService with multi-PG support
- Weighted PG selection (PgWeightSelector)
- Utility classes: HashUtils, WebClientUtil
- Frontend: pages/order/index.vue (order form)
- Frontend: pages/order/popup.vue (PG popup)
- Frontend: plugins/inicis.client.ts, nicepay.client.ts

**Complexity:** ⭐⭐⭐⭐ Complex (~20 files)

---

### Phase 2-5: Order Domain
**File:** [phase2-order-domain.md](./phase2-order-domain.md) (625 lines)

**Contents:**
- Command Pattern: CreateOrderCommand, CancelOrderCommand, OrderCommandInvoker
- OrderService with net cancel logic
- OrderController REST API
- Frontend: pages/order/progress-popup.vue (order creation)
- Frontend: pages/order/complete.vue (success page)
- Frontend: pages/order/failed.vue (error page)
- Two-phase order creation
- Automatic payment rollback on failure

**Complexity:** ⭐⭐⭐ Moderate-Complex

---

### Data Requirements
**File:** [data-requirements.md](./data-requirements.md) (381 lines)

**Contents:**
- Initial test data (3 members, 10 products)
- Reward points initialization
- Domain relationship mappings
- Scenario-specific data requirements
- SQL scripts for data loading
- Data cleanup scripts

**Purpose:** Define all data needed for testing and development

---

### Test Scenarios
**File:** [test-scenarios.md](./test-scenarios.md) (671 lines)

**Contents:**
- 10 comprehensive test scenarios
- INICIS/NICEPAY payment flows
- Point payment testing
- Mixed payment (point + card)
- Weighted PG selection
- Net cancel testing
- Order cancellation
- CRUD operation tests
- Error handling scenarios

**Purpose:** Validate all functionality works correctly

---

## Total Statistics

- **Total Lines:** 5,089
- **Total Files:** 8 requirements documents
- **Estimated Implementation:**
  - Phase 1: ~40 files (data layer)
  - Phase 2: ~60 files (business logic + frontend)
  - **Total:** ~100 files

## Implementation Sequence

### Recommended Order
1. ✅ **Phase 1** - Complete data layer in one session
2. ✅ **Phase 2-1** - Member (simplest, establishes patterns)
3. ✅ **Phase 2-2** - Product (similar to Member)
4. ✅ **Phase 2-3** - RewardPoints (builds on Member)
5. ✅ **Phase 2-4** - Payment (most complex, ~20 files)
6. ✅ **Phase 2-5** - Order (depends on Payment)
7. ✅ **Testing** - Run all test scenarios

### Context Management
- **Phase 1:** Single context (data consistency critical)
- **Phase 2:** New context per domain (reduces noise)
- **Never modify** Phase 1 data layer in Phase 2

## Key Features Documented

### Design Patterns
- ✅ Factory Pattern (Payment processors and gateways)
- ✅ Adapter Pattern (Multi-PG integration)
- ✅ Strategy Pattern (Payment methods)
- ✅ Command Pattern (Order operations with rollback)

### Business Logic
- ✅ Multi-PG support (INICIS, NicePay, Toss)
- ✅ Weighted PG selection
- ✅ Point payment system
- ✅ Mixed payment (point + card)
- ✅ Net cancel (automatic payment rollback)
- ✅ Order cancellation with refunds
- ✅ Comprehensive audit logging

### Frontend Features
- ✅ Popup-based payment (SSR compatible)
- ✅ PostMessage communication
- ✅ Dynamic popup sizing per PG
- ✅ Progress indicators
- ✅ Success/failure pages
- ✅ Point balance display
- ✅ Transaction history

## Conventions Applied

All requirements follow conventions defined in:
- `/docs/conventions/common.md` (naming, encoding, git, comments)
- `/docs/conventions/api.md` (REST API design, endpoints, responses)
- `/docs/conventions/fo.md` (components, layout, styling, events)
- `/docs/conventions/query.md` (SQL, indexes, migrations)

## Configuration

### application.yml Highlights
```yaml
# PG Credentials (test mode)
inicis:
  mid: INIpayTest
  signKey: SU5JTElURV9UUklQTEVERVNfS0VZU1RS

nicepay:
  mid: nicepay00m
  merchantKey: EYzu8jGGMfq...

# Weighted Selection
payment:
  weight:
    inicis: 50
    nicepay: 50
```

## Testing Strategy

### Unit Tests
- Service layer business logic
- Utility functions (HashUtils, PgWeightSelector)
- Point calculations

### Integration Tests
- API endpoints (Postman collections)
- Database operations
- PG integrations

### E2E Tests
- Full payment flows (browser)
- Order creation and cancellation
- Point earn/use/refund

## Success Criteria

Project rebuild is successful when:
- ✅ All 8 tables exist and functional
- ✅ All Entity/DTO classes compile
- ✅ All Mapper XMLs valid
- ✅ All API endpoints return correct responses
- ✅ All frontend pages render correctly
- ✅ All 10 test scenarios pass
- ✅ 100% convention compliance
- ✅ No compilation errors
- ✅ Design patterns correctly implemented

## Document Usage

### For Developers
1. Read Phase 1 first (understand data structures)
2. Review domain documents in sequence
3. Reference data requirements for testing
4. Use test scenarios to validate implementation

### For Project Managers
- Each document = clear deliverable
- Line counts indicate complexity
- Test scenarios = acceptance criteria

### For QA
- Test scenarios provide test cases
- Data requirements define test data
- Expected results clearly specified

## Maintenance

When updating:
1. Keep all documents synchronized
2. Update README if adding documents
3. Maintain line count accuracy
4. Reference CLAUDE.md for architecture changes

## Related Documents

- [CLAUDE.md](../../CLAUDE.md) - Project overview and architecture
- [plan.md](../plan.md) - Reverse engineering and rebuild strategy
- [prompts.md](../prompts.md) - Executable prompts for Claude Code

## Notes

- All code examples follow actual implementation
- SQL scripts tested on PostgreSQL
- Frontend examples use Nuxt.js 3 + Vuetify 3
- All PG integrations use test credentials
- PostMessage protocol defined for popup communication
- Net cancel logic extensively documented
- Point system ensures auditability

---

**Generated:** 2025-01-27
**Source:** VibePay codebase (87 Java files + 12 Vue files)
**Method:** Reverse engineering via Claude Code
**Status:** ✅ Complete - Ready for implementation
