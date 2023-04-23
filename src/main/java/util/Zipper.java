package util.misc;

import matchstarting.ControlManager;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static util.FileSystemConstants.*;

/**
 * https://memorynotfound.com/java-7z-seven-zip-example-compress-decompress-file/
 */
public class Zipper {

    /**
     * Kombiniert den Match-Log-Ordner zu einer ZIP-Datei
     * @throws IOException Eine Datei ist nicht vorhanden
     */
    public static void combineZips() throws IOException {
        //Warten, bis alle Prozesse sicher beendet sind
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //
        }
        assert Paths.get(CURRENT_MATCH_DIR.getAbsolutePath()).toFile().isDirectory();
        assert Paths.get(CURRENT_MATCH_DIR.getAbsolutePath()).toFile().listFiles() != null;

        File[] files = Paths.get(CURRENT_MATCH_DIR.getAbsolutePath()).toFile().listFiles();

        try (SevenZOutputFile out = new SevenZOutputFile(new File(LOG_DIR.getAbsolutePath() + "/" +
                ControlManager.getInstance().getCurrentMatch().getMatchID() + ".7z"))){
            for (File file : files){
                addToArchiveCompression(out, file, String.valueOf(ControlManager.getInstance().getCurrentMatch().getMatchID()));
            }
        }
        /*
        GzipParameters params = new GzipParameters();
        params.setCompressionLevel(9);
        TarArchiveOutputStream finalOutput = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(LOG_FILE)), params));
        TarArchiveInputStream tempZip;
        File tempFile = Paths.get(TEMP_LOG_DIR + "/temp").toFile();
        for (File file : files) {
            tempZip = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));
            tempZip.getNextEntry(); // ignore first folder
            ArchiveEntry archiveEntry = tempZip.getNextEntry();
            while (archiveEntry != null) {
                byte[] bytes = new byte[(int) archiveEntry.getSize()];
                tempZip.read(bytes);
                tempFile.delete();
                Files.write(tempFile.toPath(), bytes);
                finalOutput.putArchiveEntry(new TarArchiveEntry(tempFile, archiveEntry.getName()));
                IOUtils.copy(new FileInputStream(tempFile), finalOutput);
                finalOutput.closeArchiveEntry();
                archiveEntry = tempZip.getNextEntry();
            }
            tempZip.close();
        }
        finalOutput.finish();
        finalOutput.close();

        FileUtils.deleteDirectory(Paths.get(TEMP_LOG_DIR).toFile());

         */
    }

    /**
     * Fuegt Dateiinhalt zu einer bestehenden Zip-Datei hinzu
     * @param out Output-Datei
     * @param file Hinzuzufuegende Datei
     * @param dir Directory-Name
     * @throws IOException Eine Datei ist nicht vorhanden
     */
    private static void addToArchiveCompression(SevenZOutputFile out, File file, String dir) throws IOException {
        String name = dir + File.separator + file.getName();
        if (file.isFile()){
            SevenZArchiveEntry entry = out.createArchiveEntry(file, name);
            out.putArchiveEntry(entry);

            FileInputStream in = new FileInputStream(file);
            byte[] b = new byte[1024];
            int count = 0;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.closeArchiveEntry();

        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(out, child, name);
                }
            }
        } else {
            System.out.println(file.getName() + " is not supported");
        }
    }
}
