/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Lab 5 COSC 310                                             *
*                                                            * 
* This is a custom file class for a file allocation          *
* simulator.                                                 *
*                                                            *
* @author Cadence Stewart                                    * 
* @version Apr 5 2024                                        *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public class File {
    private String fileName;
    private int fileSize;
    private int numBlocks;
    private int startIndex;
    private int endIndex;

    // constructor
    public void FileObject(String fileName, int fileSize, int numBlocks, int startIndex, int endIndex) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.numBlocks = numBlocks;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    // Getter methods
    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}