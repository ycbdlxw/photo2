package com.ycbd.photoservice.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Slf4j
@Service
public class SQLUtil implements Serializable {
	private static final long serialVersionUID = 7614285289075969240L;


	/**
	 * @param Columns         当前插入表的字段集合
	 * @param ListColumn   插入的记录map集合
	 * @param tableName       插入的数据库表名
	 * @param primaryKeyName  插入的关键字段名称
	 * @return                SQLParam SQL语句
	 */
	public static SQLParam createInsertSQLParamByAtrribule(Map<String, Object> Columns,Map<String, Object> ListColumn, String tableName,
			String primaryKeyName) {
		//Map<String, Object> ListColumn=colService.
		SQLParam param = new SQLParam();
		try {
			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("INSERT INTO ").append(tableName).append("(");
			StringBuffer valueBuffer = new StringBuffer();
			valueBuffer.append("VALUES(");
			List<Object> values = new Vector<Object>();
			int index = 0;
			
			for (Map.Entry<String, Object> entry : ListColumn.entrySet()) {
				//if(Tools.getIsNull(entry.getValue()))continue;//空字符或是null则跳过
				if (index > 0) {
					headBuffer.append(",");
					valueBuffer.append(",");
				}
				index++;
				headBuffer.append(entry.getKey());
				valueBuffer.append("?");
				values.add(entry.getValue());
				//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			}
			headBuffer.append(")").append(valueBuffer).append(")");
			param.setValue(values.toArray());
			param.setSql(headBuffer.toString());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return param;
	}
	/**
	 * 创建插入SQL
	 */
	public static SQLParam createInsertSQLParamByMap(Map<String, Object> ListColumn, String tableName) {
		SQLParam param = new SQLParam();
		try {
			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("INSERT INTO ").append(tableName).append("(");
			StringBuffer valueBuffer = new StringBuffer();
			valueBuffer.append("VALUES(");
			List<Object> values = new Vector<Object>();
			int index = 0;
			for (Map.Entry<String, Object> entry : ListColumn.entrySet()) {
				//if(Tools.getIsNull(entry.getValue()))continue;//空字符或是null则跳过
				if (index > 0) {
					headBuffer.append(",");
					valueBuffer.append(",");
				}
				index++;
				headBuffer.append(entry.getKey());
				valueBuffer.append("?");
				values.add(entry.getValue());
				//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			}
			headBuffer.append(")").append(valueBuffer).append(")");
			param.setValue(values.toArray());
			param.setSql(headBuffer.toString());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return param;
	}
	/**
	 * 创建更新SQL 根据传入的map 数据创建语句
	 */
	public static SQLParam createUpdateSQLParamByMap(Map<String, Object> ListColumn, String tableName,
			String primaryKeyName) {

		SQLParam param = new SQLParam();
		try {

			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("UPDATE ").append(tableName).append(" SET ");
			List<Object> values = new Vector<Object>();
			Object idValue = null;
			int index = 0;

			for (Map.Entry<String, Object> entry : ListColumn.entrySet()) {
				if (primaryKeyName.equals(entry.getKey())) {
					idValue = entry.getValue();
					continue;
				}
				if (index > 0)
					headBuffer.append(",");
				index++;
				headBuffer.append(entry.getKey()).append("=?");
				values.add(entry.getValue());
				//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			}
			values.add(idValue);
			headBuffer.append(" WHERE " + primaryKeyName + "=?");
			param.setSql(headBuffer.toString());
			param.setValue(values.toArray());

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return param;
	}
	
	/**
	 * 通过where条件创建更新SQL 根据传入的map 数据创建语句
	 */
	public static SQLParam createUpdateSQLParamByWhere(Map<String, Object> ListColumn, String tableName,
			String whereStr) {

		SQLParam param = new SQLParam();
		try {

			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("UPDATE ").append(tableName).append(" SET ");
			List<Object> values = new Vector<Object>();
			int index = 0;
			for (Map.Entry<String, Object> entry : ListColumn.entrySet()) {
				
				if (index > 0)
					headBuffer.append(",");
				
				headBuffer.append(entry.getKey()).append("=?");
				values.add(entry.getValue());
				index++;
				//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			}
			//values.add(idValue);
			headBuffer.append(" WHERE " + whereStr);
			param.setSql(headBuffer.toString());
			param.setValue(values.toArray());

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return param;
	}

	
	/**
	 * 创建多个条件删除SQL 根据传入的map ，如果传的值中有,号，则利用FIND_IN_SET功能
	 */
	public static SQLParam createBatchDeleteSQLParamByMap(Map<String, Object> ListColumn, String tableName) {
		SQLParam param = new SQLParam();
		try {
			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("delete from ").append(tableName).append(" where ");
			List<Object> values = new Vector<Object>();
			int index = 0;
			for (Map.Entry<String, Object> entry : ListColumn.entrySet()) {
				if (index > 0)
					headBuffer.append(" and ");
				index++;
				String value=entry.getValue().toString();
				if(value.contains(","))
				{
					headBuffer.append("  FIND_IN_SET(" + entry.getKey() + ",?)");
					
				}
					
				else
				   headBuffer.append(entry.getKey()).append("=?");
				values.add(entry.getValue());
			}
			param.setSql(headBuffer.toString());
			param.setValue(values.toArray());

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return param;
	}

	
	/**
	 * 创建批量更新SQL 根据传入的map 利用FIND_IN_SET功能，将多个需要更新记录一起更新处理
	 */
	public static SQLParam createBatchUpdateSQLParamByMap(Map<String, Object> ListColumn, String tableName,
			String primaryKeyName) {

		SQLParam param = new SQLParam();
		try {

			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("UPDATE ").append(tableName).append(" SET ");
			List<Object> values = new Vector<Object>();
			Object idValue = null;
			int index = 0;

			for (Map.Entry<String, Object> entry : ListColumn.entrySet()) {
				if (primaryKeyName.equals(entry.getKey())) {
					idValue = entry.getValue();
					continue;
				}
				if (index > 0)
					headBuffer.append(",");
				index++;
				headBuffer.append(entry.getKey()).append("=?");
				values.add(entry.getValue());

			}
			values.add(idValue);
			headBuffer.append(" WHERE FIND_IN_SET(" + primaryKeyName + ",?)");
			param.setSql(headBuffer.toString());
			param.setValue(values.toArray());

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return param;
	}

	/**
	 * 创建更新SQL 根据传入的map 数据创建语句
	 */
	public static SQLParam createUpdateSQLParamByMap(Map<String, String> ListColumn, String tableName,
			Object... params) {

		SQLParam param = new SQLParam();
		try {

			StringBuffer headBuffer = new StringBuffer();
			headBuffer.append("UPDATE ").append(tableName).append(" SET ");
			List<Object> values = new Vector<Object>();
			Object idValue = null;
			int index = 0;

			for (Map.Entry<String, String> entry : ListColumn.entrySet()) {
				for (int x = 0; x < params.length; x++) {
					if (params[x].equals(entry.getKey())) {
						idValue = entry.getValue();
						continue;
					}

				}

				if (index > 0)
					headBuffer.append(",");
				index++;
				headBuffer.append(entry.getKey()).append("=?");
				values.add(entry.getValue());
				//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			}

			values.add(idValue);
			headBuffer.append(" WHERE ");
			for (int x = 0; x < params.length; x++) {
				headBuffer.append(params[x] + "=? and ");
			}
			// +primaryKeyName+"=?") ;
			param.setSql(headBuffer.toString());
			param.setValue(values.toArray());

		} catch (Exception e) {
			log.error(e.getMessage());

		}
		return param;
	}

	public static class SQLParam {
		private String sql;

		private Object[] value;

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public Object[] getValue() {
			return value;
		}

		public void setValue(Object[] value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return sql + "--" + Arrays.toString(value);
		}
	}
}
