
import { useEffect, useMemo, useState } from 'react';
import { Button, Card, DatePicker, Form, Input, message, Modal, Result, Space, Switch, Table, Tooltip } from 'antd';
import { PlusOutlined, ReloadOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import request from '../api/index.js';
import { useAuthContext } from '../hooks/AuthProvider.jsx';
import './TenantList.css';

const { TextArea } = Input;

const initialPagination = { current: 1, pageSize: 10, total: 0 };

export default function TenantList() {
  const { user } = useAuthContext();
  const hasTenantPermission = useMemo(() => {
    if (!user) return false;
    const perms = Array.isArray(user.permissions) ? user.permissions : [];
    const hasPrivilege = perms.includes('tenant:list') || perms.includes('*:*:*');
    return user.tenantId === 0 && hasPrivilege;
  }, [user]);

  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState([]);
  const [pagination, setPagination] = useState(initialPagination);
  const [keyword, setKeyword] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    if (hasTenantPermission) {
      fetchTenants({ pageNum: pagination.current, pageSize: pagination.pageSize, keyword });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hasTenantPermission]);

  const fetchTenants = async ({ pageNum = 1, pageSize = 10, keyword: kw = '' } = {}) => {
    setLoading(true);
    try {
      const res = await request.get('/tenants', {
        params: {
          pageNum,
          pageSize,
          keyword: kw || undefined,
        },
      });
      if (res.code === 200 && res.data) {
        const pageData = res.data.pageData || res.data;
        setTableData(pageData.list || []);
        setPagination({
          current: pageData.pageNum || pageNum,
          pageSize: pageData.pageSize || pageSize,
          total: pageData.total || 0,
        });
      } else {
        message.error(res.message || '获取租户列表失败');
      }
    } catch (error) {
      message.error(error.message || '获取租户列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = ({ current, pageSize }) => {
    fetchTenants({ pageNum: current, pageSize, keyword });
  };

  const handleSearch = () => {
    fetchTenants({ pageNum: 1, pageSize: pagination.pageSize, keyword });
  };

  const openCreateModal = () => {
    setEditingRecord(null);
    form.resetFields();
    setModalVisible(true);
  };

  const openEditModal = (record) => {
    setEditingRecord(record);
    form.setFieldsValue({
      ...record,
      expiredAt: record.expiredAt ? dayjs(record.expiredAt) : null,
    });
    setModalVisible(true);
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    form.resetFields();
    setEditingRecord(null);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitLoading(true);
      const payload = {
        ...values,
        expiredAt: values.expiredAt ? values.expiredAt.format('YYYY-MM-DD') : null,
      };
      let res;
      if (editingRecord) {
        res = await request.put(`/tenants/${editingRecord.id}`, payload);
      } else {
        res = await request.post('/tenants', payload);
      }
      if (res.code === 200) {
        message.success(editingRecord ? '租户更新成功' : '租户创建成功');
        handleModalCancel();
        fetchTenants({ pageNum: pagination.current, pageSize: pagination.pageSize, keyword });
      } else {
        message.error(res.message || '操作失败');
      }
    } catch (error) {
      if (error?.errorFields) {
        return;
      }
      message.error(error.message || '操作失败');
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除租户',
      content: `确定要删除租户「${record.name}」吗？此操作不可恢复。`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await request.delete(`/tenants/${record.id}`);
          if (res.code === 200) {
            message.success('已删除租户');
            fetchTenants({ pageNum: pagination.current, pageSize: pagination.pageSize, keyword });
          } else {
            message.error(res.message || '删除失败');
          }
        } catch (error) {
          message.error(error.message || '删除失败');
        }
      },
    });
  };

  const handleStatusChange = async (record, checked) => {
    try {
      const res = await request.patch(`/tenants/${record.id}/status`, {}, {
        params: { status: checked ? 1 : 0 },
      });
      if (res.code === 200) {
        message.success('租户状态已更新');
      } else {
        message.error(res.message || '更新状态失败');
      }
    } catch (error) {
      message.error(error.message || '更新状态失败');
    } finally {
      fetchTenants({ pageNum: pagination.current, pageSize: pagination.pageSize, keyword });
    }
  };

  const columns = [
    {
      title: '租户编码',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '租户名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '负责人',
      dataIndex: 'contactName',
      key: 'contactName',
      render: (text) => text || '--',
    },
    {
      title: '联系方式',
      dataIndex: 'contactPhone',
      key: 'contactPhone',
      render: (text, record) => text || record.contactEmail || '--',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (_, record) => (
        <Switch
          checked={record.status === 1}
          checkedChildren="启用"
          unCheckedChildren="停用"
          onChange={(checked) => handleStatusChange(record, checked)}
        />
      ),
    },
    {
      title: '到期日',
      dataIndex: 'expiredAt',
      key: 'expiredAt',
      render: (text) => text || '--',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (text) => text || '--',
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="编辑">
            <Button type="text" icon={<EditOutlined />} onClick={() => openEditModal(record)} />
          </Tooltip>
          <Tooltip title="删除">
            <Button type="text" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)} />
          </Tooltip>
        </Space>
      ),
    },
  ];

  if (!hasTenantPermission) {
    return (
      <div className="tenant-page">
        <Result
          status="403"
          title="暂无权限"
          subTitle="仅平台超级管理员可以访问租户配置界面。"
        />
      </div>
    );
  }

  return (
    <div className="tenant-page">
      <div className="tenant-toolbar">
        <Input.Search
          className="tenant-search"
          placeholder="搜索租户编码、名称或联系人"
          allowClear
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onSearch={handleSearch}
        />
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchTenants({ pageNum: pagination.current, pageSize: pagination.pageSize, keyword })}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            新建租户
          </Button>
        </Space>
      </div>

      <Card className="tenant-table-card">
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={tableData}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title={editingRecord ? '编辑租户' : '新建租户'}
        open={modalVisible}
        onCancel={handleModalCancel}
        onOk={handleSubmit}
        confirmLoading={submitLoading}
        okText={editingRecord ? '保存' : '创建'}
        width={640}
        destroyOnClose
      >
        <Form form={form} layout="vertical" initialValues={{ status: 1 }}>
          <Form.Item
            name="code"
            label="租户编码"
            rules={[{ required: true, message: '请输入租户编码' }]}
          >
            <Input placeholder="例如 TENANT_001" disabled={!!editingRecord} />
          </Form.Item>
          <Form.Item
            name="name"
            label="租户名称"
            rules={[{ required: true, message: '请输入租户名称' }]}
          >
            <Input placeholder="租户名称" />
          </Form.Item>
          <Form.Item
            name="contactName"
            label="联系人"
            rules={[{ required: true, message: '请输入联系人' }]}
          >
            <Input placeholder="联系人姓名" />
          </Form.Item>
          <Form.Item
            name="contactPhone"
            label="联系电话"
            rules={[{ required: true, message: '请输入联系电话' }]}
          >
            <Input placeholder="手机号或座机" />
          </Form.Item>
          <Form.Item
            name="contactEmail"
            label="联系邮箱"
            rules={[{ type: 'email', message: '请输入有效邮箱' }]}
          >
            <Input placeholder="邮箱" />
          </Form.Item>
          <Form.Item name="industry" label="所在行业">
            <Input placeholder="例如 互联网" />
          </Form.Item>
          <Form.Item name="level" label="套餐等级">
            <Input placeholder="例如 标准版 / 专业版" />
          </Form.Item>
          <Form.Item name="expiredAt" label="到期日期">
            <DatePicker className="tenant-date-picker" placeholder="请选择到期时间" />
          </Form.Item>
          <Form.Item name="configJson" label="自定义配置 (JSON)">
            <TextArea rows={3} placeholder='例如 {"maxUser":100}' />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <TextArea rows={2} placeholder="备注信息" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
