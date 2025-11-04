
export default function CartPage() {
  const cartItems = [
    { id: 1, name: 'Product A', price: '₩15,000', quantity: 1 },
    { id: 2, name: 'Product B', price: '₩25,000', quantity: 2 },
  ];

  return (
    <main className="flex min-h-screen flex-col items-center p-8 bg-black text-white">
      <h1 className="text-4xl font-bold mb-8">Cart Screen</h1>
      <div className="w-full max-w-2xl">
        {cartItems.length === 0 ? (
          <p>Your cart is empty.</p>
        ) : (
          <div className="space-y-4">
            {cartItems.map((item) => (
              <div key={item.id} className="flex justify-between items-center border border-gray-700 p-4 rounded-lg">
                <div>
                  <h2 className="text-xl font-semibold">{item.name}</h2>
                  <p className="text-gray-400">{item.price} x {item.quantity}</p>
                </div>
                <button className="px-4 py-2 bg-red-700 rounded hover:bg-red-600">Remove</button>
              </div>
            ))}
            <div className="flex justify-between items-center border-t border-gray-700 pt-4 mt-4">
              <h2 className="text-2xl font-bold">Total:</h2>
              <p className="text-2xl font-bold">₩65,000</p>
            </div>
            <button className="w-full mt-6 px-6 py-3 bg-blue-700 rounded hover:bg-blue-600 text-xl font-semibold">Proceed to Checkout</button>
          </div>
        )}
      </div>
    </main>
  );
}
