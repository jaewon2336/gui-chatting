package site.metacoding.chatapp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

/**
 * ProtocolCheck 프로토콜
 * 
 * 1. 최초 메시지는 username으로 체킹
 * 2. 구분자 :
 * 3. 전체채팅 ALL:메시지
 * 4. 귓속말 CHAT:아이디:메시지
 */

public class ChatServer {

    // 리스너(연결받기) -> 메인 스레드
    ServerSocket serverSocket;
    List<ClientThread> clientList;

    // 서버는 메시지 여러명에게 받아서 보내기(순차적)
    // 새로운 스레드, 클라이언트 수마다
    // 채팅서버는 메시지(요청)를 받을 때만 동작! -> pull 서버

    public ChatServer() {

        try {
            serverSocket = new ServerSocket(2000);
            clientList = new Vector<>(); // 동기화가 처리된 ArrayList

            // while 돌리기
            // 여러사람이 요청할 때마다 소켓이 새로 생성되어야하기 때문에 전역변수로 생성 X
            while (true) {
                Socket socket = serverSocket.accept(); // 대기 -> main 스레드가 하는 일
                System.out.println("클라이언트 연결됨");

                // while의 stack이 종료되면 t의 값이 가비지컬렉션 되기 때문에
                // 고객 socket을 기억하기 위해 전역 ArrayList에 보관하기
                ClientThread t = new ClientThread(socket);
                clientList.add(t); // 클라이언트 각각의 socket을 가지고있음
                System.out.println("고객리스트 크기 : " + clientList.size());

                new Thread(t).start();
            }
        } catch (Exception e) {
            System.out.println("오류내용 : " + e.getMessage());
        }

    }

    // 내부 클래스
    class ClientThread implements Runnable {

        // 소켓 보관 컬렉션
        Socket socket;
        String username;
        BufferedReader reader;
        BufferedWriter writer;
        boolean isLogin;

        public ClientThread(Socket socket) {
            this.socket = socket;

            // new될 때 양끝단에 버퍼달림
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 전체채팅 ALL:메시지
        public void chatPublic(String msg) {
            try {
                // System.out.println("전체채팅");

                // 메시지 받았으니까 List<고객전담스레드> 고객리스트 <== 여기에 담긴
                // 모든 클라이언트에게 메시지 전송(컬렉션 크기만큼 for문 돌려서!!)
                // 컬렉션의 1번째 데이터를 t에 넣는것
                // 자신이 보낸 메세지는 자신에게 돌아오지 않기 if문
                for (ClientThread t : clientList) { // 컬렉션타입 : 컬렉션
                    if (t != this) {
                        t.writer.write("[전체채팅]" + username + " : " + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 귓속말 CHAT:아이디:메시지
        public void chatPrivate(String reciever, String msg) {
            // System.out.println("귓속말");

            try {
                for (ClientThread t : clientList) { // 컬렉션타입 : 컬렉션
                    if (t.username.equals(reciever)) {
                        t.writer.write("[귓속말]" + username + " : " + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 프로토콜 검사기
        // ALL:안녕
        // CHAT:재원:안녕
        public void protocolCheck(String inputData) {
            // 1. 프로토콜 분리
            String[] token = inputData.split(":"); // 0번지 : 프로토콜, 배열의 크기가 2 or 3 z
            // 크기로 구분하지 말기 !! ALL:안녕:ㅋㅋㅋ 이것도 3이니까 !!
            String protocol = token[0];
            if (protocol.equals("ALL")) {
                String msg = token[1];
                chatPublic(msg);
            } else if (protocol.equals("CHAT")) {
                String reciever = token[1];
                String msg = token[2];
                chatPrivate(reciever, msg);
            } else { // 프로토콜 통과 못함
                System.out.println("프로토콜 없음");
            }
        }

        @Override
        public void run() {

            // 최초 메시지는 username
            try {
                username = reader.readLine();
                isLogin = true; // 세션 생성
            } catch (Exception e) {
                isLogin = false; // 세션 생성 X
                System.out.println("username을 받지 못했습니다.");
            }

            while (isLogin) {
                String inputData;
                try {
                    inputData = reader.readLine();
                    // System.out.println("From 클라이언트 : " + inputData);

                    // 프로토콜 검사기로 던지기
                    // protocolCheck(inputData);

                } catch (Exception e) {
                    try {
                        // 클라이언트로부터 메세지를 읽는데, 클라이언트가 연결을 해지하면
                        // readline에서 대기하다가 Stream이 끊겨서 catch로 넘어오고
                        // while은 catch만 계속 반복해서 출력된다.
                        System.out.println("오류내용 : " + e.getMessage());
                        isLogin = false;
                        // 리스트에서 참조중이라서 사라지지 않으니까 heap에 떠있는 자신을 날림
                        clientList.remove(this);
                        reader.close();
                        writer.close();
                        socket.close();
                    } catch (Exception e1) {
                        System.out.println("연결해제 프로세스 실패 " + e1.getMessage());
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }

}