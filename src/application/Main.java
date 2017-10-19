package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	
	public static Stage window;
	public static BorderPane rootPane;
	
	public static MainMode mainMode = new MainMode();
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//== Window
		window = primaryStage;
		window.setTitle("Java Desktop Google Translator");
		
		//RootPane
		rootPane = new BorderPane(mainMode);
		
		//Show Window
		window.setScene(new Scene(rootPane));
		window.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
