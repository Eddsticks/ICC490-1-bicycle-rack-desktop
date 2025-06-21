module com.icc490.bike.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports com.icc490.bike.desktop;
}
