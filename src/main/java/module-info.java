module org.githubio.desktop_beleza {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;

    opens org.githubio.desktop_beleza to javafx.fxml;
    exports org.githubio.desktop_beleza;
    exports org.githubio.desktop_beleza.config;
    opens org.githubio.desktop_beleza.config to javafx.fxml;
}