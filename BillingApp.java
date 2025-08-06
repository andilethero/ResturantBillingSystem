import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.util.*;

public class BillingApp extends Application {
    private ListView<MenuItem> menuList;
    private TextField quantityField;
    private TextArea billArea;
    private Label subtotalLabel, vatLabel, totalLabel;
    private Bill bill = new Bill();
    private Stage primaryStage;
    private String currentUser;

    private final File usersFile = new File("users.txt");

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        loginBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        registerBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        loginBtn.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if (authenticateUser(user, pass)) {
                currentUser = user;
                showBillingScreen();
            } else {
                showAlert("Invalid credentials. Try again or register.");
            }
        });

        registerBtn.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if (registerUser(user, pass)) {
                showAlert("Registration successful. You can now log in.");
            } else {
                showAlert("User already exists.");
            }
        });

        VBox loginBox = new VBox(10, userLabel, userField, passLabel, passField, loginBtn, registerBtn);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: #f8f9fa;");

        Scene loginScene = new Scene(loginBox, 300, 250);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Cashier Login");
        primaryStage.show();
    }

    private boolean authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private boolean registerUser(String username, String password) {
        try {
            if (!usersFile.exists()) usersFile.createNewFile();

            BufferedReader reader = new BufferedReader(new FileReader(usersFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0].equals(username)) {
                    return false;
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile, true));
            writer.write(username + ":" + password + "\n");
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void showBillingScreen() {
        menuList = new ListView<>();
        menuList.getItems().addAll(
                new MenuItem("Burger", 45.00),
                new MenuItem("Chips", 20.00),
                new MenuItem("Steak", 85.00),
                new MenuItem("Drink", 15.00)
        );

        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");

        Button addButton = new Button("Add to Bill");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        addButton.setOnAction(e -> addItemToBill());

        Button saveButton = new Button("Save Receipt");
        saveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        saveButton.setOnAction(e -> saveReceipt());

        Button payButton = new Button("Pay Now");
        payButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        payButton.setOnAction(e -> processPayment());

        billArea = new TextArea();
        billArea.setEditable(false);
        billArea.setPrefHeight(200);

        subtotalLabel = new Label("Subtotal: $0.00");
        vatLabel = new Label("VAT (12%): $0.00");
        totalLabel = new Label("Total: $0.00");
        Font labelFont = Font.font("Arial", FontWeight.BOLD, 14);
        subtotalLabel.setFont(labelFont);
        vatLabel.setFont(labelFont);
        totalLabel.setFont(labelFont);

        VBox inputBox = new VBox(10,
                new Label("Menu"),
                menuList,
                quantityField,
                addButton,
                saveButton,
                payButton
        );
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ced4da; -fx-border-radius: 5px;");

        VBox billBox = new VBox(10,
                new Label("Bill Summary"),
                billArea,
                subtotalLabel,
                vatLabel,
                totalLabel,
                new Label("Previous Receipts:"),
                loadPreviousReceipts()
        );
        billBox.setPadding(new Insets(10));
        billBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-border-radius: 5px;");

        HBox root = new HBox(20, inputBox, billBox);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #e9ecef;");

        Scene scene = new Scene(root, 800, 450, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Restaurant Billing System");
        primaryStage.show();
    }

    private void addItemToBill() {
        MenuItem selectedItem = menuList.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText();

        if (selectedItem == null || quantityText.isEmpty()) {
            showAlert("Please select an item and enter quantity.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Quantity must be positive.");
                return;
            }

            OrderItem orderItem = new OrderItem(selectedItem, quantity);
            bill.addOrderItem(orderItem);
            updateBillDisplay();
            quantityField.clear();
        } catch (NumberFormatException e) {
            showAlert("Invalid quantity. Enter a number.");
        }
    }

    private void updateBillDisplay() {
        List<OrderItem> items = bill.getOrderItems();
        StringBuilder sb = new StringBuilder();

        for (OrderItem item : items) {
            sb.append(item).append("\n");
        }

        billArea.setText(sb.toString());
        subtotalLabel.setText(String.format("Subtotal: $%.2f", bill.getSubtotal()));
        vatLabel.setText(String.format("VAT (12%%): $%.2f", bill.getVAT()));
        totalLabel.setText(String.format("Total: $%.2f", bill.getTotal()));
    }

    private void saveReceipt() {
        File file = new File("receipts_" + currentUser + ".txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("---- Receipt ----\n");
            for (OrderItem item : bill.getOrderItems()) {
                writer.write(item.toString() + "\n");
            }
            writer.write(String.format("Subtotal: $%.2f\n", bill.getSubtotal()));
            writer.write(String.format("VAT (12%%): $%.2f\n", bill.getVAT()));
            writer.write(String.format("Total: $%.2f\n", bill.getTotal()));
            writer.write("------------------\n");
            showAlert("Receipt saved for user: " + currentUser);
        } catch (IOException e) {
            showAlert("Error saving receipt: " + e.getMessage());
        }
    }

    private TextArea loadPreviousReceipts() {
        TextArea area = new TextArea();
        area.setEditable(false);
        File file = new File("receipts_" + currentUser + ".txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                area.setText(reader.lines().reduce("", (a, b) -> a + b + "\n"));
            } catch (IOException e) {
                area.setText("Error loading receipts.");
            }
        } else {
            area.setText("No receipts found for user.");
        }
        return area;
    }

    private void processPayment() {
        double total = bill.getTotal();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Payment");
        dialog.setHeaderText("Total Due: $" + String.format("%.2f", total));
        dialog.setContentText("Enter payment amount:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double paid = Double.parseDouble(input);
                if (paid < total) {
                    showAlert("Insufficient payment. Please enter at least $" + total);
                } else {
                    double change = paid - total;
                    showAlert("Payment accepted. Change: $" + String.format("%.2f", change));
                    bill = new Bill();
                    updateBillDisplay();
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid amount.");
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
