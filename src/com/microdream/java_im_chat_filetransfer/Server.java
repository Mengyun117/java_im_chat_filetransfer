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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class Server extends JFrame {

	private SSLServerSocket serverSocket;
	private Hashtable<String,String> user_ip = new Hashtable<String,String>();
	private  Hashtable<String,Integer> user_port = new Hashtable<String,Integer>();
	private final static int PORT = 9999;
	private UserDatabase userDatabase = new UserDatabase();
	// ���������û����û�����Socket��Ϣ
	private final UserManager userManager = new UserManager();
	// �������û��б�ListModel��,����ά���������û��б�����ʾ������
	final DefaultTableModel onlineUsersDtm = new DefaultTableModel();
	// ���ڿ���ʱ����Ϣ��ʾ��ʽ
	// private final SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private final JPanel contentPane;
	private final JTable tableOnlineUsers;
	private final JTextPane textPaneMsgRecord;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Server frame = new Server();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Server() {
		userDatabase = new UserDatabase();

		setTitle("\u670D\u52A1\u5668");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 561, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPaneNorth = new JSplitPane();
		splitPaneNorth.setResizeWeight(0.5);
		contentPane.add(splitPaneNorth, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setPreferredSize(new Dimension(100, 300));
		scrollPaneMsgRecord.setViewportBorder(
				new TitledBorder(null, "\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneNorth.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setEditable(false);
		textPaneMsgRecord.setPreferredSize(new Dimension(100, 100));
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setPreferredSize(new Dimension(100, 300));
		splitPaneNorth.setRightComponent(scrollPaneOnlineUsers);

		onlineUsersDtm.addColumn("�û���");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("�˿�");
		onlineUsersDtm.addColumn("��¼ʱ��");
		tableOnlineUsers = new JTable(onlineUsersDtm);
		tableOnlineUsers.setEnabled(false);
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // ��JTable������������
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton btnStart = new JButton("\u542F\u52A8");
		// "����"��ť
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// ����ServerSocket�򿪶˿�9999�����ͻ�������
					//serverSocket = new ServerSocket(PORT);
					serverSocket = createSSLServerSocket();
					// �ڡ���Ϣ��¼���ı������ú�ɫ��ʾ�������������ɹ�X��������ʱ����Ϣ
					String msgRecord = dateFormat.format(new Date()) + " �����������ɹ�" + "\r\n";
					addMsgRecord(msgRecord, Color.red, 12, false, false);
					// �����������������û������̡߳������ܲ�����ͻ�����������
					new Thread() {
						@Override
						public void run() {
							while (true) {
								try {
									// ����serverSocket.accept()���������û���������
									Socket socket = serverSocket.accept();
									// Ϊ�������û��������������û������̡߳�
									// ����serverSocket.accept()�������ص�socket���󽻸����û������̡߳�������
									UserHandler userHandler = new UserHandler(socket);
									new Thread(userHandler).start();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
					}.start();

					btnStart.setEnabled(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		panelSouth.add(btnStart);
	}

	// ����Ϣ��¼�ı��������һ����Ϣ��¼
	private void addMsgRecord(final String msgRecord, Color msgColor, int fontSize, boolean isItalic,
			boolean isUnderline) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Document docs = textPaneMsgRecord.getDocument();
				try {
					docs.insertString(docs.getLength(), msgRecord, attrset);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	class UserHandler implements Runnable {
		private final Socket currentUserSocket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		public UserHandler(Socket currentUserSocket) {
			this.currentUserSocket = currentUserSocket;
			try {
				ois = new ObjectInputStream(currentUserSocket.getInputStream());
				oos = new ObjectOutputStream(currentUserSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					Message msg = (Message) ois.readObject();
					if (msg instanceof UserLogonMessage) {
						// �����û�������Ϣ
						processUserLogonMessage((UserLogonMessage) msg);
					} else if (msg instanceof RegisterMessage) {
						// �����û�ע����Ϣ
						processRegisterMessage((RegisterMessage) msg);
					} else if (msg instanceof UserLogoffMessage) {
						// �����û�������Ϣ
						processUserLogoffMessage((UserLogoffMessage) msg);
					} else if (msg instanceof PublicChatMessage) {
						// ��������Ϣ
						processPublicChatMessage((PublicChatMessage) msg);
					} else if (msg instanceof PrivateChatMessage) {
						// ����˽����Ϣ
						processPrivateChatMessage((PrivateChatMessage) msg);
					} else if (msg instanceof FileMessage) {
						// �����ļ�����������Ϣ
						processFileMessage((FileMessage) msg);
					} else if (msg instanceof SendFileMessage) {
						// �����ļ�������Ϣ
						processSendFileMessage((SendFileMessage) msg);
					} else if (msg instanceof P2PChatMessage) {
						// �����ļ�����������Ϣ
						processP2PChatMessage((P2PChatMessage) msg);
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("�ͻ��˿����Ѿ�����");
					userManager.removeUser(getName());
				} else {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (currentUserSocket != null) {
					try {
						currentUserSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// ע��
		private void processRegisterMessage(RegisterMessage msg) {
			// TODO Auto-generated method stub
			String srcUser = msg.getSrcUser();
			char[] tmppassWd = msg.getPasswd(); // ȡ����
			String passwdString = String.valueOf(tmppassWd);
			String ageString = msg.getAge();

			try {
				if (userDatabase.insertUser(srcUser, passwdString, ageString)) {// �������ɹ��������û�����ע��ɹ���Ϣ
					userDatabase.showAllUsers();
					ResultMessage resultMessage = new ResultMessage(null, ResultMessage.REGISTER_SUCCESS, "ע��ɹ�");
					synchronized (oos) {
						oos.writeObject(resultMessage);
						oos.flush();
					}
					String ip = currentUserSocket.getInetAddress().getHostAddress();
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")"
							+ "ע��ɹ�!\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
				} else {
					ResultMessage resultMessage = new ResultMessage(null, ResultMessage.REGISTER_FAILURE, "ע��ʧ��");
					synchronized (oos) {
						oos.writeObject(resultMessage);
						oos.flush();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// ����˽����Ϣ
		private void processPrivateChatMessage(PrivateChatMessage msg) {
			// TODO Auto-generated method stub
			String srcUser = msg.getSrcUser();
			String msgContent = msg.getMsgContent();
			String dstUser = msg.getDstUser();

			if (userManager.hasUser(srcUser) && userManager.hasUser(dstUser) && msgContent.length() > 0) {
				// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "��: " + dstUser + "˵"
						+ msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
				// ��˽����Ϣ�ɷ�����ת�����ض��û�
				ObjectOutputStream dstOos = userManager.getUserOos(dstUser);
				try {
					synchronized (dstOos) {
						dstOos.writeObject(msg);
						dstOos.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// ����P2P��Ϣ
		private void processP2PChatMessage(P2PChatMessage msg) {
			String dstUser = msg.getDstUser();
			String srcUser = msg.getSrcUser();
			String ip = user_ip.get(dstUser);
			int p2pport = user_port.get(dstUser);
			String msgContent = "";

			//�������ͷ������˿ں�ip��ַ;
			P2PChatMessage srcMsg = new P2PChatMessage(ip, p2pport, dstUser, srcUser);
			try {
				ObjectOutputStream o = userManager.getUserOos(srcUser);
				synchronized (o) {
					o.writeObject(srcMsg);
					o.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "��" + dstUser + "����P2P��Ϣ\r\n";
			addMsgRecord(msgRecord, Color.black, 12, false, false);
		}

		// �������û�ת����Ϣ
		private void transferMsgToOtherUsers(Message msg) {
			String[] users = userManager.getAllUsers();
			for (String user : users) {
				if (userManager.getUserSocket(user) != currentUserSocket) {
					try {
						ObjectOutputStream o = userManager.getUserOos(user);
						synchronized (o) {
							o.writeObject(msg);
							o.flush();
						}
					} catch (IOException e) {
						if (e.toString().endsWith("Connection reset")) {
							System.out.println("�ͻ���" + user + "�����Ѿ�����");
							userManager.removeUser(getName());
						} else {
							e.printStackTrace();
						}
					}
				}
			}
		}

		// �����û�������Ϣ
		private void processUserLogonMessage(UserLogonMessage msg) {
			String srcUser = msg.getSrcUser();

			try {
				if (userManager.hasUser(srcUser)) {
					// ���������ζ���û��ظ���¼����ͻ��˷��͵�¼ʧ����Ϣ
					System.err.println("�û��ظ���¼");
					ResultMessage resultMessage = new ResultMessage(null, ResultMessage.LOGON_FAILURE, "�û��ظ���¼");
					synchronized (oos) {
						oos.writeObject(resultMessage);
						oos.flush();
					}
					return;
				}

				// ����û����Ϳ����Ƿ���ȷ���������ȷ����ͻ��˷��͵�¼ʧ����Ϣ
				String password = msg.getPassword();
				boolean checkResult = userDatabase.checkUserPassword(srcUser, password);
				if (checkResult) {
					ResultMessage resultMessage = new ResultMessage(null, ResultMessage.LOGON_SUCCESS, "�û���¼�ɹ� ");
					synchronized (oos) {
						oos.writeObject(resultMessage);
						oos.flush();
					}
					// ���µ�¼���û�ת����ǰ�����û��б�
					String[] users = userManager.getAllUsers();
					OnlineUsersMessage onlineUsersMessage = new OnlineUsersMessage(srcUser);
					for (String user : users) {
						onlineUsersMessage.addUser(user);
					}
					synchronized (oos) {
						oos.writeObject(onlineUsersMessage);
						oos.flush();
					}
					// ���������������û�ת���û���¼��Ϣ
					transferMsgToOtherUsers(msg);
					PortInfo newmsg = new PortInfo(currentUserSocket.getPort(),"&&server&&",srcUser);
					try {
						synchronized (oos) {
							oos.writeObject(newmsg);
							oos.flush();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// ���û���Ϣ���뵽�������û����б���
					onlineUsersDtm.addRow(new Object[] { srcUser, currentUserSocket.getInetAddress().getHostAddress(),
							currentUserSocket.getPort(), dateFormat.format(new Date()) });
					userManager.addUser(srcUser, currentUserSocket, oos, ois);
					// ����ɫ���ֽ��û������û���¼ʱ����ӵ�����Ϣ��¼���ı�����
					String ip = currentUserSocket.getInetAddress().getHostAddress();
					user_ip.put(srcUser, ip);
					user_port.put(srcUser,currentUserSocket.getPort());
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")"
							+ "��¼��!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					return;
				} else {
					ResultMessage resultMessage = new ResultMessage(null, ResultMessage.LOGON_FAILURE, "�û���¼ʧ�� ");
					synchronized (oos) {
						oos.writeObject(resultMessage);
						oos.flush();
					}
					return;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// �����û��˳���Ϣ
		private void processUserLogoffMessage(UserLogoffMessage msg) {
			String srcUser = msg.getSrcUser();
			// ����ɫ���ֽ��û������û��˳�ʱ����ӵ�����Ϣ��¼���ı�����
			String ip = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
			final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "�˳���!\r\n";
			addMsgRecord(msgRecord, Color.green, 12, false, false);
			// �ڡ������û��б���ɾ���˳��û�
			userManager.removeUser(srcUser);
			user_ip.remove(srcUser);
			user_port.remove(srcUser);
			for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
				if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
					onlineUsersDtm.removeRow(i);
					break;
				}
			}
			// ���û��˳���Ϣת�����������������û�
			transferMsgToOtherUsers(msg);
			// ��ͻ��˷����˳��ɹ���Ϣ
			ResultMessage resultMessage = new ResultMessage(null, ResultMessage.LOGOFF_SUCCESS, "�û��˳��ɹ�");
			try {
				synchronized (oos) {
					oos.writeObject(resultMessage);
					oos.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// �����û������Ĺ�����Ϣ
		private void processPublicChatMessage(PublicChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String msgContent = msg.getMsgContent();
			if (userManager.hasUser(srcUser) && msgContent.length() > 0) {
				// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "����˵: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
				// ��������Ϣת�����������������û�
				transferMsgToOtherUsers(msg);
			}
		}
	}

	public SSLServerSocket createSSLServerSocket() throws Exception {
		String keyStoreFile = "mykeys.keystore";
		String passphrase = "123456";
		KeyStore ks = KeyStore.getInstance("PKCS12");
		char[] password = passphrase.toCharArray();
		ks.load(new FileInputStream(keyStoreFile), password);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, password);// ������Ҫ����
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(kmf.getKeyManagers(), null, null);// ����һ��Ĭ�ϵ�TrustManager����������֤��֤���Լ�����ݿɿ�

		SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
		serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
		return (SSLServerSocket) serverSocket;
	}

	// �����ļ�����������Ϣ
	private void processFileMessage(FileMessage msg) {
		// �����ļ����û�
		String dstUser = msg.getDstUser();
		try {
			ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
			synchronized (objectOutputStream) {
				objectOutputStream.writeObject(msg);
				objectOutputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processSendFileMessage(SendFileMessage msg) {
		String dstUser = msg.getDstUser();
		try {
			ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
			synchronized (objectOutputStream) {
				objectOutputStream.writeObject(msg);
				objectOutputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

// ���������û���Ϣ
class UserManager {
	private final Hashtable<String, User> onLineUsers;// �û�����user�����ӳ�䡤

	public UserManager() {
		onLineUsers = new Hashtable<String, User>();
	}

	// �ж�ĳ�û��Ƿ�����
	public boolean hasUser(String userName) {
		return onLineUsers.containsKey(userName);
	}

	// �ж������û��б��Ƿ��
	public boolean isEmpty() {
		return onLineUsers.isEmpty();
	}

	// ��ȡ�����û���Socket�ĵ��������װ�ɵĶ��������
	public ObjectOutputStream getUserOos(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOos();
		}
		return null;
	}

	// ��ȡ�����û���Socket�ĵ���������װ�ɵĶ���������
	public ObjectInputStream getUserOis(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOis();
		}
		return null;
	}

	// ��ȡ�����û���Socket
	public Socket getUserSocket(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getSocket();
		}
		return null;
	}

	// ��������û�
	public boolean addUser(String userName, Socket userSocket, ObjectOutputStream oos, ObjectInputStream ios) {
		if ((userName != null) && (userSocket != null) && (oos != null) && (ios != null)) {
			onLineUsers.put(userName, new User(userSocket, oos, ios));
			return true;
		}
		return false;
	}

	// ɾ�������û�
	public boolean removeUser(String userName) {
		if (hasUser(userName)) {
			onLineUsers.remove(userName);
			return true;
		}
		return false;
	}

	// ��ȡ���������û���
	public String[] getAllUsers() {
		String[] users = new String[onLineUsers.size()];
		int i = 0;
		for (String userName : onLineUsers.keySet()) {
			users[i++] = userName;
		}
		return users;
	}

	// ��ȡ�����û�����
	public int getOnlineUserCount() {
		return onLineUsers.size();
	}
}

class User {
	private final Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Date logonTime;

	public User(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois, Date logonTime) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		this.logonTime = logonTime;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public Date getLogonTime() {
		return logonTime;
	}

}
