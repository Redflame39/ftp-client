import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.sun.javafx.scene.control.LabeledText;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.apache.commons.net.ftp.FTPFile;

public class Controller {

    private final Image DIRECTORY_ICON = new Image("/directory.png");
    private final Image FILE_ICON = new Image("/file.png");
    private final Image UNKNOWN_ICON = new Image("/unknown.png");

    private proto_Control ftpControl;
    private final String DEFAULT_SAVE_PATH = "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads";

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
    private TextField storePathField;

    @FXML
    private Button storeButton;

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
            //TODO alert "fields must not be empty"
            return;
        }
        ftpControl = new proto_Control(hostField.getText(),
                usernameField.getText(),
                passwordField.getText());
        ftpControl.connect();
        setRemoteDirTree();
        storePathField.setText(DEFAULT_SAVE_PATH);
    }

    @FXML
    void handleDisconnect(ActionEvent event) {
        if (ftpControl != null)
            ftpControl.disconnect();
    }

    @FXML
    void handleRetrieve(ActionEvent event) {
        proto_RemoteFile selected = directoryContent.getSelectionModel().getSelectedItem();
        if (ftpControl != null && selected != null) {
            ftpControl.retrieve(selected.getFullPath(), storePathField.getText() + "/" + selected.getName());
        }
    }

    @FXML
    void handleStore(ActionEvent event) {
        if (!fieldsNotEmpty()) {
            //TODO alert "fields must not be empty"
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
