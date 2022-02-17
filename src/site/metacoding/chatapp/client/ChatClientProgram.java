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

		chatList = new TextArea(10, 30); // 가로, 세로
		chatList.setEditable(false);
		chatList.setBackground(Color.ORANGE);
		chatList.setForeground(Color.BLUE);

		scroll = new ScrollPane();
		scroll.add(chatList); // 스크롤에 textArea 붙이기

		txtHost = new JTextField(20); // 사이즈 20
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

		// JFrame에 붙이기
		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(scroll, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

		initListener();

		setVisible(true);
	}

	private void initListener() {
		// 소켓 연결 버튼
		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});

		// 텍스트 보내기 버튼
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
			
			// localhost = 127.0.0.1 루프백 주소
			// 쌤 IP = 192.168.0.132
			socket = new Socket(host, port); // 연결
			
			System.out.println(host + "서버쪽 " + port + "번 포트로 연결합니다.");

			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			chatList.append("아이디를 입력하세요.");

		} catch (Exception e2) {
			// e2.printStackTrace();
			e2.getMessage();
		}
	}

	public void send() {
		
		// 새로운 스레드(읽기 전용)
		new Thread(new 읽기전담스레드()).start();
		
		// 최초 username 전송 프로토콜
		// 아이디 입력하세요
		username = txtMsg.getText();
		
		try {
			writer.write(username + "\n");
			writer.flush(); // 버퍼에 담긴 데이터 Stream으로 흘려보내기
		} catch (Exception e1) {
			e1.printStackTrace();
		} // 내 버퍼에 담기
		
		System.out.println("username : " + username + " 이 서버로 전송되었습니다.");
		
		String msg = txtMsg.getText();
		chatList.append(msg + "\n");
		txtMsg.setText(""); // 비우기
		txtMsg.requestFocus(); // 커서 두기
		
		// 메인 스레드(쓰기 전용)
		while (true) {
			String keyboardInputData = txtMsg.getText();

			// 중계자(서버 소켓)에게만 write하면 됨
			// 끝에 "\n" 필수 -> 이거 안쓰려면 PrintWrite 쓰면 됨
			try {
				writer.write(keyboardInputData + "\n");
				writer.flush(); // 버퍼에 담긴 데이터 Stream으로 흘려보내기
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 내 버퍼에 담기
		}
	}

	class 읽기전담스레드 implements Runnable {

		@Override
		public void run() {

		}

	}

	public static void main(String[] args) {
		new ChatClientProgram();
	}

}
