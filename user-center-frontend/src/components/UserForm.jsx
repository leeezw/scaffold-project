import { Form, Input, Select, TreeSelect, Spin } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import FormField from './FormField.jsx';
import request from '../api/index.js';
import { useAuthContext } from '../hooks/AuthProvider.jsx';
import './UserForm.css';

/**
 * 用户表单组件
 * 用于新增和编辑用户
 */
export default function UserForm({ form, initialValues, onFinish }) {
  const isEdit = !!initialValues;
  const { tenantId } = useAuthContext();
  const [deptTree, setDeptTree] = useState([]);
  const [positionOptions, setPositionOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const departmentIds = Form.useWatch('departmentIds', form) || [];
  const positionIds = Form.useWatch('positionIds', form) || [];

  useEffect(() => {
    if (!tenantId) {
      setDeptTree([]);
      setPositionOptions([]);
      return;
    }
    let isMounted = true;
    setLoading(true);
    Promise.all([
      request.get('/departments/tree', { params: { tenantId } }),
      request.get('/positions/options', { params: { tenantId } })
    ]).then(([deptRes, posRes]) => {
      if (!isMounted) return;
      if (deptRes.code === 200) {
        const tree = (deptRes.data || []).map(transformDeptNode);
        setDeptTree(tree);
      }
      if (posRes.code === 200) {
        const options = (posRes.data || []).map((item) => ({
          label: item.name,
          value: item.id
        }));
        setPositionOptions(options);
      }
    }).catch(() => {
      if (!isMounted) return;
      setDeptTree([]);
      setPositionOptions([]);
    }).finally(() => {
      if (isMounted) {
        setLoading(false);
      }
    });
    return () => {
      isMounted = false;
    };
  }, [tenantId]);

  const primaryDeptOptions = useMemo(() => {
    if (!Array.isArray(departmentIds) || departmentIds.length === 0) {
      return [];
    }
    return flattenTree(deptTree)
      .filter((node) => departmentIds.includes(node.value))
      .map((node) => ({ label: node.title, value: node.value }));
  }, [departmentIds, deptTree]);

  const primaryPositionOptions = useMemo(() => {
    if (!Array.isArray(positionIds) || positionIds.length === 0) {
      return [];
    }
    return positionOptions.filter((opt) => positionIds.includes(opt.value));
  }, [positionIds, positionOptions]);

  useEffect(() => {
    if (!departmentIds || departmentIds.length === 0) {
      form.setFieldsValue({ primaryDepartmentId: undefined });
    }
  }, [departmentIds, form]);

  useEffect(() => {
    if (!positionIds || positionIds.length === 0) {
      form.setFieldsValue({ primaryPositionId: undefined });
    }
  }, [positionIds, form]);

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={{ status: 1, ...initialValues }}
      onFinish={onFinish}
      className="user-form"
    >
      {!tenantId && (
        <div className="user-form-tip">请先选择租户后再创建或编辑用户。</div>
      )}

      {loading && (
        <div className="user-form-loading">
          <Spin size="small" /> 正在加载组织与岗位数据...
        </div>
      )}

      <FormField
        name="username"
        label="用户名"
        rules={[
          { required: true, message: '请输入用户名' },
          { min: 3, message: '用户名至少3个字符' },
          { max: 20, message: '用户名最多20个字符' },
        ]}
        required
      >
        <Input placeholder="请输入用户名" disabled={isEdit} />
      </FormField>

      <FormField
        name="password"
        label="初始密码"
        rules={[
          { required: !isEdit, message: '请输入密码' },
          { min: 6, message: '密码至少6个字符' },
        ]}
        required={!isEdit}
      >
        <Input.Password placeholder={isEdit ? '留空则不修改密码' : '请输入密码'} />
      </FormField>

      <FormField
        name="nickname"
        label="昵称"
        rules={[
          { max: 50, message: '昵称最多50个字符' },
        ]}
      >
        <Input placeholder="请输入昵称" />
      </FormField>

      <FormField
        name="email"
        label="邮箱"
        rules={[
          { type: 'email', message: '请输入有效的邮箱地址' },
        ]}
      >
        <Input placeholder="请输入邮箱" />
      </FormField>

      <FormField
        name="departmentIds"
        label="所属部门"
      >
        <TreeSelect
          treeData={deptTree}
          treeCheckable
          multiple
          showCheckedStrategy={TreeSelect.SHOW_CHILD}
          placeholder={tenantId ? '请选择部门' : '请先选择租户'}
          disabled={!tenantId}
          virtual={false}
        />
      </FormField>

      <FormField
        name="primaryDepartmentId"
        label="主部门"
      >
        <Select
          placeholder="请选择主部门"
          options={primaryDeptOptions}
          disabled={!tenantId || primaryDeptOptions.length === 0}
          allowClear
        />
      </FormField>

      <FormField
        name="positionIds"
        label="岗位"
      >
        <Select
          mode="multiple"
          placeholder={tenantId ? '请选择岗位' : '请先选择租户'}
          options={positionOptions}
          disabled={!tenantId}
          allowClear
        />
      </FormField>

      <FormField
        name="primaryPositionId"
        label="主岗位"
      >
        <Select
          placeholder="请选择主岗位"
          options={primaryPositionOptions}
          disabled={!tenantId || primaryPositionOptions.length === 0}
          allowClear
        />
      </FormField>

      {/* 状态字段只在新增时显示，编辑时通过独立的状态按钮修改 */}
      {!isEdit && (
        <FormField
          name="status"
          label="状态"
        >
          <Select>
            <Select.Option value={1}>启用</Select.Option>
            <Select.Option value={0}>禁用</Select.Option>
          </Select>
        </FormField>
      )}
    </Form>
  );
}

function transformDeptNode(node) {
  return {
    title: node.name,
    value: node.id,
    children: (node.children || []).map(transformDeptNode),
  };
}

function flattenTree(tree) {
  const result = [];
  tree.forEach((node) => {
    result.push({ title: node.title, value: node.value });
    if (node.children && node.children.length > 0) {
      result.push(...flattenTree(node.children));
    }
  });
  return result;
}
