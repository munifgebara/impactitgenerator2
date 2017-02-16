/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.impactit.maven.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author Impact
 */
@Mojo(name = "diagrama", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraDiagrama extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;
    private String pasta;
    private List<Class> entidades;
    private ClassLoader classLoader;

    @Parameter(defaultValue = "${project.basedir}", property = "pastaDestino", required = true)
    private File pastaDestino;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            classLoader = Util.getClassLoader(project);
            entidades = new ArrayList<>();
            pasta = project.getCompileSourceRoots().get(0).replaceAll("\\\\", "/");
            System.out.println("Pasta raiz " + pasta);
            carregaClasses(new File(pasta));
            for (Class c : entidades) {
                getLog().info(c.getCanonicalName());
            }

            processaClasses(entidades);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void carregaClasses(File file) throws ClassNotFoundException {

        File[] arquivos = file.listFiles();
        for (File f : arquivos) {
            if (f.isDirectory()) {
                carregaClasses(f);
            } else {
                String nomeClasse = f.getAbsolutePath().replaceAll("\\\\", "/").replace(pasta, "").replaceAll("/", ".").substring(1);
                if (nomeClasse.endsWith(".java")) {
                    nomeClasse = nomeClasse.substring(0, nomeClasse.lastIndexOf(".java"));

                    try {
                        Class clazz = classLoader.loadClass(nomeClasse);
                        if (clazz.isAnnotationPresent(Entity.class)) {
                            entidades.add(clazz);
                        }
                    } catch (NoClassDefFoundError ex) {
                        System.out.println("###### "+ex.toString());

                    }
                }

            }
        }

    }

    private void processaClasses(List<Class> classes) {
        Map<String, List<Class>> pacotes = pesquisaPacotes(classes);
        getLog().info(" Pacotes " + pacotes.keySet());

        for (String pacote : pacotes.keySet()) {
            List<String> associcaos = new ArrayList<>();
            try {
                File pacoteDot = new File(pastaDestino, pacote.replaceAll("\\.", "_") + ".dot");
                FileWriter fw = new FileWriter(pacoteDot, false);
                escreveCabecalho(fw);
                fw.write("subgraph cluster" + pacote.replaceAll("\\.", "_") + "\n{\n");
                fw.write("label=\"" + pacote + "\";\n");
                for (Class entidade : pacotes.get(pacote)) {
                    associcaos.addAll(criaClasse(entidade, fw));
                }
                fw.write("}\n\n");
                for (String s : associcaos) {
                    fw.write(s + "\n");
                }
                fw.write("\n}\n\n");
                fw.close();
            } catch (Exception ex) {
                getLog().error(ex);
            }
        }

        try {
            Set<String> associcaos = new HashSet<>();
            File pacoteDot = new File(pastaDestino, "allclasses.dot");
            FileWriter fw = new FileWriter(pacoteDot, false);
            escreveCabecalho(fw);
            for (String pacote : pacotes.keySet()) {
                fw.write("subgraph cluster" + pacote.replaceAll("\\.", "_") + "\n{\n");
                fw.write("label=\"" + pacote + "\";\n");
                for (Class entidade : pacotes.get(pacote)) {
                    associcaos.addAll(criaClasse(entidade, fw));
                }
                fw.write("\n}\n\n");

            }
            for (String s : associcaos) {
                fw.write(s + "\n");
            }

            fw.write("\n}\n\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
        pacotes.put("allclasses", null);
        geraArquivoDeLoteShPng(pacotes);
        geraArquivoDeLoteShSvg(pacotes);
        geraArquivoDeLoteBatPng(pacotes);
        geraArquivoDeLoteBatSvg(pacotes);

    }

    public void geraArquivoDeLoteShPng(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2png.sh");
            FileWriter fw = new FileWriter(script, false);
            script.setExecutable(true);
            fw.write("#!/bin/sh\n");
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T png -o " + pacote.replaceAll("\\.", "_") + ".png " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraArquivoDeLoteBatPng(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2png.bat");
            FileWriter fw = new FileWriter(script, false);
            //script.setExecutable(true);
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T png -o " + pacote.replaceAll("\\.", "_") + ".png " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraArquivoDeLoteShSvg(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2svg.sh");
            FileWriter fw = new FileWriter(script, false);
            script.setExecutable(true);
            fw.write("#!/bin/sh\n");
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T svg -o " + pacote.replaceAll("\\.", "_") + ".svg " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraArquivoDeLoteBatSvg(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2svg.bat");
            FileWriter fw = new FileWriter(script, false);
            //script.setExecutable(true);
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T svg -o " + pacote.replaceAll("\\.", "_") + ".svg " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void escreveCabecalho(FileWriter fileWriter) throws IOException {
        fileWriter.write("//Gerado automaticamente por plugin da www.gumga.com.br munif@munifgebara.com.br\n\n"
                + ""
                + "digraph G{\n"
                + "fontname = \"Bitstream Vera Sans\"\n"
                + "fontsize = 8\n\n"
                + "node [\n"
                + "        fontname = \"Bitstream Vera Sans\"\n"
                + "        fontsize = 8\n"
                + "        shape = \"record\"\n"
                + "]\n\n"
                + "edge [\n"
                + "        fontname = \"Bitstream Vera Sans\"\n"
                + "        fontsize = 8\n"
                + "]\n\n");
    }

    private List<String> criaClasse(Class entidade, FileWriter fw) throws Exception {
        List<String> associacoes = new ArrayList<String>();

        if (!entidade.getSuperclass().equals(Object.class)) {
            if (entidade.getSuperclass().getSimpleName().equals("ImpactitEntity")) {
                //COLOCAR apenas uma marca
            } else {
                associacoes.add("edge [ arrowhead = \"empty\" headlabel = \"\" taillabel = \"\"] " + entidade.getSimpleName() + " -> " + entidade.getSuperclass().getSimpleName());
            }
        }

        String cor = "";
        fw.write(entidade.getSimpleName() + " [" + cor + "label = \"{" + entidade.getSimpleName() + "|");
        Field atributos[] = entidade.getDeclaredFields();
        int i = 0;
        Set<String> metodosExcluidosDoDiagrama = new HashSet<>();
        metodosExcluidosDoDiagrama.add("equals");
        metodosExcluidosDoDiagrama.add("hashCode");
        metodosExcluidosDoDiagrama.add("toString");

        for (Field f : atributos) {
            i++;
            Class tipoAtributo = f.getType();
            String tipo = tipoAtributo.getSimpleName();
            String nomeAtributo = f.getName();
            String naA = nomeAtributo.substring(0, 1).toUpperCase() + nomeAtributo.substring(1);
            metodosExcluidosDoDiagrama.add("set" + naA);
            metodosExcluidosDoDiagrama.add("get" + naA);

            if ((f.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }

            if (f.getType().equals(List.class) || f.getType().equals(Set.class) || f.getType().equals(Map.class)) {
                ParameterizedType type = (ParameterizedType) f.getGenericType();
                Type[] typeArguments = type.getActualTypeArguments();
                Class tipoGenerico = (Class) typeArguments[f.getType().equals(Map.class) ? 1 : 0];

                if (f.isAnnotationPresent(ManyToMany.class)) {
                    ManyToMany mm = f.getAnnotation(ManyToMany.class);
                    if (!mm.mappedBy().isEmpty()) {
                        continue;
                    }
                    associacoes.add("edge [arrowhead = \"none\" headlabel = \"*\" taillabel = \"*\"] " + entidade.getSimpleName() + " -> " + tipoGenerico.getSimpleName() + " [label = \"" + nomeAtributo + "\"]");
                } else if (f.isAnnotationPresent(OneToMany.class)) {
                    OneToMany oo = f.getAnnotation(OneToMany.class);
                    if (!oo.mappedBy().isEmpty()) {
                        continue;
                    }
                    associacoes.add("edge [arrowhead = \"none\" headlabel = \"*\" taillabel = \"1\"] " + entidade.getSimpleName() + " -> " + tipoGenerico.getSimpleName() + " [label = \"" + nomeAtributo + "\"]");
                } else {
                }

            } else if (f.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne mo = f.getAnnotation(ManyToOne.class);
                associacoes.add("edge [arrowhead = \"none\" headlabel = \"1\" taillabel = \"*\"] " + entidade.getSimpleName() + " -> " + tipo + " [label = \"" + nomeAtributo + "\"]");
            } else if (f.isAnnotationPresent(OneToOne.class)) {
                OneToOne oo = f.getAnnotation(OneToOne.class);
                if (!oo.mappedBy().isEmpty()) {
                    continue;
                }
                associacoes.add("edge [arrowhead = \"none\" headlabel = \"1\" taillabel = \"1\"] " + entidade.getSimpleName() + " -> " + tipo + " [label = \"" + nomeAtributo + "\"]");

            } else {
                fw.write(nomeAtributo + ":" + tipo + "\\l");
            }
        }

        fw.write("|");
        Method metodos[] = entidade.getDeclaredMethods();
        for (Method m : metodos) {
            if (!metodosExcluidosDoDiagrama.contains(m.getName())) {
                fw.write(m.getName() + ":" + m.getReturnType().getSimpleName() + "\\l");
            }
        }
        fw.write("}\"]\n");
        return associacoes;
    }

    public Map<String, List<Class>> pesquisaPacotes(List<Class> classes) {
        Map<String, List<Class>> pacotes = new HashMap<String, List<Class>>();
        for (Class classe : classes) {
            String nomeClasse = classe.getName();
            String pacote = nomeClasse.substring(0, nomeClasse.lastIndexOf('.'));
            List<Class> lista = pacotes.get(pacote);
            if (lista == null) {
                lista = new ArrayList<Class>();
                pacotes.put(pacote, lista);
            }
            lista.add(classe);
        }
        return pacotes;
    }

}
