package cl.techk.ext.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class TemplateMailLoader {
	public static <ENTITY> String loadFilledTemplate(ENTITY entity, String templatePath) throws IOException {
		final Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
		final VelocityContext context = new VelocityContext();
		context.put("data", entity);
		final Template template = Velocity.getTemplate(templatePath);
		try (StringWriter writer = new StringWriter()) {
			template.merge(context, writer);
			return writer.toString();
		}
	}
}
