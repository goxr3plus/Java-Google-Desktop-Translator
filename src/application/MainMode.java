package application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.darkprograms.speech.synthesiser.SynthesiserV2;
import com.darkprograms.speech.translator.GoogleTranslate;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXToggleButton;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javazoom.jl.player.advanced.AdvancedPlayer;

/**
 * This class represents the User Interface of the Application
 * 
 * @author GOXR3PLUS
 *
 */
public class MainMode extends StackPane {
	
	//--------------------------------------------------------------
	
	@FXML
	private HBox translateFromHBox;
	
	@FXML
	private MenuButton menuButton1;
	
	@FXML
	private JFXListView<String> listView1;
	
	@FXML
	private Label label1;
	
	@FXML
	private TextArea textArea1;
	
	@FXML
	private Button textToSpeech1;
	
	@FXML
	private JFXToggleButton autoDetectLanguage;
	
	@FXML
	private Button translate;
	
	@FXML
	private JFXListView<String> listView2;
	
	@FXML
	private Label label2;
	
	@FXML
	private TextArea textArea2;
	
	@FXML
	private Button textToSpeech2;
	
	@FXML
	private JFXToggleButton instantTranslation;
	
	private final ObservableList<String> availableLanguagesList = FXCollections.observableArrayList("Afrikaans", "Albanian", "Arabic", "Azerbaijani", "Basque", "Bengali",
			"Belarusian", "Bulgarian", "Catalan", "Chinese  Simplified", "Chinese Traditional", "Croatian", "Czech", "Danish", "Dutch", "English", "Esperanto", "Estonian",
			"Filipino", "Finnishn", "French", "Galician", "Georgian", "German", "Greek", "Gujarati", "Haitian", "Hebrew", "Hindi", "Hungarian", "Icelandic", "Indonesian", "Irish",
			"Italian", "Japanese", "Kannada", "Korean", "Latin", "Latvian", "Lithuanian", "Macedonian", "Malay", "Maltese", "Norwegian", "Persian", "Polish", "Portuguese",
			"Romanian", "Russian", "Serbian", "Slovak", "Slovenian", "Spanish", "Swahili", "Swedish", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Vietnamese",
			"Welsh", "Yiddish");
	
	private final ObservableList<String> availableLanguagesCodesList = FXCollections.observableArrayList("af", "sq", "ar", "az", "eu", "bn", "be", "bg", "ca", "zh-CN", "zh-TW",
			"hr", "cs", "da", "nl", "en", "eo", "et", "tl", "fi", "fr", "gl", "ka", "de", "el", "gu", "ht", "iw", "hi", "hu", "is", "id", "ga", "it", "ja", "kn", "ko", "la", "lv",
			"lt", "mk", "ms", "mt", "no", "fa", "pl", "pt", "ro", "ru", "sr", "sk", "sl", "es", "sw", "sv", "ta", "te", "th", "tr", "uk", "ur", "vi", "cy", "yi");
	
	// -------------------------------------------------------------
	
	/** The logger. */
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * Used to make the tranlsation calls using an External Thread
	 */
	private TranslateService translateService = new TranslateService();
	
	/**
	 * Using Java Google Synthesizer to create Speech from Text
	 */
	private PlayerService playerService = new PlayerService();
	
	//Create a Synthesizer instance
	SynthesiserV2 synthesizer = new SynthesiserV2("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw");
	
	/**
	 * Constructor.
	 */
	public MainMode() {
		
		// ------------------------------------FXMLLOADER ----------------------------------------
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMode.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "", ex);
		}
		
	}
	
	/**
	 * Called as soon as .FXML is loaded from FXML Loader
	 */
	@FXML
	private void initialize() {
		
		//============== Translate From ====================
		listView1.setItems(availableLanguagesList);
		listView1.getSelectionModel().selectedItemProperty().addListener((observable , oldValue , newValue) -> {
			label1.setText(newValue);
			//Instant Translation is Selected?
			if (instantTranslation.isSelected()) {
				translateService.startService();
			}
		});
		listView1.getSelectionModel().select("English");
		
		//============== Translate To ====================
		listView2.setItems(availableLanguagesList);
		listView2.getSelectionModel().selectedItemProperty().addListener((observable , oldValue , newValue) -> {
			label2.setText(newValue);
			//Instant Translation is Selected?
			if (instantTranslation.isSelected()) {
				translateService.startService();
			}
		});
		listView2.getSelectionModel().select("Greek");
		
		//== translate
		translate.disableProperty().bind(instantTranslation.selectedProperty());
		translate.setOnAction(a -> translateService.startService());
		
		//== textArea1	
		textArea1.textProperty().addListener((observable , oldValue , newValue) -> {
			//Instant Translation is Selected?
			if (instantTranslation.isSelected()) {
				translateService.startService();
			}
		});
		
		//== translateFromHBox
		translateFromHBox.disableProperty().bind(autoDetectLanguage.selectedProperty());
		
		//== textToSpeech1
		textToSpeech1.setOnAction(a -> playerService.startService(textArea1));
		textToSpeech2.setOnAction(a -> playerService.startService(textArea2));
		
	}
	
	/**
	 * 
	 * Call Google Translate API using this Service
	 * 
	 * Using JavaFX Service to run our tasks outside JavaFX Main Thread so the application doesn't lags at all from those Tasks
	 * 
	 * @author GOXR3PLUS
	 *
	 */
	public class TranslateService extends Service<Void> {
		
		public void startService() {
			this.restart();
		}
		
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					
					//Do the translation			
					String[] translation = { "ERROR" };
					if (autoDetectLanguage.isSelected()) //Auto Detection on?
						translation[0] = GoogleTranslate.translate(availableLanguagesCodesList.get(availableLanguagesList.indexOf(label2.getText())), textArea1.getText());
					else
						translation[0] = GoogleTranslate.translate(availableLanguagesCodesList.get(availableLanguagesList.indexOf(label1.getText())),
								availableLanguagesCodesList.get(availableLanguagesList.indexOf(label2.getText())), textArea1.getText());
					
					//Set the translated text to TextArea2 and 
					//run it on JavaFX Thread [ important (yes i talk to you bro ...)]
					Platform.runLater(() -> textArea2.setText(translation[0]));
					
					return null;
				}
				
			};
		}
		
	}
	
	/**
	 * 
	 * Using Java Google Synthesizer to create Speech from Text
	 * 
	 * Using JavaFX Service to run our tasks outside JavaFX Main Thread so the application doesn't lags at all from those Tasks
	 * 
	 * @author GOXR3PLUS
	 *
	 */
	public class PlayerService extends Service<Void> {
		AdvancedPlayer player;
		TextArea textArea;
		
		/**
		 * Start the Service giving a TextArea
		 * 
		 * @param textArea
		 */
		public void startService(TextArea textArea) {
			
			this.textArea = textArea;
			
			//Restart the Service
			this.restart();
		}
		
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					
					//Try to play the Media
					//Stop the player if it is still working
					player = new AdvancedPlayer(synthesizer.getMP3Data(textArea.getText()));
					player.play();
					
					return null;
				}
				
			};
		}
		
	}
}
