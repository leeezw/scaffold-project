import { useState, useCallback, useEffect } from 'react';
import { Space, Tag, Tree, Descriptions, Empty, Skeleton } from 'antd';
import { 
  ApiOutlined,
  MenuOutlined,
  AppstoreOutlined,
  KeyOutlined,
} from '@ant-design/icons';
import request from '../api/index.js';
import { formatDateTime } from '../utils/dateUtils.js';
import './PermissionList.css';

const { TreeNode } = Tree;

export default function PermissionList() {
  const [permissionTree, setPermissionTree] = useState([]);
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [selectedKeys, setSelectedKeys] = useState([]);
  const [selectedPermission, setSelectedPermission] = useState(null);
  const [loading, setLoading] = useState(true);

  // 加载权限树
  const loadPermissionTree = useCallback(async () => {
    setLoading(true);
    try {
      const res = await request.get('/permissions/tree');
      if (res.code === 200 && res.data) {
        const tree = Array.isArray(res.data) ? res.data : [];
        setPermissionTree(tree);
        
        // 自动展开所有节点（如果数据量不大）
        const getAllKeys = (nodes) => {
          const keys = [];
          const traverse = (items) => {
            items.forEach(item => {
              if (item.id) {
                keys.push(item.id.toString());
              }
              if (item.children && item.children.length > 0) {
                traverse(item.children);
              }
            });
          };
          traverse(nodes);
          return keys;
        };
        
        if (tree.length > 0 && tree.length < 100) {
          setExpandedKeys(getAllKeys(tree));
        }
      }
    } catch (error) {
      console.error('fetchPermissions error:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  // 初始加载和筛选变化时重新加载
  useEffect(() => {
    loadPermissionTree();
  }, [loadPermissionTree]);

  // 获取权限类型图标
  const getTypeIcon = (type) => {
    const iconMap = {
      'menu': <MenuOutlined />,
      'button': <AppstoreOutlined />,
      'api': <ApiOutlined />,
    };
    return iconMap[type] || <KeyOutlined />;
  };

  // 获取权限类型文本
  const getTypeText = (type) => {
    const typeMap = {
      'menu': '菜单',
      'button': '按钮',
      'api': '接口',
    };
    return typeMap[type] || type || '-';
  };

  // 渲染树节点
  const renderTreeNodes = (nodes) => {
    return nodes.map(node => {
      const isEnabled = node.status === 1;
      const title = (
        <div className="tree-node-content">
          <span className="tree-node-icon">{getTypeIcon(node.type)}</span>
          <span className="tree-node-name">{node.name}</span>
          <span className="tree-node-code">({node.code})</span>
          <Tag color={isEnabled ? 'success' : 'error'} className="tree-node-status">
            {isEnabled ? '启用' : '禁用'}
          </Tag>
        </div>
      );

      return (
        <TreeNode
          key={node.id}
          title={title}
        >
          {node.children && node.children.length > 0 && renderTreeNodes(node.children)}
        </TreeNode>
      );
    });
  };

  // 处理树节点选择
  const handleSelect = (selectedKeys, info) => {
    setSelectedKeys(selectedKeys);
    if (selectedKeys.length > 0 && info.selectedNodes.length > 0) {
      const node = info.selectedNodes[0];
      // 从树节点中找到对应的权限数据
      const findPermission = (nodes, key) => {
        for (const node of nodes) {
          if (node.id.toString() === key) {
            return node;
          }
          if (node.children && node.children.length > 0) {
            const found = findPermission(node.children, key);
            if (found) return found;
          }
        }
        return null;
      };
      const permission = findPermission(permissionTree, selectedKeys[0]);
      setSelectedPermission(permission);
    } else {
      setSelectedPermission(null);
    }
  };

  // 处理展开/收起
  const handleExpand = (expandedKeys) => {
    setExpandedKeys(expandedKeys);
  };

  return (
    <div className="permission-list-page">
      {/* 权限树和详情 */}
      <div className="permission-content">
        {/* 左侧权限树 */}
        <div className="tree-section">
          <div className="section-title">权限树</div>
          {loading ? (
            <div style={{ padding: '12px' }}>
              <Skeleton active paragraph={{ rows: 10 }} />
            </div>
          ) : permissionTree.length > 0 ? (
            <Tree
              showLine={{ showLeafIcon: false }}
              showIcon={false}
              expandedKeys={expandedKeys}
              selectedKeys={selectedKeys}
              onSelect={handleSelect}
              onExpand={handleExpand}
              className="permission-tree"
            >
              {renderTreeNodes(permissionTree)}
            </Tree>
          ) : (
            <Empty description="暂无权限数据" />
          )}
        </div>

        {/* 右侧权限详情 */}
        <div className="detail-section">
          <div className="section-title">权限详情</div>
          {loading ? (
            <div style={{ padding: '12px' }}>
              <Skeleton active paragraph={{ rows: 8 }} />
            </div>
          ) : selectedPermission ? (
            <Descriptions column={1} bordered>
              <Descriptions.Item label="权限编码">
                <code>{selectedPermission.code}</code>
              </Descriptions.Item>
              <Descriptions.Item label="权限名称">
                {selectedPermission.name}
              </Descriptions.Item>
              <Descriptions.Item label="类型">
                <Space>
                  {getTypeIcon(selectedPermission.type)}
                  {getTypeText(selectedPermission.type)}
                </Space>
              </Descriptions.Item>
              {selectedPermission.path && (
                <Descriptions.Item label="路径">
                  <code>{selectedPermission.path}</code>
                </Descriptions.Item>
              )}
              {selectedPermission.method && (
                <Descriptions.Item label="HTTP方法">
                  <Tag color={
                    selectedPermission.method === 'GET' ? 'blue' :
                    selectedPermission.method === 'POST' ? 'green' :
                    selectedPermission.method === 'PUT' ? 'orange' :
                    selectedPermission.method === 'DELETE' ? 'red' : 'default'
                  }>
                    {selectedPermission.method}
                  </Tag>
                </Descriptions.Item>
              )}
              <Descriptions.Item label="状态">
                <Tag color={selectedPermission.status === 1 ? 'success' : 'error'}>
                  {selectedPermission.status === 1 ? '启用' : '禁用'}
                </Tag>
              </Descriptions.Item>
              {selectedPermission.sort !== null && selectedPermission.sort !== undefined && (
                <Descriptions.Item label="排序">
                  {selectedPermission.sort}
                </Descriptions.Item>
              )}
              {selectedPermission.createTime && (
                <Descriptions.Item label="创建时间">
                  {formatDateTime(selectedPermission.createTime)}
                </Descriptions.Item>
              )}
              {selectedPermission.updateTime && (
                <Descriptions.Item label="更新时间">
                  {formatDateTime(selectedPermission.updateTime)}
                </Descriptions.Item>
              )}
            </Descriptions>
          ) : (
            <Empty description="请选择一个权限查看详情" />
          )}
        </div>
      </div>
    </div>
  );
}
