package dam.copia.gestionar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class copia implements copiaInterface {

    private static copiaInterface dao = null;
    List<File> lista = new ArrayList<>();
    boolean correcto = true;

    String codigo1 = "";
    String codigo = "";
    List<String> SHA256Lista;

    public static copiaInterface getInstance() {
        if (dao == null) {
            dao = new copia();
        }
        return dao;
    }

    @Override
    public List<File> ObtenerFicheros(File entrada) {
        if (!entrada.exists()) {
            System.out.println(entrada.getName() + " no encontrado.");
        } else if (entrada.isFile()) {
            lista.add(entrada.getAbsoluteFile());
        } else if (entrada.isDirectory()) {
            lista.add(entrada.getAbsoluteFile());
            File[] files = entrada.listFiles();
            if (files.length > 0) {
                for (File f : files) {
                    ObtenerFicheros(f);
                }
            }
        }
        return lista;
    }

    @Override
    public boolean ComprobarListas(List<File> lista1, List<File> lista2, List<String> SHA256Lista1, List<String> SHA256Lista2) {
        if (lista1.size() == lista2.size()) {
            if(SHA256Lista1.size() == SHA256Lista2.size()){
                for (int i = 0; i < SHA256Lista1.size(); i++) {
                    if(!SHA256Lista1.get(i).equals(SHA256Lista2.get(i))){
                        return false;
                    }
                }
            }

        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<String> ObtenerMD5(List<File> lista) throws NoSuchAlgorithmException {
        SHA256Lista = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            if(lista.get(i).isFile()){
                try (DigestInputStream dis = new DigestInputStream(new FileInputStream(lista.get(i)), md)) {
                while (dis.read() != -1) ; //empty loop to clear the data
                md = dis.getMessageDigest();
            } catch (IOException ex) {
                Logger.getLogger(copia.class.getName()).log(Level.SEVERE, null, ex);
            }
            StringBuilder result = new StringBuilder();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }
            SHA256Lista.add(result.toString());
            }
        }

        return SHA256Lista;
    }

    @Override
    public void ResetLista() {
        lista = new ArrayList<>();
    }

    @Override
    public void RealizarCopia(File source, File destination) {

        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            String[] files = source.list();

            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                RealizarCopia(srcFile, destFile);
            }
        } else {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception e) {

                try {
                    in.close();
                    out.close();
                    correcto = false;
                } catch (IOException e1) {
                    correcto = false;
                }
            }
        }
    }

    @Override
    public void comprimir(String archivo, String archivoZIP) {
        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archivoZIP));
            agregarCarpeta("", archivo, zip);
            zip.flush();
            zip.close();

        } catch (IOException ex) {
            Logger.getLogger(copia.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(copia.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void agregarCarpeta(String ruta, String carpeta, ZipOutputStream zip) throws Exception {
        File directorio = new File(carpeta);
        for (String nombreArchivo : directorio.list()) {
            if (ruta.equals("")) {
                agregarArchivo(directorio.getName(), carpeta + "/" + nombreArchivo, zip);
            } else {
                agregarArchivo(ruta + "/" + directorio.getName(), carpeta + "/" + nombreArchivo, zip);
            }
        }
    }

    public void agregarArchivo(String ruta, String directorio, ZipOutputStream zip) throws Exception {
        File archivo = new File(directorio);
        if (archivo.isDirectory()) {
            agregarCarpeta(ruta, directorio, zip);
        } else {
            byte[] buffer = new byte[4096];
            int leido;
            FileInputStream entrada = new FileInputStream(archivo);
            zip.putNextEntry(new ZipEntry(ruta + "/" + archivo.getName()));
            while ((leido = entrada.read(buffer)) > 0) {
                zip.write(buffer, 0, leido);
            }
        }
    }

}
