/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import control.Controller;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import org.w3c.dom.Node;

/**
 * Implementa as funções do módulo cliente da aplicação
 *
 * @author X
 */
public class Client implements Runnable {

    private final Controller c;
    private Socket client;
    private String ip, xml="";
    private int port;
    private PrintStream out;
    private Scanner in;
    private Node n;
    private boolean validation = false;

    /**
     * Constrói o módulo cliente. São criados os fluxos de entrada e saída, além
     * de registrados os números de endereço IP e da porta de comunicação.
     *
     * @param c Referência para o controlador da aplicação
     * @param s Referência para o socket cliente.
     */
    public Client(Controller c, Socket s) {
        this.c = c;
        try {
            this.client = s;
            out = new PrintStream(client.getOutputStream());
            in = new Scanner(client.getInputStream());
            ip = client.getRemoteSocketAddress().toString();
            port = client.getPort();
            validation = true;
            c.postMsg(ip + " se conectou");
        } catch (IOException ex) {
            c.postMsg(ip + " não conseguiu se conectar");
        }
    }

    /**
     * Construtor sobrecarregado. Armazena a referência do controlador.
     *
     * @param c Controlador.
     * @param n
     */
    public Client(Controller c, Node n) {
        this.c = c;
        this.n = n;
    }

    /**
     * Fornece o endereço IP do host conectado
     *
     * @return String - IP
     */
    public String getIP() {
        return ip;
    }

    /**
     * Fornece a referência para o controlador da aplicação.
     *
     * @return Controller Controlador
     */
    public Controller getControl() {
        return c;
    }

    /**
     * Envia uma requisição codificada em uma string.
     *
     * @param msg Requisição
     */
    public void sendMessage(String msg) {
        System.out.println("Client.sendMessage");
        out.flush();
        out.print(msg);
        out.flush();
        c.postMsg("mensagem enviada: " + msg);
    }

    /**
     * Conecta a um host remoto.
     *
     * @param n Node - Nó do DOM que contém um endereço IP e a porta do host
     * remoto.
     */
    /**
     * Inicia o processo cliente. Cria o codex para codificação e decodificação
     * de dados em base 64 e inicializa uma thread para tratar as mensagens de
     * entrada.
     */
    @Override
    public void run() {
        try {
            if (n != null) {
                Node temp = FileManipulator.getFirstChild(n);
                ip = temp.getTextContent();
                port = Integer.parseInt(FileManipulator.getNextSibling(temp).getTextContent());
                client = new Socket(ip, port);
                out = new PrintStream(client.getOutputStream());
                in = new Scanner(client.getInputStream());
                in.toString();
                validation = true;
                c.postMsg("conectado ao host: " + ip);
            }

        } catch (IOException ex) {
            c.postMsg("não foi possível conectar ao host: " + ip);

        }
        if (validation) {
            Codex cod = new Codex();
            int xmlType;
            MessageHandler mh;
            Thread t;
            while (validation) {
                while (in.hasNextLine()) {
                    xml += in.nextLine();
                    if (xml.endsWith("</p2pse>")) {
                        break;
                    }
                }
                xmlType = cod.translate(xml);
                mh = new MessageHandler(this, xmlType, xml);
                t = new Thread(mh);
                t.start();
            }

        }
    }
}
