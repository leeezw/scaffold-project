import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';

const TOKEN_KEY = 'uc_token';
const TENANT_KEY = 'uc_tenant_id';
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [tenantId, setTenantId] = useState(() => localStorage.getItem(TENANT_KEY) || '');
  const [user, setUser] = useState(() => {
    const cached = localStorage.getItem('uc_user');
    return cached ? JSON.parse(cached) : null;
  });

  useEffect(() => {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  }, [token]);

  useEffect(() => {
    if (tenantId) {
      localStorage.setItem(TENANT_KEY, tenantId);
    } else {
      localStorage.removeItem(TENANT_KEY);
    }
  }, [tenantId]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('uc_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('uc_user');
    }
  }, [user]);

  const value = useMemo(
    () => ({ token, setToken, user, setUser, tenantId, setTenantId }),
    [token, user, tenantId]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthContext() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return ctx;
}
