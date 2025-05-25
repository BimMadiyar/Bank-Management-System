package com.example.myjavafxapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.*;

public class EnhancedBankManagementSystem extends Application {

    private final HashMap<String, BankAccount> accounts = new HashMap<>();

    private State currentState; // Current state of the system
    private Button btnClient = new Button("Client"); // Initialize btnClient early
    private String loggedInAccountID = null; // Stores the currently logged-in account ID


    public static void main(String[] args) {
        launch(args);
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public void setClientButtonVisibility(boolean visible) {
        if (btnClient != null) {
            btnClient.setDisable(!visible);
        }
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bank Management System");
        currentState = new LoggedOutState(this);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        VBox menuBar = createMenuBar(primaryStage);
        menuBar.getStyleClass().add("menu-bar");
        root.setLeft(menuBar);

        VBox centerLayout = createCenterLayout();
        root.setCenter(centerLayout);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean authenticate(String accountNumber, String password) {
        return accounts.containsKey(accountNumber) && accounts.get(accountNumber).password.equals(password);
    }


    private void openClientLogin() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Client Login");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        TextField txtAccountNumber = new TextField();
        txtAccountNumber.setPromptText("Account ID");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");

        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnLogin.setOnAction(event -> {
            String accountNumber = txtAccountNumber.getText();
            String password = txtPassword.getText();

            if (authenticate(accountNumber, password)) {
                loggedInAccountID = accountNumber; // Store the logged-in account ID
                currentState.handleLogin(); // Update the state to LoggedInState
                loginStage.close();
            } else {
                showAlert("Error", "Invalid account number or password.", Alert.AlertType.ERROR);
            }
        });

        layout.getChildren().addAll(txtAccountNumber, txtPassword, btnLogin);
        Scene scene = new Scene(layout, 300, 200);
        loginStage.setScene(scene);
        loginStage.show();

    }



    private void openClientPanel(String accountID) {
        BankAccount currentClient = accounts.get(accountID);

        Stage clientStage = new Stage();
        clientStage.setTitle("Client Panel");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Client Operations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnDeposit = createButton("Deposit", "-fx-background-color: #28a745; -fx-text-fill: white;");
        Button btnWithdraw = createButton("Withdraw", "-fx-background-color: #ffc107; -fx-text-fill: white;");
        Button btnTransfer = createButton("Transfer", "-fx-background-color: #007bff; -fx-text-fill: white;");
        Button btnCheckBalance = createButton("Check Balance", "-fx-background-color: #17a2b8; -fx-text-fill: white;");
        Button btnBack = createButton("Back to Menu", "-fx-background-color: #6c757d; -fx-text-fill: white;");

        btnDeposit.setOnAction(e -> depositFunds(currentClient));
        btnWithdraw.setOnAction(e -> withdrawFunds(currentClient));
        btnTransfer.setOnAction(e -> transferFunds(currentClient));
        btnCheckBalance.setOnAction(e -> checkBalance(currentClient));
        btnBack.setOnAction(e -> clientStage.close());

        layout.getChildren().addAll(title, btnDeposit, btnWithdraw, btnTransfer, btnCheckBalance, btnBack);

        Scene scene = new Scene(layout, 400, 400);
        clientStage.setScene(scene);
        clientStage.show();
    }


    private void depositFunds(BankAccount currentClient) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit Funds");
        dialog.setHeaderText("Enter the amount to deposit:");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    currentClient.balance += amount;
                    showAlert("Deposit", "Successfully deposited: " + amount + "\nNew Balance: " + currentClient.balance, Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Deposit Error", "Please enter a positive amount.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Invalid amount. Please enter a valid number.", Alert.AlertType.ERROR);
            }
        });
    }

    private void withdrawFunds(BankAccount currentClient) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw Funds");
        dialog.setHeaderText("Enter the amount to withdraw:");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    if (currentClient.balance >= amount) {
                        currentClient.balance -= amount;
                        showAlert("Withdraw", "Successfully withdraw: " + amount + "\nNew Balance: " + currentClient.balance, Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Withdraw Error", "Insufficient funds. Available balance: " + currentClient.balance, Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Withdraw Error", "Please enter a positive amount.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Invalid amount. Please enter a valid number.", Alert.AlertType.ERROR);
            }
        });
    }

