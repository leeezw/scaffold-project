import { useEffect, useState } from 'react';
import request from '../api/index.js';
import './RoleList.css';

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
    <div className="role-list-page">
      <div className="data-table-card">
        <div className="table-header">
          <h2 className="table-title">角色列表</h2>
        </div>
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <span>加载中...</span>
          </div>
        ) : (
          <>
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>编码</th>
                    <th>名称</th>
                    <th>状态</th>
                    <th>权限数量</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {data.length === 0 ? (
                    <tr>
                      <td colSpan="6" className="empty-state">
                        <div className="empty-content">
                          <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                            <circle cx="32" cy="32" r="30" stroke="currentColor" strokeWidth="2" strokeDasharray="4 4"/>
                            <path d="M32 20V32M32 44H32.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                          </svg>
                          <p>暂无数据</p>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    data.map((role) => (
                      <tr key={role.id}>
                        <td>{role.id}</td>
                        <td>{role.code}</td>
                        <td>{role.name}</td>
                        <td>
                          <span className={`status-badge ${role.status === 1 ? 'status-enabled' : 'status-disabled'}`}>
                            {role.status === 1 ? '启用' : '禁用'}
                          </span>
                        </td>
                        <td>{role.permissionIds?.length || 0}</td>
                        <td>
                          <div className="action-buttons">
                            <button className="btn-icon" title="编辑">
                              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                                <path d="M11.3333 2.00001C11.5084 1.82491 11.7163 1.68605 11.9447 1.59128C12.1731 1.49651 12.4173 1.44775 12.6667 1.44775C12.916 1.44775 13.1602 1.49651 13.3886 1.59128C13.617 1.68605 13.8249 1.82491 14 2.00001C14.1751 2.1751 14.314 2.383 14.4087 2.6114C14.5035 2.8398 14.5523 3.08399 14.5523 3.33334C14.5523 3.58269 14.5035 3.82688 14.4087 4.05528C14.314 4.28368 14.1751 4.49158 14 4.66668L5.00001 13.6667L1.33334 14.6667L2.33334 11L11.3333 2.00001Z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                              </svg>
                            </button>
                            <button className="btn-icon" title="删除">
                              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                                <path d="M2 4H14M12.6667 4V13.3333C12.6667 13.687 12.5262 14.0261 12.2761 14.2761C12.0261 14.5262 11.687 14.6667 11.3333 14.6667H4.66667C4.31305 14.6667 3.97391 14.5262 3.72386 14.2761C3.47381 14.0261 3.33334 13.687 3.33334 13.3333V4M5.33334 4V2.66667C5.33334 2.31305 5.47381 1.97391 5.72386 1.72386C5.97391 1.47381 6.31305 1.33334 6.66667 1.33334H9.33334C9.68696 1.33334 10.0261 1.47381 10.2761 1.72386C10.5262 1.97391 10.6667 2.31305 10.6667 2.66667V4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                              </svg>
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
