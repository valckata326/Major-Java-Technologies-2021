package commandexecutortest;

import bg.sofia.uni.fmi.mjt.wish.list.commandhandler.CommandExecutor;
import bg.sofia.uni.fmi.mjt.wish.list.Messages;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandExecutorTest {
    private static SocketChannel firstClient;
    private static SocketChannel secondClient;
    private static CommandExecutor commandExecutor;
    private static final int ONE = 1;
    private static final int ZERO = 0;

    @BeforeClass
    public static void clientSetter() throws IOException {
        firstClient = SocketChannel.open();
        secondClient = SocketChannel.open();
    }

    @Before
    public void preparation() {
        commandExecutor = new CommandExecutor();
    }

    @Test
    public void testRegisterWithInvalidNumberOfArguments() {
        String inputCommand = "register generated";
        String response = commandExecutor.executeCommand(inputCommand, firstClient);
        assertEquals(Messages.INVALID_ARGUMENTS_NUMBER + System.lineSeparator(), response);
    }

    @Test
    public void testRegisterWhenUserIsAlreadyLoggedIn() {
        defaultRegisterInitialisation();
        String secondCommand = "register second user";
        String secondResponse = commandExecutor.executeCommand(secondCommand, firstClient);
        assertEquals(Messages.REGISTER_ALREADY_LOGGED + System.lineSeparator(), secondResponse);
    }

    @Test
    public void testRegisterWithInvalidUsername() {
        String command = "register Va|40 password";
        String username = command.split(" ")[1];
        String response = commandExecutor.executeCommand(command, firstClient);
        assertEquals(String.format(Messages.INVALID_USERNAME, username) + System.lineSeparator(), response);
    }

    @Test
    public void testRegisterWithRegisteredUsername() {
        defaultRegisterInitialisation();
        String secondCommand = "logout";
        String secondResponse = commandExecutor.executeCommand(secondCommand, firstClient);
        assertEquals(Messages.SUCCESSFUL_LOGOUT + System.lineSeparator(), secondResponse);
        String thirdCommand = "register valio neshto si";
        String thirdResponse = commandExecutor.executeCommand(thirdCommand, firstClient);
        assertEquals(String.format(Messages.ALREADY_TAKEN_USERNAME, "valio") + System.lineSeparator(),
                thirdResponse);
    }

    @Test
    public void testSuccessfulRegistration() {
        String command = "register abc defg";
        String username = "abc";
        String firstResponse = commandExecutor.executeCommand(command, firstClient);
        assertEquals(String.format(Messages.SUCCESSFUL_REGISTRATION, username) + System.lineSeparator(),
                firstResponse);
        assertTrue(commandExecutor.getServerStorage().getLoggedUsers().contains(username));
        assertTrue(commandExecutor.getServerStorage().getConnections().containsKey(firstClient));
    }

    @Test
    public void testLoginWithInvalidNumberOfParameters() {
        String firstAttempt = "login";
        String firstResponse = commandExecutor.executeCommand(firstAttempt, firstClient);
        assertEquals(Messages.INVALID_ARGUMENTS_NUMBER + System.lineSeparator(), firstResponse);

        String secondAttempt = "login kaka";
        String secondResponse = commandExecutor.executeCommand(secondAttempt, firstClient);
        assertEquals(Messages.INVALID_ARGUMENTS_NUMBER + System.lineSeparator(), secondResponse);
    }

    @Test
    public void testLoginWhenLoggedInTheServerAlready() { // additional for the task
        defaultRegisterInitialisation();
        String loginCommand = "login Valio valio";
        String secondResponse = commandExecutor.executeCommand(loginCommand, firstClient);
        assertEquals(Messages.LOGGING_TWICE + System.lineSeparator(), secondResponse);
    }

    @Test
    public void testLoginWithInvalidUsernameAndPassword() {
        defaultRegisterInitialisation();

        String secondResponse = commandExecutor.executeCommand(Messages.LOGOUT_COMMAND, firstClient);
        assertEquals(Messages.SUCCESSFUL_LOGOUT + System.lineSeparator(), secondResponse);
        assertTrue(commandExecutor.getServerStorage().getLoggedUsers().isEmpty());
        String firstLoginAttempt = "login nepravilen vhod";
        String thirdResponse = commandExecutor.executeCommand(firstLoginAttempt, firstClient);
        assertEquals(Messages.INVALID_LOGIN_COMBINATION + System.lineSeparator(), thirdResponse);
        String secondLoginAttempt = "login valio wrongpassword";
        String forthResponse = commandExecutor.executeCommand(secondLoginAttempt, firstClient);
        assertEquals(Messages.INVALID_LOGIN_COMBINATION + System.lineSeparator(), forthResponse);
    }

    @Test
    public void testLoginWhenUserIsAlreadyLoggedIn() {
        defaultRegisterInitialisation();
        String secondUser = "login valio pralio";
        String responseMessage = commandExecutor.executeCommand(secondUser, secondClient);
        assertEquals(Messages.USER_ALREADY_LOGGED_IN + System.lineSeparator(), responseMessage);
    }

    @Test
    public void testSuccessfulLogin() {
        defaultRegisterInitialisation();
        String secondResponse = commandExecutor.executeCommand(Messages.LOGOUT_COMMAND, firstClient);
        assertEquals(Messages.SUCCESSFUL_LOGOUT + System.lineSeparator(), secondResponse);
        String correctLogin = "login valio pralio";
        String thirdResponse = commandExecutor.executeCommand(correctLogin, firstClient);
        assertEquals(String.format(Messages.SUCCESSFUL_LOGIN, "valio") + System.lineSeparator(),
                thirdResponse);
    }

    @Test
    public void testSuccessfulLogout() {
        defaultRegisterInitialisation();
        String logoutExecutionResponse = commandExecutor.executeCommand(Messages.LOGOUT_COMMAND, firstClient);
        assertEquals(Messages.SUCCESSFUL_LOGOUT + System.lineSeparator(), logoutExecutionResponse);
        assertEquals(ZERO, commandExecutor.getServerStorage().getConnections().size());
    }

    @Test
    public void testLogoutWhenNotInTheSystem() {
        String logoutExecutionResponse = commandExecutor.executeCommand(Messages.LOGOUT_COMMAND, firstClient);
        assertEquals(Messages.NOT_LOGGED_IN + System.lineSeparator(), logoutExecutionResponse);
    }

    @Test
    public void testPostWishWhenNotLoggedIn() {
        String command = "post-wish valio some presents";
        String responseMessage = commandExecutor.executeCommand(command, firstClient);
        assertEquals(Messages.NOT_LOGGED_IN + System.lineSeparator(), responseMessage);
        assertTrue(commandExecutor.getServerStorage().getWishes().isEmpty());
    }

    @Test
    public void testPostWishWithInvalidNumberOfArguments() {
        defaultRegisterInitialisation();
        String postwishCommand = "post-wish valio";
        String firstResponse = commandExecutor.executeCommand(postwishCommand, firstClient);
        assertEquals(Messages.INVALID_ARGUMENTS_NUMBER + System.lineSeparator(), firstResponse);
    }

    @Test
    public void testPostWishWithNotRegisteredUser() {
        defaultRegisterInitialisation();
        String postwishCommand = "post-wish somename somepresent";
        String username = "somename";
        String firstResponse = commandExecutor.executeCommand(postwishCommand, firstClient);
        assertEquals(String.format(Messages.POST_WISH_NOT_REGISTERED, username) + System.lineSeparator(),
                firstResponse);
        assertTrue(commandExecutor.getServerStorage().getWishes().isEmpty());
    }

    @Test
    public void testPostWishSuccessfully() {
        defaultPostWishCommands();
    }

    @Test
    public void testPostWishWithSameUserAndPresentCombination() {
        defaultPostWishCommands();
        String postWishCommand = "post-wish valio kolelo";
        String username = "valio";
        String responseMessage = commandExecutor.executeCommand(postWishCommand, firstClient);
        assertEquals(String.format(Messages.POST_WISH_ALREADY_EXIST, username) + System.lineSeparator(),
                responseMessage);
        assertEquals(ONE, commandExecutor.getServerStorage().getWishes().get(username).size());
    }

    @Test
    public void testPostWishWithTwoDifferentPresents() {
        defaultPostWishCommands();
        String postWishCommand = "post-wish valio topka";
        String user = "valio";
        String wish = "topka";
        String responseMessage = commandExecutor.executeCommand(postWishCommand, firstClient);
        assertEquals(String.format(Messages.POST_WISH_SUCCESSFULLY_ADDED, wish, user) + System.lineSeparator(),
                responseMessage);
    }

    @Test
    public void testGetWishWhenNotLoggedIn() {
        String responseMessage = commandExecutor.executeCommand(Messages.GET_WISH_COMMAND, firstClient);
        assertEquals(Messages.NOT_LOGGED_IN + System.lineSeparator(), responseMessage);
    }

    @Test
    public void testGetWishWhenNoWishesAvailable() {
        defaultRegisterInitialisation();
        String responseMessage = commandExecutor.executeCommand(Messages.GET_WISH_COMMAND, firstClient);
        assertEquals(Messages.NO_WISHES_AVAILABLE + System.lineSeparator(), responseMessage);
        assertTrue(commandExecutor.getServerStorage().getWishes().isEmpty());
    }

    @Test
    public void testGetWishWithOneWishAvailable() {
        defaultPostWishCommands();
        String responseMessage = commandExecutor.executeCommand(Messages.GET_WISH_COMMAND, firstClient);
        assertEquals(Messages.NO_WISHES_AVAILABLE + System.lineSeparator(), responseMessage);
    }

    @Test
    public void testGetWishSuccessfullyWithTwoWishesOnUser() {
        defaultPostWishCommands();
        String firstPostWishCommand = Messages.POST_WISH_COMMAND + " valio topka";
        String user = "valio";
        String wish = "topka";
        String firstPostWishResponse = commandExecutor.executeCommand(firstPostWishCommand, firstClient);
        assertEquals(String.format(Messages.POST_WISH_SUCCESSFULLY_ADDED, wish, user) + System.lineSeparator(),
                firstPostWishResponse);

        String logoutCompleted = commandExecutor.executeCommand(Messages.LOGOUT_COMMAND, firstClient);
        assertEquals(Messages.SUCCESSFUL_LOGOUT + System.lineSeparator(), logoutCompleted);

        String registerAnotherUser = Messages.REGISTER_COMMAND + " second user";
        String registerWithDifferentUsername = commandExecutor.executeCommand(registerAnotherUser, firstClient);
        assertEquals(String.format(Messages.SUCCESSFUL_REGISTRATION, "second") + System.lineSeparator(),
                registerWithDifferentUsername);

        String secondPostWishCommand = Messages.POST_WISH_COMMAND + " second SomeGift";
        String secondPostWishResponse = commandExecutor.executeCommand(secondPostWishCommand, firstClient);
        assertEquals(String.format(Messages.POST_WISH_SUCCESSFULLY_ADDED, "SomeGift", "second")
                + System.lineSeparator(), secondPostWishResponse);

        String secondResponse = commandExecutor.executeCommand(Messages.GET_WISH_COMMAND, firstClient);
        String expected = "[ valio: [kolelo, topka] ]" + System.lineSeparator();
        assertEquals(expected, secondResponse);
    }

    @Test
    public void testDisconnectCommand() {
        defaultRegisterInitialisation();
        String responseMessage = commandExecutor.executeCommand(Messages.DISCONNECT_COMMAND, firstClient);
        assertEquals(Messages.DISCONNECTED_FROM_SERVER_MESSAGE + System.lineSeparator(), responseMessage);
        assertTrue(commandExecutor.getServerStorage().getConnections().isEmpty());
    }

    @Test
    public void testUnknownCommand() {
        String unknownCommand = "somecommand not working";
        String responseMessage = commandExecutor.executeCommand(unknownCommand, firstClient);
        assertEquals(Messages.UNKNOWN_COMMAND + System.lineSeparator(), responseMessage);
    }

    private void defaultPostWishCommands() {
        defaultRegisterInitialisation();
        String postwishCommand = "post-wish valio kolelo";
        String user = "valio";
        String wish = "kolelo";
        String firstResponse = commandExecutor.executeCommand(postwishCommand, firstClient);
        assertEquals(String.format(Messages.POST_WISH_SUCCESSFULLY_ADDED, wish, user) + System.lineSeparator(),
                firstResponse);
    }

    private void defaultRegisterInitialisation() {
        String register = "register valio pralio";
        String username = "valio";
        String firstResponse = commandExecutor.executeCommand(register, firstClient);
        assertEquals(String.format(Messages.SUCCESSFUL_REGISTRATION, username) + System.lineSeparator(),
                firstResponse);
    }
}
