package raffle.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import raffle.main.App;
import raffle.models.Item;
import raffle.utils.ItemDataReaderAndWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainViewController {

   @FXML
   private TableView<Item> itemTable;
   @FXML
   private TableColumn<Item, String> imageColumn;
   @FXML
   private TableColumn<Item, String> titleColumn;
   @FXML
   private TableColumn<Item, Integer> ticketsColumn;
   @FXML
   private TableColumn<Item, Double> priceColumn;
   @FXML
   private Button addItemButton;
   @FXML
   private Button deleteItemButton;
   @FXML
   private Button drawButton;
   @FXML
   private Button buyTicketsButton;
   @FXML
   private Button viewItemButton;
   @FXML
   private Button refreshListButton;

   private App app;
   private ObservableList<Item> itemList;

   @FXML
   private void initialize() {
      imageColumn.setCellValueFactory(cellData -> cellData.getValue().imageProperty());
      imageColumn.setCellFactory(new Callback<>() {
         @Override
         public TableCell<Item, String> call(TableColumn<Item, String> param) {
            return new TableCell<>() {
               private final ImageView imageView = new ImageView();

               @Override
               protected void updateItem(String imagePath, boolean empty) {
                  super.updateItem(imagePath, empty);
                  if (empty || imagePath == null) {
                     setGraphic(null);
                  } else {
                     try {
                        Image image = new Image(Paths.get(imagePath).toUri().toString(), 250, 250, true, true);
                        imageView.setImage(image);
                        imageView.setFitWidth(250);
                        imageView.setFitHeight(250);
                        imageView.setPreserveRatio(true);
                        StackPane imagePane = new StackPane(imageView);
                        imagePane.setPrefSize(250, 250);
                        setGraphic(imagePane);
                     } catch (Exception e) {
                        setGraphic(null);
                        System.err.println("Invalid image path: " + imagePath);
                     }// end of try-catch block
                  }// end of if-else block
               }// end of updateItem method
            };// end of TableCell method
         }// end of call method
      });// end of imageColumn.setCellFactory method

      titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
      titleColumn.setCellFactory(tc -> new TableCell<>() {
         @Override
         protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
               setText(null);
            } else {
               setText(item);
               setStyle("-fx-alignment: CENTER; -fx-font-size: 30px; -fx-font-weight: bold;");
            }// end of if-else block
         }// end of updateItem method
      });// end of titleColumn.setCellFactory method

      ticketsColumn.setCellValueFactory(cellData -> cellData.getValue().ticketsProperty().asObject());
      ticketsColumn.setCellFactory(tc -> {
         return new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
               super.updateItem(item, empty);
               if (empty) {
                  setText(null);
               } else {
                  setText(item.toString());
                  setStyle("-fx-alignment: CENTER; -fx-font-size: 30px; -fx-font-weight: bold;");
               }// end of if-else block
            }// end of updateItem method
         };// end of updateItem method
      });// end of ticketsColumn.setCellFactory method

      priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
      priceColumn.setCellFactory(tc -> new TableCell<>() {
         @Override
         protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
               setText(null);
            } else {
               setText("\u20AC" + item.toString());
               setStyle("-fx-alignment: CENTER; -fx-font-size: 30px; -fx-font-weight: bold;");
            }//end of if-else block
         }//end of updateItem method
      });// end of priceColumn.setCellFactory method

      // Add tooltips to the buttons
      addTooltip(addItemButton, "Add a New Item to the Raffle List");
      addTooltip(deleteItemButton, "Delete the Selected Item from the Raffle List");
      addTooltip(drawButton, "Draw a winner");
      addTooltip(refreshListButton, "Refresh the List of Items");
      addTooltip(buyTicketsButton, "Buy Tickets for the Selected Item");
      addTooltip(viewItemButton, "View Images and Description of the Selected Item");

      loadItemsFromCSV();

      Platform.runLater(() -> {
         Stage stage = (Stage) itemTable.getScene().getWindow();

         // Bind column widths to the table's width
         itemTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            imageColumn.setPrefWidth(tableWidth * 0.33);// 33% of table width
            titleColumn.setPrefWidth(tableWidth * 0.37);// 37% of table width
            ticketsColumn.setPrefWidth(tableWidth * 0.15);// 15% of table width
            priceColumn.setPrefWidth(tableWidth * 0.15);// 15% of table width
         });// end of itemTable.widthProperty method

         // Ensure the table resizes with the window
         stage.widthProperty().addListener((obs, oldVal, newVal) -> itemTable.setPrefWidth(newVal.doubleValue()));
         stage.heightProperty().addListener((obs, oldVal, newVal) -> itemTable.setPrefHeight(newVal.doubleValue()));
      });
   }// end of initialize method

   @FXML
   private void handleBuyTickets() {
      // Check if the list is null or empty for initialization
      if (itemList == null || itemList.isEmpty()) {
         showAlert(Alert.AlertType.INFORMATION, "Initialization Needed", "Please add items to initialize the application !");
         return;
      }//end of if block

      // Check if there is a selected item
      Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
         showAlert(Alert.AlertType.INFORMATION, "No Selection", "Select an item to buy tickets for !");
         return;
      }

      try {
         app.showAddPlayerView(selectedItem);
      } catch (Exception e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while trying to open the AddPlayer window !");
      }//end of try-catch block
   }// end of handleBuyTickets method

   // Load items from the data.csv file
   private void loadItemsFromCSV() {
      String userHome = System.getProperty("user.home");
      Path dataFilePath = Paths.get(userHome, "Sell & Win Raffle", "data", "data.csv");

      if (Files.exists(dataFilePath)) {
         try {
            List<Item> items = ItemDataReaderAndWriter.readItemsFromFile(dataFilePath);
            itemList = FXCollections.observableArrayList(items);
            itemTable.setItems(itemList);
         } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while loading the data.csv file !");
         } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while parsing the data !");
         }
      } else {
         showAlert(Alert.AlertType.INFORMATION, "Initialization Needed", "Please add items to initialize the application !");
      }
   }// end of loadItemsFromCSV method

   // Handle the Add Item button
   @FXML
   private void handleAddItem() {
      app.showAddItemView();
   }// end of handleAddItem method

   // Handle the Delete Item button
   @FXML
   private void handleDeleteItem() {
      // Check if the list is null or empty for initialization
      if (itemList == null || itemList.isEmpty()) {
         showAlert(Alert.AlertType.INFORMATION, "Initialization Needed", "Please add items to initialize the application !");
         return;
      }//end of if block

      Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
         Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
         alert.setTitle("Delete Item");
         alert.setHeaderText("Are you sure you want to delete the item?");
         alert.setContentText("Choose your option:");

         ButtonType buttonTypeOne = new ButtonType("Yes");
         ButtonType buttonTypeTwo = new ButtonType("No");

         alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == buttonTypeOne) {
            itemList.remove(selectedItem);
            deleteDirectory(selectedItem.getTitle());
            ItemDataReaderAndWriter.writeItemsToCSV(itemList, Paths.get(System.getProperty("user.home"), "Sell & Win Raffle", "data", "data.csv"));
            showAlert(Alert.AlertType.CONFIRMATION, "Item Deleted", "Item deleted successfully !");
         }//end of if block
      } else {
         showAlert(Alert.AlertType.INFORMATION, "No Selection", "No item selected for deletion.");
      }//end of if-else block
   }//end of handleDeleteItem method

   // Handle the Draw button
   @FXML
   private void handleDraw() {
      // Check if the list is null or empty for initialization
      if (itemList == null || itemList.isEmpty()) {
         showAlert(Alert.AlertType.INFORMATION, "Initialization Needed", "Please add items to initialize the application !");
         return;
      }

      // Get the selected item
      Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
         showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select an item to draw.");
         return;
      }

      // Show the draw view for the selected item
      try {
         app.showDrawView(selectedItem);
      } catch (Exception e) {
         showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while trying to open the Draw window.");
      }
   }//end of handleDraw method

   @FXML
   private void handleRefreshList() {
      loadItemsFromCSV();
   }//end of handleRefreshList method

   // Add a tooltip to a control
   private void addTooltip(Control control, String text) {
      Tooltip tooltip = new Tooltip(text);
      control.setTooltip(tooltip);
   }//end of addTooltip method

   // Delete the directory with the given title
   private void deleteDirectory(String title) {
      String userHome = System.getProperty("user.home");
      Path directoryPath = Paths.get(userHome, "Sell & Win Raffle", title);
      File directory = directoryPath.toFile();
      if (directory.exists()) {
         for (File file : Objects.requireNonNull(directory.listFiles())) {
            file.delete();
         }
         directory.delete();
      }//end of if block
   }//end of deleteDirectory method

   @FXML
   private void handleViewItem(){
      // Check if the list is null or empty for initialization
      if (itemList == null || itemList.isEmpty()) {
         showAlert(Alert.AlertType.INFORMATION, "Initialization Needed", "Please add items to initialize the application !");
         return;
      }//end of if block

      Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
         app.showViewItemView(selectedItem);
      } else {
         showAlert(Alert.AlertType.INFORMATION, "No Selection", "No item selected.");
      }
   }// end of handleViewItem method

   private void showAlert(Alert.AlertType alertType, String title, String message) {
      Platform.runLater(() -> {
         Alert alert = new Alert(alertType);
         alert.setTitle(title);
         alert.setHeaderText(null);
         alert.setContentText(message);
         alert.showAndWait();
      });
   }// end of showAlert method

   public void setMainApp(App app) {
      this.app = app;
   }// end of setMainApp method

}// end of MainViewController class
