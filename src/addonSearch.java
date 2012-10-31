import java.awt.Desktop;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.event.*;
import javax.swing.text.Document;
public class addonSearch extends javax.swing.JFrame {
    listener ys;
    public static File log;
    public static BufferedWriter log_out;
    public static URL logfileURL;
    public static String ysDir;
    private static ArrayList<String> localDB;
    /** Creates new form addonSearch */
    public addonSearch() {
        initComponents();
        ys = new listener();
        
        try {
            logfileURL = new URL("file:/"+System.getProperty("user.dir")+"/addonsearch.htm");
            log = new File(logfileURL.toURI());
            log.delete();
            log = new File(logfileURL.toURI());
            log_out = new BufferedWriter(new FileWriter(log, true));
            log_out.write("");
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot create logfile for chat window."
                    + " Try running this program in 'My Documents'.\n"+ex, "addonsearch", JOptionPane.ERROR_MESSAGE);
        }
        
        File file = null;
        BufferedWriter output;
        try {
            file = new File(new URL("file:/"+System.getProperty("user.dir")+"/addonsearch.cfg").toURI());
            if (file.exists()) ys_cfg(file);
            else {
                output = new BufferedWriter(new FileWriter(file));
                jFrame1.setSize(580, 420);
                jFrame1.setVisible(true);
            }
        } catch (Exception ex) {
            if (!ex.toString().equals("java.io.IOException: Stream closed")) System.out.println(ex);
            //JOptionPane.showMessageDialog(this, "Impossible to read the YSChat config file!\n"+ex, "YS_chat", JOptionPane.ERROR_MESSAGE);
        }
        
        makeLocalDB();
        
