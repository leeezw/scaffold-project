import { useAuthContext } from './AuthProvider.jsx';

export default function usePermission() {
  const { permissions } = useAuthContext();
  
  const hasPermission = (required) => {
    if (!required) return true;
    const requiredList = Array.isArray(required) ? required : [required];
    if (!permissions || permissions.length === 0) {
      return false;
    }
    return requiredList.some(code => permissions.includes(code));
  };
  
  return { hasPermission };
}
