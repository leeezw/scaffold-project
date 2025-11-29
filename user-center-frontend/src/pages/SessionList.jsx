import { useState, useRef, useCallback, useEffect } from 'react';
import { Card, Statistic, Button, Space, Tag, Modal, Form, message, Input, Select } from 'antd';
import { 
  DesktopOutlined, 
  MobileOutlined,
  TabletOutlined,
  GlobalOutlined,
  LogoutOutlined,
  PoweroffOutlined,
} from '@ant-design/icons';
import request from '../api/index.js';
import ProTableV2 from '../components/ProTableV2.jsx';
import { formatDateTime } from '../utils/dateUtils.js';
import './SessionList.css';

export default function SessionList() {
  const actionRef = useRef();
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
  });
  const [filterForm] = Form.useForm();
  const [filterParams, setFilterParams] = useState({});
  const [users, setUsers] = useState([]);
  const debounceTimerRef = useRef(null);

  // 防抖处理筛选
  const handleFilterChange = useCallback(() => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
    debounceTimerRef.current = setTimeout(() => {
      filterForm.submit();
    }, 500);
  }, [filterForm]);

  // 组件卸载时清理定时器
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  // 加载用户列表（用于筛选）
  useEffect(() => {
    const loadUsers = async () => {
      try {
        const res = await request.get('/users/page', { params: { pageSize: 1000 } });
        if (res.code === 200 && res.data && res.data.pageData) {
          const userList = Array.isArray(res.data.pageData.list) ? res.data.pageData.list : [];
          setUsers(userList);
        }
      } catch (error) {
        console.error('loadUsers error:', error);
      }
    };
    loadUsers();
  }, []);

  // 解析Session Key，提取设备ID
  const parseSessionKey = (sessionKey) => {
    // Session Key格式可能是: userId:deviceId:timestamp 或其他格式
    // 这里尝试解析，如果格式不同可能需要调整
    const parts = sessionKey.split(':');
    if (parts.length >= 2) {
      return {
        deviceId: parts[1] || 'unknown',
        timestamp: parts.length >= 3 ? parseInt(parts[2]) : null,
      };
    }
    return {
      deviceId: sessionKey.substring(0, 20) + '...',
      timestamp: null,
    };
  };

  // 获取设备图标
  const getDeviceIcon = (deviceId) => {
    const deviceLower = deviceId.toLowerCase();
    if (deviceLower.includes('mobile') || deviceLower.includes('phone') || deviceLower.includes('android') || deviceLower.includes('ios')) {
      return <MobileOutlined />;
    }
    if (deviceLower.includes('tablet') || deviceLower.includes('ipad')) {
      return <TabletOutlined />;
    }
    if (deviceLower.includes('desktop') || deviceLower.includes('windows') || deviceLower.includes('mac')) {
      return <DesktopOutlined />;
    }
    return <GlobalOutlined />;
  };

  // 请求函数 - 获取所有用户的Session
  const fetchSessions = async (params) => {
    try {
      // 先获取所有用户
      const usersRes = await request.get('/users/page', { params: { pageSize: 1000 } });
      if (usersRes.code !== 200 || !usersRes.data || !usersRes.data.pageData) {
        return {
          code: 500,
          data: { list: [], total: 0 }
        };
      }

      const userList = Array.isArray(usersRes.data.pageData.list) ? usersRes.data.pageData.list : [];
      
      // 应用用户筛选
      let filteredUsers = userList;
      if (params.userId) {
        filteredUsers = filteredUsers.filter(user => user.id === parseInt(params.userId));
      }

      // 为每个用户获取Session列表
      const sessionList = [];
      for (const user of filteredUsers) {
        try {
          const sessionRes = await request.get(`/auth/session/user/${user.id}`);
          if (sessionRes.code === 200 && sessionRes.data) {
            const sessionKeys = Array.isArray(sessionRes.data) 
              ? sessionRes.data 
              : (sessionRes.data instanceof Set ? Array.from(sessionRes.data) : []);
            
            sessionKeys.forEach(sessionKey => {
              const parsed = parseSessionKey(sessionKey);
              sessionList.push({
                id: `${user.id}-${sessionKey}`,
                userId: user.id,
                username: user.username,
                nickname: user.nickname || user.username,
                sessionKey: sessionKey,
                deviceId: parsed.deviceId,
                startTime: parsed.timestamp ? new Date(parsed.timestamp).toISOString() : null,
                lastAccessTime: parsed.timestamp ? new Date(parsed.timestamp).toISOString() : null,
              });
            });
          }
        } catch (error) {
          console.error(`Failed to fetch sessions for user ${user.id}:`, error);
        }
      }

      // 应用关键词筛选
      let filteredList = sessionList;
      if (params.keyword) {
        const keyword = params.keyword.toLowerCase();
        filteredList = filteredList.filter(item => 
          (item.username && item.username.toLowerCase().includes(keyword)) ||
          (item.nickname && item.nickname.toLowerCase().includes(keyword)) ||
          (item.deviceId && item.deviceId.toLowerCase().includes(keyword)) ||
          (item.sessionKey && item.sessionKey.toLowerCase().includes(keyword))
        );
      }

      // 更新统计数据（基于全部数据）
      setStats({
        total: filteredList.length,
        active: filteredList.length,
      });

      // 应用分页
      const pageNum = params.current || params.pageNum || 1;
      const pageSize = params.pageSize || 10;
      const startIndex = (pageNum - 1) * pageSize;
      const endIndex = startIndex + pageSize;
      const paginatedList = filteredList.slice(startIndex, endIndex);

      return {
        code: 200,
        data: {
          list: paginatedList,
          total: filteredList.length
        }
      };
    } catch (error) {
      console.error('fetchSessions error:', error);
      return {
        code: 500,
        data: { list: [], total: 0 }
      };
    }
  };

  const handleRefresh = () => {
    actionRef.current?.reload();
  };

  // 强制用户下线
  const handleKickOutUser = (record) => {
    Modal.confirm({
      title: '确认强制下线',
      content: `确定要强制用户 "${record.nickname || record.username}" 下线吗？这将踢出该用户的所有设备。`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await request.post(`/auth/session/kick-out/${record.userId}`);
          if (res.code === 200) {
            message.success('用户已强制下线');
            handleRefresh();
          } else {
            message.error(res.message || '操作失败');
          }
        } catch (error) {
          message.error(error.message || '操作失败');
        }
      }
    });
  };

  // 踢出指定设备
  const handleKickOutDevice = (record) => {
    Modal.confirm({
      title: '确认踢出设备',
      content: `确定要踢出设备 "${record.deviceId}" 吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await request.post(`/auth/session/kick-out-device?userId=${record.userId}&deviceId=${record.deviceId}`);
          if (res.code === 200) {
            message.success('设备已踢出');
            handleRefresh();
          } else {
            message.error(res.message || '操作失败');
          }
        } catch (error) {
          message.error(error.message || '操作失败');
        }
      }
    });
  };

  const columns = [
    {
      title: '用户',
      key: 'user',
      width: 150,
      render: (_, record) => (
        <div>
          <div style={{ fontWeight: 600 }}>{record.nickname || record.username}</div>
          <div style={{ fontSize: '12px', color: '#999' }}>{record.username}</div>
        </div>
      ),
    },
    {
      title: '设备',
      key: 'device',
      width: 200,
      render: (_, record) => (
        <Space>
          {getDeviceIcon(record.deviceId)}
          <span>{record.deviceId}</span>
        </Space>
      ),
    },
    {
      title: 'Session Key',
      dataIndex: 'sessionKey',
      key: 'sessionKey',
      ellipsis: true,
      render: (text) => (
        <code style={{ fontSize: '12px', background: '#f5f5f5', padding: '2px 6px', borderRadius: '3px' }}>
          {text}
        </code>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 180,
      render: (text) => text ? formatDateTime(text) : '-',
    },
    {
      title: '最后活动',
      dataIndex: 'lastAccessTime',
      key: 'lastAccessTime',
      width: 180,
      render: (text) => text ? formatDateTime(text) : '-',
    },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: () => (
        <Tag color="success">活跃</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button 
            type="text" 
            icon={<LogoutOutlined />} 
            size="small"
            danger
            title="踢出设备"
            onClick={() => handleKickOutDevice(record)}
          />
          <Button 
            type="text" 
            icon={<PoweroffOutlined />} 
            size="small"
            danger
            title="强制用户下线"
            onClick={() => handleKickOutUser(record)}
          />
        </Space>
      ),
    },
  ];

  return (
    <div className="session-list-page">
      {/* 统计卡片 */}
      <div className="stats-grid">
        <Card className="stat-card">
          <Statistic
            title="Session总数"
            value={stats.total}
            prefix={<DesktopOutlined style={{ color: '#3f8cff' }} />}
            valueStyle={{ color: '#0a1629' }}
          />
        </Card>
        <Card className="stat-card">
          <Statistic
            title="活跃Session"
            value={stats.active}
            prefix={<GlobalOutlined style={{ color: '#22c55e' }} />}
            valueStyle={{ color: '#0a1629' }}
          />
        </Card>
      </div>

      {/* 数据表格 */}
      <ProTableV2
        actionRef={actionRef}
        headerTitle="Session列表"
        columns={columns}
        request={fetchSessions}
        rowKey="id"
        search={false}
        toolbar={{
          filter: (
            <Form
              form={filterForm}
              layout="inline"
              onFinish={async (values) => {
                const newFilterParams = { ...values };
                setFilterParams(newFilterParams);
                actionRef.current?.reload();
              }}
              className="filter-form"
            >
              <Form.Item name="keyword" style={{ marginBottom: 0 }}>
                <Input
                  className='filter-search-input'
                  placeholder="搜索用户名、昵称、设备或Session Key"
                  allowClear
                  onChange={handleFilterChange}
                  onPressEnter={() => filterForm.submit()}
                />
              </Form.Item>
              <Form.Item name="userId" style={{ marginBottom: 0 }}>
                <Select
                  className='filter-status-select'
                  placeholder="选择用户"
                  allowClear
                  showSearch
                  optionFilterProp="children"
                  onChange={() => filterForm.submit()}
                  style={{ width: 200 }}
                >
                  {users.map(user => (
                    <Select.Option key={user.id} value={user.id}>
                      {user.nickname || user.username}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Form>
          ),
        }}
        params={filterParams}
      />
    </div>
  );
}
