import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

class ChatRoomClient {
    private Socket skt = null;
    public static void main(String[] args) {
        new ChatRoomClient().start();
    }

    private void init() {
        try {
            this.skt = new Socket("127.0.0.1", 8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        CountDownLatch doneSignal = new CountDownLatch(2);
        init();
        try {
            new Thread(new UserListener(doneSignal, skt.getOutputStream())).start();
            new Thread(new ServerListener(doneSignal, skt.getInputStream())).start();
            doneSignal.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closeChatRoom();
    }

    private void closeChatRoom() {
        try {
            skt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class UserListener implements Runnable {
        private final CountDownLatch doneSignal;
        DataOutputStream dos = null;

        UserListener(CountDownLatch doneSignal, OutputStream output) {
            this.doneSignal = doneSignal;
            dos = new DataOutputStream(output);
        }

        @Override
        public void run() {
            try {
                BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in));
                String msg = null;
                while (!skt.isClosed() && (msg = bfr.readLine()) != null) {
                    dos.writeUTF(msg);
                    dos.flush();
                    if (msg.equals("quit();")) {
                        // skt.shutdownOutput();
                        break;
                    }
                }
                bfr.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                doneSignal.countDown();
            }
        }
    }

    private class ServerListener implements Runnable {
        private final CountDownLatch doneSignal;
        DataInputStream dis = null;

        ServerListener(CountDownLatch doneSignal, InputStream input) {
            this.doneSignal = doneSignal;
            dis = new DataInputStream(input);
        }

        @Override
        public void run() {
            try {
                String serverMsg = null;
                while(!skt.isClosed() && (serverMsg = dis.readUTF()) != null) {
                    // if (serverMsg.equals("quit();")) {
                        // skt.shutdownInput();
                        // break;
                    // }
                    System.out.println(serverMsg);
                }
            } catch (SocketException e) {
                System.out.println(e.getMessage() + skt.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                doneSignal.countDown();
            }
        }
    }
}