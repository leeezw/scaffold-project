import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Input, Button, Dropdown, Avatar, Badge, Breadcrumb, Empty, Spin } from 'antd';
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

const ICON_MAP = {
  HomeOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  AppstoreOutlined,
  MailOutlined,
  SettingOutlined,
  SafetyOutlined,
  UserSwitchOutlined,
  FileTextOutlined,
};

const getIconComponent = (iconName) => {
  if (!iconName) {
    return undefined;
  }
  return ICON_MAP[iconName] || FileTextOutlined;
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
  const [searchValue, setSearchValue] = useState('');
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [menuTree, setMenuTree] = useState([]);
  const [menuLoading, setMenuLoading] = useState(false);
  const [pathMetaMap, setPathMetaMap] = useState({});
  const userMenuRef = useRef(null);

  const sortMenus = useCallback((menus = []) => {
    return [...menus].sort((a, b) => {
      const sortA = a?.sort ?? 0;
      const sortB = b?.sort ?? 0;
      if (sortA !== sortB) {
        return sortA - sortB;
      }
      const idA = a?.id ?? 0;
      const idB = b?.id ?? 0;
      return idA - idB;
    });
  }, []);

  const transformMenuData = useCallback((menus = []) => {
    if (!Array.isArray(menus)) {
      return [];
    }
    return sortMenus(menus).map(item => {
      const key = item?.id ? String(item.id) : (item?.code || item?.path || Math.random().toString(36).slice(2));
      const menuItem = {
        key,
        label: item?.name || key,
        path: item?.path,
        icon: getIconComponent(item?.icon),
      };
      if (Array.isArray(item?.children) && item.children.length > 0) {
        menuItem.children = transformMenuData(item.children);
      }
      return menuItem;
    });
  }, [sortMenus]);

  const extractPathMeta = useCallback((menus = []) => {
    const map = {};
    const traverse = (items = []) => {
      items.forEach(item => {
        if (item.path) {
          map[item.path] = {
            label: item.label,
            icon: item.icon,
          };
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children);
        }
      });
    };
    traverse(menus);
    return map;
  }, []);

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

  const fetchMenus = useCallback(async () => {
    if (!token) {
      setMenuTree([]);
      setPathMetaMap({});
      return;
    }
    setMenuLoading(true);
    try {
      const res = await request.get('/menus/my');
      if (res.code === 200 && Array.isArray(res.data)) {
        const transformed = transformMenuData(res.data);
        setMenuTree(transformed);
        setPathMetaMap(extractPathMeta(transformed));
      } else {
        setMenuTree([]);
        setPathMetaMap({});
      }
    } catch (error) {
      console.error('获取菜单失败:', error);
      setMenuTree([]);
      setPathMetaMap({});
    } finally {
      setMenuLoading(false);
    }
  }, [token, transformMenuData, extractPathMeta]);

  useEffect(() => {
    fetchMenus();
  }, [fetchMenus]);

  const allowedPaths = useMemo(() => {
    const paths = [];
    const traverse = (items = []) => {
      items.forEach(item => {
        if (item.path) {
          paths.push(item.path);
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children);
        }
      });
    };
    traverse(menuTree);
    return paths;
  }, [menuTree]);

  useEffect(() => {
    if (!token || menuLoading || allowedPaths.length === 0) {
      return;
    }
    const currentPath = location.pathname;
    const matched = allowedPaths.some(path => currentPath === path || currentPath.startsWith(`${path}/`));
    if (!matched) {
      navigate(allowedPaths[0], { replace: true });
    }
  }, [allowedPaths, location.pathname, menuLoading, navigate, token]);

  const currentBreadcrumb = useMemo(() => {
    const currentPath = location.pathname;
    if (!currentPath) {
      return null;
    }
    let matched = null;
    Object.entries(pathMetaMap).forEach(([path, meta]) => {
      if (currentPath === path || currentPath.startsWith(`${path}/`)) {
        if (!matched || path.length > matched.path.length) {
          matched = { path, ...meta };
        }
      }
    });
    return matched;
  }, [location.pathname, pathMetaMap]);

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

    if (currentBreadcrumb && currentBreadcrumb.path !== '/') {
      const IconComponent = currentBreadcrumb.icon || FileTextOutlined;
      items.push({
        title: (
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            {IconComponent ? <IconComponent /> : <FileTextOutlined />}
            <span>{currentBreadcrumb.label}</span>
          </span>
        ),
      });
    }

    return items;
  }, [currentBreadcrumb, navigate]);

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
          
          <div style={{ flex: 1, width: '100%' }}>
            {menuLoading ? (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '32px 0' }}>
                <Spin />
              </div>
            ) : menuTree.length > 0 ? (
              <SidebarMenu items={menuTree} onItemClick={handleMenuClick} />
            ) : (
              <Empty
                description="暂无可用菜单"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                style={{ padding: '24px 0' }}
              />
            )}
          </div>
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
