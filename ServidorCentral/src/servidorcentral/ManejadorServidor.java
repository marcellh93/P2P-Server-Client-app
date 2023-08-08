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
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Marcell Hernández
 */
public class ManejadorServidor extends Thread{
    
    private Socket socket;
    private ServidorCentral ss;
    
    private static int libros = 1;
    private static int idautores = 1;
    private static int idgeneros = 1;
    private static boolean reconexion = false;
    
    private String OS;
    private String mensaje;
    private String user;
    private String password;
    private String ipServDescarga;
    private String ptoServDescarga;
    private String catalogoLibros = "./src/servidorcentral/catalogoLibrosXML.xml";
    private String servidoresDescarga = "./src/servidorcentral/servidoresDescargaXML.xml";

    private boolean connect = false;
    private boolean session = false;
    private boolean inscrito = false;
    
    public ManejadorServidor(Socket socket, String OS)
    {
        this.socket = socket;
        this.OS = OS;
    }
    
    public void run()
    {
        try
        {
                /*Se abren los canales de comunicación
                    de entrada y salida de datos
                */
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                mensaje = in.readLine();
                /*Si se conecta un cliente*/
                if(mensaje.equalsIgnoreCase("c"))
                {
                    /*Mientras no le de a la opción de salir
                        en el menú principal se mantiene conectado
                    */
                    connect = true;
                    while(connect)
                    {
                        /*Envía los mensajes al cliente*/
                        out.println("Bienvenido, por favor, escoja una opción");
                        out.println("1 - Iniciar sesión");
                        out.println("2 - Inscripción de usuario");
                        out.println("0 - Salir");
                        out.flush();
                        /*Lee la opcion del cliente*/
                        mensaje = in.readLine();
                        /*Inicio de sesion*/
                        if(mensaje.equalsIgnoreCase("LOGIN"))
                        {
                            out.println("Usuario: ");
                            user = in.readLine();
                            out.println("Password: ");
                            password = in.readLine();
                            /*Función que valida que el cliente tiene
                                usuario y contraseña
                            */
                            session = validarInicioSesion(user, password);
                            /*Si tiene una cuenta*/
                            if(session)
                            {
                                /*Le indica al cliente que inició sesión*/
                                out.println("true");
                                /*A partir de acá menú para las descargas
                                    y visualización de libros disponibles
                                */
                                while(session)
                                {
                                    out.println("1 - Ver colecciones de libros y descargar");
                                    out.println("2 - Buscar libro por autor y descargar");
                                    out.println("3 - Buscar libro por género y descargar");
                                    out.println("4 - Ver status de descargas");
                                    out.println("5 - Retomar descargas incompletas");
                                    out.println("0 - Cerrar Sesion");
                                    out.flush();
                                    String opc = in.readLine();
                                    if(opc.equalsIgnoreCase("1"))
                                    {
                                        out.println("1");
                                        out.flush();
                                        //Determino si al menos hay un servidor registrado
                                        boolean vacio = servidoresRegistrados();
                                        if(vacio == false)
                                        {
                                            out.println("n");
                                            //Le envía el catalogo al cliente
                                            catalogoMenu(out);
                                            //Lee la opción del cliente
                                            String opc2 = in.readLine();
                                            //String que guarda el ip y puerto del SD
                                            String[] address = new String[3];
                                            //Obtienes los servidores del libro
                                            ArrayList<String> servidoresLibro = new ArrayList<>();
                                            servidoresLibro = servidoresBalanceo(opc2);
                                            //Si el libro tiene algún servidor que lo contiene
                                            if(servidoresLibro != null)
                                            {
                                                //Obtiene el servidor con menos personas conectadas
                                                String servidorDisp = servidoresDisponible(servidoresLibro);
                                                //Obtiene el ip y el puerto
                                                address = direccionServer(opc2, servidorDisp);
                                                //Si el string no devolvió null
                                                if(address != null)
                                                {
                                                    for(int j = 0; j < address.length; j++)
                                                    {
                                                        out.println(address[j]);
                                                    }
                                                }
                                                else
                                                {
                                                    System.out.println("Error, meh...");
                                                }
                                            }
                                            //Si no
                                            else
                                            {
                                                
                                            }
                                        }
                                        else
                                        {
                                            out.println("s");
                                            out.println("No hay libros registrados");
                                        }
                                        
                                    }
                                    else if(opc.equalsIgnoreCase("2"))
                                    {
                                        out.println("2");
                                        out.flush();
                                        
                                        boolean vacio = servidoresRegistrados();
                                        if(vacio == false)
                                        {
                                            out.println("n");
                                            out.println("Libros por autor: ");
                                            //Cantidad de autores únicos
                                            int tam = cantAutoresUnicos();
                                            //Manda la cantidad al cliente
                                            out.println(Integer.toString(tam));
                                            //Envía el menú al cliente
                                            autoresMenu(tam, out);
                                            //Obtiene el id del autor
                                            String opc2 = in.readLine();
                                            //Array para guardar los libros de ese autor
                                            ArrayList<String> autorLibros = new ArrayList<>();
                                            //Obtiene los libros del autor
                                            autorLibros = filtroMenuAutores(opc2, out);
                                            //Cantidad de libros de ese autor
                                            int tam2 = autorLibros.size();
                                            out.println(Integer.toString(tam2));
                                            //Manda las opciones al cliente
                                            for(int i = 0; i < tam2; i++)
                                            {
                                                out.println((i+1)+". "+autorLibros.get(i));
                                            }
                                            //Obtiene la opción del cliente
                                            int opc3 = Integer.parseInt(in.readLine());
                                            /*
                                                Obtiene el nombre del libro de ese autor
                                                en base a la opción del cliente
                                            */
                                            String libro = (String) autorLibros.get(opc3 - 1);
                                            //Array para guardar el ip y puerto del SD
                                            String [] address = new String[3];
                                            //
                                            ArrayList<String> servidoresLibro = new ArrayList<>();
                                            servidoresLibro = servidoresBalanceo2(libro);
                                            if(servidoresLibro != null)
                                            {
                                                String servidorDisp = servidoresDisponible(servidoresLibro);
                                                //Obtiene el ip y el puerto del SD
                                                address = direccionServer2(opc2, libro, servidorDisp);
                                                //Le envía al cliente el ip y el puerto
                                                if(address != null)
                                                {
                                                    for(int i = 0; i < address.length; i++)
                                                    {
                                                        out.println(address[i]);
                                                    }
                                                }
                                                else
                                                {
                                                    System.out.println("Error, meh...");
                                                }
                                            }
                                        }
                                        else
                                        {
                                            out.println("s");
                                            out.println("No hay libros registrados");
                                        }
                                    }
                                    else if(opc.equalsIgnoreCase("3"))
                                    {
                                        out.println("3");
                                        
                                        boolean vacio = servidoresRegistrados();
                                        if(vacio == false)
                                        {
                                            out.println("n");
                                            out.println("Libros por género: ");
                                            //Cantidad de autores únicos
                                            int tam = cantGenerosUnicos();
                                            //Manda la cantidad al cliente
                                            out.println(Integer.toString(tam));
                                            //Envía el menú al cliente
                                            generosMenu(tam, out);
                                            //Obtiene el id del género
                                            String opc2 = in.readLine();
                                            //Array para guardar los libros con ese género
                                            ArrayList generoLibros = new ArrayList<String>();
                                            //Obtiene los libros con ese género
                                            generoLibros = filtroMenuGeneros(opc2, out);
                                            //Cantidad de libros de ese autor
                                            int tam2 = generoLibros.size();
                                            out.println(Integer.toString(tam2));
                                            //Manda las opciones al cliente
                                            for(int i = 0; i < tam2; i++)
                                            {
                                                out.println((i+1)+". "+generoLibros.get(i));
                                            }
                                            //Obtiene la opción del cliente
                                            int opc3 = Integer.parseInt(in.readLine());
                                            /*
                                                Obtiene el nombre del libro con ese genero
                                                en base a la opción del cliente
                                            */
                                            String libro = (String) generoLibros.get(opc3 - 1);
                                            //Array para guardar el ip y el puerto del SD
                                            String[] address = new String[3];
                                            //
                                            ArrayList<String> servidoresLibro = new ArrayList<>();
                                            servidoresLibro = servidoresBalanceo2(libro);
                                            if(servidoresLibro != null)
                                            {
                                                String servidorDisp = servidoresDisponible(servidoresLibro);
                                                //Obtiene el ip y el puerto del SD
                                                address = direccionServer3(opc2, libro, servidorDisp);
                                                //Le envía al cliente el ip y el puerto
                                                if(address != null)
                                                {
                                                    for(int i = 0; i < address.length; i++)
                                                    {
                                                        out.println(address[i]);
                                                    }
                                                }
                                                else
                                                {
                                                    System.out.println("Error, meh...");
                                                }
                                            }
                                        }
                                        else
                                        {
                                            out.println("s");
                                            out.println("No hay libros registrados");
                                        }
                                    }
                                    else if(opc.equalsIgnoreCase("4"))
                                    {
                                        out.println("4");
                                        out.flush();
                                    }
                                    else if(opc.equalsIgnoreCase("5"))
                                    {
                                        out.println("5");
                                        out.flush();
                                    }
                                    else if(opc.equalsIgnoreCase("0"))
                                    {
                                        out.println("0");
                                        out.println("Has cerrado sesión");
                                        out.flush();
                                        session = false;
                                    }
                                    else
                                    {
                                        out.println("x");
                                        out.println("Comando inválido, intente de nuevo");
                                        out.flush();
                                    }
                                }
                            }
                            /*Si no tiene una cuenta*/
                            else
                            {
                                /*Indica que no tiene cuenta*/
                                out.println("false");
                                out.println("Combinación invalida, intente de nuevo");
                                out.flush();
                                //Intentos para iniciar sesión
                                /*
                                for(int i = 0; i < 3; i++)
                                {
                                    
                                }
                                */
                            }
                        }
                        /*Inscripción del cliente*/
                        else if(mensaje.equalsIgnoreCase("INSCRIPCION"))
                        {
                            out.println("Ingrese su nombre de usuario: ");
                            user = in.readLine();
                            out.println("Ingrese su contraseña: ");
                            password = in.readLine();
                            /*Función que crea una cuenta en el xml
                                para el inicio de sesión
                            */
                            inscrito = inscripcionCliente(user,password);
                            /*Si el nombre de usuario no está ocupado*/
                            if(inscrito)
                            {
                                out.println("Ud se ha inscrito satisfactoriamente");
                            }
                            /*Si ya existe*/
                            else
                            {
                                out.println("El usuario ya existe intente de nuevo");
                            }
                        }
                        /*Para salir y cerrar los canales y el socket
                            y salir del bucle infinito
                        */
                        else if(mensaje.equalsIgnoreCase("SALIR"))
                        {
                            out.println("Adiós");
                            out.flush();
                            out.close();
                            in.close();
                            socket.close();
                            connect = false;
                        }
                        /*Si el comando no existe*/
                        else
                        {
                            out.println("Comando inválido");
                            out.flush();
                        }
                    }
                    System.out.println("Se cerró la conexión");
                    System.out.println("");
                }
                /*Si se conecta un servidor de descarga*/
                else if(mensaje.equalsIgnoreCase("s"))
                {
                    //Agarra los valores del ip y puerto del servidor de descarga
                    ipServDescarga = in.readLine();
                    ptoServDescarga = in.readLine();
                    
                    //Obtiene el id del SD
                    String idSd = in.readLine();
                    
                    /*Agrega el id, el ip y el puerto del servidor al xml
                        que guarda los datos del servidor de descarga
                    */
                    agregarServidorDescarga(ipServDescarga, ptoServDescarga, idSd);
                    
                    //Obtiene la cantidad de libros almancenados en el SD
                    int size = Integer.parseInt(in.readLine());
                    
                    agregarLibrosSC(ipServDescarga, ptoServDescarga, idSd, size, in, out);
                    
                    socket.close();
                }
                else if(mensaje.equalsIgnoreCase("as"))
                {
                    //Lee la opcion de actualizacion del servidor de descarga
                    String accion = in.readLine();
                    if(accion.equalsIgnoreCase("descargas"))
                    {
                        String ip_sd = in.readLine();
                        String port_sd = in.readLine();
                        String num_descargas = in.readLine();
                        String libro = in.readLine();
                        actualizarDescargasSd(ip_sd, port_sd, num_descargas);
                        actualizarDescargasLibro(ip_sd, port_sd, libro);
                    }
                    else if(accion.equalsIgnoreCase("clientes"))
                    {
                        String ip_sd = in.readLine();
                        String port_sd = in.readLine();
                        String conectados = in.readLine();
                        actualizarClientesServidor(ip_sd, port_sd, conectados);
                    }
                    else if(accion.equalsIgnoreCase("cerrar"))
                    {
                        reconexion = true;
                        String ip_sd = in.readLine();
                        String port_sd = in.readLine();
                        eliminarServidorDescarga(ip_sd, port_sd);
                        int cantidadLibros = Integer.parseInt(in.readLine());
                        for(int i = 0; i < cantidadLibros; i++)
                        {
                            eliminarLibrosSD(ip_sd, port_sd);
                        }
                        libros = cantidadLibros();
                        idautores = cantAutoresUnicos();
                        idgeneros = cantGenerosUnicos();
                        ordenarLibrosId();
                        ordenarAutores();
                        ordenarGeneros();
                        if(libros == 0)
                        {
                            libros = 1;
                            reconexion = false;
                        }
                        if(idautores == 0)
                        {
                            idautores = 1;
                            reconexion = false;
                        }
                        if(idgeneros == 0)
                        {
                            idgeneros = 1;
                            reconexion = false;
                        }
                    }
                }
            //}     
        }
        /*Colocar las excepciones con los mensajes correspondientes*/
        catch(SocketException e)
        {
            System.err.println("Se cerró inesperadamente la conexión con el cliente");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //Inscribe al usuario en el xml de clientes
    private boolean inscripcionCliente(String login, String pw)
    {
        try
        {
            /*Esta clase sirve para poder acceder al archivo xml para lectura*/
            SAXBuilder builder = new SAXBuilder();
            
            /*Se crea un objeto de tipo doc para obtener el archivo xml*/
            Document doc = null;
            doc = builder.build("./src/servidorcentral/clienteXML.xml");
            
            /*Obtiene el elemento raíz del xml*/
            Element rootDoc = doc.getRootElement();

            /*Crea los elementos nuevos a insertar
                Estos son las etiquetas y deben 
                coincidir con lo que está en el xml
            */
            Element cliente = new Element("Cliente");

            Element lg = new Element("username");
            /*Añade el nombre del login al elemento*/
            lg.addContent(new Text(login));

            Element ct = new Element("password");
            /*Añade el password al elemento*/
            ct.addContent(new Text(pw));

            /*Añade los valores en login y password
                al elemento cliente que corresponde
                a la etiqueta en el xml con el mismo
                nombre
            */
            cliente.addContent(lg);
            cliente.addContent(ct);
            
            //Revisa si el cliente ya existe
            for(Element ele : rootDoc.getChildren("Cliente"))
            {
                /*Obtiene el texto en la etiqueta username*/
                String usuario = ele.getChildText("username");
                /*Si ya existe regresa false*/
                if(usuario.equalsIgnoreCase(login))
                {
                    return false;
                }
            }
            
            //En caso de que no exista lo inserta
            rootDoc.addContent(cliente);
            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream file = new FileOutputStream("./src/servidorcentral/clienteXML.xml");
            xmlOutput.output(rootDoc, file);
            file.flush();
            file.close();
            return true;
        }
        /*Colocar las excepciones con los mensajes correspondientes*/
        catch(Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
    
    //Valida el inicio de sesión del cliente
    private boolean validarInicioSesion(String login, String pw)
    {
        SAXBuilder builder = new SAXBuilder();
        Document readDoc = null;
        try
        {
            readDoc = builder.build("./src/servidorcentral/clienteXML.xml");
        }   
        /*Colocar las excepciones con los mensajes correspondientes*/
        catch(JDOMException | IOException e)
        {
            e.printStackTrace();
            return false;
        }
        
        Element root = readDoc.getRootElement();
        /* Lee la cantidad de clientes que hay en el xml
        System.out.println(root.getChildren("Cliente").size());
        */
        for(Element ele : root.getChildren("Cliente"))
        {
            String usuario = ele.getChildText("username");
            String pass = ele.getChildText("password");
            if(usuario.equals(login) && pass.equals(pw))
            {
                return true;
            }
        }
        return false;
    }
    
    //Agrega el servidor de descarga que realiza la conexión con el SC
    private boolean agregarServidorDescarga(String ip, String port, String idSd)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        try {
            doc = builder.build(servidoresDescarga);
        } catch (JDOMException ex) {
            System.err.println(ex);
            return false;
        } catch (IOException ex) {
            System.err.println(ex);
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        Element servidor = new Element("Server");
        servidor.setAttribute("id", idSd);
        
        Element address = new Element("ip");
        address.addContent(new Text(ip));
        
        Element puerto = new Element("port");
        puerto.addContent(new Text(port));
        
        Element cantidad = new Element("connected");
        cantidad.addContent(new Text("0"));
        
        Element downloads = new Element("downloads");
        downloads.addContent(new Text("0"));
        
        servidor.addContent(address);
        servidor.addContent(puerto);
        servidor.addContent(cantidad);
        servidor.addContent(downloads);
        
        for(Element ele : rootDoc.getChildren("Server"))
        {
            String address2 = ele.getChildText("ip");
            String puerto2 = ele.getChildText("port");
            if(address2.equalsIgnoreCase(ip) && puerto2.equalsIgnoreCase(port))
            {
                return false;
            }
        }
        
        rootDoc.addContent(servidor);
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream file;
        try
        {
            file = new FileOutputStream("./src/servidorcentral/servidoresDescargaXML.xml");
            xmlOutput.output(rootDoc, file);
            file.flush();
            file.close();
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
        return true;
    }
    
    //Agrega los libros que pertenecen al SD que se está conectando
    //En caso de ya existir el libro simplemente añade el ip y el puerto del servidor
    private void agregarLibrosSC(String ip, String puerto, String id, 
                        int size, BufferedReader br, PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            System.err.println(ex);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        
        Element rootDoc = doc.getRootElement();
        
        //Construye las etiquetas con sus datos para insertarlos en el xml
        for(int i = 0; i < size; i++)
        {
            //Crea la etiqueta para cada libro
            Element book = new Element("Book");
            //Inserta la etiqueta del libro
            Element bookname = new Element("bookname");
            String name;
            boolean existe = false;
            try {
                name = br.readLine();
                bookname.addContent(name);
                for(Element ele : rootDoc.getChildren("Book"))
                {
                    String nombrelibro = ele.getChildText("bookname");
                    //Verifica si el libro ya está en la BD
                    if(nombrelibro.equalsIgnoreCase(name))
                    {
                        existe = true;
                        break;
                    }
                    else
                    {
                        existe = false;
                    }
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //Si el libro no está guardado en el SC
            if(!existe)
            {
                pw.println("false");
                Element address = new Element("address");

                Element server = new Element("server");
                server.setAttribute("id", id);
                
                Element ipbook = new Element("ip");
                ipbook.addContent(new Text(ip));
                Element portbook = new Element("port");
                portbook.addContent(new Text(puerto));
                Element downloads = new Element("num_descargas");
                downloads.addContent(new Text("0"));

                int authors, genres;
                Element[] autores, generos;

                try {
                    server.addContent(ipbook);
                    server.addContent(portbook);
                    server.addContent(downloads);
                    address.addContent(server);

                    //Cantidad de autores del libro
                    authors = Integer.parseInt(br.readLine());
                    //Array para añadir los distintos autores del libro
                    autores = new Element[authors];
                    //Crea la etiqueta raíz de los autores del libro
                    Element authorsbook = new Element("authors");                
                    //Ciclo para insertar dinámicamente los autores
                    for(int i2 = 0; i2 < authors; i2++)
                    {
                        //Crea la etiqueta para uno de los autores del libro
                        autores[i2] = new Element("author");
                        //Crea la etiqueta para el nombre del autor
                        Element authorname = new Element("authorname");
                        //Lee el nombre del autor del SD y lo añade
                        String auth = br.readLine();
                        authorname.addContent(new Text(auth));
                        
                        boolean existe2 = false;
                        String ident = null;
                        autorLoop:
                        for(Element ids : rootDoc.getChildren("Book"))
                        {
                            Element idsautores = ids.getChild("authors");
                            for(Element ids2 : idsautores.getChildren("author"))
                            {
                                String idsautoresnombre = ids2.getChildText("authorname");
                                if(idsautoresnombre.equalsIgnoreCase(auth))
                                {
                                    existe2 = true;
                                    ident = ids2.getAttribute("id").getValue();
                                    break autorLoop;
                                }
                                else
                                {
                                    existe2 = false;
                                }
                            }
                        }
                        if(!existe2)
                        {
                            if(reconexion)
                            {
                                idautores++;
                                autores[i2].setAttribute("id", Integer.toString(idautores));
                            }
                            else
                            {
                                autores[i2].setAttribute("id", Integer.toString(idautores));
                                idautores++;
                            }
                        }
                        else if(existe2)
                        {
                            autores[i2].setAttribute("id",ident);
                        }
                        //Inserta el nombre de autor en su etiqueta
                        autores[i2].addContent(authorname);
                        authorsbook.addContent(autores[i2]);
                    }

                    //Cantidad de generos del libro
                    genres = Integer.parseInt(br.readLine());
                    //Array para añadir los distintos generos del libro
                    generos = new Element[genres];
                    //Crea la etiqueta raíz de los géneros del libro
                    Element genresbook = new Element("genres");
                    //Ciclo para insertar dinámicamente los géneros
                    for(int i2 = 0; i2 < genres; i2++)
                    {
                        generos[i2] = new Element("genre");

                        Element genrename = new Element("genrename");

                        String gene = br.readLine();
                        genrename.addContent(new Text(gene));
                        
                        boolean existe2 = false;
                        String ident = null;
                        generoLoop:
                        for(Element ids : rootDoc.getChildren("Book"))
                        {
                            
                            Element idsgeneros = ids.getChild("genres");
                            for(Element ids2 : idsgeneros.getChildren("genre"))
                            {
                                String idsgenerosnombre = ids2.getChildText("genrename");
                                if(idsgenerosnombre.equalsIgnoreCase(gene))
                                {
                                    existe2 = true;
                                    ident = ids2.getAttribute("id").getValue();
                                    break generoLoop;
                                }
                            }
                        }
                        if(!existe2)
                        {
                            if(reconexion)
                            {
                                idgeneros++;
                                generos[i2].setAttribute("id", Integer.toString(idgeneros));
                            }
                            else
                            {
                                generos[i2].setAttribute("id", Integer.toString(idgeneros));
                                idgeneros++;
                            }
                        }
                        else
                        {
                            generos[i2].setAttribute("id",ident);
                        }

                        generos[i2].addContent(genrename);
                        genresbook.addContent(generos[i2]);
                    }
                    
                    if(reconexion)
                    {
                        libros++;
                        book.setAttribute("id", Integer.toString(libros));
                    }
                    else
                    {
                        book.setAttribute("id", Integer.toString(libros));    
                        libros++;
                    }
                    book.addContent(bookname);
                    book.addContent(address);
                    book.addContent(authorsbook);
                    book.addContent(genresbook);

                    rootDoc.addContent(book);
                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    FileOutputStream file;
                    try
                    {
                        file = new FileOutputStream("./src/servidorcentral/catalogoLibrosXML.xml");
                        xmlOutput.output(rootDoc, file);
                        file.flush();
                        file.close();
                    }
                    catch (FileNotFoundException ex)
                    {
                        System.err.println(ex);
                    }
                    catch (IOException ex)
                    {
                        System.err.println(ex);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            //Si existe el libro en el SC
            else
            {
                pw.println("true");
                try {
                    String name2 = br.readLine();
                    insertLoop:
                    for (Element ele : rootDoc.getChildren("Book"))
                    {
                        String nombrelibro = ele.getChildText("bookname");
                        if(nombrelibro.equalsIgnoreCase(name2))
                        {
                            Element address = ele.getChild("address");
                            
                            Element server = new Element("server");
                            server.setAttribute("id", id);
                            
                            Element ipbook = new Element("ip");
                            ipbook.addContent(new Text(ip));
                            Element portbook = new Element("port");
                            portbook.addContent(new Text(puerto));
                            Element downloads = new Element("num_descargas");
                            downloads.addContent(new Text("0"));
                            
                            for(Element ele2 : address.getChildren("server"))
                            {
                                String dir1 = ele2.getChildText("ip");
                                String dir2 = ele2.getChildText("port");
                                if(dir1.equalsIgnoreCase(ip) && dir2.equalsIgnoreCase(puerto))
                                {
                                    //No hace nada
                                    break insertLoop;
                                }
                            }
                            server.addContent(ipbook);
                            server.addContent(portbook);
                            server.addContent(downloads);
                            address.addContent(server);

                            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                            FileOutputStream file;
                            try
                            {
                                file = new FileOutputStream("./src/servidorcentral/catalogoLibrosXML.xml");
                                xmlOutput.output(rootDoc, file);
                                file.flush();
                                file.close();
                                break insertLoop;
                            }
                            catch (FileNotFoundException ex)
                            {
                                System.err.println(ex);
                                break insertLoop;
                            }
                            catch (IOException ex)
                            {
                                System.err.println(ex);
                                break insertLoop;
                            }
                        }
                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        }
    }
    
    //Envía el catálogo general de libros al cliente
    private void catalogoMenu(PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element readDoc = doc.getRootElement();
        
        int tam = readDoc.getChildren("Book").size();
        pw.println(Integer.toString(tam));
        pw.println("Los libros en el sistema son:");
        
        for(Element ele : readDoc.getChildren("Book"))
        {
            pw.println(ele.getAttribute("id").getValue()+".");
            pw.println("Nombre: " + ele.getChildText("bookname"));
            
            Element autor = ele.getChild("authors");
            pw.println("Autor(es):");
            tam = autor.getChildren("author").size();
            pw.println(Integer.toString(tam));
            for(Element ele2 : autor.getChildren("author"))
            {
                pw.println(ele2.getChildText("authorname"));
            }
            
            Element genero = ele.getChild("genres");
            pw.println("Género(s):");
            tam = genero.getChildren("genre").size();
            pw.println(Integer.toString(tam));
            for(Element ele2 : genero.getChildren("genre"))
            {
                pw.println(ele2.getChildText("genrename"));
            }
        }
    }
    
    //Cuenta la cantidad de autores únicos
    private int cantAutoresUnicos()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return 0;
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
        
        Element rootDoc = doc.getRootElement();
        //Crear array de autores
        ArrayList<String> autores = new ArrayList<String>();
        //Contador para saber cuantos autores únicos hay
        int cantidad = 0;
        
        for (Element ele : rootDoc.getChildren("Book"))
        {
            //Boolean que indica si el arraylist ya tiene ese autor
            boolean existe = false;
            Element authors = ele.getChild("authors");
            //Variable que obtiene el nombre del autor
            String authorname = null;
            for(Element ele2 : authors.getChildren("author"))
            {
                authorname = ele2.getChildText("authorname");
                for(int i = 0; i < autores.size(); i++)
                {
                    if(authorname.equalsIgnoreCase(autores.get(i)))
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
                    autores.add(authorname);
                    cantidad++;
                }
            }
        }
        return cantidad;
    }
    
    //Primer menu de autores
    private void autoresMenu(int tam, PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(int i = 1; i <= tam; i++)
        {
            librosLoop:
            for(Element ele : rootDoc.getChildren("Book"))
            {
                Element autor = ele.getChild("authors");
                for(Element ele2 : autor.getChildren("author"))
                {
                    String idautor = ele2.getAttribute("id").getValue();
                    if(idautor.equalsIgnoreCase(Integer.toString(i)))
                    {
                        pw.println(idautor+". "+ele2.getChildText("authorname"));
                        break librosLoop;
                    }
                }
            }
        }
    }
    
    //Filtra los autores en base al id del autor seleccionado por el cliente
    private ArrayList<String> filtroMenuAutores(String idAutor, PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        Element rootDoc = doc.getRootElement();
        
        ArrayList autorLibros = new ArrayList<String>();
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            Element authors = ele.getChild("authors");
            for(Element ele2 : authors.getChildren("author"))
            {
                String idAuthor = ele2.getAttribute("id").getValue();
                if(idAuthor.equalsIgnoreCase(idAutor))
                {
                    autorLibros.add(ele.getChildText("bookname"));
                }
            }
        }
        autorLoop:
        for(Element ele : rootDoc.getChildren("Book"))
        {
            Element authors = ele.getChild("authors");
            for(Element ele2 : authors.getChildren("author"))
            {
                String idAuthor = ele2.getAttribute("id").getValue();
                if(idAuthor.equalsIgnoreCase(idAutor))
                {
                    pw.println(ele2.getChildText("authorname"));
                    break autorLoop;
                }
            }
        }
        if(autorLibros.size() > 0)
        {
            return autorLibros;
        }
        else
        {
            return null;
        }
    }
    
    //Filtra los géneros en base al id del género seleccionado por el cliente
    private ArrayList<String> filtroMenuGeneros(String idGenero, PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        Element rootDoc = doc.getRootElement();
        
        ArrayList generoLibros = new ArrayList<String>();
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            Element generos = ele.getChild("genres");
            for(Element ele2 : generos.getChildren("genre"))
            {
                String idGenre = ele2.getAttribute("id").getValue();
                if(idGenre.equalsIgnoreCase(idGenero))
                {
                    generoLibros.add(ele.getChildText("bookname"));
                }
            }
        }
        generoLoop:
        for(Element ele : rootDoc.getChildren("Book"))
        {
            Element genres = ele.getChild("genres");
            for(Element ele2 : genres.getChildren("genre"))
            {
                String idGenres = ele2.getAttribute("id").getValue();
                if(idGenres.equalsIgnoreCase(idGenero))
                {
                    pw.println(ele2.getChildText("genrename"));
                    break generoLoop;
                }
            }
        }
        if(generoLibros.size() > 0)
        {
            return generoLibros;
        }
        else
        {
            return null;
        }
    }
    
    //Cuenta la cantidad de géneros únicos
    private int cantGenerosUnicos()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return 0;
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
        
        Element rootDoc = doc.getRootElement();
        //Crear array de autores
        ArrayList<String> generos = new ArrayList<String>();
        //Contador para saber cuantos autores únicos hay
        int cantidad = 0;
        
        for (Element ele : rootDoc.getChildren("Book"))
        {
            //Boolean que indica si el arraylist ya tiene ese genero
            boolean existe = false;
            Element genres = ele.getChild("genres");
            //Variable que obtiene el nombre del genero
            String genrename = null;
            for(Element ele2 : genres.getChildren("genre"))
            {
                genrename = ele2.getChildText("genrename");
                for(int i = 0; i < generos.size(); i++)
                {
                    if(genrename.equalsIgnoreCase(generos.get(i)))
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
                    generos.add(genrename);
                    cantidad++;
                }
            }
        }
        return cantidad;
    }
    
    //Primer menu de generos
    private void generosMenu(int tam, PrintWriter pw)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(int i = 1; i <= tam; i++)
        {
            librosLoop:
            for(Element ele : rootDoc.getChildren("Book"))
            {
                Element autor = ele.getChild("genres");
                for(Element ele2 : autor.getChildren("genre"))
                {
                    String idautor = ele2.getAttribute("id").getValue();
                    if(idautor.equalsIgnoreCase(Integer.toString(i)))
                    {
                        pw.println(idautor+". "+ele2.getChildText("genrename"));
                        break librosLoop;
                    }
                }
            }
        }
    }
    
    //Me devuelve el servidor y el libro que el cliente va a descargar
    //Este método es para el catálogo de libros completo
    private String[] direccionServer(String idLibro, String servidor)
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        Element rootDoc = doc.getRootElement();
        
        String[] direccion = new String[3];
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            String attBook = ele.getAttribute("id").getValue();
            if(attBook.equalsIgnoreCase(idLibro))
            {
                Element addressTag = ele.getChild("address");
                //addressLoop:
                for(Element ele2 : addressTag.getChildren("server"))
                {
                    if(ele2.getAttribute("id").getValue().equalsIgnoreCase(servidor))
                    {
                        direccion[0] = ele2.getChildText("ip");
                        direccion[1] = ele2.getChildText("port");
                        direccion[2] = ele.getChildText("bookname");
                        return direccion;
                    }
                }
            }
        }
        
        return null;
    }
    
    //Lo mismo que direccionServer, solo que este sirve para el menú de autores
    private String[] direccionServer2(String idAutor, String nombreLibro, String servidorD)
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        Element rootDoc = doc.getRootElement();
        
        String[] direccion = new String[3];
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            if(ele.getChildText("bookname").equalsIgnoreCase(nombreLibro))
            {
                Element authorsTag = ele.getChild("authors");
                for(Element ele2 : authorsTag.getChildren("author"))
                {
                    if(ele2.getAttribute("id").getValue().equalsIgnoreCase(idAutor))
                    {
                        Element addressTag = ele.getChild("address");
                        for(Element ele3 : addressTag.getChildren("server"))
                        {
                            if(ele3.getAttribute("id").getValue().equalsIgnoreCase(servidorD))
                            {
                                direccion[0] = ele3.getChildText("ip");
                                direccion[1] = ele3.getChildText("port");
                                direccion[2] = ele.getChildText("bookname");
                                return direccion;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    //Lo mismo que direccionServer, solo que este sirve para el menú de géneros
    private String[] direccionServer3(String idGenero, String nombreLibro, String servidorD)
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        Element rootDoc = doc.getRootElement();
        
        String[] direccion = new String[3];
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            if(ele.getChildText("bookname").equalsIgnoreCase(nombreLibro))
            {
                Element genresTag = ele.getChild("genres");
                for(Element ele2 : genresTag.getChildren("genre"))
                {
                    if(ele2.getAttribute("id").getValue().equalsIgnoreCase(idGenero))
                    {
                        Element addressTag = ele.getChild("address");
                        for(Element ele3 : addressTag.getChildren("server"))
                        {
                            if(ele3.getAttribute("id").getValue().equalsIgnoreCase(servidorD))
                            {
                                direccion[0] = ele3.getChildText("ip");
                                direccion[1] = ele3.getChildText("port");
                                direccion[2] = ele.getChildText("bookname");
                                return direccion;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    //Actualiza los clientes conectados de un servidor
    private boolean actualizarClientesServidor(String ipsd, String puerto, String connected)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(servidoresDescarga);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Server"))
        {
            String ip = ele.getChildText("ip");
            String port = ele.getChildText("port");
            if(ip.equalsIgnoreCase(ipsd) && port.equalsIgnoreCase(puerto))
            {
                ele.getChild("connected").setText(connected);
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                FileOutputStream file;
                try
                {
                    file = new FileOutputStream("./src/servidorcentral/servidoresDescargaXML.xml");
                    xmlOutput.output(rootDoc, file);
                    file.flush();
                    file.close();
                    break;
                }
                catch (FileNotFoundException ex)
                {
                    System.err.println(ex);
                    break;
                }
                catch (IOException ex)
                {
                    System.err.println(ex);
                    break;
                }
                
            }
        }
        
        return false;
    }
    
    //Actualiza la cantidad de descargas totales actuales de un servidor
    private void actualizarDescargasSd(String ip, String puerto, String descargas)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(servidoresDescarga);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Server"))
        {
            String servidor_ip = ele.getChildText("ip");
            String servidor_port = ele.getChildText("port");
            if(servidor_ip.equalsIgnoreCase(ip) && servidor_port.equalsIgnoreCase(puerto))
            {
                ele.getChild("downloads").setText(descargas);
                
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                FileOutputStream file;
                try
                {
                    file = new FileOutputStream("./src/servidorcentral/servidoresDescargaXML.xml");
                    xmlOutput.output(rootDoc, file);
                    file.flush();
                    file.close();
                    break;
                }
                catch (FileNotFoundException ex)
                {
                    System.err.println(ex);
                    break;
                }
                catch (IOException ex)
                {
                    System.err.println(ex);
                    break;
                }
                
            }
        }
    }
    
    //Actualiza la cantidad de descargas de un libro con respecto a un servidor
    private boolean actualizarDescargasLibro(String ip, String puerto, String libroDescargado)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        Element rootDoc = doc.getRootElement();
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            String nombre = ele.getChildText("bookname");
            if(nombre.equalsIgnoreCase(libroDescargado))
            {
                Element direccion = ele.getChild("address");
                for(Element ele2 : direccion.getChildren("server"))
                {
                    String ip2 = ele2.getChildText("ip");
                    String port = ele2.getChildText("port");
                    if(ip2.equalsIgnoreCase(ip) && port.equalsIgnoreCase(puerto))
                    {
                        int num_descargas = Integer.parseInt(ele2.getChildText("num_descargas"));
                        num_descargas++;
                        String num_string = Integer.toString(num_descargas);
                        ele2.getChild("num_descargas").setText(num_string);
                        
                        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                        FileOutputStream file;
                        try
                        {
                            file = new FileOutputStream(catalogoLibros);
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
        }
        
        return false;
    }
    
    //Verifica que el SC tiene SD registrados, si no tiene, devuelve un valor
    //para indicarle al cliente que no hay libros disponibles
    private boolean servidoresRegistrados()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(servidoresDescarga);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        //Si tiene al menos un servidor
        if(rootDoc.getChildren("Server").size() > 0)
        {
            return false;
        }
        //Si está vacío
        else
        {
            return true;
        }
    }
    
    //Me devuelve un array que tiene el id de los servidores que tiene el libro
    //que pidió el cliente. Se usa sólo para el catálogo completo
    private ArrayList<String> servidoresBalanceo(String opc2)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        ArrayList<String> servidores = new ArrayList<>();
        
        for(Element ele : rootDoc.getChildren("Book"))
        {
            if(ele.getAttribute("id").getValue().equalsIgnoreCase(opc2))
            {
                Element address = ele.getChild("address");
                for(Element ele2 : address.getChildren("server"))
                {
                    servidores.add(ele2.getAttribute("id").getValue());
                }
            }
        }
        
        if(servidores.size() > 0)
        {
            return servidores;
        }
        else
        {
            return null;
        }
    }
    
    //Lo mismo que servidoresBalanceo, sólo que este se usa en el caso del cátalogo
    //filtrado por autores y género
    private ArrayList<String> servidoresBalanceo2(String opc3)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(catalogoLibros);
            
            Element rootDoc = doc.getRootElement();
            
            ArrayList<String> servidores = new ArrayList<>();
            
            for(Element ele : rootDoc.getChildren("Book"))
            {
                if(ele.getChildText("bookname").equalsIgnoreCase(opc3))
                {
                    Element address = ele.getChild("address");
                    for(Element ele2 : address.getChildren("server"))
                    {
                        servidores.add(ele2.getAttribute("id").getValue());
                    }
                }
            }
            if(servidores.size() > 0)
            {
                return servidores;
            }
            else
            {
                return null;
            }
        } 
        catch (JDOMException ex) 
        {
            ex.printStackTrace();
            return null;
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            return null;
        }
    }
    
    //Me devuelve el servidor con menos personas conectadas con respecto a un libro
    //que se quiere descargar
    private String servidoresDisponible(ArrayList<String> servidores)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(servidoresDescarga);
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Element rootDoc = doc.getRootElement();
        
        //Valor minimo de personas conectadas
        int min_value = 0;
        //Boolean para saber si encontró el mínimo entre los servidores del libro
        boolean have_min = false;
        //Hasta que no encuentre el mínimo
        while(!have_min)
        {
            connectLoop:
            //Recorre el xml de servidores de descarga
            for(Element ele : rootDoc.getChildren("Server"))
            {
                //Por cada servidor compara con los que tienen el libro
                for(int i = 0; i < servidores.size(); i++)
                {
                    //Si encuentra uno de los servidores que tienen el libro
                    if(ele.getAttribute("id").getValue().equalsIgnoreCase(servidores.get(i)))
                    {
                        //Obtiene las personas conectadas de ese servidor
                        int connected = Integer.parseInt(ele.getChildText("connected"));
                        //Si ese servidor tiene el mínimo lo establece para balancear luego
                        if(connected == min_value)
                        {
                            have_min = true;
                            break connectLoop;
                        }
                    }
                }
            }
            //Si ninguno tiene el valor minimo de personas establecido antes le suma 1
            //a ver si con ese consigue el mínimo real
            if(!have_min)
            {
                min_value++;
            }
        }
        
        String min_value2 = Integer.toString(min_value);
        
        //Se vuelve a hacer lo mismo pero en base al mínimo de personas conectadas
        //me devuelve el id de uno de los servidores que tenga el libro
        for(Element ele : rootDoc.getChildren("Server"))
        {
            for(int i = 0; i < servidores.size(); i++)
            {
                if(ele.getAttribute("id").getValue().equalsIgnoreCase(servidores.get(i)))
                {
                    if(ele.getChildText("connected").equalsIgnoreCase(min_value2))
                    {
                        return ele.getAttribute("id").getValue();
                    }
                }
            }
        }
        
        return null;
    }
    
    //Quita al SD una vez se desconecta
    private void eliminarServidorDescarga(String ip, String port)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(servidoresDescarga);
            
            Element rootDoc = doc.getRootElement();
            
            for(Element ele : rootDoc.getChildren("Server"))
            {
                String dir = ele.getChildText("ip");
                String puerto = ele.getChildText("port");
                if(dir.equalsIgnoreCase(ip) && puerto.equalsIgnoreCase(port))
                {
                    ele.detach();
                    XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                    FileOutputStream file;
                    try
                    {
                        file = new FileOutputStream(servidoresDescarga);
                        xmlOutput.output(rootDoc, file);
                        file.flush();
                        file.close();
                        return;
                    }
                    catch (FileNotFoundException ex)
                    {
                        System.err.println(ex);
                        return;
                    }
                    catch (IOException ex)
                    {
                        System.err.println(ex);
                        return;
                    }
                }
            }
        } catch (JDOMException ex) {
            //
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    //Quita los libros del SD que se desconectó
    private void eliminarLibrosSD(String ip, String port)
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(catalogoLibros);
            
            Element rootDoc = doc.getRootElement();
            
            for(Element ele : rootDoc.getChildren("Book"))
            {
                Element address = ele.getChild("address");
                for(Element ele2 : address.getChildren("server"))
                {
                    String dir = ele2.getChildText("ip");
                    String puerto = ele2.getChildText("port");
                    if(dir.equalsIgnoreCase(ip) && puerto.equalsIgnoreCase(port))
                    {
                        ele2.detach();
                        if(address.getChildren("server").isEmpty())
                        {
                            ele.detach();
                        }
                        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                        FileOutputStream file;
                        try
                        {
                            file = new FileOutputStream(catalogoLibros);
                            xmlOutput.output(rootDoc, file);
                            file.flush();
                            file.close();
                            return;
                        }
                        catch (FileNotFoundException ex)
                        {
                            System.err.println(ex);
                            return;
                        }
                        catch (IOException ex)
                        {
                            System.err.println(ex);
                            return;
                        }
                    }
                }
            }
            
        } catch (JDOMException ex) {
            //
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private int cantidadLibros()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try {
            doc = builder.build(catalogoLibros);
        } catch (JDOMException ex) {
            ex.printStackTrace();
            return 0;
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
        
        Element rootDoc = doc.getRootElement();
        //Crear array de autores
        ArrayList<String> autores = new ArrayList<String>();
        //Contador para saber cuantos autores únicos hay
        int cantidad = 0;
        
        for (Element ele : rootDoc.getChildren("Book"))
        {
            cantidad++;
        }
        return cantidad;
    }

    //Verifica si hay un libro sin servidores y lo elimina
    private void ordenarLibrosId()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(catalogoLibros);
            
            Element rootDoc = doc.getRootElement();
            
            if(!rootDoc.getChildren("Book").isEmpty())
            {
                List<Element> books = rootDoc.getChildren("Book");
                for(int i = 1; i <= libros; i++)
                {
                    books.get(i - 1).setAttribute("id", Integer.toString(i));
                }
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                FileOutputStream file;
                try
                {
                    file = new FileOutputStream(catalogoLibros);
                    xmlOutput.output(rootDoc, file);
                    file.flush();
                    file.close();
                    return;
                }
                catch (FileNotFoundException ex)
                {
                    System.err.println(ex);
                    return;
                }
                catch (IOException ex)
                {
                    System.err.println(ex);
                    return;
                }
            }
        } catch (JDOMException ex) {
            //
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void ordenarAutores()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(catalogoLibros);
            
            Element rootDoc = doc.getRootElement();
            
            if(!rootDoc.getChildren("Book").isEmpty())
            {
                ArrayList<String> autores = new ArrayList<>();
                String author = null;
                for(int i = 1; i <= idautores; i++)
                {
                    boolean existe = false;
                    organizarLoop:
                    for(Element ele : rootDoc.getChildren("Book"))
                    {
                        Element authors = ele.getChild("authors");
                        for(Element ele2 : authors.getChildren("author"))
                        {
                            author = ele2.getChildText("authorname");
                            for(int j = 0; j < autores.size(); j++)
                            {
                                if(author.equalsIgnoreCase(autores.get(j)))
                                {
                                   existe = true;
                                   break;
                                }
                            }
                            if(!existe)
                            {
                                break organizarLoop;
                            }
                            existe = false;
                        }
                    }
                    for(Element ele : rootDoc.getChildren("Book"))
                    {
                        Element authors = ele.getChild("authors");
                        for(Element ele2 : authors.getChildren("author"))
                        {
                            if(ele2.getChildText("authorname").equalsIgnoreCase(author))
                            {
                                ele2.setAttribute("id", Integer.toString(i));
                                autores.add(author);
                            }
                        }
                    }
                }
                
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                FileOutputStream file;
                try
                {
                    file = new FileOutputStream(catalogoLibros);
                    xmlOutput.output(rootDoc, file);
                    file.flush();
                    file.close();
                    return;
                }
                catch (FileNotFoundException ex)
                {
                    System.err.println(ex);
                    return;
                }
                catch (IOException ex)
                {
                    System.err.println(ex);
                    return;
                }
            }
        } catch (JDOMException ex) {
            //
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void ordenarGeneros()
    {
        SAXBuilder builder = new SAXBuilder();
        
        Document doc = null;
        
        try
        {
            doc = builder.build(catalogoLibros);
            
            Element rootDoc = doc.getRootElement();
            
            if(!rootDoc.getChildren("Book").isEmpty())
            {
                ArrayList<String> generos = new ArrayList<>();
                String genre = null;
                for(int i = 1; i <= idgeneros; i++)
                {
                    boolean existe = false;
                    organizarLoop:
                    for(Element ele : rootDoc.getChildren("Book"))
                    {
                        Element authors = ele.getChild("genres");
                        for(Element ele2 : authors.getChildren("genre"))
                        {
                            genre = ele2.getChildText("genrename");
                            for(int j = 0; j < generos.size(); j++)
                            {
                                if(genre.equalsIgnoreCase(generos.get(j)))
                                {
                                   existe = true;
                                   break;
                                }
                            }
                            if(!existe)
                            {
                                break organizarLoop;
                            }
                            existe = false;
                        }
                    }
                    for(Element ele : rootDoc.getChildren("Book"))
                    {
                        Element authors = ele.getChild("genres");
                        for(Element ele2 : authors.getChildren("genre"))
                        {
                            if(ele2.getChildText("genrename").equalsIgnoreCase(genre))
                            {
                                ele2.setAttribute("id", Integer.toString(i));
                                generos.add(genre);
                            }
                        }
                    }
                }
                
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                FileOutputStream file;
                try
                {
                    file = new FileOutputStream(catalogoLibros);
                    xmlOutput.output(rootDoc, file);
                    file.flush();
                    file.close();
                    return;
                }
                catch (FileNotFoundException ex)
                {
                    System.err.println(ex);
                    return;
                }
                catch (IOException ex)
                {
                    System.err.println(ex);
                    return;
                }
            }
        } catch (JDOMException ex) {
            //
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
