
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class GUI extends javax.swing.JFrame {

    JLabel[] labels = new JLabel[225];
    JLabel[][] labelBoard = new JLabel[15][15];
    Tile[][] board = new Tile[15][15];

    ArrayList<Word> placedWords = new ArrayList<>();
    
    ArrayList<Tile> thisTurnWord;

    final Color cyan = new Color(83, 212, 251);
    final Color blue = new Color(22, 85, 250);
    final Color red = new Color(246, 73, 61);
    final Color pink = new Color(252, 176, 180);
    final Color normal = new Color(0, 160, 133);

    Tile centerTile;

    boolean firstTurn = true;

    Letters letters = new Letters();

    /**
     * the letters the hand has
     */
    String[] bottomPlayerLetters = new String[7];

    ArrayList<String> dictWords = new ArrayList<>();

    Tile blankDroppedTile;

    /**
     * Creates new form GUI
     */
    public GUI() {
        initComponents();

        //////              joker selecting letter setup            /////////////
        for (int i = 0; i < jPanelLetters.getComponentCount(); i++) {
            /*  this loop adds function to the buttons that you pick the "joker"
             to be. when you pick a letter it attemps to move the mouse to the center
             with Robot  */
            final String buttonText = ((JButton)jPanelLetters.getComponent(i)).getText();
            jPanelLetters.getComponent(i).addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent me) {
                    int index = -1;
                    for (int j = 0; j < bottomPlayerLabels.length; j++) {
                        // cycles through the hand to find the Label the user clicked
                        if (bottomPlayerLabels[j] == focusedHandLabel) {
                            index = j;
                        }
                    }
                    focusedLetter = buttonText;

                    try {
                        new Robot().mouseMove(getX() + getWidth() / 2 - 70, getY() + getHeight() / 2 - 5);
                    } catch (AWTException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    blanks[focusedBoardTile.y][focusedBoardTile.x] = true;
                    putLetter(focusedBoardTile, buttonText, index);

                    
                    jFrameJokerSelect.setVisible(false);
                }
            }
            );
        }

        try {
            readDictionary();
            System.out.println();
        } catch (IOException ex) {
        }
        thisTurnWord = new ArrayList<>();

        //adds all board-tile to "labels"
        for (int i = 0; i < labels.length; i++) {
            labels[i] = (JLabel)jPanelTiles.getComponent(i);
        }

        labelBoard = ArrayToMatrix(labels); //convert the 1-dim "labels to 2-dim "labelBoard" (15x15)

        ///////////                   tiles setup                 ///////////////////
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {

                board[i][j] = new Tile(labelBoard[i][j], null, TileType.normal, j, i);

                final int _i = i;
                final int _j = j;

                board[i][j].getLabel().addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        if (!jFrameJokerSelect.isVisible()) { //makes sure you can't click hand while joker window is visible
                            System.out.println(board[_i][_j].letter);
//                            System.out.println(_i + " " + _j);

                            if (thisTurnWord.contains(board[_i][_j])) { //if you clicked a letter that was placed this turn
                                for (int k = 0; k < bottomPlayerLetters.length; k++) {
                                    if (bottomPlayerLetters[k] == null) { //finds the first empty hand-spot
                                        if (blanks[_i][_j] == true) { //checks if the letter retrieved was a joker before placed.
                                            //gives back a joker
                                            bottomPlayerLetters[k] = "_";
                                            bottomPlayerLabels[k].setText("_");
                                            blanks[_i][_j] = false;
                                            break;
                                        } else {
                                            //gives back the letter as-is.
                                            bottomPlayerLetters[k] = board[_i][_j].getLetter();
                                            bottomPlayerLabels[k].setText(board[_i][_j].letter);
                                            break;
                                        }
                                    }
                                }
                                thisTurnWord.remove(board[_i][_j]); //removes the picked letter from letters placed this turn
                                board[_i][_j].letter = null;
                                board[_i][_j].label.setText(null);
                            }
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent me) { //highlights hovered tile
                        if (!jFrameJokerSelect.isVisible()) {
                            board[_i][_j].label.setBackground(paintTile(_i, _j, true));
                            focusedBoardTile = board[_i][_j];
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent me) { // de-highlights exited tile
                        if (!jFrameJokerSelect.isVisible()) {
                            focusedBoardTile = null;
                        }
                        board[_i][_j].label.setBackground(paintTile(_i, _j, false));
                    }

                });
            }
        }
        setBoard();

        for (int i = 0; i < 7; i++) {
            bottomPlayerLabels[i] = (JLabel)jPanelHand.getComponent(i);
            bottomPlayerLetters[i] = letters.getRandomLetter();
            bottomPlayerLetters[0] = "_";
            bottomPlayerLabels[i].setText(bottomPlayerLetters[i]);
            final int _i = i;
            bottomPlayerLabels[i].addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent me) {
                    if (!jFrameJokerSelect.isVisible()) {
                        focusedLetter = bottomPlayerLetters[_i];

                        focusedHandLabel = bottomPlayerLabels[_i];
                        if (focusedLetter != null) {
                            setMouse(focusedLetter);
                            focusedHandLabel.setText(null);
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    if (focusedBoardTile != null) { // put the focusedLetter on the board
                        if (focusedLetter != null) {
                            putLetter(focusedBoardTile, focusedLetter, _i);
                        }
                    } else { // if the letter was "dropped" not on a tile
                        if (hoveredHandLabel == null) { // if the letter was dropped outside of the board or the hand
                            focusedHandLabel.setText(focusedLetter);
                            focusedLetter = null;
                            setCursor(defuaultCursor);
                            focusedHandLabel = null; // removes the reference to the focused label where the letter was taken from
                        } else {
                            if (focusedLetter != null) { // if the letter was dropped on another hand label
                                if (hoveredHandLabel == focusedHandLabel) { //if it was dropped where it was picked up
                                    focusedHandLabel.setText(focusedLetter);
                                    setCursor(defuaultCursor);

                                } else { //if it was dropped on another hand label

                                    // set the released-at label things//
                                    /////////////////////////////////////
                                    String letterAtReleased = null;
                                    if (hoveredHandLabel.getText() != null) {
                                        letterAtReleased = hoveredHandLabel.getText();
                                    }
//                                System.out.println(hoveredHandTile.getText());

                                    hoveredHandLabel.setText(focusedLetter);

                                    for (int j = 0; j < bottomPlayerLabels.length; j++) {
                                        if (bottomPlayerLabels[j] == hoveredHandLabel) {
                                            bottomPlayerLetters[j] = focusedLetter;
                                        }
                                    }

                                    //////////////////////////////////
                                    // set the original label things//
                                    //////////////////////////////////
                                    if (letterAtReleased != null) {
                                        focusedHandLabel.setText(letterAtReleased);
                                    } else {
                                        focusedHandLabel.setText(null);
                                    }

                                    bottomPlayerLetters[_i] = letterAtReleased;

                                    ////////////////////////////////////
                                    // nullify and change other things//
                                    ////////////////////////////////////
                                    focusedLetter = null;
                                    focusedHandLabel = null;
                                    setCursor(defuaultCursor);
                                }
                            }
                        }
                    }
//                    for (int j = 0; j < bottomPlayer.length; j++) {
//                        System.out.print(bottomPlayer[j] + "  ");
//                    }System.out.println();
                }

                @Override
                public void mouseEntered(MouseEvent me) {
                    hoveredHandLabel = bottomPlayerLabels[_i];
//                    System.out.println("hoverhanded " + _i + "  " + hoveredHandTile.getText() + " " + focusedLetter);
                }

                @Override
                public void mouseExited(MouseEvent me) {
                    hoveredHandLabel = null;
                }

            });;
        }

        centerTile = board[7][7];

