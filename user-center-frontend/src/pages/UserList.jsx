import { useState, useRef, useCallback, useEffect } from 'react';
import { Card, Statistic, Button, Space, Tag, Modal, Form, message, Input, Select } from 'antd';
import { 
  UserOutlined, 
  CheckCircleOutlined, 
  StopOutlined, 
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PoweroffOutlined,
} from '@ant-design/icons';
import { ProFormText, ProFormSelect } from '@ant-design/pro-components';
import request from '../api/index.js';
import UserForm from '../components/UserForm.jsx';
import ProTableV2 from '../components/ProTableV2.jsx';
import './UserList.css';

const { Search } = Input;

export default function UserList() {
  const actionRef = useRef();
  const [stats, setStats] = useState({
    total: 0,
    enabled: 0,
    disabled: 0,
    today: 0
  });
  const [form] = Form.useForm();
  const [filterForm] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [filterParams, setFilterParams] = useState({ status: 'all' });
  const debounceTimerRef = useRef(null);

  // 防抖处理筛选
  const handleFilterChange = useCallback(() => {
    // 清除之前的定时器
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
    // 设置新的定时器，500ms 后执行搜索
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

  // 请求函数 - 适配 ProTableV2
  const fetchUsers = async (params) => {
    try {
      const res = await request.get('/users/page', { params });
      // 处理新的响应结构：{ code, data: { pageData: { list, total }, total, enabledCount, disabledCount, todayNewCount } }
      if (res.code === 200 && res.data) {
        // 更新统计数据
        setStats({
          total: res.data.total || 0,
          enabled: res.data.enabledCount || 0,
          disabled: res.data.disabledCount || 0,
          today: res.data.todayNewCount || 0
        });
        
        // 返回 ProTableV2 期望的格式
        return {
          code: 200,
          data: res.data.pageData || { list: [], total: 0 }
        };
      }
      return res;
    } catch (error) {
      console.error('fetchUsers error:', error);
      return {
        code: 500,
        data: { list: [], total: 0 }
      };
    }
  };

  // 处理数据变化（仅用于通知，不触发刷新，避免无限循环）
  const handleDataChange = (data, total) => {
    console.log('handleDataChange', data, total);
    // 统计数据已从后端获取，这里不需要再计算
  };

  const handleRefresh = () => {
    actionRef.current?.reload();
  };

  const handleAddUser = () => {
    setEditingUser(null);
    form.resetFields();
    // 确保表单字段完全清空
    form.setFieldsValue({
      username: undefined,
      password: undefined,
      nickname: undefined,
      email: undefined,
      status: 1,
    });
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
          form.resetFields();
          setEditingUser(null);
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
          form.resetFields();
          // 确保表单字段完全清空
          form.setFieldsValue({
            username: undefined,
            password: undefined,
            nickname: undefined,
            email: undefined,
            status: 1,
          });
          setEditingUser(null);
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
    // 清空表单字段
    form.setFieldsValue({
      username: undefined,
      password: undefined,
      nickname: undefined,
      email: undefined,
      status: 1,
    });
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
      hideInSearch: true,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      ellipsis: true,
    },
    {
      title: '昵称',
      dataIndex: 'nickname',
      key: 'nickname',
      ellipsis: true,
      hideInSearch: true,
      render: (text) => text || '-',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      ellipsis: true,
      hideInSearch: true,
      render: (text) => text || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      valueType: 'select',
      valueEnum: {
        1: {
          text: '启用',
          status: 'Success',
        },
        0: {
          text: '禁用',
          status: 'Error',
        },
      },
      render: (_, record) => (
        <Tag color={record.status === 1 ? 'success' : 'error'}>
          {record.status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
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
      <ProTableV2
        actionRef={actionRef}
        headerTitle="用户列表"
        columns={columns}
        request={fetchUsers}
        rowKey="id"
        onDataChange={handleDataChange}
        search={false}
        toolbar={{
          filter: (
            <Form
              form={filterForm}
              layout="inline"
              initialValues={{ status: 'all' }}
              onFinish={async (values) => {
                // 当筛选条件变化时，更新筛选参数并触发表格刷新
                const newFilterParams = { ...values };
                // 处理 status 参数（如果存在且是 'all'，则移除）
                if (newFilterParams.status === 'all') {
                  delete newFilterParams.status;
                }
                setFilterParams(newFilterParams);
                // 触发表格刷新，ProTable 会将 filterParams 合并到 requestParams 中
                actionRef.current?.reload();
              }}
              className="filter-form"
            >
              <Form.Item name="keyword" style={{ marginBottom: 0 }}>
                <Input
                  className='filter-search-input'
                  placeholder="搜索用户名、昵称、邮箱或手机号"
                  allowClear
                  onChange={handleFilterChange}
                  onPressEnter={() => filterForm.submit()}
                />
              </Form.Item>
              <Form.Item name="status" style={{ marginBottom: 0 }}>
                <Select
                  className='filter-status-select'
                  placeholder="用户状态"
                  allowClear
                  onChange={() => filterForm.submit()}
                >
                  <Select.Option value="all">全部</Select.Option>
                  <Select.Option value={1}>启用</Select.Option>
                  <Select.Option value={0}>禁用</Select.Option>
                </Select>
              </Form.Item>
            </Form>
          ),
          actions: [
            <Button 
              key="add" 
              type="primary" 
              icon={<PlusOutlined />} 
              onClick={handleAddUser}
            >
              新增用户
            </Button>,
          ],
        }}
        params={filterParams}
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
          key={editingUser ? `edit-${editingUser.id}` : 'add'}
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
