package client;

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
                case "login" -> "implement login function";
                case "register" -> "implement register function";
                default -> "Unknown command. Type 'help' for a list of available commands.";
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
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
