/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import atividadep2p.FileManipulator;
import atividadep2p.FilesManager;
import atividadep2p.HostsManager;
import atividadep2p.Server;
import atividadep2p.UpdateRepository;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import view.MainFrame;

/**
 * Implementa as funções que intermediam as operações entre a visualização e o modelo
 * @author X
 */
public class Controller {

    private MainFrame mainF;
    private Server s;

    /**
     * Armazena a referência para o frame que será controlado.
     * @param mf Referência para o frame
     */
    public void addMainFrame(MainFrame mf) {
        this.mainF = mf;
    }

    /**
     * Registra um host no arquivo "host.xml"
     * @param hostIP Endereço IP do host adicionado
     */
    public synchronized void addHost(String hostIP) {
        while (FileManipulator.isWriting()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileManipulator.setWriting(true);
        HostsManager.addHost(hostIP, s.getPorta());
        FileManipulator.setWriting(false);
        notifyAll();

    }

    /**
     * Adiciona um arquivo à pasta de arquivos baixados.
     * @param file Arquivo baixado
     */
    public void saveNewFile(File file){
        try {
            FileManipulator.saveNewFile(file);
        } catch (IOException ex) {
            System.out.println("Não foi possível transferir arquivo");
        }
        mainF.post("arquivo adicionado ao repositório com sucesso!");
    }
    
    public void updateRepository(String qry) throws IOException{
        UpdateRepository.update(qry);
    }

    /**
     * Registra a classe que irá tratar as requisições recebidas.
     * @param s Classe com as funções de servidor.
     */
    public void addServer(Server s) {
        this.s = s;
    }

    /**
     * Inicia a conexão com os hosts conhecidos.
     */
    public void connectAll() {
        s.connectAll();
    }

    /**
     * Repassa uma mensagem para ser exibida no frame.
     * @param msg Mensagem
     */
    public void postMsg(String msg) {
        mainF.post(msg);
    }

    /**
     * Repassa a String que contém as atualizações para o gerenciador de hosts 
     * conhecidos.
     * 
     * @param xml String que representa a requisição XML
     */
    public void addUpdates(String xml) {
        HostsManager.addUpdates(xml);
    }

    /**
     * Obtém uma lista contendo as atualizações da lista de hosts conhecidos.
     * 
     * @return LinkedList Lista de atualizações.
     */
    public LinkedList getUpdates() {
        return HostsManager.getUpdates();
    }

    /**
     * Limpa o arquivo que contém a lista de hosts conhecidos.
     */
    public void clearUpdates() {
        HostsManager.clearUpdates();
    }

    /**
     * Registra uma lista de hosts conhecido por um peer.
     * 
     * @param hosts Lista de hosts fornecida.
     */
    public synchronized void addHostGroup(LinkedList<String> hosts) {
        while (FileManipulator.isWriting()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileManipulator.setWriting(true);
        hosts.stream().forEach((host) -> {
            try {
                HostsManager.addHostGroup(host);
            } catch (SAXException | ParserConfigurationException | IOException | XPathExpressionException | TransformerException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        FileManipulator.setWriting(false);
        notifyAll();

    }

    /**
     * Envia uma requisição solicitando a lista de hosts conhecidos pelo host 
     * no IP especificado.
     * 
     * @param ip Endereço IP do host que será consultado.
     */
    public void getRemoteHosts(String ip) {
        System.out.println("controller.getRemoteHosts");
        s.getRemoteHosts(HostsManager.getHostXml(), ip);
        
        
    }
    
    /**
     * Envia uma lista com nomes de arquivo ao host no IP especificado, 
     * solicitando o download dos mesmos.
     * 
     * @param list
     * @param ip
     * @throws TransformerException
     */
    public void Download(LinkedList list,String ip) throws TransformerException{
        s.Download(FileManipulator.remoteSearchXml(list), ip);
        postMsg("requisição enviada!");
    }

    /**
     * Obtém a lista dos hosts conhecidos localmente.
     * 
     * @return String - DOM codificado, criado a partir do arquivo XML que 
     * contém a lista de hosts.
     */
    public String getHosts() {
        return HostsManager.getHosts();
    }

    /**
     * Envia uma requisição de busca de arquivos para um host no IP 
     * especificado.
     * 
     * @param list Lista de palavras-chave da busca.
     * @param ip Endereço IP do host remoto.
     * @throws TransformerException
     */
    public void remoteSearch(LinkedList list,String ip) throws TransformerException {
        s.remoteSearch(FileManipulator.remoteSearchXml(list), ip);
    }

    /**
     * Realiza a busca local dos arquivos, com base em uma lista de palavras-chave
     * recebida.
     * 
     * @param list Lista com as palavras-chave da busca.
     * @return String que representa o DOM contendo o resultado da busca.
     */
    public String SearchFile(LinkedList list) {
        try {
            return FilesManager.searchFile(FileManipulator.stringToDoc(FileManipulator.remoteSearchXml(list)).getDocumentElement());

        } catch (TransformerException | SAXException | IOException | ParserConfigurationException ex) {
            System.out.println("não conseguiu achar");
        }
        return null;

    }
}
