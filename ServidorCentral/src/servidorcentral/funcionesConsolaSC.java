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
public class funcionesConsolaSC extends Thread{
    
    public funcionesConsolaSC()
    {
        
    }
    
    public void run()
    {
        Scanner sc = new Scanner(System.in);
        for(;;)
        {
            System.out.println("Escoja una de las siguientes opciones: ");
            System.out.println("1. Ver Clientes por Servidor de Descarga");
            System.out.println("2. Ver Descargas por Servidor de Descarga");
            System.out.println("");
            String opc = sc.nextLine();
            System.out.println("");
            if(opc.equalsIgnoreCase("1"))
            {
                int cantidadSd = cantidadServidores();
                if(cantidadSd == 0)
                {
                    System.out.println("El Servidor Central no tiene Servidores de Descarga registrados");
                    System.out.println("");
                }
                else
                {
                    clientesPorServidor();
                }
            }
            else if(opc.equalsIgnoreCase("2"))
            {
                int cantidadSd = cantidadServidores();
                if(cantidadSd == 0)
                {
                    System.out.println("El Servidor Central no tiene Servidores de Descarga registrados");
                    System.out.println("");
                }
                else
                {
                    ArrayList<String> idServers = idServidores();
                    if(idServers != null)
                    {
                        if(idServers.size() == 0)
                        {
                            System.out.println("El Servidor Central no tiene Servidores de Descarga registrados");
                            System.out.println("");
                        }
                        else if(idServers.size() > 0)
                        {
                            for(int i = 0; i < cantidadSd; i++)
                            {
                                descargasPorServidor1(idServers.get(i));
                                descargasPorServidor2(idServers.get(i));
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Hubo un error");
                        System.out.println("");
                    }
                }
            }
            else
            {
                System.out.println("Opción inválida, intente otra opción");
                System.out.println("");
            }
        }
    }
    
    //Muestra los clientes por servidor
    private void clientesPorServidor()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("./src/servidorcentral/servidoresDescargaXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Server"))
        {
            String id = ele.getAttribute("id").getValue();
            String ip = ele.getChildText("ip");
            String port = ele.getChildText("port");
            String clientes = ele.getChildText("connected");
            System.out.println("Servidor #"+id+": ip -> "+ip+", puerto -> "+port+", clientes conectados: "+clientes);
            System.out.println("");
        }
    }
    
    //Obtiene la cantidad de servidores registrados
    private int cantidadServidores()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("./src/servidorcentral/servidoresDescargaXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        int cantidad = rootDoc.getChildren("Server").size();
        
        if(cantidad > 0)
        {
            return cantidad;
        }
        else
        {
            return 0;
        }
    }
    
    //Muestra el total de descargas de un servidor
    private void descargasPorServidor1(String id)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("./src/servidorcentral/servidoresDescargaXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Server"))
        {
            if(ele.getAttribute("id").getValue().equalsIgnoreCase(id))
            {
                String ip = ele.getChildText("ip");
                String port = ele.getChildText("port");
                String num_descargas = ele.getChildText("downloads");
                System.out.println("El servidor con el ip "+ip+" y el puerto "
                        +port+" tiene "+num_descargas+" descargas");
                System.out.println("Este servidor contiene los siguientes libros con "
                        + "su respectivas descargas:");
                break;
            }
        }
    }
    
    //Muestra las descargas detalladas de cada servidor con sus descargas por libros
    private void descargasPorServidor2(String id)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("./src/servidorcentral/catalogoLibrosXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            Element address = ele.getChild("address");
            for(Element ele2 : address.getChildren("server"))
            {
                if(ele2.getAttribute("id").getValue().equalsIgnoreCase(id))
                {
                    String libro = ele.getChildText("bookname");
                    String descargas = ele2.getChildText("num_descargas");
                    System.out.println("- Libro: "+libro+", Descargas: "+descargas);
                    break;
                }
            }
        }
        System.out.println("");
    }
    
    //Me trae el id de los servidores conectados
    private ArrayList<String> idServidores()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build("./src/servidorcentral/servidoresDescargaXML.xml");
            
            Element rootDoc = doc.getRootElement();
            
            ArrayList<String> ids = new ArrayList<>();
            
            for(Element ele : rootDoc.getChildren("Server"))
            {
                ids.add(ele.getAttribute("id").getValue());
            }
            return ids;
            
        } catch (JDOMException ex) {
            //
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
}
