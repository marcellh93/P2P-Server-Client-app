/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorcentral;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
/**
 *
 * @author Marcell Hernández
 */
public class ServidorCentral {
    
    private ServerSocket Servidor;
    private int puerto; //Puerto 47523
    private String OS = System.getProperty("os.name");
    
    public ServidorCentral(int puerto)
    {
        this.puerto = puerto;
    }
    
    //Inicializa el SC
    public void correrServidor()
    {
        
        try
        {
            this.Servidor = new ServerSocket(this.puerto);
            servidoresDescarga();
            catalogoLibros();
            new funcionesConsolaSC().start();
            for(;;)
            {
                //Espera que un cliente se oonecte
                Socket socket = Servidor.accept();
                
                //Abre el hilo con el manejador del servidor
                new ManejadorServidor(socket, OS).start();
            }
        }
        catch(IOException ioe)
        {
            System.err.println(ioe);
        }
    }

    //Coloca el xml de los SD solo con el root element
    private boolean servidoresDescarga()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("./src/servidorcentral/servidoresDescargaXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        rootDoc.removeContent();
        
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream file;
        try
        {
            file = new FileOutputStream("./src/servidorcentral/servidoresDescargaXML.xml");
            xmlOutput.output(rootDoc, file);
            file.flush();
            file.close();
            return true;
        }
        catch (FileNotFoundException ex)
        {
            System.err.println(ex);
            return false;
        }
        catch (IOException ex)
        {
            System.err.println(ex);
            return false;
        }
    }
    
    //Coloca el xml del catalogo de libros solo con el root element
    private boolean catalogoLibros()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("./src/servidorcentral/catalogoLibrosXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        rootDoc.removeContent();
        
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream file;
        try
        {
            file = new FileOutputStream("./src/servidorcentral/catalogoLibrosXML.xml");
            xmlOutput.output(rootDoc, file);
            file.flush();
            file.close();
            return true;
        }
        catch (FileNotFoundException ex)
        {
            System.err.println(ex);
            return false;
        }
        catch (IOException ex)
        {
            System.err.println(ex);
            return false;
        }
    }
    
    //Getters y setters utilizados
    public ServerSocket getServidor() {
        return Servidor;
    }

    public void setServidor(ServerSocket Servidor) {
        this.Servidor = Servidor;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }
}
