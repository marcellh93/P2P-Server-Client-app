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
public class Main3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        servidorDescarga sd3 = new servidorDescarga(3,"localhost",47523,47526);
        sd3.correrServidor(sd3);
    }
    
}
