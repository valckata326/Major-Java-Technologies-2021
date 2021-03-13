package bg.sofia.uni.fmi.mjt.wish.list;

public interface Messages {
    String REGISTER_COMMAND = "register";
    String ALREADY_TAKEN_USERNAME = "[ Username %s is already taken, select another one ]";
    String SUCCESSFUL_REGISTRATION = "[ Username %s successfully registered ]";
    String INVALID_ARGUMENTS_NUMBER = "[ Invalid number of arguments ]";
    String REGISTER_ALREADY_LOGGED = "[ You are already logged in, so you cannot register ]";

    String LOGIN_COMMAND = "login";
    String LOGGING_TWICE = "[ You are already logged in, so you cannot switch accounts ]";
    String INVALID_LOGIN_COMBINATION = "[ Invalid username/password combination ]";
    String USER_ALREADY_LOGGED_IN = "[ This user is already logged in ]";
    String SUCCESSFUL_LOGIN = "[ User %s successfully logged in ]";

    String LOGOUT_COMMAND = "logout";
    String SUCCESSFUL_LOGOUT = "[ Successfully logged out ]";
    String NOT_LOGGED_IN = "[ You are not logged in ]";

    String POST_WISH_COMMAND = "post-wish";
    String POST_WISH_NOT_REGISTERED = "[ Student with username %s is not registered ]";
    String POST_WISH_SUCCESSFULLY_ADDED = "[ Gift %s for student %s submitted successfully ]";
    String POST_WISH_ALREADY_EXIST = "[ The same gift for student %s was already submitted ]";

    String GET_WISH_COMMAND = "get-wish";
    String NO_WISHES_AVAILABLE = "[ There are no students present in the wish list ]";

    String DISCONNECT_COMMAND = "disconnect";
    String DISCONNECTED_FROM_SERVER_MESSAGE = "[ Disconnected from server ]";

    String USERNAME_REGEX = "^[A-Za-z0-9-_.]+$";
    String INVALID_USERNAME = "[ Username %s is invalid, select a valid one ]";

    String UNKNOWN_COMMAND = "[ Unknown command ]";
}
