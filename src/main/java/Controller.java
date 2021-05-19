import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class Controller {

    private proto_Control ftpControl;
    private final String DEFAULT_SAVE_PATH = "C:/ftp_retrieved_files";

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
    void initialize() {
        remoteDirTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observableValue, TreeItem<String> stringTreeItem, TreeItem<String> t1) {

                updateDirectoryContentList(t1);
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
            ftpControl.retrieve(selected.getFullPath(), storePathField.getText());
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
        ArrayList<String> names = ftpControl.listFiles(fullPath);
        for (String child : names) {
            proto_RemoteFile remoteFile = new proto_RemoteFile(child, fullPath.concat(child));
            content.add(remoteFile);
        }
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
