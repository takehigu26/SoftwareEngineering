/*new*/
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class GameoverScene {
    private Scene scene = null;
    private Parent root = null;

    public GameoverScene () {
        try {
            System.out.println("Starting GameoverScene");
            root = FXMLLoader.load(getClass().getResource("GameoverScene.fxml"));
            scene = new Scene (root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Scene getScene () {
        return scene;
    }
}
