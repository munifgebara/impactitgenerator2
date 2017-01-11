package br.com.munif.impactit.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
@Mojo(name = "gera", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraCadastro extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private String nomePacoteBase;
    private String nomeEntidade;
    private String pasta;
    private Class classeEntidade;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
            nomePacoteBase = nomeCompletoEntidade.toLowerCase().replaceAll("\\.entidades", ".aplicacao");
            pasta = project.getCompileSourceRoots().get(0);
            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);

            getLog().info("ImpactIt");
            getLog().info("Gerando cadastro para " + nomeEntidade);
            getLog().info("Pasta " + pasta);
            getLog().info("Pacote " + nomePacoteBase);

            geraRepository();
            geraService();
            geraController();
            geraApi();

            geraViewLista();
            geraViewEdita();
        } catch (ClassNotFoundException ex) {
            getLog().error(ex);
        }

    }

    private void geraRepository() {
        File f = new File(pasta + "/" + nomePacoteBase.replaceAll("\\.", "/"));
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/" + nomeEntidade + "Repository.java");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(arquivoClasse), "UTF-8");
            String primeiroAtributo = Util.primeiraMaiuscula(Util.primeiroAtributo(classeEntidade).getName());

            fw.write(""
                    + "/*\n"
                    + " * Gerado automaticamente em " + Util.hoje() + " por " + System.getProperty("user.name") + ".\n"
                    + " */\n"
                    + "package " + nomePacoteBase + ";\n"
                    + "\n");

            fw.write(""
                    + "import br.com.impactit.edda.entidades." + nomeEntidade + ";\n"
                    + "import java.util.List;\n"
                    + "import org.springframework.data.jpa.repository.JpaRepository;\n"
                    + "import org.springframework.stereotype.Repository;\n"
                    + "\n"
                    + "@Repository\n"
                    + "public interface " + nomeEntidade + "Repository extends JpaRepository<" + nomeEntidade + ", String>{\n"
                    + "    \n"
                    + "    List<" + nomeEntidade + "> findAllByOrderBy" + primeiroAtributo + "Asc();\n"
                    + "    \n"
                    + "    List<" + nomeEntidade + "> findBy" + primeiroAtributo + "LikeOrderBy" + primeiroAtributo + "Asc(String nome);\n"
                    + "    \n"
                    + "    \n"
                    + "}\n"
                    + ""
                    + "");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraService() {
        File f = new File(pasta + "/" + nomePacoteBase.replaceAll("\\.", "/"));
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/" + nomeEntidade + "Service.java");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(arquivoClasse), "UTF-8");
            String primeiroAtributo = Util.primeiraMaiuscula(Util.primeiroAtributo(classeEntidade).getName());
            fw.write(""
                    + "/*\n"
                    + " * Gerado automaticamente em " + Util.hoje() + " por " + System.getProperty("user.name") + ".\n"
                    + " */\n"
                    + "package " + nomePacoteBase + ";\n"
                    + "\n");

            fw.write(""
                    + "import br.com.impactit.edda.entidades." + nomeEntidade + ";\n"
                    + "import br.com.impactit.framework.ImpactitService;\n"
                    + "import java.util.List;\n"
                    + "import org.springframework.beans.factory.annotation.Autowired;\n"
                    + "import org.springframework.data.jpa.repository.JpaRepository;\n"
                    + "import org.springframework.stereotype.Service;\n"
                    + "import org.springframework.transaction.annotation.Transactional;\n"
                    + "\n"
                    + "@Service\n"
                    + "public class " + nomeEntidade + "Service extends ImpactitService<" + nomeEntidade + "> {\n"
                    + "\n"
                    + "    @Autowired\n"
                    + "    private " + nomeEntidade + "Repository repository;\n"
                    + "\n"
                    + "    @Autowired\n"
                    + "    public " + nomeEntidade + "Service(JpaRepository<" + nomeEntidade + ", String> repository) {\n"
                    + "        super(repository);\n"
                    + "    }\n"
                    + "\n"
                    + "    @Transactional(readOnly = true)\n"
                    + "    @Override\n"
                    + "    public List<" + nomeEntidade + "> findAll() {\n"
                    + "        List<" + nomeEntidade + "> result = repository.findAllByOrderBy" + primeiroAtributo + "Asc();\n"
                    + "        return result;\n"
                    + "    }\n"
                    + "\n"
                    + "    public List<" + nomeEntidade + "> listaFiltrando(String filtro) {\n"
                    + "        return repository.findBy" + primeiroAtributo + "LikeOrderBy" + primeiroAtributo + "Asc(\"%\" + filtro + \"%\");\n"
                    + "    }\n"
                    + "\n"
                    + "}\n"
                    + ""
                    + "");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraController() {
        File f = new File(pasta + "/" + nomePacoteBase.replaceAll("\\.", "/"));
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/" + nomeEntidade + "Controller.java");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(arquivoClasse), "UTF-8");
            fw.write(""
                    + "/*\n"
                    + " * Gerado automaticamente em " + Util.hoje() + " por " + System.getProperty("user.name") + ".\n"
                    + " */\n"
                    + "package " + nomePacoteBase + ";\n"
                    + "\n");

            fw.write(""
                    + "import br.com.impactit.edda.entidades." + nomeEntidade + ";\n"
                    + "import br.com.impactit.framework.ImpactitJSFCrudController;\n"
                    + "import java.util.List;\n"
                    + "import javax.faces.bean.ManagedBean;\n"
                    + "import javax.faces.bean.SessionScoped;"
                    + "\n"
                    + "@ManagedBean\n"
                    + "@SessionScoped\n"
                    + "public class " + nomeEntidade + "Controller extends ImpactitJSFCrudController<" + nomeEntidade + ">{\n"
                    + "\n"
                    + "    public " + nomeEntidade + "Controller() {\n"
                    + "        super(" + nomeEntidade + ".class);\n"
                    + "    }\n"
                    + "\n"
                    + "    @Override\n"
                    + "    public List<" + nomeEntidade + "> listaTodos() {\n"
                    + "        return service.findAll();\n"
                    + "    }\n"
                    + "\n"
                    + "    @Override\n"
                    + "    public List<" + nomeEntidade + "> listaTodosFiltrando() {\n"
                    + "        \n"
                    + "        return ((" + nomeEntidade + "Service)service).listaFiltrando(filtro);\n"
                    + "    }\n"
                    + "\n"
                    + "\n"
                    + "}\n"
                    + ""
                    + "");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraViewLista() {
        String primeiroAtributo = Util.primeiroAtributo(classeEntidade).getName();
        File f = new File(pasta.replaceAll("\\\\", "/").replaceAll("/java", "/webapp") + "/" + nomeEntidade.toLowerCase());
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/lista.xhtml");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(arquivoClasse), "UTF-8");
            fw.write(""
                    + "<?xml version='1.0' encoding='UTF-8' ?>\n"
                    + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                    + "\n"
                    + "\n"
                    + "<ui:composition xmlns:ui=\"http://java.sun.com/jsf/facelets\" xmlns:h=\"http://java.sun.com/jsf/html\" xmlns:p=\"http://primefaces.org/ui\" template=\"/WEB-INF/template.xhtml\">    \n"
                    + "    <ui:define name=\"content\">\n"
                    + "        <h:form id=\"formulario\">\n"
                    + "            <p:panel>\n"
                    + "                <h:outputText value=\"Lista " + nomeEntidade + "s\" />\n"
                    + "                <p:messages id=\"msgs\" showDetail=\"true\" />\n"
                    + "                <br/>\n"
                    + "                <p:panel>\n"
                    + "                    <h:outputText value=\"Filtro\"/>\n"
                    + "                    <p:inputText value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.filtro}\"/>\n"
                    + "                    <p:commandButton actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.filtrar()}\" value=\"Filtrar\" ajax=\"false\" />\n"
                    + "                    <p:commandButton actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.novo()}\" value=\"Novo\" ajax=\"false\" action=\"edita\" />\n"
                    + "                    <br/>\n"
                    + "                    Filtrando por " + primeiroAtributo + ".\n"
                    + "                </p:panel>\n"
                    + "                <p:dataTable value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.lista}\" emptyMessage=\"Nenhum registro corresponde ao filtro.\" var=\"obj\">\n"
                    + "                    <p:column headerText=\"" + Util.primeiraMaiuscula(primeiroAtributo) + "\" sortBy=\"#{obj." + primeiroAtributo + "}\" >\n"
                    + "                        <h:outputText value=\"#{obj." + primeiroAtributo + "}\" />\n"
                    + "                    </p:column>\n"
                    + "\n"
                    + "                    <p:column headerText=\"Editar\" style=\"width: 10%; text-align: center;\">                                               \n"
                    + "                        <p:commandButton id=\"editarLink\" action=\"edita\" actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.editar(obj)}\" icon=\"ui-icon-search\" ajax=\"false\" /> \n"
                    + "                    </p:column>\n"
                    + "                    <p:column style=\"text-align: center\"  headerText=\"Excluir\" >\n"
                    + "                        <p:commandButton icon=\"ui-icon-trash\" update=\"@form\" actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.excluir(obj)}\" onclick=\"if (!confirm('Deseja excluir este registro?')) return false;\"/>\n"
                    + "                    </p:column>\n"
                    + "                </p:dataTable>\n"
                    + "            </p:panel>\n"
                    + "        </h:form>\n"
                    + "    </ui:define>\n"
                    + "</ui:composition>\n"
                    + "");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraViewEdita() {
        String primeiroAtributo = Util.primeiroAtributo(classeEntidade).getName();
        File f = new File(pasta.replaceAll("\\\\", "/").replaceAll("/java", "/webapp") + "/" + nomeEntidade.toLowerCase());
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/edita.xhtml");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(arquivoClasse), "UTF-8");
            fw.write(""
                    + "<?xml version='1.0' encoding='UTF-8' ?>\n"
                    + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                    + "\n"
                    + "<ui:composition xmlns:ui=\"http://java.sun.com/jsf/facelets\" xmlns:h=\"http://java.sun.com/jsf/html\" xmlns:p=\"http://primefaces.org/ui\"\n"
                    + "                template=\"/WEB-INF/template.xhtml\" xmlns:f=\"http://java.sun.com/jsf/core\">\n"
                    + "\n"
                    + "    \n"
                    + "    <ui:define name=\"content\">\n"
                    + "        <p:messages id=\"msgs\" showDetail=\"false\" />\n"
                    + "        <h:form id=\"formulario\">\n"
                    + "            <p:panelGrid style=\"margin-top:20px\">\n"
                    + "                <f:facet name=\"header\">\n"
                    + "                    <p:row>\n"
                    + "                        <p:column colspan=\"6\">Editando " + nomeEntidade + "</p:column>      \n"
                    + "                    </p:row>\n"
                    + "                </f:facet>\n"
                    + "                <p:row>\n"
                    + "                    <p:column><h:outputText value=\"" + Util.primeiraMaiuscula(primeiroAtributo) + ":*\"/></p:column>\n"
                    + "                    <p:column colspan=\"5\">\n"
                    + "                        <p:inputText id=\"" + primeiroAtributo + "\" size=\"50\"  value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.entidade." + primeiroAtributo + "}\" required=\"true\" requiredMessage=\"" + primeiroAtributo + " é obrigatório\"/>\n"
                    + "                        <p:message for=\"" + primeiroAtributo + "\" showDetail=\"true\" />\n"
                    + "                    </p:column>\n"
                    + "                </p:row>\n"
                    + "\n"
                    + "                <f:facet name=\"footer\">\n"
                    + "                    <p:row>\n"
                    + "                        <p:column colspan=\"6\" style=\"text-align: right\">\n"
                    + "                            <p:commandButton actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.salvar()}\" action=\"lista\" update=\"msgs\" ajax=\"false\" value=\"Salvar\"/> \n"
                    + "                            <p:spacer width=\"20\"/>\n"
                    + "                            <p:commandButton actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controller.cancelar()}\" action=\"lista\" update=\"msgs\" ajax=\"false\" value=\"Cancelar\" immediate=\"true\"/> \n"
                    + "                        </p:column>\n"
                    + "                    </p:row>\n"
                    + "                </f:facet>\n"
                    + "            </p:panelGrid>\n"
                    + "        </h:form>\n"
                    + "    </ui:define>\n"
                    + "</ui:composition>"
                    + "");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraViewEditaVelho() {

        File f = new File(pasta.replaceAll("\\\\", "/").replaceAll("/java", "/webapp") + "/" + nomeEntidade.toLowerCase());
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/edita.xhtml");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "<ui:composition xmlns=\"http://www.w3.org/1999/xhtml\"\n"
                    + "                xmlns:h=\"http://java.sun.com/jsf/html\"\n"
                    + "                xmlns:f=\"http://java.sun.com/jsf/core\"\n"
                    + "                xmlns:ui=\"http://java.sun.com/jsf/facelets\"\n"
                    + "                xmlns:p=\"http://primefaces.org/ui\"\n"
                    + "                template=\"/WEB-INF/template.xhtml\">\n"
                    + "\n"
                    + "    <ui:define name=\"content\">\n"
                    + "        <h:outputScript name=\"jquery/jquery.js\" library=\"primefaces\" target=\"head\"/>\n"
                    + "        <h:form>\n"
                    + "            <p:panelGrid styleClass=\"Container100\">\n"
                    + "                <f:facet name=\"header\">\n"
                    + "                    <p:row>\n"
                    + "                        <p:column colspan=\"6\" >\n"
                    + "                            <h:outputText value=\" #{msg.cadastrar} / #{msg.editar} #{msg." + Util.primeiraMinuscula(nomeEntidade) + "}\" />\n"
                    + "                        </p:column>\n"
                    + "                    </p:row>\n"
                    + "                    <p:row>\n"
                    + "                        <p:column colspan=\"6\" >\n"
                    + "                            <p:messages id=\"msgs\" showDetail=\"false\" />\n"
                    + "                        </p:column>\n"
                    + "                    </p:row>\n"
                    + "                </f:facet>\n"
                    + ""
                    + "");

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                fw.write("\n"
                        + "<!--" + atributo.getName() + " " + atributo.getType() + "-->"
                        + "\n"
                        + "                <p:row>\n"
                        + "                    <p:column>\n"
                        + "                        <p:outputLabel for=\"" + atributo.getName() + "\""
                        + " value=\"#{msg." + Util.primeiraMinuscula(nomeEntidade) + "_" + atributo.getName() + "_label}\"  />\n"
                        + "                    </p:column>\n"
                        + "                    <p:column>\n");

                if (atributo.isAnnotationPresent(ManyToOne.class)) {
                    fw.write("                        <p:selectOneMenu id=\"" + atributo.getName() + "\" "
                            + "value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\" effect=\"fade\" converter=\"#{sistemaControlador.converter('" + atributo.getType().getCanonicalName() + "')}\">  \n"
                            + "                            <f:selectItems value=\"#{sistemaControlador.lista('" + atributo.getType().getCanonicalName() + "')}\" var=\"item\" itemLabel=\"#{item}\" itemValue=\"#{item}\"/>  \n"
                            + "                         </p:selectOneMenu>\n"
                    );
                } else if (atributo.isAnnotationPresent(Temporal.class)) {
                    Temporal t = atributo.getAnnotation(Temporal.class);
                    String mascara = "dd/MM/yyyy HH:mm:ss";
                    if (t.value().equals(TemporalType.DATE)) {
                        mascara = "dd/MM/yyyy";
                    }
                    if (t.value().equals(TemporalType.TIME)) {
                        mascara = "HH:mm:ss";
                    }

                    fw.write("                        <p:calendar  id=\"" + atributo.getName() + "\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\" mask=\"true\" navigator=\"true\" pattern=\"" + mascara + "\" effect=\"slide\" locale=\"br\"  />\n");

                } else if (atributo.getType().equals(BigDecimal.class)) {
                    fw.write(""
                            + "                        <p:inputText  id=\"" + atributo.getName() + "\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\"  \n"
                            + "                                      onkeypress=\"mascaraMonetaria(this)\"\n"
                            + "                                      converter=\"moneyConverter\">\n"
                            + "                            <p:ajax event=\"blur\" process=\"@this\" update=\"" + atributo.getName() + "\"/>\n"
                            + "                        </p:inputText>\n"
                    );
                } else if (atributo.getType().isEnum()) {
                    Object[] enumConstants = atributo.getType().getEnumConstants();
                    if (enumConstants.length < 4) {
                        fw.write("                        <p:selectOneRadio id=\"" + atributo.getName() + "\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\" >\n");
                    } else {
                        fw.write("                        <p:selectOneMenu id=\"" + atributo.getName() + "\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\" >\n");
                    }
                    for (Object obj : enumConstants) {
                        fw.write("                        <f:selectItem itemLabel=\"" + obj.toString() + "\" itemValue=\"" + obj + "\" />\n");
                    }
                    if (enumConstants.length < 4) {
                        fw.write("                        </p:selectOneRadio>\n");
                    } else {
                        fw.write("                        </p:selectOneMenu>\n");
                    }
                } else if (atributo.getType().equals(Boolean.class) || atributo.getType().equals(boolean.class)) {
                    fw.write("                        <p:selectBooleanCheckbox id=\"" + atributo.getName() + "\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\" />\n");
                } else {
                    fw.write(""
                            + "                        <p:inputText  id=\"" + atributo.getName() + "\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade." + atributo.getName() + "}\"  requiredMessage=\"#{msg." + Util.primeiraMinuscula(nomeEntidade) + "_" + atributo.getName() + "_required_message}\"  />\n"
                    );
                }
                fw.write(""
                        + "                        <p:message for=\"" + atributo.getName() + "\" showDetail=\"true\"/>\n"
                        + "                    </p:column>\n"
                        + "                </p:row> \n"
                        + ""
                );
            }
            fw.write(""
                    + "\n"
                    + "                <f:facet name=\"footer\" >\n"
                    + "                    <p:row  >\n"
                    + "                        <p:column colspan=\"12\" >\n"
                    + "                            <div style=\"float: right\">\n"
                    + "                                <p:selectBooleanCheckbox id=\"continua\" value=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.continuarInserindo}\" rendered=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade.novo}\"/>\n"
                    + "                            <p:outputLabel for=\"continua\" value=\"#{msg.continuarInserindo}\" rendered=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.entidade.novo}\"/>\n"
                    + "                            <p:spacer width=\"20\"/>\n"
                    + "                            <p:commandButton action=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.salvar()}\" update=\"msgs\" ajax=\"false\" value=\"#{msg.salvar}\" /> \n"
                    + "                            <p:spacer width=\"20\"/>\n"
                    + "                            <p:commandButton actionListener=\"#{" + Util.primeiraMinuscula(nomeEntidade) + "Controlador.cancelar()}\"  action=\"lista\" update=\"msgs\" ajax=\"false\" value=\"#{msg.cancelar}\" immediate=\"true\"/> \n"
                    + "                            </div>\n"
                    + "                        </p:column>\n"
                    + "                    </p:row>\n"
                    + "                </f:facet>\n"
                    + "            </p:panelGrid>\n"
                    + "        </h:form>\n"
                    + "\n"
                    + "    </ui:define>\n"
                    + "\n"
                    + "</ui:composition>"
                    + ""
                    + ""
            );
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraApi() {
        File f = new File(pasta + "/" + nomePacoteBase.replaceAll("\\.", "/"));
        f.mkdirs();
        File arquivoClasse = new File(f.getAbsolutePath() + "/" + nomeEntidade + "API.java");
        getLog().info(arquivoClasse.getAbsolutePath());
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(arquivoClasse), "UTF-8");
            String primeiroAtributo = Util.primeiraMaiuscula(Util.primeiroAtributo(classeEntidade).getName());
            fw.write(""
                    + "/*\n"
                    + " * Gerado automaticamente em " + Util.hoje() + " por " + System.getProperty("user.name") + ".\n"
                    + " */\n"
                    + "package " + nomePacoteBase + ";\n"
                    + "\n");

            fw.write(""
                    + "import "+nomeCompletoEntidade+" ;\n"
                    + "import br.com.impactit.framework.ImpactitApi;\n"
                    + "import javax.servlet.annotation.WebServlet;\n"
                    + "\n"
                    + "\n"
                    + "@WebServlet(name = \""+nomeCompletoEntidade+"API\", urlPatterns = {\"/api/" + nomeEntidade.toLowerCase() + "/*\"})\n"
                    + "public class " + nomeEntidade + "API extends ImpactitApi<" + nomeEntidade + "> {\n"
                    + "\n"
                    + "    @Override\n"
                    + "    public Class<" + nomeEntidade + "> getClazz() {\n"
                    + "        return " + nomeEntidade + ".class;\n"
                    + "    }\n"
                    + "\n"
                    + "}"
                    + "");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}
