package org.lobzik.yselector;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class YDiskSelector {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
/*
        try {
            HashMap picsMap = ODSParser.parse(new File("/home/lobzik/Temp/ydisk-test/1554_9Е/1554_9Е.ods"));
            System.out.println(picsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        FrameWindow frame = new FrameWindow("Yandex Disk Selector v0.1 by l0bzik for Agata :)");
        frame.setVisible(true);

    }


}
