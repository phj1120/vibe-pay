# Quick Start: Order & Payment Feature

## 1. Install Dependencies (5 min)

```bash
cd /Users/parkh/Dev/git/Project/vibe-pay/fo

pnpm install js-cookie react-hook-form @hookform/resolvers \
  @radix-ui/react-label @radix-ui/react-radio-group \
  @radix-ui/react-separator lucide-react

pnpm install -D @types/js-cookie
```

## 2. Start Development Server

```bash
pnpm run dev
```

Visit: http://localhost:3000

## 3. Test Flow (5 min)

### Step 1: Add items to basket
1. Go to homepage
2. Add products to basket
3. Go to `/basket`

### Step 2: Create order
1. Select products
2. Click "주문하기"
3. Fill in order form
4. Select payment method
5. Click "결제하기"

### Step 3: Payment (in popup)
1. Popup opens automatically
2. PG window loads
3. Complete test payment
4. Popup closes

### Step 4: Confirmation
1. Order complete page shows
2. Check order details

## 4. Backend API Requirements

Ensure these endpoints are implemented:

```
GET  /api/order/generateOrderNumber
POST /api/payments/initiate
POST /api/order/order
```

See `/docs/domain/order/order-add.md` for detailed API specs.

## 5. Common Issues

### Popup Blocked
- Enable popups in browser settings

### Cookie Not Found
- Check expiration (5 minutes)

### PG Script Error
- Check internet connection

---

**Ready to start!** 🚀
