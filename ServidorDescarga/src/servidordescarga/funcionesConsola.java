/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidordescarga;

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
public class funcionesConsola extends Thread{
    
    private int id;
    private servidorDescarga sd;
    
    public funcionesConsola(int id, servidorDescarga sd)
    {
        this.id = id;
        this.sd = sd;
    }
    
    public void run()
    {
        Scanner sc = new Scanner(System.in);
        for(;;)
        {
            System.out.println("Escoja una de las siguientes opciones: ");
            System.out.println("1. Ver libros que se están descargando");
            System.out.println("2. Ver estadísticas de los libros descargados");
            System.out.println("3. Clientes fieles");
            System.out.println("0. Cerrar Servidor");
            System.out.println("");
            String opc = sc.nextLine();
            System.out.println("");
            if(opc.equalsIgnoreCase("1"))
            {
                librosDescargando();
                System.out.println("");
            }
            else if(opc.equalsIgnoreCase("2"))
            {
                librosDescargados();
                System.out.println("");
            }
            else if(opc.equalsIgnoreCase("3"))
            {
                clientesFieles();
                System.out.println("");
            }
            else if(opc.equalsIgnoreCase("0"))
            {
                try {
                    Socket s = sd.getS();
                    s = new Socket(sd.getDirIP(), sd.getPuertocentral());
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    //El proceso para quitar al SD del SC ya que se está desconectando
                    //así los clientes no son redirigidos hacia acá erróneamente
                    out.println("as");
                    out.println("cerrar");
                    out.println(InetAddress.getLocalHost().getHostAddress());
                    out.println(Integer.toString(sd.getSs().getLocalPort()));
                    out.println(Integer.toString(cantidadLibros()));
                    //
                    out.flush();
                    out.close();
                    s.close();
                    sd.getSs().close();
                    return;
                } catch (IOException ex) {
                    //
                }
            }
            else
            {
                System.out.println("Opción inválida, intente otra opción");
                System.out.println("");
            }
        }
    }
    
    //Ver libros que se están descargando en ese momento con el número de
    //clientes asociados
    private void librosDescargando()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("C:\\redes2\\servidor_" + this.id + "\\librosDescargandoXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        boolean IsDownloading = false;
        
        for(Element ele : rootDoc.getChildren("Libro"))
        {
            if(Integer.parseInt(ele.getChildText("num_clientes")) > 0)
            {
                IsDownloading = true;
                break;
            }
            else
            {
               IsDownloading = false;
            }
        }
        
        if(IsDownloading)
        {
            System.out.println("Los libros que se están descargando ahora son: ");
            for(Element ele : rootDoc.getChildren())
            {
                if(Integer.parseInt(ele.getChildText("num_clientes")) > 0)
                {
                    String nombre = ele.getChildText("nombre");
                    String num_clientes = ele.getChildText("num_clientes");
                    System.out.println("- Libro: "+nombre+", Clientes descargando: "+num_clientes);
                }
            }
        }
        else
        {
            System.out.println("No se están descargando libros en este momento");
        }
    }
    
    //Cuales libros ya se han descargado y en que cantidad
    private void librosDescargados()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("C:\\redes2\\servidor_" + this.id + "\\librosEstadisticasXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        ArrayList<String> librosMenos = new ArrayList<>();
        ArrayList<String> librosMas = new ArrayList<>();
        
        int count_min = 0;
        int count_max = 0;
        
        //Obtiene el valor máximo de descargas entre todos los libros
        for(Element ele : rootDoc.getChildren("Libro"))
        {
            if(Integer.parseInt(ele.getChildText("num_descargas")) > count_max)
            {
                count_max = Integer.parseInt(ele.getChildText("num_descargas"));
            }
        }
        
        //Obtiene el o los libros con mas descargas
        for(Element ele: rootDoc.getChildren("Libro"))
        {
            if(Integer.parseInt(ele.getChildText("num_descargas")) == count_max)
            {
                librosMas.add(ele.getChildText("nombreLibro"));
            }
        }
        
        //Variable que me indica si ya encontré el valor mínimo de descargas en los libros
        boolean get_lower_value = false;
        
        //Hasta que no determine el valor mínimo se mantiene en este ciclo
        while(!get_lower_value)
        {
            for(Element ele : rootDoc.getChildren("Libro"))
            {
                if(Integer.parseInt(ele.getChildText("num_descargas")) == count_min)
                {
                    get_lower_value = true;
                    break;
                }
            }
            if(rootDoc.getChildren("Libro").size() == 0)
            {
                get_lower_value = true;
            }
            if(!get_lower_value)
            {
                count_min++;
            }
        }
        
        //Obtiene el o los libros con menos descargas
        for(Element ele : rootDoc.getChildren("Libro"))
        {
            if(Integer.parseInt(ele.getChildText("num_descargas")) == count_min)
            {
                librosMenos.add(ele.getChildText("nombreLibro"));
            }
        }
        
        if(librosMas.size() > 0 || librosMenos.size() > 0)
        {
            if(librosMas.size() == 1)
            {
                System.out.println("El libro mas descargado es: " + librosMas.get(0));
                System.out.println("");
            }
            else
            {
                System.out.println("Los libros mas descargados son: ");
                for(int i = 0; i < librosMas.size(); i++)
                {
                    System.out.println("- " + librosMas.get(i));
                }
                System.out.println("");
            }
            if(librosMenos.size() == 1)
            {
                System.out.println("El libro menos descargado es: " + librosMenos.get(0));
            }
            else
            {
                System.out.println("Los libros menos descargados son: ");
                for(int i = 0; i < librosMenos.size(); i++)
                {
                    System.out.println("- " + librosMenos.get(i));
                }
            }
        }
        else
        {
            System.out.println("No hay registro de libros descargados");
        }
    }
    
    //Clientes que solicitan mas libros (por su nombre de inscripciòn e IP)
    private void clientesFieles()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build("C:\\redes2\\servidor_" + this.id + "\\clientesFielesXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        int count = 0;
        
        for(Element ele : rootDoc.getChildren("Cliente"))
        {
            int num_descargas = Integer.parseInt(ele.getChildText("num_descargas"));
            if(num_descargas > count)
            {
                count = num_descargas;
            }
        }
        
        ArrayList<String> listaClientes = new ArrayList<>();
        ArrayList<String> listaClientesIp = new ArrayList<>();
        
        String num_descargas = Integer.toString(count);
        
        for(Element ele : rootDoc.getChildren("Cliente"))
        {
            if(ele.getChildText("num_descargas").equalsIgnoreCase(num_descargas))
            {
                listaClientes.add(ele.getChildText("nombre"));
                listaClientesIp.add(ele.getChildText("ip"));
            }
        }
        
        if(listaClientes.size() > 0)
        {
            if(listaClientes.size() == 1)
            {
                System.out.println("El cliente mas fiel es: " + listaClientes.get(0));
                System.out.println("Su ip es: " + listaClientesIp.get(0));
            }
            else if(listaClientes.size() > 1)
            {
                System.out.println("Los clientes mas fieles son: ");
                for(int i = 0; i < listaClientes.size(); i++)
                {
                    System.out.print("- ");
                    System.out.println(listaClientes.get(i) + ". Su ip es: " + listaClientesIp.get(i));
                }
            }
        }
        else
        {
            System.out.println("Este servidor no tiene descargas registradas");
        }
    }
    
    private int cantidadLibros()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build("C:\\redes2\\servidor_"+this.id+"\\libros.xml");
            
            Element rootDoc = doc.getRootElement();
            
            return rootDoc.getChildren("Libro").size();
            
        } catch (JDOMException ex) {
            //
            return 0;
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
