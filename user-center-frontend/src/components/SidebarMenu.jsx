import { NavLink, useLocation } from 'react-router-dom';
import { useState } from 'react';
import './SidebarMenu.css';

/**
 * 菜单项配置
 * @typedef {Object} MenuItem
 * @property {string} key - 唯一标识
 * @property {string} label - 显示文本
 * @property {string} path - 路由路径
 * @property {React.ReactNode} icon - 图标组件
 * @property {MenuItem[]} [children] - 子菜单项
 * @property {boolean} [external] - 是否外部链接
 */

/**
 * 侧边栏菜单组件
 * @param {Object} props
 * @param {MenuItem[]} props.items - 菜单项列表
 * @param {Function} [props.onItemClick] - 菜单项点击回调
 */
export default function SidebarMenu({ items = [], onItemClick }) {
  const location = useLocation();
  const [expandedKeys, setExpandedKeys] = useState(new Set());

  const toggleExpand = (key) => {
    const newExpanded = new Set(expandedKeys);
    if (newExpanded.has(key)) {
      newExpanded.delete(key);
    } else {
      newExpanded.add(key);
    }
    setExpandedKeys(newExpanded);
  };

  const isActive = (item) => {
    if (item.path) {
      return location.pathname === item.path || location.pathname.startsWith(item.path + '/');
    }
    if (item.children) {
      return item.children.some(child => isActive(child));
    }
    return false;
  };

  const renderMenuItem = (item, level = 0) => {
    const hasChildren = item.children && item.children.length > 0;
    const isExpanded = expandedKeys.has(item.key);
    const active = isActive(item);

    if (hasChildren) {
      return (
        <div key={item.key} className={`menu-item menu-item-group ${active ? 'active' : ''}`}>
          <div
            className="menu-item-content"
            onClick={() => toggleExpand(item.key)}
          >
            {item.icon && (
              <span className="menu-icon">
                {item.icon}
              </span>
            )}
            <span className="menu-text">{item.label}</span>
            <svg
              className={`menu-arrow ${isExpanded ? 'expanded' : ''}`}
              width="12"
              height="12"
              viewBox="0 0 12 12"
              fill="none"
            >
              <path
                d="M4.5 3L7.5 6L4.5 9"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
          {isExpanded && (
            <div className="menu-children">
              {item.children.map(child => renderMenuItem(child, level + 1))}
            </div>
          )}
          <div className="menu-item-indicator"></div>
        </div>
      );
    }

    const content = (
      <div className="menu-item-content">
        {item.icon && (
          <span className="menu-icon">
            {item.icon}
          </span>
        )}
        <span className="menu-text">{item.label}</span>
      </div>
    );

    if (item.external) {
      return (
        <a
          key={item.key}
          href={item.path}
          target="_blank"
          rel="noopener noreferrer"
          className={`menu-item ${active ? 'active' : ''}`}
          onClick={() => onItemClick?.(item)}
        >
          {content}
          <div className="menu-item-indicator"></div>
        </a>
      );
    }

    // 特殊处理：如果path是#，使用div而不是NavLink
    if (item.path === '#') {
      return (
        <div
          key={item.key}
          className={`menu-item ${active ? 'active' : ''}`}
          onClick={() => onItemClick?.(item)}
        >
          {content}
          <div className="menu-item-indicator"></div>
        </div>
      );
    }

    return (
      <NavLink
        key={item.key}
        to={item.path}
        end={!hasChildren}
        className={`menu-item ${active ? 'active' : ''}`}
        onClick={() => onItemClick?.(item)}
      >
        {content}
        <div className="menu-item-indicator"></div>
      </NavLink>
    );
  };

  return (
    <nav className="sidebar-menu">
      {items.map(item => renderMenuItem(item))}
    </nav>
  );
}

