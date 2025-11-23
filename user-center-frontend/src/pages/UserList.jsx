import { useEffect, useState } from 'react';
import request from '../api/index.js';

export default function UserList() {
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [query, setQuery] = useState({ keyword: '', pageNum: 1, pageSize: 10 });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
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
    fetchUsers();
  }, [query]);

  return (
    <div className="card">
      <div className="card-header">
        <h2>用户列表</h2>
        <div>
          <input
            placeholder="搜索用户名"
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value, pageNum: 1 })}
          />
        </div>
      </div>
      {loading ? (
        <p>加载中...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>昵称</th>
              <th>邮箱</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            {data.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.nickname}</td>
                <td>{user.email}</td>
                <td>{user.status === 1 ? '启用' : '禁用'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <div className="pagination">
        <button
          disabled={query.pageNum === 1}
          onClick={() => setQuery({ ...query, pageNum: query.pageNum - 1 })}
        >
          上一页
        </button>
        <span>
          {query.pageNum} / {Math.ceil(total / query.pageSize) || 1}
        </span>
        <button
          disabled={query.pageNum >= Math.ceil(total / query.pageSize)}
          onClick={() => setQuery({ ...query, pageNum: query.pageNum + 1 })}
        >
          下一页
        </button>
      </div>
    </div>
  );
}
