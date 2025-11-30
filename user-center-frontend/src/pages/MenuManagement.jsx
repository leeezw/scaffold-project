import { useEffect, useState } from 'react';
import { Card, Tree, Button, Modal, Form, Input, Select, Switch, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import usePermission from '../hooks/usePermission.js';
import './MenuManagement.css';

export default function MenuManagement() {
  const [treeData, setTreeData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [editingNode, setEditingNode] = useState(null);
  const { hasPermission } = usePermission();

  const loadTree = async () => {
    setLoading(true);
    try {
      const res = await request.get('/menus/tree');
      if (res.code === 200) {
        const transform = (nodes) => nodes.filter(Boolean).map(node => ({
          key: node.id,
          title: node.name,
          raw: node,
          children: node.children ? transform(node.children) : [],
        }));
        setTreeData(transform(Array.isArray(res.data) ? res.data : []));
      }
    } catch (error) {
      message.error(error.message || '加载菜单失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTree();
  }, []);

  const openModal = (parentId, record) => {
    setEditingNode(record || null);
    form.resetFields();
    form.setFieldsValue({
      parentId: record ? record.parentId : parentId || 0,
      type: record?.type || 'MENU',
      visible: record?.visible ?? true,
      status: record?.status ?? 1,
      name: record?.name,
      path: record?.path,
      component: record?.component,
      permission: record?.permission,
      sort: record?.sort || 0,
      icon: record?.icon,
    });
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingNode) {
        const res = await request.put(`/menus/${editingNode.id}`, values);
        if (res.code === 200) {
          message.success('更新成功');
          setModalVisible(false);
          loadTree();
        }
      } else {
        const res = await request.post('/menus', values);
        if (res.code === 200) {
          message.success('创建成功');
          setModalVisible(false);
          loadTree();
        }
      }
    } catch {
      // ignore
    }
  };

  const handleDelete = (node) => {
    Modal.confirm({
      title: '确认删除菜单？',
      content: `确定删除【${node.raw.name}】吗？`,
      onOk: async () => {
        try {
          const res = await request.delete(`/menus/${node.key}`);
          if (res.code === 200) {
            message.success('删除成功');
            loadTree();
          }
        } catch (error) {
          message.error(error.message || '删除失败');
        }
      },
    });
  };

  const renderTitle = (node) => (
    <div className="menu-node">
      <span>{node.title}</span>
      <span className="menu-type">{node.raw.type}</span>
      <div>
        {hasPermission('menu:create') && (
          <Button size="small" type="link" onClick={() => openModal(node.key)}>
            <PlusOutlined />
          </Button>
        )}
        {hasPermission('menu:update') && (
          <Button size="small" type="link" onClick={() => openModal(node.raw.parentId, node.raw)}>
            <EditOutlined />
          </Button>
        )}
        {hasPermission('menu:delete') && (
          <Button size="small" type="link" danger onClick={() => handleDelete(node)}>
            <DeleteOutlined />
          </Button>
        )}
      </div>
    </div>
  );

  const enhancedTree = treeData.map(node => ({
    ...node,
    title: renderTitle(node),
    children: node.children?.map(child => ({
      ...child,
      title: renderTitle(child),
    })),
  }));

  return (
    <div className="menu-page">
      <Card
        title="菜单管理"
        extra={hasPermission('menu:create') && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal(0)}>
            新增菜单
          </Button>
        )}
      >
        <Tree
          showLine
          loading={loading}
          treeData={enhancedTree}
          defaultExpandAll
        />
      </Card>

      <Modal
        open={modalVisible}
        title={editingNode ? '编辑菜单' : '新增菜单'}
        onCancel={() => setModalVisible(false)}
        onOk={handleSubmit}
        destroyOnClose
      >
        <Form layout="vertical" form={form}>
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
                { label: '目录', value: 'CATALOG' },
                { label: '菜单', value: 'MENU' },
                { label: '按钮', value: 'BUTTON' },
              ]}
            />
          </Form.Item>
          <Form.Item name="path" label="路由地址">
            <Input />
          </Form.Item>
          <Form.Item name="component" label="组件路径">
            <Input />
          </Form.Item>
          <Form.Item name="permission" label="权限标识">
            <Input />
          </Form.Item>
          <Form.Item name="icon" label="图标标识">
            <Input placeholder="如：HomeOutlined" />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <Input type="number" />
          </Form.Item>
          <Form.Item name="visible" label="是否显示" valuePropName="checked">
            <Switch />
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
