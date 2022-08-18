/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.Arrays;
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
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Realiza as operações envolvendo manipulações de arquivo. Tais operações 
 * englobam buscas, uploads e downloads.
 * 
 * @author X
 */
public class FileManipulator {

    private static final String DownloadsPath = "Files" + File.separator;
    private static final String getFilePath = "xml" + File.separator + "getFile.xml";
    private static final String filePath = "xml" + File.separator + "file.xml";
    private static final String searchFileResponsePath = "xml" + File.separator + "SearchFileResponse.xml";
    private static final String getFilesResponsePath = "xml" + File.separator + "getFilesResponse.xml";
    private static boolean writing = false;

    /**
     * Converte um arquivo em um arranjo de bytes.
     * 
     * @param path Caminho do arquivo a ser convertido.
     * @return byte[] - Conjunto de bytes que correspondem ao arquivo convertido.
     */
    public static byte[] ToByte(String path) {
        FileInputStream in;
        File file = new File(path);
        byte[] bFile = new byte[(int) file.length()];
        try {
            //converte o arquivo em um array de bytes
            in = new FileInputStream(file);
            in.read(bFile);
            in.close();
            return bFile;
        } catch (Exception e) {
            System.out.println("Não foi possível converter arquivo");
            return null;
        }

    }

    /**
     * Converte um arquivo XML em string
     *
     * @param path Local do arquivo XML
     * @return String - XML codificado em string
     */
    public static String ToString(String path) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        org.w3c.dom.Document doc;
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            //Verifica se o XML está de acordo cm o DTD
            db.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    System.out.println("ERRO: XML não corresponde ao DTD");
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    System.out.println("ERRO: XML não corresponde ao DTD");
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    System.out.println("ERRO: XML não corresponde ao DTD");
                }
            });
            doc = db.parse(path); //transforma o xml em document
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource((Node) doc), new StreamResult(writer));
            String output = writer.getBuffer().toString(); //document to string
            return output;
        } catch (TransformerException e) {
        } catch (SAXException | ParserConfigurationException ex) {
            System.out.println("Não foi possível configurar o parser");
        } catch (IOException ex) {
            System.out.println("p2pse.dtd não encontrado");
        }

        return null;
    }

    /**
     * Obtém o nome do primeiro filho de um nó da árvore XML
     * 
     * @param parent Nó da árvore a partir de onde será feita a busca.
     * @return String - Nome do nó filho.
     */
    public static String getFirstChildTag(Node parent) {

        // search for node
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return child.getNodeName();
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    }

    /**
     * Obtém a referência para o primeiro filho de um nó
     * 
     * @param parent Referência inicial da busca.
     * @return Node - Referência para o primeiro filho, se encontrado. Caso
     * contrário, retorna "null".
     */
    public static Node getFirstChild(Node parent) {

        // search for node
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return child;
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    }

    /**
     * Obtém o próximo nó em um mesmo nível da árvore XML
     * 
     * @param sibling Nó folha referência para a busca.
     * @return Node - Referência para o mó "irmão" mais próximo.
     */
    public static Node getNextSibling(Node sibling) {

        // search for node
        Node child = sibling.getNextSibling();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return child;
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    }

    /**
     * Remove todos os filhos de um nó especificado.
     * 
     * @param node Referência cujos nós filhos serão removidos.
     */
    public static void removeChilds(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }

    }

    /**
     * Transforma o DOM em um arquivo XML a ser armazenado no caminho especificado.
     * 
     * @param doc DOM a ser convertido para arquivo.
     * @param docPath Destino do arquivo gerado.
     * @throws TransformerException
     */
    public static synchronized void printDoc(Document doc, String docPath) throws TransformerException {
        StreamResult result;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        result = new StreamResult(new File(docPath));
        transformer.transform(source, result);
        System.out.println("File saved!");
    }

    /**
     * Recebe uma string representando um arquivo XML e a converte em um DOM.
     * 
     * @param xml String que representa o arquivo XML.
     * @return Document - Estrutura DOM gerada a partir da string.
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document stringToDoc(String xml) throws SAXException, IOException, ParserConfigurationException {
        System.out.println(xml);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(new InputSource(new StringReader(xml)));
        return doc;
    }

    /**
     * Indica se existe uma gravação de arquivo em andamento.
     * 
     * @return boolean - Status da gravação
     */
    public static boolean isWriting() {
        return writing;
    }

    /**
     * Configura o estado de gravação de arquivo
     * 
     * @param writing Indicador do estado de gravação.    
     */
    public static void setWriting(boolean writing) {
        FileManipulator.writing = writing;
    }

    /**
     * Insere todos os termos de busca em um arquivo XML e o converte para uma string.
     * 
     * @param files Lista contendo os termos de busca usados para localizar os arquivos desejados.
     * @return String que representa o arquivo XML gerado para a busca.
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static String remoteSearchXml(LinkedList<String> files) throws TransformerConfigurationException, TransformerException {
        
        Node n = NewSearchXml(), importedNode;
        Document doc = n.getOwnerDocument();
        Element docEL = doc.getDocumentElement();
        
        for (String s : files) {
            Element fileName = doc.createElement("keywords");
            fileName.appendChild(doc.createTextNode(s));
            docEL.appendChild(fileName);
        }
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource((Node) doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();

        return output;
    }

    /**
     * Gera um DOM para o arquivo "getFile.xml". O DOM será o recepiente 
     * para uma nova busca remota de arquivos. 
     * 
     * @return Node - Referência para o nó recém-criado do tipo ELEMENT, usado 
     * para enumerar os termos de busca.
     */
    private static Node NewSearchXml() {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        Element temp;
        Node tempChild;
        DocumentBuilder db;
        
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(getFilePath);
            temp = doc.getDocumentElement();
            tempChild = FileManipulator.getFirstChild(temp);
            FileManipulator.removeChilds(tempChild);
            return tempChild;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("não foi possível carregar SearchFileResponse.xml");
        }
        return null;
    }

    /**
     * Gera um nó de um DOM sem conteúdo a partir do arquivo "SearchFileResponse.xml". O 
     * documento gerado contém as informações sobre o resultado da busca de arquivos.
     * 
     * @return Node - Referência para o nó correspondente ao resultado da busca.
     */
    public static Node NewFileResponse() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        Element temp;
        Node tempChild;
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(searchFileResponsePath);
            temp = doc.getDocumentElement();
            tempChild = FileManipulator.getFirstChild(temp);
            FileManipulator.removeChilds(tempChild);
            return tempChild;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("não foi possível carregar SearchFileResponse.xml");
        }
        return null;
    }

    /**
     * Gera um DOM a partir do arquivo "file.xml". O documento gerado conterá as 
     * informações sobre os arquivos que atendem ao critério de busca.
     * 
     * @return Document - Referência para o DOM gerado.
     */
    public static Document LoadFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(filePath);
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("não foi possível carregar file.xml");
        }
        return null;
    }

    /**
     * Gera um nó de um DOM sem conteúdo, a partir do arquivo "getFilesResponse.xml". O 
     * documento gerado será usado como recipiente para os arquivos que serão 
     * enviados.
     * 
     * @return Node - Referência para o nó onde serão inseridos os arquivos.
     */
    public static Node NewGetFilesResponse() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        Element temp;
        Node tempChild1, tempChild2;
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(getFilesResponsePath);
            temp = doc.getDocumentElement();
            tempChild1 = FileManipulator.getFirstChild(temp);
            tempChild2 = FileManipulator.getFirstChild(tempChild1);
            FileManipulator.removeChilds(tempChild2);
            return tempChild2;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("não foi possível carregar getFilesResponse.xml");
        }
        return null;
    }

    /**
     * Processa a string que representa o XML contendo os arquivos baixados. O
     * método extrai os arquivos de dados, converte-os para uma sequência de 
     * bytes e os salva em disco. Após isso, a lista de arquivos disponíveis
     * para consulta remota também é atualizada com os novos dados.
     * 
     * @param xml String que representa o arquivo XML e contém os dados a serem
     * extraídos.
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public static void serializeDownloads(String xml) throws SAXException, 
            IOException, ParserConfigurationException, XPathExpressionException {
        Codex cod = new Codex();
        Document doc = stringToDoc(xml);
        String fileName, data;
        Node temp;
        byte[] bytes;
        FileOutputStream out;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//fileData");
        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nl.getLength(); i++) {
            temp = getFirstChild(nl.item(i));
            fileName = temp.getTextContent();
            data = getNextSibling(temp).getTextContent();
            out = new FileOutputStream(DownloadsPath + fileName);
            bytes = cod.Decode(data);
            out.write(bytes);
            out.close();
            System.out.println(fileName + " downloaded");
            updateFileXml(fileName, bytes.length);

        }
    }

    /**
     * Atualiza o arquivo "file.xml" com informações sobre os arquivos 
     * disponíveis para busca.
     * 
     * @param fileName Nome do arquivo.
     * @param fileSize Tamanho.
     */
    public static void updateFileXml(String fileName, long fileSize) {
        try {
            Document doc = LoadFile();
            Element docEL = doc.getDocumentElement();
            Element host = doc.createElement("file");
            docEL.appendChild(host);

            Element fName = doc.createElement("fileName");
            fName.appendChild(doc.createTextNode(fileName));
            Element port = doc.createElement("fileSize");
            port.appendChild(doc.createTextNode(String.valueOf(fileSize)));

            host.appendChild(fName);
            host.appendChild(port);

            FileManipulator.printDoc(doc, filePath);
        } catch (TransformerException ex) {
            Logger.getLogger(HostsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Salva um arquivo baixado no diretório "Files" e atualiza a lista de 
     * arquivos disponíveis para busca.
     * 
     * @param file Arquivo baixado.
     * @throws IOException
     */
    public static void saveNewFile(File file) throws IOException {
        File temp = new File(DownloadsPath);
        if (!temp.exists()) {
            temp.mkdir();
        }
        temp = new File(DownloadsPath+file.getName());
        if (!temp.exists()) {
            temp.createNewFile();
        }
        FileChannel dest;
        FileChannel sour;
        sour = new FileInputStream(file).getChannel();
        dest = new FileOutputStream(temp).getChannel();
        if (dest != null && sour != null) {
            dest.transferFrom(sour, 0, sour.size());
        }
        if (sour != null) {
            sour.close();
        }
        if (dest != null) {
            dest.close();
        }
        System.out.println(file.length());
        updateFileXml(file.getName(), file.length());
    }

    /**
     * Obtém uma representação em string do DOM que contém os arquivos que
     * satisfazem os critérios de busca.
     * 
     * @param xml String que representa o arquivo "XML".
     * @return String que representa o DOM contendo o resultado da busca.
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public static String getFile(String xml) throws TransformerConfigurationException, 
            TransformerException, SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        
        String fileName;
        Document d = stringToDoc(xml);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//fileName");
        NodeList nl = (NodeList) expr.evaluate(d, XPathConstants.NODESET);
        Node fResp = FileManipulator.NewGetFilesResponse();
        Document doc = fResp.getOwnerDocument();

        for (int i = 0; i < nl.getLength(); i++) {
            fileName = nl.item(i).getTextContent();
            Node file = doc.createElement("fileName");
            file.appendChild(doc.createTextNode(fileName));
            fResp.appendChild(file);
            Node data = doc.createElement("data");
            data.appendChild(doc.createTextNode(Arrays.toString(FileManipulator.ToByte(DownloadsPath + fileName))));
            fResp.appendChild(data);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource((Node) doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();

        return output;
    }
}
