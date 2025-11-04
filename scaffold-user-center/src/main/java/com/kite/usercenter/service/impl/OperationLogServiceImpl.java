package com.kite.usercenter.service.impl;

import com.kite.usercenter.entity.OperationLog;
import com.kite.usercenter.entity.OperationLogEntity;
import com.kite.usercenter.mapper.OperationLogMapper;
import com.kite.usercenter.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现
 * 
 * @author yourname
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Override
    @Async("operationLogExecutor")
    public void saveAsync(OperationLogEntity logEntity) {
        save(logEntity);
    }
    
    @Override
    public void save(OperationLogEntity logEntity) {
        OperationLog operationLog = new OperationLog();
        BeanUtils.copyProperties(logEntity, operationLog);
        operationLogMapper.insert(operationLog);
    }
    
    @Override
    public OperationLog getById(Long id) {
        return operationLogMapper.selectById(id);
    }
    
    @Override
    public List<OperationLog> getPage(Long userId, String module, String operationType,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return operationLogMapper.selectPage(userId, module, operationType, startTime, endTime, offset, pageSize);
    }
    
    @Override
    public long getCount(Long userId, String module, String operationType,
                         LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogMapper.count(userId, module, operationType, startTime, endTime);
    }
    
    @Override
    public int deleteBeforeTime(LocalDateTime time) {
        return operationLogMapper.deleteBeforeTime(time);
    }
}

