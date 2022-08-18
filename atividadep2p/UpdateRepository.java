/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Stack;

/**
 *
 * @author X
 */
public class UpdateRepository {

    private static final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.65.16.2", 3128));
    private static final String DownloadsPath = "Files" + File.separator;
    private static final String fileType = "gif";

    public static void update(String qry) throws MalformedURLException, IOException {
        CustomSearch c = new CustomSearch();
        ReadableByteChannel rbc;
        File file;
        file = new File(DownloadsPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        int i;

        Stack<String> s = c.search(qry, fileType, 1);
        for (String item : s) {

            try {
                file = new File(DownloadsPath);
                i = file.list().length;
                URL website = new URL(item);
                file = new File(DownloadsPath + item.substring(item.lastIndexOf('/')));
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    file.delete();
                    file.createNewFile();
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    rbc = Channels.newChannel(website.openConnection(/*proxy*/).getInputStream());
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    rbc.close();
                }
                FileManipulator.updateFileXml(file.getName(), file.length());
            } catch (IOException ex) {
                System.out.println("não foi possível salvar " + file.getPath());
            }

        }
    }
}
