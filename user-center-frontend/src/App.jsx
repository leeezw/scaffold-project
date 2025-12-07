import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthContext } from './hooks/AuthProvider.jsx';
import Login from './pages/Login.jsx';
import UserList from './pages/UserList.jsx';
import RoleList from './pages/RoleList.jsx';
import PermissionList from './pages/PermissionList.jsx';
import SessionList from './pages/SessionList.jsx';
import TenantList from './pages/TenantList.jsx';
import AppLayout from './components/AppLayout.jsx';

function PrivateRoute({ children }) {
  const { token } = useAuthContext();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={(
          <PrivateRoute>
            <AppLayout />
          </PrivateRoute>
        )}
      >
        <Route index element={<UserList />} />
        <Route path="roles" element={<RoleList />} />
        <Route path="permissions" element={<PermissionList />} />
        <Route path="sessions" element={<SessionList />} />
        <Route path="tenants" element={<TenantList />} />
      </Route>
    </Routes>
  );
}
