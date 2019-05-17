import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.util.ResourceBundle;



public class GameoverController {

	@FXML Label resultLabel;

  @FXML Button closeBtn;

  @FXML
  private ResourceBundle resources;

	@FXML
	private void closeBtnClicked()
	{
		System.out.println("CLOSE BUTTON CLICKED");
	}
}
