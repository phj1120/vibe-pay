'use client';

import { useState, useEffect } from 'react';
import apiClient from '@/utils/api-client';
import { useRouter } from 'next/navigation';

interface PointHistory {
  pointHistoryNo: string;
  memberNo: string;
  amount: number;
  pointTransactionCode: string;
  pointTransactionReasonCode: string;
  pointTransactionReasonNo: string;
  startDateTime: string;
  endDateTime: string;
  upperPointHistoryNo: string;
  remainPoint: number;
  registDateTime: string;
}

export default function MemberDetailPage() {
  const [activeTab, setActiveTab] = useState('info'); // 'info', 'point', 'orders'
  const [memberInfo, setMemberInfo] = useState<{ name: string; email: string; phone: string; memberSince: string; } | null>(null);
  const [totalPoints, setTotalPoints] = useState<number>(0);
  const [pointHistory, setPointHistory] = useState<PointHistory[]>([]);
  const router = useRouter();

  useEffect(() => {
    const fetchMemberData = async () => {
      try {
        // Fetch member info (placeholder for now)
        setMemberInfo({
          name: 'John Doe',
          email: 'john.doe@example.com',
          phone: '010-1234-5678',
          memberSince: '2023-01-01',
        });

        // Fetch total available points
        const totalPointsResponse = await apiClient.get('/points/total');
        setTotalPoints(totalPointsResponse.data);

        // Fetch point history
        const pointHistoryResponse = await apiClient.get('/points/history');
        setPointHistory(pointHistoryResponse.data);

      } catch (error) {
        console.error('Failed to fetch member data:', error);
        // Optionally redirect to login if token is invalid/expired and refresh failed
        // router.push('/login');
      }
    };
    fetchMemberData();
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
            <p className="text-xl mb-4">Total Available Points: <strong>{totalPoints}</strong></p>

            <h3 className="text-xl font-semibold mb-2">Point History</h3>
            {pointHistory.length > 0 ? (
              <ul className="space-y-2">
                {pointHistory.map((item) => (
                  <li key={item.pointHistoryNo} className="border border-gray-700 p-3 rounded-lg">
                    <p><strong>Amount:</strong> {item.amount} ({item.pointTransactionCode === '001' ? 'Earned' : 'Used'})</p>
                    <p><strong>Reason:</strong> {item.pointTransactionReasonCode}</p>
                    <p><strong>Date:</strong> {new Date(item.registDateTime).toLocaleDateString()}</p>
                    {item.remainPoint !== undefined && <p><strong>Remaining:</strong> {item.remainPoint}</p>}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No point history found.</p>
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
