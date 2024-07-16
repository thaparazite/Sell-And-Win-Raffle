package raffle.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Player {
   private final SimpleIntegerProperty id;
   private final SimpleStringProperty name;
   private final SimpleStringProperty phoneNumber;
   private final SimpleIntegerProperty numberOfTickets;

   public Player(int id, String name, String phoneNumber, int numberOfTickets) {
      this.id = new SimpleIntegerProperty(id);
      this.name = new SimpleStringProperty(name);
      this.phoneNumber = new SimpleStringProperty(phoneNumber);
      this.numberOfTickets = new SimpleIntegerProperty(numberOfTickets);
   }

   public int getId() {
      return id.get();
   }

   public SimpleIntegerProperty idProperty() {
      return id;
   }

   public String getName() {
      return name.get();
   }

   public SimpleStringProperty nameProperty() {
      return name;
   }

   public String getPhoneNumber() {
      return phoneNumber.get();
   }

   public SimpleStringProperty phoneNumberProperty() {
      return phoneNumber;
   }

   public int getNumberOfTickets() {
      return numberOfTickets.get();
   }

   public SimpleIntegerProperty numberOfTicketsProperty() {
      return numberOfTickets;
   }

   public void setName(String name) {
      this.name.set(name);
   }

   public void setPhoneNumber(String phoneNumber) {
      this.phoneNumber.set(phoneNumber);
   }

   public void setNumberOfTickets(int numberOfTickets) {
      this.numberOfTickets.set(numberOfTickets);
   }
}
