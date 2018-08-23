
import javax.swing.JLabel;

public class Tile {

    public JLabel label;
    String letter;
    TileType type;
    int x, y;
    private boolean moveable = false;

    /**
     *
     * @param _label the assigned label
     * @param _letter the letter this tile will contain
     * @param _type the type of score on this tile
     * @param _x
     * @param _y
     */
    public Tile(JLabel _label, String _letter, TileType _type, int _x, int _y) {
        label = _label;
        letter = _letter;
        type = _type;
        x = _x;
        y = _y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public JLabel getLabel() {
        return label;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }
    
    

}
