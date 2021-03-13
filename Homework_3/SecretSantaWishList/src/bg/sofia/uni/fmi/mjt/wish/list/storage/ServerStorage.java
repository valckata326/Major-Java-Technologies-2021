package bg.sofia.uni.fmi.mjt.wish.list.storage;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerStorage {
    private Map<String, String> users;
    private Map<String, Set<String>> wishes;
    private Set<String> loggedUsers;
    private Map<SocketChannel, String> connections;

    public ServerStorage() {
        users = new HashMap<>();
        wishes = new HashMap<>();
        loggedUsers = new HashSet<>();
        connections = new HashMap<>();
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public Map<String, Set<String>> getWishes() {
        return wishes;
    }

    public Set<String> getLoggedUsers() {
        return loggedUsers;
    }

    public Map<SocketChannel, String> getConnections() {
        return connections;
    }
}
