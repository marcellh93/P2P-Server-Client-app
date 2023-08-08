/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidordescarga;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
/**
 *
 * @author Marcell Hernández
 */
public class servidorDescarga extends Thread{
    private ServerSocket ss;
    private Socket s;
    
    private String dirIP;
    private String dirIpPropia;
    
    private int id;
    private int puertocentral;
    private int puertopropio;
    private int i = 0;
    private int descargas = 0;

    public servidorDescarga(int id, String dirIP, int puertocentral, int puertopropio)
    {
        this.id = id;
        this.dirIP = dirIP;
        this.puertocentral = puertocentral;
        this.puertopropio = puertopropio;
    }
    
    public void correrServidor(servidorDescarga sd)
    {
        try {
            /*Se conecta con servidor central*/
            this.s = new Socket(this.dirIP, this.puertocentral);
            this.ss = new ServerSocket(this.puertopropio);
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(),true);
            librosDescargando();
            librosEstadisticas();
            clientesFielesXML();
            out.println("s");
            
            //Obtiene el ip del servidor de descarga y lo manda
            out.println(InetAddress.getLocalHost().getHostAddress());
            //Obtengo el puerto del servidor de descarga y lo manda
            out.println(Integer.toString(ss.getLocalPort()));
            enviarLibrosSC(in, out);
            s.close();
            in.close();
            out.close();
            new funcionesConsola(sd.getIdSd(), sd).start();
            for(;;)
            {
                //Espera peticiones para aceptarlas
                Socket socket = ss.accept();
                if(ss.isClosed())
                {
                    break;
                }
                //Imprime personas conectadas
                //System.out.println("Personas conectadas: "+i);
                //System.out.println("");
                
                //Abre el hilo para manejar las descargas con el cliente
                new ManejadorSD(socket,sd).start();
                
                //Aumenta las personas conectadas en 1
                this.i++;
            }
            
        } catch (ConnectException ex){
            System.err.println("No se pudo conectar con el servidor");
        } catch (SocketException ex) {
            System.err.println("Adiós, servidor cerrado");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    //Actualiza al SC con respecto al catálogo de libros de este SD
    public void enviarLibrosSC(BufferedReader br, PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        Document readDoc = null;
        try {
            //Lee el xml de libro en base al id del servidor de descarga
            readDoc = builder.build(new File("C:\\redes2\\servidor_"+this.id+"\\libros.xml"));
        } catch (JDOMException ex) {
            System.err.println(ex);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        Element root = readDoc.getRootElement();
        
        //Enviar ID del servidor al SC
        pw.println(id);
        
        //Cantidad de libros en el servidor
        int size = root.getChildren("Libro").size();
        pw.println(Integer.toString(size));
        
        for(Element ele : root.getChildren("Libro"))
        {
            //Enviar nombre de libro
            String nombre = ele.getChildText("nombre");
            pw.println(nombre);
            String existe;
            try {
                existe = br.readLine();            
                if(existe.equalsIgnoreCase("false"))
                {
                    //Proceso de envío de autores
                    int i = 0;
                    for(Element ele2 : ele.getChildren("autores"))
                    {
                        for (Element ele3 : ele2.getChildren("autor"))
                        {
                            i++;
                        }
                    }
                    //System.out.println("Cantidad de autores en el libro "+nombre+": "+i);
                    pw.println(Integer.toString(i));
                    for(Element ele2 : ele.getChildren("autores"))
                    {
                        for (Element ele3 : ele2.getChildren("autor"))
                        {
                            String autor = ele3.getChildText("nombre");
                            pw.println(autor);
                        }
                    }

                    //Proceso de envío de géneros
                    i = 0;
                    for(Element ele2 : ele.getChildren("generos"))
                    {
                        for (Element ele3 : ele2.getChildren("genero"))
                        {
                            i++;
                        }
                    }
                    //System.out.println("Cantidad de generos en el libro "+nombre+": "+i);
                    pw.println(Integer.toString(i));
                    for(Element ele2 : ele.getChildren("generos"))
                    {
                        for (Element ele3 : ele2.getChildren("genero"))
                        {
                            String genero = ele3.getChildText("nombre");
                            pw.println(genero);
                        }
                    }
                }
                else if(existe.equalsIgnoreCase("true"))
                {
                    pw.println(nombre);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    //Coloca el xml de los libros que se están descargando con 0 clientes conectados
    private boolean librosDescargando()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("C:\\redes2\\servidor_" + this.id + "\\librosDescargandoXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Libro"))
        {
            ele.getChild("num_clientes").setText("0");
        }
        
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream file;
        try
        {
            file = new FileOutputStream("C:\\redes2\\servidor_" + this.id + "\\librosDescargandoXML.xml");
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
    
    //Coloca el xml de las estadisticas con las descargas en 0
    private boolean librosEstadisticas()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("C:\\redes2\\servidor_" + this.id + "\\librosEstadisticasXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Libro"))
        {
            ele.getChild("num_descargas").setText("0");
        }
        
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream file;
        try
        {
            file = new FileOutputStream("C:\\redes2\\servidor_" + this.id + "\\librosEstadisticasXML.xml");
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
    
    //Coloca el xml de los clientes fieles con solo el root element
    private boolean clientesFielesXML()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("C:\\redes2\\servidor_" + this.id + "\\clientesFielesXML.xml");
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
            file = new FileOutputStream("C:\\redes2\\servidor_" + this.id + "\\clientesFielesXML.xml");
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
    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getIdSd() {
        return id;
    }

    public void setIdSd(int id) {
        this.id = id;
    }

    public int getDescargas() {
        return descargas;
    }

    public void setDescargas(int descargas) {
        this.descargas = descargas;
    }

    public Socket getS() {
        return s;
    }

    public String getDirIP() {
        return dirIP;
    }

    public int getPuertocentral() {
        return puertocentral;
    }

    public ServerSocket getSs() {
        return ss;
    }
    
}
