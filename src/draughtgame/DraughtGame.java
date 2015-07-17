                                                                     
//Author: Laurentiu Dan Gheorghe 
//St number: 2842769
//Project: Draughts Game -> GUI & HCI

package draughtgame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DraughtGame extends Application{
    private BorderPane root;
    private DraughtControl board2;
    
    public void init(){
        board2 = new DraughtControl();
        root = new BorderPane(board2);  
    }
    public void stop(){
        
    }
    public void start(Stage primaryStage){
        
        primaryStage.setTitle("Draught Game");
        Scene scene = new Scene(root, 800, 590);
        primaryStage.setScene(scene);
        Image icon = new Image(DraughtGame.class.getResourceAsStream("logos/draught.jpg"));
        primaryStage.getIcons().add(icon);
 
        
        primaryStage.show();
        primaryStage.setResizable(false);

    }
    public static void main(String[] args) {
        // TODO code application logic here
        launch(args);
    }
    
}
class DraughtControl extends Control{
    private DraughtBoard board1;
    DraughtControl(){
        setSkin(new DraughtControlSkin(this));
        board1 = new DraughtBoard();
        this.getChildren().add(board1);
       
        
        board1.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                board1.setGameInteraction(event.getX(), event.getY());
            }
        });

    }   
}
class DraughtControlSkin extends SkinBase<DraughtControl> {
	// default constructor for the class
	public DraughtControlSkin(DraughtControl rc) {
		super(rc);
	}
}

final class DraughtBoard extends GridPane{
    private final int BOARD_SIZE = 8;
    private final int SQUARE_SIZE = 75;


    private Rectangle[][] rect;  
      
    private VBox menuLayout;
    private Button resetGame;
    private Label gameName;
    private Button drawGame;
    private MenuBar menuBar;
    private Menu menu;
    private MenuItem newGameItem;
    private MenuItem saveGameItem;
    private MenuItem loadGameItem;
    private MenuItem quitGameItem;
    private TextField player1_name;
    private TextField player2_name;
    private VBox players_names;
    private Label names;
    private Button names_entered;
    private HBox buttonOptions;
    private int coordX;
    private int coordY;
    private final Circle[][] pCircle;
    private VBox gameNameBox;
    private Label display;
  
    //============display the names of the players and score======
    private String player_Red, player_White;
    private Label display_red, display_white;
    private VBox display_names;
    private int score_red, score_white;
    //-----------------------------------------
    
    //==================================
    private DraughtsPieces boardValues;
    private DraughtsMove [] legalMoves;
    private int selectedRow, selectedCol;
    private int currentPlayer;
    boolean gameInProgress = true;
    boolean validNames = false;
    //=====================================
    private Button cancel;
    private Button ok;
    private Stage dialogStage;
    private boolean multipleWindow = true;
    
    private  int [] storeValues2;
    
    
    public DraughtBoard(){

        pCircle = new Circle[8][8];
        boardValues = new DraughtsPieces();
        newGame();
        
    }
    public void setGameInteraction(double x1, double y1){
        setCoord(x1, y1);
        if(coordX >=0 && coordX <=7 && coordY >=0 && coordY <= 7){
            clickPiece(coordY, coordX);
            initializeCirclesAndSquares();
        }

    }
    public void newGame(){
        configureBoardLayout(this);
        initializeCirclesAndSquares();
        initializeRightControl();
        initializeMenu();
        addSquaresToBoard(this);
        
        currentPlayer = boardValues.red();
  //      legalMoves = boardValues.getLegalMoves(boardValues.red());
        selectedRow = -1;       //no row selected yet
        gameInProgress = false;
        initializeCirclesAndSquares();
        resetGame.setDisable(true);
        legalMoves = boardValues.getLegalMoves(currentPlayer);
    }
    public void clickPiece(int row, int col){
        //if the user selects a valid piece to move
        if(gameInProgress != false) {
        } else {
            return;
        }
        
        deleteStrokeRect();

        drawStrokeRect();
        
        for(int i = 0; i<legalMoves.length; i++){
            if(legalMoves[i].fromRow() == row && legalMoves[i].fromCol() == col){
                selectedRow = row;
                selectedCol = col;
 
                initializeCirclesAndSquares();
            return;     
            }
        }
        
        deleteStrokeRect();
        drawStrokeRect();
        //if no pices has been selected, the user must select a valid piece
        if(selectedRow < 0){
            display.setText("Click on valid piece");
            return;
        }
        deleteStrokeRect();
        
        //proceed if user selects a valid square where the piece will be moved
        for(int i = 0; i<legalMoves.length; i++){
            if(legalMoves[i].fromRow() == selectedRow && legalMoves[i].fromCol() == selectedCol
               && legalMoves[i].toRow() == row && legalMoves[i].toCol() == col){
                    doMakeMove(legalMoves[i]);
            return;
            }
        }

        display.setText("Click on the square");
        deleteStrokeRect();
    }
    
