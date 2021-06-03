package by.bsuir.controller;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import by.bsuir.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import by.bsuir.ftp.*;

public class Controller {

    private proto_Control ftpControl;
    private boolean connectedToServer = false;
    private final String DEFAULT_SAVE_PATH = "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads";
    private final Image DIRECTORY_ICON = new Image("/directory.png");
    private final Image FILE_ICON = new Image("/file.png");
    private final Image UNKNOWN_ICON = new Image("/unknown.png");

    @FXML
    private TextField hostField;

    @FXML
    private TextField usernameField;

    @FXML
    private Button connectButton;

    @FXML
    private Button disconnectButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TreeView<String> remoteDirTree;

    @FXML
    private ListView<proto_RemoteFile> directoryContent;

    @FXML
    private Button retrieveButton;

    @FXML
    private Button storeFileButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button storeDirectoryButton;

    @FXML
    private TextField portField;

    @FXML
    private Button createFolderButton;

    @FXML
    void initialize() {
        remoteDirTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observableValue, TreeItem<String> stringTreeItem, TreeItem<String> t1) {

                updateDirectoryContentList(t1);
            }
        });
        directoryContent.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    proto_RemoteFile selected = directoryContent.getSelectionModel().getSelectedItem();
                    if (selected == null)
                        return;
                    if (selected.getName().equals("..")) {
                        TreeItem<String> parent = remoteDirTree.getSelectionModel().getSelectedItem().getParent();
                        if (parent == null)
                            return;
                        updateDirectoryContentList(parent);
                        remoteDirTree.getSelectionModel().select(parent);
                    } else {
                        remoteDirTree.getSelectionModel().getSelectedItem().getChildren().forEach(item -> {
                            if (item.getValue().equals(selected.getName())) {
                                updateDirectoryContentList(item);
                                remoteDirTree.getSelectionModel().select(item);
                            }
                        });
                    }
                }
            }
        });
    }

    @FXML
    private void handleConnect(ActionEvent event) {
        if (!fieldsNotEmpty()) {
            AlertController.showAlert("Fields must not be empty", Alert.AlertType.INFORMATION);
            return;
        }
        if (connectedToServer) {
            AlertController.showAlert("You are already connected to server", Alert.AlertType.INFORMATION);
            return;
        }
        ftpControl = new proto_Control(hostField.getText(),
                Integer.parseInt(portField.getText()),
                usernameField.getText(),
                passwordField.getText());
        boolean successfullyConnected = ftpControl.connect();
        if (successfullyConnected) {
            setRemoteDirTree();
            connectedToServer = true;
        } else {
            AlertController.showAlert(
                    "Cannot connect to server " + hostField.getText() + ":" + portField.getText(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleDisconnect(ActionEvent event) {
        if (connectedToServer) {
            ftpControl.disconnect();
            connectedToServer = false;
            remoteDirTree.setRoot(null);
            directoryContent.getItems().clear();
            AlertController.showAlert("You have been successfully disconnected from server!", Alert.AlertType.INFORMATION);
        } else {
            AlertController.showAlert("You are not connected to server", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    void handleRetrieve(ActionEvent event) {
        if (!connectedToServer) {
            AlertController.showAlert("You are not connected to server", Alert.AlertType.INFORMATION);
            return;
        }
        proto_RemoteFile selected = directoryContent.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.getName().equals("..")) {
            if (!selected.getFile().hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) {
                AlertController.showAlert("You have no permission to retrieve this file!", Alert.AlertType.ERROR);
                return;
            }
            if (!selected.getFile().isDirectory()) {
                boolean successfully = ftpControl.retrieve(selected.getFullPath(),
                        DEFAULT_SAVE_PATH + "/" + selected.getName());
                if (!successfully) {
                    AlertController.showAlert("Cannot retrieve file from server!", Alert.AlertType.ERROR);
                }
            } else {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                boolean successfully = ftpControl.retrieveDirectory(selected.getFullPath(),
                        DEFAULT_SAVE_PATH + "\\" + selected.getName()
                                + "_" + timestamp.toString().replaceAll("[^0-9]", "") + ".zip");
                if (!successfully) {
                    AlertController.showAlert("Cannot retrieve directory from server!", Alert.AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    void handleStoreFile(ActionEvent event) {
        if (!connectedToServer) {
            AlertController.showAlert("You are not connected to server", Alert.AlertType.INFORMATION);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to store");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("All Files", "*");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File file = fileChooser.showOpenDialog(Main.primaryStage);
        if (file != null) {
            ftpControl.store(file, getFullPath(remoteDirTree.getSelectionModel().getSelectedItem()));

            updateDirectoryContentList(remoteDirTree.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    void handleStoreDirectory(ActionEvent event) {
        if (!connectedToServer) {
            AlertController.showAlert("You are not connected to server", Alert.AlertType.INFORMATION);
            return;
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose file to store");
        File file = directoryChooser.showDialog(Main.primaryStage);
        if (file != null) {
            ftpControl.storeDirectory(file,
                    getFullPath(remoteDirTree.getSelectionModel().getSelectedItem()) + file.getName());
            updateDirectoryContentList(remoteDirTree.getSelectionModel().getSelectedItem());
            setRemoteDirTree();
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (!connectedToServer) {
            AlertController.showAlert("You are not connected to server", Alert.AlertType.INFORMATION);
            return;
        }
        if (directoryContent.getSelectionModel().getSelectedItem() == null) {
            AlertController.showAlert("Select item to delete", Alert.AlertType.INFORMATION);
            return;
        }
        if (!AlertController.confirmationAlert("Are you sure you want to delete this file?")) {
            return;
        }
        proto_RemoteFile selected = directoryContent.getSelectionModel().getSelectedItem();
        switch (selected.getFile().getType()) {
            case FTPFile.FILE_TYPE: {
                if (ftpControl.deleteFile(selected.getFullPath())) {
                    directoryContent.getItems().removeIf(
                            proto_remoteFile -> proto_remoteFile.getName().equals(selected.getName()));
                } else {
                    AlertController.showAlert("File not deleted", Alert.AlertType.ERROR);
                }
                break;
            }
            case FTPFile.DIRECTORY_TYPE: {
                if (ftpControl.deleteDirectory(selected.getFullPath())) {
                    directoryContent.getItems().removeIf(
                            proto_remoteFile -> proto_remoteFile.getName().equals(selected.getName()));
                    remoteDirTree.getSelectionModel().getSelectedItem().getChildren().removeIf(
                            directory -> directory.getValue().equals(selected.getName()));
                } else {
                    AlertController.showAlert("File not deleted", Alert.AlertType.ERROR);
                }
            }
        }

    }

    @FXML
    void handleCreateFolder(ActionEvent event) {
        if (!connectedToServer) {
            AlertController.showAlert("You are not connected to server", Alert.AlertType.INFORMATION);
            return;
        }
        String newFolderName = AlertController.textDialog("Specify the folder name", "New folder name: ");
        ftpControl.makeDirectory(remoteDirTree.getSelectionModel().getSelectedItem().getValue() + "/" + newFolderName);
        updateDirectoryContentList(remoteDirTree.getSelectionModel().getSelectedItem());
        setRemoteDirTree();
    }

    private void setRemoteDirTree() {
        remoteDirTree.setRoot(new TreeItem<>("/"));
        setChilds(remoteDirTree.getRoot(), "/");
    }

    private void setChilds(TreeItem<String> parentNode, String parentRoot) {
        ArrayList<String> listChilds = ftpControl.listDirectories(parentRoot);
        if (listChilds.isEmpty())
            return;
        for (String child : listChilds) {
            TreeItem<String> childNode = new TreeItem<>(child);
            parentNode.getChildren().add(childNode);
            setChilds(childNode, parentNode.getValue().concat("/").concat(child));
        }
    }

    private void updateDirectoryContentList(TreeItem<String> selected) {
        directoryContent.getItems().clear();
        ArrayList<proto_RemoteFile> content = new ArrayList<>();
        String fullPath = getFullPath(selected);
        ArrayList<FTPFile> files = ftpControl.listFiles(fullPath);
        for (FTPFile file : files) {
            proto_RemoteFile remoteFile = new proto_RemoteFile(file, fullPath.concat(file.getName()));
            content.add(remoteFile);
        }
        proto_RemoteFile linkBack = new proto_RemoteFile();
        linkBack.setName("..");
        directoryContent.getItems().add(linkBack);
        directoryContent.getItems().addAll(content);
    }

    private String getFullPath(final TreeItem<String> children) {
        LinkedList<String> listPath = new LinkedList<>();
        TreeItem<String> current = children;
        while (!current.getValue().equals("/")) {
            listPath.add(current.getValue());
            current = current.getParent();
        }
        StringBuilder result = new StringBuilder("/");
        Iterator<String> iterator = listPath.descendingIterator();
        while (iterator.hasNext()) {
            result.append(iterator.next());
            result.append("/");
        }
        return result.toString();
    }

    private boolean fieldsNotEmpty() {
        return !hostField.getText().isEmpty()
                && !usernameField.getText().isEmpty()
                && !passwordField.getText().isEmpty();
    }

}
