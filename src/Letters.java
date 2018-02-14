
import java.util.ArrayList;
import java.util.Random;

public class Letters {

    ArrayList<String> letterBank = new ArrayList<>();

    int[] lettersAmount = {2, 9, 2, 2, 4, 12, 2,
        3, 2, 9, 1, 1, 4, 2, 6, 8, 2,
        1, 6, 4, 6, 4, 2, 2, 1, 2, 1};

    char[] alphabet
            = {'_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public Letters() {
        for (int i = 0; i < lettersAmount.length; i++) {
            for (int j = 0; j < lettersAmount[i]; j++) {
                letterBank.add(String.valueOf(alphabet[i]));
            }
        }
    }

    /**
     * selected a random letter in the ArrayList "letterBank", returns it, and 
     * deletes it from the List.
     * @return the selected letter
     */
    public String getRandomLetter() {
        int randomIndex = new Random().nextInt(letterBank.size());
        String selectedLetter = letterBank.get(randomIndex);
        letterBank.remove(selectedLetter);
        return selectedLetter.toUpperCase();
    }

}
//  THIS WAS USED TO TEST
//
//    public static void main(String[] args) {
//        ArrayList<String> letterBank = new ArrayList<>();
//
//        int[] lettersAmount = {2, 9, 2, 2, 4, 12, 2,
//            3, 2, 9, 1, 1, 4, 2, 6, 8, 2,
//            1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
//
//        char[] alphabet
//                = {' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
//                    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
//
//        for (int i = 0; i < lettersAmount.length; i++) {
//            for (int j = 0; j < lettersAmount[i]; j++) {
//                letterBank.add(String.valueOf(alphabet[i]));
//            }
//        }
//        
//        for (int i = 0; i < letterBank.size(); i++) {
//            System.out.println(letterBank.get(i));
//        }
//    }

