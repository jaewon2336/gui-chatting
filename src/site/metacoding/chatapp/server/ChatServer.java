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
 * ProtocolCheck ��������
 * 
 * 1. ���� �޽����� username���� üŷ
 * 2. ������ :
 * 3. ��üä�� ALL:�޽���
 * 4. �ӼӸ� CHAT:���̵�:�޽���
 */

public class ChatServer {

    // ������(����ޱ�) -> ���� ������
    ServerSocket serverSocket;
    List<ClientThread> clientList;

    // ������ �޽��� �������� �޾Ƽ� ������(������)
    // ���ο� ������, Ŭ���̾�Ʈ ������
    // ä�ü����� �޽���(��û)�� ���� ���� ����! -> pull ����

    public ChatServer() {

        try {
            serverSocket = new ServerSocket(2000);
            clientList = new Vector<>(); // ����ȭ�� ó���� ArrayList

            // while ������
            // ��������� ��û�� ������ ������ ���� �����Ǿ���ϱ� ������ ���������� ���� X
            while (true) {
                Socket socket = serverSocket.accept(); // ��� -> main �����尡 �ϴ� ��
                System.out.println("Ŭ���̾�Ʈ �����");

                // while�� stack�� ����Ǹ� t�� ���� �������÷��� �Ǳ� ������
                // �� socket�� ����ϱ� ���� ���� ArrayList�� �����ϱ�
                ClientThread t = new ClientThread(socket);
                clientList.add(t); // Ŭ���̾�Ʈ ������ socket�� ����������
                System.out.println("������Ʈ ũ�� : " + clientList.size());

                new Thread(t).start();
            }
        } catch (Exception e) {
            System.out.println("�������� : " + e.getMessage());
        }

    }

    // ���� Ŭ����
    class ClientThread implements Runnable {

        // ���� ���� �÷���
        Socket socket;
        String username;
        BufferedReader reader;
        BufferedWriter writer;
        boolean isLogin;

        public ClientThread(Socket socket) {
            this.socket = socket;

            // new�� �� �糡�ܿ� ���۴޸�
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ��üä�� ALL:�޽���
        public void chatPublic(String msg) {
            try {
                // System.out.println("��üä��");

                // �޽��� �޾����ϱ� List<�����㽺����> ������Ʈ <== ���⿡ ���
                // ��� Ŭ���̾�Ʈ���� �޽��� ����(�÷��� ũ�⸸ŭ for�� ������!!)
                // �÷����� 1��° �����͸� t�� �ִ°�
                // �ڽ��� ���� �޼����� �ڽſ��� ���ƿ��� �ʱ� if��
                for (ClientThread t : clientList) { // �÷���Ÿ�� : �÷���
                    if (t != this) {
                        t.writer.write("[��üä��]" + username + " : " + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // �ӼӸ� CHAT:���̵�:�޽���
        public void chatPrivate(String reciever, String msg) {
            // System.out.println("�ӼӸ�");

            try {
                for (ClientThread t : clientList) { // �÷���Ÿ�� : �÷���
                    if (t.username.equals(reciever)) {
                        t.writer.write("[�ӼӸ�]" + username + " : " + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // �������� �˻��
        // ALL:�ȳ�
        // CHAT:���:�ȳ�
        public void protocolCheck(String inputData) {
            // 1. �������� �и�
            String[] token = inputData.split(":"); // 0���� : ��������, �迭�� ũ�Ⱑ 2 or 3 z
            // ũ��� �������� ���� !! ALL:�ȳ�:������ �̰͵� 3�̴ϱ� !!
            String protocol = token[0];
            if (protocol.equals("ALL")) {
                String msg = token[1];
                chatPublic(msg);
            } else if (protocol.equals("CHAT")) {
                String reciever = token[1];
                String msg = token[2];
                chatPrivate(reciever, msg);
            } else { // �������� ��� ����
                System.out.println("�������� ����");
            }
        }

        @Override
        public void run() {

            // ���� �޽����� username
            try {
                username = reader.readLine();
                isLogin = true; // ���� ����
            } catch (Exception e) {
                isLogin = false; // ���� ���� X
                System.out.println("username�� ���� ���߽��ϴ�.");
            }

            while (isLogin) {
                String inputData;
                try {
                    inputData = reader.readLine();
                    // System.out.println("From Ŭ���̾�Ʈ : " + inputData);

                    // �������� �˻��� ������
                    // protocolCheck(inputData);

                } catch (Exception e) {
                    try {
                        // Ŭ���̾�Ʈ�κ��� �޼����� �дµ�, Ŭ���̾�Ʈ�� ������ �����ϸ�
                        // readline���� ����ϴٰ� Stream�� ���ܼ� catch�� �Ѿ����
                        // while�� catch�� ��� �ݺ��ؼ� ��µȴ�.
                        System.out.println("�������� : " + e.getMessage());
                        isLogin = false;
                        // ����Ʈ���� �������̶� ������� �����ϱ� heap�� ���ִ� �ڽ��� ����
                        clientList.remove(this);
                        reader.close();
                        writer.close();
                        socket.close();
                    } catch (Exception e1) {
                        System.out.println("�������� ���μ��� ���� " + e1.getMessage());
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }

}