    //draw strokes for valid moves on the rectangles
    public void drawStrokeRect(){
        if(legalMoves.length == 0)
            return;
        for(int i = 0; i<legalMoves.length; i++){
            int row = legalMoves[i].fromRow();
            int col = legalMoves[i].fromCol();
            rect[col][row].setStroke(Color.YELLOW);
            rect[col][row].setStrokeWidth(2);
            rect[col][row].setStrokeType(StrokeType.INSIDE);
            
            int a = legalMoves[i].toRow();
            int b = legalMoves[i].toCol();
            if(coordY == row && coordX == col){
                rect[b][a].setStroke(Color.YELLOW);
                rect[b][a].setStrokeWidth(2);
                rect[b][a].setStrokeType(StrokeType.INSIDE);
            }
        }
    }
    
    //remove all the strokes from the rectangles
    public void deleteStrokeRect(){
        for(int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                rect[i][j].setStroke(null);
            }
        }
    }

    public void doMakeMove(DraughtsMove move){
        
        boardValues.makeMove(move);
        //moved to here
        if(currentPlayer == boardValues.red()){
            display.setText(player_White+"\'s turn");
        }
        else{
            display.setText(player_Red+"\'s turn");
        }
        
        //update the score for both player
        int white = boardValues.countWhite();
        int red = boardValues.countRed();
        
        display_red.setText("RED:     ["+score_red+"] -> "+player_Red+" ("+red+")");
        display_white.setText("WHITE: ["+score_white+"] -> "+player_White+" ("+white+")");
        //--------------------------------------------------------------------
        
        if(move.isJump()){
            legalMoves = boardValues.getLegalJumpsFrom(currentPlayer, move.toRow(), move.toCol());
            if(legalMoves != null){
                if(currentPlayer == boardValues.red())
                    display.setText(player_Red+" Continue Jumping");
                else
                    display.setText(player_White+" Continue Jumping");
                
                selectedRow = move.toRow();
                selectedCol = move.toCol();
             //   initializeCirclesAndSquares();
                return;
            }
        }
        
        //swap the player
        if(currentPlayer == boardValues.red()){
            currentPlayer = boardValues.white();
            legalMoves = boardValues.getLegalMoves(currentPlayer);
            
            if(legalMoves == null){
                display.setText("Game over. "+player_Red+" wins");
                gameInProgress = false;
                resetGame.setDisable(false);
                score_red++;    //update score
                currentPlayer = boardValues.red();  //winner starts the next game
                //display final score after the match ends
                display_red.setText("RED:    ["+score_red+"]>"+player_Red+" ("+red+")");
                display_white.setText("WHITE:["+score_white+"]>"+player_White+" ("+white+")");
                return;
            }
            
            else if(legalMoves[0].isJump()){
                display.setText(player_White +" Make Your Jump");
            }
  
        }
        else{
            currentPlayer = boardValues.red();
            legalMoves = boardValues.getLegalMoves(currentPlayer);
            if(legalMoves == null){
                display.setText("Game Over. "+player_White+" Wins");
                gameInProgress = false;
                resetGame.setDisable(false);
                score_white++;  //update score
                currentPlayer = boardValues.white();    //winner starts the next game;
                display_red.setText("RED:    ["+score_red+"]>"+player_Red+" ("+red+")");
                display_white.setText("WHITE:["+score_white+"]>"+player_White+" ("+white+")");
                return;
            }
            else if(legalMoves[0].isJump())
                display.setText(player_Red+" Make Your Jump");
    
        }
        
        selectedRow = -1;
 
        if(legalMoves != null){
          boolean check = true;
            for(int i = 1; i<legalMoves.length; i++){
                if(legalMoves[i].fromRow() != legalMoves[0].fromRow()
                || legalMoves[i].fromCol() != legalMoves[0].fromCol()){
                    check = false;
                }
                if(check){
                    selectedRow = legalMoves[0].fromRow();
                    selectedCol = legalMoves[0].fromCol();
                }
                break;
            }
            
        }
        
        //redisplay the updated circles on the board
    initializeCirclesAndSquares();

    }

    private void initializeCirclesAndSquares(){

    //remove the old circles    
    for(int i = 0; i<8; i++){
        for(int j = 0; j<8; j++){
            getChildren().remove(pCircle[i][j]);
        }
    }
    
    //add the circles to the board according to the values that are hold 
    //by the array from Class DraughtsPIeces     
        for(int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                if(boardValues.pieceAt(i, j) == 1){     //red circle
                    pCircle[i][j] = new Circle(SQUARE_SIZE/2-5, Color.RED);
                    this.add(pCircle[i][j], j, i);
                }
                else if(boardValues.pieceAt(i, j) == 2){    //king red
                    pCircle[i][j] = new Circle(SQUARE_SIZE/2-5, Color.RED);
                    pCircle[i][j].setStroke(Color.BLUE);
                    pCircle[i][j].setStrokeType(StrokeType.INSIDE);
                    pCircle[i][j].setStrokeWidth(6);
                    this.add(pCircle[i][j], j, i);
                }
                else if(boardValues.pieceAt(i, j) == 3){     //white circle
                    pCircle[i][j] = new Circle(SQUARE_SIZE/2-5, Color.WHITE);
                    this.add(pCircle[i][j], j, i);
                }
                else if(boardValues.pieceAt(i, j) == 4){    //king white
                    pCircle[i][j] = new Circle(SQUARE_SIZE/2-5, Color.WHITE);
                    pCircle[i][j].setStroke(Color.BLUE);
                    pCircle[i][j].setStrokeType(StrokeType.INSIDE);
                    pCircle[i][j].setStrokeWidth(6);
                    this.add(pCircle[i][j], j, i);
                }
            }
        }
    }

    public void initializeMenu(){
        menuLayout  = new VBox();
        menuBar     = new MenuBar();
        menu        = new Menu("Game Extra Options");

        newGameItem     = new MenuItem("New Game");
        saveGameItem    = new MenuItem("Save Game");
        loadGameItem    = new MenuItem("Load Game");
        quitGameItem    = new MenuItem("Quit Game");
        
        menuLayout.getChildren().add(menuBar);
        menuBar.getMenus().add(menu);
        menu.getItems().addAll(newGameItem, saveGameItem, loadGameItem, quitGameItem);
        
        this.add(menuLayout, 8, 0); 
        
        //saving game with menuitem
        newGameItem.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                if(gameInProgress || validNames){         //pop up message if game in progress
                    
                    if(multipleWindow){     //avoid multiple windows to popup
                       popUpDialog("Game is ON. Start new Game?"); 
                        multipleWindow = false;     //prevent reopen the window
                        //if button ok is pressed reset the game
                        ok.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                gameInProgress = false;
                                boardValues.boardReset();
                                initializeCirclesAndSquares();
                                player_Red =" ";
                                player_White =" ";
                                display_red.setText(" ");
                                display_white.setText(" ");
                                validNames = false;
                                player1_name.setText("Player RED(less 15 letters)");
                                player2_name.setText("Player WHITE(less 15 letters)");
                                display.setText("New Game. Enter Names!"); 
                                multipleWindow = true;
                                resetGame.setDisable(true);
                                dialogStage.close();
                            }
                        });
                        
                        //if button cancel is pressed, resume the game
                        cancel.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                multipleWindow = true;
                                dialogStage.close();
                            }
                        });
                        
                        //if close window button is closed, resume the game
                        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                           public void handle(WindowEvent event) {
                               multipleWindow = true;
                               dialogStage.close();
                           }
                       });
                    }
                }
            }
        });
        
        //save the game state into a file
        saveGameItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if(gameInProgress){
                    if(multipleWindow){     //prevent multiple windows to pop up
                        multipleWindow = false;
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save the Game");

                        StringBuffer t = new StringBuffer(""+player_Red+"\n"
                                     +player_White+"\n"
                                     +score_red+"\n"
                                     +score_white+"\n"
                                     +currentPlayer+"\n"
                                     +boardValues.saveBoardValues());
       
                        File file = fileChooser.showSaveDialog(dialogStage);
                        try{
                        if(file != null){
                            multipleWindow = true;
                            display.setText("Game Saved");
                            FileWriter fileWriter = null;
                            fileWriter = new FileWriter(file);
                            fileWriter.write(t.toString());
                            fileWriter.close();

                        }
                        else{multipleWindow = true;}
                        }
                        catch(Exception e){display.setText("Error. Cant Save Game.");}
                    }
                }
            }
        });
        
        //loading a game
        loadGameItem.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                if(gameInProgress){         //if game in progress alert the player
                    if(multipleWindow){     //avoid multiple window to pop up
                        multipleWindow = false;
                        
                        popUpDialog("Game in Progress. Load Game?");
                        ok.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                loadSavedGame();
                                gameInProgress = true;
                                multipleWindow = true;
                                selectedRow = -1;
                                validNames = true;
                                
                                //load the new pieces on the board
                                int k=0;
                                for(int i = 0; i<8; i++){
                                    for(int j = 0; j<8; j++){
                                        boardValues.setValue(i, j, storeValues2[k]);
                                        k++;
                                    }
                                }
                                legalMoves = boardValues.getLegalMoves(currentPlayer);
                                storeValues2 = null;             //empty the array;
                                initializeCirclesAndSquares();   //initialize the pieces on board
                                deleteStrokeRect();             //delete previous rect strokes
                                dialogStage.close();            //close the window;
                            }
                        });
                        cancel.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                multipleWindow = true;
                                dialogStage.close();
                            }
                        });
                        
                        //close the stage if close button is pressed
                        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            public void handle(WindowEvent event) {
                                multipleWindow = true;
                                dialogStage.close();
                                
                            }
                        });
                    }
                }
                else if(!gameInProgress){       //if no game is started allow loading
                        if(multipleWindow){     //avoid multiple window to pop up
                        multipleWindow = false;
                        
                        popUpDialog("Load a Game?");
                        ok.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                loadSavedGame();
                                gameInProgress = true;
                                multipleWindow = true;
                                selectedRow = -1;
                                validNames = true;
                                //load the new pieces on the board
                                int k=0;
                                for(int i = 0; i<8; i++){
                                    for(int j = 0; j<8; j++){
                                        boardValues.setValue(i, j, storeValues2[k]);
                                        k++;
                                    }
                                }
                                legalMoves = boardValues.getLegalMoves(currentPlayer);
                                storeValues2 = null;             //empty the array;
                                initializeCirclesAndSquares();   //initialize the pieces on board
                                dialogStage.close();            //close the window;
                            }
                        });
                        cancel.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                multipleWindow = true;
                                dialogStage.close();
                            }
                        });
                        
                        //close the stage if the close button is pressed
                        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            public void handle(WindowEvent event) {
                                multipleWindow = true;
                                dialogStage.close();
                                
                            }
                        });
                    }
                }
            }
        });
        
        
        
        //quit menu item, if game in progress pop up a window
        //with to confirm the exit from game
        //if game is not in progress, simply exit
        quitGameItem.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){

                if(!gameInProgress)
                    quitGame();
                else{
                    if(multipleWindow){
                        popUpDialog("Are you sure you want to exit?");
                        multipleWindow = false;     //prevent multiple windows to pop up
                        //in case when user selects ok
                        ok.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent e){
                                quitGame();
                                multipleWindow = true;
                            }
                        });
                        //in case user selects cancel
                        cancel.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent e){
                                dialogStage.close();
                                multipleWindow = true;
                            }
                        });
                        //in case the use presses the close mark
                        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            public void handle(WindowEvent event) {
                                dialogStage.close();
                                multipleWindow = true;
                            }
                        });
                   }
                }
            }
        });
        
    }
    
    //function to load a file and update the currentPlayer, score, boardValues
    //players names
    public void loadSavedGame(){
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(dialogStage);
        
        int[]storeValues = new int[64];
        int j = 0;
        try{
            String line ="";
            BufferedReader input = new BufferedReader(new FileReader(file));
 
            if((line = input.readLine()) != null){      //get the name of the red player
                player_Red = line;
            }
            if((line = input.readLine()) != null){      //get the name of the white player
                player_White = line;
            }
            if((line = input.readLine()) != null){      //get the score of the red player
                score_red = Integer.parseInt(line);
            }
            if((line = input.readLine()) != null){      //get the score of the white player
                score_white = Integer.parseInt(line);
            }
           
            if((line = input.readLine()) != null){      //get the current player
                currentPlayer = Integer.parseInt(line);
            }

            if((line = input.readLine()) != null){    //retrieve the board values   
                for(int i = 0; i < line.length(); i++){
                    int num;
                    char b; 
                    b = line.charAt(i);
                    num = Character.getNumericValue(b);
                    storeValues[j] = num;
                    j++;
                
                }
                
            }
            //store all the valus in storeValues2
            storeValues2 = new int[64];
            storeValues2 = storeValues;

            //----------------------------------------------------------------
            input.close();
            }catch(IOException | NumberFormatException e){display.setText("Cant load Game");}
        
            display.setText("Game Uploaded");
            int white = boardValues.countWhite();
            int red = boardValues.countRed();
            display_red.setText("RED:     ["+score_red+"] -> "+player_Red+" ("+red+")");
            display_white.setText("WHITE: ["+score_white+"] -> "+player_White+" ("+white+")");
    }
    
    //right size of the game withou the menu included, buttons and textfields etc.
    public void initializeRightControl(){
        gameName        = new Label("Draughts Game");
        gameNameBox     = new VBox();
        gameNameBox.getChildren().add(gameName);
        resetGame       = new Button("Reset Game");
        drawGame        = new Button("Draw");
        buttonOptions   = new HBox();
        buttonOptions.getChildren().addAll(resetGame, drawGame);
        displayGameName();
        
        names           = new Label("Players Names");
        player1_name    = new TextField();
        player2_name    = new TextField();
        names_entered   = new Button("Start Game");
        players_names   = new VBox();
        display         = new Label("Enter player\'s names");
        display.setFont(new Font(16));
        
        //display players names
        display_red = new Label("");
        display_white = new Label("");
        display_names = new VBox();
        display_white.setFont(new Font(14));
        display_red.setFont(new Font(14));
        names.setFont(new Font(18));
        names_entered.setFont(new Font(14));
        display_names.getChildren().addAll(display_red, display_white);
        this.add(display_names, 8, 3);
        //----------------------
        
        this.add(display, 8, 2);        //add the display label  
          
        
        resetGame.setPrefSize(300, 200);
        drawGame.setPrefSize(300, 200);
        
        players_names.getChildren().addAll(names, player1_name, player2_name, names_entered);
        player1_name.setText("Player RED(less 15 letters)");
        player2_name.setText("PLayer WHITE(less 15 letters)");
        names_entered.setPrefWidth(210);
        names_entered.setPadding(new Insets(10, 35, 10, 35));
        
        
        players_names.setSpacing(5);
        
        
       // multipleWindow = true;
        //draw button is active only if game in progress, and prevents 
        //to open multiple windows when clicked
        drawGame.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                if(multipleWindow){
                  if(gameInProgress){
                    if(currentPlayer == boardValues.red() || currentPlayer == boardValues.white()){
                        if(currentPlayer == boardValues.red())
                            popUpDialog(""+player_White+" do you draw?");
                        else{
                            popUpDialog(""+player_Red+" do you draw?");
                        }
                        
                        dialogStage.setTitle("Paused Game");
                        multipleWindow = false;
                        gameInProgress = false;     //wait for an answer from the opponent
                        ok.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                               
                                gameInProgress = false;
                                display.setText("Draw Game. Click Reset");
                                resetGame.setDisable(false);
                                multipleWindow = true;
                                dialogStage.close();
                                
                            }
                        });
                        cancel.setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent event){
                                display.setText("No Draw Accepted!");
                                multipleWindow = true;
                                gameInProgress = true;  //return back to game;
                                dialogStage.close();
                            }
                        });
                        //if use presses the close button on right top side (not cancel of ok)
                        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                            public void handle(WindowEvent e){
                                gameInProgress = true;
                                multipleWindow = true;
                                display.setText("No Draw Accepted!");
                                dialogStage.close();
                            }
                        });
                        
                    }
   
                }
              
                }
        
            }
        });
        
        //delete the text player one when mouse pressed 
        player1_name.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                player1_name.setText("");
            }
        });
        //delete the text for textfield when mouse pressed
        player2_name.setOnMouseClicked(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                player2_name.setText("");
            }
        });
        
        //reset game button works only if there is a draw or the game is finished
        //also the game is not in progress
        resetGame.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {

                if(gameInProgress == false && validNames == true){
                    display_red.setText("RED:    ["+score_red+"]>"+player_Red+" (12)");
                    display_white.setText("WHITE:["+score_white+"]>"+player_White+" (12)");
                    boardValues.boardReset();
                    initializeCirclesAndSquares();
                    legalMoves = boardValues.getLegalMoves(currentPlayer);
                    gameInProgress = true;
                    resetGame.setDisable(true);
                }
            }
        });
        
        //text fields with the names of the players
        //the names entered are checked for validity
        //length no longer than 15 letter lower or uppper case,
        //no symbols allowed
        names_entered.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if(gameInProgress == true){
                    display.setText("Game in Progress");
                }
                else if(validNames){
                    display.setText("Press Reset Game");
                }
                else{
                    String s1 = player1_name.getText();
                    String s2 = player2_name.getText();
                    if(checkInputNames(s1) && checkInputNames(s2)){
                        player_Red = player1_name.getText();
                        player_White = player2_name.getText();
                        display_red.setText("RED:    [0]>"+player_Red+" (12)");
                        display_white.setText("WHITE:[0]>"+player_White+" (12)");
                        player1_name.setText(null);
                        player2_name.setText(null);
                        gameInProgress = true;
                        display.setText("Valid Names");
                        validNames = true;
                        score_red = 0; 
                        score_white = 0;
                    }
                    else{
                        display.setText("Invalid Name");
                    }
                }
            }
        });
        
        

        this.add(gameNameBox, 8, 1);
        this.add(buttonOptions, 8, 4);
        this.add(players_names, 8, 6);
    }
    
    //create a new stage
    //for pop up window with different messages
    //couldn't use the dialog library
    public void popUpDialog(String question){
        dialogStage = new Stage();

        VBox all = new VBox();
        HBox hb = new HBox();
        Label label = new Label();
        label.setFont(new Font(16));
        label.setText(question);
        
        ok = new Button("OK");
        ok.setPadding(new Insets(5, 20, 5, 20));
        ok.setMaxWidth(Double.MAX_VALUE);
        cancel = new Button("Cancel");
        cancel.setPadding(new Insets(5, 10, 5, 10));
        cancel.setMaxWidth(Double.MAX_VALUE);
        
        hb.getChildren().addAll(ok, cancel);
        hb.setSpacing(100);
        all.setSpacing(35);
       
        
        all.getChildren().addAll(label, hb);
        all.setPadding(new Insets(50, 20, 20, 50));
        GridPane sp = new GridPane();
        Image icon = new Image(DraughtGame.class.getResourceAsStream("logos/question.jpg"));
        dialogStage.getIcons().add(icon);
        
        sp.getChildren().add(all);
   
        Scene scene2 = new Scene(sp, 300, 200);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setScene(scene2);
        dialogStage.setResizable(false);
        dialogStage.show(); 
    
    }
    
    //function to check the user input for names entered
    public boolean checkInputNames(String name){
        name = name+"";
        String checkName = name.trim();  //remove any white spaces
        boolean check = true;

        if(name.length() > 15)
            return false;
        if(checkName.length() == 0)
            return false;
        else{
            int i = 0;
            while(i<checkName.length() && check){
                int a = (int)checkName.charAt(i);
                if(a >= 65 && a <= 90 || a >=97 && a<=122){i++;}
                else{
                    check = false;
                    i++;
                }
            } 
        }
        return check;
    }

    //display the game's name on the right side
    public void displayGameName(){
        gameName.setFont(Font.font("null", FontWeight.EXTRA_BOLD, 25));
        gameName.setTextFill(Color.RED);
        gameName.setCache(true);
        Reflection r = new Reflection();
        r.setFraction(0.9);
        gameName.setEffect(r);
    }
    
    //add squares to the board
    private void addSquaresToBoard(GridPane board){
    	Color[] squareColors = new Color[] {
    			Color.BLACK, Color.GREEN
    	};
        rect = new Rectangle[8][8];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                rect[col][row] = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, squareColors[(row+col)%2]);
                board.add(rect[col][row], col, row);
            }
        }
    }
    //setting the board
    private void configureBoardLayout(GridPane board){
        for (int i=0; i<BOARD_SIZE; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(SQUARE_SIZE);
            rowConstraints.setPrefHeight(SQUARE_SIZE);
            rowConstraints.setMaxHeight(SQUARE_SIZE);
            rowConstraints.setValignment(VPos.CENTER);
            board.getRowConstraints().add(rowConstraints);

            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setMinWidth(SQUARE_SIZE);
            colConstraints.setMaxWidth(SQUARE_SIZE);
            colConstraints.setPrefWidth(SQUARE_SIZE);
            colConstraints.setHalignment(HPos.CENTER);
            board.getColumnConstraints().add(colConstraints);
        }
    }

    public void quitGame(){
        Platform.exit();
    }
    public void setCoord(double x, double y){
        coordX = (int)(x / 75);
        coordY = (int)(y / 75);
    }
    public int getX(){ return coordX;}
    public int getY(){ return coordY;}
      
}
 
