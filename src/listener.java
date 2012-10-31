import java.io.*;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;

public class listener {
    public String ip;
    public String port;
    public String version;
    public boolean connected;
    public ArrayList<String> localAddons;
    private String username="addonsearch";
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Thread keepAlive;
    private Thread listener;

    public void connect() throws IOException {
        if (!connected) {
            try {
                socket = new Socket(ip, Integer.parseInt(port));
            } catch (Exception ex) {
                addonSearch.log_out.append("<font color=red><b>error connecting to server "+ip+":"+port+"</b></font><br/>");
                addonSearch.refresh();
                return;
            }
            in = socket.getInputStream();
            out = socket.getOutputStream();
            String packet = "";
            try {
                int vers = Integer.parseInt(version.substring(0,8));
                version = "0"+Integer.toHexString(vers);
                version=version.substring(6)+version.substring(4,6)+version.substring(2,4)+version.substring(0,2);
            } catch (NumberFormatException ex) {
                System.out.println("You must put a number in the version box!");
                return;
            }
            if (StringToHex(username).length()>30) {
                packet = "1800000001000000"+StringToHex(username).substring(0, 30);
                packet+="00"+version;
            } else {
                packet = "1800000001000000"+StringToHex(username);
                for (int i=0; i<15-username.length(); i++) {packet+="00";}
                packet+="00"+version;
            }
            byte[] b = new BigInteger(packet, 16).toByteArray();
            out.write(b);
            connected=true;
            SendMess30s SendMess30s = new SendMess30s(keepAlive);
            keepAlive = new Thread(SendMess30s);
            keepAlive.start();
            OutputListener OutputListener = new OutputListener(listener);
            listener = new Thread(OutputListener);
            listener.start();
            
            addonSearch.log_out.append("<font color=red><b><u>***You are connected***</u></b></font><br/>");
            addonSearch.refresh();
        }
    }

    public void disconnect() throws IOException {
        if (connected) {
            socket.close(); connected=false; keepAlive.stop(); listener.stop();
            
            addonSearch.log_out.append("<font color=red><b><u>***Connection stopped***</u></b></font><br/>");
            addonSearch.refresh();
        }
    }

