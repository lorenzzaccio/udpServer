/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package udp;

/**
 *
 * @author lorenzzaccio
 */
public interface protocole {

    final static int START_BYTE = 23;
    final static int END_BYTE = 32;
    final static int GET_SPEED = 0;
    final static int GET_MAG1 = 1;
    final static int GET_FAST_MODE = 2;
    final static int GET_DECHETS = 3;
    final static int GET_VIDE = 4;
    final static int GET_POUBELLE = 5;
    final static int STOP_SUBMIT_CMD = 1;
    final static int START_SUBMIT_CMD = 2;
    final static int STOP_PROG_CMD = 3;
    final static int START_PROG_CMD = 4;
    final static int UPLOAD_PROG_FROM_PC_CMD = 5;
    final static int DOWNLOAD_PROG_TO_PC_CMD = 6;
    final static int SET_DECHETS_CMD = 10;
    final static int SET_POUBELLE_CMD = 11;
    final static int SET_MAGP1_CMD = 12;
    final static int SET_SPEED_CMD = 13;
    final static int SET_FAST_MODE_CMD = 14;
    final static int SET_MODE_AUTO_CMD = 15;
    final static int SET_FIN_BATON_CMD = 16;
    final static int SET_POS_REPOS_CMD = 17;
    final static int SET_SEND_VAR_CMD = 18;
    final static int SET_REG_CMD = 19;
    final static int GET_ENV_CMD = 24;
    final static int GET_SPEED_CMD = 25;
    final static int GET_DECHETS_CMD = 26;
    final static int GET_BONNES_CMD = 27;
    final static int GET_TOTAL_A_PRODUIRE_CMD = 28;
    final static int GET_POUBELLE_CMD = 29;
    final static int GET_VIDE_CMD = 30;
    final static int GET_MAG_P1_CMD = 31;
    final static int GET_ROBOT_SPEED_CMD = 32;
    final static int GET_REG_CMD = 33;
}
