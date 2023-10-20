package com.ycbd.photoservice.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ycbd.photoservice.Mappers.PhotoMapper;
import com.ycbd.photoservice.Mappers.SystemMapper;



@Service
public class SystemService {
     @Resource
   protected PhotoMapper mapper;
   @Resource
   protected SystemMapper systemMapper;
   public Map<String, Object> login(String username,String password){
    
    return systemMapper.getUserAccount(username,password);
   }

     public List<Map<String, Object>> getMenuTree() {
        return mapper.getMenuTree();
    }
     public List<Map<String,Object>> getHeaderData(String table) {
        return mapper.getHeaderData(table);
    }

    public int getDataCount(String table, String params) {
        return mapper.getDataCount(table, params);
    }

    public List<Map<String, Object>> getSearchData(String table) {
        return null;
    }

    public int updateData(String table, String string, Map<String, Object> map, int id) {
        return 0;
    }

    public void delData(String table, String idString) {
        mapper.delData(table, idString);
    }

    public int addSave(String table, Map<String, Object> map) {
        return mapper.add(table, map);
    }

    public List<Map<String, Object>> getEditData(String table) {
        return null;
    }

    public List<Map<String, Object>> getItemsData(String table,int pageIndex,int pageSize,String whereStr,String sortByAndType,String groupbyString) {
            return  mapper.getItemsData(table, pageIndex, pageSize,whereStr, sortByAndType, groupbyString);
       
    }

    public List<Map<String, Object>> getDatas(String table, int pageIndex, int pageSize, String sortByString,
            String groupByString, Map<String, Object> pMap) {
        return null;
    }

    public List<Map<String, Object>> constructTree(List<Map<String, Object>> items) {
        return null;
    }

    public List<Map<String, Object>> getData(String table, String whereString) {
        
        return mapper.getData(table, whereString);
    }

     public List<Map<String, Object>> getTreeData(String table) {

        return mapper.getTreeData(table);
    }

    
}
