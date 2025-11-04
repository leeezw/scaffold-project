package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志 Mapper
 * 
 * @author yourname
 */
@Mapper
public interface OperationLogMapper {
    
    /**
     * 插入操作日志
     */
    int insert(OperationLog operationLog);
    
    /**
     * 根据ID查询
     */
    OperationLog selectById(Long id);
    
    /**
     * 分页查询
     */
    List<OperationLog> selectPage(@Param("userId") Long userId,
                                  @Param("module") String module,
                                  @Param("operationType") String operationType,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);
    
    /**
     * 查询总数
     */
    long count(@Param("userId") Long userId,
               @Param("module") String module,
               @Param("operationType") String operationType,
               @Param("startTime") LocalDateTime startTime,
               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 删除指定时间之前的日志
     */
    int deleteBeforeTime(LocalDateTime time);
}

