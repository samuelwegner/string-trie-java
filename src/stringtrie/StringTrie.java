/******************************************************************************
    StringTrie.java : Class to represent a string trie data structure.
	
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

/* Project: StringTrie
 * This: StringTrie.java
 * Date: 14-May-2018
 * Author: Sam Wegner
 * Purpose: Class to represent a string trie data structure. The trie stores 
 *          strings in a branching linked list of "node" arrays, with each array
 *          the size of the supported character set. Each node represents
 *          one character; strings beginning with the same character sequence
 *          share the same nodes for their shared prefix characters.
 *          Insertion, lookup, and deletion operations have constant time
 *          complexity.
 *
 *          Note that words loaded into the trie are automatically converted to
 *          lowercase, and all searching methods are case-insensitive.
 *          Words may contain letters (A-Z), apostrophes, hyphens/dashes, and
 *          spaces. The term 'word' is used here to mean a string that exists
 *          in the trie, but these strings may be phrases containing multiple
 *          natural-language words. All public methods are safe to call
 *          regardless of whether words are currently loaded in the trie.
 */
package stringtrie;

import java.util.*;
import java.io.*;

public final class StringTrie {
    private class TrieNode {
        public TrieNode[] next; // Next trie node cluster in word(s)
        public boolean isWord; // Is this the last character of a word?

        public TrieNode() {}
    }
    
    private TrieNode[] root; // Base node cluster
    private int wordCount; // Number of words loaded
    
    // Count of valid characters: 'a'...'z', '\'', '-', ' '
    public static final int CHAR_COUNT = 29;
    // Count of alphabetic characters: 'a'...'z'
    public static final int ALPHA_COUNT = 26;
    // Maximum character length for words
    public static final int WORD_MAX_LENGTH = 128;

    // Node cluster index offsets for non-alphabetic characters
    private static final int APOSTROPHE = 0;
    private static final int HYPHEN = 1;
    private static final int SPACE = 2;
    
    public StringTrie() {}
    
    /**
     * Get count of words currently loaded in the trie.
     * @return Word count
     */
    public int getWordCount() {
        return wordCount;
    }
    
    /**
     * Check whether a character is valid to insert in the trie.
     * Note that alphabetic characters must be converted to lowercase, or they
     * will be considered invalid.
     * @param c Character to check
     * @return True if character is valid
     */
    public static boolean isValid(char c) {
        return ('a' <= c && 'z' >= c) || '\'' == c || '-' == c || ' ' == c;
    }
    
    /**
     * Check whether a character denotes the end of a word from an input stream.
     * This is mainly used for reading text files.
     * @param c Character to check
     * @return True if this is a word delimiter
     */
    public static boolean isDelimiter(char c) {
        return '\n' == c || '\r' == c;
    }
    
