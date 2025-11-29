import { useEffect, useState } from 'react';
import { Card, Table, Tag, Space, Button } from 'antd';
import { EyeOutlined, LogoutOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import './SessionList.css';

export default function SessionList() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchSessions = async () => {
      setLoading(true);
      try {
        const res = await request.get('/auth/session/my-sessions');
        if (res.code === 200) {
          setSessions(res.data || []);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchSessions();
  }, []);

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
      <Card className="data-table-card" title="我的 Session">
        <Table
          columns={columns}
          dataSource={sessions.map((session, index) => ({ key: index, session }))}
          loading={loading}
          rowKey="key"
          locale={{
            emptyText: '暂无 Session'
          }}
        />
      </Card>
    </div>
  );
}
