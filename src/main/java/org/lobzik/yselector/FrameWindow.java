package org.lobzik.yselector;


import com.github.sardine.DavResource;

import java.awt.Frame;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.TreeMap;
import java.util.LinkedList;

/**
 *
 * /**
 *
 * @author lobzik
 */
public class FrameWindow extends Frame implements ActionListener, WindowListener {

    TextArea ta;
    MenuBar mb;

    Menu mFile;
    MenuItem miOpen;
    MenuItem miSave;
    MenuItem miExit;

    TextField source;
    Button browseSource;

    Button copyB;
    Label result;
    TextArea notfoundLabel;
    FileDialog fdlg;
    CopyListener copyListener;
    StopListener stopListener;
    Frame frame;

    public FrameWindow(String szTitle, Properties props) {
        super(szTitle);
        setSize(800, 500);

        mb = new MenuBar();
        mFile = new Menu("File");

        miOpen = new MenuItem("Open...");
        mFile.add(miOpen);

        // miSave = new MenuItem("Save As...");
        // mFile.add(miSave);
        mFile.add("-");

        miExit = new MenuItem("Exit");
        mFile.add(miExit);

        mb.add(mFile);

        miOpen.addActionListener(this);
        //miSave.addActionListener(this);
        miExit.addActionListener(this);

        setMenuBar(mb);

        this.addWindowListener(this);

        Label l1 = new Label("Working folder");
        l1.setBounds(30, 50, 100, 30);
        add(l1);

        source = new TextField("");
        source.setBounds(30, 80, 500, 30);
        add(source);

        browseSource = new Button("Browse");
        browseSource.setBounds(550, 80, 80, 30);
        browseSource.addActionListener(this);
        add(browseSource);



        copyB = new Button("Copy!");
        copyB.setBounds(30, 120, 80, 30);
        copyListener = new CopyListener(props);
        stopListener = new StopListener();
        copyB.addActionListener(copyListener);
        add(copyB);

        result = new Label("");
        result.setBounds(15, 150, 800, 70);
        add(result);

        notfoundLabel = new TextArea("");
        notfoundLabel.setBounds(15, 250, 700, 170);
        add(notfoundLabel);

        this.setLayout(null);
        frame = this;
    }

    public void windowClosing(WindowEvent e) {
        setVisible(false);
        System.exit(0);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(miOpen) || e.getSource().equals(browseSource)) {
            fdlg = new FileDialog(this, "Open from", FileDialog.LOAD);
            fdlg.show();
            source.setText(fdlg.getDirectory());

        }
    }


    public class CopyListener implements ActionListener {
        Properties props;
        public CopyListener(Properties props) {
            super();
            this.props = props;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(copyB)) {
                if (fdlg == null || fdlg.getDirectory() == null || fdlg.getDirectory().length() == 0) {
                    result.setText("Working folder not selected");
                    return;
                }
                try {

                    copyB.removeActionListener(copyListener);
                    copyB.addActionListener(stopListener);
                    copyB.setLabel("STOP");
                    File ods = new File(fdlg.getDirectory() + File.separator + fdlg.getFile());
                    TreeMap picsMap = ODSParser.parse(ods);
                    File workingDir = new File(fdlg.getDirectory());
                    result.setText("Fetch from " + workingDir.getName());
                    new Thread(() -> {
                        WebDavWorker.fetch(workingDir, result, notfoundLabel, picsMap, props);
                        copyB.removeActionListener(stopListener);
                        copyB.addActionListener(copyListener);
                        copyB.setLabel("Copy!");
                    }).start();


                } catch (Exception ex) {
                    result.setText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public class StopListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(copyB)) {
                WebDavWorker.finish();
            }
        }
    }

}