//************************************************* A D A P T E R    P A T T E R N *************************************************************************************//

    public interface CurrencyAdapter {
        double convert(double amount);
    }

    public static class DollarToTengeAdapter implements CurrencyAdapter {
        private static final double EXCHANGE_RATE = 500;

        @Override
        public double convert(double amount) {
            return amount * EXCHANGE_RATE;
        }
    }

    public static class TengeToDollarAdapter implements CurrencyAdapter {
        private static final double EXCHANGE_RATE = 500;

        @Override
        public double convert(double amount) {
            return amount / EXCHANGE_RATE;
        }
    }

    private void transferFunds(BankAccount senderAccount) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Transfer Funds");
        dialog.setHeaderText("Enter recipient's account ID:");
        dialog.setContentText("Account ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(recipientId -> {
            if (accounts.containsKey(recipientId)) {
                BankAccount recipientAccount = accounts.get(recipientId);

                TextInputDialog amountDialog = new TextInputDialog();
                amountDialog.setTitle("Transfer Funds");
                amountDialog.setHeaderText("Enter the amount to transfer:");
                amountDialog.setContentText("Amount:");

                Optional<String> amountResult = amountDialog.showAndWait();
                amountResult.ifPresent(amountStr -> {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount > 0) {
                            if (senderAccount.balance >= amount) {
                                double convertedAmount = amount;

                                if (!senderAccount.currency.equals(recipientAccount.currency)) {
                                    CurrencyAdapter adapter;
                                    if (senderAccount.currency.equals("USD") && recipientAccount.currency.equals("KZT")) {
                                        adapter = new DollarToTengeAdapter();
                                    } else if (senderAccount.currency.equals("KZT") && recipientAccount.currency.equals("USD")) {
                                        adapter = new TengeToDollarAdapter();
                                    } else {
                                        showAlert("Transfer Error", "Unsupported currency conversion.", Alert.AlertType.ERROR);
                                        return;
                                    }
                                    convertedAmount = adapter.convert(amount);
                                }

                                senderAccount.balance -= amount;
                                recipientAccount.balance += convertedAmount;
                                showAlert("Transfer Successful",
                                        String.format("Transferred: %.2f %s to %s (Name: %s)\nRecipient received: %.2f %s",
                                                amount, senderAccount.currency, recipientId, recipientAccount.accountHolder,
                                                convertedAmount, recipientAccount.currency),
                                        Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Transfer Error", "Insufficient funds. Available balance: " + senderAccount.balance, Alert.AlertType.ERROR);
                            }
                        } else {
                            showAlert("Transfer Error", "Please enter a positive amount.", Alert.AlertType.ERROR);
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Input Error", "Invalid amount. Please enter a valid number.", Alert.AlertType.ERROR);
                    }
                });
            } else {
                showAlert("Transfer Error", "Recipient account not found.", Alert.AlertType.ERROR);
            }
        });
    }

