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
public class ManejadorSD extends Thread
{

    private Socket socket;
    private servidorDescarga sd;
    
    private String user;
    private String libro;
    private String ipUser;

    public ManejadorSD(Socket socket, servidorDescarga sd)
    {
        this.socket = socket;
        this.sd = sd;
    }

    public void run()
    {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            String accion = in.readLine();
            if(accion.equalsIgnoreCase("0"))
            {
                //Obtiene el nombre del usuario y el libro que quiere descargar
                user = in.readLine();
                libro = in.readLine();
                ipUser = in.readLine();
                int suma = 0;

                //System.out.println("El usuario " + user + " desea descargar el libro: " + libro);
                //Array con los detalles del libro
                String[] detallesLibro = new String[2];
                detallesLibro = directorioLibro(libro);
                //El primer elemento del array es la ruta
                //El segundo es el nombre que se le asignará al archivo en el cliente
                //System.out.println("La ruta del libro es: " + detallesLibro[0]);
                //Se le manda el nombre con la extensión que tendrá el archivo
                out.println(detallesLibro[1]);
                out.flush();
                //
                File file = new File(detallesLibro[0]);
                int count;
                byte[] buffer = new byte[1024 * 16];
                OutputStream outs = this.socket.getOutputStream();
                BufferedInputStream ins = new BufferedInputStream(new FileInputStream(file));
                //Manda el tamaño total del archivo
                out.println(file.length());
                String mensaje = in.readLine();
                if(mensaje.equalsIgnoreCase("true"))
                {
                    //Levanta el socket con el SC
                    Socket sc = sd.getS();
                    sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                    PrintWriter out2 = new PrintWriter(sc.getOutputStream(), true);
                    //Actualiza al SC con respecto a los clientes conectados
                    actualizarLibrosDescargando(libro, suma);
                    out2.println("as");
                    out2.println("clientes");
                    //Manda el IP
                    out2.println(InetAddress.getLocalHost().getHostAddress());
                    //Manda el puerto
                    out2.println(Integer.toString(sd.getSs().getLocalPort()));
                    //Manda la cantidad de personas conectadas
                    out2.println(sd.getI());
                    out2.flush();
                    out2.close();
                    sc.close();
                    //Empieza a mandar el archivo
                    while ((count = ins.read(buffer)) >= 0)
                    {
                        outs.write(buffer, 0, count);
                        outs.flush();
                        if(sd.getSs().isClosed())
                        {
                            return;
                        }
                    }
                    //Cierra canales con cliente
                    out.close();
                    in.close();
                    //Aumenta el número de descargas completadas
                    sd.setDescargas(sd.getDescargas() + 1);
                    //Envía actualización a SC sobre cantidad de archivos descargados
                    sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                    out2 = new PrintWriter(sc.getOutputStream(), true);
                    out2.println("as");
                    out2.println("descargas");
                    //Manda el ip
                    out2.println(InetAddress.getLocalHost().getHostAddress());
                    //Manda el puerto
                    out2.println(Integer.toString(sd.getSs().getLocalPort()));
                    //Manda el total de descargas del SD
                    out2.println(Integer.toString(sd.getDescargas()));
                    //Manda el nombre del libro
                    out2.println(libro);
                    out2.flush();
                    out2.close();
                    sc.close();
                    //Actualiza los clientes conectados en el SC
                    suma = 1;
                    actualizarLibrosDescargando(libro, suma);

                    sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                    out2 = new PrintWriter(sc.getOutputStream(), true);
                    out2.println("as");
                    out2.println("clientes");
                    //Terminó la descarga, reduce la cantidad de personas conectadas
                    sd.setI(sd.getI() - 1);
                    actualizarLibrosDescargados(libro);
                    actualizarClientesFieles(user, ipUser);
                    //Manda el IP
                    out2.println(InetAddress.getLocalHost().getHostAddress());
                    //Manda el puerto
                    out2.println(Integer.toString(sd.getSs().getLocalPort()));
                    //Manda la cantidad de personas conectadas
                    out2.println(sd.getI());
                    out2.flush();
                    out2.close();
                    sc.close();
                }
                else
                {
                    sd.setI(sd.getI() - 1);
                }
                //Cierra la conexión con el cliente
                socket.close();
            }
            else if(accion.equalsIgnoreCase("1"))
            {
                //Obtiene los datos del cliente
                user = in.readLine();
                libro = in.readLine();
                ipUser = in.readLine();
                //Obtiene lo que se ha descargado hasta ese punto
                long bytesDescargados = Long.parseLong(in.readLine());
                //Busca el archivo para retomar la descarga
                int suma = 0;
                String[] detallesLibro = new String[2];
                detallesLibro = directorioLibro(libro);
                //
                //
                //Crea los canales para la descarga
                File file = new File(detallesLibro[0]);
                int count;
                byte[] buffer = new byte[1024 * 16];
                //Arreglo de bytes con el tamaño de lo que ya descargó
                byte[] sizeActual = new byte[(int)bytesDescargados];
                OutputStream outs = this.socket.getOutputStream();
                BufferedInputStream ins = new BufferedInputStream(new FileInputStream(file));
                //
                //
                //Levanta el socket con el SC
                Socket sc = sd.getS();
                sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                PrintWriter out2 = new PrintWriter(sc.getOutputStream(), true);
                //Actualiza al SC con respecto a los clientes conectados
                actualizarLibrosDescargando(libro, suma);
                out2.println("as");
                out2.println("clientes");
                //Manda el IP
                out2.println(InetAddress.getLocalHost().getHostAddress());
                //Manda el puerto
                out2.println(Integer.toString(sd.getSs().getLocalPort()));
                //Manda la cantidad de personas conectadas
                out2.println(sd.getI());
                out2.flush();
                out2.close();
                sc.close();
                //Lee los bytes que ha descargado del archivo para empezar donde quedó
                ins.read(sizeActual);
                out.println(file.length());
                //Empieza a mandar el archivo
                
                while ((count = ins.read(buffer)) >= 0)
                {
                    outs.write(buffer, 0, count);
                    outs.flush();
                    if(sd.getSs().isClosed())
                    {
                        return;
                    }
                }
                
                out.close();
                in.close();
                //
                //Aumenta el número de descargas completadas
                sd.setDescargas(sd.getDescargas() + 1);
                //Envía actualización a SC sobre cantidad de archivos descargados
                sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                out2 = new PrintWriter(sc.getOutputStream(), true);
                out2.println("as");
                out2.println("descargas");
                //Manda el ip
                out2.println(InetAddress.getLocalHost().getHostAddress());
                //Manda el puerto
                out2.println(Integer.toString(sd.getSs().getLocalPort()));
                //Manda el total de descargas del SD
                out2.println(Integer.toString(sd.getDescargas()));
                //Manda el nombre del libro
                out2.println(libro);
                out2.flush();
                out2.close();
                sc.close();
                //Actualiza los clientes conectados en el SC
                suma = 1;
                actualizarLibrosDescargando(libro, suma);

                sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                out2 = new PrintWriter(sc.getOutputStream(), true);
                out2.println("as");
                out2.println("clientes");
                //Terminó la descarga, reduce la cantidad de personas conectadas
                sd.setI(sd.getI() - 1);
                actualizarLibrosDescargados(libro);
                actualizarClientesFieles(user, ipUser);
                //Manda el IP
                out2.println(InetAddress.getLocalHost().getHostAddress());
                //Manda el puerto
                out2.println(Integer.toString(sd.getSs().getLocalPort()));
                //Manda la cantidad de personas conectadas
                out2.println(sd.getI());
                out2.flush();
                out2.close();
                sc.close();
            }
            //System.out.println("");
        } catch (SocketException e) {
            try {
                System.err.println("Se cerró inesperadamente la conexión con el cliente "+user);
                Socket sc = new Socket(sd.getDirIP(), sd.getPuertocentral());
                PrintWriter out2 = new PrintWriter(sc.getOutputStream(), true);
                out2.println("as");
                out2.println("clientes");
                //Terminó la descarga, reduce la cantidad de personas conectadas
                sd.setI(sd.getI() - 1);
                int suma = 1;
                actualizarLibrosDescargando(libro, suma);
                //Manda el IP
                out2.println(InetAddress.getLocalHost().getHostAddress());
                //Manda el puerto
                out2.println(Integer.toString(sd.getSs().getLocalPort()));
                //Manda la cantidad de personas conectadas
                out2.println(sd.getI());
                out2.flush();
                out2.close();
                sc.close();
                this.socket.close();
            } catch (IOException ex) {
                //
            }
        } catch (FileNotFoundException ex) {
            System.err.println("No se encontró el archivo");
            sd.setI(sd.getI() - 1);
        } catch (IOException ex) {
            ex.printStackTrace();
            sd.setI(sd.getI() - 1);
        }
    }

    //Obtiene la ruta del archivo y el nombre que se le asignará al mismo
    //una vez se descargue
    private String[] directorioLibro(String libro)
    {
        SAXBuilder builder = new SAXBuilder();

        Document doc = null;

        try {
            doc = builder.build("C:\\redes2\\servidor_" + sd.getIdSd() + "\\libros.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String[] dirLibro = new String[2];

        Element rootDoc = doc.getRootElement();

        for (Element ele : rootDoc.getChildren("Libro"))
        {
            if (ele.getChildText("nombre").equalsIgnoreCase(libro))
            {
                dirLibro[0] = ele.getChildText("ruta");
                dirLibro[1] = ele.getChildText("nombre") + ele.getChildText("extension");
                return dirLibro;
            }
        }
        return dirLibro;
    }

    //Ver libros que se están descargando en ese momento con el número de
    //clientes asociados
    private boolean actualizarLibrosDescargando(String libro, int suma)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;

        try {
            doc = builder.build("C:\\redes2\\servidor_" + sd.getIdSd() + "\\librosDescargandoXML.xml");
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
            if(ele.getChildText("nombre").equalsIgnoreCase(libro))
            {
                int clientes = Integer.parseInt(ele.getChildText("num_clientes"));
                //Suma o resta cliente dependiendo de la situación
                if(suma == 0)
                    clientes++;
                else if(suma == 1)
                    clientes--;
                //Cambia el valor de la etiqueta num_clientes
                ele.getChild("num_clientes").setText(Integer.toString(clientes));
                
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                FileOutputStream file;
                try
                {
                    file = new FileOutputStream("C:\\redes2\\servidor_" + sd.getIdSd() + "\\librosDescargandoXML.xml");
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
        }
        
        return false;
    }

    //Cuales libros ya se han descargado y en que cantidad
    private boolean actualizarLibrosDescargados(String libro)
    {
        SAXBuilder builder = new SAXBuilder();

        Document doc = null;

        try {
            doc = builder.build("C:\\redes2\\servidor_" + sd.getIdSd() + "\\librosEstadisticasXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        Element rootDoc = doc.getRootElement();

        boolean existe = false;
        for(Element ele : rootDoc.getChildren("Libro"))
        {
            if(ele.getChildText("nombreLibro").equalsIgnoreCase(libro))
            {
                existe = true;
                break;
            }
            else
            {
                existe = false;
            }
        }
        if(!existe)
        {
            Element book = new Element("Libro");
            
            Element nombreLibro = new Element("nombreLibro");
            nombreLibro.addContent(libro);
            
            Element num_descargas = new Element("num_descargas");
            num_descargas.addContent("1");
            
            book.addContent(nombreLibro);
            book.addContent(num_descargas);
            
            rootDoc.addContent(book);
            
            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream file;
            try
            {
                file = new FileOutputStream("C:\\redes2\\servidor_" + sd.getIdSd() + "\\librosEstadisticasXML.xml");
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
        else
        {
            for(Element ele : rootDoc.getChildren("Libro"))
            {
                if(ele.getChildText("nombreLibro").equalsIgnoreCase(libro))
                {
                    int num_descargas = Integer.parseInt(ele.getChildText("num_descargas"));
                    num_descargas++;
                    ele.getChild("num_descargas").setText(Integer.toString(num_descargas));
                    
                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    FileOutputStream file;
                    try
                    {
                        file = new FileOutputStream("C:\\redes2\\servidor_" + sd.getIdSd() + "\\librosEstadisticasXML.xml");
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
            }
        }
        return false;
    }

    //Clientes que solicitan mas libros (por su nombre de inscripciòn e IP)
    private boolean actualizarClientesFieles(String nombre, String ip)
    {
        SAXBuilder builder = new SAXBuilder();

        Document doc = null;

        try {
            doc = builder.build("C:\\redes2\\servidor_" + sd.getIdSd() + "\\clientesFielesXML.xml");
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        Element rootDoc = doc.getRootElement();
        
        boolean existe = false;
        for (Element ele : rootDoc.getChildren("Cliente"))
        {
            //Si lo encuentra es porque ya ha descargado libros
            if(ele.getChildText("nombre").equalsIgnoreCase(nombre))
            {
                existe = true;
                break;
            }
            else
            {
                existe = false;
            }
        }
        if(!existe)
        {
            Element cliente = new Element("Cliente");
            
            Element ipCliente = new Element("ip");
            ipCliente.addContent(new Text(ip));
            
            Element nombreCliente = new Element("nombre");
            nombreCliente.addContent(new Text(nombre));
            
            Element num_descargas = new Element("num_descargas");
            num_descargas.addContent(new Text("1"));
            
            cliente.addContent(ipCliente);
            cliente.addContent(nombreCliente);
            cliente.addContent(num_descargas);
            
            rootDoc.addContent(cliente);
            
            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream file;
            try
            {
                file = new FileOutputStream("C:\\redes2\\servidor_" + sd.getIdSd() + "\\clientesFielesXML.xml");
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
        else
        {
            for(Element ele : rootDoc.getChildren("Cliente"))
            {
                if(ele.getChildText("nombre").equalsIgnoreCase(nombre))
                {
                    int act_descargas = Integer.parseInt(ele.getChildText("num_descargas"));
                    act_descargas++;
                    ele.getChild("num_descargas").setText(Integer.toString(act_descargas));
                    
                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    FileOutputStream file;
                    try
                    {
                        file = new FileOutputStream("C:\\redes2\\servidor_" + sd.getIdSd() + "\\clientesFielesXML.xml");
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
            }
        }
        return false;
    }
}
