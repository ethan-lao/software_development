import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.shape.Line;

public class Database extends Application {
	static Map<String,Location> businessLocations = new HashMap<String,Location>();
	static Map<String,double[]> savedLocations = new HashMap<String,double[]>();
	static double userLongitude;
	static double userLatitude;
	static Map<String,Location> outputLocations = new LinkedHashMap<String,Location>();
	static Map<String,Location> favoriteLocations = new LinkedHashMap<String,Location>();
	
	/**
	 * Main method
	 * 
	 * @param args
	 * 				String[] arguments
	 * @throws IOException
	 * 			throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Scanner fin1 = new Scanner(new FileInputStream("locations.txt"));
		while (fin1.hasNext()) {
			String[] temp = fin1.nextLine().split("\t");
			businessLocations.put(temp[0], new Location(temp[0], temp[1], temp[2], Double.parseDouble(temp[3]), Double.parseDouble(temp[4]), -1));
		}
		fin1.close();
		Scanner fin2 = new Scanner(new FileInputStream("savedLocations.txt"));
		while (fin2.hasNext()) {
			String[] temp = fin2.nextLine().split("\t");
			double[] coor = {Double.parseDouble(temp[1]), Double.parseDouble(temp[2])};
			savedLocations.put(temp[0], coor);
		}
		fin2.close();
		Scanner fin3 = new Scanner(new FileInputStream("reviews.txt"));
		while (fin3.hasNext()) {
			String[] temp = fin3.nextLine().split("\t");
			boolean f = Boolean.parseBoolean(temp[1]);
			if (f == true) {
				businessLocations.get(temp[0]).setFavorite(f);
			}
			float r = Float.parseFloat(temp[2]);
			if (r != -1) {
				businessLocations.get(temp[0]).setRating(r);
			}
		}
		updateFavoritesList();
		fin3.close();
		launch(args);
	}
	
	/**
	 * Adds location to saved locations
	 * 
	 * @param n
	 * 			String name
	 * @param lon
	 * 			double longitude
	 * @param lat
	 * 			double latitude
	 */
	private void addSavedLocation(String n, double lon, double lat) {
		double[] v = {lon,lat};
		savedLocations.put(n, v);
	}
	
	/**
	 * Saves locations to savedLocations.txt
	 * 
	 * @throws IOException
	 * 			throws IOException
	 */
	private void saveMyLocations() throws IOException {
		PrintWriter out = new PrintWriter(new File("savedLocations.txt"));
		for(String s : savedLocations.keySet()) {
			out.println(s + "\t" + savedLocations.get(s)[0] + "\t" + savedLocations.get(s)[1]);
		}
		out.close();
	}
	
	/**
	 * Saves favorites and ratings to reviews.txt
	 * 
	 * @throws IOException
	 * 			throws IOException
	 */
	private static void saveReviews() throws IOException {
		PrintWriter out = new PrintWriter(new File("reviews.txt"));
		for(String s : businessLocations.keySet()) {
			if (businessLocations.get(s).getFavorite() == true || businessLocations.get(s).getRating() != -1) {
				out.println(s + "\t" + businessLocations.get(s).getFavorite() + "\t" + businessLocations.get(s).getRating());
			}
		}
		out.close();
	}
	
	/**
	 * Removes a location from saved locations
	 * 
	 * @param n
	 * 			String name
	 */
	private void removeMyLocations(String n) {
		savedLocations.remove(n);
	}
	
	/**
	 * Updates location distances
	 */
	private void updateDistances() {
		if (userLongitude != -1000 && userLatitude != -1000) {
			for (String s : businessLocations.keySet()) {
				businessLocations.get(s).setDistance(68.972181*(Math.hypot((businessLocations.get(s).getLongitude() - userLongitude),
																	(businessLocations.get(s).getLatitude() - userLatitude))));
			}
		}
		else {
			for (String s : businessLocations.keySet()) {
				businessLocations.get(s).setDistance(-1);
			}
		}
	}
	
	/**
	 * Updates outputLocations based on business type
	 * 
	 * @param t
	 * 			String type
	 */
	private void orderLocationsType(String t) {
		Map<String,Double> typeMap = new LinkedHashMap<String,Double>();
		for (String s : businessLocations.keySet()) {
			if (businessLocations.get(s).getType().compareTo(t) == 0) {
				typeMap.put(s, businessLocations.get(s).getPriority());
			}
		}
		typeMap = sortByValue(typeMap, false);
		outputLocations.clear();
		for (String s : typeMap.keySet()) {
			outputLocations.put(s, businessLocations.get(s));
		}
	}
	
	/**
	 * Updates outputLocations based on business name
	 * 
	 * @param n
	 * 			String name
	 */
	private void orderLocationsName(String n) {
		Map<String,Double> nameMap = new LinkedHashMap<String,Double>();
		for (String s : businessLocations.keySet()) {
			if (businessLocations.get(s).getName().toLowerCase().indexOf(n) != -1) {
				nameMap.put(s, businessLocations.get(s).getPriority());
			}
		}
		nameMap = sortByValue(nameMap, false);
		outputLocations.clear();
		for (String s : nameMap.keySet()) {
			outputLocations.put(s, businessLocations.get(s));
		}
	}
	
	
	/**
	 * Updates outputLocations based on distance
	 */
	private void orderLocationsDistance() {
		Map<String,Double> distanceMap = new LinkedHashMap<String,Double>();
		for (String s : businessLocations.keySet()) {
			distanceMap.put(s, businessLocations.get(s).getPriority());
		}
		distanceMap = sortByValue(distanceMap, false);
		outputLocations.clear();
		for (String s : distanceMap.keySet()) {
			outputLocations.put(s, businessLocations.get(s));
		}
	}
	
	/**
	 * Updates favorite for a location
	 * 
	 * @param n
	 * 			String name
	 * @param f
	 * 			boolean favorite
	 * 
	 * @throws IOException
	 */
	private void updateFavorite(String n, boolean f) throws IOException {
		businessLocations.get(n).setFavorite(f);
		updateFavoritesList();
		saveReviews();
	}
	
	/**
	 * Updates rating for a location
	 * @param n
	 * 			String name
	 * @param r
	 * 			float rating
	 * 
	 * @throws IOException
	 */
	private void updateRating(String n, float r) throws IOException {
		businessLocations.get(n).setRating(r);
		saveReviews();
	}
	
	/**
	 * Updates the list of favorites
	 * 
	 * @throws IOException
	 */
	private static void updateFavoritesList() throws IOException {
		favoriteLocations.clear();
		Map<String,Double> favoritesMap = new LinkedHashMap<String,Double>();
		for (String s : businessLocations.keySet()) {
			if (businessLocations.get(s).getFavorite() == true) {
				favoritesMap.put(s, businessLocations.get(s).getPriority());
			}
		}
		favoritesMap = sortByValue(favoritesMap, false);
		for (String s : favoritesMap.keySet()) {
			favoriteLocations.put(s, businessLocations.get(s));
		}
		saveReviews();
	}
	
