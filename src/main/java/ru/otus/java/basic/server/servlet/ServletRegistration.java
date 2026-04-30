package ru.otus.java.basic.server.servlet;

public record ServletRegistration(String name, Servlet servlet, String mapping) {}