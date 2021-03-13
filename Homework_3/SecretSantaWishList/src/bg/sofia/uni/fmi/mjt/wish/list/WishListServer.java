package bg.sofia.uni.fmi.mjt.wish.list;

import bg.sofia.uni.fmi.mjt.wish.list.commandhandler.CommandExecutor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class WishListServer {
    private static final String SERVER_HOST = "localhost";
    private static int port;
    private static final int BUFFER_SIZE = 1024;
    private ByteBuffer buffer;
    private CommandExecutor commandExecutor;
    private Selector selector;
    private boolean flag;

    public WishListServer(int port) {
        WishListServer.port = port;
        commandExecutor = new CommandExecutor();
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerChannel(serverSocketChannel, selector);
            flag = false;
            while (!flag) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        String clientCommand = getClientCommand(clientChannel);
                        if (!verifyClientConnected(clientCommand)) {
                            continue;
                        }
                        clientCommand = clientCommand.replace(System.getProperty("line.separator"), "");
                        String response = commandExecutor.executeCommand(clientCommand, clientChannel);
                        sendResponse(response, clientChannel);
                    } else if (key.isAcceptable()) {
                        acceptClient(selector, key);
                    }
                    keyIterator.remove();
                }
            }

        } catch (IOException exception) {
            throw new UncheckedIOException("Server failed to start", exception);
        }
    }

    public void stop() {
        flag = true;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void sendResponse(String returnCommand, SocketChannel clientChannel) throws IOException {
        buffer.clear();
        buffer.put(returnCommand.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }
    }

    private void configureServerChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientCommand(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int r = clientChannel.read(buffer);
        if (r <= 0) {
            clientChannel.close();
            return null;
        }
        buffer.flip();
        byte[] clientMessage = new byte[buffer.remaining()];
        buffer.get(clientMessage);
        return new String(clientMessage, StandardCharsets.UTF_8);
    }

    private void acceptClient(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = socketChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private boolean verifyClientConnected(String clientCommand) {
        return clientCommand != null;
    }

}
