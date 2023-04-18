package nju.SEIII.EASIEST.Utilities;

/**
 * Trie is a data structure used to efficiently store and retrieve a set of strings.
 * This class provides three static methods to work with the Trie data structure.
 * i_GetTriePositionForString - This method takes a string, an array of strings, two arrays of integers,
 * and four integer values. It returns the position of the string in the array if it already exists,
 * otherwise it adds the string to the array and returns the position.
 * This method also updates the two arrays of integers to maintain the trie structure.
 * i_GetTriePositionForString_old - This method is the older version of the above method. It takes a
 * string, an array of strings, two arrays of integers, and two integer values. It works in the same way
 * as the newer method but has a different implementation for maintaining the trie structure.
 * i_GetTriePositionForStringAndAddCount - This method is similar to the first method, but it takes an
 * additional integer array. It returns the position of the string in the array and increments the
 * corresponding integer in the integer array if the string already exists, otherwise it adds the
 * string to the array and sets the corresponding integer to 1.
 **/
public class Trie {
    public static int i_GetTriePositionForString(String sText, String[] sArray, int[] iLessPointer, int[] iMorePointer, int iFirstElement, int iLastElement, boolean bDontAddNewString) {
        int iTriePosition;
        int iLastTriePosition;
        if (iLastElement < iFirstElement) {
            sArray[iFirstElement] = sText;
            iLessPointer[iFirstElement] = -1;
            iMorePointer[iFirstElement] = -1;
            return iFirstElement;
        } else {
            iTriePosition = iFirstElement;

//         int iLastTriePosition;
            label33:
            do {
                do {
                    iLastTriePosition = iTriePosition;
                    if (sText.compareTo(sArray[iTriePosition]) < 0) {
                        iTriePosition = iLessPointer[iTriePosition];
                        continue label33;
                    }

                    if (sText.compareTo(sArray[iTriePosition]) <= 0) {
                        return iTriePosition;
                    }

                    iTriePosition = iMorePointer[iTriePosition];
                } while (iTriePosition != -1);

                if (bDontAddNewString) {
                    return -1;
                }

                ++iLastElement;
                sArray[iLastElement] = sText;
                iLessPointer[iLastElement] = -1;
                iMorePointer[iLastElement] = -1;
                iMorePointer[iLastTriePosition] = iLastElement;
                return iLastElement;
            } while (iTriePosition != -1);

            if (bDontAddNewString) {
                return -1;
            } else {
                ++iLastElement;
                sArray[iLastElement] = sText;
                iLessPointer[iLastElement] = -1;
                iMorePointer[iLastElement] = -1;
                iLessPointer[iLastTriePosition] = iLastElement;
                return iLastElement;
            }
        }
    }

    public static int i_GetTriePositionForString_old(String sText, String[] sArray, int[] iLessPointer, int[] iMorePointer, int iLastElement, boolean bDontAddNewString) {
        int iTriePosition;
        int iLastTriePosition;
        if (iLastElement == 0) {
            iLastElement = 1;
            sArray[iLastElement] = sText;
            iLessPointer[iLastElement] = 0;
            iMorePointer[iLastElement] = 0;
            return 1;
        } else {
            iTriePosition = 1;

//         int iLastTriePosition;
            label33:
            do {
                do {
                    iLastTriePosition = iTriePosition;
                    if (sText.compareTo(sArray[iTriePosition]) < 0) {
                        iTriePosition = iLessPointer[iTriePosition];
                        continue label33;
                    }

                    if (sText.compareTo(sArray[iTriePosition]) <= 0) {
                        return iTriePosition;
                    }

                    iTriePosition = iMorePointer[iTriePosition];
                } while (iTriePosition != 0);

                if (bDontAddNewString) {
                    return 0;
                }

                ++iLastElement;
                sArray[iLastElement] = sText;
                iLessPointer[iLastElement] = 0;
                iMorePointer[iLastElement] = 0;
                iMorePointer[iLastTriePosition] = iLastElement;
                return iLastElement;
            } while (iTriePosition != 0);

            if (bDontAddNewString) {
                return 0;
            } else {
                ++iLastElement;
                sArray[iLastElement] = sText;
                iLessPointer[iLastElement] = 0;
                iMorePointer[iLastElement] = 0;
                iLessPointer[iLastTriePosition] = iLastElement;
                return iLastElement;
            }
        }
    }

    public static int i_GetTriePositionForStringAndAddCount(String sText, String[] sArray, int[] iCountArray, int[] iLessPointer, int[] iMorePointer, int iFirstElement, int iLastElement, boolean bDontAddNewString, int iCount) {
        int iPos = i_GetTriePositionForString(sText, sArray, iLessPointer, iMorePointer, iFirstElement, iLastElement, bDontAddNewString);
        if (iPos >= 0) {
            iCountArray[iPos]++;
        }
        return iPos;
    }
}
