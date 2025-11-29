import { Form, Input, Select } from 'antd';
import FormField from './FormField.jsx';
import './UserForm.css';

const { Option } = Select;

/**
 * 角色表单组件
 * 用于新增和编辑角色
 */
export default function RoleForm({ form, initialValues, onFinish }) {
  const isEdit = !!initialValues;

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={{ status: 1, ...initialValues }}
      onFinish={onFinish}
      className="user-form"
    >
      <FormField
        name="code"
        label="角色编码"
        rules={[
          { required: true, message: '请输入角色编码' },
          { min: 2, message: '角色编码至少2个字符' },
          { max: 50, message: '角色编码最多50个字符' },
          { pattern: /^[A-Za-z0-9_]+$/, message: '角色编码只能包含字母、数字和下划线' },
        ]}
        required
      >
        <Input placeholder="请输入角色编码" disabled={isEdit} />
      </FormField>

      <FormField
        name="name"
        label="角色名称"
        rules={[
          { required: true, message: '请输入角色名称' },
          { min: 2, message: '角色名称至少2个字符' },
          { max: 50, message: '角色名称最多50个字符' },
        ]}
        required
      >
        <Input placeholder="请输入角色名称" />
      </FormField>

      {/* 状态字段只在新增时显示，编辑时通过独立的状态按钮修改 */}
      {!isEdit && (
        <FormField
          name="status"
          label="状态"
        >
          <Select>
            <Option value={1}>启用</Option>
            <Option value={0}>禁用</Option>
          </Select>
        </FormField>
      )}
    </Form>
  );
}

