/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.impactit.maven.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author Impact
 */
public class Util {

    public final static String IDENTACAO4 = "    ";
    public final static String IDENTACAO8 = "        ";
    public final static String IDENTACAO12 = "            ";
    public final static String IDENTACAO16 = "                ";

    public final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static String primeiraMaiuscula(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static String primeiraMinuscula(String s) {
        return s.substring(0, 1).toLowerCase().concat(s.substring(1));
    }

    public static List<Field> getTodosAtributosMenosIdAutomatico(Class classe) {
        List<Field> todosAtributos = getTodosAtributosNaoEstaticos(classe);
        List<Field> aRemover = new ArrayList<>();
        for (Field f : todosAtributos) {
            if ("id".equals(f.getName())) {
                aRemover.add(f);
            }
            if ("ti".equals(f.getName())) {
                aRemover.add(f);
            }
            if ("cd".equals(f.getName())) {
                aRemover.add(f);
            }
            if ("version".equals(f.getName())) {
                aRemover.add(f);
            }
        }
        todosAtributos.removeAll(aRemover);
        return todosAtributos;
    }

    public static List<Field> getTodosAtributosNaoEstaticos(Class classe) throws SecurityException {
        List<Field> aRetornar = new ArrayList<>();
        List<Field> estaticos = new ArrayList<>();
        if (!classe.getSuperclass().equals(Object.class)) {
            aRetornar.addAll(getTodosAtributosNaoEstaticos(classe.getSuperclass()));
        }
        aRetornar.addAll(Arrays.asList(classe.getDeclaredFields()));
        for (Field f : aRetornar) {
            if (Modifier.isStatic(f.getModifiers())) {
                estaticos.add(f);
            }
        }
        aRetornar.removeAll(estaticos);
        return aRetornar;
    }

    public static ClassLoader getClassLoader(MavenProject project) {
        ClassLoader aRetornar = null;
        try {
            List elementos = new ArrayList();
            elementos.addAll(project.getRuntimeClasspathElements());
            elementos.addAll(project.getTestClasspathElements());

            URL[] runtimeUrls = new URL[elementos.size()];
            for (int i = 0; i < elementos.size(); i++) {
                String element = (String) elementos.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            aRetornar = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return aRetornar;
    }

    public static Field primeiroAtributo(Class classe) {
        return getTodosAtributosMenosIdAutomatico(classe).get(0);
    }

    public static Class getTipoGenerico(Field atributo) {
        ParameterizedType type = (ParameterizedType) atributo.getGenericType();
        Type[] typeArguments = type.getActualTypeArguments();
        Class tipoGenerico = (Class) typeArguments[atributo.getType().equals(Map.class) ? 1 : 0];
        return tipoGenerico;
    }

    public static String etiqueta(Field atributo) {
        return primeiraMaiuscula(atributo.getDeclaringClass().getSimpleName() + "_label_" + atributo.getName());
    }

    public static String windowsSafe(String s) {
        return s.replaceAll("\\\\", "/");
    }

    public static void adicionaLinha(String nomeArquivo, String linhaMarcador, String linhaNova) throws IOException {
        String arquivo = nomeArquivo;
        String arquivoTmp = nomeArquivo + "-tmp";

        BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoTmp));
        BufferedReader reader = new BufferedReader(new FileReader(arquivo));

        String linha;
        boolean colocou = false;
        while ((linha = reader.readLine()) != null) {
            if (linha.contains(linhaMarcador) && !colocou) {
                writer.write(linhaNova + "\n");
                colocou = true;
            }
            writer.write(linha + "\n");
        }

        writer.close();
        reader.close();

        new File(arquivo).delete();
        new File(arquivoTmp).renameTo(new File(arquivo));
    }

    public static String todosAtributosSeparadosPorVirgula(Class classeEntidade) {
        StringBuilder sb = new StringBuilder();
        for (Field f : getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            sb.append(f.getName()).append(",");

        }
        sb.setLength(sb.length() - 1);
        return sb.toString().replace("oi,", "").replaceAll("vs", "");
    }

    public static String hoje() {
        return sdf.format(new Date());
    }

}
