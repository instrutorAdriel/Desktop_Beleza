module org.githubio.desktop_beleza {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;


    opens org.githubio.desktop_beleza to javafx.fxml;
    exports org.githubio.desktop_beleza;
    exports Model;
    opens Model to javafx.fxml;
    exports Controller;
    opens Controller to javafx.fxml;
}