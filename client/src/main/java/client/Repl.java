package client;

import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.util.Scanner;

public class Repl {
    private final ServerFacade server;
    private boolean isLoggedIn = false;
    private String authToken = null;


    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to 240 chess. Type Help to get started.");

        Scanner scanner = new Scanner(System.in);
        String result = "";

        while(true) {
            printPrompt();

            // READ
            String input = scanner.nextLine();

            // EVAL
            result = eval(input);

            // PRINT
            System.out.println(result);

            // QUIT
            if(result.equals("quit")) {
                break;
            }
        }
        System.out.println("Goodbye!");
    }

    public String eval(String input) {
        try {
            String[] args = input.split(" ");
            String command = args[0].toLowerCase();

            return switch (command) {
                case "help" -> help();
                case "quit" -> "quit";
                case "login" -> loginHandler(args);
                case "register" -> registerHandler(args);
                default -> "Unknown command. Type 'help' for a list of available commands.";
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String registerHandler(String[] args) throws ResponseException {
        if(args.length != 4) {
            return "Error: invalid command. Use: register <USERNAME> <PASSWORD> <EMAIL>";
        }

        String username = args[1];
        String password = args[2];
        String email = args[3];

        UserData userData = new UserData(username, password, email);
        AuthData authData = server.register(userData);

        isLoggedIn = true;
        authToken = authData.authToken();

        return "Logged in as " + username;
    }

    public String loginHandler(String[] args) throws ResponseException {
        if(args.length != 3) {
            return "Error: invalid command. Use:  login <USERNAME> <PASSWORD>";
        }

        String username = args[1];
        String password = args[2];

        UserData userData = new UserData(username, password, null);
        AuthData authData = server.login(userData);

        isLoggedIn = true;
        authToken = authData.authToken();

        return "Logged in as " + username;
    }














    public String help() {
        if(!isLoggedIn) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> : to create an account
                    login <USERNAME> <PASSWORD> : to play chess
                    quit : playing chess
                    help : with possible commands
                    """;
        } else {
            return """
                  create <GAME_NAME> - a game
                  list - games
                  join <GAME_ID> [WHITE|BLACK] - a game
                  observe <GAME_ID> - a game
                  logout - when you are done
                  quit - playing chess
                  help - with possible commands
            """;
        }
    }




















    private void printPrompt() {
        if(!isLoggedIn) {
            System.out.print("[LOGGED_OUT] >>> ");
        } else {
            System.out.print("[LOGGED_IN] >>> ");
        }
    }

}