//        addKeyListener(this);
    }

    java.awt.Cursor defuaultCursor = this.getCursor();

    boolean[][] blanks = new boolean[board.length][board[0].length];

    /**
     * place a letter on the board.
     *
     * @param tile the tile you want to place a letter in
     * @param str the letter you place
     * @param placeInPlayerHand where the letter was in the hand
     */
    public void putLetter(Tile tile, String str, int placeInPlayerHand) {
        if (str.equals("_")) {
            jFrameJokerSelect.setSize(874, 225);
            jFrameJokerSelect.setLocationRelativeTo(this);
            jFrameJokerSelect.setLocation(this.getX() + this.getWidth(), this.getHeight() / 2 - 20);

            setCursor(defuaultCursor);

            jFrameJokerSelect.setVisible(true);
        } else {
            if (ValidLetterPlacement(tile)) {
                tile.label.setText(focusedLetter); // assign the letter to the board tile
                focusedBoardTile = null; //nullify the refernce to the board tile
                focusedLetter = null; //nullify the reference to the letter
                bottomPlayerLetters[placeInPlayerHand] = null; // set the backstage letter of the player to null
                focusedHandLabel.setText(null); // set the text of the label to null
                setCursor(defuaultCursor);
                focusedHandLabel = null; //removes the reference to the focused label

            } else {
                focusedHandLabel.setText(focusedLetter);
                focusedLetter = null;
                setCursor(defuaultCursor);
                focusedHandLabel = null;
            }
        }
//        for (Tile _tile : thisTurnWord) {
//            System.out.print(board[_tile.y][_tile.x].letter + " ");
//        }
//        System.out.println();
        
    }

    boolean ValidLetterPlacement(Tile tile) {
        if (firstTurn) {
            if (thisTurnWord.isEmpty()) {
                thisTurnWord.add(tile);

                tile.setLetter(focusedLetter);

                return true;
            } else {
                Tile placedLetter = thisTurnWord.get(thisTurnWord.size() - 1); // the last letter placed
                if ((tile.x == placedLetter.x && tile.y - placedLetter.y == 1)
                        || (tile.y == placedLetter.y && tile.x - placedLetter.x == 1)) {
                    if (thisTurnWord.size() >= 2) {
                        Tile firstLetter = thisTurnWord.get(0);
                        if (tile.x == firstLetter.x || tile.y == firstLetter.y) {
                            if (tile.letter == null) {
                                thisTurnWord.add(tile);

                                tile.setLetter(focusedLetter);

                                return true;
                            }
                        }
                    } else {
                        if (tile.letter == null) {
                            thisTurnWord.add(tile);

                            tile.setLetter(focusedLetter);

                            return true;
                        }
                    }
                }
            }
        } else {

            final int up = 0, right = 1, down = 2, left = 3;
            ArrayList<Integer> adjacents = new ArrayList<>();

            if (board[tile.y][tile.x].letter != null) {
                System.out.println(false);
                return false;
            }

            boolean bool = false;
            if (tile.y == 0) {
                if (board[tile.y][tile.x + 1] != null || board[tile.y][tile.x - 1] != null
                        || board[tile.y + 1][tile.x] != null) {
                    if (board[tile.y][tile.x + 1].letter != null || board[tile.y][tile.x - 1].letter != null
                            || board[tile.y + 1][tile.x].letter != null) {
                        if (board[tile.y][tile.x + 1].letter != null) {
                            adjacents.add(right);
                        }
                        if (board[tile.y][tile.x - 1].letter != null) {
                            adjacents.add(left);
                        }
                        if (board[tile.y + 1][tile.x].letter != null) {
                            adjacents.add(down);
                        }
                        bool = true;
                    }
                }
            } else if (tile.y == board.length - 1) { // max y
                if (board[tile.y][tile.x + 1] != null || board[tile.y][tile.x - 1] != null
                        || board[tile.y - 1][tile.x] != null) {
                    if (board[tile.y][tile.x + 1].letter != null || board[tile.y][tile.x - 1].letter != null
                            || board[tile.y - 1][tile.x].letter != null) {

                        if (board[tile.y][tile.x + 1].letter != null) {
                            adjacents.add(right);
                        }
                        if (board[tile.y][tile.x - 1].letter != null) {
                            adjacents.add(left);
                        }
                        if (board[tile.y - 1][tile.x].letter != null) {
                            adjacents.add(up);
                        }

                        bool = true;
                    }
                }
            } else if (tile.x == 0) {
                if (board[tile.y][tile.x + 1] != null
                        || board[tile.y - 1][tile.x] != null || board[tile.y + 1][tile.x] != null) {
                    if (board[tile.y][tile.x + 1].letter != null
                            || board[tile.y - 1][tile.x].letter != null || board[tile.y + 1][tile.x].letter != null) {

                        if (board[tile.y][tile.x + 1].letter != null) {
                            adjacents.add(right);
                        }
                        if (board[tile.y + 1][tile.x].letter != null) {
                            adjacents.add(down);
                        }
                        if (board[tile.y - 1][tile.x].letter != null) {
                            adjacents.add(up);
                        }

                        bool = true;
                    }
                }
            } else if (tile.x == board[0].length - 1) { // max x
                if (board[tile.y][tile.x - 1] != null
                        || board[tile.y - 1][tile.x] != null || board[tile.y + 1][tile.x] != null) {
                    if (board[tile.y][tile.x - 1].letter != null
                            || board[tile.y - 1][tile.x].letter != null || board[tile.y + 1][tile.x].letter != null) {

                        if (board[tile.y][tile.x - 1].letter != null) {
                            adjacents.add(left);
                        }
                        if (board[tile.y + 1][tile.x].letter != null) {
                            adjacents.add(down);
                        }
                        if (board[tile.y - 1][tile.x].letter != null) {
                            adjacents.add(up);
                        }

                        bool = true;
                    }
                }
            } else {
                if (board[tile.y][tile.x + 1] != null || board[tile.y][tile.x - 1] != null
                        || board[tile.y - 1][tile.x] != null || board[tile.y + 1][tile.x] != null) {
                    if (board[tile.y][tile.x + 1].letter != null || board[tile.y][tile.x - 1].letter != null
                            || board[tile.y - 1][tile.x].letter != null || board[tile.y + 1][tile.x].letter != null) {
                        if (board[tile.y][tile.x + 1].letter != null) {
                            adjacents.add(right);
                        }
                        if (board[tile.y][tile.x - 1].letter != null) {
                            adjacents.add(left);
                        }
                        if (board[tile.y + 1][tile.x].letter != null) {
                            adjacents.add(down);
                        }
                        if (board[tile.y - 1][tile.x].letter != null) {
                            adjacents.add(up);
                        }

                        bool = true;
                    }
                }
            }
            if (bool) {

            }
            System.out.println(bool);
            return bool;
        }
        return false;
    }

    public boolean ValidWordTurn() { // print why it might be invalid ?
        String theWord = "";
        boolean bool;
        for (int i = 0; i < thisTurnWord.size(); i++) {
            theWord += thisTurnWord.get(i).getLetter().toUpperCase();
        }
        if (dictWords.contains(theWord)) {
            bool = true;
            if (firstTurn) {
                boolean PlacedInCenter = false;
                for (Tile tile : thisTurnWord) {
                    if (tile == centerTile) {
                        PlacedInCenter = true;
                    }
                }
                bool = PlacedInCenter;
            }
        } else {
            bool = false;
        }
        System.out.println(bool);
        return bool;
    }

    /**
     * Array of labels in the hand
     */
    JLabel[] bottomPlayerLabels = new JLabel[7];

    public Color paintTile(int i, int j, boolean b) {
        if (b) {
            switch (board[i][j].type) {
                case normal:
                    return new Color(0, 220, 199);
                case letterx2:
                    return new Color(159, 251, 255);
                case letterx3:
                    return new Color(24, 169, 249);
                case wordx2:
                    return new Color(254, 211, 214);
                case wordx3:
                    return new Color(255, 134, 120);
                case center:
                    return new Color(254, 211, 214);
            }
        } else {
            switch (board[i][j].type) {
                case normal:
                    return normal;
                case letterx2:
                    return cyan;
                case letterx3:
                    return blue;
                case wordx2:
                    return pink;
                case wordx3:
                    return red;
                case center:
                    return pink;
            }
        }
        return null;
    }

    public int CalculateWordValue(ArrayList<Tile> word) {
        return 0;
    }

    /**
     * sets up the board with the layout
     */
    final void setBoard() {
        for (int i = 1; i < 10; i += 4) {
            if (i == 1) {
                for (int j = 5; j < 10; j += 4) {
                    board[i][j].getLabel().setBackground(blue);
                    board[i][j].type = TileType.letterx3;
                }
                for (int j = 5; j < 10; j += 4) {
                    board[13][j].getLabel().setBackground(blue);
                    board[i][j].type = TileType.letterx3;
                }
            } else {
                for (int j = 1; j < 15; j += 4) {
                    board[i][j].getLabel().setBackground(blue);
                    board[i][j].type = TileType.letterx3;
                }
            }
        }
        board[13][5].label.setBackground(blue);
        board[13][9].label.setBackground(blue);
        board[13][5].type = TileType.letterx3;
        board[13][9].type = TileType.letterx3;

        for (int i = 0; i < 15; i += 7) {
            for (int j = 3; j < 15; j += 4) {
                if (j != 7) {
                    board[i][j].getLabel().setBackground(cyan);
                    board[i][j].type = TileType.letterx2;
                }
            }
        }
        for (int i = 3; i < 15; i += 4) {
            for (int j = 0; j < 15; j += 7) {
                if (i != 7) {
                    board[i][j].getLabel().setBackground(cyan);
                    board[i][j].type = TileType.letterx2;
                }
            }
        }

        for (int i = 2; i < 15;) {
            for (int j = 6; j < 9; j += 2) {
                board[i][j].getLabel().setBackground(cyan);
                board[i][j].type = TileType.letterx2;
            }
            if (i == 6) {
                i += 2;
            } else {
                i += 4;
            }
        }
        for (int j = 2; j < 15;) {
            for (int i = 6; i < 9; i += 2) {
                board[i][j].getLabel().setBackground(cyan);
                board[i][j].type = TileType.letterx2;
            }
            if (j == 6) {
                j += 2;
            } else {
                j += 4;
            }
        }

        for (int i = 0; i < 15;) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.normal && ((i != 7 && j != 7))) {
                    board[i][j].getLabel().setBackground(pink);
                    board[i][j].type = TileType.wordx2;
                }
                i++;
            }
        }

        for (int i = 14; i > 0;) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.normal && (i != 7 && j != 7)) {
                    board[i][j].getLabel().setBackground(pink);
                    board[i][j].type = TileType.wordx2;
                }
                i--;
            }
        }

        for (int i = 0; i < 15; i += 7) {
            for (int j = 0; j < 15; j += 7) {
                if (i == 7 && j == 7) {
                } else {
                    board[i][j].getLabel().setBackground(red);
                    board[i][j].type = TileType.wordx3;
                }
            }
        }

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.normal) {
                    board[i][j].label.setBackground(normal);
                }
            }
        }
        board[7][7].label.setBackground(pink);
        board[7][7].type = TileType.center;

    }

    /**
     * the String of the "picked up" letter from the player's hand
     */
    public String focusedLetter;

    /**
     * the label of the pressed player tile
     */
    public JLabel focusedHandLabel;
    /**
     * the focused tile on the board
     */
    public Tile focusedBoardTile;
    /**
     * the label that the player hovers above - used to replace places in the
     * hand
     */
    public JLabel hoveredHandLabel;

    public void setMouse(String str) {
        focusedLetter = str;
        char ch = str.toLowerCase().charAt(0);
        int index = -1;
        for (int i = 0; i < letters.alphabet.length; i++) {
            if (ch == letters.alphabet[i]) {
                index = i;
                break;
            }
        }
        switch (index) {
            //<editor-fold defaultstate="collapsed" desc="many cases">
            case 0:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/_.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 1:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/A.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 2:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/B.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 3:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/C.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 4:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/D.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 5:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/E.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 6:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/F.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 7:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/G.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 8:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/H.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 9:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/I.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 10:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/J.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 11:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/K.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 12:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/L.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 13:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/M.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 14:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/N.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 15:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/O.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 16:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/P.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 17:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/Q.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 18:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/R.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 19:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/S.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 20:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/T.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 21:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/U.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 22:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/V.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 23:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/W.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 24:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/X.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 25:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/Y.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
            case 26:
                try {
                    setCursor(Toolkit.getDefaultToolkit()
                            .createCustomCursor(new ImageIcon(getClass().getResource("/LetterImages/Z.png")).getImage(),
                                    new Point(20, 20), "custom cursor"));
                } catch (IndexOutOfBoundsException | HeadlessException e) {
                }
                break;
        }
        //</editor-fold>
    }

    public static JLabel[][] ArrayToMatrix(JLabel[] components) {
        JLabel[][] componentsesMatrix = new JLabel[15][15];
        for (int i = 0; i < componentsesMatrix.length; i++) {
            for (int j = 0; j < componentsesMatrix[i].length; j++) {
                componentsesMatrix[i][j] = components[(i * componentsesMatrix[i].length) + j];
            }
        }
        return componentsesMatrix;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialogHelp = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jLabel238 = new javax.swing.JLabel();
        jLabel239 = new javax.swing.JLabel();
        jLabel240 = new javax.swing.JLabel();
        jLabel241 = new javax.swing.JLabel();
        jLabel242 = new javax.swing.JLabel();
        jLabel243 = new javax.swing.JLabel();
        jLabel244 = new javax.swing.JLabel();
        jLabel237 = new javax.swing.JLabel();
        jFrameJokerSelect = new javax.swing.JFrame();
        jPanelLetters = new javax.swing.JPanel();
        jLetter1 = new javax.swing.JButton();
        jLetter2 = new javax.swing.JButton();
        jLetter3 = new javax.swing.JButton();
        jLetter4 = new javax.swing.JButton();
        jLetter5 = new javax.swing.JButton();
        jLetter6 = new javax.swing.JButton();
        jLetter7 = new javax.swing.JButton();
        jLetter8 = new javax.swing.JButton();
        jLetter9 = new javax.swing.JButton();
        jLetter10 = new javax.swing.JButton();
        jLetter11 = new javax.swing.JButton();
        jLetter12 = new javax.swing.JButton();
        jLetter13 = new javax.swing.JButton();
        jLetter14 = new javax.swing.JButton();
        jLetter15 = new javax.swing.JButton();
        jLetter16 = new javax.swing.JButton();
        jLetter17 = new javax.swing.JButton();
        jLetter18 = new javax.swing.JButton();
        jLetter19 = new javax.swing.JButton();
        jLetter20 = new javax.swing.JButton();
        jLetter21 = new javax.swing.JButton();
        jLetter22 = new javax.swing.JButton();
        jLetter23 = new javax.swing.JButton();
        jLetter24 = new javax.swing.JButton();
        jLetter25 = new javax.swing.JButton();
        jLetter26 = new javax.swing.JButton();
        jLabel226 = new javax.swing.JLabel();
        jPanelHand = new javax.swing.JPanel();
        BottomPlayer1 = new javax.swing.JLabel();
        BottomPlayer2 = new javax.swing.JLabel();
        BottomPlayer3 = new javax.swing.JLabel();
        BottomPlayer4 = new javax.swing.JLabel();
        BottomPlayer5 = new javax.swing.JLabel();
        BottomPlayer6 = new javax.swing.JLabel();
        BottomPlayer7 = new javax.swing.JLabel();
        jPanelTiles = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        jLabel94 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        jLabel100 = new javax.swing.JLabel();
        jLabel101 = new javax.swing.JLabel();
        jLabel102 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        jLabel105 = new javax.swing.JLabel();
        jLabel106 = new javax.swing.JLabel();
        jLabel107 = new javax.swing.JLabel();
        jLabel108 = new javax.swing.JLabel();
        jLabel109 = new javax.swing.JLabel();
        jLabel110 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jLabel112 = new javax.swing.JLabel();
        jLabel113 = new javax.swing.JLabel();
        jLabel114 = new javax.swing.JLabel();
        jLabel115 = new javax.swing.JLabel();
        jLabel116 = new javax.swing.JLabel();
        jLabel117 = new javax.swing.JLabel();
        jLabel118 = new javax.swing.JLabel();
        jLabel119 = new javax.swing.JLabel();
        jLabel120 = new javax.swing.JLabel();
        jLabel121 = new javax.swing.JLabel();
        jLabel122 = new javax.swing.JLabel();
        jLabel123 = new javax.swing.JLabel();
        jLabel124 = new javax.swing.JLabel();
        jLabel125 = new javax.swing.JLabel();
        jLabel126 = new javax.swing.JLabel();
        jLabel127 = new javax.swing.JLabel();
        jLabel128 = new javax.swing.JLabel();
        jLabel129 = new javax.swing.JLabel();
        jLabel130 = new javax.swing.JLabel();
        jLabel131 = new javax.swing.JLabel();
        jLabel132 = new javax.swing.JLabel();
        jLabel133 = new javax.swing.JLabel();
        jLabel134 = new javax.swing.JLabel();
        jLabel135 = new javax.swing.JLabel();
        jLabel136 = new javax.swing.JLabel();
        jLabel137 = new javax.swing.JLabel();
        jLabel138 = new javax.swing.JLabel();
        jLabel139 = new javax.swing.JLabel();
        jLabel140 = new javax.swing.JLabel();
        jLabel141 = new javax.swing.JLabel();
        jLabel142 = new javax.swing.JLabel();
        jLabel143 = new javax.swing.JLabel();
        jLabel144 = new javax.swing.JLabel();
        jLabel145 = new javax.swing.JLabel();
        jLabel146 = new javax.swing.JLabel();
        jLabel147 = new javax.swing.JLabel();
        jLabel148 = new javax.swing.JLabel();
        jLabel149 = new javax.swing.JLabel();
        jLabel150 = new javax.swing.JLabel();
        jLabel151 = new javax.swing.JLabel();
        jLabel152 = new javax.swing.JLabel();
        jLabel153 = new javax.swing.JLabel();
        jLabel154 = new javax.swing.JLabel();
        jLabel155 = new javax.swing.JLabel();
        jLabel156 = new javax.swing.JLabel();
        jLabel157 = new javax.swing.JLabel();
        jLabel158 = new javax.swing.JLabel();
        jLabel159 = new javax.swing.JLabel();
        jLabel160 = new javax.swing.JLabel();
        jLabel161 = new javax.swing.JLabel();
        jLabel162 = new javax.swing.JLabel();
        jLabel163 = new javax.swing.JLabel();
        jLabel164 = new javax.swing.JLabel();
        jLabel165 = new javax.swing.JLabel();
        jLabel166 = new javax.swing.JLabel();
        jLabel167 = new javax.swing.JLabel();
        jLabel168 = new javax.swing.JLabel();
        jLabel169 = new javax.swing.JLabel();
        jLabel170 = new javax.swing.JLabel();
        jLabel171 = new javax.swing.JLabel();
        jLabel172 = new javax.swing.JLabel();
        jLabel173 = new javax.swing.JLabel();
        jLabel174 = new javax.swing.JLabel();
        jLabel175 = new javax.swing.JLabel();
        jLabel176 = new javax.swing.JLabel();
        jLabel177 = new javax.swing.JLabel();
        jLabel178 = new javax.swing.JLabel();
        jLabel179 = new javax.swing.JLabel();
        jLabel180 = new javax.swing.JLabel();
        jLabel181 = new javax.swing.JLabel();
        jLabel182 = new javax.swing.JLabel();
        jLabel183 = new javax.swing.JLabel();
        jLabel184 = new javax.swing.JLabel();
        jLabel185 = new javax.swing.JLabel();
        jLabel186 = new javax.swing.JLabel();
        jLabel187 = new javax.swing.JLabel();
        jLabel188 = new javax.swing.JLabel();
        jLabel189 = new javax.swing.JLabel();
        jLabel190 = new javax.swing.JLabel();
        jLabel191 = new javax.swing.JLabel();
        jLabel192 = new javax.swing.JLabel();
        jLabel193 = new javax.swing.JLabel();
        jLabel194 = new javax.swing.JLabel();
        jLabel195 = new javax.swing.JLabel();
        jLabel196 = new javax.swing.JLabel();
        jLabel197 = new javax.swing.JLabel();
        jLabel198 = new javax.swing.JLabel();
        jLabel199 = new javax.swing.JLabel();
        jLabel200 = new javax.swing.JLabel();
        jLabel201 = new javax.swing.JLabel();
        jLabel202 = new javax.swing.JLabel();
        jLabel203 = new javax.swing.JLabel();
        jLabel204 = new javax.swing.JLabel();
        jLabel205 = new javax.swing.JLabel();
        jLabel206 = new javax.swing.JLabel();
        jLabel207 = new javax.swing.JLabel();
        jLabel208 = new javax.swing.JLabel();
        jLabel209 = new javax.swing.JLabel();
        jLabel210 = new javax.swing.JLabel();
        jLabel211 = new javax.swing.JLabel();
        jLabel212 = new javax.swing.JLabel();
        jLabel213 = new javax.swing.JLabel();
        jLabel214 = new javax.swing.JLabel();
        jLabel215 = new javax.swing.JLabel();
        jLabel216 = new javax.swing.JLabel();
        jLabel217 = new javax.swing.JLabel();
        jLabel218 = new javax.swing.JLabel();
        jLabel219 = new javax.swing.JLabel();
        jLabel220 = new javax.swing.JLabel();
        jLabel221 = new javax.swing.JLabel();
        jLabel222 = new javax.swing.JLabel();
        jLabel223 = new javax.swing.JLabel();
        jLabel224 = new javax.swing.JLabel();
        jLabel225 = new javax.swing.JLabel();
        jButtonPlay = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();

        jDialogHelp.setSize(new java.awt.Dimension(225, 310));

        jLabel238.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel238.setText(" - Letter x 2");

        jLabel239.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel239.setText(" - Letter x 3");

        jLabel240.setBackground(new java.awt.Color(24, 169, 249));
        jLabel240.setOpaque(true);
        jLabel240.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel240MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel240MouseExited(evt);
            }
        });

        jLabel241.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel241.setText(" - Word x 2");

        jLabel242.setBackground(new java.awt.Color(252, 176, 180));
        jLabel242.setOpaque(true);
        jLabel242.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel242MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel242MouseExited(evt);
            }
        });

        jLabel243.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel243.setText(" - Word x 3");

        jLabel244.setBackground(new java.awt.Color(246, 73, 61));
        jLabel244.setOpaque(true);
        jLabel244.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel244MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel244MouseExited(evt);
            }
        });

        jLabel237.setBackground(new java.awt.Color(83, 212, 251));
        jLabel237.setOpaque(true);
        jLabel237.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel237MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel237MouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel244, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel243, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel242, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel241, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel240, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel239, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel237, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel238, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel238, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel237, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel239, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel240, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel241, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel242, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel243, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel244, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jDialogHelpLayout = new javax.swing.GroupLayout(jDialogHelp.getContentPane());
        jDialogHelp.getContentPane().setLayout(jDialogHelpLayout);
        jDialogHelpLayout.setHorizontalGroup(
            jDialogHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialogHelpLayout.setVerticalGroup(
            jDialogHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jFrameJokerSelect.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                jFrameJokerSelectWindowClosing(evt);
            }
        });

        jPanelLetters.setLayout(new java.awt.GridLayout(2, 13));

        jLetter1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter1.setText("A");
        jPanelLetters.add(jLetter1);

        jLetter2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter2.setText("B");
        jPanelLetters.add(jLetter2);

        jLetter3.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter3.setText("C");
        jPanelLetters.add(jLetter3);

        jLetter4.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter4.setText("D");
        jPanelLetters.add(jLetter4);

        jLetter5.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter5.setText("E");
        jPanelLetters.add(jLetter5);

        jLetter6.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter6.setText("F");
        jPanelLetters.add(jLetter6);

        jLetter7.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter7.setText("G");
        jPanelLetters.add(jLetter7);

        jLetter8.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter8.setText("H");
        jPanelLetters.add(jLetter8);

        jLetter9.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter9.setText("I");
        jPanelLetters.add(jLetter9);

        jLetter10.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter10.setText("J");
        jPanelLetters.add(jLetter10);

        jLetter11.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter11.setText("K");
        jPanelLetters.add(jLetter11);

        jLetter12.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter12.setText("L");
        jPanelLetters.add(jLetter12);

        jLetter13.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter13.setText("M");
        jPanelLetters.add(jLetter13);

        jLetter14.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter14.setText("N");
        jPanelLetters.add(jLetter14);

        jLetter15.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter15.setText("O");
        jPanelLetters.add(jLetter15);

        jLetter16.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter16.setText("P");
        jPanelLetters.add(jLetter16);

        jLetter17.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter17.setText("Q");
        jPanelLetters.add(jLetter17);

        jLetter18.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter18.setText("R");
        jPanelLetters.add(jLetter18);

        jLetter19.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter19.setText("S");
        jPanelLetters.add(jLetter19);

        jLetter20.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter20.setText("T");
        jPanelLetters.add(jLetter20);

        jLetter21.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter21.setText("U");
        jPanelLetters.add(jLetter21);

        jLetter22.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter22.setText("V");
        jPanelLetters.add(jLetter22);

        jLetter23.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter23.setText("W");
        jPanelLetters.add(jLetter23);

        jLetter24.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter24.setText("X");
        jPanelLetters.add(jLetter24);

        jLetter25.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter25.setText("Y");
        jPanelLetters.add(jLetter25);

        jLetter26.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLetter26.setText("Z");
        jPanelLetters.add(jLetter26);

        jLabel226.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel226.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel226.setText("Choose a letter");

        javax.swing.GroupLayout jFrameJokerSelectLayout = new javax.swing.GroupLayout(jFrameJokerSelect.getContentPane());
        jFrameJokerSelect.getContentPane().setLayout(jFrameJokerSelectLayout);
        jFrameJokerSelectLayout.setHorizontalGroup(
            jFrameJokerSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrameJokerSelectLayout.createSequentialGroup()
                .addGroup(jFrameJokerSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrameJokerSelectLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanelLetters, javax.swing.GroupLayout.PREFERRED_SIZE, 854, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jFrameJokerSelectLayout.createSequentialGroup()
                        .addGap(332, 332, 332)
                        .addComponent(jLabel226, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jFrameJokerSelectLayout.setVerticalGroup(
            jFrameJokerSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrameJokerSelectLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelLetters, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel226, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                .addGap(23, 23, 23))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        jPanelHand.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        jPanelHand.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanelHand.setLayout(new java.awt.GridLayout(1, 7, 10, 0));

        BottomPlayer1.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer1.setOpaque(true);
        jPanelHand.add(BottomPlayer1);

        BottomPlayer2.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer2.setOpaque(true);
        jPanelHand.add(BottomPlayer2);

        BottomPlayer3.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer3.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer3.setOpaque(true);
        jPanelHand.add(BottomPlayer3);

        BottomPlayer4.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer4.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer4.setOpaque(true);
        jPanelHand.add(BottomPlayer4);

        BottomPlayer5.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer5.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer5.setOpaque(true);
        jPanelHand.add(BottomPlayer5);

        BottomPlayer6.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer6.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer6.setOpaque(true);
        jPanelHand.add(BottomPlayer6);

        BottomPlayer7.setBackground(new java.awt.Color(255, 255, 255));
        BottomPlayer7.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        BottomPlayer7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BottomPlayer7.setOpaque(true);
        jPanelHand.add(BottomPlayer7);

        jPanelTiles.setLayout(new java.awt.GridLayout(15, 15));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        jPanelTiles.add(jLabel1);

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel2.setOpaque(true);
        jPanelTiles.add(jLabel2);

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel3.setOpaque(true);
        jPanelTiles.add(jLabel3);

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel4.setOpaque(true);
        jPanelTiles.add(jLabel4);

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel5.setOpaque(true);
        jPanelTiles.add(jLabel5);

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel6.setOpaque(true);
        jPanelTiles.add(jLabel6);

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel7.setOpaque(true);
        jPanelTiles.add(jLabel7);

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel8.setOpaque(true);
        jPanelTiles.add(jLabel8);

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel9.setOpaque(true);
        jPanelTiles.add(jLabel9);

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel10.setOpaque(true);
        jPanelTiles.add(jLabel10);

        jLabel11.setBackground(new java.awt.Color(255, 255, 255));
        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel11.setOpaque(true);
        jPanelTiles.add(jLabel11);

        jLabel12.setBackground(new java.awt.Color(255, 255, 255));
        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel12.setOpaque(true);
        jPanelTiles.add(jLabel12);

        jLabel13.setBackground(new java.awt.Color(255, 255, 255));
        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel13.setOpaque(true);
        jPanelTiles.add(jLabel13);

        jLabel14.setBackground(new java.awt.Color(255, 255, 255));
        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel14.setOpaque(true);
        jPanelTiles.add(jLabel14);

        jLabel15.setBackground(new java.awt.Color(255, 255, 255));
        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel15.setOpaque(true);
        jPanelTiles.add(jLabel15);

        jLabel16.setBackground(new java.awt.Color(255, 255, 255));
        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel16.setOpaque(true);
        jPanelTiles.add(jLabel16);

        jLabel17.setBackground(new java.awt.Color(255, 255, 255));
        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel17.setOpaque(true);
        jPanelTiles.add(jLabel17);

        jLabel18.setBackground(new java.awt.Color(255, 255, 255));
        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel18.setOpaque(true);
        jPanelTiles.add(jLabel18);

        jLabel19.setBackground(new java.awt.Color(255, 255, 255));
        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel19.setOpaque(true);
        jPanelTiles.add(jLabel19);

        jLabel20.setBackground(new java.awt.Color(255, 255, 255));
        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel20.setOpaque(true);
        jPanelTiles.add(jLabel20);

        jLabel21.setBackground(new java.awt.Color(255, 255, 255));
        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel21.setOpaque(true);
        jPanelTiles.add(jLabel21);

        jLabel22.setBackground(new java.awt.Color(255, 255, 255));
        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel22.setOpaque(true);
        jPanelTiles.add(jLabel22);

        jLabel23.setBackground(new java.awt.Color(255, 255, 255));
        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel23.setOpaque(true);
        jPanelTiles.add(jLabel23);

        jLabel24.setBackground(new java.awt.Color(255, 255, 255));
        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel24.setOpaque(true);
        jPanelTiles.add(jLabel24);

        jLabel25.setBackground(new java.awt.Color(255, 255, 255));
        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel25.setOpaque(true);
        jPanelTiles.add(jLabel25);

        jLabel26.setBackground(new java.awt.Color(255, 255, 255));
        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel26.setOpaque(true);
        jPanelTiles.add(jLabel26);

        jLabel27.setBackground(new java.awt.Color(255, 255, 255));
        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel27.setOpaque(true);
        jPanelTiles.add(jLabel27);

        jLabel28.setBackground(new java.awt.Color(255, 255, 255));
        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel28.setOpaque(true);
        jPanelTiles.add(jLabel28);

        jLabel29.setBackground(new java.awt.Color(255, 255, 255));
        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel29.setOpaque(true);
        jPanelTiles.add(jLabel29);

        jLabel30.setBackground(new java.awt.Color(255, 255, 255));
        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel30.setOpaque(true);
        jPanelTiles.add(jLabel30);

        jLabel31.setBackground(new java.awt.Color(255, 255, 255));
        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel31.setOpaque(true);
        jPanelTiles.add(jLabel31);

        jLabel32.setBackground(new java.awt.Color(255, 255, 255));
        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel32.setOpaque(true);
        jPanelTiles.add(jLabel32);

        jLabel33.setBackground(new java.awt.Color(255, 255, 255));
        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel33.setOpaque(true);
        jPanelTiles.add(jLabel33);

        jLabel34.setBackground(new java.awt.Color(255, 255, 255));
        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel34.setOpaque(true);
        jPanelTiles.add(jLabel34);

        jLabel35.setBackground(new java.awt.Color(255, 255, 255));
        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel35.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel35.setOpaque(true);
        jPanelTiles.add(jLabel35);

        jLabel36.setBackground(new java.awt.Color(255, 255, 255));
        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel36.setOpaque(true);
        jPanelTiles.add(jLabel36);

        jLabel37.setBackground(new java.awt.Color(255, 255, 255));
        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel37.setOpaque(true);
        jPanelTiles.add(jLabel37);

        jLabel38.setBackground(new java.awt.Color(255, 255, 255));
        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel38.setOpaque(true);
        jPanelTiles.add(jLabel38);

        jLabel39.setBackground(new java.awt.Color(255, 255, 255));
        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel39.setOpaque(true);
        jPanelTiles.add(jLabel39);

        jLabel40.setBackground(new java.awt.Color(255, 255, 255));
        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel40.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel40.setOpaque(true);
        jPanelTiles.add(jLabel40);

        jLabel41.setBackground(new java.awt.Color(255, 255, 255));
        jLabel41.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel41.setOpaque(true);
        jPanelTiles.add(jLabel41);

        jLabel42.setBackground(new java.awt.Color(255, 255, 255));
        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel42.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel42.setOpaque(true);
        jPanelTiles.add(jLabel42);

        jLabel43.setBackground(new java.awt.Color(255, 255, 255));
        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel43.setOpaque(true);
        jPanelTiles.add(jLabel43);

        jLabel44.setBackground(new java.awt.Color(255, 255, 255));
        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel44.setOpaque(true);
        jPanelTiles.add(jLabel44);

        jLabel45.setBackground(new java.awt.Color(255, 255, 255));
        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel45.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel45.setOpaque(true);
        jPanelTiles.add(jLabel45);

        jLabel46.setBackground(new java.awt.Color(255, 255, 255));
        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel46.setOpaque(true);
        jPanelTiles.add(jLabel46);

        jLabel47.setBackground(new java.awt.Color(255, 255, 255));
        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel47.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel47.setOpaque(true);
        jPanelTiles.add(jLabel47);

        jLabel48.setBackground(new java.awt.Color(255, 255, 255));
        jLabel48.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel48.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel48.setOpaque(true);
        jPanelTiles.add(jLabel48);

        jLabel49.setBackground(new java.awt.Color(255, 255, 255));
        jLabel49.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel49.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel49.setOpaque(true);
        jPanelTiles.add(jLabel49);

        jLabel50.setBackground(new java.awt.Color(255, 255, 255));
        jLabel50.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel50.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel50.setOpaque(true);
        jPanelTiles.add(jLabel50);

        jLabel51.setBackground(new java.awt.Color(255, 255, 255));
        jLabel51.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel51.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel51.setOpaque(true);
        jPanelTiles.add(jLabel51);

        jLabel52.setBackground(new java.awt.Color(255, 255, 255));
        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel52.setOpaque(true);
        jPanelTiles.add(jLabel52);

        jLabel53.setBackground(new java.awt.Color(255, 255, 255));
        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel53.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel53.setOpaque(true);
        jPanelTiles.add(jLabel53);

        jLabel54.setBackground(new java.awt.Color(255, 255, 255));
        jLabel54.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel54.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel54.setOpaque(true);
        jPanelTiles.add(jLabel54);

        jLabel55.setBackground(new java.awt.Color(255, 255, 255));
        jLabel55.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel55.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel55.setOpaque(true);
        jPanelTiles.add(jLabel55);

        jLabel56.setBackground(new java.awt.Color(255, 255, 255));
        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel56.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel56.setOpaque(true);
        jPanelTiles.add(jLabel56);

        jLabel57.setBackground(new java.awt.Color(255, 255, 255));
        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel57.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel57.setOpaque(true);
        jPanelTiles.add(jLabel57);

        jLabel58.setBackground(new java.awt.Color(255, 255, 255));
        jLabel58.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel58.setOpaque(true);
        jPanelTiles.add(jLabel58);

        jLabel59.setBackground(new java.awt.Color(255, 255, 255));
        jLabel59.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel59.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel59.setOpaque(true);
        jPanelTiles.add(jLabel59);

        jLabel60.setBackground(new java.awt.Color(255, 255, 255));
        jLabel60.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel60.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel60.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel60.setOpaque(true);
        jPanelTiles.add(jLabel60);

        jLabel61.setBackground(new java.awt.Color(255, 255, 255));
        jLabel61.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel61.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel61.setOpaque(true);
        jPanelTiles.add(jLabel61);

        jLabel62.setBackground(new java.awt.Color(255, 255, 255));
        jLabel62.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel62.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel62.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel62.setOpaque(true);
        jPanelTiles.add(jLabel62);

        jLabel63.setBackground(new java.awt.Color(255, 255, 255));
        jLabel63.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel63.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel63.setOpaque(true);
        jPanelTiles.add(jLabel63);

        jLabel64.setBackground(new java.awt.Color(255, 255, 255));
        jLabel64.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel64.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel64.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel64.setOpaque(true);
        jPanelTiles.add(jLabel64);

        jLabel65.setBackground(new java.awt.Color(255, 255, 255));
        jLabel65.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel65.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel65.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel65.setOpaque(true);
        jPanelTiles.add(jLabel65);

        jLabel66.setBackground(new java.awt.Color(255, 255, 255));
        jLabel66.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel66.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel66.setOpaque(true);
        jPanelTiles.add(jLabel66);

        jLabel67.setBackground(new java.awt.Color(255, 255, 255));
        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel67.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel67.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel67.setOpaque(true);
        jPanelTiles.add(jLabel67);

        jLabel68.setBackground(new java.awt.Color(255, 255, 255));
        jLabel68.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel68.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel68.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel68.setOpaque(true);
        jPanelTiles.add(jLabel68);

        jLabel69.setBackground(new java.awt.Color(255, 255, 255));
        jLabel69.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel69.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel69.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel69.setOpaque(true);
        jPanelTiles.add(jLabel69);

        jLabel70.setBackground(new java.awt.Color(255, 255, 255));
        jLabel70.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel70.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel70.setOpaque(true);
        jPanelTiles.add(jLabel70);

        jLabel71.setBackground(new java.awt.Color(255, 255, 255));
        jLabel71.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel71.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel71.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel71.setOpaque(true);
        jPanelTiles.add(jLabel71);

        jLabel72.setBackground(new java.awt.Color(255, 255, 255));
        jLabel72.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel72.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel72.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel72.setOpaque(true);
        jPanelTiles.add(jLabel72);

        jLabel73.setBackground(new java.awt.Color(255, 255, 255));
        jLabel73.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel73.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel73.setOpaque(true);
        jPanelTiles.add(jLabel73);

        jLabel74.setBackground(new java.awt.Color(255, 255, 255));
        jLabel74.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel74.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel74.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel74.setOpaque(true);
        jPanelTiles.add(jLabel74);

        jLabel75.setBackground(new java.awt.Color(255, 255, 255));
        jLabel75.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel75.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel75.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel75.setOpaque(true);
        jPanelTiles.add(jLabel75);

        jLabel76.setBackground(new java.awt.Color(255, 255, 255));
        jLabel76.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel76.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel76.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel76.setOpaque(true);
        jPanelTiles.add(jLabel76);

        jLabel77.setBackground(new java.awt.Color(255, 255, 255));
        jLabel77.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel77.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel77.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel77.setOpaque(true);
        jPanelTiles.add(jLabel77);

        jLabel78.setBackground(new java.awt.Color(255, 255, 255));
        jLabel78.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel78.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel78.setOpaque(true);
        jPanelTiles.add(jLabel78);

        jLabel79.setBackground(new java.awt.Color(255, 255, 255));
        jLabel79.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel79.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel79.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel79.setOpaque(true);
        jPanelTiles.add(jLabel79);

        jLabel80.setBackground(new java.awt.Color(255, 255, 255));
        jLabel80.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel80.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel80.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel80.setOpaque(true);
        jPanelTiles.add(jLabel80);

        jLabel81.setBackground(new java.awt.Color(255, 255, 255));
        jLabel81.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel81.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel81.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel81.setOpaque(true);
        jPanelTiles.add(jLabel81);

        jLabel82.setBackground(new java.awt.Color(255, 255, 255));
        jLabel82.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel82.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel82.setOpaque(true);
        jPanelTiles.add(jLabel82);

        jLabel83.setBackground(new java.awt.Color(255, 255, 255));
        jLabel83.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel83.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel83.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel83.setOpaque(true);
        jPanelTiles.add(jLabel83);

        jLabel84.setBackground(new java.awt.Color(255, 255, 255));
        jLabel84.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel84.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel84.setOpaque(true);
        jPanelTiles.add(jLabel84);

        jLabel85.setBackground(new java.awt.Color(255, 255, 255));
        jLabel85.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel85.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel85.setOpaque(true);
        jPanelTiles.add(jLabel85);

        jLabel86.setBackground(new java.awt.Color(255, 255, 255));
        jLabel86.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel86.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel86.setOpaque(true);
        jPanelTiles.add(jLabel86);

        jLabel87.setBackground(new java.awt.Color(255, 255, 255));
        jLabel87.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel87.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel87.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel87.setOpaque(true);
        jPanelTiles.add(jLabel87);

        jLabel88.setBackground(new java.awt.Color(255, 255, 255));
        jLabel88.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel88.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel88.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel88.setOpaque(true);
        jPanelTiles.add(jLabel88);

        jLabel89.setBackground(new java.awt.Color(255, 255, 255));
        jLabel89.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel89.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel89.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel89.setOpaque(true);
        jPanelTiles.add(jLabel89);

        jLabel90.setBackground(new java.awt.Color(255, 255, 255));
        jLabel90.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel90.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel90.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel90.setOpaque(true);
        jPanelTiles.add(jLabel90);

        jLabel91.setBackground(new java.awt.Color(255, 255, 255));
        jLabel91.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel91.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel91.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel91.setOpaque(true);
        jPanelTiles.add(jLabel91);

        jLabel92.setBackground(new java.awt.Color(255, 255, 255));
        jLabel92.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel92.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel92.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel92.setOpaque(true);
        jPanelTiles.add(jLabel92);

        jLabel93.setBackground(new java.awt.Color(255, 255, 255));
        jLabel93.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel93.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel93.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel93.setOpaque(true);
        jPanelTiles.add(jLabel93);

        jLabel94.setBackground(new java.awt.Color(255, 255, 255));
        jLabel94.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel94.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel94.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel94.setOpaque(true);
        jPanelTiles.add(jLabel94);

        jLabel95.setBackground(new java.awt.Color(255, 255, 255));
        jLabel95.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel95.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel95.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel95.setOpaque(true);
        jPanelTiles.add(jLabel95);

        jLabel96.setBackground(new java.awt.Color(255, 255, 255));
        jLabel96.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel96.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel96.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel96.setOpaque(true);
        jPanelTiles.add(jLabel96);

        jLabel97.setBackground(new java.awt.Color(255, 255, 255));
        jLabel97.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel97.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel97.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel97.setOpaque(true);
        jPanelTiles.add(jLabel97);

        jLabel98.setBackground(new java.awt.Color(255, 255, 255));
        jLabel98.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel98.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel98.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel98.setOpaque(true);
        jPanelTiles.add(jLabel98);

        jLabel99.setBackground(new java.awt.Color(255, 255, 255));
        jLabel99.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel99.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel99.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel99.setOpaque(true);
        jPanelTiles.add(jLabel99);

        jLabel100.setBackground(new java.awt.Color(255, 255, 255));
        jLabel100.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel100.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel100.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel100.setOpaque(true);
        jPanelTiles.add(jLabel100);

        jLabel101.setBackground(new java.awt.Color(255, 255, 255));
        jLabel101.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel101.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel101.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel101.setOpaque(true);
        jPanelTiles.add(jLabel101);

        jLabel102.setBackground(new java.awt.Color(255, 255, 255));
        jLabel102.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel102.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel102.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel102.setOpaque(true);
        jPanelTiles.add(jLabel102);

        jLabel103.setBackground(new java.awt.Color(255, 255, 255));
        jLabel103.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel103.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel103.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel103.setOpaque(true);
        jPanelTiles.add(jLabel103);

        jLabel104.setBackground(new java.awt.Color(255, 255, 255));
        jLabel104.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel104.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel104.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel104.setOpaque(true);
        jPanelTiles.add(jLabel104);

        jLabel105.setBackground(new java.awt.Color(255, 255, 255));
        jLabel105.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel105.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel105.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel105.setOpaque(true);
        jPanelTiles.add(jLabel105);

        jLabel106.setBackground(new java.awt.Color(255, 255, 255));
        jLabel106.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel106.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel106.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel106.setOpaque(true);
        jPanelTiles.add(jLabel106);

        jLabel107.setBackground(new java.awt.Color(255, 255, 255));
        jLabel107.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel107.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel107.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel107.setOpaque(true);
        jPanelTiles.add(jLabel107);

        jLabel108.setBackground(new java.awt.Color(255, 255, 255));
        jLabel108.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel108.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel108.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel108.setOpaque(true);
        jPanelTiles.add(jLabel108);

        jLabel109.setBackground(new java.awt.Color(255, 255, 255));
        jLabel109.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel109.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel109.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel109.setOpaque(true);
        jPanelTiles.add(jLabel109);

        jLabel110.setBackground(new java.awt.Color(255, 255, 255));
        jLabel110.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel110.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel110.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel110.setOpaque(true);
        jPanelTiles.add(jLabel110);

        jLabel111.setBackground(new java.awt.Color(255, 255, 255));
        jLabel111.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel111.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel111.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel111.setOpaque(true);
        jPanelTiles.add(jLabel111);

        jLabel112.setBackground(new java.awt.Color(255, 255, 255));
        jLabel112.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel112.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel112.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel112.setOpaque(true);
        jPanelTiles.add(jLabel112);

        jLabel113.setBackground(new java.awt.Color(255, 255, 255));
        jLabel113.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel113.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel113.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel113.setOpaque(true);
        jPanelTiles.add(jLabel113);

        jLabel114.setBackground(new java.awt.Color(255, 255, 255));
        jLabel114.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel114.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel114.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel114.setOpaque(true);
        jPanelTiles.add(jLabel114);

        jLabel115.setBackground(new java.awt.Color(255, 255, 255));
        jLabel115.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel115.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel115.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel115.setOpaque(true);
        jPanelTiles.add(jLabel115);

        jLabel116.setBackground(new java.awt.Color(255, 255, 255));
        jLabel116.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel116.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel116.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel116.setOpaque(true);
        jPanelTiles.add(jLabel116);

        jLabel117.setBackground(new java.awt.Color(255, 255, 255));
        jLabel117.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel117.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel117.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel117.setOpaque(true);
        jPanelTiles.add(jLabel117);

        jLabel118.setBackground(new java.awt.Color(255, 255, 255));
        jLabel118.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel118.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel118.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel118.setOpaque(true);
        jPanelTiles.add(jLabel118);

        jLabel119.setBackground(new java.awt.Color(255, 255, 255));
        jLabel119.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel119.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel119.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel119.setOpaque(true);
        jPanelTiles.add(jLabel119);

        jLabel120.setBackground(new java.awt.Color(255, 255, 255));
        jLabel120.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel120.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel120.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel120.setOpaque(true);
        jPanelTiles.add(jLabel120);

        jLabel121.setBackground(new java.awt.Color(255, 255, 255));
        jLabel121.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel121.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel121.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel121.setOpaque(true);
        jPanelTiles.add(jLabel121);

        jLabel122.setBackground(new java.awt.Color(255, 255, 255));
        jLabel122.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel122.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel122.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel122.setOpaque(true);
        jPanelTiles.add(jLabel122);

        jLabel123.setBackground(new java.awt.Color(255, 255, 255));
        jLabel123.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel123.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel123.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel123.setOpaque(true);
        jPanelTiles.add(jLabel123);

        jLabel124.setBackground(new java.awt.Color(255, 255, 255));
        jLabel124.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel124.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel124.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel124.setOpaque(true);
        jPanelTiles.add(jLabel124);

        jLabel125.setBackground(new java.awt.Color(255, 255, 255));
        jLabel125.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel125.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel125.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel125.setOpaque(true);
        jPanelTiles.add(jLabel125);

        jLabel126.setBackground(new java.awt.Color(255, 255, 255));
        jLabel126.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel126.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel126.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel126.setOpaque(true);
        jPanelTiles.add(jLabel126);

        jLabel127.setBackground(new java.awt.Color(255, 255, 255));
        jLabel127.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel127.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel127.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel127.setOpaque(true);
        jPanelTiles.add(jLabel127);

        jLabel128.setBackground(new java.awt.Color(255, 255, 255));
        jLabel128.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel128.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel128.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel128.setOpaque(true);
        jPanelTiles.add(jLabel128);

        jLabel129.setBackground(new java.awt.Color(255, 255, 255));
        jLabel129.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel129.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel129.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel129.setOpaque(true);
        jPanelTiles.add(jLabel129);

        jLabel130.setBackground(new java.awt.Color(255, 255, 255));
        jLabel130.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel130.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel130.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel130.setOpaque(true);
        jPanelTiles.add(jLabel130);

        jLabel131.setBackground(new java.awt.Color(255, 255, 255));
        jLabel131.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel131.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel131.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel131.setOpaque(true);
        jPanelTiles.add(jLabel131);

        jLabel132.setBackground(new java.awt.Color(255, 255, 255));
        jLabel132.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel132.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel132.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel132.setOpaque(true);
        jPanelTiles.add(jLabel132);

        jLabel133.setBackground(new java.awt.Color(255, 255, 255));
        jLabel133.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel133.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel133.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel133.setOpaque(true);
        jPanelTiles.add(jLabel133);

        jLabel134.setBackground(new java.awt.Color(255, 255, 255));
        jLabel134.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel134.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel134.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel134.setOpaque(true);
        jPanelTiles.add(jLabel134);

        jLabel135.setBackground(new java.awt.Color(255, 255, 255));
        jLabel135.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel135.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel135.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel135.setOpaque(true);
        jPanelTiles.add(jLabel135);

        jLabel136.setBackground(new java.awt.Color(255, 255, 255));
        jLabel136.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel136.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel136.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel136.setOpaque(true);
        jPanelTiles.add(jLabel136);

        jLabel137.setBackground(new java.awt.Color(255, 255, 255));
        jLabel137.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel137.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel137.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel137.setOpaque(true);
        jPanelTiles.add(jLabel137);

        jLabel138.setBackground(new java.awt.Color(255, 255, 255));
        jLabel138.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel138.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel138.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel138.setOpaque(true);
        jPanelTiles.add(jLabel138);

        jLabel139.setBackground(new java.awt.Color(255, 255, 255));
        jLabel139.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel139.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel139.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel139.setOpaque(true);
        jPanelTiles.add(jLabel139);

        jLabel140.setBackground(new java.awt.Color(255, 255, 255));
        jLabel140.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel140.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel140.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel140.setOpaque(true);
        jPanelTiles.add(jLabel140);

        jLabel141.setBackground(new java.awt.Color(255, 255, 255));
        jLabel141.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel141.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel141.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel141.setOpaque(true);
        jPanelTiles.add(jLabel141);

        jLabel142.setBackground(new java.awt.Color(255, 255, 255));
        jLabel142.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel142.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel142.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel142.setOpaque(true);
        jPanelTiles.add(jLabel142);

        jLabel143.setBackground(new java.awt.Color(255, 255, 255));
        jLabel143.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel143.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel143.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel143.setOpaque(true);
        jPanelTiles.add(jLabel143);

        jLabel144.setBackground(new java.awt.Color(255, 255, 255));
        jLabel144.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel144.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel144.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel144.setOpaque(true);
        jPanelTiles.add(jLabel144);

        jLabel145.setBackground(new java.awt.Color(255, 255, 255));
        jLabel145.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel145.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel145.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel145.setOpaque(true);
        jPanelTiles.add(jLabel145);

        jLabel146.setBackground(new java.awt.Color(255, 255, 255));
        jLabel146.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel146.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel146.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel146.setOpaque(true);
        jPanelTiles.add(jLabel146);

        jLabel147.setBackground(new java.awt.Color(255, 255, 255));
        jLabel147.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel147.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel147.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel147.setOpaque(true);
        jPanelTiles.add(jLabel147);

        jLabel148.setBackground(new java.awt.Color(255, 255, 255));
        jLabel148.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel148.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel148.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel148.setOpaque(true);
        jPanelTiles.add(jLabel148);

        jLabel149.setBackground(new java.awt.Color(255, 255, 255));
        jLabel149.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel149.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel149.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel149.setOpaque(true);
        jPanelTiles.add(jLabel149);

        jLabel150.setBackground(new java.awt.Color(255, 255, 255));
        jLabel150.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel150.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel150.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel150.setOpaque(true);
        jPanelTiles.add(jLabel150);

        jLabel151.setBackground(new java.awt.Color(255, 255, 255));
        jLabel151.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel151.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel151.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel151.setOpaque(true);
        jPanelTiles.add(jLabel151);

        jLabel152.setBackground(new java.awt.Color(255, 255, 255));
        jLabel152.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel152.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel152.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel152.setOpaque(true);
        jPanelTiles.add(jLabel152);

        jLabel153.setBackground(new java.awt.Color(255, 255, 255));
        jLabel153.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel153.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel153.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel153.setOpaque(true);
        jPanelTiles.add(jLabel153);

        jLabel154.setBackground(new java.awt.Color(255, 255, 255));
        jLabel154.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel154.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel154.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel154.setOpaque(true);
        jPanelTiles.add(jLabel154);

        jLabel155.setBackground(new java.awt.Color(255, 255, 255));
        jLabel155.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel155.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel155.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel155.setOpaque(true);
        jPanelTiles.add(jLabel155);

        jLabel156.setBackground(new java.awt.Color(255, 255, 255));
        jLabel156.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel156.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel156.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel156.setOpaque(true);
        jPanelTiles.add(jLabel156);

        jLabel157.setBackground(new java.awt.Color(255, 255, 255));
        jLabel157.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel157.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel157.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel157.setOpaque(true);
        jPanelTiles.add(jLabel157);

        jLabel158.setBackground(new java.awt.Color(255, 255, 255));
        jLabel158.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel158.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel158.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel158.setOpaque(true);
        jPanelTiles.add(jLabel158);

        jLabel159.setBackground(new java.awt.Color(255, 255, 255));
        jLabel159.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel159.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel159.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel159.setOpaque(true);
        jPanelTiles.add(jLabel159);

        jLabel160.setBackground(new java.awt.Color(255, 255, 255));
        jLabel160.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel160.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel160.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel160.setOpaque(true);
        jPanelTiles.add(jLabel160);

        jLabel161.setBackground(new java.awt.Color(255, 255, 255));
        jLabel161.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel161.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel161.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel161.setOpaque(true);
        jPanelTiles.add(jLabel161);

        jLabel162.setBackground(new java.awt.Color(255, 255, 255));
        jLabel162.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel162.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel162.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel162.setOpaque(true);
        jPanelTiles.add(jLabel162);

        jLabel163.setBackground(new java.awt.Color(255, 255, 255));
        jLabel163.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel163.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel163.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel163.setOpaque(true);
        jPanelTiles.add(jLabel163);

        jLabel164.setBackground(new java.awt.Color(255, 255, 255));
        jLabel164.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel164.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel164.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel164.setOpaque(true);
        jPanelTiles.add(jLabel164);

        jLabel165.setBackground(new java.awt.Color(255, 255, 255));
        jLabel165.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel165.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel165.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel165.setOpaque(true);
        jPanelTiles.add(jLabel165);

        jLabel166.setBackground(new java.awt.Color(255, 255, 255));
        jLabel166.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel166.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel166.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel166.setOpaque(true);
        jPanelTiles.add(jLabel166);

        jLabel167.setBackground(new java.awt.Color(255, 255, 255));
        jLabel167.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel167.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel167.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel167.setOpaque(true);
        jPanelTiles.add(jLabel167);

        jLabel168.setBackground(new java.awt.Color(255, 255, 255));
        jLabel168.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel168.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel168.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel168.setOpaque(true);
        jPanelTiles.add(jLabel168);

        jLabel169.setBackground(new java.awt.Color(255, 255, 255));
        jLabel169.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel169.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel169.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel169.setOpaque(true);
        jPanelTiles.add(jLabel169);

        jLabel170.setBackground(new java.awt.Color(255, 255, 255));
        jLabel170.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel170.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel170.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel170.setOpaque(true);
        jPanelTiles.add(jLabel170);

        jLabel171.setBackground(new java.awt.Color(255, 255, 255));
        jLabel171.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel171.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel171.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel171.setOpaque(true);
        jPanelTiles.add(jLabel171);

        jLabel172.setBackground(new java.awt.Color(255, 255, 255));
        jLabel172.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel172.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel172.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel172.setOpaque(true);
        jPanelTiles.add(jLabel172);

        jLabel173.setBackground(new java.awt.Color(255, 255, 255));
        jLabel173.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel173.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel173.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel173.setOpaque(true);
        jPanelTiles.add(jLabel173);

        jLabel174.setBackground(new java.awt.Color(255, 255, 255));
        jLabel174.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel174.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel174.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel174.setOpaque(true);
        jPanelTiles.add(jLabel174);

        jLabel175.setBackground(new java.awt.Color(255, 255, 255));
        jLabel175.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel175.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel175.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel175.setOpaque(true);
        jPanelTiles.add(jLabel175);

        jLabel176.setBackground(new java.awt.Color(255, 255, 255));
        jLabel176.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel176.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel176.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel176.setOpaque(true);
        jPanelTiles.add(jLabel176);

        jLabel177.setBackground(new java.awt.Color(255, 255, 255));
        jLabel177.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel177.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel177.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel177.setOpaque(true);
        jPanelTiles.add(jLabel177);

        jLabel178.setBackground(new java.awt.Color(255, 255, 255));
        jLabel178.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel178.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel178.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel178.setOpaque(true);
        jPanelTiles.add(jLabel178);

        jLabel179.setBackground(new java.awt.Color(255, 255, 255));
        jLabel179.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel179.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel179.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel179.setOpaque(true);
        jPanelTiles.add(jLabel179);

        jLabel180.setBackground(new java.awt.Color(255, 255, 255));
        jLabel180.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel180.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel180.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel180.setOpaque(true);
        jPanelTiles.add(jLabel180);

        jLabel181.setBackground(new java.awt.Color(255, 255, 255));
        jLabel181.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel181.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel181.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel181.setOpaque(true);
        jPanelTiles.add(jLabel181);

        jLabel182.setBackground(new java.awt.Color(255, 255, 255));
        jLabel182.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel182.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel182.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel182.setOpaque(true);
        jPanelTiles.add(jLabel182);

        jLabel183.setBackground(new java.awt.Color(255, 255, 255));
        jLabel183.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel183.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel183.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel183.setOpaque(true);
        jPanelTiles.add(jLabel183);

        jLabel184.setBackground(new java.awt.Color(255, 255, 255));
        jLabel184.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel184.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel184.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel184.setOpaque(true);
        jPanelTiles.add(jLabel184);

        jLabel185.setBackground(new java.awt.Color(255, 255, 255));
        jLabel185.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel185.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel185.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel185.setOpaque(true);
        jPanelTiles.add(jLabel185);

        jLabel186.setBackground(new java.awt.Color(255, 255, 255));
        jLabel186.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel186.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel186.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel186.setOpaque(true);
        jPanelTiles.add(jLabel186);

        jLabel187.setBackground(new java.awt.Color(255, 255, 255));
        jLabel187.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel187.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel187.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel187.setOpaque(true);
        jPanelTiles.add(jLabel187);

        jLabel188.setBackground(new java.awt.Color(255, 255, 255));
        jLabel188.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel188.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel188.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel188.setOpaque(true);
        jPanelTiles.add(jLabel188);

        jLabel189.setBackground(new java.awt.Color(255, 255, 255));
        jLabel189.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel189.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel189.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel189.setOpaque(true);
        jPanelTiles.add(jLabel189);

        jLabel190.setBackground(new java.awt.Color(255, 255, 255));
        jLabel190.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel190.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel190.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel190.setOpaque(true);
        jPanelTiles.add(jLabel190);

        jLabel191.setBackground(new java.awt.Color(255, 255, 255));
        jLabel191.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel191.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel191.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel191.setOpaque(true);
        jPanelTiles.add(jLabel191);

        jLabel192.setBackground(new java.awt.Color(255, 255, 255));
        jLabel192.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel192.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel192.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel192.setOpaque(true);
        jPanelTiles.add(jLabel192);

        jLabel193.setBackground(new java.awt.Color(255, 255, 255));
        jLabel193.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel193.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel193.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel193.setOpaque(true);
        jPanelTiles.add(jLabel193);

        jLabel194.setBackground(new java.awt.Color(255, 255, 255));
        jLabel194.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel194.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel194.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel194.setOpaque(true);
        jPanelTiles.add(jLabel194);

        jLabel195.setBackground(new java.awt.Color(255, 255, 255));
        jLabel195.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel195.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel195.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel195.setOpaque(true);
        jPanelTiles.add(jLabel195);

        jLabel196.setBackground(new java.awt.Color(255, 255, 255));
        jLabel196.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel196.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel196.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel196.setOpaque(true);
        jPanelTiles.add(jLabel196);

        jLabel197.setBackground(new java.awt.Color(255, 255, 255));
        jLabel197.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel197.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel197.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel197.setOpaque(true);
        jPanelTiles.add(jLabel197);

        jLabel198.setBackground(new java.awt.Color(255, 255, 255));
        jLabel198.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel198.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel198.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel198.setOpaque(true);
        jPanelTiles.add(jLabel198);

        jLabel199.setBackground(new java.awt.Color(255, 255, 255));
        jLabel199.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel199.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel199.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel199.setOpaque(true);
        jPanelTiles.add(jLabel199);

        jLabel200.setBackground(new java.awt.Color(255, 255, 255));
        jLabel200.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel200.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel200.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel200.setOpaque(true);
        jPanelTiles.add(jLabel200);

        jLabel201.setBackground(new java.awt.Color(255, 255, 255));
        jLabel201.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel201.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel201.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel201.setOpaque(true);
        jPanelTiles.add(jLabel201);

        jLabel202.setBackground(new java.awt.Color(255, 255, 255));
        jLabel202.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel202.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel202.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel202.setOpaque(true);
        jPanelTiles.add(jLabel202);

        jLabel203.setBackground(new java.awt.Color(255, 255, 255));
        jLabel203.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel203.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel203.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel203.setOpaque(true);
        jPanelTiles.add(jLabel203);

        jLabel204.setBackground(new java.awt.Color(255, 255, 255));
        jLabel204.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel204.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel204.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel204.setOpaque(true);
        jPanelTiles.add(jLabel204);

        jLabel205.setBackground(new java.awt.Color(255, 255, 255));
        jLabel205.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel205.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel205.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel205.setOpaque(true);
        jPanelTiles.add(jLabel205);

        jLabel206.setBackground(new java.awt.Color(255, 255, 255));
        jLabel206.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel206.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel206.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel206.setOpaque(true);
        jPanelTiles.add(jLabel206);

        jLabel207.setBackground(new java.awt.Color(255, 255, 255));
        jLabel207.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel207.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel207.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel207.setOpaque(true);
        jPanelTiles.add(jLabel207);

        jLabel208.setBackground(new java.awt.Color(255, 255, 255));
        jLabel208.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel208.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel208.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel208.setOpaque(true);
        jPanelTiles.add(jLabel208);

        jLabel209.setBackground(new java.awt.Color(255, 255, 255));
        jLabel209.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel209.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel209.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel209.setOpaque(true);
        jPanelTiles.add(jLabel209);

        jLabel210.setBackground(new java.awt.Color(255, 255, 255));
        jLabel210.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel210.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel210.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel210.setOpaque(true);
        jPanelTiles.add(jLabel210);

        jLabel211.setBackground(new java.awt.Color(255, 255, 255));
        jLabel211.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel211.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel211.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel211.setOpaque(true);
        jPanelTiles.add(jLabel211);

        jLabel212.setBackground(new java.awt.Color(255, 255, 255));
        jLabel212.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel212.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel212.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel212.setOpaque(true);
        jPanelTiles.add(jLabel212);

        jLabel213.setBackground(new java.awt.Color(255, 255, 255));
        jLabel213.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel213.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel213.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel213.setOpaque(true);
        jPanelTiles.add(jLabel213);

        jLabel214.setBackground(new java.awt.Color(255, 255, 255));
        jLabel214.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel214.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel214.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel214.setOpaque(true);
        jPanelTiles.add(jLabel214);

        jLabel215.setBackground(new java.awt.Color(255, 255, 255));
        jLabel215.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel215.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel215.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel215.setOpaque(true);
        jPanelTiles.add(jLabel215);

        jLabel216.setBackground(new java.awt.Color(255, 255, 255));
        jLabel216.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel216.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel216.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel216.setOpaque(true);
        jPanelTiles.add(jLabel216);

        jLabel217.setBackground(new java.awt.Color(255, 255, 255));
        jLabel217.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel217.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel217.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel217.setOpaque(true);
        jPanelTiles.add(jLabel217);

        jLabel218.setBackground(new java.awt.Color(255, 255, 255));
        jLabel218.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel218.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel218.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel218.setOpaque(true);
        jPanelTiles.add(jLabel218);

        jLabel219.setBackground(new java.awt.Color(255, 255, 255));
        jLabel219.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel219.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel219.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel219.setOpaque(true);
        jPanelTiles.add(jLabel219);

        jLabel220.setBackground(new java.awt.Color(255, 255, 255));
        jLabel220.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel220.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel220.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel220.setOpaque(true);
        jPanelTiles.add(jLabel220);

        jLabel221.setBackground(new java.awt.Color(255, 255, 255));
        jLabel221.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel221.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel221.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel221.setOpaque(true);
        jPanelTiles.add(jLabel221);

        jLabel222.setBackground(new java.awt.Color(255, 255, 255));
        jLabel222.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel222.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel222.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel222.setOpaque(true);
        jPanelTiles.add(jLabel222);

        jLabel223.setBackground(new java.awt.Color(255, 255, 255));
        jLabel223.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel223.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel223.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel223.setOpaque(true);
        jPanelTiles.add(jLabel223);

        jLabel224.setBackground(new java.awt.Color(255, 255, 255));
        jLabel224.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel224.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel224.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel224.setOpaque(true);
        jPanelTiles.add(jLabel224);

        jLabel225.setBackground(new java.awt.Color(255, 255, 255));
        jLabel225.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel225.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel225.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, null, new java.awt.Color(0, 0, 0)));
        jLabel225.setOpaque(true);
        jPanelTiles.add(jLabel225);

        jButtonPlay.setText("Play Turn");
        jButtonPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlayActionPerformed(evt);
            }
        });

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jMenu1.setText("Help");
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu1MouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jPanelTiles, javax.swing.GroupLayout.PREFERRED_SIZE, 615, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addComponent(jButtonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addGap(9, 9, 9))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jPanelHand, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jPanelTiles, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(338, 338, 338)
                        .addComponent(jButtonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(89, 89, 89)
                        .addComponent(jButton1)))
                .addGap(18, 18, 18)
                .addComponent(jPanelHand, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked
        if ((this.getX() + this.getWidth() + jDialogHelp.getWidth()) > 1920) {
            jDialogHelp.setLocation(this.getX() - jDialogHelp.getWidth(), this.getY());
        } else {
            jDialogHelp.setLocation(this.getX() + this.getWidth(), this.getY());
        }
        jDialogHelp.setVisible(true);
    }//GEN-LAST:event_jMenu1MouseClicked

    private void jLabel237MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel237MouseEntered
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.letterx2) {
                    board[i][j].label.setBackground(paintTile(i, j, true));
                }
            }
        }
    }//GEN-LAST:event_jLabel237MouseEntered

    private void jLabel237MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel237MouseExited
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.letterx2) {
                    board[i][j].label.setBackground(paintTile(i, j, false));
                }
            }
        }
    }//GEN-LAST:event_jLabel237MouseExited

    private void jLabel240MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel240MouseEntered
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.letterx3) {
                    board[i][j].label.setBackground(paintTile(i, j, true));
                }
            }
        }
    }//GEN-LAST:event_jLabel240MouseEntered

    private void jLabel240MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel240MouseExited
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.letterx3) {
                    board[i][j].label.setBackground(paintTile(i, j, false));
                }
            }
        }
    }//GEN-LAST:event_jLabel240MouseExited

    private void jLabel242MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel242MouseEntered
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.wordx2) {
                    board[i][j].label.setBackground(paintTile(i, j, true));
                }
            }
        }
    }//GEN-LAST:event_jLabel242MouseEntered

    private void jLabel242MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel242MouseExited
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.wordx2) {
                    board[i][j].label.setBackground(paintTile(i, j, false));
                }
            }
        }
    }//GEN-LAST:event_jLabel242MouseExited

    private void jLabel244MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel244MouseEntered
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.wordx3) {
                    board[i][j].label.setBackground(paintTile(i, j, true));
                }
            }
        }
    }//GEN-LAST:event_jLabel244MouseEntered

    private void jLabel244MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel244MouseExited
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j].type == TileType.wordx3) {
                    board[i][j].label.setBackground(paintTile(i, j, false));
                }
            }
        }
    }//GEN-LAST:event_jLabel244MouseExited

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        if (jDialogHelp.isVisible()) {
            if ((this.getX() + this.getWidth() + jDialogHelp.getWidth()) > 1920) {
                jDialogHelp.setLocation(this.getX() - jDialogHelp.getWidth(), this.getY());
            } else {
                jDialogHelp.setLocation(this.getX() + this.getWidth(), this.getY());
            }
        }
    }//GEN-LAST:event_formComponentMoved

    private void jButtonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlayActionPerformed
        if (!jFrameJokerSelect.isVisible()) {
            if (ValidWordTurn()) {
                int dir = (thisTurnWord.get(0).y == thisTurnWord.get(1).y) ? Word.horizontal : Word.vertical;
                placedWords.add(new Word(thisTurnWord, dir, CalculateWordValue(thisTurnWord)));
                thisTurnWord.clear();
                System.out.println(thisTurnWord.size());
                firstTurn = false;
                System.out.println(placedWords.get(0).getWord());
            }
        }
    }//GEN-LAST:event_jButtonPlayActionPerformed

    private void jFrameJokerSelectWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jFrameJokerSelectWindowClosing
        focusedHandLabel.setText(focusedLetter);
        focusedLetter = null;
        setCursor(defuaultCursor);
        focusedHandLabel = null;
    }//GEN-LAST:event_jFrameJokerSelectWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (blanks[i][j]) {
                    System.out.print("t ");
                } else {
                    System.out.print("f ");
                }
            }
            System.out.println();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
    //<editor-fold defaultstate="collapsed" desc="components">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BottomPlayer1;
    private javax.swing.JLabel BottomPlayer2;
    private javax.swing.JLabel BottomPlayer3;
    private javax.swing.JLabel BottomPlayer4;
    private javax.swing.JLabel BottomPlayer5;
    private javax.swing.JLabel BottomPlayer6;
    private javax.swing.JLabel BottomPlayer7;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonPlay;
    private javax.swing.JDialog jDialogHelp;
    private javax.swing.JFrame jFrameJokerSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel111;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel116;
    private javax.swing.JLabel jLabel117;
    private javax.swing.JLabel jLabel118;
    private javax.swing.JLabel jLabel119;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel120;
    private javax.swing.JLabel jLabel121;
    private javax.swing.JLabel jLabel122;
    private javax.swing.JLabel jLabel123;
    private javax.swing.JLabel jLabel124;
    private javax.swing.JLabel jLabel125;
    private javax.swing.JLabel jLabel126;
    private javax.swing.JLabel jLabel127;
    private javax.swing.JLabel jLabel128;
    private javax.swing.JLabel jLabel129;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel130;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel132;
    private javax.swing.JLabel jLabel133;
    private javax.swing.JLabel jLabel134;
    private javax.swing.JLabel jLabel135;
    private javax.swing.JLabel jLabel136;
    private javax.swing.JLabel jLabel137;
    private javax.swing.JLabel jLabel138;
    private javax.swing.JLabel jLabel139;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel140;
    private javax.swing.JLabel jLabel141;
    private javax.swing.JLabel jLabel142;
    private javax.swing.JLabel jLabel143;
    private javax.swing.JLabel jLabel144;
    private javax.swing.JLabel jLabel145;
    private javax.swing.JLabel jLabel146;
    private javax.swing.JLabel jLabel147;
    private javax.swing.JLabel jLabel148;
    private javax.swing.JLabel jLabel149;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel150;
    private javax.swing.JLabel jLabel151;
    private javax.swing.JLabel jLabel152;
    private javax.swing.JLabel jLabel153;
    private javax.swing.JLabel jLabel154;
    private javax.swing.JLabel jLabel155;
    private javax.swing.JLabel jLabel156;
    private javax.swing.JLabel jLabel157;
    private javax.swing.JLabel jLabel158;
    private javax.swing.JLabel jLabel159;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel160;
    private javax.swing.JLabel jLabel161;
    private javax.swing.JLabel jLabel162;
    private javax.swing.JLabel jLabel163;
    private javax.swing.JLabel jLabel164;
    private javax.swing.JLabel jLabel165;
    private javax.swing.JLabel jLabel166;
    private javax.swing.JLabel jLabel167;
    private javax.swing.JLabel jLabel168;
    private javax.swing.JLabel jLabel169;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel170;
    private javax.swing.JLabel jLabel171;
    private javax.swing.JLabel jLabel172;
    private javax.swing.JLabel jLabel173;
    private javax.swing.JLabel jLabel174;
    private javax.swing.JLabel jLabel175;
    private javax.swing.JLabel jLabel176;
    private javax.swing.JLabel jLabel177;
    private javax.swing.JLabel jLabel178;
    private javax.swing.JLabel jLabel179;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel180;
    private javax.swing.JLabel jLabel181;
    private javax.swing.JLabel jLabel182;
    private javax.swing.JLabel jLabel183;
    private javax.swing.JLabel jLabel184;
    private javax.swing.JLabel jLabel185;
    private javax.swing.JLabel jLabel186;
    private javax.swing.JLabel jLabel187;
    private javax.swing.JLabel jLabel188;
    private javax.swing.JLabel jLabel189;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel190;
    private javax.swing.JLabel jLabel191;
    private javax.swing.JLabel jLabel192;
    private javax.swing.JLabel jLabel193;
    private javax.swing.JLabel jLabel194;
    private javax.swing.JLabel jLabel195;
    private javax.swing.JLabel jLabel196;
    private javax.swing.JLabel jLabel197;
    private javax.swing.JLabel jLabel198;
    private javax.swing.JLabel jLabel199;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel200;
    private javax.swing.JLabel jLabel201;
    private javax.swing.JLabel jLabel202;
    private javax.swing.JLabel jLabel203;
    private javax.swing.JLabel jLabel204;
    private javax.swing.JLabel jLabel205;
    private javax.swing.JLabel jLabel206;
    private javax.swing.JLabel jLabel207;
    private javax.swing.JLabel jLabel208;
    private javax.swing.JLabel jLabel209;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel210;
    private javax.swing.JLabel jLabel211;
    private javax.swing.JLabel jLabel212;
    private javax.swing.JLabel jLabel213;
    private javax.swing.JLabel jLabel214;
    private javax.swing.JLabel jLabel215;
    private javax.swing.JLabel jLabel216;
    private javax.swing.JLabel jLabel217;
    private javax.swing.JLabel jLabel218;
    private javax.swing.JLabel jLabel219;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel220;
    private javax.swing.JLabel jLabel221;
    private javax.swing.JLabel jLabel222;
    private javax.swing.JLabel jLabel223;
    private javax.swing.JLabel jLabel224;
    private javax.swing.JLabel jLabel225;
    private javax.swing.JLabel jLabel226;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel237;
    private javax.swing.JLabel jLabel238;
    private javax.swing.JLabel jLabel239;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel240;
    private javax.swing.JLabel jLabel241;
    private javax.swing.JLabel jLabel242;
    private javax.swing.JLabel jLabel243;
    private javax.swing.JLabel jLabel244;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JButton jLetter1;
    private javax.swing.JButton jLetter10;
    private javax.swing.JButton jLetter11;
    private javax.swing.JButton jLetter12;
    private javax.swing.JButton jLetter13;
    private javax.swing.JButton jLetter14;
    private javax.swing.JButton jLetter15;
    private javax.swing.JButton jLetter16;
    private javax.swing.JButton jLetter17;
    private javax.swing.JButton jLetter18;
    private javax.swing.JButton jLetter19;
    private javax.swing.JButton jLetter2;
    private javax.swing.JButton jLetter20;
    private javax.swing.JButton jLetter21;
    private javax.swing.JButton jLetter22;
    private javax.swing.JButton jLetter23;
    private javax.swing.JButton jLetter24;
    private javax.swing.JButton jLetter25;
    private javax.swing.JButton jLetter26;
    private javax.swing.JButton jLetter3;
    private javax.swing.JButton jLetter4;
    private javax.swing.JButton jLetter5;
    private javax.swing.JButton jLetter6;
    private javax.swing.JButton jLetter7;
    private javax.swing.JButton jLetter8;
    private javax.swing.JButton jLetter9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelHand;
    private javax.swing.JPanel jPanelLetters;
    private javax.swing.JPanel jPanelTiles;
    // End of variables declaration//GEN-END:variables
//</editor-fold>

    /**
     * reads the dictionary files and adds all the words to {@code dictWords}
     *
     * @throws IOException
     */
    public void readDictionary() throws IOException {
        try {
            File f = new File(getClass().getResource("/dictionary.txt").toURI());

            BufferedReader b = new BufferedReader(new FileReader(f));

            String readLine = "";

            while ((readLine = b.readLine()) != null) {
                dictWords.add(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException ex) {
        }
    }

    public void removeTheseMouseListeners() {
        for (Component button : jPanelLetters.getComponents()) {
            for (int i = 0; i < button.getMouseListeners().length; i++) {
                button.removeMouseListener(button.getMouseListeners()[i]);
            }
        }
    }

}
