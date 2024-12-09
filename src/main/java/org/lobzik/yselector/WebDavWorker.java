package org.lobzik.yselector;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebDavWorker {
    final static String URL = "https://webdav.yandex.ru/";
    final static String ROOT_FOLDER = "/Сезон 2025";
    final static String LOGIN = "agata-119";
    final static String PSWD = "rigkxpbecusshfux";
    final static AtomicBoolean run = new AtomicBoolean(true);

    public static void fetch(File sourceDir, Label result, TextArea notfoundLabel, TreeMap picsMap) {
        run.set(true);
        notfoundLabel.setText("");
        LinkedList<DavResource> filesList = new LinkedList();
        String folderName = sourceDir.getName();
        String startFolder = ROOT_FOLDER + "/" + folderName;



        Sardine sardine = SardineFactory.begin(LOGIN, PSWD);
        try {
            List<DavResource> list = new LinkedList();
            String startFolderUrl = getAbsUrl(startFolder);
            list.addAll(sardine.list(startFolderUrl));
            for (int i = 0; i < list.size(); i++) {
                if (!run.get()) break;
                DavResource res = list.get(i);
                String url = getAbsUrl(res);

                if (res.isDirectory()) {
                    if (!url.equals(startFolderUrl)) {
                        //System.out.println(url);
                        result.setText("Looking in " + url);
                        try {
                            List<DavResource> subdir = sardine.list(url);
                            for (DavResource dr : subdir) {
                                if (dr.getPath().equals(res.getPath())) continue;
                                boolean exists = false;
                                for (DavResource item : list) {
                                    if (item.getPath().equals(dr.getPath())) exists = true;
                                }
                                if (!exists) list.add(dr);
                            }
                        } catch (Exception se) {
                            System.out.println("Error while looking in " + url);
                            se.printStackTrace();
                            continue;
                        }


                    }
                } else {
                    filesList.add(res);//System.out.println(res.getPath());
                }

            }
            result.setText(filesList.size() + " files found");
            HashMap<String, DavResource> filesMap = new HashMap();
            for (DavResource dr : filesList) {
                String numberName = extractNumberFromFilename(dr.getName());
                filesMap.put(numberName, dr);
            }
            Thread.sleep(1500);

            long size = 0l;
            int notFoundCounter = 0;
            List<String> notFoundList = new LinkedList();
            int foundCounter = 0;
            int skippedCounter = 0;
            for (Object key: picsMap.keySet()) {
                if (!run.get()) break;
                String name = (String) key;
                String lastNameFolder = name;
                String fileNamePrefix = "";
                if (name.startsWith("Portrait_")) {
                    lastNameFolder = name.substring("Portrait_".length());
                    //fileNamePrefix = "1PORT_";
                }
                File nameDir = new File (sourceDir.getAbsolutePath()+ File.separator + lastNameFolder);
                if (!nameDir.exists()) {
                    nameDir.mkdir();
                } else if (nameDir.isFile()) {
                    continue;
                }
                HashSet<String> filesSet = (HashSet<String>) picsMap.get(key);
                for (String fileNum : filesSet) {
                    if (!run.get()) break;
                    DavResource dr = filesMap.get(fileNum);
                    if (dr == null) {
                        notFoundList.add(nameDir.getName() + File.separator + fileNum);
                        continue;
                    }

                    foundCounter++;
                    String filename = nameDir.getName() + File.separator + fileNamePrefix + dr.getName();

                    String url = getAbsUrl(dr);
                    File dst = new File(nameDir.getAbsolutePath() + File.separator + fileNamePrefix + dr.getName());
                    if (dst.exists() && dst.isFile() && Files.size(Paths.get(dst.getAbsolutePath())) == dr.getContentLength()) {
                        skippedCounter++;
                        continue;

                    }


                    size += dr.getContentLength();

                    try (InputStream is = sardine.get(url);
                         OutputStream os = new FileOutputStream(dst);) {
                        rewriteStreams(is, os, filename, dr.getContentLength(), result);
                    }
                }

            }

            result.setText("Done. " + humanBytes(size) + " downloaded. " + foundCounter + " files found, " + notFoundList.size() + " not found, " + skippedCounter + " skipped.");
            StringBuilder notFoundText = new StringBuilder();
            notFoundText.append("Files not found:\n");
            for (String nfFile:notFoundList) notFoundText.append(nfFile).append("\n");
            notfoundLabel.setText(notFoundText.toString());

        } catch (Exception e) {
            result.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getAbsUrl(String drPath) {
        String path = drPath.replaceAll(" ", "%20").substring(1);
        String url = URL + path;
        return url;
    }

    public static String getAbsUrl(DavResource dr) {
        return getAbsUrl(dr.getPath());
    }

    public static void rewriteStreams(InputStream is, OutputStream os, String filename, long size, Label result) throws Exception {
        long bufferLength = 65536;
        byte[] buff = new byte[(int) bufferLength];
        int count = 0;
        long bytes = 0;
        while ((count = is.read(buff)) != -1) {
            if (!run.get()) break;
            long percent = 100 * bytes / size;
            result.setText("Downloading " + filename + ", " + percent + "%");
            os.write(buff, 0, count);
            bytes += count;
        }
    }

    public static String humanBytes(long length) {
        String[] prefixes = {"Bytes", "KB", "MB", "GB"};
        float num = length;
        final int step = 1024;
        for (int i = 0; i < prefixes.length; i++) {
            String val = prefixes[i];
            if (num < step) {
                return (((num >= 2) ? getFormattedFloat(num, 1) : getFormattedFloat(num, (i == 0) ? 0 : 2)) + " " + val);
            }
            num = num / step;
        }
        return (length + " " + prefixes[0]);
    }

    public static String getFormattedFloat(float val, int fraction) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(fraction);
        nf.setMinimumFractionDigits(fraction);
        String resStr = nf.format(val);
        return resStr;
    }

    public static String extractNumberFromFilename(String filename) {
        if (filename.contains(".")) {
            int beginIndex = filename.lastIndexOf(".") - 5;
            if (beginIndex < 0) beginIndex = 0;
            filename = filename.substring(beginIndex, filename.lastIndexOf("."));//remove extension
        }
        filename = filename.replaceAll("\\D+", "");
        filename = ODSParser.removeLeadingZeroes(filename);
        return filename;
    }

    public static void finish()  {
        run.set(false);
    }
}
