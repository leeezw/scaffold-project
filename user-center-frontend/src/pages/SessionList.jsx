import { useEffect, useState } from 'react';
import request from '../api/index.js';

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
    <div className="card">
      <div className="card-header">
        <h2>我的 Session</h2>
      </div>
      {loading ? (
        <p>加载中...</p>
      ) : sessions.length === 0 ? (
        <p>暂无 session</p>
      ) : (
        <ul>
          {sessions.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
