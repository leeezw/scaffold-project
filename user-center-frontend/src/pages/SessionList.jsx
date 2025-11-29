import { useEffect, useState } from 'react';
import request from '../api/index.js';
import './SessionList.css';

export default function SessionList() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchSessions = async () => {
      setLoading(true);
      try {
        const res = await request.get('/auth/session/my-sessions');
        if (res.code === 200) {
          setSessions(res.data || []);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchSessions();
  }, []);

  return (
    <div className="session-list-page">
      <div className="data-table-card">
        <div className="table-header">
          <h2 className="table-title">我的 Session</h2>
        </div>
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <span>加载中...</span>
          </div>
        ) : sessions.length === 0 ? (
          <div className="empty-state-full">
            <div className="empty-content">
              <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                <circle cx="32" cy="32" r="30" stroke="currentColor" strokeWidth="2" strokeDasharray="4 4"/>
                <path d="M32 20V32M32 44H32.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              <p>暂无 Session</p>
              <span className="empty-hint">您当前没有活跃的会话</span>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Session ID</th>
                  <th>创建时间</th>
                  <th>最后活动</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map((session, index) => (
                  <tr key={index}>
                    <td>
                      <code className="session-id">{session}</code>
                    </td>
                    <td>-</td>
                    <td>-</td>
                    <td>
                      <span className="status-badge status-enabled">活跃</span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-icon" title="查看详情">
                          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                            <path d="M8 1C4.13401 1 1 4.13401 1 8C1 11.866 4.13401 15 8 15C11.866 15 15 11.866 15 8C15 4.13401 11.866 1 8 1ZM8 13.5C5.51472 13.5 3.5 11.4853 3.5 9C3.5 6.51472 5.51472 4.5 8 4.5C10.4853 4.5 12.5 6.51472 12.5 9C12.5 11.4853 10.4853 13.5 8 13.5Z" fill="currentColor"/>
                            <path d="M8 6C7.17157 6 6.5 6.67157 6.5 7.5C6.5 8.32843 7.17157 9 8 9C8.82843 9 9.5 8.32843 9.5 7.5C9.5 6.67157 8.82843 6 8 6Z" fill="currentColor"/>
                          </svg>
                        </button>
                        <button className="btn-icon btn-icon-danger" title="退出登录">
                          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                            <path d="M6 12L10 8L6 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M10 8H2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
