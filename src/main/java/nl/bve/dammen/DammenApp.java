package nl.bve.dammen;

import java.io.File;
import java.net.URISyntaxException;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.bve.dammen.domein.ComputerSpeler;
import nl.bve.dammen.domein.Damspel;

public class DammenApp extends Application implements EventHandler<ActionEvent> {
	private Damspel spel = new Damspel();
	private ComputerSpeler computer = new ComputerSpeler(spel);
	private GridPane dambord = new GridPane();
	private Button reset, geselecteerd1, geselecteerd2, bevestigen;
	private CheckBox roterenBox, bevestigenBox;
	private ToggleButton groteStenen, computerSpeler;
	private Label stenenZwart = new Label(""), stenenWit = new Label("");
	private String spelerUitzicht = spel.getSpeler();
	private Stage stage;
	
	public void start(Stage stage) throws URISyntaxException {
		this.stage = stage;
		BorderPane applicatiePanel = new BorderPane();
		
		VBox menuBox = new VBox(10);
		menuBox.setPadding(new Insets(10));
		menuBox.setPrefWidth(150);
		
		bevestigen = new Button("Zet bevestigen");
		bevestigen.setPrefWidth(150);
		bevestigen.setOnAction(this);
		bevestigen.setDisable(true);
		
		reset = new Button("Reset");
		reset.setPrefWidth(150);
		reset.setOnAction(this);
		
		roterenBox = new CheckBox("Bord roteren");

		bevestigenBox = new CheckBox("Zetten bevestigen");
		bevestigenBox.setSelected(true);
		
		Label spelerAanZetLabel = new Label("Speler: ");
		Label spelerAanZet = new Label(spel.getSpeler());
		spelerAanZet.textProperty().bind(spel.getSpelerProperty());
		
		groteStenen = new ToggleButton("grote stenen");
		groteStenen.setOnAction(this);
		groteStenen.setPrefWidth(150);
		
		computerSpeler = new ToggleButton("computerspeler aan");
		computerSpeler.setOnAction(this);
		computerSpeler.setPrefWidth(150);
		
		menuBox.getChildren().add(bevestigen);
		menuBox.getChildren().add(reset);
		menuBox.getChildren().add(roterenBox);
		menuBox.getChildren().add(bevestigenBox);
		menuBox.getChildren().add(new HBox(5, spelerAanZetLabel, spelerAanZet));
		menuBox.getChildren().add(stenenZwart);
		menuBox.getChildren().add(stenenWit);
		menuBox.getChildren().add(groteStenen);
		menuBox.getChildren().add(computerSpeler);
		
		Label meldingen = new Label("meldingen komen hier!");
		meldingen.textProperty().bind(spel.getMeldingProperty());
		meldingen.setStyle("-fx-text-fill: red");
		meldingen.setPadding(new Insets(5));

		dambord.setPadding(new Insets(5));
		for (int i=0; i < 100; i++) {
			Button b = new Button();
			b.setPrefSize(40, 40);
			b.setId(i+"");
			b.setOnAction(this);
			dambord.add(b, (i % 10), (i / 10));
		}
		updateDambord();
		
		applicatiePanel.setLeft(menuBox);
		applicatiePanel.setTop(meldingen);
		applicatiePanel.setCenter(dambord);
		
		BorderPane.setAlignment(meldingen, Pos.BOTTOM_RIGHT);
		
		Scene scene = new Scene(applicatiePanel);
		File cssSheet = new File(getClass().getResource("stylesheet.css").toURI());
		// File cssSheet = new File("layout/stylesheet.css");
		scene.getStylesheets().add(cssSheet.toURI().toString());
		stage.setResizable(false);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.setTitle("Dammen!");
		stage.show();
	}
	
	private void updateDambord() {
		int witStenen=0, witDammen=0, zwartStenen=0, zwartDammen=0;
		
		for (Node n : dambord.getChildren()) {
			Integer id = Integer.parseInt(n.getId());
			String status = spel.getVeldStatus(id);
			n.getStyleClass().clear();
			n.getStyleClass().add("button");
			n.getStyleClass().add(status);
			
			if (status.equals("WIT")) witStenen++;
			if (status.equals("ZWART")) zwartStenen++;
			if (status.equals("WITDAM")) witDammen++;
			if (status.equals("ZWARTDAM")) zwartDammen++;
		}
		
		if (geselecteerd1 != null) {
			geselecteerd1.getStyleClass().add("selectedbutton");
			
			if (geselecteerd2 != null) {
				geselecteerd2.getStyleClass().add("selectedbutton");
			}
		}
		
		if (roterenBox.isSelected() && (!spelerUitzicht.equals(spel.getSpeler()))) {
			rotateBord();
		}
		
		stenenZwart.setText("Stenen Z: " +zwartStenen+ " (" +zwartDammen+ ")" );
		stenenWit.setText("Stenen W: " +witStenen+ " (" +witDammen+ ")" );
	}
	
	private void rotateBord() {
		spelerUitzicht = spel.getSpeler();
		
		RotateTransition rotateTransition = new RotateTransition(Duration.millis(3000), dambord);
        rotateTransition.setByAngle(180f);
        rotateTransition.play();
	}
	
	public void handle(ActionEvent event) {
		if (event.getSource() == reset) {
			spel.reset();
		} else if (event.getSource() == computerSpeler) {
			computer.setEnabled(computerSpeler.isSelected());
		} else if (event.getSource() == groteStenen) {
			for (Node n : dambord.getChildren()) {
				if (groteStenen.isSelected()) {
					((Button)n).setPrefSize(60, 60);
				} else {
					((Button)n).setPrefSize(40, 40);
				}
			}
				
			stage.sizeToScene();
		} else if (event.getSource() == geselecteerd1) {
			geselecteerd1 = null;
			geselecteerd2 = null;
			bevestigen.setDisable(true);
		} else if (event.getSource() == geselecteerd2) {
			geselecteerd2 = null;
			bevestigen.setDisable(true);
		} else if (geselecteerd1 == null){
			Button geklikt = (Button)event.getSource(); 
			int vanVeldId = Integer.parseInt(geklikt.getId());
			
			if (spel.isVeldSpeelbaar(vanVeldId)) {
				geselecteerd1 = (Button)event.getSource();
			}
		} else if ((geselecteerd2 == null && bevestigenBox.isSelected())) {
			Button geklikt = (Button)event.getSource();
			int vanVeldId = Integer.parseInt(geselecteerd1.getId());
			int naarVeldId = Integer.parseInt(geklikt.getId());

			if (spel.isZetMogelijk(vanVeldId, naarVeldId)) {
				geselecteerd2 = geklikt;
				bevestigen.setDisable(false);
			}
		} else if ((event.getSource() == bevestigen && geselecteerd2 != null) || !bevestigenBox.isSelected()) {
			Button geselecteerd2 = (this.geselecteerd2 == null ? (Button)event.getSource() : this.geselecteerd2); 

			int vanVeldId = Integer.parseInt(geselecteerd1.getId());
			int naarVeldId = Integer.parseInt(geselecteerd2.getId());
			
			if (spel.doeZet(vanVeldId, naarVeldId)) {
				this.geselecteerd1 = null;
				this.geselecteerd2 = null;
				bevestigen.setDisable(true);
			}
		}
		
		updateDambord();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
}