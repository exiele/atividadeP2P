/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Gerencia as operações envolvendo informações sobre hosts/peers que estão
 * executando a mesma aplicação em uma rede.
 *
 * @author X
 */
public class HostsManager {

    private static LinkedList updates = new LinkedList();
    private final static String getHostsPath = "xml" + File.separator + "getHosts.xml";
    private final static String hostsPath = "xml" + File.separator + "host.xml";
    private final static String getHostsResponsePath = "xml" + File.separator + "getHostsResponse.xml";

    /**
     * Atualiza a lista de hosts conhecidos.
     *
     * @param up Lista atualizada dos hosts conhecidos.
     */
    public static void addUpdates(String up) {
        updates.add(up);
    }

    /**
     * Obtém a lista com as atualizações.
     *
     * @return LinkedList - Lista de atualizações
     */
    public static LinkedList getUpdates() {
        return updates;
    }

    /**
     * Limpa a lista de atualizações.
     */
    public static void clearUpdates() {
        updates = new LinkedList();
    }

    /**
     * Gera um DOM a partir do arquivo "getHostsResponse.xml".
     *
     * @return Node - Nó de referência para inserção da lista de hosts
     * conhecidos.
     */
    private static Node NewHostsResponse() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        Element temp;
        Node tempChild;
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(getHostsResponsePath);
            temp = doc.getDocumentElement();
            tempChild = FileManipulator.getFirstChild(temp);
            FileManipulator.removeChilds(tempChild);
            return tempChild;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("não foi possível carregar getHostsResponse.xml");
        }
        return null;
    }

    /**
     * Gera uma string que representa o arquivo "host.xml". O arquivo contém a
     * lista de hosts conhecidos.
     *
     * @return String do arquivo "host.xml".
     */
    public static String getHosts() {
        try {
            Document doc = LoadHosts();
            NodeList hostsNodes = null;
            try {
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("//host");
                hostsNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                System.out.println(hostsNodes.getLength());
            } catch (XPathExpressionException ex) {
                System.out.println("não foi possível carregar os hosts");
            }
            Node hostResponse = NewHostsResponse();
            doc = hostResponse.getOwnerDocument();
            Node importedNode;
            if (hostsNodes != null) {
                for (int i = 0; i < hostsNodes.getLength(); i++) {
                    importedNode = doc.importNode(hostsNodes.item(i), true);
                    hostResponse.appendChild(importedNode);
                }
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource((Node) doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            System.out.println(output);
            return output;
        } catch (TransformerConfigurationException ex) {
            System.out.println("não foi possível carregar os hosts");
        } catch (TransformerException ex) {
            System.out.println("não foi possível carregar os hosts");
        }
        return "não foi possivel recuperar hosts";
    }

    /**
     * Gera um DOM a partir do arquivo "host.xml", que contém a lista de hosts
     * locais.
     *
     * @return Document DOM que representa o arquivo XML convertido.
     */
    private static Document LoadHosts() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(hostsPath);
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("não foi possível carregar host.xml");
        }
        return null;
    }

    /**
     * Insere as informações referentes a um novo host no arquivo "host.xml".
     *
     * @param hostIP Endereço IP do host.
     * @param hostPort Porta usada pelo host.
     */
    public static synchronized void addHost(String hostIP, int hostPort) {
        try {
            Document doc = LoadHosts();
            Element docEL = doc.getDocumentElement();
            Element host = doc.createElement("host");
            docEL.appendChild(host);

            Element iP = doc.createElement("ip");
            iP.appendChild(doc.createTextNode(hostIP));
            Element port = doc.createElement("port");
            port.appendChild(doc.createTextNode(String.valueOf(hostPort)));

            host.appendChild(iP);
            host.appendChild(port);

            FileManipulator.printDoc(doc, hostsPath);
        } catch (TransformerException ex) {
            Logger.getLogger(HostsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Adiciona elementos da lista de hosts obtida remotamente à lista local
     *
     * @param xml String representando o arquivo XML que contém a lista de hosts
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws XPathExpressionException
     * @throws TransformerException
     */
    public static synchronized void addHostGroup(String xml) throws SAXException, ParserConfigurationException, IOException, XPathExpressionException, TransformerException {
        org.w3c.dom.Document doc;
        Node ipNode, portaNode;
        String ip, porta;
        doc = FileManipulator.stringToDoc(xml);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//host");
        NodeList hostsNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < hostsNodes.getLength(); i++) {
            ipNode = FileManipulator.getFirstChild(hostsNodes.item(i));
            ip = ipNode.getTextContent();
            System.out.println("ip: " + ip);
            portaNode = FileManipulator.getNextSibling(ipNode);
            porta = portaNode.getTextContent();
            System.out.println("porta: " + porta);
            addHost(ip, Integer.parseInt(porta));
        }
        FileManipulator.setWriting(false);
    }

    /**
     * Obtém a representação em String do arquivo "getHosts.xml"
     *
     * @return String do arquivo XML.
     */
    public static String getHostXml() {
        return FileManipulator.ToString(getHostsPath);
    }
}
