package edu.school21.annotations.processors;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("edu.school21.annotations.processors.HtmlForm")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class HtmlProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HtmlForm.class);

        for (Element element : elements) {
            HtmlForm htmlForm = element.getAnnotation(HtmlForm.class);
            List<? extends Element> inputElements = element.getEnclosedElements();

            try {
                writeHTML(htmlForm, inputElements);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    void writeHTML(HtmlForm form, List<? extends Element> inputs)
            throws IOException {
        FileObject fileObject = processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT, "", form.fileName());
        PrintWriter writer = new PrintWriter(fileObject.openWriter());
        writer.println("<form action = \"" + form.action() +
                "\" method = \"" + form.method() + "\">");

        for (Element input : inputs) {
            HtmlInput htmlInput = input.getAnnotation(HtmlInput.class);

            if (htmlInput == null) {
                continue;
            }

            writer.println("\t<input type = \"" + htmlInput.type() +
                    "\" name = \"" + htmlInput.name() +
                    "\" placeholder = \"" + htmlInput.placeholder() + "\">");
        }

        writer.println("\t<input type = \"submit\" value = \"Send\">");
        writer.println("</form>");
        writer.close();
    }
}
