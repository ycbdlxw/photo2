package com.ycbd.photoservice.services;


import cn.hutool.core.date.DateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.ycbd.photoservice.tools.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


/**
 * 
 * @auther lxw
 * @since 2020年10月11日 下午5:29:36
 */
@Service
public class LogDaoService {

	@Autowired
	protected JdbcTemplate jdbcTemplate;
	
	/**
	 * 跨数据源保存对象
	 * 
	 * @param saveData     需要保存的字段列表Map
	 * @param tableName      需要保存的表名
	 * @param dbsrc          TODO
	 * @return 返回受影响行数
	 */
	public Integer save(Map<String, Object> saveData, String tableName, String dbsrc) {
		SQLUtil.SQLParam param = SQLUtil.createInsertSQLParamByMap(saveData, getTablename(tableName));
		int result = save(tableName, param, dbsrc);
		return result;
	}
	private  String getTablename(String tableName){
		String result="";
		String dataStr= DateUtil.today();
		//生成的数据表名
		dataStr=dataStr.split("-")[0]+dataStr.split("-")[1];
		String newtableName=tableName+"_"+dataStr;
		jdbcTemplate.execute("create TABLE IF NOT EXISTS "+newtableName+" LIKE  "+tableName);
		return newtableName;
	};
	
	/**
	 * 
	 * @param dbName
	 * @param param
	 * @param dbsrc  TODO
	 * @return 保存后的ID
	 */
	public Integer save(String dbName, SQLUtil.SQLParam param, String dbsrc) {
		String sql = param.getSql();
		Object[] args = param.getValue();
		KeyHolder keyHolder = new GeneratedKeyHolder();
	// JdbcTemplate jdbcTemplate = DataSourceFactory.getTemplate(dbsrc);
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				int index = 0;
				for (Object obj : args) {
					index++;
					if (obj==null)
						ps.setObject(index, null);
					else
						ps.setObject(index, obj+"");
				}

				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	

}
