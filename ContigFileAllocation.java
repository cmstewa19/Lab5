/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Lab 5 COSC 310                                             *
*                                                            * 
* This is a Contiguous Allocation file system simulator.     *
*                                                            *
* @author Cadence Stewart                                    * 
* @version Apr 5 2024                                        *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ContigFileAllocation{

    private static Scanner input=new Scanner(System.in);

    char[] blockArray = new char[200];
    FileMap fileMap;

    final int BLOCKSIZE = 1024;
    final char OPEN = '_';
    final char USED = 'U';
    final char DELETED = 'D';

    // constructor
    public ContigFileAllocation() {
        for (int i = 0; i < blockArray.length; i++) {
            blockArray[i] = OPEN;
        }
        this.fileMap = new FileMap();
    }

    // write - writes a fileName of some byte size to the blockMap
    public void write(String fileName, int fileSize){
        int numBlocks = (int)Math.ceil((double)fileSize / BLOCKSIZE); // convert file size in bytes to block size (kb)

        // check disk utilization
        if (calculateDiskUse() > 0.8) {
            compactDisk();
        }

        // check if enough space after compaction
        boolean enoughSpace = checkSpace(fileSize, numBlocks); 
        if (!enoughSpace) {
            System.out.println("Error: Not enough space available after compaction to write file.");
            return;
        }

        int startIndex = -1;
        int endIndex = -1;
        boolean writeSuccess = false;
        
        // find first open set of contiguous blocks 
        for (int i = 0; i <= blockArray.length - numBlocks; i++) {
            boolean spaceFound = true;
            
            for (int j = i; j < i + numBlocks; j++) {
                if (blockArray[j] != OPEN) {
                    spaceFound = false;
                    break;      // exit if non-open block is found
                }
            }

            // if a big enough contiguous space is found set start and end index
            if (spaceFound) {
                startIndex = i;
                endIndex = i + numBlocks;
                break;          
            }
        }

        // error if no open block is found
        if (startIndex == -1) {
            System.out.println("Error: No open contiguous block space found.");
            return;
        }

        // set 'open' blocks to 'used' to simulate file writing to the block 
        for (int i = startIndex; i < endIndex; i++) {
                blockArray[i] = USED;         
        }
        // update fileMap
        fileMap.addFile(fileName, fileSize, numBlocks, startIndex);
        writeSuccess = true;

        if (writeSuccess) {
            System.out.printf("Writing %s to disk in %d blocks.%n", fileName, numBlocks);
        } else {
            System.out.println("Error: write failed.");
        }
    }

    // access - counts the number of blocks (and therefore bytes) read to read a given file
    public void access(String fileName) {

        int fileSize = fileMap.getFileSize(fileName);
        int numBlocks = fileMap.getNumBlocks(fileName);

        if (fileSize != -1 && numBlocks != -1) {        
            int physicalSize = numBlocks * BLOCKSIZE;
            System.out.printf("Reading file %s: %d bytes (%d bytes on disk)%n", 
                fileName, fileSize, physicalSize);
        } else {
            System.out.println("File not found: " + fileName);
        }
    }

    // delete
    public void del(String fileName) {
        
        // get start and end indices from fileMap
        int startIndex = fileMap.getStartIndex(fileName);
        int endIndex = fileMap.getStartIndex(fileName) + fileMap.getNumBlocks(fileName);

        boolean deleteSuccess = false;

        // set 'used' blocks to 'deleted' 
        for (int i = startIndex; i < endIndex; i++) {
            if (blockArray[i] == USED) {
                blockArray[i] = DELETED;      
                deleteSuccess = true;
            }
        }

        if (deleteSuccess) {
            fileMap.deleteFile(fileName);           // delete file from fileMap
            System.out.printf("File %s deleted.%n", fileName);
        } else {
            System.out.println("Error: delete failed.");
        }
    }

    // dump
    public void dump(char[] blockArray) {
        System.out.println("Block Status Dump:");
        // print array
        for (int i = 0; i < blockArray.length; i++) {
            if (i % 10 == 0) {
                System.out.printf("%4d:  ", i);
            }

            System.out.print(blockArray[i] + " ");

            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
    }
    
    //  dump-all - shows the info given by the “dump” command as well as a map of each file block (which file it maps to – or “free”)
    public void dump_all() {
        dump(blockArray); 
        System.out.printf("%nFile Map:%n");

        // iterate through through each block in blockArray
        for (int i = 0; i < blockArray.length; i++) {
            String allocatedToFile = null;

            // iterate through each file in the fileMap to check if the block belongs to it
            for (String fileName : fileMap.getFileNames()) { 
                int startIndex = fileMap.getStartIndex(fileName);
                int endIndex = fileMap.getStartIndex(fileName) + fileMap.getNumBlocks(fileName);

                if (i >= startIndex && i < endIndex) {
                    allocatedToFile = fileName;
                    break; // found the file to which block is allocated
                }
            }

            // print information about the block
            if (allocatedToFile != null) {
                System.out.printf("Block %d -> %s%n", i, allocatedToFile);
            }
        }
    }

    // disk compaction - mark DELETED as OPEN
    public void compactDisk() {
        for (int i = 0; i < blockArray.length; i++) {
            if (blockArray[i] == DELETED) {
                blockArray[i] = OPEN;
            }
        }
    }

    // calculate disk use
    private double calculateDiskUse() {
        int usedBlocks = 0;
        for (int i = 0; i < blockArray.length; i++) {
            if (blockArray[i] == USED) {
                usedBlocks++;
            }
        }
        // calculate the disk utilization percentage
        return (double) usedBlocks / blockArray.length;
    }

    // check if there is enough contiguous space
    private boolean checkSpace(int fileSize, int numBlocks) {

        int contigOpenBlocks = 0;
        
        // iterate through blockArray to find a contiguous sequence of OPEN blocks
        for (int i = 0; i < blockArray.length; i++) {
            if (blockArray[i] == OPEN) {
                contigOpenBlocks++;
                
                // if enough space is found
                if (contigOpenBlocks == numBlocks) {
                    return true;
                }
            } else {
                // reset count if a used block is found
                contigOpenBlocks = 0;
            }
        }
        return false; // not enough space
    }


    // driver 
    public void driver(){
        String choice; 

        do {
            choice = input.nextLine();

            // pattern to store address in file system simulator
            Pattern writePattern = Pattern.compile("store ([^/]+) (\\d+)"); 
            Matcher writeMatcher = writePattern.matcher(choice);

            // pattern to access filename for use in file system simulator
            Pattern accessPattern = Pattern.compile("access ([^/]+)"); 
            Matcher accessMatcher = accessPattern.matcher(choice);   

            // pattern to delete filename for use in file system simulator
            Pattern delPattern = Pattern.compile("del ([^/]+)"); 
            Matcher delMatcher = delPattern.matcher(choice);            

            // write 
            if (writeMatcher.find()) {
                String fileName = writeMatcher.group(1);
                int fileSize = Integer.parseInt(writeMatcher.group(2));
                write(fileName, fileSize);
            }

            // access 
            else if (accessMatcher.find()) {
                String fileName = accessMatcher.group(1);
                access(fileName);
            }

            // delete
            else if (delMatcher.find()) {
                String fileName = delMatcher.group(1);
                del(fileName);
            }

            else if (choice.equals("dir")){
                fileMap.dir();
            }

            else if (choice.equals("dump")){
                dump(blockArray);
            }

            else if (choice.equals("dump-all")){
                dump_all();
            }

            else if (choice.equals("?") || choice.equals("help")){
                        System.out.println("-------------");                
                        System.out.println("| Commands: |");
                        System.out.println("-------------");
                        System.out.printf("   %-15s %n", "store <fileName> <numBytes>");
                        System.out.printf("   %-15s %n", "access <filename>");
                        System.out.printf("   %-15s %n%n", "del <filename>");
                        System.out.printf("   %-15s %s%n", "dir", "Lists filesystem contents & attributes");
                        System.out.printf("   %-15s %s%n", "dump", "Writes a disk-block map");
                        System.out.printf("   %-15s %s%n", "dump-all", "Writes a disk-block map");
                        System.out.printf("   %-15s %s%n", "? or help", "Prints this command list");
                        System.out.printf("   %-15s %n", "exit or x");
            }

            else if (choice.equals("exit") || choice.equals("x")){
                System.out.println("Exiting...");
                return;
            }

            else{
                System.out.println("Invalid input. Enter ? or help for help screen.");
            }
        } while (true); 
    }

    // main
    public static void main(String[] args) {
        ContigFileAllocation ContigFileAllocation = new ContigFileAllocation();
        ContigFileAllocation.driver();
    }
}