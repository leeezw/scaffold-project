import { useEffect, useState } from 'react';
import { Card, Table, Statistic, Input, Button, Space, Tag, Pagination, Select } from 'antd';
import { 
  UserOutlined, 
  CheckCircleOutlined, 
  StopOutlined, 
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import request from '../api/index.js';
import './UserList.css';

const { Search } = Input;

export default function UserList() {
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [stats, setStats] = useState({
    total: 0,
    enabled: 0,
    disabled: 0,
    today: 0
  });
  const [query, setQuery] = useState({ keyword: '', pageNum: 1, pageSize: 10 });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, [query]);

  useEffect(() => {
    calculateStats();
  }, [data, total]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await request.get('/users/page', { params: query });
      if (res.code === 200) {
        setData(res.data.list);
        setTotal(res.data.total);
      }
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = () => {
    const enabled = data.filter(u => u.status === 1).length;
    const disabled = data.filter(u => u.status === 0).length;
    setStats({
      total: total,
      enabled: enabled,
      disabled: disabled,
      today: 0
    });
  };

  const handleSearch = (value) => {
    setQuery({ ...query, keyword: value, pageNum: 1 });
  };

  const handlePageChange = (page, pageSize) => {
    setQuery({ ...query, pageNum: page, pageSize });
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '昵称',
      dataIndex: 'nickname',
      key: 'nickname',
      render: (text) => text || '-',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      render: (text) => text || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={status === 1 ? 'success' : 'error'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
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
            icon={<EditOutlined />} 
            size="small"
            title="编辑"
          />
          <Button 
            type="text" 
            icon={<DeleteOutlined />} 
            size="small"
            danger
            title="删除"
          />
        </Space>
      ),
    },
  ];

  return (
    <div className="user-list-page">
      {/* 统计卡片 */}
      <div className="stats-grid">
        <Card className="stat-card">
          <Statistic
            title="用户总数"
            value={stats.total || total}
            prefix={<UserOutlined style={{ color: '#3f8cff' }} />}
            valueStyle={{ color: '#0a1629' }}
          />
        </Card>
        <Card className="stat-card">
          <Statistic
            title="已启用"
            value={stats.enabled}
            prefix={<CheckCircleOutlined style={{ color: '#22c55e' }} />}
            valueStyle={{ color: '#0a1629' }}
          />
        </Card>
        <Card className="stat-card">
          <Statistic
            title="已禁用"
            value={stats.disabled}
            prefix={<StopOutlined style={{ color: '#fb923c' }} />}
            valueStyle={{ color: '#0a1629' }}
          />
        </Card>
        <Card className="stat-card">
          <Statistic
            title="今日新增"
            value={stats.today}
            prefix={<PlusOutlined style={{ color: '#a855f7' }} />}
            valueStyle={{ color: '#0a1629' }}
          />
        </Card>
      </div>

      {/* 数据表格 */}
      <Card 
        className="data-table-card"
        title="用户列表"
        extra={
          <Space>
            <Button type="primary" icon={<PlusOutlined />}>
              新增用户
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          rowKey="id"
          pagination={false}
          locale={{
            emptyText: '暂无数据'
          }}
        />
        
        {data.length > 0 && (
          <div className="pagination-wrapper">
            <div className="pagination-info">
              共 {total} 条，每页显示
              <Select
                value={query.pageSize}
                onChange={(value) => setQuery({ ...query, pageSize: value, pageNum: 1 })}
                style={{ width: 80, margin: '0 8px' }}
              >
                <Select.Option value={10}>10</Select.Option>
                <Select.Option value={20}>20</Select.Option>
                <Select.Option value={50}>50</Select.Option>
              </Select>
              条
            </div>
            <Pagination
              current={query.pageNum}
              total={total}
              pageSize={query.pageSize}
              onChange={handlePageChange}
              showSizeChanger={false}
              showQuickJumper
              showTotal={(total) => `共 ${total} 条`}
            />
          </div>
        )}
      </Card>
    </div>
  );
}