//***************************************************************************************************************************************************************************//

    private void checkBalance(BankAccount currentClient) {
        showAlert("Balance", "Your current balance is: " + currentClient.balance, Alert.AlertType.INFORMATION);
    }

    private VBox createMenuBar(Stage primaryStage) {
        VBox menuBar = new VBox(20);
        menuBar.setPadding(new Insets(20));
        menuBar.setStyle("-fx-background-color: #f4f4f4;");

        Label menuTitle = new Label("Menu");
        menuTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnAdmin = new Button("Admin");
        btnClient = new Button("BankOperations");
        Button btnRegister = new Button("Register New Account");
        Button btnExit = new Button("Exit");

        btnAdmin.setPrefSize(200, 50);
        btnClient.setPrefSize(200, 50);
        btnRegister.setPrefSize(200, 50);
        btnExit.setPrefSize(200, 50);

        btnAdmin.setOnAction(e -> openAdminLogin());
        btnClient.setOnAction(e -> currentState.handleClientButton());
        btnClient.setOnAction(e -> {
            if (loggedInAccountID != null) {
                openClientPanel(loggedInAccountID); // Use the stored account ID
            } else {
                showAlert("Error", "Please log in first.", Alert.AlertType.ERROR);
            }
        });

        btnRegister.setOnAction(e -> openRegistrationForm());
        btnExit.setOnAction(e -> primaryStage.close());

        MenuButton accountMenu = new MenuButton("Account");
        MenuItem loginItem = new MenuItem("Login");
        MenuItem logoutItem = new MenuItem("Logout");

        accountMenu.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-pref-width: 200px; -fx-pref-height: 40px; " +
                "-fx-padding: 5 15 5 15; -fx-border-radius: 5px;");

        loginItem.setStyle("-fx-text-fill: green; -fx-font-size: 14px; -fx-pref-width: 150px; -fx-pref-height: 30px;");
        logoutItem.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-pref-width: 150px; -fx-pref-height: 30px;");


        loginItem.setOnAction(e -> openClientLogin()); // Open login window
        logoutItem.setOnAction(e -> currentState.handleLogout());

        accountMenu.getItems().addAll(loginItem, logoutItem);

        btnClient.setDisable(true); // Initially disable the Client button

        menuBar.getChildren().addAll(menuTitle, btnAdmin, btnClient, accountMenu, btnRegister, btnExit);
        menuBar.setAlignment(Pos.TOP_CENTER);
        return menuBar;
    }


    private VBox createCenterLayout() {
        VBox centerLayout = new VBox(20);
        centerLayout.getStyleClass().add("vbox");
        centerLayout.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome to the Bank Management System");
        welcomeLabel.getStyleClass().add("label-title");

        Image image = new Image(Objects.requireNonNull(getClass().getResource("/n.jpg")).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(1200);
        imageView.setFitHeight(800);
        imageView.setPreserveRatio(true);

        centerLayout.getChildren().addAll(welcomeLabel, imageView);
        return centerLayout;
    }

    private void registerBankAccount(String accountID, BankAccount bankAccount){
        bankAccount.notifyObservers(); // Notified all observers

        accounts.put(accountID, bankAccount);
    }
    private void openRegistrationForm() {
        Stage registrationStage = new Stage();
        registrationStage.setTitle("Register New Account");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Register New Account");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtAccountNumber = new TextField();
        txtAccountNumber.setPromptText("Account ID");
        txtAccountNumber.setPrefWidth(300);

        TextField txtAccountHolder = new TextField();
        txtAccountHolder.setPromptText("Account Holder Name");
        txtAccountHolder.setPrefWidth(300);

        TextField txtInitialBalance = new TextField();
        txtInitialBalance.setPromptText("Initial Balance");
        txtInitialBalance.setPrefWidth(300);

        PasswordField txtAccountPassword = new PasswordField();
        txtAccountPassword.setPromptText("Account Password");
        txtAccountPassword.setPrefWidth(300);

        ComboBox<String> accountTypeComboBox = new ComboBox<>();
        accountTypeComboBox.getItems().addAll("Dollar", "Tenge");
        accountTypeComboBox.setPromptText("Select Account Type");
        accountTypeComboBox.setPrefWidth(300);

        Button btnRegister = new Button("Register");
        btnRegister.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnRegister.setOnAction(e -> {
            String stringAccountNumber = txtAccountNumber.getText();
            String accountHolder = txtAccountHolder.getText();
            String balanceStr = txtInitialBalance.getText();
            String accountPassword = txtAccountPassword.getText();
            String accountType = accountTypeComboBox.getValue();

            if (stringAccountNumber.isEmpty() || accountHolder.isEmpty() || balanceStr.isEmpty() || accountPassword.isEmpty() || accountType == null) {
                showAlert("Error", "All fields must be filled out.", Alert.AlertType.ERROR);
                return;
            }

            if (accounts.containsKey(stringAccountNumber)) {
                showAlert("Error", "The Client with this ID already exists!", Alert.AlertType.ERROR);
            } else {
                try {
                    double balance = Double.parseDouble(balanceStr);
                    BankAccount account = BankAccountFactory.createBankAccount(accountType, stringAccountNumber, accountHolder, balance, accountPassword);
                    RegisterObserver registerObserver = new RegisterObserver();
                    account.addObserver(registerObserver);

                    registerBankAccount(stringAccountNumber, account);

                    account.removeObserver(registerObserver);
                    showAlert("Success", "Account created successfully!", Alert.AlertType.INFORMATION);
                    registrationStage.close();
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Initial Balance must be a valid number.", Alert.AlertType.ERROR);
                } catch (IllegalArgumentException ex) {
                    showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        layout.getChildren().addAll(title, txtAccountNumber, txtAccountHolder, txtInitialBalance, txtAccountPassword, accountTypeComboBox, btnRegister);

        Scene scene = new Scene(layout, 400, 450);
        registrationStage.setScene(scene);
        registrationStage.show();
    }


    public void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openAdminLogin() {
        Admin admin = Admin.getInstance(); //----- S I N G L E T O N -----// getInstance() method //

        Stage loginStage = new Stage();
        loginStage.setTitle("Admin Login");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Label lblPassword = new Label("Enter Admin Password:");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");

        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        btnLogin.setOnAction(e -> {
            String password = txtPassword.getText();
            if (admin.getPassword().equals(password)) {
                loginStage.close();
                openAdminPanel();
            } else {
                showAlert("Error", "Incorrect Password", Alert.AlertType.ERROR);
            }
        });

        layout.getChildren().addAll(lblPassword, txtPassword, btnLogin);
        Scene scene = new Scene(layout, 300, 200);
        loginStage.setScene(scene);
        loginStage.show();
    }


    private void openAdminPanel() {
        Stage adminStage = new Stage();
        adminStage.setTitle("Admin Panel");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Admin Operations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnViewClients = createButton("View All Clients", "-fx-background-color: #17a2b8; -fx-text-fill: white;");
        Button btnDeleteClient = createButton("Delete Client", "-fx-background-color: #dc3545; -fx-text-fill: white;");
        Button btnBack = createButton("Back to Menu", "-fx-background-color: #6c757d; -fx-text-fill: white;");

        btnViewClients.setOnAction(e -> viewClients());
        btnDeleteClient.setOnAction(e -> deleteClient());
        btnBack.setOnAction(e -> adminStage.close());

        layout.getChildren().addAll(title, btnViewClients, btnDeleteClient, btnBack);

        Scene scene = new Scene(layout, 400, 400);
        adminStage.setScene(scene);
        adminStage.show();
    }

    private Button createButton(String text, String style) {
        Button button = new Button(text);
        button.setStyle(style);
        button.setPrefSize(200, 50);
        return button;
    }

    private void viewClients() {
        Stage viewStage = new Stage();
        viewStage.setTitle("All Clients");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        TableView<BankAccount> tableView = new TableView<>();
        TableColumn<BankAccount, String> colAccountNumber = new TableColumn<>("Account ID");
        colAccountNumber.setCellValueFactory(cellData -> cellData.getValue().accountNumberProperty());

        TableColumn<BankAccount, String> colAccountHolder = new TableColumn<>("Account Holder");
        colAccountHolder.setCellValueFactory(cellData -> cellData.getValue().accountHolderProperty());

        TableColumn<BankAccount, Double> colBalance = new TableColumn<>("Balance");
        colBalance.setCellValueFactory(cellData -> cellData.getValue().balanceProperty().asObject());

        TableColumn<BankAccount, String> colCurrency = new TableColumn<>("Currency");
        colCurrency.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());

        tableView.getColumns().addAll(colAccountNumber, colAccountHolder, colBalance, colCurrency);
        tableView.setItems(javafx.collections.FXCollections.observableArrayList(accounts.values()));

        Button btnClose = createButton("Close", "-fx-background-color: #007bff; -fx-text-fill: white;");
        btnClose.setOnAction(e -> viewStage.close());

        layout.getChildren().addAll(tableView, btnClose);
        Scene scene = new Scene(layout, 600, 400);
        viewStage.setScene(scene);
        viewStage.show();
    }

    private void deleteBankAccount(BankAccount bankAccount){
        bankAccount.notifyObservers(); // Notified all observers
        String accountNumber = bankAccount.accountNumber;

        accounts.remove(accountNumber);
    }
    private void deleteClient() {
        Stage deleteStage = new Stage();
        deleteStage.setTitle("Delete Client");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Delete Client by Account ID");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtAccountNumber = new TextField();
        txtAccountNumber.setPromptText("Enter Account ID");
        txtAccountNumber.setPrefWidth(300);

        Button btnDelete = createButton("Delete", "-fx-background-color: #dc3545; -fx-text-fill: white;");
        Button btnCancel = createButton("Cancel", "-fx-background-color: #6c757d; -fx-text-fill: white;");

        btnDelete.setOnAction(e -> {
            String accountNumber = txtAccountNumber.getText();

            if (accounts.containsKey(accountNumber)) {
                BankAccount bankAccountToDelete = accounts.get(accountNumber);
                DeleteObserver deleteNotify = new DeleteObserver();
                bankAccountToDelete.addObserver(deleteNotify);

                deleteBankAccount(bankAccountToDelete); // Method where Observer pattern is used

                bankAccountToDelete.removeObserver(deleteNotify);

                showAlert("Success", "Client deleted successfully.", Alert.AlertType.INFORMATION);
                deleteStage.close();
            } else {
                showAlert("Error", "Client not found.", Alert.AlertType.ERROR);
            }
        });

        btnCancel.setOnAction(e -> deleteStage.close());

        layout.getChildren().addAll(title, txtAccountNumber, btnDelete, btnCancel);

        Scene scene = new Scene(layout, 400, 300);
        deleteStage.setScene(scene);
        deleteStage.show();
    }

//***************************************** S I N G L E T O N   P A T T E R N *****************************************************************************************//


    public static class Admin {
        private static Admin instance;
        private static final String password = "";

        public static Admin getInstance(){
            if (instance == null){
                instance = new Admin();
            }
            return instance;
        }

        public String getPassword(){
            return password;
        }
    }

//***************************************** O B S E R V E R   P A T T E R N *****************************************************************************************//

    interface AccountObserver {
        void update(BankAccount bankAccount);
    }

    public static class DeleteObserver implements AccountObserver {
        @Override
        public void update(BankAccount bankAccount) {
            System.out.println("The Client " + bankAccount.accountHolder + " (" + bankAccount.accountNumber + ") has been deleted!");
        }
    }

    public static class RegisterObserver implements AccountObserver {
        @Override
        public void update(BankAccount bankAccount) {
            System.out.println("The Client " + bankAccount.accountHolder + " (" + bankAccount.accountNumber + ") has been registered!");
        }
    }

    public static class BankAccount {
        private final String accountNumber;
        private final String accountHolder;
        private double balance;
        private String currency;
        private final String password;
        private final List<AccountObserver> observers = new ArrayList<>();

        public BankAccount(String accountNumber, String accountHolder, double balance, String password) {
            this.accountNumber = accountNumber;
            this.accountHolder = accountHolder;
            this.balance = balance;
            this.password = password;
        }

        public StringProperty accountNumberProperty() {
            return  new SimpleStringProperty(accountNumber);
        }

        public StringProperty accountHolderProperty() {
            return new SimpleStringProperty(accountHolder);
        }

        public DoubleProperty balanceProperty() {
            return new SimpleDoubleProperty(balance);
        }

        public StringProperty currencyProperty() {
            return new SimpleStringProperty(currency);
        }


        public void addObserver(AccountObserver observer) {
            observers.add(observer);
        }

        public void removeObserver(AccountObserver observer) {
            observers.remove(observer);
        }

        private void notifyObservers() {
            for (AccountObserver observer : observers) {
                observer.update(this);
            }
        }
    }

//***************************************** F A C T O R Y   M E T H O D *****************************************************************************************//

    public static class DollarBankAccount extends BankAccount {
        public final String currency = "USD";
        public DollarBankAccount(String accountNumber, String accountHolder, double balance, String password) {
            super(accountNumber, accountHolder, balance, password);
            super.currency = currency;
        }
        @Override
        public StringProperty currencyProperty() {
            return new SimpleStringProperty(currency);
        }
    }

    public static class TengeBankAccount extends BankAccount {
        public final String currency = "KZT";

        public TengeBankAccount(String accountNumber, String accountHolder, double balance, String password) {
            super(accountNumber, accountHolder, balance, password);
            super.currency = currency;
        }
        @Override
        public StringProperty currencyProperty() {
            return new SimpleStringProperty(currency);
        }
    }

    public static class BankAccountFactory {
        public static BankAccount createBankAccount(String type, String accountNumber, String accountHolder, double balance, String password) {
            return switch (type.toLowerCase()) {
                case "dollar" -> new DollarBankAccount(accountNumber, accountHolder, balance, password);
                case "tenge" -> new TengeBankAccount(accountNumber, accountHolder, balance, password);
                default -> throw new IllegalArgumentException("Invalid bank account type: " + type);
            };
        }
    }
}


//***************************************** S T A T E    P A T T E R N *****************************************************************************************//

interface State {
    void handleLogin();
    void handleLogout();
    void handleClientButton();
}

class LoggedInState implements State {
    private final EnhancedBankManagementSystem system;

    public LoggedInState(EnhancedBankManagementSystem system) {
        this.system = system;
        system.setClientButtonVisibility(true); // Enable the Client button
    }

    @Override
    public void handleLogin() {
        system.showAlert("Login", "You are already logged in.", Alert.AlertType.INFORMATION);
    }

    @Override
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to log out?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                system.setState(new LoggedOutState(system));
                system.showAlert("Logout", "Logged out successfully.", Alert.AlertType.INFORMATION);
            }
        });
    }

    @Override
    public void handleClientButton() {
        system.showAlert("Client Access", "Client operations functionality.", Alert.AlertType.INFORMATION);
    }
}

class LoggedOutState implements State {
    private final EnhancedBankManagementSystem system;

    public LoggedOutState(EnhancedBankManagementSystem system) {
        this.system = system;
        system.setClientButtonVisibility(false); // Disable the Client button
    }

    @Override
    public void handleLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login successful!", ButtonType.OK);
        alert.showAndWait();
        system.setState(new LoggedInState(system));
    }

    @Override
    public void handleLogout() {
        system.showAlert("Logout", "You are not logged in.", Alert.AlertType.INFORMATION);
    }

    @Override
    public void handleClientButton() {
        system.showAlert("Client Access Denied", "Please log in first.", Alert.AlertType.WARNING);
    }
}