package raffle.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import raffle.models.Item;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AddItemController {

   @FXML
   private TextField titleTextField;
   @FXML
   private TextField descriptionTextField;
   @FXML
   private TextField numberOfTicketsTextField;
   @FXML
   private TextField ticketPriceTextField;
   @FXML
   private Button addDDefaultImageButton;
   @FXML
   private Button addItemButton;
   @FXML
   private Button clearFieldsButton;
   @FXML
   private ImageView itemImageView;

   private String imagePath;
   private String title;

   @FXML
   public void initialize() {
      // Add tooltips to the controls
      addTooltip(titleTextField, "Enter the title of the item");
      addTooltip(descriptionTextField, "Enter the description of the item");
      addTooltip(numberOfTicketsTextField, "Enter the number of tickets available for the item");
      addTooltip(ticketPriceTextField, "Enter the price of each ticket for the item");
      addTooltip(addDDefaultImageButton, "Click here to add an image for the item");
      addTooltip(addItemButton, "Click here to add the item to the raffle");
      addTooltip(clearFieldsButton, "Click here to clear all fields");
   }// end of initialize method

   @FXML
   private void handleAddImage() {
      // Get the title from the title text field
      String userHome = System.getProperty("user.home");
      Path appDirectoryPath = Paths.get(userHome, "Sell & Win Raffle");
      Path dataFilePath = appDirectoryPath.resolve("data/data.csv");

      // Create a file chooser dialog
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Image");
      fileChooser.getExtensionFilters().addAll(
              new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff", "*.webp")
      );

      // Set initial directory to the app's directory
      fileChooser.setInitialDirectory(appDirectoryPath.toFile());

      // Show the file chooser dialog
      Stage stage = (Stage) addDDefaultImageButton.getScene().getWindow();
      File selectedFile = fileChooser.showOpenDialog(stage);

      if (selectedFile != null) {
         // Extract the title from the path
         Path relativePath = appDirectoryPath.relativize(selectedFile.toPath());
         String[] pathParts = relativePath.toString().split(Pattern.quote(File.separator));
         if (pathParts.length > 1) {
            title = pathParts[0];
         }// end of if statement

         // Check if the selected directory matches the title
         if (! selectedFile.getParentFile().getName().equals(title)) {
            showAlert(Alert.AlertType.ERROR, "Error", "The selected image is not in the correct directory !");
            return;
         }// end of if statement

         imagePath = selectedFile.getAbsolutePath();
         Image image = new Image(selectedFile.toURI().toString());
         itemImageView.setImage(image);

         // Read the data.csv and match titles
         try {
            List<String> existingItems = Files.readAllLines(dataFilePath);
            boolean updated = false;
            for (int i = 0; i < existingItems.size(); i++) {
               String[] fields = existingItems.get(i).split(",");
               if (fields.length > 1 && fields[1].replace("\"", "").equals(title)) {
                  fields[0] = imagePath;  // Update the image path
                  existingItems.set(i, String.join(",", fields));
                  updated = true;
                  break;
               }// end of if statement
            }// end of for loop
            if (updated) {
               Files.write(dataFilePath, existingItems);
               showAlert(Alert.AlertType.CONFIRMATION, "Image Updated", "The default image has been updated successfully !");
            } else {
               showAlert(Alert.AlertType.ERROR, "Error", "The image path could not be updated !");
            }// end of if-else block
         } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the data.csv file !");
         }// end of try-catch block
      }// end of if statement
   }// end of handleAddImage method

   @FXML
   private void handleAddItem() {
      title = titleTextField.getText();// get the text from the titleTextField
      title = titleTextField.getText().trim();// remove leading and trailing whitespaces
      title = title.replaceAll("[<>:\"/\\\\|?*]", "_");// replace invalid characters with underscore
      String description = descriptionTextField.getText();
      String numberOfTicketsText = numberOfTicketsTextField.getText();
      String ticketPriceText = ticketPriceTextField.getText();

      // Validate inputs
      if (title.isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Title field cannot be empty !");
         return;
      }

      if (description.isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Description field cannot be empty !");
         return;
      }

      if (numberOfTicketsText.isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Number of tickets field cannot be empty !");
         return;
      }

      if (ticketPriceText.isEmpty()) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Ticket price field cannot be empty !");
         return;
      }

      // Validate the number of tickets
      int numberOfTickets;
      try {
         numberOfTickets = Integer.parseInt(numberOfTicketsText);
         if (numberOfTickets <= 0) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Number of tickets must be a positive integer !");
            return;
         }
      } catch (NumberFormatException e) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Enter a valid number for the number of tickets !");
         return;
      }// end of try-catch block

      // Validate the ticket price
      double ticketPrice;
      try {
         ticketPrice = Double.parseDouble(ticketPriceText);
         if (ticketPrice <= 0) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Ticket price must be a positive number !");
            return;
         }
      } catch (NumberFormatException e) {
         showAlert(Alert.AlertType.WARNING, "Input Error", "Enter a valid number for the ticket price !");
         return;
      }// end of try-catch block

      String directoryName = title;
      String userHome = System.getProperty("user.home");
      String directoryPath = Paths.get(userHome, "Sell & Win Raffle", directoryName).toString();
      Path recordsPath = Paths.get(userHome, "Sell & Win Raffle", "records");
      Path csvFilePath = recordsPath.resolve(directoryName + ".csv");

      // Check if directory already exists
      Path path = Paths.get(directoryPath);
      if (Files.exists(path)) {
         showAlert(Alert.AlertType.WARNING, "Directory already exists !", "A directory for " + title + " already exists !");
         return;
      }

      // Check if CSV file already exists
      if (Files.exists(csvFilePath)) {
         showAlert(Alert.AlertType.WARNING, "CSV file already exists !", "A record for " + title + " already exists !\n\n" +
                 "Please delete the existing record before adding a new one.\n\n" +
                 "Check path:\n\n" + recordsPath);
         return;
      }

      // Create the directory and the records directory
      try {
         Files.createDirectories(path);
         Files.createDirectories(recordsPath);

         // Generate the CSV content
         List<String> csvContent = generateCSVContent(numberOfTickets);
         Files.write(csvFilePath, csvContent);

         // Create a new item
         Item newItem = new Item(imagePath, title, description, numberOfTickets, ticketPrice);

         // Prepare the item data
         String itemData = String.join(",",
                                       newItem.getImage(),
                                       "\"" + newItem.getTitle() + "\"",
                                       "\"" + newItem.getDescription() + "\"",  // Add double quotes around the description
                                       String.valueOf(newItem.getTickets()),
                                       String.valueOf(newItem.getPrice()));

         // Create the data directory and the data.csv file
         Path dataDirectoryPath = Paths.get(userHome, "Sell & Win Raffle", "data");
         Path dataFilePath = dataDirectoryPath.resolve("data.csv");

         // Create the data directory
         Files.createDirectories(dataDirectoryPath);

         // Check if the data.csv file exists
         if (Files.exists(dataFilePath)) {
            // Read the existing items from the file
            List<String> existingItems = Files.readAllLines(dataFilePath);

            // Add the new item to the list
            existingItems.add(itemData);

            // Write the updated list back to the file
            Files.write(dataFilePath, existingItems);
         } else {
            // Write the header and the item data to the data.csv file
            List<String> lines = new ArrayList<>();
            lines.add("Image, Title, Description, Available Tickets, Ticket Price");
            lines.add(itemData);
            Files.write(dataFilePath, lines);
         }// end of if-else block
      } catch (IOException e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while creating the directory or file !");
      }// end of try-catch block

      // Show a confirmation message
      showAlert(Alert.AlertType.CONFIRMATION, "Item Added", title + " has been added successfully !\n"
              + "Please manually add images to the following path:\n\n" + path
              + "\n\nbefore selecting the default image view !");

   }// end of handleAddItem method

   // Generate the CSV content for the item
   private List<String> generateCSVContent(int numberOfTickets) {
      List<String> content = IntStream.rangeClosed(1, numberOfTickets)
                                      .mapToObj(i -> i + ",,,")
                                      .collect(Collectors.toList());
      content.addFirst("ID, Name, Phone Number, Purchased Tickets");
      return content;
   }// end of generateCSVContent method

   // Show an alert dialog
   private void showAlert(Alert.AlertType alertType, String title, String message) {
      Alert alert = new Alert(alertType);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
   }// end of showAlert method

   // Clear all the fields
   @FXML
   private void handleClearFields() {
      titleTextField.clear();
      descriptionTextField.clear();
      numberOfTicketsTextField.clear();
      ticketPriceTextField.clear();
      itemImageView.setImage(null);
      imagePath = null;
   }// end of handleClearFields method

   // Add a tooltip to a control
   private void addTooltip(Control control, String text) {
      Tooltip tooltip = new Tooltip(text);
      control.setTooltip(tooltip);
   }// end of addTooltip method

}// end of AddItemController class
