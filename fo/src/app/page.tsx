export default function Home() {
  const products = [
    { id: 1, name: 'Product 1', price: '₩10,000', image: '/next.svg' },
    { id: 2, name: 'Product 2', price: '₩20,000', image: '/next.svg' },
    { id: 3, name: 'Product 3', price: '₩30,000', image: '/next.svg' },
  ];

  return (
    <main className="flex min-h-screen flex-col items-center p-8 bg-black text-white">
      <h1 className="text-4xl font-bold mb-8">Main Screen - Product List</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product) => (
          <div key={product.id} className="border border-gray-700 p-4 rounded-lg flex flex-col items-center">
            {/* Placeholder for product image */}
            <div className="w-32 h-32 bg-gray-800 mb-4 flex items-center justify-center text-gray-500">
              Image
            </div>
            <h2 className="text-xl font-semibold mb-2">{product.name}</h2>
            <p className="text-lg text-gray-400">{product.price}</p>
            <button className="mt-4 px-4 py-2 bg-gray-700 rounded hover:bg-gray-600">Add to Cart</button>
          </div>
        ))}
      </div>
    </main>
  );
}

