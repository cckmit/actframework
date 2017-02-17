package act.view;

import act.app.ActionContext;
import act.util.ActContext;
import org.osgl.http.H;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.osgl.http.H.Format.*;

/**
 * Resolve template path for {@link ActContext}
 */
public class TemplatePathResolver {

    private static final Set<H.Format> supportedFormats = new HashSet<>();

    public final String resolve(ActContext context) {
        String path = context.templatePath();
        return resolveTemplatePath(path, context);
    }

    public final String resolveWithContextMethodPath(ActContext context) {
        String methodPath = context.methodPath();
        String path = context.templatePath();
        String[] sa = path.split("\\.");
        int level = sa.length + 1;
        StringBuilder sb;
        if (S.notBlank(methodPath)) {
            while (--level > 0) {
                methodPath = S.beforeLast(methodPath, ".");
            }
            sb = S.builder(methodPath);
        } else {
            sb = S.builder();
        }
        if (!path.startsWith("/")) {
            sb.append("/");
        }
        path = sb.append(path).toString().replace('.', '/');
        return resolveTemplatePath(path, context);
    }

    /**
     * Sub class shall use this method to implement template path resolving logic.
     *
     * The default implementation will append the template file suffix based on the
     * {@link ActionContext#accept() accepted format}. E.g. if accepted format is
     * `text/html`, then it will append `.html` to the path specified if suffix
     * is not presented in the path
     *
     * @param path the template path string
     * @param context the act context
     * @return the template path amended by the logic described above
     */
    protected String resolveTemplatePath(String path, ActContext context) {
        return amendSuffix(path, context);
    }

    protected String amendSuffix(String path, ActContext context) {
        if (path.contains(".")) {
            return path;
        }
        H.Format fmt = context.accept();
        if (UNKNOWN == fmt) {
            fmt = HTML;
        }
        if (isAcceptFormatSupported(fmt)) {
            return S.builder(path).append(".").append(fmt.name()).toString();
        }
        throw E.unsupport("Request accept not supported: %s", fmt);
    }

    public static void registerSupportedFormats(H.Format ... fmts) {
        supportedFormats.addAll(C.listOf(fmts));
    }

    public static void registerSupportedFormats(Collection<H.Format> fmts) {
        supportedFormats.addAll(fmts);
    }

    public static boolean isAcceptFormatSupported(H.Format fmt) {
        return (UNKNOWN == fmt || HTML == fmt || JSON == fmt || XML == fmt || TXT == fmt || CSV == fmt) || supportedFormats.contains(fmt);
    }
}
