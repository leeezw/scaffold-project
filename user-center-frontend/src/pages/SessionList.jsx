import { useState } from 'react';
import { Tag, Space, Button } from 'antd';
import { EyeOutlined, LogoutOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import ProTable from '../components/ProTable.jsx';
import './SessionList.css';

export default function SessionList() {
  const [refreshKey, setRefreshKey] = useState(0);

  // 请求函数
  const fetchSessions = async () => {
    const res = await request.get('/auth/session/my-sessions');
    if (res.code === 200 && res.data) {
      // 转换数据格式
      return {
        ...res,
        data: {
          list: res.data.map((session, index) => ({ key: index, session })),
          total: res.data.length
        }
      };
    }
    return res;
  };

  const columns = [
    {
      title: 'Session ID',
      dataIndex: 'session',
      key: 'session',
      render: (text) => (
        <code className="session-id">{text}</code>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: () => '-',
    },
    {
      title: '最后活动',
      dataIndex: 'lastActivity',
      key: 'lastActivity',
      render: () => '-',
    },
    {
      title: '状态',
      key: 'status',
      render: () => (
        <Tag color="success">活跃</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <Button 
            type="text" 
            icon={<EyeOutlined />} 
            size="small"
            title="查看详情"
          />
          <Button 
            type="text" 
            icon={<LogoutOutlined />} 
            size="small"
            danger
            title="退出登录"
          />
        </Space>
      ),
    },
  ];

  return (
    <div className="session-list-page">
      <ProTable
        key={refreshKey}
        title="我的 Session"
        columns={columns}
        request={fetchSessions}
        params={{}}
        rowKey="key"
        showPagination={false}
      />
    </div>
  );
}
