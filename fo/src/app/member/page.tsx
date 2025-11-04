'use client';

import { useState, useEffect } from 'react';
import apiClient from '@/utils/api-client';
import { useRouter } from 'next/navigation';

export default function MemberDetailPage() {
  const [activeTab, setActiveTab] = useState('info'); // 'info', 'point', 'orders'
  const [memberInfo, setMemberInfo] = useState(null); // Placeholder for member data
  const router = useRouter();

  useEffect(() => {
    // In a real application, you would fetch member info here
    // For now, we'll use a placeholder
    const fetchMemberInfo = async () => {
      try {
        // const response = await apiClient.get('/members/me'); // Example API call
        // setMemberInfo(response.data);
        setMemberInfo({
          name: 'John Doe',
          email: 'john.doe@example.com',
          phone: '010-1234-5678',
          memberSince: '2023-01-01',
          points: 1250,
        });
      } catch (error) {
        console.error('Failed to fetch member info:', error);
        // Optionally redirect to login if token is invalid/expired and refresh failed
        // router.push('/login');
      }
    };
    fetchMemberInfo();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    router.push('/login');
  };

  return (
    <main className="flex min-h-screen flex-col items-center p-8 bg-black text-white">
      <h1 className="text-4xl font-bold mb-8">Member Detail Page</h1>

      <div className="w-full max-w-3xl bg-gray-900 p-6 rounded-lg shadow-lg">
        <div className="flex border-b border-gray-700 mb-4">
          <button
            className={`py-2 px-4 ${activeTab === 'info' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-400'}`}
            onClick={() => setActiveTab('info')}
          >
            Member Info
          </button>
          <button
            className={`py-2 px-4 ${activeTab === 'point' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-400'}`}
            onClick={() => setActiveTab('point')}
          >
            Points
          </button>
          <button
            className={`py-2 px-4 ${activeTab === 'orders' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-400'}`}
            onClick={() => setActiveTab('orders')}
          >
            Order History
          </button>
        </div>

        {activeTab === 'info' && (
          <div>
            <h2 className="text-2xl font-semibold mb-4">My Information</h2>
            {memberInfo ? (
              <div className="space-y-2">
                <p><strong>Name:</strong> {memberInfo.name}</p>
                <p><strong>Email:</strong> {memberInfo.email}</p>
                <p><strong>Phone:</strong> {memberInfo.phone}</p>
                <p><strong>Member Since:</strong> {memberInfo.memberSince}</p>
              </div>
            ) : (
              <p>Loading member information...</p>
            )}
          </div>
        )}

        {activeTab === 'point' && (
          <div>
            <h2 className="text-2xl font-semibold mb-4">My Points</h2>
            {memberInfo ? (
              <p className="text-xl">You have <strong>{memberInfo.points}</strong> points.</p>
            ) : (
              <p>Loading point information...</p>
            )}
          </div>
        )}

        {activeTab === 'orders' && (
          <div>
            <h2 className="text-2xl font-semibold mb-4">My Order History</h2>
            <p>No orders found. (This section will be implemented later)</p>
          </div>
        )}

        <button
          onClick={handleLogout}
          className="mt-8 px-6 py-3 bg-red-700 rounded hover:bg-red-600 text-xl font-semibold w-full"
        >
          Logout
        </button>
      </div>
    </main>
  );
}