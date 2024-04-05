class FileBlock {
    int blockNumber;    // block number
    FileBlock next;     // next block

    public FileBlock(int blockNumber) {
        this.blockNumber = blockNumber;
    }
}

class FileAttributes {
    String fileName;
    int fileSize;
    FileBlock head = null;      // head of linkedlist of blocks 
    int blockCount;             // total number of blocks used by the file 

    public FileAttributes(String fileName, int fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.blockCount = 0;
    }
}