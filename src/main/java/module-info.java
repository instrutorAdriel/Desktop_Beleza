module org.githubio.desktop_beleza {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires spring.security.crypto;
    requires java.desktop;
    requires java.dotenv;
    requires atlantafx.base;
    requires mysql.connector.j;
    requires jakarta.mail;

    opens org.githubio.desktop_beleza to javafx.fxml;
    exports org.githubio.desktop_beleza;
    exports org.githubio.desktop_beleza.config;
    opens org.githubio.desktop_beleza.config to javafx.fxml;
    exports org.githubio.desktop_beleza.controller;
    opens org.githubio.desktop_beleza.controller to javafx.fxml;
    exports org.githubio.desktop_beleza.model;
    opens org.githubio.desktop_beleza.model to javafx.fxml;
    exports org.githubio.desktop_beleza.components;
    opens org.githubio.desktop_beleza.components to javafx.fxml;
    exports org.githubio.desktop_beleza.model.dao;
    opens org.githubio.desktop_beleza.model.dao to javafx.fxml;
    exports org.githubio.desktop_beleza.service;
    opens org.githubio.desktop_beleza.service to javafx.fxml;
}