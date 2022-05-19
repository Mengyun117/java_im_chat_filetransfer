/*****************************************************************************************************
 * �������Ȩ��Mengyun199���У�All Rights Reserved (C) 2022-
 *****************************************************************************************************
 * ������hanasaki-workstation
 * ��¼�û���Mengyun Jia
 * �������ƣ�hanasaki-workstation
 * ��ϵ�����䣺jiamengyun1024@outlook.com
 *****************************************************************************************************
 * ������ݣ�2022
 * �����ˣ�Mengyun Jia
 *****************************************************************************************************/

package com.microdream.java_im_chat_filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class UserDatabase {
	// ## DEFINE VARIABLES SECTION ##
	// define the driver to use JDBC
	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	// the database name
	String dbName = "USERDB";
	// define the Derby connection URL to use  ���ݿ������ַ���
	String connectionURL = "jdbc:derby:" + dbName + ";create=true";
	Connection conn;  //  �������ݿ����Ӷ���

	public UserDatabase() {
		// ## LOAD DRIVER SECTION ##
		try {
			/*
			 * * Load the Derby driver.* When the embedded Driver is used this
			 * action start the Derby engine.* Catch an error and suggest a
			 * CLASSPATH problem
			 */
			Class.forName(driver);
			System.out.println(driver + " loaded. ");
		} catch (ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		//  SQL���
		String createString = "create table USERTABLE " // ����
				+ "(USERNAME varchar(20) primary key not null, " // �û���
				+ "HASHEDPWD char(20) for bit data, " // �����HASHֵ		
				+ "REGISTERTIME timestamp default CURRENT_TIMESTAMP, "// ע��ʱ��
				+ "SALT char(20) for bit data, " //  ����
				+ "AGE varchar(10))";//  ����
				
		
		try {
			DriverManager.setLogWriter(new PrintWriter(new File("log.txt")));
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);
			// Create a statement to issue simple commands.
			Statement s = conn.createStatement();
			// Call utility method to check if table exists.
			// Create the table if needed
			if (!checkTable(conn)) {
				System.out.println(" . . . . creating table USERTABLE");
				s.execute(createString);
			}
			s.close();// ����Ҫ��
			System.out.println("Database openned normally");
		} catch (SQLException e) {
			errorPrint(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Insert a new user into the USERTABLE table
	public boolean insertUser(String userName, String userPwd, String age) {//  �������û�
		try {
			if (!userName.isEmpty() && !userPwd.isEmpty()) {//  ��ڲ������
				PreparedStatement psTest = conn.prepareStatement(
						"select * from USERTABLE where USERNAME=?",
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				psTest.setString(1, userName);//  ���ò���������Ҫ��SQL����еĵ�һ���ʺŸ�ֵ
				ResultSet rs = psTest.executeQuery();//  ���Կ����,ִ��preparedstatement
				rs.last();//  �ѱ���α��Ƶ����һ��
				int n = rs.getRow();//  �����α��к�
				psTest.close();
				if (n == 0) {//  �ձ�
					PreparedStatement psInsert = conn
							.prepareStatement("insert into USERTABLE values (?,?,?,?,?)");
					//  �����������ֵ
					byte[] salt = new byte[160/8];
					new SecureRandom().nextBytes(salt);;
					//  ����ֵ��userPwd�ϲ�
					//userPwd = userPwd + salt;
					
					MessageDigest digest = MessageDigest.getInstance("SHA-1");
					digest.update(userPwd.getBytes());
					digest.update(salt);
					
					byte[] hashedPwd = digest.digest();
					psInsert.setString(1, userName);//  ��   ��һ���������û���
					psInsert.setBytes(2, hashedPwd);//  ����2����prepareStatement�ڶ�������
					psInsert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					psInsert.setBytes(4, salt);// 
					psInsert.setString(5, age);// 
					psInsert.executeUpdate();//  insert���Ҫ��
					psInsert.close();
					System.out.println("�ɹ�ע�����û�" + userName);
					return true;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			errorPrint(e);
		}
		System.out.println("�û�" + userName + "�Ѿ�����");
		return false;
	}

	public boolean deleteUser(String userName, String userPwd) {
		if (checkUserPassword(userName, userPwd) == true) {
			try {
				PreparedStatement psDelete = conn
						.prepareStatement("delete from USERTABLE where USERNAME=?");//  ɾ��ע��
				psDelete.setString(1, userName);
				int n = psDelete.executeUpdate();
				psDelete.close();
				if (n > 0) {
					System.out.println("�ɹ�ɾ���û�" + userName);
					return true;
				} else {
					System.out.println("ɾ���û�" + userName + "ʧ��");
					return false;
				}
			} catch (SQLException e) {
				errorPrint(e);
			}
		}
		return false;
	}

	// check if userName with password userPwd can logon ����û���¼
	public boolean checkUserPassword(String userName, String userPwd) {
		try {
			if (!userName.isEmpty() && !userPwd.isEmpty()) {
				//  ȡ�Σ������ݿ���ȡ�Σ��θ�����ϲ������ϣ�����ö���sql���ȡ�Σ�
				PreparedStatement pStatement = conn.prepareStatement("select SALT from USERTABLE where USERNAME=?");		
				pStatement.setString(1, userName);//  ��Ԥ����ĵ�һ���ʺŸ�ֵ
				ResultSet resultSet = pStatement.executeQuery();
				resultSet.next();
				byte[] salt = new byte[20];
				salt = resultSet.getBytes("SALT");

				MessageDigest digest = MessageDigest.getInstance("SHA-1");
				digest.update(userPwd.getBytes());
				digest.update(salt);
				byte[] hashedPwd = digest.digest();
				PreparedStatement psTest = conn.prepareStatement(
						"select * from USERTABLE where USERNAME=? and HASHEDPWD=?",
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				psTest.setString(1, userName);
				psTest.setBytes(2, hashedPwd);	
				psTest.executeQuery();
				ResultSet rs = psTest.executeQuery();
				rs.last();
				int n = rs.getRow();
				psTest.close();
				return n > 0 ? true : false;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			errorPrint(e);
		}
		return false;
	}

	// show the information of all users in table USERTABLE, should be called
	// before the program exited
	public void showAllUsers() {
		String printLine = "  ______________��ǰ����ע���û�______________";
		try {
			Statement s = conn.createStatement();
			// Select all records in the USERTABLE table
			ResultSet users = s
					.executeQuery("select USERNAME, HASHEDPWD, REGISTERTIME from USERTABLE order by REGISTERTIME");

			// Loop through the ResultSet and print the data
			System.out.println(printLine);
			while (users.next()) {
				System.out.println("User-Name: " + users.getString("USERNAME") //�û���
						+ " Hashed-Pasword: " + Base64.getEncoder().encodeToString(users.getBytes("HASHEDPWD")) //����HASHֵ��BASE64����
						+ " Regiester-Time " + users.getTimestamp("REGISTERTIME"));//ע��ʱ��
			}
			System.out.println(printLine);
			// Close the resultSet
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// �ر����ݿ�
	public void shutdownDatabase() {
		/***
		 * In embedded mode, an application should shut down Derby. Shutdown
		 * throws the XJ015 exception to confirm success.
		 ***/
		if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
			boolean gotSQLExc = false;
			try {
				conn.close();
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database did not shut down normally");
			} else {
				System.out.println("Database shut down normally");
			}
		}
	}

	/*** Check for USER table ****/
	public boolean checkTable(Connection conTst) throws SQLException {
		try {
			Statement s = conTst.createStatement();
			s.execute("update USERTABLE set USERNAME= 'TEST', REGISTERTIME = CURRENT_TIMESTAMP where 1=3");
		} catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			// System.out.println("  Utils GOT:  " + theError);
			/** If table exists will get - WARNING 02000: No row was found **/
			if (theError.equals("42X05")) // Table does not exist
			{
				return false;
			} else if (theError.equals("42X14") || theError.equals("42821")) {
				System.out
						.println("checkTable: Incorrect table definition. Drop table USERTABLE and rerun this program");
				throw sqle;
			} else {
				System.out.println("checkTable: Unhandled SQLException");
				throw sqle;
			}
		}
		return true;
	}

	// Exception reporting methods with special handling of SQLExceptions
	static void errorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLExceptionPrint((SQLException) e);
		} else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	}

	// Iterates through a stack of SQLExceptions
	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}
	
}

