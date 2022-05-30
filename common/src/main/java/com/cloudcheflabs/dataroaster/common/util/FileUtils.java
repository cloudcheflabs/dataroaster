package com.cloudcheflabs.dataroaster.common.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.*;
import java.net.URL;

public class FileUtils {

    private static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static InputStream readFile(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream readFileFromClasspath(String filePath) {
        try {
            return new ClassPathResource(filePath).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fileToString(String filePath, boolean fromClasspath) {
        try {
            return fromClasspath ? IOUtils.toString(readFileFromClasspath(filePath)) :
                    IOUtils.toString(readFile(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stringToFile(String string, String path, boolean executable) {
        try{
            Writer output = new BufferedWriter(new FileWriter(path));
            output.write(string);
            output.close();
            new File(path).setExecutable(executable);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void createDirectory(String directoryPath) {
        try {
            org.apache.commons.io.FileUtils.forceMkdir(new File(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDirectory(String directoryPath) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(directoryPath));
        } catch (IOException e) {
        }
    }

    public static void copyFilesFromClasspathToFileSystem(String rootPathInClasspath, String dirToScan, String targetDirectory) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        String path = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + rootPathInClasspath + "/" + dirToScan + "/*";
        try {
            Resource[] resources = resolver.getResources(path);
            for (Resource resource : resources) {
                if (resource.exists() & resource.isReadable() && resource.contentLength() > 0) {
                    URL url = resource.getURL();
                    String urlString = url.toExternalForm();
                    LOG.info("urlString: {}", urlString);

                    String subDir = dirToScan + "/";

                    String targetName = urlString.substring(urlString.lastIndexOf(subDir) + subDir.length());
                    LOG.info("targetName: {}", targetName);

                    File destination = new File(targetDirectory, targetName);
                    org.apache.commons.io.FileUtils.copyURLToFile(url, destination);
                    LOG.info("Copied " + url + " to " + destination.getAbsolutePath());
                } else {
                    LOG.debug("Did not copy, seems to be directory: " + resource.getDescription());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
