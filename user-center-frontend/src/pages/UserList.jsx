import { useEffect, useState } from 'react';
import { Card, Statistic, Button, Space, Tag, Modal, Form, message } from 'antd';
import { 
  UserOutlined, 
  CheckCircleOutlined, 
  StopOutlined, 
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PoweroffOutlined,
} from '@ant-design/icons';
import request from '../api/index.js';
import UserForm from '../components/UserForm.jsx';
import ProTable from '../components/ProTable.jsx';
import './UserList.css';

export default function UserList() {
  const [stats, setStats] = useState({
    total: 0,
    enabled: 0,
    disabled: 0,
    today: 0
  });
  const [query, setQuery] = useState({ keyword: '', pageNum: 1, pageSize: 10 });
  const [form] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  // 请求函数
  const fetchUsers = async (params) => {
    const res = await request.get('/users/page', { params });
    return res;
  };

  // 处理数据变化，更新统计数据
  const handleDataChange = (data, total) => {
    const enabled = data.filter(u => u.status === 1).length;
    const disabled = data.filter(u => u.status === 0).length;
    setStats({
      total: total,
      enabled: enabled,
      disabled: disabled,
      today: 0
    });
  };

  const handleRefresh = () => {
    setRefreshKey(prev => prev + 1);
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
      // 编辑时不设置状态，状态通过独立的状态按钮修改
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
          handleRefresh();
        } else {
          message.error(res.message || '更新失败');
        }
      } else {
        // 新增用户
        const res = await request.post('/users', values);
        if (res.code === 200) {
          message.success('用户创建成功');
          setModalVisible(false);
          handleRefresh();
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

  // 修改用户状态
  const handleChangeStatus = async (record) => {
    const newStatus = record.status === 1 ? 0 : 1;
    const statusText = newStatus === 1 ? '启用' : '禁用';
    
    Modal.confirm({
      title: `确认${statusText}用户`,
      content: `确定要${statusText}用户 "${record.nickname || record.username}" 吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          // 根据 OpenAPI 规范：/api/users/status PUT
          // Request: { id, status }
          const res = await request.put('/users/status', {
            id: record.id,
            status: newStatus
          });
          
          if (res.code === 200) {
            message.success(`用户已${statusText}`);
            handleRefresh();
          } else {
            message.error(res.message || `${statusText}失败`);
          }
        } catch (error) {
          message.error(error.message || `${statusText}失败`);
        }
      }
    });
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
      width: 200,
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
            icon={<PoweroffOutlined />} 
            size="small"
            title={record.status === 1 ? '禁用' : '启用'}
            danger={record.status === 1}
            onClick={() => handleChangeStatus(record)}
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
            value={stats.total}
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
      <ProTable
        key={refreshKey}
        title="用户列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAddUser}>
            新增用户
          </Button>
        }
        columns={columns}
        request={fetchUsers}
        params={query}
        rowKey="id"
        onDataChange={handleDataChange}
      />

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
