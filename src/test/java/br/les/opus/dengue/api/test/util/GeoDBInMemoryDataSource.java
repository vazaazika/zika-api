package br.les.opus.dengue.api.test.util;

import geodb.GeoDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class GeoDBInMemoryDataSource extends SingleConnectionDataSource {
	
	private List<Resource> sqlInitResources;
	
	private void runSqlFile(Resource resource) throws IOException, SQLException {
		File file = resource.getFile();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		Connection connection = getConnection();
		while (line != null) {
			Statement stm = connection.createStatement();
			stm.execute(line);
			line = reader.readLine();
		}
		reader.close();
	}
	
	public void runInitSqlResources() throws IOException, SQLException {
		if (sqlInitResources != null && !sqlInitResources.isEmpty()) {
			for (Resource resource : sqlInitResources) {
				runSqlFile(resource);
			}
		}
	}
 
    public GeoDBInMemoryDataSource() throws SQLException {
        setDriverClassName("org.h2.Driver");
        org.h2.tools.Server.createTcpServer().start();
        setUrl("jdbc:h2:tcp://localhost/mem:test;DB_CLOSE_ON_EXIT=FALSE");
        setSuppressClose(true);
        setAutoCommit(true);
    }
    
    public void destroyTcpServer() throws SQLException {
    	org.h2.tools.Server.createTcpServer().stop();
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();
        GeoDB.InitGeoDB(conn);
        return conn;
    }
    
    public List<Resource> getSqlInitResources() {
		return sqlInitResources;
	}

	public void setSqlInitResources(List<Resource> sqlInitResources) {
		this.sqlInitResources = sqlInitResources;
	}
}
