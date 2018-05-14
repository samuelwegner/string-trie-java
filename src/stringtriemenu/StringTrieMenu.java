/******************************************************************************
    StringTrieMenu.java : User interface for manipulating a StringTrie.
	
    Copyright (C) 2018  Samuel Wegner (samuelwegner@hotmail.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
******************************************************************************/

/* Project: StringTrieMenu
 * This: StringTrieMenu.java
 * Date: 12-May-2018
 * Author: Sam Wegner
 * Purpose: Implements a basic console user interface for manipulating a
 *          StringTrie.
 */
package stringtriemenu;

import stringtrie.*;
import java.util.*;
import java.io.*;

public final class StringTrieMenu {
	private static final Scanner USER_IN = new Scanner(System.in);

    public static void main(String[] args) {
        StringTrie trie = new StringTrie();
        
		boolean repeat = true;
		do {
			System.out.println("\n\t\tTRIE WORD LIST");
			System.out.printf("\nWords currently loaded: %d\n",
				trie.getWordCount());
			System.out.print(
                "\nSelect an option:\n"
				+ "\t1. Load words from a text file\n"
                + "\t2. Add a single word\n"
                + "\t3. Print all words to the screen\n"
                + "\t4. Write all words to a text file\n"
                + "\t5. Search for an exact word match\n"
                + "\t6. Search for words containing a prefix\n"
                + "\t7. Remove a single word\n"
                + "\t8. Unload all words\n"
                + "\t0. Exit program\n> ");
            
            String tmp = USER_IN.nextLine();
            char option = (0 < tmp.length()) ? tmp.charAt(0) : '?';
            
            switch(option) {
                case '1':
                    menuLoadFile(trie);
                    break;
                case '2':
                    menuAddWord(trie);
                    break;
                case '3':
                    menuPrintWords(trie);
                    break;
                case '4':
                    menuWriteFile(trie);
                    break;
                case '5':
                    menuSearch(trie);
                    break;
                case '6':
                    menuSearchPrefix(trie);
                    break;
                case '7':
                    menuRemoveWord(trie);
                    break;
                case '8':
                    menuUnload(trie);
                    break;
                case '0':
                    repeat = false;
                    break;
                default:
                    System.out.println("Invalid selection");
            }
		} while (repeat);
    }
    
    private static void menuLoadFile(StringTrie trie) {
        System.out.print(
            "\nText files must include a newline after each word,"
            + "\nincluding the last word. Valid characters are:"
            + "\nletters (A-Z), apostrophe, hyphen, and space\n"
            + "\nEnter the file path: ");
        
        String path = USER_IN.nextLine();
        int words = trie.loadFile(path);
        
        System.out.printf(
            "\nAdded %d words to the trie.\n"
            + "\nPress Enter to continue.\n",
            words);
        USER_IN.nextLine();
    }

    private static void menuAddWord(StringTrie trie) {
        System.out.print(
            "\nValid word characters are:"
            + "\nletters (A-Z), apostrophe, hyphen, and space\n"
            + "\nEnter a word to be added: ");
        
        String word = USER_IN.nextLine();
        
        System.out.printf(
            "\nWord \"%s\" was%s added to the trie.\n"
            + "\nPress Enter to continue.\n",
            word, trie.addWord(word) ? "" : " not");
        USER_IN.nextLine();
    }

    private static void menuPrintWords(StringTrie trie) {
        System.out.print("\nWords currently in the trie:\n");
        trie.printWords();
        
        System.out.print("\nPress Enter to continue.\n");
        USER_IN.nextLine();
    }

    private static void menuWriteFile(StringTrie trie) {
        System.out.print("\nEnter the output file path:\n");
        
        String path = USER_IN.nextLine();
        File outFile = new File(path);
        boolean error = false;
        
        if (outFile.exists() && !outFile.canWrite()) {
            System.out.print(
                "\nCannot write to the specified path."
                + "\nCanceling write operation.\n");
            error = true;
        }
        
        if (!error && outFile.exists()) {
            boolean valid = false;
            do {
                System.out.print(
                    "\nFile already exists."
                    + "\nDo you want to overwrite it? (Y/N) ");

                String tmp = USER_IN.nextLine();
                char option = (0 < tmp.length()) ? tmp.charAt(0) : '?';
                switch (option) {
                    case 'Y':
                    case 'y':
                        valid = true;
                        break;
                    case 'N':
                    case 'n':
                        System.out.println("Canceling write operation.");
                        error = true;
                        valid = true;
                        break;
                    default:
                        System.out.println("Invalid selection");
                }
            } while (!valid);
        }
        
        if (!error) {
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                trie.printWords(out, true);
                System.out.print("\nFinished writing to file.\n");
            }
            catch (Exception ex) {
                System.err.println("Failed to write to file: " + path);
            }
        }

        System.out.print("\nPress Enter to continue.\n");
        USER_IN.nextLine();
    }

    private static void menuSearch(StringTrie trie) {
        System.out.print(
            "\nValid word characters are:"
            + "\nletters (A-Z), apostrophe, hyphen, and space\n"
            + "\nEnter a word to search for: ");
        
        String word = USER_IN.nextLine();
        
        System.out.printf(
            "\nWord \"%s\" was%s found in the trie.\n"
            + "\nPress Enter to continue.\n",
            word, trie.search(word) ? "" : " not");
        USER_IN.nextLine();
    }

    private static void menuSearchPrefix(StringTrie trie) {
        System.out.print(
            "\nPrefix search will find any words (inclusive) that"
            + "\nbegin with the prefix characters.\n"
            + "\nValid word characters are:"
            + "\nletters (A-Z), apostrophe, hyphen, and space\n"
            + "\nEnter a word prefix to search for: ");
        
        String prefix = USER_IN.nextLine();
        List<String> words = new ArrayList<>();
        int wordCount = trie.searchPrefix(prefix, words);
        
        System.out.printf(
            "\nFound %d words containing the \"%s\" prefix:\n",
            wordCount, prefix);
        
        for (int i = 0; i < wordCount; ++i) {
            System.out.println(words.get(i));
        }

        System.out.print("\nPress Enter to continue.\n");
        USER_IN.nextLine();
    }
    
    private static void menuRemoveWord(StringTrie trie) {
        System.out.print(
            "\nValid word characters are:"
            + "\nletters (A-Z), apostrophe, hyphen, and space\n"
            + "\nEnter a word to be removed: ");
        
        trie.removeWord(USER_IN.nextLine());
        
        System.out.printf(
            "\nTrie now contains %d words.\n"
            + "\nPress Enter to continue.\n",
            trie.getWordCount());
        USER_IN.nextLine();
    }

    private static void menuUnload(StringTrie trie) {
        trie.unload();
        
        System.out.print(
            "\nAll words have been removed from the trie.\n"
            + "\nPress Enter to continue.\n");
        USER_IN.nextLine();
    }
}
