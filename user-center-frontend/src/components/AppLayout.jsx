import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect, useRef, useMemo } from 'react';
import { Input, Button, Dropdown, Avatar, Badge, Breadcrumb } from 'antd';
import { 
  SearchOutlined, 
  BellOutlined, 
  UserOutlined, 
  LogoutOutlined,
  DownOutlined,
  HomeOutlined,
  TeamOutlined,
  ClockCircleOutlined
} from '@ant-design/icons';
import { useAuthContext } from '../hooks/AuthProvider.jsx';
import SidebarMenu from './SidebarMenu.jsx';
import './AppLayout.css';

// 菜单配置 - 可以从数据库获取
const menuItems = [
  {
    key: 'users',
    label: '用户',
    path: '/',
    icon: HomeOutlined,
  },
  {
    key: 'roles',
    label: '角色',
    path: '/roles',
    icon: TeamOutlined,
  },
  {
    key: 'sessions',
    label: 'Session',
    path: '/sessions',
    icon: ClockCircleOutlined,
  },
];

// 退出登录菜单项
const logoutItem = {
  key: 'logout',
  label: '退出登录',
  path: '#',
  icon: LogoutOutlined,
};

export default function AppLayout() {
  const { user, setUser, setToken } = useAuthContext();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchValue, setSearchValue] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const userMenuRef = useRef(null);

  // 面包屑配置
  const breadcrumbMap = {
    '/': { title: '用户管理', icon: HomeOutlined },
    '/roles': { title: '角色管理', icon: TeamOutlined },
    '/sessions': { title: 'Session管理', icon: ClockCircleOutlined },
  };

  // 生成面包屑数据
  const breadcrumbItems = useMemo(() => {
    const items = [
      {
        title: (
          <span 
            style={{ display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer' }}
            onClick={() => navigate('/')}
          >
            <HomeOutlined />
            <span>首页</span>
          </span>
        ),
      },
    ];

    const currentPath = location.pathname;
    if (currentPath !== '/' && breadcrumbMap[currentPath]) {
      const config = breadcrumbMap[currentPath];
      items.push({
        title: (
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <config.icon />
            <span>{config.title}</span>
          </span>
        ),
      });
    }

    return items;
  }, [location.pathname, navigate]);

  // 点击外部区域关闭下拉菜单
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target)) {
        setShowUserMenu(false);
      }
    };

    if (showUserMenu) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showUserMenu]);

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

  const handleSearch = (e) => {
    e.preventDefault();
    // 搜索逻辑可以在这里实现
    console.log('搜索:', searchValue);
  };

  // 用户下拉菜单项
  const userMenuItems = [
    {
      key: 'logout',
      label: (
        <div onClick={handleLogout} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <LogoutOutlined />
          <span>退出登录</span>
        </div>
      ),
    },
  ];

  return (
    <div className="app-layout">
      {/* 左侧菜单栏 */}
      <aside className="sidebar">
        <div className="sidebar-content">
          {/* Logo 区域 */}
          <div className="sidebar-logo">
            <div className="logo-icon">
              <div style={{ 
                width: '32px', 
                height: '32px', 
                borderRadius: '8px', 
                background: '#3f8cff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontSize: '18px',
                fontWeight: 'bold'
              }}>
                U
              </div>
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
        <header className="toolbar">
          <div className="toolbar-content">
            {/* 左侧搜索框 */}
            <Input
              className="toolbar-search"
              placeholder="搜索用户名、昵称或邮箱"
              prefix={<SearchOutlined />}
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              onPressEnter={handleSearch}
              allowClear
            />

            {/* 右侧工具栏 */}
            <div className="toolbar-actions">
              {/* 通知按钮 */}
              <Badge count={0} size="small">
                <Button
                  type="text"
                  icon={<BellOutlined />}
                  className="toolbar-btn-icon"
                  title="通知"
                />
              </Badge>

              {/* 用户资料下拉 */}
              <Dropdown
                menu={{ items: userMenuItems }}
                trigger={['click']}
                placement="bottomRight"
                classNames={{ root: 'user-profile-dropdown-menu' }}
              >
                <Button
                  type="text"
                  className="toolbar-btn-user"
                  style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '8px',
                    height: '40px',
                    padding: '0 12px'
                  }}
                >
                  <Avatar 
                    size={32} 
                    icon={<UserOutlined />}
                    style={{ backgroundColor: '#e2e8f0', color: '#64748b' }}
                  />
                  <span className="user-name">{user?.nickname || user?.username || 'User'}</span>
                  <DownOutlined style={{ fontSize: '12px', color: '#64748b' }} />
                </Button>
              </Dropdown>
            </div>
          </div>
        </header>

        {/* 面包屑导航 */}
        <nav className="breadcrumb-nav">
          <Breadcrumb
            items={breadcrumbItems}
            separator="/"
            className="app-breadcrumb"
          />
        </nav>

        <section className="content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
