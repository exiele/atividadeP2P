/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import control.Controller;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implementa as funções do módulo servidor da aplicação.
 * @author X
 */
public class Server implements Runnable {

    private final int porta;
    private final List<Client> clientes;
    private ServerSocket server;
    private final Controller c;

    /**
     * Inicializa o servidor, armazenando a porta e uma referência para o controlador
     * 
     * @param porta Número da porta de comunicação
     * @param c Referência para o controlador da aplicação
     */
    public Server(int porta, Controller c) {
        clientes = new ArrayList<>();
        this.c = c;
        this.porta = porta;
    }

    /**
     * Conecta-se a todos os hosts conhecidos.
     */
    public void connectAll() {
        Document doc;
        String hosts;
        Client temp;
        Thread t;
        try {
            hosts = HostsManager.getHosts();
            doc = FileManipulator.stringToDoc(hosts);
            System.out.println(doc.toString());
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//host");
            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                temp = new Client(c,nl.item(i));
                t = new Thread(temp);
                t.start();
                clientes.add(temp);
            }
            
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Realiza uma requisição a um host no IP especificado, solicitando a lista 
     * dos hosts conhecidos por cada um.
     * 
     * @param xml String que contém a requisição codificada.
     * @param ip Endereço IP do host de destino.
     */
    public void getRemoteHosts(String xml, String ip){
        System.out.println("Server.getRemoteHosts");
        for (Client cliente : clientes) {
            if(cliente.getIP().equals(ip)){
                cliente.sendMessage(xml);
            }
        }
        
    }

    /**
     * Realiza uma requisição de busca de arquivos no IP especificado.
     * 
     * @param xml String que contém a requisição codificada.
     * @param ip Endereço IP do host remoto.
     */
    public void remoteSearch(String xml, String ip){
        clientes.stream().filter(ip::equals).forEach((Client cliente) -> {
            cliente.sendMessage(xml);
        });
    }

    /**
     * Realiza uma requisição de download de arquivo para um host no IP
     * especificado.
     * 
     * @param xml String que contém a requisição
     * @param ip Endereço IP do host remoto
     */
    public void Download(String xml, String ip){
        clientes.stream().filter(ip::equals).forEach((Client cliente) -> {
            cliente.sendMessage(xml);
        });
    }

    /**
     * Obtém o número da porta onde são feitas as conexões.
     * @return int - Número da porta
     */
    public int getPorta() {
        return porta;
    }

    /**
     * Inicializa o servidor e aguarda conexões com clientes
     */
    @Override
    public void run() {
        Client temp;
        Thread t;
        try {
            server = new ServerSocket(porta);
        } catch (IOException ex) {
            System.out.println("não foi possível abrir o servidor");
        }
        while (true) {
            try {
                Socket cliente = server.accept();
                temp = new Client(c, cliente);
                t = new Thread(temp);
                t.start();
                clientes.add(temp);

            } catch (IOException ex) {
                System.out.println("não foi possível estabelecer conexão");
            }
        }
    }

}
