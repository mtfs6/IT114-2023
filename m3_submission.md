package NumberGuesser4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class NumberGuesser4 {
    private int maxLevel = 1;
    private int level = 1;
    private int strikes = 0;
    private int maxStrikes = 5;
    private int number = -1;
    private boolean pickNewRandom = true;
    private Random random = new Random();
    private String fileName = "ng4.txt";
    private String[] fileHeaders = { "Level", "Strikes", "Number", "MaxLevel", "Hash" };// used for demo readability
    private boolean usedHint = false; // 4.5 solution
    private String username = ""; // 4.6 solution

    private void saveState() {
        String[] data = { level + "", strikes + "", number + "", maxLevel + "" };

        // 4.2 Implement anti-data tampering of the save file data (basic example)
        String dataString = String.join(",", data).hashCode() + "";
        // System.out.println("Saving hashcode " + dataString);
        String output = String.join(",", data) + "," + dataString;
        // end 4.2

        // Note: we don't need a file reference as FileWriter creates the file if it
        // doesn't exist
        try (FileWriter fw = new FileWriter(username + fileName)) {
            fw.write(String.join(",", fileHeaders));
            fw.write("\n");// new line
            fw.write(output);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadState() {
        File file = new File(username + fileName);
        if (!file.exists()) {
            // Not providing output here as it's expected for a fresh start
            return;
        }
        try (Scanner reader = new Scanner(file)) {
            int lineNumber = 0;
            while (reader.hasNextLine()) {
                String text = reader.nextLine();
                // System.out.println("Text: " + text);
                if (lineNumber == 1) {
                    String[] data = text.split(",");
                    String level = data[0];
                    String strikes = data[1];
                    String number = data[2];
                    String maxLevel = data[3];
                    // 4.2 Implement anti-data tampering of the save file data (basic example)
                    if (data.length < 5) {
                        return;
                    }
                    String hash = data[4];
                    String dataString = String.format("%s,%s,%s,%s", data[0], data[1], data[2], data[3]).hashCode()
                            + "";
                    // System.out.println("Loaded hash " + hash);
                    // System.out.println("Calculated hash " + dataString);
                    if (!hash.equals(dataString)) {
                        System.out.println("File tampered with, rejecting...");
                        return;
                    }
                    // end 4.2

                    int temp = strToNum(level);
                    if (temp > -1) {
                        this.level = temp;
                    }
                    temp = strToNum(strikes);
                    if (temp > -1) {
                        this.strikes = temp;
                    }
                    temp = strToNum(number);
                    if (temp > -1) {
                        this.number = temp;
                        pickNewRandom = false;
                    }
                    temp = strToNum(maxLevel);
                    if (temp > -1) {
                        this.maxLevel = temp;
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {// specific exception
            e.printStackTrace();
        } catch (Exception e2) {// any other unhandled exception
            e2.printStackTrace();
        }
        System.out.println("Loaded state");
        int range = 10 + ((level - 1) * 5);
        System.out.println("Welcome to level " + level);
        System.out.println(
                "I picked a random number between 1-" + (range) + ", let's see if you can guess.");
    }

    /***
     * Gets a random number between 1 and level.
     * 
     * @param level (level to use as upper bounds)
     * @return number between bounds
     */
    private void generateNewNumber(int level) {
        int range = 10 + ((level - 1) * 5);
        System.out.println("Welcome to level " + level);
        System.out.println(
                "I picked a random number between 1-" + (range) + ", let's see if you can guess.");
        number = random.nextInt(range) + 1;
    }

    private void win() {
        System.out.println("That's right!");
        level++;// level up!
        strikes = 0;
        usedHint = false; // 4.5 solution
    }

    private boolean processCommands(String message) {
        boolean processed = false;
        if (message.equalsIgnoreCase("quit")) {
            System.out.println("Tired of playing? No problem, see you next time.");
            processed = true;
        }
        // 4.5 add a hint command that cna be used once per level and only after 2
        // strikes have been used that reduces the guess range
        else if (message.equalsIgnoreCase("hint")) {
            if (usedHint) {
                System.out.println("Hint already used this level");
            } else if (strikes >= 2) {
                usedHint = true;
                // attempt to show a number 3 increments away to the left or 0
                int min = Math.max(number - 3, 0);
                int range = 10 + ((level - 1) * 5);
                // attempt to show a number 3 increments away to the right or the highest number
                // for this level
                int max = Math.min(number + 3, range);
                System.out.println("Hint: The number is between " + min + " and " + max);
            } else {
                System.out.println("Hint isn't eligible yet, you need to have two strikes first");
            }
        }
        // end 4.5

        // TODO add other conditions here
        return processed;
    }

    private void lose() {
        System.out.println("Uh oh, looks like you need to get some more practice.");
        System.out.println("The correct number was " + number);
        strikes = 0;
        level--;
        if (level < 1) {
            level = 1;
        }
    }

    private void processGuess(int guess) {
        if (guess < 0) {
            return;
        }
        System.out.println("You guessed " + guess);
        if (guess == number) {
            win();
            pickNewRandom = true;
        } else {
            System.out.println("That's wrong");

            // 4.1: Display higher/lower hint
            if (number < guess) {
                System.out.println("The correct number is lower than your guess");
            } else if (number > guess) {
                System.out.println("The correct number is higher than your guess");
            }
            // end 4.1

            // 4.4 display cold, warm, hot indicator
            int diff = Math.abs(number - guess);
            if (diff >= 7) {
                System.out.println("Your guess was cold");
            } else if (diff >= 5) {
                System.out.println("Your guess was warm");
            } else if (diff >= 1) {
                System.out.println("Your guess was hot");
            }
            // end 4.4

            strikes++;
            if (strikes >= maxStrikes) {
                lose();
                pickNewRandom = true;
            }
        }
    }

    private int strToNum(String message) {
        int guess = -1;
        try {
            guess = Integer.parseInt(message.trim());
        } catch (NumberFormatException e) {
            System.out.println("You didn't enter a number, please try again");
        } catch (Exception e2) {
            System.out.println("Null message received");
        }
        return guess;
    }

    public void start() {
        try (Scanner input = new Scanner(System.in);) {
            System.out.println("Welcome to NumberGuesser4.0");
            System.out.println("To exit, type the word 'quit'.");
            // 4.6 Implement separate save files on a "what's your name?" prompt
            System.out.println("What's your name?");
            username = input.nextLine();
            System.out.println("Welcome, " + username);
            // do it before load so the name is used to find the loadfile
            loadState();

            // 4.3 difficulty selector
            System.out.println("Type the number for the difficulty you want to play on: 1. Easy, 2. Medium, 3. Hard");
            String diffString = input.nextLine();
            int diff = 1;
            try {
                diff = Integer.parseInt(diffString);
            } catch (Exception e) {
                System.out.println("Invalid choice, difficulty set to easy");
            }
            switch (diff) {
                case 1:
                    maxStrikes = 10;
                    break;
                case 2:
                    maxStrikes = 5;
                    break;
                case 3:
                    maxStrikes = 3;
                    break;
            }
            System.out.println("Maximum Strikes set to " + maxStrikes);
            // end 4.3
            do {
                if (pickNewRandom) {
                    generateNewNumber(level);
                    saveState();
                    pickNewRandom = false;
                }
                System.out.println("Type a number and press enter");
                // we'll want to use a local variable here
                // so we can feed it into multiple functions
                String message = input.nextLine();
                // early termination check
                if (processCommands(message)) {
                    // command handled; don't proceed with game logic
                    break;
                }
                // this is just to demonstrate we can return a value and pass it into another
                // method
                int guess = strToNum(message);
                processGuess(guess);
                // the following line is the same as the above two lines
                // processGuess(getGuess(message));
            } while (true);
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Goodbye.");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        System.out.println("Thanks for playing!");
    }

    public static void main(String[] args) {
        NumberGuesser4 ng = new NumberGuesser4();
        ng.start();
    }
}
