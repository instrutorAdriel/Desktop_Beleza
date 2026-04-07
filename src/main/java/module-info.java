module org.githubio.desktop_beleza {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.githubio.desktop_beleza to javafx.fxml;
    exports org.githubio.desktop_beleza;
}