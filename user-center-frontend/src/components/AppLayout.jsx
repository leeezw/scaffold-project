import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuthContext } from '../hooks/AuthProvider.jsx';

export default function AppLayout() {
  const { user, setUser, setToken } = useAuthContext();
  const navigate = useNavigate();

  const logout = () => {
    setToken(null);
    setUser(null);
    navigate('/login', { replace: true });
  };

  return (
    <div className="layout">
      <aside>
        <div className="logo">User Center</div>
        <nav>
          <NavLink to="/" end>
            用户
          </NavLink>
          <NavLink to="/roles">角色</NavLink>
          <NavLink to="/sessions">Session</NavLink>
        </nav>
      </aside>
      <main>
        <header className="topbar">
          <div>
            欢迎，{user?.nickname || user?.username}
          </div>
          <button onClick={logout}>退出</button>
        </header>
        <section className="content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
