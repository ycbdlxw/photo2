package com.ycbd.photoservice.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import cn.hutool.core.convert.impl.DateConverter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SqiteService {
    private final String dbFilePath;

    public SqiteService(@Value("${db.file.path:/Volumes/homes/photos.db}")String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }

    public void createDatabaseAndTable() {
        try (Connection connection = getConnection(dbFilePath)) {
            createTableIfNotExists(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int saveDataToTable(List<Map<String, Object>> dataList) {
        try (Connection connection = getConnection(dbFilePath)) {
         return  insertDataIntoTable(connection, dataList);  
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int saveDataToTable(Map<String, Object> dataMap) {
        try (Connection connection = getConnection(dbFilePath)) {
         return  insertDataIntoTable(connection, dataMap);  
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    Connection getConnection(String dbFilePath) throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        SQLiteDataSource dataSource = new SQLiteDataSource(config);
        dataSource.setUrl("jdbc:sqlite:" + dbFilePath);
        return dataSource.getConnection();
    }

    private void createTableIfNotExists(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS fileinfo (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                            "currentDir TEXT, " +
                            "filePath TEXT, " +
                            "GPSFlag INTEGER, " +
                            "shootingTime TEXT, " +
                            "currentDate TEXT, " +
                            "type TEXT, " +
                            "title TEXT, " +
                            "url TEXT, " +
                            "filename TEXT, " +
                            "thumbnails TEXT, " +
                            "selected INTEGER, " +
                            "user TEXT, " +
                            "model TEXT, " +
                            "desc TEXT, " +
                            "md5 TEXT" +
                            ")"
            );
            // 为字段添加索引
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fileinfo_filepath ON fileinfo (filePath)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fileinfo_currentdate ON fileinfo (currentDate)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fileinfo_shootingtime ON fileinfo (shootingTime)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fileinfo_currentdir ON fileinfo (currentDir)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fileinfo_type ON fileinfo (type)");
        statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_fileinfo_md5 ON fileinfo (md5)");

        }
    }
   private int insertDataIntoTable(Connection connection, Map<String, Object>  data) throws SQLException {
    int affectedRows = 0;  
    String filePath = (String) data.get("filePath");
    try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO fileinfo " +
                        "(currentDir, filePath, GPSFlag, shootingTime, currentDate, type, title, url, filename, thumbnails, selected,user,model, desc, md5) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)"
        )) {
            
            statement.setString(1, (String) data.get("currentDir"));
            statement.setString(2, filePath);
            if(data.get("GPSFlag") == null)
                statement.setNull(3, Types.BOOLEAN);
            else
             statement.setBoolean(3, (Boolean) data.get("GPSFlag"));
            statement.setString(4,  (String) data.get("shootingTime"));
            statement.setString(5, (String) data.get("currentDate"));
            statement.setString(6, (String) data.get("type"));
            statement.setString(7, (String) data.get("title"));
            statement.setString(8, (String) data.get("url"));
            statement.setString(9, (String) data.get("filename"));
            statement.setString(10, (String) data.get("thumbnails"));
            statement.setBoolean(11, (Boolean) data.get("selected"));
            statement.setString(12, (String) data.get("user"));
            statement.setString(13, (String) data.get("model"));
            statement.setString(14, (String) data.get("desc"));
            statement.setString(15, (String) data.get("md5"));
            affectedRows = statement.executeUpdate();
        }
       if(affectedRows>0)
        System.out.println("保存成功");
       else
        System.out.println(filePath+"保存失败");

        return affectedRows;

    }
    private int insertDataIntoTable(Connection connection, List<Map<String, Object> > dataList) throws SQLException {
       connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO fileinfo " +
                        "(currentDir, filePath, GPSFlag, shootingTime, currentDate, type, title, url, filename, thumbnails, selected,user,model, desc, md5) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)"
        )) {
            for (Map<String, Object> data : dataList) {
            statement.setString(1, (String) data.get("currentDir"));
            statement.setString(2, (String) data.get("filePath"));
            if(data.get("GPSFlag") == null)
                statement.setNull(3, Types.BOOLEAN);
            else
             statement.setBoolean(3, (Boolean) data.get("GPSFlag"));
            statement.setString(4,  (String) data.get("shootingTime"));
            statement.setString(5, (String) data.get("currentDate"));
            statement.setString(6, (String) data.get("type"));
            statement.setString(7, (String) data.get("title"));
            statement.setString(8, (String) data.get("url"));
            statement.setString(9, (String) data.get("filename"));
            statement.setBoolean(10, (Boolean) data.get("thumbnails"));
            statement.setBoolean(11, (Boolean) data.get("selected"));
            statement.setString(12, (String) data.get("user"));
            statement.setString(13, (String) data.get("model"));
            statement.setString(14, (String) data.get("desc"));
            statement.setString(15, (String) data.get("md5"));
           // statement.executeUpdate();
           statement.addBatch(); // 添加到批处理
            }
            statement.executeBatch(); // 执行批处理
        }
        connection.commit(); // 提交事务
        System.out.println("保存成功");
        return dataList.size();

    }
     public List<Map<String, Object>> queryByColumns(String queryStr,List<String> columns) {
        List<Map<String, Object>> dataList = new ArrayList<>();
         try (Connection connection = getConnection(dbFilePath)) {
             Statement statement = connection.createStatement();
            ResultSet row = statement.executeQuery(queryStr);
             while (row.next()) {
                Map<String, Object> data = new HashMap<>();
                for (String column : columns) {
                    data.put(column, row.getString(column));
                }
                dataList.add(data);
             }
         } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }
        public long queryLastTime() {
          long lastTime=0;
         try (Connection connection = getConnection(dbFilePath)) {
             Statement statement = connection.createStatement();
            ResultSet row = statement.executeQuery("select max(shootingTime) from fileinfo");
             if (row.next()) {
             lastTime= DateUtil.parse(row.getString(1).toString(), "yyyy-MM-dd HH:mm:ss").toTimestamp().getTime();

             }
          } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastTime;
    }

    public List<Map<String, Object>> query(String queryStr) {
        List<Map<String, Object>> dataList = new ArrayList<>();
         try (Connection connection = getConnection(dbFilePath)) {
             Statement statement = connection.createStatement();
            ResultSet row = statement.executeQuery(queryStr);
             while (row.next()) {
                Map<String, Object> data = new HashMap<>();
            data.put("currentDir", row.getString("currentDir"));
            data.put("filePath", row.getString("filePath"));
            data.put("GPSFlag", row.getBoolean("GPSFlag"));
            data.put("shootingTime", row.getString("shootingTime"));
            data.put("currentDate", row.getString("currentDate"));
            data.put("type", row.getString("type"));
            data.put("title", row.getString("title"));
            data.put("url", row.getString("url"));
            data.put("filename", row.getString("filename"));
            data.put("thumbnails", row.getString("thumbnails"));
            data.put("selected", row.getBoolean("selected"));
            data.put("desc", row.getString("desc"));
             data.put("user", row.getString("user"));
              data.put("model", row.getString("model"));
            data.put("md5", row.getString("md5"));
            dataList.add(data);
            }
           
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return dataList;
    }

    /**
     *   根据filePath内容查询是否存在数据库，如果存在，则从dataList中去除，以确保处理不存在数据库中的文件记录
     * @param queryString
     * @param dataList
     * @return
     */

    public List<String> queryDataNotInCurrentMonth(String queryString, List<String> dataList,String root)  {
        String sqlString="select filePath from fileinfo where "+queryString;
        try (Connection connection = getConnection(dbFilePath);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlString)) {
            while (resultSet.next()) {
                dataList.remove(root+resultSet.getString("filePath"));
            }
         } catch (SQLException e) {
           // fail("Exception occurred: " + e.getMessage());
        }
        return dataList;
      
    }

    public boolean isTableExists() {
    try (Connection connection = getConnection(dbFilePath)) {
       DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, "fileinfo", null);
        boolean exists = resultSet.next();
        resultSet.close();
        return exists;
    }
    catch (SQLException e) {
        e.printStackTrace();
    }
    return false;

    }

    public List<Map<String, Object>> getDataByPage(String queryStr, List<String> columns, int pageSize,
            int pageNumber) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try (Connection connection = getConnection(dbFilePath)) {
            Statement statement = connection.createStatement();
            String queryWithPagination = queryStr + " LIMIT " + pageSize + " OFFSET " + (pageNumber - 1) * pageSize;
            ResultSet row = statement.executeQuery(queryWithPagination);
            while (row.next()) {
                Map<String, Object> data = new HashMap<>();
                columns.
                        stream().
                        forEach(column -> {
                            try {
                                data.put(column, row.getObject(column));
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        });
                
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public int getTotal(String queryDbStr) {
        int count=0;
        try (Connection connection = getConnection(dbFilePath)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(queryDbStr);
        if (resultSet.next()) {
            count = resultSet.getInt(1);
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int updateContent(String updateString) {
         int count=0;
        try (Connection connection = getConnection(dbFilePath)) {
            Statement statement = connection.createStatement();
           count = statement.executeUpdate(updateString);
       
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}