package com.kite.usercenter.service;

import com.kite.usercenter.entity.OperationLog;
import com.kite.usercenter.entity.OperationLogEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 * 
 * @author yourname
 */
public interface OperationLogService {
    
    /**
     * 保存操作日志（异步）
     */
    void saveAsync(OperationLogEntity logEntity);
    
    /**
     * 保存操作日志（同步）
     */
    void save(OperationLogEntity logEntity);
    
    /**
     * 根据ID查询
     */
    OperationLog getById(Long id);
    
    /**
     * 分页查询
     */
    List<OperationLog> getPage(Long userId, String module, String operationType,
                               LocalDateTime startTime, LocalDateTime endTime,
                               Integer pageNum, Integer pageSize);
    
    /**
     * 查询总数
     */
    long getCount(Long userId, String module, String operationType,
                  LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 删除指定时间之前的日志
     */
    int deleteBeforeTime(LocalDateTime time);
}

