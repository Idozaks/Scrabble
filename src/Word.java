
import java.util.ArrayList;

/**
 * A class that would help keep track of the words placed in the game.
 *
 * @author Ido
 */
public class Word {

    public static final int horizontal = 0;
    public static final int vertical = 1;

    public ArrayList<Tile> wordTiles;
    public int direction;
    // some kind of ownership variable?
    public int wordValue;
    public String word;

    /**
     * 
     * @param arr the word places in form of a List of Tiles
     * @param dir what direction the word was placed (i.e horiz / vert)
     * @param val the value of the word
     */
    public Word(ArrayList<Tile> arr, int dir, int val) {
        wordTiles = (ArrayList<Tile>)arr.clone();
        direction = dir;
        wordValue = val;
        word = getWord();
    }

    public final String getWord() {
        String word = "";
        for (int i = 0; i < wordTiles.size(); i++) {
            if (i == 0) {
                word += wordTiles.get(i).getLetter();
            } else {
                word += wordTiles.get(i).getLetter().toLowerCase();
            }
        }

        return word;
    }

}
