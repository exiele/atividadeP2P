/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * Trata as requisições recebidas
 *
 * @author X
 */
public class MessageHandler implements Runnable {

    private final Client c;
    private final int xmlType;
    private final String xml;

    /**
     * @param c Referência para o módulo cliente
     * @param xmlType Indica qual o tipo de mensagem XML recebida
     * @param xml String que representa a mensagem XML
     */
    public MessageHandler(Client c, int xmlType, String xml) {
        this.c = c;
        this.xmlType = xmlType;
        this.xml = xml;

    }

    /**
     * Responde às requisições de acordo com o tipo da mensagem XML recebida.
     *
     * 1 = getFile | 2 = getFilesResponse | 3 = getHosts | 4 = getHostsResponse
     * | 5 = searchFile | 6 = searchFileResponse | Outro valor = Mensagem
     * desconhecida |
     */
    @Override
    public void run() {
        switch (xmlType) {
            case 1: {
                try {
                    c.sendMessage(FileManipulator.getFile(xml));

                } catch (TransformerException | SAXException | IOException | ParserConfigurationException | XPathExpressionException ex) {
                    System.out.println("não foi possível encontrar o(s) arquivo(s)");
                }
                break;
            }
            case 2: {
                try {
                    FileManipulator.serializeDownloads(xml);
                    c.getControl().postMsg("Arquivos baixados:\n\n" + xml);
                } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException ex) {
                    System.out.println("não foi possível serializar arquivos");
                }
                break;
            }

            case 3:
                System.out.println("xml 3");
                c.sendMessage(HostsManager.getHosts());
                break;
            case 4:
                c.getControl().addUpdates(xml);
                c.getControl().postMsg("Hosts encontrados:\n\n" + xml);
                break;
            case 5: {
                try {
                    c.sendMessage(FilesManager.searchFile(FileManipulator.stringToDoc(xml).getDocumentElement()));
                } catch (SAXException | IOException | ParserConfigurationException | TransformerException ex) {
                    System.out.println("não foi possível pesquisar por arquivo");
                }
                break;
            }
            case 6:
                c.getControl().postMsg("Arquivos encontrados:\n\n" + xml);
                break;
            default:
                c.getControl().postMsg("mensagem desconhecida recebida:\n\n" + xml);
                break;
        }
    }
}
