import { useRef } from 'react';
import { Tag, Space, Button } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import ProTableV2 from '../components/ProTableV2.jsx';
import './RoleList.css';

export default function RoleList() {
  const actionRef = useRef();

  // 请求函数
  const fetchRoles = async (params) => {
    const res = await request.get('/roles', { params });
    return res;
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '编码',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
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
      title: '权限数量',
      dataIndex: 'permissionIds',
      key: 'permissionIds',
      render: (permissionIds) => permissionIds?.length || 0,
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
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
    <div className="role-list-page">
      <ProTableV2
        actionRef={actionRef}
        headerTitle="角色列表"
        columns={columns}
        request={fetchRoles}
        rowKey="id"
        search={false}
        pagination={false}
      />
    </div>
  );
}
