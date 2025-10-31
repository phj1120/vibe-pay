# Required Dependencies for Order/Payment Feature

Please install the following npm packages:

```bash
cd /Users/parkh/Dev/git/Project/vibe-pay/fo

# Install required dependencies
pnpm install js-cookie
pnpm install react-hook-form @hookform/resolvers
pnpm install @radix-ui/react-label @radix-ui/react-radio-group @radix-ui/react-separator
pnpm install lucide-react

# Install type definitions
pnpm install -D @types/js-cookie
```

## Dependencies Overview

- **js-cookie**: Cookie management for order data storage
- **react-hook-form**: Form state management
- **@hookform/resolvers**: Zod integration for form validation
- **@radix-ui/react-***: UI component primitives
- **lucide-react**: Icon library
- **@types/js-cookie**: TypeScript definitions
