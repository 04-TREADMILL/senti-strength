package nju.SEIII.EASIEST.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOps {
    public static boolean backupFileAndDeleteOriginal(String sFileName, int iMaxBackups) {
        int iLastBackup;
        File f;
        for (iLastBackup = iMaxBackups; iLastBackup >= 0; --iLastBackup) {
            f = new File(sFileName + iLastBackup + ".bak");
            if (f.exists()) {
                break;
            }
        }

        if (iLastBackup < 1) {
            f = new File(sFileName);
            if (f.exists()) {
                f.renameTo(new File(sFileName + "1.bak"));
                return true;
            } else {
                return false;
            }
        } else {
            if (iLastBackup == iMaxBackups) {
                f = new File(sFileName + iLastBackup + ".bak");
                try {
                    Path f_path = Paths.get(f.toURI());
                    Files.delete(f_path);
                } catch (IOException e) {
                }
                --iLastBackup;
            }

            for (int i = iLastBackup; i > 0; --i) {
                f = new File(sFileName + i + ".bak");
                f.renameTo(new File(sFileName + (i + 1) + ".bak"));
            }

            f = new File(sFileName);
            f.renameTo(new File(sFileName + "1.bak"));
            return true;
        }
    }

    public static int i_CountLinesInTextFile(String sFileLocation) {
        int iLines = 0;

        try {
            BufferedReader rReader;
            for (rReader = new BufferedReader(new FileReader(sFileLocation)); rReader.ready(); ++iLines) {
                rReader.readLine();
            }

            rReader.close();
            return iLines;
        } catch (IOException var5) {
            var5.printStackTrace();
            return -1;
        }
    }

    public static String getNextAvailableFilename(String sFileNameStart, String sFileNameEnd) {
        for (int i = 0; i <= 1000; ++i) {
            String sFileName = sFileNameStart + i + sFileNameEnd;
            File f = new File(sFileName);
            if (!f.isFile()) {
                return sFileName;
            }
        }

        return "";
    }

    public static String s_ChopFileNameExtension(String sFilename) {
        if (sFilename != null && !sFilename.equals("")) {
            int iLastDotPos = sFilename.lastIndexOf(".");
            if (iLastDotPos > 0) {
                sFilename = sFilename.substring(0, iLastDotPos);
            }
        }

        return sFilename;
    }
}
