package gameClient;
import GUI.MyFrame;
import Server.Game_Server_Ex2;
import api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.lang.Thread.sleep;

public class Ex2 implements Runnable{

    public static game_service _game;
    static Arena _ar;
    static MyFrame _mainFrame;
    private static long id = -1;
    private static int level = 0;
    private static boolean isClicked = true;

    public static void main(String[] args) {
        try {
            id = Integer.parseInt(args[0]);
            level = Integer.parseInt(args[1]);
        }
        catch(Exception e) {
            id = -1;
            level = 0;
            isClicked = false;
        }
        Thread client = new Thread(new Ex2());
        client.start();
    }

    @Override
    public void run() {
        _mainFrame = new MyFrame();
        introPanel();
        _mainFrame.add(intro, BorderLayout.CENTER);
        _mainFrame.pack();

        while (!isClicked) {
            Thread.onSpinWait();
        }

        _game = Game_Server_Ex2.getServer(level);
        _ar = new Arena(_game);
        _mainFrame.initFrame(_ar);
        intro.setVisible(false);
        _mainFrame.pack();

        _game.login(id);
        _game.startGame();
        int dt;
        while(_game.isRunning()) {
                _ar.moveAgents();
                _mainFrame.repaint();
                dt = isCloseToPokemon()? 50 : 110;
            try {
                sleep(dt);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isCloseToPokemon() {
        for(Agent ag : _ar.JsonToAgents()){
            for(Pokemon p : _ar.getPokemons())
                if(ag.get_curr_edge() == p.get_edge())
                    return true;
        }
        return false;
    }

    private void introPanel() {
        select_level_LBL.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        select_level_LBL.setText("Select level:");

        id_field.addTextListener(new TextListener() {
            @Override
            public void textValueChanged(TextEvent e) {
                try {
                    id  = Long.parseLong(id_field.getText());
                    playBTN.setEnabled(true);
                }catch (Exception ex){
                    playBTN.setEnabled(false);
                }
            }
        });

        level_selector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Level 0", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6", "Level 7", "Level 8", "Level 9", "Level 10", "Level 11", "Level 12", "Level 13", "Level 14", "Level 15", "Level 16", "Level 17", "Level 18", "Level 19", "Level 20", "Level 21", "Level 22", "Level 23" }));
        level_selector.setToolTipText("");
        level_selector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                level = level_selector.getSelectedIndex();
                System.out.println(level);
            }
        });


        playBTN.setText("Play!");
        playBTN.setEnabled(false);
        playBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                isClicked = true;
                System.out.println("starting to play!");
            }
        });

        enter_ID_LBL.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        enter_ID_LBL.setText("Enter your ID: ");

        javax.swing.GroupLayout introLayout = new javax.swing.GroupLayout(intro);
        intro.setLayout(introLayout);
        introLayout.setHorizontalGroup(
                introLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(introLayout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addGroup(introLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(introLayout.createSequentialGroup()
                                                .addComponent(enter_ID_LBL, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(10, 10, 10)
                                                .addComponent(id_field, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(introLayout.createSequentialGroup()
                                                .addComponent(select_level_LBL, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(10, 10, 10)
                                                .addComponent(level_selector, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(playBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        introLayout.setVerticalGroup(
                introLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(introLayout.createSequentialGroup()
                                .addGap(147, 147, 147)
                                .addGroup(introLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(enter_ID_LBL, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(id_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(introLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(select_level_LBL, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(level_selector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(37, 37, 37)
                                .addComponent(playBTN))
        );
    }

}
