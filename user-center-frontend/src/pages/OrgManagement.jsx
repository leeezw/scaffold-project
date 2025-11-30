import { useEffect, useState } from 'react';
import { Card, Tree, Space, Button, Modal, Form, Input, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import usePermission from '../hooks/usePermission.js';
import './OrgManagement.css';

const DEFAULT_TENANT = 1;

export default function OrgManagement() {
  const [tenantOptions, setTenantOptions] = useState([]);
  const [tenantId, setTenantId] = useState(DEFAULT_TENANT);
  const [treeData, setTreeData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [editingNode, setEditingNode] = useState(null);
  const { hasPermission } = usePermission();

  const loadTenants = async () => {
    try {
      const res = await request.get('/tenants');
      if (res.code === 200) {
        const options = (res.data || []).map(item => ({ label: item.name, value: item.id }));
        setTenantOptions(options);
        if (!tenantId && options.length > 0) {
          setTenantId(options[0].value);
        }
      }
    } catch (error) {
      message.error(error.message || '加载租户失败');
    }
  };

  const loadTree = async (id) => {
    if (!id) return;
    setLoading(true);
    try {
      const res = await request.get('/orgs/tree', { params: { tenantId: id } });
      if (res.code === 200) {
        const list = Array.isArray(res.data) ? res.data : [];
        const transform = (nodes) => nodes.map(node => ({
          key: node.id,
          title: node.name,
          children: node.children ? transform(node.children) : [],
          raw: node,
        }));
        setTreeData(transform(list));
      }
    } catch (error) {
      message.error(error.message || '加载组织失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTenants();
  }, []);

  useEffect(() => {
    if (tenantId) {
      loadTree(tenantId);
    }
  }, [tenantId]);

  const openModal = (parentId, record) => {
    setEditingNode(record || null);
    form.resetFields();
    form.setFieldsValue({
      tenantId: tenantId,
      parentId: record ? record.parentId : (parentId || 0),
      type: 'DEPT',
      status: 1,
      name: record?.name,
      sort: record?.sort || 0,
    });
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingNode) {
        const res = await request.put(`/orgs/${editingNode.id}`, values);
        if (res.code === 200) {
          message.success('更新成功');
          setModalVisible(false);
          loadTree(tenantId);
        }
      } else {
        const res = await request.post('/orgs', values);
        if (res.code === 200) {
          message.success('创建成功');
          setModalVisible(false);
          loadTree(tenantId);
        }
      }
    } catch {
      // ignore
    }
  };

  const handleDelete = (node) => {
    Modal.confirm({
      title: '确认删除部门？',
      content: `确定删除【${node.raw.name}】吗？`,
      onOk: async () => {
        try {
          const res = await request.delete(`/orgs/${node.key}`);
          if (res.code === 200) {
            message.success('删除成功');
            loadTree(tenantId);
          }
        } catch (error) {
          message.error(error.message || '删除失败');
        }
      },
    });
  };

  const renderTitle = (node) => (
    <div className="org-node">
      <span>{node.title}</span>
      <Space>
        {hasPermission('org:create') && (
          <Button size="small" type="link" onClick={() => openModal(node.key)}>
            <PlusOutlined />
          </Button>
        )}
        {hasPermission('org:update') && (
          <Button size="small" type="link" onClick={() => openModal(node.raw.parentId, node.raw)}>
            <EditOutlined />
          </Button>
        )}
        {hasPermission('org:delete') && (
          <Button size="small" type="link" danger onClick={() => handleDelete(node)}>
            <DeleteOutlined />
          </Button>
        )}
      </Space>
    </div>
  );

  const enhancedTreeData = treeData.map(node => ({
    ...node,
    title: renderTitle(node),
    children: node.children?.map(child => ({
      ...child,
      title: renderTitle(child),
    })),
  }));

  return (
    <div className="org-page">
      <Card
        title="组织管理"
        extra={(
          <Space>
            <Select
              placeholder="选择租户"
              options={tenantOptions}
              value={tenantId}
              onChange={setTenantId}
              style={{ width: 200 }}
            />
            {hasPermission('org:create') && (
              <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal(0)}>
                新增根节点
              </Button>
            )}
          </Space>
        )}
      >
        <Tree
          showLine
          loading={loading}
          treeData={enhancedTreeData}
          defaultExpandAll
        />
      </Card>

      <Modal
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        title={editingNode ? '编辑组织' : '新增组织'}
      >
        <Form layout="vertical" form={form}>
          <Form.Item name="tenantId" label="租户">
            <Select options={tenantOptions} disabled />
          </Form.Item>
          <Form.Item name="parentId" label="父级ID">
            <Input disabled />
          </Form.Item>
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: '请输入名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="type" label="类型">
            <Select
              options={[
                { label: '组织', value: 'ORG' },
                { label: '部门', value: 'DEPT' },
              ]}
            />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <Input type="number" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: 1 },
                { label: '禁用', value: 0 },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
