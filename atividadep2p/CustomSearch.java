package atividadep2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Stack;

public class CustomSearch {

    public Stack<String> search(String qry,String fileType, int multiplier) throws MalformedURLException, IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.65.16.2", 3128));
        String key = "AIzaSyAwWLxm76uRejlH2sXtYbZqb3WT29Z7IRg";
        String cx = "018315036819994739617:abllqktciec";
        String searchType = "image";
        int startPage = 1;
        URL url;
        Stack<String> links = new Stack();
        
        for (int i = 0; i < multiplier; i++) {
            url = new URL("https://www.googleapis.com/customsearch/v1?key=" + key
                    + "&cx=" + cx + "&q=" + qry + "&fileType=" + fileType
                    + "&searchType=" + searchType + "&start=" + startPage + "&alt=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(/*proxy*/);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;

            System.out.println("\nArquivos encontrados na página"+startPage+":\n");

            while ((output = br.readLine()) != null) {

                if (output.contains("\"link\": \"")) {
                    String link = output.substring(output.indexOf("\"link\": \"") + ("\"link\": \"").length(), output.indexOf("\","));
                    System.out.println(link);
                    links.push(link);
                }
            }
            conn.disconnect();
            startPage++;
        }

        return links;
    }
}
