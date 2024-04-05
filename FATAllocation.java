/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Lab 5 COSC 310                                             *
*                                                            * 
* This is a Linked-List Allocation using Memory Table        *
* (FAT) file system simulator.                               *
*                                                            *
* @author Cadence Stewart                                    * 
* @version Apr 5 2024                                        *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FATAllocation {
    private static Scanner input=new Scanner(System.in);

    private FATEntry[] fat = new FATEntry[200];
    private HashMap<String, FileAttributes> files = new HashMap<>();

    private static final int BLOCKSIZE = 1024;
    final char OPEN = '_';
    final char USED = 'U';

    // constructor
    public FATAllocation() {
        // initalize FAT
        for (int i = 0; i < fat.length; i++) {
            fat[i] = new FATEntry(OPEN, i); // initialize as open with no next block 
        }
    }

    // dir - print directory listing
    public void dir() {
        System.out.println("Directory Listing: ");
        for (Map.Entry<String, FileAttributes> entry : files.entrySet()) {
            String fileName = entry.getKey();
            FileAttributes fileAttributes = entry.getValue();

            int fileSize = fileAttributes.fileSize;
            int startBlock = fileAttributes.head != -1 ? fileAttributes.head : -1;
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
        int lastBlock = -1;

        for (int i = 0; i < fat.length && allocatedBlocks < numBlocks; i++) {
            if (fat[i].status == OPEN) { // if block is free
                fat[i].status = USED;

                if (lastBlock == -1) {
                    attributes.head = i; // first block of file
                } else {
                    fat[lastBlock].next = i;    // link new block to last block
                }
                lastBlock = i;
                allocatedBlocks++;
            }
        }
        
        attributes.blockCount = allocatedBlocks; // update reference to last block
        files.put(fileName, attributes);
        
        System.out.printf("Writing %s to disk in %d blocks.%n", fileName, numBlocks);    
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

        // traverse FAT and set blocks to OPEN
        int currentBlock = fileAttributes.head;   

        while (currentBlock != -1){
            int nextBlock = fat[currentBlock].next;
            fat[currentBlock].status = OPEN; // set block as free
            fat[currentBlock].next = -1;   
            currentBlock = nextBlock;
        }

        files.remove(fileName);

        System.out.printf("File %s deleted.%n", fileName);
    }

    public void dump(FATEntry[] fat) {
        System.out.println("Block Status Dump:");
        // display block statuses 
        for (int i = 0; i < fat.length; i++) {
            if ((i % 10) == 0) {
                System.out.printf("%4d: ", i);
            }

            System.out.print(fat[i].status + " "); 

            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }

    //  dump-all - shows the info given by the “dump” command as well as a map of each file block (which file it maps to – or “free”)
    public void dump_all(FATEntry[] fat) {
        dump(fat); // Dump file allocation table 
    
        System.out.printf("%nFile Lists:%n");
    
        // Iterate through each file in the fileMap
        for (String fileName : files.keySet()) {
            FileAttributes fileAttributes = files.get(fileName);
    
            System.out.printf("File: %s%n", fileName);

            int startBlock = fileAttributes.head;
            int currentBlockIndex = startBlock;

            while (currentBlockIndex != -1 && currentBlockIndex < fat.length) {
                FATEntry currentEntry = fat[currentBlockIndex];
                System.out.printf(" -> Block #%d%n", currentBlockIndex);
                currentBlockIndex = currentEntry.next;
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
                dump(fat);
            }

            else if (choice.equals("dump-all")){
                dump_all(fat);
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
        FATAllocation FATAllocation = new FATAllocation();
        FATAllocation.driver();
    }
    
    class FATEntry {
        char status;
        int next;
    
        public FATEntry(char status, int next) {
            this.status = status;
            this.next = next;
        }
    }
    
    class FileAttributes {
        String fileName;
        int fileSize;
        int head;
        int blockCount;
    
        public FileAttributes(String fileName, int fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.head = -1; // -1 indicates no blocks allocated yet
            this.blockCount = 0;
        }
    }
}