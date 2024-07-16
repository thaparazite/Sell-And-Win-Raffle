package raffle.utils;

import javafx.scene.control.Alert;
import raffle.models.Item;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemDataReaderAndWriter {

   public static List<Item> readItemsFromFile(Path filePath) throws IOException {
      List<Item> items = new ArrayList<>();
      List<String> lines = Files.readAllLines(filePath);

      // Skip the header line
      for (int i = 1; i < lines.size(); i++) {
         String line = lines.get(i);
         String[] fields = parseCSVLine(line);
         if (fields.length >= 5) {
            String image = fields[0];
            String title = fields[1].replace("\"", "");
            String description = fields[2].replace("\"", "");
            int tickets = Integer.parseInt(fields[3]);
            double price = Double.parseDouble(fields[4]);

            items.add(new Item(image, title, description, tickets, price));
         }// end of if statement
      }// end of for loop
      return items;
   }// end of readItemsFromFile method

   // Parse a line of CSV file
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
               inQuotes = ! inQuotes;
               break;
            default:
               sb.append(c);
               break;
         }// end of switch
      }// end of for loop
      tokens.add(sb.toString());

      return tokens.toArray(new String[0]);
   }// end of parseCSVLine method

   // Write the items to the CSV file
   public static void writeItemsToCSV(List<Item> items, Path filePath) {
      items.sort(Comparator.comparing(Item::getTitle));
      // Write the items to the CSV file
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
         bw.write("Image, Title, Description, Available Tickets, Ticket Price");
         bw.newLine();
         for (Item item : items) {
            String line = String.join(",",
                                      item.getImage(),
                                      "\"" + item.getTitle() + "\"",
                                      "\"" + item.getDescription() + "\"",
                                      String.valueOf(item.getTickets()),
                                      String.valueOf(item.getPrice()));
            bw.write(line);
            bw.newLine();
         }// end of for loop
      } catch (IOException e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while writing to the file !");
      }// end of try-catch block
   }// end of writeItemsToCSV method

   // Method to show an alert dialog
   private static void showAlert(Alert.AlertType alertType, String title, String message) {
         Alert alert = new Alert(alertType);
         alert.setTitle(title);
         alert.setHeaderText(null);
         alert.setContentText(message);
         alert.showAndWait();
   }// end of showAlert method

}// end of ItemDataReaderAndWriter class
