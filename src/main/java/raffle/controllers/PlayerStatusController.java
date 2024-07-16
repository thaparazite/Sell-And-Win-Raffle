package raffle.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import raffle.models.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerStatusController {

   @FXML
   private TextField playerInput;

   @FXML
   private TextArea displayPlayerStatus;

   @FXML
   private Button checkPlayerStatus;

   private String itemTitle;

   @FXML
   private void initialize() {
      // tooltip for the checkPlayerStatus button
      addTooltip(checkPlayerStatus, "Check the player's status");
      addTooltip(playerInput, "Enter the player's name, phone number, or ID!");
      addTooltip(displayPlayerStatus, "Player's status will be displayed here!");
   }//end of initialize method

   @FXML
   private void handleCheckPlayer() {
      // Get the player input
      String input = playerInput.getText().trim();

      // Check if the input is empty
      if (input.isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Warning", "Enter a name, phone number, or ID to search!");
         return;
      }// end of if statement

      // Get the records directory and the data file path
      String userHome = System.getProperty("user.home");
      Path recordsDirectory = Paths.get(userHome, "Sell & Win Raffle", "records");
      Path dataFilePath = recordsDirectory.resolve(itemTitle + ".csv");

      // Check if the data file exists
      try (Stream<String> lines = Files.lines(dataFilePath)) {
         List<Player> matchingPlayers = lines.skip(1) // Skip the CSV header
                                             .map(line -> line.split(","))
                                             .filter(parts -> parts.length >= 4)
                                             .map(parts -> {
                                                try {
                                                   int id = Integer.parseInt(parts[0].trim());
                                                   String name = parts[1].replace("\"", "").trim();
                                                   String phone = parts[2].replace("\"", "").trim();
                                                   int tickets = Integer.parseInt(parts[3].trim());
                                                   return new Player(id, name, phone, tickets);
                                                } catch (NumberFormatException e) {
                                                   return null;
                                                }// end of try catch block
                                             })
                                             .filter(Objects::nonNull)
                                             .toList();

         // Check if the player is found
         if (matchingPlayers.isEmpty()) {
            displayPlayerStatus.setText("\n\n\tPlayer with the given name, phone number, or ID cannot be found!");
            return;
         }// end of if statement

         // Check if the input is numeric
         boolean isNumeric = input.chars().allMatch(Character::isDigit);

         // if the input is numeric and less than 10 characters
         if (isNumeric && input.length() < 10) {

            // Search by ID
            int inputId = Integer.parseInt(input);
            Player matchingPlayer = matchingPlayers.stream()
                                                   .filter(player -> player.getId() == inputId)
                                                   .findFirst()
                                                   .orElse(null);

            if (matchingPlayer == null) {
               displayPlayerStatus.setText("\n\nPlayer with the given ID cannot be found !");
            } else {
               displayPlayerStatus.setText("Player found:\n"
                                                   + "Name: " + matchingPlayer.getName() + "\n"
                                                   + "Phone Number: " + matchingPlayer.getPhoneNumber() + "\n"
                                                   + "Tickets Purchased: " + matchingPlayer.getNumberOfTickets() + "\n"
                                                   + "ID: " + matchingPlayer.getId());
            }// end of if/else block
         } else {
            Map<String, List<Player>> playersByPhoneNumber = matchingPlayers.stream()
                                                                            .collect(Collectors.groupingBy(Player::getPhoneNumber));

            // check if the map contains the input key (phone number)
            if (playersByPhoneNumber.containsKey(input)) {

               // Display players with the given phone number
               List<Player> playersWithPhone = playersByPhoneNumber.get(input);
               StringBuilder resultText = new StringBuilder();
               resultText.append("Players with phone number ").append(input).append(":\n");

               // Group players by name
               Map<String, List<Player>> playersByName = playersWithPhone.stream()
                                                                         .collect(Collectors.groupingBy(Player::getName));
               // Display the result
               for (Map.Entry<String, List<Player>> entry : playersByName.entrySet()) {
                  String name = entry.getKey();
                  List<Player> players = entry.getValue();
                  int totalTickets = players.size();
                  List<Integer> ids = players.stream().map(Player::getId).collect(Collectors.toList());

                  resultText.append("\nName: ").append(name)
                            .append("\nTickets Purchased: ").append(totalTickets)
                            .append("\nIDs: ").append(ids).append("\n");
               }// end of for loop
               // Display the result
               displayPlayerStatus.setText(resultText.toString());
            } else {
               // Display matching players by name and phone number separately
               StringBuilder resultText = new StringBuilder();
               // Group players by name and phone number
               Map<String, Map<String, List<Player>>> playersByNameAndPhone = matchingPlayers.stream()
                                                                                             .filter(player -> player.getName().equalsIgnoreCase(input))
                                                                                             .collect(Collectors.groupingBy(Player::getName, Collectors.groupingBy(Player::getPhoneNumber)));
               // Display the result
               for (Map.Entry<String, Map<String, List<Player>>> entry : playersByNameAndPhone.entrySet()) {
                  String name = entry.getKey();
                  Map<String, List<Player>> playersByPhone = entry.getValue();

                  for (Map.Entry<String, List<Player>> phoneEntry : playersByPhone.entrySet()) {
                     String phone = phoneEntry.getKey();
                     List<Player> players = phoneEntry.getValue();
                     int totalTickets = players.size();
                     List<Integer> ids = players.stream().map(Player::getId).collect(Collectors.toList());

                     resultText.append("Name: ").append(name)
                               .append("\nPhone Number: ").append(phone)
                               .append("\nTickets Purchased: ").append(totalTickets)
                               .append("\nIDs: ").append(ids).append("\n\n");
                  }// end of inner for loop
               }// end of for loop

               // Display the result
               if (resultText.isEmpty()) {
                  displayPlayerStatus.setText("\n\n\tPlayer with the given name or phone number cannot be found!");
               } else {
                  displayPlayerStatus.setText(resultText.toString());
               }// end of if block
            }// end of if block
         }// end of if block
      } catch (IOException e) {
         showAlert(Alert.AlertType.ERROR, "Error", "Failed to read the player records!");
      }// end of try catch block
   }// end of handleCheckPlayer method

   // Method to add a tooltip to a control
   private void addTooltip(Control control, String text) {
      Tooltip tooltip = new Tooltip(text);
      control.setTooltip(tooltip);
   }// end of addTooltip method

   // Set the item in the controller
   public void setItem(String itemTitle) {
      this.itemTitle = itemTitle;
   }// end of setItem method

   // Method to show an alert
   private void showAlert(Alert.AlertType alertType, String title, String message) {
      Alert alert = new Alert(alertType);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }// end of showAlert method

}// end of PlayerStatusController class
