import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect, useRef, useMemo } from 'react';
import { Input, Button, Dropdown, Avatar, Badge, Breadcrumb, Spin } from 'antd';
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

const MENU_ICON_MAP = {
  HomeOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  AppstoreOutlined,
  MailOutlined,
  SafetyOutlined,
  SettingOutlined,
  UserSwitchOutlined,
  FileTextOutlined,
};

const transformMenuTree = (menus = [], parentKey = 'menu') => {
  if (!Array.isArray(menus)) {
    return [];
  }

  return menus
    .filter((item) => item && item.type !== 'BUTTON' && item.visible !== false && item.status !== 0)
    .sort((a, b) => (a?.sort ?? 0) - (b?.sort ?? 0))
    .map((item, index) => {
      const key = item.id != null 
        ? String(item.id) 
        : String(item.permission || item.path || `${parentKey}-${index}`);

      const normalized = {
        key,
        label: item.name,
        path: item.path || undefined,
      };

      const IconComponent = item.icon ? MENU_ICON_MAP[item.icon] : undefined;
      if (IconComponent) {
        normalized.icon = IconComponent;
      }

      const children = transformMenuTree(item.children || [], key);
      if (children.length > 0) {
        normalized.children = children;
      }

      return normalized;
    });
};

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
  const [menuItems, setMenuItems] = useState([]);
  const [menuLoading, setMenuLoading] = useState(true);
  const [menuError, setMenuError] = useState('');
  const [menuReloadFlag, setMenuReloadFlag] = useState(0);
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

  useEffect(() => {
    let ignore = false;

    const fetchMenus = async () => {
      if (!token) {
        setMenuItems([]);
        setMenuError('');
        setMenuLoading(false);
        return;
      }

      setMenuLoading(true);
      setMenuError('');

      try {
        const res = await request.get('/menus/tree', {
          params: { status: 1 }
        });

        if (ignore) return;

        if (res.code !== 200) {
          throw new Error(res.message || '菜单加载失败');
        }

        const data = Array.isArray(res.data) ? res.data : [];
        setMenuItems(transformMenuTree(data));
      } catch (error) {
        if (ignore) return;
        console.error('菜单数据加载失败:', error);
        setMenuItems([]);
        setMenuError(error.message || '菜单加载失败');
      } finally {
        if (!ignore) {
          setMenuLoading(false);
        }
      }
    };

    fetchMenus();

    return () => {
      ignore = true;
    };
  }, [token, menuReloadFlag]);

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

  const retryLoadMenus = () => {
    setMenuReloadFlag((flag) => flag + 1);
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
        
          {menuLoading ? (
            <div
              style={{
                padding: '12px 16px',
                color: '#94a3b8',
                fontSize: '12px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Spin size="small" />
              <span>菜单加载中...</span>
            </div>
          ) : menuError ? (
            <div
              style={{
                padding: '12px 16px',
                color: '#ef4444',
                fontSize: '12px',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px'
              }}
            >
              <span>{menuError}</span>
              <Button
                type="link"
                size="small"
                onClick={retryLoadMenus}
                style={{ padding: 0, alignSelf: 'flex-start' }}
              >
                重试
              </Button>
            </div>
          ) : (
            <SidebarMenu items={menuItems} onItemClick={handleMenuClick} />
          )}
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
