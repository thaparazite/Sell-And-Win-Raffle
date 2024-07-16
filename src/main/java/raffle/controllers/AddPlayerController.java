package raffle.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import raffle.models.Item;
import raffle.models.Player;
import raffle.utils.ItemDataReaderAndWriter;
import raffle.utils.PlayerDataReaderAndWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AddPlayerController {

   @FXML
   private TableView<Player> playerTable;
   @FXML
   private TableColumn<Player, Integer> IDColumn;
   @FXML
   private TableColumn<Player, String> nameColumn;
   @FXML
   private TableColumn<Player, String> phoneNumberColumn;
   @FXML
   private TableColumn<Player, Integer> numberOfTicketsColumn;
   @FXML
   private TextField playerName;
   @FXML
   private TextField phoneNumber;
   @FXML
   private TextField numberOfTickets;
   @FXML
   private Label ticketsLeft;
   @FXML
   private Button addPlayerButton;
   @FXML
   private Button removePlayerButton;
   @FXML
   private Button clearFieldsButton;
   @FXML
   private Button refreshButton;

   private ObservableList<Player> playerList;
   private List<Integer> availableIDs;
   private String itemTitle;
   private Path recordsDirectory;

   @FXML
   private void initialize() {
      // Add tooltips to the controls
      addTooltip(playerName, "Enter the player's name");
      addTooltip(phoneNumber, "Enter the player's phone number");
      addTooltip(numberOfTickets, "Enter the number of tickets the player wants to buy");
      addTooltip(ticketsLeft, "Number of tickets left");
      addTooltip(addPlayerButton, "Click here to add the player");
      addTooltip(removePlayerButton, "Click here to remove the player");
      addTooltip(clearFieldsButton, "Click here to clear all fields");
      addTooltip(refreshButton, "Click here to refresh the player list");

      // Initialize the player table
      IDColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
      nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
      phoneNumberColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
      numberOfTicketsColumn.setCellValueFactory(cellData -> cellData.getValue().numberOfTicketsProperty().asObject());

      // Customize the display of the IDColumn
      IDColumn.setCellFactory(tc -> new TableCell<>() {
         @Override
         protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
               setText(null);
            } else {
               setText(item.toString());
               setStyle("-fx-alignment: CENTER; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
         }
      });// end of setCellFactory method

      // Customize the display of the nameColumn
      nameColumn.setCellFactory(tc -> new TableCell<>() {
         @Override
         protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
               setText(null);
            } else {
               setText(item);
               setStyle("-fx-alignment: CENTER; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
         }
      });

      // Customize the display of the phoneNumberColumn
      phoneNumberColumn.setCellFactory(tc -> new TableCell<>() {
         @Override
         protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
               setText(null);
            } else {
               setText(item);
               setStyle("-fx-alignment: CENTER; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
         }
      });

      // Customize the display of the numberOfTicketsColumn
      numberOfTicketsColumn.setCellFactory(tc -> new TableCell<>() {
         @Override
         protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
               setText(null);
            } else {
               setText(item.toString());
               setStyle("-fx-alignment: CENTER; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
         }
      });

      // Initialize the player list
      playerList = FXCollections.observableArrayList();
      playerTable.setItems(playerList);

      // Adjust columns dynamically with the table's width
      Platform.runLater(() -> {
         Stage stage = (Stage) playerTable.getScene().getWindow();

         playerTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            IDColumn.setPrefWidth(tableWidth * 0.10);
            nameColumn.setPrefWidth(tableWidth * 0.40);
            phoneNumberColumn.setPrefWidth(tableWidth * 0.38);
            numberOfTicketsColumn.setPrefWidth(tableWidth * 0.12);
         });// end of widthProperty method

         // Ensure the table resizes with the window
         stage.widthProperty().addListener((obs, oldVal, newVal) -> playerTable.setPrefWidth(newVal.doubleValue()));
         stage.heightProperty().addListener((obs, oldVal, newVal) -> playerTable.setPrefHeight(newVal.doubleValue()));
      });// end of Platform.runLater method
   }// end of initialize method

   // Load players from the records directory based on item title
   private void loadPlayersFromCSV() {
      String userHome = System.getProperty("user.home");
      recordsDirectory = Paths.get(userHome, "Sell & Win Raffle", "records");
      Path dataFilePath = recordsDirectory.resolve(itemTitle + ".csv");

      if (Files.exists(dataFilePath)) {
         try {
            List<Player> players = PlayerDataReaderAndWriter.readPlayersFromFile(dataFilePath);
            playerList.setAll(players);
            calculateAvailableIDs(players);
         } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while loading the " + itemTitle + ".csv file!");
         } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while parsing the data !");
         }// end of try-catch block
      } else {
         showAlert(Alert.AlertType.WARNING, "Initialization Needed", "Please add players to initialize the application !");
      }// end of if-else block
   }// end of loadPlayersFromCSV method

   // Calculate the available IDs for new players
   private void calculateAvailableIDs(List<Player> players) {
      Set<Integer> usedIDs = new HashSet<>();
      for (Player player : players) {
         if (! player.getName().isEmpty() && ! player.getPhoneNumber().isEmpty()) {
            usedIDs.add(player.getId());
         }// end of if block
      }// end of for loop

      int maxID = players.stream().mapToInt(Player::getId).max().orElse(0);
      availableIDs = IntStream.rangeClosed(1, maxID)
                              .boxed()
                              .filter(id -> ! usedIDs.contains(id))
                              .collect(Collectors.toList());

      updateTicketsLeftLabel();
   }// end of calculateAvailableIDs method

   // Update the tickets left label
   private void updateTicketsLeftLabel() {
      ticketsLeft.setText(String.valueOf(availableIDs.size()));
      updateTicketsInCSV();
   }// end of updateTicketsLeftLabel method

   // Check if the file is accessible for writing
   public boolean isFileAccessibleForWriting(Path filePath) {
      try (FileOutputStream ignored = new FileOutputStream(filePath.toFile(), true)) {
         return false;
      } catch (IOException e) {
         return true;
      }// end of try-catch block
   }// end of isFileAccessibleForWriting method

   // Update the number of tickets in the CSV file
   private void updateTicketsInCSV() {
      String userHome = System.getProperty("user.home");
      Path dataFilePath = Paths.get(userHome, "Sell & Win Raffle", "data", "data.csv");

      if (isFileAccessibleForWriting(dataFilePath)) {
         showAlert(Alert.AlertType.ERROR, "File Access Error", "The data.csv file is open in another application. Please close it and try again.");
         return;
      }// end of if block

      try {
         List<Item> items = ItemDataReaderAndWriter.readItemsFromFile(dataFilePath);
         items.stream()
              .filter(item -> item.getTitle().equals(itemTitle))
              .findFirst()
              .ifPresent(item -> item.setTickets(availableIDs.size()));
         ItemDataReaderAndWriter.writeItemsToCSV(items, dataFilePath);
      } catch (IOException e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the tickets in data.csv !");
      }// end of try-catch block
   }// end of updateTicketsInCSV method

   // Add a new player to the table
   @FXML
   private void handleAddPlayer() throws IOException {
      String userHome = System.getProperty("user.home");
      Path recordsFilePath = Paths.get(userHome, "Sell & Win Raffle", "records", itemTitle + ".csv");

      // Check if the records.csv file is accessible
      if (isFileAccessibleForWriting(recordsFilePath)) {
         showAlert(Alert.AlertType.ERROR, "File Access Error", "The " + itemTitle + ".csv file is open in another application. Please close it and try again.");
         return; // Return early, do not proceed with adding the player
      }

      // Make sure fields are not empty
      if (playerName.getText().isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Name field cannot be empty !");
         return;
      }

      if (phoneNumber.getText().isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Phone Number field cannot be empty !");
         return;
      }

      if (numberOfTickets.getText().isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Number of tickets field cannot be empty !");
         return;
      }

      // Validate the phone number input field
      try {
         int phoneNumberValue = Integer.parseInt(phoneNumber.getText());
         if (phoneNumberValue <= 0 || phoneNumber.getText().length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Enter a valid phone number format of 10 digits!");
            return;
         }
      } catch (NumberFormatException e) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Enter valid digits in Phone Number field!");
         return;
      }// end of try-catch block

      // Validate the number of tickets input field
      try {
         int numberOfTicketsValue = Integer.parseInt(numberOfTickets.getText());
         if (numberOfTicketsValue <= 0) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Number of tickets must be a positive number!");
            return;
         }
      } catch (NumberFormatException e) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Enter valid digits in Number of Tickets field!");
         return;
      }// end of try-catch block

      String name = playerName.getText().trim();
      String phone = phoneNumber.getText().trim();
      int tickets;

      // Validate the input fields
      try {
         tickets = Integer.parseInt(numberOfTickets.getText().trim());
         if (tickets <= 0) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Number of tickets must be a positive number !");
            return;
         }// end of if block
      } catch (NumberFormatException e) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Enter a valid number for the number of tickets !");
         return;
      }// end of try-catch block

      // Check if there are enough tickets available
      if (availableIDs.size() < tickets) {
         showAlert(Alert.AlertType.ERROR, "Input Error", "Not enough tickets available !");
         return;
      }// end of if block

      // Assign random IDs to the new tickets
      Random random = new Random();
      List<Integer> assignedIDs = new ArrayList<>();

      for (int i = 0; i < tickets; i++) {
         int randomIndex = random.nextInt(availableIDs.size());
         assignedIDs.add(availableIDs.remove(randomIndex));
      }// end of for loop

      // Calculate total tickets including the new ones
      int additionalTickets = tickets;
      List<Player> existingPlayers = playerList.stream()
                                               .filter(player -> player.getName().equalsIgnoreCase(name) && player.getPhoneNumber().equals(phone))
                                               .toList();

      // Add the number of tickets for existing players with the same name and phone number
      if (! existingPlayers.isEmpty()) {
         Player existingPlayer = existingPlayers.getFirst();
         additionalTickets += existingPlayer.getNumberOfTickets();
      }// end of if block

      // Update all existing entries with the same name and phone number
      int finalAdditionalTickets = additionalTickets;
      playerList.stream()
                .filter(player -> player.getName().equalsIgnoreCase(name) && player.getPhoneNumber().equals(phone))
                .forEach(player -> player.setNumberOfTickets(finalAdditionalTickets));

      // Add new entries with the same name and phone number
      assignedIDs.forEach(id -> {
         Optional<Player> existingPlayer = playerList.stream().filter(player -> player.getId() == id).findFirst();
         if (existingPlayer.isPresent()) {
            existingPlayer.get().setName(name);
            existingPlayer.get().setPhoneNumber(phone);
            existingPlayer.get().setNumberOfTickets(finalAdditionalTickets);
         } else {
            Player newPlayer = new Player(id, name, phone, finalAdditionalTickets);
            playerList.add(newPlayer);
         }// end of if-else block
      });// end of forEach loop

      // Write the updated player list to the CSV file
      PlayerDataReaderAndWriter.writePlayersToCSV(new ArrayList<>(playerList), recordsDirectory.resolve(itemTitle + ".csv"));

      // Format the assigned IDs to display 10 IDs per line in the alert dialog
      String IDs = IntStream.range(0, assignedIDs.size())
                            .mapToObj(i -> (i > 0 && i % 10 == 0) ? "\n" + assignedIDs.get(i) : assignedIDs.get(i).toString())
                            .collect(Collectors.joining(", "));

      // Show a confirmation message
      showAlert(Alert.AlertType.CONFIRMATION, "Player Added", name + " has been added with ticket IDs:\n\n" + IDs + "\n");

      updateTicketsLeftLabel();
      clearFields();
      handleRefresh();

   }// end of handleAddPlayer method

   // Remove the selected player from the table
   @FXML
   private void handleRemovePlayer() throws IOException {
      String userHome = System.getProperty("user.home");
      Path recordsFilePath = Paths.get(userHome, "Sell & Win Raffle", "records", itemTitle + ".csv");

      // Check if the records.csv file is accessible
      if (isFileAccessibleForWriting(recordsFilePath)) {
         showAlert(Alert.AlertType.ERROR, "File Access Error", "The " + itemTitle + ".csv file is open in another application. Please close it and try again.");
         return; // Return early, do not proceed with adding the player
      }

      Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
      if (selectedPlayer != null) {

         Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
         alert.setTitle("Delete Player");
         alert.setHeaderText("Are you sure you want to delete the player ?");
         alert.setContentText("Choose your option:");

         ButtonType buttonTypeOne = new ButtonType("Yes");
         ButtonType buttonTypeTwo = new ButtonType("No");

         alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == buttonTypeOne) {

            int removedID = selectedPlayer.getId();
            String name = selectedPlayer.getName();
            String phone = selectedPlayer.getPhoneNumber();

            // Find all entries with the same name and phone number
            List<Player> relatedPlayers = playerList.stream()
                                                    .filter(player -> player.getName().equalsIgnoreCase(name) && player.getPhoneNumber().equals(phone))
                                                    .toList();

            // Calculate the total number of tickets remaining after removing the selected player
            int remainingTickets = relatedPlayers.size();

            // Update the number of tickets for other entries with the same name and phone number
            relatedPlayers.forEach(player -> player.setNumberOfTickets(remainingTickets));

            // Update the selected player's entry
            selectedPlayer.setName("");
            selectedPlayer.setPhoneNumber("");
            selectedPlayer.setNumberOfTickets(0);

            availableIDs.add(removedID);
            Collections.sort(availableIDs);
            PlayerDataReaderAndWriter.writePlayersToCSV(new ArrayList<>(playerList), recordsDirectory.resolve(itemTitle + ".csv"));

            showAlert(Alert.AlertType.CONFIRMATION, "Player Removed", "Player has been removed successfully!");
            handleRefresh();
         }
      } else {
         showAlert(Alert.AlertType.INFORMATION, "No Selection", "No player selected for removal !");
      }// end of if-else block
   }// end of handleRemovePlayer method

   // Clear all input fields
   @FXML
   private void clearFields() {
      playerName.clear();
      phoneNumber.clear();
      numberOfTickets.clear();
   }// end of clearFields method

   // Refresh the player list
   @FXML
   private void handleRefresh() {
      loadPlayersFromCSV();
      playerTable.refresh();
   }// end of handleRefresh method

   // Add a tooltip to a control
   private void addTooltip(Control control, String text) {
      Tooltip tooltip = new Tooltip(text);
      control.setTooltip(tooltip);
   }// end of addTooltip method

   // Show an alert dialog
   private void showAlert(Alert.AlertType alertType, String title, String message) {
      Alert alert = new Alert(alertType);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }// end of showAlert method

   // Set the selected item
   public void setItem(Item selectedItem) {
      if (selectedItem != null) {
         this.itemTitle = selectedItem.getTitle();
         loadPlayersFromCSV();
      }// end of if block
   }// end of setItem method

}// end of AddPlayerController class
