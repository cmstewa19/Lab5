/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Lab 5 COSC 3-10                                            * 
* This is a test routine for Contiguous File allocation.     *
* @author Cadence Stewart                                    * 
* @version Apr 5 2024                                        *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public class testContig{

    public static void main(String[] args) {
        ContigFileAllocation ContigFileAllocation = new ContigFileAllocation();

        // write 
        String fileName = "file1";
        int fileSize = 15000;
        ContigFileAllocation.write(fileName, fileSize);

        // write 
        fileName = "file2";
        fileSize = 2000;
        ContigFileAllocation.write(fileName, fileSize);

        // write 
        fileName = "file3";
        fileSize = 2048;
        ContigFileAllocation.write(fileName, fileSize);

        // dir
        fileMap.dir();
        
        // del)
        fileName = "file3";
        ContigFileAllocation.del(fileName);
        fileMap.dir();

        // write
        fileName = "file4";
        fileMap.dir();
        ContigFileAllocation.access(fileName);

        ContigFileAllocation.dump(ContigFileAllocation.blockArray);

        ContigFileAllocation.dump_all(ContigFileAllocation.blockArray);

        System.out.println("Exiting...");
    }
}
