'use client';

import React, { createContext, useContext, useState, ReactNode } from 'react';
import { Member } from '@/types/member';

// 인증 상태 타입
export interface AuthState {
  isAuthenticated: boolean;
  user: Member | null;
  loading: boolean;
}

// 인증 컨텍스트 타입
interface AuthContextType {
  state: AuthState;
  login: (user: Member) => void;
  logout: () => void;
  updateUser: (user: Partial<Member>) => void;
  checkAuth: () => Promise<void>;
}

// 초기 상태
const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  loading: true,
};

// 컨텍스트 생성
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// 프로바이더 컴포넌트
export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(initialState);

  // 초기 인증 상태 확인
  React.useEffect(() => {
    checkAuth();
  }, []);

  // 로그인
  const login = (user: Member) => {
    setState({
      isAuthenticated: true,
      user,
      loading: false,
    });

    // TODO: 실제 토큰 저장 로직 구현
    // localStorage.setItem('authToken', token);
  };

  // 로그아웃
  const logout = () => {
    setState({
      isAuthenticated: false,
      user: null,
      loading: false,
    });

    // TODO: 실제 토큰 제거 로직 구현
    // localStorage.removeItem('authToken');
  };

  // 사용자 정보 업데이트
  const updateUser = (userData: Partial<Member>) => {
    if (state.user) {
      setState(prev => ({
        ...prev,
        user: { ...prev.user!, ...userData },
      }));
    }
  };

  // 인증 상태 확인
  const checkAuth = async () => {
    try {
      // TODO: 실제 인증 확인 로직 구현
      // 현재는 System Design Document에 따라 인증 기능이 비활성화됨

      // const token = localStorage.getItem('authToken');
      // if (token) {
      //   // API를 통해 사용자 정보 확인
      //   const user = await getUserInfo();
      //   setState({
      //     isAuthenticated: true,
      //     user,
      //     loading: false,
      //   });
      // } else {
      //   setState({
      //     isAuthenticated: false,
      //     user: null,
      //     loading: false,
      //   });
      // }

      // 현재는 인증 없이 사용
      setState({
        isAuthenticated: false, // 인증 기능 비활성화
        user: null,
        loading: false,
      });
    } catch (error) {
      console.error('Auth check failed:', error);
      setState({
        isAuthenticated: false,
        user: null,
        loading: false,
      });
    }
  };

  const value: AuthContextType = {
    state,
    login,
    logout,
    updateUser,
    checkAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// 훅
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// 인증 상태 확인 훅
export const useRequireAuth = () => {
  const { state } = useAuth();

  React.useEffect(() => {
    // TODO: 인증이 필요한 페이지에서 리다이렉트 로직 구현
    // if (!state.loading && !state.isAuthenticated) {
    //   router.push('/login');
    // }
  }, [state.loading, state.isAuthenticated]);

  return state;
};