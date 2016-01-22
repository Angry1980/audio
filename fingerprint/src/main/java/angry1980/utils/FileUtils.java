package angry1980.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    private static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static List<Path> getFiles(Path path, String postfix){
        final List<Path> files = new ArrayList<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(!attrs.isDirectory() && file.toString().endsWith(postfix)){
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.error("Error while trying to get files with postfix in {}", postfix, path.toString());
        }
        return files;
    }

    public static List<Path> getDirs(Path path){
        try {
            return Files.list(path).filter(Files::isDirectory).collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while trying to get dirs in {}", path.toString());
        }
        return Collections.emptyList();
    }
}