	/**
	 * Sorts a map by its values
	 * 
	 * @param unsortMap
	 * @param order
	 * @return
	 */
    private static Map<String, Double> sortByValue(Map<String, Double> unsortMap, final boolean order)
    {
        List<Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }
    
    /**
     * Constructs HBox for displaying locations
     * 
     * @param s
     * 			String name
     * @param m
     * 			Stage mainStage
     * @return
     * 			HBox row
     */
    public HBox outputHbox(String s, Stage m) {
    	VBox temp1 = new VBox();
    	temp1.setSpacing(0);
    	
    	Button name = new Button(businessLocations.get(s).getName());
    	name.setMinHeight(35);
    	name.setMinWidth(600);
    	name.setStyle("-fx-background-color: #39B7CD; -fx-background-insets: 0; -fx-background-radius: 0;");
    	
    	Button address = new Button(businessLocations.get(s).getAddress());
    	address.setMinHeight(25);
    	address.setMinWidth(600);
    	address.setStyle("-fx-background-color: #39B7CD; -fx-background-insets: 0; -fx-background-radius: 0;");
    	
    	temp1.getChildren().addAll(name, address);
    	
    	Button rating = new Button(businessLocations.get(s).getRating() + "/5.0");
    	rating.setMinHeight(60);
    	rating.setMinWidth(200);
    	rating.setStyle("-fx-background-color: #39B7CD; -fx-background-insets: 0; -fx-background-radius: 0;");
    	if (businessLocations.get(s).getRating() == -1) {
    		rating.setText("No Rating");
    	}
    	
    	HBox temp2 = new HBox();
    	temp2.setSpacing(0);
    	temp2.getChildren().addAll(temp1, rating);
    	temp2.setAlignment(Pos.CENTER);
    	
    	name.setOnAction(e -> {
    		Stage businessPop = businessStagePop(businessLocations.get(s).getName(), m);
    		businessPop.show();
    	});
    	
    	address.setOnAction(e -> {
    		Stage businessPop = businessStagePop(businessLocations.get(s).getName(), m);
    		businessPop.show();
    	});
    	
    	rating.setOnAction(e -> {
    		Stage businessPop = businessStagePop(businessLocations.get(s).getName(), m);
    		businessPop.show();
    	});
    	
    	return temp2;
    }
    
