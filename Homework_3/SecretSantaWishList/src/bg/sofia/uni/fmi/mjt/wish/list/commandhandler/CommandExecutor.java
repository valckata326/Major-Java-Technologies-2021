package bg.sofia.uni.fmi.mjt.wish.list.commandhandler;

import bg.sofia.uni.fmi.mjt.wish.list.Messages;
import bg.sofia.uni.fmi.mjt.wish.list.storage.ServerStorage;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class CommandExecutor {
    private ServerStorage serverStorage;

    public CommandExecutor() {
        serverStorage = new ServerStorage();
    }

    public String executeCommand(String command, SocketChannel clientChannel) {
        command = command.trim().replaceAll("\\s+", " ");
        String[] commandWords = command.split(" ", 2);
        String commandName = commandWords[0];
        String commandArgs = "";
        if (commandWords.length == 2) {
            commandArgs = commandWords[1];
        }
        switch (commandName) {
            case Messages.REGISTER_COMMAND -> {
                return executeRegister(commandArgs, clientChannel);
            }
            case Messages.LOGIN_COMMAND -> {
                return executeLogin(commandArgs, clientChannel);
            }
            case Messages.LOGOUT_COMMAND -> {
                return executeLogout(clientChannel);
            }
            case Messages.POST_WISH_COMMAND -> {
                return executePostWish(commandArgs, clientChannel);
            }
            case Messages.GET_WISH_COMMAND -> {
                return executeGetWish(clientChannel);
            }
            case Messages.DISCONNECT_COMMAND -> {
                return executeDisconnect(clientChannel);
            }
            default -> {
                return executeInvalid(clientChannel);
            }
        }
    }

    private String executeRegister(String args, SocketChannel clientChannel) {
        String[] argsWords = args.split("\\s+", 2);
        if (argsWords.length < 2) {
            return replyToClientWIth(Messages.INVALID_ARGUMENTS_NUMBER, clientChannel);
        } else {
            if (getServerStorage().getConnections().containsKey(clientChannel)) {
                return replyToClientWIth(Messages.REGISTER_ALREADY_LOGGED, clientChannel);
            }
            String username = argsWords[0];
            if (!username.matches(Messages.USERNAME_REGEX)) {
                return replyToClientWIth(String.format(Messages.INVALID_USERNAME, username), clientChannel);
            }
            String password = argsWords[1];
            if (getServerStorage().getUsers().containsKey(username)) {
                return replyToClientWIth(String.format(Messages.ALREADY_TAKEN_USERNAME, username), clientChannel);
            } else {
                getServerStorage().getUsers().put(username, password);
                getServerStorage().getLoggedUsers().add(username);
                getServerStorage().getConnections().put(clientChannel, username);
                return replyToClientWIth(String.format(Messages.SUCCESSFUL_REGISTRATION, username), clientChannel);
            }
        }
    }

    private String executeLogin(String args, SocketChannel clientChannel) {
        String[] argsWords = args.split("\\s+", 2);
        if (argsWords.length != 2) {
            return replyToClientWIth(Messages.INVALID_ARGUMENTS_NUMBER, clientChannel);
        } else {
            if (getServerStorage().getConnections().containsKey(clientChannel)) {
                return replyToClientWIth(Messages.LOGGING_TWICE, clientChannel);
            }
            String username = argsWords[0];
            String password = argsWords[1];
            if (!getServerStorage().getUsers().containsKey(username)) {
                return replyToClientWIth(Messages.INVALID_LOGIN_COMBINATION, clientChannel);
            } else if (!getServerStorage().getUsers().get(username).equals(password)) {
                return replyToClientWIth(Messages.INVALID_LOGIN_COMBINATION, clientChannel);
            } else if (getServerStorage().getLoggedUsers().contains(username)) {
                return replyToClientWIth(Messages.USER_ALREADY_LOGGED_IN, clientChannel);
            } else {
                getServerStorage().getLoggedUsers().add(username);
                getServerStorage().getConnections().put(clientChannel, username);
                return replyToClientWIth(String.format(Messages.SUCCESSFUL_LOGIN, username), clientChannel);
            }
        }
    }

    private String executeLogout(SocketChannel clientChannel) {
        if (getServerStorage().getConnections().containsKey(clientChannel)) {
            getServerStorage().getLoggedUsers().remove(getServerStorage().getConnections().get(clientChannel));
            getServerStorage().getConnections().remove(clientChannel);
            return replyToClientWIth(Messages.SUCCESSFUL_LOGOUT, clientChannel);
        } else {
            return replyToClientWIth(Messages.NOT_LOGGED_IN, clientChannel);
        }
    }

    private String executePostWish(String args, SocketChannel clientChannel) {
        if (!getServerStorage().getConnections().containsKey(clientChannel)) {
            return replyToClientWIth(Messages.NOT_LOGGED_IN, clientChannel);
        } else {
            String[] argsWords = args.split("\\s+", 2);
            if (argsWords.length < 2) {
                return replyToClientWIth(Messages.INVALID_ARGUMENTS_NUMBER, clientChannel);
            }
            String username = argsWords[0];
            String wish = argsWords[1];
            if (!getServerStorage().getUsers().containsKey(username)) {
                return replyToClientWIth(String.format(Messages.POST_WISH_NOT_REGISTERED, username), clientChannel);
            } else if (!getServerStorage().getWishes().containsKey(username)) {
                getServerStorage().getWishes().put(username, new HashSet<>());
                getServerStorage().getWishes().get(username).add(wish);
                return replyToClientWIth(String.format(Messages.POST_WISH_SUCCESSFULLY_ADDED, wish, username),
                        clientChannel);
            } else if (getServerStorage().getWishes().get(username).contains(wish)) {
                return replyToClientWIth(String.format(Messages.POST_WISH_ALREADY_EXIST, username), clientChannel);
            } else {
                getServerStorage().getWishes().get(username).add(wish);
                return replyToClientWIth(String.format(Messages.POST_WISH_SUCCESSFULLY_ADDED, wish, username),
                        clientChannel);
            }
        }
    }

    private String executeGetWish(SocketChannel clientChannel) {
        if (!getServerStorage().getConnections().containsKey(clientChannel)) {
            return replyToClientWIth(Messages.NOT_LOGGED_IN, clientChannel);
        }
        if (getServerStorage().getWishes().isEmpty() || verifyCanGetWish(clientChannel)) {
            return replyToClientWIth(Messages.NO_WISHES_AVAILABLE, clientChannel);
        }
        String randomUser = getRandomUser(clientChannel);
        Set<String> userWishes = new HashSet<>(getServerStorage().getWishes().get(randomUser));
        getServerStorage().getWishes().remove(randomUser);
        return replyToClientWIth(createGetWishResponse(randomUser,
                userWishes), clientChannel);
    }

    private String executeInvalid(SocketChannel clientChannel) {
        return replyToClientWIth(Messages.UNKNOWN_COMMAND, clientChannel);
    }

    private String replyToClientWIth(String returnCommand, SocketChannel clientChannel) {
        returnCommand = returnCommand + System.lineSeparator();
        return returnCommand;
    }

    private String createGetWishResponse(String username, Set<String> wishes) {
        String responseString = "[ " + username + ": [";
        List<String> userWishes = new ArrayList<>(wishes);
        for (int i = 0; i < userWishes.size() - 1; i++) {
            responseString = responseString + userWishes.get(i) + ", ";
        }
        responseString = responseString + userWishes.get(userWishes.size() - 1) + "] ]";
        return responseString;
    }

    private String executeDisconnect(SocketChannel clientChannel) {
        if (getServerStorage().getConnections().containsKey(clientChannel)) {
            getServerStorage().getLoggedUsers().remove(getServerStorage().getConnections().get(clientChannel));
            getServerStorage().getConnections().remove(clientChannel);
        }
        return replyToClientWIth(Messages.DISCONNECTED_FROM_SERVER_MESSAGE, clientChannel);
    }

    public ServerStorage getServerStorage() {
        return serverStorage;
    }

    private boolean verifyCanGetWish(SocketChannel clientChannel) {
        if (getServerStorage().getConnections().containsKey(clientChannel)) {
            return getServerStorage().getWishes().size() == 1
                    && getServerStorage().getWishes().containsKey(getServerStorage()
                    .getConnections().get(clientChannel));
        }
        return true;
    }

    private String getRandomUser(SocketChannel clientChannel) {
        List<String> wishesKeySet = new ArrayList<>(getServerStorage().getWishes().keySet());
        int size = wishesKeySet.size();
        int randomKey = new Random().nextInt(size);
        String randomUser = wishesKeySet.get(randomKey);
        while (randomUser.equals(getServerStorage().getConnections().get(clientChannel))) {
            randomKey = new Random().nextInt(size);
            randomUser = wishesKeySet.get(randomKey);
        }
        return randomUser;
    }
}