    /**
     * Load words from a newline-delimited text file into the trie.
     * Note that the last word in the file must also be followed by a newline.
     * @param path File path to load
     * @return Number of words loaded
     */
    public int loadFile(String path) {
        File inFile = new File(path);
        if (!inFile.exists()) {
            System.err.println("File does not exist: " + path);
            return 0;
        }
        else if (!inFile.canRead()) {
            System.err.println("Cannot read file: " + path);
            return 0;
        }
        
        // Create base node cluster, if necessary
        if (null == root) root = alloc();
        
        int wordsLoaded = 0;

        // Start reading from file
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
            new FileInputStream(inFile))))
        {
            TrieNode[] cluster = root;
            int cn = 0; // Number of valid chars processed in current word
            int ci = 0; // Node cluster index
            int r;
            char c;
            while ((r = br.read()) >= 0) {
                c = Character.toLowerCase((char)r);
                if (isValid(c)) {
                    // Skip word if too long
                    if (WORD_MAX_LENGTH < (cn + 1)) {
                        while((r = br.read()) >= 0) {
                            if (isDelimiter((char)r)) break;
                        }
                        cluster = root;
                        cn = 0;
                        continue;
                    }
                    
                    // Get next cluster, after first valid char
                    if (0 < cn) cluster = append(cluster, ci);
                    
                    // Insert char
                    ci = getIndex(c);
                    if (null == cluster[ci]) cluster[ci] = new TrieNode();

                    ++cn;
                }
                else {
                    // End of word
                    if (0 < cn && isDelimiter(c)) {
                        setWord(cluster, ci, true);
                        ++wordsLoaded;
                    }

                    // Prepare for new word
                    cluster = root;
                    cn = 0;
                }
            }
        }
        catch (IOException ex) {
            System.err.println("Failed to read from file: " + path);
        }
        
        if (0 >= wordCount) {
            unload();
            wordsLoaded = 0;
        }
        return wordsLoaded;
    }
    
    /**
     * Add a single word to the trie.
     * @param word String to be added
     * @return True if word was added
     */
    public boolean addWord(String word) {
        // Create base node cluster, if necessary
        if (null == root) root = alloc();
        
        TrieNode[] cluster = root;
        int cn = 0; // Number of valid chars processed in current word
        int ci = 0; // Node cluster index
        char c;
        for (int i = 0; i < word.length(); ++i) {
            c = Character.toLowerCase(word.charAt(i));
            if (isValid(c)) {
                // Skip word if too long
                if (WORD_MAX_LENGTH < (cn + 1)) return false;

                // Get next cluster, after first valid char
                if (0 < cn) cluster = append(cluster, ci);

                // Insert char
                ci = getIndex(c);
                if (null == cluster[ci]) cluster[ci] = new TrieNode();
                
                ++cn;
                
                // End of word
                if (0 < cn && (word.length() - 1) == i) {
                    setWord(cluster, ci, true);
                    return true;
                }
            }
            else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Remove a single word from the trie.
     * @param word Word string to be removed
     */
    public void removeWord(String word) {
        // Quit if no words are loaded
        if (null == root) return;
        
        TrieNode[] cluster = root;
        int ci; // Node cluster index
        char c;
        for (int i = 0; i < word.length(); ++i) {
            c = Character.toLowerCase(word.charAt(i));
            if (!isValid(c)) return;
            
            ci = getIndex(c);
            
            // Quit if char was not inserted
            if (null == cluster[ci]) return;
            
            // End of word
            if ((word.length() - 1) == i) {
                setWord(cluster, ci, false);
                return;
            }
            
            // Quit if node is a dead end
            if (null == cluster[ci].next) return;
            
            cluster = cluster[ci].next;
        }
    }
    
    /**
     * Unload all words from the trie.
     */
    public void unload() {
        root = null;
        wordCount = 0;
    }
    
    /**
     * Search for an exact word match in the trie.
     * @param word Word string to match
     * @return True if word was found
     */
    public boolean search(String word) {
        // Quit if no words are loaded
        if (null == root) return false;
        
        TrieNode[] cluster = root;
        int ci; // Node cluster index
        char c;
        for (int i = 0; i < word.length(); ++i) {
            c = Character.toLowerCase(word.charAt(i));
            if (!isValid(c)) return false;
            
            ci = getIndex(c);
            
            // Quit if char was not inserted
            if (null == cluster[ci]) return false;
            
            // End of word
            if ((word.length() - 1) == i) return cluster[ci].isWord;
            
            // Quit if node is a dead end
            if (null == cluster[ci].next) return false;
            
            cluster = cluster[ci].next;
        }
        return false;
    }
    
    /**
     * Search for words containing (inclusive) a given prefix in the trie.
     * For example, searching the prefix "cat" would find both "cat"
     * and "cathode" if those words are in the trie.
     * @param prefix Prefix search string
     * @param output List to store words found
     * @return Number of words found
     */
    public int searchPrefix(String prefix, List<String> output) {
        // Quit if no words are loaded
        if (null == root) return 0;
        
        char[] buffer = new char[WORD_MAX_LENGTH]; // String building buffer
        int bi = 0; // Current buffer index
        
        TrieNode[] cluster = root;
        int ci = 0; // Node cluster index
        char c;
        for (int i = 0; i < prefix.length(); ++i) {
            c = Character.toLowerCase(prefix.charAt(i));
            if (!isValid(c)) return 0;
            
            ci = getIndex(c);
            
            // Quit if char was not inserted
            if (null == cluster[ci]) return 0;
            
            // Quit if node is a dead end
            if (null == cluster[ci].next && ((prefix.length() - 1) != i)) {
                return 0;
            }
            
            buffer[bi] = getValue(ci);
            ++bi;
            
            if ((prefix.length() - 1) != i) cluster = cluster[ci].next;
        }
        
        int wordsFound = 0;
        
        // Is the prefix a word?
        if (cluster[ci].isWord) {
            output.add(new String(buffer, 0, bi + 1));
            ++wordsFound;
        }
        
        // Recursively search the trie, starting from current node
        if (null != cluster[ci].next) {
            wordsFound += searchPrefixRecur(prefix, output,
                cluster[ci].next, buffer, bi);
        }
        
        return wordsFound;
    }
    
    /**
     * Recursively search for words containing a given prefix in the trie.
     * This method should be accessed through helper method searchPrefix().
     * @param prefix Prefix search string
     * @param output List to store words found
     * @param cluster Current node cluster
     * @param buffer Character array for string building
     * @param bi Current buffer index
     * @return Number of words found
     */
    private int searchPrefixRecur(String prefix, List<String> output,
        TrieNode[] cluster, char[] buffer, int bi)
    {
        int wordsFound = 0;
        
        for (int ci = 0; ci < cluster.length; ++ci) {
            // Skip unused nodes
            if (null == cluster[ci]) continue;
            
            buffer[bi] = getValue(ci);
            
            if (cluster[ci].isWord) {
                output.add(new String(buffer, 0, bi + 1));
                ++wordsFound;
            }
            
            if (null != cluster[ci].next) {
                wordsFound += searchPrefixRecur(prefix, output,
                    cluster[ci].next, buffer, bi + 1);
            }
        }
        return wordsFound;
    }
    
    /**
     * Print all words currently loaded in the trie to standard output.
     */
    public void printWords() {
        printWords(System.out, false);
    }
    
    /**
     * Print all words currently loaded in the trie to a given output stream.
     * @param outStream Output stream
     * @param closeStream Set to true if the OutputStream should be closed
     *                    after printing
     */
    public void printWords(OutputStream outStream, boolean closeStream) {
        // Quit if no words are loaded
        if (null == root) return;
        
        PrintWriter pw = null;
        boolean streamOpen = false;
        try {
            pw = new PrintWriter(outStream, true);
            streamOpen = true;
            
            char[] buffer = new char[WORD_MAX_LENGTH];

            printWordsRecur(pw, root, buffer, 0);
        }
        catch (Exception ex) {
            System.err.println("Failed to write to output stream");
        }
        finally {
            if (streamOpen && closeStream && null != pw) pw.close();
        }
    }
    
    /**
     * Recursively print all words currently loaded in the trie.
     * This method should be accessed through helper method printWords().
     * @param pw Print writer for output
     * @param cluster Current node cluster
     * @param buffer Character array for string building
     * @param bi Current buffer index
     */
    private void printWordsRecur(PrintWriter pw, TrieNode[] cluster,
        char[] buffer, int bi)
    {
        for (int ci = 0; ci < cluster.length; ++ci) {
            // Skip unused nodes
            if (null == cluster[ci]) continue;
            
            buffer[bi] = getValue(ci);
            
            if (cluster[ci].isWord) {
                for (int j = 0; j <= bi; ++j) pw.print(buffer[j]);
                pw.println();
            }
            
            if (null != cluster[ci].next) {
                printWordsRecur(pw, cluster[ci].next, buffer, bi + 1);
            }
        }
    }
    
    /**
     * Create a new node cluster. Individual nodes must be initialized when
     * a character is inserted.
     * @return New node cluster
     */
    private TrieNode[] alloc() {
        return new TrieNode[CHAR_COUNT];
    }
    
    /**
     * Create a new node cluster (if necessary) linked to the specified node.
     * If the node already has a linked cluster, that cluster will be
     * returned instead.
     * Note that only valid cluster indices should be passed to this method.
     * @param cluster Node cluster to link from
     * @param ci Node cluster index
     * @return Next node cluster
     */
    private TrieNode[] append(TrieNode[] cluster, int ci) {
        if (null == cluster[ci].next) cluster[ci].next = alloc();
        
        return cluster[ci].next;
    }
    
    /**
     * Convert a character into the associated node cluster index.
     * Note that letters should be converted to lowercase before passing to
     * this method.
     * @param c Character to convert
     * @return Index in range 0 to (CHAR_COUNT - 1)
     */
    private static int getIndex(char c) {
        if ('a' <= c && 'z' >= c) {
            return c - 'a';
        }
        else if ('\'' == c) {
            return APOSTROPHE + ALPHA_COUNT;
        }
        else if ('-' == c) {
            return HYPHEN + ALPHA_COUNT;
        }
        else if (' ' == c) {
            return SPACE + ALPHA_COUNT;
        }
        else {
            throw new IllegalArgumentException(
                String.format("Cannot index character '%c'", c));
        }
    }
    
    /**
     * Convert a node cluster index into the associated character.
     * @param ci Node cluster index
     * @return Character represented by this index
     */
    private static char getValue(int ci) {
        if ( 0 <= ci && ALPHA_COUNT > ci) {
            return (char)(ci + 'a');
        }
        else if ((APOSTROPHE + ALPHA_COUNT) == ci) {
            return '\'';
        }
        else if ((HYPHEN + ALPHA_COUNT) == ci) {
            return '-';
        }
        else if ((SPACE + ALPHA_COUNT) == ci) {
            return ' ';
        }
        else {
            throw new IllegalArgumentException(
                String.format("Cluster index %d is invalid", ci));
        }
    }
    
    /**
     * Define or undefine a word ending with a character at a given node.
     * Note that only valid cluster indices should be passed to this method.
     * @param cluster Node cluster containing last character of word
     * @param ci Node cluster index of last character in word
     * @param makeWord Set to true to define a word, or false to remove a word
     */
    private void setWord(TrieNode[] cluster, int ci, boolean makeWord) {
        if (makeWord && !cluster[ci].isWord) {
            cluster[ci].isWord = true;
            ++wordCount;
        }
        else if (!makeWord && cluster[ci].isWord) {
            wordCount = (0 < wordCount) ? (wordCount - 1) : 0;
            
            if (null == cluster[ci].next) {
                cluster[ci] = null;
            }
            else {
                cluster[ci].isWord = false;
            }
        }
    }
}
