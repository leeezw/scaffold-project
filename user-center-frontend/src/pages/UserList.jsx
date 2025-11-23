import { useEffect, useState } from 'react';
import request from '../api/index.js';
import './UserList.css';

export default function UserList() {
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [stats, setStats] = useState({
    total: 0,
    enabled: 0,
    disabled: 0,
    today: 0
  });
  const [query, setQuery] = useState({ keyword: '', pageNum: 1, pageSize: 10 });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, [query]);

  useEffect(() => {
    calculateStats();
  }, [data, total]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await request.get('/users/page', { params: query });
      if (res.code === 200) {
        setData(res.data.list);
        setTotal(res.data.total);
      }
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = () => {
    // 从当前页数据中计算统计数据（实际应该从后端获取完整统计）
    const enabled = data.filter(u => u.status === 1).length;
    const disabled = data.filter(u => u.status === 0).length;
    setStats({
      total: total,
      enabled: enabled,
      disabled: disabled,
      today: 0 // 需要后端支持
    });
  };

  return (
    <div className="user-list-page">
      {/* 顶部搜索栏 */}
      <div className="page-header">
        <div className="search-section">
          <div className="search-box">
            <svg className="search-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M9 17C13.4183 17 17 13.4183 17 9C17 4.58172 13.4183 1 9 1C4.58172 1 1 4.58172 1 9C1 13.4183 4.58172 17 9 17Z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M19 19L14.65 14.65" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <input
              type="text"
              className="search-input"
              placeholder="搜索用户名、昵称或邮箱"
              value={query.keyword}
              onChange={(e) => setQuery({ ...query, keyword: e.target.value, pageNum: 1 })}
            />
          </div>
          <button className="btn-primary" onClick={() => {/* 新增用户 */}}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 3V13M3 8H13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            新增用户
          </button>
        </div>
      </div>

      {/* 统计卡片 */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon stat-icon-blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2Z" stroke="currentColor" strokeWidth="1.5"/>
              <path d="M12 6V12L16 14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </div>
          <div className="stat-content">
            <div className="stat-label">用户总数</div>
            <div className="stat-value">{stats.total || total}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-green">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17L4 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <div className="stat-content">
            <div className="stat-label">已启用</div>
            <div className="stat-value">{stats.enabled}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-orange">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 8V12M12 16H12.01" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </div>
          <div className="stat-content">
            <div className="stat-label">已禁用</div>
            <div className="stat-value">{stats.disabled}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-purple">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2V6M12 18V22M4 12H8M16 12H20" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </div>
          <div className="stat-content">
            <div className="stat-label">今日新增</div>
            <div className="stat-value">{stats.today}</div>
          </div>
        </div>
      </div>

      {/* 数据表格 */}
      <div className="data-table-card">
        <div className="table-header">
          <h2 className="table-title">用户列表</h2>
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
                    <th>用户名</th>
                    <th>昵称</th>
                    <th>邮箱</th>
                    <th>状态</th>
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
                    data.map((user) => (
                      <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.username}</td>
                        <td>{user.nickname || '-'}</td>
                        <td>{user.email || '-'}</td>
                        <td>
                          <span className={`status-badge ${user.status === 1 ? 'status-enabled' : 'status-disabled'}`}>
                            {user.status === 1 ? '启用' : '禁用'}
                          </span>
                        </td>
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

            {/* 分页 */}
            {data.length > 0 && (
              <div className="pagination">
                <div className="pagination-info">
                  共 {total} 条，每页显示
                  <select
                    className="page-size-select"
                    value={query.pageSize}
                    onChange={(e) => setQuery({ ...query, pageSize: Number(e.target.value), pageNum: 1 })}
                  >
                    <option value="10">10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                  </select>
                  条
                </div>
                <div className="pagination-controls">
                  <button
                    className="btn-pagination"
                    disabled={query.pageNum === 1}
                    onClick={() => setQuery({ ...query, pageNum: query.pageNum - 1 })}
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M10 12L6 8L10 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    上一页
                  </button>
                  <div className="page-numbers">
                    {Array.from({ length: Math.min(5, Math.ceil(total / query.pageSize)) }, (_, i) => {
                      const totalPages = Math.ceil(total / query.pageSize);
                      let page;
                      if (totalPages <= 5) {
                        page = i + 1;
                      } else if (query.pageNum <= 3) {
                        page = i + 1;
                      } else if (query.pageNum >= totalPages - 2) {
                        page = totalPages - 4 + i;
                      } else {
                        page = query.pageNum - 2 + i;
                      }
                      if (page > totalPages || page < 1) return null;
                      return (
                        <button
                          key={page}
                          className={`btn-page ${page === query.pageNum ? 'active' : ''}`}
                          onClick={() => setQuery({ ...query, pageNum: page })}
                        >
                          {page}
                        </button>
                      );
                    })}
                  </div>
                  <button
                    className="btn-pagination"
                    disabled={query.pageNum >= Math.ceil(total / query.pageSize)}
                    onClick={() => setQuery({ ...query, pageNum: query.pageNum + 1 })}
                  >
                    下一页
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M6 4L10 8L6 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