class DraughtsPieces {
      
    private final int EMPTY = 0;          //no piece
    private final int RED = 1;            //red piece
    private final int RED_KING = 2;       //red king piece
    private final int WHITE = 3;          //white piece
    private final int WHITE_KING = 4;     //white king piece;
      
    private final int[][] board;    //board with logic pieces   
      
      //class constructor
    public DraughtsPieces() {
         board = new int[8][8];
         setUpGame();       //initial state of the game
      }

      
      //initialize all the pieces on the board, white on the top side and
      //and red on the bottom
    public void setUpGame() {
         for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
               if ( row % 2 == col % 2 ) {
                  if (row < 3)
                     board[row][col] = WHITE;
                  else if (row > 4)
                     board[row][col] = RED;
                  else
                     board[row][col] = EMPTY;
               }
               else {
                  board[row][col] = EMPTY;
               }
            }
         }
      }  // end setUpGame()
      
      //reset the pieces on the board
      void boardReset(){
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
               if ( row % 2 == col % 2 ) {
                  if (row < 3)
                     board[row][col] = WHITE;
                  else if (row > 4)
                     board[row][col] = RED;
                  else
                     board[row][col] = EMPTY;
               }
               else {
                  board[row][col] = EMPTY;
               }
            }
         }
      
      }
      //count all the red pieces to display them
      public int countRed(){
          int count = 0; 
          for(int i = 0; i<8; i++){
              for(int j = 0; j<8; j++){
                  if(board[i][j] == 1 || board[i][j] == 2)
                      count++;
              }
          }
          return count;
      }
      
      //count all the white pieces to display them
      public int countWhite(){
          int count = 0;
          for(int i = 0; i<8; i++){
              for(int j = 0; j<8; j++){
                  if(board[i][j] == 3 || board[i][j] == 4)
                      count++;
              }
          }
          return count;
      }
      
      //return a piece at a specific location(row and col)
      int pieceAt(int row, int col) {
         return board[row][col];
      }
            
      
      //the move is legal
      void makeMove(DraughtsMove move) {
         makeMove(move.fromRow(), move.fromCol(), move.toRow(), move.toCol());
      }
      
      //makeMove is moving a piece from  fromRow->toRow and fromCol->toCol
      //and the piece jumped will be removed from the board
      void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
         board[toRow][toCol] = board[fromRow][fromCol];
         board[fromRow][fromCol] = EMPTY;
         if (fromRow - toRow == 2 || fromRow - toRow == -2) {
            // The move is a jump.  Remove the jumped piece from the board.
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
            board[jumpRow][jumpCol] = EMPTY;
         }
         if (toRow == 0 && board[toRow][toCol] == RED)
            board[toRow][toCol] = RED_KING;
         if (toRow == 7 && board[toRow][toCol] == WHITE)
            board[toRow][toCol] = WHITE_KING;
      }
      

      //return an array of moves, first is checked the jump moves, if any found
      //return them, else check for normal moves, if none exist return null,
      //return the valid normal moves
    public DraughtsMove[] getLegalMoves(int player) {
         
        if (player != RED && player != WHITE)
            return null;
         
        int playerKing;  // The constant representing a King belonging to player.
        if (player == RED)
            playerKing = RED_KING;
        else
            playerKing = WHITE_KING;
         
        ArrayList<DraughtsMove> moves = new ArrayList<>();  // Moves will be stored in this list.

         //check for any possible jumps, this is checking any piece of the 
         //currentPlayer on the board for any jumps, if any found, store inside
         // the Array list moves
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
               if (board[row][col] == player || board[row][col] == playerKing) {
                  if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                     moves.add(new DraughtsMove(row, col, row+2, col+2));
                  if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                     moves.add(new DraughtsMove(row, col, row-2, col+2));
                  if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                     moves.add(new DraughtsMove(row, col, row+2, col-2));
                  if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                     moves.add(new DraughtsMove(row, col, row-2, col-2));
               }
            }
        }
         

         //if any jumps were found then the user must continue, else check 
         //for any regular moves, and store them in an ArrayList
        if (moves.isEmpty()) {
            for (int row = 0; row < 8; row++) {
               for (int col = 0; col < 8; col++) {
                  if (board[row][col] == player || board[row][col] == playerKing) {
                     if (canMove(player,row,col,row+1,col+1))
                        moves.add(new DraughtsMove(row,col,row+1,col+1));
                     if (canMove(player,row,col,row-1,col+1))
                        moves.add(new DraughtsMove(row,col,row-1,col+1));
                     if (canMove(player,row,col,row+1,col-1))
                        moves.add(new DraughtsMove(row,col,row+1,col-1));
                     if (canMove(player,row,col,row-1,col-1))
                        moves.add(new DraughtsMove(row,col,row-1,col-1));
                  }
               }
            }
        }

         //if no legal moves exit return null, else create a new array and
         //transfer all the values in it
        if (moves.isEmpty())
            return null;
        else {
            DraughtsMove[] moveArray = new DraughtsMove[moves.size()];
            for (int i = 0; i < moves.size(); i++)
               moveArray[i] = moves.get(i);
            return moveArray;
        } 
    }  // end getLegalMoves
      
      
      //getLegalJumpsFrom will return a list with valid jumps from a specific
      //row and col, else return null
    public DraughtsMove[] getLegalJumpsFrom(int player, int row, int col) {
        if (player != RED && player != WHITE)
            return null;
        int playerKing;  // The constant representing a King belonging to currentPlayer
        if (player == RED)
            playerKing = RED_KING;
        else
            playerKing = WHITE_KING;
         
         // The legal jumps will be stored in this list.
        ArrayList<DraughtsMove> moves = new ArrayList<DraughtsMove>();  
        if (board[row][col] == player || board[row][col] == playerKing) {
            if (canJump(player, row, col, row+1, col+1, row+2, col+2))
               moves.add(new DraughtsMove(row, col, row+2, col+2));
            if (canJump(player, row, col, row-1, col+1, row-2, col+2))
               moves.add(new DraughtsMove(row, col, row-2, col+2));
            if (canJump(player, row, col, row+1, col-1, row+2, col-2))
               moves.add(new DraughtsMove(row, col, row+2, col-2));
            if (canJump(player, row, col, row-1, col-1, row-2, col-2))
               moves.add(new DraughtsMove(row, col, row-2, col-2));
        }
        if (moves.isEmpty())
            return null;
        else {
            DraughtsMove[] moveArray = new DraughtsMove[moves.size()];
            for (int i = 0; i < moves.size(); i++)
               moveArray[i] = moves.get(i);
            return moveArray;
         }
      }  // end getLegalMovesFrom()
      
      
      //canJump checks if the currentPlayer can jump from r1,c1 to r3,c3
      //and r2,c2 is the opponent piece between them
    private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {
         
        if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
            return false;  // (r3,c3) is off the board.
         
        if (board[r3][c3] != EMPTY)
            return false;  // (r3,c3) already contains a piece.
         
        if (player == RED) {
            if (board[r1][c1] == RED && r3 > r1)
               return false;  // Regular red piece can only move  up.
            if (board[r2][c2] != WHITE && board[r2][c2] != WHITE_KING)
               return false;  // There is no black piece to jump.
            return true;  // The jump is legal.
        }
        else {
            if (board[r1][c1] == WHITE && r3 < r1)
               return false;  // Regular black piece can only move downn.
            if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
               return false;  // There is no red piece to jump.
            return true;  // The jump is legal.
        }
         
      }  // end canJump()
      
      
      //this method is called by getLegalMoves() to determine if the player
      // can move from r1,c1 to r2,c2
    private boolean canMove(int player, int r1, int c1, int r2, int c2) {
         
        if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
            return false;  // (r2,c2) is off the board.
         
        if (board[r2][c2] != EMPTY)
            return false;  // (r2,c2) already contains a piece.
         
        if (player == RED) {
            if (board[r1][c1] == RED && r2 > r1)
               return false;    // Regular red piece can only move down.
            return true;        // The move is legal.
        }
        else {
            if (board[r1][c1] == WHITE && r2 < r1)
               return false;    // Regular black piece can only move up.
            return true;        // The move is legal.
         }
         
      }  // end canMove()
      
      //method used for saving game, the array values are store in one string;
    public String saveBoardValues(){
          StringBuilder store = new StringBuilder();
          for(int i = 0; i<8; i++){
              for(int j=0; j<8; j++){
                  store.append(board[i][j]);
              }
          }
          return store.toString();
    }
      
      //method used to load a game, setting the previous values 
    public void setValue(int row, int col, int value){
        if(value == 0)
            board[row][col] = EMPTY;
        else if(value == 1)
            board[row][col] = RED;
        else if(value == 2)
            board[row][col] = RED_KING;
        else if(value == 3)
            board[row][col] = WHITE;
        else if(value == 4)
            board[row][col] = WHITE_KING;
         
    }
      
    public int red(){return RED;}
    public int white(){return WHITE;}
      
} // end class CheckersData
   

//Draughts Move is used to get the moves for a specific piece on the board
class DraughtsMove{
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    
    public DraughtsMove(int r1, int c1, int r2, int c2){
        fromRow = r1;
        fromCol = c1;
        toRow = r2;
        toCol = c2;
    }
    
    //check if the move is a jump
    public boolean isJump(){
        return (fromRow - toRow == 2 || fromRow - toRow == -2);
    }
    
    //return the coordinates
    public int fromRow(){return fromRow;}
    public int fromCol(){return fromCol;}
    public int toRow(){return toRow;}
    public int toCol(){return toCol;}
}


