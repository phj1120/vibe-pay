
export default function OrderFailPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24 bg-black text-white">
      <h1 className="text-4xl font-bold text-red-500">Order Failed!</h1>
      <p className="text-lg">Unfortunately, your order could not be processed.</p>
      <button className="mt-8 px-6 py-3 bg-gray-700 rounded hover:bg-gray-600">Return to Cart</button>
    </main>
  );
}
