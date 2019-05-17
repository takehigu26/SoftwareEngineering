import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Translate;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Main extends Application {
    enum sceneType {
        Title, Login, Game, Gameover;
    }

    public static final int blocks = 9;
    public static final int blockSize = 50;
    private static int sceneLength = blocks * blockSize;
    public static boolean[][] isBlock = new boolean[Main.blocks][Main.blocks]; // false -> available true -> block
    private static boolean isPacman = true;
    private Character pacman, enemy, img;
    // private Pane root, start;
    private static LoginScene loginScene = null;
    public static Stage stage            = null;
    private static Scene gamescene;
    public static String ipAddress, playerName;
    // private int x,y;
    // private boolean isRight, isLeft, isUp, isDown;

    @Override
    public void start(Stage primaryStage)
    {
        try {
            //  root = new Pane();
            // scene = new Scene(root, sceneLength, sceneLength);
            loginScene = new LoginScene();
            stage      =  primaryStage;
            stage.setTitle("GamePlay");
            stage.setScene(loginScene.getScene());
            // ame  = new Game(root, scene, isPacman);
            // game.setStage();
            // scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            // stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeScene(sceneType scene)
    {
        switch (scene) {
            case Title:
                break;
            case Login:
                stage.setScene(loginScene.getScene());
                break;
            case Game:
                Pane  root = new Pane();
                Scene s    = new Scene(root, sceneLength, sceneLength);
                Game  game = new Game(root, s, isPacman);
                game.setStage();
                stage.setScene(s);
                break;
            case Gameover:
                System.out.println("here");
                GameoverScene gs = new GameoverScene();
                System.out.println("here2");
                stage.setScene(gs.getScene());
                break;
        }
        // stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
