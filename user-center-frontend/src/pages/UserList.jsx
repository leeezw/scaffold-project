import { useEffect, useState } from 'react';
import { Card, Table, Statistic, Input, Button, Space, Tag, Pagination, Select, Modal, Form, message } from 'antd';
import { 
  UserOutlined, 
  CheckCircleOutlined, 
  StopOutlined, 
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import request from '../api/index.js';
import UserForm from '../components/UserForm.jsx';
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
  const [form] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [submitLoading, setSubmitLoading] = useState(false);

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

  const handleAddUser = () => {
    setEditingUser(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEditUser = (record) => {
    setEditingUser(record);
    form.setFieldsValue({
      username: record.username,
      nickname: record.nickname,
      email: record.email,
      status: record.status,
    });
    setModalVisible(true);
  };

  const handleSubmit = async (values) => {
    setSubmitLoading(true);
    try {
      // 编辑模式下，如果密码为空则移除密码字段
      if (editingUser && !values.password) {
        delete values.password;
      }

      if (editingUser) {
        // 编辑用户
        const res = await request.put(`/users/${editingUser.id}`, values);
        if (res.code === 200) {
          message.success('用户更新成功');
          setModalVisible(false);
          fetchUsers();
        } else {
          message.error(res.message || '更新失败');
        }
      } else {
        // 新增用户
        const res = await request.post('/users', values);
        if (res.code === 200) {
          message.success('用户创建成功');
          setModalVisible(false);
          fetchUsers();
        } else {
          message.error(res.message || '创建失败');
        }
      }
    } catch (error) {
      message.error(error.message || '操作失败');
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleCancel = () => {
    setModalVisible(false);
    form.resetFields();
    setEditingUser(null);
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
            onClick={() => handleEditUser(record)}
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
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAddUser}>
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

      {/* 新增/编辑用户弹窗 */}
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onCancel={handleCancel}
        footer={null}
        width={600}
        className="user-form-modal"
        centered
        destroyOnClose
      >
        <UserForm
          form={form}
          initialValues={editingUser}
          onFinish={handleSubmit}
        />
        <div className="modal-footer">
          <Button onClick={handleCancel}>
            取消
          </Button>
          <Button 
            type="primary" 
            onClick={() => form.submit()}
            loading={submitLoading}
          >
            {editingUser ? '更新' : '创建'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