        messOut.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        });
    }
    
    private void ys_cfg(File file) throws FileNotFoundException, IOException, URISyntaxException {
        BufferedReader input;
        String line;
        input = new BufferedReader(new FileReader(file));
        try {
            while ((line = input.readLine()) != null) {
                if (line.length() > 17) {
                    if (line.substring(0, 17).equals("YSDIR           =")) {
                        ysDir = line.substring(17);
                        try {
                            file = new File(ysDir + "/config/network.cfg");
                            input = new BufferedReader(new FileReader(file));
                            try {
                                while ((line = input.readLine()) != null) {
                                    line = line.trim();
                                    if (line.substring(0, 10).equals("PORTNUMBER")) {
                                        portBox.setText(line.substring(11));
                                    }
                                }
                            } finally {
                                input.close();
                            }
                            try {
                                URL url = new URL("http://www.yspilots.com/shadowhunters/rssList.php");
                                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                                String s;
                                while ((s = in.readLine()) != null) {
                                    if (s.indexOf("http://www.yspilots.com/shadowhunters/get_info.php?ip=")!=-1) 
                                        ipBox.addItem(s.substring(s.indexOf("http://www.yspilots.com/shadowhunters/get_info.php?ip=")+54, s.indexOf("&amp;port=")));
                                }
                                in.close();
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(this, "Impossible to get the online server list!", "addonsearch", JOptionPane.ERROR_MESSAGE);
                                file = new File(ysDir + "/config/serverhistory.txt");
                                input = new BufferedReader(new FileReader(file));
                                try {
                                    while ((line = input.readLine()) != null) {
                                        if (line.length() > 0) {
                                            ipBox.addItem(line.trim());
                                        }
                                    }
                                } finally {
                                    input.close();
                                }
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, "Impossible to find the file "+file.getName()+" or to read it!\n"+ex, "addonsearch", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (line.substring(0, 17).equals("YSVERSION       =")) {
                        versionBox.setText(line.substring(17));
                    }
                }
            }
        } finally {
            input.close();
        }
    }
    
    private void makeLocalDB() {
        if (!ysDir.isEmpty()) {
            localDB = new ArrayList<String>();
            File aircraftDir = new File(ysDir + "/aircraft");
            String list[] = aircraftDir.list();
            for (String lst:list) {
                if (lst.lastIndexOf(".")!=-1)
                    if (lst.substring(lst.lastIndexOf(".")).equalsIgnoreCase(".lst"))
                        readLST(ysDir + "/aircraft/" + lst);
            }
            ys.localAddons = localDB;
        }
    }
    
    private void readLST(String file) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(file)));
            try {
                String line;
                while ((line = input.readLine()) != null) if (!line.isEmpty()) readDAT(ysDir + "/" + line.substring(0, line.indexOf(" ")));
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    private void readDAT(String file) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(file)));
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    int index = line.indexOf("IDENTIFY ");
                    int index2 = line.indexOf("IDENTIFY ".toLowerCase());
                    if (index!=-1) localDB.add(line.substring(index+10, line.length()-1));
                    else if (index2!=-1) localDB.add(line.substring(index2+10, line.length()-1));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public static void refresh() throws IOException {
        log_out.flush();
        Document doc = messOut.getDocument();
        doc.putProperty(Document.StreamDescriptionProperty, null);
        messOut.setPage(logfileURL);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        messOut_scroll = new javax.swing.JScrollPane();
        messOut = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        ipLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        portBox = new javax.swing.JTextField();
        versLabel = new javax.swing.JLabel();
        versionBox = new javax.swing.JTextField();
        connButton = new javax.swing.JButton();
        openInstaller = new javax.swing.JButton();
        ipBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        jFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jFrame1.setTitle("Change YS install dir");
        jFrame1.setAlwaysOnTop(true);

        jFileChooser1.setCurrentDirectory(new java.io.File("C:\\"));
            jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
            jFileChooser1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jFileChooser1ActionPerformed(evt);
                }
            });

            jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
            jLabel1.setText("Select the directory YSFlight is installed in");

            javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
            jFrame1.getContentPane().setLayout(jFrame1Layout);
            jFrame1Layout.setHorizontalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jFrame1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1)
                    .addContainerGap(346, Short.MAX_VALUE))
                .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
            );
            jFrame1Layout.setVerticalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jFrame1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE))
            );

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            setTitle("AddonSearch");

            messOut.setContentType("text/html");
            messOut.setEditable(false);
            messOut.setText("<html>\r<body>\r</body>\r</html>\r\n");
            messOut_scroll.setViewportView(messOut);

            ipLabel.setText("IP");

            portLabel.setText("Port");

            portBox.setText("7915");

            versLabel.setText("Version");

            versionBox.setText("20110207");

            connButton.setFont(new java.awt.Font("Tahoma", 1, 18));
            connButton.setText("Connect!");
            connButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    connButtonActionPerformed(evt);
                }
            });

            openInstaller.setFont(new java.awt.Font("Tahoma", 0, 18));
            openInstaller.setText("Open addon installer");
            openInstaller.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    openInstallerActionPerformed(evt);
                }
            });

            ipBox.setEditable(true);

            javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(ipLabel)
                            .addGap(18, 18, 18)
                            .addComponent(ipBox, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(portLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(portBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(versLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(versionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(18, 18, 18)
                    .addComponent(connButton, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(64, 64, 64)
                    .addComponent(openInstaller)
                    .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ipLabel)
                        .addComponent(ipBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(portLabel)
                        .addComponent(versLabel)
                        .addComponent(versionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(portBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(connButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(openInstaller, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(66, 66, 66))
            );

            jLabel2.setText("made by erict15 for YSFHQ.com");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(messOut_scroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(messOut_scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel2))
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void connButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connButtonActionPerformed
        if (!ipBox.getSelectedItem().toString().isEmpty() && !portBox.getText().isEmpty() && !versionBox.getText().isEmpty()) {
            ys.ip = ipBox.getSelectedItem().toString();
            ys.port = portBox.getText();
            ys.version = versionBox.getText();
            try {
                if (!ys.connected) ys.connect();
                else ys.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (!ys.connected) connButton.setText("Connect!");
            else connButton.setText("Disconnect!");
        }
    }//GEN-LAST:event_connButtonActionPerformed

    private void openInstallerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInstallerActionPerformed
        installGUI install = new installGUI();
        install.setVisible(true);
    }//GEN-LAST:event_openInstallerActionPerformed

    private void jFileChooser1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser1ActionPerformed
        if (!evt.getActionCommand().equals("CancelSelection")) {
            ysDir=jFileChooser1.getSelectedFile().toString();
            File file = null;
            try {
                file = new File(new URL("file:/"+System.getProperty("user.dir")+"/addonsearch.cfg").toURI());
                BufferedWriter output = new BufferedWriter(new FileWriter(file));
                jFrame1.setSize(580, 420);
                jFrame1.setVisible(true);
                try {
                    output.write("YSDIR           =" + ysDir + "\nYSVERSION       ="+versionBox.getText());
                } finally {
                    output.close();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot write to addonsearch config file!\n"+ex, "addonsearch", JOptionPane.ERROR_MESSAGE);
            }
            try {ys_cfg(file);} catch (Exception ex) {}
        }
        jFrame1.setVisible(false);
}//GEN-LAST:event_jFileChooser1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new addonSearch().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JButton connButton;
    private javax.swing.JComboBox ipBox;
    private javax.swing.JLabel ipLabel;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    public static javax.swing.JEditorPane messOut;
    private javax.swing.JScrollPane messOut_scroll;
    private javax.swing.JButton openInstaller;
    private javax.swing.JTextField portBox;
    private javax.swing.JLabel portLabel;
    private javax.swing.JLabel versLabel;
    private javax.swing.JTextField versionBox;
    // End of variables declaration//GEN-END:variables

}
