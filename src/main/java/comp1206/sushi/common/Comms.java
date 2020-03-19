package comp1206.sushi.common;

import java.io.*;
import java.util.*;

//Comms class a File based information exchange
//not functional enough to support sending between 2 separate applications,
//however able to proven to be able to send Arraylist<Postcard> objects
//with a concurrent thread
public class Comms implements Serializable {

    // saves the file to be worked from and the homeDirectory of messages
    String currentDir = System.getProperty("user.dir");
    private File clientMessages = new File(currentDir + "/messages/ClientMessages/");
    private File messagesFolder = new File(currentDir + "/messages/");
    private File receiveFromServer;
    private File workingFile;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    int id;
    private boolean serv;
    public static boolean permissionC = false;
    public static boolean permissionS = false;
    private File serverMessages = new File(currentDir + "/messages/ServerMessages/");

    // if the server creates the homedirectory
    // if client, makes itself file in homedirectory and creates write/read objects
    public Comms(boolean serv) {
        try {
            this.serv = serv;
            String currentDir = System.getProperty("user.dir");
            if (serv) {
                deleteDir(serverMessages);
                deleteDir(clientMessages);
                deleteDir(messagesFolder);

                messagesFolder.mkdir();
                serverMessages.mkdir();
                clientMessages.mkdir();
            } else {
                id = clientMessages.listFiles().length + 1;
                workingFile = new File(currentDir + "/messages/ClientMessages/" + id + ".ser");
                receiveFromServer = new File(currentDir + "/messages/ServerMessages/" + id + ".ser");
                workingFile.createNewFile();
                out = new ObjectOutputStream(new FileOutputStream(workingFile, false));
                in = new ObjectInputStream(new FileInputStream(workingFile));
                in.close();
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // deletes content of a file when read, so checking for filesize is sure way to
    // ensure that it is a new message
    public Object recieve() {
        try {
            if (serv) {
                File[] files = clientMessages.listFiles();
                if (files == null) {
                    return null;
                } else {
                    for (File f : files) {
                        if (f.length() != 0 && f.isFile()) {
                            //System.out.println("2");
                            workingFile = f;
                            String s = workingFile.getName();
                            //System.out.println(s);
                            id = Integer.parseInt(s.split("\\.")[0]);
                            in = new ObjectInputStream(new FileInputStream(workingFile));
                            Object message = in.readObject();
                            in.close();
                            if (workingFile.delete()) {
                                workingFile = null;
                                //System.out.println("YESS");
                                //workingFile.createNewFile();
                            }
                            return message;
                        }
                    }
                }
                return null;
            } else {
                File[] files = serverMessages.listFiles();
                if (files == null) {
                    return null;
                } else {
                    for (File f : files) {
                        int newId = Integer.valueOf(f.getName().split("\\.")[0]);
                        if (newId == id && f.length() != 0 && f.isFile()) {
                            //System.out.println("4");
                            workingFile = f;
                            in = new ObjectInputStream(new FileInputStream(workingFile));
                            Object message = in.readObject();
                            in.close();
                            if (workingFile.delete()) {
                                workingFile = null;
                                //System.out.println("YESS");
                                //workingFile.createNewFile();
                            }
                            return message;
                        }
                    }
                }
                return null;
            }
        } catch (Exception e) {
            if (e instanceof EOFException) {

            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    // creates new instance of file and writes to
    public void send(Object message) {
        try {
            if (serv) {
                //System.out.println("3");
                receiveFromServer = new File(currentDir + "/messages/ServerMessages/" + id + ".ser");
                receiveFromServer.createNewFile();
                out = new ObjectOutputStream(new FileOutputStream(receiveFromServer, false));
                out.writeObject(message);
                out.flush();
                out.close();
            }
            if (!serv) {
                //System.out.println("1");
                workingFile = new File(currentDir + "/messages/ClientMessages/" + id + ".ser");
                workingFile.createNewFile();
                out = new ObjectOutputStream(new FileOutputStream(workingFile, false));
                out.writeObject(message);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method to delete directory
    public static boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();

        return file.delete();
    }

    // proof file system works for concurrent calls to/from
    public static void main(String[] args) throws InterruptedException {
        Comms serv = new Comms(true);
        Comms client1 = new Comms(false);
        Comms client2 = new Comms(false);

        ArrayList<String> toSend = new ArrayList<>();
        ArrayList<String> toSend2 = new ArrayList<>();

        toSend.add("hello1");
        toSend.add("world2");

        toSend2.add("hello3");
        toSend2.add("world4");

        client1.send("Hello");
        //System.out.println("1"+permissionC + ", "+ permissionS);
        //Thread.sleep(1000);
        client2.send("Hello2");
        Thread.sleep(1000);
        //System.out.println("2"+permissionC + ", "+ permissionS);
        while (true) {
            String message = null;
            while (message == null) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                message = (String) serv.recieve();
            }
            if (message.equals("Hello2")) {
                serv.send(toSend2);
                Thread.sleep(1000);
                List<String> sent2 = (ArrayList) client2.recieve();
                for (String s : sent2) {
                    System.out.println(s);
                }
            }

            if (message.equals("Hello")) {
                System.out.println("IN");
                serv.send(toSend);
                Thread.sleep(1000);
                //System.out.println("3"+permissionC + ", "+ permissionS);
                List<String> sent = (ArrayList) client1.recieve();
                for (String s : sent) {
                    System.out.println(s);
                }
            }
        }
    }
}
