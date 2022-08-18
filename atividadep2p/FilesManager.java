/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import java.io.StringWriter;
import java.util.LinkedList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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

/**
 * Gerencia a busca de arquivos.
 * 
 * @author X
 */
public class FilesManager {

    /**
     * Busca um arquivo de acordo com as palavras-chave embutidas em um elemento
     * da árvore DOM, passado como parâmetro.
     * 
     * @param element Elemento que contém as informações sobre a busca
     * @return String contendo os resultados
     * @throws TransformerException
     */
    public static String searchFile(Element element) throws TransformerException {

        LinkedList<NodeList> PartialResult = new LinkedList<>();
        Document localFiles = FileManipulator.LoadFile();
        String nodeToSearch;
        NodeList filesToSearch = element.getElementsByTagName("keywords"), temp;
        if (localFiles != null) {
            for (int i = 0; i < filesToSearch.getLength(); i++) {
                nodeToSearch = filesToSearch.item(i).getTextContent();
                temp = findFiles(localFiles, nodeToSearch);
                if (temp != null) {
                    PartialResult.add(temp);
                } else {
                    System.out.println("não achou " + nodeToSearch);
                }
            }
            Node fileResponse = FileManipulator.NewFileResponse();
            Document doc = fileResponse.getOwnerDocument();
            Node importedNode;
            for (NodeList node : PartialResult) {
                for (int j = 0; j < node.getLength(); j++) {
                    importedNode = doc.importNode(node.item(j), true);
                    fileResponse.appendChild(importedNode);
                }
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
        return null;
        
    }
    
    /**
     * Localiza os arquivos cujos nomes correspondem às palavras-chave listadas
     * na string.
     * 
     * @param doc DOM que contém a lista dos arquivos locais disponíveis para a busca.
     * @param value String contendo as palavras-chave.
     * @return NodeList - Lista com os resultados da busca.
     */
    private static NodeList findFiles(Document doc, String value) {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//*[text()[contains(.,'"+value+"')]]");
            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            if (nl.getLength() > 0) {
                return nl;
            }
        } catch (XPathExpressionException ex) {
        }
        return null;
    }

    
}
