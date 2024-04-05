/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Lab 5 COSC 310                                             *
*                                                            * 
* This class contains files with attributes in a hashmap.    *
*                                                            *
* @author Cadence Stewart                                    * 
* @version Apr 5 2024                                        *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


public class FileMap {
    private HashMap<String, Map<String, Integer>> fileMap;

    // constructor
    public FileMap() {
        this.fileMap = new HashMap<>();
    }

    // add a file
    public void addFile(String fileName, int fileSize, int numBlocks, int startIndex) {
        Map<String, Integer> fileAttributes = new HashMap<>();
        fileAttributes.put("filesize", fileSize);
        fileAttributes.put("numblocks", numBlocks);
        fileAttributes.put("startindex", startIndex);

        fileMap.put(fileName, fileAttributes);
    }
    
    // delete file by file name
    public void deleteFile(String fileName) {
        fileMap.remove(fileName);
    }

    // dir - print directory listing
    public void dir() {
        System.out.println("Directory Listing: ");
        for (Map.Entry<String, Map<String, Integer>> entry : fileMap.entrySet()) {
            String fileName = entry.getKey();
            Map<String, Integer> fileAttributes = entry.getValue();
            int fileSize = fileAttributes.get("filesize");
            int numBlocks = fileAttributes.get("numblocks");
            int startIndex = fileAttributes.get("startindex");

            System.out.printf("  %s:   %d bytes in %d blocks, starting at block #%d%n", 
            fileName, fileSize, numBlocks, startIndex);
        }
    }

    // Getter methods
    public int getFileSize(String fileName) {
        Map<String, Integer> fileAttributes = fileMap.get(fileName);
        return fileAttributes.getOrDefault("filesize", -1);
    }

    public int getNumBlocks(String fileName) {
        Map<String, Integer> fileAttributes = fileMap.get(fileName);
        return fileAttributes.getOrDefault("numblocks", -1);
    }

    public int getStartIndex(String fileName) {
        Map<String, Integer> fileAttributes = fileMap.get(fileName);
        return fileAttributes.getOrDefault("startindex", -1);
    }

    public Set<String> getFileNames() {     // method to return all fileNames in the hashmap
        return new HashSet<>(fileMap.keySet());
    }
}