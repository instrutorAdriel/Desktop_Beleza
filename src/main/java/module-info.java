module org.githubio.desktop_beleza {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;

    opens org.githubio.desktop_beleza to javafx.fxml;
    exports org.githubio.desktop_beleza;
    opens org.githubio.desktop_beleza.controller to javafx.fxml;
    exports org.githubio.desktop_beleza.controller;
    opens org.githubio.desktop_beleza.model to javafx.base;

}