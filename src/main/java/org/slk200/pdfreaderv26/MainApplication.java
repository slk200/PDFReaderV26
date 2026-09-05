package org.slk200.pdfreaderv26;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.controller.MainController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

/**
 * Created by tizzer on 2019/1/19.
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = fxmlLoader.load();

        MainController mainController = fxmlLoader.getController();
        mainController.setStage(primaryStage);
        mainController.initController();

        primaryStage.getIcons().add(ImageSource.LOGO);
        primaryStage.setTitle("价格统计工具");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        ThemeManager.init(scene);
        primaryStage.setMinWidth(1300);
        primaryStage.setMinHeight(800);
    }
}
