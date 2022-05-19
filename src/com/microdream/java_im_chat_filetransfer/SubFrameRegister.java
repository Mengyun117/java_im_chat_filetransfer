package com.microdream.java_im_chat_filetransfer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.net.UnknownHostException;
import java.security.KeyStore;

public class SubFrameRegister extends JFrame {

	private final int port = 9999;
	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String localUserName;
	private String password;

	private JPanel contentPane;
	private JTextField textFieldUserName;
	private JTextField textFieldAge;
	private JButton btnRegister;
	private JPasswordField passwordFieldPwd;
	private JTextPane textPaneMsgRecord;
	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */

	public SubFrameRegister() {
		setTitle("\u6CE8\u518C");
//		this.oos = oos;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textFieldUserName = new JTextField();
		textFieldUserName.setBounds(177, 51, 138, 21);
		contentPane.add(textFieldUserName);
		textFieldUserName.setColumns(10);

		textFieldAge = new JTextField();
		textFieldAge.setBounds(177, 178, 138, 21);
		contentPane.add(textFieldAge);
		textFieldAge.setColumns(10);

		JLabel lblNewLabel = new JLabel("\u7528\u6237\u540D\uFF1A");
		lblNewLabel.setBounds(82, 54, 54, 15);
		contentPane.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("\u53E3\u4EE4\uFF1A");
		lblNewLabel_1.setBounds(82, 114, 54, 15);
		contentPane.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("\u5E74\u9F84\uFF1A");
		lblNewLabel_2.setBounds(82, 181, 54, 15);
		contentPane.add(lblNewLabel_2);

		passwordFieldPwd = new JPasswordField();
		passwordFieldPwd.setBounds(177, 111, 138, 21);
		contentPane.add(passwordFieldPwd);

		JButton btnRegister = new JButton("\u6CE8\u518C");// ע�ᰴť
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ��ȡ�û�ע����Ϣ���û������������
				String userName = textFieldUserName.getText().trim();
				char[] password = passwordFieldPwd.getPassword();
				String passwd = String.valueOf(password); 
				String age = textFieldAge.getText().trim();

//				RegisterMessage registerMessage = new RegisterMessage(userName, passwdString, age);// �û�ע����Ϣ
				// ������Ϣ��֤��Ϊ��
				if (userName.length() > 0) {
					if (passwd.length() > 0) {
						if (age.length() > 0) {
							try {
								if (socket == null) {
									socket = createSSLSocket();
									oos = new ObjectOutputStream(socket.getOutputStream());//��ȡSocket�����,�����͸��������˵����ݡ�
									ois = new ObjectInputStream(socket.getInputStream());//��ȡSocket�����������ӷ������˷��ص����ݡ�
								}
							} catch (UnknownHostException e1) {
								JOptionPane.showMessageDialog(null, "�Ҳ�������������");
								e1.printStackTrace();
								System.exit(0);
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(null, "������I/O���󣬷�����δ������");
								e1.printStackTrace();
								System.exit(0);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							// ������������û�ע����Ϣ���û����Ϳ����רҵ
							RegisterMessage register = new RegisterMessage(userName, password, age);//�û�ע����Ϣ
							try {
									oos.writeObject(register);
									oos.flush();
									
									ResultMessage msg = (ResultMessage) ois.readObject();
									if(msg.getResult() == ResultMessage.REGISTER_SUCCESS) {
										JOptionPane.showMessageDialog(null, "ע��ɹ���");
									}else if (msg.getResult() == ResultMessage.REGISTER_FAILURE) {
										JOptionPane.showMessageDialog(null, "ע��ʧ�ܣ�");
									}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						} else {
							JOptionPane.showMessageDialog(null, "���������䣡");
						}
					} else {
						JOptionPane.showMessageDialog(null, "���������룡");
					}
				} else {
					JOptionPane.showMessageDialog(null, "�������û�����");
				}

			}
			
			// ����Ϣ��¼�ı��������һ����Ϣ��¼
			private void addMsgRecord(final String msgRecord, Color msgColor, int fontSize, boolean isItalic, boolean isUnderline) {
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
			
		});
		btnRegister.setBounds(93, 232, 93, 23);
		contentPane.add(btnRegister);

		JButton btnNewButtonClose = new JButton("\u5173\u95ED");
		btnNewButtonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SubFrameRegister.this.setVisible(false);
			}
		});
		btnNewButtonClose.setBounds(247, 232, 93, 23);
		contentPane.add(btnNewButtonClose);

	}

	// ������������������
	public SSLSocket createSSLSocket() throws Exception {
		String passphrase = "123456";
		char[] password = passphrase.toCharArray();
		String trustStoreFile = "mykeys.keystore";
		KeyStore ts = KeyStore.getInstance("PKCS12");
		ts.load(new FileInputStream(trustStoreFile), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ts);
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, tmf.getTrustManagers(), null); // �ͻ�������password����ȥ��
		SSLSocketFactory factory = sslContext.getSocketFactory();
		socket = (SSLSocket) factory.createSocket("localhost", port);
		return (SSLSocket) socket;
	}
}