    /**
     * Constructs a Stage for business information
     * 
     * @param s
     * 			String name
     * @param m
     * 			Stage mainStage
     * @return
     * 			Stage businessPop
     */
    public Stage businessStagePop(String s, Stage m) {
    	final BooleanProperty firstTime = new SimpleBooleanProperty(true);
    	
    	Stage businessPop = new Stage();
    	businessPop.initModality(Modality.APPLICATION_MODAL);
    	businessPop.initOwner(m);
    	businessPop.setTitle(businessLocations.get(s).getName());
		
    	BorderPane businessPopLayout = new BorderPane();
    	businessPopLayout.setPadding(new Insets(20, 20, 20, 20));
    	businessPopLayout.setBackground(Background.EMPTY);
    	businessPopLayout.setStyle("-fx-background-color: #191414");
    	
    	VBox display = new VBox();
    	display.setSpacing(10);
		
		Label name = new Label(s);
		name.setTextFill(Paint.valueOf("#FFFFFF"));
		
		Label type = new Label(businessLocations.get(s).getType().substring(0,1).toUpperCase() + businessLocations.get(s).getType().substring(1));
		type.setTextFill(Paint.valueOf("#FFFFFF"));
		
		Line break1 = new Line(20, 40, 430, 40);
		break1.setStyle("-fx-stroke-width: 5px; -fx-stroke: #FFFFFF");
		
		Label address = new Label("Address: " + businessLocations.get(s).getAddress());
		address.setTextFill(Paint.valueOf("#FFFFFF"));
		
		Label location = new Label("Coordinates: (" + businessLocations.get(s).getLatitude() + ", " + businessLocations.get(s).getLongitude() + ")");
		location.setTextFill(Paint.valueOf("#FFFFFF"));
		
		DecimalFormat df = new DecimalFormat("#.##");
		Label distance = new Label("Distance: " + df.format(businessLocations.get(s).getDistance()) + " mi.");
		distance.setTextFill(Paint.valueOf("#FFFFFF"));
		
		Line break2 = new Line(20, 40, 430, 40);
		break2.setStyle("-fx-stroke-width: 5px; -fx-stroke: #FFFFFF");
		
		Label ratingTitle = new Label("Your Rating: " + businessLocations.get(s).getRating() + "/5.0");
		ratingTitle.setTextFill(Paint.valueOf("#FFFFFF"));
		
		if (businessLocations.get(s).getRating() == -1) {
			ratingTitle.setText("Your Rating: No Rating");
		}
		
		ToggleButton favorite = new ToggleButton("Favorite");
		favorite.setSelected(businessLocations.get(s).getFavorite());
		if (favorite.isSelected()) {
			favorite.setStyle("-fx-background-color: #39B7CD");
		}
		
		Button rate = new Button("Rate");
		
		if (businessLocations.get(s).getDistance() == -1) {
			display.getChildren().addAll(name, type, break1, address, location, break2, ratingTitle, favorite, rate);
		}
		else {
			display.getChildren().addAll(name, type, break1, address, location, distance, break2, ratingTitle, favorite, rate);
		}

				
		businessPopLayout.setCenter(display);
		
		Scene dialogScene;
		if (businessLocations.get(s).getDistance() == -1) {
			dialogScene = new Scene(businessPopLayout, 450, 260);
		}
		else {
			dialogScene = new Scene(businessPopLayout, 450, 285);
		}
		businessPop.setScene(dialogScene);
		businessPop.setResizable(false);
				
		favorite.focusedProperty().addListener((observable,  oldValue,  newValue) -> {
            if(newValue && firstTime.get()){
                ratingTitle.requestFocus(); // Delegate the focus to container
                firstTime.setValue(false); // Variable value changed for future references
            }
        });
		
		favorite.setOnAction(e -> {
			boolean fav = favorite.isSelected();
			try {
				updateFavorite(businessLocations.get(s).getName(), fav);
			} catch (IOException e1) {
				
			}
			if (fav) {
				favorite.setStyle("-fx-background-color: #39B7CD");
			}
			else {
				favorite.setStyle(null);
			}
			ratingTitle.requestFocus();
		});
		
		rate.setOnAction(e -> {
			Stage ratePop = new Stage();
			ratePop.initModality(Modality.APPLICATION_MODAL);
			ratePop.initOwner(m);
			ratePop.setTitle("Rate");
			
			BorderPane rateLayout = new BorderPane();
			rateLayout.setPadding(new Insets(10, 10, 10, 10));
			
			VBox rateVbox = new VBox();
			rateVbox.setSpacing(10);
			Label ratePopLabel = new Label("Enter a number between 0 and 5");
			
			Line break3 = new Line(20, 40, 280, 40);
			break3.setStyle("-fx-stroke-width: 2px");
			
			HBox rateHbox = new HBox();
			rateHbox.setSpacing(10);
			
			
			ComboBox rateInput = new ComboBox();
			rateInput.getItems().addAll("", "0.0", "0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0");
			rateInput.setEditable(true);
			rateInput.setPromptText("Ex. 3.7");
			rateInput.valueProperty().setValue("");
			rateInput.setVisibleRowCount(4);
			rateInput.setMaxWidth(80);
			
			
			Button rateButton = new Button("Rate!");
			Label orLabel = new Label("OR");
			Button deleteRate = new Button("Delete Rating");
			rateHbox.getChildren().addAll(rateInput, rateButton, orLabel, deleteRate);
			rateHbox.setAlignment(Pos.CENTER);
			
			rateVbox.getChildren().addAll(ratePopLabel, break3, rateHbox);
			rateVbox.setAlignment(Pos.CENTER);
			
			rateLayout.setCenter(rateVbox);
			
			Scene rateScene = new Scene(rateLayout, 290, 90);
			
			deleteRate.setOnAction(e1 -> {
				ratePop.close();
				businessLocations.get(s).setRating(-1);
				try {
					saveReviews();
				} catch (IOException e2) {
				}
				ratingTitle.setText("Your Rating: No Rating");
			});
			
			rateInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
		        @Override
		        public void handle(KeyEvent ke) {
		            if (ke.getCode().equals(KeyCode.ENTER)) {
		            	rateButton.fire();
		            	rateButton.requestFocus();
		            }
		        }
		    });
			
			rateButton.setOnAction(e1 -> {
				boolean numberError = false;
				float r = -1;
				try {
					double r0 = Double.parseDouble((String) rateInput.getValue());
					DecimalFormat df1 = new DecimalFormat("#.#");
					r = Float.parseFloat(df1.format(r0));
					if (r < 0 || r > 5) {
						numberError = true;
					}
				}	catch (Exception ee) {
					numberError = true;
				}
				
					
				if (((String) rateInput.getValue()).equals(null) || ((String) rateInput.getValue()).equals("")) {
					Stage ratePopError = new Stage();
					ratePopError.setTitle("Error");
					ratePopError.initModality(Modality.APPLICATION_MODAL);
					ratePopError.initOwner(m);
		            Label error = new Label("Error: Please enter a rating first");
		            Button errorOk = new Button("OK");
		            //errorOk.setMinWidth(300);
		            VBox errorVbox = new VBox(10);
		            errorVbox.getChildren().addAll(error, errorOk);
		            errorVbox.setPadding(new Insets(10, 10, 10, 10));
		            Scene errorRateDialogScene = new Scene(errorVbox, 190, 70);
		            ratePopError.setScene(errorRateDialogScene);
		            ratePopError.show();
		            
		            errorOk.setOnAction(f -> {
		            	ratePopError.close();
					});
				}
				else if (numberError) {
					Stage ratePopError1 = new Stage();
					ratePopError1.setTitle("Error");
					ratePopError1.initModality(Modality.APPLICATION_MODAL);
					ratePopError1.initOwner(m);
					Label error0 = new Label("Error: Please enter a number between 0 and 5");
					Button errorOk0 = new Button("OK");
					//errorOk0.setMinWidth(300);
					VBox errorVbox0 = new VBox(10);
					errorVbox0.getChildren().addAll(error0, errorOk0);
					errorVbox0.setPadding(new Insets(10, 10, 10, 10));
					Scene errorRateDialogScene1 = new Scene(errorVbox0, 300, 70);
					ratePopError1.setScene(errorRateDialogScene1);
					ratePopError1.show();
	            
					errorOk0.setOnAction(f -> {
						ratePopError1.close();
					});
				}
				else {
					try {
						updateRating(businessLocations.get(s).getName(), r);
					} catch (IOException e2) {
						
					}
					ratePop.close();
					ratingTitle.setText("Your Rating: " + businessLocations.get(s).getRating() + "/5.0");
				}
			});
			
			ratePop.setScene(rateScene);
			ratePop.show();
			ratePopLabel.requestFocus();
		});
		
		return businessPop;
   }
    
    @Override
    public void start(Stage startupStage) {
    	FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
    	startupStage.setTitle("LOC8R");
    	
//START UP______________________________________________________________________________________________    	
    	BorderPane startLayout = new BorderPane();
		startLayout.setPadding(new Insets(60, 60, 60, 60));
		
		Image logo = new Image("file:src/images/Loc8r_white.png");
		ImageView imgView = new ImageView(logo);
		imgView.setPreserveRatio(true);
		imgView.setFitHeight(300);
		startLayout.setTop(imgView);
		startLayout.setAlignment(startLayout.getTop(), Pos.CENTER);
		startLayout.setMargin(startLayout.getTop(), new Insets(0));
		
		startLayout.setBackground(Background.EMPTY);
		startLayout.setStyle("-fx-background-color: #191414");
		
		VBox cen = new VBox();
		cen.setSpacing(20);
		
		Label entLoc = new Label("Enter your location");
		entLoc.setFont(Font.font("Helvetica", FontWeight.MEDIUM, 40));
		entLoc.setTextFill(Paint.valueOf("#FFFFFF"));
		
		Line lineBreak1 = new Line(60, 40, 640, 40);
		lineBreak1.setStyle("-fx-stroke-width: 5px; -fx-stroke: #FFFFFF");		
		
		HBox startHbox1 = new HBox();
		startHbox1.setSpacing(10);
		TextField latInput = new TextField("");
		latInput.setPromptText("latitude (-90 to 90, Ex. 30.26741)");
		latInput.setMinWidth(355);
		ObservableList<String> options = FXCollections.observableArrayList("Reset");
		ComboBox savedLocDropdown = new ComboBox(options);
		for (String s : savedLocations.keySet()) {
			savedLocDropdown.getItems().add(s);
		}
		savedLocDropdown.setVisibleRowCount(5);
		savedLocDropdown.setMinWidth(220);
		savedLocDropdown.setPromptText("Select a saved location");
		startHbox1.getChildren().addAll(latInput, savedLocDropdown);
		
		HBox startHbox2 = new HBox();
		startHbox2.setSpacing(10);
		TextField lonInput = new TextField("");
		lonInput.setPromptText("longitude (-180 to 180, Ex. -97.72521)");
		lonInput.setMinWidth(355);
		Button saveLoc = new Button("Save Location");
		saveLoc.setMinWidth(220);
		startHbox2.getChildren().addAll(lonInput, saveLoc);
		
		Line lineBreak2 = new Line(60, 40, 640, 40);
		lineBreak2.setStyle("-fx-stroke-width: 5px; -fx-stroke: #FFFFFF");		
		
		HBox startHbox3 = new HBox();
		startHbox3.setSpacing(10);
		Button go = new Button("GO!");
		Button skipLoc = new Button("Skip this Step");
		Label warning = new Label("Warning: results will not take your location into account");
		warning.setTextFill(Paint.valueOf("#FFFFFF"));
		startHbox3.getChildren().addAll(go, skipLoc, warning);
		
		cen.getChildren().addAll(entLoc, lineBreak1, startHbox1, startHbox2, lineBreak2, startHbox3);
		
		startLayout.setCenter(cen);
		
		Scene startUp = new Scene(startLayout, 700, 700);
		startupStage.setResizable(false);
		startupStage.setScene(startUp);
		startupStage.show();
		entLoc.requestFocus();
		
//MAIN STAGE
		Stage mainStage = new Stage();
		mainStage.setTitle("LOC8R");
		
	//HOME SCENE
		BorderPane homeLayout = new BorderPane();
		
		VBox homeVbox = new VBox();
		
		Label search = new Label("Search by:");
		search.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
		search.setTextFill(Paint.valueOf("#FFFFFF"));
		Line break1 = new Line(20, 40, 700, 40);
		break1.setStyle("-fx-stroke: #FFFFFF; -fx-stroke-width: 5px");
		
		HBox searchButtons = new HBox();
		Button businessType = new Button("Business Type");
		businessType.setMinWidth(200);
		businessType.setMinHeight(40);
		businessType.setFont(Font.font("Helvitica", 20));
		businessType.setTextFill(Paint.valueOf("#FFFFFF"));
		businessType.setStyle("-fx-padding: 9 15 15 15; -fx-background-insets: 0,0 0 5 0, 0 0 6 0, 0 0 7 0;" + 
			    			  "-fx-background-radius: 8; -fx-background-color: \r\n" + 
			    			  "linear-gradient(from 0% 93% to 0% 100%, #249baf 0%, #39B7CD 100%)," + 
			    			  "#249baf, #39B7CD, radial-gradient(center 50% 50%, radius 100%, #249baf, #39B7CD);" + 
			    			  "-fx-font-weight: bold; -fx-font-size: 1.1em;");
		Button businessName = new Button("Name");
		businessName.setMinWidth(200);
		businessName.setMinHeight(40);
		businessName.setFont(Font.font("Helvitica", 20));
		businessName.setTextFill(Paint.valueOf("#FFFFFF"));
		businessName.setStyle("-fx-padding: 9 15 15 15; -fx-background-insets: 0,0 0 5 0, 0 0 6 0, 0 0 7 0;" + 
			    			  "-fx-background-radius: 8; -fx-background-color: \r\n" + 
			    			  "linear-gradient(from 0% 93% to 0% 100%, #249baf 0%, #39B7CD 100%)," + 
			    			  "#249baf, #39B7CD, radial-gradient(center 50% 50%, radius 100%, #249baf, #39B7CD);" + 
			    			  "-fx-font-weight: bold; -fx-font-size: 1.1em;");
		Button nearest = new Button("Nearest");
		nearest.setMinWidth(200);
		nearest.setMinHeight(40);
		nearest.setFont(Font.font("Helvitica", 20));
		nearest.setTextFill(Paint.valueOf("#FFFFFF"));
		nearest.setStyle("-fx-padding: 9 15 15 15; -fx-background-insets: 0,0 0 5 0, 0 0 6 0, 0 0 7 0;" + 
			    		 "-fx-background-radius: 8; -fx-background-color: \r\n" + 
			    		 "linear-gradient(from 0% 93% to 0% 100%, #249baf 0%, #39B7CD 100%)," + 
			    		 "#249baf, #39B7CD, radial-gradient(center 50% 50%, radius 100%, #249baf, #39B7CD);" + 
			    		 "-fx-font-weight: bold; -fx-font-size: 1.1em;");
		searchButtons.getChildren().addAll(businessType, businessName, nearest);
		searchButtons.setSpacing(10);
		searchButtons.setAlignment(Pos.TOP_LEFT);
		
		Line break2 = new Line(20, 40, 700, 40);
		break2.setStyle("-fx-stroke: #FFFFFF; -fx-stroke-width: 5px");
		
		HBox searchButtons2 = new HBox();
		searchButtons2.setSpacing(10);
		Button viewFavorites = new Button("View Favorites");
		viewFavorites.setMinWidth(200);
		viewFavorites.setMinHeight(40);
		viewFavorites.setFont(Font.font("Helvitica", 20));
		viewFavorites.setTextFill(Paint.valueOf("#FFFFFF"));
		viewFavorites.setStyle("-fx-padding: 9 15 15 15; -fx-background-insets: 0,0 0 5 0, 0 0 6 0, 0 0 7 0;" + 
							   "-fx-background-radius: 8; -fx-background-color: \r\n" + 
							   "linear-gradient(from 0% 93% to 0% 100%, #249baf 0%, #39B7CD 100%)," + 
							   "#249baf, #39B7CD, radial-gradient(center 50% 50%, radius 100%, #249baf, #39B7CD);" + 
							   "-fx-font-weight: bold; -fx-font-size: 1.1em;");
		Button viewAll = new Button("View All Businesses");
		viewAll.setMinWidth(200);
		viewAll.setMinHeight(40);
		viewAll.setFont(Font.font("Helvitica", 20));
		viewAll.setTextFill(Paint.valueOf("#FFFFFF"));
		viewAll.setStyle("-fx-padding: 9 15 15 15; -fx-background-insets: 0,0 0 5 0, 0 0 6 0, 0 0 7 0;" + 
							   "-fx-background-radius: 8; -fx-background-color: \r\n" + 
							   "linear-gradient(from 0% 93% to 0% 100%, #249baf 0%, #39B7CD 100%)," + 
							   "#249baf, #39B7CD, radial-gradient(center 50% 50%, radius 100%, #249baf, #39B7CD);" + 
							   "-fx-font-weight: bold; -fx-font-size: 1.1em;");
		searchButtons2.getChildren().addAll(viewFavorites, viewAll);
		
		homeVbox.getChildren().addAll(search, searchButtons, break2, searchButtons2);
		homeVbox.setAlignment(Pos.TOP_LEFT);
		homeVbox.setSpacing(40);
		
		Button resetLoc = new Button("Change Location");
		resetLoc.setMinWidth(200);
		resetLoc.setMinHeight(40);
		resetLoc.setFont(Font.font("Helvitica", 20));
		resetLoc.setTextFill(Paint.valueOf("#FFFFFF"));
		resetLoc.setStyle("-fx-padding: 9 15 15 15; -fx-background-insets: 0,0 0 5 0, 0 0 6 0, 0 0 7 0;" + 
							   "-fx-background-radius: 8; -fx-background-color: \r\n" + 
							   "linear-gradient(from 0% 93% to 0% 100%, #249baf 0%, #39B7CD 100%)," + 
							   "#249baf, #39B7CD, radial-gradient(center 50% 50%, radius 100%, #249baf, #39B7CD);" + 
							   "-fx-font-weight: bold; -fx-font-size: 1.1em;");
		homeLayout.setBottom(resetLoc);
		homeLayout.setAlignment(homeLayout.getBottom(), Pos.BOTTOM_RIGHT);
		homeLayout.setCenter(homeVbox);
		//homeLayout.setAlignment(startLayout.getCenter(), Pos.CENTER);
				
		homeLayout.setBackground(Background.EMPTY);
		homeLayout.setStyle("-fx-background-color: #191414");
		homeLayout.setPadding(new Insets(60, 60, 60, 60));
		Scene home = new Scene(homeLayout, 900, 620);
		mainStage.setResizable(false);
		
	//SEARCH BY TYPE SCENE
		BorderPane searchByTypeLayout = new BorderPane();
		searchByTypeLayout.setPadding(new Insets(10, 50, 10, 50));
		searchByTypeLayout.setBackground(Background.EMPTY);
		searchByTypeLayout.setStyle("-fx-background-color: #191414");
		
		HBox searchByTypeHbox = new HBox();
		searchByTypeHbox.setSpacing(10);
		Set<String> types = new HashSet<>();
		for (String s: businessLocations.keySet()) {
			types.add(businessLocations.get(s).getType().substring(0,1).toUpperCase() + businessLocations.get(s).getType().substring(1));
		}
		List<String> typeList = new ArrayList<String>();
		typeList.addAll(types);
		Collections.sort(typeList);
		ObservableList<String> typeOptions = FXCollections.observableArrayList(typeList);
		ComboBox typeOptionsDropdown = new ComboBox(typeOptions);
		typeOptionsDropdown.setVisibleRowCount(10);
		typeOptionsDropdown.setMinWidth(200);
		typeOptionsDropdown.setPromptText("Select a business type");
		Label gap = new Label("                                                                                                                                          ");
		Button refresh1 = new Button("Refresh");
		Label gapT = new Label("");
		Button returnHome1 = new Button("Home");
		returnHome1.setMinWidth(30);
		searchByTypeHbox.getChildren().addAll(typeOptionsDropdown, gap, refresh1, gapT, returnHome1);
		
		searchByTypeLayout.setTop(searchByTypeHbox);
		Scene searchByType = new Scene(searchByTypeLayout, 900, 620);
		
	//SEARCH BY NAME SCENE
		BorderPane searchByNameLayout = new BorderPane();
		searchByNameLayout.setPadding(new Insets(10, 50, 10, 50));
		searchByNameLayout.setBackground(Background.EMPTY);
		searchByNameLayout.setStyle("-fx-background-color: #191414");
		
		HBox searchByNameHbox = new HBox();
		searchByNameHbox.setSpacing(10);
		TextField searchInput = new TextField("");
		searchInput.setPromptText("Search Businesses");
		searchInput.setMinWidth(400);
		Button searchGo = new Button("GO!");
		Button refresh2 = new Button("Refresh");
		Label gapNa = new Label("");
		Button returnHome2 = new Button("Home");
		returnHome2.setMinWidth(30);
		Label gap2 = new Label("                                                               ");
		searchByNameHbox.getChildren().addAll(searchInput, searchGo, gap2, refresh2, gapNa, returnHome2);
		
		searchByNameLayout.setTop(searchByNameHbox);
		
		Scene searchByName = new Scene(searchByNameLayout, 900, 620);
	
	//SEARCH BY NEAREST SCENE
		BorderPane searchByNearestLayout = new BorderPane();
		searchByNearestLayout.setPadding(new Insets(10, 50, 10, 50));
		searchByNearestLayout.setBackground(Background.EMPTY);
		searchByNearestLayout.setStyle("-fx-background-color: #191414");		

		HBox nearestTopHbox = new HBox();
		nearestTopHbox.setSpacing(10);
		Button refresh3 = new Button("Refresh");
		Label gapN = new Label("");
		Button returnHome3 = new Button("Home");
		returnHome3.setMinWidth(30);
		nearestTopHbox.getChildren().addAll(refresh3, gapN, returnHome3);
		nearestTopHbox.setAlignment(Pos.TOP_RIGHT);
		searchByNearestLayout.setTop(nearestTopHbox);
		
		
		Scene searchByNearest = new Scene(searchByNearestLayout, 900, 620);
		
	//VIEW FAVORITES SCENE
		BorderPane favoritesMainLayout = new BorderPane();
		favoritesMainLayout.setPadding(new Insets(10, 40, 10, 40));
		favoritesMainLayout.setBackground(Background.EMPTY);
		favoritesMainLayout.setStyle("-fx-background-color: #191414");
		
		HBox favoritesHBox = new HBox();
		favoritesHBox.setSpacing(10);
		Button refresh4 = new Button("Refresh");
		Label gap3 = new Label("");
		Button returnHome4 = new Button("Home");
		returnHome4.setMinWidth(30);
		
		favoritesHBox.getChildren().addAll(refresh4, gap3, returnHome4);
		favoritesHBox.setAlignment(Pos.TOP_RIGHT);
		
		favoritesMainLayout.setTop(favoritesHBox);
		
		Scene viewFavoritesScene = new Scene(favoritesMainLayout, 900, 620);
		
	//VIEW ALL SCENE
		BorderPane allMainLayout = new BorderPane();
		allMainLayout.setPadding(new Insets(10, 40, 10, 40));
		allMainLayout.setBackground(Background.EMPTY);
		allMainLayout.setStyle("-fx-background-color: #191414");
		
		HBox allHBox = new HBox();
		allHBox.setSpacing(10);;
		Button refresh5 = new Button("Refresh");
		Label gap4 = new Label("");
		Button returnHome5 = new Button("Home");
		returnHome5.setMinWidth(30);
		
		allHBox.getChildren().addAll(refresh5, gap4, returnHome5);
		allHBox.setAlignment(Pos.TOP_RIGHT);
		
		allMainLayout.setTop(allHBox);
		
		Scene viewAllScene = new Scene(allMainLayout, 900, 620);
		
//BUTTONS_________________________________________________________________________________________
		refresh1.setOnAction(e -> {
			searchByTypeLayout.setCenter(null);
			Object temp = typeOptionsDropdown.getValue();
			typeOptionsDropdown.valueProperty().set(null);
			typeOptionsDropdown.valueProperty().set(temp);
			mainStage.setScene(searchByType);
		});
		
		refresh2.setOnAction(e -> {
			searchGo.fire();
		});
		
		refresh3.setOnAction(e -> {
			nearest.fire();
		});
		
		refresh4.setOnAction(e -> {
			viewFavorites.fire();
		});
		
		refresh5.setOnAction(e -> {
			viewAll.fire();
		});
		
		returnHome1.setOnAction(e -> {
			searchByTypeLayout.setCenter(null);
			typeOptionsDropdown.valueProperty().set(null);
			mainStage.setScene(home);
		});
		
		returnHome2.setOnAction(e -> {
			mainStage.setScene(home);
			searchByNameLayout.setCenter(null);
			searchInput.setText("");
		});
		
		returnHome3.setOnAction(e1 -> {
			mainStage.setScene(home);
		});
		
		returnHome4.setOnAction(e -> {
			mainStage.setScene(home);
		});
		
		returnHome5.setOnAction(e -> {
			mainStage.setScene(home);
		});
		
		viewAll.setOnAction(e -> {
			ScrollPane allLayout = new ScrollPane();
			allLayout.setBackground(Background.EMPTY);
			allLayout.setStyle("-fx-background: #191414; -fx-background-insets: 0");
			allLayout.setMaxWidth(820);
			allLayout.setMaxHeight(550);
						
			VBox displayAll = new VBox();
			displayAll.setSpacing(10);
			for (String s: businessLocations.keySet()) {
				HBox temp = outputHbox(s, mainStage);
				displayAll.getChildren().add(temp);
			}
			
			displayAll.setAlignment(Pos.CENTER);
			allLayout.setContent(displayAll);
			
			allMainLayout.setCenter(allLayout);
			
			mainStage.setScene(viewAllScene);
		});
		
		viewFavorites.setOnAction(e -> {
			ScrollPane favoritesLayout = new ScrollPane();
			favoritesLayout.setBackground(Background.EMPTY);
			favoritesLayout.setStyle("-fx-background: #191414; -fx-background-insets: 0");
			//favoritesLayout.setPadding(new Insets(0, 0, 10, 0));
			favoritesLayout.setMaxWidth(820);
			favoritesLayout.setMaxHeight(550);
			//favoritesLayout.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			
			VBox displayFavorites = new VBox();
			displayFavorites.setSpacing(10);
			for (String s: favoriteLocations.keySet()) {
				HBox temp = outputHbox(s, mainStage);
				displayFavorites.getChildren().add(temp);
			}
			
			displayFavorites.setAlignment(Pos.CENTER);
			favoritesLayout.setContent(displayFavorites);
			
			favoritesMainLayout.setCenter(favoritesLayout);
			
			mainStage.setScene(viewFavoritesScene);
		});
		
		resetLoc.setOnAction(e -> {
			mainStage.close();
			startupStage.show();
		});
		
		nearest.setOnAction(e -> {
			if (userLongitude == -1000) {
				Stage locationError = new Stage();
				locationError.setTitle("Error");
				locationError.initModality(Modality.APPLICATION_MODAL);
				locationError.initOwner(mainStage);
	            Label error = new Label("Error: Please enter a latitude and longitude coordinate first");
	            Button errorOk = new Button("OK");
	            //errorOk.setMinWidth(300);
	            VBox errorVbox = new VBox(10);
	            errorVbox.setPadding(new Insets(10, 10, 10, 10));
	            errorVbox.getChildren().addAll(error, errorOk);
	            Scene errorDialogScene = new Scene(errorVbox, 330, 70);
	            locationError.setScene(errorDialogScene);
	            locationError.setResizable(false);
	            locationError.show();
	            
	            errorOk.setOnAction(f -> {
	            	locationError.close();
				});
			}
			else {
				orderLocationsDistance();
				VBox displayNearest = new VBox();
				displayNearest.setSpacing(10);
				int counter1 = 0;
				for (String s: outputLocations.keySet()) {
					counter1 ++;
					
					HBox temp = outputHbox(s, mainStage);
					displayNearest.getChildren().add(temp);
					
					if (counter1 == 8) {
						break;
					}
				}
				displayNearest.setAlignment(Pos.TOP_CENTER);
				displayNearest.setPadding(new Insets(10, 0, 10, 0));
				
				searchByNearestLayout.setAlignment(searchByNearestLayout.getTop(), Pos.TOP_RIGHT);
				searchByNearestLayout.setCenter(displayNearest);
				
				
				mainStage.setScene(searchByNearest);
			}
		});
		
		searchGo.setOnAction(e -> {
			if (searchInput.getText().trim().isEmpty() || searchInput.getText() == null) {
        	}
        	else {
        		orderLocationsName(searchInput.getText().toLowerCase());
    			VBox displayName = new VBox();
    			displayName.setSpacing(10);
    			if (outputLocations.size() < 8) {
    				for (String s: outputLocations.keySet()) {
    					HBox temp = outputHbox(s, mainStage);
    					displayName.getChildren().add(temp);
    				}
    			}
    			else {
    				int counter = 0;
    				for (String s: outputLocations.keySet()) {
    					counter ++;
    					
    					HBox temp = outputHbox(s, mainStage);
    					displayName.getChildren().add(temp);
    					
    					if (counter == 8) {
    						break;
    					}
    				}
    			}
    			displayName.setAlignment(Pos.TOP_CENTER);
    			displayName.setPadding(new Insets(10, 0, 10, 0));
    			searchByNameLayout.setCenter(displayName);
        	}
		});
		
		latInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				KeyCode code = ke.getCode();

			    if (code == KeyCode.TAB && !ke.isShiftDown() && !ke.isControlDown()) {
			        ke.consume();
			        TextField temp = (TextField) ke.getSource();            
			        KeyEvent newEvent 
			          = new KeyEvent(ke.getSource(),
			                     ke.getTarget(), ke.getEventType(),
			                     ke.getCharacter(), ke.getText(),
			                     ke.getCode(), ke.isShiftDown(),
			                     true, ke.isAltDown(),
			                     ke.isMetaDown());

			        temp.fireEvent(newEvent);  
			        lonInput.requestFocus();
			    }
			    if (ke.getCode().equals(KeyCode.ENTER)) {
	                lonInput.requestFocus();
	            }
			}
		});
		
		lonInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				KeyCode code = ke.getCode();

			    if (code == KeyCode.TAB && !ke.isShiftDown() && !ke.isControlDown()) {
			        ke.consume();
			        TextField temp = (TextField) ke.getSource();            
			        KeyEvent newEvent 
			          = new KeyEvent(ke.getSource(),
			                     ke.getTarget(), ke.getEventType(),
			                     ke.getCharacter(), ke.getText(),
			                     ke.getCode(), ke.isShiftDown(),
			                     true, ke.isAltDown(),
			                     ke.isMetaDown());

			        temp.fireEvent(newEvent);  
			        go.requestFocus();
			    }
			    if (ke.getCode().equals(KeyCode.ENTER)) {
	                go.requestFocus();
	                go.fire();
	            }
			}
		});
		
		savedLocDropdown.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
			    if (ke.getCode().equals(KeyCode.ENTER)) {
	                go.requestFocus();
	                go.fire();
	            }
			}
		});
		
		searchInput.setOnKeyReleased(new EventHandler<KeyEvent>() {
	        @Override
			public void handle(KeyEvent ke) {
	        	if (searchInput.getText().trim().isEmpty() || searchInput.getText() == null) {
	        	}
	        	else {
	        		searchGo.fire();
	        	}
	        	if (ke.getCode().equals(KeyCode.ENTER)) {
	                searchGo.fire();
	                searchGo.requestFocus();
	            }
	        }
	    });
		
		businessName.setOnAction(e -> {
			mainStage.setScene(searchByName);
			searchInput.requestFocus();
		});
		
		typeOptionsDropdown.setOnAction(e -> {
			if (typeOptionsDropdown.getValue() == null) {
			}
			else {
				orderLocationsType(((String) typeOptionsDropdown.getValue()).toLowerCase());
				VBox displayType = new VBox();
				displayType.setSpacing(10);
				if (outputLocations.size() < 8) {
					for (String s: outputLocations.keySet()) {
						HBox temp = outputHbox(s, mainStage);
						displayType.getChildren().add(temp);
					}
				}
				else {
					int counter = 0;
					for (String s: outputLocations.keySet()) {
						counter ++;
						
						HBox temp = outputHbox(s, mainStage);
						displayType.getChildren().add(temp);
						
						if (counter == 8) {
							break;
						}
					}
				}
				displayType.setAlignment(Pos.TOP_CENTER);
				displayType.setPadding(new Insets(10, 0, 10, 0));
				searchByTypeLayout.setCenter(displayType);
			}
		});
		
		businessType.setOnAction(e -> {
			mainStage.setScene(searchByType);
		});
		
		skipLoc.setOnAction(e -> {
			userLongitude = -1000;
			userLatitude = -1000;
			updateDistances();
			startupStage.close();
			mainStage.setScene(home);
			mainStage.show();
		});
		
		go.setOnAction(e -> {
			boolean numberError = false;
			try {
				double lon = Double.parseDouble(lonInput.getText());
				double lat = Double.parseDouble(latInput.getText());
				if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
					numberError = true;
				}
			}	catch (Exception ee) {
				numberError = true;
			}
			
				
			if (lonInput.getText().trim().isEmpty() || lonInput.getText() == null ||
				latInput.getText().trim().isEmpty() || latInput.getText() == null) {
				Stage saveLocPopError = new Stage();
				saveLocPopError.setTitle("Error");
				saveLocPopError.initModality(Modality.APPLICATION_MODAL);
				saveLocPopError.initOwner(startupStage);
	            Label error = new Label("Error: Please enter a latitude and longitude coordinate first");
	            Button errorOk = new Button("OK");
	            //errorOk.setMinWidth(300);
	            VBox errorVbox = new VBox(10);
	            errorVbox.getChildren().addAll(error, errorOk);
	            errorVbox.setPadding(new Insets(10, 10, 10, 10));
	            Scene errorDialogScene = new Scene(errorVbox, 330, 70);
	            saveLocPopError.setScene(errorDialogScene);
	            saveLocPopError.setResizable(false);
	            saveLocPopError.show();
	            
	            errorOk.setOnAction(f -> {
	            	saveLocPopError.close();
				});
			}
			else if (numberError) {
				Stage saveLocPopError0 = new Stage();
				saveLocPopError0.setTitle("Error");
				saveLocPopError0.initModality(Modality.APPLICATION_MODAL);
				saveLocPopError0.initOwner(startupStage);
				Label error0 = new Label("Error: Please enter a valid coordinate");
				Button errorOk0 = new Button("OK");
				//errorOk0.setMinWidth(300);
				VBox errorVbox0 = new VBox(10);
				errorVbox0.getChildren().addAll(error0, errorOk0);
				errorVbox0.setPadding(new Insets(10, 10, 10, 10));
				Scene errorDialogScene0 = new Scene(errorVbox0, 214, 70);
				saveLocPopError0.setScene(errorDialogScene0);
				saveLocPopError0.setResizable(false);
				saveLocPopError0.show();
            
				errorOk0.setOnAction(f -> {
					saveLocPopError0.close();
				});
			}
			else {
				userLongitude = Double.parseDouble(lonInput.getText());
				userLatitude = Double.parseDouble(latInput.getText());
				updateDistances();
				startupStage.close();
				mainStage.setScene(home);
				mainStage.show();
			}
			
		});
		
		savedLocDropdown.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    ContextMenu locOptions = new ContextMenu();
                    MenuItem remove = new MenuItem("Remove a saved location");
                    locOptions.getItems().add(remove);
                    savedLocDropdown.setContextMenu(locOptions);
                    remove.setOnAction(ee -> {
                    	Stage removeLocPop = new Stage();
                    	removeLocPop.setTitle("Remove Location");
                    	removeLocPop.initModality(Modality.APPLICATION_MODAL);
                    	removeLocPop.initOwner(startupStage);
        	            //Label fff = new Label("Remove this location");
                    	HBox removeOptions = new HBox();
                    	removeOptions.setSpacing(10);
        	            Button delete = new Button("Remove");
        	            //delete.setMinWidth(300);
        	            delete.setDisable(true);
        	            Button cancel = new Button("Cancel");
        	            removeOptions.getChildren().addAll(delete, cancel);
        	            
        	            ObservableList<String> options = FXCollections.observableArrayList();
        	    		ComboBox removeLocDropdown = new ComboBox(options);
        	    		for (String s : savedLocations.keySet()) {
        	    			removeLocDropdown.getItems().add(s);
        	    		}
        	    		removeLocDropdown.setVisibleRowCount(5);
        	    		removeLocDropdown.setMinWidth(180);
        	    		removeLocDropdown.setPromptText("Select a location");
        	            
        	            VBox errorVbox = new VBox(10);
        	            errorVbox.setPadding(new Insets(10, 10, 10, 10));
        	            errorVbox.getChildren().addAll(removeLocDropdown, removeOptions);
        	            Scene removeLocScene = new Scene(errorVbox, 200, 80);
        	            removeLocPop.setScene(removeLocScene);
        	            removeLocPop.setResizable(false);
        	            removeLocPop.show();
        	            
        	            removeLocDropdown.setOnAction(g -> {
        	            	delete.setDisable(false);
        	            });
        	            
        	            delete.setOnAction(g -> {
        	            	removeMyLocations((String) removeLocDropdown.getValue());
        	            	try {
								saveMyLocations();
							} catch (IOException e1) {
								
							}
        	            	savedLocDropdown.getItems().remove((String) removeLocDropdown.getValue());
        	            	removeLocPop.close();
						});
        	            
        	            cancel.setOnAction(g -> {
        	            	removeLocPop.close();
        	            });
                    });
                }
            }
        });
		
		savedLocDropdown.setOnAction(e -> {
			String n = (String) savedLocDropdown.getValue();
			if (n.compareTo("Reset") == 0) {
				lonInput.setText("");
				latInput.setText("");
			}
			else {
				lonInput.setText(savedLocations.get(n)[0] + "");
				latInput.setText(savedLocations.get(n)[1] + "");
			}
		});
		
		saveLoc.setOnAction(e -> {
			
			boolean numberError = false;
			try {
				double lon = Double.parseDouble(lonInput.getText());
				double lat = Double.parseDouble(latInput.getText());
				if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
					numberError = true;
				}
			}	catch (Exception ee) {
				numberError = true;
			}
			
				
			if (lonInput.getText().trim().isEmpty() || lonInput.getText() == null ||
				latInput.getText().trim().isEmpty() || latInput.getText() == null) {
				Stage saveLocPopError = new Stage();
				saveLocPopError.setTitle("Error");
				saveLocPopError.initModality(Modality.APPLICATION_MODAL);
				saveLocPopError.initOwner(startupStage);
	            Label error = new Label("Error: Please enter a latitude and longitude coordinate first");
	            Button errorOk = new Button("OK");
	            //errorOk.setMinWidth(30);
	            VBox errorVbox = new VBox(10);
	            errorVbox.getChildren().addAll(error, errorOk);
	            errorVbox.setPadding(new Insets(10, 10, 10, 10));
	            Scene errorDialogScene = new Scene(errorVbox, 330, 70);
	            saveLocPopError.setScene(errorDialogScene);
	            saveLocPopError.setResizable(false);
	            saveLocPopError.show();
	            
	            errorOk.setOnAction(f -> {
	            	saveLocPopError.close();
				});
			}
			else if (numberError) {
				Stage saveLocPopError0 = new Stage();
				saveLocPopError0.setTitle("Error");
				saveLocPopError0.initModality(Modality.APPLICATION_MODAL);
				saveLocPopError0.initOwner(startupStage);
				Label error0 = new Label("Error: Please enter a valid coordinate");
				Button errorOk0 = new Button("OK");
				//errorOk0.setMinWidth(300);
				VBox errorVbox0 = new VBox(10);
				errorVbox0.setPadding(new Insets(10, 10, 10, 10));
				errorVbox0.getChildren().addAll(error0, errorOk0);
				Scene errorDialogScene0 = new Scene(errorVbox0, 214, 70);
				saveLocPopError0.setScene(errorDialogScene0);
				saveLocPopError0.setResizable(false);
				saveLocPopError0.show();
            
				errorOk0.setOnAction(f -> {
					saveLocPopError0.close();
				});
			}
			else {
				Double lon = Double.parseDouble(lonInput.getText());
				Double lat = Double.parseDouble(latInput.getText());
			
				Stage saveLocPop = new Stage();
				saveLocPop.setTitle("Save Location");
				saveLocPop.initModality(Modality.APPLICATION_MODAL);
				saveLocPop.initOwner(startupStage);
				VBox saveLocPopVbox = new VBox(10);
				Label savLocPopText = new Label("Enter location name:");
				//Line breakSaveLocPop = new Line(10, 20, 190, 20);
				//breakSaveLocPop.setStyle("-fx-stroke-width: 5px");
				TextField locNameInput = new TextField("");
				locNameInput.setPromptText("Ex. Home");
				//locNameInput.setMaxWidth(185);
				Button saveLocPopBtn = new Button("Save");
				saveLocPopVbox.setPadding(new Insets(10, 10, 10, 10));
				saveLocPopVbox.getChildren().addAll(savLocPopText, locNameInput, saveLocPopBtn);
				Scene dialogScene = new Scene(saveLocPopVbox, 220, 110);
				saveLocPop.setScene(dialogScene);
				saveLocPop.setResizable(false);
				saveLocPop.show();
				savLocPopText.requestFocus();
				
				locNameInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent ke) {
					    if (ke.getCode().equals(KeyCode.ENTER)) {
					    	saveLocPopBtn.requestFocus();
					    	saveLocPopBtn.fire();
			            }
					}
				});
			
				saveLocPopBtn.setOnAction(f -> {
					if (locNameInput.getText().trim().isEmpty() || locNameInput.getText() == null) {
						Stage saveLocPopError2 = new Stage();
						saveLocPopError2.setTitle("Error");
						saveLocPopError2.initModality(Modality.APPLICATION_MODAL);
						saveLocPopError2.initOwner(startupStage);
			            Label error2 = new Label("Error: Please enter a valid name");
			            Button errorOk2 = new Button("OK");
			            //errorOk2.setMinWidth(300);
			            VBox errorVbox2 = new VBox(10);
			            errorVbox2.getChildren().addAll(error2, errorOk2);
			            errorVbox2.setPadding(new Insets(10, 10, 10, 10));
			            Scene errorDialogScene2 = new Scene(errorVbox2, 186, 70);
			            saveLocPopError2.setScene(errorDialogScene2);
			            saveLocPopError2.setResizable(false);
			            saveLocPopError2.show();
			            
			            errorOk2.setOnAction(g -> {
			            	saveLocPopError2.close();
						});
					}
					else if (savedLocations.containsKey(locNameInput.getText())) {
						Stage saveLocPopError3 = new Stage();
						saveLocPopError3.setTitle("Error");
						saveLocPopError3.initModality(Modality.APPLICATION_MODAL);
						saveLocPopError3.initOwner(startupStage);
			            Label error3 = new Label("Error: The name already exists");
			            Button errorOk3 = new Button("OK");
			            //errorOk3.setMinWidth(300);
			            VBox errorVbox3 = new VBox(10);
			            errorVbox3.getChildren().addAll(error3, errorOk3);
			            errorVbox3.setPadding(new Insets(10, 10, 10, 10));
			            Scene errorDialogScene3 = new Scene(errorVbox3, 178, 70);
			            saveLocPopError3.setScene(errorDialogScene3);
			            saveLocPopError3.setResizable(false);
			            saveLocPopError3.show();
			            
			            errorOk3.setOnAction(g -> {
			            	saveLocPopError3.close();
						});
					}
					else {
						addSavedLocation(locNameInput.getText(), lon, lat);
						try {
							saveMyLocations();
						} catch (IOException e1) {
							
						}
						savedLocDropdown.getItems().add(locNameInput.getText());
						savedLocDropdown.setValue(locNameInput.getText());
						saveLocPop.close();
					}
				});
				
			}
			
		});
		

		
    }
    
}