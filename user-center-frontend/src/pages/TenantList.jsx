import { useEffect, useState } from 'react';
import { Table, Button, Space, Modal, Form, Input, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import usePermission from '../hooks/usePermission.js';
import './TenantList.css';

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

export default function TenantList() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [editing, setEditing] = useState(null);
  const { hasPermission } = usePermission();

  const loadTenants = async () => {
    setLoading(true);
    try {
      const res = await request.get('/tenants');
      if (res.code === 200) {
        setData(Array.isArray(res.data) ? res.data : []);
      }
    } catch (error) {
      message.error(error.message || '加载租户失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTenants();
  }, []);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editing) {
        const res = await request.put(`/tenants/${editing.id}`, values);
        if (res.code === 200) {
          message.success('更新成功');
          setModalVisible(false);
          setEditing(null);
          loadTenants();
        }
      } else {
        const res = await request.post('/tenants', values);
        if (res.code === 200) {
          message.success('创建成功');
          setModalVisible(false);
          loadTenants();
        }
      }
    } catch {
      // ignore
    }
  };

  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除租户？',
      content: `删除后将无法恢复，确定删除租户【${record.name}】吗？`,
      onOk: async () => {
        try {
          const res = await request.delete(`/tenants/${record.id}`);
          if (res.code === 200) {
            message.success('删除成功');
            loadTenants();
          }
        } catch (error) {
          message.error(error.message || '删除失败');
        }
      },
    });
  };

  const openModal = (record) => {
    if (record) {
      setEditing(record);
      form.setFieldsValue(record);
    } else {
      setEditing(null);
      form.resetFields();
      form.setFieldsValue({ status: 1 });
    }
    setModalVisible(true);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '编码', dataIndex: 'code' },
    { title: '名称', dataIndex: 'name' },
    { 
      title: '状态', 
      dataIndex: 'status',
      render: (status) => status === 1 ? '启用' : '禁用',
    },
    { title: '备注', dataIndex: 'remark' },
    {
      title: '操作',
      width: 160,
      render: (_, record) => (
        <Space>
          {hasPermission('tenant:update') && (
            <Button type="link" icon={<EditOutlined />} onClick={() => openModal(record)}>
              编辑
            </Button>
          )}
          {hasPermission('tenant:delete') && (
            <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
              删除
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="tenant-page">
      <div className="tenant-header">
        <h2>租户管理</h2>
        {hasPermission('tenant:create') && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>
            新增租户
          </Button>
        )}
      </div>

      <Table
        rowKey="id"
        dataSource={data}
        columns={columns}
        loading={loading}
      />

      <Modal
        open={modalVisible}
        title={editing ? '编辑租户' : '新增租户'}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        destroyOnClose
      >
        <Form layout="vertical" form={form} initialValues={{ status: 1 }}>
          <Form.Item
            label="租户编码"
            name="code"
            rules={[{ required: true, message: '请输入租户编码' }]}
          >
            <Input placeholder="请输入编码" disabled={!!editing} />
          </Form.Item>
          <Form.Item
            label="租户名称"
            name="name"
            rules={[{ required: true, message: '请输入租户名称' }]}
          >
            <Input placeholder="请输入名称" />
          </Form.Item>
          <Form.Item label="状态" name="status">
            <Select options={STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item label="备注" name="remark">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
