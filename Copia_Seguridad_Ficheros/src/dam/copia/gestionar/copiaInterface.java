package dam.copia.gestionar;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface copiaInterface {
    
    public boolean ComprobarListas(List<File> lista1, List<File> lista2, List<String> SHA256Lista1, List<String> SHA256Lista2);
    public List<File> ObtenerFicheros(File file);
    public void ResetLista();
    public void RealizarCopia(File file, File file2);
    public void comprimir(String archivo, String archivoZIP);
    public List<String> ObtenerMD5(List<File> lista) throws NoSuchAlgorithmException;
}
