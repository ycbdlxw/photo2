package com.ycbd.photoservice.Mappers;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemMapper {
    //获取登录用户信息
    Map<String, Object> getUserAccount(@Param("username") String username, @Param("password") String password);
    
}
