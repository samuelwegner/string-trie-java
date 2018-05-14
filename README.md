# string-trie-java

This project contains a Java class (StringTrie) implementing a trie data
structure for storing strings, as well as a menu-based console application
for manipulating StringTrie objects.

## Description

StringTrie supports strings containing English letters (A-Za-z),
apostrophes ('), hyphens/dashes (-), and spaces ( ). Strings are converted to
lowercase before insertion into the trie, and lookup methods are case-
insensitive. The class includes methods for typical CRUD operations as well as
file I/O methods for importing and exporting strings.

The major advantage of the trie data structure is its constant time complexity
for insertion, lookup, and deletion operations. These operations are dependent
on the length of the string argument, rather than the number of strings stored
in the trie. Trie data is also stored in sorted order.

Notable disadvantages of the trie include its high space complexity relative to
many other data structures (due to a large number of null pointers present when
storing a typical data set) and the limited number of supported characters
(which is necessary to keep the space complexity within reasonable limits).
Strings are also subject to a maximum length, to bound the space complexity.

## Installing

This project was developed for Java 8, but may be compatible with previous
Java versions.

Only the source files are included in this project. To run the project, open
and compile the source files with your preferred Java IDE; I used NetBeans IDE.
The StringTrie class and StringTrieMenu console application are in separate
packages; if you intend to include StringTrie in your own program, you can
omit the StringTrieMenu package.

## Authors

[Samuel Wegner](https://github.com/samuelwegner/) - Project owner

## License

This project is licensed under the [GPL v3](LICENSE).