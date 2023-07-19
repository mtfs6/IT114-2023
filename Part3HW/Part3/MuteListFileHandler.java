package Module4.Part3;

import java.io.*;
import java.util.*;

public class MuteListFileHandler {

	private static final String MUTE_LIST_FILE_PATH = "muteList.txt";

    public static void writeMuteListToFile(HashMap<Long, ArrayList<Long>> muteList) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(MUTE_LIST_FILE_PATH))) {
            outputStream.writeObject(muteList);
            System.out.println("Mute list has been written to file: " + MUTE_LIST_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error writing mute list to file: " + e.getMessage());
        }
    }

    public static HashMap<Long, ArrayList<Long>> readMuteListFromFile() {
        HashMap<Long, ArrayList<Long>> muteList = new HashMap<>();
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(MUTE_LIST_FILE_PATH))) {
            muteList = (HashMap<Long, ArrayList<Long>>) inputStream.readObject();
            System.out.println("Mute list has been read from file: " + MUTE_LIST_FILE_PATH);
        } catch (FileNotFoundException e) {
            System.err.println("Mute list file not found. Creating a new one.");
            // If the file is not found, return an empty HashMap
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading mute list from file: " + e.getMessage());
        }
        return muteList;
    }
}
