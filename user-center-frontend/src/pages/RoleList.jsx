import { useState } from 'react';
import { Tag, Space, Button } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../api/index.js';
import ProTable from '../components/ProTable.jsx';
import './RoleList.css';

export default function RoleList() {
  const [refreshKey, setRefreshKey] = useState(0);

  // 请求函数
  const fetchRoles = async () => {
    const res = await request.get('/roles');
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
      render: (status) => (
        <Tag color={status === 1 ? 'success' : 'error'}>
          {status === 1 ? '启用' : '禁用'}
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
      <ProTable
        key={refreshKey}
        title="角色列表"
        columns={columns}
        request={fetchRoles}
        params={{}}
        rowKey="id"
        showPagination={false}
      />
    </div>
  );
}
