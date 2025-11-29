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
  ClockCircleOutlined,
  AppstoreOutlined,
  MailOutlined,
  SettingOutlined,
  SafetyOutlined,
  UserSwitchOutlined,
  FileTextOutlined
} from '@ant-design/icons';
import { useAuthContext } from '../hooks/AuthProvider.jsx';
import request from '../api/index.js';
import SidebarMenu from './SidebarMenu.jsx';
import './AppLayout.css';

// 菜单配置 - 可以从数据库获取
// 支持多级菜单、分组和分隔符
const menuItems = [
  {
    key: 'users',
    label: '用户管理',
    path: '/',
    icon: HomeOutlined,
  },
  {
    key: 'system',
    label: '系统管理',
    icon: AppstoreOutlined,
    children: [
      {
        key: 'g1',
        label: '权限管理',
        type: 'group',
        children: [
          {
            key: 'roles',
            label: '角色管理',
            path: '/roles',
            icon: TeamOutlined,
          },
          {
            key: 'permissions',
            label: '权限配置',
            path: '/permissions',
            icon: SafetyOutlined,
          },
        ],
      },
      {
        key: 'g2',
        label: '系统设置',
        type: 'group',
        children: [
          {
            key: 'settings',
            label: '系统设置',
            icon: SettingOutlined,
            children: [
              {
                key: 'general',
                label: '通用设置',
                path: '/settings/general',
                icon: FileTextOutlined,
              },
              {
                key: 'security',
                label: '安全设置',
                path: '/settings/security',
                icon: SafetyOutlined,
              },
            ],
          },
          {
            key: 'users-config',
            label: '用户配置',
            path: '/system/users-config',
            icon: UserSwitchOutlined,
          },
        ],
      },
    ],
  },
  {
    type: 'divider',
  },
  {
    key: 'sessions',
    label: 'Session管理',
    path: '/sessions',
    icon: ClockCircleOutlined,
  },
  {
    key: 'notifications',
    label: '通知中心',
    icon: MailOutlined,
    children: [
      {
        key: 'messages',
        label: '消息通知',
        path: '/notifications/messages',
      },
      {
        key: 'alerts',
        label: '告警通知',
        path: '/notifications/alerts',
      },
    ],
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
  const { user, setUser, setToken, token } = useAuthContext();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchValue, setSearchValue] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const userMenuRef = useRef(null);

  // 如果 token 存在但没有用户信息，尝试获取用户信息
  useEffect(() => {
    const fetchCurrentUser = async () => {
      if (token && !user) {
        try {
          // 根据 OpenAPI 规范：/api/auth/current GET
          const res = await request.get('/auth/current', {
            params: {}
          });
          if (res.code === 200 && res.data) {
            setUser(res.data);
          }
        } catch (err) {
          console.warn('获取当前用户信息失败:', err);
          // 如果失败，尝试传递空对象作为 loginUser 参数
          try {
            const res = await request.get('/auth/current', {
              params: { loginUser: {} }
            });
            if (res.code === 200 && res.data) {
              setUser(res.data);
            }
          } catch (err2) {
            console.warn('获取当前用户信息失败（第二次尝试）:', err2);
          }
        }
      }
    };

    fetchCurrentUser();
  }, [token, user, setUser]);

  // 面包屑配置
  const breadcrumbMap = {
    '/': { title: '用户管理', icon: HomeOutlined },
    '/roles': { title: '角色管理', icon: TeamOutlined },
    '/permissions': { title: '权限管理', icon: SafetyOutlined },
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

  const handleLogout = async () => {
    try {
      // 根据 OpenAPI 规范：/api/auth/logout POST
      // 调用登出接口
      await request.post('/auth/logout');
    } catch (error) {
      console.error('登出接口调用失败:', error);
      // 即使接口调用失败，也清除本地状态
    } finally {
      // 清除本地存储和状态
      setToken(null);
      setUser(null);
      localStorage.removeItem('uc_token');
      localStorage.removeItem('uc_user');
      localStorage.removeItem('uc_remember');
      navigate('/login', { replace: true });
    }
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

  // 获取用户头像显示内容
  const getUserAvatarProps = () => {
    // 检查 avatar 是否存在且不为空字符串
    if (user?.avatar && typeof user.avatar === 'string' && user.avatar.trim() !== '') {
      return { src: user.avatar };
    }
    // 如果有昵称或用户名，显示首字母
    const name = user?.nickname || user?.username || '';
    if (name) {
      return { 
        style: { backgroundColor: '#3f8cff', color: 'white' },
        children: name.charAt(0).toUpperCase()
      };
    }
    // 默认显示图标
    return { 
      icon: <UserOutlined />,
      style: { backgroundColor: '#3f8cff', color: 'white' }
    };
  };

  // 用户下拉菜单项
  const userMenuItems = [
    {
      key: 'user-info',
      label: (
        <div className="user-menu-header">
          <Avatar 
            size="large"
            {...getUserAvatarProps()}
          />
          <div className="user-menu-info">
            <div className="user-menu-name">{user?.nickname || user?.username || 'User'}</div>
            <div className="user-menu-email">{user?.email || user?.phone || ''}</div>
          </div>
        </div>
      ),
      disabled: true,
      className: 'user-menu-item-info',
    },
    {
      type: 'divider',
      className: 'user-menu-divider',
    },
    {
      key: 'logout',
      label: (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <LogoutOutlined />
          <span>退出登录</span>
        </div>
      ),
      className: 'user-menu-item',
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
                menu={{ 
                  items: userMenuItems,
                  onClick: ({ key }) => {
                    if (key === 'logout') {
                      handleLogout();
                    }
                  }
                }}
                trigger={['click']}
                placement="bottomRight"
                classNames={{ root: 'user-profile-dropdown-menu' }}
              >
                <Button
                  type="text"
                  className="toolbar-btn-user"
                >
                  <Avatar 
                    size={32}
                    {...getUserAvatarProps()}
                  />
                  <span className="user-name">{user?.nickname || user?.username || 'User'}</span>
                  <DownOutlined className="user-dropdown-icon" />
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
