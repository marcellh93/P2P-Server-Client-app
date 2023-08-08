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
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
/**
 *
 * @author Marcell Hernández
 */
public class Cliente {
    
    private Socket socket;
    private String dirIP;
    private int puerto; //Puerto 50000
    private Scanner scanner;
    
    private String mensaje;
    private String session;
    private String inscrito;
    private String rutaXml = "./src/cliente/statusDescargasXML.xml";
    
    public Cliente(String dirIP, int puerto)
    {
        this.dirIP = dirIP;
        this.puerto = puerto;
    }
    
    public void correrCliente(Cliente c)
    {
        try
        {
            //Se levanta el socket en el servidor central
            socket = new Socket(dirIP, puerto);
            //System.out.println(socket.getInetAddress().getHostAddress());     //Imprime la dirección ip
            
            //Se levantan los canales de lectura y escritura y la lectura
            //por consola
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            scanner = new Scanner(System.in);
            /*Envía un mensaje indicando que es un cliente*/
            out.println("c");
            /*Variable del ciclo infinito del menú*/
            boolean connect = true;
            
            while(connect)
            {
                /*Lee e imprime por pantalla los mensajes del servidor*/
                System.out.println(in.readLine());
                System.out.println(in.readLine());
                System.out.println(in.readLine());
                System.out.println(in.readLine());
                System.out.println("");
                /*Lee por consola la opción*/
                mensaje = scanner.nextLine();
                System.out.println("");
                /*Menú dependiendo de la opción*/
                /*Inicio de sesión*/
                if(mensaje.equalsIgnoreCase("1"))
                {
                    out.println("LOGIN");
                    System.out.println(in.readLine());
                    String usuario = scanner.nextLine();
                    out.println(usuario);
                    System.out.println(in.readLine());
                    out.println(scanner.nextLine());
                    out.flush();
                    session = in.readLine();
                    System.out.println("\n\n\n\n\n\n");
                    if(session.equalsIgnoreCase("true"))
                    {
                        /*Acá van las opciones para el menú al iniciar sesión*/
                        while(session.equalsIgnoreCase("true"))
                        {
                            System.out.println(in.readLine());
                            System.out.println(in.readLine());
                            System.out.println(in.readLine());
                            System.out.println(in.readLine());
                            System.out.println(in.readLine());
                            System.out.println(in.readLine());
                            System.out.println("");
                            out.println(scanner.nextLine());
                            out.flush();
                            String opc = in.readLine();
                            System.out.println("");
                            if(opc.equalsIgnoreCase("1"))
                            {
                                System.out.println("\n\n\n\n\n\n");
                                
                                String vacio = in.readLine();
                                if(vacio.equalsIgnoreCase("n"))
                                {
                                    int tam = Integer.parseInt(in.readLine());
                                    //Cantidad de libros
                                    System.out.println("Hay "+tam+" libros");
                                    //Mensaje de los libros que existen son
                                    System.out.println(in.readLine());
                                    System.out.println("");
                                    //Imprime el catálogo
                                    for(int i = 0; i < tam; i++)
                                    {
                                        //Numero de id del libro
                                        System.out.println(in.readLine());
                                        //Nombre del libro
                                        System.out.println(in.readLine());
                                        //Texto de autores
                                        System.out.println(in.readLine());
                                        //Cantidad de autores
                                        int tam2 = Integer.parseInt(in.readLine());
                                        for(int j = 0; j < tam2; j++)
                                        {
                                            //Nombre del autor
                                            System.out.println(in.readLine());
                                        }
                                        //Texto de géneros
                                        System.out.println(in.readLine());
                                        //Cantidad de generos
                                        tam2 = Integer.parseInt(in.readLine());
                                        for(int j = 0; j < tam2; j++)
                                        {
                                            //Nombre del género
                                            System.out.println(in.readLine());
                                        }
                                        //Separa los libros en consola
                                        System.out.println("");
                                    }
                                    //Envía al servidor central el libro a descargar
                                    out.println(scanner.nextLine());
                                    out.flush();
                                    System.out.println("");
                                    //Obtiene ip y puerto del SD
                                    //Además del nombre del libro
                                    String ipSd = in.readLine();
                                    int portSd = Integer.parseInt(in.readLine());                                
                                    String libro = in.readLine();
                                    //Muestra el ip y el puerto del SD
                                    //Y el nombre del libro
                                    System.out.println("Ip del servidor: "+ipSd);
                                    System.out.println("Puerto del servidor: "+portSd);
                                    System.out.println("Libro a descargar: "+libro);
                                    System.out.println("Conectando...");
                                    //Abre la clase que maneja la descarga
                                    new ManejadorDescargas(ipSd, portSd, usuario, libro, rutaXml, c, null, "0").start();
                                    //
                                    Thread.sleep(300);
                                    System.out.println("Presione enter para seguir...");
                                    scanner.nextLine();

                                    System.out.println("");
                                }
                                else if(vacio.equalsIgnoreCase("s"))
                                {
                                    System.out.println(in.readLine());
                                    System.out.println("");
                                }
                            }
                            else if(opc.equalsIgnoreCase("2"))
                            {
                                String vacio = in.readLine();
                                if(vacio.equalsIgnoreCase("n"))
                                {
                                    System.out.println("\n\n\n\n\n\n");
                                    //Mensaje de libros por autor
                                    System.out.println(in.readLine());
                                    //Obtiene cantidad de autores únicos
                                    int tam = Integer.parseInt(in.readLine());
                                    //Imprime por pantalla los autores
                                    for(int i = 0; i < tam; i++)
                                    {
                                        System.out.println(in.readLine());
                                    }
                                    System.out.println("");
                                    //Manda la selección del autor
                                    out.println(scanner.nextLine());
                                    out.flush();
                                    System.out.println("");
                                    //Imprime el nombre del autor con sus libros
                                    System.out.println("Libros de: "+in.readLine());
                                    //Cantidad de libros del autor
                                    tam = Integer.parseInt(in.readLine());
                                    //Imprime el nombre de los libros del autor escogido
                                    for(int i = 0; i < tam; i++)
                                    {
                                        System.out.println(in.readLine());
                                    }
                                    System.out.println("");
                                    //Manda la opción al servidor central
                                    out.println(scanner.nextLine());
                                    out.flush();
                                    //Obtiene el ip y el puerto del SD
                                    System.out.println("");
                                    String ipSd = in.readLine();
                                    int portSd = Integer.parseInt(in.readLine());
                                    String libro = in.readLine();
                                    //Muestra el ip y el puerto por consola
                                    System.out.println("Ip del servidor: "+ipSd);
                                    System.out.println("Puerto del servidor: "+portSd);
                                    System.out.println("Libro a descargar: "+libro);
                                    System.out.println("Conectando...");
                                    //Abre la clase que maneja la descarga
                                    System.out.println("");
                                    new ManejadorDescargas(ipSd, portSd, usuario, libro, rutaXml, c, null, "0").start();
                                    //
                                    Thread.sleep(300);
                                    System.out.println("Presione enter para seguir...");
                                    scanner.nextLine();
                                    System.out.println("");
                                }
                                else if(vacio.equalsIgnoreCase("s"))
                                {
                                    System.out.println(in.readLine());
                                    System.out.println("");
                                }
                            }
                            else if(opc.equalsIgnoreCase("3"))
                            {
                                String vacio = in.readLine();
                                if(vacio.equalsIgnoreCase("n"))
                                {
                                    System.out.println("\n\n\n\n\n\n");
                                    //Mensaje de libros por género
                                    System.out.println(in.readLine());
                                    //Cantidad de libros por género
                                    int tam = Integer.parseInt(in.readLine());
                                    //Imprime por pantalla los géneros
                                    for(int i = 0;i < tam; i++)
                                    {
                                        System.out.println(in.readLine());
                                    }
                                    System.out.println("");
                                    //Manda la selección del género
                                    out.println(scanner.nextLine());
                                    out.flush();
                                    System.out.println("");
                                    //Imprime el nombre del género seleccionado
                                    System.out.println("Libros de: "+in.readLine());
                                    //Cantidad de libros con ese género
                                    tam = Integer.parseInt(in.readLine());
                                    //Imprime el nombre de los libros con ese género
                                    for(int i = 0; i < tam; i++)
                                    {
                                        System.out.println(in.readLine());
                                    }
                                    System.out.println("");
                                    //Manda la opción al servidor central
                                    out.println(scanner.nextLine());
                                    out.flush();
                                    //Obtiene el ip y puerto del SD
                                    System.out.println("");
                                    String ipSd = in.readLine();
                                    int portSd = Integer.parseInt(in.readLine());
                                    String libro = in.readLine();
                                    //Muestra el ip y el puerto por consola
                                    System.out.println("Ip del servidor: "+ipSd);
                                    System.out.println("Puerto del servidor: "+portSd);
                                    System.out.println("Libro a descargar: "+libro);
                                    System.out.println("Conectando...");
                                    //Abre la clase que maneja la descarga
                                    System.out.println("");
                                    new ManejadorDescargas(ipSd, portSd, usuario, libro, rutaXml, c, null, "0").start();
                                    //
                                    Thread.sleep(300);
                                    System.out.println("Presione enter para seguir...");
                                    scanner.nextLine();
                                    System.out.println("");
                                }
                                else if(vacio.equalsIgnoreCase("s"))
                                {
                                    System.out.println(in.readLine());
                                    System.out.println("");
                                }
                            }
                            else if(opc.equalsIgnoreCase("4"))
                            {
                                mostrarStatusDescargas(usuario);
                                System.out.println("");
                            }
                            else if(opc.equalsIgnoreCase("5"))
                            {
                                ArrayList<String> librosI = mostrarDescargasIncompletas(usuario);
                                if(librosI != null)
                                {
                                    if(librosI.size() > 0)
                                    {
                                        System.out.println("De cual libro desea retomar la descarga: ");
                                        for(int i = 0; i < librosI.size(); i++)
                                        {
                                            System.out.println((i+1)+". "+librosI.get(i));
                                        }
                                        System.out.println("");
                                        try
                                        {
                                            int opc2 = Integer.parseInt(scanner.nextLine());
                                            System.out.println("");
                                            if(opc2 > librosI.size() || opc2 < 0)
                                            {
                                                System.out.println("Comando inválido");
                                                System.out.println("");
                                            }
                                            else
                                            {
                                                String libro = librosI.get(opc2 - 1);
                                                ArrayList<String> datosD = traerDatosDescarga(usuario, libro);
                                                String ipSd = datosD.get(0);
                                                int portSd = Integer.parseInt(datosD.get(1));
                                                String ruta = datosD.get(2);
                                                System.out.println("Usuario solicitando retomar descarga: "+usuario);
                                                System.out.println("Retomando descarga del libro:"+libro);
                                                System.out.println("Datos del servidor de descarga"
                                                        + ": IP -> "+ipSd+", Puerto -> "+portSd);
                                                System.out.println("Ruta del archivo incompleto: "+ruta);
                                                new ManejadorDescargas(ipSd, portSd, usuario, libro, rutaXml, c, ruta, "1").start();
                                                //
                                                Thread.sleep(300);
                                                System.out.println("Presione enter para volver al menú...");
                                                scanner.nextLine();
                                            }
                                        }
                                        catch(NumberFormatException e)
                                        {
                                            System.err.println("No puede ingresar una letra como opción");
                                            System.out.println("");
                                        }
                                    }
                                    else if(librosI.size() == 0)
                                    {
                                        System.out.println("Este usuario no tiene descargas incompletas en esta máquina");
                                        System.out.println("");
                                    }
                                }
                            }
                            else if(opc.equalsIgnoreCase("0"))
                            {
                                System.out.println(in.readLine());
                                System.out.println("");
                                session = "false";
                            }
                            else
                            {
                                System.out.println(in.readLine());
                                System.out.println("");
                            }
                        }
                    }
                    else
                    {
                        System.out.println(in.readLine());
                    }
                }
                /*Inscripción de usuario*/
                else if(mensaje.equalsIgnoreCase("2"))
                {
                    out.println("INSCRIPCION");
                    System.out.println(in.readLine());
                    out.println(scanner.nextLine());
                    System.out.println(in.readLine());
                    out.println(scanner.nextLine());
                    System.out.println(in.readLine());
                }
                /*Salir y cerrar el programa*/
                else if(mensaje.equalsIgnoreCase("0"))
                {
                    out.println("SALIR");
                    System.out.println(in.readLine());
                    out.close();
                    in.close();
                    scanner.close();
                    socket.close();
                    connect = false;
                }
                /*Opción inválida*/
                else
                {
                    out.println(mensaje);
                    System.out.println(in.readLine());
                }
            }
            //Cierra la conexión con el servidor
            socket.close();
            
        }
        /*Colocar las excepciones con los mensajes correspondientes*/
        catch(ConnectException ex)
        {
            System.err.println("No se pudo conectar con el servidor");
        }
        catch(IOException ioe)
        {
            System.err.println(ioe);
        }
        catch(NumberFormatException e)
        {
            System.err.println("No se puede leer un texto como número");
            System.err.println("Presione enter para mostrar el menú");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //Muestra el status de la descarga
    private void mostrarStatusDescargas(String us)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(rutaXml);
            
            Element rootDoc = doc.getRootElement();
            
            if(rootDoc.getChildren("Descarga").size() > 0)
            {
                System.out.println("Status de descargas del cliente "+us+": ");
                for(Element ele : rootDoc.getChildren("Descarga"))
                {
                    if(ele.getChildText("cliente").equalsIgnoreCase(us))
                    {
                        String libro = ele.getChildText("libro");
                        String porcentaje = ele.getChildText("porcentaje");
                        System.out.println("- Libro: "+libro+", Porcentaje descargado: "+porcentaje+"%");
                    }
                }
            }
            else
            {
                System.out.println("Este usuario no ha realizado descargas en esta máquina");
            }
        } 
        catch (JDOMException ex) 
        {
            System.err.println("Intente de nuevo");
            System.err.println("Presione enter para ver el menú de nuevo...");
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }

    //Obtiene los libros incompleto del usuario conectado
    private ArrayList<String> mostrarDescargasIncompletas(String user)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(rutaXml);
            
            Element rootDoc = doc.getRootElement();
            
            ArrayList<String> librosIncompletos = new ArrayList<>();
            
            for(Element ele : rootDoc.getChildren("Descarga"))
            {
                String cliente = ele.getChildText("cliente");
                String status = ele.getChildText("status");
                if(cliente.equals(user) && status.equalsIgnoreCase("Incompleto"))
                {
                    librosIncompletos.add(ele.getChildText("libro"));
                }
            }
            
            return librosIncompletos;
            
        } 
        catch (JDOMException ex) 
        {
            System.err.println("Intente de nuevo");
            System.err.println("Presione enter para ver el menú de nuevo...");
            return null;
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            return null;
        }
    }
    
    //Se trae los datos necesarios para retomar la descarga
    private ArrayList<String> traerDatosDescarga(String user, String libro)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(rutaXml);
            
            Element rootDoc = doc.getRootElement();
            
            ArrayList<String> servidorDatos = new ArrayList<>();
            
            for(Element ele : rootDoc.getChildren("Descarga"))
            {
                String cliente = ele.getChildText("cliente");
                String book = ele.getChildText("libro");
                if(cliente.equals(user) && book.equalsIgnoreCase(libro))
                {
                    String ip = ele.getChildText("ip");
                    String puerto = ele.getChildText("puerto");
                    String ruta = ele.getChildText("ruta");

                    servidorDatos.add(ip);
                    servidorDatos.add(puerto);
                    servidorDatos.add(ruta);
                    return servidorDatos;
                }
            }
            return null;
        } 
        catch (JDOMException ex) 
        {
            System.err.println("Intente de nuevo");
            System.err.println("Presione enter para ver el menú de nuevo...");
            return null;
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            return null;
        }
    }
    
    //Getters y setters utilizados
    public String getSession() {
        return session;
    }
    
}
