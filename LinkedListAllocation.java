class Block {
    char data; // Or however you wish to represent the data within a block
    int nextBlockIndex; // Index of the next block in the file; -1 if this is the last block

    // Constructor, getters, and setters
}

// Changes in ContigFileAllocation
public class ContigFileAllocation {
    Block[] blockArray = new Block[200]; // Use Block objects instead of char

    // Adjust methods for write, read, and delete operations
}