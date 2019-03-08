import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class ChatRoomServer {
    ArrayList<CliSocket> cliSockets = new ArrayList<>();

    public static void main(String[] args) {
        new ChatRoomServer().startService();
    }
    
    private void startService() {
        try (ServerSocket sskt = new ServerSocket(8888)) {
            while(true) {
                CliSocket cliSkt = new CliSocket(sskt.accept());
                cliSockets.add(cliSkt);
                new Thread(cliSkt).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class CliSocket implements Runnable {
        Socket socket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;


        public CliSocket(Socket skt) {
            try {
                this.socket = skt;
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void notifyUser(String msg) {
            try {
                dos.writeUTF(socket.getInetAddress() + ": " + msg);
                dos.flush();
            } catch (Exception e) {
                //TODO: handle exception
            }
        }

        @Override
        public void run() {
            try {
                String msg = null;
                while (true) {
                    msg = dis.readUTF();
                    if (msg.trim().equals("quit();")) {
                        // socket.shutdownInput();
                        // notifyUser("quit();");
                        // socket.shutdownOutput();
                        break;
                    } else {
                        for (CliSocket cliSkt : cliSockets) {
                            if (!this.equals(cliSkt)) {
                                cliSkt.notifyUser(msg);
                            }
                        }
                    }
                }
                socket.shutdownInput();
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}