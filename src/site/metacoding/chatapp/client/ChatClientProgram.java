package site.metacoding.chatapp.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ChatClientProgram extends JFrame {

	JPanel northPanel, southPanel;
	TextArea chatList;

	ScrollPane scroll;

	JTextField txtHost, txtPort, txtMsg;

	JButton btnConnect, btnSend;

	Socket socket;

	String username, host;

	int port;

	BufferedReader reader;
	BufferedWriter writer;

	public ChatClientProgram() {
		setTitle("MyChat1.0");
		setSize(400, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		northPanel = new JPanel();
		southPanel = new JPanel();

		chatList = new TextArea(10, 30); // ����, ����
		chatList.setEditable(false);
		chatList.setBackground(Color.ORANGE);
		chatList.setForeground(Color.BLUE);

		scroll = new ScrollPane();
		scroll.add(chatList); // ��ũ�ѿ� textArea ���̱�

		txtHost = new JTextField(20); // ������ 20
		txtHost.setText("127.0.0.1");

		txtPort = new JTextField(5);
		txtPort.setText("2000");

		txtMsg = new JTextField(25);

		btnConnect = new JButton("Connect");
		btnSend = new JButton("Send");

		northPanel.add(txtHost);
		northPanel.add(txtPort);
		northPanel.add(btnConnect);

		southPanel.add(txtMsg);
		southPanel.add(btnSend);

		// JFrame�� ���̱�
		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(scroll, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

		initListener();

		setVisible(true);
	}

	private void initListener() {
		// ���� ���� ��ư
		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});

		// �ؽ�Ʈ ������ ��ư
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
	}

	public void connect() {
		try {
			host = txtHost.getText();
			port = Integer.parseInt(txtPort.getText());
			
			// localhost = 127.0.0.1 ������ �ּ�
			// �� IP = 192.168.0.132
			socket = new Socket(host, port); // ����
			
			System.out.println(host + "������ " + port + "�� ��Ʈ�� �����մϴ�.");

			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			chatList.append("���̵� �Է��ϼ���.");

		} catch (Exception e2) {
			// e2.printStackTrace();
			e2.getMessage();
		}
	}

	public void send() {
		
		// ���ο� ������(�б� ����)
		new Thread(new �б����㽺����()).start();
		
		// ���� username ���� ��������
		// ���̵� �Է��ϼ���
		username = txtMsg.getText();
		
		try {
			writer.write(username + "\n");
			writer.flush(); // ���ۿ� ��� ������ Stream���� ���������
		} catch (Exception e1) {
			e1.printStackTrace();
		} // �� ���ۿ� ���
		
		System.out.println("username : " + username + " �� ������ ���۵Ǿ����ϴ�.");
		
		String msg = txtMsg.getText();
		chatList.append(msg + "\n");
		txtMsg.setText(""); // ����
		txtMsg.requestFocus(); // Ŀ�� �α�
		
		// ���� ������(���� ����)
		while (true) {
			String keyboardInputData = txtMsg.getText();

			// �߰���(���� ����)���Ը� write�ϸ� ��
			// ���� "\n" �ʼ� -> �̰� �Ⱦ����� PrintWrite ���� ��
			try {
				writer.write(keyboardInputData + "\n");
				writer.flush(); // ���ۿ� ��� ������ Stream���� ���������
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // �� ���ۿ� ���
		}
	}

	class �б����㽺���� implements Runnable {

		@Override
		public void run() {

		}

	}

	public static void main(String[] args) {
		new ChatClientProgram();
	}

}
