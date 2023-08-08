/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidordescarga;

/**
 *
 * @author Marcell Hernández
 */
public class Main2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        servidorDescarga sd2 = new servidorDescarga(2,"localhost",47523,47525);
        sd2.correrServidor(sd2);
    }
    
}
