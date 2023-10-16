package com.ycbd.photoservice.Mappers;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PhotoMapper {
     //获取菜单
    List<Map<String, Object>>  getMenuTree();
    //获取列表表头数据
    List<Map<String, Object>> getHeaderData(String table);
    //获取表数据
    List<Map<String, Object>> getItemsData(String table,int pageIndex,int pageSize,String whereStr,String sortByAndType,String groupbyString);
    //获取数据总数
    Integer getDataCount(String table,String whereStr);
    //获取数据总数
    List<Map<String, Object>> getData(String table,String whereStr);
    //通过列表属性获取数据
    List<Map<String, Object>> getAttributeData(String table,String attributeName);
    //获取表的主键值字符
   // String getIntKeys(String table);
    //String  getNullKeys(String table);
    //将指定字段返回为一组字符串数组
   // List<String> getArrayString(String table);
    //获取显示类型
   // String  getShowType();
    /*通过对象更新，只更新不为空的内容*/
    int updateByObject(String table,String updateContent,String whereString);
    //增加数据 Map<String, Object> 中需要有table，Columns，及与字段列对应值的Map<String, Object>数据
    Integer add(String table,Map<String, Object> pMap);
    //删除数据，可以删除多个指定id的数据
    int delData(String table,String idString);
   
    //获取指定表的树形数据
    List<Map<String, Object>> getTreeData(String table);
    //用户登录
    List<Map<String, Object>> getUserAccount(String name,String password);
    Map<String, Object> getGPSInfo(int id);
}
