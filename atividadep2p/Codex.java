/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * Realiza operações de codificação e decodificação, além de interpretação da
 * requisição realizada pelo cliente.
 * 
 * @author X
 */
public class Codex {

    private final Base64.Decoder decoder;
    private final Base64.Encoder encoder;

    /**
     * Inicializa o codificador e o decodificador
     */
    public Codex() {
        this.encoder = Base64.getEncoder();
        this.decoder = Base64.getDecoder();
    }

    /**
     * Codifica um arquivo na base 64
     * @param path Caminho do arquivo
     * @return String - Arquivo codificado na base 64
     */
    public String Encode(String path) {
        Path p = Paths.get(path);
        return encoder.encodeToString(FileManipulator.ToByte(path));
    }

    /**
     * Decodifica um arquivo na base 64
     * @param File Arquivo codificado
     * @return byte[] - Array de byte decodificado
     */
    public byte[] Decode(String File) {
        return decoder.decode(File);
    }

    /**
     * Identifica a requisição realizada pelo cliente.
     * Erro = -1 |
     * Requisição inválida = 0 |
     * getFile = 1 |
     * getFilesResponse = 2 |
     * getHosts = 3 |
     * getHostsResponse = 4 |
     * searchFile = 5 |
     * searchFileResponse = 6
     *
     * @param xml Arquivo XML codificado em String
     * @return int - Retorna um valor inteiro correspondente ao tipo de requisição.
     */
    public int translate(String xml) {
        String tipoXml;
        try {
            tipoXml = FileManipulator.getFirstChild(FileManipulator.stringToDoc(xml).getDocumentElement()).getNodeName();
            System.out.println("["+tipoXml+"]");
            switch (tipoXml) {
                case "getFile":
                    return 1;
                case "getFilesResponse":
                    return 2;
                case "getHosts":
                    return 3;
                case "getHostsResponse":
                    return 4;
                case "searchFile":
                    return 5;
                case "searchFileResponse":
                    return 6;
                default:
                    return 0;

            }
        } catch (SAXException | IOException | ParserConfigurationException | DOMException ex) {
            ex.printStackTrace();
            System.out.println("Codex error");
            return -1;
            
        }
    }

}
