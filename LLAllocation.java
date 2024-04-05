/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Lab 5 COSC 310                                             *
*                                                            * 
* This is a Linked-List Allocation file system simulator.    *
*                                                            *
* @author Cadence Stewart                                    * 
* @version Apr 5 2024                                        *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;

public class LLAllocation {
    private static Scanner input=new Scanner(System.in);

    private FileBlock[] blocks = new FileBlock[200];
    private char[] blockStatuses = new char[200];
    private HashMap<String, FileAttributes> files = new HashMap<>();

    private static final int BLOCKSIZE = 1024;
    final char OPEN = '_';
    final char USED = 'U';

    // constructor
    public LLAllocation() {
        // initalize free blocks
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = null;
        }
        // initialize all bloackStatuses to OPEN
        Arrays.fill(blockStatuses, OPEN);
    }

    // dir - print directory listing
    public void dir() {
        System.out.println("Directory Listing: ");
        for (Map.Entry<String, FileAttributes> entry : files.entrySet()) {
            String fileName = entry.getKey();
            FileAttributes fileAttributes = entry.getValue();

            int fileSize = fileAttributes.fileSize;
            int startBlock = fileAttributes.head != null ? fileAttributes.head.blockNumber : -1;
            int blockCount = fileAttributes.blockCount;

            System.out.printf("  %s:   %d bytes in %d blocks, starting at block #%d%n", 
            fileName, fileSize, blockCount, startBlock);
        }
    }

    // write - writes a fileName of some byte size to the blockMap
    public void write(String fileName, int fileSize) {

        if (files.containsKey(fileName)) {
            System.out.println("Error: duplicate file.");
            return;
        }
        
        int numBlocks = (int)Math.ceil((double)fileSize / BLOCKSIZE); // convert file size in bytes to block size (kb)
        
        
        FileAttributes attributes = new FileAttributes(fileName, fileSize);
        int allocatedBlocks = 0;
        FileBlock lastBlock = null;

        for (int i = 0; i < blocks.length && allocatedBlocks < numBlocks; i++) {
            if (blocks[i] == null) { // if block is free
                blockStatuses[i] = USED;

                FileBlock newBlock = findFreeBlock();
                blocks[i] = newBlock; // allocate block

                if (lastBlock == null) {
                    attributes.head = newBlock; // first block of file
                } else {
                    lastBlock.next = newBlock;
                }
                lastBlock = newBlock;
                allocatedBlocks++;
            }
        }
        
        attributes.blockCount = allocatedBlocks; // update reference to last block
        files.put(fileName, attributes);
        
        System.out.printf("Writing %s to disk in %d blocks.%n", fileName, numBlocks);    
    }

    // findFreeBlock - helper method
    private FileBlock findFreeBlock() {
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] == null) {
                return new FileBlock(i);
            }
        }
        return null; // if no free block is found
    }

    public void access(String fileName) {
        // check if file exists
        if (!files.containsKey(fileName)) {
            System.out.printf("Error: File %s does not exist.%n", fileName);
            return;
        }

        // get attributes
        FileAttributes fileAttributes = files.get(fileName);

        // count blocks and bytes
        int fileSize = fileAttributes.fileSize;     // get file size
        int physicalSize = fileAttributes.blockCount * BLOCKSIZE;   // calculate physical size

        System.out.printf("Reading file %s: %d bytes (%d bytes on disk)%n", 
            fileName, fileSize, physicalSize);
    }
    

    public void del(String fileName) {
        // check if file exists
        if (!files.containsKey(fileName)) {
            System.out.printf("File %s not found.%n", fileName);
            return;
        }

        // get file attributes
        FileAttributes fileAttributes = files.get(fileName);

        // traverse linkedlist and delete blocks
        FileBlock currentBlock = fileAttributes.head;
        FileBlock previousBlock = null;     

        while (currentBlock != null){
            int blockIndex = currentBlock.blockNumber;
            blocks[blockIndex] = null; // set block as free
            blockStatuses[blockIndex] = OPEN;   // mark as open in the block status array
            
            FileBlock nextBlock = currentBlock.next;

            // Unlink the current block
            if (previousBlock != null) {
                previousBlock.next = null;
            } else {
                fileAttributes.head = null; // If it's the head, set head to null
            }
            // Move to the next block
            previousBlock = currentBlock;
            currentBlock = nextBlock; 
        }

        files.remove(fileName);

        System.out.printf("File %s deleted.%n", fileName);
    }

    // dump
    public void dump(char[] blockStatuses) {
        System.out.println("Block Status Dump:");
        // display block statuses 
        for (int i = 0; i < blockStatuses.length; i++) {
            if ((i % 10) == 0) {
                System.out.printf("%4d: ", i);
            }

            System.out.print(blockStatuses[i] + " "); 

            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
    }

    //  dump-all - shows the info given by the “dump” command as well as a map of each file block (which file it maps to – or “free”)
    public void dump_all() {
        dump(blockStatuses); // Dump block status information
    
        System.out.printf("%nFile Lists:%n");
    
        // Iterate through each file in the fileMap
        for (String fileName : files.keySet()) {
            FileAttributes fileAttributes = files.get(fileName);
    
            System.out.printf("File: %s%n", fileName);

            FileBlock currentBlock = fileAttributes.head;
            while (currentBlock != null) {
                System.out.printf(" -> Block #%d%n", currentBlock.blockNumber);
                currentBlock = currentBlock.next;
            }
            System.out.println();
        }

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
                dir();
            }

            else if (choice.equals("dump")){
                dump(blockStatuses);
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
        LLAllocation LLAllocation = new LLAllocation();
        LLAllocation.driver();
    }

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
}
