import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import request from '../api/index.js';
import { useAuthContext } from '../hooks/AuthProvider.jsx';
import './Login.css';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { setToken, setUser } = useAuthContext();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      // 根据 OpenAPI 规范：/api/auth/login POST
      // Request: { username, password }
      // Response: ResultMapStringObject { code, message, data: Map<String, Object>, timestamp, success }
      const res = await request.post('/auth/login', { 
        username, 
        password
      });
      
      if (res.code !== 200) {
        throw new Error(res.message || '登录失败');
      }
      
      // 根据 OpenAPI 规范，响应是 ResultMapStringObject
      // data 是一个 Map<String, Object>，通常包含 token 和 user 信息
      // 根据实际返回结构处理
      const data = res.data || {};
      
      // 尝试多种可能的 token 字段名
      const token = data.token || data.accessToken || data.access_token || data.Token;
      
      if (!token) {
        throw new Error('登录响应中未找到 token');
      }
      
      setToken(token);
      
      // 如果响应中包含用户信息，直接使用
      // 注意：根据 OpenAPI，data 是 Map<String, Object>，可能需要根据实际返回结构调整
      let user = data.user || data.userInfo || data.loginUser || data.User;
      
      // 如果登录响应中没有用户信息，调用 /api/auth/current 获取
      if (!user) {
        try {
          // 根据 OpenAPI 规范：/api/auth/current GET
          // 需要 loginUser 查询参数，但通常只需要 token 即可
          // 先尝试不传参数，如果失败再传空对象
          const currentUserRes = await request.get('/auth/current', {
            params: {}
          });
          if (currentUserRes.code === 200 && currentUserRes.data) {
            user = currentUserRes.data;
          }
        } catch (err) {
          console.warn('获取当前用户信息失败:', err);
          // 如果获取失败，尝试传递空对象作为 loginUser 参数
          try {
            const currentUserRes = await request.get('/auth/current', {
              params: { loginUser: {} }
            });
            if (currentUserRes.code === 200 && currentUserRes.data) {
              user = currentUserRes.data;
            }
          } catch (err2) {
            console.warn('获取当前用户信息失败（第二次尝试）:', err2);
          }
        }
      }
      
      // 设置用户信息
      if (user) {
        setUser(user);
      }
      
      // 如果选择了记住我，可以设置更长的过期时间（前端处理）
      if (rememberMe) {
        // 前端记住用户信息，但 token 过期时间由后端控制
        localStorage.setItem('uc_remember', 'true');
      } else {
        localStorage.removeItem('uc_remember');
      }
      
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || err.message || '登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-form-container">
        <h1 className="form-title">登录用户中心</h1>

        <form className="login-form" onSubmit={handleSubmit}>
          {error && (
            <div className="login-error">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="7" fill="rgba(125, 40, 40, 1)"/>
                <path d="M8 4V8M8 12H8.01" stroke="white" strokeWidth="1.5" strokeLinecap="round"/>
              </svg>
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">用户名</label>
            <div className="input-wrapper">
              <input
                type="text"
                className="form-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="请输入用户名"
                required
                autoComplete="username"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">密码</label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? "text" : "password"}
                className="form-input password-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="请输入密码"
                required
                autoComplete="current-password"
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? (
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10 3.75C5.83333 3.75 2.275 6.34167 0.833333 10C2.275 13.6583 5.83333 16.25 10 16.25C14.1667 16.25 17.725 13.6583 19.1667 10C17.725 6.34167 14.1667 3.75 10 3.75ZM10 14.1667C7.7 14.1667 5.83333 12.3 5.83333 10C5.83333 7.7 7.7 5.83333 10 5.83333C12.3 5.83333 14.1667 7.7 14.1667 10C14.1667 12.3 12.3 14.1667 10 14.1667ZM10 7.5C8.61667 7.5 7.5 8.61667 7.5 10C7.5 11.3833 8.61667 12.5 10 12.5C11.3833 12.5 12.5 11.3833 12.5 10C12.5 8.61667 11.3833 7.5 10 7.5Z" fill="#7d8592"/>
                    <path d="M1.66667 1.66667L18.3333 18.3333" stroke="#7d8592" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                ) : (
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10 3.75C5.83333 3.75 2.275 6.34167 0.833333 10C2.275 13.6583 5.83333 16.25 10 16.25C14.1667 16.25 17.725 13.6583 19.1667 10C17.725 6.34167 14.1667 3.75 10 3.75ZM10 14.1667C7.7 14.1667 5.83333 12.3 5.83333 10C5.83333 7.7 7.7 5.83333 10 5.83333C12.3 5.83333 14.1667 7.7 14.1667 10C14.1667 12.3 12.3 14.1667 10 14.1667ZM10 7.5C8.61667 7.5 7.5 8.61667 7.5 10C7.5 11.3833 8.61667 12.5 10 12.5C11.3833 12.5 12.5 11.3833 12.5 10C12.5 8.61667 11.3833 7.5 10 7.5Z" fill="#7d8592"/>
                  </svg>
                )}
              </button>
            </div>
          </div>

          <div className="form-options">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="checkbox-input"
              />
              <span className="checkbox-text">记住我</span>
            </label>
            <a href="#" className="forgot-password">忘记密码？</a>
          </div>

          <button 
            type="submit" 
            className="login-button"
            disabled={loading}
          >
            {loading ? (
              <>
                <svg className="spinner" width="16" height="16" viewBox="0 0 16 16">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="2" fill="none" strokeDasharray="37.7" strokeDashoffset="9.4">
                    <animate attributeName="stroke-dasharray" values="37.7,37.7;18.85,56.55;37.7,37.7" dur="1s" repeatCount="indefinite"/>
                    <animate attributeName="stroke-dashoffset" values="0;-18.85;0" dur="1s" repeatCount="indefinite"/>
                  </circle>
                </svg>
                登录中...
              </>
            ) : (
              <>
                <span>登录</span>
                <img 
                  src="https://ide.code.fun/api/image?token=6922da31043f1900118e884b&name=17281b52a2fa8b6968a2ff5f41495b56.png"
                  alt=""
                  className="button-arrow"
                />
              </>
            )}
          </button>
        </form>

        <div className="form-footer">
          <p>还没有账户？</p>
        </div>
      </div>
    </div>
  );
}
