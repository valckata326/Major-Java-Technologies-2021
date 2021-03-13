package bg.sofia.uni.fmi.mjt.wish.list;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WishListClient {
    private static final int SERVER_PORT = 3333;
    private static final String SERVER_HOST = "localhost";
    public static final String DISCONNECTED = "[ Disconnected from server ]";
    private ByteBuffer buffer;
    private boolean flag;

    public WishListClient() {
        this.flag = false;
        buffer = ByteBuffer.allocateDirect(1024);
    }

    public void startClient() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            while (!flag) {
                String message = scanner.nextLine();
                if (message.isEmpty()) {
                    continue;
                }
                message = message + System.lineSeparator();
                sendMessageToServer(socketChannel, message);

                String serverReply = serverReply(socketChannel);
                System.out.print(serverReply);
                validateDisconnection(serverReply);
            }

        } catch (IOException exception) {
            throw new UncheckedIOException("There is a problem with client connection", exception);
        }
    }

    private void sendMessageToServer(SocketChannel socketChannel, String message) throws IOException {
        buffer.clear(); //switching to writing mode
        buffer.put(message.getBytes());
        buffer.flip(); //switching to reading mode
        socketChannel.write(buffer);
    }

    private String serverReply(SocketChannel serverChannel) {
        try {
            buffer.clear(); // switching to writing mode
            serverChannel.read(buffer); // server filled the buffer
            buffer.flip(); // switching to reading mode

            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);
            return new String(byteArray, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.out.println("There is a problem with the server reply");
            exception.printStackTrace();
            return null;
        }
    }

    private void validateDisconnection(String serverReply) {
        serverReply = serverReply.replace(System.getProperty("line.separator"), "");
        if (serverReply.equals(DISCONNECTED)) {
            flag = true;
        }
    }

    public static void main(String[] args) {
        WishListClient client = new WishListClient();
        client.startClient();
    }
}
