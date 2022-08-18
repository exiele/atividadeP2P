/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atividadep2p;

import control.Controller;
import view.MainFrame;

/**
 * Classe principal da aplicação Peer-To-Peer.
 * @author X
 */
public class AtividadeP2P {

    private static final int port = 1008;

    /**
     * Inicializa as principais classes de modelo, controle e visualização
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        Controller c = new Controller();
        MainFrame m = new MainFrame(c);
        c.addMainFrame(m);
        m.setVisible(true);
        Server s = new Server(port, c);
        Thread t = new Thread(s);
        t.start();
        c.addServer(s);
    }
}
