/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ManejadorDescargas extends Thread{

    private Socket sd;
    private String ipSd;
    private int portSd;
    private String user;
    private String libro;
    private String rutaXML;
    private String mensaje;
    private String accion;
    private String rutaRetomar;
    private Cliente c;
    private FileOutputStream fos;
    
    public ManejadorDescargas(String ipSd, int portSd, String user, String libro, String rutaXML,
            Cliente c, String rutaRetomar, String accion)
    {
        this.ipSd = ipSd;
        this.portSd = portSd;
        this.user = user;
        this.libro = libro;
        this.rutaXML = rutaXML;
        this.c = c;
        //Acción 0 indica iniciar descarga desde 0
        //Accion 1 indica retomar descarga
        this.accion = accion;
        //Si no va a retomar el valor serpa nulo, de resto será la ruta donde
        //está el archivo incompleto
        this.rutaRetomar = rutaRetomar;
    }
    
    public void run()
    {
        try {
            //En base a la acción descarga desde cero o retoma descarga
            if(this.accion.equalsIgnoreCase("0"))
            {
                sd = new Socket(this.ipSd, this.portSd);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(sd.getInputStream()));
                PrintWriter out = new PrintWriter(sd.getOutputStream(),true);
                //Obtiene el nombre de la carpeta home del usuario conectado
                String homeFolder = System.getProperty("user.home");
                //Crea un string con el nombre del directorio de descarga
                String downloadDir = homeFolder + "\\ProyectDownloadsRedes\\" + user;
                //Creo la variable para generar el directorio de descarga del usuario
                File downloadFolder = new File(downloadDir);
                //Si el directorio no existe la crea
                if(!downloadFolder.exists())
                {
                    //Si pudo crear el directorio
                    if(downloadFolder.mkdirs())
                    {
                        System.out.println("Directorio creado");
                    }
                    //Si no
                    else{
                        System.out.println("Hubo un error creando el directorio de descarga");
                    }
                }
                out.println(this.accion);
                //Manda el nombre de usuario y libro para la descarga
                out.println(this.user);
                out.println(this.libro);
                out.println(InetAddress.getLocalHost().getHostAddress());
                //Obtiene el nombre con el que se guardará el archivo
                String nombreArchivo = in.readLine();
                System.out.println("Lo podrás encontrar en este directorio: " +
                        downloadDir + "\\" +nombreArchivo);
                //Indica donde va a guardarse el archivo
                String rutaArchivo = downloadDir + "\\" + nombreArchivo;
                fos = new FileOutputStream(rutaArchivo);
                //
                byte[] buffer = new byte[1024 * 16];
                int count;
                InputStream ins = this.sd.getInputStream();
                //Status de la descarga
                String statusD = "Descargando";
                //Obtiene el tamaño total del archivo
                long tamanhoArchivo = Long.parseLong(in.readLine());
                //Variable para los bytes descargados
                long bytesDescargados = 0;
                //Porcentaje descargado
                long porcentaje = 0;
                //Genera el nodo para llevar el status en el xml
                boolean crearStatus =
                crearNodoXmlStatus(this.user, this.libro, this.ipSd, 
                        Integer.toString(this.portSd), statusD, tamanhoArchivo, 
                        porcentaje, rutaArchivo);
                if(crearStatus)
                {
                    out.println("true");
                    //Este ciclo guarda el archivo
                    while((count = ins.read(buffer)) >= 0)
                    {
                        //Escribe en el archivo los bytes mandados por el SD
                        fos.write(buffer, 0, count);
                        //Obtiene los bytes descargados
                        bytesDescargados = fos.getChannel().size();
                        //Actualiza el xml en base a lo descargado
                        porcentaje = ((bytesDescargados*100)/tamanhoArchivo);
                        //Actualiza el xml del status
                        actualizarStatus(this.user, this.libro, porcentaje);
                        //Si cierra sesión tumba la descarga
                        if(c.getSession().equalsIgnoreCase("false"))
                        {
                            fos.close();
                            out.close();
                            in.close();
                            sd.close();
                            descargaCancelada(user, libro);
                            return;
                        }
                        //Pausa el proceso esa cantidad ms para visualizar las estadísticas
                        Thread.sleep(50);
                    }
                    //Actualiza que la descarga está completa
                    descargaFinalizada(this.user, this.libro);
                }
                else
                {
                    out.println("false");
                    if(mensaje.equalsIgnoreCase("0"))
                    {
                        //Hubo un error
                    }
                    else if(mensaje.equalsIgnoreCase("1"))
                    {
                        System.err.println("El archivo que desea descargar o ya lo descargó"
                                + " o su descarga está incompleta.");
                        System.err.println("En el caso que esté incompleto ingrese a la opción"
                                + " del menú para retomar descargas incompletas.");
                    }
                }
                //
                fos.close();
                out.close();
                in.close();
                //Cierra la conexión con el SD
                sd.close();
            }
            else if(this.accion.equalsIgnoreCase("1"))
            {
                sd = new Socket(this.ipSd, this.portSd);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(sd.getInputStream()));
                PrintWriter out = new PrintWriter(sd.getOutputStream(),true);
                out.println(this.accion);
                //Consigue el archivo incompleto
                File archivoIncompleto = new File(rutaRetomar);
                String status = "Descargando";
                //Obtiene el tamaño real de lo que se descargó
                long bytesDescargados = archivoIncompleto.length();
                //Manda los detalles para recuperar la descarga
                out.println(this.user);
                out.println(this.libro);
                out.println(InetAddress.getLocalHost().getHostAddress());
                out.println(Long.toString(bytesDescargados));
                
                //
                fos = new FileOutputStream(rutaRetomar, true);
                byte[] buffer = new byte[1024 * 16];
                int count;
                InputStream ins = this.sd.getInputStream();
                long tamanhoArchivo = Long.parseLong(in.readLine());
                long porcentaje = ((bytesDescargados*100)/tamanhoArchivo);
                //
                while((count = ins.read(buffer)) >= 0)
                {
                    //Escribe en el archivo los bytes mandados por el SD
                    fos.write(buffer, 0, count);
                    //Obtiene los bytes descargados
                    bytesDescargados = fos.getChannel().size();
                    //Actualiza el xml en base a lo descargado
                    porcentaje = ((bytesDescargados*100)/tamanhoArchivo);
                    //Actualiza el xml del status
                    actualizarStatus(this.user, this.libro, porcentaje);
                    //Si cierra sesión tumba la descarga
                    if(c.getSession().equalsIgnoreCase("false"))
                    {
                        fos.close();
                        out.close();
                        in.close();
                        sd.close();
                        descargaCancelada(user, libro);
                        return;
                    }
                    //Pausa el proceso esa cantidad ms para visualizar las estadísticas
                    Thread.sleep(50);
                }
                //Actualiza que la descarga está completa
                descargaFinalizada(this.user, this.libro);
                //
                fos.close();
                out.close();
                in.close();
                sd.close();
            }
        }
        catch (ConnectException ex)
        {
            System.err.println("Problemas con la conexión al servidor de descarga");
            System.err.println("Presione enter para ver el menú de nuevo");
            return;
        }
        catch (SocketException ex){
            System.err.println("Se ha perdido la conexión con el servidor de descarga");
            System.err.println("Presione enter para ver el menú de nuevo");
            //Si se cae el servidor tumba la escritura al archivo
            try {
                fos.close();
                sd.close();
                descargaCancelada(user, libro);
                
            } catch (IOException ex1) {
                //
            }
            return;
        }
        catch (IOException ex) {
            System.err.println(ex);
            System.err.println("Presione enter para ver el menú de nuevo");
            return;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
    }
    
    //Crea el nodo de la descarga en el xml
    private boolean crearNodoXmlStatus(String cliente, String book, String ip,
            String puerto, String status, long tamanhoTotal, long percentage, String ruta)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(this.rutaXML);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            this.mensaje = "0";
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            this.mensaje = "0";
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        Element descarga = new Element("Descarga");
        
        Element user = new Element("cliente");
        user.addContent(cliente);
        
        Element bookSd = new Element("libro");
        bookSd.addContent(book);
        
        Element dir = new Element("ip");
        dir.addContent(ip);
        
        Element port = new Element("puerto");
        port.addContent(puerto);
        
        Element estado = new Element("status");
        estado.addContent(status);
        
        Element sizeF = new Element("tamanhoTotal");
        sizeF.addContent(Long.toString(tamanhoTotal));
        
        Element porcentajeD = new Element("porcentaje");
        porcentajeD.addContent(Long.toString(percentage));
        
        Element route = new Element("ruta");
        route.addContent(ruta);
        
        for(Element ele : rootDoc.getChildren("Descarga"))
        {
            if(ele.getChildText("cliente").equals(cliente) &&
                    ele.getChildText("libro").equalsIgnoreCase(book))
            {
                this.mensaje = "1";
                return false;
            }
        }
        
        descarga.addContent(user);
        descarga.addContent(bookSd);
        descarga.addContent(dir);
        descarga.addContent(port);
        descarga.addContent(estado);
        descarga.addContent(sizeF);
        descarga.addContent(porcentajeD);
        descarga.addContent(route);
        
        rootDoc.addContent(descarga);
        
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream file;
        try
        {
            file = new FileOutputStream(rutaXML);
            xmlOutput.output(rootDoc, file);
            file.flush();
            file.close();
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            this.mensaje = "0";
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            this.mensaje = "0";
            return false;
        }
    }
    
    //Actualiza el status acorde a lo que va descargando
    private void actualizarStatus(String cliente, String book,
            long percentage)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(this.rutaXML);
            
            Element rootDoc = doc.getRootElement();

            actLoop:
            for(Element ele : rootDoc.getChildren("Descarga"))
            {
                if(ele.getChildText("cliente").equals(cliente) &&
                        ele.getChildText("libro").equalsIgnoreCase(book))
                {
                    long perc = Long.parseLong(ele.getChildText("porcentaje"));
                    if(percentage > perc)
                    {
                        ele.getChild("porcentaje").setText(Long.toString(percentage));

                        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                        FileOutputStream file;
                        try
                        {
                            file = new FileOutputStream(rutaXML);
                            xmlOutput.output(rootDoc, file);
                            file.flush();
                            file.close();
                            break actLoop;
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                            break actLoop;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            break actLoop;
                        }
                    }
                    break actLoop;
                }
            }
        }
        catch (JDOMException ex) 
        {
            //
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    
    //Coloca el status en "Incompleto"
    private void descargaCancelada(String cliente, String book)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(rutaXML);
            
            Element rootDoc = doc.getRootElement();
            
            actLoop:
            for(Element ele : rootDoc.getChildren("Descarga"))
            {
                String user = ele.getChildText("cliente");
                String libroD = ele.getChildText("libro");
                if(user.equals(cliente) && libroD.equalsIgnoreCase(book))
                {
                    ele.getChild("status").setText("Incompleto");
                    
                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    FileOutputStream file;
                    try
                    {
                        file = new FileOutputStream(rutaXML);
                        xmlOutput.output(rootDoc, file);
                        file.flush();
                        file.close();
                        break actLoop;
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                        break actLoop;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        break actLoop;
                    }
                }
            }
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    //Coloca el status en "Completo"
    private void descargaFinalizada(String cliente, String book)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        //while(true)
        //{
            try {
                doc = builder.build(this.rutaXML);

                Element rootDoc = doc.getRootElement();

                actLoop:
                for(Element ele : rootDoc.getChildren("Descarga"))
                {
                    if(ele.getChildText("cliente").equals(cliente) &&
                            ele.getChildText("libro").equalsIgnoreCase(book))
                    {
                        ele.getChild("status").setText("Completado");

                        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                        FileOutputStream file;
                        try
                        {
                            file = new FileOutputStream(rutaXML);
                            xmlOutput.output(rootDoc, file);
                            file.flush();
                            file.close();
                            return;
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                            break actLoop;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            break actLoop;
                        }
                    }
                }
            }
            catch (JDOMException ex) 
            {
                System.err.println("Error con el xml");
                System.out.println("");
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        //}
    }
}
