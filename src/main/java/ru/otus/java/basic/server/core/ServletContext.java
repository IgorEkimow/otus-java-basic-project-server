package ru.otus.java.basic.server.core;

import ru.otus.java.basic.server.http.HttpMethod;
import ru.otus.java.basic.server.servlet.Servlet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ServletContext {
    private final Map<String, ServletRegistration> servlets = new ConcurrentHashMap<>();
    private final List<ServletMapping> mappings = new ArrayList<>();

    public void addServlet(String name, Servlet servlet) {
        servlets.put(name, new ServletRegistration(name, servlet));
        servlet.init();
    }

    public void addMapping(String servletName, String urlPattern) {
        ServletRegistration registration = servlets.get(servletName);
        if (registration != null) {
            Servlet servlet = registration.servlet();
            mappings.add(new ServletMapping(servlet, urlPattern));
        }
    }

    public Optional<ServletMatch> findServlet(String path) {
        for (ServletMapping mapping : mappings) {
            Map<String, String> pathVariables = mapping.match(path);
            if (pathVariables != null) {
                return Optional.of(new ServletMatch(mapping.servlet(), pathVariables));
            }
        }
        return Optional.empty();
    }

    public boolean hasServletForPath(String path) {
        return mappings.stream().anyMatch(m -> m.match(path) != null);
    }

    public Set<HttpMethod> getAllowedMethods(String path) {
        return Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);
    }

    public void destroy() {
        servlets.values().forEach(reg -> reg.servlet().destroy());
    }

    private record ServletRegistration(String name, Servlet servlet) {
    }

    private static class ServletMapping {
        private final Servlet servlet;
        private final String pattern;

        public ServletMapping(Servlet servlet, String pattern) {
            this.servlet = servlet;
            this.pattern = pattern;
        }

        public Servlet servlet() {
            return servlet;
        }

        public Map<String, String> match(String path) {
            String regex = pattern.replaceAll("\\{[^/]+\\}", "([^/]+)");

            if (pattern.endsWith("/*")) {
                String basePattern = pattern.substring(0, pattern.length() - 2);
                regex = basePattern.replaceAll("\\{[^/]+\\}", "([^/]+)");

                if (path.equals(basePattern) || path.equals(basePattern + "/")) {
                    return new HashMap<>();
                }

                regex = basePattern.replaceAll("\\{[^/]+\\}", "([^/]+)") + "/(.*)";
            }

            java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = compiledPattern.matcher(path);

            if (matcher.matches()) {
                Map<String, String> pathVariables = new HashMap<>();
                java.util.regex.Matcher paramMatcher = java.util.regex.Pattern.compile("\\{([^/]+)\\}").matcher(pattern);

                int groupIndex = 1;
                while (paramMatcher.find()) {
                    if (groupIndex <= matcher.groupCount()) {
                        String paramName = paramMatcher.group(1);
                        String paramValue = matcher.group(groupIndex);
                        pathVariables.put(paramName, paramValue);
                    }
                    groupIndex++;
                }

                return pathVariables;
            }

            return null;
        }
    }

    public record ServletMatch(Servlet servlet, Map<String, String> pathVariables) {
        public String getName() {
            return servlet.getClass().getSimpleName();
        }
    }
}