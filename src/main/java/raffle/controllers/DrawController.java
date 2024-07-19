package raffle.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import raffle.models.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrawController {

   @FXML
   private Label generatedNumber;

   @FXML
   private Label winerLabel;

   @FXML
   private Button checkPlayerStatus;

   @FXML
   private Button startStopButton;

   private boolean isRunning = false;
   private final Random random = new Random();
   private int lastNumber;
   private Thread numberGeneratorThread;
   private String itemTitle;

   @FXML
   private void initialize() {
      addTooltip(checkPlayerStatus, "Check Player's Status");
      addTooltip(startStopButton, "Start the Number Generation.\nStop the Number Generation to Pick a Winner");
      addTooltip(generatedNumber, "Last Generated Number");
      addTooltip(winerLabel, "Winner of the Draw");
   }// end of initialize method

   @FXML
   private void handleStartStop() {
      if (! isRunning) {
         isRunning = true;
         startNumberGeneration();
         startStopButton.setText("Stop");
         startStopButton.getTooltip().setText("Stop the number generation and pick a winner !");
      } else {
         isRunning = false;
         stopNumberGeneration();
         startStopButton.setText("Start");
         startStopButton.getTooltip().setText("Start the number generation !");
      }// end of if block
   }// end of handleStartStop method

   // Method to retrieve unique IDs from the CSV file
   private List<Integer> retrieveUniqueIdsFromCSV() {
      // Check if the item title is set
      if (itemTitle == null) {
         showAlert(Alert.AlertType.WARNING, "Warning", "Item title is null. Please set the item title before retrieving IDs.");
         return new ArrayList<>(); // Return an empty list since itemTitle is not set
      }// end of if block

      // Get the path to the records directory and the data file
      String userHome = System.getProperty("user.home");
      Path recordsDirectory = Paths.get(userHome, "Sell & Win Raffle", "records");
      Path dataFilePath = recordsDirectory.resolve(itemTitle + ".csv");

      List<Integer> uniqueIds = new ArrayList<>();
      // Read the file and extract the IDs
      try (Stream<String> lines = Files.lines(dataFilePath)) {
         uniqueIds = lines
                 .skip(1) // Skip header
                 .map(line -> Integer.parseInt(line.split(",")[0])) // Extract ID and convert to Integer
                 .distinct() // Ensure IDs are unique
                 .collect(Collectors.toList());
      } catch (IOException e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while reading the file !");
      }// end of try-catch block

      return uniqueIds;
   }// end of retrieveUniqueIdsFromCSV method

   // Method to start the number generation
   private void startNumberGeneration() {
      // Check if the thread is running and interrupt it
      if (numberGeneratorThread != null && numberGeneratorThread.isAlive()) {
         numberGeneratorThread.interrupt();
      }// end of if block

      // Create a new thread to generate numbers
      numberGeneratorThread = new Thread(() -> {
         while (isRunning) {
            lastNumber = random.nextInt(retrieveUniqueIdsFromCSV().size()) + 1; // Generate a random number from 1 to 1000
            Platform.runLater(() -> generatedNumber.setText(String.valueOf(lastNumber)));
            try {
               Thread.sleep(100); // Pause for 100 milliseconds
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         }
      });
      numberGeneratorThread.setDaemon(true);
      numberGeneratorThread.start();
   }// end of startNumberGeneration method

   // Method to stop the number generation
   private void stopNumberGeneration() {
      isRunning = false;
      if (numberGeneratorThread != null) {
         try {
            numberGeneratorThread.join(); // Ensure the thread stops
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }// end of try-catch block
      }// end of if block

      // Display the last generated number
      Platform.runLater(() -> generatedNumber.setText(String.valueOf(lastNumber))); // Display the last generated number
      getPlayerNameById(lastNumber); // Get the player name for the last generated number

      // Display the winner label if the player name is found for the last generated number
      if (! getPlayerNameById(lastNumber).isEmpty()) {
         // Display the winner label
         winerLabel.setText("*** Winner ***" + "\n" + getPlayerNameById(lastNumber) + "\nID: " + lastNumber);
      } else {
         winerLabel.setText("ID: " + lastNumber + "\nTicket Not Sold !");
      }// end of if block
   }// end of stopNumberGeneration method

   // Method to get the player name by ID
   private String getPlayerNameById(int searchId) {
      // Check if the item title is set
      if (itemTitle == null) {
         showAlert(Alert.AlertType.ERROR, "Error", "Item title is null. Please set the item title before searching.");
         return "Item title not set";
      }// end of if block

      // Get the path to the records directory and the data file
      String userHome = System.getProperty("user.home");
      Path recordsDirectory = Paths.get(userHome, "Sell & Win Raffle", "records");
      Path dataFilePath = recordsDirectory.resolve(itemTitle + ".csv");

      String playerName = "ID not found";

      // Read the file and search for the ID
      try (Stream<String> lines = Files.lines(dataFilePath)) {
         Optional<String> matchingLine = lines
                 .skip(1) // Skip header
                 .filter(line -> Integer.parseInt(line.split(",")[0]) == searchId)
                 .findFirst();
         // Extract the player name from the matching line
         if (matchingLine.isPresent()) {
            String[] parts = matchingLine.get().split(",");
            playerName = parts.length > 1 ? parts[1] : "Name not available !"; // Assuming the name is in the second column
         } else {
            showAlert(Alert.AlertType.INFORMATION, "Error", "Player not found !");
         }// end of if block
      } catch (IOException e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while reading the file !");
      }// end of try-catch block
      return playerName.trim().replace("\"", "");
   }// end of getPlayerNameById method

   // Method to check the player status
   @FXML
   private void handleCheckPlayerStatus() throws Exception {
      // Get the current window and hide it
      Stage stage = (Stage) checkPlayerStatus.getScene().getWindow();
      stage.hide();

      // Load the FXML file for the Player Status window
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/player-status-view.fxml"));

      // Create a new scene with the loaded FXML file
      Scene scene = new Scene(loader.load());

      // Create a new stage for the Player Status window
      Stage newStage = new Stage();
      newStage.setTitle("Player Status");
      newStage.setScene(scene);

      // Get the controller for the Player Status window and set the main app
      PlayerStatusController controller = loader.getController();
      controller.setItem(itemTitle);

      // Show the Player Status window and show the Draw window when it's closed
      newStage.showAndWait();

      // Show the Draw window
      stage.show();
   }// end of handleCheckPlayerStatus method

   // Method to add a tooltip to a control
   private void addTooltip(Control control, String text) {
      Tooltip tooltip = new Tooltip(text);
      control.setTooltip(tooltip);
   }// end of addTooltip method

   // Method to show an alert
   private void showAlert(Alert.AlertType alertType, String title, String message) {
      Alert alert = new Alert(alertType);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }// end of showAlert method

   // Set the item in the controller
   public void setItem(Item selectedItem) {
      if (selectedItem != null && selectedItem.getTitle() != null) {
         this.itemTitle = selectedItem.getTitle();
         // Update the count and display it as soon as the item is set
         lastNumber = retrieveUniqueIdsFromCSV().size();
         Platform.runLater(() -> generatedNumber.setText(String.valueOf(lastNumber)));
      } else {
         showAlert(Alert.AlertType.ERROR, "Error", "Item title is null. Please set the item title before setting the item !");
      }// end of if block
   }// end of setItem method

}// end of DrawController class