    private String StringToHex(String str) {
        char[] chars = str.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    private String HexToString(String str) {
        str=str.replaceAll("/", "").replaceAll("x", "");
        byte[] b = new BigInteger(str, 16).toByteArray();
        return new String(b);
    }

    private String byteToString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += "/x"+Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private class SendMess30s implements Runnable {
        Thread SendMess30s;
        public SendMess30s(Thread t) {
            SendMess30s = t;
        }
        public void run() {
            while (connected) {
                try {
                    out.write(new BigInteger("0400000025000000", 16).toByteArray());
                    Thread.sleep(30000);
                } catch (InterruptedException ex) {
                    SendMess30s.stop();
                } catch (IOException ex) {
                    System.out.println("disconnected from server");
                    connected = false;
                    SendMess30s.stop();
                }
            }
        }
        public void stop() {
            SendMess30s.stop();
        }
    }

    private class OutputListener implements Runnable {
        Thread OutputListener;
        String listplayer = "/x00/x00/x25/x00/x00/x00";
        public OutputListener (Thread t) {OutputListener = t;}
        public void run() {
            String ismess = "/x00/x00/x00/x20/x00/x00/x00/x00/x00/x00/x00/x00/x00/x00/x00";
            while (connected) {
                try {
                    byte[] b = new byte[2048];
                    in.read(b);
                    String mess = byteToString(b);
                    int is_mess = mess.indexOf(ismess);
                    is_mess = mess.indexOf(ismess);
                    while (is_mess != -1) {
                        String mess3 = "";
                        if (is_mess != -1) {
                            String mess2 = mess.substring(is_mess + 15 * 4);
                            int pos = mess2.indexOf("/x00");
                            mess2 = mess2.substring(0, pos);
                            mess = mess.substring(is_mess + 15 * 4 + pos);
                            mess3 = mess2.replaceAll("/x00", "");
                            is_mess = mess.indexOf(ismess);
                        }
                        mess3=HexToString(mess3).replaceAll("(-!-)", " ");
                        
                        int index = mess3.indexOf("took off (");
                        int index2 = mess3.indexOf("took off [");
                        if ((index!=-1 || index2!=-1) && mess3.substring(0, 15).indexOf('*')==-1) {
                            String plane;
                            if (index2!=-1) {
                                index2=index2+10;
                                plane = mess3.substring(index2);
                                plane = plane.substring(0, plane.indexOf("](IFF"));
                            } else {
                                index=index+10;
                                plane = mess3.substring(index);
                                plane = plane.substring(0, plane.length()-1);
                            }
                            if (!searchLocal(plane)) {
                                ArrayList<String[]> link = search(plane);
                                if (link==null) addonSearch.log_out.append(
                                        "Aircraft <i>"+plane+"</i> not found.<br/>");
                                else for (String[] pack:link) {
                                   addonSearch.log_out.append(
                                           "Install <i>"+pack[4]+"</i> from pack \"<b>"+pack[1]
                                           + "</b>\" on <a href=\""+pack[0]+"\">"+pack[3]+"</a><br/>"); 
                                }
                                addonSearch.log_out.append(mess3+"<br/><br/>");
                                addonSearch.refresh();
                            }
                        }
                    }
                } catch (IOException ex) {
                    System.out.println(ex);
                    connected=false;
                }
            }
            try {
                addonSearch.log_out.append("<font color=red><b>connection lost to server "+ip+":"+port+"</b></font><br/>");
                disconnect();
                addonSearch.connButton.setText("Connect!");
                addonSearch.refresh();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    private ArrayList<String[]> search(String plane) {
        try {
            plane = URLEncoder.encode(plane, "UTF-8");
            URL result = new URL("http://uploader.s57.xrea.com/finder/result.php?category=aircraft&keyword="+
                    plane+"&search_mode=or&sort_category=aircraft&order_category=asc");
            BufferedReader in = new BufferedReader(new InputStreamReader(result.openStream()));
            String inputLine;
            String out = "";
            while ((inputLine = in.readLine()) != null) out+=inputLine+"\n";
            in.close();
            
            if (out.indexOf("Sorry,We did not find results for: ")!=-1) return null;
            
            out = out.substring(out.indexOf("\n<tr>")+1).replaceAll("<tr><td ", "\n<tr><td ");
            ArrayList<String> rows = new ArrayList<String>();
            while (out.indexOf("\n<tr>")!=-1) {
                try {rows.add(out.substring(out.indexOf("\n<tr>"), out.replaceFirst("\n<tr>", "").indexOf("\n<tr>")));
                } catch (StringIndexOutOfBoundsException ex) {rows.add(out.substring(out.indexOf("\n<tr>")));}
                out = out.replaceFirst("\n<tr>", "");
                if (out.indexOf("\n<tr>")!=-1) out = out.substring(out.indexOf("\n<tr>"));
                else break;
            }
            
            ArrayList<String[]> output = new ArrayList<String[]>();
            for (String row:rows) {
                //System.out.println(row);
                if (row.indexOf("<span style='background-color:yellow;font-weight:bold;'>")!=-1) {
                    String link, packName, website, siteName, planeName;
                    try {
                        planeName = row.substring(row.indexOf("<span style='background-color:yellow;font-weight:bold;'>")+56);
                        planeName = planeName.substring(0, planeName.indexOf("</td>")).replaceAll("</span>", "");
                        
                        website = row.substring(row.indexOf("<tr><td style='border-right:none;'><a href='")+44);
                        website = website.substring(0, website.indexOf("</a>"));
                        siteName = website.substring(website.indexOf("' target='_blank'>")+18);
                        website = website.substring(0, website.indexOf("' target='_blank'>"));

                        link = row.substring(row.indexOf("</td><td style='border-right:none;'><a href='")+45);
                        link = link.substring(0, link.indexOf("</a>"));
                        packName = link.substring(link.indexOf("' target='_blank'>")+18);
                        link = link.substring(0, link.indexOf("' target='_blank'>"));
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                        return null;
                    }
                    if (link!=null && packName!=null && website!=null && siteName!=null) {
                        String[] pack = new String[5];
                        pack[0] = link;
                        pack[1] = packName;
                        pack[2] = website;
                        pack[3] = siteName;
                        pack[4] = planeName;
                        output.add(pack);
                    }
                } else continue;
            }
            return output;
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
    
    private boolean searchLocal(String plane) {
        if (localAddons!=null) if (localAddons.indexOf(plane)!=-1) return true;
        return false;
    }
}