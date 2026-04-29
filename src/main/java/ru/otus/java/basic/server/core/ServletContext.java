package ru.otus.java.basic.server.core;

import ru.otus.java.basic.server.http.HttpMethod;
import ru.otus.java.basic.server.servlet.Servlet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            sortMappings();
        }
    }

    private void sortMappings() {
        mappings.sort(Comparator.comparingInt(ServletMapping::getPriority).thenComparingInt(m -> -m.getPattern().length()));
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

    private record ServletRegistration(String name, Servlet servlet) {}

    private static class ServletMapping {
        private final Servlet servlet;
        private final String pattern;
        private final int priority;
        private final java.util.regex.Pattern compiledPattern;
        private final List<String> paramNames;

        public ServletMapping(Servlet servlet, String pattern) {
            this.servlet = servlet;
            this.pattern = pattern;
            this.priority = calculatePriority(pattern);
            this.paramNames = extractParamNames(pattern);
            this.compiledPattern = compilePattern(pattern);
        }

        public Servlet servlet() {
            return servlet;
        }

        public String getPattern() {
            return pattern;
        }

        public int getPriority() {
            return priority;
        }

        private static int calculatePriority(String pattern) {
            if (pattern.contains("*")) {
                return 2;
            }

            if (pattern.contains("{")) {
                return 1;
            }

            return 0;
        }

        private static List<String> extractParamNames(String pattern) {
            List<String> params = new ArrayList<>();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^/]+)\\}").matcher(pattern);
            while (matcher.find()) {
                params.add(matcher.group(1));
            }

            return Collections.unmodifiableList(params);
        }

        private static java.util.regex.Pattern compilePattern(String pattern) {
            String regex;

            if (pattern.endsWith("/*")) {
                String basePattern = pattern.substring(0, pattern.length() - 2);
                String baseRegex = basePattern.replaceAll("\\{([^/]+)\\}", "([^/]+)");
                regex = "^" + baseRegex + "(?:/(.*))?$";
            } else if (pattern.endsWith("/**")) {
                String basePattern = pattern.substring(0, pattern.length() - 3);
                String baseRegex = basePattern.replaceAll("\\{([^/]+)\\}", "([^/]+)");
                regex = "^" + baseRegex + "(?:/.*)?$";
            } else {
                regex = "^" + pattern.replaceAll("\\{([^/]+)\\}", "([^/]+)") + "$";
            }

            return java.util.regex.Pattern.compile(regex);
        }

        public Map<String, String> match(String path) {
            java.util.regex.Matcher matcher = compiledPattern.matcher(path);

            if (matcher.matches()) {
                Map<String, String> pathVariables = new HashMap<>();

                for (int i = 0; i < paramNames.size(); i++) {
                    String paramName = paramNames.get(i);
                    String paramValue = matcher.group(i + 1);
                    if (paramValue != null) {
                        pathVariables.put(paramName, paramValue);
                    }
                }

                if (pattern.endsWith("/*") && matcher.groupCount() > paramNames.size()) {
                    String wildcardMatch = matcher.group(paramNames.size() + 1);
                    if (wildcardMatch != null && !wildcardMatch.isEmpty()) {
                        pathVariables.put("*", wildcardMatch);
                    }
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