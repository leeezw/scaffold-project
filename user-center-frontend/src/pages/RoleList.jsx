import { useEffect, useState } from 'react';
import request from '../api/index.js';

export default function RoleList() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchRoles = async () => {
      setLoading(true);
      try {
        const res = await request.get('/roles');
        if (res.code === 200) {
          setData(res.data);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchRoles();
  }, []);

  return (
    <div className="card">
      <div className="card-header">
        <h2>角色列表</h2>
      </div>
      {loading ? (
        <p>加载中...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>编码</th>
              <th>名称</th>
              <th>状态</th>
              <th>权限数量</th>
            </tr>
          </thead>
          <tbody>
            {data.map((role) => (
              <tr key={role.id}>
                <td>{role.id}</td>
                <td>{role.code}</td>
                <td>{role.name}</td>
                <td>{role.status === 1 ? '启用' : '禁用'}</td>
                <td>{role.permissionIds?.length || 0}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
