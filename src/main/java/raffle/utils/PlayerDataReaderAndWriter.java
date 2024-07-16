package raffle.utils;

import raffle.models.Player;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerDataReaderAndWriter {

   public static List<Player> readPlayersFromFile(Path filePath) throws IOException {
      List<Player> players = new ArrayList<>();
      List<String> lines = Files.readAllLines(filePath);

      for (int i = 1; i < lines.size(); i++) {
         String line = lines.get(i);
         String[] fields = parseCSVLine(line);
         if (fields.length >= 4) {
            int id = Integer.parseInt(fields[0]);
            String name = fields[1].replace("\"", "");
            String phoneNumber = fields[2].replace("\"", "");
            int numberOfTickets = fields[3].isEmpty() ? 0 : Integer.parseInt(fields[3]);

            players.add(new Player(id, name, phoneNumber, numberOfTickets));
         }// end of if statement
      }// end of for loop
      return players;
   }// end of readPlayersFromFile method

   private static String[] parseCSVLine(String line) {
      List<String> tokens = new ArrayList<>();
      boolean inQuotes = false;
      StringBuilder sb = new StringBuilder();

      for (char c : line.toCharArray()) {
         switch (c) {
            case ',':
               if (inQuotes) {
                  sb.append(c);
               } else {
                  tokens.add(sb.toString());
                  sb = new StringBuilder();
               }
               break;
            case '"':
               inQuotes = !inQuotes;
               break;
            default:
               sb.append(c);
               break;
         }
      }
      tokens.add(sb.toString());

      return tokens.toArray(new String[0]);
   }// end of parseCSVLine method

   public static void writePlayersToCSV(List<Player> players, Path filePath) throws IOException {
      players.sort(Comparator.comparingInt(Player::getId));
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
         bw.write("ID,Name,Phone Number,Number of Tickets");
         bw.newLine();
         for (Player player : players) {
            String line = String.join(",",
                                      String.valueOf(player.getId()),
                                      "\"" + player.getName() + "\"",
                                      "\"" + player.getPhoneNumber() + "\"",
                                      String.valueOf(player.getNumberOfTickets()));
            bw.write(line);
            bw.newLine();
         }// end of for loop
      }// end of try-with-resources
   }// end of writePlayersToCSV method
}// end of PlayerDataReaderAndWriter class
