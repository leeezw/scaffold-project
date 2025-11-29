import { Outlet, useNavigate } from 'react-router-dom';
import { useAuthContext } from '../hooks/AuthProvider.jsx';
import SidebarMenu from './SidebarMenu.jsx';
import './AppLayout.css';

// 菜单配置 - 可以从数据库获取
const menuItems = [
  {
    key: 'users',
    label: '用户',
    path: '/',
    icon: (
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
        <path d="M2.25 6.75L9 1.5L15.75 6.75V15C15.75 15.3978 15.592 15.7794 15.3107 16.0607C15.0294 16.342 14.6478 16.5 14.25 16.5H3.75C3.35218 16.5 2.97064 16.342 2.68934 16.0607C2.40804 15.7794 2.25 15.3978 2.25 15V6.75Z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        <path d="M6.75 16.5V9H11.25V16.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },
  {
    key: 'roles',
    label: '角色',
    path: '/roles',
    icon: (
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
        <path d="M12.75 3.75C13.7446 3.75 14.6984 4.14509 15.4017 4.84835C16.1049 5.55161 16.5 6.50544 16.5 7.5C16.5 8.49456 16.1049 9.44839 15.4017 10.1517C14.6984 10.8549 13.7446 11.25 12.75 11.25M12.75 3.75C11.7554 3.75 10.8016 4.14509 10.0983 4.84835C9.39509 5.55161 9 6.50544 9 7.5C9 8.49456 9.39509 9.44839 10.0983 10.1517C10.8016 10.8549 11.7554 11.25 12.75 11.25M12.75 3.75C10.6789 3.75 9 5.42893 9 7.5C9 9.57107 10.6789 11.25 12.75 11.25M12.75 3.75C14.8211 3.75 16.5 5.42893 16.5 7.5C16.5 9.57107 14.8211 11.25 12.75 11.25M1.5 15C1.5 12.9289 3.17893 11.25 5.25 11.25M5.25 11.25C7.32107 11.25 9 12.9289 9 15M5.25 11.25C3.17893 11.25 1.5 12.9289 1.5 15M5.25 11.25C5.25 9.17893 6.92893 7.5 9 7.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },
  {
    key: 'sessions',
    label: 'Session',
    path: '/sessions',
    icon: (
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
        <path d="M9 1.5V9M9 16.5C5.27208 16.5 2.25 13.4779 2.25 9.75C2.25 6.02208 5.27208 3 9 3C12.7279 3 15.75 6.02208 15.75 9.75C15.75 13.4779 12.7279 16.5 9 16.5Z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },
];

// 退出登录菜单项
const logoutItem = {
  key: 'logout',
  label: '退出登录',
  path: '#',
  icon: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
      <path d="M7.5 17.5H4.16667C3.72464 17.5 3.30072 17.3244 2.98816 17.0118C2.67559 16.6993 2.5 16.2754 2.5 15.8333V4.16667C2.5 3.72464 2.67559 3.30072 2.98816 2.98816C3.30072 2.67559 3.72464 2.5 4.16667 2.5H7.5M13.3333 14.1667L17.5 10M17.5 10L13.3333 5.83333M17.5 10H7.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
};

export default function AppLayout() {
  const { user, setUser, setToken } = useAuthContext();
  const navigate = useNavigate();

  const handleLogout = () => {
    setToken(null);
    setUser(null);
    navigate('/login', { replace: true });
  };

  const handleMenuClick = (item) => {
    if (item.key === 'logout') {
      handleLogout();
    }
  };

  return (
    <div className="app-layout">
      {/* 左侧菜单栏 */}
      <aside className="sidebar">
        <div className="sidebar-content">
          {/* Logo 区域 */}
          <div className="sidebar-logo">
            <div className="logo-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <rect width="32" height="32" rx="8" fill="#3f8cff"/>
                <path d="M16 8L24 14V24C24 24.5304 23.7893 25.0391 23.4142 25.4142C23.0391 25.7893 22.5304 26 22 26H10C9.46957 26 8.96086 25.7893 8.58579 25.4142C8.21071 25.0391 8 24.5304 8 24V14L16 8Z" fill="white"/>
                <path d="M12 26V16H20V26" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </div>
            <div className="logo-text">
              <div className="logo-title">用户中心</div>
              <div className="logo-subtitle">User Center</div>
            </div>
          </div>
          
          <SidebarMenu items={menuItems} onItemClick={handleMenuClick} />
          <div className="sidebar-footer">
            <SidebarMenu items={[logoutItem]} onItemClick={handleMenuClick} />
          </div>
        </div>
      </aside>

      {/* 主内容区 */}
      <main className="main-content">
        <header className="topbar">
          <div className="topbar-content">
            <div>
              欢迎，{user?.nickname || user?.username}
            </div>
          </div>
        </header>
        <section className="content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
